<template>
  <div class="data-source-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">
        <i class="el-icon-database"></i>
        数据源配置管理
      </h2>
      <p class="page-description">管理各种数据接入源的配置，监控连接状态和数据更新频率</p>
    </div>

    <!-- 搜索和操作区域 -->
    <div class="search-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchForm.dataSourceName"
            placeholder="请输入数据源名称"
            clearable
            prefix-icon="el-icon-search"
          />
        </el-col>
        <el-col :span="6">
          <el-select v-model="searchForm.dataSourceType" placeholder="数据源类型" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option label="数据库" value="数据库"></el-option>
            <el-option label="API接口" value="API接口"></el-option>
            <el-option label="缓存" value="缓存"></el-option>
            <el-option label="文件" value="文件"></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="searchForm.connectionStatus" placeholder="连接状态" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option label="已连接" value="已连接"></el-option>
            <el-option label="连接失败" value="连接失败"></el-option>
            <el-option label="未连接" value="未连接"></el-option>
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
        <el-table-column prop="dataSourceName" label="数据源名称" min-width="200" />
        <el-table-column prop="dataSourceType" label="数据源类型" width="120">
          <template #default="scope">
            <el-tag :type="getTypeColor(scope.row.dataSourceType)" size="small">
              {{ scope.row.dataSourceType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="connectionUrl" label="连接地址" min-width="300" show-overflow-tooltip />
        <el-table-column prop="connectionStatus" label="连接状态" width="100">
          <template #default="scope">
            <el-tag
              :type="getStatusType(scope.row.connectionStatus)"
              size="small"
            >
              {{ scope.row.connectionStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateFrequency" label="更新频率" width="100" />
        <el-table-column prop="updatedTime" label="更新时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="mini" @click="handleEdit(scope.row)" type="text">编辑</el-button>
            <el-button size="mini" @click="handleTest(scope.row)" type="text">测试连接</el-button>
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
      width="50%"
      :before-close="handleCloseDialog"
    >
      <el-form :model="formData" :rules="formRules" ref="dataForm" label-width="120px">
        <el-form-item label="数据源名称" prop="dataSourceName">
          <el-input v-model="formData.dataSourceName" placeholder="请输入数据源名称" />
        </el-form-item>
        <el-form-item label="数据源类型" prop="dataSourceType">
          <el-select v-model="formData.dataSourceType" placeholder="请选择数据源类型" style="width: 100%">
            <el-option label="数据库" value="数据库"></el-option>
            <el-option label="API接口" value="API接口"></el-option>
            <el-option label="缓存" value="缓存"></el-option>
            <el-option label="文件" value="文件"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="连接地址" prop="connectionUrl">
          <el-input v-model="formData.connectionUrl" placeholder="请输入连接地址" />
        </el-form-item>
        <el-form-item label="更新频率" prop="updateFrequency">
          <el-select v-model="formData.updateFrequency" placeholder="请选择更新频率" style="width: 100%">
            <el-option label="实时" value="实时"></el-option>
            <el-option label="每分钟" value="每分钟"></el-option>
            <el-option label="每小时" value="每小时"></el-option>
            <el-option label="每天" value="每天"></el-option>
            <el-option label="每周" value="每周"></el-option>
          </el-select>
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
  name: 'DataSourceManagement',
  data() {
    return {
      loading: false,
      dialogVisible: false,
      submitting: false,
      isEdit: false,
      searchForm: {
        dataSourceName: '',
        dataSourceType: '',
        connectionStatus: ''
      },
      tableData: [],
      selectedRows: [],
      pagination: {
        current: 1,
        size: 20,
        total: 0
      },
      formData: {
        dataSourceName: '',
        dataSourceType: '',
        connectionUrl: '',
        updateFrequency: ''
      },
      formRules: {
        dataSourceName: [
          { required: true, message: '请输入数据源名称', trigger: 'blur' }
        ],
        dataSourceType: [
          { required: true, message: '请选择数据源类型', trigger: 'change' }
        ],
        connectionUrl: [
          { required: true, message: '请输入连接地址', trigger: 'blur' }
        ],
        updateFrequency: [
          { required: true, message: '请选择更新频率', trigger: 'change' }
        ]
      }
    }
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑数据源' : '新增数据源'
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
          url: '/object-model/dataSource/page',
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
        dataSourceName: '',
        dataSourceType: '',
        connectionStatus: ''
      }
      this.pagination.current = 1
      this.loadData()
    },

    // 新增
    handleAdd() {
      this.isEdit = false
      this.formData = {
        dataSourceName: '',
        dataSourceType: '',
        connectionUrl: '',
        updateFrequency: ''
      }
      this.dialogVisible = true
    },

    // 编辑
    handleEdit(row) {
      this.isEdit = true
      this.formData = { ...row }
      this.dialogVisible = true
    },

    // 测试连接
    handleTest(row) {
      this.$message.info('测试连接功能开发中...')
    },

    // 删除
    handleDelete(row) {
      this.$confirm('确认删除该数据源吗？', '提示', {
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
      this.$refs.dataForm.validate((valid) => {
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

    // 获取状态类型
    getStatusType(status) {
      const statusMap = {
        '已连接': 'success',
        '连接失败': 'danger',
        '未连接': 'info'
      }
      return statusMap[status] || 'info'
    },

    // 获取类型颜色
    getTypeColor(type) {
      const typeMap = {
        '数据库': 'primary',
        'API接口': 'success',
        '缓存': 'warning',
        '文件': 'info'
      }
      return typeMap[type] || 'info'
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
      this.$refs.dataForm.resetFields()
    }
  }
}
</script>

<style scoped>
.data-source-management {
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
