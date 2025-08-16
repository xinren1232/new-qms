const http = require('http');

async function checkModelsData() {
  console.log('🔍 检查模型数据结构...\n');
  
  try {
    const response = await new Promise((resolve, reject) => {
      const req = http.request({
        hostname: 'localhost',
        port: 3004,
        path: '/api/chat/models',
        method: 'GET'
      }, (res) => {
        let data = '';
        res.on('data', (chunk) => data += chunk);
        res.on('end', () => resolve(JSON.parse(data)));
      });
      req.on('error', reject);
      req.end();
    });
    
    if (response.success) {
      console.log('✅ 模型数据获取成功\n');
      console.log(`📊 模型总数: ${response.data.models.length}\n`);
      
      response.data.models.forEach((model, index) => {
        console.log(`${index + 1}. ${model.name}`);
        console.log(`   ID: ${model.provider}`);
        console.log(`   模型: ${model.model}`);
        console.log(`   BaseURL: ${model.baseURL}`);
        console.log(`   Features: ${JSON.stringify(model.features || 'undefined')}`);
        console.log('');
      });
      
      // 检查外网可用模型
      const externalModels = response.data.models.filter(m => m.features && m.features.external);
      console.log(`🌐 外网可用模型数量: ${externalModels.length}`);
      externalModels.forEach(model => {
        console.log(`   - ${model.name}`);
      });
      
    } else {
      console.log('❌ 模型数据获取失败:', response.message);
    }
    
  } catch (error) {
    console.error('❌ 请求失败:', error.message);
  }
}

checkModelsData();
