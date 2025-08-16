<template>
  <div class="feishu-login">
    <!-- 飞书登录按钮 -->
    <el-button 
      type="primary" 
      size="large"
      style="width: 100%; margin-bottom: 16px;"
      @click="handleFeishuLogin"
      :loading="feishuLoading"
    >
      <template #icon>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
        </svg>
      </template>
      飞书一键登录
    </el-button>
    
    <!-- 开发环境模拟登录 -->
    <div v-if="isDev" class="dev-mock-login">
      <el-divider>开发环境</el-divider>
      <el-button 
        type="success" 
        size="large"
        style="width: 100%;"
        @click="handleMockLogin"
        :loading="mockLoading"
      >
        模拟飞书登录
      </el-button>
    </div>
    
    <!-- 登录状态提示 -->
    <div v-if="loginStatus" class="login-status">
      <el-alert
        :title="loginStatus.title"
        :description="loginStatus.description"
        :type="loginStatus.type"
        :closable="false"
        show-icon
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const route = useRoute()

// 响应式数据
const feishuLoading = ref(false)
const mockLoading = ref(false)
const loginStatus = ref(null)
const isDev = ref(import.meta.env.MODE === 'development')

// 飞书服务配置
const FEISHU_SERVICE_URL = import.meta.env.MODE === 'development'
  ? 'http://localhost:8085'
  : import.meta.env.VITE_APP_FEISHU_SERVICE_URL

// 事件定义
const emit = defineEmits(['login-success', 'login-error'])

// 飞书登录处理
const handleFeishuLogin = async () => {
  try {
    feishuLoading.value = true
    loginStatus.value = {
      title: '正在跳转到飞书登录...',
      description: '请在弹出的飞书页面完成登录',
      type: 'info'
    }
    
    // 获取飞书登录URL
    const response = await axios.get(`${FEISHU_SERVICE_URL}/auth/feishu/login-url`)
    
    if (response.data.success) {
      // 跳转到飞书登录页面
      window.location.href = response.data.data.loginUrl
    } else {
      throw new Error(response.data.message || '获取登录URL失败')
    }
  } catch (error) {
    console.error('飞书登录失败:', error)
    loginStatus.value = {
      title: '登录失败',
      description: error.message || '无法连接到飞书服务',
      type: 'error'
    }
    emit('login-error', error)
  } finally {
    feishuLoading.value = false
  }
}

// 模拟登录处理 (开发环境)
const handleMockLogin = async () => {
  try {
    mockLoading.value = true
    
    const mockUserData = {
      userId: 'dev_user_001',
      name: '开发测试用户',
      email: 'dev@company.com'
    }
    
    const response = await axios.post(`${FEISHU_SERVICE_URL}/auth/feishu/mock-login`, mockUserData)
    
    if (response.data.success) {
      // 保存token和用户信息
      const { token, user } = response.data.data
      localStorage.setItem('token', token)
      localStorage.setItem('userInfo', JSON.stringify(user))
      
      loginStatus.value = {
        title: '模拟登录成功',
        description: '正在跳转到系统首页...',
        type: 'success'
      }
      
      emit('login-success', { token, user })
      
      // 延迟跳转
      setTimeout(() => {
        router.push('/')
      }, 1000)
    } else {
      throw new Error(response.data.message || '模拟登录失败')
    }
  } catch (error) {
    console.error('模拟登录失败:', error)
    loginStatus.value = {
      title: '模拟登录失败',
      description: error.message,
      type: 'error'
    }
    emit('login-error', error)
  } finally {
    mockLoading.value = false
  }
}

// 处理飞书回调
const handleFeishuCallback = () => {
  const token = route.query.token
  const error = route.query.message
  
  if (token) {
    // 登录成功
    localStorage.setItem('token', token)
    
    // 获取用户信息
    getUserInfo(token)
  } else if (error) {
    // 登录失败
    loginStatus.value = {
      title: '飞书登录失败',
      description: decodeURIComponent(error),
      type: 'error'
    }
    emit('login-error', new Error(error))
  }
}

// 获取用户信息
const getUserInfo = async (token) => {
  try {
    const response = await axios.get(`${FEISHU_SERVICE_URL}/auth/userinfo`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (response.data.success) {
      const user = response.data.data
      localStorage.setItem('userInfo', JSON.stringify(user))
      
      loginStatus.value = {
        title: '登录成功',
        description: `欢迎回来，${user.username}！`,
        type: 'success'
      }
      
      emit('login-success', { token, user })
      
      // 跳转到首页
      setTimeout(() => {
        router.push('/')
      }, 1000)
    } else {
      throw new Error(response.data.message || '获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    loginStatus.value = {
      title: '获取用户信息失败',
      description: error.message,
      type: 'error'
    }
    emit('login-error', error)
  }
}

// 组件挂载
onMounted(() => {
  // 检查是否是飞书回调
  if (route.path === '/auth/success' || route.path === '/auth/error') {
    handleFeishuCallback()
  }
})
</script>

<style lang="scss" scoped>
.feishu-login {
  .dev-mock-login {
    margin-top: 16px;
    padding: 16px;
    background: #f5f7fa;
    border-radius: 8px;
    border: 1px dashed #dcdfe6;
  }
  
  .login-status {
    margin-top: 16px;
  }
  
  :deep(.el-button) {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
  }
}
</style>
