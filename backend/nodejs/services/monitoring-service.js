const EventEmitter = require('events');
const os = require('os');
const fs = require('fs');
const path = require('path');

class MonitoringService extends EventEmitter {
  constructor() {
    super();
    this.metrics = {
      system: {
        cpu: 0,
        memory: 0,
        disk: 0,
        uptime: 0
      },
      application: {
        totalRequests: 0,
        successfulRequests: 0,
        failedRequests: 0,
        averageResponseTime: 0,
        activeConnections: 0,
        errorRate: 0
      },
      ai: {
        totalConversations: 0,
        totalMessages: 0,
        modelUsage: {},
        averageResponseTime: 0,
        errorCount: 0
      }
    };
    
    this.alerts = [];
    this.thresholds = {
      cpu: 80,           // CPU使用率超过80%
      memory: 85,        // 内存使用率超过85%
      disk: 90,          // 磁盘使用率超过90%
      errorRate: 10,     // 错误率超过10%
      responseTime: 5000 // 响应时间超过5秒
    };
    
    this.isMonitoring = false;
    this.monitoringInterval = null;
    
    // 启动监控
    this.startMonitoring();
  }

  // 启动监控
  startMonitoring() {
    if (this.isMonitoring) return;
    
    this.isMonitoring = true;
    console.log('🔍 实时监控服务启动');
    
    // 每30秒收集一次系统指标
    this.monitoringInterval = setInterval(() => {
      this.collectSystemMetrics();
      this.checkAlerts();
    }, 30000);
    
    // 立即收集一次
    this.collectSystemMetrics();
  }

  // 停止监控
  stopMonitoring() {
    if (!this.isMonitoring) return;
    
    this.isMonitoring = false;
    if (this.monitoringInterval) {
      clearInterval(this.monitoringInterval);
      this.monitoringInterval = null;
    }
    console.log('🔍 实时监控服务停止');
  }

  // 收集系统指标
  collectSystemMetrics() {
    try {
      // CPU使用率
      const cpus = os.cpus();
      let totalIdle = 0;
      let totalTick = 0;
      
      cpus.forEach(cpu => {
        for (let type in cpu.times) {
          totalTick += cpu.times[type];
        }
        totalIdle += cpu.times.idle;
      });
      
      this.metrics.system.cpu = Math.round((1 - totalIdle / totalTick) * 100);
      
      // 内存使用率
      const totalMem = os.totalmem();
      const freeMem = os.freemem();
      this.metrics.system.memory = Math.round(((totalMem - freeMem) / totalMem) * 100);
      
      // 系统运行时间
      this.metrics.system.uptime = Math.round(os.uptime());
      
      // 磁盘使用率（简化版）
      this.getDiskUsage();
      
      // 发出指标更新事件
      this.emit('metricsUpdated', this.metrics);
      
    } catch (error) {
      console.error('❌ 收集系统指标失败:', error);
    }
  }

  // 获取磁盘使用率
  getDiskUsage() {
    try {
      const stats = fs.statSync(process.cwd());
      // 这是一个简化的实现，实际项目中可能需要更复杂的磁盘监控
      this.metrics.system.disk = 0; // 默认值
    } catch (error) {
      this.metrics.system.disk = 0;
    }
  }

  // 记录请求指标
  recordRequest(success = true, responseTime = 0) {
    this.metrics.application.totalRequests++;
    
    if (success) {
      this.metrics.application.successfulRequests++;
    } else {
      this.metrics.application.failedRequests++;
    }
    
    // 计算错误率
    this.metrics.application.errorRate = 
      (this.metrics.application.failedRequests / this.metrics.application.totalRequests) * 100;
    
    // 更新平均响应时间（简化算法）
    if (this.metrics.application.averageResponseTime === 0) {
      this.metrics.application.averageResponseTime = responseTime;
    } else {
      this.metrics.application.averageResponseTime = 
        (this.metrics.application.averageResponseTime + responseTime) / 2;
    }
  }

  // 记录AI对话指标
  recordAIConversation(modelName, responseTime, success = true) {
    this.metrics.ai.totalConversations++;
    
    if (!this.metrics.ai.modelUsage[modelName]) {
      this.metrics.ai.modelUsage[modelName] = 0;
    }
    this.metrics.ai.modelUsage[modelName]++;
    
    if (!success) {
      this.metrics.ai.errorCount++;
    }
    
    // 更新AI平均响应时间
    if (this.metrics.ai.averageResponseTime === 0) {
      this.metrics.ai.averageResponseTime = responseTime;
    } else {
      this.metrics.ai.averageResponseTime = 
        (this.metrics.ai.averageResponseTime + responseTime) / 2;
    }
  }

  // 记录消息
  recordMessage() {
    this.metrics.ai.totalMessages++;
  }

  // 检查告警条件
  checkAlerts() {
    const alerts = [];
    
    // CPU告警
    if (this.metrics.system.cpu > this.thresholds.cpu) {
      alerts.push({
        type: 'CPU_HIGH',
        level: 'WARNING',
        message: `CPU使用率过高: ${this.metrics.system.cpu}%`,
        value: this.metrics.system.cpu,
        threshold: this.thresholds.cpu,
        timestamp: new Date().toISOString()
      });
    }
    
    // 内存告警
    if (this.metrics.system.memory > this.thresholds.memory) {
      alerts.push({
        type: 'MEMORY_HIGH',
        level: 'WARNING',
        message: `内存使用率过高: ${this.metrics.system.memory}%`,
        value: this.metrics.system.memory,
        threshold: this.thresholds.memory,
        timestamp: new Date().toISOString()
      });
    }
    
    // 错误率告警
    if (this.metrics.application.errorRate > this.thresholds.errorRate) {
      alerts.push({
        type: 'ERROR_RATE_HIGH',
        level: 'ERROR',
        message: `错误率过高: ${this.metrics.application.errorRate.toFixed(2)}%`,
        value: this.metrics.application.errorRate,
        threshold: this.thresholds.errorRate,
        timestamp: new Date().toISOString()
      });
    }
    
    // 响应时间告警
    if (this.metrics.application.averageResponseTime > this.thresholds.responseTime) {
      alerts.push({
        type: 'RESPONSE_TIME_HIGH',
        level: 'WARNING',
        message: `平均响应时间过长: ${this.metrics.application.averageResponseTime}ms`,
        value: this.metrics.application.averageResponseTime,
        threshold: this.thresholds.responseTime,
        timestamp: new Date().toISOString()
      });
    }
    
    // 处理新告警
    alerts.forEach(alert => {
      this.addAlert(alert);
    });
  }

  // 添加告警
  addAlert(alert) {
    // 检查是否已存在相同类型的告警（避免重复）
    const existingAlert = this.alerts.find(a => 
      a.type === alert.type && 
      Date.now() - new Date(a.timestamp).getTime() < 300000 // 5分钟内
    );
    
    if (!existingAlert) {
      this.alerts.unshift(alert);
      
      // 只保留最近100个告警
      if (this.alerts.length > 100) {
        this.alerts = this.alerts.slice(0, 100);
      }
      
      // 发出告警事件
      this.emit('alert', alert);
      
      // 控制台输出告警
      const emoji = alert.level === 'ERROR' ? '🚨' : '⚠️';
      console.log(`${emoji} [${alert.level}] ${alert.message}`);
    }
  }

  // 获取当前指标
  getMetrics() {
    return {
      ...this.metrics,
      timestamp: new Date().toISOString(),
      uptime: process.uptime()
    };
  }

  // 获取告警列表
  getAlerts(limit = 50) {
    return this.alerts.slice(0, limit);
  }

  // 清除告警
  clearAlerts() {
    this.alerts = [];
    console.log('✅ 告警列表已清除');
  }

  // 更新告警阈值
  updateThresholds(newThresholds) {
    this.thresholds = { ...this.thresholds, ...newThresholds };
    console.log('✅ 告警阈值已更新:', this.thresholds);
  }

  // 获取系统健康状态
  getHealthStatus() {
    const cpu = this.metrics.system.cpu;
    const memory = this.metrics.system.memory;
    const errorRate = this.metrics.application.errorRate;
    
    let status = 'HEALTHY';
    let issues = [];
    
    if (cpu > this.thresholds.cpu || memory > this.thresholds.memory) {
      status = 'WARNING';
      if (cpu > this.thresholds.cpu) issues.push('CPU使用率过高');
      if (memory > this.thresholds.memory) issues.push('内存使用率过高');
    }
    
    if (errorRate > this.thresholds.errorRate) {
      status = 'ERROR';
      issues.push('错误率过高');
    }
    
    return {
      status,
      issues,
      metrics: this.getMetrics(),
      alerts: this.getAlerts(10)
    };
  }
}

module.exports = MonitoringService;
