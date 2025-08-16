import { getPermissionMenu } from '@/api/role'
import { asyncRoutes, constantRoutes } from '@/router'

import store from '../index'

const state = {
  routes: [],
  addRoutes: [],
  buttons: ['edit', 'search', 'del', 'view'], // 按鈕权限
  buttonRoles: [],
  flowRoutes: [],
  appRoutes: [],
  systemManageRoutes: []
}

const mutations = {
  SET_ROUTES: (state, routes) => {
    state.addRoutes = routes
    state.routes = constantRoutes.concat(routes)
  },
  SET_BUTTONS: (state, buttons) => {
    state.buttons = buttons
  },
  SET_FLOW_ROUTES: (state, routes) => {
    state.flowRoutes = routes
  },
  SET_APP_ROUTES: (state, routes) => {
    state.appRoutes = routes
  },
  SET_SYSTEM_MANAGE_ROUTES: (state, routes) => {
    state.systemManageRoutes = routes
  }
}

const actions = {
  generateRoutes({ commit }) {
    // eslint-disable-next-line no-async-promise-executor
    return new Promise(async (resolve) => {
      // 查询菜单
      if (['development', 'dev', 'sit','uat','gray','production'].includes(process.env.NODE_ENV)) {
        await store.dispatch('user/getCurrentUser')
        commit('SET_ROUTES', asyncRoutes)
        resolve(asyncRoutes)
      } else {
        const user = await store.dispatch('user/getCurrentUser')

        if (user) {
          const res = await getPermissionMenu(user.employeeNo)
          const roleRouters = getRoleRouters(res.data.iscUiResource4AuthVoList)
          const accessedRoutes = filterAsyncRoutes(null, asyncRoutes, roleRouters)
          // 流程权限
          const flowRoutes = filterFlowRoutes(res.data.iscUiResource4AuthVoList)

          if (flowRoutes.length) commit('SET_FLOW_ROUTES', flowRoutes[0].childList)
          // 应用中心权限
          const appRoutes = filterAppRoutes(res.data.iscUiResource4AuthVoList)

          if (appRoutes.length) commit('SET_APP_ROUTES', appRoutes[0].childList)
          // 系统管理权限
          const systemManageRoutes = filterSystemManageRoutes(res.data.iscUiResource4AuthVoList)

          if (systemManageRoutes.length) commit('SET_SYSTEM_MANAGE_ROUTES', systemManageRoutes)

          commit('SET_ROUTES', accessedRoutes)
          resolve(accessedRoutes)
        }
      }
    })
  }
}

function traverse(arr, fun) {
  arr.forEach((item) => {
    fun(item)
    if (item.childList) {
      traverse(item.childList, fun)
    }
  })
}

function getRoleRouters(arr) {
  if (!arr) return {}
  const map = {}

  traverse(arr, (item) => {
    // 菜单
    if (item.reType === '02') {
      const key = getPathKey(item.uiUrl)

      map[key] = item
    }
  })

  return map
}

function filterFlowRoutes(arr) {
  return arr.filter((item) => item.uiUrl === '/process-manage')[0]?.childList ?? []
}

// 应用中心菜单
function filterAppRoutes(arr) {
  return arr.filter((item) => item.uiUrl === '/app-center')[0]?.childList ?? []
}

// 系统管理菜单
function filterSystemManageRoutes(arr) {
  return arr.filter((item) => item.reCode === 'system_manager')?.[0]?.childList ?? []
}

function hasPermission(roleRouters, route) {
  return roleRouters[route.targetPath]
}

function getPathKey(path) {
  const arr = path.split(/[\\\/]/)

  return arr.filter((value) => value).join('#')
}

/**
 * 递归过滤异步路由表，返回符合用户角色权限的路由表
 * @param routes asyncRoutes
 * @param roles
 */
export function filterAsyncRoutes(parent, routes, roleRouters) {
  return routes.reduce((prev, route) => {
    const tmp = { ...route }
    const targetPath = parent ? `${parent.targetPath}/${tmp.path}` : tmp.path

    tmp.targetPath = getPathKey(targetPath)
    if (tmp.children) {
      tmp.children = filterAsyncRoutes(tmp, tmp.children, roleRouters)
      if (tmp.children.length && !tmp.children.every((item) => item.hidden)) {
        prev.push(tmp)
      }
    } else if (tmp.hidden || hasPermission(roleRouters, tmp)) {
      prev.push(tmp)
    }

    return prev
  }, [])
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
