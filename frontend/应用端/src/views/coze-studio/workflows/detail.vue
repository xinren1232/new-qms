<template>
  <div class="workflow-detail">
    <div class="detail-header">
      <div class="header-left">
        <el-button @click="goBack" :icon="ArrowLeft" circle />
        <div class="workflow-info">
          <h1 class="workflow-title">{{ workflow.name }}</h1>
          <div class="workflow-meta">
            <el-tag :type="getStatusType(workflow.status)">
              {{ getStatusText(workflow.status) }}
            </el-tag>
            <span class="meta-item">创建时间：{{ formatDate(workflow.createdAt) }}</span>
            <span class="meta-item">更新时间：{{ formatDate(workflow.updatedAt) }}</span>
          </div>
        </div>
      </div>
      <div class="header-actions">
        <el-button @click="editWorkflow" type="primary" :icon="Edit">
          编辑工作流
        </el-button>
        <el-button @click="runWorkflow" :icon="VideoPlay">
          运行测试
        </el-button>
        <el-dropdown @command="handleCommand">
          <el-button :icon="MoreFilled" circle />
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="duplicate">复制工作流</el-dropdown-item>
              <el-dropdown-item command="export">导出配置</el-dropdown-item>
              <el-dropdown-item command="delete" divided>删除工作流</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div class="detail-content">
      <el-tabs v-model="activeTab" class="workflow-tabs">
        <el-tab-pane label="工作流概览" name="overview">
          <div class="overview-content">
            <div class="workflow-description">
              <h3>描述</h3>
              <p>{{ workflow.description || '暂无描述' }}</p>
            </div>
            
            <div class="workflow-stats">
              <div class="stat-card">
                <div class="stat-value">{{ workflow.nodeCount || 0 }}</div>
                <div class="stat-label">节点数量</div>
              </div>
              <div class="stat-card">
                <div class="stat-value">{{ workflow.runCount || 0 }}</div>
                <div class="stat-label">运行次数</div>
              </div>
              <div class="stat-card">
                <div class="stat-value">{{ workflow.successRate || '0%' }}</div>
                <div class="stat-label">成功率</div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="节点配置" name="nodes">
          <div class="nodes-content">
            <div class="nodes-list">
              <div v-for="node in workflow.nodes" :key="node.id" class="node-item">
                <div class="node-header">
                  <div class="node-icon">
                    <el-icon><component :is="getNodeIcon(node.type)" /></el-icon>
                  </div>
                  <div class="node-info">
                    <div class="node-name">{{ node.name }}</div>
                    <div class="node-type">{{ getNodeTypeName(node.type) }}</div>
                  </div>
                </div>
                <div class="node-config">
                  <pre>{{ JSON.stringify(node.config, null, 2) }}</pre>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="运行历史" name="history">
          <div class="history-content">
            <el-table :data="runHistory" style="width: 100%">
              <el-table-column prop="id" label="运行ID" width="120" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.status)">
                    {{ getStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="startTime" label="开始时间" width="180">
                <template #default="{ row }">
                  {{ formatDate(row.startTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="duration" label="耗时" width="100" />
              <el-table-column prop="trigger" label="触发方式" width="120" />
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button size="small" @click="viewRunDetail(row)">
                    查看详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  ArrowLeft, 
  Edit, 
  VideoPlay, 
  MoreFilled,
  User,
  Cpu,
  Connection,
  Document
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const activeTab = ref('overview')
const workflow = ref({
  id: route.params.id,
  name: '客户服务工作流',
  description: '处理客户咨询和问题解决的自动化工作流',
  status: 'active',
  createdAt: new Date('2024-01-15'),
  updatedAt: new Date('2024-01-20'),
  nodeCount: 5,
  runCount: 128,
  successRate: '95.3%',
  nodes: [
    {
      id: 'start',
      name: '开始节点',
      type: 'start',
      config: { trigger: 'manual' }
    },
    {
      id: 'classify',
      name: '问题分类',
      type: 'ai',
      config: { model: 'gpt-3.5-turbo', prompt: '分析客户问题类型' }
    },
    {
      id: 'route',
      name: '路由分发',
      type: 'condition',
      config: { conditions: ['技术问题', '账单问题', '其他'] }
    },
    {
      id: 'response',
      name: '生成回复',
      type: 'ai',
      config: { model: 'gpt-4', prompt: '生成专业回复' }
    },
    {
      id: 'end',
      name: '结束节点',
      type: 'end',
      config: {}
    }
  ]
})

const runHistory = ref([
  {
    id: 'run_001',
    status: 'success',
    startTime: new Date('2024-01-20 10:30:00'),
    duration: '2.3s',
    trigger: '手动触发'
  },
  {
    id: 'run_002',
    status: 'failed',
    startTime: new Date('2024-01-20 09:15:00'),
    duration: '1.8s',
    trigger: 'API调用'
  }
])

const goBack = () => {
  router.back()
}

const editWorkflow = () => {
  router.push(`/coze-plugins/workflows/${workflow.value.id}/edit`)
}

const runWorkflow = () => {
  ElMessage.success('工作流运行中...')
}

const handleCommand = (command) => {
  switch (command) {
    case 'duplicate':
      ElMessage.success('工作流已复制')
      break
    case 'export':
      ElMessage.success('配置已导出')
      break
    case 'delete':
      ElMessage.warning('删除功能待实现')
      break
  }
}

const viewRunDetail = (run) => {
  ElMessage.info(`查看运行详情: ${run.id}`)
}

const getStatusType = (status) => {
  const types = {
    active: 'success',
    inactive: 'info',
    error: 'danger',
    success: 'success',
    failed: 'danger',
    running: 'warning'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    active: '活跃',
    inactive: '未激活',
    error: '错误',
    success: '成功',
    failed: '失败',
    running: '运行中'
  }
  return texts[status] || status
}

const getNodeIcon = (type) => {
  const icons = {
    start: 'VideoPlay',
    ai: 'Cpu',
    condition: 'Connection',
    end: 'Document'
  }
  return icons[type] || 'Document'
}

const getNodeTypeName = (type) => {
  const names = {
    start: '开始节点',
    ai: 'AI节点',
    condition: '条件节点',
    end: '结束节点'
  }
  return names[type] || type
}

const formatDate = (date) => {
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  // 加载工作流详情
})
</script>

<style lang="scss" scoped>
.workflow-detail {
  height: 100%;
  display: flex;
  flex-direction: column;

  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    background: white;
    border-bottom: 1px solid #e4e7ed;

    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;

      .workflow-info {
        .workflow-title {
          margin: 0 0 8px 0;
          font-size: 24px;
          font-weight: 600;
          color: #1f2937;
        }

        .workflow-meta {
          display: flex;
          align-items: center;
          gap: 16px;
          font-size: 14px;
          color: #6b7280;

          .meta-item {
            display: flex;
            align-items: center;
          }
        }
      }
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .detail-content {
    flex: 1;
    padding: 20px;
    overflow: auto;

    .workflow-tabs {
      height: 100%;

      .overview-content {
        .workflow-description {
          margin-bottom: 24px;

          h3 {
            margin: 0 0 12px 0;
            font-size: 16px;
            font-weight: 600;
            color: #1f2937;
          }

          p {
            margin: 0;
            color: #6b7280;
            line-height: 1.6;
          }
        }

        .workflow-stats {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
          gap: 16px;

          .stat-card {
            padding: 20px;
            background: white;
            border-radius: 8px;
            border: 1px solid #e5e7eb;
            text-align: center;

            .stat-value {
              font-size: 32px;
              font-weight: 700;
              color: #1f2937;
              margin-bottom: 8px;
            }

            .stat-label {
              font-size: 14px;
              color: #6b7280;
            }
          }
        }
      }

      .nodes-content {
        .nodes-list {
          display: flex;
          flex-direction: column;
          gap: 16px;

          .node-item {
            background: white;
            border: 1px solid #e5e7eb;
            border-radius: 8px;
            overflow: hidden;

            .node-header {
              display: flex;
              align-items: center;
              gap: 12px;
              padding: 16px;
              background: #f9fafb;
              border-bottom: 1px solid #e5e7eb;

              .node-icon {
                width: 40px;
                height: 40px;
                border-radius: 8px;
                background: #3b82f6;
                display: flex;
                align-items: center;
                justify-content: center;
                color: white;
                font-size: 18px;
              }

              .node-info {
                .node-name {
                  font-size: 16px;
                  font-weight: 600;
                  color: #1f2937;
                  margin-bottom: 4px;
                }

                .node-type {
                  font-size: 14px;
                  color: #6b7280;
                }
              }
            }

            .node-config {
              padding: 16px;

              pre {
                margin: 0;
                font-size: 12px;
                color: #374151;
                background: #f3f4f6;
                padding: 12px;
                border-radius: 4px;
                overflow-x: auto;
              }
            }
          }
        }
      }

      .history-content {
        background: white;
        border-radius: 8px;
        overflow: hidden;
      }
    }
  }
}
</style>
