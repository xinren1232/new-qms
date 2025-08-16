/**
 * 直接修复数据库表结构
 */

const fs = require('fs');
const path = require('path');

// 使用child_process执行sqlite3命令
const { exec } = require('child_process');

const dbPath = path.join(__dirname, 'database', 'chat_history.db');

console.log('🔧 开始直接修复数据库表结构...');
console.log('📍 数据库路径:', dbPath);

// 检查数据库文件是否存在
if (!fs.existsSync(dbPath)) {
  console.log('📁 数据库文件不存在，将创建新的数据库文件');
}

// SQL修复命令
const sqlCommands = [
  // 添加last_login_at字段（如果不存在）
  "ALTER TABLE users ADD COLUMN last_login_at DATETIME;",
  
  // 创建conversations表
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
  );`,
  
  // 创建messages表
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
  );`,
  
  // 创建system_logs表
  `CREATE TABLE IF NOT EXISTS system_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    level TEXT NOT NULL,
    message TEXT NOT NULL,
    source TEXT,
    user_id TEXT,
    metadata TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
  );`,
  
  // 创建索引
  "CREATE INDEX IF NOT EXISTS idx_conversations_user_created ON conversations(user_id, created_at DESC);",
  "CREATE INDEX IF NOT EXISTS idx_conversations_updated ON conversations(updated_at DESC);",
  "CREATE INDEX IF NOT EXISTS idx_conversations_status ON conversations(is_deleted);",
  "CREATE INDEX IF NOT EXISTS idx_messages_conversation_created ON messages(conversation_id, created_at ASC);",
  "CREATE INDEX IF NOT EXISTS idx_messages_user_created ON messages(user_id, created_at DESC);",
  "CREATE INDEX IF NOT EXISTS idx_messages_role ON messages(message_type);",
  "CREATE INDEX IF NOT EXISTS idx_logs_level_created ON system_logs(level, created_at DESC);",
  "CREATE INDEX IF NOT EXISTS idx_logs_source ON system_logs(source);"
];

// 执行SQL命令的函数
function executeSqlCommand(command) {
  return new Promise((resolve, reject) => {
    const sqliteCommand = `sqlite3 "${dbPath}" "${command}"`;
    exec(sqliteCommand, (error, stdout, stderr) => {
      if (error) {
        // 忽略"duplicate column name"错误
        if (error.message.includes('duplicate column name') || 
            error.message.includes('already exists')) {
          console.log('⚠️ 跳过已存在的结构:', error.message.split('\n')[0]);
          resolve();
        } else {
          console.error('❌ SQL执行失败:', error.message);
          reject(error);
        }
      } else {
        console.log('✅ SQL执行成功');
        resolve(stdout);
      }
    });
  });
}

// 主修复函数
async function fixDatabase() {
  try {
    console.log('🚀 开始执行数据库修复...');
    
    for (let i = 0; i < sqlCommands.length; i++) {
      const command = sqlCommands[i];
      console.log(`\n[${i + 1}/${sqlCommands.length}] 执行SQL命令...`);
      console.log('📝', command.substring(0, 50) + '...');
      
      await executeSqlCommand(command);
    }
    
    console.log('\n🎉 数据库表结构修复完成！');
    
    // 验证表结构
    console.log('\n🔍 验证表结构...');
    await executeSqlCommand('.tables');
    
  } catch (error) {
    console.error('❌ 数据库修复失败:', error.message);
  }
}

// 运行修复
fixDatabase();
