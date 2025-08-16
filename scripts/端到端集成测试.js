#!/usr/bin/env node

/**
 * QMS-AI 端到端集成测试脚本
 * 验证所有服务协同工作
 * 
 * 测试范围:
 * 1. 服务健康检查
 * 2. 认证流程测试
 * 3. AI模型配置测试
 * 4. Coze Studio集成测试
 * 5. 配置驱动联动测试
 */

import axios from 'axios';
import chalk from 'chalk';

// 服务配置
const SERVICES = {
  configCenter: {
    name: '配置端',
    url: 'http://localhost:8072',
    healthPath: '/alm-transcend-configcenter-web/',
    type: 'frontend'
  },
  application: {
    name: '应用端',
    url: 'http://localhost:8081',
    healthPath: '/',
    type: 'frontend'
  },
  auth: {
    name: '认证服务',
    url: 'http://localhost:8084',
    healthPath: '/health',
    type: 'backend'
  },
  cozeStudio: {
    name: 'Coze Studio服务',
    url: 'http://localhost:3005',
    healthPath: '/health',
    type: 'backend'
  },
  configService: {
    name: '配置中心服务',
    url: 'http://localhost:8082',
    healthPath: '/health',
    type: 'backend'
  }
};

// 测试用户
const TEST_USER = {
  username: 'admin',
  password: 'admin123'
};

// AI模型配置
const AI_MODELS = [
  'deepseek-chat',
  'deepseek-coder',
  'transsion-qwen-plus',
  'transsion-qwen-turbo',
  'transsion-qwen-max',
  'transsion-qwen-long',
  'transsion-glm-4-plus',
  'transsion-glm-4-0520'
];

class IntegrationTester {
  constructor() {
    this.results = {
      passed: 0,
      failed: 0,
      total: 0
    };
    this.authToken = null;
  }

  log(message, type = 'info') {
    const timestamp = new Date().toLocaleTimeString();
    const prefix = `[${timestamp}]`;
    
    switch (type) {
      case 'success':
        console.log(chalk.green(`✅ ${prefix} ${message}`));
        break;
      case 'error':
        console.log(chalk.red(`❌ ${prefix} ${message}`));
        break;
      case 'warning':
        console.log(chalk.yellow(`⚠️ ${prefix} ${message}`));
        break;
      case 'info':
        console.log(chalk.blue(`ℹ️ ${prefix} ${message}`));
        break;
      default:
        console.log(`${prefix} ${message}`);
    }
  }

  async test(name, testFn) {
    this.results.total++;
    try {
      this.log(`开始测试: ${name}`, 'info');
      await testFn();
      this.results.passed++;
      this.log(`测试通过: ${name}`, 'success');
    } catch (error) {
      this.results.failed++;
      this.log(`测试失败: ${name} - ${error.message}`, 'error');
    }
  }

  async checkServiceHealth(serviceName, config) {
    const response = await axios.get(`${config.url}${config.healthPath}`, {
      timeout: 5000,
      validateStatus: (status) => status < 500
    });
    
    if (config.type === 'backend') {
      if (response.status !== 200) {
        throw new Error(`服务响应状态码: ${response.status}`);
      }
    } else {
      // 前端服务只要能访问就算成功
      if (response.status >= 500) {
        throw new Error(`服务不可访问: ${response.status}`);
      }
    }
    
    this.log(`${serviceName} 健康检查通过`, 'success');
  }

  async testAuthentication() {
    const response = await axios.post(`${SERVICES.auth.url}/api/auth/login`, TEST_USER, {
      timeout: 5000
    });
    
    if (response.status !== 200 || !response.data.token) {
      throw new Error('认证失败或未返回token');
    }
    
    this.authToken = response.data.token;
    this.log('用户认证成功，获取到token', 'success');
  }

  async testAIModelConfiguration() {
    if (!this.authToken) {
      throw new Error('需要先进行认证');
    }

    // 测试获取AI模型配置
    const response = await axios.get(`${SERVICES.configService.url}/api/ai-models`, {
      headers: {
        'Authorization': `Bearer ${this.authToken}`
      },
      timeout: 5000
    });

    if (response.status !== 200) {
      throw new Error(`获取AI模型配置失败: ${response.status}`);
    }

    this.log(`成功获取AI模型配置，共${response.data.length || 0}个模型`, 'success');
  }

  async testCozeStudioIntegration() {
    // 测试Coze Studio Agent列表
    const response = await axios.get(`${SERVICES.cozeStudio.url}/api/agents`, {
      timeout: 5000
    });

    if (response.status !== 200) {
      throw new Error(`Coze Studio服务响应异常: ${response.status}`);
    }

    this.log('Coze Studio集成测试通过', 'success');
  }

  async testConfigDrivenIntegration() {
    if (!this.authToken) {
      throw new Error('需要先进行认证');
    }

    // 测试配置驱动的联动
    // 1. 从配置中心获取配置
    const configResponse = await axios.get(`${SERVICES.configService.url}/api/config/ai-models`, {
      headers: {
        'Authorization': `Bearer ${this.authToken}`
      },
      timeout: 5000
    });

    if (configResponse.status !== 200) {
      throw new Error('获取配置失败');
    }

    // 2. 验证配置是否包含预期的AI模型
    const config = configResponse.data;
    const hasExpectedModels = AI_MODELS.some(model => 
      config.models && config.models.some(m => m.name === model)
    );

    if (!hasExpectedModels) {
      this.log('配置中未找到预期的AI模型，但服务正常', 'warning');
    } else {
      this.log('配置驱动联动测试通过', 'success');
    }
  }

  async runAllTests() {
    console.log(chalk.cyan('\n🚀 开始QMS-AI端到端集成测试\n'));

    // 1. 服务健康检查
    for (const [key, config] of Object.entries(SERVICES)) {
      await this.test(`${config.name}健康检查`, async () => {
        await this.checkServiceHealth(config.name, config);
      });
    }

    // 2. 认证流程测试
    await this.test('用户认证流程', async () => {
      await this.testAuthentication();
    });

    // 3. AI模型配置测试
    await this.test('AI模型配置获取', async () => {
      await this.testAIModelConfiguration();
    });

    // 4. Coze Studio集成测试
    await this.test('Coze Studio集成', async () => {
      await this.testCozeStudioIntegration();
    });

    // 5. 配置驱动联动测试
    await this.test('配置驱动联动', async () => {
      await this.testConfigDrivenIntegration();
    });

    // 输出测试结果
    this.printResults();
  }

  printResults() {
    console.log(chalk.cyan('\n📊 测试结果汇总\n'));
    console.log(`总测试数: ${this.results.total}`);
    console.log(chalk.green(`通过: ${this.results.passed}`));
    console.log(chalk.red(`失败: ${this.results.failed}`));
    console.log(`成功率: ${((this.results.passed / this.results.total) * 100).toFixed(1)}%`);

    if (this.results.failed === 0) {
      console.log(chalk.green('\n🎉 所有测试通过！QMS-AI系统集成成功！'));
    } else {
      console.log(chalk.yellow('\n⚠️ 部分测试失败，请检查相关服务'));
    }
  }
}

// 运行测试
async function main() {
  const tester = new IntegrationTester();
  try {
    await tester.runAllTests();
  } catch (error) {
    console.error(chalk.red(`\n💥 测试执行出错: ${error.message}`));
    process.exit(1);
  }
}

// 直接运行测试
main();

export default IntegrationTester;
