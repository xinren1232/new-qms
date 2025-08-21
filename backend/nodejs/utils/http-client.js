/**
 * QMS-AI HTTP客户端工具
 * 统一管理外部API调用，支持超时、重试、监控、熔断
 */

const axios = require('axios');
const EventEmitter = require('events');
const { RobustHttpClient } = require('./robust-http-client');

class HttpClient extends EventEmitter {
  constructor(options = {}) {
    super();

    this.options = {
      timeout: options.timeout || 30000, // 30秒默认超时
      retries: options.retries || 3,
      retryDelay: options.retryDelay || 1000,
      retryDelayMultiplier: options.retryDelayMultiplier || 2,
      maxRetryDelay: options.maxRetryDelay || 10000,
      enableMetrics: options.enableMetrics !== false,
      // 熔断器配置
      circuitBreaker: {
        enabled: options.circuitBreaker?.enabled !== false,
        failureThreshold: options.circuitBreaker?.failureThreshold || 5,
        resetTimeout: options.circuitBreaker?.resetTimeout || 60000,
        ...options.circuitBreaker
      }
    };

    // 使用健壮的HTTP客户端
    this.robustClient = new RobustHttpClient({
      timeout: this.options.timeout,
      retries: this.options.retries,
      retryDelay: this.options.retryDelay,
      retryDelayMultiplier: this.options.retryDelayMultiplier,
      circuitBreaker: this.options.circuitBreaker,
      headers: {
        'User-Agent': 'QMS-AI/1.0',
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      }
    });

    // 监听健壮客户端事件
    this.setupRobustClientListeners();

    // 请求统计（保持向后兼容）
    this.metrics = {
      totalRequests: 0,
      successRequests: 0,
      failedRequests: 0,
      timeoutRequests: 0,
      retryRequests: 0,
      averageResponseTime: 0,
      responseTimeSum: 0
    };

    // 创建axios实例（保持向后兼容）
    this.client = axios.create({
      timeout: this.options.timeout,
      headers: {
        'User-Agent': 'QMS-AI/1.0',
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      }
    });

    // 设置健壮客户端事件监听器
    this.setupRobustClientListeners();

    // 设置传统拦截器（保持向后兼容）
    this.setupLegacyInterceptors();
  }

  /**
   * 设置健壮客户端事件监听器
   */
  setupRobustClientListeners() {
    this.robustClient.on('request-start', (data) => {
      console.log(`🌐 HTTP请求: ${data.method?.toUpperCase()} ${data.url}`);
      this.emit('request', data);
    });

    this.robustClient.on('request-success', (data) => {
      this.metrics.successRequests++;
      this.updateResponseTime(data.duration);
      console.log(`✅ HTTP成功: ${data.method?.toUpperCase()} ${data.url} (${data.duration}ms)`);
      this.emit('success', data);
    });

    this.robustClient.on('request-error', (data) => {
      this.metrics.failedRequests++;
      if (data.error?.includes('timeout')) {
        this.metrics.timeoutRequests++;
      }
      console.error(`❌ HTTP失败: ${data.method?.toUpperCase()} ${data.url} - ${data.error}`);
      this.emit('error', data);
    });

    this.robustClient.on('circuit-breaker-open', (data) => {
      console.warn(`⚠️ 熔断器开启: ${data.serviceKey}`);
      this.emit('circuit-breaker-open', data);
    });

    this.robustClient.on('circuit-breaker-closed', (data) => {
      console.log(`✅ 熔断器关闭: ${data.serviceKey}`);
      this.emit('circuit-breaker-closed', data);
    });
  }

  /**
   * 设置传统拦截器（保持向后兼容）
   */
  setupLegacyInterceptors() {
    // 请求拦截器
    this.client.interceptors.request.use(
      (config) => {
        config.metadata = { startTime: Date.now() };

        return config;
      },
      (error) => {
        console.error('❌ HTTP请求拦截器错误:', error.message);
        return Promise.reject(error);
      }
    );
    
    // 响应拦截器
    this.client.interceptors.response.use(
      (response) => {
        const duration = Date.now() - response.config.metadata.startTime;
        this.updateMetrics(true, duration);
        
        console.log(`✅ HTTP响应: ${response.status} ${response.config.url} (${duration}ms)`);
        this.emit('response', { 
          status: response.status, 
          url: response.config.url, 
          duration,
          response 
        });
        
        return response;
      },
      (error) => {
        const duration = error.config?.metadata ? 
          Date.now() - error.config.metadata.startTime : 0;
        
        if (error.code === 'ECONNABORTED') {
          this.metrics.timeoutRequests++;
          console.error(`⏰ HTTP超时: ${error.config?.url} (${duration}ms)`);
        } else {
          console.error(`❌ HTTP错误: ${error.config?.url} - ${error.message} (${duration}ms)`);
        }
        
        this.updateMetrics(false, duration);
        this.emit('error', { 
          error, 
          url: error.config?.url, 
          duration,
          isTimeout: error.code === 'ECONNABORTED'
        });
        
        return Promise.reject(error);
      }
    );
  }

  /**
   * 更新请求统计
   */
  updateMetrics(success, duration) {
    if (success) {
      this.metrics.successRequests++;
    } else {
      this.metrics.failedRequests++;
    }
    
    this.metrics.responseTimeSum += duration;
    this.metrics.averageResponseTime = 
      this.metrics.responseTimeSum / this.metrics.totalRequests;
  }

  /**
   * 带重试的HTTP请求
   */
  async requestWithRetry(config, attempt = 1) {
    try {
      const response = await this.client.request(config);
      return response;
    } catch (error) {
      if (attempt < this.options.retries && this.shouldRetry(error)) {
        this.metrics.retryRequests++;
        
        const delay = Math.min(
          this.options.retryDelay * Math.pow(this.options.retryDelayMultiplier, attempt - 1),
          this.options.maxRetryDelay
        );
        
        console.warn(`🔄 HTTP重试 ${attempt}/${this.options.retries}: ${config.url} (延迟${delay}ms)`);
        this.emit('retry', { attempt, url: config.url, delay, error });
        
        await this.sleep(delay);
        return this.requestWithRetry(config, attempt + 1);
      }
      
      throw error;
    }
  }

  /**
   * 判断是否应该重试
   */
  shouldRetry(error) {
    // 网络错误或5xx服务器错误才重试
    if (error.code === 'ECONNABORTED') return false; // 超时不重试
    if (error.code === 'ENOTFOUND') return true; // DNS错误重试
    if (error.code === 'ECONNREFUSED') return true; // 连接拒绝重试
    if (error.response?.status >= 500) return true; // 5xx错误重试
    if (error.response?.status === 429) return true; // 限流重试
    
    return false;
  }

  /**
   * 延迟函数
   */
  sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  /**
   * GET请求 - 使用健壮客户端
   */
  async get(url, config = {}) {
    const fallback = config.fallback;
    delete config.fallback;

    try {
      return await this.robustClient.get(url, config, fallback);
    } catch (error) {
      // 降级到传统客户端
      console.warn('健壮客户端失败，降级到传统客户端');
      return this.requestWithRetry({
        method: 'GET',
        url,
        ...config
      });
    }
  }

  /**
   * POST请求 - 使用健壮客户端
   */
  async post(url, data = null, config = {}) {
    const fallback = config.fallback;
    delete config.fallback;

    try {
      return await this.robustClient.post(url, data, config, fallback);
    } catch (error) {
      // 降级到传统客户端
      console.warn('健壮客户端失败，降级到传统客户端');
      return this.requestWithRetry({
        method: 'POST',
        url,
        data,
        ...config
      });
    }
  }

  /**
   * PUT请求
   */
  async put(url, data = null, config = {}) {
    return this.requestWithRetry({
      method: 'PUT',
      url,
      data,
      ...config
    });
  }

  /**
   * DELETE请求
   */
  async delete(url, config = {}) {
    return this.requestWithRetry({
      method: 'DELETE',
      url,
      ...config
    });
  }

  /**
   * AI API专用请求（更长超时时间）
   */
  async aiRequest(url, data, config = {}) {
    const aiConfig = {
      timeout: 0, // 解除AI请求超时限制，支持长时间响应
      ...config,
      headers: {
        'Content-Type': 'application/json',
        ...config.headers
      }
    };

    return this.requestWithRetry({
      method: 'POST',
      url,
      data,
      ...aiConfig
    });
  }

  /**
   * 配置中心请求（快速超时）
   */
  async configRequest(url, config = {}) {
    const configConfig = {
      timeout: 5000, // 配置请求5秒超时
      ...config
    };

    return this.requestWithRetry({
      method: 'GET',
      url,
      ...configConfig
    });
  }

  /**
   * 健康检查请求（最快超时）
   */
  async healthCheck(url, config = {}) {
    const healthConfig = {
      timeout: 3000, // 健康检查3秒超时
      retries: 1, // 只重试1次
      ...config
    };

    try {
      return await this.robustClient.healthCheck(url, healthConfig);
    } catch (error) {
      // 降级到传统客户端
      console.warn('健壮客户端健康检查失败，降级到传统客户端');
      return this.requestWithRetry({
        method: 'GET',
        url,
        ...healthConfig
      });
    }
  }

  /**
   * 获取请求统计
   */
  getMetrics() {
    const successRate = this.metrics.totalRequests > 0 ? 
      (this.metrics.successRequests / this.metrics.totalRequests * 100).toFixed(2) : 0;
    
    const timeoutRate = this.metrics.totalRequests > 0 ? 
      (this.metrics.timeoutRequests / this.metrics.totalRequests * 100).toFixed(2) : 0;

    return {
      ...this.metrics,
      successRate: `${successRate}%`,
      timeoutRate: `${timeoutRate}%`,
      averageResponseTime: Math.round(this.metrics.averageResponseTime)
    };
  }

  /**
   * 重置统计
   */
  resetMetrics() {
    this.metrics = {
      totalRequests: 0,
      successRequests: 0,
      failedRequests: 0,
      timeoutRequests: 0,
      retryRequests: 0,
      averageResponseTime: 0,
      responseTimeSum: 0
    };
  }

  /**
   * 设置全局超时
   */
  setTimeout(timeout) {
    this.options.timeout = timeout;
    this.client.defaults.timeout = timeout;
  }

  /**
   * 设置重试次数
   */
  setRetries(retries) {
    this.options.retries = retries;
  }

  /**
   * 获取配置信息
   */
  getConfig() {
    return {
      timeout: this.options.timeout,
      retries: this.options.retries,
      retryDelay: this.options.retryDelay,
      retryDelayMultiplier: this.options.retryDelayMultiplier,
      maxRetryDelay: this.options.maxRetryDelay
    };
  }
}

// 创建全局HTTP客户端实例
const httpClient = new HttpClient({
  timeout: parseInt(process.env.HTTP_TIMEOUT) || 30000,
  retries: parseInt(process.env.HTTP_RETRIES) || 3,
  retryDelay: parseInt(process.env.HTTP_RETRY_DELAY) || 1000
});

// 监听HTTP事件用于调试
if (process.env.NODE_ENV === 'development') {
  httpClient.on('request', ({ method, url }) => {
    console.log(`🔍 [DEBUG] HTTP请求: ${method} ${url}`);
  });
  
  httpClient.on('retry', ({ attempt, url, delay }) => {
    console.log(`🔍 [DEBUG] HTTP重试: ${url} 第${attempt}次 延迟${delay}ms`);
  });
  
  httpClient.on('error', ({ url, error, isTimeout }) => {
    console.log(`🔍 [DEBUG] HTTP错误: ${url} ${isTimeout ? '超时' : error.message}`);
  });
}

module.exports = {
  HttpClient,
  httpClient
};
