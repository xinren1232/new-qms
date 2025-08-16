/*
 * @Author: BanLi
 * @Date: 2021-06-25 10:53:45
 * @LastEditTime: 2021-06-25 16:17:09
 * @Description: 项目权限相关
 */

import request from '@/app/request'

const BASE_URL = process.env.VUE_APP_BASE_API_2

// 获取角色是否为超级管理员
export const getUserIsSuperAdmin = (params) => {
  return request.get(BASE_URL + 'ipm/permission/checkSupperAdministrator', { params })
}

// 获取角色是否为域管理员
export const getUserIsDomainAdmin = (domainType, domainBid) => {
  return request.get(BASE_URL + `ipm/domain/${domainType}/${domainBid}/checkInsAdministrator`)
}

// 获取 项目、存储域 是否能查看/编辑的权限
export const getDomainDataList = (domainType) => {
  return request.get(BASE_URL + `ipm/permission/operation/${domainType}`)
}

// 获取 项目、存储域 是否能查看/编辑的权限
export const getDomainDataItem = ({ bid, domainType }) => {
  return request.get(BASE_URL + `ipm/permission/operation/${domainType}/${bid}`)
}

// 获取创建者、所有者的实例权限
export const getCreatorAndOwner = (objType) => {
  return request.get(BASE_URL + `ipm/permission/object/${objType}/getGlobalObjectPermissions`)
}

// 获取是否可以创建的操作权限
export const getCreatePermission = (domainType) => {
  return request.get(BASE_URL + `ipm/permission/${domainType}/findCreateObjBid`)
}

// // 获取可创建的模板列表
// export const getCreateTemplates = (domainType) => {
//   return request.get(BASE_URL + `ipm/permission/${domainType}/findCreateObjBid`)
// }

// 获取关联对象创建类型权限
export const getAssociatedObject = ({ domainBid, domainType, objType }) => {
  return request.get(BASE_URL + `ipm/permission/${domainType}/${domainBid}/${objType}/findCreateObjBid`)
}

// 获取关联对象的操作权限
export const getAssociatedOperation = ({ domainBid, domainType, objType }) => {
  return request.get(BASE_URL + `ipm/permission/operation/${domainType}/${domainBid}/${objType}`)
}
