<template>
  <div class="login-container">
    <!-- 背景动画效果 -->
    <div class="background-animation">
      <!-- 科技圆环 -->
      <div class="tech-circles">
        <div class="circle circle-1" />
        <div class="circle circle-2" />
        <div class="circle circle-3" />
        <div class="circle circle-4" />
      </div>

      <!-- 数据流线条 -->
      <div class="data-streams">
        <div class="stream stream-1" />
        <div class="stream stream-2" />
        <div class="stream stream-3" />
        <div class="stream stream-4" />
      </div>

      <!-- 光点粒子 -->
      <div class="light-particles">
        <div
          v-for="i in 30"
          :key="i"
          class="light-dot"
        />
      </div>

      <!-- 能量波纹 -->
      <div class="energy-ripples">
        <div class="ripple ripple-1" />
        <div class="ripple ripple-2" />
        <div class="ripple ripple-3" />
      </div>

      <!-- 连接线 -->
      <div class="connection-lines">
        <div class="line line-1" />
        <div class="line line-2" />
        <div class="line line-3" />
        <div class="line line-4" />
      </div>

      <!-- 网格背景 -->
      <div class="grid-background" />
    </div>

    <!-- 系统标题区域 -->
    <div class="system-header">
      <!-- 主标题区域 -->
      <div class="main-title-section">
        <div class="title-badge">
          <i class="el-icon-setting" />
          <span>QMS-AI管理系统配置中心</span>
        </div>
        <div class="title-subtitle">数字化驱动·配置效能提升</div>
        <div class="title-subtitle-en">Improvement of Configuration Efficiency Driven by Digitalization</div>
      </div>
    </div>

    <!-- 左侧品牌区域 -->
    <div class="brand-section">
      <div class="brand-content">
        <div class="brand-logo">
          <div class="logo-icon">
            <svg viewBox="0 0 100 100" class="logo-svg">
              <defs>
                <linearGradient id="logoGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" style="stop-color:#4facfe" />
                  <stop offset="100%" style="stop-color:#00f2fe" />
                </linearGradient>
              </defs>
              <circle cx="50" cy="50" r="45" fill="url(#logoGradient)" opacity="0.2" />
              <path d="M30 35 L50 25 L70 35 L70 65 L50 75 L30 65 Z" fill="url(#logoGradient)" />
              <circle cx="50" cy="50" r="8" fill="white" />
            </svg>
          </div>
          <h1 class="brand-title">QMS-AI</h1>
        </div>
        <div class="brand-subtitle">质量管理系统配置中心</div>
        <div class="brand-description">Quality Management System Configuration Center</div>

        <div class="feature-highlights">
          <!-- 核心功能标语 -->
          <div class="core-slogan">
            <div class="slogan-main">QMS-AI配置管理Plus+</div>
            <div class="slogan-sub">QMS-AI Configuration Management Upgrade</div>
          </div>

          <!-- 功能描述 -->
          <div class="feature-description">
            <div class="desc-main">用例数据&配置流程全覆盖</div>
            <div class="desc-sub">Full Coverage of Use Case Data & Configuration Process</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录区域 -->
    <div class="login-section">
      <div class="login-form">
        <div class="login-header">
          <h2>欢迎登录QMS-AI</h2>
          <p>配置中心 - 管理员专用登录系统</p>
        </div>

        <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form-content">
          <el-form-item prop="username">
            <div class="input-wrapper">
              <label class="input-label">用户账号</label>
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                prefix-icon="el-icon-user"
                size="large"
                @keyup.enter.native="handleLogin"
              />
            </div>
          </el-form-item>

          <el-form-item prop="password">
            <div class="input-wrapper">
              <label class="input-label">登录密码</label>
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="el-icon-lock"
                size="large"
                show-password
                @keyup.enter.native="handleLogin"
              />
            </div>
          </el-form-item>

          <el-form-item>
            <el-button
              :loading="loading"
              type="primary"
              size="large"
              class="login-button"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '立即登录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <div class="login-tips">
            <p class="tip-text">
              <i class="el-icon-info" />
              配置中心仅限系统管理员访问
            </p>
            <p class="default-account">
              默认管理员账户：<code>admin</code> / <code>admin123</code>
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { login } from '@/api/login'
// import { setToken } from '@/app/cookie' // 暂时注释未使用的导入

export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        username: 'admin',
        password: 'admin123'
      },
      loginRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' }
        ]
      },
      loading: false
    }
  },
  methods: {
    async handleLogin() {
      this.$refs.loginForm.validate(async (valid) => {
        if (valid) {
          this.loading = true
          
          try {
            // 调用登录API
            const response = await login({
              username: this.loginForm.username,
              password: this.loginForm.password
            })

            if (response.success) {
              // 验证是否为管理员账户
              if (response.data.user.role !== 'ADMIN') {
                this.$message.error('配置中心仅限管理员访问')

                return
              }

              // 保存token和管理员信息
              localStorage.setItem('qms_config_token', response.data.token)
              localStorage.setItem('qms_config_user', JSON.stringify(response.data.user))

              this.$message.success('管理员登录成功')

              // 跳转到主页面
              this.$router.push('/')
            } else {
              this.$message.error(response.message || '登录失败')
            }
          } catch (error) {
            console.error('登录错误:', error)
            // 检查是否是网络错误
            if (error.message && error.message.includes('Network Error')) {
              this.$message.error('无法连接到认证服务，请检查服务是否启动')
            } else {
              this.$message.error('登录失败，请检查用户名和密码')
            }
          } finally {
            this.loading = false
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a1428 0%, #1e3a8a 30%, #3b82f6 70%, #60a5fa 100%);
  display: flex;
  position: relative;
  overflow: hidden;
}

// 背景动画效果
.background-animation {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;

  // 网格背景
  .grid-background {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-image:
      linear-gradient(rgba(0, 212, 255, 0.3) 1px, transparent 1px),
      linear-gradient(90deg, rgba(0, 212, 255, 0.3) 1px, transparent 1px);
    background-size: 50px 50px;
    animation: gridMove 15s linear infinite, gridPulse 3s ease-in-out infinite;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background-image:
        linear-gradient(rgba(0, 255, 255, 0.15) 1px, transparent 1px),
        linear-gradient(90deg, rgba(0, 255, 255, 0.15) 1px, transparent 1px);
      background-size: 25px 25px;
      animation: gridMove 10s linear infinite reverse;
    }
  }

  // 科技圆环
  .tech-circles {
    position: absolute;
    top: 50%;
    left: 30%;
    transform: translate(-50%, -50%);

    .circle {
      position: absolute;
      border: 3px solid rgba(96, 165, 250, 0.8);
      border-radius: 50%;
      animation: rotate 10s linear infinite, circlePulse 4s ease-in-out infinite;
      box-shadow:
        0 0 40px rgba(96, 165, 250, 0.6),
        inset 0 0 30px rgba(96, 165, 250, 0.3),
        0 0 80px rgba(59, 130, 246, 0.4);

      &.circle-1 {
        width: 280px;
        height: 280px;
        top: -140px;
        left: -140px;
        border-style: dashed;
        border-width: 4px;
        border-color: rgba(147, 197, 253, 0.9);
        box-shadow:
          0 0 50px rgba(147, 197, 253, 0.7),
          inset 0 0 40px rgba(147, 197, 253, 0.4),
          0 0 100px rgba(59, 130, 246, 0.5);
      }

      &.circle-2 {
        width: 400px;
        height: 400px;
        top: -200px;
        left: -200px;
        animation-duration: 15s;
        animation-direction: reverse;
        border-width: 3px;
        border-color: rgba(96, 165, 250, 0.8);
        box-shadow:
          0 0 45px rgba(96, 165, 250, 0.6),
          inset 0 0 35px rgba(96, 165, 250, 0.3),
          0 0 90px rgba(59, 130, 246, 0.4);
      }

      &.circle-3 {
        width: 520px;
        height: 520px;
        top: -260px;
        left: -260px;
        border-style: dotted;
        border-width: 4px;
        border-color: rgba(147, 197, 253, 0.7);
        animation-duration: 20s;
        box-shadow:
          0 0 40px rgba(147, 197, 253, 0.5),
          inset 0 0 30px rgba(147, 197, 253, 0.2),
          0 0 80px rgba(59, 130, 246, 0.3);
      }

      &.circle-4 {
        width: 640px;
        height: 640px;
        top: -320px;
        left: -320px;
        border-width: 2px;
        border-color: rgba(96, 165, 250, 0.6);
        animation-duration: 25s;
        animation-direction: reverse;
        box-shadow:
          0 0 35px rgba(96, 165, 250, 0.4),
          inset 0 0 25px rgba(96, 165, 250, 0.15),
          0 0 70px rgba(59, 130, 246, 0.25);
      }
    }
  }

  // 数据流线条
  .data-streams {
    position: absolute;
    width: 100%;
    height: 100%;

    .stream {
      position: absolute;
      width: 5px;
      height: 150px;
      background: linear-gradient(to bottom,
        transparent,
        rgba(147, 197, 253, 1),
        rgba(96, 165, 250, 1),
        rgba(59, 130, 246, 0.9),
        transparent);
      animation: streamFlow 4s ease-in-out infinite;
      box-shadow:
        0 0 20px rgba(147, 197, 253, 0.8),
        0 0 40px rgba(96, 165, 250, 0.6),
        0 0 60px rgba(59, 130, 246, 0.4);
      border-radius: 3px;

      &.stream-1 {
        left: 15%;
        top: 20%;
        animation-delay: 0s;
        background: linear-gradient(to bottom,
          transparent,
          rgba(147, 197, 253, 1),
          rgba(147, 197, 253, 0.9),
          rgba(96, 165, 250, 0.8),
          transparent);
        box-shadow:
          0 0 25px rgba(147, 197, 253, 0.9),
          0 0 50px rgba(96, 165, 250, 0.7);
      }

      &.stream-2 {
        left: 25%;
        top: 60%;
        animation-delay: 1s;
        animation-duration: 5s;
        background: linear-gradient(to bottom,
          transparent,
          rgba(96, 165, 250, 1),
          rgba(96, 165, 250, 0.9),
          rgba(59, 130, 246, 0.8),
          transparent);
        box-shadow:
          0 0 25px rgba(96, 165, 250, 0.9),
          0 0 50px rgba(59, 130, 246, 0.7);
      }

      &.stream-3 {
        left: 45%;
        top: 30%;
        animation-delay: 2s;
        animation-duration: 6s;
        background: linear-gradient(to bottom,
          transparent,
          rgba(147, 197, 253, 1),
          rgba(147, 197, 253, 0.9),
          rgba(96, 165, 250, 0.8),
          transparent);
        box-shadow:
          0 0 25px rgba(147, 197, 253, 0.9),
          0 0 50px rgba(96, 165, 250, 0.7);
      }

      &.stream-4 {
        left: 65%;
        top: 70%;
        animation-delay: 3s;
        animation-duration: 4.5s;
        background: linear-gradient(to bottom,
          transparent,
          rgba(96, 165, 250, 1),
          rgba(96, 165, 250, 0.9),
          rgba(59, 130, 246, 0.8),
          transparent);
        box-shadow:
          0 0 25px rgba(96, 165, 250, 0.9),
          0 0 50px rgba(59, 130, 246, 0.7);
      }
    }
  }

  // 光点粒子
  .light-particles {
    position: absolute;
    width: 100%;
    height: 100%;

    .light-dot {
      position: absolute;
      width: 6px;
      height: 6px;
      background: rgba(147, 197, 253, 1);
      border-radius: 50%;
      box-shadow:
        0 0 25px rgba(147, 197, 253, 0.9),
        0 0 50px rgba(96, 165, 250, 0.7),
        0 0 75px rgba(59, 130, 246, 0.5),
        0 0 100px rgba(59, 130, 246, 0.3);
      animation: lightFloat 8s ease-in-out infinite, lightTwinkle 2s ease-in-out infinite;

      // 为每个光点设置不同的位置和动画延迟
      &:nth-child(1) { left: 10%; top: 20%; animation-delay: 0s; }
      &:nth-child(2) { left: 25%; top: 15%; animation-delay: 0.5s; }
      &:nth-child(3) { left: 40%; top: 25%; animation-delay: 1s; }
      &:nth-child(4) { left: 55%; top: 10%; animation-delay: 1.5s; }
      &:nth-child(5) { left: 70%; top: 30%; animation-delay: 2s; }
      &:nth-child(6) { left: 15%; top: 45%; animation-delay: 2.5s; }
      &:nth-child(7) { left: 35%; top: 50%; animation-delay: 3s; }
      &:nth-child(8) { left: 60%; top: 40%; animation-delay: 3.5s; }
      &:nth-child(9) { left: 80%; top: 55%; animation-delay: 4s; }
      &:nth-child(10) { left: 20%; top: 70%; animation-delay: 4.5s; }
      &:nth-child(11) { left: 45%; top: 75%; animation-delay: 5s; }
      &:nth-child(12) { left: 65%; top: 65%; animation-delay: 5.5s; }
      &:nth-child(13) { left: 85%; top: 80%; animation-delay: 6s; }
      &:nth-child(14) { left: 30%; top: 85%; animation-delay: 6.5s; }
      &:nth-child(15) { left: 50%; top: 90%; animation-delay: 7s; }
      &:nth-child(16) { left: 75%; top: 85%; animation-delay: 7.5s; }
      &:nth-child(17) { left: 90%; top: 70%; animation-delay: 8s; }
      &:nth-child(18) { left: 95%; top: 45%; animation-delay: 8.5s; }
      &:nth-child(19) { left: 85%; top: 25%; animation-delay: 9s; }
      &:nth-child(20) { left: 75%; top: 5%; animation-delay: 9.5s; }
      &:nth-child(21) { left: 5%; top: 60%; animation-delay: 10s; }
      &:nth-child(22) { left: 12%; top: 80%; animation-delay: 10.5s; }
      &:nth-child(23) { left: 38%; top: 35%; animation-delay: 11s; }
      &:nth-child(24) { left: 52%; top: 60%; animation-delay: 11.5s; }
      &:nth-child(25) { left: 68%; top: 20%; animation-delay: 12s; }
      &:nth-child(26) { left: 82%; top: 40%; animation-delay: 12.5s; }
      &:nth-child(27) { left: 92%; top: 60%; animation-delay: 13s; }
      &:nth-child(28) { left: 78%; top: 75%; animation-delay: 13.5s; }
      &:nth-child(29) { left: 58%; top: 85%; animation-delay: 14s; }
      &:nth-child(30) { left: 42%; top: 95%; animation-delay: 14.5s; }
    }
  }

  // 能量波纹
  .energy-ripples {
    position: absolute;
    top: 50%;
    left: 30%;
    transform: translate(-50%, -50%);

    .ripple {
      position: absolute;
      border: 3px solid rgba(147, 197, 253, 0.6);
      border-radius: 50%;
      animation: rippleExpand 6s ease-out infinite;
      box-shadow: 0 0 30px rgba(147, 197, 253, 0.4);

      &.ripple-1 {
        width: 120px;
        height: 120px;
        top: -60px;
        left: -60px;
        animation-delay: 0s;
        border-color: rgba(147, 197, 253, 0.7);
        box-shadow: 0 0 40px rgba(147, 197, 253, 0.5);
      }

      &.ripple-2 {
        width: 240px;
        height: 240px;
        top: -120px;
        left: -120px;
        animation-delay: 2s;
        border-color: rgba(96, 165, 250, 0.6);
        box-shadow: 0 0 35px rgba(96, 165, 250, 0.4);
      }

      &.ripple-3 {
        width: 360px;
        height: 360px;
        top: -180px;
        left: -180px;
        animation-delay: 4s;
        border-color: rgba(59, 130, 246, 0.5);
        box-shadow: 0 0 30px rgba(59, 130, 246, 0.3);
      }
    }
  }

  // 连接线
  .connection-lines {
    position: absolute;
    width: 100%;
    height: 100%;

    .line {
      position: absolute;
      background: linear-gradient(90deg,
        transparent,
        rgba(147, 197, 253, 0.9),
        rgba(96, 165, 250, 1),
        rgba(147, 197, 253, 0.9),
        transparent);
      animation: lineFlow 8s ease-in-out infinite;
      box-shadow: 0 0 15px rgba(96, 165, 250, 0.6);
      border-radius: 2px;

      &.line-1 {
        width: 240px;
        height: 3px;
        top: 25%;
        left: 20%;
        animation-delay: 0s;
        box-shadow: 0 0 20px rgba(147, 197, 253, 0.7);
      }

      &.line-2 {
        width: 180px;
        height: 3px;
        top: 45%;
        left: 60%;
        animation-delay: 2s;
        transform: rotate(45deg);
        box-shadow: 0 0 18px rgba(96, 165, 250, 0.6);
      }

      &.line-3 {
        width: 220px;
        height: 3px;
        top: 65%;
        left: 15%;
        animation-delay: 4s;
        transform: rotate(-30deg);
        box-shadow: 0 0 16px rgba(59, 130, 246, 0.5);
      }

      &.line-4 {
        width: 150px;
        height: 3px;
        top: 80%;
        left: 70%;
        animation-delay: 6s;
        transform: rotate(60deg);
        box-shadow: 0 0 14px rgba(147, 197, 253, 0.6);
      }
    }
  }
}

@keyframes lightBeam {
  0%, 100% {
    opacity: 0.3;
    transform: translateX(-50px) skewX(-5deg);
  }
  50% {
    opacity: 1;
    transform: translateX(50px) skewX(5deg);
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
    opacity: 0.4;
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
    opacity: 1;
  }
}

// 系统标题区域
.system-header {
  position: absolute;
  top: 40px;
  left: 40px;
  z-index: 10;
  max-width: 500px;



  // 主标题区域
  .main-title-section {
    .title-badge {
      display: inline-flex;
      align-items: center;
      background: rgba(0, 212, 255, 0.15);
      border: 2px solid rgba(0, 212, 255, 0.4);
      border-radius: 30px;
      padding: 12px 20px;
      margin-bottom: 20px;
      backdrop-filter: blur(15px);
      box-shadow: 0 8px 32px rgba(0, 212, 255, 0.2);
      max-width: 400px;

      i {
        font-size: 20px;
        color: #00d4ff;
        margin-right: 8px;
        filter: drop-shadow(0 0 8px rgba(0, 212, 255, 0.6));
        flex-shrink: 0;
      }

      span {
        font-size: 18px;
        color: rgba(255, 255, 255, 0.95);
        font-weight: 600;
        letter-spacing: 0.5px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }

    .title-subtitle {
      font-size: 22px;
      color: rgba(255, 255, 255, 0.9);
      margin-bottom: 10px;
      font-weight: 600;
      text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
    }

    .title-subtitle-en {
      font-size: 14px;
      color: rgba(255, 255, 255, 0.7);
      font-style: italic;
      letter-spacing: 0.5px;
      text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      max-width: 480px;
    }
  }
}

// 左侧品牌区域
.brand-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
  z-index: 2;

  .brand-content {
    max-width: 600px;
    color: white;

    .brand-logo {
      display: flex;
      align-items: center;
      margin-bottom: 30px;

      .logo-icon {
        width: 80px;
        height: 80px;
        margin-right: 20px;

        .logo-svg {
          width: 100%;
          height: 100%;
          filter: drop-shadow(0 0 20px rgba(79, 172, 254, 0.5));
        }
      }

      .brand-title {
        font-size: 48px;
        font-weight: 700;
        margin: 0;
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
        text-shadow: 0 0 30px rgba(79, 172, 254, 0.5);
      }
    }

    .brand-subtitle {
      font-size: 18px;
      color: #e0e6ed;
      margin-bottom: 8px;
      font-weight: 500;
    }

    .brand-description {
      font-size: 14px;
      color: #9ca3af;
      margin-bottom: 60px;
      font-style: italic;
    }

    .feature-highlights {
      // 核心功能标语
      .core-slogan {
        margin-bottom: 40px;

        .slogan-main {
          font-size: 42px;
          font-weight: 700;
          background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
          margin-bottom: 10px;
          text-shadow: 0 0 30px rgba(79, 172, 254, 0.3);
          line-height: 1.2;
        }

        .slogan-sub {
          font-size: 16px;
          color: rgba(255, 255, 255, 0.7);
          font-weight: 300;
          letter-spacing: 1px;
          text-transform: uppercase;
        }
      }

      // 功能描述
      .feature-description {
        .desc-main {
          font-size: 28px;
          font-weight: 600;
          color: rgba(255, 255, 255, 0.95);
          margin-bottom: 8px;
          text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
        }

        .desc-sub {
          font-size: 14px;
          color: rgba(255, 255, 255, 0.6);
          font-style: italic;
          letter-spacing: 0.5px;
        }
      }
    }
  }
}

// 右侧登录区域
.login-section {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  position: relative;
  z-index: 2;

  .login-form {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(20px);
    border-radius: 20px;
    box-shadow:
      0 20px 40px rgba(0, 0, 0, 0.1),
      0 0 0 1px rgba(255, 255, 255, 0.2);
    padding: 50px 40px;
    width: 100%;
    max-width: 400px;
    border: 1px solid rgba(255, 255, 255, 0.2);

    .login-header {
      text-align: center;
      margin-bottom: 40px;

      h2 {
        color: #1a1a2e;
        margin: 0 0 10px;
        font-weight: 700;
        font-size: 28px;
        background: linear-gradient(135deg, #1a1a2e 0%, #4facfe 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }

      p {
        color: #6b7280;
        margin: 0;
        font-size: 14px;
      }
    }

    .login-form-content {
      margin-bottom: 30px;

      .input-wrapper {
        margin-bottom: 25px;

        .input-label {
          display: block;
          color: #374151;
          font-size: 14px;
          font-weight: 500;
          margin-bottom: 8px;
        }
      }

      .login-button {
        width: 100%;
        height: 50px;
        font-size: 16px;
        font-weight: 600;
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        border: none;
        border-radius: 12px;
        transition: all 0.3s ease;
        box-shadow: 0 4px 15px rgba(79, 172, 254, 0.3);

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 8px 25px rgba(79, 172, 254, 0.4);
        }

        &:active {
          transform: translateY(0);
        }
      }
    }

    .login-footer {
      .login-tips {
        text-align: center;

        .tip-text {
          color: #6b7280;
          font-size: 14px;
          margin: 0 0 15px;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;

          i {
            color: #4facfe;
          }
        }

        .default-account {
          color: #9ca3af;
          font-size: 13px;
          margin: 0;

          code {
            color: #4facfe;
            font-family: 'Courier New', monospace;
            background: rgba(79, 172, 254, 0.1);
            padding: 3px 6px;
            border-radius: 4px;
            font-weight: 600;
          }
        }
      }
    }
  }
}

// Element UI 样式覆盖
::v-deep .el-input {
  .el-input__inner {
    height: 50px;
    line-height: 50px;
    border-radius: 12px;
    border: 2px solid #e5e7eb;
    font-size: 15px;
    transition: all 0.3s ease;
    background: rgba(255, 255, 255, 0.8);

    &:focus {
      border-color: #4facfe;
      box-shadow: 0 0 0 3px rgba(79, 172, 254, 0.1);
    }
  }

  .el-input__prefix {
    left: 15px;

    .el-input__icon {
      color: #9ca3af;
      font-size: 18px;
    }
  }
}

::v-deep .el-form-item {
  margin-bottom: 0;

  .el-form-item__error {
    color: #ef4444;
    font-size: 12px;
    margin-top: 5px;
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .login-container {
    flex-direction: column;
  }

  .brand-section {
    flex: none;
    padding: 40px 20px;

    .brand-content {
      text-align: center;

      .feature-highlights {
        .highlight-main {
          font-size: 42px;
        }
      }
    }
  }

  .login-section {
    width: 100%;
    padding: 20px;
  }
}

@media (max-width: 768px) {
  .brand-section {
    padding: 30px 20px;

    .brand-content {
      .brand-logo {
        flex-direction: column;
        text-align: center;

        .logo-icon {
          margin-right: 0;
          margin-bottom: 15px;
        }

        .brand-title {
          font-size: 36px;
        }
      }

      .feature-highlights {
        .highlight-item .highlight-text {
          font-size: 24px;
        }

        .highlight-main {
          font-size: 32px;
        }
      }
    }
  }

  .login-section {
    .login-form {
      padding: 40px 30px;
    }
  }
}

// 动画关键帧
@keyframes gridMove {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(50px, 50px);
  }
}

@keyframes rotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@keyframes streamFlow {
  0% {
    opacity: 0;
    transform: translateY(-50px);
  }
  50% {
    opacity: 1;
    transform: translateY(0);
  }
  100% {
    opacity: 0;
    transform: translateY(50px);
  }
}

@keyframes lightFloat {
  0%, 100% {
    transform: translateY(0px) scale(1);
    opacity: 0.8;
  }
  50% {
    transform: translateY(-30px) scale(1.3);
    opacity: 1;
  }
}

@keyframes gridPulse {
  0%, 100% {
    opacity: 0.3;
  }
  50% {
    opacity: 0.6;
  }
}

@keyframes circlePulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.6;
  }
  50% {
    transform: scale(1.05);
    opacity: 1;
  }
}

@keyframes lightTwinkle {
  0%, 100% {
    box-shadow:
      0 0 15px rgba(0, 255, 255, 0.8),
      0 0 30px rgba(0, 212, 255, 0.6),
      0 0 45px rgba(0, 212, 255, 0.4);
  }
  50% {
    box-shadow:
      0 0 25px rgba(0, 255, 255, 1),
      0 0 50px rgba(0, 212, 255, 0.8),
      0 0 75px rgba(0, 212, 255, 0.6);
  }
}

@keyframes rippleExpand {
  0% {
    transform: scale(0);
    opacity: 1;
  }
  100% {
    transform: scale(3);
    opacity: 0;
  }
}

@keyframes lineFlow {
  0%, 100% {
    opacity: 0;
    transform: translateX(-50px) scale(0.5);
  }
  50% {
    opacity: 1;
    transform: translateX(0) scale(1);
  }
}
</style>
