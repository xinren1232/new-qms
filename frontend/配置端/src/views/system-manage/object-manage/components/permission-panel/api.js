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
export function addUpdateObjectPermission(data) {
  return request({
    url: BASE_URL + 'permission/saveOrUpdateObjPermission',
    method: 'post',
    data
  })
}
// 删除对象绑定的操作权限
export function deleteObjectBindOperationPermission(bid) {
  return request({
    url: BASE_URL + `object/permission/logicalDelete/${bid}`,
    method: 'post'
  })
}

// 查询对象绑定的操作权限
export function queryObjectBindOperationPermission(modelCode) {
  return request({
    url: BASE_URL + `permission/listObjPermissions/${modelCode}`,
    method: 'get'
  })
}

// 删除premissionBid
export function deletePermission(modelCode,permissionBid) {
  return request({
    url: BASE_URL + `permission/deleteByPermissionBid/${modelCode}/${permissionBid}`,
    method: 'post'
  })
}
// 下发对象权限APP
export function distributeAppPermission(modelCode) {
  return request({
    url: BASE_URL + `permission/distributePermission/${modelCode}`,
    method: 'post'
  })
}
// 下发对象权限到子类
export function distributeObjectPermission(modelCode) {
  return request({
    url: BASE_URL + `permission/distributeObjectPermission/${modelCode}`,
    method: 'post'
  })
}
