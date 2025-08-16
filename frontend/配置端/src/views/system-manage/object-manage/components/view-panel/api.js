import request from '@/app/request'

const BASE_URL = process.env.VUE_APP_CONFIG_CENTER


// 查询操作权限，入参为基类Code baseModel
export function queryOperationPermission(baseModel) {
  return request({
    url: BASE_URL + `object/operation/listByBaseModel/${baseModel}`,
    method: 'get'
  })
}

// 提交对象绑定的操作权限
export function objectBindView(data) {
  return request({
    url: BASE_URL + 'object/viewRule/saveOrUpdate',
    method: 'post',
    data
  })
}

// 查询对象绑定的操作权限
export function queryObjectBindViews(modelCode) {
  return request({
    url: BASE_URL + `object/viewRule/listAndViewInfoByModelCode/${modelCode}`,
    method: 'get'
  })
}
