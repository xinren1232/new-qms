import { defineStore } from 'pinia'
import { ref, reactive, computed } from 'vue'
import Cookies from 'js-cookie'
import { ElMessage } from 'element-plus'

// 用户信息接口
export interface UserInfo {
  uid: string
  employeeNo: string
  name: string
  deptId: string
  deptName: string
  email?: string
  phone?: string
  avatar?: string
  roles?: string[]
  permissions?: string[]
}

export const useUserStore = defineStore('user', () => {
  // 状态定义
  const user = ref<UserInfo>({
    uid: '',
    employeeNo: '',
    name: '',
    deptId: '',
    deptName: ''
  })
  
  const token = ref(localStorage.getItem('token') || '')
  const rtoken = ref(localStorage.getItem('rtoken') || '')
  const userMap = ref<Record<string, UserInfo>>({})
  const userInfoMap = ref<Record<string, any>>({})
  const loading = ref(false)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value && !!user.value.uid)
  const userName = computed(() => user.value.name)
  const userEmployeeNo = computed(() => user.value.employeeNo)
  const employeeNo = computed(() => user.value.employeeNo) // 添加employeeNo计算属性
  const userDepartment = computed(() => user.value.deptName)
  const userAvatar = computed(() => user.value.avatar || generateDefaultAvatar(user.value.name))

  // 生成默认头像
  const generateDefaultAvatar = (name: string) => {
    if (!name) return ''
    const firstChar = name.charAt(0).toUpperCase()
    return `data:image/svg+xml;base64,${btoa(`
      <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 40 40">
        <circle cx="20" cy="20" r="20" fill="#409EFF"/>
        <text x="20" y="26" text-anchor="middle" fill="white" font-size="16" font-family="Arial">${firstChar}</text>
      </svg>
    `)}`
  }

  // 设置用户信息
  const setUser = (userInfo: UserInfo) => {
    user.value = { ...userInfo }
    Cookies.set('user', JSON.stringify(userInfo))
  }

  // 设置Token
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  // 设置刷新Token
  const setRToken = (newRToken: string) => {
    rtoken.value = newRToken
    localStorage.setItem('rtoken', newRToken)
  }

  // 设置用户映射
  const setUserMap = (map: Record<string, UserInfo>) => {
    userMap.value = { ...map }
  }

  // 设置用户信息映射
  const setUserInfoMap = (map: Record<string, any>) => {
    userInfoMap.value = { ...map }
  }

  // 获取当前用户信息
  const getCurrentUser = async () => {
    // 如果已经有用户信息，直接返回
    if (user.value.name && user.value.employeeNo) {
      return user.value
    }

    try {
      loading.value = true
      // 这里应该调用实际的API
      // const response = await getCurrentUserAPI()
      
      // 模拟API响应
      const mockUser: UserInfo = {
        uid: '1001',
        employeeNo: 'EMP001',
        name: '管理员',
        deptId: 'DEPT001',
        deptName: '系统管理部',
        email: 'admin@qms.com',
        phone: '13800138000',
        roles: ['ADMIN'],
        permissions: ['*']
      }
      
      setUser(mockUser)
      return mockUser
    } catch (error) {
      console.error('获取用户信息失败:', error)
      ElMessage.error('获取用户信息失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 用户登录
  const login = async (credentials: { username: string; password: string }) => {
    try {
      loading.value = true
      // 这里应该调用实际的登录API
      // const response = await loginAPI(credentials)
      
      // 模拟登录响应
      const mockResponse = {
        token: 'mock-jwt-token-' + Date.now(),
        rtoken: 'mock-refresh-token-' + Date.now(),
        user: {
          uid: '1001',
          employeeNo: credentials.username,
          name: '管理员',
          deptId: 'DEPT001',
          deptName: '系统管理部',
          email: 'admin@qms.com',
          roles: ['ADMIN'],
          permissions: ['*']
        }
      }
      
      setToken(mockResponse.token)
      setRToken(mockResponse.rtoken)
      setUser(mockResponse.user)
      
      ElMessage.success('登录成功')
      return mockResponse
    } catch (error) {
      console.error('登录失败:', error)
      ElMessage.error('登录失败，请检查用户名和密码')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 用户登出
  const logout = async () => {
    try {
      // 这里可以调用登出API
      // await logoutAPI()
      
      // 清除本地状态
      user.value = {
        uid: '',
        employeeNo: '',
        name: '',
        deptId: '',
        deptName: ''
      }
      token.value = ''
      rtoken.value = ''
      
      // 清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('rtoken')
      Cookies.remove('user')
      
      ElMessage.success('已退出登录')
    } catch (error) {
      console.error('登出失败:', error)
      ElMessage.error('登出失败')
    }
  }

  // 刷新Token
  const refreshToken = async () => {
    try {
      if (!rtoken.value) {
        throw new Error('没有刷新Token')
      }
      
      // 这里应该调用刷新Token的API
      // const response = await refreshTokenAPI(rtoken.value)
      
      // 模拟刷新Token响应
      const mockResponse = {
        token: 'refreshed-jwt-token-' + Date.now(),
        rtoken: 'refreshed-refresh-token-' + Date.now()
      }
      
      setToken(mockResponse.token)
      setRToken(mockResponse.rtoken)
      
      return mockResponse.token
    } catch (error) {
      console.error('刷新Token失败:', error)
      // Token刷新失败，需要重新登录
      await logout()
      throw error
    }
  }

  // 获取用户列表
  const getUsers = async (employeeNos: string[]) => {
    try {
      // 这里应该调用获取用户列表的API
      // const response = await getUsersAPI(employeeNos)
      
      // 模拟用户列表响应
      const mockUsers = employeeNos.map(no => ({
        uid: `uid-${no}`,
        employeeNo: no,
        name: `用户-${no}`,
        deptId: 'DEPT001',
        deptName: '系统管理部'
      }))
      
      // 更新用户映射
      const newUserMap = { ...userMap.value }
      mockUsers.forEach(user => {
        newUserMap[user.employeeNo] = user
      })
      setUserMap(newUserMap)
      
      return mockUsers
    } catch (error) {
      console.error('获取用户列表失败:', error)
      throw error
    }
  }

  // 初始化用户状态
  const initializeUser = () => {
    // 从Cookie恢复用户信息
    const savedUser = Cookies.get('user')
    if (savedUser) {
      try {
        const userInfo = JSON.parse(savedUser)
        user.value = userInfo
      } catch (error) {
        console.error('解析用户信息失败:', error)
        Cookies.remove('user')
      }
    }

    // 从localStorage恢复Token
    const savedToken = localStorage.getItem('token')
    if (savedToken) {
      token.value = savedToken
    }

    const savedRToken = localStorage.getItem('rtoken')
    if (savedRToken) {
      rtoken.value = savedRToken
    }
  }

  return {
    // 状态
    user,
    token,
    rtoken,
    userMap,
    userInfoMap,
    loading,
    
    // 计算属性
    isLoggedIn,
    userName,
    userEmployeeNo,
    employeeNo,
    userDepartment,
    userAvatar,
    
    // 方法
    setUser,
    setToken,
    setRToken,
    setUserMap,
    setUserInfoMap,
    getCurrentUser,
    login,
    logout,
    refreshToken,
    getUsers,
    initializeUser
  }
})
