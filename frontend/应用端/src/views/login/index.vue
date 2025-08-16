<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 系统标题 -->
      <div class="system-header">
        <div class="system-logo">
          <el-icon><Setting /></el-icon>
        </div>
        <h1 class="system-title">QMS智能管理系统</h1>
        <p class="system-subtitle">AI驱动的智能化管理平台</p>
      </div>

      <!-- 登录表单 -->
      <div class="login-form-container">
        <el-tabs v-model="activeTab" class="login-tabs">
          <!-- 飞书登录标签页 -->
          <el-tab-pane label="飞书登录" name="feishu">
            <div class="feishu-login-container">
              <FeishuLogin @login-success="handleLoginSuccess" @login-error="handleLoginError" />
              <el-divider>或</el-divider>
              <p class="switch-tip">
                <el-button type="text" @click="activeTab = 'login'">使用账号密码登录</el-button>
              </p>
            </div>
          </el-tab-pane>

          <!-- 登录标签页 -->
          <el-tab-pane label="账号登录" name="login">
            <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" class="login-form">
              <el-form-item prop="username">
                <el-input
                  v-model="loginForm.username"
                  placeholder="请输入用户名"
                  prefix-icon="User"
                  size="large"
                />
              </el-form-item>
              <el-form-item prop="password">
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  prefix-icon="Lock"
                  size="large"
                  show-password
                  @keyup.enter="handleLogin"
                />
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  size="large"
                  style="width: 100%"
                  @click="handleLogin"
                  :loading="loginLoading"
                >
                  登录
                </el-button>
              </el-form-item>
            </el-form>

            <!-- 演示账户提示 -->
            <div class="demo-accounts">
              <el-divider>演示账户</el-divider>
              <div class="demo-account-list">
                <el-button
                  type="text"
                  size="small"
                  @click="fillDemoAccount('admin')"
                  class="demo-account-btn"
                >
                  <el-icon><User /></el-icon>
                  管理员账户 (admin / admin123)
                </el-button>
              </div>
            </div>
          </el-tab-pane>

          <!-- 注册标签页 -->
          <el-tab-pane label="用户注册" name="register">
            <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" class="register-form">
              <el-form-item prop="username">
                <el-input
                  v-model="registerForm.username"
                  placeholder="请输入用户名"
                  prefix-icon="User"
                  size="large"
                />
              </el-form-item>
              <el-form-item prop="password">
                <el-input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="请输入密码"
                  prefix-icon="Lock"
                  size="large"
                  show-password
                />
              </el-form-item>
              <el-form-item prop="confirmPassword">
                <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  placeholder="请确认密码"
                  prefix-icon="Lock"
                  size="large"
                  show-password
                />
              </el-form-item>
              <el-form-item prop="realName">
                <el-input
                  v-model="registerForm.realName"
                  placeholder="请输入真实姓名"
                  prefix-icon="UserFilled"
                  size="large"
                />
              </el-form-item>
              <el-form-item prop="email">
                <el-input
                  v-model="registerForm.email"
                  placeholder="请输入邮箱"
                  prefix-icon="Message"
                  size="large"
                />
              </el-form-item>
              <el-form-item prop="phone">
                <el-input
                  v-model="registerForm.phone"
                  placeholder="请输入手机号"
                  prefix-icon="Phone"
                  size="large"
                />
              </el-form-item>
              <el-form-item prop="department">
                <el-select
                  v-model="registerForm.department"
                  placeholder="请选择部门"
                  size="large"
                  style="width: 100%"
                >
                  <el-option label="技术部" value="技术部" />
                  <el-option label="业务部" value="业务部" />
                  <el-option label="数据部" value="数据部" />
                  <el-option label="运营部" value="运营部" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button
                  type="success"
                  size="large"
                  style="width: 100%"
                  @click="handleRegister"
                  :loading="registerLoading"
                >
                  注册
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 演示账户 -->
      <div class="demo-accounts">
        <h4>演示账户</h4>
        <div class="account-list">
          <div
            v-for="user in demoUsers"
            :key="user.username"
            class="account-item"
            @click="selectDemoUser(user)"
          >
            <div class="account-info">
              <strong>{{ user.realName }}</strong>
              <span>{{ user.username }}</span>
              <small>{{ user.role }}</small>
            </div>
          </div>
        </div>
      </div>

      <!-- 系统特性 -->
      <div class="system-features">
        <div class="feature-item">
          <el-icon><ChatDotRound /></el-icon>
          <span>AI对话分析</span>
        </div>
        <div class="feature-item">
          <el-icon><DataBoard /></el-icon>
          <span>智能数据源管理</span>
        </div>
        <div class="feature-item">
          <el-icon><Setting /></el-icon>
          <span>配置驱动架构</span>
        </div>
        <div class="feature-item">
          <el-icon><User /></el-icon>
          <span>权限精细控制</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { login, register } from '@/api/auth'
import FeishuLogin from '@/components/FeishuLogin.vue'

const router = useRouter()
const route = useRoute()
const loginFormRef = ref()
const registerFormRef = ref()

// 当前活跃标签页
const activeTab = ref('feishu')

// 加载状态
const loginLoading = ref(false)
const registerLoading = ref(false)

// 登录表单
const loginForm = ref({
  username: '',
  password: ''
})

// 注册表单
const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  email: '',
  phone: '',
  department: ''
})

// 演示账户
const demoUsers = ref([
  {
    username: 'admin',
    password: 'admin123',
    realName: '系统管理员',
    role: '管理员'
  },
  {
    username: 'zhangsan',
    password: 'user123',
    realName: '张三',
    role: '普通用户'
  },
  {
    username: 'lisi',
    password: 'user123',
    realName: '李四',
    role: '数据分析师'
  }
])

// 表单验证规则
const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.value.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  department: [{ required: true, message: '请选择部门', trigger: 'change' }]
}

// 登录处理
const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loginLoading.value = true

    const response = await login(loginForm.value)

    if (response.success) {
      // 保存用户信息和token
      localStorage.setItem('qms_token', response.data.token)
      localStorage.setItem('qms_user', JSON.stringify(response.data.user))

      // 记录登录操作日志
      const { logActions } = await import('@/utils/operationLog')
      logActions.login({
        loginMethod: 'password',
        userAgent: navigator.userAgent,
        timestamp: new Date().toISOString()
      })

      ElMessage.success('登录成功')

      // 获取重定向路径
      const redirect = route.query.redirect || '/'
      router.push(decodeURIComponent(redirect))
    } else {
      ElMessage.error(response.message || '登录失败')
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error('登录失败，请检查网络连接')
  } finally {
    loginLoading.value = false
  }
}

// 注册处理
const handleRegister = async () => {
  try {
    await registerFormRef.value.validate()
    registerLoading.value = true

    const response = await register(registerForm.value)

    if (response.success) {
      ElMessage.success('注册成功，请等待管理员审批')
      // 切换到登录标签页
      activeTab.value = 'login'
      // 清空注册表单
      registerForm.value = {
        username: '',
        password: '',
        confirmPassword: '',
        realName: '',
        email: '',
        phone: '',
        department: ''
      }
    } else {
      ElMessage.error(response.message || '注册失败')
    }
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error('注册失败，请检查网络连接')
  } finally {
    registerLoading.value = false
  }
}

// 选择演示用户
const selectDemoUser = (user) => {
  loginForm.value.username = user.username
  loginForm.value.password = user.password
  activeTab.value = 'login'
  ElMessage.info(`已选择演示账户：${user.realName}`)
}

// 填充演示账户
const fillDemoAccount = (type) => {
  if (type === 'admin') {
    loginForm.value.username = 'admin'
    loginForm.value.password = 'admin123'
    ElMessage.info('已填充管理员账户信息')
  }
}

// 飞书登录成功处理
const handleLoginSuccess = ({ token, user }) => {
  console.log('飞书登录成功:', user)
  ElMessage.success(`欢迎回来，${user.username}！`)
  // 路由跳转由FeishuLogin组件处理
}

// 飞书登录失败处理
const handleLoginError = (error) => {
  console.error('飞书登录失败:', error)
  ElMessage.error('飞书登录失败，请重试')
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-container {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  width: 100%;
  max-width: 480px;
  overflow: hidden;
}

.system-header {
  background: linear-gradient(135deg, #409EFF, #66b1ff);
  color: white;
  text-align: center;
  padding: 40px 20px;

  .system-logo {
    width: 64px;
    height: 64px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
    font-size: 32px;
  }

  .system-title {
    margin: 0 0 8px 0;
    font-size: 28px;
    font-weight: 600;
  }

  .system-subtitle {
    margin: 0;
    font-size: 14px;
    opacity: 0.9;
  }
}

.login-form-container {
  padding: 40px;

  .login-tabs {
    :deep(.el-tabs__header) {
      margin-bottom: 30px;
    }

    :deep(.el-tabs__item) {
      font-size: 16px;
      font-weight: 500;
    }
  }

  .login-form,
  .register-form {
    .el-form-item {
      margin-bottom: 24px;
    }
  }

  .feishu-login-container {
    padding: 20px 0;

    .switch-tip {
      text-align: center;
      margin: 0;
      color: #606266;
      font-size: 14px;
    }
  }
}

.demo-accounts {
  padding: 20px 40px;
  border-top: 1px solid #ebeef5;
  background: #fafafa;

  h4 {
    margin: 0 0 16px 0;
    color: #606266;
    font-size: 14px;
    text-align: center;
  }

  .account-list {
    display: flex;
    gap: 12px;
    justify-content: center;
    flex-wrap: wrap;
  }

  .account-item {
    background: white;
    border: 1px solid #dcdfe6;
    border-radius: 8px;
    padding: 12px;
    cursor: pointer;
    transition: all 0.3s;
    min-width: 120px;
    text-align: center;

    &:hover {
      border-color: #409EFF;
      box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
    }

    .account-info {
      strong {
        display: block;
        color: #303133;
        font-size: 14px;
        margin-bottom: 4px;
      }

      span {
        display: block;
        color: #606266;
        font-size: 12px;
        margin-bottom: 2px;
      }

      small {
        color: #909399;
        font-size: 11px;
      }
    }
  }
}

.system-features {
  padding: 20px 40px;
  background: #f8f9fa;
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 16px;

  .feature-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    color: #606266;
    font-size: 12px;

    .el-icon {
      font-size: 24px;
      color: #409EFF;
    }
  }
}

@media (max-width: 768px) {
  .login-page {
    padding: 10px;
  }

  .login-container {
    max-width: 100%;
  }

  .system-header {
    padding: 30px 20px;

    .system-title {
      font-size: 24px;
    }
  }

  .login-form-container {
    padding: 30px 20px;
  }

  .demo-accounts {
    padding: 15px 20px;

    .account-list {
      gap: 8px;
    }

    .account-item {
      min-width: 100px;
      padding: 8px;
    }
  }

  .demo-accounts {
    margin-top: 20px;

    .demo-account-list {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .demo-account-btn {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 12px;
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      background: #f9f9f9;
      color: #606266;
      font-size: 12px;
      transition: all 0.3s;

      &:hover {
        background: #ecf5ff;
        border-color: #409eff;
        color: #409eff;
      }
    }
  }

  .system-features {
    padding: 15px 20px;

    .feature-item {
      font-size: 11px;

      .el-icon {
        font-size: 20px;
      }
    }
  }
}
</style>
