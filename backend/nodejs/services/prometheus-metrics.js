const promClient = require('prom-client');

class PrometheusMetrics {
  constructor() {
    // 创建默认指标收集器
    this.register = new promClient.Registry();
    
    // 收集默认指标（CPU、内存等）
    promClient.collectDefaultMetrics({
      register: this.register,
      prefix: 'qms_ai_',
      gcDurationBuckets: [0.001, 0.01, 0.1, 1, 2, 5]
    });

    this.initCustomMetrics();
  }

  initCustomMetrics() {
    // HTTP请求总数
    this.httpRequestsTotal = new promClient.Counter({
      name: 'qms_ai_http_requests_total',
      help: 'Total number of HTTP requests',
      labelNames: ['method', 'route', 'status_code'],
      registers: [this.register]
    });

    // HTTP请求持续时间
    this.httpRequestDuration = new promClient.Histogram({
      name: 'qms_ai_http_request_duration_seconds',
      help: 'Duration of HTTP requests in seconds',
      labelNames: ['method', 'route', 'status_code'],
      buckets: [0.1, 0.5, 1, 2, 5, 10],
      registers: [this.register]
    });

    // AI模型请求总数
    this.aiRequestsTotal = new promClient.Counter({
      name: 'qms_ai_model_requests_total',
      help: 'Total number of AI model requests',
      labelNames: ['model_provider', 'model_name', 'status'],
      registers: [this.register]
    });

    // AI模型响应时间
    this.aiResponseTime = new promClient.Histogram({
      name: 'qms_ai_model_response_time_seconds',
      help: 'AI model response time in seconds',
      labelNames: ['model_provider', 'model_name'],
      buckets: [0.5, 1, 2, 5, 10, 30, 60],
      registers: [this.register]
    });

    // 数据库连接池状态
    this.dbConnectionsActive = new promClient.Gauge({
      name: 'qms_ai_db_connections_active',
      help: 'Number of active database connections',
      labelNames: ['database_type'],
      registers: [this.register]
    });

    // 缓存命中率
    this.cacheHitRate = new promClient.Gauge({
      name: 'qms_ai_cache_hit_rate',
      help: 'Cache hit rate percentage',
      labelNames: ['cache_type'],
      registers: [this.register]
    });

    // 用户会话数
    this.activeUsers = new promClient.Gauge({
      name: 'qms_ai_active_users',
      help: 'Number of active users',
      registers: [this.register]
    });

    // 对话总数
    this.conversationsTotal = new promClient.Counter({
      name: 'qms_ai_conversations_total',
      help: 'Total number of conversations',
      labelNames: ['user_type'],
      registers: [this.register]
    });

    // 消息总数
    this.messagesTotal = new promClient.Counter({
      name: 'qms_ai_messages_total',
      help: 'Total number of messages',
      labelNames: ['message_type', 'model_provider'],
      registers: [this.register]
    });

    // 错误率
    this.errorRate = new promClient.Gauge({
      name: 'qms_ai_error_rate',
      help: 'Error rate percentage',
      labelNames: ['service', 'error_type'],
      registers: [this.register]
    });

    // 系统健康状态
    this.systemHealth = new promClient.Gauge({
      name: 'qms_ai_system_health',
      help: 'System health status (1=healthy, 0=unhealthy)',
      labelNames: ['component'],
      registers: [this.register]
    });

    // 用户评分分布
    this.userRatings = new promClient.Histogram({
      name: 'qms_ai_user_ratings',
      help: 'Distribution of user ratings',
      labelNames: ['model_provider'],
      buckets: [1, 2, 3, 4, 5],
      registers: [this.register]
    });

    // 导出请求数
    this.exportRequests = new promClient.Counter({
      name: 'qms_ai_export_requests_total',
      help: 'Total number of export requests',
      labelNames: ['export_type', 'status'],
      registers: [this.register]
    });
  }

  // 记录HTTP请求
  recordHttpRequest(method, route, statusCode, duration) {
    this.httpRequestsTotal.inc({
      method,
      route,
      status_code: statusCode
    });

    this.httpRequestDuration.observe({
      method,
      route,
      status_code: statusCode
    }, duration);
  }

  // 记录AI请求
  recordAiRequest(modelProvider, modelName, status, responseTime) {
    this.aiRequestsTotal.inc({
      model_provider: modelProvider,
      model_name: modelName,
      status
    });

    if (responseTime && status === 'success') {
      this.aiResponseTime.observe({
        model_provider: modelProvider,
        model_name: modelName
      }, responseTime);
    }
  }

  // 更新数据库连接状态
  updateDbConnections(databaseType, activeConnections) {
    this.dbConnectionsActive.set({
      database_type: databaseType
    }, activeConnections);
  }

  // 更新缓存命中率
  updateCacheHitRate(cacheType, hitRate) {
    this.cacheHitRate.set({
      cache_type: cacheType
    }, hitRate);
  }

  // 更新活跃用户数
  updateActiveUsers(count) {
    this.activeUsers.set(count);
  }

  // 记录新对话
  recordConversation(userType = 'anonymous') {
    this.conversationsTotal.inc({
      user_type: userType
    });
  }

  // 记录新消息
  recordMessage(messageType, modelProvider = 'unknown') {
    this.messagesTotal.inc({
      message_type: messageType,
      model_provider: modelProvider
    });
  }

  // 更新错误率
  updateErrorRate(service, errorType, rate) {
    this.errorRate.set({
      service,
      error_type: errorType
    }, rate);
  }

  // 更新系统健康状态
  updateSystemHealth(component, isHealthy) {
    this.systemHealth.set({
      component
    }, isHealthy ? 1 : 0);
  }

  // 记录用户评分
  recordUserRating(modelProvider, rating) {
    this.userRatings.observe({
      model_provider: modelProvider
    }, rating);
  }

  // 记录导出请求
  recordExportRequest(exportType, status) {
    this.exportRequests.inc({
      export_type: exportType,
      status
    });
  }

  // 获取所有指标
  async getMetrics() {
    return await this.register.metrics();
  }

  // 获取指标的内容类型
  getContentType() {
    return this.register.contentType;
  }

  // 清除所有指标
  clear() {
    this.register.clear();
  }

  // 创建中间件用于自动记录HTTP请求
  createMiddleware() {
    return (req, res, next) => {
      const startTime = Date.now();
      
      // 监听响应结束事件
      res.on('finish', () => {
        const duration = (Date.now() - startTime) / 1000;
        const route = req.route ? req.route.path : req.path;
        
        this.recordHttpRequest(
          req.method,
          route,
          res.statusCode,
          duration
        );
      });

      next();
    };
  }

  // 定期更新系统指标
  startPeriodicUpdates(dbAdapter, cache) {
    setInterval(async () => {
      try {
        // 更新数据库健康状态
        if (dbAdapter) {
          const dbHealth = await dbAdapter.healthCheck();
          this.updateSystemHealth('database', dbHealth.database.status === 'healthy');
          
          // 如果是PostgreSQL，更新连接池状态
          if (dbHealth.database.type === 'postgresql' && dbAdapter.primaryDB.pool) {
            this.updateDbConnections('postgresql', dbAdapter.primaryDB.pool.totalCount);
          }
        }

        // 更新缓存健康状态和命中率
        if (cache && cache.isReady()) {
          const cacheHealth = await cache.healthCheck();
          this.updateSystemHealth('cache', cacheHealth.status === 'healthy');
          
          // 这里可以添加缓存命中率统计逻辑
          // this.updateCacheHitRate('redis', hitRate);
        }

        // 更新系统整体健康状态
        this.updateSystemHealth('system', true);

      } catch (error) {
        console.error('更新监控指标失败:', error.message);
        this.updateSystemHealth('system', false);
      }
    }, 30000); // 每30秒更新一次
  }
}

module.exports = PrometheusMetrics;
