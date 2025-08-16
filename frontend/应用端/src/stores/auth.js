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
  
  // 清除认证状态并重定向到登录页
  const redirectToLogin = () => {
    user.value = null
    token.value = null
    permissions.value = []
    roles.value = []

    // 清除localStorage
    localStorage.removeItem('qms_token')
    localStorage.removeItem('qms_user')
    localStorage.removeItem('qms_user_id')
    localStorage.removeItem('qms_user_name')
    localStorage.removeItem('qms_user_email')
    localStorage.removeItem('qms_user_department')
    localStorage.removeItem('qms_user_role')

    console.log('🔐 用户未认证，需要登录')
    return false
  }

  // 初始化认证状态
  const initializeAuth = async () => {
    if (!token.value) {
      console.log('🔐 没有token，需要用户登录')
      return redirectToLogin()
    }

    loading.value = true
    try {
      // 验证现有token的有效性
      const response = await fetch('/api/auth/userinfo', {
        headers: {
          'Authorization': `Bearer ${token.value}`,
          'Content-Type': 'application/json'
        }
      })

      if (response.ok) {
        const result = await response.json()
        if (result.success) {
          user.value = result.data
          permissions.value = result.data.permissions || []
          roles.value = result.data.roles || [result.data.role]

          // 更新localStorage中的用户信息
          localStorage.setItem('qms_user', JSON.stringify(user.value))
          localStorage.setItem('qms_user_id', user.value.id)
          localStorage.setItem('qms_user_name', user.value.username)
          localStorage.setItem('qms_user_email', user.value.email || '')
          localStorage.setItem('qms_user_department', user.value.department || '')
          localStorage.setItem('qms_user_role', user.value.role || 'USER')

          console.log('✅ 用户认证初始化成功:', user.value)
          return true
        }
      }

      // 如果获取用户信息失败，清除token并要求重新登录
      console.warn('⚠️ Token无效或已过期，需要重新登录')
      return redirectToLogin()
    } catch (error) {
      console.error('❌ 用户认证初始化失败:', error)
      return redirectToLogin()
    } finally {
      loading.value = false
    }
  }

  // 确保只初始化一次（避免在路由守卫中重复等待）
  const __initState = {
    done: false,
    pending: null
  }
  const ensureInitialized = async () => {
    if (__initState.done) return isLoggedIn.value
    if (__initState.pending) return __initState.pending
    __initState.pending = initializeAuth().then(ok => {
      __initState.done = true
      __initState.pending = null
      return ok
    }).catch(() => {
      __initState.done = true
      __initState.pending = null
      return false
    })
    return __initState.pending
  }

  // 用户登录
  const login = async (credentials) => {
    loginLoading.value = true
    try {
      const response = await fetch('/api/auth/login', {
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
        roles.value = result.data.roles || [result.data.user.role]

        // 保存认证信息到localStorage
        localStorage.setItem('qms_token', token.value)
        localStorage.setItem('qms_user', JSON.stringify(user.value))
        localStorage.setItem('qms_user_id', user.value.id)
        localStorage.setItem('qms_user_name', user.value.username)
        localStorage.setItem('qms_user_email', user.value.email || '')
        localStorage.setItem('qms_user_department', user.value.department || '')
        localStorage.setItem('qms_user_role', user.value.role || 'USER')

        ElMessage.success('登录成功！欢迎使用QMS-AI系统')
        console.log('✅ 用户登录成功:', user.value)
        return { success: true, user: user.value }
      } else {
        ElMessage.error(result.message || '登录失败，请检查用户名和密码')
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
      const response = await fetch('/api/auth/register', {
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
  const logout = async () => {
    try {
      // 调用后端登出API
      if (token.value) {
        await fetch('/api/auth/logout', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token.value}`,
            'Content-Type': 'application/json'
          }
        })
      }
    } catch (error) {
      console.warn('⚠️ 登出API调用失败:', error)
    }

    // 清除本地状态
    user.value = null
    token.value = null
    permissions.value = []
    roles.value = []

    // 清除localStorage
    localStorage.removeItem('qms_token')
    localStorage.removeItem('qms_user')
    localStorage.removeItem('qms_user_id')
    localStorage.removeItem('qms_user_name')
    localStorage.removeItem('qms_user_email')
    localStorage.removeItem('qms_user_department')
    localStorage.removeItem('qms_user_role')

    ElMessage.info('已安全退出登录')
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
    ensureInitialized,
    redirectToLogin,
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
