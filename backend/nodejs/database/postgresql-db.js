const { Pool } = require('pg');
const fs = require('fs');
const path = require('path');

class PostgreSQLDatabase {
  constructor() {
    this.pool = null;
    this.isConnected = false;
  }

  // 初始化数据库连接
  async initialize() {
    try {
      // 数据库连接配置
      const config = {
        host: process.env.DB_HOST || 'localhost',
        port: process.env.DB_PORT || 5432,
        database: process.env.DB_NAME || 'qms',
        user: process.env.DB_USER || 'qms_app',
        password: process.env.DB_PASSWORD || 'qms123',
        max: 20, // 连接池最大连接数
        idleTimeoutMillis: 30000, // 空闲连接超时时间
        connectionTimeoutMillis: 2000, // 连接超时时间
        ssl: process.env.NODE_ENV === 'production' ? { rejectUnauthorized: false } : false
      };

      this.pool = new Pool(config);

      // 测试连接
      const client = await this.pool.connect();
      console.log('✅ PostgreSQL数据库连接成功');
      
      // 检查数据库版本
      const result = await client.query('SELECT version()');
      console.log('📍 数据库版本:', result.rows[0].version.split(' ')[0], result.rows[0].version.split(' ')[1]);
      
      client.release();
      this.isConnected = true;

      // 初始化数据库表结构
      await this.initializeSchema();

      return true;
    } catch (error) {
      console.error('❌ PostgreSQL数据库连接失败:', error.message);
      this.isConnected = false;
      return false;
    }
  }

  // 初始化数据库表结构
  async initializeSchema() {
    try {
      const schemaPath = path.join(__dirname, 'postgresql-schema.sql');
      if (fs.existsSync(schemaPath)) {
        const schema = fs.readFileSync(schemaPath, 'utf8');
        await this.pool.query(schema);
        console.log('✅ 数据库表结构初始化完成');
      }
    } catch (error) {
      console.error('❌ 数据库表结构初始化失败:', error.message);
      throw error;
    }
  }

  // 获取数据库连接
  async getConnection() {
    if (!this.isConnected) {
      throw new Error('数据库未连接');
    }
    return await this.pool.connect();
  }

  // 执行查询
  async query(text, params = []) {
    const client = await this.getConnection();
    try {
      const result = await client.query(text, params);
      return result;
    } finally {
      client.release();
    }
  }

  // 执行事务
  async transaction(callback) {
    const client = await this.getConnection();
    try {
      await client.query('BEGIN');
      const result = await callback(client);
      await client.query('COMMIT');
      return result;
    } catch (error) {
      await client.query('ROLLBACK');
      throw error;
    } finally {
      client.release();
    }
  }

  // 保存对话
  async saveConversation(conversationData) {
    const { id, user_id, title, model_provider, model_name, model_config } = conversationData;
    
    const query = `
      INSERT INTO chat_conversations (id, user_id, title, model_provider, model_name, model_config)
      VALUES ($1, $2, $3, $4, $5, $6)
      ON CONFLICT (id) DO UPDATE SET
        title = EXCLUDED.title,
        model_provider = EXCLUDED.model_provider,
        model_name = EXCLUDED.model_name,
        model_config = EXCLUDED.model_config,
        updated_at = CURRENT_TIMESTAMP
      RETURNING *
    `;
    
    const result = await this.query(query, [id, user_id, title, model_provider, model_name, JSON.stringify(model_config)]);
    return result.rows[0];
  }

  // 保存消息
  async saveMessage(messageData) {
    const { 
      id, conversation_id, user_id, message_type, content, 
      model_info, response_time, token_usage, rating, feedback 
    } = messageData;
    
    return await this.transaction(async (client) => {
      // 插入消息
      const insertQuery = `
        INSERT INTO chat_messages (
          id, conversation_id, user_id, message_type, content,
          model_info, response_time, token_usage, rating, feedback
        )
        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
        RETURNING *
      `;
      
      const messageResult = await client.query(insertQuery, [
        id, conversation_id, user_id, message_type, content,
        JSON.stringify(model_info), response_time, JSON.stringify(token_usage), rating, feedback
      ]);

      // 更新对话的消息计数和更新时间
      const updateQuery = `
        UPDATE chat_conversations 
        SET message_count = message_count + 1, updated_at = CURRENT_TIMESTAMP
        WHERE id = $1
      `;
      
      await client.query(updateQuery, [conversation_id]);

      return messageResult.rows[0];
    });
  }

  // 获取用户对话列表
  async getUserConversations(userId, limit = 20, offset = 0) {
    const query = `
      SELECT * FROM get_user_recent_conversations($1, $2)
      OFFSET $3
    `;
    
    const result = await this.query(query, [userId, limit, offset]);
    return result.rows;
  }

  // 获取对话消息
  async getConversationMessages(conversationId, limit = 50, offset = 0) {
    const query = `
      SELECT 
        id, message_type, content, model_info, response_time,
        token_usage, rating, feedback, created_at
      FROM chat_messages
      WHERE conversation_id = $1 AND is_deleted = FALSE
      ORDER BY created_at ASC
      LIMIT $2 OFFSET $3
    `;
    
    const result = await this.query(query, [conversationId, limit, offset]);
    return result.rows;
  }

  // 搜索消息
  async searchMessages(userId, searchText, limit = 50) {
    const result = await this.query(
      'SELECT * FROM search_messages($1, $2, $3)',
      [userId, searchText, limit]
    );
    return result.rows;
  }

  // 获取用户统计信息
  async getUserStats(userId) {
    const query = `
      SELECT * FROM user_chat_stats WHERE user_id = $1
    `;
    
    const result = await this.query(query, [userId]);
    return result.rows[0] || null;
  }

  // 获取模型使用统计
  async getModelUsageStats(days = 30) {
    const query = `
      SELECT * FROM model_usage_stats 
      WHERE usage_date >= CURRENT_DATE - INTERVAL '${days} days'
      ORDER BY usage_date DESC, conversation_count DESC
    `;
    
    const result = await this.query(query);
    return result.rows;
  }

  // 删除对话（软删除）
  async deleteConversation(conversationId, userId) {
    return await this.transaction(async (client) => {
      // 软删除对话
      await client.query(
        'UPDATE chat_conversations SET is_deleted = TRUE WHERE id = $1 AND user_id = $2',
        [conversationId, userId]
      );

      // 软删除相关消息
      await client.query(
        'UPDATE chat_messages SET is_deleted = TRUE WHERE conversation_id = $1 AND user_id = $2',
        [conversationId, userId]
      );

      return true;
    });
  }

  // 更新消息评分
  async updateMessageRating(messageId, userId, rating, feedback = null) {
    const query = `
      UPDATE chat_messages 
      SET rating = $3, feedback = $4, updated_at = CURRENT_TIMESTAMP
      WHERE id = $1 AND user_id = $2 AND is_deleted = FALSE
      RETURNING *
    `;
    
    const result = await this.query(query, [messageId, userId, rating, feedback]);
    return result.rows[0];
  }

  // 获取标签列表
  async getTags() {
    const query = 'SELECT * FROM chat_tags ORDER BY name';
    const result = await this.query(query);
    return result.rows;
  }

  // 为对话添加标签
  async addConversationTag(conversationId, tagId) {
    const query = `
      INSERT INTO conversation_tags (conversation_id, tag_id)
      VALUES ($1, $2)
      ON CONFLICT (conversation_id, tag_id) DO NOTHING
    `;
    
    await this.query(query, [conversationId, tagId]);
    return true;
  }

  // 健康检查
  async healthCheck() {
    try {
      const result = await this.query('SELECT 1 as health');
      return {
        status: 'healthy',
        database: 'postgresql',
        connected: this.isConnected,
        timestamp: new Date().toISOString()
      };
    } catch (error) {
      return {
        status: 'unhealthy',
        database: 'postgresql',
        connected: false,
        error: error.message,
        timestamp: new Date().toISOString()
      };
    }
  }

  // 关闭连接池
  async close() {
    if (this.pool) {
      await this.pool.end();
      this.isConnected = false;
      console.log('✅ PostgreSQL连接池已关闭');
    }
  }
}

module.exports = PostgreSQLDatabase;
