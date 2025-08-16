/**
 * QMS-AI 日志工具
 * 统一的日志管理，支持多级别日志和文件输出
 */

const winston = require('winston');
const path = require('path');
const fs = require('fs');

// 确保日志目录存在
const logDir = path.join(__dirname, '../logs');
if (!fs.existsSync(logDir)) {
  fs.mkdirSync(logDir, { recursive: true });
}

// 自定义日志格式
const logFormat = winston.format.combine(
  winston.format.timestamp({
    format: 'YYYY-MM-DD HH:mm:ss'
  }),
  winston.format.errors({ stack: true }),
  winston.format.printf(({ level, message, timestamp, stack }) => {
    if (stack) {
      return `${timestamp} [${level.toUpperCase()}]: ${message}\n${stack}`;
    }
    return `${timestamp} [${level.toUpperCase()}]: ${message}`;
  })
);

// 创建Winston logger实例
const logger = winston.createLogger({
  level: process.env.LOG_LEVEL || 'info',
  format: logFormat,
  defaultMeta: { service: 'qms-ai' },
  transports: [
    // 错误日志文件
    new winston.transports.File({
      filename: path.join(logDir, 'error.log'),
      level: 'error',
      maxsize: 5242880, // 5MB
      maxFiles: 5,
      tailable: true
    }),
    
    // 综合日志文件
    new winston.transports.File({
      filename: path.join(logDir, 'combined.log'),
      maxsize: 5242880, // 5MB
      maxFiles: 5,
      tailable: true
    })
  ]
});

// 在开发环境下添加控制台输出
if (process.env.NODE_ENV !== 'production') {
  logger.add(new winston.transports.Console({
    format: winston.format.combine(
      winston.format.colorize(),
      winston.format.simple()
    )
  }));
}

// 扩展logger功能
class QMSLogger {
  constructor(component = 'QMS-AI') {
    this.component = component;
    this.logger = logger;
  }

  /**
   * 信息日志
   */
  info(message, meta = {}) {
    this.logger.info(`[${this.component}] ${message}`, meta);
  }

  /**
   * 警告日志
   */
  warn(message, meta = {}) {
    this.logger.warn(`[${this.component}] ${message}`, meta);
  }

  /**
   * 错误日志
   */
  error(message, error = null, meta = {}) {
    if (error instanceof Error) {
      this.logger.error(`[${this.component}] ${message}`, {
        error: error.message,
        stack: error.stack,
        ...meta
      });
    } else {
      this.logger.error(`[${this.component}] ${message}`, meta);
    }
  }

  /**
   * 调试日志
   */
  debug(message, meta = {}) {
    this.logger.debug(`[${this.component}] ${message}`, meta);
  }

  /**
   * 详细日志
   */
  verbose(message, meta = {}) {
    this.logger.verbose(`[${this.component}] ${message}`, meta);
  }

  /**
   * HTTP请求日志
   */
  http(method, url, statusCode, responseTime, meta = {}) {
    const message = `${method} ${url} ${statusCode} - ${responseTime}ms`;
    this.logger.http(`[${this.component}] ${message}`, meta);
  }

  /**
   * 数据库操作日志
   */
  database(operation, table, duration, meta = {}) {
    const message = `DB ${operation} ${table} - ${duration}ms`;
    this.logger.info(`[${this.component}] ${message}`, meta);
  }

  /**
   * AI模型调用日志
   */
  aiModel(model, operation, tokens, duration, meta = {}) {
    const message = `AI ${model} ${operation} - ${tokens} tokens, ${duration}ms`;
    this.logger.info(`[${this.component}] ${message}`, meta);
  }

  /**
   * 性能监控日志
   */
  performance(metric, value, unit = 'ms', meta = {}) {
    const message = `PERF ${metric}: ${value}${unit}`;
    this.logger.info(`[${this.component}] ${message}`, meta);
  }

  /**
   * 安全事件日志
   */
  security(event, details, meta = {}) {
    const message = `SECURITY ${event}: ${details}`;
    this.logger.warn(`[${this.component}] ${message}`, meta);
  }

  /**
   * 用户操作日志
   */
  userAction(userId, action, resource, meta = {}) {
    const message = `USER ${userId} ${action} ${resource}`;
    this.logger.info(`[${this.component}] ${message}`, meta);
  }

  /**
   * 系统事件日志
   */
  system(event, details, meta = {}) {
    const message = `SYSTEM ${event}: ${details}`;
    this.logger.info(`[${this.component}] ${message}`, meta);
  }

  /**
   * 创建子logger
   */
  child(childComponent) {
    return new QMSLogger(`${this.component}:${childComponent}`);
  }

  /**
   * 获取原始winston logger
   */
  getWinstonLogger() {
    return this.logger;
  }
}

// 创建默认logger实例
const defaultLogger = new QMSLogger();

// 导出便捷方法
module.exports = {
  // 默认logger实例
  logger: defaultLogger,
  
  // QMSLogger类
  QMSLogger,
  
  // 便捷方法
  info: (message, meta) => defaultLogger.info(message, meta),
  warn: (message, meta) => defaultLogger.warn(message, meta),
  error: (message, error, meta) => defaultLogger.error(message, error, meta),
  debug: (message, meta) => defaultLogger.debug(message, meta),
  verbose: (message, meta) => defaultLogger.verbose(message, meta),
  
  // 专用日志方法
  http: (method, url, statusCode, responseTime, meta) => 
    defaultLogger.http(method, url, statusCode, responseTime, meta),
  database: (operation, table, duration, meta) => 
    defaultLogger.database(operation, table, duration, meta),
  aiModel: (model, operation, tokens, duration, meta) => 
    defaultLogger.aiModel(model, operation, tokens, duration, meta),
  performance: (metric, value, unit, meta) => 
    defaultLogger.performance(metric, value, unit, meta),
  security: (event, details, meta) => 
    defaultLogger.security(event, details, meta),
  userAction: (userId, action, resource, meta) => 
    defaultLogger.userAction(userId, action, resource, meta),
  system: (event, details, meta) => 
    defaultLogger.system(event, details, meta),
  
  // 创建组件logger
  createLogger: (component) => new QMSLogger(component),
  
  // 获取winston实例
  getWinstonLogger: () => logger
};
