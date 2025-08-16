/**
 * 检查后端服务状态
 */

const http = require('http');

const services = [
  { name: '配置中心', url: 'http://localhost:3003/health', type: 'backend' },
  { name: '聊天服务', url: 'http://localhost:3004/health', type: 'backend' },
  { name: '认证服务', url: 'http://localhost:8084/health', type: 'backend' },
  { name: '高级功能', url: 'http://localhost:3009/health', type: 'backend' },
  { name: '配置端', url: 'http://localhost:8072/', type: 'frontend' },
  { name: '应用端', url: 'http://localhost:8080/', type: 'frontend' }
];

async function checkService(service) {
  return new Promise((resolve) => {
    const req = http.get(service.url, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        console.log(`✅ ${service.name}: ${res.statusCode} - ${data.substring(0, 100)}`);
        resolve(true);
      });
    });
    req.on('error', (err) => {
      console.log(`❌ ${service.name}: ${err.message}`);
      resolve(false);
    });
    req.setTimeout(5000, () => {
      console.log(`⏰ ${service.name}: 超时`);
      req.destroy();
      resolve(false);
    });
  });
}

async function checkAll() {
  console.log('🔍 检查QMS-AI系统完整状态...\n');

  const backendServices = services.filter(s => s.type === 'backend');
  const frontendServices = services.filter(s => s.type === 'frontend');

  console.log('🔧 后端服务检查:');
  let backendHealthy = 0;
  for (const service of backendServices) {
    const isHealthy = await checkService(service);
    if (isHealthy) backendHealthy++;
  }

  console.log('\n🌐 前端服务检查:');
  let frontendHealthy = 0;
  for (const service of frontendServices) {
    const isHealthy = await checkService(service);
    if (isHealthy) frontendHealthy++;
  }

  const totalHealthy = backendHealthy + frontendHealthy;

  console.log(`\n📊 系统状态总结:`);
  console.log(`   后端服务: ${backendHealthy}/${backendServices.length} 正常`);
  console.log(`   前端服务: ${frontendHealthy}/${frontendServices.length} 正常`);
  console.log(`   总计: ${totalHealthy}/${services.length} 个服务正常运行`);

  if (totalHealthy === services.length) {
    console.log('\n🎉 QMS-AI系统完全启动成功！');
    console.log('📍 访问地址:');
    console.log('   配置端: http://localhost:8072/alm-transcend-configcenter-web/');
    console.log('   应用端: http://localhost:8080/');
  } else {
    console.log('\n⚠️ 部分服务未正常运行，请检查日志');
  }
}

checkAll();
