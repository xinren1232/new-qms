/**
 * 认证相关API
 */

import request from '@/utils/request'

// 权限服务基础URL - 使用代理，不设置完整URL
const AUTH_BASE_URL = ''

/**
 * 用户登录
 * @param {Object} loginData - 登录数据
 * @param {string} loginData.username - 用户名
 * @param {string} loginData.password - 密码
 */
export function login(loginData) {
  return request({
    url: '/api/auth/login',
    method: 'post',
    data: loginData
  })
}

/**
 * 用户注册
 * @param {Object} registerData - 注册数据
 */
export function register(registerData) {
  return request({
    url: '/api/auth/register',
    method: 'post',
    data: registerData
  })
}

/**
 * 获取用户信息
 */
export function getUserInfo() {
  return request({
    url: `/api/auth/userinfo`,
    method: 'get'
  })
}

/**
 * 用户登出
 */
export function logout() {
  return request({
    url: `/api/auth/logout`,
    method: 'post'
  })
}

/**
 * 检查权限
 * @param {string} permission - 权限标识
 */
export function checkPermission(permission) {
  return request({
    url: `/api/auth/check-permission`,
    method: 'post',
    data: { permission }
  })
}

/**
 * 获取用户权限列表
 */
export function getUserPermissions() {
  return request({
    url: `/api/auth/permissions`,
    method: 'get'
  })
}

/**
 * 获取用户列表 (管理员)
 */
export function getUserList() {
  return request({
    url: `${AUTH_BASE_URL}/admin/users`,
    method: 'get'
  })
}

/**
 * 审批用户 (管理员)
 * @param {Object} approvalData - 审批数据
 * @param {string} approvalData.userId - 用户ID
 * @param {boolean} approvalData.approved - 是否通过
 * @param {string} approvalData.reason - 审批理由
 */
export function approveUser(approvalData) {
  return request({
    url: `${AUTH_BASE_URL}/admin/approve-user`,
    method: 'post',
    data: approvalData
  })
}

/**
 * 检查用户名是否可用
 * @param {string} username - 用户名
 */
export function checkUsername(username) {
  return request({
    url: `${AUTH_BASE_URL}/auth/check-username`,
    method: 'get',
    params: { username }
  })
}

/**
 * 检查邮箱是否可用
 * @param {string} email - 邮箱
 */
export function checkEmail(email) {
  return request({
    url: `${AUTH_BASE_URL}/auth/check-email`,
    method: 'get',
    params: { email }
  })
}

/**
 * 修改密码
 * @param {Object} passwordData - 密码数据
 * @param {string} passwordData.oldPassword - 旧密码
 * @param {string} passwordData.newPassword - 新密码
 */
export function changePassword(passwordData) {
  return request({
    url: `${AUTH_BASE_URL}/auth/change-password`,
    method: 'post',
    data: passwordData
  })
}

/**
 * 更新用户信息
 * @param {Object} userInfo - 用户信息
 */
export function updateUserInfo(userInfo) {
  return request({
    url: `${AUTH_BASE_URL}/auth/update-profile`,
    method: 'put',
    data: userInfo
  })
}

/**
 * 获取用户的AI对话历史
 * @param {Object} params - 查询参数
 */
export function getUserConversations(params) {
  return request({
    url: `${AUTH_BASE_URL}/user/conversations`,
    method: 'get',
    params
  })
}

/**
 * 获取用户的操作日志
 * @param {Object} params - 查询参数
 */
export function getUserLogs(params) {
  return request({
    url: `${AUTH_BASE_URL}/user/logs`,
    method: 'get',
    params
  })
}

/**
 * 权限常量定义
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
 * 角色常量定义
 */
export const ROLES = {
  ADMIN: 'ADMIN',           // 系统管理员
  ANALYST: 'ANALYST',       // 数据分析师
  USER: 'USER'              // 普通用户
}
