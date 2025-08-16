import request from '@/app/request'

const UC_URL = process.env.VUE_APP_GATEWAY_URL
const PLM_URL = process.env.VUE_APP_PLM
const IPM_URL = process.env.VUE_APP_BASE_API

const URL = {
  // 根据工号查询用户(根据工号批量查询)
  queryUserInfoByEmployees: IPM_URL + 'plm/user/batchFindQuitUserDetailByNo',
  // 模糊查询用户
  queryUserInfo: UC_URL + 'uac-user-service/v2/api/uac-user/user/webPages',
  // 获取所有根部门
  queryRootDeptList: UC_URL + 'uac-org-service/v2/api/uac-org/dept/webChildDepts',
  // 按部门Id查询部门下子部门和人员
  queryChildDeptAndEmployee: UC_URL + 'uac-org-service/v2/api/uac-org/dept/webChildDepts',
  // 根据部门id查询该部门下所有人员
  listEmployeeByDeptId: UC_URL + 'uac-user-service/v2/api/uac-user/user/webPages',
  batchQueryDeptById: UC_URL + 'uac-org-service/v2/api/uac-org/dept/batchQueryDeptByIds',
  // 查询角色Tree
  queryRoleTree: PLM_URL + 'role/tree',
  // 获取对象树
  queryObjectTree: PLM_URL + 'object/tree'
}

// 查询部门下的人
export const queryUserLitByDeptId = (data) => {
  return request({
    url: URL.listEmployeeByDeptId,
    method: 'post',
    data
  })
}

// 获取所有根部门
export const queryRootDeptList = () => {
  return request({
    url: URL.queryRootDeptList,
    method: 'post'
  })
}

// 按部门Id查询部门下子部门和人员
export const queryChildDeptAndEmployee = (data) => {
  return request({
    url: URL.queryChildDeptAndEmployee,
    method: 'post',
    data
  })
}

// 批量查询部门信息
export const batchQueryDeptById = (data) => {
  return request({
    url: URL.batchQueryDeptById,
    method: 'post',
    data: { deptIds: data }
  })
}
