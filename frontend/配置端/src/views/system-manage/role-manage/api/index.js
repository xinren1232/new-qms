import request from '@/app/request'

const BASE_URL = process.env.VUE_APP_CONFIG_CENTER

// 查询角色树

export const getRoleTree = () => {
  return request({
    url: `${BASE_URL}role/tree`,
    method: 'get',
    data: {}
  })
}

// 查询全量角色数据，业务角色+系统角色
export const getAllRoleTree = () => {
  return request({
    url: `${BASE_URL}role/treeAndSystem`,
    method: 'get'
  })
}

// 新增编辑角色
export const addOrEditRole = (data) => {
  return request({
    url: `${BASE_URL}role/saveOrUpdate`,
    method: 'post',
    data
  })
}
// 删除角色
export const deleteRole = (bid) => {
  return request({
    url: `${BASE_URL}role/logicalDelete/${bid}`,
    method: 'post'
  })
}
// 更新角色状态
export const updateRoleStatus = ({ bid, enableFlag }) => {
  return request({
    url: `${BASE_URL}role/changeEnableFlag/${bid}/${enableFlag}`,
    method: 'post'
  })
}

// 角色添加成员
export const addRoleMember = (data) => {
  return request({
    url: `${BASE_URL}/role/addUsers`,
    method: 'post',
    data
  })
}

// 查询角色成员
export const getRoleMemberByCode = (roleCode) => {
  return request({
    url: `${BASE_URL}role/getUsersByRoleCode/${roleCode}`,
    method: 'get'
  })
}

// 更新角色成员
export const updateRoleMember = (roleCode, params) => {
  return request({
    url: `${BASE_URL}role/deleteAndAddUsers/${roleCode}`,
    method: 'post',
    data: params
  })
}
