import { getProjectList } from '@/api/my-project'
import { getAllUsersByProjectId } from '@/api/plan'
import { ymd } from '@/utils/format/date'

export default {
  methods: {
    /**
     @name: getProjectList
     @description:获取项目列表
     @param key 数据挂载到组件对应code
     @param filter 过滤条件
     @author: jiaqiang.zhang
     @time: 2021-09-14 15:30:11
    **/
    getProjectList(key, filter = ['preparation', 'underway']) {
      const target = this.find(key)
      const map = {}
      const params = { stateCode: filter }

      getProjectList(params).then((data) => {
        const res = []

        data?.data?.forEach((item) => {
          res.push({ key: item.bid, value: item.name })
          map[item.bid] = item
        })
        target.constraint.data = res
      })

      return map
    },
    /**
    @name: getProjectTeam
    @description: 获取项目下团队列表
    @param: projBid 项目ID
    @param: key 数据挂载到组件对应code
    @author: jiaqiang.zhang
    @time: 2021-09-14 15:32:07
    **/
    getProjectTeam(projBid, key = []) {
      const res = []

      getAllUsersByProjectId({ objInsBid: projBid }).then((data) => {
        data?.data?.forEach((item) => {
          res.push({ key: item.jobNumber, value: item.name })
        })
        key.forEach((list) => {
          const target = this.find(list)

          target.constraint.data = res
        })
      })

      return res
    },
    ymd(timeStamp) {
      return ymd(timeStamp)
    }
  }
}
