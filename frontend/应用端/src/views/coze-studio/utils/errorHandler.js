/**
 * Coze Studio 错误处理工具
 * 提供统一的错误处理和用户友好的错误提示
 */

import { ElMessage, ElNotification } from 'element-plus'

// 错误类型定义
export const ERROR_TYPES = {
  NETWORK: 'network',
  VALIDATION: 'validation',
  PERMISSION: 'permission',
  STORAGE: 'storage',
  COMPONENT: 'component',
  API: 'api',
  UNKNOWN: 'unknown'
}

// 错误级别定义
export const ERROR_LEVELS = {
  INFO: 'info',
  WARNING: 'warning',
  ERROR: 'error',
  CRITICAL: 'critical'
}

class ErrorHandler {
  constructor() {
    this.errorLog = []
    this.maxLogSize = 100
    this.setupGlobalErrorHandler()
  }

  /**
   * 设置全局错误处理
   */
  setupGlobalErrorHandler() {
    // Vue 错误处理
    window.addEventListener('error', (event) => {
      this.handleError({
        type: ERROR_TYPES.COMPONENT,
        level: ERROR_LEVELS.ERROR,
        message: event.message,
        source: event.filename,
        line: event.lineno,
        column: event.colno,
        stack: event.error?.stack
      })
    })

    // Promise 错误处理
    window.addEventListener('unhandledrejection', (event) => {
      this.handleError({
        type: ERROR_TYPES.API,
        level: ERROR_LEVELS.ERROR,
        message: event.reason?.message || '未处理的Promise错误',
        stack: event.reason?.stack
      })
    })
  }

  /**
   * 处理错误
   * @param {Object} error 错误对象
   */
  handleError(error) {
    const errorInfo = this.normalizeError(error)
    
    // 记录错误日志
    this.logError(errorInfo)
    
    // 显示用户友好的错误提示
    this.showUserError(errorInfo)
    
    // 上报错误（如果需要）
    this.reportError(errorInfo)
  }

  /**
   * 标准化错误对象
   * @param {*} error 原始错误
   * @returns {Object} 标准化的错误对象
   */
  normalizeError(error) {
    if (typeof error === 'string') {
      return {
        type: ERROR_TYPES.UNKNOWN,
        level: ERROR_LEVELS.ERROR,
        message: error,
        timestamp: new Date().toISOString()
      }
    }

    if (error instanceof Error) {
      return {
        type: this.detectErrorType(error),
        level: ERROR_LEVELS.ERROR,
        message: error.message,
        stack: error.stack,
        timestamp: new Date().toISOString()
      }
    }

    return {
      type: ERROR_TYPES.UNKNOWN,
      level: ERROR_LEVELS.ERROR,
      timestamp: new Date().toISOString(),
      ...error
    }
  }

  /**
   * 检测错误类型
   * @param {Error} error 错误对象
   * @returns {string} 错误类型
   */
  detectErrorType(error) {
    const message = error.message.toLowerCase()
    
    if (message.includes('network') || message.includes('fetch')) {
      return ERROR_TYPES.NETWORK
    }
    
    if (message.includes('permission') || message.includes('unauthorized')) {
      return ERROR_TYPES.PERMISSION
    }
    
    if (message.includes('storage') || message.includes('quota')) {
      return ERROR_TYPES.STORAGE
    }
    
    if (message.includes('validation') || message.includes('invalid')) {
      return ERROR_TYPES.VALIDATION
    }
    
    return ERROR_TYPES.UNKNOWN
  }

  /**
   * 记录错误日志
   * @param {Object} errorInfo 错误信息
   */
  logError(errorInfo) {
    this.errorLog.unshift(errorInfo)
    
    // 限制日志大小
    if (this.errorLog.length > this.maxLogSize) {
      this.errorLog = this.errorLog.slice(0, this.maxLogSize)
    }
    
    // 控制台输出
    console.error('[Coze Studio Error]', errorInfo)
  }

  /**
   * 显示用户友好的错误提示
   * @param {Object} errorInfo 错误信息
   */
  showUserError(errorInfo) {
    const userMessage = this.getUserFriendlyMessage(errorInfo)
    
    switch (errorInfo.level) {
      case ERROR_LEVELS.INFO:
        ElMessage.info(userMessage)
        break
      case ERROR_LEVELS.WARNING:
        ElMessage.warning(userMessage)
        break
      case ERROR_LEVELS.ERROR:
        ElMessage.error(userMessage)
        break
      case ERROR_LEVELS.CRITICAL:
        ElNotification.error({
          title: '严重错误',
          message: userMessage,
          duration: 0 // 不自动关闭
        })
        break
    }
  }

  /**
   * 获取用户友好的错误消息
   * @param {Object} errorInfo 错误信息
   * @returns {string} 用户友好的消息
   */
  getUserFriendlyMessage(errorInfo) {
    const messageMap = {
      [ERROR_TYPES.NETWORK]: '网络连接失败，请检查网络设置',
      [ERROR_TYPES.VALIDATION]: '输入数据有误，请检查后重试',
      [ERROR_TYPES.PERMISSION]: '权限不足，请联系管理员',
      [ERROR_TYPES.STORAGE]: '存储空间不足或存储失败',
      [ERROR_TYPES.COMPONENT]: '组件加载失败，请刷新页面重试',
      [ERROR_TYPES.API]: 'API调用失败，请稍后重试',
      [ERROR_TYPES.UNKNOWN]: '发生未知错误，请联系技术支持'
    }

    return messageMap[errorInfo.type] || errorInfo.message || '发生未知错误'
  }

  /**
   * 上报错误
   * @param {Object} errorInfo 错误信息
   */
  reportError(errorInfo) {
    // 这里可以实现错误上报逻辑
    // 例如发送到错误监控服务
    if (errorInfo.level === ERROR_LEVELS.CRITICAL) {
      // 上报严重错误
      console.log('上报严重错误:', errorInfo)
    }
  }

  /**
   * 获取错误日志
   * @param {number} limit 限制数量
   * @returns {Array} 错误日志列表
   */
  getErrorLog(limit = 50) {
    return this.errorLog.slice(0, limit)
  }

  /**
   * 清空错误日志
   */
  clearErrorLog() {
    this.errorLog = []
  }

  /**
   * 创建错误边界组件
   * @param {Function} fallbackComponent 降级组件
   * @returns {Object} Vue组件
   */
  createErrorBoundary(fallbackComponent) {
    return {
      name: 'ErrorBoundary',
      data() {
        return {
          hasError: false,
          error: null
        }
      },
      errorCaptured(error, instance, info) {
        this.hasError = true
        this.error = error
        
        this.handleError({
          type: ERROR_TYPES.COMPONENT,
          level: ERROR_LEVELS.ERROR,
          message: error.message,
          stack: error.stack,
          component: instance?.$options.name,
          info
        })
        
        return false // 阻止错误继续传播
      },
      render() {
        if (this.hasError) {
          return fallbackComponent ? fallbackComponent(this.error) : null
        }
        return this.$slots.default()
      }
    }
  }

  /**
   * 异步操作错误处理装饰器
   * @param {Function} fn 异步函数
   * @param {Object} options 选项
   * @returns {Function} 包装后的函数
   */
  asyncErrorHandler(fn, options = {}) {
    return async (...args) => {
      try {
        return await fn(...args)
      } catch (error) {
        this.handleError({
          type: options.type || ERROR_TYPES.API,
          level: options.level || ERROR_LEVELS.ERROR,
          message: error.message,
          stack: error.stack,
          context: options.context
        })
        
        if (options.rethrow) {
          throw error
        }
        
        return options.defaultValue
      }
    }
  }

  /**
   * 验证错误处理
   * @param {Object} validationResult 验证结果
   */
  handleValidationError(validationResult) {
    if (validationResult.errors && validationResult.errors.length > 0) {
      const firstError = validationResult.errors[0]
      this.handleError({
        type: ERROR_TYPES.VALIDATION,
        level: ERROR_LEVELS.WARNING,
        message: firstError.message,
        field: firstError.field
      })
    }
  }

  /**
   * 网络错误处理
   * @param {Response} response HTTP响应
   */
  handleNetworkError(response) {
    let message = '网络请求失败'
    
    switch (response.status) {
      case 400:
        message = '请求参数错误'
        break
      case 401:
        message = '未授权访问'
        break
      case 403:
        message = '访问被禁止'
        break
      case 404:
        message = '请求的资源不存在'
        break
      case 500:
        message = '服务器内部错误'
        break
      case 502:
        message = '网关错误'
        break
      case 503:
        message = '服务不可用'
        break
    }
    
    this.handleError({
      type: ERROR_TYPES.NETWORK,
      level: ERROR_LEVELS.ERROR,
      message,
      status: response.status,
      url: response.url
    })
  }
}

// 创建单例实例
const errorHandler = new ErrorHandler()

export default errorHandler
export { ERROR_TYPES, ERROR_LEVELS }
