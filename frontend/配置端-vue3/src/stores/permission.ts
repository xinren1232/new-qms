import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from './user'

// 权限相关类型定义
export interface PermissionButton {
  code: string
  name: string
  icon?: string
}

export interface MenuPermission {
  id: string
  name: string
  path: string
  component?: string
  icon?: string
  children?: MenuPermission[]
  buttons?: PermissionButton[]
}

export const usePermissionStore = defineStore('permission', () => {
  // 状态定义
  const routes = ref<RouteRecordRaw[]>([])
  const addRoutes = ref<RouteRecordRaw[]>([])
  const buttons = ref<string[]>(['edit', 'search', 'del', 'view', 'add'])
  const buttonRoles = ref<string[]>([])
  const flowRoutes = ref<RouteRecordRaw[]>([])
  const appRoutes = ref<RouteRecordRaw[]>([])
  const systemManageRoutes = ref<RouteRecordRaw[]>([])
  const menuPermissions = ref<MenuPermission[]>([])
  const loading = ref(false)

  // 计算属性
  const allRoutes = computed(() => routes.value)
  const dynamicRoutes = computed(() => addRoutes.value)
  const hasPermission = computed(() => (permission: string) => {
    return buttons.value.includes(permission)
  })

  // 设置路由
  const setRoutes = (newRoutes: RouteRecordRaw[]) => {
    addRoutes.value = newRoutes
    routes.value = [...routes.value, ...newRoutes]
  }

  // 设置按钮权限
  const setButtons = (newButtons: string[]) => {
    buttons.value = [...newButtons]
  }

  // 设置流程路由
  const setFlowRoutes = (newRoutes: RouteRecordRaw[]) => {
    flowRoutes.value = [...newRoutes]
  }

  // 设置应用路由
  const setAppRoutes = (newRoutes: RouteRecordRaw[]) => {
    appRoutes.value = [...newRoutes]
  }

  // 设置系统管理路由
  const setSystemManageRoutes = (newRoutes: RouteRecordRaw[]) => {
    systemManageRoutes.value = [...newRoutes]
  }

  // 设置菜单权限
  const setMenuPermissions = (permissions: MenuPermission[]) => {
    menuPermissions.value = [...permissions]
  }

  // 检查路由权限
  const hasRoutePermission = (routePath: string): boolean => {
    // 在开发环境或管理员用户，允许访问所有路由
    const userStore = useUserStore()
    if (process.env.NODE_ENV === 'development' || 
        userStore.user.roles?.includes('ADMIN')) {
      return true
    }

    // 检查用户是否有访问该路由的权限
    const checkPermission = (permissions: MenuPermission[], path: string): boolean => {
      for (const permission of permissions) {
        if (permission.path === path) {
          return true
        }
        if (permission.children && checkPermission(permission.children, path)) {
          return true
        }
      }
      return false
    }

    return checkPermission(menuPermissions.value, routePath)
  }

  // 检查按钮权限
  const hasButtonPermission = (buttonCode: string, routePath?: string): boolean => {
    // 在开发环境或管理员用户，允许所有按钮操作
    const userStore = useUserStore()
    if (process.env.NODE_ENV === 'development' || 
        userStore.user.roles?.includes('ADMIN')) {
      return true
    }

    // 检查全局按钮权限
    if (buttons.value.includes(buttonCode)) {
      return true
    }

    // 如果指定了路由路径，检查该路由下的按钮权限
    if (routePath) {
      const findRoutePermission = (permissions: MenuPermission[], path: string): MenuPermission | null => {
        for (const permission of permissions) {
          if (permission.path === path) {
            return permission
          }
          if (permission.children) {
            const found = findRoutePermission(permission.children, path)
            if (found) return found
          }
        }
        return null
      }

      const routePermission = findRoutePermission(menuPermissions.value, routePath)
      if (routePermission?.buttons) {
        return routePermission.buttons.some(btn => btn.code === buttonCode)
      }
    }

    return false
  }

  // 过滤异步路由
  const filterAsyncRoutes = (routes: RouteRecordRaw[], permissions: MenuPermission[]): RouteRecordRaw[] => {
    const filteredRoutes: RouteRecordRaw[] = []

    routes.forEach(route => {
      const hasPermission = permissions.some(permission => 
        permission.path === route.path || 
        (route.name && permission.name === route.name)
      )

      if (hasPermission) {
        const filteredRoute = { ...route }
        
        // 递归过滤子路由
        if (route.children) {
          filteredRoute.children = filterAsyncRoutes(route.children, permissions)
        }
        
        filteredRoutes.push(filteredRoute)
      }
    })

    return filteredRoutes
  }

  // 生成路由
  const generateRoutes = async (asyncRoutes: RouteRecordRaw[]) => {
    try {
      loading.value = true
      const userStore = useUserStore()

      // 在开发环境或管理员用户，直接返回所有路由
      if (process.env.NODE_ENV === 'development' || 
          userStore.user.roles?.includes('ADMIN')) {
        setRoutes(asyncRoutes)
        return asyncRoutes
      }

      // 获取用户权限
      await userStore.getCurrentUser()
      
      // 这里应该调用获取权限菜单的API
      // const permissionResponse = await getPermissionMenuAPI(userStore.user.employeeNo)
      
      // 模拟权限数据
      const mockPermissions: MenuPermission[] = [
        {
          id: '1',
          name: '系统管理',
          path: '/system-manage',
          icon: 'Setting',
          children: [
            {
              id: '1-1',
              name: '属性维护',
              path: '/system-manage/properties-manage',
              icon: 'Tools',
              buttons: [
                { code: 'add', name: '新增' },
                { code: 'edit', name: '编辑' },
                { code: 'del', name: '删除' },
                { code: 'view', name: '查看' }
              ]
            },
            {
              id: '1-2',
              name: '角色管理',
              path: '/system-manage/role-manage',
              icon: 'UserFilled',
              buttons: [
                { code: 'add', name: '新增' },
                { code: 'edit', name: '编辑' },
                { code: 'del', name: '删除' }
              ]
            }
          ]
        },
        {
          id: '2',
          name: 'AI管理配置',
          path: '/ai-management',
          icon: 'Cpu',
          children: [
            {
              id: '2-1',
              name: 'AI模型配置',
              path: '/ai-management/model-config',
              icon: 'Setting',
              buttons: [
                { code: 'edit', name: '编辑' },
                { code: 'view', name: '查看' }
              ]
            }
          ]
        }
      ]

      setMenuPermissions(mockPermissions)
      
      // 过滤路由
      const accessedRoutes = filterAsyncRoutes(asyncRoutes, mockPermissions)
      setRoutes(accessedRoutes)
      
      return accessedRoutes
    } catch (error) {
      console.error('生成路由失败:', error)
      // 发生错误时，返回空路由
      setRoutes([])
      return []
    } finally {
      loading.value = false
    }
  }

  // 重置权限状态
  const resetPermissions = () => {
    routes.value = []
    addRoutes.value = []
    buttons.value = ['edit', 'search', 'del', 'view', 'add']
    buttonRoles.value = []
    flowRoutes.value = []
    appRoutes.value = []
    systemManageRoutes.value = []
    menuPermissions.value = []
  }

  return {
    // 状态
    routes,
    addRoutes,
    buttons,
    buttonRoles,
    flowRoutes,
    appRoutes,
    systemManageRoutes,
    menuPermissions,
    loading,
    
    // 计算属性
    allRoutes,
    dynamicRoutes,
    hasPermission,
    
    // 方法
    setRoutes,
    setButtons,
    setFlowRoutes,
    setAppRoutes,
    setSystemManageRoutes,
    setMenuPermissions,
    hasRoutePermission,
    hasButtonPermission,
    filterAsyncRoutes,
    generateRoutes,
    resetPermissions
  }
})
