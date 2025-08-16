<template>
  <div
    id="cloudDocument"
    ref="cloudDocument"
  />
</template>

<script>
import { getUserAuthentication } from './api'

export default {
  name: 'Index',
  async created() {
    const { data } = await getUserAuthentication()
    const { appId, noncestr, openId, signature, timestamp } = data

    window.webComponent
      .config({
        openId, // 当前登录用户的open id，要确保与生成 signature 使用的 user_access_token 相对应，使用 app_access_token 时此项不填。
        signature, // 签名
        appId, // 应用 appId
        timestamp, // 时间戳（毫秒）
        nonceStr: noncestr, // 随机字符串
        url: 'ipm.transsion.com', // 参与签名加密计算的url
        jsApiList: ['DocsComponent'], // 指定要使用的组件，请根据对应组件的开发文档填写。如云文档组件，填写['DocsComponent']
        lang: 'zh' // 指定组件的国际化语言：en-英文、zh-中文、ja-日文
      })
      .then(() => {
        const config = {
          // 组件参数
          src: 'https://bytedance.feishu.cn/docs/doccndIw0JiqrG5GtVB0e9joaVf',
          minHeight: '500px',
          width: '100%'
        }
        const dom = document.querySelector('#cloudDocument')

        window.webComponent.render('DocsComponent', config, dom)
      })
  },
  methods: {

  }
}
</script>

<style scoped lang="scss">
#cloudDocument {
  position: absolute;
  right: 0;
  top: 0;
  z-index: 9999;
  background-color: #ffffff;
  width: 100%;
  height: 80vh;
}
</style>
