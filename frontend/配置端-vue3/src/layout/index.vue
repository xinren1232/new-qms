<template>
  <div class="app-wrapper">
    <el-container class="layout-container">
      <!-- 侧边栏 -->
      <el-aside :width="sidebarWidth" class="sidebar-container">
        <div class="logo-container">
          <h2>QMS-AI配置中心</h2>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          :collapse="isCollapse"
          router
        >
          <template v-for="route in routes" :key="route.path">
            <el-sub-menu v-if="route.children && route.children.length > 1" :index="route.path">
              <template #title>
                <el-icon><component :is="route.meta?.icon || 'Menu'" /></el-icon>
                <span>{{ route.meta?.title }}</span>
              </template>
              <el-menu-item
                v-for="child in route.children"
                :key="child.path"
                :index="route.path + '/' + child.path"
              >
                <el-icon><component :is="child.meta?.icon || 'Document'" /></el-icon>
                <span>{{ child.meta?.title }}</span>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="route.path + '/' + route.children?.[0]?.path">
              <el-icon><component :is="route.meta?.icon || 'Menu'" /></el-icon>
              <span>{{ route.meta?.title }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部导航 -->
        <el-header class="navbar">
          <div class="navbar-left">
            <el-button
              type="text"
              @click="toggleSidebar"
              class="hamburger"
            >
              <el-icon><Expand v-if="isCollapse" /><Fold v-else /></el-icon>
            </el-button>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item>首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ currentRoute?.meta?.title }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="navbar-right">
            <el-dropdown>
              <span class="el-dropdown-link">
                {{ currentUser.name || '管理员' }}
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleUserCenter">个人中心</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 主内容 -->
        <el-main class="app-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Setting,
  Cpu,
  Tools,
  Document,
  Menu,
  Expand,
  Fold,
  ArrowDown
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

// 使用stores
const appStore = useAppStore()
const userStore = useUserStore()
const permissionStore = usePermissionStore()

// 计算属性
const sidebarWidth = computed(() => appStore.sidebarOpened ? '210px' : '64px')
const activeMenu = computed(() => route.path)
const currentRoute = computed(() => route)
const isCollapse = computed(() => !appStore.sidebarOpened)
const currentUser = computed(() => userStore.user)

// 从路由配置中获取菜单
const routes = computed(() => {
  // 这里应该从router实例中获取路由配置
  // 为了演示，先使用静态配置
  return [
    {
      path: '/system-manage/properties-manage',
      meta: { title: '属性维护', icon: 'Tools' },
      children: []
    },
    {
      path: '/system-manage/role-manage',
      meta: { title: '角色管理', icon: 'UserFilled' },
      children: []
    },
    {
      path: '/ai-management',
      meta: { title: 'AI管理配置', icon: 'Cpu' },
      children: [
        {
          path: 'model-config',
          meta: { title: 'AI模型配置', icon: 'Setting' }
        },
        {
          path: 'conversation-config',
          meta: { title: '对话配置', icon: 'ChatDotRound' }
        }
      ]
    }
  ]
})

// 方法
const toggleSidebar = () => {
  appStore.toggleSidebar()
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await userStore.logout()
    router.push('/login')
  } catch (error) {
    // 用户取消操作
  }
}

const handleUserCenter = () => {
  ElMessage.info('个人中心功能开发中...')
}

// 生命周期
onMounted(() => {
  // 组件挂载时可以进行一些初始化操作
})
</script>

<style scoped>
.app-wrapper {
  position: relative;
  height: 100vh;
  width: 100%;
}

.layout-container {
  height: 100%;
}

.sidebar-container {
  background-color: #304156;
  transition: width 0.28s;
}

.logo-container {
  height: 50px;
  line-height: 50px;
  text-align: center;
  background-color: #2b2f3a;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
}

.sidebar-menu {
  border: none;
  height: calc(100vh - 50px);
  overflow-y: auto;
}

.navbar {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 50px !important;
}

.navbar-left {
  display: flex;
  align-items: center;
}

.hamburger {
  margin-right: 20px;
  color: #5a5e66;
}

.navbar-right {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  cursor: pointer;
  color: #409EFF;
  display: flex;
  align-items: center;
}

.app-main {
  background-color: #f0f2f5;
  padding: 20px;
  min-height: calc(100vh - 50px);
}
</style>
