import { getLanguage } from '@/i18n'
import event from '@/utils/event'
import { combineRequest } from '@/utils'
import { queryDictItemByCodes } from '@/api/dict'

const set = event.$set
// 缓存的字典map

const cacheDictMap = {}
// 系统初始化 需要处理的字典集合

const initDictList = []

// 取值时，语言映射的字段
const valueFieldMap = {
  en: 'en',
  zh: 'zh'
}
// 字典-远程数据源
const state = initState(initDictList)

const mutations = {
  SET_DICT: (state, params) => {
    const lang = getLanguage().split('-')[0]
    const map = {}

    // 动态添加字典项
    set(
      state,
      params.type,
      params.data.map((item) => {
        // 处理多语言
        const value = item[valueFieldMap[lang]]

        value && (item.value = value)
        // map结构，用于key->value转换
        map[item.keyCode] = value

        return item
      })
    )
    state[`${params.type}_MAP`] = map
  }
}

const actions = {
  /**
   * 返回字典，里面包含数组结构和map结构
   * @return  {list, map}
   */
  queryDict({ commit, state }, types) {
    return queryDict({ commit, state }, queryDictItemByCodes, types)
  },
  /** 返回数组结构的字典，用于下拉框等组件的数据源 */
  queryDictList({ commit, state }, types) {
    return queryDict({ commit, state }, queryDictItemByCodes, types).then(({ list }) => list)
  },
  /** 返回map结构的字典，用于表格等组件做value->label映射 */
  queryDictMap({ commit, state }, types) {
    return queryDict({ commit, state }, queryDictItemByCodes, types).then(({ map }) => map)
  },
  setDict({ commit }, { code, values }) {
    commit('SET_DICT', { type: code, data: values })
  },
  // 设置合成编码规则
  setEncode({ commit }, params) {
    commit('SET_ENCODE', params)
  }
}
let dictRequest = null

function queryDict({ commit, state }, httpRequest, params) {
  const doQuery = (typesStrs, { commit, state }) => {
    return new Promise((resolve) => {
      // ①筛选【未查询过】的字典类型
      const queryTypes = []
      let types = typesStrs.join(',').split(',')

      types = Array.from(new Set(types))
      types.forEach((type) => {
        if (type && !cacheDictMap[type]) {
          queryTypes.push(type)
        }
      })
      if (queryTypes.length) {
        // ②只查询【未查询过】的数据字典
        httpRequest(queryTypes.join(',')).then(({ data }) => {
          if (data) {
            data.forEach((item) => {
              cacheDictMap[item.code] = true
              commit('SET_DICT', { type: item.code, data: item.dictionaryItems })// .filter(o => o.enableFlag !== 0) })
            })
          }
          resolve(getResult(state, types))
        })
      } else {
        // ③若需要的字典都已查询过，则直接返回缓存数据
        resolve(getResult(state, types))
      }
    })
  }

  // 请求合并
  if (!dictRequest) dictRequest = combineRequest(doQuery, { commit, state })

  return dictRequest(params)
}

function initState(initDictList) {
  return initDictList.reduce((prev, next) => {
    prev[next] = []
    prev[next + '_MAP'] = {}

    return prev
  }, {})
}

function getResult(state, types) {
  const map = {}
  const list = {}

  types.forEach((type) => {
    map[type] = state[`${type}_MAP`]
    list[type] = state[type]
  })

  return { list, map }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
