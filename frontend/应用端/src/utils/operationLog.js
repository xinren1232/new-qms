/**
 * 用户操作日志记录工具
 */

import { userStorage, getCurrentUserId, DATA_KEYS } from '@/utils/dataIsolation'
import { getUser } from '@/utils/auth'

/**
 * 操作类型常量
 */
export const OPERATION_TYPES = {
  LOGIN: 'login',
  LOGOUT: 'logout',
  VIEW: 'view',
  CREATE: 'create',
  UPDATE: 'update',
  DELETE: 'delete',
  EXPORT: 'export',
  IMPORT: 'import',
  SEARCH: 'search',
  AI_CHAT: 'ai_chat',
  CONFIG_CHANGE: 'config_change'
}

/**
 * 模块常量
 */
export const MODULES = {
  AUTH: 'auth',
  DASHBOARD: 'dashboard',
  AI_CONVERSATION: 'ai_conversation',
  DATA_SOURCE: 'data_source',
  AI_RULE: 'ai_rule',
  USER_MANAGEMENT: 'user_management',
  SYSTEM_CONFIG: 'system_config',
  PROFILE: 'profile'
}

/**
 * 记录操作日志
 */
export function logOperation(operation) {
  try {
    const userId = getCurrentUserId()
    const user = getUser()
    
    if (!userId || !user) {
      console.warn('用户未登录，无法记录操作日志')
      return
    }

    const logEntry = {
      id: generateLogId(),
      userId,
      username: user.username,
      timestamp: new Date().toISOString(),
      type: operation.type,
      module: operation.module,
      action: operation.action,
      target: operation.target || null,
      details: operation.details || null,
      ip: getClientIP(),
      userAgent: navigator.userAgent,
      success: operation.success !== false, // 默认为成功
      errorMessage: operation.errorMessage || null
    }

    // 获取现有日志
    const existingLogs = userStorage.getItem(DATA_KEYS.OPERATION_LOGS) || []
    
    // 添加新日志
    existingLogs.unshift(logEntry)
    
    // 限制日志数量（保留最近1000条）
    const maxLogs = 1000
    if (existingLogs.length > maxLogs) {
      existingLogs.splice(maxLogs)
    }
    
    // 保存日志
    userStorage.setItem(DATA_KEYS.OPERATION_LOGS, existingLogs)
    
    console.log('操作日志已记录:', logEntry)
  } catch (error) {
    console.error('记录操作日志失败:', error)
  }
}

/**
 * 获取用户操作日志
 */
export function getOperationLogs(filters = {}) {
  try {
    const logs = userStorage.getItem(DATA_KEYS.OPERATION_LOGS) || []
    
    let filteredLogs = logs
    
    // 按类型过滤
    if (filters.type) {
      filteredLogs = filteredLogs.filter(log => log.type === filters.type)
    }
    
    // 按模块过滤
    if (filters.module) {
      filteredLogs = filteredLogs.filter(log => log.module === filters.module)
    }
    
    // 按时间范围过滤
    if (filters.startDate) {
      const startDate = new Date(filters.startDate)
      filteredLogs = filteredLogs.filter(log => new Date(log.timestamp) >= startDate)
    }
    
    if (filters.endDate) {
      const endDate = new Date(filters.endDate)
      filteredLogs = filteredLogs.filter(log => new Date(log.timestamp) <= endDate)
    }
    
    // 按成功状态过滤
    if (filters.success !== undefined) {
      filteredLogs = filteredLogs.filter(log => log.success === filters.success)
    }
    
    // 分页
    if (filters.page && filters.pageSize) {
      const start = (filters.page - 1) * filters.pageSize
      const end = start + filters.pageSize
      filteredLogs = filteredLogs.slice(start, end)
    }
    
    return filteredLogs
  } catch (error) {
    console.error('获取操作日志失败:', error)
    return []
  }
}

/**
 * 清空操作日志
 */
export function clearOperationLogs() {
  try {
    userStorage.removeItem(DATA_KEYS.OPERATION_LOGS)
    console.log('操作日志已清空')
  } catch (error) {
    console.error('清空操作日志失败:', error)
  }
}

/**
 * 导出操作日志
 */
export function exportOperationLogs(format = 'json') {
  try {
    const logs = userStorage.getItem(DATA_KEYS.OPERATION_LOGS) || []
    const user = getUser()
    
    const exportData = {
      exportTime: new Date().toISOString(),
      userId: getCurrentUserId(),
      username: user?.username,
      totalCount: logs.length,
      logs
    }
    
    let content, filename, mimeType
    
    if (format === 'json') {
      content = JSON.stringify(exportData, null, 2)
      filename = `operation_logs_${user?.username}_${Date.now()}.json`
      mimeType = 'application/json'
    } else if (format === 'csv') {
      content = convertLogsToCSV(logs)
      filename = `operation_logs_${user?.username}_${Date.now()}.csv`
      mimeType = 'text/csv'
    } else {
      throw new Error('不支持的导出格式')
    }
    
    // 创建下载链接
    const blob = new Blob([content], { type: mimeType })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    link.click()
    
    // 清理
    URL.revokeObjectURL(url)
    
    // 记录导出操作
    logOperation({
      type: OPERATION_TYPES.EXPORT,
      module: MODULES.PROFILE,
      action: '导出操作日志',
      details: { format, count: logs.length }
    })
    
  } catch (error) {
    console.error('导出操作日志失败:', error)
    logOperation({
      type: OPERATION_TYPES.EXPORT,
      module: MODULES.PROFILE,
      action: '导出操作日志',
      success: false,
      errorMessage: error.message
    })
  }
}

/**
 * 生成日志ID
 */
function generateLogId() {
  return `log_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

/**
 * 获取客户端IP（简化版）
 */
function getClientIP() {
  // 在实际应用中，可以通过后端API获取真实IP
  return 'unknown'
}

/**
 * 将日志转换为CSV格式
 */
function convertLogsToCSV(logs) {
  if (logs.length === 0) return ''
  
  const headers = ['时间', '操作类型', '模块', '操作', '目标', '详情', '状态', '错误信息']
  const csvContent = [
    headers.join(','),
    ...logs.map(log => [
      log.timestamp,
      log.type,
      log.module,
      log.action,
      log.target || '',
      JSON.stringify(log.details || '').replace(/"/g, '""'),
      log.success ? '成功' : '失败',
      log.errorMessage || ''
    ].map(field => `"${field}"`).join(','))
  ].join('\n')
  
  return csvContent
}

/**
 * 常用操作日志记录函数
 */
export const logActions = {
  login: (details) => logOperation({
    type: OPERATION_TYPES.LOGIN,
    module: MODULES.AUTH,
    action: '用户登录',
    details
  }),
  
  logout: (details) => logOperation({
    type: OPERATION_TYPES.LOGOUT,
    module: MODULES.AUTH,
    action: '用户登出',
    details
  }),
  
  viewPage: (pageName, details) => logOperation({
    type: OPERATION_TYPES.VIEW,
    module: pageName,
    action: '访问页面',
    target: pageName,
    details
  }),
  
  aiChat: (question, answer, details) => logOperation({
    type: OPERATION_TYPES.AI_CHAT,
    module: MODULES.AI_CONVERSATION,
    action: 'AI对话',
    target: 'chat',
    details: { question, answer, ...details }
  }),
  
  configChange: (configType, oldValue, newValue) => logOperation({
    type: OPERATION_TYPES.CONFIG_CHANGE,
    module: MODULES.SYSTEM_CONFIG,
    action: '配置修改',
    target: configType,
    details: { oldValue, newValue }
  })
}
