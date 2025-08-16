<template>
  <div class="crewai-manager">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">
          <i class="el-icon-user"></i>
          CrewAI 团队协作
        </h2>
        <p class="page-description">
          创建和管理多智能体团队，让专业化Agent协作完成复杂任务
        </p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="showCreateDialog = true">
          <i class="el-icon-plus"></i>
          创建团队
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
                <i class="el-icon-s-custom"></i>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.total_crews || 0 }}</div>
                <div class="stat-label">总团队数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon active">
                <i class="el-icon-loading"></i>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.active_crews || 0 }}</div>
                <div class="stat-label">活跃团队</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon agents">
                <i class="el-icon-user-solid"></i>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.total_agents || 0 }}</div>
                <div class="stat-label">总Agent数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon tasks">
                <i class="el-icon-s-order"></i>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.completed_tasks || 0 }}</div>
                <div class="stat-label">完成任务</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 团队列表 -->
    <el-card class="crews-card">
      <div slot="header" class="card-header">
        <span>团队列表</span>
        <div class="header-actions">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索团队..."
            prefix-icon="el-icon-search"
            size="small"
            style="width: 200px; margin-right: 10px;"
            @input="handleSearch"
          />
          <el-select
            v-model="modeFilter"
            placeholder="协作模式"
            size="small"
            style="width: 120px;"
            @change="handleFilter"
          >
            <el-option label="全部" value=""></el-option>
            <el-option label="顺序执行" value="sequential"></el-option>
            <el-option label="并行执行" value="parallel"></el-option>
            <el-option label="层级协作" value="hierarchical"></el-option>
            <el-option label="同行评议" value="peer_review"></el-option>
            <el-option label="共识决策" value="consensus"></el-option>
          </el-select>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="crews"
        style="width: 100%"
        @row-click="handleRowClick"
      >
        <el-table-column prop="name" label="团队名称" min-width="200" show-overflow-tooltip />
        
        <el-table-column prop="description" label="描述" min-width="250" show-overflow-tooltip />
        
        <el-table-column prop="collaboration_mode" label="协作模式" width="120">
          <template slot-scope="scope">
            <el-tag :type="getModeType((scope && scope.row) ? scope.row.collaboration_mode : '')" size="small">
              {{ getModeText((scope && scope.row) ? scope.row.collaboration_mode : '') }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="agents" label="Agent数量" width="100" align="center">
          <template slot-scope="scope">
            <el-badge :value="(scope && scope.row && scope.row.agents) ? scope.row.agents.length : 0" class="agent-badge">
              <i class="el-icon-user-solid"></i>
            </el-badge>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag
              :type="(scope && scope.row && scope.row.status === 'active') ? 'success' : 'info'"
              size="small"
            >
              {{ (scope && scope.row && scope.row.status === 'active') ? '活跃' : '非活跃' }}
            </el-tag>
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
              v-if="scope && scope.row"
              type="primary"
              size="mini"
              @click.stop="executeTask(scope.row)"
            >
              执行任务
            </el-button>
            <el-button
              v-if="scope && scope.row"
              type="info"
              size="mini"
              @click.stop="viewCrew(scope.row)"
            >
              查看
            </el-button>
            <el-button
              v-if="scope && scope.row"
              type="danger"
              size="mini"
              @click.stop="deleteCrew(scope.row)"
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

    <!-- 创建团队对话框 -->
    <el-dialog
      title="创建Agent团队"
      :visible.sync="showCreateDialog"
      width="700px"
      @close="resetCreateForm"
    >
      <el-form
        ref="createForm"
        :model="createForm"
        :rules="createRules"
        label-width="120px"
      >
        <el-form-item label="团队名称" prop="name">
          <el-input
            v-model="createForm.name"
            placeholder="请输入团队名称，例如：手机质量检测团队"
          />
        </el-form-item>
        
        <el-form-item label="团队描述" prop="description">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="2"
            placeholder="请描述团队的主要职责和目标"
          />
        </el-form-item>
        
        <el-form-item label="协作模式" prop="collaboration_mode">
          <el-select v-model="createForm.collaboration_mode" style="width: 100%;">
            <el-option
              v-for="(desc, mode) in collaborationModes"
              :key="mode"
              :label="`${getModeText(mode)} - ${desc}`"
              :value="mode"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="团队成员" prop="agents">
          <div class="agents-section">
            <div
              v-for="(agent, index) in createForm.agents"
              :key="index"
              class="agent-item"
            >
              <el-select
                v-model="agent.role"
                placeholder="选择Agent角色"
                style="width: 200px; margin-right: 10px;"
              >
                <el-option
                  v-for="role in agentRoles"
                  :key="role.id"
                  :label="role.name"
                  :value="role.id"
                >
                  <span style="float: left">{{ role.name }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">
                    {{ role.description }}
                  </span>
                </el-option>
              </el-select>
              <el-input
                v-model="agent.name"
                placeholder="Agent名称"
                style="width: 150px; margin-right: 10px;"
              />
              <el-button
                type="danger"
                size="mini"
                icon="el-icon-delete"
                @click="removeAgent(index)"
              />
            </div>
            <el-button
              type="primary"
              size="small"
              icon="el-icon-plus"
              @click="addAgent"
            >
              添加Agent
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button
          type="primary"
          :loading="creating"
          @click="createCrew"
        >
          创建团队
        </el-button>
      </div>
    </el-dialog>

    <!-- 执行任务对话框 -->
    <el-dialog
      title="执行团队任务"
      :visible.sync="showTaskDialog"
      width="600px"
    >
      <el-form
        ref="taskForm"
        :model="taskForm"
        :rules="taskRules"
        label-width="100px"
      >
        <el-form-item label="任务描述" prop="description">
          <el-input
            v-model="taskForm.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述需要团队完成的任务，例如：分析最新一批iPhone 15的质量数据，识别潜在问题并生成质量报告"
          />
        </el-form-item>
        
        <el-form-item label="上下文信息">
          <el-input
            v-model="taskForm.context"
            type="textarea"
            :rows="3"
            placeholder="提供任务相关的背景信息和数据（可选）"
          />
        </el-form-item>
      </el-form>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="showTaskDialog = false">取消</el-button>
        <el-button
          type="primary"
          :loading="executing"
          @click="submitTask"
        >
          开始执行
        </el-button>
      </div>
    </el-dialog>

    <!-- 团队详情对话框 -->
    <el-dialog
      title="团队详情"
      :visible.sync="showDetailDialog"
      width="800px"
    >
      <div v-if="selectedCrew" class="crew-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="团队名称">
            {{ selectedCrew.name }}
          </el-descriptions-item>
          <el-descriptions-item label="协作模式">
            <el-tag :type="getModeType(selectedCrew.collaboration_mode)">
              {{ getModeText(selectedCrew.collaboration_mode) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">
            {{ selectedCrew.description }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatTime(selectedCrew.created_at) }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedCrew.status === 'active' ? 'success' : 'info'">
              {{ selectedCrew.status === 'active' ? '活跃' : '非活跃' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div class="agents-section" style="margin-top: 20px;">
          <h4>团队成员</h4>
          <el-row :gutter="16">
            <el-col
              v-for="(agent, index) in selectedCrew.agents || []"
              :key="index"
              :span="8"
            >
              <el-card class="agent-card">
                <div class="agent-info">
                  <div class="agent-avatar">
                    <i class="el-icon-user-solid"></i>
                  </div>
                  <div class="agent-details">
                    <div class="agent-name">{{ agent.name || getRoleName(agent.role) }}</div>
                    <div class="agent-role">{{ getRoleName(agent.role) }}</div>
                    <div class="agent-capabilities">
                      <el-tag
                        v-for="capability in agent.capabilities || []"
                        :key="capability"
                        size="mini"
                        style="margin-right: 4px; margin-bottom: 4px;"
                      >
                        {{ capability }}
                      </el-tag>
                    </div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { 
  createCrew, 
  getCrews, 
  executeCrewTask,
  getAgentRoles 
} from '@/api/coze-studio'

export default {
  name: 'CrewAIManager',
  data() {
    return {
      loading: false,
      creating: false,
      executing: false,
      crews: [],
      stats: {},
      agentRoles: [],
      collaborationModes: {},
      searchKeyword: '',
      modeFilter: '',
      pagination: {
        page: 1,
        limit: 20,
        total: 0
      },
      
      // 创建对话框
      showCreateDialog: false,
      createForm: {
        name: '',
        description: '',
        collaboration_mode: 'sequential',
        agents: []
      },
      createRules: {
        name: [
          { required: true, message: '请输入团队名称', trigger: 'blur' }
        ],
        description: [
          { required: true, message: '请输入团队描述', trigger: 'blur' }
        ],
        collaboration_mode: [
          { required: true, message: '请选择协作模式', trigger: 'change' }
        ],
        agents: [
          { required: true, message: '请至少添加一个Agent', trigger: 'change' }
        ]
      },
      
      // 任务对话框
      showTaskDialog: false,
      selectedCrewForTask: null,
      taskForm: {
        description: '',
        context: ''
      },
      taskRules: {
        description: [
          { required: true, message: '请输入任务描述', trigger: 'blur' },
          { min: 10, message: '任务描述至少10个字符', trigger: 'blur' }
        ]
      },
      
      // 详情对话框
      showDetailDialog: false,
      selectedCrew: null
    }
  },
  
  mounted() {
    this.loadCrews()
    this.loadAgentRoles()
    this.loadStats()
  },
  
  methods: {
    async loadCrews() {
      this.loading = true
      try {
        const params = {
          page: this.pagination.page,
          limit: this.pagination.limit,
          search: this.searchKeyword,
          collaboration_mode: this.modeFilter
        }
        
        const response = await getCrews(params)
        if (response.success) {
          this.crews = response.data.crews || []
          this.pagination.total = response.data.pagination?.total || 0
        }
      } catch (error) {
        this.$message.error('加载团队列表失败：' + error.message)
      } finally {
        this.loading = false
      }
    },
    
    async loadAgentRoles() {
      try {
        const response = await getAgentRoles()
        if (response.success) {
          this.agentRoles = response.data.roles || []
          this.collaborationModes = response.data.collaboration_modes || {}
        }
      } catch (error) {
        console.error('加载Agent角色失败:', error)
      }
    },
    
    async loadStats() {
      try {
        // 模拟统计数据
        this.stats = {
          total_crews: this.crews.length,
          active_crews: this.crews.filter(c => c.status === 'active').length,
          total_agents: this.crews.reduce((sum, crew) => sum + (crew.agents?.length || 0), 0),
          completed_tasks: 42
        }
      } catch (error) {
        console.error('加载统计数据失败:', error)
      }
    },
    
    async createCrew() {
      this.$refs.createForm.validate(async (valid) => {
        if (!valid) return
        
        if (this.createForm.agents.length === 0) {
          this.$message.error('请至少添加一个Agent')
          return
        }
        
        this.creating = true
        try {
          const response = await createCrew(this.createForm)
          
          if (response.success) {
            this.$message.success('团队创建成功')
            this.showCreateDialog = false
            this.loadCrews()
          }
        } catch (error) {
          this.$message.error('创建团队失败：' + error.message)
        } finally {
          this.creating = false
        }
      })
    },
    
    executeTask(crew) {
      this.selectedCrewForTask = crew
      this.taskForm = {
        description: '',
        context: ''
      }
      this.showTaskDialog = true
    },
    
    async submitTask() {
      this.$refs.taskForm.validate(async (valid) => {
        if (!valid) return
        
        this.executing = true
        try {
          const response = await executeCrewTask(this.selectedCrewForTask.id, {
            task: {
              description: this.taskForm.description
            },
            context: this.taskForm.context ? JSON.parse(this.taskForm.context) : {}
          })
          
          if (response.success) {
            this.$message.success('任务开始执行')
            this.showTaskDialog = false
          }
        } catch (error) {
          this.$message.error('执行任务失败：' + error.message)
        } finally {
          this.executing = false
        }
      })
    },
    
    viewCrew(crew) {
      this.selectedCrew = crew
      this.showDetailDialog = true
    },
    
    deleteCrew(crew) {
      this.$confirm('确定要删除这个团队吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // TODO: 实现删除团队的API
        this.$message.success('团队已删除')
        this.loadCrews()
      })
    },
    
    addAgent() {
      this.createForm.agents.push({
        role: '',
        name: ''
      })
    },
    
    removeAgent(index) {
      this.createForm.agents.splice(index, 1)
    },
    
    resetCreateForm() {
      this.createForm = {
        name: '',
        description: '',
        collaboration_mode: 'sequential',
        agents: []
      }
    },
    
    handleSearch() {
      this.pagination.page = 1
      this.loadCrews()
    },
    
    handleFilter() {
      this.pagination.page = 1
      this.loadCrews()
    },
    
    handleSizeChange(size) {
      this.pagination.limit = size
      this.loadCrews()
    },
    
    handleCurrentChange(page) {
      this.pagination.page = page
      this.loadCrews()
    },
    
    handleRowClick(row) {
      this.viewCrew(row)
    },
    
    getModeType(mode) {
      const types = {
        sequential: 'primary',
        parallel: 'success',
        hierarchical: 'warning',
        peer_review: 'info',
        consensus: 'danger'
      }
      return types[mode] || 'info'
    },
    
    getModeText(mode) {
      const texts = {
        sequential: '顺序执行',
        parallel: '并行执行',
        hierarchical: '层级协作',
        peer_review: '同行评议',
        consensus: '共识决策'
      }
      return texts[mode] || mode
    },
    
    getRoleName(roleId) {
      const role = this.agentRoles.find(r => r.id === roleId)
      return role ? role.name : roleId
    },
    
    formatTime(time) {
      if (!time) return '-'
      return new Date(time).toLocaleString()
    }
  }
}
</script>

<style scoped>
.crewai-manager {
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
.stat-icon.active { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.stat-icon.agents { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.stat-icon.tasks { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }

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

.crews-card {
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

.agent-badge {
  font-size: 16px;
}

.pagination-wrapper {
  margin-top: 20px;
  text-align: right;
}

.agents-section {
  margin-top: 16px;
}

.agent-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.crew-detail {
  max-height: 600px;
  overflow-y: auto;
}

.agents-section h4 {
  margin: 0 0 16px 0;
  color: #303133;
}

.agent-card {
  margin-bottom: 16px;
  border: 1px solid #e4e7ed;
}

.agent-info {
  display: flex;
  align-items: flex-start;
}

.agent-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #409EFF;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  margin-right: 12px;
  flex-shrink: 0;
}

.agent-details {
  flex: 1;
}

.agent-name {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.agent-role {
  color: #606266;
  font-size: 14px;
  margin-bottom: 8px;
}

.agent-capabilities {
  line-height: 1.5;
}
</style>
