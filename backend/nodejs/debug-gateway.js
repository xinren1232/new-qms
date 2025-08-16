console.log('🔍 开始调试API网关...');

try {
  console.log('1. 加载基础模块...');
  const express = require('express');
  console.log('✅ Express 加载成功');
  
  const cors = require('cors');
  console.log('✅ CORS 加载成功');
  
  const path = require('path');
  console.log('✅ Path 加载成功');
  
  const fs = require('fs');
  console.log('✅ FS 加载成功');
  
  const compression = require('compression');
  console.log('✅ Compression 加载成功');
  
  const helmet = require('helmet');
  console.log('✅ Helmet 加载成功');
  
  const http = require('http');
  console.log('✅ HTTP 加载成功');
  
  const WebSocket = require('ws');
  console.log('✅ WebSocket 加载成功');
  
  console.log('2. 创建Express应用...');
  const app = express();
  const server = http.createServer(app);
  console.log('✅ Express应用和HTTP服务器创建成功');
  
  console.log('3. 配置中间件...');
  app.use(helmet({
    contentSecurityPolicy: false
  }));
  app.use(compression());
  app.use(cors());
  app.use(express.json({ limit: '60mb' }));
  app.use(express.urlencoded({ extended: true, limit: '60mb' }));
  console.log('✅ 中间件配置成功');
  
  console.log('4. 配置路由...');
  app.get('/health', (req, res) => {
    res.json({
      status: 'ok',
      timestamp: new Date().toISOString(),
      message: 'QMS API网关运行正常'
    });
  });
  console.log('✅ 健康检查路由配置成功');
  
  console.log('5. 启动服务器...');
  const PORT = process.env.GATEWAY_PORT || 8085;
  
  server.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 调试版API网关启动成功！`);
    console.log(`📍 地址: http://localhost:${PORT}`);
    console.log(`🔧 健康检查: http://localhost:${PORT}/health`);
  });
  
  server.on('error', (error) => {
    console.error(`❌ 服务器启动失败:`, error.message);
    process.exit(1);
  });
  
} catch (error) {
  console.error('❌ 调试过程中发生错误:', error.message);
  console.error('错误堆栈:', error.stack);
  process.exit(1);
}
