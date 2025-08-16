<template>
  <div class="login-container">
    <div class="login-wrapper">
      <!-- 左侧品牌区域 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="logo">
            <el-icon size="48"><Setting /></el-icon>
          </div>
          <h1 class="brand-title">QMS-AI</h1>
          <h2 class="brand-subtitle">智能质量管理系统</h2>
          <p class="brand-description">
            基于配置驱动架构的现代化智能质量管理系统，
            提供AI场景应用、质量检测、预测分析等功能
          </p>
          <div class="security-notice">
            <el-icon><Lock /></el-icon>
            <span>安全登录，保护您的数据</span>
          </div>
          <div class="features">
            <div class="feature-item">
              <el-icon><Cpu /></el-icon>
              <span>AI智能分析</span>
            </div>
            <div class="feature-item">
              <el-icon><Monitor /></el-icon>
              <span>实时质量监控</span>
            </div>
            <div class="feature-item">
              <el-icon><DataAnalysis /></el-icon>
              <span>数据驱动决策</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录区域 -->
      <div class="login-section">
        <div class="login-form-wrapper">
          <div class="form-header">
            <h3>{{ isLoginMode ? '用户登录' : '用户注册' }}</h3>
            <p>{{ isLoginMode ? '欢迎回来，请登录您的账户' : '创建新账户，开始使用QMS-AI' }}</p>
            <div v-if="isLoginMode" class="login-notice">
              <el-alert
                title="安全提示"
                description="为了保护您的数据安全和隐私，QMS-AI系统要求所有用户必须登录后才能使用。"
                type="info"
                :closable="false"
                show-icon
              />
            </div>
          </div>

          <!-- 登录表单 -->
          <el-form
            v-if="isLoginMode"
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            @submit.prevent="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名或邮箱"
                size="large"
                :prefix-icon="User"
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                :prefix-icon="Lock"
                show-password
                clearable
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            
            <el-form-item>
              <div class="form-options">
                <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
                <div class="help-links">
                  <el-link type="primary" @click="showForgotPassword">忘记密码？</el-link>
                  <el-link type="info" @click="goToHelp">登录帮助</el-link>
                </div>
              </div>
            </el-form-item>
            
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="authStore.loginLoading"
                @click="handleLogin"
                class="login-button"
              >
                {{ authStore.loginLoading ? '登录中...' : '登录' }}
              </el-button>
            </el-form-item>
          </el-form>

          <!-- 注册表单 -->
          <el-form
            v-else
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            class="register-form"
            @submit.prevent="handleRegister"
          >
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item prop="username">
                  <el-input
                    v-model="registerForm.username"
                    placeholder="用户名"
                    size="large"
                    :prefix-icon="User"
                    clearable
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item prop="realName">
                  <el-input
                    v-model="registerForm.realName"
                    placeholder="真实姓名"
                    size="large"
                    :prefix-icon="UserFilled"
                    clearable
                  />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item prop="email">
              <el-input
                v-model="registerForm.email"
                placeholder="邮箱地址"
                size="large"
                :prefix-icon="Message"
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="phone">
              <el-input
                v-model="registerForm.phone"
                placeholder="手机号码"
                size="large"
                :prefix-icon="Phone"
                clearable
              />
            </el-form-item>
            
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item prop="department">
                  <el-input
                    v-model="registerForm.department"
                    placeholder="部门"
                    size="large"
                    :prefix-icon="OfficeBuilding"
                    clearable
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item prop="position">
                  <el-input
                    v-model="registerForm.position"
                    placeholder="职位"
                    size="large"
                    :prefix-icon="Briefcase"
                    clearable
                  />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="密码"
                size="large"
                :prefix-icon="Lock"
                show-password
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="确认密码"
                size="large"
                :prefix-icon="Lock"
                show-password
                clearable
                @keyup.enter="handleRegister"
              />
            </el-form-item>
            
            <el-form-item>
              <el-checkbox v-model="registerForm.agreeTerms">
                我已阅读并同意
                <el-link type="primary" @click="showTerms">《用户协议》</el-link>
                和
                <el-link type="primary" @click="showPrivacy">《隐私政策》</el-link>
              </el-checkbox>
            </el-form-item>
            
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="authStore.registerLoading"
                @click="handleRegister"
                class="register-button"
              >
                {{ authStore.registerLoading ? '注册中...' : '注册账户' }}
              </el-button>
            </el-form-item>
          </el-form>

          <!-- 切换登录/注册 -->
          <div class="form-footer">
            <span v-if="isLoginMode">
              还没有账户？
              <el-link type="primary" @click="toggleMode">立即注册</el-link>
            </span>
            <span v-else>
              已有账户？
              <el-link type="primary" @click="toggleMode">立即登录</el-link>
            </span>
          </div>

          <!-- 快速登录提示 -->
          <div v-if="isLoginMode" class="quick-login-tips">
            <el-divider>快速体验</el-divider>
            <div class="demo-accounts">
              <p class="tips-title">演示账户（可直接使用）：</p>
              <div class="demo-account-list">
                <div class="demo-account" @click="fillDemoAccount('admin')">
                  <strong>管理员：</strong> admin / admin123
                </div>
                <div class="demo-account" @click="fillDemoAccount('developer')">
                  <strong>开发者：</strong> developer / dev123
                </div>
                <div class="demo-account" @click="fillDemoAccount('quality')">
                  <strong>质量工程师：</strong> quality / quality123
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 忘记密码对话框 -->
    <el-dialog
      v-model="forgotPasswordVisible"
      title="找回密码"
      width="400px"
      :before-close="closeForgotPassword"
    >
      <el-form :model="forgotPasswordForm" :rules="forgotPasswordRules" ref="forgotPasswordFormRef">
        <el-form-item prop="email">
          <el-input
            v-model="forgotPasswordForm.email"
            placeholder="请输入注册邮箱"
            :prefix-icon="Message"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeForgotPassword">取消</el-button>
        <el-button type="primary" @click="handleForgotPassword">发送重置邮件</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import {
  User,
  Lock,
  Message,
  Phone,
  UserFilled,
  OfficeBuilding,
  Briefcase,
  Setting,
  Cpu,
  Monitor,
  DataAnalysis
} from '@element-plus/icons-vue'

// 路由和store
const router = useRouter()
const authStore = useAuthStore()

// 表单模式
const isLoginMode = ref(true)

// 表单引用
const loginFormRef = ref()
const registerFormRef = ref()
const forgotPasswordFormRef = ref()

// 登录表单
const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

// 注册表单
const registerForm = reactive({
  username: '',
  realName: '',
  email: '',
  phone: '',
  department: '',
  position: '',
  password: '',
  confirmPassword: '',
  agreeTerms: false
})

// 忘记密码表单
const forgotPasswordForm = reactive({
  email: ''
})

const forgotPasswordVisible = ref(false)

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3到20个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const forgotPasswordRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

// 方法
const toggleMode = () => {
  isLoginMode.value = !isLoginMode.value
  // 清空表单
  if (isLoginMode.value) {
    Object.assign(loginForm, { username: '', password: '', rememberMe: false })
  } else {
    Object.assign(registerForm, {
      username: '',
      realName: '',
      email: '',
      phone: '',
      department: '',
      position: '',
      password: '',
      confirmPassword: '',
      agreeTerms: false
    })
  }
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  const result = await authStore.login(loginForm)
  if (result.success) {
    ElMessage.success('登录成功！')
    // 获取重定向路径
    const redirect = router.currentRoute.value.query.redirect || '/dashboard'
    router.push(redirect)
  }
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  if (!registerForm.agreeTerms) {
    ElMessage.warning('请先同意用户协议和隐私政策')
    return
  }
  
  const result = await authStore.register(registerForm)
  if (result.success) {
    ElMessage.success('注册成功！请登录')
    isLoginMode.value = true
  }
}

const showForgotPassword = () => {
  forgotPasswordVisible.value = true
}

const closeForgotPassword = () => {
  forgotPasswordVisible.value = false
  Object.assign(forgotPasswordForm, { email: '' })
}

const handleForgotPassword = () => {
  ElMessage.info('重置密码邮件已发送，请查收邮箱')
  closeForgotPassword()
}

const showTerms = () => {
  ElMessage.info('用户协议页面')
}

const showPrivacy = () => {
  ElMessage.info('隐私政策页面')
}

// 快速填充演示账户
const fillDemoAccount = (type) => {
  const accounts = {
    admin: { username: 'admin', password: 'admin123' },
    developer: { username: 'developer', password: 'dev123' },
    quality: { username: 'quality', password: 'quality123' }
  }

  const account = accounts[type]
  if (account) {
    loginForm.username = account.username
    loginForm.password = account.password
    ElMessage.success(`已填充${type === 'admin' ? '管理员' : type === 'developer' ? '开发者' : '质量工程师'}账户信息`)
  }
}

// 跳转到帮助页面
const goToHelp = () => {
  router.push('/help/login')
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-wrapper {
  display: flex;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  max-width: 1000px;
  width: 100%;
  min-height: 600px;
}

.brand-section {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 60px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-content {
  text-align: center;
  max-width: 400px;
}

.logo {
  margin-bottom: 20px;
}

.brand-title {
  font-size: 48px;
  font-weight: bold;
  margin: 0 0 8px 0;
  background: linear-gradient(45deg, #fff, #e0e7ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.brand-subtitle {
  font-size: 20px;
  margin: 0 0 20px 0;
  opacity: 0.9;
}

.brand-description {
  font-size: 16px;
  line-height: 1.6;
  opacity: 0.8;
  margin-bottom: 40px;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  opacity: 0.9;
}

.login-section {
  flex: 1;
  padding: 60px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-form-wrapper {
  width: 100%;
  max-width: 400px;
}

.form-header {
  text-align: center;
  margin-bottom: 40px;
}

.form-header h3 {
  font-size: 28px;
  color: #303133;
  margin: 0 0 8px 0;
}

.form-header p {
  color: #909399;
  margin: 0;
}

.login-form,
.register-form {
  margin-bottom: 20px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.help-links {
  display: flex;
  gap: 12px;
}

.login-button,
.register-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
}

.form-footer {
  text-align: center;
  color: #909399;
}

.security-notice {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 20px;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  font-size: 14px;
  opacity: 0.9;
}

.login-notice {
  margin-top: 20px;
}

.quick-login-tips {
  margin-top: 30px;
  padding-top: 20px;
}

.tips-title {
  font-size: 14px;
  color: #606266;
  margin: 0 0 12px 0;
  text-align: center;
}

.demo-account-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.demo-account {
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 13px;
  color: #606266;
}

.demo-account:hover {
  background: #e6f7ff;
  color: #1890ff;
  transform: translateY(-1px);
}

@media (max-width: 768px) {
  .login-wrapper {
    flex-direction: column;
    max-width: 400px;
  }
  
  .brand-section {
    padding: 40px 20px;
  }
  
  .login-section {
    padding: 40px 20px;
  }
  
  .brand-title {
    font-size: 36px;
  }
  
  .brand-subtitle {
    font-size: 18px;
  }
}
</style>
