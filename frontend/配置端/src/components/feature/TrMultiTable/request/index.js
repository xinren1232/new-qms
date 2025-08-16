import axios from 'axios'
import Cookies from 'js-cookie'

const appCode = process.env.VUE_APP_CODE.toLowerCase()
const TOKEN_KEY = `${appCode}-token`
const RTOKEN_KEY = `${appCode}-rtoken`

export function getCookie(key) {
  return Cookies.get(key)
}

function getToken() {
  return getCookie(TOKEN_KEY)
}

function getRToken() {
  return getCookie(RTOKEN_KEY)
}

const pako = require('pako')

// 压缩数据
function compress(data) {
  // 先注释掉btoa()，暂时发现没什么意义
  return btoa(pako.gzip(data, { to: 'string' }))
}

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

  return config
}, Promise.reject)

// 返回结果前拦截
service.interceptors.response.use((response) => {

  return response.data

}, doError)

function doError({ message }) {
  throw new Error(message)
}


export default service
