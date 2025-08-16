<template>
  <div id="app">
    <keep-alive>
      <router-view />
    </keep-alive>
    <network-offline v-if="offline" />
  </div>
</template>

<script>
import { getRToken, getToken } from '@/app/cookie'

export default {
  name: 'App',
  components: {
    NetworkOffline: () => import('@@/bussiness/NetworkOffline')
  },
  computed: {
    offline({ $store }) {
      return !$store.getters.online
    }
  },
  beforeCreate() {
    // this.$store.dispatch('auth2/getSuperAdmin')

    Object.assign(this, {
      gateway: process.env.VUE_APP_GATEWAY_URL,
      token: getToken(),
      rtoken: getRToken(),
      userProfileConfig: { url: location.host, theme: 'light' }
    })
  },

  mounted() {
    this.$store.dispatch('permission/generateRoutes')
    // window.addEventListener('resize', this.onResize)

    window.addEventListener('load', () => {
      this.lazyLoadWaterMarkComponent()
    }, { once: true })
  },
  methods: {
    onResize() {
      // 刷新页面
      location.reload()
    },

    // 懒加载水印组件，等到页面加载完成以后再加载
    async lazyLoadWaterMarkComponent() {
      const { registerWatermark } = await import('../node_modules/@transsion/watermark/dist/index.js')

      // 为了避免接口请求慢导致水印内容为空，这里延迟一下
      setTimeout(() => {
        const currentUser = this.$store.getters.user

        registerWatermark({
          color: 'rgba(0, 0, 0, 0.12)',
          content: `${currentUser.name}_${currentUser.employeeNo}`
        })
      }, 0)
    }
  }
}
</script>
