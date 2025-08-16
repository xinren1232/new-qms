import Cookie from 'js-cookie'

import { getUsers } from '@/api/user'
import store from '@/store'

export default {
  data() {
    return {
      showShare: false,
      shareConfig: {
        gateway: process.env.VUE_APP_GATEWAY_URL,
        token: Cookie.get('ipm-token'),
        rtoken: Cookie.get('ipm-rtoken'),
        userInfo: this.$store.getters.user,
        bussConfig: {}
      }
    }
  },
  mounted() {
    const { $store } = this

    this.$EventBus.$on('custom-share', async (data) => {
      const {
        completeRate,
        createdBy,
        createdTime,
        fullPath,
        name,
        person,
        planEndTime,
        planStartTime,
        projectName,
        roleBid,
        shareType,
        shareUrl,
        url
      } = data
      // 翻译人员
      const jobNumber = [createdBy, person].filter(Boolean).join(',')

      if (jobNumber.length) {
        const userMap = {}
        const { data: users } = await getUsers(jobNumber)

        users.forEach((item) => (userMap[item?.employeeVo.jobNumber] = item?.userVo.realName))
        // 设置人员信息store
        await store.dispatch('user/setUserMap', userMap)
      }

      this.showShare = true
      this.shareConfig.userInfo = $store.getters.user
      let msgCardDesc = ''
      let msgCardTheme = ''

      switch (shareType) {
        case 'simple-doc':
          msgCardDesc = `创建人：${$store.getters.userMap[createdBy]}[${createdBy}]\n创建时间：${createdTime}\n附件：${url}`
          msgCardTheme = `【${projectName}】${fullPath}/${name}`
          break
        case 'domain-doc':
          msgCardDesc = `创建人：${$store.getters.userMap[createdBy]}[${createdBy}]\n创建时间：${createdTime}`
          msgCardTheme = `【${projectName}】${fullPath}/${name}`
          break
        case 'WBS':
          {
            const header =
              roleBid || person
                ? roleBid
                  ? `角色[${roleBid}] `
                  : `${$store.getters.userMap[person]}[${person}]`
                : '无'

            msgCardDesc = `创建人：${$store.getters.userMap[createdBy]}[${createdBy}]\n责任人：${header}\n计划开始时间：${planStartTime}\n计划结束时间：${planEndTime}\n完成率：${completeRate}%`
            msgCardTheme = `【${projectName}】${name}`
          }
          break
        case 'flow':
          msgCardDesc = `单据号：${data.code}\n当前节点：${data.node}\n状态：${data.state}\n申请人：${data.apply}\n审批人：${data.approve}\n申请时间：${data.startTime}`
          msgCardTheme = `【流程标题】${data.title}`
          break
        case 'relation-doc':
          msgCardDesc = `创建人：${data.created_by__user__name}\n创建时间：${data.created_time}`
          msgCardTheme = `${name}`
          break
      }
      let imgUrl = ''

      switch (shareType) {
        case 'simple-doc':
          imgUrl = 'https://transsion-platform02.oss-cn-shenzhen.aliyuncs.com/attachment/doc.png'
          break
        case 'domain-doc':
          imgUrl = 'https://transsion-platform02.oss-cn-shenzhen.aliyuncs.com/attachment/doc.png'
          break
        case 'WBS':
          imgUrl = 'https://transsion-platform02.oss-cn-shenzhen.aliyuncs.com/attachment/task.png'
          break
        case 'flow':
          imgUrl = 'https://transsion-platform01.oss-accelerate.aliyuncs.com/attachment/flow.png'
          break
      }
      this.shareConfig.bussConfig = {
        url: shareUrl,
        msgCardTheme,
        msgCardDesc,
        cardType: '03',
        groupName: name,
        imgUrl
      }
      setTimeout(() => {
        this.$refs.customShare.showUserDialog()
      }, 100)
    })
  },
  methods: {
    // 关闭分享弹窗
    onCancel() {
      this.showShare = false
    }
  }
}
