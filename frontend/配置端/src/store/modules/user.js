import Cookie from 'js-cookie'
import { set } from 'vue'

import { getCurrentUser, getRefreshToken, getUsers } from '@/api/user'
import { getRToken, getToken, removeToken } from '@/app/cookie'
import { toLogin } from '@/app/login'
import { combineRequest } from '@/utils'
import { generateUserAvatar } from '@/hooks/useUserAvatar'

const state = {
  userMap: {},
  userInfoMap: {},
  user: {
    uid: '',
    employeeNo: '',
    name: '',
    deptId: '',
    deptName: ''
  },
  token: '',
  rtoken: ''
}

const mutations = {
  SET_USER: (state, user) => {
    state.user = user
    Cookie.set('user', JSON.stringify(user))
  },
  SET_TOKEN: (state, token) => {
    state.token = token
  },
  SET_RTOKEN: (state, rtoken) => {
    state.rtoken = rtoken
  },
  SET_USER_MAP: (state, userMap) => {
    state.userMap = userMap
  },
  SET_USER_INFO_MAP: (state, userInfoMap) => {
    state.userInfoMap = userInfoMap
  }
}
// refresh token需要使用节流
let refreshTokenRequest = null
let debounceEmployeeNos = ''
let debounceTimeoutId = null
const actions = {
  getCurrentUser({ commit, state }) {
    // 如果已经获取过了则不重新获取
    if (state.user?.name && state.user?.employeeNo) {
      return Promise.resolve(state.user)
    }

    // 根据token获取用户工号
    return new Promise((resolve) => {
      if (!state.user || !state.user.id) {
        const token = getToken()

        if (token) {
          return getCurrentUser(token).then(({ data }) => {
            data.avatar = generateUserAvatar(data.employeeNo)
            data.zh_name = data.name
            data.en_name = data.enName
            commit('SET_USER', data)
            resolve(data)
          })
        }
      } else {
        resolve(state.user)
      }
    })
  },
  setToken({ commit }, token) {
    commit('SET_TOKEN', token)
  },
  setRToken({ commit }, rtoken) {
    commit('SET_RTOKEN', rtoken)
  },
  refreshToken() {
    const doRequest = () => {
      return getRefreshToken(getRToken())
    }

    if (!refreshTokenRequest) refreshTokenRequest = combineRequest(doRequest, null, 2000)

    return refreshTokenRequest()
  },
  logout({ commit }) {
    commit('SET_USER', {})
    removeToken()
    toLogin()
  },
  setUserMap({ commit }, userMap) {
    commit('SET_USER_MAP', userMap)
  },
  setUserInfoMap({ commit }, userInfoMap) {
    commit('SET_USER_INFO_MAP', userInfoMap)
  },
  // 连续调用方法时会记录调用的参数，直到最后一次合并所有参数一起查询
  getDebounceUser({ state: { userMap } }, employeeNo) {
    debounceEmployeeNos += `,${employeeNo}`
    clearTimeout(debounceTimeoutId)
    debounceTimeoutId = setTimeout(async () => {
      const users = [...new Set(debounceEmployeeNos.slice(1).split(','))]

      const { data } = await getUsers(users)

      // 将请求回来的用户信息保存为映射信息
      data.forEach(({ employeeVo, userVo }) => {
        set(userMap, employeeVo.jobNumber, userVo.realName)
      })
    }, 300)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
