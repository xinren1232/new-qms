<template>
  <div class="dashboard">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="4" animated />
    </div>

    <!-- 主要内容 -->
    <div v-else>
      <div class="dashboard-header">
        <h1>QMS配置驱动智能管理系统</h1>
        <p>欢迎使用基于配置驱动的智能管理系统</p>
        <el-tag type="success" size="small">系统运行正常</el-tag>
      </div>

      <el-row :gutter="20">
      <el-col :span="6">
        <el-card>
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="stat-content">
              <h3>AI对话记录</h3>
              <p>{{ statistics.totalMessages || 0 }} 条</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card>
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><DataBoard /></el-icon>
            </div>
            <div class="stat-content">
              <h3>对话会话</h3>
              <p>{{ statistics.totalConversations || 0 }} 个</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card>
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><Setting /></el-icon>
            </div>
            <div class="stat-content">
              <h3>平均响应时间</h3>
              <p>{{ statistics.averageResponseTime || 0 }}ms</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card>
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-content">
              <h3>用户管理</h3>
              <p>89 人</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ChatDotRound, DataBoard, Setting, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 加载状态
const loading = ref(true)

// 统计数据
const statistics = ref({
  totalMessages: 0,
  totalConversations: 0,
  averageResponseTime: 0,
  averageRating: 0
})

// 加载统计数据
const loadStatistics = async () => {
  loading.value = true
  try {
    const response = await fetch('/api/chat/statistics?user_id=anonymous&days=30')
    const result = await response.json()

    if (result.success && result.data) {
      statistics.value = {
        totalMessages: result.data.totalMessages || 0,
        totalConversations: result.data.totalConversations || 0,
        averageResponseTime: result.data.averageResponseTime || 0,
        averageRating: result.data.averageRating || 0
      }
      console.log('✅ 统计数据加载成功:', statistics.value)
    } else {
      console.warn('⚠️ 统计数据加载失败')
    }
  } catch (error) {
    console.error('❌ 加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}

// 页面加载时获取统计数据
onMounted(() => {
  loadStatistics()
})
</script>

<style lang="scss" scoped>
.dashboard {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
}

.loading-container {
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;
}

.dashboard-header {
  text-align: center;
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);

  h1 {
    margin-bottom: 8px;
    color: #303133;
    font-weight: 600;
  }

  p {
    margin-bottom: 15px;
    color: #606266;
  }
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  
  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 8px;
    background: linear-gradient(135deg, #409EFF, #66b1ff);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 24px;
  }
  
  .stat-content {
    h3 {
      margin: 0 0 4px 0;
      font-size: 14px;
      color: #606266;
    }
    
    p {
      margin: 0;
      font-size: 24px;
      font-weight: 600;
      color: #303133;
    }
  }
}
</style>
