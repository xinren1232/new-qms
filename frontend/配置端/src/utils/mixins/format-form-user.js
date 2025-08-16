import { cloneDeep } from 'lodash'

import { typeOf } from '@/utils/index.js'

/**
 * 处理表单里面user组件只传工号
 * @param { Object } form 表单数据
 * @param { Array } attrsList 属性列表
 */
export default function formatFormUser(form = {}, attrsList = []) {
  const userTypeAttrs = attrsList.reduce((acc, { innerName = '', type = '' }) => {
    if (type === 'user') {
      acc.push(innerName)
    }

    return acc
  }, [])

  const copyForm = cloneDeep(form) || {}

  for (const innerName of userTypeAttrs) {
    const attrValue = copyForm[innerName]

    if (typeOf(attrValue) === 'object') {
      copyForm[innerName] = attrValue.employeeNo || ''
    } else if (typeOf(attrValue) === 'array') {
      copyForm[innerName] = attrValue.map(({ employeeNo = '' }) => employeeNo)
    }
  }

  return copyForm
}
