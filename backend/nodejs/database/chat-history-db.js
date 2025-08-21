/**
 * QMS AI聊天记录数据库操作模块
 * 用于管理用户个人对话历史记录
 */

const sqlite3 = require('sqlite3').verbose();
const path = require('path');
const fs = require('fs');

class ChatHistoryDB {
  constructor() {
    // 使用SQLite作为轻量级数据库
    const dbPath = path.join(__dirname, 'chat_history.db');
    this.db = new sqlite3.Database(dbPath, (err) => {
      if (err) {
        console.error('❌ 数据库连接失败:', err.message);
      } else {
        console.log('✅ 聊天记录数据库连接成功');
        this.initTables();
      }
    });
  }

  // 数据库迁移
  migrateDatabase() {
    return new Promise((resolve) => {
      // 简化迁移逻辑，直接resolve，让CREATE TABLE IF NOT EXISTS处理
      resolve();
    });
  }

  // 初始化数据表
  initTables() {
    // 先执行迁移
    this.migrateDatabase().then(() => {
    const tables = [
      // 用户表
      `CREATE TABLE IF NOT EXISTS users (
        id TEXT PRIMARY KEY,
        username TEXT NOT NULL,
        email TEXT,
        password TEXT,
        department TEXT,
        role TEXT,
        permissions TEXT,
        status TEXT DEFAULT 'active',
        last_login_at DATETIME,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
      )`,
      
      // 对话会话表
      `CREATE TABLE IF NOT EXISTS chat_conversations (
        id TEXT PRIMARY KEY,
        user_id TEXT NOT NULL,
        title TEXT NOT NULL DEFAULT '新对话',
        model_provider TEXT NOT NULL,
        model_name TEXT NOT NULL,
        model_config TEXT, -- JSON字符串
        message_count INTEGER DEFAULT 0,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        is_deleted BOOLEAN DEFAULT FALSE,
        FOREIGN KEY (user_id) REFERENCES users(id)
      )`,
      
      // 对话消息表
      `CREATE TABLE IF NOT EXISTS chat_messages (
        id TEXT PRIMARY KEY,
        conversation_id TEXT NOT NULL,
        user_id TEXT NOT NULL,
        message_type TEXT NOT NULL CHECK (message_type IN ('user', 'assistant')),
        content TEXT NOT NULL,
        model_info TEXT, -- JSON字符串
        response_time INTEGER,
        token_usage TEXT, -- JSON字符串
        rating INTEGER DEFAULT NULL,
        feedback TEXT DEFAULT NULL,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        is_deleted BOOLEAN DEFAULT FALSE,
        FOREIGN KEY (conversation_id) REFERENCES chat_conversations(id),
        FOREIGN KEY (user_id) REFERENCES users(id)
      )`,
      
      // 对话标签表
      `CREATE TABLE IF NOT EXISTS chat_tags (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL UNIQUE,
        color TEXT DEFAULT '#409EFF',
        description TEXT,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
      )`,
      
      // 对话-标签关联表
      `CREATE TABLE IF NOT EXISTS conversation_tags (
        conversation_id TEXT NOT NULL,
        tag_id INTEGER NOT NULL,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        PRIMARY KEY (conversation_id, tag_id),
        FOREIGN KEY (conversation_id) REFERENCES chat_conversations(id),
        FOREIGN KEY (tag_id) REFERENCES chat_tags(id)
      )`,

      // 添加conversations表（用于兼容性）
      `CREATE TABLE IF NOT EXISTS conversations (
        id TEXT PRIMARY KEY,
        user_id TEXT NOT NULL,
        title TEXT NOT NULL DEFAULT '新对话',
        model_provider TEXT NOT NULL,
        model_name TEXT NOT NULL,
        model_config TEXT,
        message_count INTEGER DEFAULT 0,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        is_deleted BOOLEAN DEFAULT FALSE,
        FOREIGN KEY (user_id) REFERENCES users(id)
      )`,

      // 添加messages表（用于兼容性）
      `CREATE TABLE IF NOT EXISTS messages (
        id TEXT PRIMARY KEY,
        conversation_id TEXT NOT NULL,
        user_id TEXT NOT NULL,
        message_type TEXT NOT NULL CHECK (message_type IN ('user', 'assistant')),
        content TEXT NOT NULL,
        model_info TEXT,
        response_time INTEGER,
        token_usage TEXT,
        rating INTEGER,
        feedback TEXT,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        is_deleted BOOLEAN DEFAULT FALSE,
        FOREIGN KEY (conversation_id) REFERENCES conversations(id),
        FOREIGN KEY (user_id) REFERENCES users(id)
      )`,

      // 系统日志表
      `CREATE TABLE IF NOT EXISTS system_logs (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        level TEXT NOT NULL,
        message TEXT NOT NULL,
        source TEXT,
        user_id TEXT,
        metadata TEXT,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES users(id)
      )`
    ];

    // 创建索引（避免重复定义）
    const indexes = [
      // 聊天相关表索引
      'CREATE INDEX IF NOT EXISTS idx_chat_conversations_user_created ON chat_conversations(user_id, created_at DESC)',
      'CREATE INDEX IF NOT EXISTS idx_chat_messages_conversation_created ON chat_messages(conversation_id, created_at ASC)',
      'CREATE INDEX IF NOT EXISTS idx_chat_messages_user_created ON chat_messages(user_id, created_at DESC)',
      // 新表索引
      'CREATE INDEX IF NOT EXISTS idx_conversations_user_created ON conversations(user_id, created_at DESC)',
      'CREATE INDEX IF NOT EXISTS idx_conversations_updated ON conversations(updated_at DESC)',
      'CREATE INDEX IF NOT EXISTS idx_conversations_status ON conversations(is_deleted)',
      'CREATE INDEX IF NOT EXISTS idx_messages_conversation_created ON messages(conversation_id, created_at ASC)',
      'CREATE INDEX IF NOT EXISTS idx_messages_user_created ON messages(user_id, created_at DESC)',
      'CREATE INDEX IF NOT EXISTS idx_messages_role ON messages(message_type)',
      'CREATE INDEX IF NOT EXISTS idx_messages_model ON messages(model_info)',
      'CREATE INDEX IF NOT EXISTS idx_users_username ON users(username)',
      'CREATE INDEX IF NOT EXISTS idx_users_email ON users(email)',
      'CREATE INDEX IF NOT EXISTS idx_users_created ON users(created_at)',
      'CREATE INDEX IF NOT EXISTS idx_logs_level_created ON system_logs(level, created_at DESC)',
      'CREATE INDEX IF NOT EXISTS idx_logs_source ON system_logs(source)'
    ];

    // 串行执行表创建
    this.createTablesSequentially(tables, 0, () => {
      // 表创建完成后，创建Coze Studio表
      this.createCozeStudioTables(() => {
        // Coze Studio表创建完成后，创建索引
        this.createIndexesSequentially(indexes, 0, () => {
          // 索引创建完成后，插入默认标签
          this.initDefaultTags();
        });
      });
    });
    }).catch(err => {
      console.error('❌ 数据库初始化失败:', err);
    });
  }

  // 创建Coze Studio相关表
  createCozeStudioTables(callback) {
    // 检查是否是认证服务或配置服务，如果是则跳过Coze Studio表创建
    const isAuthService = process.env.SERVICE_TYPE === 'auth' ||
                         process.argv.some(arg => arg.includes('auth-service'));
    const isConfigService = process.env.SERVICE_TYPE === 'config' ||
                           process.argv.some(arg => arg.includes('lightweight-config-service'));

    if (isAuthService) {
      console.log('⏭️ 认证服务跳过Coze Studio表创建');
      callback();
      return;
    }

    if (isConfigService) {
      console.log('⏭️ 配置服务跳过Coze Studio表创建');
      callback();
      return;
    }

    try {
      // 读取Coze Studio表结构SQL文件
      const cozeSchemaPath = path.join(__dirname, 'coze-studio-schema.sql');
      const cozeSchema = fs.readFileSync(cozeSchemaPath, 'utf8');

      // 分割SQL语句
      const statements = cozeSchema.split(';').filter(stmt => stmt.trim());

      // 串行执行SQL语句
      this.executeStatementsSequentially(statements, 0, callback);
    } catch (error) {
      console.error('❌ 读取Coze Studio表结构失败:', error);
      callback(); // 继续执行，不阻塞其他表的创建
    }
  }

  // 串行执行SQL语句
  executeStatementsSequentially(statements, index, callback) {
    if (index >= statements.length) {
      console.log('✅ Coze Studio表结构创建完成');
      callback();
      return;
    }

    const statement = statements[index].trim();
    if (!statement) {
      this.executeStatementsSequentially(statements, index + 1, callback);
      return;
    }

    // 添加超时保护，防止无限循环
    const timeout = setTimeout(() => {
      console.error(`⚠️ SQL语句执行超时 (${index + 1}), 跳过: ${statement.substring(0, 50)}...`);
      this.executeStatementsSequentially(statements, index + 1, callback);
    }, 5000);

    this.db.run(statement, (err) => {
      clearTimeout(timeout);
      if (err) {
        console.error(`❌ 执行Coze Studio SQL语句失败 (${index + 1}):`, err.message);
      } else {
        console.log(`✅ 执行Coze Studio SQL语句成功 (${index + 1})`);
      }
      this.executeStatementsSequentially(statements, index + 1, callback);
    });
  }

  // 串行创建表
  createTablesSequentially(tables, index, callback) {
    if (index >= tables.length) {
      callback();
      return;
    }

    this.db.run(tables[index], (err) => {
      if (err) {
        console.error(`❌ 创建表${index + 1}失败:`, err.message);
      } else {
        console.log(`✅ 创建表${index + 1}成功`);
      }
      this.createTablesSequentially(tables, index + 1, callback);
    });
  }

  // 串行创建索引
  createIndexesSequentially(indexes, index, callback) {
    if (index >= indexes.length) {
      console.log('✅ 所有索引创建完成');
      callback();
      return;
    }

    // 添加超时保护，防止无限循环
    const timeout = setTimeout(() => {
      console.error(`⚠️ 索引创建超时 (${index + 1}), 跳过: ${indexes[index].substring(0, 50)}...`);
      this.createIndexesSequentially(indexes, index + 1, callback);
    }, 3000);

    this.db.run(indexes[index], (err) => {
      clearTimeout(timeout);
      if (err) {
        console.error(`❌ 创建索引${index + 1}失败:`, err.message);
      } else {
        console.log(`✅ 创建索引${index + 1}成功`);
      }
      this.createIndexesSequentially(indexes, index + 1, callback);
    });
  }

  // 初始化默认标签
  initDefaultTags() {
    const defaultTags = [
      { name: '质量管理', color: '#67C23A', description: '质量管理相关问题' },
      { name: '缺陷分析', color: '#E6A23C', description: '产品缺陷分析讨论' },
      { name: '流程优化', color: '#409EFF', description: '业务流程优化建议' },
      { name: '标准规范', color: '#909399', description: '标准规范咨询' },
      { name: '数据分析', color: '#F56C6C', description: '数据统计分析' },
      { name: '其他', color: '#C0C4CC', description: '其他类型对话' }
    ];

    const insertTag = this.db.prepare(`
      INSERT OR IGNORE INTO chat_tags (name, color, description) 
      VALUES (?, ?, ?)
    `);

    defaultTags.forEach(tag => {
      insertTag.run([tag.name, tag.color, tag.description]);
    });

    insertTag.finalize();
  }

  // 创建或获取用户
  async createOrGetUser(userData) {
    return new Promise((resolve, reject) => {
      const { id, username, email, department, role } = userData;
      
      // 先尝试获取用户
      this.db.get(
        'SELECT * FROM users WHERE id = ?',
        [id],
        (err, row) => {
          if (err) {
            reject(err);
            return;
          }

          if (row) {
            // 用户已存在，更新信息
            this.db.run(
              `UPDATE users SET username = ?, email = ?, department = ?, role = ?, 
               updated_at = CURRENT_TIMESTAMP WHERE id = ?`,
              [username, email, department, role, id],
              (err) => {
                if (err) reject(err);
                else resolve({ ...row, username, email, department, role });
              }
            );
          } else {
            // 创建新用户
            this.db.run(
              `INSERT INTO users (id, username, email, department, role) 
               VALUES (?, ?, ?, ?, ?)`,
              [id, username, email, department, role],
              (err) => {
                if (err) reject(err);
                else resolve({ id, username, email, department, role });
              }
            );
          }
        }
      );
    });
  }

  // 创建新对话会话
  async createConversation(conversationData) {
    return new Promise((resolve, reject) => {
      const {
        id,
        user_id,
        title,
        model_provider,
        model_name,
        model_config
      } = conversationData;

      this.db.run(
        `INSERT INTO chat_conversations 
         (id, user_id, title, model_provider, model_name, model_config) 
         VALUES (?, ?, ?, ?, ?, ?)`,
        [
          id,
          user_id,
          title || '新对话',
          model_provider,
          model_name,
          JSON.stringify(model_config || {})
        ],
        function(err) {
          if (err) reject(err);
          else resolve({ id, user_id, title, model_provider, model_name });
        }
      );
    });
  }

  // 保存对话会话
  async saveConversation(conversationData) {
    return new Promise((resolve, reject) => {
      const {
        id,
        user_id,
        title,
        model_provider,
        model_name
      } = conversationData;

      const sql = `
        INSERT OR REPLACE INTO chat_conversations
        (id, user_id, title, model_provider, model_name, created_at, updated_at, is_deleted)
        VALUES (?, ?, ?, ?, ?, datetime('now'), datetime('now'), FALSE)
      `;

      this.db.run(sql, [id, user_id, title, model_provider, model_name], function(err) {
        if (err) {
          console.error('保存对话失败:', err);
          reject(err);
        } else {
          console.log('✅ 对话保存成功:', id);
          resolve({ id: id, changes: this.changes });
        }
      });
    });
  }

  // 保存聊天消息
  async saveMessage(messageData) {
    return new Promise((resolve, reject) => {
      const {
        id,
        conversation_id,
        user_id,
        message_type,
        content,
        model_info,
        response_time,
        token_usage
      } = messageData;

      // 使用INSERT OR REPLACE来处理ID冲突
      this.db.run(
        `INSERT OR REPLACE INTO chat_messages
         (id, conversation_id, user_id, message_type, content, model_info, response_time, token_usage, created_at, updated_at, is_deleted)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?,
                 COALESCE((SELECT created_at FROM chat_messages WHERE id = ?), datetime('now')),
                 datetime('now'),
                 FALSE)`,
        [
          id,
          conversation_id,
          user_id,
          message_type,
          content,
          JSON.stringify(model_info || {}),
          response_time || null,
          JSON.stringify(token_usage || {}),
          id  // 用于COALESCE查询
        ],
        function(err) {
          if (err) {
            console.error('❌ 保存消息失败:', err);
            reject(err);
          } else {
            console.log('✅ 消息保存成功:', id, '(changes:', this.changes, ')');
            resolve(messageData);
          }
        }
      );

      // 更新对话统计
      this.updateConversationStats(conversation_id);
    });
  }

  // 更新对话统计信息
  updateConversationStats(conversationId) {
    this.db.run(
      `UPDATE chat_conversations SET 
       message_count = (SELECT COUNT(*) FROM chat_messages WHERE conversation_id = ? AND is_deleted = FALSE),
       updated_at = CURRENT_TIMESTAMP 
       WHERE id = ?`,
      [conversationId, conversationId]
    );
  }

  // 获取用户对话列表
  async getUserConversations(userId, options = {}) {
    return new Promise((resolve, reject) => {
      const { limit = 20, offset = 0, model_provider } = options;
      
      let sql = `
        SELECT c.*, 
               (SELECT COUNT(*) FROM chat_messages WHERE conversation_id = c.id AND is_deleted = FALSE) as message_count,
               (SELECT content FROM chat_messages WHERE conversation_id = c.id AND message_type = 'user' AND is_deleted = FALSE ORDER BY created_at ASC LIMIT 1) as first_message
        FROM chat_conversations c 
        WHERE c.user_id = ? AND c.is_deleted = FALSE
      `;
      
      const params = [userId];
      
      if (model_provider) {
        sql += ' AND c.model_provider = ?';
        params.push(model_provider);
      }
      
      sql += ' ORDER BY c.updated_at DESC LIMIT ? OFFSET ?';
      params.push(limit, offset);

      this.db.all(sql, params, (err, rows) => {
        if (err) reject(err);
        else {
          const conversations = rows.map(row => ({
            ...row,
            model_config: JSON.parse(row.model_config || '{}')
          }));
          resolve(conversations);
        }
      });
    });
  }

  // 获取对话详细信息和消息
  async getConversationWithMessages(conversationId, userId) {
    return new Promise((resolve, reject) => {
      // 先获取对话信息
      this.db.get(
        'SELECT * FROM chat_conversations WHERE id = ? AND user_id = ? AND is_deleted = FALSE',
        [conversationId, userId],
        (err, conversation) => {
          if (err) {
            reject(err);
            return;
          }

          if (!conversation) {
            resolve(null);
            return;
          }

          // 获取消息列表
          this.db.all(
            `SELECT * FROM chat_messages 
             WHERE conversation_id = ? AND is_deleted = FALSE 
             ORDER BY created_at ASC`,
            [conversationId],
            (err, messages) => {
              if (err) {
                reject(err);
                return;
              }

              const result = {
                ...conversation,
                model_config: JSON.parse(conversation.model_config || '{}'),
                messages: messages.map(msg => ({
                  ...msg,
                  model_info: JSON.parse(msg.model_info || '{}'),
                  token_usage: JSON.parse(msg.token_usage || '{}')
                }))
              };

              resolve(result);
            }
          );
        }
      );
    });
  }

  // 删除对话（软删除）
  async deleteConversation(conversationId, userId) {
    return new Promise((resolve, reject) => {
      this.db.run(
        'UPDATE chat_conversations SET is_deleted = TRUE WHERE id = ? AND user_id = ?',
        [conversationId, userId],
        function(err) {
          if (err) reject(err);
          else resolve(this.changes > 0);
        }
      );
    });
  }

  // 获取用户统计信息
  async getUserStats(userId) {
    return new Promise((resolve, reject) => {
      const sql = `
        SELECT
          COUNT(DISTINCT c.id) as total_conversations,
          COUNT(m.id) as total_messages,
          COUNT(CASE WHEN m.message_type = 'user' THEN 1 END) as user_messages,
          COUNT(CASE WHEN m.message_type = 'assistant' THEN 1 END) as ai_responses,
          AVG(m.rating) as avg_rating,
          COUNT(CASE WHEN m.rating IS NOT NULL THEN 1 END) as rated_messages,
          MAX(c.updated_at) as last_chat_time,
          MIN(c.created_at) as first_chat_time
        FROM chat_conversations c
        LEFT JOIN chat_messages m ON c.id = m.conversation_id AND m.is_deleted = FALSE
        WHERE c.user_id = ? AND c.is_deleted = FALSE
      `;

      this.db.get(sql, [userId], (err, row) => {
        if (err) reject(err);
        else resolve(row);
      });
    });
  }

  // 对消息进行评分
  async rateMessage(messageId, userId, rating, feedback = null) {
    return new Promise((resolve, reject) => {
      // 首先验证消息是否属于该用户
      this.db.get(
        'SELECT id FROM chat_messages WHERE id = ? AND user_id = ? AND message_type = "assistant" AND is_deleted = FALSE',
        [messageId, userId],
        (err, row) => {
          if (err) {
            reject(err);
            return;
          }

          if (!row) {
            resolve(false); // 消息不存在或无权限
            return;
          }

          // 更新评分
          this.db.run(
            'UPDATE chat_messages SET rating = ?, feedback = ? WHERE id = ?',
            [rating, feedback, messageId],
            function(err) {
              if (err) reject(err);
              else resolve(this.changes > 0);
            }
          );
        }
      );
    });
  }

  // 获取评分统计
  async getRatingStats(userId, options = {}) {
    return new Promise((resolve, reject) => {
      let sql = `
        SELECT
          AVG(m.rating) as avg_rating,
          COUNT(CASE WHEN m.rating = 1 THEN 1 END) as rating_1_count,
          COUNT(CASE WHEN m.rating = 2 THEN 1 END) as rating_2_count,
          COUNT(CASE WHEN m.rating = 3 THEN 1 END) as rating_3_count,
          COUNT(CASE WHEN m.rating = 4 THEN 1 END) as rating_4_count,
          COUNT(CASE WHEN m.rating = 5 THEN 1 END) as rating_5_count,
          COUNT(CASE WHEN m.rating IS NOT NULL THEN 1 END) as total_rated,
          COUNT(m.id) as total_messages,
          c.model_provider,
          DATE(m.created_at) as rating_date
        FROM chat_messages m
        JOIN chat_conversations c ON m.conversation_id = c.id
        WHERE m.user_id = ? AND m.message_type = 'assistant' AND m.is_deleted = FALSE AND c.is_deleted = FALSE
      `;

      const params = [userId];

      if (options.model_provider) {
        sql += ' AND c.model_provider = ?';
        params.push(options.model_provider);
      }

      sql += ' GROUP BY c.model_provider, DATE(m.created_at) ORDER BY rating_date DESC';

      this.db.all(sql, params, (err, rows) => {
        if (err) reject(err);
        else {
          // 计算总体统计
          const totalStats = {
            avg_rating: 0,
            rating_distribution: { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 },
            total_rated: 0,
            total_messages: 0,
            rating_rate: 0
          };

          let totalRatingSum = 0;
          let totalRatedCount = 0;
          let totalMessageCount = 0;

          rows.forEach(row => {
            totalRatingSum += (row.avg_rating || 0) * (row.total_rated || 0);
            totalRatedCount += row.total_rated || 0;
            totalMessageCount += row.total_messages || 0;

            totalStats.rating_distribution[1] += row.rating_1_count || 0;
            totalStats.rating_distribution[2] += row.rating_2_count || 0;
            totalStats.rating_distribution[3] += row.rating_3_count || 0;
            totalStats.rating_distribution[4] += row.rating_4_count || 0;
            totalStats.rating_distribution[5] += row.rating_5_count || 0;
          });

          totalStats.avg_rating = totalRatedCount > 0 ? totalRatingSum / totalRatedCount : 0;
          totalStats.total_rated = totalRatedCount;
          totalStats.total_messages = totalMessageCount;
          totalStats.rating_rate = totalMessageCount > 0 ? (totalRatedCount / totalMessageCount) * 100 : 0;

          resolve({
            overall: totalStats,
            by_model: rows,
            daily_stats: rows
          });
        }
      });
    });
  }

  // 获取统计数据
  async getStatistics(userId, days = 7) {
    return new Promise((resolve, reject) => {
      const dateLimit = new Date();
      dateLimit.setDate(dateLimit.getDate() - days);

      const queries = [
        // 总对话数
        `SELECT COUNT(*) as totalConversations FROM chat_conversations
         WHERE user_id = ? AND created_at >= ? AND is_deleted = FALSE`,

        // 总消息数
        `SELECT COUNT(*) as totalMessages FROM chat_messages
         WHERE user_id = ? AND created_at >= ? AND is_deleted = FALSE`,

        // 平均响应时间
        `SELECT AVG(response_time) as averageResponseTime FROM chat_messages
         WHERE user_id = ? AND created_at >= ? AND response_time IS NOT NULL AND is_deleted = FALSE`,

        // 平均评分
        `SELECT AVG(rating) as averageRating FROM chat_messages
         WHERE user_id = ? AND created_at >= ? AND rating IS NOT NULL AND is_deleted = FALSE`
      ];

      const results = {};
      let completed = 0;

      queries.forEach((query, index) => {
        this.db.all(query, [userId, dateLimit.toISOString()], (err, rows) => {
          if (err) {
            console.error(`统计查询 ${index} 失败:`, err);
            results[index] = null;
          } else {
            results[index] = rows;
          }

          completed++;
          if (completed === queries.length) {
            // 处理结果
            const stats = {
              totalConversations: results[0]?.[0]?.totalConversations || 0,
              totalMessages: results[1]?.[0]?.totalMessages || 0,
              averageResponseTime: Math.round(results[2]?.[0]?.averageResponseTime || 0),
              averageRating: parseFloat((results[3]?.[0]?.averageRating || 0).toFixed(2))
            };

            resolve(stats);
          }
        });
      });
    });
  }

  // 关闭数据库连接
  close() {
    this.db.close((err) => {
      if (err) {
        console.error('❌ 关闭数据库失败:', err.message);
      } else {
        console.log('✅ 数据库连接已关闭');
      }
    });
  }
}

module.exports = ChatHistoryDB;
