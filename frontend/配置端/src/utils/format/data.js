/*
 * @Author: BanLi
 * @Date: 2021-06-30 13:53:09
 * @LastEditTime: 2021-07-13 11:22:15
 * @Description: 格式化数据
 */

// 是不是普通对象
export function isPlainObject(obj) {
  return Object.prototype.toString.call(obj) === '[object Object]'
}

// 把数据转换为字符串
export function toString(value) {
  if (value === null || value === undefined) return ''
  if (Array.isArray(value) || (isPlainObject(value) && value.toString === Object.prototype.toString)) { return JSON.stringify(value) }

  return String(value)
}
