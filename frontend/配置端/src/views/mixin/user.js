let queryList = []

import { getUsers } from '@/api/user'
import store from '@/store'

export default {
  data() {
    return { userMap: {} }
  },
  computed: {
    _userMap() {
      return store.getters.userMap
    }
  },
  methods: {
    queryUsers(data, fields) {
      let arr = []

      data.forEach((item) => {
        const fieldArr = fields.split(',')

        fieldArr.forEach((field) => {
          arr.push(item[field])
        })
      })
      // 人员工号去重
      arr = [...new Set(arr)].filter((item) => !this._userMap?.[item])
      // 数组分割成为每组100个(平台查询人员最大数量为100个)
      arr = arr.map((item, index) => !(index % 100) && arr.slice(index, index + 100)).filter(Boolean)
      let employees = arr.map((item) => item.join(','))

      employees = employees.filter((item) => !queryList.includes(item)) // 再从保存的工号中筛选一遍，进行防抖
      employees.forEach((item) => queryList.push(item)) // 将该次请求的工号保存起来
      employees.forEach((item) => {
        getUsers(item)
          .then(({ data }) => {
            this.userMap = {}
            data.forEach((titem) => {
              if (titem.employeeVo) this.userMap[titem.employeeVo.jobNumber] = titem.userVo.realName
              queryList = []
            })
            // 容错处理，如果人员已经离职或不存在 名字置空并缓存
            item.split(',').forEach((i) => {
              if (!this.userMap?.[i]) this.userMap[i] = i
            })
            store.dispatch('user/setUserMap', { ...this._userMap, ...this.userMap })
          })
          .catch((e) => e)
      })
    },
    userRender(row, field) {
      if (!row) return

      // 如果是字符串则表示传入的是工号
      if (typeof row === 'string') {
        if (this._userMap[row]) {
          return this._userMap[row] ?? row
        }
      }

      // 如果是对象则表示传入的是对象，则表明传入的是行数据
      const employeeNo = row[field]

      console.log(this._userMap)

      return this._userMap[employeeNo] ?? employeeNo
    },
    queryCompleted(response) {
      this.queryUsers(response.data, 'updatedBy')
    }
  }
}
