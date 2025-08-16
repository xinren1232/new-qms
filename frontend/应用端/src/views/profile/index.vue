<template>
  <div class="profile-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">
          <el-icon><User /></el-icon>
          个人中心
        </h2>
        <p class="page-description">管理您的个人信息和账户设置</p>
      </div>
    </div>

    <el-row :gutter="20">
      <!-- 左侧个人信息 -->
      <el-col :span="8">
        <el-card class="profile-card">
          <div class="profile-header">
            <el-avatar :size="80" :src="userInfo.avatar">
              {{ userInfo.realName?.charAt(0) || userInfo.username?.charAt(0) }}
            </el-avatar>
            <div class="profile-info">
              <h3>{{ userInfo.realName || userInfo.username }}</h3>
              <p>{{ getRoleLabel(userInfo.role) }}</p>
              <el-tag :type="getStatusTagType(userInfo.status)">
                {{ getStatusLabel(userInfo.status) }}
              </el-tag>
            </div>
          </div>
          
          <el-divider />
          
          <div class="profile-stats">
            <div class="stat-item">
              <span class="stat-label">注册时间</span>
              <span class="stat-value">{{ formatDate(userInfo.createdAt) }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">最后登录</span>
              <span class="stat-value">{{ formatDateTime(userInfo.lastLoginTime) }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">登录次数</span>
              <span class="stat-value">{{ userInfo.loginCount || 0 }}</span>
            </div>
          </div>
        </el-card>

        <!-- 快捷操作 -->
        <el-card class="quick-actions-card">
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="activeTab = 'basic'">
              <el-icon><Edit /></el-icon>
              编辑资料
            </el-button>
            <el-button @click="activeTab = 'security'">
              <el-icon><Lock /></el-icon>
              安全设置
            </el-button>
            <el-button @click="activeTab = 'logs'">
              <el-icon><Document /></el-icon>
              操作日志
            </el-button>
            <el-button @click="exportLogs">
              <el-icon><Download /></el-icon>
              导出日志
            </el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧详细信息 -->
      <el-col :span="16">
        <el-card>
          <el-tabs v-model="activeTab">
            <!-- 基本信息 -->
            <el-tab-pane label="基本信息" name="basic">
              <el-form
                ref="basicFormRef"
                :model="basicForm"
                :rules="basicRules"
                label-width="100px"
                class="profile-form"
              >
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="用户名" prop="username">
                      <el-input v-model="basicForm.username" disabled />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="真实姓名" prop="realName">
                      <el-input v-model="basicForm.realName" />
                    </el-form-item>
                  </el-col>
                </el-row>
                
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="邮箱" prop="email">
                      <el-input v-model="basicForm.email" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="手机号" prop="phone">
                      <el-input v-model="basicForm.phone" />
                    </el-form-item>
                  </el-col>
                </el-row>
                
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="部门" prop="department">
                      <el-input v-model="basicForm.department" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="职位" prop="position">
                      <el-input v-model="basicForm.position" />
                    </el-form-item>
                  </el-col>
                </el-row>
                
                <el-form-item>
                  <el-button type="primary" @click="updateBasicInfo" :loading="updating">
                    保存修改
                  </el-button>
                  <el-button @click="resetBasicForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 安全设置 -->
            <el-tab-pane label="安全设置" name="security">
              <el-form
                ref="passwordFormRef"
                :model="passwordForm"
                :rules="passwordRules"
                label-width="100px"
                class="profile-form"
              >
                <el-form-item label="当前密码" prop="oldPassword">
                  <el-input
                    v-model="passwordForm.oldPassword"
                    type="password"
                    show-password
                    placeholder="请输入当前密码"
                  />
                </el-form-item>
                
                <el-form-item label="新密码" prop="newPassword">
                  <el-input
                    v-model="passwordForm.newPassword"
                    type="password"
                    show-password
                    placeholder="请输入新密码"
                  />
                </el-form-item>
                
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    show-password
                    placeholder="请再次输入新密码"
                  />
                </el-form-item>
                
                <el-form-item>
                  <el-button type="primary" @click="changePassword" :loading="changingPassword">
                    修改密码
                  </el-button>
                  <el-button @click="resetPasswordForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 操作日志 -->
            <el-tab-pane label="操作日志" name="logs">
              <div class="logs-section">
                <div class="logs-filter">
                  <el-row :gutter="16">
                    <el-col :span="6">
                      <el-select v-model="logFilter.type" placeholder="操作类型" clearable @change="filterLogs">
                        <el-option label="全部" value="" />
                        <el-option label="登录" value="login" />
                        <el-option label="登出" value="logout" />
                        <el-option label="查看" value="view" />
                        <el-option label="修改" value="update" />
                      </el-select>
                    </el-col>
                    <el-col :span="8">
                      <el-date-picker
                        v-model="logFilter.dateRange"
                        type="daterange"
                        range-separator="至"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期"
                        @change="filterLogs"
                      />
                    </el-col>
                    <el-col :span="4">
                      <el-button @click="refreshLogs">
                        <el-icon><Refresh /></el-icon>
                        刷新
                      </el-button>
                    </el-col>
                  </el-row>
                </div>
                
                <el-table :data="operationLogs" stripe style="width: 100%">
                  <el-table-column prop="timestamp" label="时间" width="160">
                    <template #default="{ row }">
                      {{ formatDateTime(row.timestamp) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="type" label="类型" width="100" />
                  <el-table-column prop="module" label="模块" width="120" />
                  <el-table-column prop="action" label="操作" width="150" />
                  <el-table-column prop="target" label="目标" width="120" />
                  <el-table-column label="状态" width="80">
                    <template #default="{ row }">
                      <el-tag :type="row.success ? 'success' : 'danger'">
                        {{ row.success ? '成功' : '失败' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="details" label="详情" min-width="200">
                    <template #default="{ row }">
                      <span v-if="typeof row.details === 'string'">{{ row.details }}</span>
                      <el-popover v-else-if="row.details" trigger="hover" width="300">
                        <pre>{{ JSON.stringify(row.details, null, 2) }}</pre>
                        <template #reference>
                          <el-button type="text" size="small">查看详情</el-button>
                        </template>
                      </el-popover>
                      <span v-else>-</span>
                    </template>
                  </el-table-column>
                </el-table>
                
                <div class="logs-pagination">
                  <el-pagination
                    v-model:current-page="logPagination.page"
                    v-model:page-size="logPagination.pageSize"
                    :page-sizes="[10, 20, 50]"
                    :total="logPagination.total"
                    layout="total, sizes, prev, pager, next"
                    @size-change="handleLogSizeChange"
                    @current-change="handleLogCurrentChange"
                  />
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUser, setUser } from '@/utils/auth'
import { getOperationLogs, exportOperationLogs } from '@/utils/operationLog'
import { updateUserInfo, changePassword as changePasswordAPI } from '@/api/auth'

// 响应式数据
const activeTab = ref('basic')
const updating = ref(false)
const changingPassword = ref(false)

// 用户信息
const userInfo = ref(getUser() || {})

// 基本信息表单
const basicFormRef = ref()
const basicForm = reactive({
  username: '',
  realName: '',
  email: '',
  phone: '',
  department: '',
  position: ''
})

// 密码修改表单
const passwordFormRef = ref()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 操作日志
const operationLogs = ref([])
const logFilter = reactive({
  type: '',
  dateRange: null
})

const logPagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

// 表单验证规则
const basicRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 初始化基本信息表单
const initBasicForm = () => {
  Object.assign(basicForm, {
    username: userInfo.value.username || '',
    realName: userInfo.value.realName || '',
    email: userInfo.value.email || '',
    phone: userInfo.value.phone || '',
    department: userInfo.value.department || '',
    position: userInfo.value.position || ''
  })
}

// 更新基本信息
const updateBasicInfo = async () => {
  try {
    await basicFormRef.value.validate()
    updating.value = true
    
    const response = await updateUserInfo(basicForm)
    
    if (response.success) {
      // 更新本地用户信息
      Object.assign(userInfo.value, basicForm)
      setUser(userInfo.value)
      
      ElMessage.success('个人信息更新成功')
    } else {
      ElMessage.error(response.message || '更新失败')
    }
  } catch (error) {
    console.error('更新个人信息失败:', error)
    ElMessage.error('更新失败')
  } finally {
    updating.value = false
  }
}

// 重置基本信息表单
const resetBasicForm = () => {
  initBasicForm()
}

// 修改密码
const changePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true
    
    const response = await changePasswordAPI({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    
    if (response.success) {
      ElMessage.success('密码修改成功')
      resetPasswordForm()
    } else {
      ElMessage.error(response.message || '密码修改失败')
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error('密码修改失败')
  } finally {
    changingPassword.value = false
  }
}

// 重置密码表单
const resetPasswordForm = () => {
  Object.assign(passwordForm, {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
  passwordFormRef.value?.clearValidate()
}

// 加载操作日志
const loadOperationLogs = () => {
  const filters = {
    type: logFilter.type,
    startDate: logFilter.dateRange?.[0],
    endDate: logFilter.dateRange?.[1],
    page: logPagination.page,
    pageSize: logPagination.pageSize
  }
  
  const logs = getOperationLogs(filters)
  operationLogs.value = logs
  logPagination.total = logs.length
}

// 过滤日志
const filterLogs = () => {
  logPagination.page = 1
  loadOperationLogs()
}

// 刷新日志
const refreshLogs = () => {
  loadOperationLogs()
}

// 日志分页处理
const handleLogSizeChange = (size) => {
  logPagination.pageSize = size
  loadOperationLogs()
}

const handleLogCurrentChange = (page) => {
  logPagination.page = page
  loadOperationLogs()
}

// 导出日志
const exportLogs = () => {
  exportOperationLogs('json')
}

// 工具函数
const getRoleLabel = (role) => {
  const roleLabels = {
    'ADMIN': '系统管理员',
    'ANALYST': '数据分析师',
    'USER': '普通用户'
  }
  return roleLabels[role] || role
}

const getStatusTagType = (status) => {
  const statusTypes = {
    'active': 'success',
    'disabled': 'danger',
    'pending': 'warning'
  }
  return statusTypes[status] || 'info'
}

const getStatusLabel = (status) => {
  const statusLabels = {
    'active': '正常',
    'disabled': '禁用',
    'pending': '待审批'
  }
  return statusLabels[status] || status
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  initBasicForm()
  loadOperationLogs()
})
</script>

<style lang="scss" scoped>
.profile-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  
  .page-title {
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 0 0 8px 0;
    font-size: 24px;
    font-weight: 600;
    color: #303133;
  }
  
  .page-description {
    margin: 0;
    color: #606266;
    font-size: 14px;
  }
}

.profile-card {
  margin-bottom: 20px;
  
  .profile-header {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .profile-info {
      h3 {
        margin: 0 0 8px 0;
        font-size: 18px;
        color: #303133;
      }
      
      p {
        margin: 0 0 8px 0;
        color: #606266;
        font-size: 14px;
      }
    }
  }
  
  .profile-stats {
    .stat-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;
      border-bottom: 1px solid #f0f0f0;
      
      &:last-child {
        border-bottom: none;
      }
      
      .stat-label {
        color: #606266;
        font-size: 14px;
      }
      
      .stat-value {
        color: #303133;
        font-weight: 500;
      }
    }
  }
}

.quick-actions-card {
  .quick-actions {
    display: flex;
    flex-direction: column;
    gap: 12px;
    
    .el-button {
      justify-content: flex-start;
    }
  }
}

.profile-form {
  .el-form-item {
    margin-bottom: 20px;
  }
}

.logs-section {
  .logs-filter {
    margin-bottom: 20px;
  }
  
  .logs-pagination {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .el-row {
    .el-col {
      margin-bottom: 20px;
    }
  }
  
  .quick-actions {
    .el-button {
      width: 100%;
    }
  }
}
</style>
