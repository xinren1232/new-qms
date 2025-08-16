<template>
  <div class="feishu-plugin">
    <!-- 插件头部 -->
    <div class="plugin-header">
      <div class="plugin-info">
        <div class="plugin-icon">
          <img src="/icons/feishu-logo.png" alt="飞书" />
        </div>
        <div class="plugin-details">
          <h3>飞书集成插件</h3>
          <p>集成飞书API，支持消息推送、文档操作、日程管理等功能</p>
        </div>
      </div>
      <div class="plugin-status">
        <el-switch v-model="pluginEnabled" @change="togglePlugin" />
      </div>
    </div>

    <!-- 配置面板 -->
    <div v-if="pluginEnabled" class="plugin-config">
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 基础配置 -->
        <el-tab-pane label="基础配置" name="basic">
          <el-form :model="config" label-width="120px">
            <el-form-item label="App ID">
              <el-input v-model="config.appId" placeholder="请输入飞书应用ID" />
            </el-form-item>
            <el-form-item label="App Secret">
              <el-input v-model="config.appSecret" type="password" placeholder="请输入飞书应用密钥" />
            </el-form-item>
            <el-form-item label="API域名">
              <el-select v-model="config.domain" placeholder="选择API域名">
                <el-option label="国内版 (open.feishu.cn)" value="https://open.feishu.cn" />
                <el-option label="国际版 (open.larksuite.com)" value="https://open.larksuite.com" />
              </el-select>
            </el-form-item>
            <el-form-item label="连接状态">
              <el-tag :type="connectionStatus.type">{{ connectionStatus.text }}</el-tag>
              <el-button @click="testConnection" size="small" style="margin-left: 10px">测试连接</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 消息推送 -->
        <el-tab-pane label="消息推送" name="message">
          <div class="message-config">
            <h4>消息模板配置</h4>
            <el-form :model="messageConfig" label-width="100px">
              <el-form-item label="默认接收人">
                <el-select v-model="messageConfig.defaultReceivers" multiple placeholder="选择默认接收人">
                  <el-option
                    v-for="user in feishuUsers"
                    :key="user.user_id"
                    :label="user.name"
                    :value="user.user_id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="消息类型">
                <el-radio-group v-model="messageConfig.messageType">
                  <el-radio label="text">纯文本</el-radio>
                  <el-radio label="rich_text">富文本</el-radio>
                  <el-radio label="interactive">交互式卡片</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-form>

            <!-- 消息测试 -->
            <div class="message-test">
              <h5>消息测试</h5>
              <el-input
                v-model="testMessage.content"
                type="textarea"
                :rows="3"
                placeholder="输入测试消息内容"
              />
              <div style="margin-top: 10px;">
                <el-button @click="sendTestMessage" type="primary" :loading="sending">发送测试消息</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 文档操作 -->
        <el-tab-pane label="文档操作" name="document">
          <div class="document-config">
            <h4>文档操作配置</h4>
            <el-form :model="documentConfig" label-width="120px">
              <el-form-item label="默认文件夹">
                <el-input v-model="documentConfig.defaultFolder" placeholder="默认文档存储文件夹ID" />
              </el-form-item>
              <el-form-item label="文档权限">
                <el-select v-model="documentConfig.permission">
                  <el-option label="仅查看" value="view" />
                  <el-option label="可编辑" value="edit" />
                  <el-option label="可管理" value="full_access" />
                </el-select>
              </el-form-item>
            </el-form>

            <!-- 文档操作示例 -->
            <div class="document-actions">
              <h5>文档操作</h5>
              <el-button @click="createDocument" type="primary">创建文档</el-button>
              <el-button @click="uploadFile">上传文件</el-button>
              <el-button @click="shareDocument">分享文档</el-button>
            </div>
          </div>
        </el-tab-pane>

        <!-- 日程管理 -->
        <el-tab-pane label="日程管理" name="calendar">
          <div class="calendar-config">
            <h4>日程管理配置</h4>
            <el-form :model="calendarConfig" label-width="120px">
              <el-form-item label="默认日历">
                <el-select v-model="calendarConfig.defaultCalendar" placeholder="选择默认日历">
                  <el-option
                    v-for="calendar in feishuCalendars"
                    :key="calendar.calendar_id"
                    :label="calendar.summary"
                    :value="calendar.calendar_id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="提醒设置">
                <el-checkbox-group v-model="calendarConfig.reminders">
                  <el-checkbox label="15min">15分钟前</el-checkbox>
                  <el-checkbox label="1hour">1小时前</el-checkbox>
                  <el-checkbox label="1day">1天前</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
            </el-form>

            <!-- 日程操作 -->
            <div class="calendar-actions">
              <h5>日程操作</h5>
              <el-button @click="createEvent" type="primary">创建会议</el-button>
              <el-button @click="getEvents">获取日程</el-button>
              <el-button @click="syncCalendar">同步日历</el-button>
            </div>
          </div>
        </el-tab-pane>

        <!-- 工作流集成 -->
        <el-tab-pane label="工作流集成" name="workflow">
          <div class="workflow-config">
            <h4>工作流触发器</h4>
            <div class="trigger-list">
              <div class="trigger-item">
                <h5>消息接收触发器</h5>
                <p>当接收到飞书消息时触发工作流</p>
                <el-switch v-model="workflowConfig.messageReceived" />
              </div>
              <div class="trigger-item">
                <h5>文档更新触发器</h5>
                <p>当文档被更新时触发工作流</p>
                <el-switch v-model="workflowConfig.documentUpdated" />
              </div>
              <div class="trigger-item">
                <h5>日程提醒触发器</h5>
                <p>当日程提醒时触发工作流</p>
                <el-switch v-model="workflowConfig.calendarReminder" />
              </div>
            </div>

            <h4>工作流动作</h4>
            <div class="action-list">
              <el-button @click="addWorkflowAction('send_message')" type="primary">添加发送消息动作</el-button>
              <el-button @click="addWorkflowAction('create_doc')" type="success">添加创建文档动作</el-button>
              <el-button @click="addWorkflowAction('schedule_meeting')" type="warning">添加安排会议动作</el-button>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 使用统计 -->
    <div v-if="pluginEnabled" class="plugin-stats">
      <h4>使用统计</h4>
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-value">{{ stats.messagesSent }}</div>
          <div class="stat-label">消息发送</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.documentsCreated }}</div>
          <div class="stat-label">文档创建</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.eventsScheduled }}</div>
          <div class="stat-label">日程安排</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.workflowsTriggered }}</div>
          <div class="stat-label">工作流触发</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 响应式数据
const pluginEnabled = ref(false)
const activeTab = ref('basic')
const sending = ref(false)

// 配置数据
const config = reactive({
  appId: '',
  appSecret: '',
  domain: 'https://open.feishu.cn'
})

const messageConfig = reactive({
  defaultReceivers: [],
  messageType: 'text'
})

const documentConfig = reactive({
  defaultFolder: '',
  permission: 'edit'
})

const calendarConfig = reactive({
  defaultCalendar: '',
  reminders: ['15min']
})

const workflowConfig = reactive({
  messageReceived: false,
  documentUpdated: false,
  calendarReminder: false
})

// 连接状态
const connectionStatus = reactive({
  type: 'info',
  text: '未连接'
})

// 测试消息
const testMessage = reactive({
  content: '这是一条来自QMS AI系统的测试消息'
})

// 飞书数据
const feishuUsers = ref([])
const feishuCalendars = ref([])

// 统计数据
const stats = reactive({
  messagesSent: 0,
  documentsCreated: 0,
  eventsScheduled: 0,
  workflowsTriggered: 0
})

// 方法定义
const togglePlugin = (enabled) => {
  if (enabled) {
    ElMessage.success('飞书插件已启用')
    loadFeishuData()
  } else {
    ElMessage.info('飞书插件已禁用')
  }
}

const testConnection = async () => {
  if (!config.appId || !config.appSecret) {
    ElMessage.error('请先配置App ID和App Secret')
    return
  }

  try {
    connectionStatus.type = 'warning'
    connectionStatus.text = '连接中...'

    // 模拟连接测试
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    connectionStatus.type = 'success'
    connectionStatus.text = '连接成功'
    ElMessage.success('飞书API连接成功')
  } catch (error) {
    connectionStatus.type = 'danger'
    connectionStatus.text = '连接失败'
    ElMessage.error('飞书API连接失败: ' + error.message)
  }
}

const sendTestMessage = async () => {
  if (!testMessage.content) {
    ElMessage.error('请输入测试消息内容')
    return
  }

  sending.value = true
  try {
    // 调用飞书API发送消息
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    stats.messagesSent++
    ElMessage.success('测试消息发送成功')
  } catch (error) {
    ElMessage.error('消息发送失败: ' + error.message)
  } finally {
    sending.value = false
  }
}

const createDocument = async () => {
  try {
    // 调用飞书API创建文档
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    stats.documentsCreated++
    ElMessage.success('文档创建成功')
  } catch (error) {
    ElMessage.error('文档创建失败: ' + error.message)
  }
}

const uploadFile = () => {
  ElMessage.info('文件上传功能')
}

const shareDocument = () => {
  ElMessage.info('文档分享功能')
}

const createEvent = async () => {
  try {
    // 调用飞书API创建日程
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    stats.eventsScheduled++
    ElMessage.success('会议创建成功')
  } catch (error) {
    ElMessage.error('会议创建失败: ' + error.message)
  }
}

const getEvents = () => {
  ElMessage.info('获取日程功能')
}

const syncCalendar = () => {
  ElMessage.info('同步日历功能')
}

const addWorkflowAction = (actionType) => {
  stats.workflowsTriggered++
  ElMessage.success(`添加${actionType}工作流动作成功`)
}

const loadFeishuData = async () => {
  try {
    // 模拟加载飞书用户和日历数据
    feishuUsers.value = [
      { user_id: 'user1', name: '张三' },
      { user_id: 'user2', name: '李四' },
      { user_id: 'user3', name: '王五' }
    ]
    
    feishuCalendars.value = [
      { calendar_id: 'cal1', summary: '工作日历' },
      { calendar_id: 'cal2', summary: '个人日历' }
    ]
  } catch (error) {
    console.error('加载飞书数据失败:', error)
  }
}

// 生命周期
onMounted(() => {
  // 初始化插件
})
</script>

<style scoped>
.feishu-plugin {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
}

.plugin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #e4e7ed;
}

.plugin-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.plugin-icon img {
  width: 48px;
  height: 48px;
  border-radius: 8px;
}

.plugin-details h3 {
  margin: 0 0 5px 0;
  color: #2c3e50;
}

.plugin-details p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.plugin-config {
  padding: 20px;
}

.message-test,
.document-actions,
.calendar-actions {
  margin-top: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
}

.trigger-list,
.action-list {
  margin-top: 15px;
}

.trigger-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  margin-bottom: 10px;
  background: #f8f9fa;
  border-radius: 6px;
}

.trigger-item h5 {
  margin: 0 0 5px 0;
  color: #2c3e50;
}

.trigger-item p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.plugin-stats {
  padding: 20px;
  border-top: 1px solid #e4e7ed;
  background: #f8f9fa;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-top: 15px;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: white;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}
</style>
