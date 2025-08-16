<template>
  <div class="data-source-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">智能数据源管理</h2>
      <p class="page-description">统一管理AI系统的数据源，实时监控连接状态和性能指标</p>
    </div>

    <!-- 数据源概览 -->
    <el-row :gutter="20" class="datasource-overview">
      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-icon success">
              <el-icon><Database /></el-icon>
            </div>
            <div class="metric-info">
              <h3>{{ totalDataSources }}</h3>
              <p>总数据源</p>
              <span class="metric-trend positive">+2</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-icon primary">
              <el-icon><Connection /></el-icon>
            </div>
            <div class="metric-info">
              <h3>{{ activeConnections }}</h3>
              <p>活跃连接</p>
              <span class="metric-trend positive">+1</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-icon warning">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="metric-info">
              <h3>{{ errorConnections }}</h3>
              <p>异常连接</p>
              <span class="metric-trend negative">+1</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-icon info">
              <el-icon><Timer /></el-icon>
            </div>
            <div class="metric-info">
              <h3>{{ avgResponseTime }}ms</h3>
              <p>平均响应时间</p>
              <span class="metric-trend positive">-15ms</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索和操作区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="数据源名称">
          <el-input v-model="searchForm.name" placeholder="请输入数据源名称" clearable />
        </el-form-item>
        <el-form-item label="数据源类型">
          <el-select v-model="searchForm.type" placeholder="请选择类型" clearable>
            <el-option label="全部" value="" />
            <el-option label="MySQL" value="mysql" />
            <el-option label="PostgreSQL" value="postgresql" />
            <el-option label="MongoDB" value="mongodb" />
            <el-option label="Redis" value="redis" />
            <el-option label="Elasticsearch" value="elasticsearch" />
          </el-select>
        </el-form-item>
        <el-form-item label="连接状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="正常" value="connected" />
            <el-option label="异常" value="error" />
            <el-option label="未连接" value="disconnected" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading" icon="Search">
            搜索
          </el-button>
          <el-button @click="handleReset" icon="Refresh">重置</el-button>
          <el-button type="success" @click="showAddDialog = true" icon="Plus">
            新增数据源
          </el-button>
          <el-button type="warning" @click="testAllConnections" icon="Connection">
            批量测试连接
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据源列表 -->
    <el-card class="table-card" shadow="never">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="name" label="数据源名称" width="200" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)" size="small">
              {{ getTypeName(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="host" label="主机地址" width="150" />
        <el-table-column prop="port" label="端口" width="80" />
        <el-table-column prop="database" label="数据库" width="120" />
        <el-table-column prop="status" label="连接状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              <el-icon><component :is="getStatusIcon(row.status)" /></el-icon>
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="responseTime" label="响应时间" width="100">
          <template #default="{ row }">
            <span :class="getResponseTimeClass(row.responseTime)">
              {{ row.responseTime }}ms
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="lastTest" label="最后测试" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="text" @click="testConnection(row)" icon="Connection" size="small">
              测试连接
            </el-button>
            <el-button type="text" @click="editDataSource(row)" icon="Edit" size="small">
              编辑
            </el-button>
            <el-button type="text" @click="viewDetails(row)" icon="View" size="small">
              详情
            </el-button>
            <el-button type="text" @click="deleteDataSource(row)" icon="Delete" size="small" style="color: #f56c6c;">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑数据源对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingDataSource ? '编辑数据源' : '新增数据源'"
      width="600px"
    >
      <el-form :model="dataSourceForm" :rules="formRules" ref="dataSourceFormRef" label-width="100px">
        <el-form-item label="数据源名称" prop="name">
          <el-input v-model="dataSourceForm.name" placeholder="请输入数据源名称" />
        </el-form-item>
        <el-form-item label="数据源类型" prop="type">
          <el-select v-model="dataSourceForm.type" placeholder="请选择数据源类型" style="width: 100%">
            <el-option label="MySQL" value="mysql" />
            <el-option label="PostgreSQL" value="postgresql" />
            <el-option label="MongoDB" value="mongodb" />
            <el-option label="Redis" value="redis" />
            <el-option label="Elasticsearch" value="elasticsearch" />
          </el-select>
        </el-form-item>
        <el-form-item label="主机地址" prop="host">
          <el-input v-model="dataSourceForm.host" placeholder="请输入主机地址" />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input-number v-model="dataSourceForm.port" :min="1" :max="65535" style="width: 100%" />
        </el-form-item>
        <el-form-item label="数据库名" prop="database">
          <el-input v-model="dataSourceForm.database" placeholder="请输入数据库名" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="dataSourceForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="dataSourceForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="dataSourceForm.description" type="textarea" :rows="3" placeholder="请输入描述信息" />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="primary" @click="saveDataSource" :loading="saving">
            {{ editingDataSource ? '更新' : '保存' }}
          </el-button>
          <el-button type="success" @click="testAndSave" :loading="testing">
            测试并保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { formatDate } from '@/utils/format'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const testing = ref(false)
const tableData = ref([])
const selectedRows = ref([])
const showAddDialog = ref(false)
const editingDataSource = ref(null)
const dataSourceFormRef = ref()

// 搜索表单
const searchForm = ref({
  name: '',
  type: '',
  status: ''
})

// 分页数据
const pagination = ref({
  current: 1,
  size: 20,
  total: 0
})

// 概览数据
const totalDataSources = ref(12)
const activeConnections = ref(10)
const errorConnections = ref(2)
const avgResponseTime = ref(85)

// 数据源表单
const dataSourceForm = reactive({
  name: '',
  type: '',
  host: '',
  port: 3306,
  database: '',
  username: '',
  password: '',
  description: ''
})

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入数据源名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择数据源类型', trigger: 'change' }],
  host: [{ required: true, message: '请输入主机地址', trigger: 'blur' }],
  port: [{ required: true, message: '请输入端口', trigger: 'blur' }],
  database: [{ required: true, message: '请输入数据库名', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 模拟数据
const mockData = [
  {
    id: 1,
    name: '主数据库',
    type: 'mysql',
    host: '192.168.1.100',
    port: 3306,
    database: 'qms_main',
    status: 'connected',
    responseTime: 45,
    lastTest: '2024-01-15 14:30:25'
  },
  {
    id: 2,
    name: '用户数据库',
    type: 'postgresql',
    host: '192.168.1.101',
    port: 5432,
    database: 'user_db',
    status: 'connected',
    responseTime: 62,
    lastTest: '2024-01-15 14:28:15'
  },
  {
    id: 3,
    name: '缓存数据库',
    type: 'redis',
    host: '192.168.1.102',
    port: 6379,
    database: '0',
    status: 'error',
    responseTime: 0,
    lastTest: '2024-01-15 14:25:10'
  }
]

// 工具方法
const getTypeColor = (type) => {
  const colorMap = {
    mysql: 'primary',
    postgresql: 'success',
    mongodb: 'warning',
    redis: 'danger',
    elasticsearch: 'info'
  }
  return colorMap[type] || 'info'
}

const getTypeName = (type) => {
  const nameMap = {
    mysql: 'MySQL',
    postgresql: 'PostgreSQL',
    mongodb: 'MongoDB',
    redis: 'Redis',
    elasticsearch: 'Elasticsearch'
  }
  return nameMap[type] || type
}

const getStatusType = (status) => {
  const typeMap = {
    connected: 'success',
    error: 'danger',
    disconnected: 'info'
  }
  return typeMap[status] || 'info'
}

const getStatusIcon = (status) => {
  const iconMap = {
    connected: 'CircleCheckFilled',
    error: 'CircleCloseFilled',
    disconnected: 'WarningFilled'
  }
  return iconMap[status] || 'InfoFilled'
}

const getStatusText = (status) => {
  const textMap = {
    connected: '正常',
    error: '异常',
    disconnected: '未连接'
  }
  return textMap[status] || '未知'
}

const getResponseTimeClass = (time) => {
  if (time === 0) return 'response-error'
  if (time < 50) return 'response-excellent'
  if (time < 100) return 'response-good'
  if (time < 200) return 'response-average'
  return 'response-poor'
}

// 业务方法
const loadData = () => {
  loading.value = true
  setTimeout(() => {
    tableData.value = mockData
    pagination.value.total = mockData.length
    loading.value = false
  }, 500)
}

const handleSearch = () => {
  pagination.value.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.value = {
    name: '',
    type: '',
    status: ''
  }
  pagination.value.current = 1
  loadData()
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const handlePageChange = (page) => {
  pagination.value.current = page
  loadData()
}

const handleSizeChange = (size) => {
  pagination.value.size = size
  pagination.value.current = 1
  loadData()
}

const testConnection = (row) => {
  ElMessage.info(`正在测试 ${row.name} 的连接...`)
  setTimeout(() => {
    ElMessage.success(`${row.name} 连接测试成功`)
  }, 1000)
}

const testAllConnections = () => {
  ElMessage.info('正在批量测试所有数据源连接...')
  setTimeout(() => {
    ElMessage.success('批量连接测试完成')
  }, 2000)
}

const editDataSource = (row) => {
  editingDataSource.value = row
  Object.assign(dataSourceForm, row)
  showAddDialog.value = true
}

const viewDetails = (row) => {
  ElMessage.info(`查看 ${row.name} 的详细信息`)
}

const deleteDataSource = (row) => {
  ElMessageBox.confirm(
    `确定要删除数据源 "${row.name}" 吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

const saveDataSource = async () => {
  try {
    await dataSourceFormRef.value.validate()
    saving.value = true

    setTimeout(() => {
      ElMessage.success(editingDataSource.value ? '更新成功' : '保存成功')
      showAddDialog.value = false
      resetForm()
      loadData()
      saving.value = false
    }, 1000)
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const testAndSave = async () => {
  try {
    await dataSourceFormRef.value.validate()
    testing.value = true

    setTimeout(() => {
      ElMessage.success('连接测试成功，数据源已保存')
      showAddDialog.value = false
      resetForm()
      loadData()
      testing.value = false
    }, 2000)
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const resetForm = () => {
  Object.assign(dataSourceForm, {
    name: '',
    type: '',
    host: '',
    port: 3306,
    database: '',
    username: '',
    password: '',
    description: ''
  })
  editingDataSource.value = null
}

// 组件挂载
onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.data-source-page {
  padding: 0;
}

.page-header {
  margin-bottom: 24px;

  .page-title {
    margin: 0 0 8px 0;
    font-size: 24px;
    font-weight: 500;
    color: #303133;
  }

  .page-description {
    margin: 0;
    font-size: 14px;
    color: #606266;
    line-height: 1.5;
  }
}

.datasource-overview {
  margin-bottom: 20px;
}

.metric-card {
  .metric-content {
    display: flex;
    align-items: center;
    gap: 16px;

    .metric-icon {
      width: 48px;
      height: 48px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 24px;

      &.success {
        background: linear-gradient(135deg, #67c23a, #85ce61);
      }

      &.warning {
        background: linear-gradient(135deg, #e6a23c, #ebb563);
      }

      &.info {
        background: linear-gradient(135deg, #909399, #a6a9ad);
      }

      &.primary {
        background: linear-gradient(135deg, #409eff, #66b1ff);
      }
    }

    .metric-info {
      h3 {
        margin: 0 0 4px 0;
        font-size: 24px;
        font-weight: 600;
        color: #303133;
      }

      p {
        margin: 0 0 4px 0;
        font-size: 14px;
        color: #606266;
      }

      .metric-trend {
        font-size: 12px;

        &.positive {
          color: #67c23a;
        }

        &.negative {
          color: #f56c6c;
        }
      }
    }
  }
}

.search-card,
.table-card {
  margin-bottom: 16px;

  :deep(.el-card__body) {
    padding: 16px;
  }
}

.table-card {
  :deep(.el-card__body) {
    padding: 0;
  }
}

.pagination-wrapper {
  padding: 16px;
  text-align: right;
  border-top: 1px solid #ebeef5;
}

// 响应时间样式
.response-excellent {
  color: #67c23a;
  font-weight: 600;
}

.response-good {
  color: #409eff;
  font-weight: 500;
}

.response-average {
  color: #e6a23c;
  font-weight: 500;
}

.response-poor {
  color: #f56c6c;
  font-weight: 600;
}

.response-error {
  color: #909399;
  font-style: italic;
}

@media (max-width: 768px) {
  .metric-card .metric-content {
    flex-direction: column;
    text-align: center;
    gap: 8px;
  }

  .pagination-wrapper {
    text-align: center;
  }
}
</style>
