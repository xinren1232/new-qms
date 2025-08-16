/**
 * QMS-AI配置中心登录API
 * 专用于配置管理中心的管理员认证
 */

import axios from 'axios'

// QMS-AI认证服务API基础URL
// 使用Vue代理，避免CORS问题 - 修改时间戳: 2025-08-14 14:45
const AUTH_BASE_URL = ''

console.log('🔧 QMS-AI登录API配置 (通过网关):', AUTH_BASE_URL)

// 创建专用的axios实例，避免与现有request拦截器冲突
const authRequest = axios.create({
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 简化的响应拦截器
authRequest.interceptors.response.use(
  response => response.data,
  error => {
    console.error('认证请求失败:', error)

    return Promise.reject(error)
  }
)

/**
 * 配置中心管理员登录
 * @param {Object} loginData - 登录数据
 * @param {string} loginData.username - 用户名
 * @param {string} loginData.password - 密码
 */
export function login(loginData) {
  const loginUrl = `${AUTH_BASE_URL}/api/auth/login`

  console.log('🔐 发起登录请求:', loginUrl, loginData)

  return authRequest({
    url: loginUrl,
    method: 'post',
    data: loginData
  })
}

/**
 * 获取管理员信息
 */
export function getUserInfo() {
  const token = localStorage.getItem('qms_config_token')

  return authRequest({
    url: `${AUTH_BASE_URL}/api/auth/userinfo`,
    method: 'get',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

/**
 * 管理员登出
 */
export function logout() {
  const token = localStorage.getItem('qms_config_token')

  return authRequest({
    url: `${AUTH_BASE_URL}/api/auth/logout`,
    method: 'post',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}

/**
 * 验证管理员权限
 */
export function validateAdminAccess() {
  const token = localStorage.getItem('qms_config_token')

  if (!token) {
    return Promise.reject(new Error('未登录'))
  }

  return authRequest({
    url: `${AUTH_BASE_URL}/api/auth/validate`,
    method: 'get',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
}
