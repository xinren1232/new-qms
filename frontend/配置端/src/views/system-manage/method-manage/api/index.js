import request from '@/app/request'

const BASE_URL = process.env.VUE_APP_CONFIG_CENTER

// 分页查询列表
export const pageQuery = (params) => {
  return request({
    url: BASE_URL + '/method/page',
    method: 'post',
    data: params
  })
}

// 新增,修改单条属性
export const createOrUpdateAttribute = (params) => {
  return request({
    url: BASE_URL + '/method/saveOrUpdate',
    method: 'post',
    data: params
  })
}
// 批量新增属性，导出
export const batchCreateAttribute = (params) => {
  return request({
    url: BASE_URL + '/attribute/standard/bulkAdd',
    method: 'post',
    data: params
  })
}
// 删除单条属性
export const deleteAttribute = bid => {
  return request({
    url: BASE_URL + `/method/logicalDeleteByBid/${bid}`,
    method: 'post'
  })
}
// 获取单条方法详情
export const getMethodDetail = bid => {
  return request({
    url: BASE_URL + `/method/get/${bid}`,
    method: 'get'
  })
}

