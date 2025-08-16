import request from '@/utils/request'
import type { UserInfo } from '@/stores/user'

const userCenterBaseUrl = `${import.meta.env.VITE_APP_GATEWAY_URL}/uac-auth-service/v2/api/`

// 用户相关接口参数类型定义
export interface LoginCredentials {
  username: string
  password: string
}

export interface RefreshTokenRequest {
  utoken: string
}

export interface UserQueryParams {
  deptId?: string
  pageNum?: number
  pageSize?: number
  keyword?: string
}

export interface UserDetailResponse {
  employeeVo: {
    jobNumber: string
    name: string
    deptId: string
    deptName: string
  }
  userVo: {
    realName: string
    email: string
    phone: string
  }
}

/**
 * 根据token查询用户信息
 */
export function getCurrentUser(token?: string): Promise<{ data: UserInfo }> {
  return request({
    url: `${userCenterBaseUrl}uac-auth/utoken/getUserInfo`,
    method: 'post',
    data: token ? { token } : undefined
  })
}

/**
 * 刷新token
 */
export function getRefreshToken(refreshToken: string): Promise<{ data: { token: string; utoken: string; rtoken: string } }> {
  if (!refreshToken) {
    return Promise.reject(new Error('Refresh token is required'))
  }

  return request({
    url: `${userCenterBaseUrl}uac-auth/rtoken/get`,
    method: 'post',
    data: { utoken: refreshToken }
  })
}

/**
 * 根据工号批量查询用户信息
 * @param employees 工号数组或以逗号分隔的字符串
 */
export async function getUsers(employees: string[] | string): Promise<{ data: UserDetailResponse[] }> {
  if (!employees) {
    return Promise.reject(new Error('Employees parameter is required'))
  }
  
  const params = Array.isArray(employees) ? employees : employees.split(',')

  return request({
    url: import.meta.env.VITE_APP_GATEWAY_URL + '/service-ipm-project/plm/user/batchFindQuitUserDetailByNo',
    method: 'post',
    data: params
  })
}

/**
 * 查询部门下的用户
 */
export function queryUserListByDeptId(data: UserQueryParams): Promise<{ data: UserInfo[] }> {
  return request({
    url: import.meta.env.VITE_APP_GATEWAY_URL + '/uac-user-service/v2/api/uac-user/user/webPages',
    method: 'post',
    data
  })
}

/**
 * 用户登录
 */
export function login(credentials: LoginCredentials): Promise<{ data: { token: string; rtoken: string; user: UserInfo } }> {
  return request({
    url: `${userCenterBaseUrl}uac-auth/login`,
    method: 'post',
    data: credentials
  })
}

/**
 * 用户登出
 */
export function logout(): Promise<{ data: any }> {
  return request({
    url: `${userCenterBaseUrl}uac-auth/logout`,
    method: 'post'
  })
}

/**
 * 获取用户权限
 */
export function getUserPermissions(userId: string): Promise<{ data: string[] }> {
  return request({
    url: `${userCenterBaseUrl}uac-auth/permissions/${userId}`,
    method: 'get'
  })
}

/**
 * 获取用户角色
 */
export function getUserRoles(userId: string): Promise<{ data: string[] }> {
  return request({
    url: `${userCenterBaseUrl}uac-auth/roles/${userId}`,
    method: 'get'
  })
}

/**
 * 更新用户信息
 */
export function updateUserInfo(userId: string, userInfo: Partial<UserInfo>): Promise<{ data: UserInfo }> {
  return request({
    url: `${userCenterBaseUrl}uac-user/user/${userId}`,
    method: 'put',
    data: userInfo
  })
}

/**
 * 修改密码
 */
export function changePassword(data: { oldPassword: string; newPassword: string }): Promise<{ data: any }> {
  return request({
    url: `${userCenterBaseUrl}uac-auth/password/change`,
    method: 'post',
    data
  })
}

/**
 * 重置密码
 */
export function resetPassword(userId: string): Promise<{ data: any }> {
  return request({
    url: `${userCenterBaseUrl}uac-auth/password/reset/${userId}`,
    method: 'post'
  })
}

/**
 * 获取用户头像
 */
export function getUserAvatar(userId: string): Promise<{ data: string }> {
  return request({
    url: `${userCenterBaseUrl}uac-user/user/${userId}/avatar`,
    method: 'get'
  })
}

/**
 * 上传用户头像
 */
export function uploadUserAvatar(userId: string, file: File): Promise<{ data: string }> {
  const formData = new FormData()
  formData.append('avatar', file)
  
  return request({
    url: `${userCenterBaseUrl}uac-user/user/${userId}/avatar`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
