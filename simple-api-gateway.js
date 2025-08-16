/**
 * 简化版QMS API网关 - 用于测试启动问题
 */

const express = require('express');
const cors = require('cors');
const http = require('http');

const app = express();
const server = http.createServer(app);
const PORT = process.env.GATEWAY_PORT || 8085;

console.log('🌐 启动简化版QMS API网关服务...');

// 基础中间件
app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// 健康检查
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    timestamp: new Date().toISOString(),
    message: '简化版API网关运行正常',
    services: {
      chat: 'http://localhost:3004',
      config: 'http://localhost:3003',
      auth: 'http://localhost:8084'
    }
  });
});

// 基础路由测试
app.get('/', (req, res) => {
  res.json({
    message: '欢迎使用QMS API网关',
    version: '1.0.0-simple',
    endpoints: [
      'GET /health - 健康检查',
      'GET /status - 服务状态'
    ]
  });
});

// 服务状态检查
app.get('/status', (req, res) => {
  res.json({
    gateway: 'running',
    timestamp: new Date().toISOString(),
    uptime: process.uptime(),
    memory: process.memoryUsage()
  });
});

// 错误处理
app.use((error, req, res, next) => {
  console.error('🚨 网关错误:', error);
  res.status(500).json({
    success: false,
    message: '网关内部错误',
    error: error.message
  });
});

// 启动服务器
server.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 简化版QMS API网关启动成功！`);
  console.log(`📍 访问地址: http://localhost:${PORT}`);
  console.log(`🔧 健康检查: http://localhost:${PORT}/health`);
  console.log(`📊 状态检查: http://localhost:${PORT}/status`);
});

server.on('error', (error) => {
  if (error.code === 'EADDRINUSE') {
    console.error(`❌ 端口 ${PORT} 已被占用，请检查是否有其他服务在运行`);
    process.exit(1);
  } else {
    console.error(`❌ 服务器启动失败:`, error.message);
    process.exit(1);
  }
});

// 优雅关闭
process.on('SIGTERM', () => {
  console.log('🛑 收到SIGTERM信号，正在关闭网关服务...');
  server.close(() => {
    process.exit(0);
  });
});

process.on('SIGINT', () => {
  console.log('🛑 收到SIGINT信号，正在关闭网关服务...');
  server.close(() => {
    process.exit(0);
  });
});
