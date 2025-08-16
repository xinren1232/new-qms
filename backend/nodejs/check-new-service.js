const http = require('http');

function checkNewService() {
  console.log('🔍 检查新AI聊天服务状态 (端口3004)...\n');
  
  // 检查模型列表
  const modelsReq = http.request({
    hostname: 'localhost',
    port: 3004,
    path: '/api/models',
    method: 'GET'
  }, (res) => {
    let data = '';
    res.on('data', (chunk) => {
      data += chunk;
    });
    res.on('end', () => {
      try {
        const result = JSON.parse(data);
        if (result.success) {
          console.log(`✅ 模型列表获取成功，共${result.data.models.length}个模型:`);
          result.data.models.forEach((model, index) => {
            console.log(`${index + 1}. ${model.name} (${model.id})`);
            console.log(`   提供商: ${model.provider}`);
            console.log(`   模型: ${model.model}`);
            console.log(`   功能: ${JSON.stringify(model.features)}`);
            console.log('');
          });
          
          // 检查DeepSeek模型
          const deepseekChat = result.data.models.find(m => m.id === 'deepseek-chat');
          const deepseekReasoner = result.data.models.find(m => m.id === 'deepseek-reasoner');
          
          console.log('🔍 DeepSeek模型检查:');
          console.log(`DeepSeek Chat: ${deepseekChat ? '✅ 已添加' : '❌ 未找到'}`);
          console.log(`DeepSeek Reasoner: ${deepseekReasoner ? '✅ 已添加' : '❌ 未找到'}`);
          
          if (deepseekChat) {
            console.log('\n📋 DeepSeek Chat详情:');
            console.log(`   名称: ${deepseekChat.name}`);
            console.log(`   提供商: ${deepseekChat.provider}`);
            console.log(`   模型: ${deepseekChat.model}`);
            console.log(`   功能: ${JSON.stringify(deepseekChat.features)}`);
          }
          
          if (deepseekReasoner) {
            console.log('\n📋 DeepSeek Reasoner详情:');
            console.log(`   名称: ${deepseekReasoner.name}`);
            console.log(`   提供商: ${deepseekReasoner.provider}`);
            console.log(`   模型: ${deepseekReasoner.model}`);
            console.log(`   功能: ${JSON.stringify(deepseekReasoner.features)}`);
          }
          
        } else {
          console.log('❌ 模型列表获取失败:', result.message);
        }
      } catch (error) {
        console.log('❌ 解析响应失败:', error.message);
        console.log('原始响应:', data);
      }
    });
  });
  
  modelsReq.on('error', (err) => {
    console.log('❌ 模型列表请求失败:', err.message);
  });
  
  modelsReq.end();
}

checkNewService();
