/**
 * 智能模型管理器
 * 支持智能模型选择、无缝切换、负载均衡、性能监控
 */

const EventEmitter = require('events');
const logger = require('../utils/logger');

class IntelligentModelManager extends EventEmitter {
  constructor(options = {}) {
    super();
    
    this.options = {
      warmupEnabled: options.warmupEnabled !== false,
      performanceTracking: options.performanceTracking !== false,
      autoOptimization: options.autoOptimization !== false,
      ...options
    };
    
    this.models = new Map();
    this.modelPool = new Map();
    this.performanceMetrics = new Map();
    this.loadBalancer = new LoadBalancer();
    this.sessionStates = new Map();
    this.initialized = false;

    // 不在构造函数中调用异步方法
  }

  /**
   * 初始化模型配置
   */
  async initializeModels() {
    try {
      // 从配置中心获取模型配置
      const ConfigClient = require('../config/config-client');
      const configClient = new ConfigClient();
      const modelConfigs = await configClient.getConfig('ai_models');
      
      if (modelConfigs && modelConfigs.models) {
        Object.entries(modelConfigs.models).forEach(([modelId, config]) => {
          this.registerModel(modelId, config);
        });
      }
      
      // 预热高优先级模型
      if (this.options.warmupEnabled) {
        await this.warmupPriorityModels();
      }
      
      logger.info(`🤖 智能模型管理器初始化完成，注册了 ${this.models.size} 个模型`);
    } catch (error) {
      logger.error('智能模型管理器初始化失败:', error);
    }
  }

  /**
   * 注册模型
   */
  registerModel(modelId, config) {
    const modelInfo = {
      id: modelId,
      name: config.name,
      model: config.model,
      provider: this.getProvider(config.baseURL),
      config,
      capabilities: this.analyzeCapabilities(config),
      performance: {
        avgResponseTime: 0,
        successRate: 100,
        totalRequests: 0,
        failedRequests: 0,
        lastUsed: null
      },
      status: config.enabled ? 'available' : 'disabled',
      priority: this.calculatePriority(config),
      warmupStatus: 'cold'
    };
    
    this.models.set(modelId, modelInfo);
    this.performanceMetrics.set(modelId, []);
    
    logger.info(`📝 注册模型: ${modelInfo.name} (${modelId})`);
  }

  /**
   * 智能模型选择
   */
  async selectOptimalModel(task, context = {}) {
    try {
      const candidates = this.getAvailableModels(task.type);
      
      if (candidates.length === 0) {
        throw new Error('没有可用的模型');
      }
      
      if (candidates.length === 1) {
        return candidates[0];
      }
      
      // 计算每个模型的适配分数
      const scores = await Promise.all(
        candidates.map(model => this.scoreModel(model, task, context))
      );
      
      // 选择得分最高的模型
      const bestIndex = scores.indexOf(Math.max(...scores));
      const selectedModel = candidates[bestIndex];
      
      logger.info(`🎯 智能选择模型: ${selectedModel.name} (得分: ${scores[bestIndex].toFixed(2)})`);
      
      return selectedModel;
    } catch (error) {
      logger.error('智能模型选择失败:', error);
      // 返回默认模型
      return this.getDefaultModel();
    }
  }

  /**
   * 模型评分算法
   */
  async scoreModel(model, task, context) {
    let score = 0;
    
    // 基础可用性分数 (40%)
    if (model.status === 'available') {
      score += 40;
    } else if (model.status === 'warming') {
      score += 20;
    }
    
    // 性能分数 (30%)
    const perfScore = this.calculatePerformanceScore(model);
    score += perfScore * 0.3;
    
    // 能力匹配分数 (20%)
    const capabilityScore = this.calculateCapabilityScore(model, task);
    score += capabilityScore * 0.2;
    
    // 负载均衡分数 (10%)
    const loadScore = this.calculateLoadScore(model);
    score += loadScore * 0.1;
    
    return score;
  }

  /**
   * 计算性能分数
   */
  calculatePerformanceScore(model) {
    const perf = model.performance;
    
    // 响应时间分数 (越快越好)
    const responseScore = Math.max(0, 100 - (perf.avgResponseTime / 100));
    
    // 成功率分数
    const successScore = perf.successRate;
    
    // 使用频率分数 (适度使用的模型得分更高)
    const usageScore = perf.totalRequests > 0 ? 
      Math.min(100, 50 + (perf.totalRequests / 100)) : 50;
    
    return (responseScore + successScore + usageScore) / 3;
  }

  /**
   * 计算能力匹配分数
   */
  calculateCapabilityScore(model, task) {
    const capabilities = model.capabilities;
    let score = 50; // 基础分数
    
    // 根据任务类型调整分数
    switch (task.type) {
      case 'reasoning':
        if (capabilities.reasoning) score += 30;
        if (capabilities.tools) score += 10;
        break;
      case 'vision':
        if (capabilities.vision) score += 40;
        else score -= 20;
        break;
      case 'code':
        if (capabilities.code) score += 30;
        if (capabilities.tools) score += 10;
        break;
      case 'chat':
        if (capabilities.chat) score += 20;
        break;
      default:
        // 通用任务，所有模型都适用
        break;
    }
    
    return Math.max(0, Math.min(100, score));
  }

  /**
   * 计算负载分数
   */
  calculateLoadScore(model) {
    const currentLoad = this.loadBalancer.getCurrentLoad(model.id);
    return Math.max(0, 100 - currentLoad);
  }

  /**
   * 无缝模型切换
   */
  async switchModel(fromModelId, toModelId, sessionId = null) {
    try {
      const fromModel = this.models.get(fromModelId);
      const toModel = this.models.get(toModelId);
      
      if (!fromModel || !toModel) {
        throw new Error('模型不存在');
      }
      
      logger.info(`🔄 模型切换: ${fromModel.name} → ${toModel.name}`);
      
      // 保存会话状态
      let sessionState = null;
      if (sessionId) {
        sessionState = await this.exportSessionState(fromModelId, sessionId);
      }
      
      // 预热目标模型
      await this.warmupModel(toModelId);
      
      // 导入会话状态
      if (sessionState && sessionId) {
        await this.importSessionState(toModelId, sessionId, sessionState);
      }
      
      this.emit('model-switched', {
        from: fromModelId,
        to: toModelId,
        sessionId,
        timestamp: new Date().toISOString()
      });
      
      return {
        success: true,
        fromModel: fromModel.name,
        toModel: toModel.name,
        switchTime: Date.now()
      };
    } catch (error) {
      logger.error('模型切换失败:', error);
      throw error;
    }
  }

  /**
   * 预热模型
   */
  async warmupModel(modelId) {
    const model = this.models.get(modelId);
    if (!model) {
      throw new Error(`模型不存在: ${modelId}`);
    }
    
    if (model.warmupStatus === 'warm') {
      return; // 已经预热
    }
    
    model.warmupStatus = 'warming';
    
    try {
      // 发送预热请求
      const warmupRequest = {
        model: model.model,
        messages: [{ role: 'user', content: 'Hello' }],
        max_tokens: 10
      };
      
      const startTime = Date.now();
      await this.callModel(modelId, warmupRequest);
      const warmupTime = Date.now() - startTime;
      
      model.warmupStatus = 'warm';
      model.lastWarmup = Date.now();
      
      logger.info(`🔥 模型预热完成: ${model.name} (${warmupTime}ms)`);
    } catch (error) {
      model.warmupStatus = 'failed';
      logger.error(`模型预热失败: ${model.name} - ${error.message}`);
    }
  }

  /**
   * 预热高优先级模型
   */
  async warmupPriorityModels() {
    const priorityModels = Array.from(this.models.values())
      .filter(model => model.status === 'available' && model.priority > 7)
      .sort((a, b) => b.priority - a.priority)
      .slice(0, 3); // 预热前3个高优先级模型
    
    const warmupPromises = priorityModels.map(model => 
      this.warmupModel(model.id).catch(error => 
        logger.warn(`预热模型失败: ${model.name} - ${error.message}`)
      )
    );
    
    await Promise.all(warmupPromises);
  }

  /**
   * 导出会话状态
   */
  async exportSessionState(modelId, sessionId) {
    const stateKey = `${modelId}:${sessionId}`;
    return this.sessionStates.get(stateKey) || null;
  }

  /**
   * 导入会话状态
   */
  async importSessionState(modelId, sessionId, state) {
    const stateKey = `${modelId}:${sessionId}`;
    this.sessionStates.set(stateKey, state);
  }

  /**
   * 记录模型性能
   */
  recordPerformance(modelId, responseTime, success = true) {
    const model = this.models.get(modelId);
    if (!model) return;
    
    const perf = model.performance;
    perf.totalRequests++;
    
    if (success) {
      // 更新平均响应时间
      perf.avgResponseTime = (perf.avgResponseTime * (perf.totalRequests - 1) + responseTime) / perf.totalRequests;
    } else {
      perf.failedRequests++;
    }
    
    // 更新成功率
    perf.successRate = ((perf.totalRequests - perf.failedRequests) / perf.totalRequests) * 100;
    perf.lastUsed = Date.now();
    
    // 记录详细指标
    const metrics = this.performanceMetrics.get(modelId);
    metrics.push({
      timestamp: Date.now(),
      responseTime,
      success
    });
    
    // 保持最近1000条记录
    if (metrics.length > 1000) {
      metrics.splice(0, metrics.length - 1000);
    }
  }

  /**
   * 获取可用模型
   */
  getAvailableModels(taskType = null) {
    return Array.from(this.models.values())
      .filter(model => model.status === 'available')
      .filter(model => !taskType || this.isModelSuitableForTask(model, taskType));
  }

  /**
   * 检查模型是否适合任务
   */
  isModelSuitableForTask(model, taskType) {
    const capabilities = model.capabilities;
    
    switch (taskType) {
      case 'vision':
        return capabilities.vision;
      case 'reasoning':
        return capabilities.reasoning;
      case 'code':
        return capabilities.code;
      default:
        return true; // 通用任务
    }
  }

  /**
   * 获取默认模型
   */
  getDefaultModel() {
    return Array.from(this.models.values())
      .filter(model => model.status === 'available')
      .sort((a, b) => b.priority - a.priority)[0];
  }

  /**
   * 分析模型能力
   */
  analyzeCapabilities(config) {
    const features = config.features || {};
    return {
      reasoning: features.reasoning || false,
      vision: features.vision || false,
      tools: features.tools || false,
      code: config.model.includes('code') || false,
      chat: true // 所有模型都支持对话
    };
  }

  /**
   * 计算模型优先级
   */
  calculatePriority(config) {
    let priority = 5; // 基础优先级
    
    // 根据模型名称调整优先级
    if (config.model.includes('gpt-4')) priority += 3;
    if (config.model.includes('claude')) priority += 2;
    if (config.model.includes('deepseek')) priority += 1;
    
    // 根据功能调整优先级
    if (config.features?.reasoning) priority += 1;
    if (config.features?.vision) priority += 1;
    if (config.features?.tools) priority += 1;
    
    return Math.min(10, priority);
  }

  /**
   * 获取提供商
   */
  getProvider(baseURL) {
    if (baseURL.includes('openai.com')) return 'OpenAI';
    if (baseURL.includes('deepseek.com')) return 'DeepSeek';
    if (baseURL.includes('anthropic.com')) return 'Anthropic';
    if (baseURL.includes('transsion.com')) return 'Transsion';
    return 'Unknown';
  }

  /**
   * 调用模型（占位符）
   */
  async callModel(modelId, request) {
    // 这里应该集成实际的模型调用逻辑
    return { content: 'Response', usage: { tokens: 10 } };
  }

  /**
   * 获取性能统计
   */
  getPerformanceStats() {
    const stats = {};
    
    this.models.forEach((model, modelId) => {
      stats[modelId] = {
        name: model.name,
        performance: model.performance,
        warmupStatus: model.warmupStatus,
        capabilities: model.capabilities
      };
    });
    
    return stats;
  }
}

/**
 * 负载均衡器
 */
class LoadBalancer {
  constructor() {
    this.loads = new Map();
  }

  getCurrentLoad(modelId) {
    return this.loads.get(modelId) || 0;
  }

  incrementLoad(modelId) {
    const current = this.loads.get(modelId) || 0;
    this.loads.set(modelId, current + 1);
  }

  decrementLoad(modelId) {
    const current = this.loads.get(modelId) || 0;
    this.loads.set(modelId, Math.max(0, current - 1));
  }
}

module.exports = IntelligentModelManager;
