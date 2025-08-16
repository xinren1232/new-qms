/**
 * 性能监控中间件
 * 为现有服务添加监控能力
 */

const prometheus = require('prom-client')

// 创建指标收集器
const httpRequestDuration = new prometheus.Histogram({
  name: 'http_request_duration_seconds',
  help: 'HTTP请求持续时间',
  labelNames: ['method', 'route', 'status', 'service']
})

const httpRequestTotal = new prometheus.Counter({
  name: 'http_requests_total',
  help: 'HTTP请求总数',
  labelNames: ['method', 'route', 'status', 'service']
})

const activeConnections = new prometheus.Gauge({
  name: 'active_connections',
  help: '当前活跃连接数',
  labelNames: ['service']
})

const memoryUsage = new prometheus.Gauge({
  name: 'memory_usage_bytes',
  help: '内存使用量',
  labelNames: ['service']
})

// 监控中间件
function createMonitoringMiddleware(serviceName) {
  return (req, res, next) => {
    const start = Date.now()
    
    // 增加活跃连接数
    activeConnections.labels(serviceName).inc()
    
    // 监听响应结束
    res.on('finish', () => {
      const duration = (Date.now() - start) / 1000
      const route = req.route?.path || req.path
      
      // 记录请求持续时间
      httpRequestDuration
        .labels(req.method, route, res.statusCode, serviceName)
        .observe(duration)
      
      // 记录请求总数
      httpRequestTotal
        .labels(req.method, route, res.statusCode, serviceName)
        .inc()
      
      // 减少活跃连接数
      activeConnections.labels(serviceName).dec()
    })
    
    next()
  }
}

// 内存监控
function startMemoryMonitoring(serviceName) {
  setInterval(() => {
    const usage = process.memoryUsage()
    memoryUsage.labels(serviceName).set(usage.heapUsed)
  }, 10000) // 每10秒更新一次
}

// 指标导出端点
function createMetricsEndpoint() {
  return async (req, res) => {
    try {
      res.set('Content-Type', prometheus.register.contentType)
      res.end(await prometheus.register.metrics())
    } catch (error) {
      res.status(500).end(error.message)
    }
  }
}

// 健康检查端点
function createHealthEndpoint(serviceName) {
  return (req, res) => {
    const uptime = process.uptime()
    const memUsage = process.memoryUsage()
    
    res.json({
      service: serviceName,
      status: 'healthy',
      timestamp: new Date().toISOString(),
      uptime: `${Math.floor(uptime / 60)}分${Math.floor(uptime % 60)}秒`,
      memory: {
        used: `${Math.round(memUsage.heapUsed / 1024 / 1024)}MB`,
        total: `${Math.round(memUsage.heapTotal / 1024 / 1024)}MB`
      },
      version: process.env.npm_package_version || '1.0.0'
    })
  }
}

module.exports = {
  createMonitoringMiddleware,
  startMemoryMonitoring,
  createMetricsEndpoint,
  createHealthEndpoint,
  prometheus
}
