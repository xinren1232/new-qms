import request from '@/app/request'

const BASE_URL = process.env.VUE_APP_CONFIG_CENTER

// 分页查询列表
export const pageQuery = (params) => {
  return request({
    url: BASE_URL + 'language/page',
    method: 'post',
    data: params
  })
}

// 新增,修改单条属性
export const createOrUpdate = (params) => {
  return request({
    url: BASE_URL + 'language/saveOrUpdate',
    method: 'post',
    data: params
  })
}
// 删除单条属性
export const deleteItem = bid => {
  return request({
    url: BASE_URL + `language/logicalDelete/${bid}`,
    method: 'post'
  })
}

// 查看国际化版本
export const queryLangVersion = () => {
  return request({
    url: BASE_URL + 'language/version/get',
    method: 'get'
  })
}
