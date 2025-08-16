import Cookies from 'js-cookie'

import store from '@/store'

// const appCode = 'plm'// process.env.VUE_APP_CODE.toLowerCase()
const TOKEN_KEY = `ipm-token`
const RTOKEN_KEY = `ipm-rtoken`
const USER_ID_KEY = 'userId'
const EXPIRE_TIME = 30 // 过期时间

export function getCookie(key) {
  return Cookies.get(key)
}

export function setCookie(key, value) {
  return Cookies.set(key, value, { expires: EXPIRE_TIME })
}

export function getToken() {
  const TOKEN_KEY = `ipm-token`

  return getCookie(TOKEN_KEY)
}

export function getRToken() {
  const RTOKEN_KEY = `ipm-rtoken`

  return getCookie(RTOKEN_KEY)
}
export function getUserJobNumber() {
  return store.getters.employeeNo
}

export function setToken(token, rtoken) {
  if (!token) return

  setCookie(TOKEN_KEY, token)
  setCookie(RTOKEN_KEY, rtoken)

  store.dispatch('user/setToken', token)
  store.dispatch('user/setRToken', rtoken)

  // store.dispatch('permission/generateRoutes')
}

export function removeToken() {
  Cookies.remove(TOKEN_KEY)
  Cookies.remove(RTOKEN_KEY)

  store.dispatch('user/setToken', '')
  store.dispatch('user/setRToken', '')
}

export function getUserId() {
  return store.getters.employeeNo || getCookie(USER_ID_KEY)
}
export function setUserId(token) {
  setCookie(USER_ID_KEY, token)
}

export function removeUserId() {
  Cookies.remove(USER_ID_KEY)
}

export default Cookies
