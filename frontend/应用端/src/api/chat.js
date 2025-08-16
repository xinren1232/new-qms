/**
 * 聊天API服务
 */

import request from '@/utils/request'

// AI配置 - 由后端统一管理，前端不再硬编码
const AI_CONFIG = {
  // 配置由后端聊天服务统一管理
  // 前端只需要调用 /api/chat/send 接口
}

// 为了兼容性，保留 TRANSSION_CONFIG 导出（已废弃，请使用后端配置）
const TRANSSION_CONFIG = {
  // 此配置已废弃，实际配置由后端管理
  deprecated: true,
  message: '请使用后端统一配置管理'
}

/**
 * 发送聊天消息
 * @param {Object} params - 聊天参数
 * @param {string} params.message - 用户消息
 * @param {string} params.conversation_id - 对话ID
 * @param {Array} params.history - 历史对话（可选）
 */
export function sendChatMessage(params) {
  return request({
    url: '/api/chat/send',
    method: 'post',
    timeout: 0, // 解除时间限制，通过右侧执行流程展示进展
    data: {
      ...params
    }
  })
}

/**
 * 图片解析聊天（视觉）
 * @param {{ message: string, image_url: string, conversation_id?: string, model?: string }} data
 */
export function chatWithVision(data) {
  return request({
    url: '/api/chat/vision',
    method: 'post',
    data
  })
}


/**
 * 获取聊天历史
 * @param {Object} params - 查询参数
 * @param {string} params.conversation_id - 对话ID（可选）
 * @param {number} params.limit - 限制数量（可选）
 */
export function getChatHistory(params = {}) {
  return request({
    url: '/api/chat/history',
    method: 'get',
    params
  })
}

/**
 * 删除聊天记录
 * @param {string} conversationId - 对话ID
 */
export function deleteChatHistory(conversationId) {
  return request({
    url: `/api/chat/history/${conversationId}`,
    method: 'delete'
  })
}

/**
 * 获取聊天统计信息
 */
export function getChatStatistics() {
  return request({
    url: '/api/chat/statistics',
    method: 'get'
  })
}

/**
 * 评价消息
 * @param {Object} params - 评价参数
 * @param {string} params.message_id - 消息ID
 * @param {string} params.feedback - 反馈类型 (like/dislike)
 * @param {string} params.comment - 评价内容（可选）
 */
export function rateMessage(params) {
  return request({
    url: '/api/chat/rate',
    method: 'post',
    data: params
  })
}

/**
 * 获取推荐问题
 * @param {string} category - 问题分类（可选）
 */
export function getRecommendedQuestions(category = 'quality') {
  return request({
    url: '/api/chat/recommended-questions',
    method: 'get',
    params: { category }
  })
}

/**
 * 导出聊天记录
 * @param {Object} params - 导出参数
 * @param {string} params.conversation_id - 对话ID（可选）
 * @param {string} params.format - 导出格式 (json/csv/pdf)
 * @param {string} params.start_date - 开始日期（可选）
 * @param {string} params.end_date - 结束日期（可选）
 */
export function exportChatHistory(params) {
  return request({
    url: '/api/chat/export',
    method: 'post',
    data: params,
    responseType: 'blob'
  })
}

/**
 * 获取AI模型状态
 */
export function getModelStatus() {
  return request({
    url: '/api/chat/model-status',
    method: 'get'
  })
}

/**
 * 切换AI模型
 * @param {Object} params - 模型参数
 * @param {string} params.model - 模型名称
 * @param {Object} params.config - 模型配置
 */
export function switchModel(params) {
  return request({
    url: '/api/chat/switch-model',
    method: 'post',
    data: params
  })
}

/**
 * 获取对话摘要
 * @param {string} conversationId - 对话ID
 */
export function getConversationSummary(conversationId) {
  return request({
    url: `/api/chat/summary/${conversationId}`,
    method: 'get'
  })
}

/**
 * 搜索聊天记录
 * @param {Object} params - 搜索参数
 * @param {string} params.keyword - 搜索关键词
 * @param {string} params.start_date - 开始日期（可选）
 * @param {string} params.end_date - 结束日期（可选）
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页大小
 */
export function searchChatHistory(params) {
  return request({
    url: '/api/chat/search',
    method: 'post',
    data: params
  })
}

/**
 * 获取热门问题
 * @param {number} limit - 限制数量
 */
export function getPopularQuestions(limit = 10) {
  return request({
    url: '/api/chat/popular-questions',
    method: 'get',
    params: { limit }
  })
}

/**
 * 质量管理专用问答API
 */

/**
 * 质量问题诊断
 * @param {Object} params - 诊断参数
 * @param {string} params.description - 问题描述
 * @param {Array} params.symptoms - 症状列表
 * @param {Object} params.context - 上下文信息
 */
export function diagnoseQualityIssue(params) {
  return request({
    url: '/api/chat/quality/diagnose',
    method: 'post',
    data: params
  })
}

/**
 * 质量改进建议
 * @param {Object} params - 改进参数
 * @param {string} params.current_situation - 当前状况
 * @param {Array} params.goals - 改进目标
 * @param {Object} params.constraints - 约束条件
 */
export function getQualityImprovementSuggestions(params) {
  return request({
    url: '/api/chat/quality/improvement',
    method: 'post',
    data: params
  })
}

/**
 * 标准解读
 * @param {Object} params - 解读参数
 * @param {string} params.standard_name - 标准名称
 * @param {string} params.section - 具体章节（可选）
 * @param {string} params.question - 具体问题（可选）
 */
export function interpretStandard(params) {
  return request({
    url: '/api/chat/quality/standard-interpretation',
    method: 'post',
    data: params
  })
}

/**
 * 数据分析解释
 * @param {Object} params - 分析参数
 * @param {Array} params.data - 数据数组
 * @param {string} params.data_type - 数据类型
 * @param {string} params.analysis_goal - 分析目标
 */
export function explainDataAnalysis(params) {
  return request({
    url: '/api/chat/quality/data-analysis',
    method: 'post',
    data: params
  })
}

/**
 * 流程优化建议
 * @param {Object} params - 优化参数
 * @param {string} params.process_description - 流程描述
 * @param {Array} params.pain_points - 痛点列表
 * @param {Object} params.requirements - 需求要求
 */
export function getProcessOptimization(params) {
  return request({
    url: '/api/chat/quality/process-optimization',
    method: 'post',
    data: params
  })
}

// 获取可用模型列表
export function getAvailableModels() {
  return request({
    url: '/api/chat/models',
    method: 'get'
  })
}

// 重复函数已删除，使用上面的定义

// 获取对话列表
export function getConversationList(params = {}) {
  return request({
    url: '/api/chat/conversations',
    method: 'get',
    params: {
      limit: params.limit || 20,
      offset: params.offset || 0,
      model_provider: params.model_provider
    }
  }).then(response => {
    // 确保返回标准格式
    if (response && response.data) {
      return {
        success: true,
        data: response.data,
        message: '获取对话列表成功'
      }
    } else {
      return {
        success: false,
        data: [],
        message: '对话列表为空'
      }
    }
  }).catch(error => {
    console.error('❌ 获取对话列表失败:', error)
    // 返回失败状态，让前端处理降级
    return {
      success: false,
      message: '获取对话列表失败: ' + (error.message || '网络错误'),
      data: []
    }
  })
}

// 获取指定对话的消息
export function getConversationMessages(conversationId) {
  return request({
    url: `/api/chat/conversations/${conversationId}/messages`,
    method: 'get'
  })
}

// 删除对话
export function deleteConversation(conversationId) {
  return request({
    url: `/api/chat/conversations/${conversationId}`,
    method: 'delete'
  })
}

// 更新对话
export function updateConversation(conversationId, data) {
  return request({
    url: `/api/chat/conversations/${conversationId}`,
    method: 'put',
    data
  })
}

// 清空所有对话
export function clearAllConversations() {
  return request({
    url: '/api/chat/conversations',
    method: 'delete'
  })
}

// ===== 案例与经验指导（新）=====
export function importCaseExcel(formData) {
  // formData: { file: File, category?: string }
  return request({
    url: '/api/cases/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function searchCases(params) {
  // { q, category, limit }
  return request({
    url: '/api/cases/search',
    method: 'get',
    params
  })
}

export function getCaseGuidance(body) {
  // { query, category, limit }
  return request({
    url: '/api/chat/case-guidance',
    method: 'post',
    data: body
  })
}

// 导出Transsion配置供其他模块使用
export { TRANSSION_CONFIG }
