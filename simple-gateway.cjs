const express = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');
const cors = require('cors');

const app = express();
const PORT = 8085;

console.log('🌐 启动QMS简化API网关...');

// 基础中间件
app.use(cors());
app.use(express.json());

// 健康检查
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    timestamp: new Date().toISOString(),
    message: 'QMS API网关运行正常',
    services: {
      chat: 'http://localhost:3004',
      config: 'http://localhost:3003',
      auth: 'http://localhost:8084',
      app: 'http://localhost:8081',
      configUI: 'http://localhost:8072'
    }
  });
});

// 代理配置
const proxyOptions = {
  changeOrigin: true,
  logLevel: 'info',
  onError: (err, req, res) => {
    console.error('代理错误:', err.message);
    res.status(500).json({ error: '服务暂时不可用' });
  }
};

// 带路径重写的代理配置
const createProxyWithPathRewrite = (target, pathPrefix) => ({
  ...proxyOptions,
  pathRewrite: {
    [`^${pathPrefix}`]: pathPrefix // 保持原路径
  }
});

// API路由代理
app.use('/api/chat', createProxyMiddleware({
  target: 'http://localhost:3004',
  ...createProxyWithPathRewrite('http://localhost:3004', '/api/chat')
}));

app.use('/api/config', createProxyMiddleware({
  target: 'http://localhost:3003',
  ...createProxyWithPathRewrite('http://localhost:3003', '/api/config')
}));

app.use('/api/auth', createProxyMiddleware({
  target: 'http://localhost:8084',
  ...createProxyWithPathRewrite('http://localhost:8084', '/api/auth')
}));

// 前端路由代理
app.use('/config', createProxyMiddleware({
  target: 'http://localhost:8072',
  ...proxyOptions,
  pathRewrite: {
    '^/config': ''
  }
}));

app.use('/', createProxyMiddleware({
  target: 'http://localhost:8081',
  ...proxyOptions
}));

app.listen(PORT, () => {
  console.log(`🚀 QMS简化API网关启动成功！`);
  console.log(`📍 统一访问地址: http://localhost:${PORT}`);
  console.log(`🔧 健康检查: http://localhost:${PORT}/health`);
  console.log(`\n📋 服务路由:`);
  console.log(`  🏠 应用端: http://localhost:${PORT}/`);
  console.log(`  ⚙️ 配置端: http://localhost:${PORT}/config/`);
  console.log(`  💬 聊天API: http://localhost:${PORT}/api/chat/`);
  console.log(`  🔐 认证API: http://localhost:${PORT}/api/auth/`);
  console.log(`  📋 配置API: http://localhost:${PORT}/api/config/`);
});
