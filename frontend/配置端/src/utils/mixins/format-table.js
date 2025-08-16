// import { cloneDeep } from 'lodash'
import { getDictByType } from '@/api/dict'
import { getUsers } from '@/api/user.js'
import request from '@/app/request'
import { customFormat } from '@/utils/format/date'
import { parseString, typeOf } from '@/utils/index.js'
import { yyyyMMddRegexp } from '@/utils/validate/index.js'

const numReg = /^\d*$/

export default {
  data() {
    return {
      tableFormatList: ['select', 'date', 'role-select', 'user', 'team-select'], // 需要format的值
      formatMap: {}, // 字典映射数据
      roleSelectList: [], //  角色选择器的数据
      userMap: new Map() // user组件的值
    }
  },
  watch: {
    columns: {
      handler(val) {
        // console.log('columns', JSON.parse(JSON.stringify(val)))
        // 有类型为下拉的且有字典的就请求数据
        // 因constraint 可能为null， 所以需要单独解构
        if (
          val.some(({ constraint, formatType = '' }) => {
            const { type: dictType = '' } = constraint || {}

            return dictType && ['select'].includes(formatType)
          })
        ) {
          this.getDictData()
        }

        // 角色选择器
        if (val.some(({ formatType = '' }) => formatType === 'role-select')) {
          this.getRoleData()
        }
        // 人员组件
      },
      deep: true,
      immediate: true
    },
    data: {
      async handler(val) {
        try {
          const userList = this.columns
            .filter(({ formatType }) => ['team-select', 'user'].includes(formatType))
            .map(({ prop }) => prop)
          let employees = []

          for (const obj of val) {
            for (const key in obj) {
              if (Object.hasOwnProperty.call(obj, key)) {
                const element = obj[key]

                if (userList.includes(key) && numReg.test(element)) {
                  employees.push(element)
                }
              }
            }
          }
          // 去重
          employees = [...new Set(employees)]
          if (employees.length) {
            await this.getUserInfo(employees.join(','))
            if (this.isTableX) {
              this.tableData = [...val]
            }
          }
          // const xTable = this.tableRef
          // for (const item of this.columns) {
          //   if (item.filters) {
          //     const filterList = val.filter((e) => {
          //       return this.getLabel(e,item)
          //     }).map(e => {
          //       const label = this.getLabel(e,item)
          //       return {
          //         label,
          //         value: label
          //       }
          //     })
          //     let map = new Map();
          //     for (let item of filterList) {
          //       if (!map.has(item.value)) {
          //         map.set(item.value, item);
          //       }
          //     }
          //     xTable.setFilter(item, [...map.values()])
          //     this.$nextTick(() => {
          //       // this.$set(item, 'filters', [...map.values()])
          //       xTable.updateData()
          //       this.$forceUpdate()
          //     })
          //   }
          // }
          // console.log(JSON.parse(JSON.stringify(this.columns)))
          // xTable.updateData()
        } catch (error) {
          console.log(error)
        }
      }
      // immediate: true
    }
  },
  methods: {
    // 获取字典值
    getDictData() {
      return new Promise((resolve, reject) => {
        const dictList = this.columns
          .filter(({ constraint }) => {
            // constraint 可能会为null
            const { type = '' } = constraint || {}

            return type
          })
          .map(({ constraint: { type = '' } = {} }) => type)

        if (dictList.length === 0) return
        getDictByType([...new Set(dictList)].join(','))
          .then((res) => {
            const list = res.data || []
            const formatMap = {}

            for (const { code = '', values = [] } of list) {
              formatMap[code] = values.map(({ chValue: label, key: value }) => {
                return {
                  label,
                  value
                }
              })
            }
            this.formatMap = formatMap
            // 把字典值抛到外层 有需要的话可以使用
            this.$emit('getDict', this.formatMap)
            if (this.isTableX) {
              this.tableData = [...this.data]
            }
            resolve()
          })
          .catch((e) => {
            console.log(e)
            reject(new Error(false))
          })
      })
    },
    getUserInfo(employees) {
      return new Promise((resolve, reject) => {
        const userMap = new Map()

        getUsers(employees)
          .then(({ data }) => {
            for (const item of data) {
              const { employeeVo: { jobNumber = '' } = {}, userVo: { realName = '' } = {} } = item

              userMap.set(jobNumber, realName)
            }
          })
          .catch((e) => {
            console.log(e)
            reject(new Error(false))
          })
          .finally(() => {
            this.userMap = userMap
            resolve()
          })
      })
    },
    // 获取role-select 数据
    getRoleData() {
      return new Promise((resolve, reject) => {
        request
          .post(process.env.VUE_APP_BASE_API_2 + 'ipm/role/findRoleTree', { objInsBid: '' })
          .then((data) => {
            const list = data?.data ?? []

            this.roleSelectList = this.treeTolist(list)
            // 外面需要的话可以传出去
            this.$emit('roleSelect', this.roleSelectList)
            resolve()
          })
          .catch((e) => {
            console.log(e)
            reject(new Error(false))
          })
      })
    },
    // 树的平铺
    treeTolist(tree) {
      const list = []
      const stack = [...tree]

      while (stack.length) {
        const node = stack.pop()
        const children = node.childRoles

        if (children) {
          stack.push(...children)
        }
        list.push(node)
      }

      return list
    },

    getLabel(row, cloumn) {
      const { selectMultiple = false, constraint: { type = '' } = {}, prop, formatType: columnType } = cloumn
      let name = ''
      const value = row[prop] // 后端返回的值

      switch (columnType) {
        case 'select':
          name = this.filterSelect(value, type, selectMultiple)
          break
        case 'date':
          name = value ? (yyyyMMddRegexp.test(value) ? value : customFormat(value)) : ''
          break
        case 'role-select':
          name = this.filterRoleSelect(value)
          break
        case 'user':
        case 'team-select':
          name = this.filterUser(value)
          break
        default:
          name = value
          break
      }
      // if (['select', 'role-select', 'user'].includes(columnType)) {
      row[`${prop}_mapping`] = name

      // }
      return name
    },
    // select
    /**
     *
     * @param {当前值} value
     * @param {字典的key} type
     * @param {是否多选} selectMultiple
     */
    filterSelect(value, type, selectMultiple) {
      if (!value) return ''
      const arr = this.formatMap[type] || []

      if (!selectMultiple) {
        return arr.find(({ value: value1 }) => String(value1) === String(value))?.label ?? value
      } else {
        // 多选
        const valList = parseString(value)

        return valList.map(item => arr.find(({ value: value1 }) => String(value1) === String(item))?.label ?? item).join('、')
      }
    },
    // role-select处理
    filterRoleSelect(value) {
      let name = ''

      if (Array.isArray(value)) {
        name = value
          .map((item) => this.roleSelectList.find(({ bid }) => String(bid) === String(item))?.name ?? item)
          .join('、')
      } else {
        name = this.roleSelectList.find(({ bid }) => String(bid) === String(value))?.name ?? value
      }

      return name
    },
    // user组件处理
    filterUser(value) {
      let name = ''

      switch (typeOf(value)) {
        case 'object':
          if (Object.keys(value).length) {
            const { employeeName = '', employeeNo = '' } = value

            name = `${employeeName}[${employeeNo}]`
          }
          break
        case 'array':
          name = value.map((acc, item) => {
            if (Object.keys(item).length) {
              const { employeeName = '', employeeNo = '' } = item

              acc.push(`${employeeName}[${employeeNo}]`)
            }

            return acc
          }, []).join('、')
          break
        default:
          // console.log('userMap', this.userMap)
          // 工号 请求接口处理
          if (numReg.test(value) && this.userMap.has(value)) {
            name = `${this.userMap.get(value)}[${value}]`
          } else if (value) {
            name = `${value}`
          }
          break
      }

      return name
    }
  }
}
