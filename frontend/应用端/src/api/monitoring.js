/**
 * AI监控相关API
 */

import request from '@/utils/request'

// 监控服务基础URL - 使用代理，不需要完整URL
const MONITORING_BASE_URL = ''

/**
 * 获取系统总体统计
 * @param {Object} params - 查询参数
 * @param {string} params.startTime - 开始时间
 * @param {string} params.endTime - 结束时间
 */
export function getOverallStatistics(params) {
  return request({
    url: `${MONITORING_BASE_URL}/api/monitoring/overall-statistics`,
    method: 'get',
    params
  })
}

/**
 * 获取请求趋势数据
 * @param {Object} params - 查询参数
 * @param {string} params.startTime - 开始时间
 * @param {string} params.endTime - 结束时间
 */
export function getRequestTrend(params) {
  return request({
    url: `${MONITORING_BASE_URL}/api/monitoring/request-trend`,
    method: 'get',
    params
  })
}

/**
 * 获取响应时间趋势
 * @param {Object} params - 查询参数
 * @param {string} params.startTime - 开始时间
 * @param {string} params.endTime - 结束时间
 */
export function getResponseTimeTrend(params) {
  return request({
    url: `${MONITORING_BASE_URL}/api/monitoring/response-time-trend`,
    method: 'get',
    params
  })
}

/**
 * 获取系统资源使用情况
 * @param {Object} params - 查询参数
 */
export function getSystemResources(params) {
  return request({
    url: `${MONITORING_BASE_URL}/api/monitoring/system-resources`,
    method: 'get',
    params
  })
}

/**
 * 获取错误分析数据
 * @param {Object} params - 查询参数
 * @param {string} params.startTime - 开始时间
 * @param {string} params.endTime - 结束时间
 */
export function getErrorAnalysis(params) {
  return request({
    url: `${MONITORING_BASE_URL}/api/monitoring/error-analysis`,
    method: 'get',
    params
  })
}

/**
 * 获取API性能数据
 * @param {Object} params - 查询参数
 * @param {string} params.startTime - 开始时间
 * @param {string} params.endTime - 结束时间
 */
export function getApiPerformance(params) {
  return request({
    url: `${MONITORING_BASE_URL}/api/monitoring/api-performance`,
    method: 'get',
    params
  })
}

/**
 * 分页查询问答记录
 * @param {Object} params - 查询参数
 * @param {number} params.current - 当前页
 * @param {number} params.size - 页大小
 * @param {string} params.userId - 用户ID
 * @param {string} params.modelProvider - 模型提供商
 * @param {string} params.chatStatus - 对话状态
 * @param {string} params.startTime - 开始时间
 * @param {string} params.endTime - 结束时间
 */
export function getChatRecords(params) {
  return request({
    url: `${MONITORING_BASE_URL}/api/ai-monitoring/chat-records`,
    method: 'get',
    params
  })
}

/**
 * 分页查询反馈记录
 * @param {Object} params - 查询参数
 * @param {number} params.current - 当前页
 * @param {number} params.size - 页大小
 * @param {string} params.feedbackType - 反馈类型
 * @param {string} params.userId - 用户ID
 * @param {string} params.startTime - 开始时间
 * @param {string} params.endTime - 结束时间
 */
export function getFeedbackRecords(params) {
  return request({
    url: `${MONITORING_BASE_URL}/api/ai-monitoring/feedback-records`,
    method: 'get',
    params
  })
}

/**
 * 获取监控仪表板数据
 * @param {Object} params - 查询参数
 * @param {string} params.startTime - 开始时间
 * @param {string} params.endTime - 结束时间
 */
export function getDashboardData(params) {
  return request({
    url: `${MONITORING_BASE_URL}/api/ai-monitoring/dashboard`,
    method: 'get',
    params
  })
}

/**
 * 获取实时监控指标
 */
export function getRealTimeMetrics() {
  return request({
    url: `${MONITORING_BASE_URL}/api/ai-monitoring/realtime-metrics`,
    method: 'get'
  })
}

/**
 * 记录AI问答
 * @param {Object} data - 问答记录数据
 */
export function recordChatMessage(data) {
  return request({
    url: `${MONITORING_BASE_URL}/api/ai-monitoring/chat-record`,
    method: 'post',
    data
  })
}

/**
 * 记录用户反馈
 * @param {Object} data - 反馈数据
 */
export function recordFeedback(data) {
  return request({
    url: `${MONITORING_BASE_URL}/api/ai-monitoring/feedback`,
    method: 'post',
    data
  })
}

/**
 * 获取使用趋势数据
 * @param {Object} params - 查询参数
 * @param {string} params.startTime - 开始时间
 * @param {string} params.endTime - 结束时间
 * @param {string} params.period - 统计周期 (hour, day, week, month)
 */
export function getUsageTrend(params) {
  return request({
    url: `${MONITORING_BASE_URL}/api/ai-monitoring/usage-trend`,
    method: 'get',
    params
  })
}

/**
 * 获取满意度趋势
 * @param {Object} params - 查询参数
 * @param {string} params.startTime - 开始时间
 * @param {string} params.endTime - 结束时间
 */
export function getSatisfactionTrend(params) {
  return request({
    url: `${MONITORING_BASE_URL}/api/ai-monitoring/satisfaction-trend`,
    method: 'get',
    params
  })
}

/**
 * 获取响应时间分布
 * @param {Object} params - 查询参数
 * @param {string} params.startTime - 开始时间
 * @param {string} params.endTime - 结束时间
 */
export function getResponseTimeDistribution(params) {
  return request({
    url: `${MONITORING_BASE_URL}/api/ai-monitoring/response-time-distribution`,
    method: 'get',
    params
  })
}

/**
 * 获取热门问题
 * @param {Object} params - 查询参数
 * @param {string} params.startTime - 开始时间
 * @param {string} params.endTime - 结束时间
 * @param {number} params.limit - 限制数量
 */
export function getPopularQuestions(params) {
  return request({
    url: `${MONITORING_BASE_URL}/api/ai-monitoring/popular-questions`,
    method: 'get',
    params
  })
}

/**
 * 获取最近活跃用户
 * @param {Object} params - 查询参数
 * @param {string} params.startTime - 开始时间
 * @param {number} params.limit - 限制数量
 */
export function getActiveUsers(params) {
  return request({
    url: `${MONITORING_BASE_URL}/api/ai-monitoring/active-users`,
    method: 'get',
    params
  })
}

/**
 * 获取低评分反馈
 * @param {Object} params - 查询参数
 * @param {string} params.startTime - 开始时间
 * @param {string} params.endTime - 结束时间
 * @param {number} params.limit - 限制数量
 */
export function getLowRatingFeedback(params) {
  return request({
    url: `${MONITORING_BASE_URL}/api/ai-monitoring/low-rating-feedback`,
    method: 'get',
    params
  })
}

/**
 * 获取改进建议
 * @param {Object} params - 查询参数
 * @param {string} params.startTime - 开始时间
 * @param {string} params.endTime - 结束时间
 * @param {number} params.limit - 限制数量
 */
export function getImprovementSuggestions(params) {
  return request({
    url: `${MONITORING_BASE_URL}/api/ai-monitoring/improvement-suggestions`,
    method: 'get',
    params
  })
}

/**
 * 获取用户详细统计
 * @param {string} userId - 用户ID
 * @param {Object} params - 查询参数
 * @param {string} params.startTime - 开始时间
 * @param {string} params.endTime - 结束时间
 */
export function getUserDetailStatistics(userId, params) {
  return request({
    url: `${MONITORING_BASE_URL}/api/ai-monitoring/user-detail/${userId}`,
    method: 'get',
    params
  })
}

/**
 * 获取模型详细统计
 * @param {string} modelProvider - 模型提供商
 * @param {string} modelName - 模型名称
 * @param {Object} params - 查询参数
 * @param {string} params.startTime - 开始时间
 * @param {string} params.endTime - 结束时间
 */
export function getModelDetailStatistics(modelProvider, modelName, params) {
  return request({
    url: `${MONITORING_BASE_URL}/api/ai-monitoring/model-detail/${modelProvider}/${modelName}`,
    method: 'get',
    params
  })
}

/**
 * 导出监控报告
 * @param {Object} params - 查询参数
 * @param {string} params.startTime - 开始时间
 * @param {string} params.endTime - 结束时间
 * @param {string} params.format - 导出格式 (excel, pdf)
 */
export function exportMonitoringReport(params) {
  return request({
    url: `${MONITORING_BASE_URL}/api/ai-monitoring/export-report`,
    method: 'get',
    params,
    responseType: 'blob'
  })
}
