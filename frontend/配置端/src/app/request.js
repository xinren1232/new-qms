import axios from 'axios'
import { Message } from 'element-ui'

import { getRToken, getToken, getUserId, setToken } from '@/app/cookie'
import { toLogin } from '@/app/login'
import { getLanguage } from '@/i18n'
import router from '@/router'
import store from '@/store'

const pako = require('pako')

// 压缩数据
function compress(data) {
  // 先注释掉btoa()，暂时发现没什么意义
  return btoa(pako.gzip(data, { to: 'string' }))
}

axios.defaults.headers.common['P-LangId'] = getLanguage()
axios.defaults.headers.common['P-Auth'] = getToken()
axios.defaults.headers.common['P-Rtoken'] = getRToken()
axios.defaults.headers.common['P-AppId'] = process.env.VUE_APP_INNER_ID

const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API, // url = base url + request url
  withCredentials: true, // send cookies when cross-domain requests
  timeout: 1000 * 60 * 2, // request timeout
  transformRequest: [
    // 复用原有的转换，实现json --> json string
    axios.defaults.transformRequest[0],
    (data, header) => {
      const contentType = header['Content-Type']

      // 如果数据长度小于500KB,则不压缩
      if (contentType === 'application/json;charset=utf-8' && data && data.length > 1024 * 500) {
        // gzip压缩字符串 如果使用了btoa数据base64格式转码，给后端传一个标识base64,同步解码
        header['Content-Encoding'] = 'gzip,base64'

        return compress(data)
      } else {
        return data
      }
    }
  ]
})

// 请求前拦截
service.interceptors.request.use((config) => {
  config.headers.Authorization = config.headers['P-Auth'] = getToken()
  config.headers['P-Rtoken'] = getRToken()
  config.headers['P-EmpNo'] = getUserId()

  return config
}, Promise.reject)

// 返回结果前拦截
service.interceptors.response.use((response) => {
  const res = response.data

  if (!res) return
  // xhr 正常是 ok，sso 正常返回 0
  if (isTokenExpired(res.code)) {
    // 刷新token
    return store
      .dispatch('user/refreshToken')
      .then(({ data }) => {
        if (data.token || data.utoken || data.rtoken) {
          setToken(data.rtoken, data.utoken)
          // 重置请求中的token参数
          const params = response?.config.params || {}

          if (params) params.utoken = data.rtoken

          response.config.headers.Authorization = response.config.headers['P-Auth'] = data.rtoken
          response.config.headers['P-Rtoken'] = data.utoken

          return service(response.config)
        } else {
          doError({ message: '刷新token报错' })

          return Promise.reject(new Error('刷新token报错'))
        }

      })
      .catch(toLogin)
  } else if (isTokenInvalid(res.code)) {
    toLogin()
  } else if (res instanceof Blob) {
    // 导出文件时 TOKEN 过期处理
    const reader = new FileReader()

    reader.readAsText(res, 'utf-8')
    reader.onload = () => {
      if (reader.result.indexOf('code') > 0) {
        const CODE = JSON.parse(reader.result).code

        if (isTokenExpired(CODE)) toLogin()
        // 导出token过期处理
        else {
          Message.error({ message: JSON.parse(reader.result).message })
        } // 导出没有权限提示
      } else {
        // 获取excel 导出名称
        const name = response.headers['content-disposition']?.split("'")[2] ?? 'Excel导出'
        const fileName = name.split('.')[0]
        const fileType = name.split('.')[1] ?? 'xlsx'

        exportFileCallback(res, `${window.decodeURIComponent(fileName)}_${Date.now()}.${fileType}`)
      }
    }
  } else if (res.code !== '200' && res.code[0] !== '8' && res.code[0] !== 'E') {
    // E-***开头的都是流程业务 审批过程中异常，需要做特殊业务处理
    return doError(res)
  } else {
    return res
  }
}, doError)

function doError({ code, message, response,success }) {
  // 权限拦截
  if (code === '512') return router.push('/401')

  let _message = response?.data?.message ?? message

  if (_message?.charAt(_message.length - 1) === ';') {
    _message = _message.slice(0, _message.length - 1)
  }
  Message.warning(_message)

  return { code, message: _message, success }
}

const CODE = {
  OK: 200,
  TOKEN_INVALID: 2002, // Token 无效的情况
  TOKEN_EXPIRED: 30003, // token 已过期
  TOKEN_EMPTY: 30004, // token 为空
  REFRESH_TOKEN_EXPIRED: 30008, // Refresh Token已过期
  ERROR_TOKEN: 30009 // Token 错误
}

function isTokenExpired(code) {
  return +code === CODE.TOKEN_EXPIRED
}

function isTokenInvalid(code) {
  return [CODE.TOKEN_EMPTY, CODE.REFRESH_TOKEN_EXPIRED, CODE.ERROR_TOKEN, CODE.TOKEN_INVALID, 30010].includes(+code)
}

/** 下载文件-请求后处理函数 */
function exportFileCallback(blob, name) {
  const downloadElement = document.createElement('a')
  const href = window.URL.createObjectURL(blob) // 创建下载的链接

  downloadElement.href = href
  downloadElement.download = name // 下载后文件名
  document.body.appendChild(downloadElement)
  downloadElement.click() // 点击下载
  document.body.removeChild(downloadElement) // 下载完成移除元素
  window.URL.revokeObjectURL(href) // 释放掉blob对象
}

export default service
