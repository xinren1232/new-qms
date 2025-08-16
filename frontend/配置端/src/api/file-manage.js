// 文件管理API

import request from '@/app/request.js'

const BASE = process.env.VUE_APP_CONFIG_CENTER + 'filemanagement/'
const DATA_DRIVEN = process.env.VUE_APP_SCRUM + 'file/'

// --- 文件库
// 文件库列表
export function getFileLibraryList(params) {
  return request.post(BASE + 'cfgFileLibrary/page', params)
}

// 新增、编辑
export function addOrUpdateFileLibrary(params) {
  return request.post(BASE + 'saveOrUpdateCfgFileLibrary', params)
}
// 硬删除文件库
export function deleteFileLibrary(bid) {
  return request.post(BASE + `cfgFileLibrary/delete/${bid}`)
}
// 更新状态
export function updateFileLibraryStatus(params) {
  return request.post(BASE + 'cfgFileLibrary/updateEnable', params)
}

// 获取文件库详情
export function getSingleFileLibrary(bid) {
  return request.get(BASE + 'cfgFileLibrary/get/' + bid)
}

// --- 文件类型
// 文件类型表格列表
export function getFileTypeList(params) {
  return request.post(BASE + 'fileType/list', params)
}

// 新增/编辑
export function addOrUpdateFileType(params) {
  return request.post(BASE + 'fileType/saveOrUpdate', params)
}

// 硬删除文件类型
export function deleteFileType(bid) {
  return request.post(BASE + `fileType/delete/${bid}`)
}
// 更新状态
export function updateFileTypeStatus(params) {
  return request.post(BASE + 'fileType/updateEnable', params)
}

// 获取文件类型详情
export function getSingleFileType(bid) {
  return request.get(BASE + 'fileType/get/' + bid)
}

// 操作文件类型下方关系列表(选取、删除)
export function operateFileTypeRelation(params) {
  return request.post(BASE + 'fileType/addOrDeleteFileTypeViewer', params)
}

// --- 复制规则
// 复制规则列表
export function getCopyRuleList(params) {
  return request.post(BASE + 'cfgFileCopyRule/page', params)
}

// 新增/编辑
export function addOrUpdateCopyRule(params) {
  return request.post(BASE + 'cfgFileCopyRule/saveOrUpdate', params)
}
// 硬删除复制规则
export function deleteCopyRule(bid) {
  return request.post(BASE + `cfgFileCopyRule/delete/${bid}`)
}
// 更新状态
export function updateCopyRuleStatus(params) {
  return request.post(BASE + 'cfgFileCopyRule/updateEnable', params)
}

// 获取复制规则详情
export function getSingleCopyRule(bid) {
  return request.get(BASE + 'cfgFileCopyRule/get/' + bid)
}

// 操作复制规则详情下方关系列表(选取、删除)
export function operateCopyRuleRelation(params) {
  return request.post(BASE + 'cfgFileTypeRuleRel/operate', params)
}

// 创建复制规则下面的【目标文件库】关系
export function updateCopyRuleFileLibrary(params) {
  return request.post(BASE + 'cfgFileLibraryRuleRel/addOrUpdate', params)
}

// 删除目标文件库
export function deleteCopyRuleFileLibrary(bid) {
  return request.post(BASE + 'cfgFileLibraryRuleRel/delete/' + bid)
}

// --- 查看器
// 查看器列表
export function getFileViewerList(params) {
  return request.post(BASE + 'fileViewer/list', params)
}

// 新增/编辑
export function addOrUpdateFileViewer(params) {
  return request.post(BASE + 'fileViewer/saveOrUpdate', params)
}
// 硬删除查看器
export function deleteFileViewer(bid) {
  return request.post(BASE + `fileViewer/delete/${bid}`)
}
// 更新状态
export function updateFileViewerStatus(params) {
  return request.post(BASE + 'fileViewer/updateEnable', params)
}

// 获取查看器详情
export function getSingleFileViewer(bid) {
  return request.get(BASE + 'fileViewer/get/' + bid)
}

// --- 复制执行
export function getCopyExecList(params) {
  return request.post(DATA_DRIVEN + 'copy/execution/queryPageByCondition', params)
}

// 批量执行复制
export function batchExecCopy(params) {
  return request.post(DATA_DRIVEN + 'copy/execution/batchExecute', params)
}
