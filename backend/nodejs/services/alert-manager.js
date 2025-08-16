/**
 * QMS-AI 告警管理器
 * 提供系统监控、告警规则、通知发送等功能
 */

const EventEmitter = require('events');
const fs = require('fs');
const path = require('path');

class AlertManager extends EventEmitter {
  constructor(options = {}) {
    super();
    
    this.options = {
      checkInterval: options.checkInterval || 60000, // 1分钟检查一次
      alertHistoryLimit: options.alertHistoryLimit || 1000,
      enableNotifications: options.enableNotifications !== false
    };
    
    // 告警规则
    this.rules = new Map();
    
    // 告警历史
    this.alertHistory = [];
    
    // 当前活跃告警
    this.activeAlerts = new Map();
    
    // 系统指标
    this.metrics = {
      cpu: 0,
      memory: 0,
      disk: 0,
      responseTime: 0,
      errorRate: 0,
      activeConnections: 0
    };
    
    // 定时器
    this.checkTimer = null;
    
    this.initializeDefaultRules();
  }

  /**
   * 初始化默认告警规则
   */
  initializeDefaultRules() {
    // CPU使用率告警
    this.addRule({
      id: 'high_cpu_usage',
      name: 'CPU使用率过高',
      type: 'threshold',
      metric: 'cpu',
      operator: '>',
      threshold: 80,
      duration: 300000, // 5分钟
      severity: 'warning',
      description: 'CPU使用率超过80%'
    });

    // 内存使用率告警
    this.addRule({
      id: 'high_memory_usage',
      name: '内存使用率过高',
      type: 'threshold',
      metric: 'memory',
      operator: '>',
      threshold: 85,
      duration: 300000,
      severity: 'warning',
      description: '内存使用率超过85%'
    });

    // 磁盘使用率告警
    this.addRule({
      id: 'high_disk_usage',
      name: '磁盘使用率过高',
      type: 'threshold',
      metric: 'disk',
      operator: '>',
      threshold: 90,
      duration: 600000, // 10分钟
      severity: 'critical',
      description: '磁盘使用率超过90%'
    });

    // 响应时间告警
    this.addRule({
      id: 'high_response_time',
      name: '响应时间过长',
      type: 'threshold',
      metric: 'responseTime',
      operator: '>',
      threshold: 5000, // 5秒
      duration: 180000, // 3分钟
      severity: 'warning',
      description: '平均响应时间超过5秒'
    });

    // 错误率告警
    this.addRule({
      id: 'high_error_rate',
      name: '错误率过高',
      type: 'threshold',
      metric: 'errorRate',
      operator: '>',
      threshold: 10, // 10%
      duration: 300000,
      severity: 'critical',
      description: '错误率超过10%'
    });

    console.log(`📋 已加载${this.rules.size}个默认告警规则`);
  }

  /**
   * 添加告警规则
   */
  addRule(rule) {
    // 验证规则
    if (!rule.id || !rule.name || !rule.metric) {
      throw new Error('告警规则必须包含id、name和metric字段');
    }

    // 设置默认值
    const fullRule = {
      type: 'threshold',
      operator: '>',
      threshold: 0,
      duration: 300000, // 5分钟
      severity: 'warning',
      enabled: true,
      created: new Date().toISOString(),
      ...rule
    };

    this.rules.set(rule.id, fullRule);
    console.log(`✅ 添加告警规则: ${rule.name}`);
    
    this.emit('ruleAdded', fullRule);
    return fullRule;
  }

  /**
   * 删除告警规则
   */
  removeRule(ruleId) {
    const rule = this.rules.get(ruleId);
    if (rule) {
      this.rules.delete(ruleId);
      
      // 清除相关的活跃告警
      if (this.activeAlerts.has(ruleId)) {
        this.resolveAlert(ruleId);
      }
      
      console.log(`🗑️ 删除告警规则: ${rule.name}`);
      this.emit('ruleRemoved', rule);
      return true;
    }
    return false;
  }

  /**
   * 更新系统指标
   */
  updateMetrics(newMetrics) {
    this.metrics = { ...this.metrics, ...newMetrics };
    this.emit('metricsUpdated', this.metrics);
  }

  /**
   * 开始监控
   */
  start() {
    if (this.checkTimer) {
      console.warn('⚠️ 告警管理器已在运行');
      return;
    }

    console.log('🚀 启动告警管理器');
    this.checkTimer = setInterval(() => {
      this.checkAlerts();
    }, this.options.checkInterval);

    this.emit('started');
  }

  /**
   * 停止监控
   */
  stop() {
    if (this.checkTimer) {
      clearInterval(this.checkTimer);
      this.checkTimer = null;
      console.log('🔒 告警管理器已停止');
      this.emit('stopped');
    }
  }

  /**
   * 检查告警
   */
  async checkAlerts() {
    const now = Date.now();
    
    for (const [ruleId, rule] of this.rules.entries()) {
      if (!rule.enabled) continue;

      try {
        const shouldAlert = this.evaluateRule(rule);
        const activeAlert = this.activeAlerts.get(ruleId);

        if (shouldAlert) {
          if (!activeAlert) {
            // 新告警
            this.triggerAlert(rule);
          } else {
            // 更新现有告警
            activeAlert.lastTriggered = now;
            activeAlert.count++;
          }
        } else if (activeAlert) {
          // 告警恢复
          this.resolveAlert(ruleId);
        }
      } catch (error) {
        console.error(`❌ 检查告警规则失败 ${ruleId}:`, error.message);
      }
    }
  }

  /**
   * 评估告警规则
   */
  evaluateRule(rule) {
    const metricValue = this.metrics[rule.metric];
    if (metricValue === undefined) {
      return false;
    }

    switch (rule.operator) {
      case '>':
        return metricValue > rule.threshold;
      case '<':
        return metricValue < rule.threshold;
      case '>=':
        return metricValue >= rule.threshold;
      case '<=':
        return metricValue <= rule.threshold;
      case '==':
        return metricValue === rule.threshold;
      case '!=':
        return metricValue !== rule.threshold;
      default:
        return false;
    }
  }

  /**
   * 触发告警
   */
  triggerAlert(rule) {
    const alert = {
      id: `${rule.id}_${Date.now()}`,
      ruleId: rule.id,
      ruleName: rule.name,
      severity: rule.severity,
      message: rule.description,
      metricValue: this.metrics[rule.metric],
      threshold: rule.threshold,
      triggered: new Date().toISOString(),
      lastTriggered: Date.now(),
      count: 1,
      status: 'active'
    };

    this.activeAlerts.set(rule.id, alert);
    this.addToHistory(alert);

    console.warn(`🚨 告警触发: ${rule.name} - ${rule.description}`);
    console.warn(`📊 当前值: ${alert.metricValue}, 阈值: ${rule.threshold}`);

    this.emit('alertTriggered', alert);
    
    // 发送通知
    if (this.options.enableNotifications) {
      this.sendNotification(alert);
    }
  }

  /**
   * 解决告警
   */
  resolveAlert(ruleId) {
    const alert = this.activeAlerts.get(ruleId);
    if (alert) {
      alert.status = 'resolved';
      alert.resolved = new Date().toISOString();
      
      this.activeAlerts.delete(ruleId);
      this.addToHistory(alert);

      console.log(`✅ 告警恢复: ${alert.ruleName}`);
      this.emit('alertResolved', alert);
      
      // 发送恢复通知
      if (this.options.enableNotifications) {
        this.sendRecoveryNotification(alert);
      }
    }
  }

  /**
   * 添加到历史记录
   */
  addToHistory(alert) {
    this.alertHistory.unshift({ ...alert });
    
    // 限制历史记录数量
    if (this.alertHistory.length > this.options.alertHistoryLimit) {
      this.alertHistory = this.alertHistory.slice(0, this.options.alertHistoryLimit);
    }
  }

  /**
   * 发送告警通知
   */
  async sendNotification(alert) {
    try {
      // 这里可以集成各种通知方式：邮件、短信、钉钉、企业微信等
      console.log(`📧 发送告警通知: ${alert.ruleName}`);
      
      // 示例：写入通知日志
      const notification = {
        type: 'alert',
        alert: alert,
        timestamp: new Date().toISOString()
      };
      
      await this.writeNotificationLog(notification);
      
    } catch (error) {
      console.error('❌ 发送告警通知失败:', error.message);
    }
  }

  /**
   * 发送恢复通知
   */
  async sendRecoveryNotification(alert) {
    try {
      console.log(`📧 发送恢复通知: ${alert.ruleName}`);
      
      const notification = {
        type: 'recovery',
        alert: alert,
        timestamp: new Date().toISOString()
      };
      
      await this.writeNotificationLog(notification);
      
    } catch (error) {
      console.error('❌ 发送恢复通知失败:', error.message);
    }
  }

  /**
   * 写入通知日志
   */
  async writeNotificationLog(notification) {
    try {
      const logDir = path.join(process.cwd(), 'logs');
      if (!fs.existsSync(logDir)) {
        fs.mkdirSync(logDir, { recursive: true });
      }
      
      const logFile = path.join(logDir, 'notifications.log');
      const logEntry = JSON.stringify(notification) + '\n';
      
      fs.appendFileSync(logFile, logEntry);
    } catch (error) {
      console.error('❌ 写入通知日志失败:', error.message);
    }
  }

  /**
   * 获取告警统计
   */
  getAlertStats() {
    const stats = {
      totalRules: this.rules.size,
      activeAlerts: this.activeAlerts.size,
      totalAlerts: this.alertHistory.length,
      alertsByStatus: {},
      alertsBySeverity: {},
      recentAlerts: this.alertHistory.slice(0, 10)
    };

    // 按状态统计
    this.alertHistory.forEach(alert => {
      stats.alertsByStatus[alert.status] = 
        (stats.alertsByStatus[alert.status] || 0) + 1;
    });

    // 按严重程度统计
    this.alertHistory.forEach(alert => {
      stats.alertsBySeverity[alert.severity] = 
        (stats.alertsBySeverity[alert.severity] || 0) + 1;
    });

    return stats;
  }

  /**
   * 获取所有规则
   */
  getRules() {
    return Array.from(this.rules.values());
  }

  /**
   * 获取活跃告警
   */
  getActiveAlerts() {
    return Array.from(this.activeAlerts.values());
  }

  /**
   * 获取告警历史
   */
  getAlertHistory(limit = 50) {
    return this.alertHistory.slice(0, limit);
  }

  /**
   * 清除告警历史
   */
  clearHistory() {
    this.alertHistory = [];
    console.log('🧹 告警历史已清除');
    this.emit('historyCleaned');
  }

  /**
   * 导出配置
   */
  exportConfig() {
    return {
      rules: Array.from(this.rules.entries()),
      options: this.options,
      timestamp: new Date().toISOString()
    };
  }

  /**
   * 导入配置
   */
  importConfig(config) {
    try {
      // 清除现有规则
      this.rules.clear();
      
      // 导入规则
      if (config.rules && Array.isArray(config.rules)) {
        config.rules.forEach(([id, rule]) => {
          this.rules.set(id, rule);
        });
      }
      
      // 更新选项
      if (config.options) {
        this.options = { ...this.options, ...config.options };
      }
      
      console.log(`✅ 导入配置成功: ${this.rules.size}个规则`);
      this.emit('configImported', config);
      
    } catch (error) {
      console.error('❌ 导入配置失败:', error.message);
      throw error;
    }
  }
}

module.exports = AlertManager;
