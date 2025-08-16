<template>
  <div class="ai-rule-config">
    <div class="page-header">
      <h2>AI规则配置</h2>
      <p class="text-muted">配置AI行为规则和约束条件</p>
    </div>

    <!-- 规则统计 -->
    <div class="rule-stats">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon total">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">总规则数</div>
                <div class="stat-value">{{ stats.totalRules }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon active">
                <el-icon><SuccessFilled /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">启用规则</div>
                <div class="stat-value">{{ stats.activeRules }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon triggered">
                <el-icon><Bell /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">今日触发</div>
                <div class="stat-value">{{ stats.todayTriggered }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon blocked">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">拦截次数</div>
                <div class="stat-value">{{ stats.blockedCount }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 规则分类 -->
    <div class="rule-categories">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>规则分类管理</span>
            <el-button type="primary" @click="handleAddCategory">
              <el-icon><Plus /></el-icon>
              新增分类
            </el-button>
          </div>
        </template>

        <el-row :gutter="20">
          <el-col :span="6" v-for="category in ruleCategories" :key="category.id">
            <div class="category-card" @click="handleSelectCategory(category)">
              <div class="category-icon" :class="category.type">
                <el-icon>
                  <component :is="category.icon" />
                </el-icon>
              </div>
              <div class="category-info">
                <div class="category-name">{{ category.name }}</div>
                <div class="category-count">{{ category.ruleCount }} 条规则</div>
              </div>
              <div class="category-status">
                <el-tag :type="category.enabled ? 'success' : 'info'">
                  {{ category.enabled ? '启用' : '禁用' }}
                </el-tag>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- 规则列表 -->
    <div class="rules-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>规则列表 - {{ selectedCategory?.name || '全部规则' }}</span>
            <div class="header-actions">
              <el-input
                v-model="searchText"
                placeholder="搜索规则..."
                size="small"
                style="width: 200px;"
                clearable
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-button type="primary" @click="handleAddRule">
                <el-icon><Plus /></el-icon>
                新增规则
              </el-button>
            </div>
          </div>
        </template>

        <!-- 规则表格 -->
        <vxe-table
          ref="ruleTableRef"
          :data="filteredRules"
          :loading="loading"
          border
          stripe
          resizable
          height="500"
        >
          <vxe-column type="seq" width="60" title="序号" />
          <vxe-column field="name" title="规则名称" min-width="150">
            <template #default="{ row }">
              <div class="rule-name-cell">
                <div class="rule-priority" :class="getPriorityClass(row.priority)">
                  {{ row.priority }}
                </div>
                <span class="rule-name">{{ row.name }}</span>
              </div>
            </template>
          </vxe-column>
          <vxe-column field="category" title="分类" width="120">
            <template #default="{ row }">
              <el-tag :type="getCategoryTagType(row.category)">
                {{ getCategoryText(row.category) }}
              </el-tag>
            </template>
          </vxe-column>
          <vxe-column field="condition" title="触发条件" min-width="200" show-overflow />
          <vxe-column field="action" title="执行动作" min-width="150">
            <template #default="{ row }">
              <el-tag :type="getActionTagType(row.action)">
                {{ getActionText(row.action) }}
              </el-tag>
            </template>
          </vxe-column>
          <vxe-column field="triggerCount" title="触发次数" width="100" />
          <vxe-column field="lastTriggered" title="最后触发" width="150" />
          <vxe-column field="enabled" title="状态" width="100">
            <template #default="{ row }">
              <el-switch
                v-model="row.enabled"
                @change="handleRuleStatusChange(row)"
              />
            </template>
          </vxe-column>
          <vxe-column title="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleEditRule(row)">
                编辑
              </el-button>
              <el-button size="small" @click="handleTestRule(row)">
                测试
              </el-button>
              <el-button type="danger" size="small" @click="handleDeleteRule(row)">
                删除
              </el-button>
            </template>
          </vxe-column>
        </vxe-table>
      </el-card>
    </div>

    <!-- 规则编辑对话框 -->
    <el-dialog
      v-model="ruleDialogVisible"
      :title="editingRule.id ? '编辑规则' : '新增规则'"
      width="800px"
    >
      <el-form :model="editingRule" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="规则名称">
              <el-input v-model="editingRule.name" placeholder="请输入规则名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规则分类">
              <el-select v-model="editingRule.category" placeholder="选择分类">
                <el-option
                  v-for="cat in ruleCategories"
                  :key="cat.id"
                  :label="cat.name"
                  :value="cat.type"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="优先级">
              <el-select v-model="editingRule.priority" placeholder="选择优先级">
                <el-option label="高" value="高" />
                <el-option label="中" value="中" />
                <el-option label="低" value="低" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="执行动作">
              <el-select v-model="editingRule.action" placeholder="选择动作">
                <el-option label="拦截" value="block" />
                <el-option label="警告" value="warn" />
                <el-option label="记录" value="log" />
                <el-option label="转发" value="forward" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="触发条件">
          <el-input
            v-model="editingRule.condition"
            type="textarea"
            :rows="3"
            placeholder="请输入触发条件，支持正则表达式"
          />
        </el-form-item>

        <el-form-item label="规则描述">
          <el-input
            v-model="editingRule.description"
            type="textarea"
            :rows="2"
            placeholder="请输入规则描述"
          />
        </el-form-item>

        <el-form-item label="启用规则">
          <el-switch v-model="editingRule.enabled" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveRule">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document,
  SuccessFilled,
  Bell,
  Warning,
  Plus,
  Search,
  Shield,
  ChatDotRound,
  DataAnalysis,
  Lock
} from '@element-plus/icons-vue'

// 响应式数据
const ruleTableRef = ref()
const loading = ref(false)
const searchText = ref('')
const ruleDialogVisible = ref(false)
const selectedCategory = ref(null)

// 统计数据
const stats = reactive({
  totalRules: 24,
  activeRules: 18,
  todayTriggered: 156,
  blockedCount: 23
})

// 规则分类
const ruleCategories = ref([
  {
    id: 1,
    name: '内容安全',
    type: 'content',
    icon: 'Shield',
    ruleCount: 8,
    enabled: true
  },
  {
    id: 2,
    name: '对话控制',
    type: 'conversation',
    icon: 'ChatDotRound',
    ruleCount: 6,
    enabled: true
  },
  {
    id: 3,
    name: '数据保护',
    type: 'data',
    icon: 'DataAnalysis',
    ruleCount: 5,
    enabled: true
  },
  {
    id: 4,
    name: '访问控制',
    type: 'access',
    icon: 'Lock',
    ruleCount: 5,
    enabled: false
  }
])

// 规则数据
const rules = ref([
  {
    id: 1,
    name: '敏感词过滤',
    category: 'content',
    priority: '高',
    condition: '包含敏感词汇',
    action: 'block',
    triggerCount: 45,
    lastTriggered: '2024-01-01 10:30',
    enabled: true,
    description: '过滤用户输入中的敏感词汇'
  },
  {
    id: 2,
    name: '频率限制',
    category: 'conversation',
    priority: '中',
    condition: '1分钟内超过10次请求',
    action: 'warn',
    triggerCount: 12,
    lastTriggered: '2024-01-01 09:15',
    enabled: true,
    description: '限制用户请求频率'
  },
  {
    id: 3,
    name: '个人信息保护',
    category: 'data',
    priority: '高',
    condition: '包含身份证、手机号等个人信息',
    action: 'block',
    triggerCount: 8,
    lastTriggered: '2024-01-01 08:45',
    enabled: true,
    description: '保护用户个人隐私信息'
  },
  {
    id: 4,
    name: 'IP白名单',
    category: 'access',
    priority: '高',
    condition: 'IP不在白名单内',
    action: 'block',
    triggerCount: 156,
    lastTriggered: '2024-01-01 11:20',
    enabled: false,
    description: '只允许白名单IP访问'
  }
])

// 编辑规则表单
const editingRule = reactive({
  id: 0,
  name: '',
  category: '',
  priority: '',
  condition: '',
  action: '',
  description: '',
  enabled: true
})

// 计算属性
const filteredRules = computed(() => {
  let filtered = rules.value

  if (selectedCategory.value) {
    filtered = filtered.filter(rule => rule.category === selectedCategory.value.type)
  }

  if (searchText.value) {
    filtered = filtered.filter(rule =>
      rule.name.toLowerCase().includes(searchText.value.toLowerCase()) ||
      rule.condition.toLowerCase().includes(searchText.value.toLowerCase())
    )
  }

  return filtered
})

// 方法
const getPriorityClass = (priority: string) => {
  const classMap: Record<string, string> = {
    '高': 'high',
    '中': 'medium',
    '低': 'low'
  }
  return classMap[priority] || 'medium'
}

const getCategoryTagType = (category: string) => {
  const typeMap: Record<string, string> = {
    'content': 'danger',
    'conversation': 'primary',
    'data': 'warning',
    'access': 'info'
  }
  return typeMap[category] || 'default'
}

const getCategoryText = (category: string) => {
  const textMap: Record<string, string> = {
    'content': '内容安全',
    'conversation': '对话控制',
    'data': '数据保护',
    'access': '访问控制'
  }
  return textMap[category] || category
}

const getActionTagType = (action: string) => {
  const typeMap: Record<string, string> = {
    'block': 'danger',
    'warn': 'warning',
    'log': 'info',
    'forward': 'success'
  }
  return typeMap[action] || 'default'
}

const getActionText = (action: string) => {
  const textMap: Record<string, string> = {
    'block': '拦截',
    'warn': '警告',
    'log': '记录',
    'forward': '转发'
  }
  return textMap[action] || action
}

const handleAddCategory = () => {
  ElMessage.info('新增分类功能开发中...')
}

const handleSelectCategory = (category: any) => {
  selectedCategory.value = selectedCategory.value?.id === category.id ? null : category
}

const handleAddRule = () => {
  Object.assign(editingRule, {
    id: 0,
    name: '',
    category: '',
    priority: '',
    condition: '',
    action: '',
    description: '',
    enabled: true
  })
  ruleDialogVisible.value = true
}

const handleEditRule = (row: any) => {
  Object.assign(editingRule, { ...row })
  ruleDialogVisible.value = true
}

const handleTestRule = (row: any) => {
  ElMessage.info(`测试规则: ${row.name}`)
}

const handleDeleteRule = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除规则 "${row.name}" 吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    const index = rules.value.findIndex(r => r.id === row.id)
    if (index > -1) {
      rules.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  })
}

const handleRuleStatusChange = (row: any) => {
  const status = row.enabled ? '启用' : '禁用'
  ElMessage.success(`${status}规则成功`)
}

const handleSaveRule = () => {
  if (!editingRule.name || !editingRule.category || !editingRule.condition) {
    ElMessage.warning('请填写完整的规则信息')
    return
  }

  if (editingRule.id) {
    // 编辑
    const index = rules.value.findIndex(r => r.id === editingRule.id)
    if (index > -1) {
      rules.value[index] = { ...editingRule }
    }
    ElMessage.success('规则更新成功')
  } else {
    // 新增
    const newRule = {
      ...editingRule,
      id: Date.now(),
      triggerCount: 0,
      lastTriggered: '-'
    }
    rules.value.push(newRule)
    ElMessage.success('规则创建成功')
  }

  ruleDialogVisible.value = false
}

onMounted(() => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 1000)
})
</script>

<style scoped>
.ai-rule-config {
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

/* 规则统计样式 */
.rule-stats {
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

.stat-icon.triggered {
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

/* 规则分类样式 */
.rule-categories {
  margin-bottom: 20px;
}

.category-card {
  display: flex;
  align-items: center;
  padding: 15px;
  border: 2px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
  margin-bottom: 15px;
}

.category-card:hover {
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.category-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  font-size: 18px;
  color: white;
}

.category-icon.content {
  background: linear-gradient(135deg, #f56c6c 0%, #f45656 100%);
}

.category-icon.conversation {
  background: linear-gradient(135deg, #409eff 0%, #3a8ee6 100%);
}

.category-icon.data {
  background: linear-gradient(135deg, #e6a23c 0%, #cf9236 100%);
}

.category-icon.access {
  background: linear-gradient(135deg, #909399 0%, #82848a 100%);
}

.category-info {
  flex: 1;
}

.category-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.category-count {
  font-size: 12px;
  color: #909399;
}

.category-status {
  margin-left: 10px;
}

/* 规则列表样式 */
.rules-section {
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

.rule-name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rule-priority {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: white;
}

.rule-priority.high {
  background: #f56c6c;
}

.rule-priority.medium {
  background: #e6a23c;
}

.rule-priority.low {
  background: #67c23a;
}

.rule-name {
  font-weight: 500;
  color: #303133;
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
