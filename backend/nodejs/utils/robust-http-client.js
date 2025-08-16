/**
 * 健壮的HTTP客户端
 * 提供超时、重试、熔断、降级等容错机制
 */

const axios = require('axios');
const EventEmitter = require('events');

/**
 * 熔断器类
 */
class CircuitBreaker extends EventEmitter {
  constructor(options = {}) {
    super();
    
    this.options = {
      failureThreshold: options.failureThreshold || 5,     // 失败阈值
      resetTimeout: options.resetTimeout || 60000,         // 重置超时(1分钟)
      monitoringPeriod: options.monitoringPeriod || 10000, // 监控周期(10秒)
      halfOpenMaxCalls: options.halfOpenMaxCalls || 3,     // 半开状态最大调用次数
      ...options
    };
    
    this.state = 'CLOSED';        // CLOSED, OPEN, HALF_OPEN
    this.failureCount = 0;        // 失败次数
    this.successCount = 0;        // 成功次数
    this.lastFailureTime = null;  // 最后失败时间
    this.nextAttemptTime = null;  // 下次尝试时间
    
    // 统计信息
    this.stats = {
      totalCalls: 0,
      successCalls: 0,
      failureCalls: 0,
      timeouts: 0,
      circuitOpenCalls: 0
    };
  }

  /**
   * 执行操作
   */
  async execute(operation, fallback = null) {
    this.stats.totalCalls++;
    
    // 检查熔断器状态
    if (this.state === 'OPEN') {
      if (Date.now() < this.nextAttemptTime) {
        this.stats.circuitOpenCalls++;
        const error = new Error('熔断器开启，服务不可用');
        error.code = 'CIRCUIT_BREAKER_OPEN';
        
        if (fallback) {
          console.log('🔄 熔断器开启，执行降级策略');
          return await fallback();
        }
        
        throw error;
      } else {
        // 进入半开状态
        this.state = 'HALF_OPEN';
        this.successCount = 0;
        console.log('🔄 熔断器进入半开状态');
        this.emit('half-open');
      }
    }
    
    try {
      const result = await operation();
      this.onSuccess();
      return result;
    } catch (error) {
      this.onFailure(error);
      
      if (fallback && this.state === 'OPEN') {
        console.log('🔄 操作失败，执行降级策略');
        return await fallback();
      }
      
      throw error;
    }
  }

  /**
   * 成功回调
   */
  onSuccess() {
    this.stats.successCalls++;
    this.failureCount = 0;
    
    if (this.state === 'HALF_OPEN') {
      this.successCount++;
      if (this.successCount >= this.options.halfOpenMaxCalls) {
        this.state = 'CLOSED';
        console.log('✅ 熔断器关闭，服务恢复正常');
        this.emit('closed');
      }
    }
  }

  /**
   * 失败回调
   */
  onFailure(error) {
    this.stats.failureCalls++;
    this.failureCount++;
    this.lastFailureTime = Date.now();
    
    if (error.code === 'ECONNABORTED' || error.code === 'ETIMEDOUT') {
      this.stats.timeouts++;
    }
    
    if (this.failureCount >= this.options.failureThreshold) {
      this.state = 'OPEN';
      this.nextAttemptTime = Date.now() + this.options.resetTimeout;
      console.warn(`⚠️ 熔断器开启，${this.options.resetTimeout / 1000}秒后重试`);
      this.emit('open', { failureCount: this.failureCount, error });
    }
  }

  /**
   * 获取状态
   */
  getState() {
    return {
      state: this.state,
      failureCount: this.failureCount,
      successCount: this.successCount,
      lastFailureTime: this.lastFailureTime,
      nextAttemptTime: this.nextAttemptTime,
      stats: this.stats
    };
  }

  /**
   * 重置熔断器
   */
  reset() {
    this.state = 'CLOSED';
    this.failureCount = 0;
    this.successCount = 0;
    this.lastFailureTime = null;
    this.nextAttemptTime = null;
    console.log('🔄 熔断器已重置');
    this.emit('reset');
  }
}

/**
 * 健壮的HTTP客户端
 */
class RobustHttpClient extends EventEmitter {
  constructor(options = {}) {
    super();
    
    this.options = {
      // 基础配置
      timeout: options.timeout || 30000,           // 30秒超时
      retries: options.retries || 3,               // 重试3次
      retryDelay: options.retryDelay || 1000,      // 重试延迟1秒
      retryDelayMultiplier: options.retryDelayMultiplier || 2, // 指数退避
      
      // 熔断器配置
      circuitBreaker: {
        enabled: options.circuitBreaker?.enabled !== false,
        failureThreshold: options.circuitBreaker?.failureThreshold || 5,
        resetTimeout: options.circuitBreaker?.resetTimeout || 60000,
        ...options.circuitBreaker
      },
      
      // 请求配置
      headers: {
        'User-Agent': 'QMS-AI-RobustClient/1.0',
        'Accept': 'application/json',
        'Content-Type': 'application/json',
        ...options.headers
      },
      
      ...options
    };
    
    // 创建axios实例
    this.client = axios.create({
      timeout: this.options.timeout,
      headers: this.options.headers
    });
    
    // 熔断器实例
    this.circuitBreakers = new Map();
    
    // 设置拦截器
    this.setupInterceptors();
    
    // 统计信息
    this.stats = {
      totalRequests: 0,
      successRequests: 0,
      failedRequests: 0,
      retriedRequests: 0,
      circuitBreakerTrips: 0,
      avgResponseTime: 0
    };
  }

  /**
   * 设置请求和响应拦截器
   */
  setupInterceptors() {
    // 请求拦截器
    this.client.interceptors.request.use(
      (config) => {
        config.metadata = { 
          startTime: Date.now(),
          requestId: this.generateRequestId()
        };
        
        this.stats.totalRequests++;
        this.emit('request-start', { 
          url: config.url, 
          method: config.method,
          requestId: config.metadata.requestId
        });
        
        return config;
      },
      (error) => {
        this.emit('request-error', error);
        return Promise.reject(error);
      }
    );
    
    // 响应拦截器
    this.client.interceptors.response.use(
      (response) => {
        const duration = Date.now() - response.config.metadata.startTime;
        
        // 更新统计信息
        this.stats.successRequests++;
        this.updateAvgResponseTime(duration);
        
        this.emit('request-success', {
          url: response.config.url,
          method: response.config.method,
          status: response.status,
          duration,
          requestId: response.config.metadata.requestId
        });
        
        return response;
      },
      (error) => {
        const duration = error.config ? 
          Date.now() - error.config.metadata.startTime : 0;
        
        this.stats.failedRequests++;
        
        this.emit('request-error', {
          url: error.config?.url,
          method: error.config?.method,
          error: error.message,
          duration,
          requestId: error.config?.metadata?.requestId
        });
        
        return Promise.reject(error);
      }
    );
  }

  /**
   * 获取或创建熔断器
   */
  getCircuitBreaker(serviceKey) {
    if (!this.options.circuitBreaker.enabled) {
      return null;
    }
    
    if (!this.circuitBreakers.has(serviceKey)) {
      const breaker = new CircuitBreaker(this.options.circuitBreaker);
      
      breaker.on('open', (data) => {
        this.stats.circuitBreakerTrips++;
        this.emit('circuit-breaker-open', { serviceKey, ...data });
      });
      
      breaker.on('closed', () => {
        this.emit('circuit-breaker-closed', { serviceKey });
      });
      
      breaker.on('half-open', () => {
        this.emit('circuit-breaker-half-open', { serviceKey });
      });
      
      this.circuitBreakers.set(serviceKey, breaker);
    }
    
    return this.circuitBreakers.get(serviceKey);
  }

  /**
   * 执行HTTP请求
   */
  async request(config, fallback = null) {
    const serviceKey = this.getServiceKey(config.url);
    const circuitBreaker = this.getCircuitBreaker(serviceKey);
    
    const operation = async () => {
      return await this.executeWithRetry(config);
    };
    
    if (circuitBreaker) {
      return await circuitBreaker.execute(operation, fallback);
    } else {
      return await operation();
    }
  }

  /**
   * 带重试的请求执行
   */
  async executeWithRetry(config) {
    let lastError;
    
    for (let attempt = 0; attempt <= this.options.retries; attempt++) {
      try {
        if (attempt > 0) {
          this.stats.retriedRequests++;
          const delay = this.calculateRetryDelay(attempt);
          console.log(`🔄 重试请求 ${config.url} (${attempt}/${this.options.retries})，延迟 ${delay}ms`);
          await this.sleep(delay);
        }
        
        const response = await this.client.request(config);
        return response;
        
      } catch (error) {
        lastError = error;
        
        // 检查是否应该重试
        if (!this.shouldRetry(error, attempt)) {
          break;
        }
      }
    }
    
    throw lastError;
  }

  /**
   * 判断是否应该重试
   */
  shouldRetry(error, attempt) {
    // 已达到最大重试次数
    if (attempt >= this.options.retries) {
      return false;
    }
    
    // 4xx错误通常不重试（除了429）
    if (error.response && error.response.status >= 400 && error.response.status < 500) {
      return error.response.status === 429; // 只重试429 Too Many Requests
    }
    
    // 网络错误、超时、5xx错误可以重试
    return (
      error.code === 'ECONNREFUSED' ||
      error.code === 'ENOTFOUND' ||
      error.code === 'ECONNABORTED' ||
      error.code === 'ETIMEDOUT' ||
      (error.response && error.response.status >= 500)
    );
  }

  /**
   * 计算重试延迟（指数退避）
   */
  calculateRetryDelay(attempt) {
    return this.options.retryDelay * Math.pow(this.options.retryDelayMultiplier, attempt - 1);
  }

  /**
   * GET请求
   */
  async get(url, config = {}, fallback = null) {
    return await this.request({ ...config, method: 'GET', url }, fallback);
  }

  /**
   * POST请求
   */
  async post(url, data = {}, config = {}, fallback = null) {
    return await this.request({ ...config, method: 'POST', url, data }, fallback);
  }

  /**
   * PUT请求
   */
  async put(url, data = {}, config = {}, fallback = null) {
    return await this.request({ ...config, method: 'PUT', url, data }, fallback);
  }

  /**
   * DELETE请求
   */
  async delete(url, config = {}, fallback = null) {
    return await this.request({ ...config, method: 'DELETE', url }, fallback);
  }

  /**
   * 健康检查
   */
  async healthCheck(url, config = {}) {
    return await this.get(url, { 
      ...config, 
      timeout: config.timeout || 5000 
    });
  }

  /**
   * 获取服务键
   */
  getServiceKey(url) {
    try {
      const urlObj = new URL(url);
      return `${urlObj.protocol}//${urlObj.host}`;
    } catch (error) {
      return url;
    }
  }

  /**
   * 生成请求ID
   */
  generateRequestId() {
    return Math.random().toString(36).substring(2, 15) + 
           Math.random().toString(36).substring(2, 15);
  }

  /**
   * 更新平均响应时间
   */
  updateAvgResponseTime(duration) {
    const totalRequests = this.stats.successRequests;
    this.stats.avgResponseTime = 
      (this.stats.avgResponseTime * (totalRequests - 1) + duration) / totalRequests;
  }

  /**
   * 睡眠函数
   */
  sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  /**
   * 获取统计信息
   */
  getStats() {
    const circuitBreakerStats = {};
    for (const [key, breaker] of this.circuitBreakers.entries()) {
      circuitBreakerStats[key] = breaker.getState();
    }
    
    return {
      ...this.stats,
      circuitBreakers: circuitBreakerStats,
      successRate: this.stats.totalRequests > 0 ? 
        (this.stats.successRequests / this.stats.totalRequests * 100).toFixed(2) + '%' : '0%'
    };
  }

  /**
   * 重置统计信息
   */
  resetStats() {
    this.stats = {
      totalRequests: 0,
      successRequests: 0,
      failedRequests: 0,
      retriedRequests: 0,
      circuitBreakerTrips: 0,
      avgResponseTime: 0
    };
    
    // 重置所有熔断器
    for (const breaker of this.circuitBreakers.values()) {
      breaker.reset();
    }
  }
}

// 创建默认实例
const defaultClient = new RobustHttpClient();

module.exports = {
  RobustHttpClient,
  CircuitBreaker,
  default: defaultClient
};
