/**
 * 简单的数据库修复脚本
 */

const fs = require('fs');
const path = require('path');

// 使用现有的数据库连接
const ChatHistoryDB = require('./database/chat-history-db');

async function fixDatabase() {
  console.log('🔧 开始修复数据库表结构...');
  
  const db = new ChatHistoryDB();
  
  // 等待数据库初始化
  await new Promise(resolve => setTimeout(resolve, 1000));
  
  try {
    // 1. 添加last_login_at字段
    console.log('📝 添加 last_login_at 字段...');
    await new Promise((resolve, reject) => {
      db.db.run('ALTER TABLE users ADD COLUMN last_login_at DATETIME', (err) => {
        if (err && !err.message.includes('duplicate column name')) {
          console.error('❌ 添加字段失败:', err.message);
          reject(err);
        } else {
          console.log('✅ last_login_at 字段处理完成');
          resolve();
        }
      });
    });

    // 2. 创建conversations表
    console.log('📝 创建 conversations 表...');
    await new Promise((resolve, reject) => {
      db.db.run(`CREATE TABLE IF NOT EXISTS conversations (
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
      )`, (err) => {
        if (err) {
          console.error('❌ 创建conversations表失败:', err.message);
          reject(err);
        } else {
          console.log('✅ conversations 表创建成功');
          resolve();
        }
      });
    });

    // 3. 创建messages表
    console.log('📝 创建 messages 表...');
    await new Promise((resolve, reject) => {
      db.db.run(`CREATE TABLE IF NOT EXISTS messages (
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
      )`, (err) => {
        if (err) {
          console.error('❌ 创建messages表失败:', err.message);
          reject(err);
        } else {
          console.log('✅ messages 表创建成功');
          resolve();
        }
      });
    });

    // 4. 创建system_logs表
    console.log('📝 创建 system_logs 表...');
    await new Promise((resolve, reject) => {
      db.db.run(`CREATE TABLE IF NOT EXISTS system_logs (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        level TEXT NOT NULL,
        message TEXT NOT NULL,
        source TEXT,
        user_id TEXT,
        metadata TEXT,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES users(id)
      )`, (err) => {
        if (err) {
          console.error('❌ 创建system_logs表失败:', err.message);
          reject(err);
        } else {
          console.log('✅ system_logs 表创建成功');
          resolve();
        }
      });
    });

    // 5. 创建索引
    console.log('📝 创建索引...');
    const indexes = [
      'CREATE INDEX IF NOT EXISTS idx_conversations_user_created ON conversations(user_id, created_at DESC)',
      'CREATE INDEX IF NOT EXISTS idx_conversations_updated ON conversations(updated_at DESC)',
      'CREATE INDEX IF NOT EXISTS idx_conversations_status ON conversations(is_deleted)',
      'CREATE INDEX IF NOT EXISTS idx_messages_conversation_created ON messages(conversation_id, created_at ASC)',
      'CREATE INDEX IF NOT EXISTS idx_messages_user_created ON messages(user_id, created_at DESC)',
      'CREATE INDEX IF NOT EXISTS idx_messages_role ON messages(message_type)',
      'CREATE INDEX IF NOT EXISTS idx_messages_model ON messages(model_info)',
      'CREATE INDEX IF NOT EXISTS idx_logs_level_created ON system_logs(level, created_at DESC)',
      'CREATE INDEX IF NOT EXISTS idx_logs_source ON system_logs(source)'
    ];

    for (const indexSql of indexes) {
      await new Promise((resolve) => {
        db.db.run(indexSql, (err) => {
          if (err) {
            console.log('⚠️ 索引创建跳过:', err.message);
          } else {
            console.log('✅ 索引创建成功');
          }
          resolve();
        });
      });
    }

    console.log('🎉 数据库表结构修复完成！');
    
  } catch (error) {
    console.error('❌ 数据库修复失败:', error.message);
  } finally {
    // 关闭数据库连接
    if (db.db) {
      db.db.close();
    }
  }
}

// 运行修复
fixDatabase();
