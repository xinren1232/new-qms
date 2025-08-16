const ChatHistoryDB = require('./chat-history-db');
const PostgreSQLDatabase = require('./postgresql-db');
const RedisCache = require('./redis-cache');
const DatabasePerformanceOptimizer = require('./performance-optimizer');
const OptimizedPoolManager = require('./optimized-pool-manager');

class DatabaseAdapter {
  constructor() {
    this.primaryDB = null;
    this.cache = null;
    this.dbType = process.env.DB_TYPE || 'sqlite'; // 'sqlite' 或 'postgresql'
    this.cacheEnabled = process.env.CACHE_ENABLED !== 'false';
    this.performanceOptimizer = null;
    this.poolManager = null; // 新增：优化的连接池管理器
  }

  // 初始化数据库和缓存
  async initialize() {
    try {
      console.log(`🔄 初始化数据库适配器 (类型: ${this.dbType})`);

      // 初始化主数据库
      if (this.dbType === 'postgresql') {
        // 初始化优化的连接池管理器
        this.poolManager = new OptimizedPoolManager();
        await this.poolManager.initialize();

        this.primaryDB = new PostgreSQLDatabase();
        // 将连接池管理器传递给PostgreSQL数据库
        this.primaryDB.setPoolManager(this.poolManager);
        const dbSuccess = await this.primaryDB.initialize();
        if (!dbSuccess) {
          console.warn('⚠️ PostgreSQL初始化失败，回退到SQLite');
          this.dbType = 'sqlite';
          this.primaryDB = new ChatHistoryDB();
          this.primaryDB.initTables();
          // 关闭连接池管理器
          if (this.poolManager) {
            await this.poolManager.close();
            this.poolManager = null;
          }
        }
      } else {
        this.primaryDB = new ChatHistoryDB();
        this.primaryDB.initTables();
      }

      // 初始化Redis缓存（带超时）
      if (this.cacheEnabled) {
        this.cache = new RedisCache();
        try {
          const initPromise = this.cache.initialize();
          const timeoutPromise = new Promise((_, reject) =>
            setTimeout(() => reject(new Error('Redis初始化超时')), 5000)
          );

          const cacheSuccess = await Promise.race([initPromise, timeoutPromise]);
          if (!cacheSuccess) {
            console.warn('⚠️ Redis初始化失败，禁用缓存功能');
            this.cacheEnabled = false;
            this.cache = null;
          }
        } catch (error) {
          console.warn('⚠️ Redis初始化超时或失败，禁用缓存功能:', error.message);
          this.cacheEnabled = false;
          this.cache = null;
        }
      }

      // 初始化性能优化器
      this.performanceOptimizer = new DatabasePerformanceOptimizer(this);

      // 创建性能索引（仅SQLite）
      if (this.dbType === 'sqlite') {
        await this.performanceOptimizer.createOptimizedIndexes();
      }

      console.log(`✅ 数据库适配器初始化完成`);
      console.log(`📍 主数据库: ${this.dbType}`);
      console.log(`📍 缓存系统: ${this.cacheEnabled ? 'Redis' : '禁用'}`);
      console.log(`📍 性能优化: 已启用`);

      return true;
    } catch (error) {
      console.error('❌ 数据库适配器初始化失败:', error.message);
      return false;
    }
  }

  // 添加通用的数据库操作方法
  async get(sql, params = []) {
    if (this.dbType === 'postgresql') {
      const result = await this.primaryDB.query(sql, params);
      return result.rows[0];
    } else {
      return new Promise((resolve, reject) => {
        this.primaryDB.db.get(sql, params, (err, row) => {
          if (err) reject(err);
          else resolve(row);
        });
      });
    }
  }

  async run(sql, params = []) {
    if (this.dbType === 'postgresql') {
      return await this.primaryDB.query(sql, params);
    } else {
      return new Promise((resolve, reject) => {
        this.primaryDB.db.run(sql, params, function(err) {
          if (err) reject(err);
          else resolve({ changes: this.changes, lastID: this.lastID });
        });
      });
    }
  }

  // 通用查询方法，支持SELECT、INSERT、UPDATE、DELETE
  async query(sql, params = []) {
    if (this.dbType === 'postgresql') {
      const result = await this.primaryDB.query(sql, params);
      return result.rows;
    } else {
      // 判断SQL类型
      const sqlType = sql.trim().toUpperCase().split(' ')[0];

      if (sqlType === 'SELECT') {
        return new Promise((resolve, reject) => {
          this.primaryDB.db.all(sql, params, (err, rows) => {
            if (err) reject(err);
            else resolve(rows || []);
          });
        });
      } else {
        // INSERT, UPDATE, DELETE
        return new Promise((resolve, reject) => {
          this.primaryDB.db.run(sql, params, function(err) {
            if (err) reject(err);
            else resolve({ changes: this.changes, lastID: this.lastID });
          });
        });
      }
    }
  }

  // 保存对话
  async saveConversation(conversationData) {
    try {
      let result;
      
      if (this.dbType === 'postgresql') {
        result = await this.primaryDB.saveConversation(conversationData);
      } else {
        // SQLite版本
        const { id, user_id, title, model_provider, model_name } = conversationData;
        result = await new Promise((resolve, reject) => {
          this.primaryDB.db.run(
            `INSERT OR REPLACE INTO chat_conversations
             (id, user_id, title, model_provider, model_name, updated_at)
             VALUES (?, ?, ?, ?, ?, datetime('now'))`,
            [id, user_id, title, model_provider, model_name],
            function(err) {
              if (err) reject(err);
              else resolve({ id, user_id, title, model_provider, model_name });
            }
          );
        });
      }

      // 清除相关缓存
      if (this.cacheEnabled && this.cache) {
        await this.cache.clearUserCache(conversationData.user_id);
      }

      return result;
    } catch (error) {
      console.error('❌ 保存对话失败:', error.message);
      throw error;
    }
  }

  // 保存消息
  async saveMessage(messageData) {
    try {
      let result;

      if (this.dbType === 'postgresql') {
        result = await this.primaryDB.saveMessage(messageData);
      } else {
        // SQLite版本
        const { 
          id, conversation_id, user_id, message_type, content,
          model_info, response_time, rating, feedback 
        } = messageData;
        
        result = await new Promise((resolve, reject) => {
          this.primaryDB.db.run(
            `INSERT INTO chat_messages
             (id, conversation_id, user_id, message_type, content, model_info, response_time, rating, feedback, created_at, updated_at)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now'), datetime('now'))`,
            [
              id, conversation_id, user_id, message_type, content,
              JSON.stringify(model_info || {}),
              response_time, rating, feedback
            ],
            function(err) {
              if (err) reject(err);
              else resolve({ id, conversation_id, user_id, message_type, content });
            }
          );
        });
      }

      // 清除相关缓存
      if (this.cacheEnabled && this.cache) {
        await this.cache.clearUserCache(messageData.user_id);
      }

      return result;
    } catch (error) {
      console.error('❌ 保存消息失败:', error.message);
      throw error;
    }
  }

  // 获取用户对话列表
  async getUserConversations(userId, limit = 20, offset = 0) {
    try {
      // 尝试从缓存获取
      if (this.cacheEnabled && this.cache && offset === 0) {
        const cached = await this.cache.getUserConversations(userId);
        if (cached) {
          return cached.slice(0, limit);
        }
      }

      let result;

      if (this.dbType === 'postgresql') {
        result = await this.primaryDB.getUserConversations(userId, limit, offset);
      } else {
        // SQLite版本
        result = await new Promise((resolve, reject) => {
          this.primaryDB.db.all(
            `SELECT c.*, COUNT(m.id) as message_count
             FROM chat_conversations c
             LEFT JOIN chat_messages m ON c.id = m.conversation_id AND m.is_deleted = 0
             WHERE c.user_id = ? AND c.is_deleted = 0
             GROUP BY c.id
             ORDER BY c.updated_at DESC
             LIMIT ? OFFSET ?`,
            [userId, limit, offset],
            (err, rows) => {
              if (err) reject(err);
              else resolve(rows || []);
            }
          );
        });
      }

      // 缓存结果（仅首页）
      if (this.cacheEnabled && this.cache && offset === 0) {
        await this.cache.cacheUserConversations(userId, result);
      }

      return result;
    } catch (error) {
      console.error('❌ 获取用户对话列表失败:', error.message);
      return [];
    }
  }

  // 获取对话消息
  async getConversationMessages(conversationId, limit = 50, offset = 0) {
    try {
      if (this.dbType === 'postgresql') {
        return await this.primaryDB.getConversationMessages(conversationId, limit, offset);
      } else {
        // SQLite版本
        return await new Promise((resolve, reject) => {
          this.primaryDB.db.all(
            `SELECT * FROM chat_messages
             WHERE conversation_id = ? AND is_deleted = 0
             ORDER BY created_at ASC
             LIMIT ? OFFSET ?`,
            [conversationId, limit, offset],
            (err, rows) => {
              if (err) reject(err);
              else resolve(rows || []);
            }
          );
        });
      }
    } catch (error) {
      console.error('❌ 获取对话消息失败:', error.message);
      return [];
    }
  }

  // ==================== 用户管理方法 ====================

  // 根据ID获取用户
  async getUserById(userId) {
    try {
      if (this.dbType === 'postgresql') {
        const result = await this.primaryDB.query('SELECT * FROM users WHERE id = $1', [userId]);
        return result.rows[0] || null;
      } else {
        // SQLite版本
        return await new Promise((resolve, reject) => {
          this.primaryDB.db.get(
            'SELECT * FROM users WHERE id = ?',
            [userId],
            (err, row) => {
              if (err) reject(err);
              else resolve(row || null);
            }
          );
        });
      }
    } catch (error) {
      console.error('❌ 获取用户失败:', error.message);
      return null;
    }
  }

  // 根据用户名获取用户
  async getUserByUsername(username) {
    try {
      if (this.dbType === 'postgresql') {
        const result = await this.primaryDB.query('SELECT * FROM users WHERE username = $1', [username]);
        return result.rows[0] || null;
      } else {
        // SQLite版本
        return await new Promise((resolve, reject) => {
          this.primaryDB.db.get(
            'SELECT * FROM users WHERE username = ?',
            [username],
            (err, row) => {
              if (err) reject(err);
              else resolve(row || null);
            }
          );
        });
      }
    } catch (error) {
      console.error('❌ 根据用户名获取用户失败:', error.message);
      return null;
    }
  }

  // 根据邮箱获取用户
  async getUserByEmail(email) {
    try {
      if (this.dbType === 'postgresql') {
        const result = await this.primaryDB.query('SELECT * FROM users WHERE email = $1', [email]);
        return result.rows[0] || null;
      } else {
        // SQLite版本
        return await new Promise((resolve, reject) => {
          this.primaryDB.db.get(
            'SELECT * FROM users WHERE email = ?',
            [email],
            (err, row) => {
              if (err) reject(err);
              else resolve(row || null);
            }
          );
        });
      }
    } catch (error) {
      console.error('❌ 根据邮箱获取用户失败:', error.message);
      return null;
    }
  }

  // 创建用户
  async createUser(userData) {
    try {
      const {
        id, username, email, password, role, department,
        permissions, status, created_at
      } = userData;

      if (this.dbType === 'postgresql') {
        const query = `
          INSERT INTO users (id, username, email, password, role, department, permissions, status, created_at)
          VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
          RETURNING *
        `;
        const result = await this.primaryDB.query(query, [
          id, username, email, password, role, department,
          JSON.stringify(permissions), status, created_at
        ]);
        return result.rows[0];
      } else {
        // SQLite版本
        return await new Promise((resolve, reject) => {
          this.primaryDB.db.run(
            `INSERT INTO users (id, username, email, password, role, department, permissions, status, created_at)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
            [id, username, email, password, role, department, JSON.stringify(permissions), status, created_at],
            function(err) {
              if (err) reject(err);
              else resolve({ id, username, email, role, department, status });
            }
          );
        });
      }
    } catch (error) {
      console.error('❌ 创建用户失败:', error.message);
      throw error;
    }
  }

  // 删除用户
  async deleteUser(userId) {
    try {
      if (this.dbType === 'postgresql') {
        await this.primaryDB.query('DELETE FROM users WHERE id = $1', [userId]);
      } else {
        // SQLite版本
        return await new Promise((resolve, reject) => {
          this.primaryDB.db.run(
            'DELETE FROM users WHERE id = ?',
            [userId],
            function(err) {
              if (err) reject(err);
              else resolve(this.changes);
            }
          );
        });
      }
    } catch (error) {
      console.error('❌ 删除用户失败:', error.message);
      throw error;
    }
  }

  // 更新用户最后登录时间
  async updateUserLastLogin(userId) {
    try {
      const lastLoginAt = new Date().toISOString();

      if (this.dbType === 'postgresql') {
        await this.primaryDB.query(
          'UPDATE users SET last_login_at = $1 WHERE id = $2',
          [lastLoginAt, userId]
        );
      } else {
        // SQLite版本
        return await new Promise((resolve, reject) => {
          this.primaryDB.db.run(
            'UPDATE users SET last_login_at = ? WHERE id = ?',
            [lastLoginAt, userId],
            function(err) {
              if (err) reject(err);
              else resolve(this.changes);
            }
          );
        });
      }
    } catch (error) {
      console.error('❌ 更新用户登录时间失败:', error.message);
    }
  }

  // 搜索消息
  async searchMessages(userId, searchText, limit = 50) {
    try {
      if (this.dbType === 'postgresql') {
        return await this.primaryDB.searchMessages(userId, searchText, limit);
      } else {
        // SQLite版本（简单文本搜索）
        return await new Promise((resolve, reject) => {
          this.primaryDB.db.all(
            `SELECT m.*, c.title as conversation_title
             FROM chat_messages m
             JOIN chat_conversations c ON m.conversation_id = c.id
             WHERE m.user_id = ? AND m.is_deleted = 0 AND c.is_deleted = 0
               AND m.content LIKE ?
             ORDER BY m.created_at DESC
             LIMIT ?`,
            [userId, `%${searchText}%`, limit],
            (err, rows) => {
              if (err) reject(err);
              else resolve(rows || []);
            }
          );
        });
      }
    } catch (error) {
      console.error('❌ 搜索消息失败:', error.message);
      return [];
    }
  }

  // 获取用户统计信息
  async getUserStats(userId) {
    try {
      // 尝试从缓存获取
      if (this.cacheEnabled && this.cache) {
        const cached = await this.cache.getUserStats(userId);
        if (cached) {
          return cached;
        }
      }

      let result;

      if (this.dbType === 'postgresql') {
        result = await this.primaryDB.getUserStats(userId);
      } else {
        // SQLite版本
        result = await new Promise((resolve, reject) => {
          this.primaryDB.db.get(
            `SELECT
               COUNT(DISTINCT c.id) as total_conversations,
               COUNT(m.id) as total_messages,
               COUNT(CASE WHEN m.message_type = 'user' THEN 1 END) as user_messages,
               COUNT(CASE WHEN m.message_type = 'assistant' THEN 1 END) as ai_responses,
               AVG(m.rating) as avg_rating,
               MAX(c.updated_at) as last_chat_time,
               MIN(c.created_at) as first_chat_time
             FROM chat_conversations c
             LEFT JOIN chat_messages m ON c.id = m.conversation_id AND m.is_deleted = 0
             WHERE c.user_id = ? AND c.is_deleted = 0`,
            [userId],
            (err, row) => {
              if (err) reject(err);
              else resolve(row || {});
            }
          );
        });
      }

      // 缓存结果
      if (this.cacheEnabled && this.cache && result) {
        await this.cache.cacheUserStats(userId, result);
      }

      return result;
    } catch (error) {
      console.error('❌ 获取用户统计失败:', error.message);
      return null;
    }
  }

  // 删除对话
  async deleteConversation(conversationId, userId) {
    try {
      if (this.dbType === 'postgresql') {
        await this.primaryDB.deleteConversation(conversationId, userId);
      } else {
        // SQLite版本
        await new Promise((resolve, reject) => {
          this.primaryDB.db.run(
            'UPDATE chat_conversations SET is_deleted = 1 WHERE id = ? AND user_id = ?',
            [conversationId, userId],
            (err) => {
              if (err) reject(err);
              else resolve();
            }
          );
        });

        // 同时软删除相关消息
        await new Promise((resolve, reject) => {
          this.primaryDB.db.run(
            'UPDATE chat_messages SET is_deleted = 1 WHERE conversation_id = ? AND user_id = ?',
            [conversationId, userId],
            (err) => {
              if (err) reject(err);
              else resolve();
            }
          );
        });
      }

      // 清除相关缓存
      if (this.cacheEnabled && this.cache) {
        await this.cache.clearUserCache(userId);
      }

      return true;
    } catch (error) {
      console.error('❌ 删除对话失败:', error.message);
      return false;
    }
  }

  // 健康检查
  async healthCheck() {
    const health = {
      database: {
        type: this.dbType,
        status: 'unknown'
      },
      cache: {
        enabled: this.cacheEnabled,
        status: 'unknown'
      },
      timestamp: new Date().toISOString()
    };

    try {
      // 检查主数据库
      if (this.dbType === 'postgresql') {
        const dbHealth = await this.primaryDB.healthCheck();
        health.database.status = dbHealth.status;
      } else {
        health.database.status = 'healthy'; // SQLite通常总是可用的
      }

      // 检查缓存
      if (this.cacheEnabled && this.cache) {
        const cacheHealth = await this.cache.healthCheck();
        health.cache.status = cacheHealth.status;
      } else {
        health.cache.status = 'disabled';
      }

      return health;
    } catch (error) {
      health.database.status = 'unhealthy';
      health.error = error.message;
      return health;
    }
  }

  // 关闭连接
  async close() {
    if (this.primaryDB && typeof this.primaryDB.close === 'function') {
      await this.primaryDB.close();
    }

    if (this.cache) {
      await this.cache.close();
    }

    console.log('✅ 数据库适配器已关闭');
  }

  // 检查数据库连接状态
  isConnected() {
    try {
      if (this.dbType === 'postgresql') {
        return this.primaryDB && this.primaryDB.pool && !this.primaryDB.pool.ended;
      } else {
        return this.primaryDB && this.primaryDB.db && this.primaryDB.db.open;
      }
    } catch (error) {
      return false;
    }
  }
}

module.exports = DatabaseAdapter;
