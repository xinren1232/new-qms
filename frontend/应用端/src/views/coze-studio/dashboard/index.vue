<template>
  <div class="coze-dashboard">
    <!-- 顶部统计卡片 -->
    <div class="dashboard-stats">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon agent">
                <el-icon><User /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.agents }}</div>
                <div class="stat-label">AI Agents</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon workflow">
                <el-icon><Connection /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.workflows }}</div>
                <div class="stat-label">工作流</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon knowledge">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.knowledge }}</div>
                <div class="stat-label">知识库</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon plugin">
                <el-icon><Tools /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.plugins }}</div>
                <div class="stat-label">插件</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 快速操作 -->
    <div class="quick-actions">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>快速开始</span>
          </div>
        </template>
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="action-item" @click="navigateTo('/coze-plugins/agents')">
              <div class="action-icon">
                <el-icon><User /></el-icon>
              </div>
              <div class="action-content">
                <h4>创建 Agent</h4>
                <p>创建智能对话助手</p>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="action-item" @click="navigateTo('/coze-plugins/workflows')">
              <div class="action-icon">
                <el-icon><Connection /></el-icon>
              </div>
              <div class="action-content">
                <h4>设计工作流</h4>
                <p>可视化流程设计</p>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="action-item" @click="navigateTo('/coze-plugins/knowledge')">
              <div class="action-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="action-content">
                <h4>管理知识库</h4>
                <p>上传和管理文档</p>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="action-item" @click="navigateTo('/coze-plugins/plugins')">
              <div class="action-icon">
                <el-icon><Tools /></el-icon>
              </div>
              <div class="action-content">
                <h4>开发插件</h4>
                <p>扩展平台功能</p>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- 主要内容区域 -->
    <el-row :gutter="20">
      <!-- 左侧内容 -->
      <el-col :span="16">
        <!-- 最近项目 -->
        <div class="recent-projects">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>最近项目</span>
                <el-button type="primary" size="small" @click="navigateTo('/coze-plugins/agents')">
                  查看全部
                </el-button>
              </div>
            </template>
            <div class="project-grid">
              <div
                v-for="project in recentProjects"
                :key="project.id"
                class="project-card"
                @click="openProject(project)"
              >
                <div class="project-header">
                  <div class="project-icon">
                    <el-icon>
                      <component :is="getProjectIcon(project.type)" />
                    </el-icon>
                  </div>
                  <div class="project-status">
                    <el-tag :type="getStatusType(project.status)" size="small">
                      {{ project.status }}
                    </el-tag>
                  </div>
                </div>
                <div class="project-content">
                  <h4>{{ project.name }}</h4>
                  <p>{{ project.description }}</p>
                  <div class="project-meta">
                    <span class="project-type">{{ getProjectTypeText(project.type) }}</span>
                    <span class="project-time">{{ formatTime(project.updatedAt) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 使用趋势图表 -->
        <div class="usage-trends" style="margin-top: 20px;">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>使用趋势</span>
                <el-radio-group v-model="trendPeriod" size="small">
                  <el-radio-button label="7d">7天</el-radio-button>
                  <el-radio-button label="30d">30天</el-radio-button>
                  <el-radio-button label="90d">90天</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div class="trend-chart">
              <div class="chart-placeholder">
                <el-icon><TrendCharts /></el-icon>
                <p>使用趋势图表将在这里显示</p>
                <el-button type="primary" size="small" @click="loadTrendData">
                  加载数据
                </el-button>
              </div>
            </div>
          </el-card>
        </div>
      </el-col>

      <!-- 右侧内容 -->
      <el-col :span="8">
        <!-- 最近活动 -->
        <div class="recent-activities">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>最近活动</span>
                <el-button type="text" size="small" @click="refreshActivities">
                  <el-icon><Refresh /></el-icon>
                  刷新
                </el-button>
              </div>
            </template>
            <div class="activity-list">
              <div
                v-for="activity in recentActivities"
                :key="activity.id"
                class="activity-item"
              >
                <div class="activity-icon">
                  <el-icon>
                    <component :is="getActivityIcon(activity.type)" />
                  </el-icon>
                </div>
                <div class="activity-content">
                  <div class="activity-title">{{ activity.title }}</div>
                  <div class="activity-desc">{{ activity.description }}</div>
                  <div class="activity-time">{{ formatTime(activity.createdAt) }}</div>
                </div>
              </div>
              <div v-if="recentActivities.length === 0" class="empty-activities">
                <el-empty description="暂无活动记录" :image-size="60" />
              </div>
            </div>
          </el-card>
        </div>

        <!-- 快速链接 -->
        <div class="quick-links" style="margin-top: 20px;">
          <el-card>
            <template #header>
              <span>快速链接</span>
            </template>
            <div class="link-list">
              <div class="link-item" @click="navigateTo('/coze-plugins/models')">
                <el-icon><Cpu /></el-icon>
                <span>模型管理</span>
                <el-icon class="arrow"><ArrowRight /></el-icon>
              </div>
              <div class="link-item" @click="navigateTo('/coze-plugins/test')">
                <el-icon><DataAnalysis /></el-icon>
                <span>效果评测</span>
                <el-icon class="arrow"><ArrowRight /></el-icon>
              </div>
              <div class="link-item" @click="openDocumentation">
                <el-icon><Document /></el-icon>
                <span>开发文档</span>
                <el-icon class="arrow"><ArrowRight /></el-icon>
              </div>
              <div class="link-item" @click="openCommunity">
                <el-icon><ChatDotRound /></el-icon>
                <span>社区支持</span>
                <el-icon class="arrow"><ArrowRight /></el-icon>
              </div>
            </div>
          </el-card>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  User,
  Connection,
  Document,
  Tools,
  TrendCharts,
  Refresh,
  ArrowRight,
  ChatDotRound,
  Cpu,
  DataAnalysis,
  Edit,
  Plus,
  Setting
} from '@element-plus/icons-vue'

// 导入API函数
import {
  getAgents,
  getWorkflows,
  getKnowledgeBases,
  getPlugins,
  getProjects
} from '@/api/coze-studio'

const router = useRouter()

// 统计数据
const stats = reactive({
  agents: 0,
  workflows: 0,
  knowledge: 0,
  plugins: 0
})

// 最近项目
const recentProjects = ref([])

// 趋势数据
const trendPeriod = ref('7d')

// 最近活动
const recentActivities = ref([])

// 导航到指定页面
const navigateTo = (path) => {
  router.push(path)
}

// 打开项目
const openProject = (project) => {
  const routeMap = {
    agent: '/coze-plugins/agents',
    workflow: '/coze-plugins/workflows',
    knowledge: '/coze-plugins/knowledge',
    plugin: '/coze-plugins/plugins'
  }
  router.push(`${routeMap[project.type]}?id=${project.id}`)
}

// 获取项目图标
const getProjectIcon = (type) => {
  const iconMap = {
    agent: User,
    workflow: Connection,
    knowledge: Document,
    plugin: Tools
  }
  return iconMap[type] || User
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    active: 'success',
    draft: 'warning',
    archived: 'info'
  }
  return typeMap[status] || 'info'
}

// 获取项目类型文本
const getProjectTypeText = (type) => {
  const textMap = {
    agent: 'AI Agent',
    workflow: '工作流',
    knowledge: '知识库',
    plugin: '插件'
  }
  return textMap[type] || type
}

// 格式化时间
const formatTime = (time) => {
  const now = new Date()
  const target = new Date(time)
  const diff = now - target

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`

  return target.toLocaleDateString()
}

// 获取活动图标
const getActivityIcon = (type) => {
  const iconMap = {
    create: Plus,
    edit: Edit,
    delete: Tools,
    publish: Connection,
    test: DataAnalysis
  }
  return iconMap[type] || Document
}

// 刷新活动
const refreshActivities = async () => {
  try {
    // 模拟加载最近活动
    recentActivities.value = [
      {
        id: 1,
        type: 'create',
        title: '创建了新的 Agent',
        description: '智能客服助手 v2.0',
        createdAt: new Date(Date.now() - 300000) // 5分钟前
      },
      {
        id: 2,
        type: 'edit',
        title: '更新了工作流',
        description: '订单处理流程优化',
        createdAt: new Date(Date.now() - 1800000) // 30分钟前
      },
      {
        id: 3,
        type: 'publish',
        title: '发布了知识库',
        description: '产品FAQ知识库上线',
        createdAt: new Date(Date.now() - 3600000) // 1小时前
      },
      {
        id: 4,
        type: 'test',
        title: '运行了测试',
        description: 'GPT-4 模型性能测试',
        createdAt: new Date(Date.now() - 7200000) // 2小时前
      }
    ]
  } catch (error) {
    console.error('刷新活动失败:', error)
  }
}

// 加载趋势数据
const loadTrendData = () => {
  // 这里可以集成图表库如 ECharts
  console.log(`加载 ${trendPeriod.value} 的趋势数据`)
}

// 打开文档
const openDocumentation = () => {
  window.open('https://docs.coze.cn', '_blank')
}

// 打开社区
const openCommunity = () => {
  window.open('https://community.coze.cn', '_blank')
}

// 加载数据
const loadData = async () => {
  try {
    // 获取真实统计数据
    const [agentsRes, workflowsRes, knowledgeRes, pluginsRes, projectsRes] = await Promise.allSettled([
      getAgents({ limit: 1 }),
      getWorkflows({ limit: 1 }),
      getKnowledgeBases({ limit: 1 }),
      getPlugins({ limit: 1 }),
      getProjects({ limit: 10 })
    ]);

    // 更新统计数据
    stats.agents = agentsRes.status === 'fulfilled' ? (agentsRes.value.data?.total || 0) : 0;
    stats.workflows = workflowsRes.status === 'fulfilled' ? (workflowsRes.value.data?.total || 0) : 0;
    stats.knowledge = knowledgeRes.status === 'fulfilled' ? (knowledgeRes.value.data?.total || 0) : 0;
    stats.plugins = pluginsRes.status === 'fulfilled' ? (pluginsRes.value.data?.total || 0) : 0;

    // 获取最近项目
    if (projectsRes.status === 'fulfilled' && projectsRes.value.data?.items) {
      recentProjects.value = projectsRes.value.data.items.map(project => ({
        id: project.id,
        name: project.name,
        description: project.description,
        type: project.type,
        status: project.status,
        updatedAt: new Date(project.updated_at)
      }));
    } else {
      // 如果没有项目数据，显示空状态
      recentProjects.value = [];
    }

    // 加载活动数据
    await refreshActivities()
  } catch (error) {
    console.error('加载数据失败:', error)
    // 发生错误时显示默认值
    stats.agents = 0;
    stats.workflows = 0;
    stats.knowledge = 0;
    stats.plugins = 0;
    recentProjects.value = [];
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
@import "@/styles/variables.scss";
@import "@/styles/responsive.scss";
@import "@/styles/modern-theme.scss";

.coze-dashboard {
  padding: 20px;
  background: var(--el-bg-color-page);
  min-height: calc(100vh - 60px);

  .dashboard-stats {
    margin-bottom: 20px;

    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        gap: 15px;

        .stat-icon {
          width: 50px;
          height: 50px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24px;
          color: white;

          &.agent {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          }

          &.workflow {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          }

          &.knowledge {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
          }

          &.plugin {
            background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
          }
        }

        .stat-info {
          .stat-number {
            font-size: 28px;
            font-weight: bold;
            color: var(--el-text-color-primary);
            line-height: 1;
          }

          .stat-label {
            font-size: 14px;
            color: var(--el-text-color-regular);
            margin-top: 4px;
          }
        }
      }
    }
  }

  .quick-actions {
    margin-bottom: 20px;

    .action-item {
      display: flex;
      align-items: center;
      gap: 15px;
      padding: 20px;
      border-radius: 12px;
      background: var(--el-bg-color);
      border: 1px solid var(--el-border-color-light);
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        border-color: var(--el-color-primary);
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      .action-icon {
        width: 40px;
        height: 40px;
        border-radius: 10px;
        background: var(--el-color-primary-light-9);
        display: flex;
        align-items: center;
        justify-content: center;
        color: var(--el-color-primary);
        font-size: 20px;
      }

      .action-content {
        h4 {
          margin: 0 0 4px 0;
          font-size: 16px;
          color: var(--el-text-color-primary);
        }

        p {
          margin: 0;
          font-size: 14px;
          color: var(--el-text-color-regular);
        }
      }
    }
  }

  .recent-projects {
    .project-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 20px;

      .project-card {
        padding: 20px;
        border-radius: 12px;
        background: var(--el-bg-color);
        border: 1px solid var(--el-border-color-light);
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          border-color: var(--el-color-primary);
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }

        .project-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 15px;

          .project-icon {
            width: 40px;
            height: 40px;
            border-radius: 10px;
            background: var(--el-color-primary-light-9);
            display: flex;
            align-items: center;
            justify-content: center;
            color: var(--el-color-primary);
            font-size: 20px;
          }
        }

        .project-content {
          h4 {
            margin: 0 0 8px 0;
            font-size: 16px;
            color: var(--el-text-color-primary);
          }

          p {
            margin: 0 0 15px 0;
            font-size: 14px;
            color: var(--el-text-color-regular);
            line-height: 1.5;
          }

          .project-meta {
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 12px;
            color: var(--el-text-color-secondary);

            .project-type {
              font-weight: 500;
            }
          }
        }
      }
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .trend-chart {
    .chart-placeholder {
      text-align: center;
      padding: 60px 20px;
      color: var(--el-text-color-secondary);

      .el-icon {
        font-size: 48px;
        margin-bottom: 16px;
        color: var(--el-color-primary-light-5);
      }

      p {
        margin: 0 0 16px 0;
        font-size: 14px;
      }
    }
  }

  .recent-activities {
    .activity-list {
      max-height: 400px;
      overflow-y: auto;

      .activity-item {
        display: flex;
        gap: 12px;
        padding: 12px 0;
        border-bottom: 1px solid var(--el-border-color-lighter);

        &:last-child {
          border-bottom: none;
        }

        .activity-icon {
          width: 32px;
          height: 32px;
          border-radius: 8px;
          background: var(--el-color-primary-light-9);
          display: flex;
          align-items: center;
          justify-content: center;
          color: var(--el-color-primary);
          flex-shrink: 0;
        }

        .activity-content {
          flex: 1;

          .activity-title {
            font-size: 14px;
            font-weight: 500;
            color: var(--el-text-color-primary);
            margin-bottom: 4px;
          }

          .activity-desc {
            font-size: 12px;
            color: var(--el-text-color-regular);
            margin-bottom: 4px;
          }

          .activity-time {
            font-size: 12px;
            color: var(--el-text-color-secondary);
          }
        }
      }

      .empty-activities {
        padding: 40px 20px;
      }
    }
  }

  .quick-links {
    .link-list {
      .link-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px 16px;
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.2s ease;
        margin-bottom: 4px;

        &:hover {
          background: var(--el-fill-color-light);
        }

        .el-icon {
          font-size: 16px;
          color: var(--el-text-color-regular);
        }

        span {
          flex: 1;
          font-size: 14px;
          color: var(--el-text-color-primary);
        }

        .arrow {
          font-size: 12px;
          color: var(--el-text-color-secondary);
        }
      }
    }
  }
}
</style>
