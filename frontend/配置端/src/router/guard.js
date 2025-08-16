import { Message } from 'element-ui'
import NProgress from 'nprogress'

import { getToken, setToken } from '@/app/cookie'
import { toLogin } from '@/app/login'
import i18n from '@/i18n'
import { getRequestParam } from '@/utils'

import store from '../store'
import router from '.'

NProgress.configure({ showSpinner: false })

// 不需要重定向到login白名单
const whiteList = ['/login']

router.beforeEach(async (to, from, next) => {
  NProgress.start()

  document.title = getPageTitle(to.meta)

  // 判断是否含有token
  const currToken = getToken()
  const requestParams = getRequestParam()

  if (requestParams.token) {
    // 登录之后跳转的url上会带有token信息
    setToken(requestParams.token, requestParams.rtoken)
    next()
    NProgress.done()
  } else if (currToken) {
    try {
      next()
    } catch (error) {
      // 清理token, 退出登录
      await store.dispatch('user/logout')
      Message.error(error || 'Has Error')
      toLogin({ to, next })
    }
    NProgress.done()
  } else {
    if (whiteList.indexOf(to.path) !== -1) {
      // 若在白名单中就直接跳过
      next()
    } else {
      // 否则跳转到登录界面
      toLogin({ to, next })
      NProgress.done()
    }
  }
})

router.afterEach(NProgress.done)

/**
 * @description 获取Title
 * @param key
 * @returns {string}
 */
function getPageTitle({ name, title }) {
  return (title = i18n.t(name || title)) && `${title} - QMS-AI配置中心`
}
