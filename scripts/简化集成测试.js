#!/usr/bin/env node

/**
 * QMS-AI 简化端到端集成测试
 * 使用Node.js内置模块验证所有服务协同工作
 */

import http from 'http';
import https from 'https';
import { URL } from 'url';

// 服务配置
const SERVICES = {
  configCenter: {
    name: '配置端',
    url: 'http://localhost:8072/alm-transcend-configcenter-web/',
    type: 'frontend'
  },
  application: {
    name: '应用端', 
    url: 'http://localhost:8081/',
    type: 'frontend'
  },
  auth: {
    name: '认证服务',
    url: 'http://localhost:8084/health',
    type: 'backend'
  },
  cozeStudio: {
    name: 'Coze Studio服务',
    url: 'http://localhost:3005/health',
    type: 'backend'
  },
  configService: {
    name: '配置中心服务',
    url: 'http://localhost:8082/health',
    type: 'backend'
  }
};

class SimpleTester {
  constructor() {
    this.results = {
      passed: 0,
      failed: 0,
      total: 0
    };
  }

  log(message, type = 'info') {
    const timestamp = new Date().toLocaleTimeString();
    const symbols = {
      success: '✅',
      error: '❌', 
      warning: '⚠️',
      info: 'ℹ️'
    };
    
    console.log(`${symbols[type] || 'ℹ️'} [${timestamp}] ${message}`);
  }

  async httpRequest(url, options = {}) {
    return new Promise((resolve, reject) => {
      const urlObj = new URL(url);
      const client = urlObj.protocol === 'https:' ? https : http;
      
      const req = client.request(url, {
        method: 'GET',
        timeout: 5000,
        ...options
      }, (res) => {
        let data = '';
        res.on('data', chunk => data += chunk);
        res.on('end', () => {
          resolve({
            status: res.statusCode,
            data: data,
            headers: res.headers
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

  async checkServiceHealth(serviceName, url, type) {
    try {
      const response = await this.httpRequest(url);
      
      if (type === 'backend') {
        if (response.status !== 200) {
          throw new Error(`服务响应状态码: ${response.status}`);
        }
      } else {
        // 前端服务只要能访问就算成功
        if (response.status >= 500) {
          throw new Error(`服务不可访问: ${response.status}`);
        }
      }
      
      this.log(`${serviceName} 健康检查通过 (状态码: ${response.status})`, 'success');
    } catch (error) {
      throw new Error(`${serviceName} 健康检查失败: ${error.message}`);
    }
  }

  async testAuthentication() {
    try {
      const postData = JSON.stringify({
        username: 'admin',
        password: 'admin123'
      });

      const response = await new Promise((resolve, reject) => {
        const req = http.request('http://localhost:8084/api/auth/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Content-Length': Buffer.byteLength(postData)
          },
          timeout: 5000
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
          reject(new Error('认证请求超时'));
        });
        
        req.write(postData);
        req.end();
      });

      if (response.status !== 200) {
        throw new Error(`认证失败，状态码: ${response.status}`);
      }

      let responseData;
      try {
        responseData = JSON.parse(response.data);
      } catch (e) {
        throw new Error('认证响应格式错误');
      }

      if (!responseData.token) {
        throw new Error('认证成功但未返回token');
      }

      this.log('用户认证成功，获取到token', 'success');
    } catch (error) {
      throw new Error(`认证测试失败: ${error.message}`);
    }
  }

  async runAllTests() {
    console.log('\n🚀 开始QMS-AI简化端到端集成测试\n');

    // 1. 服务健康检查
    for (const [key, config] of Object.entries(SERVICES)) {
      await this.test(`${config.name}健康检查`, async () => {
        await this.checkServiceHealth(config.name, config.url, config.type);
      });
    }

    // 2. 认证流程测试
    await this.test('用户认证流程', async () => {
      await this.testAuthentication();
    });

    // 输出测试结果
    this.printResults();
  }

  printResults() {
    console.log('\n📊 测试结果汇总\n');
    console.log(`总测试数: ${this.results.total}`);
    console.log(`✅ 通过: ${this.results.passed}`);
    console.log(`❌ 失败: ${this.results.failed}`);
    console.log(`成功率: ${((this.results.passed / this.results.total) * 100).toFixed(1)}%`);

    if (this.results.failed === 0) {
      console.log('\n🎉 所有测试通过！QMS-AI系统集成成功！');
      console.log('\n🎯 系统状态:');
      console.log('✅ 配置端 (Vue2) - 正常运行');
      console.log('✅ 应用端 (Vue3) - 正常运行');
      console.log('✅ 认证服务 (Node.js) - 正常运行');
      console.log('✅ Coze Studio服务 (Node.js) - 正常运行');
      console.log('✅ 配置中心服务 (Node.js) - 正常运行');
      console.log('\n🚀 系统已准备好进行功能扩展开发！');
    } else {
      console.log('\n⚠️ 部分测试失败，请检查相关服务');
    }
  }
}

// 运行测试
async function main() {
  const tester = new SimpleTester();
  try {
    await tester.runAllTests();
  } catch (error) {
    console.error(`\n💥 测试执行出错: ${error.message}`);
    process.exit(1);
  }
}

main();
