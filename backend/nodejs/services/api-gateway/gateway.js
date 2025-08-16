/**
 * QMS AI API网关服务
 * 统一管理所有API请求，提供负载均衡、限流、认证等功能
 */

const express = require('express')
const { createProxyMiddleware } = require('http-proxy-middleware')
const rateLimit = require('express-rate-limit')
const cors = require('cors')
const helmet = require('helmet')
const compression = require('compression')
const winston = require('winston')

const app = express()
const PORT = process.env.GATEWAY_PORT || 8085

// 日志配置
const logger = winston.createLogger({
  level: 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.errors({ stack: true }),
    winston.format.json()
  ),
  transports: [
    new winston.transports.File({ filename: 'logs/gateway-error.log', level: 'error' }),
    new winston.transports.File({ filename: 'logs/gateway.log' }),
    new winston.transports.Console({
      format: winston.format.simple()
    })
  ]
})

// 服务配置
const services = {
  // 聊天服务
  chat: {
    target: 'http://localhost:3004',
    pathRewrite: { '^/api/chat': '' },
    timeout: 30000,
    retries: 3
  },
  // 配置服务
  config: {
    target: 'http://localhost:3003',
    pathRewrite: { '^/api/config': '' },
    timeout: 10000,
    retries: 2
  },
  // 对象模型服务 (配置中心)
  objectModel: {
    target: 'http://localhost:3003',
    pathRewrite: { '^/api/object-model': '/api/object-model' },
    timeout: 10000,
    retries: 2
  },
  // 认证服务
  auth: {
    target: 'http://localhost:8084',
    pathRewrite: { '^/api/auth': '/api/auth' },
    timeout: 10000,
    retries: 2
  },
  // 用户服务 (如果有)
  user: {
    target: 'http://localhost:3005',
    pathRewrite: { '^/api/user': '' },
    timeout: 10000,
    retries: 2
  },
  // Coze Studio 服务（Agent/插件/工作流等）
  cozeStudio: {
    target: 'http://localhost:3005',
    pathRewrite: { '^/api/coze-studio': '/api' },
    timeout: 20000,
    retries: 2
  }
}

// 中间件配置
app.use(helmet()) // 安全头
app.use(compression()) // 压缩
app.use(cors({
  origin: ['http://localhost:8081', 'http://localhost:8072'],
  credentials: true
}))
app.use(express.json({ limit: '50mb' }))
app.use(express.urlencoded({ extended: true, limit: '50mb' }))

// 请求日志中间件
app.use((req, res, next) => {
  const start = Date.now()
  
  res.on('finish', () => {
    const duration = Date.now() - start
    logger.info({
      method: req.method,
      url: req.url,
      status: res.statusCode,
      duration: `${duration}ms`,
      userAgent: req.get('User-Agent'),
      ip: req.ip
    })
  })
  
  next()
})

// 限流配置
const createRateLimiter = (windowMs, max, message) => {
  return rateLimit({
    windowMs,
    max,
    message: { error: message },
    standardHeaders: true,
    legacyHeaders: false,
    handler: (req, res) => {
      logger.warn(`Rate limit exceeded for IP: ${req.ip}`)
      res.status(429).json({ error: message })
    }
  })
}

// 不同服务的限流策略
const rateLimiters = {
  // 聊天服务限流 (每分钟30次)
  chat: createRateLimiter(60 * 1000, 30, '聊天请求过于频繁，请稍后再试'),
  // 配置服务限流 (每分钟100次)
  config: createRateLimiter(60 * 1000, 100, '配置请求过于频繁，请稍后再试'),
  // 认证服务限流 (每分钟50次)
  auth: createRateLimiter(60 * 1000, 50, '认证请求过于频繁，请稍后再试'),
  // 通用限流 (每分钟200次)
  general: createRateLimiter(60 * 1000, 200, '请求过于频繁，请稍后再试')
}

// 健康检查
app.get('/health', (req, res) => {
  res.json({
    status: 'healthy',
    timestamp: new Date().toISOString(),
    uptime: process.uptime(),
    services: Object.keys(services),
    version: '1.0.0'
  })
})

// 服务状态检查
app.get('/status', async (req, res) => {
  const axios = require('axios')
  const serviceStatus = {}
  
  for (const [name, config] of Object.entries(services)) {
    try {
      const response = await axios.get(`${config.target}/health`, { timeout: 5000 })
      serviceStatus[name] = {
        status: 'healthy',
        responseTime: response.headers['x-response-time'] || 'unknown'
      }
    } catch (error) {
      serviceStatus[name] = {
        status: 'unhealthy',
        error: error.message
      }
    }
  }
  
  res.json({
    gateway: 'healthy',
    services: serviceStatus,
    timestamp: new Date().toISOString()
  })
})

// 创建代理中间件
const createProxy = (serviceName, config) => {
  return createProxyMiddleware({
    target: config.target,
    changeOrigin: true,
    pathRewrite: config.pathRewrite,
    timeout: config.timeout,
    
    // 错误处理
    onError: (err, req, res) => {
      logger.error(`Proxy error for ${serviceName}:`, {
        error: err.message,
        url: req.url,
        target: config.target
      })
      
      if (!res.headersSent) {
        res.status(503).json({
          error: '服务暂时不可用',
          service: serviceName,
          message: '请稍后重试'
        })
      }
    },
    
    // 请求拦截
    onProxyReq: (proxyReq, req, res) => {
      // 添加请求头
      proxyReq.setHeader('X-Gateway-Timestamp', Date.now())
      proxyReq.setHeader('X-Real-IP', req.ip)
      proxyReq.setHeader('X-Forwarded-For', req.ip)
    },
    
    // 响应拦截
    onProxyRes: (proxyRes, req, res) => {
      // 添加响应头
      proxyRes.headers['X-Gateway'] = 'QMS-AI-Gateway'
      proxyRes.headers['X-Service'] = serviceName
    }
  })
}

// 路由配置
// 认证服务路由 (优先级最高)
app.use('/api/auth', rateLimiters.auth, createProxy('auth', services.auth))

// 聊天服务路由
app.use('/api/chat', rateLimiters.chat, createProxy('chat', services.chat))

// 配置服务路由
app.use('/api/config', rateLimiters.config, createProxy('config', services.config))

// 对象模型服务路由
app.use('/api/object-model', rateLimiters.config, createProxy('objectModel', services.objectModel))

// Coze Studio 服务路由
app.use('/api/coze-studio', rateLimiters.general, createProxy('cozeStudio', services.cozeStudio))

// 用户服务路由 (如果存在)
if (services.user) {
  app.use('/api/user', rateLimiters.general, createProxy('user', services.user))
}

// 通用API路由 (兜底)
app.use('/api/*', rateLimiters.general, (req, res) => {
  logger.warn(`Unknown API route: ${req.url}`)
  res.status(404).json({
    error: 'API路由不存在',
    path: req.url,
    availableServices: Object.keys(services)
  })
})

// 静态文件服务 (如果需要)
app.use('/static', express.static('public'))

// 全局错误处理
app.use((error, req, res, next) => {
  logger.error('Gateway error:', {
    error: error.message,
    stack: error.stack,
    url: req.url,
    method: req.method
  })
  
  if (!res.headersSent) {
    res.status(500).json({
      error: '网关内部错误',
      message: '请联系系统管理员'
    })
  }
})

// 404处理
app.use('*', (req, res) => {
  res.status(404).json({
    error: '页面不存在',
    path: req.originalUrl
  })
})

// 优雅关闭
process.on('SIGTERM', () => {
  logger.info('SIGTERM received, shutting down gracefully')
  server.close(() => {
    logger.info('Process terminated')
    process.exit(0)
  })
})

process.on('SIGINT', () => {
  logger.info('SIGINT received, shutting down gracefully')
  server.close(() => {
    logger.info('Process terminated')
    process.exit(0)
  })
})

// 启动服务
const server = app.listen(PORT, () => {
  logger.info(`🚀 QMS AI API网关启动成功`)
  logger.info(`📡 监听端口: ${PORT}`)
  logger.info(`🔗 服务列表: ${Object.keys(services).join(', ')}`)
  logger.info(`🌐 健康检查: http://localhost:${PORT}/health`)
  logger.info(`📊 服务状态: http://localhost:${PORT}/status`)
})

module.exports = app
