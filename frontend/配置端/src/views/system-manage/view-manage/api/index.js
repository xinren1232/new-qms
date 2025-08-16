import request from '@/app/request'

const BASE_URL = process.env.VUE_APP_CONFIG_CENTER

// 分页查询列表
export const pageQuery = (params) => {
  return request({
    url: BASE_URL + 'view/page',
    method: 'post',
    data: params
  })
}

// 删除视图
export const deleteView = (bid) => {
  return request({
    url: BASE_URL + `view/logicalDelete/${bid}`,
    method: 'post'
  })
}
// 设置视图状态
export const setViewStatus = ({ bid,enableFlag }) => {
  return request({
    url: BASE_URL + `view/changeEnableFlag/${bid}/${enableFlag}`,
    method: 'post'
  })
}
// 新增和修改视图
export const saveOrUpdate = (params) => {
  return request({
    url: BASE_URL + 'view/saveOrUpdate',
    method: 'post',
    data: params
  })
}

// 获取视图详情
export const getViewDetail = (bid) => {
  return request({
    url: BASE_URL + `view/get/${bid}`,
    method: 'get'
  })
}
