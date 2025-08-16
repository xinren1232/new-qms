import request from '../request'

const BASE_URL = process.env.VUE_APP_CONFIG_CENTER

// 查询角色树
export const getRoleTree = () => {
  return request({
    url: `${BASE_URL}role/tree`,
    method: 'get',
    data: {}
  })
}
// 查询对象树
export const queryObjectTree = () => {
  return request({
    url: BASE_URL + '/object/treeAndLockInfo',
    method: 'get'
  })
}
