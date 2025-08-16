<template>
  <div class="menu-debug">
    <h2>菜单权限调试页面</h2>
    
    <el-card title="用户信息">
      <pre>{{ JSON.stringify(userInfo, null, 2) }}</pre>
    </el-card>
    
    <el-card title="路由配置" style="margin-top: 20px;">
      <div v-for="route in allRoutes" :key="route.path" style="margin-bottom: 10px;">
        <h4>{{ route.path }} - {{ route.meta?.title }}</h4>
        <p>Hidden: {{ route.meta?.hidden }}</p>
        <p>Roles: {{ JSON.stringify(route.meta?.roles) }}</p>
        <p>Has Children: {{ !!route.children }}</p>
        <p>Has Permission: {{ hasMenuPermission(route) }}</p>
        <hr>
      </div>
    </el-card>
    
    <el-card title="过滤后的路由" style="margin-top: 20px;">
      <div v-for="route in filteredRoutes" :key="route.path" style="margin-bottom: 10px;">
        <h4>{{ route.path }} - {{ route.meta?.title }}</h4>
        <p>Children Count: {{ route.children?.length || 0 }}</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getUser } from '@/utils/auth'

const router = useRouter()
const userInfo = ref({})

// 获取用户信息
const getUserInfo = () => {
  try {
    const userStr = localStorage.getItem('qms_user')
    if (userStr) {
      userInfo.value = JSON.parse(userStr)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 检查菜单权限
const hasMenuPermission = (route) => {
  const user = userInfo.value
  if (!user) return false
  
  // 检查角色权限
  if (route.meta?.roles) {
    const userRoles = user.roles || []
    const allowedRoles = Array.isArray(route.meta.roles) ? route.meta.roles : [route.meta.roles]
    const hasRole = userRoles.some(role => allowedRoles.includes(role))
    if (!hasRole) return false
  }
  
  return true
}

// 所有路由
const allRoutes = computed(() => {
  return router.options.routes
})

// 过滤后的路由
const filteredRoutes = computed(() => {
  return router.options.routes.filter(route => 
    !route.meta?.hidden && route.children && hasMenuPermission(route)
  )
})

onMounted(() => {
  getUserInfo()
})
</script>

<style scoped>
.menu-debug {
  padding: 20px;
}

pre {
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
}

hr {
  margin: 10px 0;
  border: none;
  border-top: 1px solid #eee;
}
</style>
