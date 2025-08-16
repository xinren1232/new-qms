const state = {
  objectData: {}, // 对象数据
  roleData: {}, // 角色数据
  stateData: {} // 状态数据
}

const mutations = {
  SET_OBJECT_DATA: (state, objectData) => {
    state.objectData = objectData

  },
  SET_ROLE_DATA: (state, roleData) => {
    state.roleData = roleData
  },
  SET_STATE_DATA: (state, stateData) => {
    state.stateData = stateData
  }
}

const actions = {
  setObjectData({ commit }, objectData) {
    commit('SET_OBJECT_DATA', objectData)
  },
  setRoleData({ commit }, roleData) {
    commit('SET_ROLE_DATA', roleData)
  },
  setStateData({ commit }, stateData) {
    commit('SET_STATE_DATA', stateData)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
