/**
 * @param {string} path
 * @returns {Boolean}
 */
export function isExternal(path) {
  return /^(https?:|mailto:|tel:)/.test(path)
}

// 是否为员工工号
export function isEmployeeNo(string) {
  return /18[0-9]{6}/g.test(string)
}

// 只允许数字和小数点
export const onlyNumberRegexp = /^\d+$|^\d*\.\d+$/g

// 时间yyyy-mm-dd
export const yyyyMMddRegexp = /\d{4}\-(0?[1-9]|1[012])\-(0?[1-9]|[12][0-9]|3[01])*/
