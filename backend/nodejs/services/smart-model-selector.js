/**
 * 智能模型选择器
 * 根据环境可用性和任务类型智能选择最适合的AI模型
 */

class SmartModelSelector {
  constructor() {
    // 模型配置：区分内部和外部模型
    this.modelConfig = {
      // 外部模型 - 随时可用
      external: {
        'deepseek-chat': {
          name: 'DeepSeek Chat',
          provider: 'deepseek',
          type: 'external',
          capabilities: ['chat', 'reasoning', 'analysis'],
          maxTokens: 4096,
          cost: 0.01, // 每1K tokens成本
          priority: 8,
          availability: 'always'
        },
        'deepseek-coder': {
          name: 'DeepSeek Coder',
          provider: 'deepseek',
          type: 'external',
          capabilities: ['coding', 'analysis', 'debugging'],
          maxTokens: 8192,
          cost: 0.01,
          priority: 7,
          availability: 'always'
        }
      },
      
      // 内部模型 - 仅公司环境可用
      internal: {
        'gpt-4o': {
          name: 'GPT-4o',
          provider: 'openai',
          type: 'internal',
          capabilities: ['chat', 'reasoning', 'analysis', 'vision', 'tools'],
          maxTokens: 4096,
          cost: 0.03,
          priority: 9,
          availability: 'office_only'
        },
        'o3': {
          name: 'O3',
          provider: 'openai',
          type: 'internal',
          capabilities: ['reasoning', 'complex_analysis', 'tools'],
          maxTokens: 4096,
          cost: 0.05,
          priority: 10,
          availability: 'office_only'
        },
        'claude-3.7-sonnet': {
          name: 'Claude 3.5 Sonnet',
          provider: 'anthropic',
          type: 'internal',
          capabilities: ['generation', 'analysis', 'writing', 'tools'],
          maxTokens: 4096,
          cost: 0.025,
          priority: 9,
          availability: 'office_only'
        },
        'deepseek-v3': {
          name: 'DeepSeek V3 (内网)',
          provider: 'deepseek',
          type: 'internal',
          capabilities: ['chat', 'reasoning'],
          maxTokens: 4096,
          cost: 0.008,
          priority: 6,
          availability: 'office_only'
        },
        'qwen-plus': {
          name: 'Qwen Plus',
          provider: 'qwen',
          type: 'internal',
          capabilities: ['chat', 'analysis', 'reasoning'],
          maxTokens: 8192,
          cost: 0.015,
          priority: 7,
          availability: 'office_only'
        },
        'glm-4-plus': {
          name: 'GLM-4 Plus',
          provider: 'zhipu',
          type: 'internal',
          capabilities: ['multimodal', 'analysis'],
          maxTokens: 4096,
          cost: 0.02,
          priority: 6,
          availability: 'office_only'
        }
      }
    };
    
    // 任务类型到模型能力的映射
    this.taskCapabilityMapping = {
      'research': ['chat', 'reasoning', 'analysis'],
      'analysis': ['reasoning', 'complex_analysis', 'analysis'],
      'generation': ['generation', 'writing', 'chat'],
      'validation': ['reasoning', 'analysis'],
      'coding': ['coding', 'analysis', 'debugging'],
      'translation': ['chat', 'analysis'],
      'summarization': ['generation', 'writing', 'analysis']
    };
    
    // 环境检测缓存
    this.environmentCache = {
      isOfficeEnvironment: null,
      lastCheck: null,
      checkInterval: 5 * 60 * 1000 // 5分钟检查一次
    };
  }

  /**
   * 检测当前环境是否为公司内网环境
   */
  async detectEnvironment() {
    const now = Date.now();
    
    // 如果缓存有效，直接返回
    if (this.environmentCache.lastCheck && 
        (now - this.environmentCache.lastCheck) < this.environmentCache.checkInterval) {
      return this.environmentCache.isOfficeEnvironment;
    }
    
    try {
      // 尝试访问传音代理服务来判断是否在公司环境
      const axios = require('axios');
      const response = await axios.get('https://hk-intra-paas.transsion.com/health', {
        timeout: 3000,
        validateStatus: () => true // 接受所有状态码
      });
      
      // 如果能访问到传音代理，说明在公司环境
      const isOffice = response.status < 500;
      
      // 更新缓存
      this.environmentCache.isOfficeEnvironment = isOffice;
      this.environmentCache.lastCheck = now;
      
      console.log(`🌐 环境检测: ${isOffice ? '公司内网' : '外部网络'}`);
      return isOffice;
      
    } catch (error) {
      // 无法访问传音代理，说明在外部环境
      this.environmentCache.isOfficeEnvironment = false;
      this.environmentCache.lastCheck = now;
      
      console.log(`🌐 环境检测: 外部网络 (${error.message})`);
      return false;
    }
  }

  /**
   * 获取当前环境下可用的模型列表
   */
  async getAvailableModels() {
    const isOfficeEnvironment = await this.detectEnvironment();
    const availableModels = [];
    
    // 外部模型始终可用
    Object.entries(this.modelConfig.external).forEach(([id, config]) => {
      availableModels.push({ id, ...config });
    });
    
    // 内部模型仅在公司环境可用
    if (isOfficeEnvironment) {
      Object.entries(this.modelConfig.internal).forEach(([id, config]) => {
        availableModels.push({ id, ...config });
      });
    }
    
    // 按优先级排序
    availableModels.sort((a, b) => b.priority - a.priority);
    
    console.log(`📋 可用模型: ${availableModels.map(m => m.name).join(', ')}`);
    return availableModels;
  }

  /**
   * 为任务选择最优模型
   */
  async selectOptimalModel(taskType, requirements = {}) {
    const availableModels = await this.getAvailableModels();
    const requiredCapabilities = this.taskCapabilityMapping[taskType] || ['chat'];
    
    // 筛选具备所需能力的模型
    const capableModels = availableModels.filter(model => {
      return requiredCapabilities.some(capability => 
        model.capabilities.includes(capability)
      );
    });
    
    if (capableModels.length === 0) {
      console.warn(`⚠️ 没有找到支持 ${taskType} 任务的模型，使用默认模型`);
      return availableModels[0]; // 返回优先级最高的可用模型
    }
    
    // 根据需求进一步筛选
    let selectedModel = capableModels[0]; // 默认选择优先级最高的
    
    // 如果需要长上下文
    if (requirements.longContext && requirements.maxTokens > 4096) {
      const longContextModels = capableModels.filter(m => m.maxTokens >= requirements.maxTokens);
      if (longContextModels.length > 0) {
        selectedModel = longContextModels[0];
      }
    }
    
    // 如果需要低成本
    if (requirements.lowCost) {
      const sortedByCost = [...capableModels].sort((a, b) => a.cost - b.cost);
      selectedModel = sortedByCost[0];
    }
    
    // 如果需要高性能
    if (requirements.highPerformance) {
      const highPerfModels = capableModels.filter(m => m.priority >= 8);
      if (highPerfModels.length > 0) {
        selectedModel = highPerfModels[0];
      }
    }
    
    console.log(`🎯 为 ${taskType} 任务选择模型: ${selectedModel.name} (${selectedModel.type})`);
    return selectedModel;
  }

  /**
   * 为AutoGPT任务分配模型
   */
  async allocateModelsForTasks(tasks) {
    const allocation = {};
    const isOfficeEnvironment = await this.detectEnvironment();
    
    for (const task of tasks) {
      const requirements = {
        longContext: task.estimated_time > 15, // 长时间任务可能需要更多上下文
        lowCost: task.priority <= 2, // 低优先级任务使用低成本模型
        highPerformance: task.priority >= 4 // 高优先级任务使用高性能模型
      };
      
      const selectedModel = await this.selectOptimalModel(task.type, requirements);
      allocation[task.id] = selectedModel.id;
    }
    
    // 生成分配报告
    const report = {
      environment: isOfficeEnvironment ? 'office' : 'external',
      allocation: allocation,
      summary: this.generateAllocationSummary(allocation, tasks)
    };
    
    console.log(`📊 模型分配完成:`, report.summary);
    return report;
  }

  /**
   * 生成分配摘要
   */
  generateAllocationSummary(allocation, tasks) {
    const modelUsage = {};
    const typeDistribution = { internal: 0, external: 0 };
    
    Object.values(allocation).forEach(modelId => {
      modelUsage[modelId] = (modelUsage[modelId] || 0) + 1;
      
      // 统计内外部模型使用情况
      const isInternal = this.modelConfig.internal[modelId];
      if (isInternal) {
        typeDistribution.internal++;
      } else {
        typeDistribution.external++;
      }
    });
    
    return {
      total_tasks: tasks.length,
      model_usage: modelUsage,
      type_distribution: typeDistribution,
      most_used_model: Object.keys(modelUsage).reduce((a, b) => 
        modelUsage[a] > modelUsage[b] ? a : b
      )
    };
  }

  /**
   * 获取模型回退策略
   */
  async getModelFallbackStrategy(primaryModelId) {
    const availableModels = await this.getAvailableModels();
    const primaryModel = availableModels.find(m => m.id === primaryModelId);
    
    if (!primaryModel) {
      return availableModels[0]; // 返回优先级最高的可用模型
    }
    
    // 寻找具有相似能力的备用模型
    const fallbackModels = availableModels.filter(model => {
      if (model.id === primaryModelId) return false;
      
      // 检查是否有重叠的能力
      const hasOverlap = model.capabilities.some(cap => 
        primaryModel.capabilities.includes(cap)
      );
      
      return hasOverlap;
    });
    
    // 按优先级排序
    fallbackModels.sort((a, b) => b.priority - a.priority);
    
    return fallbackModels[0] || availableModels[0];
  }

  /**
   * 检查模型是否可用
   */
  async isModelAvailable(modelId) {
    const availableModels = await this.getAvailableModels();
    return availableModels.some(model => model.id === modelId);
  }

  /**
   * 获取环境状态报告
   */
  async getEnvironmentReport() {
    const isOfficeEnvironment = await this.detectEnvironment();
    const availableModels = await this.getAvailableModels();
    
    return {
      environment: isOfficeEnvironment ? 'office' : 'external',
      available_models: availableModels.length,
      internal_models_available: isOfficeEnvironment,
      external_models_available: true,
      models: availableModels.map(m => ({
        id: m.id,
        name: m.name,
        type: m.type,
        capabilities: m.capabilities
      })),
      recommendations: this.generateEnvironmentRecommendations(isOfficeEnvironment)
    };
  }

  /**
   * 生成环境建议
   */
  generateEnvironmentRecommendations(isOfficeEnvironment) {
    if (isOfficeEnvironment) {
      return [
        '✅ 公司环境：可使用所有8个AI模型',
        '🚀 建议优先使用高性能内部模型（GPT-4o, O3, Claude）',
        '💰 内部模型成本更低，适合大量任务'
      ];
    } else {
      return [
        '⚠️ 外部环境：仅可使用2个外部模型',
        '🔄 建议使用DeepSeek Chat进行通用任务',
        '💻 建议使用DeepSeek Coder进行编程任务',
        '🏢 如需更多模型，请在公司环境中使用'
      ];
    }
  }
}

module.exports = SmartModelSelector;
