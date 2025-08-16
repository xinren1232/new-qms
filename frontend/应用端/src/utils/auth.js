/**
 * 认证和权限管理工具
 */

import { ElMessage } from 'element-plus'
import { getUserInfo, checkPermission } from '@/api/auth'

// Token相关
export const TOKEN_KEY = 'qms_token'
export const USER_KEY = 'qms_user'

/**
 * 获取Token
 */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

/**
 * 设置Token
 */
export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 移除Token
 */
export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

/**
 * 获取用户信息
 */
export function getUser() {
  try {
    const userStr = localStorage.getItem(USER_KEY)
    return userStr ? JSON.parse(userStr) : null
  } catch (error) {
    console.error('获取用户信息失败:', error)
    return null
  }
}

/**
 * 设置用户信息
 */
export function setUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

/**
 * 检查是否已登录
 */
export function isLoggedIn() {
  const token = getToken()
  const user = getUser()
  return !!(token && user)
}

/**
 * 检查Token是否有效
 */
export async function validateToken() {
  const token = getToken()
  if (!token) {
    return false
  }

  try {
    // 调用后端验证Token
    const response = await getUserInfo()
    if (response.success) {
      // 更新用户信息
      setUser(response.data)
      return true
    } else {
      // Token无效，清除本地存储
      removeToken()
      return false
    }
  } catch (error) {
    console.error('Token验证失败:', error)
    removeToken()
    return false
  }
}

/**
 * 检查用户权限
 */
export async function hasPermission(permission) {
  const user = getUser()
  if (!user) {
    return false
  }

  // 超级管理员拥有所有权限
  if (user.role === 'ADMIN' || user.permissions?.includes('*')) {
    return true
  }

  // 检查具体权限
  if (user.permissions?.includes(permission)) {
    return true
  }

  // 调用后端验证权限
  try {
    const response = await checkPermission(permission)
    return response.success && response.data
  } catch (error) {
    console.error('权限检查失败:', error)
    return false
  }
}

/**
 * 检查用户角色
 */
export function hasRole(role) {
  const user = getUser()
  if (!user) {
    return false
  }

  if (Array.isArray(user.roles)) {
    return user.roles.includes(role)
  }

  return user.role === role
}

/**
 * 权限常量
 */
export const PERMISSIONS = {
  // AI对话管理
  AI_CONVERSATION_VIEW: 'ai:conversation:view',
  AI_CONVERSATION_ANALYZE: 'ai:conversation:analyze',
  AI_CONVERSATION_EXPORT: 'ai:conversation:export',
  
  // 数据源管理
  AI_DATASOURCE_VIEW: 'ai:datasource:view',
  AI_DATASOURCE_ADD: 'ai:datasource:add',
  AI_DATASOURCE_EDIT: 'ai:datasource:edit',
  AI_DATASOURCE_DELETE: 'ai:datasource:delete',
  AI_DATASOURCE_TEST: 'ai:datasource:test',
  
  // AI规则管理
  AI_RULE_VIEW: 'ai:rule:view',
  AI_RULE_ADD: 'ai:rule:add',
  AI_RULE_EDIT: 'ai:rule:edit',
  AI_RULE_DELETE: 'ai:rule:delete',
  
  // 用户管理
  USER_VIEW: 'user:view',
  USER_ADD: 'user:add',
  USER_EDIT: 'user:edit',
  USER_DELETE: 'user:delete',
  USER_APPROVE: 'user:approve',
  
  // 系统配置
  SYSTEM_CONFIG_VIEW: 'system:config:view',
  SYSTEM_CONFIG_EDIT: 'system:config:edit',
  
  // 超级管理员权限
  ADMIN_ALL: '*'
}

/**
 * 角色常量
 */
export const ROLES = {
  ADMIN: 'ADMIN',           // 系统管理员
  ANALYST: 'ANALYST',       // 数据分析师
  USER: 'USER'              // 普通用户
}

/**
 * 登出
 */
export function logout() {
  // 清理用户数据
  import('@/utils/dataIsolation').then(({ cleanupUserData }) => {
    cleanupUserData()
  })

  removeToken()
  // 可以在这里调用后端登出API
  ElMessage.success('已退出登录')
}

/**
 * 权限指令工具
 */
export function checkDirectivePermission(el, binding) {
  const { value } = binding
  const permissions = Array.isArray(value) ? value : [value]
  
  const user = getUser()
  if (!user) {
    el.parentNode && el.parentNode.removeChild(el)
    return
  }

  // 超级管理员拥有所有权限
  if (user.role === 'ADMIN' || user.permissions?.includes('*')) {
    return
  }

  // 检查是否有任一权限
  const hasAnyPermission = permissions.some(permission => 
    user.permissions?.includes(permission)
  )

  if (!hasAnyPermission) {
    el.parentNode && el.parentNode.removeChild(el)
  }
}
