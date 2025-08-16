const http = require('http');

function checkService() {
  console.log('🔍 检查AI聊天服务状态...\n');
  
  // 检查健康状态
  const healthReq = http.request({
    hostname: 'localhost',
    port: 3003,
    path: '/health',
    method: 'GET'
  }, (res) => {
    let data = '';
    res.on('data', (chunk) => {
      data += chunk;
    });
    res.on('end', () => {
      console.log('✅ 健康检查通过');
      console.log('响应:', data);
      console.log('');
      
      // 检查模型列表
      checkModels();
    });
  });
  
  healthReq.on('error', (err) => {
    console.log('❌ 健康检查失败:', err.message);
  });
  
  healthReq.end();
}

function checkModels() {
  console.log('📋 检查模型列表...');
  
  const modelsReq = http.request({
    hostname: 'localhost',
    port: 3003,
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
          });
          
          // 检查DeepSeek模型
          const deepseekChat = result.data.models.find(m => m.id === 'deepseek-chat');
          const deepseekReasoner = result.data.models.find(m => m.id === 'deepseek-reasoner');
          
          console.log('\n🔍 DeepSeek模型检查:');
          console.log(`DeepSeek Chat: ${deepseekChat ? '✅ 已添加' : '❌ 未找到'}`);
          console.log(`DeepSeek Reasoner: ${deepseekReasoner ? '✅ 已添加' : '❌ 未找到'}`);
          
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

checkService();
