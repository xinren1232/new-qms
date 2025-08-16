/*
 * @Author: BanLi
 * @Date: 2021-03-09 18:04:26
 * @LastEditTime: 2021-06-29 17:44:35
 * @Description: 附件上传
 */

import request from '@/app/request'

const BASE_URL = process.env.VUE_APP_BASE_API_2

// 上传附件
export function uploadAttachment(...params) {
  // const files = params.get('files')
  // params.append('files', files, encodeURIComponent(files.name))
  // // params.set('files', files)
  return request.post(BASE_URL + 'ipm/attachment/uploadAttachment', ...params)
}

// 批量上传文件
export function uploadAttachments(...params) {
  return request.post(BASE_URL + 'ipm/attachment/uploadAttachments', ...params)
}
