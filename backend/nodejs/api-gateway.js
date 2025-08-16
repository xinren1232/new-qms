/**
 * QMS API网关服务
 * 统一入口，减少端口数量
 * 优化版本 - 增强性能、监控和错误处理
 */

const express = require('express');
const cors = require('cors');
const path = require('path');
const fs = require('fs');
const compression = require('compression');
const helmet = require('helmet');
const http = require('http');
const WebSocket = require('ws');
const axios = require('axios');

const app = express();
const server = http.createServer(app);

// 服务配置
const SERVICES = {
  chat: { url: 'http://localhost:3004', name: '聊天服务' },
  config: { url: 'http://localhost:3003', name: '配置中心' },
  auth: { url: 'http://localhost:8084', name: '认证服务' },
  coze: { url: 'http://localhost:3005', name: 'Coze Studio' },
  app: { url: 'http://localhost:8081', name: '应用端' }
};

// 性能监控
const metrics = {
  requests: 0,
  errors: 0,
  responseTime: [],
  startTime: Date.now(),
  serviceStats: {
    chat: { requests: 0, errors: 0, avgResponseTime: 0 },
    config: { requests: 0, errors: 0, avgResponseTime: 0 },
    auth: { requests: 0, errors: 0, avgResponseTime: 0 },
    coze: { requests: 0, errors: 0, avgResponseTime: 0 }
  }
};

// 中间件
app.use(helmet({
  contentSecurityPolicy: false // 前端已有构建资源，可视需要细化CSP
}));
app.use(compression());
app.use(cors());
// 提升网关请求体大小限制，支持较大base64/CSV
app.use(express.json({ limit: '60mb' }));
app.use(express.urlencoded({ extended: true, limit: '60mb' }));

console.log('🌐 启动QMS API网关服务...');

// 性能监控中间件
app.use((req, res, next) => {
  const startTime = Date.now();
  metrics.requests++;

  // 识别服务类型
  let serviceType = 'unknown';
  if (req.path.startsWith('/api/chat')) serviceType = 'chat';
  else if (req.path.startsWith('/api/config')) serviceType = 'config';
  else if (req.path.startsWith('/api/auth')) serviceType = 'auth';
  else if (req.path.startsWith('/api/coze-studio')) serviceType = 'coze';

  res.on('finish', () => {
    const responseTime = Date.now() - startTime;
    metrics.responseTime.push(responseTime);

    // 保持最近1000个响应时间记录
    if (metrics.responseTime.length > 1000) {
      metrics.responseTime.shift();
    }

    // 更新服务统计
    if (serviceType !== 'unknown' && metrics.serviceStats[serviceType]) {
      metrics.serviceStats[serviceType].requests++;
      const stats = metrics.serviceStats[serviceType];
      stats.avgResponseTime = ((stats.avgResponseTime * (stats.requests - 1)) + responseTime) / stats.requests;

      if (res.statusCode >= 400) {
        stats.errors++;
        metrics.errors++;
      }
    }
  });

  next();
});

// 健康检查 - 增强版
app.get('/health', (req, res) => {
  const avgResponseTime = metrics.responseTime.length > 0
    ? Math.round(metrics.responseTime.reduce((a, b) => a + b, 0) / metrics.responseTime.length)
    : 0;

  res.json({
    status: 'ok',
    timestamp: new Date().toISOString(),
    message: 'QMS API网关运行正常',
    version: '2.1.0',
    uptime: Math.round(process.uptime()),
    memory: {
      used: Math.round(process.memoryUsage().heapUsed / 1024 / 1024) + 'MB',
      total: Math.round(process.memoryUsage().heapTotal / 1024 / 1024) + 'MB'
    },
    metrics: {
      totalRequests: metrics.requests,
      totalErrors: metrics.errors,
      errorRate: metrics.requests > 0 ? ((metrics.errors / metrics.requests) * 100).toFixed(2) + '%' : '0%',
      avgResponseTime: avgResponseTime + 'ms',
      uptime: Math.round((Date.now() - metrics.startTime) / 1000) + 's'
    },
    services: {
      chat: {
        url: 'http://localhost:3004',
        requests: metrics.serviceStats.chat.requests,
        errors: metrics.serviceStats.chat.errors,
        avgResponseTime: Math.round(metrics.serviceStats.chat.avgResponseTime) + 'ms'
      },
      config: {
        url: 'http://localhost:3003',
        requests: metrics.serviceStats.config.requests,
        errors: metrics.serviceStats.config.errors,
        avgResponseTime: Math.round(metrics.serviceStats.config.avgResponseTime) + 'ms'
      },
      auth: {
        url: 'http://localhost:8084',
        requests: metrics.serviceStats.auth.requests,
        errors: metrics.serviceStats.auth.errors,
        avgResponseTime: Math.round(metrics.serviceStats.auth.avgResponseTime) + 'ms'
      },
      coze: {
        url: 'http://localhost:3005',
        requests: metrics.serviceStats.coze.requests,
        errors: metrics.serviceStats.coze.errors,
        avgResponseTime: Math.round(metrics.serviceStats.coze.avgResponseTime) + 'ms'
      }
    }
  });
});

// 监控端点 - 提供详细的性能指标
app.get('/metrics', (req, res) => {
  const now = Date.now();
  const uptimeSeconds = Math.round((now - metrics.startTime) / 1000);

  // 计算最近5分钟的请求数
  const fiveMinutesAgo = now - 5 * 60 * 1000;
  const recentRequests = metrics.responseTime.filter((_, index) => {
    const requestTime = now - (metrics.responseTime.length - index) * 1000; // 估算时间
    return requestTime > fiveMinutesAgo;
  }).length;

  res.json({
    timestamp: new Date().toISOString(),
    uptime: {
      seconds: uptimeSeconds,
      formatted: `${Math.floor(uptimeSeconds / 3600)}h ${Math.floor((uptimeSeconds % 3600) / 60)}m ${uptimeSeconds % 60}s`
    },
    requests: {
      total: metrics.requests,
      recent5min: recentRequests,
      rps: uptimeSeconds > 0 ? (metrics.requests / uptimeSeconds).toFixed(2) : '0'
    },
    errors: {
      total: metrics.errors,
      rate: metrics.requests > 0 ? ((metrics.errors / metrics.requests) * 100).toFixed(2) + '%' : '0%'
    },
    performance: {
      avgResponseTime: metrics.responseTime.length > 0
        ? Math.round(metrics.responseTime.reduce((a, b) => a + b, 0) / metrics.responseTime.length) + 'ms'
        : '0ms',
      p95ResponseTime: metrics.responseTime.length > 0
        ? Math.round(metrics.responseTime.sort((a, b) => a - b)[Math.floor(metrics.responseTime.length * 0.95)]) + 'ms'
        : '0ms'
    },
    memory: process.memoryUsage(),
    services: metrics.serviceStats
  });
});

// 服务健康检查端点
app.get('/health/services', async (req, res) => {
  const axios = require('axios');
  const serviceChecks = [];

  for (const [key, service] of Object.entries(SERVICES)) {
    try {
      const startTime = Date.now();
      const response = await axios.get(`${service.url}/health`, { timeout: 5000 });
      const responseTime = Date.now() - startTime;

      serviceChecks.push({
        name: service.name,
        url: service.url,
        status: 'healthy',
        responseTime: responseTime + 'ms',
        statusCode: response.status,
        lastCheck: new Date().toISOString()
      });
    } catch (error) {
      serviceChecks.push({
        name: service.name,
        url: service.url,
        status: 'unhealthy',
        error: error.message,
        lastCheck: new Date().toISOString()
      });
    }
  }

  const healthyCount = serviceChecks.filter(s => s.status === 'healthy').length;
  const totalCount = serviceChecks.length;

  res.json({
    timestamp: new Date().toISOString(),
    overall: healthyCount === totalCount ? 'healthy' : 'degraded',
    summary: `${healthyCount}/${totalCount} services healthy`,
    services: serviceChecks
  });
});

// 代理函数（使用挂载后的路径 req.path 构建URL）- 优化版
const proxyRequest = async (req, res, targetUrl) => {
  const startTime = Date.now();

  try {
    const axios = require('axios');

    // 构建完整的目标URL（挂载后路径）
    const queryString = req.url.includes('?') ? req.url.substring(req.url.indexOf('?')) : '';
    const fullUrl = `${targetUrl}${req.path}${queryString}`;

    console.log(`🔄 代理请求: ${req.method} ${req.originalUrl} → ${fullUrl}`);

    const cleanHeaders = { ...req.headers };
    delete cleanHeaders.host;
    delete cleanHeaders['content-length'];
    delete cleanHeaders.connection;

    // 添加代理标识
    cleanHeaders['x-forwarded-for'] = req.ip;
    cleanHeaders['x-forwarded-proto'] = req.protocol;
    cleanHeaders['x-forwarded-host'] = req.get('host');
    cleanHeaders['x-proxy-by'] = 'QMS-API-Gateway';

    const response = await axios({
      method: req.method,
      url: fullUrl,
      data: req.method !== 'GET' && req.method !== 'HEAD' ? req.body : undefined,
      headers: cleanHeaders,
      timeout: 30000,
      validateStatus: () => true,
      maxRedirects: 5
    });

    // 添加响应头
    Object.keys(response.headers).forEach(key => {
      if (key.toLowerCase() !== 'transfer-encoding') {
        res.set(key, response.headers[key]);
      }
    });

    // 添加性能指标
    res.set('x-response-time', `${Date.now() - startTime}ms`);
    res.set('x-proxy-by', 'QMS-API-Gateway');

    res.status(response.status);
    if (response.headers['content-type']?.includes('application/json')) {
      res.json(response.data);
    } else {
      res.send(response.data);
    }

    // 记录成功的请求
    console.log(`✅ 代理成功: ${req.method} ${req.originalUrl} [${response.status}] ${Date.now() - startTime}ms`);

  } catch (error) {
    const responseTime = Date.now() - startTime;
    console.error(`❌ 代理错误 ${req.originalUrl} [${responseTime}ms]:`, error.message);

    if (error.response) {
      res.status(error.response.status);
      if (error.response.headers['content-type']?.includes('application/json')) {
        res.json(error.response.data);
      } else {
        res.send(error.response.data);
      }
    } else if (error.code === 'ECONNREFUSED') {
      res.status(503).json({
        success: false,
        message: '目标服务不可用',
        error: 'Service Unavailable',
        targetUrl: targetUrl,
        timestamp: new Date().toISOString()
      });
    } else if (error.code === 'ETIMEDOUT') {
      res.status(504).json({
        success: false,
        message: '服务响应超时',
        error: 'Gateway Timeout',
        targetUrl: targetUrl,
        timestamp: new Date().toISOString()
      });
    } else {
      res.status(500).json({
        success: false,
        message: '网关内部错误',
        error: error.message,
        targetUrl: targetUrl,
        timestamp: new Date().toISOString()
      });
    }
  }
};

// 代理函数（支持路径重写）
const proxyRequestWithRewrite = async (req, res, targetUrl, rewrittenPath) => {
  try {
    const axios = require('axios');

    // 构建完整的目标URL（使用重写后的路径）
    // 从原始URL中提取查询参数，但不重复添加
    const originalUrl = req.originalUrl;
    const queryIndex = originalUrl.indexOf('?');
    const queryString = queryIndex !== -1 ? originalUrl.substring(queryIndex) : '';

    // 确保重写后的路径不包含查询参数
    const cleanRewrittenPath = rewrittenPath.split('?')[0];
    const fullUrl = `${targetUrl}${cleanRewrittenPath}${queryString}`;

    console.log(`🔄 代理请求(路径重写): ${req.method} ${req.originalUrl} → ${fullUrl}`);

    const cleanHeaders = { ...req.headers };
    delete cleanHeaders.host;
    delete cleanHeaders['content-length'];
    delete cleanHeaders.connection;

    const response = await axios({
      method: req.method,
      url: fullUrl,
      data: req.method !== 'GET' && req.method !== 'HEAD' ? req.body : undefined,
      headers: cleanHeaders,
      timeout: 30000,
      maxBodyLength: Infinity,
      maxContentLength: Infinity,
      validateStatus: () => true
    });

    // 转发响应头
    Object.keys(response.headers).forEach(key => {
      if (!['connection', 'transfer-encoding', 'content-encoding'].includes(key.toLowerCase())) {
        res.set(key, response.headers[key]);
      }
    });

    res.status(response.status);
    if (response.headers['content-type']?.includes('application/json')) {
      res.json(response.data);
    } else {
      res.send(response.data);
    }
  } catch (error) {
    console.error(`❌ 代理错误(路径重写) ${req.originalUrl}:`, error.message);

    if (error.response) {
      res.status(error.response.status);
      if (error.response.headers['content-type']?.includes('application/json')) {
        res.json(error.response.data);
      } else {
        res.send(error.response.data);
      }
    } else if (error.code === 'ECONNREFUSED') {
      res.status(503).json({
        success: false,
        message: '目标服务不可用',
        error: 'Service Unavailable',
        targetUrl: targetUrl
      });
    } else {
      res.status(500).json({
        success: false,
        message: '网关内部错误',
        error: error.message,
        targetUrl: targetUrl
      });
    }
  }
};

// 代理函数（保留原始URL req.originalUrl，用于保持 /api 前缀）
const proxyOriginalUrl = async (req, res, targetBase) => {
  try {
    const axios = require('axios');

    const fullUrl = `${targetBase}${req.originalUrl}`; // originalUrl 已包含查询参数
    console.log(`🔄 代理请求(原始URL): ${req.method} ${req.originalUrl} → ${fullUrl}`);

    const cleanHeaders = { ...req.headers };
    delete cleanHeaders.host;
    delete cleanHeaders['content-length'];
    delete cleanHeaders.connection;

    const isMultipart = (req.headers['content-type'] || '').startsWith('multipart/form-data');
    const data = (req.method !== 'GET' && req.method !== 'HEAD') ? (isMultipart ? req : req.body) : undefined;

    const response = await axios({
      method: req.method,
      url: fullUrl,
      data,
      headers: cleanHeaders,
      timeout: 30000,
      maxBodyLength: Infinity,
      maxContentLength: Infinity,
      validateStatus: () => true
    });

    Object.keys(response.headers).forEach(key => {
      if (key.toLowerCase() !== 'transfer-encoding') {
        res.set(key, response.headers[key]);
      }
    });

    res.status(response.status);
    if (response.headers['content-type']?.includes('application/json')) {
      res.json(response.data);
    } else {
      res.send(response.data);
    }
  } catch (error) {
    console.error(`❌ 代理错误(原始URL) ${req.originalUrl}:`, error.message);
    res.status(500).json({ error: '网关内部错误', message: error.message });
  }
};

// AI聊天服务代理（保留完整 /api 前缀）
app.use('/api/chat', (req, res) => {
  return proxyOriginalUrl(req, res, 'http://localhost:3004');
});

// 认证服务代理（保留完整 /api 前缀）
app.use('/api/auth', (req, res) => {
  return proxyOriginalUrl(req, res, 'http://localhost:8084');
});

// 案例库（chat-service 提供），需要完整保留 /api 前缀
app.use('/api/cases', (req, res) => {
  return proxyOriginalUrl(req, res, 'http://localhost:3004');
});

// 案例指导（chat-service 提供）
app.use('/api/chat/case-guidance', (req, res) => {
  return proxyOriginalUrl(req, res, 'http://localhost:3004');
});


// 配置中心服务代理（保留完整 /api 前缀）
app.use('/api/config', (req, res) => {
  return proxyOriginalUrl(req, res, 'http://localhost:3003');
});


// Coze Studio 服务代理（智能路径重写）
app.use('/api/coze-studio', (req, res) => {
  try {
    // 智能路径重写逻辑
    let rewrittenPath;
    const originalPath = req.originalUrl;

    if (originalPath === '/api/coze-studio/health') {
      // 健康检查端点：/api/coze-studio/health -> /health
      rewrittenPath = '/health';
    } else if (originalPath.startsWith('/api/coze-studio/api/')) {
      // API端点：/api/coze-studio/api/xxx -> /api/xxx
      rewrittenPath = originalPath.replace(/^\/api\/coze-studio/, '');
    } else {
      // 其他端点：/api/coze-studio/xxx -> /api/xxx
      rewrittenPath = originalPath.replace(/^\/api\/coze-studio/, '/api');
    }

    // 使用专门的代理函数处理路径重写
    return proxyRequestWithRewrite(req, res, 'http://localhost:3005', rewrittenPath);
  } catch (e) {
    console.error('Coze Studio代理错误:', e.message);
    res.status(502).json({ success: false, message: 'Coze Studio 网关代理失败', error: e.message });
  }
});

// 配置端API代理 - 代理所有/api/请求到配置服务
app.use('/api', (req, res, next) => {
  // 跳过已经处理的特定路由
  if (req.path.startsWith('/coze-studio') || req.path.startsWith('/object-model')) {
    return next();
  }

  console.log(`🔄 配置端API代理: ${req.method} ${req.originalUrl} → http://localhost:3003${req.originalUrl}`);
  return proxyOriginalUrl(req, res, 'http://localhost:3003');
});

// 应用端业务API代理 - 添加缺失的路由
app.use('/api/object-model', (req, res) => {
  // 重写路径，使用完整的原始URL路径
  const originalPath = req.path;
  req.path = req.originalUrl.replace(/\?.*$/, ''); // 移除查询参数
  proxyRequest(req, res, 'http://localhost:3003');
  req.path = originalPath; // 恢复原始路径
});

// 健康检查代理
app.use('/health', (req, res) => {
  proxyRequest(req, res, 'http://localhost:3003/health');
});

// 处理favicon.ico请求
app.get('/favicon.ico', (req, res) => {
  const faviconPath = path.join(__dirname, '../../frontend/应用端/public/favicon.ico');
  if (fs.existsSync(faviconPath)) {
    res.sendFile(faviconPath);
  } else {
    // 返回一个简单的透明图标或204状态
    res.status(204).end();
  }
});

// 静态文件服务 - 应用端
const appDistPath = path.join(__dirname, '../../frontend/应用端/dist');
if (fs.existsSync(appDistPath)) {
  app.use('/', express.static(appDistPath, { maxAge: '7d', etag: true, lastModified: true }));
  console.log('📁 应用端静态文件服务已启用(缓存:7d)');
} else {
  // 开发模式：代理到应用端开发服务器
  console.log('🔄 开发模式：代理应用端到 http://localhost:8081');
  app.use('/', (req, res, next) => {
    // 如果是API请求或配置端请求，跳过
    if (req.path.startsWith('/api/') || req.path.startsWith('/config/')) {
      return next();
    }
    proxyRequest(req, res, 'http://localhost:8081');
  });
}

// 静态文件服务 - 配置端
const configDistPath = path.join(__dirname, '../../frontend/配置端/dist');
if (fs.existsSync(configDistPath)) {
  app.use('/config', express.static(configDistPath, { maxAge: '7d', etag: true, lastModified: true }));
  console.log('📁 配置端静态文件服务已启用(缓存:7d)');
} else {
  console.log('⚠️  配置端构建文件不存在，请先运行: cd frontend/配置端 && npm run build');
}

// SPA路由支持 - 应用端
app.get('*', (req, res, next) => {
  // 如果是API请求，跳过
  if (req.path.startsWith('/api/') || req.path.startsWith('/config/')) {
    return next();
  }

  const indexPath = path.join(appDistPath, 'index.html');
  if (fs.existsSync(indexPath)) {
    res.sendFile(indexPath);
  } else {
    // 开发模式：代理到应用端开发服务器
    proxyRequest(req, res, 'http://localhost:8081');
  }
});

// 错误处理中间件
app.use((error, req, res, next) => {
  console.error('🚨 网关错误:', error);
  res.status(500).json({
    success: false,
    message: '网关内部错误',
    error: process.env.NODE_ENV === 'development' ? error.message : undefined
  });
});

const PORT = process.env.GATEWAY_PORT || 8085;

// 创建WebSocket服务器
const wss = new WebSocket.Server({
  server,
  path: '/config-sync'
});

// WebSocket连接处理
wss.on('connection', (ws, req) => {
  console.log('🔌 WebSocket客户端连接:', req.url);

  // 发送欢迎消息
  ws.send(JSON.stringify({
    type: 'welcome',
    message: 'WebSocket连接成功',
    timestamp: new Date().toISOString()
  }));

  // 处理消息
  ws.on('message', (message) => {
    try {
      const data = JSON.parse(message);
      console.log('📨 收到WebSocket消息:', data);

      // 回显消息（示例）
      ws.send(JSON.stringify({
        type: 'response',
        data: data,
        timestamp: new Date().toISOString()
      }));
    } catch (error) {
      console.error('❌ WebSocket消息处理错误:', error);
      ws.send(JSON.stringify({
        type: 'error',
        message: '消息格式错误',
        timestamp: new Date().toISOString()
      }));
    }
  });

  // 处理连接关闭
  ws.on('close', () => {
    console.log('🔌 WebSocket客户端断开连接');
  });

  // 处理错误
  ws.on('error', (error) => {
    console.error('❌ WebSocket错误:', error);
  });
});

// 启动HTTP服务器
server.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 QMS API网关启动成功！`);
  console.log(`📍 统一访问地址: http://localhost:${PORT}`);
  console.log(`🔧 健康检查: http://localhost:${PORT}/health`);
  console.log(`🔌 WebSocket端点: ws://localhost:${PORT}/config-sync`);
  console.log(`\n📋 服务路由:`);
  console.log(`  🏠 应用端前端: http://localhost:${PORT}/`);
  console.log(`  ⚙️  配置端前端: http://localhost:${PORT}/config/`);
  console.log(`  💬 AI聊天API: http://localhost:${PORT}/api/chat/`);
  console.log(`  🔐 认证API: http://localhost:${PORT}/api/auth/`);
  console.log(`  📋 配置API: http://localhost:${PORT}/api/config/`);
  console.log(`\n🎯 端口整合完成！从8个端口减少到1个端口`);
  console.log(`💡 提示: 请确保后端服务(3003, 3004)正在运行`);
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
  process.exit(0);
});

process.on('SIGINT', () => {
  console.log('🛑 收到SIGINT信号，正在关闭网关服务...');
  process.exit(0);
});
