/**
 * QMS-AI 配置驱动的AI模型动态管理器
 * 
 * 功能:
 * 1. 动态加载和管理8个AI模型
 * 2. 配置驱动的模型切换
 * 3. 负载均衡和故障转移
 * 4. 性能监控和优化
 */

const { EventEmitter } = require('events');
const fs = require('fs').promises;
const path = require('path');

// AI模型配置
const AI_MODELS = {
  // 外部模型 - 直连DeepSeek
  'deepseek-chat': {
    name: 'DeepSeek Chat',
    provider: 'deepseek',
    endpoint: 'https://api.deepseek.com/v1/chat/completions',
    type: 'external',
    capabilities: ['chat', 'reasoning'],
    maxTokens: 4096,
    priority: 1
  },
  'deepseek-coder': {
    name: 'DeepSeek Coder',
    provider: 'deepseek',
    endpoint: 'https://api.deepseek.com/v1/chat/completions',
    type: 'external',
    capabilities: ['coding', 'analysis'],
    maxTokens: 8192,
    priority: 1
  },

  // 内部模型 - 通过传音代理
  'transsion-qwen-plus': {
    name: 'Transsion Qwen Plus',
    provider: 'transsion',
    endpoint: 'http://internal-proxy/qwen-plus',
    type: 'internal',
    capabilities: ['chat', 'analysis', 'reasoning'],
    maxTokens: 8192,
    priority: 2
  },
  'transsion-qwen-turbo': {
    name: 'Transsion Qwen Turbo',
    provider: 'transsion',
    endpoint: 'http://internal-proxy/qwen-turbo',
    type: 'internal',
    capabilities: ['chat', 'fast-response'],
    maxTokens: 4096,
    priority: 3
  },
  'transsion-qwen-max': {
    name: 'Transsion Qwen Max',
    provider: 'transsion',
    endpoint: 'http://internal-proxy/qwen-max',
    type: 'internal',
    capabilities: ['chat', 'complex-reasoning'],
    maxTokens: 16384,
    priority: 1
  },
  'transsion-qwen-long': {
    name: 'Transsion Qwen Long',
    provider: 'transsion',
    endpoint: 'http://internal-proxy/qwen-long',
    type: 'internal',
    capabilities: ['long-context', 'document-analysis'],
    maxTokens: 32768,
    priority: 2
  },
  'transsion-glm-4-plus': {
    name: 'Transsion GLM-4 Plus',
    provider: 'transsion',
    endpoint: 'http://internal-proxy/glm-4-plus',
    type: 'internal',
    capabilities: ['chat', 'multimodal'],
    maxTokens: 8192,
    priority: 2
  },
  'transsion-glm-4-0520': {
    name: 'Transsion GLM-4 0520',
    provider: 'transsion',
    endpoint: 'http://internal-proxy/glm-4-0520',
    type: 'internal',
    capabilities: ['chat', 'latest-features'],
    maxTokens: 8192,
    priority: 3
  }
};

class AIModelManager extends EventEmitter {
  constructor() {
    super();
    this.models = new Map();
    this.activeModels = new Set();
    this.modelStats = new Map();
    this.config = {
      loadBalancing: true,
      failover: true,
      healthCheckInterval: 30000,
      maxRetries: 3
    };
    this.healthCheckTimer = null;
  }

  async initialize() {
    console.log('🤖 初始化AI模型管理器...');
    
    // 加载模型配置
    await this.loadModelConfigurations();
    
    // 启动健康检查
    this.startHealthCheck();
    
    // 加载配置文件
    await this.loadConfigFromFile();
    
    console.log(`✅ AI模型管理器初始化完成，共加载${this.models.size}个模型`);
    this.emit('initialized');
  }

  async loadModelConfigurations() {
    for (const [modelId, config] of Object.entries(AI_MODELS)) {
      this.models.set(modelId, {
        ...config,
        id: modelId,
        status: 'unknown',
        lastCheck: null,
        responseTime: 0,
        errorCount: 0,
        requestCount: 0,
        successCount: 0
      });
      
      this.modelStats.set(modelId, {
        totalRequests: 0,
        successfulRequests: 0,
        failedRequests: 0,
        averageResponseTime: 0,
        lastUsed: null
      });
    }
  }

  async loadConfigFromFile() {
    try {
      const configPath = path.join(process.cwd(), 'config', 'ai-models.json');
      const configData = await fs.readFile(configPath, 'utf-8');
      const fileConfig = JSON.parse(configData);
      
      // 合并配置
      Object.assign(this.config, fileConfig);
      console.log('📄 从配置文件加载AI模型配置');
    } catch (error) {
      console.log('⚠️ 未找到配置文件，使用默认配置');
      await this.saveConfigToFile();
    }
  }

  async saveConfigToFile() {
    try {
      const configDir = path.join(process.cwd(), 'config');
      await fs.mkdir(configDir, { recursive: true });
      
      const configPath = path.join(configDir, 'ai-models.json');
      const configData = {
        ...this.config,
        models: Object.fromEntries(this.models),
        lastUpdated: new Date().toISOString()
      };
      
      await fs.writeFile(configPath, JSON.stringify(configData, null, 2));
      console.log('💾 AI模型配置已保存到文件');
    } catch (error) {
      console.error('❌ 保存配置文件失败:', error.message);
    }
  }

  async checkModelHealth(modelId) {
    const model = this.models.get(modelId);
    if (!model) return false;

    try {
      const startTime = Date.now();
      
      // 这里应该实际调用模型API进行健康检查
      // 为了演示，我们模拟一个检查
      const isHealthy = await this.simulateHealthCheck(model);
      
      const responseTime = Date.now() - startTime;
      
      model.status = isHealthy ? 'healthy' : 'unhealthy';
      model.lastCheck = new Date().toISOString();
      model.responseTime = responseTime;
      
      if (isHealthy) {
        model.errorCount = 0;
        this.activeModels.add(modelId);
      } else {
        model.errorCount++;
        this.activeModels.delete(modelId);
      }
      
      return isHealthy;
    } catch (error) {
      model.status = 'error';
      model.errorCount++;
      this.activeModels.delete(modelId);
      return false;
    }
  }

  async simulateHealthCheck(model) {
    // 模拟健康检查 - 在实际环境中这里会调用真实的API
    const random = Math.random();
    
    // 外部模型有95%的可用性
    if (model.type === 'external') {
      return random > 0.05;
    }
    
    // 内部模型有90%的可用性
    return random > 0.1;
  }

  startHealthCheck() {
    if (this.healthCheckTimer) {
      clearInterval(this.healthCheckTimer);
    }

    this.healthCheckTimer = setInterval(async () => {
      console.log('🔍 执行AI模型健康检查...');
      
      const healthPromises = Array.from(this.models.keys()).map(modelId => 
        this.checkModelHealth(modelId)
      );
      
      await Promise.all(healthPromises);
      
      const healthyCount = this.activeModels.size;
      const totalCount = this.models.size;
      
      console.log(`📊 健康检查完成: ${healthyCount}/${totalCount} 模型可用`);
      
      this.emit('healthCheckComplete', {
        healthy: healthyCount,
        total: totalCount,
        activeModels: Array.from(this.activeModels)
      });
      
    }, this.config.healthCheckInterval);
  }

  selectBestModel(capability = 'chat', excludeModels = []) {
    const availableModels = Array.from(this.activeModels)
      .filter(modelId => !excludeModels.includes(modelId))
      .map(modelId => this.models.get(modelId))
      .filter(model => model.capabilities.includes(capability))
      .sort((a, b) => {
        // 按优先级和响应时间排序
        if (a.priority !== b.priority) {
          return a.priority - b.priority;
        }
        return a.responseTime - b.responseTime;
      });

    return availableModels[0] || null;
  }

  async processRequest(request, capability = 'chat') {
    const model = this.selectBestModel(capability);
    
    if (!model) {
      throw new Error('没有可用的AI模型');
    }

    try {
      const startTime = Date.now();
      
      // 这里应该实际调用模型API
      const response = await this.simulateModelRequest(model, request);
      
      const responseTime = Date.now() - startTime;
      
      // 更新统计信息
      this.updateModelStats(model.id, true, responseTime);
      
      return {
        model: model.id,
        response,
        responseTime
      };
      
    } catch (error) {
      this.updateModelStats(model.id, false, 0);
      
      // 故障转移
      if (this.config.failover) {
        const fallbackModel = this.selectBestModel(capability, [model.id]);
        if (fallbackModel) {
          console.log(`🔄 故障转移: ${model.id} -> ${fallbackModel.id}`);
          return this.processRequest(request, capability);
        }
      }
      
      throw error;
    }
  }

  async simulateModelRequest(model, request) {
    // 模拟API调用 - 在实际环境中这里会调用真实的API
    await new Promise(resolve => setTimeout(resolve, 100 + Math.random() * 200));
    
    return {
      content: `这是来自${model.name}的模拟响应: ${request.slice(0, 50)}...`,
      model: model.id,
      usage: {
        promptTokens: request.length,
        completionTokens: 100,
        totalTokens: request.length + 100
      }
    };
  }

  updateModelStats(modelId, success, responseTime) {
    const stats = this.modelStats.get(modelId);
    if (!stats) return;

    stats.totalRequests++;
    stats.lastUsed = new Date().toISOString();
    
    if (success) {
      stats.successfulRequests++;
      stats.averageResponseTime = (stats.averageResponseTime + responseTime) / 2;
    } else {
      stats.failedRequests++;
    }
  }

  getModelStatus() {
    const status = {
      totalModels: this.models.size,
      activeModels: this.activeModels.size,
      models: {},
      statistics: {}
    };

    for (const [modelId, model] of this.models) {
      status.models[modelId] = {
        name: model.name,
        status: model.status,
        type: model.type,
        capabilities: model.capabilities,
        responseTime: model.responseTime,
        errorCount: model.errorCount,
        lastCheck: model.lastCheck
      };
    }

    for (const [modelId, stats] of this.modelStats) {
      status.statistics[modelId] = { ...stats };
    }

    return status;
  }

  async shutdown() {
    console.log('🛑 关闭AI模型管理器...');
    
    if (this.healthCheckTimer) {
      clearInterval(this.healthCheckTimer);
    }
    
    await this.saveConfigToFile();
    
    console.log('✅ AI模型管理器已关闭');
    this.emit('shutdown');
  }
}

module.exports = AIModelManager;
