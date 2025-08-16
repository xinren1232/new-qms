import request from '@/app/request'

const BASE_URL = process.env.VUE_APP_CONFIG_CENTER

// 分页查询列表
export const pageQuery = params => {
  return request({
    url: BASE_URL + 'relation/page',
    method: 'post',
    data: params
  })
}

// 新增更新关系
export const createOrUpdate = params => {
  return request({
    url: BASE_URL + 'relation/saveOrUpdate',
    method: 'post',
    data: params
  })
}

// 删除关系
export const deleteItem = bid => {
  return request({
    url: BASE_URL + `relation/logicalDelete/${bid}`,
    method: 'post'
  })
}
// 设置关系状态
export const setStatus = ({ bid, enableFlag }) => {
  return request({
    url: BASE_URL + `relation/changeEnableFlag/${bid}/${enableFlag}`,
    method: 'post'
  })
}
// 关联查询
export const listRelation = modelCode => {
  return request({
    url: BASE_URL + `/relation/listRelation/${modelCode}`,
    method: 'get'
  })
}

// 多对象关联查询
export const getMultiRelatedByModelCode = modelCode => {
  return request({
    url: BASE_URL + `object/multiRelated/poly/getMultiRelatedByModelCode/${modelCode}`,
    method: 'get'
  })
}
// 多对象关联查询
export const deleteMultiRelated = data => {
  return request({
    url: BASE_URL + `object/multiRelated/poly/deleteMultiRelated`,
    method: 'post',
    data
  })
}
