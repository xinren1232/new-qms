import request from '@/app/request'

const BASE_URL = process.env.VUE_APP_CONFIG_CENTER

export const exportExcel = (title = '',beanName = '',param = {}) => {
  return request({
    url: BASE_URL + 'exportExcel',
    method: 'post',
    responseType: 'blob',
    data: { title,beanName,param }
  })
}
