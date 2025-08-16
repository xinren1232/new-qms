import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

// 屏蔽router.push错误
const originalPush = Router.prototype.push

Router.prototype.push = function (location) {
  return originalPush.call(this, location).catch((err) => err)
}

import app from './modules/app' // 分类后的组件

export const constantRoutes = [
  {
    path: '*',
    redirect: '/system-manage/properties-manage'
  }
]

export const asyncRoutes = [...app]

const createRouter = () =>
  new Router({
    scrollBehavior: () => ({ y: 0 }),
    routes: constantRoutes.concat(asyncRoutes)
  })
const router = createRouter()

export function resetRouter() {
  const newRouter = createRouter()

  router.matcher = newRouter.matcher // reset router
}

export default router
