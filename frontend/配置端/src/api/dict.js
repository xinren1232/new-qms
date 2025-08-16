import request from '@/app/request'

/** 每个系统的字典接口应该只有一个 */
export function getDictByType(code) {
  return request({
    url: process.env.VUE_APP_DICT_API + 'base/dictionaries',
    method: 'get',
    params: { codes: code, appCode: 'ipm', state: 'enable' }
  })
}

// 获取字典列表
export function getDictList() {
  return request({
    url: process.env.VUE_APP_DICT_API + 'base/dictionary/page',
    method: 'post',
    data: {
      current: 1,
      param: { refAppCode: 'IPM', state: 'enable' },
      size: 9999
    }
  })
}

// =====================NEW =====================
// 新增字典
// const addDict = (data) => {
//   return request({
//     url: process.env.VUE_APP_CONFIG_CEBTER + 'manager/cfg/dictionary/add',
//     method: 'post',
//     data
//   })
// }

export const queryDictItems = (dictCode) => {
  return request({
    url: process.env.VUE_APP_CONFIG_CENTER + '/dictionary/listDictionaryAndItemByCodesAndEnableFlags',
    method: 'post',
    data: {
      codes: Array.isArray(dictCode) ? dictCode : [dictCode]
    }
  })
}

export const queryDictItemByCodes = (dictCode) => {
  return request({
    url: process.env.VUE_APP_CONFIG_CENTER + '/dictionary/listDictionaryAndItemByCodesAndEnableFlags',
    method: 'post',
    data: {
      codes: Array.isArray(dictCode) ? dictCode : [dictCode]
    }
  })
}
