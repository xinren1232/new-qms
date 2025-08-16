import Vue, { h } from 'vue'

import App from './App.vue'
import initApp from './app/init'
import router from './router'
import store from './store'
import i18n from '@/i18n'
import localConfigCenter from './config/local-config-center'

(() => {
  initApp(Vue)

  // 初始化本地配置中心
  Vue.prototype.$localConfigCenter = localConfigCenter
  console.log('⚙️ 本地配置中心初始化完成')

  let app = new Vue({
    router,
    store,
    i18n,
    render: () => h(App)
  })

  const METHODS = {
    mount() {
      app = app.$mount('#app')
    },

    unmount() {
      app.$destroy()
    }
  }

  if (window.__POWERED_BY_WUJIE__) {
    window.__WUJIE_MOUNT = METHODS.mount
    window.__WUJIE_UNMOUNT = METHODS.unmount
  } else {
    METHODS.mount()
  }
})()
