import request from '@/app/request'
// import { dfs } from '@/utils'

const BASE_URL = process.env.VUE_APP_CONFIG_CENTER

// 保存对象绑定的生命周期信息
export const saveLifecycleTemplateForObject = (params) => {
  return request.post(BASE_URL + 'object/life-cycle/saveObjectLifeCycle', params)
}

// 查询对象绑定的生命周期信息
export const queryLifecycleTemplateForObject = (modelCode) => {
  return request.get(BASE_URL + `object/life-cycle/listByModelCode/${modelCode}`)
}
