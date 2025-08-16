/**
 * 最小化API网关 - 仅使用Node.js内置模块
 */

const http = require('http');
const url = require('url');

const PORT = 8085;

console.log('🌐 启动最小化API网关...');

// 添加错误处理
process.on('uncaughtException', (err) => {
  console.error('❌ 未捕获的异常:', err);
  process.exit(1);
});

process.on('unhandledRejection', (reason, promise) => {
  console.error('❌ 未处理的Promise拒绝:', reason);
  process.exit(1);
});

// 简单的代理函数
function proxyRequest(req, res, targetHost, targetPort, targetPath) {
  const options = {
    hostname: 'localhost',
    port: targetPort,
    path: targetPath || req.url,
    method: req.method,
    headers: {
      ...req.headers,
      host: `localhost:${targetPort}`
    }
  };

  const proxyReq = http.request(options, (proxyRes) => {
    // 设置CORS头
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');
    
    res.writeHead(proxyRes.statusCode, proxyRes.headers);
    proxyRes.pipe(res);
  });

  proxyReq.on('error', (err) => {
    console.error(`代理错误: ${err.message}`);
    res.writeHead(500, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ error: '服务暂时不可用' }));
  });

  req.pipe(proxyReq);
}

// 创建服务器
const server = http.createServer((req, res) => {
  const parsedUrl = url.parse(req.url, true);
  const path = parsedUrl.pathname;

  console.log(`📡 ${new Date().toISOString()} ${req.method} ${path} - 来源: ${req.headers.origin || 'unknown'}`);

  // 处理OPTIONS请求（CORS预检）
  if (req.method === 'OPTIONS') {
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');
    res.writeHead(200);
    res.end();
    return;
  }

  // 健康检查
  if (path === '/health') {
    res.setHeader('Content-Type', 'application/json');
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.writeHead(200);
    res.end(JSON.stringify({
      status: 'ok',
      timestamp: new Date().toISOString(),
      message: '最小化API网关运行正常'
    }));
    return;
  }

  // API路由代理
  if (path.startsWith('/api/auth/')) {
    console.log(`🔄 代理请求: ${req.method} ${path} -> http://localhost:8084${path}`);
    proxyRequest(req, res, 'localhost', 8084, path);
  } else if (path.startsWith('/api/chat/')) {
    console.log(`🔄 代理请求: ${req.method} ${path} -> http://localhost:3004${path}`);
    proxyRequest(req, res, 'localhost', 3004, path);
  } else if (path.startsWith('/api/config/')) {
    console.log(`🔄 代理请求: ${req.method} ${path} -> http://localhost:3003${path}`);
    proxyRequest(req, res, 'localhost', 3003, path);
  } else {
    // 404
    res.setHeader('Content-Type', 'application/json');
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.writeHead(404);
    res.end(JSON.stringify({ error: 'Not Found' }));
  }
});

// 启动服务器
server.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 最小化API网关启动成功！`);
  console.log(`📍 访问地址: http://localhost:${PORT}`);
  console.log(`🔧 健康检查: http://localhost:${PORT}/health`);
  console.log(`📋 API路由:`);
  console.log(`   🔐 认证服务: http://localhost:${PORT}/api/auth/*`);
  console.log(`   💬 聊天服务: http://localhost:${PORT}/api/chat/*`);
  console.log(`   ⚙️ 配置服务: http://localhost:${PORT}/api/config/*`);
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
process.on('SIGINT', () => {
  console.log('🛑 收到SIGINT信号，正在关闭网关服务...');
  server.close(() => {
    process.exit(0);
  });
});
