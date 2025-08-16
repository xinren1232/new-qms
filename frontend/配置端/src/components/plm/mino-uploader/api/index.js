import request from '@/app/request'

const VUE_APP_GATEWAY = process.env.VUE_APP_GATEWAY


const CAD_URL = VUE_APP_GATEWAY + '/ipm-integration/'

// 获取CAD上传地址
export function getUploadUrl(params) {
  return request.post(CAD_URL + '/apm/files/presigned', params)
}

// 上传完成后 提交文件ID
export function uploadFileFinished(idList) {
  return request.post(CAD_URL + '/apm/files/finished', { fileIds: idList, returnUrl: true })
}

// 获取下载地址
export function getDownloadUrl(id) {
  return request.get(CAD_URL + `apm/files/presigned/${id}`)
}
export function allocate(id) {
  return request({
    url: CAD_URL + `apm/xxd/node/allocate/${id}`,
    method: 'post'
  })
}
export const addInstance = (spaceBid,applicationBid,params) => {
  return request({
    url: VUE_APP_GATEWAY + `/transcend-plm-datadriven-apm/apm/space/${spaceBid}/app/${applicationBid}/data-driven/add`,
    method: 'post',
    data: params
  })
}
