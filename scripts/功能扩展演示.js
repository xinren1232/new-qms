#!/usr/bin/env node

/**
 * QMS-AI 配置驱动的功能扩展演示
 * 
 * 演示内容:
 * 1. AI模型动态管理
 * 2. 配置驱动的联动开发
 * 3. 实时功能切换
 * 4. 性能监控集成
 */

import AIModelManager from '../backend/nodejs/ai-model-manager.js';
import http from 'http';

class FeatureExpansionDemo {
  constructor() {
    this.aiManager = new AIModelManager();
    this.features = new Map();
    this.configCache = new Map();
  }

  log(message, type = 'info') {
    const timestamp = new Date().toLocaleTimeString();
    const symbols = {
      success: '✅',
      error: '❌',
      warning: '⚠️',
      info: 'ℹ️',
      demo: '🎯',
      ai: '🤖'
    };
    
    console.log(`${symbols[type] || 'ℹ️'} [${timestamp}] ${message}`);
  }

  async initialize() {
    this.log('初始化功能扩展演示...', 'demo');
    
    // 初始化AI模型管理器
    await this.aiManager.initialize();
    
    // 注册功能模块
    this.registerFeatures();
    
    // 设置事件监听
    this.setupEventListeners();
    
    this.log('功能扩展演示初始化完成', 'success');
  }

  registerFeatures() {
    // 注册智能质检功能
    this.features.set('quality-inspection', {
      name: '智能质检',
      enabled: true,
      aiCapability: 'analysis',
      config: {
        threshold: 0.8,
        autoFix: true,
        reportLevel: 'detailed'
      }
    });

    // 注册代码生成功能
    this.features.set('code-generation', {
      name: '代码生成',
      enabled: true,
      aiCapability: 'coding',
      config: {
        language: 'javascript',
        style: 'modern',
        includeTests: true
      }
    });

    // 注册文档分析功能
    this.features.set('document-analysis', {
      name: '文档分析',
      enabled: true,
      aiCapability: 'long-context',
      config: {
        maxLength: 32768,
        extractSummary: true,
        generateTags: true
      }
    });

    // 注册对话助手功能
    this.features.set('chat-assistant', {
      name: '对话助手',
      enabled: true,
      aiCapability: 'chat',
      config: {
        personality: 'professional',
        contextWindow: 4096,
        temperature: 0.7
      }
    });

    this.log(`注册了${this.features.size}个功能模块`, 'info');
  }

  setupEventListeners() {
    // 监听AI模型状态变化
    this.aiManager.on('healthCheckComplete', (status) => {
      this.log(`AI模型健康检查: ${status.healthy}/${status.total} 可用`, 'ai');
      this.adaptFeaturesToModelStatus(status);
    });

    // 监听配置变化
    this.aiManager.on('configUpdated', (config) => {
      this.log('AI模型配置已更新', 'info');
      this.reloadFeatureConfigs();
    });
  }

  adaptFeaturesToModelStatus(status) {
    // 根据AI模型状态自动调整功能
    for (const [featureId, feature] of this.features) {
      const availableModels = status.activeModels.filter(modelId => {
        const model = this.aiManager.models.get(modelId);
        return model && model.capabilities.includes(feature.aiCapability);
      });

      if (availableModels.length === 0) {
        feature.enabled = false;
        this.log(`功能 ${feature.name} 已禁用 - 无可用AI模型`, 'warning');
      } else {
        feature.enabled = true;
        this.log(`功能 ${feature.name} 已启用 - 使用模型: ${availableModels[0]}`, 'success');
      }
    }
  }

  async demonstrateQualityInspection() {
    this.log('演示智能质检功能...', 'demo');
    
    const feature = this.features.get('quality-inspection');
    if (!feature.enabled) {
      this.log('智能质检功能不可用', 'error');
      return;
    }

    const testCode = `
function calculateTotal(items) {
  let total = 0;
  for(let i = 0; i < items.length; i++) {
    total += items[i].price * items[i].quantity;
  }
  return total;
}`;

    try {
      const result = await this.aiManager.processRequest(
        `请检查以下代码的质量问题并提供改进建议:\n${testCode}`,
        'analysis'
      );

      this.log(`质检结果: ${result.response.content.slice(0, 100)}...`, 'success');
      this.log(`使用模型: ${result.model}, 响应时间: ${result.responseTime}ms`, 'info');
    } catch (error) {
      this.log(`质检失败: ${error.message}`, 'error');
    }
  }

  async demonstrateCodeGeneration() {
    this.log('演示代码生成功能...', 'demo');
    
    const feature = this.features.get('code-generation');
    if (!feature.enabled) {
      this.log('代码生成功能不可用', 'error');
      return;
    }

    const prompt = '生成一个Vue3组件，用于显示用户列表，包含搜索和分页功能';

    try {
      const result = await this.aiManager.processRequest(prompt, 'coding');
      
      this.log(`代码生成完成: ${result.response.content.slice(0, 100)}...`, 'success');
      this.log(`使用模型: ${result.model}, 响应时间: ${result.responseTime}ms`, 'info');
    } catch (error) {
      this.log(`代码生成失败: ${error.message}`, 'error');
    }
  }

  async demonstrateDocumentAnalysis() {
    this.log('演示文档分析功能...', 'demo');
    
    const feature = this.features.get('document-analysis');
    if (!feature.enabled) {
      this.log('文档分析功能不可用', 'error');
      return;
    }

    const document = `
QMS-AI系统是一个基于配置驱动的质量管理系统，集成了8个AI模型，
支持智能质检、代码生成、文档分析等功能。系统采用Node.js微服务架构，
前端使用Vue2和Vue3技术栈，具有高性能、高可用性的特点。
系统支持动态配置管理，可以根据业务需求灵活调整功能模块。
`;

    try {
      const result = await this.aiManager.processRequest(
        `请分析以下文档并提取关键信息:\n${document}`,
        'long-context'
      );
      
      this.log(`文档分析完成: ${result.response.content.slice(0, 100)}...`, 'success');
      this.log(`使用模型: ${result.model}, 响应时间: ${result.responseTime}ms`, 'info');
    } catch (error) {
      this.log(`文档分析失败: ${error.message}`, 'error');
    }
  }

  async demonstrateChatAssistant() {
    this.log('演示对话助手功能...', 'demo');
    
    const feature = this.features.get('chat-assistant');
    if (!feature.enabled) {
      this.log('对话助手功能不可用', 'error');
      return;
    }

    const questions = [
      '请介绍QMS-AI系统的主要功能',
      '如何优化系统性能？',
      '配置驱动开发有什么优势？'
    ];

    for (const question of questions) {
      try {
        const result = await this.aiManager.processRequest(question, 'chat');
        
        this.log(`问题: ${question}`, 'info');
        this.log(`回答: ${result.response.content.slice(0, 150)}...`, 'success');
        this.log(`模型: ${result.model}, 时间: ${result.responseTime}ms`, 'info');
        
        // 模拟间隔
        await new Promise(resolve => setTimeout(resolve, 1000));
      } catch (error) {
        this.log(`对话失败: ${error.message}`, 'error');
      }
    }
  }

  async demonstrateConfigDrivenDevelopment() {
    this.log('演示配置驱动开发...', 'demo');
    
    // 模拟配置变更
    this.log('模拟配置变更: 启用高级功能', 'info');
    
    // 动态添加新功能
    this.features.set('advanced-analytics', {
      name: '高级分析',
      enabled: true,
      aiCapability: 'reasoning',
      config: {
        depth: 'deep',
        includeVisualization: true,
        exportFormat: 'json'
      }
    });

    this.log('新功能已动态添加: 高级分析', 'success');
    
    // 模拟功能切换
    const qualityFeature = this.features.get('quality-inspection');
    qualityFeature.config.threshold = 0.9;
    qualityFeature.config.autoFix = false;
    
    this.log('质检功能配置已更新: 提高阈值, 禁用自动修复', 'info');
    
    // 展示当前功能状态
    this.showFeatureStatus();
  }

  showFeatureStatus() {
    console.log('\n📋 当前功能状态:');
    for (const [featureId, feature] of this.features) {
      const status = feature.enabled ? '✅' : '❌';
      console.log(`  ${status} ${feature.name} (${featureId})`);
      console.log(`     能力要求: ${feature.aiCapability}`);
      console.log(`     配置: ${JSON.stringify(feature.config)}`);
    }
  }

  async runFullDemo() {
    console.log('\n🎯 开始QMS-AI功能扩展完整演示\n');
    
    await this.initialize();
    
    // 等待AI模型初始化
    await new Promise(resolve => setTimeout(resolve, 2000));
    
    // 演示各个功能
    await this.demonstrateQualityInspection();
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    await this.demonstrateCodeGeneration();
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    await this.demonstrateDocumentAnalysis();
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    await this.demonstrateChatAssistant();
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    await this.demonstrateConfigDrivenDevelopment();
    
    // 显示AI模型状态
    const modelStatus = this.aiManager.getModelStatus();
    console.log('\n🤖 AI模型状态汇总:');
    console.log(`总模型数: ${modelStatus.totalModels}`);
    console.log(`可用模型: ${modelStatus.activeModels}`);
    
    console.log('\n🎉 功能扩展演示完成！');
    console.log('\n✨ QMS-AI系统特点:');
    console.log('  🔧 配置驱动 - 动态功能管理');
    console.log('  🤖 AI集成 - 8个模型协同工作');
    console.log('  ⚡ 高性能 - 负载均衡和故障转移');
    console.log('  🔄 实时监控 - 自动健康检查');
    console.log('  🎯 联动开发 - 配置端→应用端');
    
    await this.aiManager.shutdown();
  }
}

// 运行演示
async function main() {
  const demo = new FeatureExpansionDemo();
  try {
    await demo.runFullDemo();
  } catch (error) {
    console.error(`💥 演示执行出错: ${error.message}`);
    process.exit(1);
  }
}

main();
