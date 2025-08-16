#!/usr/bin/env node

/**
 * QMS-AI 简化功能扩展演示
 * 展示配置驱动的联动开发能力
 */

import http from 'http';
import { URL } from 'url';

// AI模型配置
const AI_MODELS = {
  'deepseek-chat': { name: 'DeepSeek Chat', type: 'external', capability: 'chat' },
  'deepseek-coder': { name: 'DeepSeek Coder', type: 'external', capability: 'coding' },
  'transsion-qwen-plus': { name: 'Qwen Plus', type: 'internal', capability: 'analysis' },
  'transsion-qwen-turbo': { name: 'Qwen Turbo', type: 'internal', capability: 'chat' },
  'transsion-qwen-max': { name: 'Qwen Max', type: 'internal', capability: 'reasoning' },
  'transsion-qwen-long': { name: 'Qwen Long', type: 'internal', capability: 'long-context' },
  'transsion-glm-4-plus': { name: 'GLM-4 Plus', type: 'internal', capability: 'multimodal' },
  'transsion-glm-4-0520': { name: 'GLM-4 0520', type: 'internal', capability: 'latest' }
};

class FeatureDemo {
  constructor() {
    this.features = new Map();
    this.activeModels = new Set(Object.keys(AI_MODELS));
  }

  log(message, type = 'info') {
    const timestamp = new Date().toLocaleTimeString();
    const symbols = {
      success: '✅',
      error: '❌',
      warning: '⚠️',
      info: 'ℹ️',
      demo: '🎯',
      ai: '🤖',
      config: '⚙️'
    };
    
    console.log(`${symbols[type] || 'ℹ️'} [${timestamp}] ${message}`);
  }

  async httpRequest(url, options = {}) {
    return new Promise((resolve, reject) => {
      const req = http.request(url, {
        method: 'GET',
        timeout: 3000,
        ...options
      }, (res) => {
        let data = '';
        res.on('data', chunk => data += chunk);
        res.on('end', () => {
          resolve({
            status: res.statusCode,
            data: data
          });
        });
      });

      req.on('error', reject);
      req.on('timeout', () => {
        req.destroy();
        reject(new Error('请求超时'));
      });
      
      req.end();
    });
  }

  initializeFeatures() {
    // 智能质检功能
    this.features.set('quality-inspection', {
      name: '智能质检',
      enabled: true,
      requiredCapability: 'analysis',
      config: {
        threshold: 0.8,
        autoFix: true,
        reportLevel: 'detailed'
      }
    });

    // 代码生成功能
    this.features.set('code-generation', {
      name: '代码生成',
      enabled: true,
      requiredCapability: 'coding',
      config: {
        language: 'javascript',
        style: 'modern',
        includeTests: true
      }
    });

    // 文档分析功能
    this.features.set('document-analysis', {
      name: '文档分析',
      enabled: true,
      requiredCapability: 'long-context',
      config: {
        maxLength: 32768,
        extractSummary: true,
        generateTags: true
      }
    });

    // 对话助手功能
    this.features.set('chat-assistant', {
      name: '对话助手',
      enabled: true,
      requiredCapability: 'chat',
      config: {
        personality: 'professional',
        contextWindow: 4096,
        temperature: 0.7
      }
    });

    this.log(`初始化了${this.features.size}个功能模块`, 'config');
  }

  selectModelForCapability(capability) {
    const availableModels = Array.from(this.activeModels)
      .filter(modelId => AI_MODELS[modelId].capability === capability);
    
    return availableModels[0] || null;
  }

  async simulateAIRequest(modelId, prompt, capability) {
    // 模拟AI请求处理
    const model = AI_MODELS[modelId];
    const startTime = Date.now();
    
    // 模拟处理时间
    await new Promise(resolve => setTimeout(resolve, 100 + Math.random() * 300));
    
    const responseTime = Date.now() - startTime;
    
    return {
      model: modelId,
      modelName: model.name,
      response: `这是来自${model.name}的模拟响应: ${prompt.slice(0, 50)}...`,
      responseTime,
      capability
    };
  }

  async demonstrateQualityInspection() {
    this.log('演示智能质检功能...', 'demo');
    
    const feature = this.features.get('quality-inspection');
    const modelId = this.selectModelForCapability('analysis');
    
    if (!modelId) {
      this.log('没有可用的分析模型', 'error');
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

    const result = await this.simulateAIRequest(
      modelId,
      `请检查以下代码的质量问题:\n${testCode}`,
      'analysis'
    );

    this.log(`质检完成 - 模型: ${result.modelName}, 时间: ${result.responseTime}ms`, 'success');
    this.log(`建议: 代码结构良好，建议添加参数验证和错误处理`, 'info');
  }

  async demonstrateCodeGeneration() {
    this.log('演示代码生成功能...', 'demo');
    
    const modelId = this.selectModelForCapability('coding');
    
    if (!modelId) {
      this.log('没有可用的编码模型', 'error');
      return;
    }

    const prompt = '生成一个Vue3组件，用于显示用户列表，包含搜索和分页功能';
    
    const result = await this.simulateAIRequest(modelId, prompt, 'coding');
    
    this.log(`代码生成完成 - 模型: ${result.modelName}, 时间: ${result.responseTime}ms`, 'success');
    this.log(`生成了包含搜索和分页的Vue3用户列表组件`, 'info');
  }

  async demonstrateDocumentAnalysis() {
    this.log('演示文档分析功能...', 'demo');
    
    const modelId = this.selectModelForCapability('long-context');
    
    if (!modelId) {
      this.log('没有可用的长文本模型', 'error');
      return;
    }

    const document = `QMS-AI系统是一个基于配置驱动的质量管理系统，集成了8个AI模型...`;
    
    const result = await this.simulateAIRequest(
      modelId,
      `请分析以下文档:\n${document}`,
      'long-context'
    );
    
    this.log(`文档分析完成 - 模型: ${result.modelName}, 时间: ${result.responseTime}ms`, 'success');
    this.log(`提取关键信息: 配置驱动、8个AI模型、微服务架构`, 'info');
  }

  async demonstrateChatAssistant() {
    this.log('演示对话助手功能...', 'demo');
    
    const modelId = this.selectModelForCapability('chat');
    
    if (!modelId) {
      this.log('没有可用的对话模型', 'error');
      return;
    }

    const questions = [
      '请介绍QMS-AI系统的主要功能',
      '如何优化系统性能？'
    ];

    for (const question of questions) {
      const result = await this.simulateAIRequest(modelId, question, 'chat');
      
      this.log(`问题: ${question}`, 'info');
      this.log(`回答完成 - 模型: ${result.modelName}, 时间: ${result.responseTime}ms`, 'success');
      
      await new Promise(resolve => setTimeout(resolve, 500));
    }
  }

  async checkServiceIntegration() {
    this.log('检查服务集成状态...', 'demo');
    
    const services = [
      { name: '配置端', url: 'http://localhost:8072' },
      { name: '应用端', url: 'http://localhost:8081' },
      { name: '认证服务', url: 'http://localhost:8084/health' },
      { name: 'Coze Studio', url: 'http://localhost:3005/health' }
    ];

    for (const service of services) {
      try {
        const response = await this.httpRequest(service.url);
        this.log(`${service.name}: 运行正常 (${response.status})`, 'success');
      } catch (error) {
        this.log(`${service.name}: 连接失败`, 'warning');
      }
    }
  }

  demonstrateConfigDrivenDevelopment() {
    this.log('演示配置驱动开发...', 'demo');
    
    // 模拟配置变更
    this.log('配置变更: 启用高级功能模式', 'config');
    
    // 动态调整功能配置
    const qualityFeature = this.features.get('quality-inspection');
    qualityFeature.config.threshold = 0.9;
    qualityFeature.config.autoFix = false;
    
    this.log('质检功能配置已更新: 提高阈值到0.9, 禁用自动修复', 'config');
    
    // 添加新功能
    this.features.set('advanced-analytics', {
      name: '高级分析',
      enabled: true,
      requiredCapability: 'reasoning',
      config: {
        depth: 'deep',
        includeVisualization: true
      }
    });
    
    this.log('新功能已动态添加: 高级分析', 'config');
    
    // 展示配置驱动的优势
    this.log('配置驱动优势:', 'info');
    console.log('  🔄 实时配置更新 - 无需重启服务');
    console.log('  🎯 功能模块化 - 独立开发和部署');
    console.log('  ⚙️ 动态功能切换 - 根据需求调整');
    console.log('  🔧 配置端→应用端联动 - 统一管理');
  }

  showSystemStatus() {
    console.log('\n📊 QMS-AI系统状态汇总:');
    
    // AI模型状态
    console.log('\n🤖 AI模型 (8个):');
    for (const [modelId, model] of Object.entries(AI_MODELS)) {
      const status = this.activeModels.has(modelId) ? '✅' : '❌';
      console.log(`  ${status} ${model.name} (${model.type}) - ${model.capability}`);
    }
    
    // 功能模块状态
    console.log('\n🎯 功能模块:');
    for (const [featureId, feature] of this.features) {
      const status = feature.enabled ? '✅' : '❌';
      console.log(`  ${status} ${feature.name} - 需要: ${feature.requiredCapability}`);
    }
    
    // 系统特点
    console.log('\n✨ 系统特点:');
    console.log('  🔧 配置驱动架构 - 动态功能管理');
    console.log('  🤖 多AI模型集成 - 6个内部 + 2个外部');
    console.log('  ⚡ 微服务架构 - Node.js + Vue');
    console.log('  🔄 实时监控 - 健康检查和性能优化');
    console.log('  🎯 联动开发 - 配置端→应用端');
  }

  async runDemo() {
    console.log('\n🎯 开始QMS-AI功能扩展演示\n');
    
    // 初始化
    this.initializeFeatures();
    
    // 检查服务集成
    await this.checkServiceIntegration();
    
    console.log('\n--- 功能演示 ---');
    
    // 演示各个功能
    await this.demonstrateQualityInspection();
    await this.demonstrateCodeGeneration();
    await this.demonstrateDocumentAnalysis();
    await this.demonstrateChatAssistant();
    
    console.log('\n--- 配置驱动演示 ---');
    
    // 演示配置驱动开发
    this.demonstrateConfigDrivenDevelopment();
    
    // 显示系统状态
    this.showSystemStatus();
    
    console.log('\n🎉 功能扩展演示完成！');
    console.log('\n🚀 QMS-AI系统已准备好进行生产部署和进一步功能扩展！');
  }
}

// 运行演示
async function main() {
  const demo = new FeatureDemo();
  try {
    await demo.runDemo();
  } catch (error) {
    console.error(`💥 演示执行出错: ${error.message}`);
    process.exit(1);
  }
}

main();
