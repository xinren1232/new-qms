#!/usr/bin/env node

/**
 * 数据库初始化脚本
 * 用于项目启动时初始化数据库和基础数据
 */

const path = require('path');
const fs = require('fs');

// 设置环境变量
require('dotenv').config({ path: path.join(__dirname, '../.env') });

const DatabaseAdapter = require('../backend/nodejs/database/database-adapter');

class DatabaseInitializer {
  constructor() {
    this.dbAdapter = new DatabaseAdapter();
  }

  /**
   * 初始化数据库
   */
  async initialize() {
    console.log('🚀 开始初始化QMS-AI数据库...\n');

    try {
      // 1. 检查数据目录
      await this.ensureDataDirectory();

      // 2. 初始化数据库适配器
      console.log('📊 初始化数据库适配器...');
      await this.dbAdapter.initialize();

      // 3. 检查数据库连接
      console.log('🔗 测试数据库连接...');
      await this.testDatabaseConnection();

      // 4. 创建基础表结构
      console.log('🏗️ 创建数据库表结构...');
      await this.createTables();

      // 5. 插入初始数据
      console.log('📝 插入初始数据...');
      await this.insertInitialData();

      // 6. 验证数据库状态
      console.log('✅ 验证数据库状态...');
      await this.verifyDatabase();

      console.log('\n🎉 数据库初始化完成！');
      return true;

    } catch (error) {
      console.error('\n❌ 数据库初始化失败:', error.message);
      console.error('详细错误:', error);
      return false;
    }
  }

  /**
   * 确保数据目录存在
   */
  async ensureDataDirectory() {
    const dataDir = path.join(__dirname, '../data');
    
    if (!fs.existsSync(dataDir)) {
      fs.mkdirSync(dataDir, { recursive: true });
      console.log('📁 创建数据目录:', dataDir);
    }

    // 检查数据库文件路径
    const dbPath = process.env.SQLITE_DB_PATH || './data/qms_ai.db';
    const fullDbPath = path.resolve(dbPath);
    console.log('📍 数据库文件路径:', fullDbPath);

    // 确保数据库文件目录存在
    const dbDir = path.dirname(fullDbPath);
    if (!fs.existsSync(dbDir)) {
      fs.mkdirSync(dbDir, { recursive: true });
      console.log('📁 创建数据库目录:', dbDir);
    }
  }

  /**
   * 测试数据库连接
   */
  async testDatabaseConnection() {
    try {
      // 执行简单查询测试连接
      const result = await this.dbAdapter.query('SELECT 1 as test');
      
      if (result && result.length > 0) {
        console.log('✅ 数据库连接正常');
        return true;
      } else {
        throw new Error('数据库查询返回空结果');
      }
    } catch (error) {
      console.error('❌ 数据库连接失败:', error.message);
      throw error;
    }
  }

  /**
   * 创建数据库表结构
   */
  async createTables() {
    try {
      // 检查表是否已存在
      const tables = await this.checkExistingTables();
      
      if (tables.length > 0) {
        console.log('📋 发现现有表:', tables.join(', '));
        console.log('⚠️ 跳过表创建，使用现有结构');
        return;
      }

      // 创建对话表
      await this.createConversationsTable();
      
      // 创建消息表
      await this.createMessagesTable();
      
      // 创建用户表
      await this.createUsersTable();
      
      // 创建配置表
      await this.createConfigTable();

      console.log('✅ 数据库表创建完成');
    } catch (error) {
      console.error('❌ 创建数据库表失败:', error.message);
      throw error;
    }
  }

  /**
   * 检查现有表
   */
  async checkExistingTables() {
    try {
      let query;
      if (process.env.DB_TYPE === 'postgresql') {
        query = `
          SELECT table_name 
          FROM information_schema.tables 
          WHERE table_schema = 'public'
        `;
      } else {
        query = `
          SELECT name 
          FROM sqlite_master 
          WHERE type='table' AND name NOT LIKE 'sqlite_%'
        `;
      }
      
      const result = await this.dbAdapter.query(query);
      return result.map(row => row.table_name || row.name);
    } catch (error) {
      console.log('📋 无法检查现有表，可能是新数据库');
      return [];
    }
  }

  /**
   * 创建对话表
   */
  async createConversationsTable() {
    const sql = `
      CREATE TABLE IF NOT EXISTS conversations (
        id TEXT PRIMARY KEY,
        user_id TEXT NOT NULL,
        title TEXT NOT NULL,
        model_provider TEXT,
        model_name TEXT,
        system_prompt TEXT,
        temperature REAL DEFAULT 0.7,
        max_tokens INTEGER DEFAULT 2000,
        is_deleted INTEGER DEFAULT 0,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
      )
    `;
    
    await this.dbAdapter.query(sql);
    console.log('✅ 创建conversations表');
  }

  /**
   * 创建消息表
   */
  async createMessagesTable() {
    const sql = `
      CREATE TABLE IF NOT EXISTS messages (
        id TEXT PRIMARY KEY,
        conversation_id TEXT NOT NULL,
        user_id TEXT NOT NULL,
        message_type TEXT NOT NULL,
        content TEXT NOT NULL,
        model_info TEXT,
        response_time INTEGER,
        token_usage TEXT,
        is_deleted INTEGER DEFAULT 0,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (conversation_id) REFERENCES conversations(id)
      )
    `;
    
    await this.dbAdapter.query(sql);
    console.log('✅ 创建messages表');
  }

  /**
   * 创建用户表
   */
  async createUsersTable() {
    const sql = `
      CREATE TABLE IF NOT EXISTS users (
        id TEXT PRIMARY KEY,
        username TEXT UNIQUE NOT NULL,
        email TEXT UNIQUE,
        password_hash TEXT,
        display_name TEXT,
        avatar_url TEXT,
        role TEXT DEFAULT 'user',
        is_active INTEGER DEFAULT 1,
        last_login_at DATETIME,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
      )
    `;
    
    await this.dbAdapter.query(sql);
    console.log('✅ 创建users表');
  }

  /**
   * 创建配置表
   */
  async createConfigTable() {
    const sql = `
      CREATE TABLE IF NOT EXISTS config_items (
        id TEXT PRIMARY KEY,
        key TEXT UNIQUE NOT NULL,
        value TEXT,
        description TEXT,
        category TEXT,
        data_type TEXT DEFAULT 'string',
        is_encrypted INTEGER DEFAULT 0,
        is_active INTEGER DEFAULT 1,
        version INTEGER DEFAULT 1,
        created_by TEXT,
        updated_by TEXT,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
      )
    `;
    
    await this.dbAdapter.query(sql);
    console.log('✅ 创建config_items表');
  }

  /**
   * 插入初始数据
   */
  async insertInitialData() {
    try {
      // 插入系统配置
      await this.insertSystemConfig();
      
      // 插入AI模型配置
      await this.insertAIModelConfig();
      
      // 插入默认用户（如果不存在）
      await this.insertDefaultUser();

      console.log('✅ 初始数据插入完成');
    } catch (error) {
      console.error('❌ 插入初始数据失败:', error.message);
      throw error;
    }
  }

  /**
   * 插入系统配置
   */
  async insertSystemConfig() {
    const configs = [
      {
        key: 'system.name',
        value: 'QMS-AI智能质量管理系统',
        description: '系统名称',
        category: 'system'
      },
      {
        key: 'system.version',
        value: '2.0.0',
        description: '系统版本',
        category: 'system'
      },
      {
        key: 'system.maintenance_mode',
        value: 'false',
        description: '维护模式',
        category: 'system',
        data_type: 'boolean'
      }
    ];

    for (const config of configs) {
      try {
        await this.dbAdapter.query(
          `INSERT OR IGNORE INTO config_items (id, key, value, description, category, data_type) 
           VALUES (?, ?, ?, ?, ?, ?)`,
          [
            this.generateId(),
            config.key,
            config.value,
            config.description,
            config.category,
            config.data_type || 'string'
          ]
        );
      } catch (error) {
        console.warn(`⚠️ 配置项 ${config.key} 可能已存在`);
      }
    }
  }

  /**
   * 插入AI模型配置
   */
  async insertAIModelConfig() {
    const aiConfig = {
      key: 'ai.models',
      value: JSON.stringify([
        {
          id: 'deepseek-chat',
          name: 'DeepSeek Chat',
          provider: 'deepseek',
          model: 'deepseek-chat',
          enabled: true,
          priority: 1
        },
        {
          id: 'deepseek-coder',
          name: 'DeepSeek Coder',
          provider: 'deepseek',
          model: 'deepseek-coder',
          enabled: true,
          priority: 2
        }
      ]),
      description: 'AI模型配置列表',
      category: 'ai',
      data_type: 'json'
    };

    try {
      await this.dbAdapter.query(
        `INSERT OR IGNORE INTO config_items (id, key, value, description, category, data_type) 
         VALUES (?, ?, ?, ?, ?, ?)`,
        [
          this.generateId(),
          aiConfig.key,
          aiConfig.value,
          aiConfig.description,
          aiConfig.category,
          aiConfig.data_type
        ]
      );
    } catch (error) {
      console.warn('⚠️ AI模型配置可能已存在');
    }
  }

  /**
   * 插入默认用户
   */
  async insertDefaultUser() {
    try {
      const defaultUser = {
        id: this.generateId(),
        username: 'admin',
        email: 'admin@qms-ai.com',
        display_name: '系统管理员',
        role: 'admin'
      };

      await this.dbAdapter.query(
        `INSERT OR IGNORE INTO users (id, username, email, display_name, role) 
         VALUES (?, ?, ?, ?, ?)`,
        [
          defaultUser.id,
          defaultUser.username,
          defaultUser.email,
          defaultUser.display_name,
          defaultUser.role
        ]
      );
    } catch (error) {
      console.warn('⚠️ 默认用户可能已存在');
    }
  }

  /**
   * 验证数据库状态
   */
  async verifyDatabase() {
    try {
      // 检查表数量
      const tables = await this.checkExistingTables();
      console.log(`📊 数据库包含 ${tables.length} 个表:`, tables.join(', '));

      // 检查配置数量
      const configCount = await this.dbAdapter.query(
        'SELECT COUNT(*) as count FROM config_items'
      );
      console.log(`⚙️ 配置项数量: ${configCount[0].count}`);

      // 检查用户数量
      const userCount = await this.dbAdapter.query(
        'SELECT COUNT(*) as count FROM users'
      );
      console.log(`👥 用户数量: ${userCount[0].count}`);

      return true;
    } catch (error) {
      console.error('❌ 数据库验证失败:', error.message);
      throw error;
    }
  }

  /**
   * 生成唯一ID
   */
  generateId() {
    return Date.now().toString(36) + Math.random().toString(36).substr(2);
  }

  /**
   * 关闭数据库连接
   */
  async close() {
    if (this.dbAdapter) {
      await this.dbAdapter.close();
    }
  }
}

// 如果直接运行此脚本
if (require.main === module) {
  const initializer = new DatabaseInitializer();
  
  initializer.initialize()
    .then((success) => {
      if (success) {
        console.log('\n✅ 数据库初始化成功！');
        process.exit(0);
      } else {
        console.log('\n❌ 数据库初始化失败！');
        process.exit(1);
      }
    })
    .catch((error) => {
      console.error('\n💥 初始化过程中发生错误:', error);
      process.exit(1);
    })
    .finally(() => {
      initializer.close();
    });
}

module.exports = DatabaseInitializer;
