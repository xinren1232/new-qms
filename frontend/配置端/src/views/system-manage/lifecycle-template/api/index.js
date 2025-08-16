import request from '@/app/request'

const BASE_URL = process.env.VUE_APP_CONFIG_CENTER

// 分页查询列表
export const pageQuery = (params) => {
  return request({
    url: BASE_URL + '/life-cycle/template/page',
    method: 'post',
    data: params
  })
}

// 新增,修改单条属性
export const createOrUpdateAttribute = (params) => {
  return request({
    url: BASE_URL + '/life-cycle/template/saveOrUpdate',
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
export const deleteTemplate = bid => {
  return request({
    url: BASE_URL + `life-cycle/template/logicalDelete/${bid}`,
    method: 'post'
  })
}

// 设置可用版本
export const setAvailableVersion = (params) => {
  return request({
    url: BASE_URL + 'life-cycle/template/changeVersion',
    method: 'post',
    data: params
  })
}

// 获取可用版本列表
export const getAvailableVersionList = (params) => {
  return request({
    url: BASE_URL + '/life-cycle/template/listVersion',
    method: 'post',
    data: params
  })
}

// 获取版本详情
// "templateBid": "",
// "version": ""
export const getVersionDetail = (templateBid,version) => {
  return request({
    url: BASE_URL + 'life-cycle/template/getOne',
    method: 'post',
    data: { version,templateBid }
  })
}
// 查询所有状态
export const getAllState = (params = {}) => {
  return request({
    url: BASE_URL + '/life-cycle/state/list',
    method: 'post',
    data: params
  })
}

// 设为默认版本
export const setDefaultVersion = (bid,currentVersion) => {
  return request({
    url: BASE_URL + '/life-cycle/template/changeVersion',
    method: 'post',
    data: { bid,currentVersion }
  })
}
// 设置状态
export const setState = (params) => {
  return request({
    url: BASE_URL + 'life-cycle/template/changeEnableFlag',
    method: 'post',
    data: params
  })
}
