/**
 * QMS-AI 配置客户端
 * 用于从配置中心获取配置，支持本地缓存和热更新
 */

const EventEmitter = require('events');
const { httpClient } = require('../utils/http-client');

class ConfigClient extends EventEmitter {
  constructor(options = {}) {
    super();

    // 配置中心节点
    this.configNodes = options.configNodes || (options.configServiceUrl ? [options.configServiceUrl] : ['http://localhost:3003']);
    this.configServiceUrl = options.configServiceUrl || this.configNodes[0];
    this.currentNodeIndex = 0;
    this.cache = new Map();
    this.sseConnection = null;
    this.retryCount = 0;
    this.maxRetries = options.maxRetries || 5;
    this.retryDelay = options.retryDelay || 2000;
    this.connected = false;

    // 节点健康状态
    this.nodeHealth = new Map();
    this.configNodes.forEach((url, index) => {
      this.nodeHealth.set(index, { healthy: true, lastCheck: Date.now(), errors: 0 });
    });

    // 启动健康检查
    this.startHealthCheck();

    // 自动连接
    this.connect();
  }

  /**
   * 连接配置中心
   */
  async connect() {
    const healthyNodes = this.getHealthyNodes();

    if (healthyNodes.length === 0) {
      console.error('❌ 所有配置中心节点都不可用');
      this.connected = false;
      this.emit('all-nodes-failed');
      return;
    }

    // 尝试连接健康的节点
    for (const nodeIndex of healthyNodes) {
      try {
        const nodeUrl = this.configNodes[nodeIndex];
        const healthResponse = await httpClient.healthCheck(`${nodeUrl}/health`);

        if (healthResponse.status === 200 && healthResponse.data && healthResponse.data.success) {
          console.log(`✅ 配置中心连接成功: ${nodeUrl}`);
          this.currentNodeIndex = nodeIndex;
          this.connected = true;
          this.retryCount = 0;

          // 建立SSE连接监听配置变更
          this.setupSSEConnection();

          this.emit('connected', { nodeUrl, nodeIndex });
          return;
        }
      } catch (error) {
        console.warn(`⚠️ 节点 ${this.configNodes[nodeIndex]} 连接失败: ${error.message}`);
        this.markNodeUnhealthy(nodeIndex, error);
      }
    }

    // 所有节点都失败
    this.connected = false;

    // 重试连接
    if (this.retryCount < this.maxRetries) {
      this.retryCount++;
      console.log(`🔄 ${this.retryDelay / 1000}秒后重试连接 (${this.retryCount}/${this.maxRetries})`);
      setTimeout(() => this.connect(), this.retryDelay);
    } else {
      console.error('❌ 配置中心连接失败，已达到最大重试次数');
      this.emit('connectionFailed');
    }
  }

  /**
   * 启动健康检查
   */
  startHealthCheck() {
    setInterval(async () => {
      await this.performHealthCheck();
    }, 30000); // 30秒检查一次

    // 立即执行一次
    this.performHealthCheck();
  }

  /**
   * 执行健康检查
   */
  async performHealthCheck() {
    const checkPromises = this.configNodes.map(async (nodeUrl, index) => {
      try {
        const response = await httpClient.healthCheck(`${nodeUrl}/health`, { timeout: 5000 });
        // 检查HTTP状态码和响应数据
        // 兼容不同的健康检查响应格式：success字段或status字段
        const isHealthy = response.status === 200 && response.data &&
          (response.data.success === true || response.data.status === 'UP' || response.data.status === 'ok');

        if (isHealthy) {
          this.markNodeHealthy(index);
        } else {
          this.markNodeUnhealthy(index, new Error(`健康检查失败: HTTP ${response.status}, data: ${JSON.stringify(response.data)}`));
        }
      } catch (error) {
        this.markNodeUnhealthy(index, error);
      }
    });

    await Promise.allSettled(checkPromises);
  }

  /**
   * 标记节点健康
   */
  markNodeHealthy(nodeIndex) {
    const health = this.nodeHealth.get(nodeIndex);
    if (health && !health.healthy) {
      console.log(`✅ 配置节点 ${this.configNodes[nodeIndex]} 恢复健康`);
      this.emit('node-recovered', { nodeIndex, nodeUrl: this.configNodes[nodeIndex] });
    }

    this.nodeHealth.set(nodeIndex, {
      healthy: true,
      lastCheck: Date.now(),
      errors: 0
    });
  }

  /**
   * 标记节点不健康
   */
  markNodeUnhealthy(nodeIndex, error) {
    const health = this.nodeHealth.get(nodeIndex) || { errors: 0 };
    health.healthy = false;
    health.lastCheck = Date.now();
    health.errors++;
    health.lastError = error.message;

    this.nodeHealth.set(nodeIndex, health);

    console.warn(`⚠️ 配置节点 ${this.configNodes[nodeIndex]} 不健康: ${error.message}`);
    this.emit('node-unhealthy', { nodeIndex, nodeUrl: this.configNodes[nodeIndex], error: error.message });
  }

  /**
   * 获取健康的节点索引
   */
  getHealthyNodes() {
    const healthyNodes = [];
    for (let i = 0; i < this.configNodes.length; i++) {
      const health = this.nodeHealth.get(i);
      if (health && health.healthy) {
        healthyNodes.push(i);
      }
    }
    return healthyNodes;
  }

  /**
   * 建立SSE连接
   */
  setupSSEConnection() {
    try {
      // 注意：这里使用简单的HTTP请求模拟SSE，实际项目中可以使用EventSource
      // 由于Node.js环境限制，这里采用轮询方式
      this.startConfigPolling();
    } catch (error) {
      console.error('❌ SSE连接建立失败:', error);
    }
  }

  /**
   * 开始配置轮询（简化版的配置变更监听）
   */
  startConfigPolling() {
    // 每30秒检查一次配置变更
    setInterval(async () => {
      if (this.connected) {
        try {
          // 检查AI模型配置是否有变更
          const currentAiConfig = await this.getConfig('ai_models');
          const cachedAiConfig = this.cache.get('ai_models');
          
          if (currentAiConfig && JSON.stringify(currentAiConfig) !== JSON.stringify(cachedAiConfig)) {
            console.log('📢 检测到AI模型配置变更');
            this.cache.set('ai_models', currentAiConfig);
            this.emit('configChanged', 'ai_models', currentAiConfig);
          }
        } catch (error) {
          console.warn('⚠️ 配置轮询检查失败:', error.message);
        }
      }
    }, 30000);
  }

  /**
   * 获取配置 - 支持多节点故障转移
   */
  async getConfig(configName, key = null, useCache = true) {
    // 如果使用缓存且缓存中有数据
    if (useCache && this.cache.has(configName)) {
      const cachedConfig = this.cache.get(configName);
      if (key) {
        return this.getNestedValue(cachedConfig, key);
      }
      return cachedConfig;
    }

    // 从配置中心获取 - 支持故障转移
    const healthyNodes = this.getHealthyNodes();

    if (healthyNodes.length === 0) {
      console.warn('⚠️ 所有配置节点不可用，使用缓存配置');
      return this.getFromCache(configName, key);
    }

    // 尝试从健康的节点获取配置
    for (const nodeIndex of healthyNodes) {
      try {
        const nodeUrl = this.configNodes[nodeIndex];
        const url = key
          ? `${nodeUrl}/api/configs/${configName}?key=${key}`
          : `${nodeUrl}/api/configs/${configName}`;

        const response = await httpClient.configRequest(url);

        if (response.data.success) {
          const config = response.data.data;

          // 缓存完整配置（如果没有指定key）
          if (!key) {
            this.cache.set(configName, config);
          }

          console.log(`✅ 从节点 ${nodeUrl} 获取配置: ${configName}${key ? '.' + key : ''}`);
          return config;
        } else {
          throw new Error(response.data.message || '获取配置失败');
        }
      } catch (error) {
        console.warn(`⚠️ 节点 ${this.configNodes[nodeIndex]} 获取配置失败: ${error.message}`);
        this.markNodeUnhealthy(nodeIndex, error);
      }
    }

    // 所有节点都失败，使用缓存
    console.error(`❌ 所有节点获取配置失败 ${configName}${key ? '.' + key : ''}，使用缓存`);
    return this.getFromCache(configName, key);
  }

  /**
   * 从缓存获取配置
   */
  getFromCache(configName, key = null) {
    if (this.cache.has(configName)) {
      console.log(`📦 使用缓存配置: ${configName}`);
      const cachedConfig = this.cache.get(configName);
      if (key) {
        return this.getNestedValue(cachedConfig, key);
      }
      return cachedConfig;
    }

    return null;
  }

  /**
   * 获取AI模型配置
   */
  async getAIModels() {
    try {
      // 使用当前连接的节点URL
      const currentNodeUrl = this.configNodes[this.currentNodeIndex];
      const response = await httpClient.configRequest(`${currentNodeUrl}/api/ai/models`);

      if (response.data.success) {
        const aiConfig = response.data.data;
        this.cache.set('ai_models', aiConfig);

        // 转换对象格式为数组格式以兼容聊天服务
        if (aiConfig && aiConfig.models) {
          const modelsArray = Object.keys(aiConfig.models).map(key => {
            const model = aiConfig.models[key];

            // 根据baseURL判断provider和是否为外网模型
            let provider = 'unknown';
            let isExternal = false;

            if (model.baseURL && model.baseURL.includes('api.deepseek.com')) {
              // 外网DeepSeek模型
              provider = 'deepseek';
              isExternal = true;
            } else if (key.includes('deepseek')) {
              // 内网DeepSeek模型（通过传音代理）
              provider = 'deepseek-internal';
              isExternal = false;
            } else if (key.includes('gpt') || key.includes('openai')) {
              provider = 'openai';
            } else if (key.includes('claude') || key.includes('anthropic')) {
              provider = 'anthropic';
            } else if (key.includes('gemini') || key.includes('google')) {
              provider = 'google';
            }

            return {
              id: key,
              provider: provider,
              status: model.enabled ? 'active' : 'inactive',
              ...model,
              features: {
                ...model.features,
                external: isExternal
              }
            };
          });
          return modelsArray;
        }

        return aiConfig;
      } else {
        throw new Error(response.data.message || '获取AI模型配置失败');
      }
    } catch (error) {
      console.error('❌ 获取AI模型配置失败:', error.message);

      // 尝试使用缓存
      if (this.cache.has('ai_models')) {
        console.log('📦 使用缓存的AI模型配置');
        const cachedConfig = this.cache.get('ai_models');

        // 转换缓存的配置格式
        if (cachedConfig && cachedConfig.models) {
          const modelsArray = Object.keys(cachedConfig.models).map(key => {
            const model = cachedConfig.models[key];

            // 根据baseURL判断provider和是否为外网模型
            let provider = 'unknown';
            let isExternal = false;

            if (model.baseURL && model.baseURL.includes('api.deepseek.com')) {
              // 外网DeepSeek模型
              provider = 'deepseek';
              isExternal = true;
            } else if (key.includes('deepseek')) {
              // 内网DeepSeek模型（通过传音代理）
              provider = 'deepseek-internal';
              isExternal = false;
            } else if (key.includes('gpt') || key.includes('openai')) {
              provider = 'openai';
            } else if (key.includes('claude') || key.includes('anthropic')) {
              provider = 'anthropic';
            } else if (key.includes('gemini') || key.includes('google')) {
              provider = 'google';
            }

            return {
              id: key,
              provider: provider,
              status: model.enabled ? 'active' : 'inactive',
              ...model,
              features: {
                ...model.features,
                external: isExternal
              }
            };
          });
          return modelsArray;
        }

        return cachedConfig;
      }

      return null;
    }
  }

  /**
   * 切换AI模型
   */
  async switchAIModel(modelKey) {
    try {
      const response = await httpClient.post(`${this.configServiceUrl}/api/ai/models/switch`, {
        model_key: modelKey
      }, { timeout: 5000 });
      
      if (response.data.success) {
        console.log(`✅ 模型切换成功: ${modelKey}`);
        
        // 更新缓存
        const aiConfig = this.cache.get('ai_models');
        if (aiConfig) {
          aiConfig.default_model = modelKey;
          this.cache.set('ai_models', aiConfig);
        }
        
        this.emit('modelSwitched', modelKey, response.data.model_info);
        return response.data;
      } else {
        throw new Error(response.data.message || '切换模型失败');
      }
    } catch (error) {
      console.error(`❌ 切换模型失败 ${modelKey}:`, error.message);
      throw error;
    }
  }

  /**
   * 获取服务配置
   */
  async getServiceConfig() {
    try {
      const response = await httpClient.configRequest(`${this.configServiceUrl}/api/services`);
      
      if (response.data.success) {
        const serviceConfig = response.data.data;
        this.cache.set('services', serviceConfig);
        return serviceConfig;
      } else {
        throw new Error(response.data.message || '获取服务配置失败');
      }
    } catch (error) {
      console.error('❌ 获取服务配置失败:', error.message);
      
      // 尝试使用缓存
      if (this.cache.has('services')) {
        console.log('📦 使用缓存的服务配置');
        return this.cache.get('services');
      }
      
      return null;
    }
  }

  /**
   * 获取嵌套值
   */
  getNestedValue(obj, key) {
    return key.split('.').reduce((current, prop) => {
      return current && current[prop] !== undefined ? current[prop] : null;
    }, obj);
  }

  /**
   * 检查连接状态
   */
  isConnected() {
    return this.connected;
  }

  /**
   * 获取缓存的配置
   */
  getCachedConfig(configName) {
    return this.cache.get(configName) || null;
  }

  /**
   * 清除缓存
   */
  clearCache(configName = null) {
    if (configName) {
      this.cache.delete(configName);
      console.log(`🗑️ 已清除缓存: ${configName}`);
    } else {
      this.cache.clear();
      console.log('🗑️ 已清除所有缓存');
    }
  }

  /**
   * 获取缓存状态
   */
  getCacheStatus() {
    return {
      connected: this.connected,
      cached_configs: Array.from(this.cache.keys()),
      cache_size: this.cache.size,
      config_service_url: this.configServiceUrl
    };
  }

  /**
   * 关闭连接
   */
  close() {
    this.connected = false;
    this.cache.clear();
    
    if (this.sseConnection) {
      this.sseConnection.close();
      this.sseConnection = null;
    }
    
    console.log('🔒 配置客户端已关闭');
  }
}

module.exports = ConfigClient;
