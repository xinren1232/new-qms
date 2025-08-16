import Cookies from 'js-cookie'
import { useUserStore } from '@/stores/user'

// 常量定义
const TOKEN_KEY = 'ipm-token'
const RTOKEN_KEY = 'ipm-rtoken'
const USER_ID_KEY = 'userId'
const EXPIRE_TIME = 30 // 过期时间（天）

// 通用Cookie操作
export function getCookie(key: string): string | undefined {
  return Cookies.get(key)
}

export function setCookie(key: string, value: string): void {
  Cookies.set(key, value, { expires: EXPIRE_TIME })
}

export function removeCookie(key: string): void {
  Cookies.remove(key)
}

// Token相关操作
export function getToken(): string | undefined {
  return getCookie(TOKEN_KEY)
}

export function getRToken(): string | undefined {
  return getCookie(RTOKEN_KEY)
}

export function setToken(token: string, rtoken?: string): void {
  if (!token) return

  setCookie(TOKEN_KEY, token)
  if (rtoken) {
    setCookie(RTOKEN_KEY, rtoken)
  }

  // 更新store中的token
  const userStore = useUserStore()
  userStore.setToken(token)
  if (rtoken) {
    userStore.setRToken(rtoken)
  }
}

export function removeToken(): void {
  removeCookie(TOKEN_KEY)
  removeCookie(RTOKEN_KEY)

  // 清除store中的token
  const userStore = useUserStore()
  userStore.setToken('')
  userStore.setRToken('')
}

// 用户ID相关操作
export function getUserId(): string | undefined {
  const userStore = useUserStore()
  return userStore.employeeNo || getCookie(USER_ID_KEY)
}

export function setUserId(userId: string): void {
  setCookie(USER_ID_KEY, userId)
}

export function removeUserId(): void {
  removeCookie(USER_ID_KEY)
}

// 获取用户工号
export function getUserJobNumber(): string | undefined {
  const userStore = useUserStore()
  return userStore.employeeNo
}

// 检查是否已登录
export function isLoggedIn(): boolean {
  return !!getToken()
}

// 清除所有认证信息
export function clearAuth(): void {
  removeToken()
  removeUserId()
}

export default Cookies
