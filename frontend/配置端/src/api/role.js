import Cookies from '@/app/cookie'
import request from '@/app/request'

// 后台接口 根据应用获取用户或角色已授权应用资源
export function getPermissionMenu(jobNumber) {
  const params = {
    appCode: process.env.VUE_APP_PERMISSION_APP_CODE, // 应用编码
    jobNumber, // 账号
    password: process.env.VUE_APP_PERMISSION_APP_KEY, // 应用key
    lang: Cookies.get('language') || 'zh' // 语言标记
  }

  return request({
    baseURL: process.env.VUE_APP_GATEWAY_URL,
    url: 'service-unified-permission/unified-permission/IscAuth/getUiRe4Out',
    method: 'post',
    data: params
  })
}
// 功能权限-根据角色ID获取用户列表路径
export function getRoleUser4Out(roleCode) {
  const params = {
    roleCode,
    appCode: process.env.VUE_APP_PERMISSION_APP_CODE, // 应用编码
    password: process.env.VUE_APP_PERMISSION_APP_KEY, // 应用key
    lang: Cookies.get('language') || 'zh' // 语言标记
  }

  return request({
    baseURL: process.env.VUE_APP_GATEWAY_URL,
    url: 'service-unified-permission/unified-permission/IscAuth/getRoleUser4Out',
    method: 'post',
    data: params
  })
}
