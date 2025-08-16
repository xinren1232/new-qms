import request from '@/app/request'

const BASE_URL = process.env.VUE_APP_CONFIG_CENTER


export const releaseProperties = (modelCode,data) => {
  return request({
    url: `${BASE_URL}/object-table/publish/${modelCode}`,
    method: 'post',
    data
  })
}
