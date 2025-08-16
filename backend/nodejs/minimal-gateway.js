const express = require('express');
const cors = require('cors');
const http = require('http');
const httpProxy = require('http');

console.log('🌐 启动QMS-AI统一API网关...');

const app = express();
const server = http.createServer(app);
const PORT = 8085;

// 服务配置
const services = {
  chat: 'http://localhost:3004',
  config: 'http://localhost:3003',
  auth: 'http://localhost:8084'
};

// CORS配置
const corsOptions = {
  origin: [
    'http://localhost:8081',  // 应用端
    'http://localhost:8073',  // 配置端
    'http://localhost:3000',  // 开发环境
    'http://127.0.0.1:8081',
    'http://127.0.0.1:8073',
    'http://127.0.0.1:3000'
  ],
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS', 'PATCH'],
  allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With', 'Accept', 'Origin'],
  exposedHeaders: ['Content-Length', 'X-Foo', 'X-Bar']
};

// 基础中间件
app.use(cors(corsOptions));
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ extended: true, limit: '50mb' }));

// 请求日志中间件
app.use((req, res, next) => {
  console.log(`📡 ${new Date().toISOString()} ${req.method} ${req.url} - 来源: ${req.get('origin') || 'unknown'}`);
  next();
});

// 健康检查
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    timestamp: new Date().toISOString(),
    message: 'QMS API网关运行正常',
    services,
    version: '1.0.0'
  });
});

// 聊天服务代理
app.use('/api/chat', createProxyMiddleware({
  target: services.chat,
  changeOrigin: true,
  timeout: 60000,
  proxyTimeout: 60000,
  onError: (err, req, res) => {
    console.error('❌ 聊天服务代理错误:', err.message);
    res.status(500).json({ error: '聊天服务暂时不可用', details: err.message });
  },
  onProxyReq: (proxyReq, req, res) => {
    console.log(`🔄 代理聊天请求: ${req.method} ${req.url} -> ${services.chat}${req.url}`);
  }
}));

// 模型管理代理
app.use('/api/models', createProxyMiddleware({
  target: services.chat,
  changeOrigin: true,
  timeout: 30000,
  onError: (err, req, res) => {
    console.error('❌ 模型服务代理错误:', err.message);
    res.status(500).json({ error: '模型服务暂时不可用', details: err.message });
  },
  onProxyReq: (proxyReq, req, res) => {
    console.log(`🔄 代理模型请求: ${req.method} ${req.url} -> ${services.chat}${req.url}`);
  }
}));

// 认证服务代理
app.use('/api/auth', createProxyMiddleware({
  target: services.auth,
  changeOrigin: true,
  timeout: 30000,
  onError: (err, req, res) => {
    console.error('❌ 认证服务代理错误:', err.message);
    res.status(500).json({ error: '认证服务暂时不可用', details: err.message });
  },
  onProxyReq: (proxyReq, req, res) => {
    console.log(`🔄 代理认证请求: ${req.method} ${req.url} -> ${services.auth}${req.url}`);
  }
}));

// 配置服务代理
app.use('/api/configs', createProxyMiddleware({
  target: services.config,
  changeOrigin: true,
  timeout: 30000,
  onError: (err, req, res) => {
    console.error('❌ 配置服务代理错误:', err.message);
    res.status(500).json({ error: '配置服务暂时不可用', details: err.message });
  },
  onProxyReq: (proxyReq, req, res) => {
    console.log(`🔄 代理配置请求: ${req.method} ${req.url} -> ${services.config}${req.url}`);
  }
}));

// AI模型配置代理
app.use('/api/ai', createProxyMiddleware({
  target: services.config,
  changeOrigin: true,
  timeout: 30000,
  onError: (err, req, res) => {
    console.error('❌ AI配置服务代理错误:', err.message);
    res.status(500).json({ error: 'AI配置服务暂时不可用', details: err.message });
  },
  onProxyReq: (proxyReq, req, res) => {
    console.log(`🔄 代理AI配置请求: ${req.method} ${req.url} -> ${services.config}${req.url}`);
  }
}));

// 监控和统计代理
app.use('/api/monitoring', createProxyMiddleware({
  target: services.chat,
  changeOrigin: true,
  timeout: 30000,
  onError: (err, req, res) => {
    console.error('❌ 监控服务代理错误:', err.message);
    res.status(500).json({ error: '监控服务暂时不可用', details: err.message });
  },
  onProxyReq: (proxyReq, req, res) => {
    console.log(`🔄 代理监控请求: ${req.method} ${req.url} -> ${services.chat}${req.url}`);
  }
}));

// 通用API代理 (兜底)
app.use('/api', (req, res) => {
  console.log(`⚠️ 未匹配的API请求: ${req.method} ${req.url}`);
  res.status(404).json({
    error: 'API路径未找到',
    path: req.url,
    availableRoutes: ['/api/chat', '/api/models', '/api/auth', '/api/configs', '/api/ai', '/api/monitoring']
  });
});

// 404处理
app.use('*', (req, res) => {
  res.status(404).json({
    error: '页面未找到',
    path: req.url,
    message: '请检查请求路径是否正确'
  });
});

// 启动服务器
server.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 QMS-AI统一API网关启动成功！`);
  console.log(`📍 统一访问地址: http://localhost:${PORT}`);
  console.log(`🔧 健康检查: http://localhost:${PORT}/health`);
  console.log(`📋 API路由:`);
  console.log(`   💬 聊天服务: http://localhost:${PORT}/api/chat/*`);
  console.log(`   🤖 模型管理: http://localhost:${PORT}/api/models/*`);
  console.log(`   🔐 认证服务: http://localhost:${PORT}/api/auth/*`);
  console.log(`   ⚙️ 配置服务: http://localhost:${PORT}/api/configs/*`);
  console.log(`   🧠 AI配置: http://localhost:${PORT}/api/ai/*`);
  console.log(`   📊 监控服务: http://localhost:${PORT}/api/monitoring/*`);
  console.log(`🌐 支持的前端:`);
  console.log(`   📱 应用端: http://localhost:8081`);
  console.log(`   ⚙️ 配置端: http://localhost:8073`);
});

server.on('error', (error) => {
  if (error.code === 'EADDRINUSE') {
    console.error(`❌ 端口 ${PORT} 已被占用`);
    console.error(`💡 请检查是否有其他网关服务正在运行`);
  } else {
    console.error(`❌ 服务器启动失败:`, error.message);
  }
  process.exit(1);
});

// 优雅关闭
process.on('SIGTERM', () => {
  console.log('🛑 正在关闭QMS-AI网关服务...');
  server.close(() => {
    console.log('✅ 网关服务已安全关闭');
    process.exit(0);
  });
});

process.on('SIGINT', () => {
  console.log('🛑 正在关闭QMS-AI网关服务...');
  server.close(() => {
    console.log('✅ 网关服务已安全关闭');
    process.exit(0);
  });
});
