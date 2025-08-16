
// 统一中间件
const rateLimit = require('express-rate-limit');
const helmet = require('helmet');
const compression = require('compression');

// 安全中间件
const securityMiddleware = helmet({
  contentSecurityPolicy: {
    directives: {
      defaultSrc: ["'self'"],
      styleSrc: ["'self'", "'unsafe-inline'"],
      scriptSrc: ["'self'"],
      imgSrc: ["'self'", "data:", "https:"]
    }
  }
});

// 压缩中间件
const compressionMiddleware = compression({
  filter: (req, res) => {
    if (req.headers['x-no-compression']) {
      return false;
    }
    return compression.filter(req, res);
  }
});

// 速率限制中间件
const rateLimitMiddleware = rateLimit({
  windowMs: 15 * 60 * 1000, // 15分钟
  max: 100, // 限制每个IP 100次请求
  message: {
    error: '请求过于频繁，请稍后再试',
    retryAfter: '15分钟'
  }
});

// 日志中间件
const loggingMiddleware = (req, res, next) => {
  const start = Date.now();
  
  res.on('finish', () => {
    const duration = Date.now() - start;
    console.log(`${new Date().toISOString()} ${req.method} ${req.url} ${res.statusCode} ${duration}ms`);
  });
  
  next();
};

module.exports = {
  securityMiddleware,
  compressionMiddleware,
  rateLimitMiddleware,
  loggingMiddleware
};
