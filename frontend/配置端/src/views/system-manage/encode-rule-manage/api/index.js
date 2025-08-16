import request from '@/app/request'

const BASE_URL = process.env.VUE_APP_CONFIG_CENTER

// 分页查询列表
export const pageQuery = (params) => {
  return request({
    url: BASE_URL + 'code/rule/page',
    method: 'post',
    data: params
  })
}

// 新增,修改单条属性
export const createOrUpdateEncode = (params) => {
  return request({
    url: BASE_URL + 'code/rule/saveOrUpdate',
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
export const deleteRule = bid => {
  return request({
    url: BASE_URL + `code/rule/logicalDelete/${bid}`,
    method: 'post'
  })
}

// 规则条目查询
export const queryRuleItem = (params) => {
  return request({
    url: BASE_URL + 'code/rule/item/page',
    method: 'post',
    data: params
  })
}
// 规则条目新增修改
export const createUpdateRuleItem = (params) => {
  return request({
    url: BASE_URL + 'code/rule/item/saveOrUpdate',
    method: 'post',
    data: params
  })
}
// 查询规则条目详情
export const queryRuleItemDetail = (bid) => {
  return request({
    url: BASE_URL + `code/rule/item/get/${bid}`
  })
}

// 删除规则条目
export const deleteRuleItem = bid => {
  return request({
    url: BASE_URL + `code/rule/item/delete/${bid}`,
    method: 'post'
  })
}
