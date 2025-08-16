import request from '@/app/request'

const BASE_URL = process.env.VUE_APP_CONFIG_CENTER

// 查询对象树
export const queryObjectTree = () => {
  return request({
    url: BASE_URL + '/object/treeAndLockInfo',
    method: 'get'
  })
}

// 新增对象节点
export const createObjectNode = (params) => {
  return request.post(BASE_URL + 'object/add', params)
}
// 删除对象节点
export const removeObjectNode = (bid) => {
  return request.post(BASE_URL + `object/deleteWithChildrenAndInfo/${bid}`)
}

// 对象检出
export const objectCheckout = (bid) => {
  return request.post(BASE_URL + `object/checkout/${bid}`)
}
// 对象检入
export const objectCheckin = (params) => {
  return request.post(BASE_URL + 'object/checkin',params)
}

// 撤销对象检出
export const objectCancelCheckout = (bid) => {
  return request.post(BASE_URL + `object/undoCheckout/${bid}`)
}
// 检出之前获取草稿信息
export const getDraftData = (params) => {
  return request.post(BASE_URL + 'object/readDraft', params)
}
/**
 * 获取对象的详细数据
 * @param {{ modelCode: string }} params 节点数据
 * @returns
 */
export const getObjectDetails = (params) => {
  return request.post(BASE_URL + 'object/getOneObject', params)
}
// 暂存数据 把当前数据存为草稿
export const saveDraftData = (params) => {
  return request.post(BASE_URL + 'object/staging', params)
}


// 查询对象属性By Bid ************ 弃用 ************
export const getObjectAttr = (bid) => {
  return request.post(BASE_URL + `object/getObjectAndAttribute/${bid}`)
}
// 查询对象属性By Bid
export const getObjectAttrByBid = (bid) => {
  return request.post(BASE_URL + `object/getObjectAndAttribute/${bid}`)
}
// 查询对象属性By ModelCode
export const getObjectAttrByModelCode = (modelCode) => {
  return request.post(BASE_URL + `object/getObjectAndAttributeByModelCode/${modelCode}`)
}
