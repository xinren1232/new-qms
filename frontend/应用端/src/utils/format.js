/**
 * 格式化工具函数
 */

/**
 * 格式化日期
 * @param {string|Date} date 日期
 * @param {string} format 格式
 * @returns {string}
 */
export function formatDate(date, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return ''
  
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化数字
 * @param {number} num 数字
 * @param {number} precision 精度
 * @returns {string}
 */
export function formatNumber(num, precision = 2) {
  if (num === null || num === undefined || isNaN(num)) return '0'
  return Number(num).toFixed(precision)
}

/**
 * 格式化文件大小
 * @param {number} bytes 字节数
 * @returns {string}
 */
export function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 格式化货币
 * @param {number} amount 金额
 * @param {string} currency 货币符号
 * @returns {string}
 */
export function formatCurrency(amount, currency = '¥') {
  if (amount === null || amount === undefined || isNaN(amount)) return `${currency}0.00`
  return `${currency}${Number(amount).toLocaleString('zh-CN', { minimumFractionDigits: 2 })}`
}

/**
 * 格式化百分比
 * @param {number} value 值
 * @param {number} precision 精度
 * @returns {string}
 */
export function formatPercentage(value, precision = 2) {
  if (value === null || value === undefined || isNaN(value)) return '0%'
  return `${(Number(value) * 100).toFixed(precision)}%`
}

/**
 * 格式化手机号
 * @param {string} phone 手机号
 * @returns {string}
 */
export function formatPhone(phone) {
  if (!phone) return ''
  const phoneStr = String(phone)
  if (phoneStr.length === 11) {
    return phoneStr.replace(/(\d{3})(\d{4})(\d{4})/, '$1****$3')
  }
  return phoneStr
}

/**
 * 格式化身份证号
 * @param {string} idCard 身份证号
 * @returns {string}
 */
export function formatIdCard(idCard) {
  if (!idCard) return ''
  const idStr = String(idCard)
  if (idStr.length === 18) {
    return idStr.replace(/(\d{6})(\d{8})(\d{4})/, '$1********$3')
  }
  return idStr
}

/**
 * 格式化银行卡号
 * @param {string} cardNo 银行卡号
 * @returns {string}
 */
export function formatBankCard(cardNo) {
  if (!cardNo) return ''
  const cardStr = String(cardNo)
  if (cardStr.length >= 16) {
    return cardStr.replace(/(\d{4})(\d+)(\d{4})/, '$1****$3')
  }
  return cardStr
}

/**
 * 格式化时间间隔
 * @param {number} seconds 秒数
 * @returns {string}
 */
export function formatDuration(seconds) {
  if (!seconds || seconds < 0) return '0秒'
  
  const days = Math.floor(seconds / 86400)
  const hours = Math.floor((seconds % 86400) / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  
  let result = ''
  if (days > 0) result += `${days}天`
  if (hours > 0) result += `${hours}小时`
  if (minutes > 0) result += `${minutes}分钟`
  if (secs > 0 || result === '') result += `${secs}秒`
  
  return result
}

/**
 * 格式化相对时间
 * @param {string|Date} date 日期
 * @returns {string}
 */
export function formatRelativeTime(date) {
  if (!date) return ''
  
  const now = new Date()
  const target = new Date(date)
  const diff = now.getTime() - target.getTime()
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  const week = 7 * day
  const month = 30 * day
  const year = 365 * day
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < week) {
    return `${Math.floor(diff / day)}天前`
  } else if (diff < month) {
    return `${Math.floor(diff / week)}周前`
  } else if (diff < year) {
    return `${Math.floor(diff / month)}个月前`
  } else {
    return `${Math.floor(diff / year)}年前`
  }
}
