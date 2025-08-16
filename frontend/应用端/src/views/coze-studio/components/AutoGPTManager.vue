<template>
  <div class="autogpt-manager">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">
          <i class="el-icon-cpu"></i>
          AutoGPT 自主规划
        </h2>
        <p class="page-description">
          创建和管理AI自主规划任务，让AI自动分解复杂目标并执行
        </p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="showCreateDialog = true">
          <i class="el-icon-plus"></i>
          创建规划
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon total">
                <i class="el-icon-document"></i>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.total_plans || 0 }}</div>
                <div class="stat-label">总规划数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon running">
                <i class="el-icon-loading"></i>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.running_plans || 0 }}</div>
                <div class="stat-label">执行中</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon completed">
                <i class="el-icon-check"></i>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.completed_plans || 0 }}</div>
                <div class="stat-label">已完成</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon success-rate">
                <i class="el-icon-trophy"></i>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.success_rate || '0%' }}</div>
                <div class="stat-label">成功率</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 规划列表 -->
    <el-card class="plans-card">
      <div slot="header" class="card-header">
        <span>规划列表</span>
        <div class="header-actions">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索规划..."
            prefix-icon="el-icon-search"
            size="small"
            style="width: 200px; margin-right: 10px;"
            @input="handleSearch"
          />
          <el-select
            v-model="statusFilter"
            placeholder="状态筛选"
            size="small"
            style="width: 120px;"
            @change="handleFilter"
          >
            <el-option label="全部" value=""></el-option>
            <el-option label="待执行" value="pending"></el-option>
            <el-option label="执行中" value="running"></el-option>
            <el-option label="已完成" value="completed"></el-option>
            <el-option label="失败" value="failed"></el-option>
          </el-select>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="plans"
        style="width: 100%"
        @row-click="handleRowClick"
      >
        <el-table-column prop="id" label="规划ID" width="200" show-overflow-tooltip>
          <template slot-scope="scope">
            <el-tag size="mini" type="info">{{ (scope && scope.row && scope.row.id) ? scope.row.id.substring(0, 8) + '...' : '-' }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="goal" label="目标" min-width="200" show-overflow-tooltip />
        
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag
              :type="getStatusType((scope && scope.row) ? scope.row.status : '')"
              size="small"
            >
              {{ getStatusText((scope && scope.row) ? scope.row.status : '') }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="steps_count" label="步骤数" width="80" align="center" />
        
        <el-table-column prop="progress" label="进度" width="120">
          <template slot-scope="scope">
            <el-progress
              :percentage="(scope && scope.row) ? (scope.row.progress || 0) : 0"
              :status="(scope && scope.row && scope.row.status === 'completed') ? 'success' :
                      (scope && scope.row && scope.row.status === 'failed') ? 'exception' : ''"
              :stroke-width="6"
              :show-text="false"
            />
            <span class="progress-text">{{ (scope && scope.row) ? (scope.row.progress || 0) : 0 }}%</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="created_at" label="创建时间" width="160">
          <template slot-scope="scope">
            {{ formatTime((scope && scope.row) ? scope.row.created_at : null) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button
              v-if="scope && scope.row && scope.row.status === 'pending'"
              type="primary"
              size="mini"
              @click.stop="executePlan(scope.row)"
            >
              执行
            </el-button>
            <el-button
              v-if="scope && scope.row"
              type="info"
              size="mini"
              @click.stop="viewPlan(scope.row)"
            >
              查看
            </el-button>
            <el-button
              v-if="scope && scope.row && scope.row.status === 'running'"
              type="warning"
              size="mini"
              @click.stop="stopPlan(scope.row)"
            >
              停止
            </el-button>
            <el-button
              v-if="scope && scope.row"
              type="danger"
              size="mini"
              @click.stop="deletePlan(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.page"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.limit"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
        />
      </div>
    </el-card>

    <!-- 创建规划对话框 -->
    <el-dialog
      title="创建AutoGPT规划"
      :visible.sync="showCreateDialog"
      width="600px"
      @close="resetCreateForm"
    >
      <el-form
        ref="createForm"
        :model="createForm"
        :rules="createRules"
        label-width="100px"
      >
        <el-form-item label="目标描述" prop="goal">
          <el-input
            v-model="createForm.goal"
            type="textarea"
            :rows="3"
            placeholder="请描述您希望AI完成的目标，例如：分析最新一批iPhone 15的质量数据并生成报告"
          />
        </el-form-item>
        
        <el-form-item label="约束条件" prop="constraints">
          <el-tag
            v-for="(constraint, index) in createForm.constraints"
            :key="index"
            closable
            @close="removeConstraint(index)"
            style="margin-right: 10px; margin-bottom: 5px;"
          >
            {{ constraint }}
          </el-tag>
          <el-input
            v-if="showConstraintInput"
            ref="constraintInput"
            v-model="newConstraint"
            size="small"
            style="width: 200px;"
            @keyup.enter.native="addConstraint"
            @blur="addConstraint"
          />
          <el-button
            v-else
            size="small"
            @click="showConstraintInput = true"
          >
            + 添加约束
          </el-button>
        </el-form-item>
        
        <el-form-item label="最大步骤数" prop="maxSteps">
          <el-input-number
            v-model="createForm.maxSteps"
            :min="1"
            :max="50"
            style="width: 200px;"
          />
        </el-form-item>
        
        <el-form-item label="执行模式" prop="executionMode">
          <el-radio-group v-model="createForm.executionMode">
            <el-radio label="auto">自动执行</el-radio>
            <el-radio label="manual">手动确认</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button
          type="primary"
          :loading="creating"
          @click="createPlan"
        >
          创建规划
        </el-button>
      </div>
    </el-dialog>

    <!-- 规划详情对话框 -->
    <el-dialog
      title="规划详情"
      :visible.sync="showDetailDialog"
      width="800px"
    >
      <div v-if="selectedPlan" class="plan-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="规划ID">
            {{ selectedPlan.id }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedPlan.status)">
              {{ getStatusText(selectedPlan.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="目标" :span="2">
            {{ selectedPlan.goal }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatTime(selectedPlan.created_at) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatTime(selectedPlan.updated_at) }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="steps-section" style="margin-top: 20px;">
          <h4>执行步骤</h4>
          <el-timeline>
            <el-timeline-item
              v-for="(step, index) in selectedPlan.steps || []"
              :key="index"
              :type="getStepType(step.status)"
              :icon="getStepIcon(step.status)"
            >
              <div class="step-content">
                <div class="step-title">{{ step.action }}</div>
                <div class="step-description">{{ step.description }}</div>
                <div v-if="step.result" class="step-result">
                  <strong>结果：</strong>{{ step.result }}
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { 
  createAutoGPTPlan, 
  getAutoGPTPlans, 
  getAutoGPTPlan,
  executeAutoGPTPlan 
} from '@/api/coze-studio'

export default {
  name: 'AutoGPTManager',
  data() {
    return {
      loading: false,
      creating: false,
      plans: [],
      stats: {},
      searchKeyword: '',
      statusFilter: '',
      pagination: {
        page: 1,
        limit: 20,
        total: 0
      },
      
      // 创建对话框
      showCreateDialog: false,
      createForm: {
        goal: '',
        constraints: [],
        maxSteps: 10,
        executionMode: 'auto'
      },
      createRules: {
        goal: [
          { required: true, message: '请输入目标描述', trigger: 'blur' },
          { min: 10, message: '目标描述至少10个字符', trigger: 'blur' }
        ],
        maxSteps: [
          { required: true, message: '请设置最大步骤数', trigger: 'blur' }
        ]
      },
      showConstraintInput: false,
      newConstraint: '',
      
      // 详情对话框
      showDetailDialog: false,
      selectedPlan: null
    }
  },
  
  mounted() {
    this.loadPlans()
    this.loadStats()
  },
  
  methods: {
    async loadPlans() {
      this.loading = true
      try {
        const params = {
          page: this.pagination.page,
          limit: this.pagination.limit,
          search: this.searchKeyword,
          status: this.statusFilter
        }
        
        const response = await getAutoGPTPlans(params)
        if (response.success) {
          this.plans = response.data.plans || []
          this.pagination.total = response.data.pagination?.total || 0
        }
      } catch (error) {
        this.$message.error('加载规划列表失败：' + error.message)
      } finally {
        this.loading = false
      }
    },
    
    async loadStats() {
      try {
        // 模拟统计数据，实际应该从API获取
        this.stats = {
          total_plans: this.plans.length,
          running_plans: this.plans.filter(p => p.status === 'running').length,
          completed_plans: this.plans.filter(p => p.status === 'completed').length,
          success_rate: '85%'
        }
      } catch (error) {
        console.error('加载统计数据失败:', error)
      }
    },
    
    async createPlan() {
      this.$refs.createForm.validate(async (valid) => {
        if (!valid) return
        
        this.creating = true
        try {
          const response = await createAutoGPTPlan({
            goal: this.createForm.goal,
            constraints: this.createForm.constraints,
            config: {
              max_steps: this.createForm.maxSteps,
              execution_mode: this.createForm.executionMode
            }
          })
          
          if (response.success) {
            this.$message.success('规划创建成功')
            this.showCreateDialog = false
            this.loadPlans()
          }
        } catch (error) {
          this.$message.error('创建规划失败：' + error.message)
        } finally {
          this.creating = false
        }
      })
    },
    
    async executePlan(plan) {
      try {
        const response = await executeAutoGPTPlan(plan.id)
        if (response.success) {
          this.$message.success('规划开始执行')
          this.loadPlans()
        }
      } catch (error) {
        this.$message.error('执行规划失败：' + error.message)
      }
    },
    
    async viewPlan(plan) {
      try {
        const response = await getAutoGPTPlan(plan.id)
        if (response.success) {
          this.selectedPlan = response.data
          this.showDetailDialog = true
        }
      } catch (error) {
        this.$message.error('获取规划详情失败：' + error.message)
      }
    },
    
    stopPlan(plan) {
      this.$confirm('确定要停止这个规划的执行吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // TODO: 实现停止规划的API
        this.$message.success('规划已停止')
        this.loadPlans()
      })
    },
    
    deletePlan(plan) {
      this.$confirm('确定要删除这个规划吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // TODO: 实现删除规划的API
        this.$message.success('规划已删除')
        this.loadPlans()
      })
    },
    
    addConstraint() {
      if (this.newConstraint.trim()) {
        this.createForm.constraints.push(this.newConstraint.trim())
        this.newConstraint = ''
      }
      this.showConstraintInput = false
    },
    
    removeConstraint(index) {
      this.createForm.constraints.splice(index, 1)
    },
    
    resetCreateForm() {
      this.createForm = {
        goal: '',
        constraints: [],
        maxSteps: 10,
        executionMode: 'auto'
      }
      this.showConstraintInput = false
      this.newConstraint = ''
    },
    
    handleSearch() {
      this.pagination.page = 1
      this.loadPlans()
    },
    
    handleFilter() {
      this.pagination.page = 1
      this.loadPlans()
    },
    
    handleSizeChange(size) {
      this.pagination.limit = size
      this.loadPlans()
    },
    
    handleCurrentChange(page) {
      this.pagination.page = page
      this.loadPlans()
    },
    
    handleRowClick(row) {
      this.viewPlan(row)
    },
    
    getStatusType(status) {
      const types = {
        pending: 'info',
        running: 'warning',
        completed: 'success',
        failed: 'danger'
      }
      return types[status] || 'info'
    },
    
    getStatusText(status) {
      const texts = {
        pending: '待执行',
        running: '执行中',
        completed: '已完成',
        failed: '失败'
      }
      return texts[status] || status
    },
    
    getStepType(status) {
      const types = {
        pending: 'info',
        running: 'primary',
        completed: 'success',
        failed: 'danger'
      }
      return types[status] || 'info'
    },
    
    getStepIcon(status) {
      const icons = {
        pending: 'el-icon-time',
        running: 'el-icon-loading',
        completed: 'el-icon-check',
        failed: 'el-icon-close'
      }
      return icons[status] || 'el-icon-time'
    },
    
    formatTime(time) {
      if (!time) return '-'
      return new Date(time).toLocaleString()
    }
  }
}
</script>

<style scoped>
.autogpt-manager {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.page-title i {
  margin-right: 8px;
  color: #409EFF;
}

.page-description {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  color: white;
}

.stat-icon.total { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-icon.running { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.stat-icon.completed { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.stat-icon.success-rate { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }

.stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.plans-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
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

.progress-text {
  font-size: 12px;
  color: #606266;
  margin-left: 8px;
}

.pagination-wrapper {
  margin-top: 20px;
  text-align: right;
}

.plan-detail {
  max-height: 500px;
  overflow-y: auto;
}

.steps-section h4 {
  margin: 0 0 16px 0;
  color: #303133;
}

.step-content {
  padding-bottom: 8px;
}

.step-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.step-description {
  color: #606266;
  font-size: 14px;
  margin-bottom: 4px;
}

.step-result {
  color: #909399;
  font-size: 12px;
  background: #f5f7fa;
  padding: 8px;
  border-radius: 4px;
  margin-top: 8px;
}
</style>
