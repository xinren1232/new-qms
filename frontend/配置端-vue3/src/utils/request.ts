import axios, { AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getToken, getRToken, getUserId, setToken } from '@/utils/auth'
import { getLanguage } from '@/i18n'

// 响应数据类型定义
export interface ApiResponse<T = any> {
  code: string | number
  message: string
  data: T
  success: boolean
}

// 请求配置类型
export interface RequestConfig extends AxiosRequestConfig {
  skipErrorHandler?: boolean
  skipAuth?: boolean
}

// 设置默认请求头
axios.defaults.headers.common['P-LangId'] = getLanguage()
axios.defaults.headers.common['P-Auth'] = getToken()
axios.defaults.headers.common['P-Rtoken'] = getRToken()
axios.defaults.headers.common['P-AppId'] = import.meta.env.VITE_APP_INNER_ID

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  withCredentials: true,
  timeout: 1000 * 60 * 2, // 2分钟超时
  transformRequest: [
    // 复用原有的转换，实现json --> json string
    ...(axios.defaults.transformRequest as any[]),
    (data: any, headers: any) => {
      const contentType = headers['Content-Type']

      // 如果数据长度小于500KB,则不压缩
      if (contentType === 'application/json;charset=utf-8' && data && data.length > 1024 * 500) {
        // gzip压缩字符串
        headers['Content-Encoding'] = 'gzip,base64'
        return data
      } else {
        return data
      }
    }
  ]
})

// 请求拦截器
service.interceptors.request.use(
  (config: RequestConfig) => {
    // 如果不跳过认证，添加token
    if (!config.skipAuth) {
      config.headers = config.headers || {}
      config.headers.Authorization = config.headers['P-Auth'] = getToken()
      config.headers['P-Rtoken'] = getRToken()
      config.headers['P-EmpNo'] = getUserId()
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data

    if (!res) return response

    // 检查token是否过期
    if (isTokenExpired(res.code)) {
      // 刷新token
      const userStore = useUserStore()
      return userStore
        .refreshToken()
        .then((tokenData: any) => {
          if (tokenData.token || tokenData.utoken || tokenData.rtoken) {
            setToken(tokenData.rtoken, tokenData.utoken)

            // 重置请求中的token参数
            const params = response?.config.params || {}
            if (params) params.utoken = tokenData.rtoken

            response.config.headers = response.config.headers || {}
            response.config.headers.Authorization = response.config.headers['P-Auth'] = tokenData.rtoken
            response.config.headers['P-Rtoken'] = tokenData.utoken

            return service(response.config)
          } else {
            doError({ code: 'TOKEN_REFRESH_ERROR', message: '刷新token报错' })
            return Promise.reject(new Error('刷新token报错'))
          }
        })
        .catch(() => {
          toLogin()
        })
    } else if (isTokenInvalid(res.code)) {
      toLogin()
    } else if (res instanceof Blob) {
      // 导出文件时 TOKEN 过期处理
      const reader = new FileReader()
      reader.readAsText(res, 'utf-8')
      reader.onload = () => {
        if (reader.result && typeof reader.result === 'string' && reader.result.indexOf('code') > 0) {
          const result = JSON.parse(reader.result)
          const CODE = result.code

          if (isTokenExpired(CODE)) {
            toLogin()
          } else {
            ElMessage.error(result.message)
          }
        } else {
          // 获取excel 导出名称
          const name = response.headers['content-disposition']?.split("'")[2] ?? 'Excel导出'
          const fileName = name.split('.')[0]
          const fileType = name.split('.')[1] ?? 'xlsx'

          exportFileCallback(res, `${window.decodeURIComponent(fileName)}_${Date.now()}.${fileType}`)
        }
      }
    } else if (res.code !== '200' && res.code !== 200 && !String(res.code).startsWith('8') && !String(res.code).startsWith('E')) {
      // E-***开头的都是流程业务 审批过程中异常，需要做特殊业务处理
      return doError(res)
    } else {
      return res
    }
  },
  (error) => {
    return doError(error)
  }
)

// 错误处理函数
function doError(error: any): any {
  const { code, message, response } = error

  // 权限拦截
  if (code === '512') {
    const router = useRouter()
    router.push('/401')
    return
  }

  let _message = response?.data?.message ?? message

  if (_message?.charAt(_message.length - 1) === ';') {
    _message = _message.slice(0, _message.length - 1)
  }

  ElMessage.warning(_message || '请求失败')

  return { code, message: _message, success: false }
}

// 状态码定义
const CODE = {
  OK: 200,
  TOKEN_INVALID: 2002, // Token 无效的情况
  TOKEN_EXPIRED: 30003, // token 已过期
  TOKEN_EMPTY: 30004, // token 为空
  REFRESH_TOKEN_EXPIRED: 30008, // Refresh Token已过期
  ERROR_TOKEN: 30009 // Token 错误
}

// 检查token是否过期
function isTokenExpired(code: string | number): boolean {
  return +code === CODE.TOKEN_EXPIRED
}

// 检查token是否无效
function isTokenInvalid(code: string | number): boolean {
  return [CODE.TOKEN_EMPTY, CODE.REFRESH_TOKEN_EXPIRED, CODE.ERROR_TOKEN, CODE.TOKEN_INVALID, 30010].includes(+code)
}

// 跳转到登录页
function toLogin(): void {
  const router = useRouter()
  router.push('/login')
}

// 下载文件处理函数
function exportFileCallback(blob: Blob, name: string): void {
  const downloadElement = document.createElement('a')
  const href = window.URL.createObjectURL(blob)

  downloadElement.href = href
  downloadElement.download = name
  document.body.appendChild(downloadElement)
  downloadElement.click()
  document.body.removeChild(downloadElement)
  window.URL.revokeObjectURL(href)
}

export default service
