/**
 * 简化诊断脚本
 */

require('dotenv').config();

console.log('🚀 开始简化诊断...');

// 1. 检查环境变量
console.log('\n📋 环境变量:');
console.log('NODE_ENV:', process.env.NODE_ENV);
console.log('PORT:', process.env.PORT);
console.log('INTERNAL_AI_API_KEY:', process.env.INTERNAL_AI_API_KEY ? '已设置' : '未设置');
console.log('EXTERNAL_AI_API_KEY:', process.env.EXTERNAL_AI_API_KEY ? '已设置' : '未设置');

// 2. 检查数据库
console.log('\n💾 检查数据库...');
try {
  const Database = require('better-sqlite3');
  const db = new Database('./data/qms.db');
  const result = db.prepare('SELECT COUNT(*) as count FROM sqlite_master WHERE type="table"').get();
  console.log('✅ 数据库连接成功，表数量:', result.count);
  db.close();
} catch (error) {
  console.error('❌ 数据库错误:', error.message);
}

// 3. 测试AI API（使用axios）
console.log('\n🔍 测试AI API...');

async function testAPIs() {
  const axios = require('axios');
  
  // 测试内部API
  if (process.env.INTERNAL_AI_API_KEY) {
    try {
      console.log('测试内部API...');
      const response = await axios.post(
        'https://hk-intra-paas.transsion.com/tranai-proxy/v1/chat/completions',
        {
          model: 'gpt-4o',
          messages: [{ role: 'user', content: '测试' }],
          max_tokens: 10
        },
        {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${process.env.INTERNAL_AI_API_KEY}`
          },
          timeout: 10000
        }
      );
      console.log('✅ 内部API连接成功');
    } catch (error) {
      console.log('❌ 内部API失败:', error.response?.status, error.message);
    }
  }
  
  // 测试外部API
  if (process.env.EXTERNAL_AI_API_KEY) {
    try {
      console.log('测试外部API...');
      const response = await axios.post(
        'https://api.deepseek.com/chat/completions',
        {
          model: 'deepseek-chat',
          messages: [{ role: 'user', content: '测试' }],
          max_tokens: 10
        },
        {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${process.env.EXTERNAL_AI_API_KEY}`
          },
          timeout: 10000
        }
      );
      console.log('✅ 外部API连接成功');
    } catch (error) {
      console.log('❌ 外部API失败:', error.response?.status, error.message);
    }
  }
}

testAPIs().then(() => {
  console.log('\n✅ 诊断完成');
}).catch(error => {
  console.error('❌ 诊断异常:', error.message);
});
