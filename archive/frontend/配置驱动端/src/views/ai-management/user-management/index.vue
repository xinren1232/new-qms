<template>
  <div class="user-management-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">
          <el-icon><User /></el-icon>
          用户管理
        </h2>
        <p class="page-description">管理系统用户、分配权限和监控用户活动</p>
      </div>
      <div class="header-right">
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

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索用户名、邮箱或姓名"
            prefix-icon="Search"
            clearable
            @input="handleSearch"
          />
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.role" placeholder="角色筛选" clearable @change="handleSearch">
            <el-option label="全部角色" value="" />
            <el-option label="管理员" value="ADMIN" />
            <el-option label="分析师" value="ANALYST" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.status" placeholder="状态筛选" clearable @change="handleSearch">
            <el-option label="全部状态" value="" />
            <el-option label="正常" value="active" />
            <el-option label="禁用" value="disabled" />
            <el-option label="待审批" value="pending" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleSearch"
          />
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 用户列表 -->
    <div class="table-section">
      <el-table
        v-loading="loading"
        :data="userList"
        stripe
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="用户信息" min-width="200">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="40" :src="row.avatar">
                {{ row.realName?.charAt(0) || row.username?.charAt(0) }}
              </el-avatar>
              <div class="user-details">
                <div class="username">{{ row.username }}</div>
                <div class="real-name">{{ row.realName }}</div>
                <div class="email">{{ row.email }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)">
              {{ getRoleLabel(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="position" label="职位" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.lastLoginTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="editUser(row)">
              编辑
            </el-button>
            <el-button
              :type="row.status === 'active' ? 'warning' : 'success'"
              size="small"
              @click="toggleUserStatus(row)"
            >
              {{ row.status === 'active' ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" size="small" @click="deleteUser(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, approveUser } from '@/api/auth'
import { logActions, OPERATION_TYPES, MODULES } from '@/utils/operationLog'
import { hasPermission, PERMISSIONS } from '@/utils/auth'

// 响应式数据
const loading = ref(false)
const userList = ref([])
const selectedUsers = ref([])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  role: '',
  status: '',
  dateRange: null
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

// 获取用户列表
const fetchUserList = async () => {
  try {
    loading.value = true

    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      role: searchForm.role,
      status: searchForm.status,
      startDate: searchForm.dateRange?.[0],
      endDate: searchForm.dateRange?.[1]
    }

    const response = await getUserList(params)

    if (response.success) {
      userList.value = response.data.list || []
      pagination.total = response.data.total || 0

      // 记录查看操作
      logActions.viewPage(MODULES.USER_MANAGEMENT, {
        action: '查看用户列表',
        params
      })
    } else {
      ElMessage.error(response.message || '获取用户列表失败')
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1
  fetchUserList()
}

// 刷新用户列表
const refreshUserList = () => {
  fetchUserList()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
  fetchUserList()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  fetchUserList()
}

// 选择处理
const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
}

// 显示添加用户对话框
const showAddUserDialog = () => {
  ElMessage.info('添加用户功能开发中...')
}

// 编辑用户
const editUser = (user) => {
  ElMessage.info('编辑用户功能开发中...')
}

// 切换用户状态
const toggleUserStatus = async (user) => {
  try {
    const action = user.status === 'active' ? '禁用' : '启用'
    await ElMessageBox.confirm(`确定要${action}用户 ${user.username} 吗？`, '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const newStatus = user.status === 'active' ? 'disabled' : 'active'

    // 这里应该调用后端API
    // const response = await updateUserStatus(user.id, newStatus)

    // 模拟API调用
    user.status = newStatus
    ElMessage.success(`用户${action}成功`)

    // 记录操作日志
    logActions.configChange('user_status', user.status, newStatus)

  } catch (error) {
    // 用户取消操作
  }
}

// 删除用户
const deleteUser = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户 ${user.username} 吗？此操作不可恢复！`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })

    // 这里应该调用后端API
    // const response = await deleteUserById(user.id)

    // 模拟删除
    const index = userList.value.findIndex(u => u.id === user.id)
    if (index > -1) {
      userList.value.splice(index, 1)
      pagination.total--
    }

    ElMessage.success('用户删除成功')

    // 记录操作日志
    logActions.logOperation({
      type: OPERATION_TYPES.DELETE,
      module: MODULES.USER_MANAGEMENT,
      action: '删除用户',
      target: user.username,
      details: { userId: user.id }
    })

  } catch (error) {
    // 用户取消操作
  }
}

// 工具函数
const getRoleTagType = (role) => {
  const roleTypes = {
    'ADMIN': 'danger',
    'ANALYST': 'warning',
    'USER': 'info'
  }
  return roleTypes[role] || 'info'
}

const getRoleLabel = (role) => {
  const roleLabels = {
    'ADMIN': '管理员',
    'ANALYST': '分析师',
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

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  fetchUserList()
})
</script>

<style lang="scss" scoped>
.user-management-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

  .header-left {
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

  .header-right {
    display: flex;
    gap: 12px;
  }
}

.search-section {
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.table-section {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;

  .el-table {
    border-radius: 8px 8px 0 0;
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;

  .user-details {
    .username {
      font-weight: 600;
      color: #303133;
      margin-bottom: 2px;
    }

    .real-name {
      font-size: 12px;
      color: #909399;
      margin-bottom: 2px;
    }

    .email {
      font-size: 12px;
      color: #909399;
    }
  }
}

.pagination-section {
  padding: 20px;
  display: flex;
  justify-content: center;
  border-top: 1px solid #ebeef5;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;

    .header-right {
      width: 100%;
      justify-content: flex-end;
    }
  }

  .search-section {
    .el-row {
      .el-col {
        margin-bottom: 12px;
      }
    }
  }
}
</style>
