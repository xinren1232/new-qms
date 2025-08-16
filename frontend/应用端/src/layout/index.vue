<template>
  <div class="app-wrapper">
    <!-- 侧边栏 -->
    <div class="sidebar-container" :class="{ 'sidebar-collapsed': isCollapse }">
      <div class="sidebar-logo">
        <div class="logo-img">
          <el-icon><Setting /></el-icon>
        </div>
        <h1 v-show="!isCollapse" class="logo-title">QMS智能管理</h1>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="true"
        class="sidebar-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <sidebar-item
          v-for="route in routes"
          :key="route.path"
          :item="route"
          :base-path="route.path"
        />
      </el-menu>
    </div>

    <!-- 主内容区 -->
    <div class="main-container">
      <!-- 顶部导航栏 -->
      <div class="navbar">
        <div class="navbar-left">
          <el-button
            type="text"
            @click="toggleSidebar"
            class="sidebar-toggle"
          >
            <el-icon><Expand v-if="isCollapse" /><Fold v-else /></el-icon>
          </el-button>
          
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item
              v-for="item in breadcrumbs"
              :key="item.path"
              :to="{ path: item.path }"
            >
              {{ item.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="navbar-right">
          <el-button type="text" @click="refreshPage">
            <el-icon><Refresh /></el-icon>
          </el-button>

          <!-- 主题切换器 -->
          <ThemeSwitcher />

          <el-dropdown @command="handleCommand">
            <span class="user-dropdown">
              <el-avatar :size="32" :src="userInfo.avatar || '/avatar.jpg'" />
              <span class="user-name">{{ userInfo.username || '管理员' }}</span>
              <el-icon><CaretBottom /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="settings">系统设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 页面内容 -->
      <div class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import SidebarItem from './components/SidebarItem.vue'
import ThemeSwitcher from '@/components/ThemeSwitcher.vue'
import { logActions } from '@/utils/operationLog'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 侧边栏折叠状态
const isCollapse = ref(false)

// 用户信息从store获取
const userInfo = computed(() => authStore.user || {})

// 检查菜单权限 - 暂时简化，所有登录用户都可以访问
const hasMenuPermission = (route) => {
  // 如果有用户登录，就显示菜单（除了特定的管理员菜单）
  const isLoggedIn = authStore.isLoggedIn
  if (!isLoggedIn) return false

  // 只对特定的管理员功能进行权限检查
  if (route.meta?.roles && route.meta.roles.includes('ADMIN')) {
    const user = userInfo.value
    if (!user || !user.roles) return false
    return user.roles.includes('ADMIN')
  }

  return true
}

// 路由配置
const routes = computed(() => {
  const filteredRoutes = router.options.routes.filter(route => {
    const isNotHidden = !route.meta?.hidden
    const hasChildren = !!route.children
    const hasPermission = hasMenuPermission(route)

    return isNotHidden && hasChildren && hasPermission
  })

  console.log('显示的菜单:', filteredRoutes.map(r => r.meta?.title))
  return filteredRoutes
})

// 当前激活的菜单
const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})

// 面包屑导航
const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  return matched
})

// 切换侧边栏
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 刷新页面
const refreshPage = () => {
  window.location.reload()
}

// 初始化认证状态
const initAuth = async () => {
  if (!authStore.user && authStore.token) {
    await authStore.initializeAuth()
  }
}

// 处理用户下拉菜单
const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人中心功能开发中...')
      break
    case 'settings':
      ElMessage.info('系统设置功能开发中...')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        // 记录登出操作
        logActions.logout({ reason: '用户主动登出' })

        // 使用store的登出方法
        authStore.logout()

        // 跳转到登录页
        router.push('/login')
      } catch (error) {
        // 用户取消退出
      }
      break
  }
}

// 组件挂载时初始化认证状态
onMounted(() => {
  initAuth()
})
</script>

<style lang="scss" scoped>
.app-wrapper {
  display: flex;
  height: 100vh;
  width: 100%;
}

.sidebar-container {
  width: 210px;
  background-color: #304156;
  transition: width 0.28s;
  
  &.sidebar-collapsed {
    width: 64px;
  }
  
  .sidebar-logo {
    display: flex;
    align-items: center;
    padding: 16px;
    background-color: #2b3a4b;
    
    .logo-img {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #409EFF, #66b1ff);
      border-radius: 6px;
      color: white;
      font-size: 18px;
    }
    
    .logo-title {
      margin: 0 0 0 12px;
      font-size: 18px;
      font-weight: 600;
      color: #fff;
      white-space: nowrap;
    }
  }
  
  .sidebar-menu {
    border: none;
    height: calc(100vh - 64px);
    overflow-y: auto;
  }
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 50px;
  padding: 0 16px;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  
  .navbar-left {
    display: flex;
    align-items: center;
    
    .sidebar-toggle {
      margin-right: 16px;
      font-size: 18px;
    }
    
    .breadcrumb {
      font-size: 14px;
    }
  }
  
  .navbar-right {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .user-dropdown {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      
      .user-name {
        font-size: 14px;
        color: #606266;
      }
    }
  }
}

.app-main {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f5f7fa;
}

// 过渡动画
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
