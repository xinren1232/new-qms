import { defineStore } from 'pinia'
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'

export const useAuthStore = defineStore('auth', () => {
  // 用户状态
  const user = ref(null)
  const token = ref(localStorage.getItem('qms_token') || null)
  const isLoggedIn = computed(() => !!token.value && !!user.value)
  
  // 加载状态
  const loading = ref(false)
  const loginLoading = ref(false)
  const registerLoading = ref(false)
  
  // 权限相关
  const permissions = ref([])
  const roles = ref([])
  
  // 用户角色枚举
  const USER_ROLES = {
    ADMIN: 'ADMIN',
    MANAGER: 'MANAGER',
    USER: 'USER'
  }
  
  // 权限枚举
  const PERMISSIONS = {
    // 用户管理
    USER_VIEW: 'user:view',
    USER_CREATE: 'user:create',
    USER_EDIT: 'user:edit',
    USER_DELETE: 'user:delete',
    
    // AI对话管理
    CONVERSATION_VIEW: 'conversation:view',
    CONVERSATION_CREATE: 'conversation:create',
    CONVERSATION_DELETE: 'conversation:delete',
    CONVERSATION_EXPORT: 'conversation:export',
    
    // 数据源管理
    DATASOURCE_VIEW: 'datasource:view',
    DATASOURCE_CREATE: 'datasource:create',
    DATASOURCE_EDIT: 'datasource:edit',
    DATASOURCE_DELETE: 'datasource:delete',
    
    // AI规则管理
    AI_RULE_VIEW: 'ai_rule:view',
    AI_RULE_CREATE: 'ai_rule:create',
    AI_RULE_EDIT: 'ai_rule:edit',
    AI_RULE_DELETE: 'ai_rule:delete',
    
    // 系统管理
    SYSTEM_CONFIG: 'system:config',
    SYSTEM_MONITOR: 'system:monitor'
  }
  
  // 计算属性 - 检查是否为管理员
  const isAdmin = computed(() => {
    return roles.value.includes(USER_ROLES.ADMIN)
  })
  
  // 计算属性 - 检查是否为管理者（管理员或经理）
  const isManager = computed(() => {
    return roles.value.includes(USER_ROLES.ADMIN) || roles.value.includes(USER_ROLES.MANAGER)
  })
  
  // 初始化认证状态
  const initializeAuth = async () => {
    if (!token.value) {
      console.log('🔐 没有token，跳过认证初始化')
      return false
    }

    loading.value = true
    try {
      // 使用简化的API调用，直接使用fetch
      const response = await fetch('http://localhost:8083/auth/userinfo', {
        headers: {
          'Authorization': `Bearer ${token.value}`,
          'Content-Type': 'application/json'
        }
      })

      if (response.ok) {
        const result = await response.json()
        if (result.success) {
          user.value = result.data
          permissions.value = result.permissions || []
          roles.value = result.roles || []

          console.log('✅ 用户认证初始化成功:', user.value)
          return true
        }
      }

      // 如果获取用户信息失败，清除token
      logout()
      return false
    } catch (error) {
      console.error('❌ 用户认证初始化失败:', error)
      logout()
      return false
    } finally {
      loading.value = false
    }
  }
  
  // 用户登录
  const login = async (credentials) => {
    loginLoading.value = true
    try {
      const response = await fetch('http://localhost:8083/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(credentials)
      })

      const result = await response.json()

      if (result.success) {
        token.value = result.data.token
        user.value = result.data.user
        permissions.value = result.data.permissions || []
        roles.value = result.data.roles || []

        // 保存token到localStorage
        localStorage.setItem('qms_token', token.value)

        ElMessage.success('登录成功！')
        console.log('✅ 用户登录成功:', user.value)
        return { success: true, user: user.value }
      } else {
        ElMessage.error(result.message || '登录失败')
        return { success: false, message: result.message }
      }
    } catch (error) {
      console.error('❌ 登录失败:', error)
      ElMessage.error('登录失败，请检查网络连接')
      return { success: false, message: '网络错误' }
    } finally {
      loginLoading.value = false
    }
  }
  
  // 用户注册
  const register = async (userInfo) => {
    registerLoading.value = true
    try {
      const response = await fetch('http://localhost:8083/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(userInfo)
      })

      const result = await response.json()

      if (result.success) {
        ElMessage.success('注册成功！请登录')
        console.log('✅ 用户注册成功:', result.data)
        return { success: true, data: result.data }
      } else {
        ElMessage.error(result.message || '注册失败')
        return { success: false, message: result.message }
      }
    } catch (error) {
      console.error('❌ 注册失败:', error)
      ElMessage.error('注册失败，请检查网络连接')
      return { success: false, message: '网络错误' }
    } finally {
      registerLoading.value = false
    }
  }
  
  // 用户登出
  const logout = () => {
    user.value = null
    token.value = null
    permissions.value = []
    roles.value = []
    
    // 清除localStorage
    localStorage.removeItem('qms_token')
    
    ElMessage.info('已退出登录')
    console.log('✅ 用户已退出登录')
  }
  
  // 检查权限
  const hasPermission = (permission) => {
    if (isAdmin.value) return true // 管理员拥有所有权限
    return permissions.value.includes(permission)
  }
  
  // 检查角色
  const hasRole = (role) => {
    return roles.value.includes(role)
  }
  
  // 检查多个权限（需要全部拥有）
  const hasAllPermissions = (permissionList) => {
    if (isAdmin.value) return true
    return permissionList.every(permission => permissions.value.includes(permission))
  }
  
  // 检查多个权限（拥有其中一个即可）
  const hasAnyPermission = (permissionList) => {
    if (isAdmin.value) return true
    return permissionList.some(permission => permissions.value.includes(permission))
  }
  
  // 更新用户信息
  const updateUserInfo = async (newUserInfo) => {
    try {
      const response = await authAPI.updateUserInfo(newUserInfo)
      if (response.success) {
        user.value = { ...user.value, ...response.data }
        ElMessage.success('用户信息更新成功')
        return { success: true }
      } else {
        ElMessage.error(response.message || '更新失败')
        return { success: false, message: response.message }
      }
    } catch (error) {
      console.error('❌ 更新用户信息失败:', error)
      ElMessage.error('更新失败，请检查网络连接')
      return { success: false, message: '网络错误' }
    }
  }
  
  // 修改密码
  const changePassword = async (passwordData) => {
    try {
      const response = await authAPI.changePassword(passwordData)
      if (response.success) {
        ElMessage.success('密码修改成功')
        return { success: true }
      } else {
        ElMessage.error(response.message || '密码修改失败')
        return { success: false, message: response.message }
      }
    } catch (error) {
      console.error('❌ 修改密码失败:', error)
      ElMessage.error('密码修改失败，请检查网络连接')
      return { success: false, message: '网络错误' }
    }
  }
  
  return {
    // 状态
    user,
    token,
    isLoggedIn,
    loading,
    loginLoading,
    registerLoading,
    permissions,
    roles,
    
    // 计算属性
    isAdmin,
    isManager,
    
    // 常量
    USER_ROLES,
    PERMISSIONS,
    
    // 方法
    initializeAuth,
    login,
    register,
    logout,
    hasPermission,
    hasRole,
    hasAllPermissions,
    hasAnyPermission,
    updateUserInfo,
    changePassword
  }
})
