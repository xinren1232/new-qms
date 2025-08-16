<template>
  <div class="ai-rule-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">
        <i class="el-icon-setting"></i>
        AI规则配置管理
      </h2>
      <p class="page-description">配置AI的逻辑和检索规则，管理触发条件和执行动作</p>
    </div>

    <!-- 搜索和操作区域 -->
    <div class="search-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchForm.ruleName"
            placeholder="请输入规则名称"
            clearable
            prefix-icon="el-icon-search"
          />
        </el-col>
        <el-col :span="6">
          <el-select v-model="searchForm.ruleType" placeholder="规则类型" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option label="检索规则" value="检索规则"></el-option>
            <el-option label="过滤规则" value="过滤规则"></el-option>
            <el-option label="回复规则" value="回复规则"></el-option>
            <el-option label="处理规则" value="处理规则"></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="searchForm.ruleStatus" placeholder="规则状态" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option label="启用" value="启用"></el-option>
            <el-option label="禁用" value="禁用"></el-option>
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
        <el-table-column prop="ruleName" label="规则名称" min-width="200" />
        <el-table-column prop="ruleType" label="规则类型" width="120">
          <template #default="scope">
            <el-tag :type="getTypeColor(scope.row.ruleType)" size="small">
              {{ scope.row.ruleType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="scope">
            <el-tag :type="getPriorityColor(scope.row.priority)" size="small">
              {{ scope.row.priority }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ruleStatus" label="规则状态" width="100">
          <template #default="scope">
            <el-tag
              :type="scope.row.ruleStatus === '启用' ? 'success' : 'danger'"
              size="small"
            >
              {{ scope.row.ruleStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="triggerCondition" label="触发条件" min-width="200" show-overflow-tooltip />
        <el-table-column prop="executeAction" label="执行动作" min-width="200" show-overflow-tooltip />
        <el-table-column prop="updatedTime" label="更新时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="mini" @click="handleEdit(scope.row)" type="text">编辑</el-button>
            <el-button
              size="mini"
              @click="handleToggleStatus(scope.row)"
              type="text"
              :style="{ color: scope.row.ruleStatus === '启用' ? '#f56c6c' : '#67c23a' }"
            >
              {{ scope.row.ruleStatus === '启用' ? '禁用' : '启用' }}
            </el-button>
            <el-button size="mini" @click="handleDelete(scope.row)" type="text" style="color: #f56c6c;">删除</el-button>
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
      width="60%"
      :before-close="handleCloseDialog"
    >
      <el-form :model="formData" :rules="formRules" ref="ruleForm" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="规则名称" prop="ruleName">
              <el-input v-model="formData.ruleName" placeholder="请输入规则名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规则类型" prop="ruleType">
              <el-select v-model="formData.ruleType" placeholder="请选择规则类型" style="width: 100%">
                <el-option label="检索规则" value="检索规则"></el-option>
                <el-option label="过滤规则" value="过滤规则"></el-option>
                <el-option label="回复规则" value="回复规则"></el-option>
                <el-option label="处理规则" value="处理规则"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="formData.priority" placeholder="请选择优先级" style="width: 100%">
                <el-option label="高" value="高"></el-option>
                <el-option label="中" value="中"></el-option>
                <el-option label="低" value="低"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规则状态" prop="ruleStatus">
              <el-select v-model="formData.ruleStatus" placeholder="请选择规则状态" style="width: 100%">
                <el-option label="启用" value="启用"></el-option>
                <el-option label="禁用" value="禁用"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="触发条件" prop="triggerCondition">
          <el-input 
            v-model="formData.triggerCondition" 
            type="textarea" 
            :rows="3"
            placeholder="请输入触发条件描述"
          />
        </el-form-item>
        <el-form-item label="执行动作" prop="executeAction">
          <el-input 
            v-model="formData.executeAction" 
            type="textarea" 
            :rows="3"
            placeholder="请输入执行动作描述"
          />
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
  name: 'AIRuleManagement',
  data() {
    return {
      loading: false,
      dialogVisible: false,
      submitting: false,
      isEdit: false,
      searchForm: {
        ruleName: '',
        ruleType: '',
        ruleStatus: ''
      },
      tableData: [],
      selectedRows: [],
      pagination: {
        current: 1,
        size: 20,
        total: 0
      },
      formData: {
        ruleName: '',
        ruleType: '',
        priority: '',
        ruleStatus: '',
        triggerCondition: '',
        executeAction: ''
      },
      formRules: {
        ruleName: [
          { required: true, message: '请输入规则名称', trigger: 'blur' }
        ],
        ruleType: [
          { required: true, message: '请选择规则类型', trigger: 'change' }
        ],
        priority: [
          { required: true, message: '请选择优先级', trigger: 'change' }
        ],
        ruleStatus: [
          { required: true, message: '请选择规则状态', trigger: 'change' }
        ],
        triggerCondition: [
          { required: true, message: '请输入触发条件', trigger: 'blur' }
        ],
        executeAction: [
          { required: true, message: '请输入执行动作', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑AI规则' : '新增AI规则'
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
          url: '/object-model/aiRule/page',
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
        ruleName: '',
        ruleType: '',
        ruleStatus: ''
      }
      this.pagination.current = 1
      this.loadData()
    },

    // 新增
    handleAdd() {
      this.isEdit = false
      this.formData = {
        ruleName: '',
        ruleType: '',
        priority: '',
        ruleStatus: '启用',
        triggerCondition: '',
        executeAction: ''
      }
      this.dialogVisible = true
    },

    // 编辑
    handleEdit(row) {
      this.isEdit = true
      this.formData = { ...row }
      this.dialogVisible = true
    },

    // 切换状态
    handleToggleStatus(row) {
      const newStatus = row.ruleStatus === '启用' ? '禁用' : '启用'
      this.$confirm(`确认${newStatus}该规则吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$message.success(`${newStatus}成功`)
        this.loadData()
      }).catch(() => {})
    },

    // 删除
    handleDelete(row) {
      this.$confirm('确认删除该规则吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$message.success('删除成功')
        this.loadData()
      }).catch(() => {})
    },

    // 提交表单
    handleSubmit() {
      this.$refs.ruleForm.validate((valid) => {
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

    // 获取类型颜色
    getTypeColor(type) {
      const typeMap = {
        '检索规则': 'primary',
        '过滤规则': 'success',
        '回复规则': 'warning',
        '处理规则': 'info'
      }
      return typeMap[type] || 'info'
    },

    // 获取优先级颜色
    getPriorityColor(priority) {
      const priorityMap = {
        '高': 'danger',
        '中': 'warning',
        '低': 'info'
      }
      return priorityMap[priority] || 'info'
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
      this.$refs.ruleForm.resetFields()
    }
  }
}
</script>

<style scoped>
.ai-rule-management {
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
