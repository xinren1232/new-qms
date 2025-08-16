<template>
  <div class="user-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">
        <i class="el-icon-user"></i>
        用户管理
      </h2>
      <p class="page-description">管理系统用户和权限，控制用户账户状态和角色分配</p>
    </div>

    <!-- 搜索和操作区域 -->
    <div class="search-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名或姓名"
            clearable
            prefix-icon="el-icon-search"
          />
        </el-col>
        <el-col :span="6">
          <el-select v-model="searchForm.userRole" placeholder="用户角色" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option label="管理员" value="管理员"></el-option>
            <el-option label="用户" value="用户"></el-option>
            <el-option label="访客" value="访客"></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="searchForm.accountStatus" placeholder="账户状态" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option label="正常" value="正常"></el-option>
            <el-option label="禁用" value="禁用"></el-option>
            <el-option label="锁定" value="锁定"></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="handleSearch" icon="el-icon-search">搜索</el-button>
          <el-button @click="handleReset" icon="el-icon-refresh">重置</el-button>
          <el-button type="success" @click="handleAdd" icon="el-icon-plus">新增</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="userRole" label="用户角色" width="100">
          <template slot-scope="scope">
            <el-tag :type="getRoleColor((scope && scope.row) ? scope.row.userRole : '')" size="small">
              {{ (scope && scope.row) ? scope.row.userRole : '' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="200" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="accountStatus" label="账户状态" width="100">
          <template slot-scope="scope">
            <el-tag
              :type="getStatusType((scope && scope.row) ? scope.row.accountStatus : '')"
              size="small"
            >
              {{ (scope && scope.row) ? scope.row.accountStatus : '' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button
              v-if="scope && scope.row"
              size="mini"
              @click="handleEdit(scope.row)"
              type="text"
            >
              编辑
            </el-button>
            <el-button
              v-if="scope && scope.row"
              size="mini"
              @click="handleToggleStatus(scope.row)"
              type="text"
              :style="{ color: scope.row.accountStatus === '正常' ? '#f56c6c' : '#67c23a' }"
            >
              {{ scope.row.accountStatus === '正常' ? '禁用' : '启用' }}
            </el-button>
            <el-button
              v-if="scope && scope.row"
              size="mini"
              @click="handleResetPassword(scope.row)"
              type="text"
            >
              重置密码
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.current"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
        />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="50%"
      :before-close="handleCloseDialog"
    >
      <el-form :model="formData" :rules="formRules" ref="userForm" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="formData.username" placeholder="请输入用户名" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户角色" prop="userRole">
              <el-select v-model="formData.userRole" placeholder="请选择用户角色" style="width: 100%">
                <el-option label="管理员" value="管理员"></el-option>
                <el-option label="用户" value="用户"></el-option>
                <el-option label="访客" value="访客"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="账户状态" prop="accountStatus">
              <el-select v-model="formData.accountStatus" placeholder="请选择账户状态" style="width: 100%">
                <el-option label="正常" value="正常"></el-option>
                <el-option label="禁用" value="禁用"></el-option>
                <el-option label="锁定" value="锁定"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="formData.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="!isEdit" label="初始密码" prop="password">
          <el-input v-model="formData.password" type="password" placeholder="请输入初始密码" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'UserManagement',
  data() {
    return {
      loading: false,
      dialogVisible: false,
      submitting: false,
      isEdit: false,
      searchForm: {
        username: '',
        userRole: '',
        accountStatus: ''
      },
      tableData: [],
      selectedRows: [],
      pagination: {
        current: 1,
        size: 20,
        total: 0
      },
      formData: {
        username: '',
        realName: '',
        userRole: '',
        accountStatus: '',
        email: '',
        phone: '',
        password: ''
      },
      formRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        realName: [
          { required: true, message: '请输入真实姓名', trigger: 'blur' }
        ],
        userRole: [
          { required: true, message: '请选择用户角色', trigger: 'change' }
        ],
        accountStatus: [
          { required: true, message: '请选择账户状态', trigger: 'change' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入初始密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑用户' : '新增用户'
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    // 加载数据
    async loadData() {
      this.loading = true
      try {
        const response = await request({
          url: '/object-model/userManagement/page',
          method: 'post',
          data: {
            current: this.pagination.current,
            size: this.pagination.size,
            ...this.searchForm
          }
        })
        
        if (response.data.code === 200) {
          this.tableData = response.data.data.records || []
          this.pagination.total = response.data.data.total || 0
        }
      } catch (error) {
        console.error('加载数据失败:', error)
        this.$message.error('加载数据失败')
      } finally {
        this.loading = false
      }
    },

    // 搜索
    handleSearch() {
      this.pagination.current = 1
      this.loadData()
    },

    // 重置
    handleReset() {
      this.searchForm = {
        username: '',
        userRole: '',
        accountStatus: ''
      }
      this.pagination.current = 1
      this.loadData()
    },

    // 新增
    handleAdd() {
      this.isEdit = false
      this.formData = {
        username: '',
        realName: '',
        userRole: '',
        accountStatus: '正常',
        email: '',
        phone: '',
        password: ''
      }
      this.dialogVisible = true
    },

    // 编辑
    handleEdit(row) {
      this.isEdit = true
      this.formData = { ...row, password: '' }
      this.dialogVisible = true
    },

    // 切换状态
    handleToggleStatus(row) {
      const newStatus = row.accountStatus === '正常' ? '禁用' : '正常'
      this.$confirm(`确认${newStatus}该用户吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$message.success(`${newStatus}成功`)
        this.loadData()
      }).catch(() => {})
    },

    // 重置密码
    handleResetPassword(row) {
      this.$confirm('确认重置该用户密码吗？重置后密码为：123456', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$message.success('密码重置成功')
      }).catch(() => {})
    },

    // 提交表单
    handleSubmit() {
      this.$refs.userForm.validate((valid) => {
        if (valid) {
          this.submitting = true
          // 模拟提交
          setTimeout(() => {
            this.submitting = false
            this.dialogVisible = false
            this.$message.success(this.isEdit ? '编辑成功' : '新增成功')
            this.loadData()
          }, 1000)
        }
      })
    },

    // 获取角色颜色
    getRoleColor(role) {
      const roleMap = {
        '管理员': 'danger',
        '用户': 'primary',
        '访客': 'info'
      }
      return roleMap[role] || 'info'
    },

    // 获取状态类型
    getStatusType(status) {
      const statusMap = {
        '正常': 'success',
        '禁用': 'danger',
        '锁定': 'warning'
      }
      return statusMap[status] || 'info'
    },

    // 选择变化
    handleSelectionChange(selection) {
      this.selectedRows = selection
    },

    // 分页大小变化
    handleSizeChange(val) {
      this.pagination.size = val
      this.loadData()
    },

    // 当前页变化
    handleCurrentChange(val) {
      this.pagination.current = val
      this.loadData()
    },

    // 关闭弹窗
    handleCloseDialog() {
      this.dialogVisible = false
      this.$refs.userForm.resetFields()
    }
  }
}
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  color: #303133;
  margin: 0 0 8px 0;
}

.page-title i {
  margin-right: 8px;
  color: #409EFF;
}

.page-description {
  color: #606266;
  margin: 0;
}

.search-section {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.table-section {
  background: white;
  border-radius: 4px;
}

.pagination-section {
  padding: 20px;
  text-align: right;
}
</style>
