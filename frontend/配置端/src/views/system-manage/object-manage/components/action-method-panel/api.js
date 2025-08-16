import request from '@/app/request'

const BASE_URL = process.env.VUE_APP_CONFIG_CENTER


// 新增或者修改绑定的事件方法
export const createOrUpdateActionMethod = (params) => {
  return request.post(BASE_URL + 'object/eventMethod/saveOrUpdate', params)
}

// 查询绑定的事件方法
export const queryActionMethod = (params) => {
  return request.post(BASE_URL + 'object/eventMethod/page', params)
}

// 删除绑定的事件方法
export const deleteActionMethod = (bid) => {
  return request.post(BASE_URL + `object/eventMethod/logicalDelete/${bid}`)
}
