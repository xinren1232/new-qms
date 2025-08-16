// 验证DeepSeek配置
const fs = require('fs');

console.log('🔍 验证DeepSeek配置...\n');

// 读取chat-service.js文件
const chatServiceContent = fs.readFileSync('./chat-service.js', 'utf8');

// 检查是否包含新的DeepSeek配置
const hasDeepSeekChat = chatServiceContent.includes("'deepseek-chat':");
const hasDeepSeekReasoner = chatServiceContent.includes("'deepseek-reasoner':");
const hasExternalFlag = chatServiceContent.includes('external: true');
const hasCorrectApiKey = chatServiceContent.includes('sk-cab797574abf4288bcfaca253191565d');
const hasCorrectBaseURL = chatServiceContent.includes('https://api.deepseek.com');

console.log('📋 配置检查结果:');
console.log(`   DeepSeek Chat 配置: ${hasDeepSeekChat ? '✅ 存在' : '❌ 缺失'}`);
console.log(`   DeepSeek Reasoner 配置: ${hasDeepSeekReasoner ? '✅ 存在' : '❌ 缺失'}`);
console.log(`   外网标识: ${hasExternalFlag ? '✅ 存在' : '❌ 缺失'}`);
console.log(`   API Key: ${hasCorrectApiKey ? '✅ 正确' : '❌ 错误'}`);
console.log(`   Base URL: ${hasCorrectBaseURL ? '✅ 正确' : '❌ 错误'}`);

console.log('\n📄 DeepSeek配置详情:');

// 提取DeepSeek配置部分
const deepseekChatMatch = chatServiceContent.match(/'deepseek-chat':\s*{[^}]+}/s);
const deepseekReasonerMatch = chatServiceContent.match(/'deepseek-reasoner':\s*{[^}]+}/s);

if (deepseekChatMatch) {
  console.log('🔧 DeepSeek Chat 配置:');
  console.log(deepseekChatMatch[0]);
  console.log('');
}

if (deepseekReasonerMatch) {
  console.log('🔧 DeepSeek Reasoner 配置:');
  console.log(deepseekReasonerMatch[0]);
  console.log('');
}

// 检查AI_CONFIGS对象的完整性
const aiConfigsMatch = chatServiceContent.match(/const AI_CONFIGS = {([\s\S]*?)};/);
if (aiConfigsMatch) {
  const configContent = aiConfigsMatch[1];
  const modelCount = (configContent.match(/'\w+[-\w]*':\s*{/g) || []).length;
  console.log(`📊 总模型数量: ${modelCount}`);
  
  // 列出所有模型
  const modelMatches = configContent.match(/'\w+[-\w]*':/g) || [];
  console.log('📝 所有模型:');
  modelMatches.forEach((match, index) => {
    const modelName = match.replace(/[':]/g, '');
    console.log(`   ${index + 1}. ${modelName}`);
  });
}

console.log('\n✅ 配置验证完成!');

// 生成测试命令
console.log('\n🧪 测试命令:');
console.log('测试DeepSeek Chat:');
console.log('curl -X POST http://localhost:3003/api/chat/send -H "Content-Type: application/json" -d \'{"message":"你好","model_config":{"apiKey":"sk-cab797574abf4288bcfaca253191565d","baseURL":"https://api.deepseek.com","model":"deepseek-chat","name":"DeepSeek Chat"}}\'');

console.log('\n测试DeepSeek Reasoner:');
console.log('curl -X POST http://localhost:3003/api/chat/send -H "Content-Type: application/json" -d \'{"message":"分析一个质量问题","model_config":{"apiKey":"sk-cab797574abf4288bcfaca253191565d","baseURL":"https://api.deepseek.com","model":"deepseek-reasoner","name":"DeepSeek Reasoner"}}\'');
