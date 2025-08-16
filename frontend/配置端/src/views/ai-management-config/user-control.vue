<template>
  <div class="user-control-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>用户权限管控</h2>
      <p>管理应用端用户的AI使用权限、配额和访问控制</p>
    </div>

    <!-- 用户统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="32" color="#409EFF"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ userStats.total_users }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="32" color="#67C23A"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ userStats.active_users }}</div>
              <div class="stat-label">活跃用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="32" color="#E6A23C"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ userStats.quota_exceeded }}</div>
              <div class="stat-label">配额超限</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="32" color="#F56C6C"><Lock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ userStats.blocked_users }}</div>
              <div class="stat-label">被禁用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 用户管理表格 -->
    <el-card title="用户管理" class="user-table-card">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索用户..."
              style="width: 200px; margin-right: 12px;"
              clearable
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="showAddUserDialog">
              <el-icon><Plus /></el-icon>
              添加用户
            </el-button>
            <el-button @click="refreshUserList">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loadingUsers" :data="filteredUsers" style="width: 100%">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="real_name" label="真实姓名" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)" size="small">
              {{ getRoleLabel(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ai_quota_daily" label="日配额" width="100">
          <template #default="{ row }">
            <span :class="getQuotaClass(row.ai_quota_used, row.ai_quota_daily)">
              {{ row.ai_quota_used }}/{{ row.ai_quota_daily }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="last_login_at" label="最后登录" width="150">
          <template #default="{ row }">
            {{ formatTime(row.last_login_at) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="editUser(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button size="small" type="warning" @click="managePermissions(row)">
              <el-icon><Key /></el-icon>
              权限
            </el-button>
            <el-dropdown @command="handleUserAction">
              <el-button size="small">
                更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="{action: 'resetQuota', user: row}">
                    重置配额
                  </el-dropdown-item>
                  <el-dropdown-item :command="{action: 'resetPassword', user: row}">
                    重置密码
                  </el-dropdown-item>
                  <el-dropdown-item :command="{action: 'toggleStatus', user: row}">
                    {{ row.status === 'active' ? '禁用' : '启用' }}
                  </el-dropdown-item>
                  <el-dropdown-item :command="{action: 'viewUsage', user: row}">
                    使用统计
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalUsers"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 权限管理对话框 -->
    <el-dialog v-model="permissionDialogVisible" title="权限管理" width="60%">
      <div v-if="selectedUser" class="permission-management">
        <el-tabs v-model="activePermissionTab">
          <el-tab-pane label="AI模型权限" name="ai_models">
            <div class="ai-models-permission">
              <h4>可使用的AI模型</h4>
              <el-checkbox-group v-model="selectedUser.ai_models_allowed">
                <el-row :gutter="16">
                  <el-col v-for="model in availableModels" :key="model.id" :span="8">
                    <el-checkbox :label="model.id" :disabled="!model.enabled">
                      <div class="model-option">
                        <span class="model-name">{{ model.name }}</span>
                        <span class="model-cost">{{ model.cost_per_1k_tokens }}$/1K tokens</span>
                      </div>
                    </el-checkbox>
                  </el-col>
                </el-row>
              </el-checkbox-group>
              
              <h4 style="margin-top: 24px;">配额设置</h4>
              <el-row :gutter="16">
                <el-col :span="8">
                  <el-form-item label="日配额:">
                    <el-input-number 
                      v-model="selectedUser.ai_quota_daily" 
                      :min="0" 
                      :max="10000"
                      controls-position="right"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="月配额:">
                    <el-input-number 
                      v-model="selectedUser.ai_quota_monthly" 
                      :min="0" 
                      :max="100000"
                      controls-position="right"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="成本限制($/月):">
                    <el-input-number 
                      v-model="selectedUser.cost_limit_monthly" 
                      :min="0" 
                      :max="1000"
                      :precision="2"
                      controls-position="right"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="功能权限" name="features">
            <div class="feature-permissions">
              <h4>功能权限</h4>
              <el-checkbox-group v-model="selectedUser.feature_permissions">
                <el-row :gutter="16">
                  <el-col v-for="feature in availableFeatures" :key="feature.code" :span="12">
                    <el-checkbox :label="feature.code">
                      <div class="feature-option">
                        <span class="feature-name">{{ feature.name }}</span>
                        <span class="feature-desc">{{ feature.description }}</span>
                      </div>
                    </el-checkbox>
                  </el-col>
                </el-row>
              </el-checkbox-group>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="数据权限" name="data">
            <div class="data-permissions">
              <h4>数据访问权限</h4>
              <el-form label-width="120px">
                <el-form-item label="对话保留期:">
                  <el-select v-model="selectedUser.conversation_retention_days">
                    <el-option label="7天" :value="7" />
                    <el-option label="30天" :value="30" />
                    <el-option label="90天" :value="90" />
                    <el-option label="365天" :value="365" />
                    <el-option label="永久" :value="-1" />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="数据导出:">
                  <el-radio-group v-model="selectedUser.export_permission">
                    <el-radio label="none">禁止导出</el-radio>
                    <el-radio label="own">仅自己的数据</el-radio>
                    <el-radio label="team">团队数据</el-radio>
                    <el-radio label="all">所有数据</el-radio>
                  </el-radio-group>
                </el-form-item>
                
                <el-form-item label="数据分享:">
                  <el-checkbox-group v-model="selectedUser.share_permissions">
                    <el-checkbox label="share_conversation">分享对话</el-checkbox>
                    <el-checkbox label="share_public">公开分享</el-checkbox>
                    <el-checkbox label="share_team">团队分享</el-checkbox>
                  </el-checkbox-group>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
      
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUserPermissions">保存权限</el-button>
      </template>
    </el-dialog>

    <!-- 用户编辑对话框 -->
    <el-dialog v-model="userEditDialogVisible" :title="editingUser.id ? '编辑用户' : '添加用户'" width="50%">
      <el-form ref="userFormRef" :model="editingUser" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用户名:" required>
              <el-input v-model="editingUser.username" :disabled="!!editingUser.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名:" required>
              <el-input v-model="editingUser.real_name" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="邮箱:" required>
              <el-input v-model="editingUser.email" type="email" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号:">
              <el-input v-model="editingUser.phone" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="部门:">
              <el-select v-model="editingUser.department_id" placeholder="选择部门">
                <el-option 
                  v-for="dept in departments" 
                  :key="dept.id" 
                  :label="dept.name" 
                  :value="dept.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色:">
              <el-select v-model="editingUser.role" placeholder="选择角色">
                <el-option label="超级管理员" value="SUPER_ADMIN" />
                <el-option label="管理员" value="ADMIN" />
                <el-option label="经理" value="MANAGER" />
                <el-option label="高级用户" value="SENIOR_USER" />
                <el-option label="普通用户" value="USER" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="职位:">
          <el-input v-model="editingUser.position" />
        </el-form-item>
        
        <el-form-item label="状态:">
          <el-radio-group v-model="editingUser.status">
            <el-radio label="active">激活</el-radio>
            <el-radio label="inactive">未激活</el-radio>
            <el-radio label="locked">锁定</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="userEditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, computed, getCurrentInstance } from 'vue'
import localConfigCenter from '@/config/local-config-center'

export default {
  name: 'UserControl',
  setup() {
    const { proxy } = getCurrentInstance()

    // 响应式数据
    const _userStats = ref({
      total_users: 156,
      active_users: 142,
      quota_exceeded: 8,
      blocked_users: 2
    })

    const users = ref([
      {
        id: 'user_001',
        username: 'admin',
        real_name: '系统管理员',
        email: 'admin@qms.com',
        phone: '13800138000',
        department: '系统管理部',
        department_id: 'dept_001',
        position: '系统管理员',
        role: 'ADMIN',
        status: 'active',
        ai_quota_daily: 2000,
        ai_quota_used: 156,
        ai_quota_monthly: 50000,
        cost_limit_monthly: 100,
        ai_models_allowed: ['gpt-4o', 'gpt-4o-mini', 'deepseek-r1'],
        feature_permissions: ['chat:create', 'chat:export', 'analytics:view'],
        conversation_retention_days: 365,
        export_permission: 'all',
        share_permissions: ['share_conversation', 'share_team'],
        last_login_at: '2024-01-15 14:30:00',
        created_at: '2024-01-01 00:00:00'
      }
      // 更多用户数据...
    ])

    const searchKeyword = ref('')
    const loadingUsers = ref(false)
    const currentPage = ref(1)
    const pageSize = ref(20)

    const permissionDialogVisible = ref(false)
    const userEditDialogVisible = ref(false)
    const selectedUser = ref(null)
    const editingUser = ref({})
    const _activePermissionTab = ref('ai_models')

    // 可用模型和功能
    const _availableModels = ref([
      { id: 'gpt-4o', name: 'GPT-4o', enabled: true, cost_per_1k_tokens: 0.03 },
      { id: 'gpt-4o-mini', name: 'GPT-4o Mini', enabled: true, cost_per_1k_tokens: 0.015 },
      { id: 'deepseek-r1', name: 'DeepSeek R1', enabled: true, cost_per_1k_tokens: 0.002 },
      { id: 'claude-3-5-sonnet', name: 'Claude 3.5 Sonnet', enabled: false, cost_per_1k_tokens: 0.03 }
    ])

    const _availableFeatures = ref([
      { code: 'chat:create', name: '创建对话', description: '允许创建新的AI对话' },
      { code: 'chat:export', name: '导出对话', description: '允许导出对话记录' },
      { code: 'chat:share', name: '分享对话', description: '允许分享对话给其他用户' },
      { code: 'analytics:view', name: '查看统计', description: '允许查看使用统计数据' },
      { code: 'model:switch', name: '切换模型', description: '允许在对话中切换AI模型' },
      { code: 'advanced:tools', name: '高级工具', description: '允许使用高级AI工具功能' }
    ])

    const _departments = ref([
      { id: 'dept_001', name: '系统管理部' },
      { id: 'dept_002', name: '质量管理部' },
      { id: 'dept_003', name: '研发部' },
      { id: 'dept_004', name: '市场部' }
    ])

    // 计算属性
    const filteredUsersAll = computed(() => {
      let filtered = users.value

      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase()

        filtered = filtered.filter(user =>
          user.username.toLowerCase().includes(keyword) ||
      user.real_name.toLowerCase().includes(keyword) ||
      user.email.toLowerCase().includes(keyword) ||
      user.department.toLowerCase().includes(keyword)
        )
      }

      return filtered
    })

    const totalUsers = computed(() => filteredUsersAll.value.length)

    const activeUsers = computed(() => users.value.filter(user => user.status === 'active').length)

    const lockedUsers = computed(() => users.value.filter(user => user.status === 'locked').length)

    const filteredUsers = computed(() => {
      const filtered = filteredUsersAll.value
      // 分页
      const start = (currentPage.value - 1) * pageSize.value
      const end = start + pageSize.value

      return filtered.slice(start, end)
    })

    // 方法
    const _getRoleTagType = (role) => {
      const types = {
        'SUPER_ADMIN': 'danger',
        'ADMIN': 'warning',
        'MANAGER': 'primary',
        'SENIOR_USER': 'success',
        'USER': 'info'
      }

      return types[role] || 'info'
    }

    const _getRoleLabel = (role) => {
      const labels = {
        'SUPER_ADMIN': '超管',
        'ADMIN': '管理员',
        'MANAGER': '经理',
        'SENIOR_USER': '高级用户',
        'USER': '普通用户'
      }

      return labels[role] || role
    }

    const _getStatusTagType = (status) => {
      const types = {
        'active': 'success',
        'inactive': 'warning',
        'locked': 'danger'
      }

      return types[status] || 'info'
    }

    const _getStatusLabel = (status) => {
      const labels = {
        'active': '正常',
        'inactive': '未激活',
        'locked': '锁定'
      }

      return labels[status] || status
    }

    const _getQuotaClass = (used, total) => {
      const ratio = used / total

      if (ratio >= 0.9) return 'quota-danger'
      if (ratio >= 0.7) return 'quota-warning'

      return 'quota-normal'
    }

    const _formatTime = (timeStr) => {
      if (!timeStr) return '-'

      return new Date(timeStr).toLocaleString()
    }

    // 事件处理
    const _handleSearch = () => {
      currentPage.value = 1
    }

    const _handleSizeChange = (size) => {
      pageSize.value = size
      currentPage.value = 1
    }

    const _handleCurrentChange = (page) => {
      currentPage.value = page
    }

    const refreshUserList = async () => {
      loadingUsers.value = true
      try {
        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 1000))
        proxy.$message.success('用户列表已刷新')
      } finally {
        loadingUsers.value = false
      }
    }

    const addUser = () => {
      editingUser.value = {
        username: '',
        real_name: '',
        email: '',
        phone: '',
        department_id: '',
        position: '',
        role: 'USER',
        status: 'active'
      }
      userEditDialogVisible.value = true
    }

    const editUser = (_user) => {
      editingUser.value = { ..._user }
      userEditDialogVisible.value = true
    }

    const saveUser = async () => {
      try {
        // 验证表单
        if (!editingUser.value.username || !editingUser.value.real_name || !editingUser.value.email) {
          proxy.$message.error('请填写必填字段')

          return
        }

        // 模拟保存
        if (editingUser.value.id) {
          // 更新用户
          const index = users.value.findIndex(u => u.id === editingUser.value.id)

          if (index > -1) {
            users.value[index] = { ...editingUser.value }
          }
          proxy.$message.success('用户信息已更新')
        } else {
          // 添加用户
          editingUser.value.id = 'user_' + Date.now()
          editingUser.value.created_at = new Date().toISOString()
          editingUser.value.ai_quota_daily = 1000
          editingUser.value.ai_quota_used = 0
          users.value.push({ ...editingUser.value })
          proxy.$message.success('用户已添加')
        }

        userEditDialogVisible.value = false
      } catch (error) {
        proxy.$message.error('保存失败: ' + error.message)
      }
    }

    const managePermissions = (_user) => {
      selectedUser.value = { ..._user }
      permissionDialogVisible.value = true
    }

    const savePermissions = async () => {
      try {
        // 保存权限逻辑
        await _saveUserPermissions()
        permissionDialogVisible.value = false
        proxy.$message.success('权限保存成功')
      } catch (error) {
        proxy.$message.error('权限保存失败: ' + error.message)
      }
    }

    const _saveUserPermissions = async () => {
      try {
        // 更新用户权限
        const index = users.value.findIndex(u => u.id === selectedUser.value.id)

        if (index > -1) {
          users.value[index] = { ...selectedUser.value }
        }
    
        // 同步权限配置到应用端
        const permissionConfig = {
          user_id: selectedUser.value.id,
          ai_models_allowed: selectedUser.value.ai_models_allowed,
          ai_quota_daily: selectedUser.value.ai_quota_daily,
          feature_permissions: selectedUser.value.feature_permissions,
          conversation_retention_days: selectedUser.value.conversation_retention_days
        }
    
        await localConfigCenter.setConfig(`user_permission.${selectedUser.value.id}`, permissionConfig, true)

        proxy.$message.success('用户权限已保存并同步到应用端')
        permissionDialogVisible.value = false
      } catch (error) {
        proxy.$message.error('保存权限失败: ' + error.message)
      }
    }

    const handleUserAction = async ({ action, user }) => {
      switch (action) {
        case 'resetQuota':
          await resetUserQuota(user)
          break
        case 'resetPassword':
          await resetUserPassword(user)
          break
        case 'toggleStatus':
          await toggleUserStatus(user)
          break
        case 'viewUsage':
          await viewUserUsage(user)
          break
      }
    }

    const resetUserQuota = async (_user) => {
      try {
        await proxy.$confirm(`确定要重置用户 ${_user.real_name} 的配额吗？`, '确认重置', {
          type: 'warning'
        })

        // 重置配额
        const index = users.value.findIndex(u => u.id === _user.id)

        if (index > -1) {
          users.value[index].ai_quota_used = 0
        }

        proxy.$message.success('配额已重置')
      } catch {
        // 用户取消
      }
    }

    const resetUserPassword = async (_user) => {
      proxy.$message.info('重置密码功能开发中...')
    }

    const toggleUserStatus = async (_user) => {
      try {
        const newStatus = _user.status === 'active' ? 'locked' : 'active'
        const action = newStatus === 'active' ? '启用' : '禁用'

        await proxy.$confirm(`确定要${action}用户 ${_user.real_name} 吗？`, `确认${action}`, {
          type: 'warning'
        })

        const index = users.value.findIndex(u => u.id === _user.id)

        if (index > -1) {
          users.value[index].status = newStatus
        }

        proxy.$message.success(`用户已${action}`)
      } catch {
        // 用户取消
      }
    }

    const viewUserUsage = async (_user) => {
      proxy.$message.info('用户使用统计功能开发中...')
    }

    // 生命周期
    onMounted(async () => {
      await refreshUserList()
    })

    // 返回所有响应式数据和方法
    return {
      // 响应式数据
      users,
      filteredUsers,
      loadingUsers,
      searchKeyword,
      userEditDialogVisible,
      editingUser,
      permissionDialogVisible,
      selectedUser,

      // 计算属性
      totalUsers,
      activeUsers,
      lockedUsers,

      // 方法
      refreshUserList,
      addUser,
      editUser,
      saveUser,
      managePermissions,
      savePermissions,
      handleUserAction,
      resetUserQuota,
      resetUserPassword,
      toggleUserStatus,
      viewUserUsage
    }
  }
}
</script>

<style scoped>
.user-control-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.stats-cards {
  margin-bottom: 24px;
}

.stat-card {
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.user-table-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.quota-normal {
  color: #67c23a;
}

.quota-warning {
  color: #e6a23c;
}

.quota-danger {
  color: #f56c6c;
}

.model-option {
  display: flex;
  flex-direction: column;
}

.model-name {
  font-weight: 500;
}

.model-cost {
  font-size: 12px;
  color: #909399;
}

.feature-option {
  display: flex;
  flex-direction: column;
}

.feature-name {
  font-weight: 500;
}

.feature-desc {
  font-size: 12px;
  color: #909399;
}

.permission-management {
  max-height: 500px;
  overflow-y: auto;
}

.ai-models-permission h4,
.feature-permissions h4,
.data-permissions h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
}
</style>
