<template>
  <div class="login-guide">
    <el-card class="guide-card">
      <template #header>
        <div class="card-header">
          <el-icon size="24"><QuestionFilled /></el-icon>
          <span>登录帮助</span>
        </div>
      </template>
      
      <div class="guide-content">
        <el-steps :active="currentStep" direction="vertical" finish-status="success">
          <el-step title="为什么需要登录？">
            <template #description>
              <div class="step-content">
                <p>QMS-AI系统采用安全优先的设计理念，要求所有用户必须登录后才能使用，这样做的原因包括：</p>
                <ul>
                  <li><strong>数据安全</strong>：保护您的对话记录和个人数据不被他人访问</li>
                  <li><strong>个性化体验</strong>：根据您的角色和权限提供定制化功能</li>
                  <li><strong>审计追踪</strong>：确保所有操作都可以追溯到具体用户</li>
                  <li><strong>质量保证</strong>：维护系统的稳定性和可靠性</li>
                </ul>
              </div>
            </template>
          </el-step>
          
          <el-step title="如何快速登录？">
            <template #description>
              <div class="step-content">
                <p>我们提供了多种便捷的登录方式：</p>
                <div class="login-methods">
                  <div class="method-item">
                    <el-tag type="success">演示账户</el-tag>
                    <p>使用预设的演示账户快速体验系统功能</p>
                  </div>
                  <div class="method-item">
                    <el-tag type="primary">快速注册</el-tag>
                    <p>只需填写基本信息即可创建新账户</p>
                  </div>
                  <div class="method-item">
                    <el-tag type="info">记住登录</el-tag>
                    <p>勾选"记住我"选项，下次访问自动登录</p>
                  </div>
                </div>
              </div>
            </template>
          </el-step>
          
          <el-step title="演示账户说明">
            <template #description>
              <div class="step-content">
                <p>为了方便您快速体验系统，我们提供了以下演示账户：</p>
                <el-table :data="demoAccounts" style="width: 100%; margin-top: 16px;">
                  <el-table-column prop="role" label="角色" width="120" />
                  <el-table-column prop="username" label="用户名" width="120" />
                  <el-table-column prop="password" label="密码" width="120" />
                  <el-table-column prop="description" label="权限说明" />
                </el-table>
                <el-alert
                  title="注意"
                  description="演示账户仅用于功能体验，请勿在其中存储重要数据。建议注册个人账户以获得完整体验。"
                  type="warning"
                  :closable="false"
                  style="margin-top: 16px;"
                />
              </div>
            </template>
          </el-step>
          
          <el-step title="常见问题">
            <template #description>
              <div class="step-content">
                <el-collapse>
                  <el-collapse-item title="忘记密码怎么办？" name="1">
                    <p>点击登录页面的"忘记密码？"链接，输入注册邮箱即可收到重置密码的邮件。</p>
                  </el-collapse-item>
                  <el-collapse-item title="注册时需要填写哪些信息？" name="2">
                    <p>注册时需要提供用户名、邮箱、手机号、部门、职位等基本信息，所有信息都会严格保密。</p>
                  </el-collapse-item>
                  <el-collapse-item title="登录后可以做什么？" name="3">
                    <p>登录后您可以使用AI对话、查看数据分析、管理质量流程等功能，具体权限根据您的角色而定。</p>
                  </el-collapse-item>
                  <el-collapse-item title="如何保证账户安全？" name="4">
                    <p>建议使用强密码、定期更换密码、不要在公共设备上保存登录状态。</p>
                  </el-collapse-item>
                </el-collapse>
              </div>
            </template>
          </el-step>
        </el-steps>
        
        <div class="guide-actions">
          <el-button type="primary" @click="goToLogin">
            <el-icon><Right /></el-icon>
            立即登录
          </el-button>
          <el-button @click="goToRegister">
            <el-icon><UserFilled /></el-icon>
            注册新账户
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { QuestionFilled, Right, UserFilled } from '@element-plus/icons-vue'

const router = useRouter()
const currentStep = ref(0)

const demoAccounts = [
  {
    role: '管理员',
    username: 'admin',
    password: 'admin123',
    description: '拥有所有系统权限，可以管理用户和系统配置'
  },
  {
    role: '开发者',
    username: 'developer',
    password: 'dev123',
    description: '可以使用AI对话、查看配置、访问开发工具'
  },
  {
    role: '质量工程师',
    username: 'quality',
    password: 'quality123',
    description: '专注于质量管理功能，可以进行质量分析和监控'
  }
]

const goToLogin = () => {
  router.push('/login')
}

const goToRegister = () => {
  router.push('/login?mode=register')
}
</script>

<style scoped>
.login-guide {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding-top: 40px;
}

.guide-card {
  max-width: 800px;
  width: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
}

.guide-content {
  padding: 20px 0;
}

.step-content {
  padding: 16px 0;
}

.step-content p {
  margin: 0 0 12px 0;
  line-height: 1.6;
  color: #606266;
}

.step-content ul {
  margin: 12px 0;
  padding-left: 20px;
}

.step-content li {
  margin: 8px 0;
  line-height: 1.6;
  color: #606266;
}

.login-methods {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.method-item {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.method-item p {
  margin: 8px 0 0 0;
  font-size: 14px;
}

.guide-actions {
  margin-top: 40px;
  text-align: center;
  display: flex;
  gap: 16px;
  justify-content: center;
}

@media (max-width: 768px) {
  .login-guide {
    padding: 10px;
    padding-top: 20px;
  }
  
  .login-methods {
    grid-template-columns: 1fr;
  }
  
  .guide-actions {
    flex-direction: column;
    align-items: center;
  }
}
</style>
