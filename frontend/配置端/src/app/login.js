import { removeToken } from '@/app/cookie'
import { getLanguage } from '@/i18n'
import { isLocalDevelopment } from '@/utils/auth-bypass'

function normalizeRedirectHref(path) {
  // 获取到域名和哈希路径, 比如: [https://ipm.transsion.com/#/panel, type=123&test=abc]
  let [pathname, searchParams] = path.split('?')

  if (searchParams?.trim()) {
    // 解析参数, 并将 token rtoken 的值重置为空
    const params = { ...Object.fromEntries((new URLSearchParams('?' + searchParams)).entries()), token: '', rtoken: '' }

    // 转换参数
    pathname += Object.keys(params).reduce((str, key) => {
      if (params[key]) {
        str += `${key}=${params[key]}&`
      }

      return str
    }, '?').slice(0, -1)
  }

  return pathname
}

export function toLogin() {
  if (location.href.includes('redirect')) return
  removeToken()

  // 本地开发环境跳转到本地登录页面
  if (isLocalDevelopment()) {
    console.log('本地开发环境，跳转到登录页面')
    // 跳转到本地登录页面
    window.location.href = '/login'

    return
  }

  // 生产环境跳转到外部SSO登录
  location.href = `https://ipm.transsion.com/login/#/?env=${process.env.NODE_ENV}&lang=${getLanguage()}&source=IPM&redirect=${normalizeRedirectHref(location.href)}`
}
