import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

// Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// VXE Table
import VxeTable from 'vxe-table'
import 'vxe-table/lib/style.css'

// Vue I18n
import { createI18n } from 'vue-i18n'

// 应用组件
import App from './App.vue'
import router from './router'

// Stores
import { useAppStore } from './stores/app'
import { useUserStore } from './stores/user'
import { usePermissionStore } from './stores/permission'

// 创建应用实例
const app = createApp(App)

// 国际化配置
const i18n = createI18n({
  legacy: false,
  locale: 'zh-CN',
  fallbackLocale: 'en',
  messages: {
    'zh-CN': {
      common: {
        add: '新增',
        edit: '编辑',
        delete: '删除',
        save: '保存',
        cancel: '取消',
        confirm: '确认',
        search: '搜索',
        reset: '重置',
        export: '导出',
        import: '导入'
      }
    },
    'en': {
      common: {
        add: 'Add',
        edit: 'Edit',
        delete: 'Delete',
        save: 'Save',
        cancel: 'Cancel',
        confirm: 'Confirm',
        search: 'Search',
        reset: 'Reset',
        export: 'Export',
        import: 'Import'
      }
    }
  }
})

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用插件
const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(ElementPlus, { size: 'default', zIndex: 3000 })
app.use(VxeTable)
app.use(i18n)

// 全局属性
app.config.globalProperties.$ELEMENT = { size: 'default', zIndex: 3000 }

// 初始化应用状态
const initializeApp = async () => {
  // 初始化stores
  const appStore = useAppStore()
  const userStore = useUserStore()
  const permissionStore = usePermissionStore()

  // 初始化应用状态
  appStore.initializeApp()
  userStore.initializeUser()

  // 如果用户已登录，生成路由
  if (userStore.isLoggedIn) {
    try {
      // 这里需要传入实际的异步路由
      await permissionStore.generateRoutes([])
    } catch (error) {
      console.error('初始化路由失败:', error)
    }
  }
}

// 初始化并挂载应用
initializeApp().then(() => {
  app.mount('#app')
}).catch(error => {
  console.error('应用初始化失败:', error)
  app.mount('#app')
})
