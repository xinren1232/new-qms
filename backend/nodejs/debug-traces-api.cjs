const axios = require('axios');

async function debugTracesAPI() {
  console.log('🔍 开始调试 Traces API...\n');
  
  // 测试1: 直接访问 coze-studio 服务
  console.log('📍 测试1: 直接访问 coze-studio 服务');
  try {
    const response = await axios.post('http://localhost:3005/api/traces/append', {
      conversation_id: 'debug_test_' + Date.now(),
      trace: {
        step: 'debug',
        title: '调试测试',
        status: 'completed'
      }
    }, {
      headers: {
        'Content-Type': 'application/json',
        'user-id': 'debug-user'
      },
      timeout: 5000
    });
    
    console.log('✅ 直接访问成功:', response.status, response.data);
  } catch (error) {
    console.error('❌ 直接访问失败:');
    if (error.response) {
      console.error('   状态码:', error.response.status);
      console.error('   响应头:', error.response.headers);
      console.error('   响应数据:', error.response.data);
    } else if (error.request) {
      console.error('   请求错误:', error.message);
    } else {
      console.error('   其他错误:', error.message);
    }
  }
  
  console.log('\n' + '='.repeat(50) + '\n');
  
  // 测试2: 通过前端代理访问
  console.log('📍 测试2: 通过前端代理访问');
  try {
    const response = await axios.post('http://localhost:8081/api/traces/append', {
      conversation_id: 'proxy_test_' + Date.now(),
      trace: {
        step: 'proxy',
        title: '代理测试',
        status: 'completed'
      }
    }, {
      headers: {
        'Content-Type': 'application/json',
        'user-id': 'proxy-user'
      },
      timeout: 5000
    });
    
    console.log('✅ 代理访问成功:', response.status, response.data);
  } catch (error) {
    console.error('❌ 代理访问失败:');
    if (error.response) {
      console.error('   状态码:', error.response.status);
      console.error('   响应头:', error.response.headers);
      console.error('   响应数据:', error.response.data);
    } else if (error.request) {
      console.error('   请求错误:', error.message);
    } else {
      console.error('   其他错误:', error.message);
    }
  }
  
  console.log('\n' + '='.repeat(50) + '\n');
  
  // 测试3: 检查健康状态
  console.log('📍 测试3: 检查服务健康状态');
  try {
    const healthResponse = await axios.get('http://localhost:3005/health', { timeout: 5000 });
    console.log('✅ coze-studio 服务健康:', healthResponse.data);
  } catch (error) {
    console.error('❌ coze-studio 服务不健康:', error.message);
  }
  
  try {
    const proxyHealthResponse = await axios.get('http://localhost:8081/health', { timeout: 5000 });
    console.log('✅ 前端代理健康:', proxyHealthResponse.data);
  } catch (error) {
    console.error('❌ 前端代理不健康:', error.message);
  }
  
  console.log('\n🎯 调试完成！');
}

debugTracesAPI();
