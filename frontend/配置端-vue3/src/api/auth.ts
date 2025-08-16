/*
 * @Author: BanLi
 * @Date: 2021-06-25 10:53:45
 * @LastEditTime: 2025-08-03 16:40:00
 * @Description: 项目权限相关API - Vue3版本
 */

import request from '@/utils/request'

const BASE_URL = import.meta.env.VITE_APP_BASE_API_2

// 权限相关接口参数类型定义
export interface PermissionParams {
  [key: string]: any
}

export interface DomainPermissionParams {
  bid: string
  domainType: string
}

export interface AssociatedObjectParams {
  domainBid: string
  domainType: string
  objType: string
}

// 获取角色是否为超级管理员
export const getUserIsSuperAdmin = (params: PermissionParams) => {
  return request.get(BASE_URL + 'ipm/permission/checkSupperAdministrator', { params })
}

// 获取角色是否为域管理员
export const getUserIsDomainAdmin = (domainType: string, domainBid: string) => {
  return request.get(BASE_URL + `ipm/domain/${domainType}/${domainBid}/checkInsAdministrator`)
}

// 获取 项目、存储域 是否能查看/编辑的权限
export const getDomainDataList = (domainType: string) => {
  return request.get(BASE_URL + `ipm/permission/operation/${domainType}`)
}

// 获取 项目、存储域 是否能查看/编辑的权限
export const getDomainDataItem = ({ bid, domainType }: DomainPermissionParams) => {
  return request.get(BASE_URL + `ipm/permission/operation/${domainType}/${bid}`)
}

// 获取创建者、所有者的实例权限
export const getCreatorAndOwner = (objType: string) => {
  return request.get(BASE_URL + `ipm/permission/object/${objType}/getGlobalObjectPermissions`)
}

// 获取是否可以创建的操作权限
export const getCreatePermission = (domainType: string) => {
  return request.get(BASE_URL + `ipm/permission/${domainType}/findCreateObjBid`)
}

// 获取关联对象创建类型权限
export const getAssociatedObject = ({ domainBid, domainType, objType }: AssociatedObjectParams) => {
  return request.get(BASE_URL + `ipm/permission/${domainType}/${domainBid}/${objType}/findCreateObjBid`)
}

// 获取关联对象的操作权限
export const getAssociatedOperation = ({ domainBid, domainType, objType }: AssociatedObjectParams) => {
  return request.get(BASE_URL + `ipm/permission/operation/${domainType}/${domainBid}/${objType}`)
}
