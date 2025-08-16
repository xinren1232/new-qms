import store from '@/store'
import { getUsers } from '../api/user'
import { Message } from 'element-ui'

export const calcIcon = (type,multi) => {
  const iconMap = {
    text: 'multi-text',
    select: 'select',
    date: 'date',
    number: 'number',
    user: 'user',
    iframe: 'link',
    'picture-upload': 'file',
    'file-upload': 'file',
    checkbox: 'checkbox',
    department: 'department',
    object: 'object',
    role: 'role',
    encoder: 'encoder',
    'rich-editor': 'rich-editor',
    'slider': 'slider',
    'rate': 'rate',
    'switch': 'switch',
    'radio': 'radio'
  }


  if (type === 'select' && multi) return 'multi-select'

  return iconMap?.[type] || ''
}
export const calcRenderType = (item,extendComponent) => {
  const { type } = item
  const renderTypeMap = {
    text: 'defaultEditor',
    select: 'selectEditor',
    date: 'dateEditor',
    number: 'numberEditor',
    user: 'userEditor',
    iframe: 'iframeEditor',
    'picture-upload': 'pictureEditor',
    'file-upload': 'fileEditor',
    checkbox: 'checkboxEditor',
    department: 'text',
    object: 'objectEditor',
    role: 'ruleEditor',
    encoder: 'text',
    'rich-editor': 'richEditor',
    'slider': 'sliderEditor',
    'rate': 'rateEditor',
    'switch': 'switchEditor',
    'radio': 'radioEditor',
    'super-select': 'superSelectEditor'
  }

  // 动态注入扩展组件
  if (extendComponent?.length) {
    extendComponent.forEach((item) => {renderTypeMap[item.name] = item.name})
  }

  if (!renderTypeMap?.[type]) {
    Message.error(`${item.title}组件类型${type}不存在`)
  }

  return renderTypeMap?.[type] || 'text'
}

// 设置人员信息store
export function setUserStore(jobNumbers = []) {
  const storeUserMap = store.getters.userMap
  const storeUserInfoMap = store.getters.userInfoMap
  const numbers = jobNumbers.filter((item) => !storeUserMap[item])

  if (!numbers.length) return
  getUsers(numbers).then(({ data }) => {
    const userMap = {}
    const userInfoMap = {}

    data.forEach((item) => {
      const { deptName,name,positionName,jobNumber,userId,sex,deptId,email } = item

      userMap[jobNumber] = name
      userInfoMap[jobNumber] = { deptName,name,positionName,jobNumber,userId,sex,deptId,email }
    })
    store.dispatch('user/setUserMap', { ...userMap, ...storeUserMap })

    return store.dispatch('user/setUserInfoMap', { ...userInfoMap, ...storeUserInfoMap })
  })
}

// 扁平化树结构数据
export function flatTreeData(res, children = 'child') {
  const result = []
  const traverse = (arr) => {
    if (!arr) return
    arr.forEach((child) => {
      result.push(child)
      traverse(child[children])
    })
  }

  traverse(res)

  return result
}
/**
 * @param {string} path
 * @returns {Boolean}
 */
export function isExternal(path) {
  return /^(https?:|mailto:|tel:)/.test(path)
}

/**
 * 克隆对象， 如果不需要execudeKeys， 可以使用lodash.cloneDeep
 * @param {Object} source
 * @param {Object} execudeKeys 过滤的属性集合，不克隆该属性
 * @returns {Object}
 */
export function deepClone(source, execudeKeys = []) {
  if (!source && typeof source !== 'object') {
    throw new Error('error arguments', 'deepClone')
  }
  const targetObj = source.constructor === Array ? [] : {}

  Object.keys(source).forEach((key) => {
    if (execudeKeys.indexOf(key) > -1) return
    if (source[key] && typeof source[key] === 'object') {
      targetObj[key] = deepClone(source[key], execudeKeys)
    } else {
      if (source[key] !== null && source[key] !== '' && source[key] !== undefined) {
        targetObj[key] = source[key]
      }
    }
  })

  return targetObj
}
