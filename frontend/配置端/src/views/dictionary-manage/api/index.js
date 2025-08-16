import request from '@/app/request'

const BASE_URL = process.env.VUE_APP_CONFIG_CENTER

// 新增字典
export const addDict = (data) => {
  return request({
    url: BASE_URL + 'manager/cfg/dictionary/add',
    method: 'post',
    data
  })
}

// 分页查询字典
export const pageQuery = (data) => {
  return request({
    url: BASE_URL + 'dictionary/page',
    method: 'post',
    data
  })
}

// 新增、修改字典
export const createOrUpdate = (data) => {
  return request({
    url: BASE_URL + 'dictionary/saveOrUpdateDictionaryAndItem',
    method: 'post',
    data
  })
}

// 查询字典值LIST根据ID
export const queryDictItemById = bid => {
  return request({
    url: BASE_URL + `dictionary/getDictionaryAndItemByBid/${bid}`,
    method: 'get'
  })
}

// 删除字典
export const deleteDict = bid => {
  return request({
    url: BASE_URL + `dictionary/logicalDelete/${bid}`,
    method: 'post'
  })
}
// 更新字典状态
export const updateDictStatus = ({ bid, enableFlag }) => {
  return request({
    url: BASE_URL + `dictionary/changeEnableFlag/${bid}/${enableFlag}`,
    method: 'post'
  })
}

// 批量查询字典值，根据字典编码，用逗号分隔
export const queryDictItemByCodes = codes => {
  return request({
    url: BASE_URL + `dictionary/listDictionaryAndItemByCodesAndEnableFlags`,
    method: 'post',
    data: { codes: codes.split(',') }
  })
}
