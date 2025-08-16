/**
 * 高级功能API服务
 * 与后端高级功能服务进行交互
 */

import request from '@/utils/request'

// 基础配置
const ADVANCED_FEATURES_BASE_URL = '/api'

// 智能分析API
export const analyticsAPI = {
  /**
   * 获取主题分析数据
   */
  getTopics: () => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/analytics/topics`,
      method: 'get'
    })
  },

  /**
   * 获取用户行为分析数据
   */
  getBehavior: () => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/analytics/behavior`,
      method: 'get'
    })
  },

  /**
   * 获取情感分析数据
   */
  getSentiment: () => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/analytics/sentiment`,
      method: 'get'
    })
  },

  /**
   * 获取分析概览数据
   */
  getOverview: () => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/analytics/overview`,
      method: 'get'
    })
  },

  /**
   * 导出分析报告
   */
  exportReport: (format = 'pdf') => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/analytics/export`,
      method: 'post',
      data: { format },
      responseType: 'blob'
    })
  }
}

// 智能推荐API
export const recommendationAPI = {
  /**
   * 获取个性化推荐
   * @param {number} limit - 推荐数量限制
   */
  getPersonalized: (limit = 5) => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/recommendations/personalized`,
      method: 'get',
      params: { limit }
    })
  },

  /**
   * 获取热门推荐
   * @param {number} limit - 推荐数量限制
   */
  getPopular: (limit = 10) => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/recommendations/popular`,
      method: 'get',
      params: { limit }
    })
  },

  /**
   * 获取相似问题推荐
   * @param {string} question - 基础问题
   * @param {number} limit - 推荐数量限制
   */
  getSimilar: (question, limit = 5) => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/recommendations/similar`,
      method: 'post',
      data: { question, limit }
    })
  },

  /**
   * 清除推荐缓存
   */
  clearCache: () => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/recommendations/clear-cache`,
      method: 'post'
    })
  },

  /**
   * 记录推荐点击
   * @param {string} recommendationId - 推荐ID
   * @param {string} type - 推荐类型 (personalized/popular)
   */
  recordClick: (recommendationId, type) => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/recommendations/click`,
      method: 'post',
      data: { recommendationId, type }
    })
  },

  /**
   * 获取推荐统计
   */
  getStats: () => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/recommendations/stats`,
      method: 'get'
    })
  }
}

// 团队协作API
export const collaborationAPI = {
  /**
   * 获取团队统计信息
   */
  getTeamStats: () => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/collaboration/team/stats`,
      method: 'get'
    })
  },

  /**
   * 分享对话
   * @param {Object} shareData - 分享数据
   */
  shareConversation: (shareData) => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/collaboration/share`,
      method: 'post',
      data: shareData
    })
  },

  /**
   * 获取可访问的对话列表
   */
  getAccessibleConversations: () => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/collaboration/conversations`,
      method: 'get'
    })
  },

  /**
   * 获取分享的对话列表
   */
  getSharedConversations: () => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/collaboration/shared`,
      method: 'get'
    })
  },

  /**
   * 创建团队
   * @param {Object} teamData - 团队数据
   */
  createTeam: (teamData) => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/collaboration/teams`,
      method: 'post',
      data: teamData
    })
  },

  /**
   * 邀请用户加入团队
   * @param {Object} inviteData - 邀请数据
   */
  inviteUser: (inviteData) => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/collaboration/invite`,
      method: 'post',
      data: inviteData
    })
  },

  /**
   * 获取团队成员列表
   * @param {string} teamId - 团队ID
   */
  getTeamMembers: (teamId) => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/collaboration/teams/${teamId}/members`,
      method: 'get'
    })
  },

  /**
   * 更新用户权限
   * @param {string} userId - 用户ID
   * @param {Object} permissions - 权限数据
   */
  updatePermissions: (userId, permissions) => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/collaboration/permissions/${userId}`,
      method: 'put',
      data: permissions
    })
  }
}

// 系统集成API
export const integrationAPI = {
  /**
   * 获取集成统计信息
   */
  getStats: () => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/integration/stats`,
      method: 'get'
    })
  },

  /**
   * 获取相关文档推荐
   * @param {string} content - 内容
   * @param {number} limit - 推荐数量限制
   */
  getRelatedDocuments: (content, limit = 5) => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/integration/documents`,
      method: 'post',
      data: { content, limit }
    })
  },

  /**
   * 触发工作流
   * @param {Object} workflowData - 工作流数据
   */
  triggerWorkflow: (workflowData) => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/integration/workflow`,
      method: 'post',
      data: workflowData
    })
  },

  /**
   * 获取模块健康状态
   */
  getModuleHealth: () => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/integration/health`,
      method: 'get'
    })
  },

  /**
   * 同步数据
   * @param {string} module - 模块名称
   * @param {Object} data - 同步数据
   */
  syncData: (module, data) => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/integration/sync/${module}`,
      method: 'post',
      data
    })
  },

  /**
   * 获取集成日志
   * @param {Object} params - 查询参数
   */
  getIntegrationLogs: (params = {}) => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/integration/logs`,
      method: 'get',
      params
    })
  },

  /**
   * 测试模块连接
   * @param {string} module - 模块名称
   */
  testConnection: (module) => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/integration/test/${module}`,
      method: 'post'
    })
  }
}

// 仪表板API
export const dashboardAPI = {
  /**
   * 获取仪表板数据
   */
  getDashboardData: () => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/dashboard`,
      method: 'get'
    })
  },

  /**
   * 获取快速统计
   */
  getQuickStats: () => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/dashboard/quick-stats`,
      method: 'get'
    })
  },

  /**
   * 获取最近活动
   * @param {number} limit - 活动数量限制
   */
  getRecentActivities: (limit = 10) => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/dashboard/activities`,
      method: 'get',
      params: { limit }
    })
  },

  /**
   * 获取系统通知
   */
  getNotifications: () => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/dashboard/notifications`,
      method: 'get'
    })
  }
}

// 用户偏好API
export const preferencesAPI = {
  /**
   * 获取用户偏好设置
   */
  getPreferences: () => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/preferences`,
      method: 'get'
    })
  },

  /**
   * 更新用户偏好设置
   * @param {Object} preferences - 偏好设置
   */
  updatePreferences: (preferences) => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/preferences`,
      method: 'put',
      data: preferences
    })
  },

  /**
   * 重置用户偏好设置
   */
  resetPreferences: () => {
    return request({
      url: `${ADVANCED_FEATURES_BASE_URL}/preferences/reset`,
      method: 'post'
    })
  }
}

// 导出所有API
export default {
  analyticsAPI,
  recommendationAPI,
  collaborationAPI,
  integrationAPI,
  dashboardAPI,
  preferencesAPI
}
