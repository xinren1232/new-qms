<template>
  <div class="user-control">
    <div class="page-header">
      <h2>用户权限管控</h2>
      <p class="text-muted">管理用户对AI功能的访问权限</p>
    </div>

    <!-- 权限统计 -->
    <div class="permission-stats">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon total">
                <el-icon><User /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">总用户数</div>
                <div class="stat-value">{{ stats.totalUsers }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon active">
                <el-icon><UserFilled /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">活跃用户</div>
                <div class="stat-value">{{ stats.activeUsers }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon admin">
                <el-icon><Avatar /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">管理员</div>
                <div class="stat-value">{{ stats.adminUsers }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon blocked">
                <el-icon><Lock /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">被禁用户</div>
                <div class="stat-value">{{ stats.blockedUsers }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 用户列表 -->
    <div class="users-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>用户列表</span>
            <div class="header-actions">
              <el-input
                v-model="searchText"
                placeholder="搜索用户..."
                size="small"
                style="width: 200px;"
                clearable
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-button type="primary" @click="handleAddUser">
                <el-icon><Plus /></el-icon>
                添加用户
              </el-button>
            </div>
          </div>
        </template>

        <!-- 用户表格 -->
        <vxe-table
          ref="userTableRef"
          :data="filteredUsers"
          :loading="loading"
          border
          stripe
          resizable
          height="500"
        >
          <vxe-column type="seq" width="60" title="序号" />
          <vxe-column field="username" title="用户名" min-width="120">
            <template #default="{ row }">
              <div class="user-info">
                <el-avatar :size="32" :src="row.avatar" />
                <span class="username">{{ row.username }}</span>
              </div>
            </template>
          </vxe-column>
          <vxe-column field="email" title="邮箱" min-width="180" />
          <vxe-column field="role" title="角色" width="100">
            <template #default="{ row }">
              <el-tag :type="getRoleTagType(row.role)">
                {{ getRoleText(row.role) }}
              </el-tag>
            </template>
          </vxe-column>
          <vxe-column field="permissions" title="AI权限" min-width="200">
            <template #default="{ row }">
              <div class="permissions-tags">
                <el-tag v-if="row.permissions.chat" size="small" type="success">对话</el-tag>
                <el-tag v-if="row.permissions.config" size="small" type="primary">配置</el-tag>
                <el-tag v-if="row.permissions.monitor" size="small" type="warning">监控</el-tag>
                <el-tag v-if="row.permissions.admin" size="small" type="danger">管理</el-tag>
              </div>
            </template>
          </vxe-column>
          <vxe-column field="lastLogin" title="最后登录" width="150" />
          <vxe-column field="status" title="状态" width="100">
            <template #default="{ row }">
              <el-switch
                v-model="row.active"
                @change="handleStatusChange(row)"
              />
            </template>
          </vxe-column>
          <vxe-column title="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleEditPermissions(row)">
                权限
              </el-button>
              <el-button size="small" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">
                删除
              </el-button>
            </template>
          </vxe-column>
        </vxe-table>
      </el-card>
    </div>

    <!-- 权限编辑对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      title="编辑用户权限"
      width="500px"
    >
      <el-form :model="permissionForm" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="permissionForm.username" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="permissionForm.role" placeholder="选择角色">
            <el-option label="普通用户" value="user" />
            <el-option label="高级用户" value="advanced" />
            <el-option label="管理员" value="admin" />
            <el-option label="超级管理员" value="super_admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="AI权限">
          <el-checkbox-group v-model="permissionForm.permissions">
            <el-checkbox label="chat">AI对话</el-checkbox>
            <el-checkbox label="config">配置管理</el-checkbox>
            <el-checkbox label="monitor">监控查看</el-checkbox>
            <el-checkbox label="admin">系统管理</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="使用限制">
          <el-row :gutter="10">
            <el-col :span="12">
              <el-input-number
                v-model="permissionForm.dailyLimit"
                :min="0"
                :max="10000"
                placeholder="每日对话限制"
              />
            </el-col>
            <el-col :span="12">
              <el-input-number
                v-model="permissionForm.monthlyLimit"
                :min="0"
                :max="100000"
                placeholder="每月对话限制"
              />
            </el-col>
          </el-row>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePermissions">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  UserFilled,
  Avatar,
  Lock,
  Search,
  Plus
} from '@element-plus/icons-vue'

// 响应式数据
const userTableRef = ref()
const loading = ref(false)
const searchText = ref('')
const permissionDialogVisible = ref(false)

// 统计数据
const stats = reactive({
  totalUsers: 156,
  activeUsers: 142,
  adminUsers: 8,
  blockedUsers: 6
})

// 用户数据
const users = ref([
  {
    id: 1,
    username: '张三',
    email: 'zhangsan@example.com',
    role: 'admin',
    avatar: '/avatars/user1.png',
    permissions: {
      chat: true,
      config: true,
      monitor: true,
      admin: true
    },
    lastLogin: '2024-01-01 10:30',
    active: true,
    dailyLimit: 1000,
    monthlyLimit: 30000
  },
  {
    id: 2,
    username: '李四',
    email: 'lisi@example.com',
    role: 'user',
    avatar: '/avatars/user2.png',
    permissions: {
      chat: true,
      config: false,
      monitor: false,
      admin: false
    },
    lastLogin: '2024-01-01 09:15',
    active: true,
    dailyLimit: 100,
    monthlyLimit: 3000
  },
  {
    id: 3,
    username: '王五',
    email: 'wangwu@example.com',
    role: 'advanced',
    avatar: '/avatars/user3.png',
    permissions: {
      chat: true,
      config: true,
      monitor: true,
      admin: false
    },
    lastLogin: '2024-01-01 08:45',
    active: false,
    dailyLimit: 500,
    monthlyLimit: 15000
  }
])

// 权限编辑表单
const permissionForm = reactive({
  id: 0,
  username: '',
  role: '',
  permissions: [],
  dailyLimit: 0,
  monthlyLimit: 0
})

// 计算属性
const filteredUsers = computed(() => {
  if (!searchText.value) return users.value
  return users.value.filter(user =>
    user.username.toLowerCase().includes(searchText.value.toLowerCase()) ||
    user.email.toLowerCase().includes(searchText.value.toLowerCase())
  )
})

// 方法
const getRoleTagType = (role: string) => {
  const typeMap: Record<string, string> = {
    'super_admin': 'danger',
    'admin': 'warning',
    'advanced': 'primary',
    'user': 'info'
  }
  return typeMap[role] || 'default'
}

const getRoleText = (role: string) => {
  const textMap: Record<string, string> = {
    'super_admin': '超管',
    'admin': '管理员',
    'advanced': '高级用户',
    'user': '普通用户'
  }
  return textMap[role] || role
}

const handleAddUser = () => {
  ElMessage.info('添加用户功能开发中...')
}

const handleEditPermissions = (row: any) => {
  permissionForm.id = row.id
  permissionForm.username = row.username
  permissionForm.role = row.role
  permissionForm.permissions = Object.keys(row.permissions).filter(key => row.permissions[key])
  permissionForm.dailyLimit = row.dailyLimit
  permissionForm.monthlyLimit = row.monthlyLimit
  permissionDialogVisible.value = true
}

const handleEdit = (row: any) => {
  ElMessage.info(`编辑用户: ${row.username}`)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除用户 "${row.username}" 吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    const index = users.value.findIndex(u => u.id === row.id)
    if (index > -1) {
      users.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  })
}

const handleStatusChange = (row: any) => {
  const status = row.active ? '启用' : '禁用'
  ElMessage.success(`${status}用户成功`)
}

const handleSavePermissions = () => {
  const user = users.value.find(u => u.id === permissionForm.id)
  if (user) {
    user.role = permissionForm.role
    user.dailyLimit = permissionForm.dailyLimit
    user.monthlyLimit = permissionForm.monthlyLimit

    // 重置权限
    Object.keys(user.permissions).forEach(key => {
      user.permissions[key] = permissionForm.permissions.includes(key)
    })

    ElMessage.success('权限保存成功')
    permissionDialogVisible.value = false
  }
}

onMounted(() => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 1000)
})
</script>

<style scoped>
.user-control {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.text-muted {
  color: #909399;
  margin: 0;
  font-size: 14px;
}

/* 权限统计样式 */
.permission-stats {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 20px;
  color: white;
}

.stat-icon.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.active {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-icon.admin {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.blocked {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

/* 用户列表样式 */
.users-section {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  font-weight: 500;
  color: #303133;
}

.permissions-tags .el-tag {
  margin: 2px;
}

:deep(.el-card) {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

:deep(.el-card__header) {
  background: #fafafa;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-dialog) {
  border-radius: 8px;
}

:deep(.el-dialog__header) {
  background: #fafafa;
  border-bottom: 1px solid #ebeef5;
}
</style>
