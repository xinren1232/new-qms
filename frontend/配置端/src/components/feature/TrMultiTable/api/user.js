import request from '../request'

const userCenterBaseUrl = `${process.env.VUE_APP_GATEWAY_URL}/uac-auth-service/v2/api/`

/**
 * 根据token查询用户信息
 */
export function getCurrentUser() {
  return request({
    url: `${userCenterBaseUrl}uac-auth/utoken/getUserInfo`,
    method: 'post'
  })
}
/**
 * 刷新token
 */
export function getRefreshToken(refreshToken) {
  if (!refreshToken) return Promise.reject(new Error(false))

  return request({
    url: `${userCenterBaseUrl}uac-auth/rtoken/get`,
    method: 'post',
    data: { utoken: refreshToken }
  })
}
/**
 * 根据工号批量查询用户信息
 * @param {*} employees 以逗号分隔
 */
export async function getUsers(employees) {
  if (!employees) return Promise.reject(new Error(false))
  const params = Array.isArray(employees) ? employees : employees.split(',')

  return request({
    url: process.env.VUE_APP_GATEWAY_URL + `service-ipm-project/plm/user/batchFindQuitUserDetailByNo`,
    method: 'post',
    data: params
  })
}
// 查询部门下的人
export const queryUserLitByDeptId = (data) => {
  return request({
    url: process.env.VUE_APP_GATEWAY_URL + '/uac-user-service/v2/api/uac-user/user/webPages',
    method: 'post',
    data
  })
}
