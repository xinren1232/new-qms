/**
 * AI效果评测相关API
 */
import request from '@/utils/request'

const BASE_URL = process.env.VUE_APP_EVALUATION_API || 'http://localhost:3006'

// 获取可用数据集
export function getDatasets() {
  return request({
    url: `${BASE_URL}/api/evaluation/datasets`,
    method: 'get'
  })
}

// 获取可用模型
export function getEvaluationModels() {
  return request({
    url: `${BASE_URL}/api/evaluation/models`,
    method: 'get'
  })
}

// 上传评测数据文件
export function uploadEvaluationFile(file, onProgress) {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: `${BASE_URL}/api/evaluation/upload`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: onProgress
  })
}

// 创建评测任务
export function createEvaluation(data) {
  return request({
    url: `${BASE_URL}/api/evaluation/create`,
    method: 'post',
    data
  })
}

// 开始评测
export function startEvaluation(evaluationId) {
  return request({
    url: `${BASE_URL}/api/evaluation/${evaluationId}/start`,
    method: 'post'
  })
}

// 获取评测列表
export function getEvaluationList(params) {
  return request({
    url: `${BASE_URL}/api/evaluation/list`,
    method: 'get',
    params
  })
}

// 获取评测详情
export function getEvaluationDetail(evaluationId) {
  return request({
    url: `${BASE_URL}/api/evaluation/${evaluationId}`,
    method: 'get'
  })
}

// 获取评测结果
export function getEvaluationResults(evaluationId) {
  return request({
    url: `${BASE_URL}/api/evaluation/${evaluationId}/results`,
    method: 'get'
  })
}

// 删除评测
export function deleteEvaluation(evaluationId) {
  return request({
    url: `${BASE_URL}/api/evaluation/${evaluationId}`,
    method: 'delete'
  })
}

// 获取评测进度
export function getEvaluationProgress(evaluationId) {
  return request({
    url: `${BASE_URL}/api/evaluation/${evaluationId}`,
    method: 'get'
  })
}

// 停止评测
export function stopEvaluation(evaluationId) {
  return request({
    url: `${BASE_URL}/api/evaluation/${evaluationId}/stop`,
    method: 'post'
  })
}

// 导出评测结果
export function exportEvaluationResults(evaluationId, format = 'excel') {
  return request({
    url: `${BASE_URL}/api/evaluation/${evaluationId}/export`,
    method: 'get',
    params: { format },
    responseType: 'blob'
  })
}

// 获取评测统计
export function getEvaluationStats(params) {
  return request({
    url: `${BASE_URL}/api/evaluation/stats`,
    method: 'get',
    params
  })
}

// 批量删除评测
export function batchDeleteEvaluations(evaluationIds) {
  return request({
    url: `${BASE_URL}/api/evaluation/batch-delete`,
    method: 'post',
    data: { ids: evaluationIds }
  })
}

// 复制评测配置
export function cloneEvaluation(evaluationId) {
  return request({
    url: `${BASE_URL}/api/evaluation/${evaluationId}/clone`,
    method: 'post'
  })
}

// 获取评测模板
export function getEvaluationTemplates() {
  return request({
    url: `${BASE_URL}/api/evaluation/templates`,
    method: 'get'
  })
}

// 保存评测模板
export function saveEvaluationTemplate(data) {
  return request({
    url: `${BASE_URL}/api/evaluation/templates`,
    method: 'post',
    data
  })
}

// 获取评测报告
export function getEvaluationReport(evaluationId) {
  return request({
    url: `${BASE_URL}/api/evaluation/${evaluationId}/report`,
    method: 'get'
  })
}

// 分享评测结果
export function shareEvaluationResults(evaluationId, shareConfig) {
  return request({
    url: `${BASE_URL}/api/evaluation/${evaluationId}/share`,
    method: 'post',
    data: shareConfig
  })
}

// 获取评测对比
export function compareEvaluations(evaluationIds) {
  return request({
    url: `${BASE_URL}/api/evaluation/compare`,
    method: 'post',
    data: { ids: evaluationIds }
  })
}

// 获取模型性能趋势
export function getModelPerformanceTrend(modelId, params) {
  return request({
    url: `${BASE_URL}/api/evaluation/models/${modelId}/trend`,
    method: 'get',
    params
  })
}

// 获取评测指标定义
export function getEvaluationMetrics() {
  return request({
    url: `${BASE_URL}/api/evaluation/metrics`,
    method: 'get'
  })
}

// 自定义评测指标
export function createCustomMetric(data) {
  return request({
    url: `${BASE_URL}/api/evaluation/metrics/custom`,
    method: 'post',
    data
  })
}

// 获取评测建议
export function getEvaluationRecommendations(params) {
  return request({
    url: `${BASE_URL}/api/evaluation/recommendations`,
    method: 'get',
    params
  })
}

// 验证评测配置
export function validateEvaluationConfig(config) {
  return request({
    url: `${BASE_URL}/api/evaluation/validate`,
    method: 'post',
    data: config
  })
}

// 获取评测日志
export function getEvaluationLogs(evaluationId, params) {
  return request({
    url: `${BASE_URL}/api/evaluation/${evaluationId}/logs`,
    method: 'get',
    params
  })
}

// 重新运行评测
export function rerunEvaluation(evaluationId, config) {
  return request({
    url: `${BASE_URL}/api/evaluation/${evaluationId}/rerun`,
    method: 'post',
    data: config
  })
}

// 获取评测队列状态
export function getEvaluationQueue() {
  return request({
    url: `${BASE_URL}/api/evaluation/queue`,
    method: 'get'
  })
}

// 调整评测优先级
export function adjustEvaluationPriority(evaluationId, priority) {
  return request({
    url: `${BASE_URL}/api/evaluation/${evaluationId}/priority`,
    method: 'put',
    data: { priority }
  })
}

// 获取评测资源使用情况
export function getEvaluationResourceUsage(params) {
  return request({
    url: `${BASE_URL}/api/evaluation/resources`,
    method: 'get',
    params
  })
}

// 预估评测时间和成本
export function estimateEvaluationCost(config) {
  return request({
    url: `${BASE_URL}/api/evaluation/estimate`,
    method: 'post',
    data: config
  })
}

// 获取评测最佳实践
export function getEvaluationBestPractices() {
  return request({
    url: `${BASE_URL}/api/evaluation/best-practices`,
    method: 'get'
  })
}

// 评测结果反馈
export function submitEvaluationFeedback(evaluationId, feedback) {
  return request({
    url: `${BASE_URL}/api/evaluation/${evaluationId}/feedback`,
    method: 'post',
    data: feedback
  })
}

// 获取评测帮助文档
export function getEvaluationHelp(topic) {
  return request({
    url: `${BASE_URL}/api/evaluation/help`,
    method: 'get',
    params: { topic }
  })
}

export default {
  getDatasets,
  getEvaluationModels,
  uploadEvaluationFile,
  createEvaluation,
  startEvaluation,
  getEvaluationList,
  getEvaluationDetail,
  getEvaluationResults,
  deleteEvaluation,
  getEvaluationProgress,
  stopEvaluation,
  exportEvaluationResults,
  getEvaluationStats,
  batchDeleteEvaluations,
  cloneEvaluation,
  getEvaluationTemplates,
  saveEvaluationTemplate,
  getEvaluationReport,
  shareEvaluationResults,
  compareEvaluations,
  getModelPerformanceTrend,
  getEvaluationMetrics,
  createCustomMetric,
  getEvaluationRecommendations,
  validateEvaluationConfig,
  getEvaluationLogs,
  rerunEvaluation,
  getEvaluationQueue,
  adjustEvaluationPriority,
  getEvaluationResourceUsage,
  estimateEvaluationCost,
  getEvaluationBestPractices,
  submitEvaluationFeedback,
  getEvaluationHelp
}
