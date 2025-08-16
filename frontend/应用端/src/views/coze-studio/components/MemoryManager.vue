<template>
  <div class="memory-manager">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">
          <i class="el-icon-brain"></i>
          Memory 记忆管理
        </h2>
        <p class="page-description">
          管理Agent的记忆系统，包括对话记忆、实体记忆和摘要记忆
        </p>
      </div>
      <div class="header-right">
        <el-select
          v-model="selectedAgentId"
          placeholder="选择Agent"
          style="width: 200px; margin-right: 10px;"
          @change="handleAgentChange"
        >
          <el-option
            v-for="agent in agents"
            :key="agent.id"
            :label="agent.name"
            :value="agent.id"
          />
        </el-select>
        <el-button
          type="primary"
          :disabled="!selectedAgentId"
          @click="createMemorySystem"
        >
          <i class="el-icon-plus"></i>
          创建记忆系统
        </el-button>
      </div>
    </div>

    <!-- 记忆统计 -->
    <div v-if="selectedAgentId" class="memory-stats">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon buffer">
                <i class="el-icon-chat-line-round"></i>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ memoryStats.buffer_memories || 0 }}</div>
                <div class="stat-label">缓冲记忆</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon entities">
                <i class="el-icon-collection-tag"></i>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ memoryStats.entities || 0 }}</div>
                <div class="stat-label">实体记忆</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon summaries">
                <i class="el-icon-document"></i>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ memoryStats.summaries || 0 }}</div>
                <div class="stat-label">摘要记忆</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon cache">
                <i class="el-icon-cpu"></i>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ memoryStats.cache_status === 'cached' ? '已缓存' : '未缓存' }}</div>
                <div class="stat-label">缓存状态</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 记忆管理标签页 -->
    <div v-if="selectedAgentId" class="memory-tabs">
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <!-- 对话记忆 -->
        <el-tab-pane label="对话记忆" name="conversations">
          <el-card>
            <div slot="header" class="card-header">
              <span>对话记忆</span>
              <div class="header-actions">
                <el-button
                  type="primary"
                  size="small"
                  @click="showAddMemoryDialog = true"
                >
                  添加记忆
                </el-button>
                <el-button
                  type="warning"
                  size="small"
                  @click="cleanupMemory"
                >
                  清理过期记忆
                </el-button>
              </div>
            </div>

            <div class="conversation-memories">
              <div
                v-for="(memory, index) in conversationMemories"
                :key="index"
                class="memory-item"
              >
                <div class="memory-header">
                  <span class="memory-time">{{ formatTime(memory.timestamp) }}</span>
                  <el-tag
                    :type="getImportanceType(memory.importance)"
                    size="mini"
                  >
                    重要性: {{ (memory.importance * 100).toFixed(0) }}%
                  </el-tag>
                </div>
                <div class="memory-content">
                  <div class="user-message">
                    <strong>用户:</strong> {{ memory.user_message }}
                  </div>
                  <div class="agent-response">
                    <strong>Agent:</strong> {{ memory.agent_response }}
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-tab-pane>

        <!-- 实体记忆 -->
        <el-tab-pane label="实体记忆" name="entities">
          <el-card>
            <div slot="header" class="card-header">
              <span>实体记忆</span>
              <div class="header-actions">
                <el-input
                  v-model="entitySearch"
                  placeholder="搜索实体..."
                  prefix-icon="el-icon-search"
                  size="small"
                  style="width: 200px;"
                  @input="searchEntities"
                />
              </div>
            </div>

            <div class="entity-grid">
              <el-row :gutter="16">
                <el-col
                  v-for="(entity, index) in filteredEntities"
                  :key="index"
                  :span="8"
                >
                  <el-card class="entity-card">
                    <div class="entity-info">
                      <div class="entity-header">
                        <span class="entity-name">{{ entity.name }}</span>
                        <el-tag :type="getEntityTypeColor(entity.type)" size="mini">
                          {{ getEntityTypeText(entity.type) }}
                        </el-tag>
                      </div>
                      <div class="entity-description">{{ entity.description }}</div>
                      <div class="entity-stats">
                        <span class="mention-count">提及次数: {{ entity.mention_count }}</span>
                        <span class="last-mentioned">最后提及: {{ formatTime(entity.last_mentioned) }}</span>
                      </div>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </el-card>
        </el-tab-pane>

        <!-- 记忆检索 -->
        <el-tab-pane label="记忆检索" name="retrieval">
          <el-card>
            <div slot="header">
              <span>记忆检索</span>
            </div>

            <div class="retrieval-section">
              <el-form :model="retrievalForm" label-width="100px">
                <el-form-item label="查询内容">
                  <el-input
                    v-model="retrievalForm.query"
                    type="textarea"
                    :rows="3"
                    placeholder="输入要检索的内容，例如：iPhone 15质量问题"
                  />
                </el-form-item>
                <el-form-item label="检索选项">
                  <el-checkbox-group v-model="retrievalForm.options">
                    <el-checkbox label="includeBuffer">包含缓冲记忆</el-checkbox>
                    <el-checkbox label="includeSummary">包含摘要记忆</el-checkbox>
                    <el-checkbox label="includeEntities">包含实体记忆</el-checkbox>
                  </el-checkbox-group>
                </el-form-item>
                <el-form-item label="结果数量">
                  <el-input-number
                    v-model="retrievalForm.maxResults"
                    :min="1"
                    :max="20"
                    style="width: 120px;"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button
                    type="primary"
                    :loading="retrieving"
                    @click="retrieveMemory"
                  >
                    检索记忆
                  </el-button>
                </el-form-item>
              </el-form>

              <div v-if="retrievalResults" class="retrieval-results">
                <h4>检索结果</h4>
                
                <div v-if="retrievalResults.buffer && retrievalResults.buffer.length > 0" class="result-section">
                  <h5>缓冲记忆 ({{ retrievalResults.buffer.length }}条)</h5>
                  <div
                    v-for="(memory, index) in retrievalResults.buffer"
                    :key="index"
                    class="result-item"
                  >
                    <div class="result-content">
                      <div><strong>用户:</strong> {{ memory.user_message }}</div>
                      <div><strong>Agent:</strong> {{ memory.agent_response }}</div>
                      <div class="result-meta">
                        时间: {{ formatTime(memory.timestamp) }} | 
                        重要性: {{ (memory.importance * 100).toFixed(0) }}%
                      </div>
                    </div>
                  </div>
                </div>

                <div v-if="retrievalResults.entities && retrievalResults.entities.length > 0" class="result-section">
                  <h5>相关实体 ({{ retrievalResults.entities.length }}个)</h5>
                  <div class="entity-tags">
                    <el-tag
                      v-for="(entity, index) in retrievalResults.entities"
                      :key="index"
                      :type="getEntityTypeColor(entity.type)"
                      style="margin-right: 8px; margin-bottom: 8px;"
                    >
                      {{ entity.name }} ({{ entity.mention_count }}次)
                    </el-tag>
                  </div>
                </div>

                <div v-if="retrievalResults.summary" class="result-section">
                  <h5>摘要记忆</h5>
                  <div class="summary-content">
                    {{ retrievalResults.summary.content }}
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 选择Agent提示 -->
    <div v-else class="empty-state">
      <el-empty description="请先选择一个Agent来管理其记忆系统">
        <el-button type="primary" @click="loadAgents">刷新Agent列表</el-button>
      </el-empty>
    </div>

    <!-- 添加记忆对话框 -->
    <el-dialog
      title="添加对话记忆"
      :visible.sync="showAddMemoryDialog"
      width="600px"
    >
      <el-form
        ref="memoryForm"
        :model="memoryForm"
        :rules="memoryRules"
        label-width="100px"
      >
        <el-form-item label="用户消息" prop="message">
          <el-input
            v-model="memoryForm.message"
            type="textarea"
            :rows="3"
            placeholder="输入用户消息"
          />
        </el-form-item>
        <el-form-item label="Agent回复" prop="response">
          <el-input
            v-model="memoryForm.response"
            type="textarea"
            :rows="3"
            placeholder="输入Agent回复"
          />
        </el-form-item>
        <el-form-item label="对话ID">
          <el-input
            v-model="memoryForm.conversationId"
            placeholder="可选，留空将自动生成"
          />
        </el-form-item>
      </el-form>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="showAddMemoryDialog = false">取消</el-button>
        <el-button
          type="primary"
          :loading="addingMemory"
          @click="addMemory"
        >
          添加记忆
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { 
  getAgents,
  createAgentMemory,
  addConversationMemory,
  retrieveMemory,
  getMemoryStats,
  cleanupMemory
} from '@/api/coze-studio'

export default {
  name: 'MemoryManager',
  data() {
    return {
      loading: false,
      addingMemory: false,
      retrieving: false,
      agents: [],
      selectedAgentId: '',
      memoryStats: {},
      activeTab: 'conversations',
      
      // 对话记忆
      conversationMemories: [],
      
      // 实体记忆
      entities: [],
      filteredEntities: [],
      entitySearch: '',
      
      // 记忆检索
      retrievalForm: {
        query: '',
        options: ['includeBuffer', 'includeEntities'],
        maxResults: 5
      },
      retrievalResults: null,
      
      // 添加记忆对话框
      showAddMemoryDialog: false,
      memoryForm: {
        message: '',
        response: '',
        conversationId: ''
      },
      memoryRules: {
        message: [
          { required: true, message: '请输入用户消息', trigger: 'blur' }
        ],
        response: [
          { required: true, message: '请输入Agent回复', trigger: 'blur' }
        ]
      }
    }
  },
  
  mounted() {
    this.loadAgents()
  },
  
  methods: {
    async loadAgents() {
      try {
        const response = await getAgents({ limit: 100 })
        if (response.success) {
          this.agents = response.data.agents || []
        }
      } catch (error) {
        this.$message.error('加载Agent列表失败：' + error.message)
      }
    },
    
    async handleAgentChange() {
      if (this.selectedAgentId) {
        await this.loadMemoryStats()
        await this.loadMemoryData()
      }
    },
    
    async loadMemoryStats() {
      try {
        const response = await getMemoryStats(this.selectedAgentId)
        if (response.success) {
          this.memoryStats = response.data
        }
      } catch (error) {
        console.error('加载记忆统计失败:', error)
      }
    },
    
    async loadMemoryData() {
      // 模拟加载记忆数据
      this.conversationMemories = [
        {
          user_message: "你好，我想了解iPhone 15的质量检测流程",
          agent_response: "您好！iPhone 15的质量检测流程包括外观检查、功能测试、性能评估等多个环节。",
          timestamp: new Date().toISOString(),
          importance: 0.7
        },
        {
          user_message: "iPhone 15有哪些常见的质量问题？",
          agent_response: "根据质量检测数据，iPhone 15常见的质量问题包括：1. 屏幕显示异常 2. 电池续航不足 3. 摄像头对焦问题等。",
          timestamp: new Date(Date.now() - 3600000).toISOString(),
          importance: 0.9
        }
      ]
      
      this.entities = [
        {
          name: "iPhone 15",
          type: "products",
          description: "苹果公司的智能手机产品",
          mention_count: 5,
          last_mentioned: new Date().toISOString()
        },
        {
          name: "质量检测",
          type: "concepts",
          description: "产品质量检测流程",
          mention_count: 3,
          last_mentioned: new Date(Date.now() - 1800000).toISOString()
        }
      ]
      
      this.filteredEntities = [...this.entities]
    },
    
    async createMemorySystem() {
      try {
        const response = await createAgentMemory(this.selectedAgentId, {
          types: ['buffer', 'summary', 'entity'],
          maxBufferSize: 10,
          summaryThreshold: 20,
          entityExtractionEnabled: true
        })
        
        if (response.success) {
          this.$message.success('记忆系统创建成功')
          this.loadMemoryStats()
        }
      } catch (error) {
        this.$message.error('创建记忆系统失败：' + error.message)
      }
    },
    
    async addMemory() {
      this.$refs.memoryForm.validate(async (valid) => {
        if (!valid) return
        
        this.addingMemory = true
        try {
          const response = await addConversationMemory(this.selectedAgentId, {
            conversationId: this.memoryForm.conversationId || `conv_${Date.now()}`,
            message: this.memoryForm.message,
            response: this.memoryForm.response
          })
          
          if (response.success) {
            this.$message.success('记忆添加成功')
            this.showAddMemoryDialog = false
            this.loadMemoryData()
            this.loadMemoryStats()
          }
        } catch (error) {
          this.$message.error('添加记忆失败：' + error.message)
        } finally {
          this.addingMemory = false
        }
      })
    },
    
    async retrieveMemory() {
      if (!this.retrievalForm.query.trim()) {
        this.$message.warning('请输入查询内容')
        return
      }
      
      this.retrieving = true
      try {
        const options = {}
        this.retrievalForm.options.forEach(option => {
          options[option] = true
        })
        options.maxResults = this.retrievalForm.maxResults
        
        const response = await retrieveMemory(this.selectedAgentId, {
          query: this.retrievalForm.query,
          options: options
        })
        
        if (response.success) {
          this.retrievalResults = response.data
          this.$message.success('记忆检索完成')
        }
      } catch (error) {
        this.$message.error('记忆检索失败：' + error.message)
      } finally {
        this.retrieving = false
      }
    },
    
    async cleanupMemory() {
      this.$confirm('确定要清理30天前的过期记忆吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await cleanupMemory(this.selectedAgentId, 30)
          if (response.success) {
            this.$message.success('过期记忆清理完成')
            this.loadMemoryStats()
            this.loadMemoryData()
          }
        } catch (error) {
          this.$message.error('清理记忆失败：' + error.message)
        }
      })
    },
    
    searchEntities() {
      if (!this.entitySearch.trim()) {
        this.filteredEntities = [...this.entities]
      } else {
        this.filteredEntities = this.entities.filter(entity =>
          entity.name.toLowerCase().includes(this.entitySearch.toLowerCase()) ||
          entity.description.toLowerCase().includes(this.entitySearch.toLowerCase())
        )
      }
    },
    
    handleTabClick(tab) {
      this.activeTab = tab.name
    },
    
    getImportanceType(importance) {
      if (importance >= 0.8) return 'danger'
      if (importance >= 0.6) return 'warning'
      if (importance >= 0.4) return 'primary'
      return 'info'
    },
    
    getEntityTypeColor(type) {
      const colors = {
        persons: 'primary',
        organizations: 'success',
        products: 'warning',
        concepts: 'info',
        dates: 'danger',
        locations: 'primary',
        numbers: 'success'
      }
      return colors[type] || 'info'
    },
    
    getEntityTypeText(type) {
      const texts = {
        persons: '人物',
        organizations: '组织',
        products: '产品',
        concepts: '概念',
        dates: '日期',
        locations: '地点',
        numbers: '数值'
      }
      return texts[type] || type
    },
    
    formatTime(time) {
      if (!time) return '-'
      return new Date(time).toLocaleString()
    }
  }
}
</script>

<style scoped>
.memory-manager {
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

.memory-stats {
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

.stat-icon.buffer { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-icon.entities { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.stat-icon.summaries { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.stat-icon.cache { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }

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

.memory-tabs {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.conversation-memories {
  max-height: 500px;
  overflow-y: auto;
}

.memory-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  background: #fafafa;
}

.memory-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.memory-time {
  color: #909399;
  font-size: 14px;
}

.memory-content {
  line-height: 1.6;
}

.user-message {
  margin-bottom: 8px;
  color: #303133;
}

.agent-response {
  color: #606266;
}

.entity-grid {
  max-height: 500px;
  overflow-y: auto;
}

.entity-card {
  margin-bottom: 16px;
  border: 1px solid #e4e7ed;
}

.entity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.entity-name {
  font-weight: 600;
  color: #303133;
}

.entity-description {
  color: #606266;
  font-size: 14px;
  margin-bottom: 8px;
}

.entity-stats {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.retrieval-section {
  max-height: 600px;
  overflow-y: auto;
}

.retrieval-results {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.result-section {
  margin-bottom: 20px;
}

.result-section h5 {
  margin: 0 0 12px 0;
  color: #303133;
}

.result-item {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
  margin-bottom: 8px;
  background: #f9f9f9;
}

.result-meta {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.entity-tags {
  line-height: 2;
}

.summary-content {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  color: #303133;
  line-height: 1.6;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}
</style>
