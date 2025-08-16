/**
 * 数据库表结构修复脚本
 * 用于修复缺失的表和字段
 */

const sqlite3 = require('sqlite3').verbose();
const path = require('path');

class DatabaseSchemaFixer {
  constructor() {
    this.dbPath = path.join(__dirname, 'database', 'chat_history.db');
    this.db = null;
  }

  async initialize() {
    return new Promise((resolve, reject) => {
      this.db = new sqlite3.Database(this.dbPath, (err) => {
        if (err) {
          console.error('❌ 数据库连接失败:', err.message);
          reject(err);
        } else {
          console.log('✅ 数据库连接成功');
          resolve();
        }
      });
    });
  }

  async fixUserTable() {
    console.log('🔧 修复用户表结构...');
    
    // 检查是否存在last_login_at字段
    const checkColumn = `PRAGMA table_info(users)`;
    
    return new Promise((resolve, reject) => {
      this.db.all(checkColumn, (err, columns) => {
        if (err) {
          reject(err);
          return;
        }

        const hasLastLoginAt = columns.some(col => col.name === 'last_login_at');
        
        if (!hasLastLoginAt) {
          console.log('📝 添加 last_login_at 字段...');
          this.db.run(
            'ALTER TABLE users ADD COLUMN last_login_at DATETIME',
            (err) => {
              if (err) {
                console.error('❌ 添加字段失败:', err.message);
                reject(err);
              } else {
                console.log('✅ last_login_at 字段添加成功');
                resolve();
              }
            }
          );
        } else {
          console.log('✅ last_login_at 字段已存在');
          resolve();
        }
      });
    });
  }

  async createMissingTables() {
    console.log('🔧 创建缺失的表...');
    
    const tables = [
      // 确保conversations表存在（使用正确的表名）
      {
        name: 'conversations',
        sql: `CREATE TABLE IF NOT EXISTS conversations (
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
        )`
      },
      
      // 确保messages表存在
      {
        name: 'messages',
        sql: `CREATE TABLE IF NOT EXISTS messages (
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
        )`
      },
      
      // 系统日志表
      {
        name: 'system_logs',
        sql: `CREATE TABLE IF NOT EXISTS system_logs (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          level TEXT NOT NULL,
          message TEXT NOT NULL,
          source TEXT,
          user_id TEXT,
          metadata TEXT,
          created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
          FOREIGN KEY (user_id) REFERENCES users(id)
        )`
      }
    ];

    for (const table of tables) {
      await this.createTable(table.name, table.sql);
    }
  }

  async createTable(tableName, sql) {
    return new Promise((resolve, reject) => {
      this.db.run(sql, (err) => {
        if (err) {
          console.error(`❌ 创建表 ${tableName} 失败:`, err.message);
          reject(err);
        } else {
          console.log(`✅ 表 ${tableName} 创建成功`);
          resolve();
        }
      });
    });
  }

  async createIndexes() {
    console.log('🔧 创建索引...');
    
    const indexes = [
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

    for (const indexSql of indexes) {
      await this.createIndex(indexSql);
    }
  }

  async createIndex(sql) {
    return new Promise((resolve, reject) => {
      this.db.run(sql, (err) => {
        if (err) {
          console.error('❌ 创建索引失败:', err.message);
          // 不要因为索引创建失败而中断，继续执行
          resolve();
        } else {
          console.log('✅ 索引创建成功');
          resolve();
        }
      });
    });
  }

  async close() {
    return new Promise((resolve) => {
      if (this.db) {
        this.db.close((err) => {
          if (err) {
            console.error('❌ 关闭数据库失败:', err.message);
          } else {
            console.log('✅ 数据库连接已关闭');
          }
          resolve();
        });
      } else {
        resolve();
      }
    });
  }

  async fixAll() {
    try {
      await this.initialize();
      await this.fixUserTable();
      await this.createMissingTables();
      await this.createIndexes();
      console.log('🎉 数据库表结构修复完成！');
    } catch (error) {
      console.error('❌ 数据库修复失败:', error.message);
      throw error;
    } finally {
      await this.close();
    }
  }
}

// 如果直接运行此脚本
if (require.main === module) {
  const fixer = new DatabaseSchemaFixer();
  fixer.fixAll().catch(console.error);
}

module.exports = DatabaseSchemaFixer;
