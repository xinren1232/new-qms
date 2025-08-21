/**
 * axios请求封装
 */

import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const service = axios.create({
  baseURL: '', // 开发使用Vite代理，生产由Nginx/网关统一转发
  timeout: 0, // 解除超时限制，支持长时间AI响应
  retry: 2, // 重试次数
  retryDelay: 1000 // 重试延迟
})

// 避免重复弹出未授权提示
let authNotified = false;

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 添加token到请求头
    const token = localStorage.getItem('qms_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // 添加用户ID到请求头（支持匿名用户）
    const userId = localStorage.getItem('qms_user_id') || 'anonymous'
    config.headers['user-id'] = userId

    // 添加其他用户信息到请求头（对中文进行编码）
    const userName = localStorage.getItem('qms_user_name') || '匿名用户'
    const userEmail = localStorage.getItem('qms_user_email') || 'anonymous@qms.com'
    const userDepartment = localStorage.getItem('qms_user_department') || '质量管理部'
    const userRole = localStorage.getItem('qms_user_role') || 'USER'

    // 使用encodeURIComponent对可能包含中文的字段进行编码
    config.headers['user-name'] = encodeURIComponent(userName)
    config.headers['user-email'] = userEmail
    config.headers['user-department'] = encodeURIComponent(userDepartment)
    config.headers['user-role'] = userRole

    // 添加时间戳防止缓存
    if (config.method === 'get') {
      config.params = {
        ...config.params,
        _t: Date.now()
      }
    }

    console.log('🚀 发送请求:', config.url, config.data || config.params)
    return config
  },
  error => {
    console.error('❌ 请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    console.log('✅ 收到响应:', response.config.url, response.data)
    
    const res = response.data
    
    // 如果是文件下载，直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    
    // 统一处理响应格式
    if (res.success !== undefined) {
      // 标准格式: { success, message, data }
      if (res.success === true) {
        return res
      } else {
        // 业务错误
        ElMessage.error(res.message || '请求失败')
        return Promise.reject(new Error(res.message || '请求失败'))
      }
    } else if (res.code !== undefined) {
      // 兼容格式: { code, message, data }
      if (res.code === 200) {
        return res
      } else {
        // 业务错误
        ElMessage.error(res.message || '请求失败')
        return Promise.reject(new Error(res.message || '请求失败'))
      }
    } else {
      // 非标准格式，直接返回
      return res
    }
  },
  error => {
    console.error('❌ 响应错误:', error)

    const config = error.config

    // 重试逻辑
    if (config && config.retry && config.retryCount < config.retry) {
      config.retryCount = config.retryCount || 0
      config.retryCount++

      console.log(`🔄 重试请求 (${config.retryCount}/${config.retry}):`, config.url)

      // 延迟重试
      return new Promise(resolve => {
        setTimeout(() => {
          resolve(service(config))
        }, config.retryDelay || 1000)
      })
    }

    let message = '网络错误'

    if (error.response) {
      const { status, data } = error.response

      switch (status) {
        case 401:
          message = '未授权，请重新登录'
          // 清除本地存储的用户信息
          localStorage.removeItem('qms_token')
          localStorage.removeItem('qms_user')
          // 跳转到登录页
          if (window.location.pathname !== '/login') {
            window.location.href = '/login'
          }
          break
        case 403:
          message = '权限不足'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = data?.message || `请求失败 (${status})`
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时'
    } else if (error.message.includes('Network Error')) {
      message = '网络连接失败'
    }

    // 401/403 兜底与导航
    if (error.response?.status === 401) {
      if (!authNotified && window.location.pathname !== '/login') {
        authNotified = true
        ElMessage.error('未登录或登录已过期，请重新登录')
        const redirect = encodeURIComponent(window.location.pathname + window.location.search)
        setTimeout(() => {
          window.location.href = `/login?redirect=${redirect}`
          authNotified = false
        }, 300)
      }
    } else if (error.response?.status === 403) {
      ElMessage.error('权限不足，无法访问该功能')
    } else {
      // 只在最后一次重试失败时显示错误消息
      if (!config || !config.retry || config.retryCount >= config.retry) {
        ElMessage.error(message)
      }
    }

    return Promise.reject(error)
  }
)

export default service
