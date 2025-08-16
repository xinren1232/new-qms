/**
 * 本地开发环境认证绕过工具
 * 用于在本地开发时跳过外部认证系统
 */

import { setToken } from '@/app/cookie'

// 本地开发环境的默认用户信息
const LOCAL_DEV_USER = {
  token: 'local-dev-token-' + Date.now(),
  userInfo: {
    id: 'local-dev-user',
    name: '本地开发用户',
    username: 'developer',
    email: 'developer@local.dev'
  }
}

/**
 * 检查是否为本地开发环境
 */
export function isLocalDevelopment() {
  return process.env.NODE_ENV === 'development' && 
         (location.hostname === 'localhost' || location.hostname === '127.0.0.1')
}

/**
 * 设置本地开发环境的认证信息
 */
export function setLocalDevAuth() {
  if (isLocalDevelopment()) {
    setToken(LOCAL_DEV_USER.token)
    // 可以将用户信息存储到localStorage
    localStorage.setItem('local-dev-user', JSON.stringify(LOCAL_DEV_USER.userInfo))
    console.log('本地开发环境：已设置默认认证信息')

    return true
  }

  return false
}

/**
 * 获取本地开发环境的用户信息
 */
export function getLocalDevUser() {
  if (isLocalDevelopment()) {
    const userInfo = localStorage.getItem('local-dev-user')

    return userInfo ? JSON.parse(userInfo) : LOCAL_DEV_USER.userInfo
  }

  return null
}

/**
 * 清除本地开发环境的认证信息
 */
export function clearLocalDevAuth() {
  localStorage.removeItem('local-dev-user')
  console.log('本地开发环境：已清除认证信息')
}
