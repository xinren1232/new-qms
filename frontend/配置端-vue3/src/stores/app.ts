import { defineStore } from 'pinia'
import { ref, reactive, computed } from 'vue'
import Cookies from 'js-cookie'

export const useAppStore = defineStore('app', () => {
  // 状态定义
  const sidebar = reactive({
    opened: Cookies.get('sidebarStatus') ? !!+Cookies.get('sidebarStatus') : true,
    withoutAnimation: false
  })
  
  const online = ref(true)
  const device = ref('desktop')
  const language = ref(Cookies.get('language') || 'zh-CN')
  const size = ref(Cookies.get('size') || 'default')

  // 计算属性
  const sidebarOpened = computed(() => sidebar.opened)
  const isOnline = computed(() => online.value)
  const currentDevice = computed(() => device.value)
  const currentLanguage = computed(() => language.value)
  const currentSize = computed(() => size.value)

  // 操作方法
  const toggleSidebar = () => {
    sidebar.opened = !sidebar.opened
    sidebar.withoutAnimation = false
    
    if (sidebar.opened) {
      Cookies.set('sidebarStatus', '1')
    } else {
      Cookies.set('sidebarStatus', '0')
    }
  }

  const closeSidebar = (withoutAnimation = false) => {
    Cookies.set('sidebarStatus', '0')
    sidebar.opened = false
    sidebar.withoutAnimation = withoutAnimation
  }

  const openSidebar = (withoutAnimation = false) => {
    Cookies.set('sidebarStatus', '1')
    sidebar.opened = true
    sidebar.withoutAnimation = withoutAnimation
  }

  const toggleDevice = (deviceType: string) => {
    device.value = deviceType
  }

  const setSize = (newSize: string) => {
    size.value = newSize
    Cookies.set('size', newSize)
  }

  const setLanguage = (newLanguage: string) => {
    language.value = newLanguage
    Cookies.set('language', newLanguage)
    // 注意：在实际应用中，可能需要重新加载语言包而不是整个页面
    location.reload()
  }

  const changeOnlineStatus = (status: boolean) => {
    online.value = status
  }

  // 初始化方法
  const initializeApp = () => {
    // 从Cookie恢复状态
    const savedSidebarStatus = Cookies.get('sidebarStatus')
    if (savedSidebarStatus !== undefined) {
      sidebar.opened = !!+savedSidebarStatus
    }

    const savedSize = Cookies.get('size')
    if (savedSize) {
      size.value = savedSize
    }

    const savedLanguage = Cookies.get('language')
    if (savedLanguage) {
      language.value = savedLanguage
    }
  }

  return {
    // 状态
    sidebar,
    online,
    device,
    language,
    size,
    
    // 计算属性
    sidebarOpened,
    isOnline,
    currentDevice,
    currentLanguage,
    currentSize,
    
    // 方法
    toggleSidebar,
    closeSidebar,
    openSidebar,
    toggleDevice,
    setSize,
    setLanguage,
    changeOnlineStatus,
    initializeApp
  }
})

// 类型定义
export interface AppState {
  sidebar: {
    opened: boolean
    withoutAnimation: boolean
  }
  online: boolean
  device: string
  language: string
  size: string
}
