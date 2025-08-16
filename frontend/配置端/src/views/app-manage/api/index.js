import request from '@/app/request'

const BASE_URL = process.env.VUE_APP_CONFIG_CENTER

// 查询角色树
export const getRoleTree = () => {
  return request({
    url: `${BASE_URL}menu-app/tree`,
    method: 'get',
    data: {}
  })
}

// 查询全量角色数据，业务角色+系统角色
export const getAllRoleTree = () => {
  return request({
    url: `${BASE_URL}menu-app/treeAndSystem`,
    method: 'get'
  })
}

// 新增编辑角色
export const addOrEditRole = (data) => {
  return request({
    url: `${BASE_URL}menu-app/saveOrUpdate`,
    method: 'post',
    data
  })
}
// 删除角色
export const deleteRole = (bid) => {
  return request({
    url: `${BASE_URL}menu-app/logicalDelete/${bid}`,
    method: 'post'
  })
}
// 更新角色状态
export const updateRoleStatus = ({ bid, enableFlag }) => {
  return request({
    url: `${BASE_URL}menu-app/changeEnableFlag/${bid}/${enableFlag}`,
    method: 'post'
  })
}
