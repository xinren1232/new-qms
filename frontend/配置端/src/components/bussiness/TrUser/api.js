import { concat, uniqBy } from 'lodash'

import request from '@/app/request'
import store from '@/store/index.js'
import { combineRequest } from '@/utils/index'

export const BaseUrl = `${process.env.VUE_APP_GATEWAY_URL}`
export const IPMBaseUrl = `${process.env.VUE_APP_BASE_API_2}`
export const AvatarBaseUrl = ''

const RecentKey = 'selectedUser'
const RecentCount = 10

const Url = {
  queryUserInfoByEmployees: IPMBaseUrl + '/plm/user/batchFindQuitUserDetailByNo',
  // queryUserInfo: BaseUrl + '/cp-uc-extend/Employee/queryUserInfo',
  // queryRootDeptList: BaseUrl + '/cp-uc-extend/Department/queryRootDeptList',
  // queryChildDeptAndEmployee: BaseUrl + '/cp-uc-extend/Department/queryChildDeptAndEmployee',
  // queryDeptAndEmployee: BaseUrl + '/cp-uc-extend/Department/queryDeptAndEmployee',
  // listEmployeeByParam: BaseUrl + '/cp-uc-extend/Employee/listEmployeeByParam'

  // 根据工号查询用户(根据工号批量查询)
  // queryUserInfoByEmployees: IPMBaseUrl + '/plm/user/listUserByEmpNos',
  // 模糊查询用户
  queryUserInfo: BaseUrl + '/uac-user-service/v2/api/uac-user/user/webPages',
  // 获取所有根部门
  queryRootDeptList: BaseUrl + '/uac-org-service/v2/api/uac-org/dept/webChildDepts',
  // 按部门Id查询部门下子部门和人员
  queryChildDeptAndEmployee: BaseUrl + '/uac-org-service/v2/api/uac-org/dept/webChildDepts',
  // 查询部门及人员信息
  queryDeptAndEmployee: BaseUrl + '/uac-user-service/v2/api/uac-user/user/webPages',
  // 根据部门id查询该部门下所有人员
  listEmployeeByParam: BaseUrl + '/uac-user-service/v2/api/uac-user/user/webPages',
  // 根据部门id查询该部门下所有人员
  leaveEmployeeByParam: BaseUrl + '/service-pf-open-gateway/open-apis/v2/api/uac-user/user/dimission/pages'
}

let employeeRequest = null

export default class HttpService {
  constructor() {
    this.service = request
  }

  /**
   * 保存最近选择用户
   * @param {Array} users 已选择用户
   */
  setUserRecent(users) {
    const recentStr = window.localStorage.getItem(RecentKey)
    const recent = recentStr && JSON.parse(recentStr)
    const us = uniqBy(concat(users, recent), 'employeeNo').slice(0, RecentCount)

    window.localStorage.setItem(RecentKey, JSON.stringify(us))
  }

  /**
   * 获取最近选择过的用户
   */
  getUserRecent() {
    const recentStr = window.localStorage.getItem(RecentKey)
    const arr = recentStr ? JSON.parse(recentStr) : []

    return arr.filter((s) => s)
  }

  /**
   * 模糊查询用户
   * @param {string} query 查询字符串
   */
  queryUsers(query) {
    const url = Url.queryUserInfo

    return this.service.post(url, {
      current: 1,
      size: 500,
      param: {
        keyword: query
      }
    }).then((res) => {
      console.log(res.data?.dataList)
      if (res.code === '200') {
        return res.data?.dataList
      }
    })
  }

  /**
   * 根据工号查询用户
   * @param {string} employees 以逗号分隔
   */
  queryUserInfoByEmployees(employees) {
    if (!employeeRequest) employeeRequest = combineRequest(doQueryUserInfoByEmployees)

    return employeeRequest(employees).then((data) => {
      // 过滤返回结果
      const result = []
      const employeesArr = employees.split(',')
      const employeesList = [...new Set(employeesArr)]

      data.forEach((item) => {
        if (employeesList.indexOf(item.employeeNo) > -1) {
          result.push(item)
        }
      })
      console.log(result, 'result')

      return result
    })
  }

  /**
   * 获取所有根部门
   */
  queryRootDeptList() {
    const url = Url.queryRootDeptList

    return this.service.post(url).then((res) => {
      if (res.code === '200') {
        return res.data.map((item) => {
          return { ...item, leafType: 0, type: 0 }
        })
      }
    })
  }

  /**
   * 按部门Id查询部门下子部门和人员
   * @param {long} depId
   */
  async queryChildDeptAndEmployee(depId) {
    const urlDept = Url.queryChildDeptAndEmployee
    const urUser = Url.listEmployeeByParam
    let list = []
    const resDept = await this.service.post(urlDept, {
      deptId: depId
    })

    if (resDept.code === '200') {
      list = resDept.data.map((item) => {
        return { ...item, leafType: 0, type: 0 }
      })
    }
    const resUser = await this.service.post(urUser, {
      searchTotal: true,
      param: {
        deptId: depId
      }
    })

    if (resUser.code === '200') {
      list.push(...resUser.data.dataList.map((item) => {
        return { ...item, leafType: 1, type: 1, route: true }
      }))
    }
    console.log(list, 'list')

    return list
  }

  /**
   * 查询部门及人员信息
   * @param {string} search 关键词
   */
  queryDeptAndEmployee(search) {
    const url = Url.queryDeptAndEmployee

    return this.service.post(url, {
      current: 1,
      size: 500,
      param: {
        keyword: search
      }
    }).then((res) => {
      if (res.code === '200') {
        return res.data.dataList
      }
    })
  }

  /**
   * 查询离职人员信息
   * @param {string} search 关键词
   */
  queryLeaveEmployee(search) {
    const url = Url.leaveEmployeeByParam

    return this.service.post(url, {
      current: 1,
      size: 500,
      param: {
        nation: search
      }
    }).then((res) => {
      if (res.code === '200') {
        return res.data.dataList
      }
    })
  }

  /**
   * 根据部门id查询该部门下所有人员
   * @param {string} deptId 部门id
   */
  listEmployeeByParam(deptId) {
    const url = Url.listEmployeeByParam

    return this.service.post(url, {
      current: 1,
      size: 500,
      param: {
        deptId
      }
    }).then((res) => {
      if (res.code === '200') {
        return res.data
      }
    })
  }
}

function doQueryUserInfoByEmployees(employees) {
  const userMap = store.state.user.userMap
  const url = Url.queryUserInfoByEmployees
  // join(',').split(',') 解决 ['18643422,728222','162525,26728']出现
  const arr = employees.join(',').split(',').reduce((acc, employeeNo) => {
    if (!userMap[employeeNo]) {
      acc.push(employeeNo)
    }

    return acc
  }, [])
  const params = [...new Set(arr)]

  // 如果所有人员都已经请求过则不再重新请求数据
  if (!params.length) {
    const users = employees.map((item) => ({
      employeeNo: item,
      employeeName: userMap[item]
    }))

    return Promise.resolve(users)
  }

  return request.post(url, params).then((res) => {
    if (res.code === '200') {
      const result = []

      res.data.forEach((item) => {
        // 将请求过的人员缓存起来
        userMap[item.jobNumber] = item.name
        result.push({
          employeeNo: item.jobNumber,
          employeeName: item.name
        })
      })

      return result
    }
  })
}
