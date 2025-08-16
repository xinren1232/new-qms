import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

import App from './App.vue'
import router from './router'
import './styles/index.scss'
import './styles/responsive-optimizations.scss'
import { useAuthStore } from './stores/auth'
import { initConfigReceiver } from './api/config-receiver'
import globalShortcuts from './utils/global-shortcuts'

const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 全局属性
app.config.globalProperties.$ELEMENT = {}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
})

// 初始化全局快捷键
globalShortcuts.setRouter(router)
app.config.globalProperties.$shortcuts = globalShortcuts

// 全局错误处理
app.config.errorHandler = (err, vm, info) => {
  console.error('全局错误:', err, info)
}

app.mount('#app')

// 初始化认证状态（幂等、仅一次）
const authStore = useAuthStore()
authStore.ensureInitialized().then(() => {
  console.log('🔐 认证状态初始化完成（ensureInitialized）')
}).catch(err => {
  console.error('❌ 认证状态初始化失败:', err)
})

// 初始化配置接收器
const configReceiver = initConfigReceiver()
app.config.globalProperties.$configReceiver = configReceiver
console.log('⚙️ 配置接收器初始化完成')
