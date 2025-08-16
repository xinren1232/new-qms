const express = require('express');
const cors = require('cors');
const http = require('http');
const url = require('url');

console.log('🌐 启动QMS-AI简化API网关...');

const app = express();
const server = http.createServer(app);
const PORT = 8085;

// 服务配置
const services = {
  chat: { host: 'localhost', port: 3004 },
  config: { host: 'localhost', port: 3003 }, 
  auth: { host: 'localhost', port: 8084 }
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

// 简单代理函数
function createSimpleProxy(targetService) {
  return (req, res) => {
    const target = services[targetService];
    if (!target) {
      return res.status(500).json({ error: '服务配置错误' });
    }

    const options = {
      hostname: target.host,
      port: target.port,
      path: req.url,
      method: req.method,
      headers: {
        ...req.headers,
        host: `${target.host}:${target.port}`
      }
    };

    console.log(`🔄 代理请求: ${req.method} ${req.url} -> http://${target.host}:${target.port}${req.url}`);

    const proxyReq = http.request(options, (proxyRes) => {
      // 设置响应头
      res.status(proxyRes.statusCode);
      Object.keys(proxyRes.headers).forEach(key => {
        res.set(key, proxyRes.headers[key]);
      });

      // 转发响应数据
      proxyRes.pipe(res);
    });

    proxyReq.on('error', (err) => {
      console.error(`❌ 代理错误 (${targetService}):`, err.message);
      if (!res.headersSent) {
        res.status(500).json({ 
          error: `${targetService}服务暂时不可用`, 
          details: err.message 
        });
      }
    });

    // 转发请求数据
    if (req.method !== 'GET' && req.method !== 'HEAD') {
      if (req.body && Object.keys(req.body).length > 0) {
        const bodyData = JSON.stringify(req.body);
        proxyReq.setHeader('Content-Length', Buffer.byteLength(bodyData));
        proxyReq.write(bodyData);
      }
    }

    proxyReq.end();
  };
}

// 健康检查
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    timestamp: new Date().toISOString(),
    message: 'QMS API网关运行正常',
    services: {
      chat: `http://${services.chat.host}:${services.chat.port}`,
      config: `http://${services.config.host}:${services.config.port}`,
      auth: `http://${services.auth.host}:${services.auth.port}`
    },
    version: '1.0.0'
  });
});

// 路由配置
app.use('/api/chat', createSimpleProxy('chat'));
app.use('/api/models', createSimpleProxy('chat'));
app.use('/api/auth', createSimpleProxy('auth'));
app.use('/api/configs', createSimpleProxy('config'));
app.use('/api/ai', createSimpleProxy('config'));
app.use('/api/monitoring', createSimpleProxy('chat'));

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
  console.log(`🚀 QMS-AI简化API网关启动成功！`);
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
