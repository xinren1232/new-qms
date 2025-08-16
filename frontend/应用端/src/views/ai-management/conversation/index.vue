<template>
  <div class="ai-conversation-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">
        <i class="icon-chat"></i>
        AI对话记录管理
      </h2>
      <p class="page-description">管理和查看AI助手的对话记录，分析对话质量和用户满意度</p>
    </div>

    <!-- 搜索和操作区域 - 基础框架，具体字段由配置端配置 -->
    <div class="search-section">
      <el-card class="search-card" shadow="never">
        <div class="search-form">
          <!-- 动态搜索字段 - 由配置端配置具体字段 -->
          <el-row :gutter="20">
            <el-col
              v-for="field in searchFields"
              :key="field.key"
              :span="field.span || 6"
            >
              <!-- 文本输入框 -->
              <el-input
                v-if="field.type === 'input'"
                v-model="searchForm[field.key]"
                :placeholder="field.placeholder"
                clearable
                :prefix-icon="field.icon"
              />
              <!-- 下拉选择框 -->
              <el-select
                v-else-if="field.type === 'select'"
                v-model="searchForm[field.key]"
                :placeholder="field.placeholder"
                clearable
              >
                <el-option
                  v-for="option in field.options"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
              <!-- 日期范围选择器 -->
              <el-date-picker
                v-else-if="field.type === 'daterange'"
                v-model="searchForm[field.key]"
                type="daterange"
                range-separator="至"
                :start-placeholder="field.startPlaceholder"
                :end-placeholder="field.endPlaceholder"
                format="yyyy-MM-dd"
                value-format="yyyy-MM-dd"
              />
            </el-col>
            <!-- 操作按钮区域 -->
            <el-col :span="6">
              <div class="search-actions">
                <el-button type="primary" @click="handleSearch" icon="el-icon-search">搜索</el-button>
                <el-button @click="handleReset" icon="el-icon-refresh">重置</el-button>
                <el-button
                  v-if="showAdvancedSearch"
                  type="text"
                  @click="toggleAdvancedSearch"
                >
                  {{ advancedSearchVisible ? '收起' : '高级搜索' }}
                </el-button>
              </div>
            </el-col>
          </el-row>

          <!-- 高级搜索区域 -->
          <div v-if="advancedSearchVisible" class="advanced-search">
            <el-divider content-position="left">高级搜索</el-divider>
            <el-row :gutter="20">
              <el-col
                v-for="field in advancedSearchFields"
                :key="field.key"
                :span="field.span || 8"
              >
                <!-- 这里可以放置更多高级搜索字段 -->
                <component
                  :is="getFieldComponent(field.type)"
                  v-model="searchForm[field.key]"
                  v-bind="field.props"
                />
              </el-col>
            </el-row>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 操作工具栏 -->
    <div class="toolbar-section">
      <el-card class="toolbar-card" shadow="never">
        <div class="toolbar-content">
          <div class="toolbar-left">
            <span class="data-count">共 {{ pagination.total }} 条数据</span>
            <el-tag v-if="selectedRows.length > 0" type="info" size="small">
              已选择 {{ selectedRows.length }} 项
            </el-tag>
          </div>
          <div class="toolbar-right">
            <!-- 动态操作按钮 - 由配置端配置具体按钮 -->
            <el-button
              v-for="action in tableActions"
              :key="action.key"
              :type="action.type || 'default'"
              :size="action.size || 'small'"
              :icon="action.icon"
              :disabled="action.disabled || (action.needSelection && selectedRows.length === 0)"
              @click="handleAction(action)"
            >
              {{ action.label }}
            </el-button>

            <!-- 表格设置 -->
            <el-dropdown @command="handleTableSetting">
              <el-button size="small" icon="el-icon-setting">
                表格设置<i class="el-icon-arrow-down el-icon--right"></i>
              </el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="refresh">刷新数据</el-dropdown-item>
                <el-dropdown-item command="export">导出数据</el-dropdown-item>
                <el-dropdown-item command="columns">列设置</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 数据表格 - 基础框架，具体列由配置端配置 -->
    <div class="table-section">
      <el-card class="table-card" shadow="never">
        <el-table
          :data="tableData"
          v-loading="loading"
          stripe
          border
          style="width: 100%"
          :height="pageConfig.tableHeight"
          @selection-change="handleSelectionChange"
          @sort-change="handleSortChange"
          @row-click="handleRowClick"
        >
          <!-- 选择列 -->
          <el-table-column
            v-if="pageConfig.showSelection"
            type="selection"
            width="55"
            fixed="left"
          />

          <!-- 序号列 -->
          <el-table-column
            v-if="pageConfig.showIndex"
            type="index"
            label="序号"
            width="60"
            fixed="left"
          />

          <!-- 动态数据列 - 由配置端配置具体列 -->
          <el-table-column
            v-for="column in visibleColumns"
            :key="column.prop"
            :prop="column.prop"
            :label="column.label"
            :width="column.width"
            :min-width="column.minWidth"
            :fixed="column.fixed"
            :sortable="column.sortable"
            :show-overflow-tooltip="column.showTooltip !== false"
          >
            <template #default="scope">
              <!-- 根据列类型渲染不同组件 -->
              <component
                :is="getColumnComponent(column.type)"
                :value="scope.row[column.prop]"
                :column="column"
                :row="scope.row"
                @action="handleCellAction"
              />
            </template>
          </el-table-column>

          <!-- 操作列 -->
          <el-table-column
            v-if="rowActions.length > 0"
            label="操作"
            :width="getActionColumnWidth()"
            fixed="right"
          >
            <template #default="scope">
              <div class="row-actions">
                <el-button
                  v-for="action in getRowActions(scope.row)"
                  :key="action.key"
                  :type="action.type || 'text'"
                  :size="action.size || 'mini'"
                  :icon="action.icon"
                  :disabled="action.disabled"
                  @click="handleRowAction(action, scope.row)"
                >
                  {{ action.label }}
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

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

    <!-- 对话详情弹窗 -->
    <el-dialog
      title="对话详情"
      :visible.sync="dialogVisible"
      width="60%"
      :before-close="handleCloseDialog"
    >
      <div v-if="currentConversation" class="conversation-detail">
        <div class="detail-item">
          <label>对话ID：</label>
          <span>{{ currentConversation.conversationId }}</span>
        </div>
        <div class="detail-item">
          <label>用户问题：</label>
          <div class="question-content">{{ currentConversation.userQuestion }}</div>
        </div>
        <div class="detail-item">
          <label>AI回答：</label>
          <div class="answer-content">{{ currentConversation.aiResponse }}</div>
        </div>
        <div class="detail-item">
          <label>对话状态：</label>
          <el-tag :type="getStatusType(currentConversation.conversationStatus)">
            {{ currentConversation.conversationStatus }}
          </el-tag>
        </div>
        <div class="detail-item">
          <label>满意度评分：</label>
          <el-rate
            v-model="currentConversation.satisfactionScore"
            disabled
            show-score
            text-color="#ff9900"
          />
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'
import configSyncService from '@/services/configSync'

export default {
  name: 'AIConversationManagement',
  data() {
    return {
      loading: false,

      // 基础数据
      tableData: [],
      selectedRows: [],
      currentConversation: {},

      // 配置数据 - 由配置端提供
      pageConfig: {
        title: 'AI对话记录管理',
        description: '管理和查看AI助手的对话记录，分析对话质量和用户满意度',
        showSelection: true,
        showIndex: true,
        tableHeight: null
      },

      // 搜索字段配置 - 由配置端配置
      searchFields: [
        {
          key: 'userId',
          type: 'input',
          placeholder: '请输入用户ID',
          icon: 'el-icon-user',
          span: 6
        },
        {
          key: 'status',
          type: 'select',
          placeholder: '对话状态',
          span: 6,
          options: [
            { label: '全部', value: '' },
            { label: '进行中', value: '进行中' },
            { label: '已完成', value: '已完成' },
            { label: '已中断', value: '已中断' }
          ]
        },
        {
          key: 'dateRange',
          type: 'daterange',
          span: 6,
          startPlaceholder: '开始日期',
          endPlaceholder: '结束日期'
        }
      ],

      // 高级搜索字段
      advancedSearchFields: [],
      advancedSearchVisible: false,
      showAdvancedSearch: false,

      // 表格列配置 - 由配置端配置
      tableColumns: [
        {
          prop: 'conversationId',
          label: '对话ID',
          width: 150,
          type: 'text',
          sortable: true,
          visible: true
        },
        {
          prop: 'userId',
          label: '用户ID',
          width: 120,
          type: 'text',
          sortable: true,
          visible: true
        },
        {
          prop: 'userQuestion',
          label: '用户问题',
          minWidth: 300,
          type: 'text',
          showTooltip: true,
          visible: true
        },
        {
          prop: 'conversationStatus',
          label: '对话状态',
          width: 100,
          type: 'tag',
          sortable: true,
          visible: true
        },
        {
          prop: 'satisfactionScore',
          label: '满意度',
          width: 100,
          type: 'rate',
          visible: true
        },
        {
          prop: 'conversationTime',
          label: '对话时间',
          width: 160,
          type: 'datetime',
          sortable: true,
          visible: true
        }
      ],

      // 操作按钮配置 - 由配置端配置
      tableActions: [
        {
          key: 'refresh',
          label: '刷新',
          type: 'primary',
          icon: 'el-icon-refresh',
          permission: 'view'
        },
        {
          key: 'export',
          label: '导出',
          type: 'success',
          icon: 'el-icon-download',
          permission: 'export'
        },
        {
          key: 'batchDelete',
          label: '批量删除',
          type: 'danger',
          icon: 'el-icon-delete',
          needSelection: true,
          permission: 'delete'
        }
      ],

      // 行操作配置 - 由配置端配置
      rowActions: [
        {
          key: 'view',
          label: '查看',
          type: 'text',
          icon: 'el-icon-view',
          permission: 'view'
        },
        {
          key: 'analyze',
          label: '分析',
          type: 'text',
          icon: 'el-icon-data-analysis',
          permission: 'analyze'
        }
      ],
      dialogVisible: false,
      currentConversation: null,
      searchForm: {
        userId: '',
        status: '',
        dateRange: []
      },
      tableData: [],
      selectedRows: [],
      pagination: {
        current: 1,
        size: 20,
        total: 0
      }
    }
  },

  computed: {
    // 可见的表格列
    visibleColumns() {
      return this.tableColumns.filter(column =>
        column.visible && this.hasPermission(column.permission)
      )
    },

    // 允许的表格操作
    allowedTableActions() {
      return this.tableActions.filter(action =>
        this.hasPermission(action.permission)
      )
    },

    // 允许的行操作
    allowedRowActions() {
      return this.rowActions.filter(action =>
        this.hasPermission(action.permission)
      )
    }
  },

  mounted() {
    this.loadData()
    this.initConfigSync()
  },
  methods: {
    // 初始化配置同步
    initConfigSync() {
      // 监听配置更新事件
      configSyncService.onConfigUpdate((event) => {
        if (event.detail.type === 'aiManagement') {
          console.log('🔄 AI对话记录页面收到配置更新', event.detail.data)
          this.updatePageConfig(event.detail.data.aiConversation)
        }
      })

      // 获取当前配置
      const currentConfig = configSyncService.getCachedConfig('aiManagement')
      if (currentConfig && currentConfig.aiConversation) {
        this.updatePageConfig(currentConfig.aiConversation)
      }
    },

    // 配置驱动相关方法

    // 权限检查 - 与配置端的权限系统集成
    hasPermission(permission) {
      if (!permission) return true
      // 这里应该调用配置端的权限检查逻辑
      // 暂时返回true，实际应该从配置端获取用户权限
      return true
    },

    // 获取字段组件类型
    getFieldComponent(type) {
      const componentMap = {
        'input': 'el-input',
        'select': 'el-select',
        'daterange': 'el-date-picker',
        'number': 'el-input-number',
        'switch': 'el-switch'
      }
      return componentMap[type] || 'el-input'
    },

    // 获取列组件类型
    getColumnComponent(type) {
      const componentMap = {
        'text': 'span',
        'tag': 'el-tag',
        'rate': 'el-rate',
        'datetime': 'span',
        'link': 'el-link',
        'image': 'el-image'
      }
      return componentMap[type] || 'span'
    },

    // 获取操作列宽度
    getActionColumnWidth() {
      const actionCount = this.allowedRowActions.length
      return Math.max(actionCount * 60, 120)
    },

    // 获取行操作按钮
    getRowActions(row) {
      return this.allowedRowActions.filter(action => {
        // 可以根据行数据动态判断按钮是否显示
        if (action.condition) {
          return action.condition(row)
        }
        return true
      })
    },

    // 切换高级搜索
    toggleAdvancedSearch() {
      this.advancedSearchVisible = !this.advancedSearchVisible
    },

    // 处理表格操作
    handleAction(action) {
      console.log('执行操作:', action.key)
      switch (action.key) {
        case 'refresh':
          this.loadData()
          break
        case 'export':
          this.exportData()
          break
        case 'batchDelete':
          this.batchDelete()
          break
        default:
          // 可以通过配置端定义的自定义操作
          this.handleCustomAction(action)
      }
    },

    // 处理行操作
    handleRowAction(action, row) {
      console.log('执行行操作:', action.key, row)
      switch (action.key) {
        case 'view':
          this.viewDetail(row)
          break
        case 'analyze':
          this.analyzeConversation(row)
          break
        default:
          this.handleCustomRowAction(action, row)
      }
    },

    // 处理单元格操作
    handleCellAction(action, value, row, column) {
      console.log('单元格操作:', action, value, row, column)
    },

    // 处理表格设置
    handleTableSetting(command) {
      switch (command) {
        case 'refresh':
          this.loadData()
          break
        case 'export':
          this.exportData()
          break
        case 'columns':
          this.showColumnSetting()
          break
      }
    },

    // 处理排序变化
    handleSortChange({ column, prop, order }) {
      console.log('排序变化:', prop, order)
      // 可以根据配置端的排序规则进行处理
      this.loadData()
    },

    // 处理行点击
    handleRowClick(row) {
      console.log('行点击:', row)
      // 可以根据配置端的设置决定是否显示详情
      if (this.pageConfig.rowClickAction === 'detail') {
        this.viewDetail(row)
      }
    },

    // 更新页面配置
    updatePageConfig(config) {
      if (config && config.viewConfig) {
        // 更新分页大小
        if (config.viewConfig.pageSize) {
          this.pagination.size = config.viewConfig.pageSize
        }

        // 可以根据配置动态调整其他页面行为
        console.log('📋 AI对话记录页面配置已更新:', config)
      }
    },

    // 加载数据
    async loadData() {
      this.loading = true
      try {
        // 调用API获取AI对话记录数据
        const response = await request.post('/object-model/aiConversation/page', {
          current: this.pagination.current,
          size: this.pagination.size,
          ...this.searchForm
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
        userId: '',
        status: '',
        dateRange: []
      }
      this.pagination.current = 1
      this.loadData()
    },

    // 查看详情
    handleView(row) {
      this.currentConversation = row
      this.dialogVisible = true
    },

    // 质量分析
    handleAnalyze(row) {
      this.$message.info('质量分析功能开发中...')
    },

    // 导出
    handleExport(row) {
      this.$message.info('导出功能开发中...')
    },

    // 获取状态类型
    getStatusType(status) {
      const statusMap = {
        '进行中': 'warning',
        '已完成': 'success',
        '已中断': 'danger'
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
      this.currentConversation = null
    }
  }
}
</script>

<style scoped>
.ai-conversation-management {
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

.page-title .icon-chat {
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

.conversation-detail .detail-item {
  margin-bottom: 15px;
}

.conversation-detail .detail-item label {
  font-weight: bold;
  color: #303133;
  display: inline-block;
  width: 100px;
}

.question-content,
.answer-content {
  background: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  margin-top: 5px;
  line-height: 1.6;
}

.answer-content {
  background: #e8f4fd;
}
</style>
