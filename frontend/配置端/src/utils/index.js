import { editLang } from '@/api/lang'
import { getUsers } from '@/api/user'
import store from '@/store'
import { export_json_to_excel } from '@/utils/export/Export2Excel'

// 将数据转换为数组
export const toArray = (list) => {
  return Array.isArray(list) ? list : [list]
}

// Depth-First-Search 深度优先遍历
export function dfs(data, callback, { children = 'children' } = {}) {
  if (Array.isArray(data)) {
    data.forEach((item) =>
      JSON.stringify(item, (key, value) => {
        callback(value)

        return value[children]
      })
    )

    return
  }

  JSON.stringify(data, (key, value) => {
    callback(value)

    return value[children]
  })
}

// 包裹 promise，以同步的方式获取数据和错误信息
export const awaitPromise = (promise, errorExt) => {
  return promise
    .then((data) => [data, null])
    .catch((err) => {
      if (errorExt) {
        return [null, Object.assign({}, err, errorExt)]
      }

      return [null, err]
    })
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

// 是否是火狐浏览器
export const isFirefox = typeof navigator !== 'undefined' && navigator.userAgent.toLowerCase().indexOf('firefox') > -1

/**
 * 根据字典获取value的label映射
 * @param {Object} map 数据字典
 * @param {*} value key值，多个key以逗号分隔
 */
export function labelRender(map, value) {
  if (map) {
    if (typeof value === 'string') {
      return value
        .split(',')
        .map((code) => map[code])
        .join(',')
    } else {
      return map[value]
    }
  } else {
    return ''
  }
}

export function json2excel(tableJson, filenames, autowidth, bookTypes) {
  import('@/utils/export/ExportSheets').then((excel) => {
    const tHeader = []
    const dataArr = []
    const sheetnames = []

    for (const i in tableJson) {
      tHeader.push(tableJson[i].tHeader)
      dataArr.push(formatJson(tableJson[i].filterVal, tableJson[i].tableDatas))
      sheetnames.push(tableJson[i].sheetName)
    }
    excel.export_json_to_excel({
      header: tHeader,
      data: dataArr,
      sheetname: sheetnames,
      filename: filenames,
      autoWidth: autowidth,
      bookType: bookTypes
    })
  })
}

export function json2excelMuti(tableJson, filenames, autowidth, bookTypes) {
  import('@/utils/export/ExportSheets').then((excel) => {
    const tHeader = []
    const dataArr = []
    const sheetnames = []
    const multiHeader = []
    const merges = []

    for (const i in tableJson) {
      tHeader.push(tableJson[i].tHeader)
      dataArr.push(formatJson(tableJson[i].filterVal, tableJson[i].tableDatas))
      sheetnames.push(tableJson[i].sheetName)
      multiHeader.push(tableJson[i].multiHeader || [])
      merges.push(tableJson[i].merges || [])
    }

    excel.export_json_to_excel({
      multiHeader,
      merges,
      header: tHeader,
      data: dataArr,
      sheetname: sheetnames,
      filename: filenames,
      autoWidth: autowidth,
      bookType: bookTypes
    })
  })
}

/**
 * JSON数据导出excel
 * @param { name, header, headerKeys, data } 参数集合
 */
export function exportJsonToExcel({ data, filename, header, headerKeys }) {
  filename = filename || `tr-export-${new Date().getTime()}`
  const resultData = formatJson(headerKeys, data)

  export_json_to_excel({
    header,
    data: resultData,
    filename
  })
}

/**
 * 格式化JSON数据
 * @param filterVal
 * @param jsonData
 * @returns {*}
 */
function formatJson(filterVal, jsonData) {
  return jsonData.map((v) =>
    filterVal.map((j) => {
      return v[j] ? v[j] : ''
    })
  )
}

/**
 * 获取url的参数
 */
export function getRequestParam(targetPath = window.location.href) {
  const url = decodeURIComponent(targetPath) // 获取url中"?"符后的字串
  const params = {}
  const index = url.indexOf('?')

  if (index !== -1) {
    const str = url.substr(index + 1)
    const strs = str.split('&')

    for (let i = 0; i < strs.length; i++) {
      params[strs[i].split('=')[0]] = unescape(strs[i].split('=')[1])
    }
  }

  return params
}

/**
 * 用于请求合并
 * @param {Function} httpRequest 查询请求promise
 * @param {*} options 函数自带参数
 * @param {*} time 时间间隔
 */
export function combineRequest(httpRequest, options, time = 300) {
  let timer = null
  let targetPromise = null
  let targetArgs = []

  return function (...args) {
    targetArgs = targetArgs.concat(args)
    if (!targetPromise) {
      targetPromise = new Promise((resolve) => {
        if (timer) {
          clearTimeout(timer)
        }
        timer = setTimeout(() => {
          const targetOptions = { params: args }

          Object.assign(targetOptions, options)
          httpRequest(targetArgs, targetOptions).then((data) => {
            resolve(data)
          })
          targetPromise = null
          targetArgs = []
          timer = null
        }, time)
      })
    }

    return targetPromise
  }
}

/**
 * 检查数据类型
 * @param {any} target 需要校验类型的参数
 * @returns {string} 返回数据的类型
 */
export const typeOf = (target) => {
  if (target === null) return 'null'

  if (typeof target !== 'object') return typeof target
  const result = {}.toString.call(target).match(/\[object (.*)\]/)?.[1]

  return result?.toLowerCase() ?? 'undefined'
}

/**
 * 解析字符串为json
 * @param {*} value 任意类型的值
 * @param {*} parser 如果值为 falsy 则返回该值, 可以直接在 parser 函数中返回默认的值
 */
export const parseString = (value, parser) => {
  try {
    return JSON.parse(value)
  } catch (e) {
    return parser && typeOf(parser) === 'function' ? parser(value) : value
  }
}

/**
 * 枚举对象值 将键名和键值反向映射一遍
 * @param {object} target 需要枚举的目标对象
 */
export function enumeration(target) {
  if (typeOf(target) !== 'object') return {}

  return Object.entries(target).reduce((prev, item) => {
    prev[(prev[item[0]] = item[1])] = item[0]

    return prev
  }, {})
}

// 翻译 router.meta.title，用于tagsView标签视图中
export function generateTitle(title) {
  const hasKey = this.$te('route.' + title)

  if (hasKey) return this.$t('route.' + title)

  return title ?? 'unknow'
}

// 处理人员选择 多对象转换为 string 逗号凭借 [{},{}] => "11212,12121,323232"
export function userListToString(params) {
  if (!params) return ''
  const _result = params.reduce((prev, item) => {
    prev.push(item.employeeNo)

    return prev
  }, [])

  return _result.toString()
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
// 扁平化树结构数据
/**
 *flatTreeData 在数据量过大时候容易爆栈
 * @param {array} tree // 树结构
 * @param {string} children 树的子节点
 * @returns array
 */
export function treeTolist(tree, children = 'children') {
  const list = []
  const stack = [...tree]

  while (stack.length) {
    const node = stack.pop()
    const childrenNode = node[children]

    if (childrenNode) {
      stack.push(...childrenNode)
    }
    list.push(node)
  }

  return list
}
export function isRealNum(val) {
  // 先判定是否为number
  if (typeof val !== 'number') {
    return false
  }

  return !Number.isNaN(val)
}

/**
 * 带数据过滤的处理函数
 * @param target 需要处理的数组数据
 * @param handler 处理函数
 * @param initValue 初始值
 * @returns 处理过后的数组数据
 */
export function reduceMap(target = [], handler, initValue = []) {
  return target.reduce((acc, cur, index, arr) => {
    const result = handler(cur, index, arr)

    result && acc.push(result)

    return acc
  }, initValue)
}

/**
 * 带数据处理的过滤函数
 * @param target 需要处理的数组数据
 * @param handler 处理函数
 * @param initValue 初始值
 * @returns 处理过后的数组数据
 */
export function reduceFilter(target = [], handler, initValue = []) {
  return target.reduce((acc, cur, index, arr) => {
    const result = handler(cur, index, arr)

    result && acc.push(cur)

    return acc
  }, initValue)
}

export function insertLang(attrs = [], groups = []) {
  // console.log(groups)

  if (groups.length) {
    // 1.插入分组信息
    groups.forEach((item) => {
      const code = `${item.objBid}_${item.name}_groupName`
      const param = [
        { languageCode: 'zh-CN', name: '简体中文(中国)', value: item.name },
        { languageCode: 'en-US', name: '英语(美国)', value: item.name },
        { languageCode: 'ko_KR', name: '韩文(韩国)', value: item.name },
        { languageCode: 'ja_JP', name: '日语(日本)', value: item.name },
        { languageCode: 'ru_RU', name: '俄语(俄罗斯)', value: item.name }
      ]

      editLang(code, param).then((data) => {
        console.log(data)
      })
    })
  }
  // 2. 插入属性名和描述
  attrs.forEach((item) => {
    const types = ['displayName', 'description']

    types.forEach((type) => {
      if (!item[type]) return
      if (!item.innerName) return
      if (!item.objBid) return
      const code = `${item.objBid}_${item.innerName}_${type}`
      const param = [
        { languageCode: 'zh', name: '简体中文(中国)', value: item[type] },
        { languageCode: 'en', name: '英语(美国)', value: item[type] },
        { languageCode: 'ko_KR', name: '韩文(韩国)', value: item[type] },
        { languageCode: 'ja_JP', name: '日语(日本)', value: item[type] },
        { languageCode: 'ru_RU', name: '俄语(俄罗斯)', value: item[type] }
      ]

      editLang(code, param).then((data) => {
        console.log(data)
      })
    })
  })
}

// 把字典数据转化为{label,value}格式 dict -> {label:label, value: value}
export function dictToLabel(map = {}) {
  const hasMap = {}

  for (const key in map) {
    if (Object.hasOwnProperty.call(map, key)) {
      const obj = map[key]
      const arr = []

      for (const value in obj) {
        if (Object.hasOwnProperty.call(obj, value)) {
          const label = obj[value]

          arr.push({
            label,
            value
          })
        }
      }
      hasMap[key] = arr
    }
  }

  return hasMap
}

/**
 @name: mergeApiNeedUserInfo
 @description: 接口合并，目的在于有些字段需要把工号翻译成人员信息的
 @params: api api请求
 @params: fields 工号字段 比如创建人，更新人等 [createBy, updateBy]
 @author: jiaqiang.zhang
 @time: 2022-07-12 13:44:08
**/
export async function mergeApiNeedUserInfo(api, fields = []) {
  // 如果不需要翻译，直接返回结果
  if (!fields.length) return await api
  const response = await api
  const result = Array.isArray(response.data) ? response.data : response?.data?.data || []
  const numbers = [] // 工号LIST
  const userMap = {} // 工号MAP
  const userInfoMap = {} // 工号MAP
  const storeUserMap = store.getters.userMap
  const storeUserInfoMap = store.getters.userInfoMap

  // 收集工号
  result.forEach((item) => {
    fields.forEach((field) => {
      if (item[field] && !storeUserMap[item[field]]) {
        numbers.push(item[field])
      }
    })
  })
  // 对工号进行去重，去空
  const filterNumbers = [...new Set(numbers)].filter(Boolean)

  // 如果没有工号，直接返回结果
  if (!filterNumbers.length) return await api
  // 获取人员信息

  try {
    const { data: users } = await getUsers(filterNumbers)

    users.forEach((item) => {
      // 部门名称，姓名，职位名称，工号，用户id，性别，部门id
      const { deptName,name,positionName,jobNumber,userId,sex,deptId,email } = item


      userMap[jobNumber] = name
      userInfoMap[jobNumber] = { deptName,name,positionName,jobNumber,userId,sex,deptId,email }
    })
    // 设置人员信息store
    await store.dispatch('user/setUserMap', { ...userMap, ...storeUserMap })
    await store.dispatch('user/setUserInfoMap', { ...userInfoMap, ...storeUserInfoMap })
  } catch (e) {
    console.log(e)
  }

  // 返回结果
  return response
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

    store.dispatch('user/setUserInfoMap', { ...userInfoMap, ...storeUserInfoMap })

    return data
  })
}
// 是否是移动端
export const isMobile = () => {
  const UA = navigator.userAgent

  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(UA) // 是否是移动端
}
// 是否在飞书内部打开
export const isFeishu = () => {
  return /lark/i.test(window.navigator.userAgent.toLowerCase())
}

// 清除所有的Cookie
export const deleteAllCookies = () => {
  const cookies = document.cookie.split('; ')

  for (let i = 0; i < cookies.length; i++) {
    const cookie = cookies[i]
    const eqPos = cookie.indexOf('=')
    const name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie

    document.cookie = name + '=; expires=Thu, 01 Jan 1970 00:00:00 GMT'
  }
}
export const traverseArr = (arr, fun, field = 'children') => {
  arr.forEach((item) => {
    traverse(item, fun, field)
  })
}
// 遍历
export const traverse = (node, fun, field = 'children') => {
  fun(node)
  if (node[field]) {
    node[field].forEach((child) => {
      traverse(child, fun)
    })
  }
}
