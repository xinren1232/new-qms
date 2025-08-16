#!/usr/bin/env node

/**
 * QMS-AI 系统监控和性能优化脚本
 * 
 * 功能:
 * 1. 实时监控所有服务状态
 * 2. 性能指标收集和分析
 * 3. 自动优化建议
 * 4. 健康检查和告警
 */

import http from 'http';
import { URL } from 'url';
import os from 'os';
import fs from 'fs/promises';

// 服务配置
const SERVICES = {
  configCenter: { name: '配置端', url: 'http://localhost:8072', port: 8072 },
  application: { name: '应用端', url: 'http://localhost:8081', port: 8081 },
  auth: { name: '认证服务', url: 'http://localhost:8084', port: 8084 },
  cozeStudio: { name: 'Coze Studio', url: 'http://localhost:3005', port: 3005 },
  configService: { name: '配置中心服务', url: 'http://localhost:3003', port: 3003 }
};

class SystemMonitor {
  constructor() {
    this.metrics = {
      services: {},
      system: {},
      performance: {}
    };
    this.alerts = [];
  }

  log(message, type = 'info') {
    const timestamp = new Date().toISOString();
    const symbols = {
      success: '✅',
      error: '❌',
      warning: '⚠️',
      info: 'ℹ️',
      monitor: '📊'
    };
    
    console.log(`${symbols[type] || 'ℹ️'} [${timestamp}] ${message}`);
  }

  async httpRequest(url, timeout = 3000) {
    return new Promise((resolve, reject) => {
      const startTime = Date.now();
      const req = http.request(url, { timeout }, (res) => {
        const responseTime = Date.now() - startTime;
        let data = '';
        res.on('data', chunk => data += chunk);
        res.on('end', () => {
          resolve({
            status: res.statusCode,
            responseTime,
            data: data.length,
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

  async checkServiceHealth(serviceName, config) {
    try {
      const healthUrl = `${config.url}/health`;
      const response = await this.httpRequest(healthUrl);
      
      this.metrics.services[serviceName] = {
        status: 'healthy',
        responseTime: response.responseTime,
        statusCode: response.status,
        lastCheck: new Date().toISOString()
      };

      if (response.responseTime > 1000) {
        this.alerts.push({
          type: 'warning',
          service: serviceName,
          message: `响应时间过长: ${response.responseTime}ms`
        });
      }

      return true;
    } catch (error) {
      this.metrics.services[serviceName] = {
        status: 'unhealthy',
        error: error.message,
        lastCheck: new Date().toISOString()
      };

      this.alerts.push({
        type: 'error',
        service: serviceName,
        message: `服务不可用: ${error.message}`
      });

      return false;
    }
  }

  collectSystemMetrics() {
    const cpus = os.cpus();
    const totalMem = os.totalmem();
    const freeMem = os.freemem();
    const usedMem = totalMem - freeMem;
    const memUsagePercent = (usedMem / totalMem * 100).toFixed(2);

    this.metrics.system = {
      platform: os.platform(),
      arch: os.arch(),
      nodeVersion: process.version,
      uptime: os.uptime(),
      loadAverage: os.loadavg(),
      cpuCount: cpus.length,
      totalMemory: (totalMem / 1024 / 1024 / 1024).toFixed(2) + ' GB',
      freeMemory: (freeMem / 1024 / 1024 / 1024).toFixed(2) + ' GB',
      usedMemory: (usedMem / 1024 / 1024 / 1024).toFixed(2) + ' GB',
      memoryUsage: memUsagePercent + '%',
      timestamp: new Date().toISOString()
    };

    // 内存使用率告警
    if (parseFloat(memUsagePercent) > 80) {
      this.alerts.push({
        type: 'warning',
        service: 'system',
        message: `内存使用率过高: ${memUsagePercent}%`
      });
    }
  }

  async collectPerformanceMetrics() {
    const performanceData = {
      timestamp: new Date().toISOString(),
      services: {}
    };

    for (const [key, config] of Object.entries(SERVICES)) {
      try {
        const startTime = Date.now();
        await this.httpRequest(config.url, 2000);
        const responseTime = Date.now() - startTime;
        
        performanceData.services[key] = {
          responseTime,
          status: 'ok'
        };
      } catch (error) {
        performanceData.services[key] = {
          responseTime: -1,
          status: 'error',
          error: error.message
        };
      }
    }

    this.metrics.performance = performanceData;
  }

  generateOptimizationSuggestions() {
    const suggestions = [];

    // 基于服务状态的建议
    for (const [serviceName, metrics] of Object.entries(this.metrics.services)) {
      if (metrics.status === 'unhealthy') {
        suggestions.push(`🔧 ${serviceName}: 服务不可用，建议检查服务状态和日志`);
      } else if (metrics.responseTime > 1000) {
        suggestions.push(`⚡ ${serviceName}: 响应时间过长，建议优化性能或增加资源`);
      }
    }

    // 基于系统指标的建议
    const memUsage = parseFloat(this.metrics.system.memoryUsage);
    if (memUsage > 80) {
      suggestions.push('💾 系统内存使用率过高，建议释放内存或增加内存容量');
    }

    if (memUsage > 90) {
      suggestions.push('🚨 系统内存严重不足，建议立即重启服务或扩容');
    }

    // 基于负载的建议
    const loadAvg = this.metrics.system.loadAverage[0];
    const cpuCount = this.metrics.system.cpuCount;
    if (loadAvg > cpuCount) {
      suggestions.push('🔥 系统负载过高，建议优化代码或增加CPU资源');
    }

    return suggestions;
  }

  async saveMetricsToFile() {
    try {
      const metricsData = {
        ...this.metrics,
        alerts: this.alerts,
        timestamp: new Date().toISOString()
      };

      await fs.writeFile(
        'logs/system-metrics.json',
        JSON.stringify(metricsData, null, 2)
      );
    } catch (error) {
      this.log(`保存指标数据失败: ${error.message}`, 'error');
    }
  }

  printReport() {
    console.log('\n📊 QMS-AI系统监控报告\n');
    
    // 服务状态
    console.log('🔧 服务状态:');
    for (const [serviceName, metrics] of Object.entries(this.metrics.services)) {
      const status = metrics.status === 'healthy' ? '✅' : '❌';
      const responseTime = metrics.responseTime ? `(${metrics.responseTime}ms)` : '';
      console.log(`  ${status} ${serviceName}: ${metrics.status} ${responseTime}`);
    }

    // 系统指标
    console.log('\n💻 系统指标:');
    console.log(`  CPU核心数: ${this.metrics.system.cpuCount}`);
    console.log(`  内存使用: ${this.metrics.system.usedMemory}/${this.metrics.system.totalMemory} (${this.metrics.system.memoryUsage})`);
    console.log(`  系统负载: ${this.metrics.system.loadAverage[0].toFixed(2)}`);
    console.log(`  运行时间: ${Math.floor(this.metrics.system.uptime / 3600)}小时`);

    // 告警信息
    if (this.alerts.length > 0) {
      console.log('\n🚨 告警信息:');
      this.alerts.forEach(alert => {
        const symbol = alert.type === 'error' ? '❌' : '⚠️';
        console.log(`  ${symbol} [${alert.service}] ${alert.message}`);
      });
    }

    // 优化建议
    const suggestions = this.generateOptimizationSuggestions();
    if (suggestions.length > 0) {
      console.log('\n💡 优化建议:');
      suggestions.forEach(suggestion => {
        console.log(`  ${suggestion}`);
      });
    } else {
      console.log('\n✨ 系统运行良好，无需优化！');
    }
  }

  async runMonitoring() {
    this.log('开始系统监控...', 'monitor');

    // 收集系统指标
    this.collectSystemMetrics();

    // 检查所有服务健康状态
    for (const [key, config] of Object.entries(SERVICES)) {
      await this.checkServiceHealth(key, config);
    }

    // 收集性能指标
    await this.collectPerformanceMetrics();

    // 保存指标数据
    await this.saveMetricsToFile();

    // 打印报告
    this.printReport();

    this.log('系统监控完成', 'success');
  }
}

// 运行监控
async function main() {
  const monitor = new SystemMonitor();
  
  try {
    // 确保logs目录存在
    try {
      await fs.mkdir('logs', { recursive: true });
    } catch (error) {
      // 目录已存在，忽略错误
    }

    await monitor.runMonitoring();
  } catch (error) {
    console.error(`💥 监控执行出错: ${error.message}`);
    process.exit(1);
  }
}

main();
