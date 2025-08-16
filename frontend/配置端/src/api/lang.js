import request from '@/app/request'

const BASE_API = process.env.VUE_APP_BASE_API_2

// 新增国际化
export const addLang = (code, params) => {
  return request.post(BASE_API + `ipm/multi-language/editSave/code/${code}`, params)
}

// 编辑国际化
export const editLang = (code, params) => {
  return request.post(BASE_API + `ipm/multi-language/editSave?code=${code}`, params)
}

// 查询国际化翻译值
export const getLangByType = (params) => {
  return request.post(BASE_API + 'ipm/multi-language/find', params)
}
// 查询国际化翻译值
export const queryLangByType = (params) => {
  return request.post(BASE_API + 'ipm/multi-language/query', params)
}
// 查询国际化列表
export const getLang = () => {
  return request.post(BASE_API + 'ipm/multi-language/findType', { appId: 'IPM' })
}
