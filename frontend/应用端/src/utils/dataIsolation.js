/**
 * 用户数据隔离工具
 * 确保每个用户的数据独立存储和访问
 */

import { getUser } from '@/utils/auth'

/**
 * 获取当前用户ID
 */
export function getCurrentUserId() {
  const user = getUser()
  return user?.id || null
}

/**
 * 获取用户专属的存储键
 */
export function getUserStorageKey(baseKey) {
  const userId = getCurrentUserId()
  if (!userId) {
    throw new Error('用户未登录，无法获取存储键')
  }
  return `user_${userId}_${baseKey}`
}

/**
 * 用户专属的localStorage操作
 */
export const userStorage = {
  /**
   * 设置用户专属数据
   */
  setItem(key, value) {
    const userKey = getUserStorageKey(key)
    const data = {
      userId: getCurrentUserId(),
      timestamp: Date.now(),
      data: value
    }
    localStorage.setItem(userKey, JSON.stringify(data))
  },

  /**
   * 获取用户专属数据
   */
  getItem(key) {
    try {
      const userKey = getUserStorageKey(key)
      const stored = localStorage.getItem(userKey)
      if (!stored) return null

      const parsed = JSON.parse(stored)
      
      // 验证数据所有者
      if (parsed.userId !== getCurrentUserId()) {
        console.warn('数据所有者不匹配，拒绝访问')
        return null
      }

      return parsed.data
    } catch (error) {
      console.error('获取用户数据失败:', error)
      return null
    }
  },

  /**
   * 删除用户专属数据
   */
  removeItem(key) {
    const userKey = getUserStorageKey(key)
    localStorage.removeItem(userKey)
  },

  /**
   * 清空当前用户的所有数据
   */
  clear() {
    const userId = getCurrentUserId()
    if (!userId) return

    const keysToRemove = []
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i)
      if (key && key.startsWith(`user_${userId}_`)) {
        keysToRemove.push(key)
      }
    }

    keysToRemove.forEach(key => localStorage.removeItem(key))
  }
}

/**
 * 用户专属的会话存储操作
 */
export const userSessionStorage = {
  /**
   * 设置用户专属会话数据
   */
  setItem(key, value) {
    const userKey = getUserStorageKey(key)
    const data = {
      userId: getCurrentUserId(),
      timestamp: Date.now(),
      data: value
    }
    sessionStorage.setItem(userKey, JSON.stringify(data))
  },

  /**
   * 获取用户专属会话数据
   */
  getItem(key) {
    try {
      const userKey = getUserStorageKey(key)
      const stored = sessionStorage.getItem(userKey)
      if (!stored) return null

      const parsed = JSON.parse(stored)
      
      // 验证数据所有者
      if (parsed.userId !== getCurrentUserId()) {
        console.warn('会话数据所有者不匹配，拒绝访问')
        return null
      }

      return parsed.data
    } catch (error) {
      console.error('获取用户会话数据失败:', error)
      return null
    }
  },

  /**
   * 删除用户专属会话数据
   */
  removeItem(key) {
    const userKey = getUserStorageKey(key)
    sessionStorage.removeItem(userKey)
  },

  /**
   * 清空当前用户的所有会话数据
   */
  clear() {
    const userId = getCurrentUserId()
    if (!userId) return

    const keysToRemove = []
    for (let i = 0; i < sessionStorage.length; i++) {
      const key = sessionStorage.key(i)
      if (key && key.startsWith(`user_${userId}_`)) {
        keysToRemove.push(key)
      }
    }

    keysToRemove.forEach(key => sessionStorage.removeItem(key))
  }
}

/**
 * 数据隔离中间件
 * 在API请求中自动添加用户ID参数
 */
export function addUserIdToParams(params = {}) {
  const userId = getCurrentUserId()
  if (userId) {
    return {
      ...params,
      userId
    }
  }
  return params
}

/**
 * 验证数据所有权
 */
export function validateDataOwnership(data) {
  const currentUserId = getCurrentUserId()
  
  if (!currentUserId) {
    throw new Error('用户未登录')
  }

  if (data.userId && data.userId !== currentUserId) {
    throw new Error('无权访问其他用户的数据')
  }

  return true
}

/**
 * 过滤用户数据
 * 只返回属于当前用户的数据
 */
export function filterUserData(dataList) {
  const currentUserId = getCurrentUserId()
  if (!currentUserId) {
    return []
  }

  return dataList.filter(item => {
    try {
      validateDataOwnership(item)
      return true
    } catch {
      return false
    }
  })
}

/**
 * 清理用户退出时的数据
 */
export function cleanupUserData() {
  userStorage.clear()
  userSessionStorage.clear()
  console.log('用户数据已清理')
}

/**
 * 数据隔离常量
 */
export const DATA_KEYS = {
  AI_CONVERSATIONS: 'ai_conversations',
  USER_PREFERENCES: 'user_preferences',
  OPERATION_LOGS: 'operation_logs',
  DRAFT_CONFIGS: 'draft_configs',
  SEARCH_HISTORY: 'search_history',
  RECENT_ACTIVITIES: 'recent_activities'
}
