<template>
  <div class="feishu-integration">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <el-icon size="48" color="#00d4aa">
            <ChatDotRound />
          </el-icon>
        </div>
        <div class="header-text">
          <h1>飞书集成</h1>
          <p>集成飞书开放平台API，实现消息推送、文档操作、日程管理等功能</p>
        </div>
      </div>
      <div class="header-status">
        <el-tag :type="connectionStatus.type" size="large">
          {{ connectionStatus.text }}
        </el-tag>
      </div>
    </div>

    <!-- 功能概览 -->
    <div class="features-overview">
      <div class="feature-card" :class="{ active: features.messaging.enabled }">
        <div class="feature-icon">
          <el-icon><ChatDotRound /></el-icon>
        </div>
        <div class="feature-content">
          <h3>消息推送</h3>
          <p>支持发送文本、富文本、卡片消息到个人或群聊</p>
          <div class="feature-stats">
            <span>今日发送: {{ features.messaging.todayCount }}</span>
          </div>
        </div>
        <div class="feature-toggle">
          <el-switch v-model="features.messaging.enabled" @change="toggleFeature('messaging')" />
        </div>
      </div>

      <div class="feature-card" :class="{ active: features.documents.enabled }">
        <div class="feature-icon">
          <el-icon><Document /></el-icon>
        </div>
        <div class="feature-content">
          <h3>文档操作</h3>
          <p>创建、编辑、分享飞书文档和表格</p>
          <div class="feature-stats">
            <span>文档数量: {{ features.documents.count }}</span>
          </div>
        </div>
        <div class="feature-toggle">
          <el-switch v-model="features.documents.enabled" @change="toggleFeature('documents')" />
        </div>
      </div>

      <div class="feature-card" :class="{ active: features.calendar.enabled }">
        <div class="feature-icon">
          <el-icon><Calendar /></el-icon>
        </div>
        <div class="feature-content">
          <h3>日程管理</h3>
          <p>创建会议、管理日程、发送邀请</p>
          <div class="feature-stats">
            <span>本周会议: {{ features.calendar.weeklyMeetings }}</span>
          </div>
        </div>
        <div class="feature-toggle">
          <el-switch v-model="features.calendar.enabled" @change="toggleFeature('calendar')" />
        </div>
      </div>

      <div class="feature-card" :class="{ active: features.bot.enabled }">
        <div class="feature-icon">
          <el-icon><ChatDotRound /></el-icon>
        </div>
        <div class="feature-content">
          <h3>机器人</h3>
          <p>自定义机器人，自动化处理消息和任务</p>
          <div class="feature-stats">
            <span>活跃机器人: {{ features.bot.activeCount }}</span>
          </div>
        </div>
        <div class="feature-toggle">
          <el-switch v-model="features.bot.enabled" @change="toggleFeature('bot')" />
        </div>
      </div>
    </div>

    <!-- 主要功能区域 -->
    <div class="main-content">
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 基础配置 -->
        <el-tab-pane label="基础配置" name="config">
          <div class="config-section">
            <h3>应用配置</h3>
            <el-form :model="config" label-width="120px">
              <el-form-item label="App ID">
                <el-input v-model="config.appId" placeholder="请输入飞书应用ID" />
              </el-form-item>
              <el-form-item label="App Secret">
                <el-input v-model="config.appSecret" type="password" placeholder="请输入飞书应用密钥" show-password />
              </el-form-item>
              <el-form-item label="API域名">
                <el-select v-model="config.domain" placeholder="选择API域名">
                  <el-option label="国内版 (open.feishu.cn)" value="https://open.feishu.cn" />
                  <el-option label="国际版 (open.larksuite.com)" value="https://open.larksuite.com" />
                </el-select>
              </el-form-item>
              <el-form-item label="Webhook URL">
                <el-input v-model="config.webhookUrl" placeholder="自定义机器人Webhook地址" />
              </el-form-item>
              <el-form-item>
                <el-button @click="testConnection" type="primary" :loading="testing">测试连接</el-button>
                <el-button @click="saveConfig" :loading="saving">保存配置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 消息推送 -->
        <el-tab-pane label="消息推送" name="messaging">
          <div class="messaging-section">
            <div class="section-header">
              <h3>发送消息</h3>
              <el-button @click="showMessageTemplates = true" size="small">消息模板</el-button>
            </div>
            
            <el-form :model="messageForm" label-width="100px">
              <el-form-item label="接收人">
                <el-select v-model="messageForm.receivers" multiple placeholder="选择接收人">
                  <el-option
                    v-for="user in feishuUsers"
                    :key="user.user_id"
                    :label="user.name"
                    :value="user.user_id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="消息类型">
                <el-radio-group v-model="messageForm.msgType">
                  <el-radio label="text">纯文本</el-radio>
                  <el-radio label="rich_text">富文本</el-radio>
                  <el-radio label="interactive">交互卡片</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="消息内容">
                <el-input
                  v-model="messageForm.content"
                  type="textarea"
                  :rows="4"
                  placeholder="输入消息内容"
                />
              </el-form-item>
              <el-form-item>
                <el-button @click="sendMessage" type="primary" :loading="sendingMessage">发送消息</el-button>
                <el-button @click="previewMessage">预览</el-button>
              </el-form-item>
            </el-form>

            <!-- 消息历史 -->
            <div class="message-history">
              <h4>发送历史</h4>
              <el-table :data="messageHistory" style="width: 100%">
                <el-table-column prop="time" label="时间" width="180" />
                <el-table-column prop="receivers" label="接收人" />
                <el-table-column prop="content" label="内容" show-overflow-tooltip />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="scope.row.status === 'success' ? 'success' : 'danger'">
                      {{ scope.row.status === 'success' ? '成功' : '失败' }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-tab-pane>

        <!-- 文档操作 -->
        <el-tab-pane label="文档操作" name="documents">
          <div class="documents-section">
            <div class="section-header">
              <h3>文档管理</h3>
              <el-button @click="createDocument" type="primary" :icon="Plus">创建文档</el-button>
            </div>

            <div class="document-actions">
              <el-card class="action-card">
                <div class="action-icon">
                  <el-icon><DocumentAdd /></el-icon>
                </div>
                <h4>创建文档</h4>
                <p>创建新的飞书文档或表格</p>
                <el-button @click="showCreateDocDialog = true" size="small">开始创建</el-button>
              </el-card>

              <el-card class="action-card">
                <div class="action-icon">
                  <el-icon><Upload /></el-icon>
                </div>
                <h4>上传文件</h4>
                <p>上传文件到飞书云盘</p>
                <el-button @click="uploadFile" size="small">选择文件</el-button>
              </el-card>

              <el-card class="action-card">
                <div class="action-icon">
                  <el-icon><Share /></el-icon>
                </div>
                <h4>分享文档</h4>
                <p>分享文档给团队成员</p>
                <el-button @click="shareDocument" size="small">分享</el-button>
              </el-card>
            </div>

            <!-- 文档列表 -->
            <div class="document-list">
              <h4>最近文档</h4>
              <el-table :data="recentDocuments" style="width: 100%">
                <el-table-column prop="title" label="文档标题" />
                <el-table-column prop="type" label="类型" width="100" />
                <el-table-column prop="creator" label="创建者" width="120" />
                <el-table-column prop="updateTime" label="更新时间" width="180" />
                <el-table-column label="操作" width="150">
                  <template #default="scope">
                    <el-button @click="openDocument(scope.row)" size="small" text>打开</el-button>
                    <el-button @click="shareDocument(scope.row)" size="small" text>分享</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-tab-pane>

        <!-- 日程管理 -->
        <el-tab-pane label="日程管理" name="calendar">
          <div class="calendar-section">
            <div class="section-header">
              <h3>日程管理</h3>
              <el-button @click="showCreateEventDialog = true" type="primary" :icon="Plus">创建会议</el-button>
            </div>

            <div class="calendar-overview">
              <div class="calendar-stats">
                <div class="stat-item">
                  <div class="stat-value">{{ calendarStats.todayEvents }}</div>
                  <div class="stat-label">今日会议</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ calendarStats.weekEvents }}</div>
                  <div class="stat-label">本周会议</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ calendarStats.upcomingEvents }}</div>
                  <div class="stat-label">即将开始</div>
                </div>
              </div>
            </div>

            <!-- 今日日程 -->
            <div class="today-events">
              <h4>今日日程</h4>
              <div class="event-list">
                <div
                  v-for="event in todayEvents"
                  :key="event.id"
                  class="event-item"
                  :class="{ 'current': event.isCurrent }"
                >
                  <div class="event-time">{{ event.time }}</div>
                  <div class="event-content">
                    <h5>{{ event.title }}</h5>
                    <p>{{ event.description }}</p>
                    <div class="event-attendees">
                      <el-tag v-for="attendee in event.attendees" :key="attendee" size="small">
                        {{ attendee }}
                      </el-tag>
                    </div>
                  </div>
                  <div class="event-actions">
                    <el-button @click="joinMeeting(event)" size="small" type="primary">加入</el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 机器人管理 -->
        <el-tab-pane label="机器人管理" name="bot">
          <div class="bot-section">
            <div class="section-header">
              <h3>机器人管理</h3>
              <el-button @click="showCreateBotDialog = true" type="primary" :icon="Plus">创建机器人</el-button>
            </div>

            <div class="bot-list">
              <div
                v-for="bot in bots"
                :key="bot.id"
                class="bot-card"
                :class="{ 'active': bot.enabled }"
              >
                <div class="bot-header">
                  <div class="bot-info">
                    <div class="bot-avatar">
                      <el-icon><ChatDotRound /></el-icon>
                    </div>
                    <div class="bot-details">
                      <h4>{{ bot.name }}</h4>
                      <p>{{ bot.description }}</p>
                    </div>
                  </div>
                  <div class="bot-status">
                    <el-switch v-model="bot.enabled" @change="toggleBot(bot)" />
                  </div>
                </div>
                
                <div class="bot-stats">
                  <div class="stat">
                    <span class="stat-label">消息处理:</span>
                    <span class="stat-value">{{ bot.messageCount }}</span>
                  </div>
                  <div class="stat">
                    <span class="stat-label">响应时间:</span>
                    <span class="stat-value">{{ bot.responseTime }}ms</span>
                  </div>
                </div>

                <div class="bot-actions">
                  <el-button @click="configureBot(bot)" size="small">配置</el-button>
                  <el-button @click="testBot(bot)" size="small">测试</el-button>
                  <el-button @click="viewBotLogs(bot)" size="small" text>日志</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 创建文档对话框 -->
    <el-dialog v-model="showCreateDocDialog" title="创建文档" width="500px">
      <el-form :model="newDocument" label-width="100px">
        <el-form-item label="文档标题" required>
          <el-input v-model="newDocument.title" placeholder="输入文档标题" />
        </el-form-item>
        <el-form-item label="文档类型">
          <el-select v-model="newDocument.type" placeholder="选择文档类型">
            <el-option label="文档" value="doc" />
            <el-option label="表格" value="sheet" />
            <el-option label="演示文稿" value="presentation" />
          </el-select>
        </el-form-item>
        <el-form-item label="存储位置">
          <el-input v-model="newDocument.folder" placeholder="文件夹ID（可选）" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateDocDialog = false">取消</el-button>
        <el-button @click="createDocument" type="primary" :loading="creatingDoc">创建</el-button>
      </template>
    </el-dialog>

    <!-- 创建会议对话框 -->
    <el-dialog v-model="showCreateEventDialog" title="创建会议" width="600px">
      <el-form :model="newEvent" label-width="100px">
        <el-form-item label="会议标题" required>
          <el-input v-model="newEvent.title" placeholder="输入会议标题" />
        </el-form-item>
        <el-form-item label="开始时间" required>
          <el-date-picker
            v-model="newEvent.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-date-picker
            v-model="newEvent.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="参会人员">
          <el-select v-model="newEvent.attendees" multiple placeholder="选择参会人员">
            <el-option
              v-for="user in feishuUsers"
              :key="user.user_id"
              :label="user.name"
              :value="user.user_id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="会议描述">
          <el-input
            v-model="newEvent.description"
            type="textarea"
            :rows="3"
            placeholder="输入会议描述"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateEventDialog = false">取消</el-button>
        <el-button @click="createEvent" type="primary" :loading="creatingEvent">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ChatDotRound, Document, Calendar, Plus,
  DocumentAdd, Upload, Share
} from '@element-plus/icons-vue'

// 响应式数据
const activeTab = ref('config')
const testing = ref(false)
const saving = ref(false)
const sendingMessage = ref(false)
const creatingDoc = ref(false)
const creatingEvent = ref(false)

const showMessageTemplates = ref(false)
const showCreateDocDialog = ref(false)
const showCreateEventDialog = ref(false)
const showCreateBotDialog = ref(false)

// 连接状态
const connectionStatus = reactive({
  type: 'success',
  text: '已连接'
})

// 配置数据
const config = reactive({
  appId: '',
  appSecret: '',
  domain: 'https://open.feishu.cn',
  webhookUrl: ''
})

// 功能状态
const features = reactive({
  messaging: { enabled: true, todayCount: 25 },
  documents: { enabled: true, count: 12 },
  calendar: { enabled: true, weeklyMeetings: 8 },
  bot: { enabled: false, activeCount: 0 }
})

// 消息表单
const messageForm = reactive({
  receivers: [],
  msgType: 'text',
  content: ''
})

// 新文档
const newDocument = reactive({
  title: '',
  type: 'doc',
  folder: ''
})

// 新会议
const newEvent = reactive({
  title: '',
  startTime: null,
  endTime: null,
  attendees: [],
  description: ''
})

// 模拟数据
const feishuUsers = ref([
  { user_id: 'user1', name: '张三' },
  { user_id: 'user2', name: '李四' },
  { user_id: 'user3', name: '王五' }
])

const messageHistory = ref([
  {
    time: '2025-01-08 14:30',
    receivers: '张三, 李四',
    content: 'QMS系统质量报告已生成',
    status: 'success'
  },
  {
    time: '2025-01-08 13:15',
    receivers: '质量管理群',
    content: '今日审核计划提醒',
    status: 'success'
  }
])

const recentDocuments = ref([
  {
    id: 1,
    title: 'QMS质量管理手册',
    type: '文档',
    creator: '张三',
    updateTime: '2025-01-08 15:30'
  },
  {
    id: 2,
    title: '质量检查记录表',
    type: '表格',
    creator: '李四',
    updateTime: '2025-01-08 14:20'
  }
])

const calendarStats = reactive({
  todayEvents: 3,
  weekEvents: 12,
  upcomingEvents: 1
})

const todayEvents = ref([
  {
    id: 1,
    time: '09:00-10:00',
    title: '质量管理评审会议',
    description: '月度质量管理体系评审',
    attendees: ['张三', '李四', '王五'],
    isCurrent: false
  },
  {
    id: 2,
    time: '14:00-15:30',
    title: '产品质量分析',
    description: '分析本月产品质量数据',
    attendees: ['张三', '李四'],
    isCurrent: true
  },
  {
    id: 3,
    time: '16:00-17:00',
    title: '供应商质量审核',
    description: '对新供应商进行质量审核',
    attendees: ['王五'],
    isCurrent: false
  }
])

const bots = ref([
  {
    id: 1,
    name: 'QMS质量助手',
    description: '自动处理质量相关问题和通知',
    enabled: true,
    messageCount: 156,
    responseTime: 200
  },
  {
    id: 2,
    name: '审核提醒机器人',
    description: '自动发送审核计划和提醒',
    enabled: false,
    messageCount: 0,
    responseTime: 0
  }
])

// 方法定义
const testConnection = async () => {
  testing.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 2000))
    connectionStatus.type = 'success'
    connectionStatus.text = '连接成功'
    ElMessage.success('飞书API连接测试成功')
  } catch (error) {
    connectionStatus.type = 'danger'
    connectionStatus.text = '连接失败'
    ElMessage.error('连接测试失败')
  } finally {
    testing.value = false
  }
}

const saveConfig = async () => {
  saving.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('配置保存成功')
  } catch (error) {
    ElMessage.error('配置保存失败')
  } finally {
    saving.value = false
  }
}

const toggleFeature = (featureName) => {
  const feature = features[featureName]
  ElMessage.success(`${featureName} 功能已${feature.enabled ? '启用' : '禁用'}`)
}

const sendMessage = async () => {
  if (!messageForm.content || messageForm.receivers.length === 0) {
    ElMessage.error('请填写完整的消息信息')
    return
  }

  sendingMessage.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // 添加到历史记录
    messageHistory.value.unshift({
      time: new Date().toLocaleString(),
      receivers: messageForm.receivers.join(', '),
      content: messageForm.content,
      status: 'success'
    })
    
    features.messaging.todayCount++
    ElMessage.success('消息发送成功')
    
    // 重置表单
    messageForm.content = ''
    messageForm.receivers = []
  } catch (error) {
    ElMessage.error('消息发送失败')
  } finally {
    sendingMessage.value = false
  }
}

const previewMessage = () => {
  ElMessage.info('消息预览功能')
}

const createDocument = async () => {
  if (!newDocument.title) {
    ElMessage.error('请输入文档标题')
    return
  }

  creatingDoc.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    recentDocuments.value.unshift({
      id: Date.now(),
      title: newDocument.title,
      type: newDocument.type === 'doc' ? '文档' : newDocument.type === 'sheet' ? '表格' : '演示文稿',
      creator: '当前用户',
      updateTime: new Date().toLocaleString()
    })
    
    features.documents.count++
    ElMessage.success('文档创建成功')
    showCreateDocDialog.value = false
    
    // 重置表单
    Object.assign(newDocument, { title: '', type: 'doc', folder: '' })
  } catch (error) {
    ElMessage.error('文档创建失败')
  } finally {
    creatingDoc.value = false
  }
}

const uploadFile = () => {
  ElMessage.info('文件上传功能')
}

const shareDocument = () => {
  ElMessage.info('文档分享功能')
}

const openDocument = (doc) => {
  ElMessage.info(`打开文档: ${doc.title}`)
}

const createEvent = async () => {
  if (!newEvent.title || !newEvent.startTime || !newEvent.endTime) {
    ElMessage.error('请填写完整的会议信息')
    return
  }

  creatingEvent.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    calendarStats.todayEvents++
    calendarStats.weekEvents++
    ElMessage.success('会议创建成功')
    showCreateEventDialog.value = false
    
    // 重置表单
    Object.assign(newEvent, {
      title: '',
      startTime: null,
      endTime: null,
      attendees: [],
      description: ''
    })
  } catch (error) {
    ElMessage.error('会议创建失败')
  } finally {
    creatingEvent.value = false
  }
}

const joinMeeting = (event) => {
  ElMessage.info(`加入会议: ${event.title}`)
}

const toggleBot = (bot) => {
  ElMessage.success(`机器人 ${bot.name} 已${bot.enabled ? '启用' : '禁用'}`)
  if (bot.enabled) {
    features.bot.activeCount++
  } else {
    features.bot.activeCount--
  }
}

const configureBot = (bot) => {
  ElMessage.info(`配置机器人: ${bot.name}`)
}

const testBot = (bot) => {
  ElMessage.info(`测试机器人: ${bot.name}`)
}

const viewBotLogs = (bot) => {
  ElMessage.info(`查看机器人日志: ${bot.name}`)
}

// 生命周期
onMounted(() => {
  // 初始化
})
</script>

<style scoped>
.feishu-integration {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon img {
  width: 48px;
  height: 48px;
  border-radius: 8px;
}

.header-text h1 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 28px;
  font-weight: 600;
}

.header-text p {
  margin: 0;
  color: #606266;
  font-size: 16px;
}

.features-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.feature-card {
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.feature-card.active {
  border-color: #67c23a;
  background: #f0f9ff;
}

.feature-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

.feature-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #409eff;
  border-radius: 12px;
  margin-bottom: 16px;
}

.feature-icon .el-icon {
  font-size: 24px;
  color: white;
}

.feature-content h3 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 18px;
  font-weight: 600;
}

.feature-content p {
  margin: 0 0 12px 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.feature-stats {
  font-size: 12px;
  color: #909399;
}

.feature-toggle {
  margin-top: 16px;
}

.main-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.config-section,
.messaging-section,
.documents-section,
.calendar-section,
.bot-section {
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 20px;
  font-weight: 600;
}

.document-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.action-card {
  text-align: center;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

.action-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #409eff;
  border-radius: 12px;
  margin: 0 auto 16px;
}

.action-icon .el-icon {
  font-size: 24px;
  color: white;
}

.action-card h4 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}

.action-card p {
  margin: 0 0 16px 0;
  color: #606266;
  font-size: 14px;
}

.calendar-overview {
  margin-bottom: 24px;
}

.calendar-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.event-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.event-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #e4e7ed;
  transition: all 0.3s ease;
}

.event-item.current {
  border-left-color: #67c23a;
  background: #f0f9ff;
}

.event-time {
  font-weight: 600;
  color: #409eff;
  margin-right: 20px;
  min-width: 120px;
}

.event-content {
  flex: 1;
}

.event-content h5 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}

.event-content p {
  margin: 0 0 12px 0;
  color: #606266;
  font-size: 14px;
}

.event-attendees {
  display: flex;
  gap: 8px;
}

.event-actions {
  margin-left: 20px;
}

.bot-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.bot-card {
  padding: 20px;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.bot-card.active {
  border-color: #67c23a;
  background: #f0f9ff;
}

.bot-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.bot-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.bot-avatar {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #409eff;
  border-radius: 8px;
}

.bot-avatar .el-icon {
  font-size: 24px;
  color: white;
}

.bot-details h4 {
  margin: 0 0 4px 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}

.bot-details p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.bot-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
  font-size: 14px;
}

.stat-label {
  color: #606266;
}

.stat-value {
  color: #2c3e50;
  font-weight: 600;
}

.bot-actions {
  display: flex;
  gap: 8px;
}

.message-history,
.document-list,
.today-events {
  margin-top: 24px;
}

.message-history h4,
.document-list h4,
.today-events h4 {
  margin: 0 0 16px 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}
</style>
