<template>
  <div class="collaboration-center">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><UserFilled /></el-icon>
          团队协作
        </h1>
        <p class="page-description">与团队成员分享对话，协作解决质量管理问题</p>
      </div>
      <div class="header-actions">
        <el-button @click="refreshData" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
        <el-button type="primary" @click="showShareDialog">
          <el-icon><Share /></el-icon>
          分享对话
        </el-button>
      </div>
    </div>

    <!-- 团队统计 -->
    <el-row :gutter="20" class="team-stats">
      <el-col :xs="12" :sm="6" v-for="stat in teamStats" :key="stat.key">
        <div class="stat-card">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="24"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 协作内容 -->
    <el-row :gutter="20" class="collaboration-content">
      <!-- 分享管理 -->
      <el-col :xs="24" :lg="12">
        <el-card class="share-management">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Share /></el-icon>
                分享管理
              </span>
            </div>
          </template>

          <div class="share-actions">
            <el-button type="primary" @click="showShareDialog">
              <el-icon><Plus /></el-icon>
              新建分享
            </el-button>
            <el-button @click="refreshSharedConversations">
              <el-icon><Refresh /></el-icon>
              刷新列表
            </el-button>
          </div>

          <div class="shared-list" v-loading="collaboration.loading">
            <div v-if="sharedConversations.length === 0" class="empty-state">
              <el-empty description="暂无分享内容" :image-size="80">
                <el-button type="primary" @click="showShareDialog">
                  创建第一个分享
                </el-button>
              </el-empty>
            </div>
            <div v-else>
              <div
                v-for="share in sharedConversations"
                :key="share.id"
                class="share-item"
              >
                <div class="share-info">
                  <div class="share-title">{{ share.title }}</div>
                  <div class="share-meta">
                    <span class="share-type">{{ share.shareType }}</span>
                    <span class="share-time">{{ formatTime(share.sharedAt) }}</span>
                  </div>
                </div>
                <div class="share-actions">
                  <el-button size="small" @click="viewSharedConversation(share)">
                    查看
                  </el-button>
                  <el-button size="small" type="danger" @click="removeShare(share)">
                    删除
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 团队活动 -->
      <el-col :xs="24" :lg="12">
        <el-card class="team-activity">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Clock /></el-icon>
                团队活动
              </span>
            </div>
          </template>

          <div class="activity-list" v-loading="collaboration.loading">
            <div v-if="teamActivities.length === 0" class="empty-state">
              <el-empty description="暂无团队活动" :image-size="80" />
            </div>
            <div v-else>
              <div
                v-for="activity in teamActivities"
                :key="activity.id"
                class="activity-item"
              >
                <div class="activity-avatar">
                  <el-avatar :size="32" :src="activity.userAvatar">
                    {{ activity.userName?.charAt(0) }}
                  </el-avatar>
                </div>
                <div class="activity-content">
                  <div class="activity-text">
                    <strong>{{ activity.userName }}</strong>
                    {{ activity.action }}
                    <span class="activity-target">{{ activity.target }}</span>
                  </div>
                  <div class="activity-time">{{ formatTime(activity.createdAt) }}</div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分享对话弹窗 -->
    <el-dialog
      v-model="shareDialogVisible"
      title="分享对话"
      width="500px"
      :before-close="handleShareDialogClose"
    >
      <el-form :model="shareForm" :rules="shareRules" ref="shareFormRef" label-width="80px">
        <el-form-item label="对话" prop="conversationId">
          <el-select v-model="shareForm.conversationId" placeholder="选择要分享的对话" style="width: 100%">
            <el-option
              v-for="conv in availableConversations"
              :key="conv.id"
              :label="conv.title"
              :value="conv.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="分享类型" prop="shareType">
          <el-radio-group v-model="shareForm.shareType">
            <el-radio label="public">公开分享</el-radio>
            <el-radio label="team">团队内分享</el-radio>
            <el-radio label="private">私密分享</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="分享说明" prop="description">
          <el-input
            v-model="shareForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分享说明..."
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="shareDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitShare" :loading="shareSubmitting">
            确定分享
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { collaborationAPI } from '@/api/advanced-features'

// 响应式数据
const loading = ref(false)
const shareDialogVisible = ref(false)
const shareSubmitting = ref(false)
const shareFormRef = ref()

const collaboration = reactive({
  loading: false
})

const teamStats = ref([
  {
    key: 'members',
    label: '团队成员',
    value: '0',
    icon: 'User',
    color: '#409EFF'
  },
  {
    key: 'conversations',
    label: '团队对话',
    value: '0',
    icon: 'ChatDotRound',
    color: '#67C23A'
  },
  {
    key: 'shares',
    label: '分享数量',
    value: '0',
    icon: 'Share',
    color: '#E6A23C'
  },
  {
    key: 'rating',
    label: '平均评分',
    value: '0.0',
    icon: 'Star',
    color: '#F56C6C'
  }
])

const sharedConversations = ref([])
const teamActivities = ref([])
const availableConversations = ref([])

const shareForm = reactive({
  conversationId: '',
  shareType: 'team',
  description: ''
})

const shareRules = {
  conversationId: [
    { required: true, message: '请选择要分享的对话', trigger: 'change' }
  ],
  shareType: [
    { required: true, message: '请选择分享类型', trigger: 'change' }
  ]
}

// 生命周期
onMounted(() => {
  loadCollaborationData()
})

// 方法
const loadCollaborationData = async () => {
  await Promise.all([
    loadTeamStats(),
    loadSharedConversations(),
    loadTeamActivities(),
    loadAvailableConversations()
  ])
}

const loadTeamStats = async () => {
  try {
    const response = await collaborationAPI.getTeamStats()
    if (response.success) {
      const stats = response.data
      updateTeamStat('members', stats.memberCount || 0)
      updateTeamStat('conversations', stats.totalConversations || 0)
      updateTeamStat('shares', stats.totalShares || 0)
      updateTeamStat('rating', (stats.avgRating || 0).toFixed(1))
    }
  } catch (error) {
    console.error('加载团队统计失败:', error)
  }
}

const loadSharedConversations = async () => {
  collaboration.loading = true
  try {
    const response = await collaborationAPI.getSharedConversations()
    if (response.success) {
      sharedConversations.value = response.data
    }
  } catch (error) {
    console.error('加载分享对话失败:', error)
  } finally {
    collaboration.loading = false
  }
}

const loadTeamActivities = async () => {
  try {
    // 模拟团队活动数据
    teamActivities.value = [
      {
        id: 1,
        userName: '张三',
        userAvatar: '',
        action: '分享了对话',
        target: '质量体系建设讨论',
        createdAt: new Date(Date.now() - 1000 * 60 * 30)
      },
      {
        id: 2,
        userName: '李四',
        userAvatar: '',
        action: '评价了',
        target: 'ISO9001标准解读',
        createdAt: new Date(Date.now() - 1000 * 60 * 60 * 2)
      }
    ]
  } catch (error) {
    console.error('加载团队活动失败:', error)
  }
}

const loadAvailableConversations = async () => {
  try {
    const response = await collaborationAPI.getAccessibleConversations()
    if (response.success) {
      availableConversations.value = response.data
    }
  } catch (error) {
    console.error('加载可用对话失败:', error)
  }
}

const updateTeamStat = (key, value) => {
  const stat = teamStats.value.find(s => s.key === key)
  if (stat) {
    stat.value = value
  }
}

const refreshData = () => {
  loadCollaborationData()
}

const refreshSharedConversations = () => {
  loadSharedConversations()
}

const showShareDialog = () => {
  shareDialogVisible.value = true
  loadAvailableConversations()
}

const handleShareDialogClose = () => {
  shareDialogVisible.value = false
  shareFormRef.value?.resetFields()
}

const submitShare = async () => {
  try {
    await shareFormRef.value.validate()

    shareSubmitting.value = true
    const response = await collaborationAPI.shareConversation(shareForm)

    if (response.success) {
      ElMessage.success('分享成功')
      shareDialogVisible.value = false
      shareFormRef.value.resetFields()
      await loadSharedConversations()
    } else {
      ElMessage.error(response.message || '分享失败')
    }
  } catch (error) {
    console.error('分享失败:', error)
    ElMessage.error('分享失败')
  } finally {
    shareSubmitting.value = false
  }
}

const viewSharedConversation = (share) => {
  ElMessage.info('查看分享对话功能开发中...')
}

const removeShare = async (share) => {
  try {
    await ElMessageBox.confirm('确定要删除这个分享吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    ElMessage.success('删除成功')
    await loadSharedConversations()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString()
}
</script>

<style lang="scss" scoped>
.collaboration-center {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  background: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

  .header-content {
    .page-title {
      display: flex;
      align-items: center;
      font-size: 24px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 8px 0;

      .el-icon {
        margin-right: 12px;
        color: #409EFF;
      }
    }

    .page-description {
      color: #606266;
      margin: 0;
      font-size: 14px;
    }
  }

  .header-actions {
    display: flex;
    gap: 12px;
  }
}

.team-stats {
  margin-bottom: 24px;

  .stat-card {
    background: white;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    display: flex;
    align-items: center;
    transition: transform 0.2s ease;

    &:hover {
      transform: translateY(-2px);
    }

    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;
      color: white;
    }

    .stat-content {
      .stat-value {
        font-size: 24px;
        font-weight: 600;
        color: #303133;
        line-height: 1;
        margin-bottom: 4px;
      }

      .stat-label {
        font-size: 14px;
        color: #606266;
      }
    }
  }
}

.collaboration-content {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .card-title {
      display: flex;
      align-items: center;
      font-weight: 600;

      .el-icon {
        margin-right: 8px;
        color: #409EFF;
      }
    }
  }

  .share-actions {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
  }

  .shared-list, .activity-list {
    min-height: 300px;

    .empty-state {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 300px;
    }

    .share-item, .activity-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 16px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }
    }

    .share-info {
      flex: 1;

      .share-title {
        font-weight: 500;
        color: #303133;
        margin-bottom: 4px;
      }

      .share-meta {
        display: flex;
        gap: 12px;
        font-size: 12px;
        color: #909399;
      }
    }

    .activity-item {
      .activity-avatar {
        margin-right: 12px;
      }

      .activity-content {
        flex: 1;

        .activity-text {
          font-size: 14px;
          color: #303133;
          margin-bottom: 4px;

          .activity-target {
            color: #409EFF;
          }
        }

        .activity-time {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .collaboration-center {
    padding: 12px;
  }

  .page-header {
    flex-direction: column;
    align-items: stretch;

    .header-actions {
      margin-top: 16px;
      justify-content: flex-end;
    }
  }

  .team-stats {
    .el-col {
      margin-bottom: 12px;
    }
  }

  .collaboration-content {
    .el-col {
      margin-bottom: 20px;
    }
  }
}
</style>