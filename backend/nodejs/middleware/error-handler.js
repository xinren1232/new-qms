/**
 * QMS-AI 统一错误处理中间件
 * 提供全局错误处理、日志记录、错误分类和响应格式化
 */

const winston = require('winston');
const path = require('path');

// 配置日志记录器
const logger = winston.createLogger({
  level: process.env.LOG_LEVEL || 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.errors({ stack: true }),
    winston.format.json()
  ),
  defaultMeta: { service: 'qms-ai' },
  transports: [
    // 错误日志文件
    new winston.transports.File({ 
      filename: path.join(process.cwd(), 'logs', 'error.log'), 
      level: 'error',
      maxsize: 10 * 1024 * 1024, // 10MB
      maxFiles: 5
    }),
    // 综合日志文件
    new winston.transports.File({ 
      filename: path.join(process.cwd(), 'logs', 'combined.log'),
      maxsize: 10 * 1024 * 1024, // 10MB
      maxFiles: 5
    })
  ]
});

// 开发环境添加控制台输出
if (process.env.NODE_ENV !== 'production') {
  logger.add(new winston.transports.Console({
    format: winston.format.combine(
      winston.format.colorize(),
      winston.format.simple()
    )
  }));
}

// 错误类型定义
const ErrorTypes = {
  VALIDATION_ERROR: 'VALIDATION_ERROR',
  AUTHENTICATION_ERROR: 'AUTHENTICATION_ERROR',
  AUTHORIZATION_ERROR: 'AUTHORIZATION_ERROR',
  NOT_FOUND_ERROR: 'NOT_FOUND_ERROR',
  RATE_LIMIT_ERROR: 'RATE_LIMIT_ERROR',
  SERVICE_ERROR: 'SERVICE_ERROR',
  DATABASE_ERROR: 'DATABASE_ERROR',
  CACHE_ERROR: 'CACHE_ERROR',
  CONFLICT_ERROR: 'CONFLICT_ERROR',
  RATE_LIMIT_ERROR: 'RATE_LIMIT_ERROR',
  EXTERNAL_API_ERROR: 'EXTERNAL_API_ERROR',
  DATABASE_ERROR: 'DATABASE_ERROR',
  CACHE_ERROR: 'CACHE_ERROR',
  INTERNAL_ERROR: 'INTERNAL_ERROR',
  TIMEOUT_ERROR: 'TIMEOUT_ERROR'
};

// 自定义错误类
class QMSError extends Error {
  constructor(message, type = ErrorTypes.INTERNAL_ERROR, statusCode = 500, details = null) {
    super(message);
    this.name = 'QMSError';
    this.type = type;
    this.statusCode = statusCode;
    this.details = details;
    this.timestamp = new Date().toISOString();

    // 保持堆栈跟踪
    if (Error.captureStackTrace) {
      Error.captureStackTrace(this, QMSError);
    }
  }
}

// 验证错误类
class ValidationError extends QMSError {
  constructor(message, field = null, value = null) {
    super(message, ErrorTypes.VALIDATION_ERROR, 400, { field, value });
    this.name = 'ValidationError';
  }
}

// API错误类（兼容性）
class APIError extends QMSError {
  constructor(message, code, statusCode = 500, details = null) {
    super(message, code, statusCode, details);
    this.name = 'APIError';
  }
}

// 错误分类器
class ErrorClassifier {
  static classify(error) {
    // 已经是QMSError
    if (error instanceof QMSError) {
      return error;
    }

    // Joi验证错误
    if (error.isJoi) {
      return new QMSError(
        '输入数据验证失败',
        ErrorTypes.VALIDATION_ERROR,
        400,
        error.details
      );
    }

    // JWT错误
    if (error.name === 'JsonWebTokenError' || error.name === 'TokenExpiredError') {
      return new QMSError(
        '认证令牌无效或已过期',
        ErrorTypes.AUTHENTICATION_ERROR,
        401,
        { tokenError: error.message }
      );
    }

    // 权限错误
    if (error.message && error.message.includes('权限不足')) {
      return new QMSError(
        '权限不足',
        ErrorTypes.AUTHORIZATION_ERROR,
        403
      );
    }

    // 限流错误
    if (error.message && error.message.includes('Too many requests')) {
      return new QMSError(
        '请求过于频繁，请稍后再试',
        ErrorTypes.RATE_LIMIT_ERROR,
        429
      );
    }

    // HTTP超时错误
    if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
      return new QMSError(
        '请求超时，请稍后重试',
        ErrorTypes.TIMEOUT_ERROR,
        408,
        { originalError: error.message }
      );
    }

    // 外部API错误
    if (error.response && error.response.status) {
      const status = error.response.status;
      let message = '外部服务调用失败';
      let type = ErrorTypes.EXTERNAL_API_ERROR;

      if (status >= 400 && status < 500) {
        message = '外部服务请求错误';
      } else if (status >= 500) {
        message = '外部服务内部错误';
      }

      return new QMSError(
        message,
        type,
        status,
        {
          url: error.config?.url,
          method: error.config?.method,
          status: error.response.status,
          statusText: error.response.statusText
        }
      );
    }

    // 数据库错误
    if (error.code && (error.code.startsWith('SQLITE_') || error.code === 'ENOENT')) {
      return new QMSError(
        '数据库操作失败',
        ErrorTypes.DATABASE_ERROR,
        500,
        { dbError: error.message }
      );
    }

    // 文件系统错误
    if (error.code === 'ENOENT') {
      return new QMSError(
        '文件或目录不存在',
        ErrorTypes.NOT_FOUND_ERROR,
        404,
        { path: error.path }
      );
    }

    // 默认内部错误
    return new QMSError(
      '服务器内部错误',
      ErrorTypes.INTERNAL_ERROR,
      500,
      { originalError: error.message }
    );
  }
}

// 错误响应格式化器
class ErrorFormatter {
  static format(error, req) {
    const isDevelopment = process.env.NODE_ENV === 'development';
    
    const response = {
      success: false,
      error: {
        type: error.type,
        message: error.message,
        timestamp: error.timestamp,
        requestId: req.id || req.headers['x-request-id'] || 'unknown'
      }
    };

    // 开发环境包含更多调试信息
    if (isDevelopment) {
      response.error.stack = error.stack;
      response.error.details = error.details;
    }

    // 生产环境只在特定错误类型下包含详情
    if (!isDevelopment && error.details && 
        [ErrorTypes.VALIDATION_ERROR, ErrorTypes.RATE_LIMIT_ERROR].includes(error.type)) {
      response.error.details = error.details;
    }

    return response;
  }
}

// 错误统计收集器
class ErrorMetrics {
  constructor() {
    this.metrics = {
      totalErrors: 0,
      errorsByType: {},
      errorsByStatusCode: {},
      recentErrors: []
    };
  }

  record(error) {
    this.metrics.totalErrors++;
    
    // 按类型统计
    this.metrics.errorsByType[error.type] = 
      (this.metrics.errorsByType[error.type] || 0) + 1;
    
    // 按状态码统计
    this.metrics.errorsByStatusCode[error.statusCode] = 
      (this.metrics.errorsByStatusCode[error.statusCode] || 0) + 1;
    
    // 记录最近的错误（保留最近50个）
    this.metrics.recentErrors.unshift({
      type: error.type,
      message: error.message,
      statusCode: error.statusCode,
      timestamp: error.timestamp
    });
    
    if (this.metrics.recentErrors.length > 50) {
      this.metrics.recentErrors = this.metrics.recentErrors.slice(0, 50);
    }
  }

  getMetrics() {
    return { ...this.metrics };
  }

  reset() {
    this.metrics = {
      totalErrors: 0,
      errorsByType: {},
      errorsByStatusCode: {},
      recentErrors: []
    };
  }
}

// 全局错误统计实例
const errorMetrics = new ErrorMetrics();

// 异步错误处理中间件
const asyncErrorHandler = (fn) => {
  return (req, res, next) => {
    Promise.resolve(fn(req, res, next)).catch(next);
  };
};

// 404错误处理中间件
const notFoundHandler = (req, res, next) => {
  const error = new QMSError(
    `路径 ${req.originalUrl} 不存在`,
    ErrorTypes.NOT_FOUND_ERROR,
    404,
    { 
      method: req.method,
      url: req.originalUrl,
      userAgent: req.get('User-Agent')
    }
  );
  next(error);
};

// 全局错误处理中间件
const globalErrorHandler = (error, req, res, next) => {
  // 分类错误
  const classifiedError = ErrorClassifier.classify(error);
  
  // 记录错误日志
  const logData = {
    error: {
      type: classifiedError.type,
      message: classifiedError.message,
      stack: classifiedError.stack
    },
    request: {
      method: req.method,
      url: req.originalUrl,
      userAgent: req.get('User-Agent'),
      ip: req.ip,
      userId: req.user?.userId || 'anonymous'
    },
    timestamp: classifiedError.timestamp
  };

  // 根据错误级别记录日志
  if (classifiedError.statusCode >= 500) {
    logger.error('服务器错误', logData);
  } else if (classifiedError.statusCode >= 400) {
    logger.warn('客户端错误', logData);
  } else {
    logger.info('其他错误', logData);
  }

  // 记录错误统计
  errorMetrics.record(classifiedError);

  // 格式化响应
  const response = ErrorFormatter.format(classifiedError, req);
  
  // 发送响应
  res.status(classifiedError.statusCode).json(response);
};

// 未捕获异常处理
const setupGlobalHandlers = () => {
  // 未捕获的Promise拒绝
  process.on('unhandledRejection', (reason, promise) => {
    logger.error('未处理的Promise拒绝', {
      reason: reason,
      promise: promise,
      stack: reason?.stack
    });
    
    // 在生产环境中，可能需要优雅关闭
    if (process.env.NODE_ENV === 'production') {
      console.error('未处理的Promise拒绝，准备关闭进程...');
      process.exit(1);
    }
  });

  // 未捕获的异常
  process.on('uncaughtException', (error) => {
    logger.error('未捕获的异常', {
      error: error.message,
      stack: error.stack
    });
    
    console.error('未捕获的异常，进程将退出:', error);
    process.exit(1);
  });
};

// 错误统计API中间件
const errorStatsHandler = (req, res) => {
  const stats = errorMetrics.getMetrics();
  res.json({
    success: true,
    data: stats,
    timestamp: new Date().toISOString()
  });
};

module.exports = {
  QMSError,
  ValidationError,
  APIError,
  ErrorTypes,
  ErrorClassifier,
  ErrorFormatter,
  ErrorMetrics,
  errorMetrics,
  asyncErrorHandler,
  notFoundHandler,
  globalErrorHandler,
  setupGlobalHandlers,
  errorStatsHandler,
  logger
};
