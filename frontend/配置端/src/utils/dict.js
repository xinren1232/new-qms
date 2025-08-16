import store from '@/store'
import { getLanguage } from '@/i18n/index.js'
import { replaceOpacity } from '@/config/universal-colors.js'

// 获取当前国际化语言
const lang = getLanguage().split('-')[0]

/**
@name: renderFieldByDict
@description: 根据字典类型和字典值获取字典的多语言值
@author: jiaqiang.zhang
 @param {String} value 字典值
 @param {String} dictType 字典类型
@time: 2023-02-09 10:33:52
**/
export const renderFieldByDict = (value, dictType) => {
  const dict = store.getters.dict[dictType]

  if (!dict) {
    store.dispatch('dict/queryDict', dictType)
  }
  const currentDict = dict?.find(item => item.keyCode === value)

  return currentDict ? currentDict?.[lang] : value
}

// 获取字典颜色
export const getDictColor = (value, dictType) => {
  const dict = store.getters.dict[dictType]

  if (!dict) {
    store.dispatch('dict/queryDict', dictType)
  }

  const currentDict = dict?.find(item => item.keyCode === value)

  const color = currentDict ? currentDict?.custom1 : 'rgba(84, 0, 200, 0.11)'

  return {
    color: replaceOpacity(color),
    backgroundColor: color
  }
}
