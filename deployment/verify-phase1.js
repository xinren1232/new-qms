const http = require('http');

console.log('🔍 第一阶段功能验证\n');

async function verifyPhase1() {
  console.log('='.repeat(50));
  console.log('🚀 第一阶段功能验证');
  console.log('📊 验证范围: 智能分析 + 推荐功能');
  console.log('='.repeat(50));
  
  let passedChecks = 0;
  let totalChecks = 0;
  
  // 1. 服务可用性检查
  console.log('\n1️⃣ 服务可用性检查...');
  totalChecks++;
  if (await checkServiceHealth()) {
    passedChecks++;
  }
  
  // 2. 核心功能检查
  console.log('\n2️⃣ 核心功能检查...');
  
  const functions = [
    { name: '主题分析', endpoint: '/api/analytics/topics' },
    { name: '行为分析', endpoint: '/api/analytics/behavior' },
    { name: '情感分析', endpoint: '/api/analytics/sentiment' },
    { name: '个性化推荐', endpoint: '/api/recommendations/personalized?limit=3' },
    { name: '热门推荐', endpoint: '/api/recommendations/popular?limit=5' }
  ];
  
  for (const func of functions) {
    console.log(`   🔍 检查${func.name}...`);
    totalChecks++;
    if (await checkFunction(func.endpoint)) {
      console.log(`   ✅ ${func.name}功能正常`);
      passedChecks++;
    } else {
      console.log(`   ❌ ${func.name}功能异常`);
    }
  }
  
  // 3. 性能检查
  console.log('\n3️⃣ 性能检查...');
  totalChecks++;
  if (await checkPerformance()) {
    passedChecks++;
  }
  
  // 4. 数据完整性检查
  console.log('\n4️⃣ 数据完整性检查...');
  totalChecks++;
  if (await checkDataIntegrity()) {
    passedChecks++;
  }
  
  // 验证结果
  console.log('\n' + '='.repeat(50));
  console.log('📋 第一阶段验证结果');
  console.log('='.repeat(50));
  
  const successRate = ((passedChecks / totalChecks) * 100).toFixed(1);
  console.log(`✅ 通过检查: ${passedChecks}/${totalChecks} (${successRate}%)`);
  
  if (passedChecks === totalChecks) {
    console.log('\n🎉 第一阶段验证通过！');
    console.log('✅ 系统已准备好为用户提供服务');
    console.log('\n📈 可用功能:');
    console.log('• 📊 智能分析: 深度分析用户对话数据');
    console.log('• 🤖 智能推荐: 个性化问题推荐');
    console.log('• 🔄 缓存优化: 快速响应用户请求');
    console.log('• 📊 使用统计: 实时监控系统状态');
    
    console.log('\n🌐 访问地址:');
    console.log('• 服务健康检查: http://localhost:3010/health');
    console.log('• 第一阶段统计: http://localhost:3010/api/phase1/stats');
    
    console.log('\n⏭️ 下一步:');
    console.log('• 可以开始第二阶段部署 (团队协作功能)');
    console.log('• 或继续优化第一阶段功能');
  } else {
    console.log('\n⚠️ 部分功能验证失败');
    console.log('🔧 建议检查:');
    console.log('• 服务是否正常启动 (端口3010)');
    console.log('• 依赖包是否完整安装');
    console.log('• 数据库是否正常连接');
    console.log('• 网络连接是否正常');
  }
}

// 检查服务健康状态
async function checkServiceHealth() {
  try {
    const result = await makeRequest('GET', 'localhost', 3010, '/health');
    if (result.status === 'ok' && result.phase === 'Phase 1') {
      console.log('   ✅ 服务健康状态正常');
      console.log(`   📍 服务版本: ${result.phase}`);
      console.log(`   🕐 响应时间: ${new Date(result.timestamp).toLocaleString()}`);
      return true;
    } else {
      console.log('   ❌ 服务健康检查失败: 响应格式异常');
      return false;
    }
  } catch (error) {
    console.log(`   ❌ 服务健康检查失败: 无法连接到服务 (${error.message})`);
    console.log('   💡 请确保第一阶段服务已启动: node deploy-phase1.js');
    return false;
  }
}

// 检查功能可用性
async function checkFunction(endpoint) {
  try {
    const result = await makeRequest('GET', 'localhost', 3010, endpoint, {
      'user-id': 'verify_user',
      'user-name': 'verify_user'
    });
    
    return result.success === true;
  } catch (error) {
    return false;
  }
}

// 性能检查
async function checkPerformance() {
  try {
    console.log('   🚀 执行性能测试...');
    
    const startTime = Date.now();
    
    // 并发请求测试
    const promises = [];
    for (let i = 0; i < 3; i++) {
      promises.push(makeRequest('GET', 'localhost', 3010, '/api/recommendations/popular?limit=3', {
        'user-id': `perf_test_${i}`,
        'user-name': `perf_test_${i}`
      }));
    }
    
    const results = await Promise.all(promises);
    const endTime = Date.now();
    const totalTime = endTime - startTime;
    
    const successCount = results.filter(r => r.success).length;
    const avgResponseTime = totalTime / 3;
    
    console.log(`   📊 并发测试: ${successCount}/3 成功`);
    console.log(`   ⏱️ 平均响应时间: ${avgResponseTime.toFixed(1)}ms`);
    
    // 性能标准：平均响应时间 < 1000ms，成功率 > 80%
    if (avgResponseTime < 1000 && successCount >= 2) {
      console.log('   ✅ 性能检查通过');
      return true;
    } else {
      console.log('   ❌ 性能检查未达标');
      return false;
    }
  } catch (error) {
    console.log(`   ❌ 性能检查失败: ${error.message}`);
    return false;
  }
}

// 数据完整性检查
async function checkDataIntegrity() {
  try {
    console.log('   🔍 检查数据完整性...');
    
    // 检查第一阶段统计数据
    const statsResult = await makeRequest('GET', 'localhost', 3010, '/api/phase1/stats', {
      'user-id': 'verify_user',
      'user-name': 'verify_user'
    });
    
    if (statsResult.success && statsResult.data) {
      const data = statsResult.data;
      
      // 验证数据结构
      const hasValidStructure = 
        data.phase === 'Phase 1' &&
        data.user_stats &&
        data.analytics_stats &&
        data.recommendation_stats;
      
      if (hasValidStructure) {
        console.log('   ✅ 数据结构完整');
        console.log(`   📊 用户对话: ${data.user_stats.total_conversations}个`);
        console.log(`   📈 分析主题: ${data.analytics_stats.topics_count}个`);
        console.log(`   🤖 推荐数量: ${data.recommendation_stats.personalized_count}个`);
        return true;
      } else {
        console.log('   ❌ 数据结构不完整');
        return false;
      }
    } else {
      console.log('   ❌ 无法获取统计数据');
      return false;
    }
  } catch (error) {
    console.log(`   ❌ 数据完整性检查失败: ${error.message}`);
    return false;
  }
}

// HTTP请求封装
function makeRequest(method, hostname, port, path, headers = {}, data = null) {
  return new Promise((resolve, reject) => {
    const options = {
      hostname,
      port,
      path,
      method,
      headers: {
        'Content-Type': 'application/json',
        ...headers
      },
      timeout: 5000 // 5秒超时
    };

    const req = http.request(options, (res) => {
      let responseData = '';
      res.on('data', (chunk) => responseData += chunk);
      res.on('end', () => {
        try {
          const result = JSON.parse(responseData);
          resolve(result);
        } catch (error) {
          reject(new Error('响应解析失败: ' + error.message));
        }
      });
    });

    req.on('error', (error) => {
      reject(new Error('请求失败: ' + error.message));
    });

    req.on('timeout', () => {
      req.destroy();
      reject(new Error('请求超时'));
    });

    if (data) {
      req.write(data);
    }
    req.end();
  });
}

// 运行验证
verifyPhase1().catch(console.error);
