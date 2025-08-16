import dayjs from 'dayjs'

/**
 * dayjs 的API与 moment 一致, 但因为 moment 体积太大所以更改
 * 文档地址: https://dayjs.gitee.io/docs/zh-CN/installation/installation
 * 后面备注不会自动补零的意思为:
 *    若日期小于10 则只有单位(1/2/3)
 *    若日期大于10 则日期双位(10/11/12)
 *
 * 常用令牌格式:
 *  YYYY: 4位数的年份(2020)
 *  MM:   2位数的月份(01: 位数不足自动补零)
 *  DD:   2位数的日期(01: 位数不足自动补零)
 *  HH:   2位数的小时(01: 位数不足自动补零)
 *  mm:   2位数的分钟(01: 位数不足自动补零)
 *  ss:   2位数的秒数(01: 位数不足自动补零)
 *
 * 不常用但可能用到的令牌如下:
 *  YY:   2位数的年份(19 || 20)
 *  M:    1位数的月份(不会自动补零)
 *  D:    1位数的天数(不会自动补零)
 *  H:    1位数的小时(不会自动补零)
 *  h:    1位数的小时(不会自动补零 12小时制)
 *  m:    1位数的分钟(不会自动补零)
 *  s:    1位数的秒数(不会自动补零)
 *  S:    1位数的毫秒
 *  SS:   2位数的毫秒
 *  SSS:  3位数的毫秒
 *  MMM:  缩写的月份名称(Jan-Dec)
 *  MMMM: 完整的月份名称(January-December)
 *  A:    AM PM(大写的上下午)
 *  a:    am pm(小写的上下午)
 */

/**
 * 时间戳转 YYYY-MM-dd HH:mm:ss
 * @param timestamp
 * @returns {string}
 */
export const ymdhms = function (timestamp = Date.now()) {
  return dayjs(timestamp).format('YYYY-MM-DD HH:mm:ss')
}

/**
 * 时间戳转 YYYY-MM-dd
 * @param timestamp
 * @returns {string}
 */
export const ymd = function (timestamp = Date.now()) {
  return dayjs(timestamp).format('YYYY-MM-DD')
}

/**
 * 时间戳转 HH:mm:ss
 * @param timestamp
 * @returns {string}
 */
export const hms = function (timestamp = Date.now()) {
  return dayjs(timestamp).format('HH:mm:ss')
}

/**
 * 时间戳转 MM-DD HH:mm:ss
 * @param timestamp
 * @returns {string}
 */
export const mdhms = function (timestamp = Date.now()) {
  return dayjs(timestamp).format('MM-DD HH:mm:ss')
}

// 自定义时间日期解析程序
export const customFormat = function (timestamp = Date.now(), format = 'YYYY-MM-DD') {
  return dayjs(Number(timestamp)).format(format)
}
