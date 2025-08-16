/*
 * @Author: BanLi
 * @Date: 2020-11-21 17:32:30
 * @LastEditTime: 2021-03-03 17:29:37
 * @Description: 公共类型组件以及部分组件特有的props
 */
const TYPES = {
  input: 'ElInput',
  date: 'ElDatePicker',
  time: 'ElTimePicker',
  checkbox: 'ElCheckbox',
  dateYMD: 'ElDatePicker',
  dateYMDHMS: 'ElDatePicker',
  datetime: 'ElDatePicker',
  daterange: 'ElDatePicker',
  inputNumber: 'ElInputNumber',
  monthrange: 'ElDatePicker'
}

const PROPS = {
  monthrange: {
    type: 'monthrange'
  },
  date: {
    type: 'date',
    valueFormat: 'timestamp'
  },
  datetime: {
    type: 'datetime',
    valueFormat: 'timestamp'
  },
  dateYMD: {
    type: 'date',
    valueFormat: 'yyyy-MM-dd'
  },
  dateYMDHMS: {
    type: 'datetime',
    valueFormat: 'yyyy-MM-dd HH:mm:ss'
  },
  daterange: {
    type: 'daterange',
    valueFormat: 'timestamp'
  }
}

export function getComponent(formItem) {
  return formItem?.component || TYPES[formItem.type] || 'ElInput'
}
export function getComponentProp(type) {
  return PROPS[type]
}
