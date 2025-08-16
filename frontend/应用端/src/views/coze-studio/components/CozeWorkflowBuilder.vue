<template>
  <div class="coze-workflow-builder">
    <!-- 工具栏 -->
    <div class="workflow-toolbar">
      <div class="toolbar-left">
        <h3>{{ project?.name || '新建工作流' }}</h3>
        <el-tag v-if="project?.status" :type="getStatusType(project.status)">
          {{ getStatusText(project.status) }}
        </el-tag>
      </div>
      <div class="toolbar-center">
        <el-button-group>
          <el-button :icon="ZoomIn" @click="zoomIn">放大</el-button>
          <el-button @click="resetZoom">{{ Math.round(zoomLevel * 100) }}%</el-button>
          <el-button :icon="ZoomOut" @click="zoomOut">缩小</el-button>
        </el-button-group>
      </div>
      <div class="toolbar-right">
        <el-button :icon="VideoPlay" @click="executeWorkflow">执行</el-button>
        <el-button :icon="Upload" @click="publishWorkflow">发布</el-button>
        <el-button :icon="Download" type="primary" @click="saveWorkflow">保存</el-button>
      </div>
    </div>
    
    <!-- 主要构建区域 -->
    <div class="workflow-main">
      <!-- 左侧节点面板 -->
      <div class="nodes-panel">
        <!-- 搜索框 -->
        <div class="panel-search">
          <el-input
            v-model="nodeSearchQuery"
            placeholder="搜索节点、插件、工作流"
            :prefix-icon="Search"
            size="small"
            clearable
          />
        </div>

        <el-scrollbar>
          <!-- 大模型 -->
          <div class="panel-section">
            <div class="section-header">
              <el-icon class="section-icon"><Cpu /></el-icon>
              <h4>大模型</h4>
            </div>
            <div class="node-list">
              <div
                v-for="node in filteredNodes.llmNodes"
                :key="node.type"
                class="node-item"
                draggable="true"
                @dragstart="handleNodeDragStart($event, node)"
              >
                <div class="node-icon llm-node">
                  <el-icon><component :is="node.icon" /></el-icon>
                </div>
                <div class="node-info">
                  <div class="node-name">{{ node.name }}</div>
                  <div class="node-desc">{{ node.description }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 工作流 -->
          <div class="panel-section">
            <div class="section-header">
              <el-icon class="section-icon"><Connection /></el-icon>
              <h4>工作流</h4>
            </div>
            <div class="node-list">
              <div
                v-for="node in filteredNodes.workflowNodes"
                :key="node.type"
                class="node-item"
                draggable="true"
                @dragstart="handleNodeDragStart($event, node)"
              >
                <div class="node-icon workflow-node">
                  <el-icon><component :is="node.icon" /></el-icon>
                </div>
                <div class="node-info">
                  <div class="node-name">{{ node.name }}</div>
                  <div class="node-desc">{{ node.description }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 业务逻辑 -->
          <div class="panel-section">
            <div class="section-header">
              <h4>业务逻辑</h4>
            </div>
            <div class="node-list">
              <div
                v-for="node in filteredNodes.logicNodes"
                :key="node.type"
                class="node-item"
                draggable="true"
                @dragstart="handleNodeDragStart($event, node)"
              >
                <div class="node-icon logic-node">
                  <el-icon><component :is="node.icon" /></el-icon>
                </div>
                <div class="node-info">
                  <div class="node-name">{{ node.name }}</div>
                  <div class="node-desc">{{ node.description }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 输入&输出 -->
          <div class="panel-section">
            <div class="section-header">
              <h4>输入&输出</h4>
            </div>
            <div class="node-list">
              <div
                v-for="node in filteredNodes.ioNodes"
                :key="node.type"
                class="node-item"
                draggable="true"
                @dragstart="handleNodeDragStart($event, node)"
              >
                <div class="node-icon io-node">
                  <el-icon><component :is="node.icon" /></el-icon>
                </div>
                <div class="node-info">
                  <div class="node-name">{{ node.name }}</div>
                  <div class="node-desc">{{ node.description }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 数据库 -->
          <div class="panel-section">
            <div class="section-header">
              <h4>数据库</h4>
            </div>
            <div class="node-list">
              <div
                v-for="node in filteredNodes.databaseNodes"
                :key="node.type"
                class="node-item"
                draggable="true"
                @dragstart="handleNodeDragStart($event, node)"
              >
                <div class="node-icon database-node">
                  <el-icon><component :is="node.icon" /></el-icon>
                </div>
                <div class="node-info">
                  <div class="node-name">{{ node.name }}</div>
                  <div class="node-desc">{{ node.description }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 知识库&数据 -->
          <div class="panel-section">
            <div class="section-header">
              <h4>知识库&数据</h4>
            </div>
            <div class="node-list">
              <div
                v-for="node in filteredNodes.knowledgeNodes"
                :key="node.type"
                class="node-item"
                draggable="true"
                @dragstart="handleNodeDragStart($event, node)"
              >
                <div class="node-icon knowledge-node">
                  <el-icon><component :is="node.icon" /></el-icon>
                </div>
                <div class="node-info">
                  <div class="node-name">{{ node.name }}</div>
                  <div class="node-desc">{{ node.description }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 图像处理 -->
          <div class="panel-section">
            <div class="section-header">
              <h4>图像处理</h4>
            </div>
            <div class="node-list">
              <div
                v-for="node in filteredNodes.imageNodes"
                :key="node.type"
                class="node-item"
                draggable="true"
                @dragstart="handleNodeDragStart($event, node)"
              >
                <div class="node-icon image-node">
                  <el-icon><component :is="node.icon" /></el-icon>
                </div>
                <div class="node-info">
                  <div class="node-name">{{ node.name }}</div>
                  <div class="node-desc">{{ node.description }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 通知 -->
          <div class="panel-section">
            <div class="section-header">
              <h4>通知</h4>
            </div>
            <div class="node-list">
              <div
                v-for="node in filteredNodes.notificationNodes"
                :key="node.type"
                class="node-item"
                draggable="true"
                @dragstart="handleNodeDragStart($event, node)"
              >
                <div class="node-icon notification-node">
                  <el-icon><component :is="node.icon" /></el-icon>
                </div>
                <div class="node-info">
                  <div class="node-name">{{ node.name }}</div>
                  <div class="node-desc">{{ node.description }}</div>
                </div>
              </div>
            </div>
          </div>
        </el-scrollbar>
      </div>
      
      <!-- 中间画布区域 -->
      <div class="canvas-container">
        <div
          ref="canvasRef"
          class="workflow-canvas"
          :style="{ transform: `scale(${zoomLevel})` }"
          @drop="handleCanvasDrop"
          @dragover.prevent
          @click="handleCanvasClick"
          @mousemove="handleCanvasMouseMove"
          @mouseup="handleCanvasMouseUp"
        >
          <!-- 网格背景 -->
          <div class="canvas-grid"></div>
          
          <!-- 工作流节点 -->
          <div
            v-for="node in workflowNodes"
            :key="node.id"
            :class="['workflow-node', node.type, { selected: selectedNode?.id === node.id }]"
            :style="{
              left: node.x + 'px',
              top: node.y + 'px'
            }"
            @click.stop="selectNode(node)"
            @mousedown="startDrag($event, node)"
          >
            <div class="node-header">
              <div class="node-icon">
                <el-icon>
                  <component :is="getNodeIcon(node.type)" />
                </el-icon>
              </div>
              <div class="node-title">{{ node.name }}</div>
              <div class="node-actions">
                <el-button
                  :icon="Setting"
                  size="small"
                  circle
                  @click.stop="configureNode(node)"
                />
                <el-button
                  :icon="Delete"
                  size="small"
                  circle
                  type="danger"
                  @click.stop="deleteNode(node)"
                />
              </div>
            </div>
            
            <!-- 输入端口 -->
            <div class="input-ports">
              <div
                v-for="input in node.inputs"
                :key="input.name"
                class="port input-port"
                :data-port="input.name"
                :data-node-id="node.id"
                @mousedown.stop="startConnection($event, node, input, 'input')"
                @mouseup.stop="endConnection($event, node, input, 'input')"
                @mouseenter="highlightPort($event, true)"
                @mouseleave="highlightPort($event, false)"
              >
                <div class="port-dot"></div>
                <span class="port-label">{{ input.label }}</span>
              </div>
            </div>

            <!-- 输出端口 -->
            <div class="output-ports">
              <div
                v-for="output in node.outputs"
                :key="output.name"
                class="port output-port"
                :data-port="output.name"
                :data-node-id="node.id"
                @mousedown.stop="startConnection($event, node, output, 'output')"
                @mouseup.stop="endConnection($event, node, output, 'output')"
                @mouseenter="highlightPort($event, true)"
                @mouseleave="highlightPort($event, false)"
              >
                <span class="port-label">{{ output.label }}</span>
                <div class="port-dot"></div>
              </div>
            </div>
          </div>
          
          <!-- 连接线 -->
          <svg class="connections-layer">
            <g v-for="connection in connections" :key="connection.id">
              <path
                :d="getConnectionPath(connection)"
                class="connection-line"
                :class="{ selected: selectedConnection?.id === connection.id }"
                @click="selectConnection(connection)"
              />
              <!-- 连接线上的删除按钮 -->
              <g v-if="selectedConnection?.id === connection.id" class="connection-controls">
                <circle
                  :cx="getConnectionMidPoint(connection).x"
                  :cy="getConnectionMidPoint(connection).y"
                  r="8"
                  fill="#ff4757"
                  class="delete-connection-btn"
                  @click.stop="deleteConnection(connection)"
                />
                <text
                  :x="getConnectionMidPoint(connection).x"
                  :y="getConnectionMidPoint(connection).y + 1"
                  text-anchor="middle"
                  fill="white"
                  font-size="10"
                  pointer-events="none"
                >×</text>
              </g>
            </g>
            
            <!-- 临时连接线 -->
            <path
              v-if="tempConnection"
              :d="tempConnection.path"
              class="temp-connection-line"
            />
          </svg>
        </div>
      </div>
      
      <!-- 右侧属性面板 -->
      <div class="properties-panel">
        <div class="panel-header">
          <h4>属性配置</h4>
        </div>
        
        <div v-if="selectedNode" class="node-properties">
          <el-form :model="selectedNode" label-width="80px" size="small">
            <el-form-item label="节点名称">
              <el-input v-model="selectedNode.name" />
            </el-form-item>
            <el-form-item label="描述">
              <el-input
                v-model="selectedNode.description"
                type="textarea"
                :rows="3"
              />
            </el-form-item>
            
            <!-- 根据节点类型显示不同配置 -->
            <template v-if="selectedNode.type === 'llm'">
              <el-form-item label="模型">
                <el-select v-model="selectedNode.config.model">
                  <el-option label="GPT-4o" value="gpt-4o" />
                  <el-option label="Claude-3" value="claude-3" />
                  <el-option label="DeepSeek" value="deepseek" />
                </el-select>
              </el-form-item>
              <el-form-item label="提示词">
                <el-input
                  v-model="selectedNode.config.prompt"
                  type="textarea"
                  :rows="4"
                />
              </el-form-item>
              <el-form-item label="温度">
                <el-slider
                  v-model="selectedNode.config.temperature"
                  :min="0"
                  :max="2"
                  :step="0.1"
                  show-input
                />
              </el-form-item>
            </template>
            
            <template v-if="selectedNode.type === 'condition'">
              <el-form-item label="条件表达式">
                <el-input
                  v-model="selectedNode.config.condition"
                  placeholder="例如: input.length > 10"
                />
              </el-form-item>
            </template>
            
            <template v-if="selectedNode.type === 'http'">
              <el-form-item label="请求方法">
                <el-select v-model="selectedNode.config.method">
                  <el-option label="GET" value="GET" />
                  <el-option label="POST" value="POST" />
                  <el-option label="PUT" value="PUT" />
                  <el-option label="DELETE" value="DELETE" />
                </el-select>
              </el-form-item>
              <el-form-item label="URL">
                <el-input v-model="selectedNode.config.url" />
              </el-form-item>
              <el-form-item label="请求头">
                <el-input
                  v-model="selectedNode.config.headers"
                  type="textarea"
                  :rows="3"
                  placeholder="JSON格式"
                />
              </el-form-item>
            </template>
          </el-form>
        </div>
        
        <div v-else class="no-selection">
          <el-empty description="请选择一个节点进行配置" />
        </div>
      </div>
    </div>
    
    <!-- 节点配置对话框 -->
    <el-dialog
      v-model="showNodeConfig"
      :title="`配置 ${configNode?.name}`"
      width="600px"
    >
      <div v-if="configNode">
        <!-- 这里可以放置更详细的节点配置界面 -->
        <p>节点详细配置界面</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick } from 'vue'
import {
  ZoomIn,
  ZoomOut,
  VideoPlay,
  Upload,
  Download,
  Setting,
  Delete,
  Plus,
  Connection,
  Document,
  Tools,
  Search,
  DataAnalysis,
  ChatDotRound,
  Cpu,
  Grid,
  Switch,
  Refresh,
  Check,
  Edit,
  Picture,
  Bell,
  Message,
  Phone,
  Coin,
  FolderOpened,
  Files,
  Rank,
  ArrowRight,
  DataLine,
  Folder
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// Props
const props = defineProps({
  project: {
    type: Object,
    default: null
  }
})

// Emits
const emit = defineEmits(['save', 'execute'])

// 响应式数据
const canvasRef = ref(null)
const zoomLevel = ref(1)
const selectedNode = ref(null)
const selectedConnection = ref(null)
const showNodeConfig = ref(false)
const configNode = ref(null)
const tempConnection = ref(null)
const isDragging = ref(false)
const dragOffset = ref({ x: 0, y: 0 })
const nodeSearchQuery = ref('')
const isConnecting = ref(false)
const connectionStart = ref(null)
const mousePosition = ref({ x: 0, y: 0 })

// 工作流数据
const workflowNodes = reactive([
  {
    id: 'start',
    type: 'start',
    name: '开始',
    x: 100,
    y: 100,
    inputs: [],
    outputs: [{ name: 'output', label: '输出' }],
    config: {}
  }
])

const connections = reactive([])

// 完整的节点类型定义
const allNodes = {
  // 大模型
  llmNodes: [
    { type: 'llm', name: 'LLM对话', description: '大语言模型对话', icon: ChatDotRound },
    { type: 'embedding', name: '文本嵌入', description: '生成文本向量', icon: DataAnalysis },
    { type: 'classification', name: '文本分类', description: '分类文本内容', icon: Grid },
    { type: 'summarization', name: '文本摘要', description: '生成内容摘要', icon: Document }
  ],

  // 工作流
  workflowNodes: [
    { type: 'start', name: '开始', description: '工作流起始点', icon: VideoPlay },
    { type: 'end', name: '结束', description: '工作流结束点', icon: Check },
    { type: 'condition', name: '条件判断', description: '根据条件分支', icon: Switch },
    { type: 'loop', name: '循环', description: '重复执行', icon: Refresh }
  ],

  // 业务逻辑
  logicNodes: [
    { type: 'code', name: '代码', description: '执行自定义代码', icon: Document },
    { type: 'intent', name: '意图识别', description: '识别用户意图', icon: ChatDotRound },
    { type: 'batch', name: '批处理', description: '批量处理数据', icon: Grid }
  ],

  // 输入&输出
  ioNodes: [
    { type: 'input', name: '输入', description: '接收输入数据', icon: Download },
    { type: 'output', name: '输出', description: '输出处理结果', icon: Upload }
  ],

  // 数据库
  databaseNodes: [
    { type: 'sql', name: 'SQL查询', description: 'SQL数据查询', icon: DataLine },
    { type: 'insert', name: '新增数据', description: '插入新数据', icon: Plus },
    { type: 'update', name: '更新数据', description: '更新现有数据', icon: Edit },
    { type: 'delete', name: '删除数据', description: '删除指定数据', icon: Delete }
  ],

  // 知识库&数据
  knowledgeNodes: [
    { type: 'knowledge_search', name: '知识库查询', description: '搜索知识库内容', icon: Search },
    { type: 'knowledge_insert', name: '知识库写入', description: '写入知识库数据', icon: Folder },
    { type: 'knowledge_delete', name: '知识库删除', description: '删除知识库内容', icon: Delete },
    { type: 'variable_set', name: '变量赋值', description: '设置变量值', icon: Edit }
  ],

  // 图像处理
  imageNodes: [
    { type: 'image_generate', name: '图像生成', description: 'AI生成图像', icon: Picture },
    { type: 'image_recognize', name: '图像识别', description: '识别图像内容', icon: Search },
    { type: 'image_edit', name: '图像编辑', description: '编辑处理图像', icon: Edit }
  ],

  // 通知
  notificationNodes: [
    { type: 'message', name: '消息', description: '发送消息通知', icon: Message },
    { type: 'email', name: '邮件', description: '发送邮件通知', icon: Bell },
    { type: 'sms', name: '短信', description: '发送短信通知', icon: Phone }
  ]
}

// 计算属性
const filteredNodes = computed(() => {
  if (!nodeSearchQuery.value) {
    return allNodes
  }

  const query = nodeSearchQuery.value.toLowerCase()
  const filtered = {}

  Object.keys(allNodes).forEach(category => {
    filtered[category] = allNodes[category].filter(node =>
      node.name.toLowerCase().includes(query) ||
      node.description.toLowerCase().includes(query)
    )
  })

  return filtered
})

const getStatusType = (status) => {
  const typeMap = {
    draft: 'info',
    published: 'success',
    running: 'warning'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    draft: '草稿',
    published: '已发布',
    running: '运行中'
  }
  return textMap[status] || '未知'
}

// 方法
const zoomIn = () => {
  zoomLevel.value = Math.min(zoomLevel.value + 0.1, 2)
}

const zoomOut = () => {
  zoomLevel.value = Math.max(zoomLevel.value - 0.1, 0.5)
}

const resetZoom = () => {
  zoomLevel.value = 1
}

const handleNodeDragStart = (event, nodeTemplate) => {
  event.dataTransfer.setData('application/json', JSON.stringify(nodeTemplate))
}

const handleCanvasDrop = (event) => {
  event.preventDefault()
  const nodeTemplate = JSON.parse(event.dataTransfer.getData('application/json'))
  
  const rect = canvasRef.value.getBoundingClientRect()
  const x = (event.clientX - rect.left) / zoomLevel.value
  const y = (event.clientY - rect.top) / zoomLevel.value
  
  createNode(nodeTemplate, x, y)
}

const createNode = (template, x, y) => {
  const newNode = {
    id: `node_${Date.now()}`,
    type: template.type,
    name: template.name,
    x: x - 75, // 居中偏移
    y: y - 40,
    inputs: getNodeInputs(template.type),
    outputs: getNodeOutputs(template.type),
    config: getDefaultConfig(template.type)
  }
  
  workflowNodes.push(newNode)
  selectedNode.value = newNode
}

const getNodeInputs = (type) => {
  const inputMap = {
    start: [],
    end: [{ name: 'input', label: '输入' }],
    condition: [{ name: 'input', label: '输入' }],
    llm: [{ name: 'prompt', label: '提示词' }, { name: 'context', label: '上下文' }],
    http: [{ name: 'data', label: '数据' }]
  }
  return inputMap[type] || [{ name: 'input', label: '输入' }]
}

const getNodeOutputs = (type) => {
  const outputMap = {
    start: [{ name: 'output', label: '输出' }],
    end: [],
    condition: [{ name: 'true', label: '真' }, { name: 'false', label: '假' }],
    llm: [{ name: 'response', label: '回复' }],
    http: [{ name: 'response', label: '响应' }, { name: 'error', label: '错误' }]
  }
  return outputMap[type] || [{ name: 'output', label: '输出' }]
}

const getDefaultConfig = (type) => {
  const configMap = {
    llm: { model: 'gpt-4o', prompt: '', temperature: 0.7 },
    condition: { condition: '' },
    http: { method: 'GET', url: '', headers: '{}' }
  }
  return configMap[type] || {}
}

const getNodeIcon = (type) => {
  const iconMap = {
    // 工作流
    start: VideoPlay,
    end: Check,
    condition: Switch,
    loop: Refresh,

    // 大模型
    llm: ChatDotRound,
    embedding: DataAnalysis,
    classification: Grid,
    summarization: Document,

    // 业务逻辑
    code: Document,
    intent: ChatDotRound,
    batch: Grid,

    // 输入输出
    input: Download,
    output: Upload,

    // 数据库
    sql: DataLine,
    insert: Plus,
    update: Edit,
    delete: Delete,

    // 知识库
    knowledge_search: Search,
    knowledge_insert: Folder,
    knowledge_delete: Delete,
    variable_set: Edit,

    // 图像处理
    image_generate: Picture,
    image_recognize: Search,
    image_edit: Edit,

    // 通知
    message: Message,
    email: Bell,
    sms: Phone,

    // 其他
    http: Connection,
    database: Coin,
    file: Document,
    search: Search
  }
  return iconMap[type] || Tools
}

const selectNode = (node) => {
  selectedNode.value = node
  selectedConnection.value = null
}

const selectConnection = (connection) => {
  selectedConnection.value = connection
  selectedNode.value = null
}

const handleCanvasClick = () => {
  selectedNode.value = null
  selectedConnection.value = null
}

const startDrag = (event, node) => {
  isDragging.value = true
  dragOffset.value = {
    x: event.clientX - node.x,
    y: event.clientY - node.y
  }
  
  const handleMouseMove = (e) => {
    if (isDragging.value) {
      node.x = e.clientX - dragOffset.value.x
      node.y = e.clientY - dragOffset.value.y
    }
  }
  
  const handleMouseUp = () => {
    isDragging.value = false
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', handleMouseUp)
  }
  
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}

const configureNode = (node) => {
  configNode.value = node
  showNodeConfig.value = true
}

const deleteNode = (node) => {
  const index = workflowNodes.findIndex(n => n.id === node.id)
  if (index > -1) {
    workflowNodes.splice(index, 1)
    
    // 删除相关连接
    const relatedConnections = connections.filter(
      c => c.fromNode === node.id || c.toNode === node.id
    )
    relatedConnections.forEach(c => {
      const connIndex = connections.findIndex(conn => conn.id === c.id)
      if (connIndex > -1) {
        connections.splice(connIndex, 1)
      }
    })
    
    if (selectedNode.value?.id === node.id) {
      selectedNode.value = null
    }
  }
}

const getConnectionPath = (connection) => {
  const fromNode = workflowNodes.find(n => n.id === connection.fromNode)
  const toNode = workflowNodes.find(n => n.id === connection.toNode)
  
  if (!fromNode || !toNode) return ''
  
  const fromX = fromNode.x + 150
  const fromY = fromNode.y + 40
  const toX = toNode.x
  const toY = toNode.y + 40
  
  const midX = (fromX + toX) / 2
  
  return `M ${fromX} ${fromY} C ${midX} ${fromY} ${midX} ${toY} ${toX} ${toY}`
}

const executeWorkflow = async () => {
  try {
    ElMessage.info('正在执行工作流...')

    // 构建工作流定义
    const workflow = {
      id: `workflow_${Date.now()}`,
      name: '测试工作流',
      nodes: workflowNodes.map(node => ({
        id: node.id,
        type: node.type,
        name: node.name,
        config: node.config || {},
        position: { x: node.x, y: node.y }
      })),
      edges: connections.map(conn => ({
        from: conn.fromNode,
        to: conn.toNode,
        fromOutput: conn.fromOutput,
        toInput: conn.toInput
      }))
    }

    // 调用Coze Studio统一API客户端
    const cozeStudioAPI = (await import('@/api/coze-studio')).default
    const response = await cozeStudioAPI.executeWorkflow(workflow.id, {
      workflow,
      input: { message: '测试输入' },
      options: { timeout: 60000 }
    })

    // 兼容 axios 封装和 fetch Response 两种返回结构
    let resultData = response?.data || response
    if (response && typeof response.json === 'function') {
      try {
        resultData = await response.json()
      } catch (e) {
        // 非 fetch Response 或解析失败时保留原值
      }
    }

    if (resultData && resultData.success) {
      ElMessage.success(`工作流执行成功！执行ID: ${resultData.data?.executionId || '未知'}`)

      // 监听执行状态更新
      if (resultData.data?.executionId) {
        pollExecutionStatus(resultData.data.executionId)
      }
    } else {
      ElMessage.error(`工作流执行失败: ${resultData?.message || '未知错误'}`)
    }

    emit('execute', { workflow, result: resultData })
  } catch (error) {
    console.error('工作流执行错误:', error)
    ElMessage.error('工作流执行失败')
  }
}

// 轮询执行状态
const pollExecutionStatus = async (executionId) => {
  const maxAttempts = 30 // 最多轮询30次
  let attempts = 0

  const poll = async () => {
    try {
      const response = await fetch(`/api/workflows/execution/${executionId}`)
      const result = await response.json()

      if (result.success) {
        const execution = result.data

        if (execution.status === 'completed') {
          ElMessage.success('工作流执行完成！')
          console.log('执行结果:', execution.output)
          return
        } else if (execution.status === 'failed') {
          ElMessage.error(`工作流执行失败: ${execution.error}`)
          return
        } else if (execution.status === 'running' && attempts < maxAttempts) {
          attempts++
          setTimeout(poll, 2000) // 2秒后再次轮询
        }
      }
    } catch (error) {
      console.error('轮询执行状态失败:', error)
    }
  }

  poll()
}

const publishWorkflow = () => {
  ElMessage.success('工作流发布成功')
}

const saveWorkflow = () => {
  emit('save', { nodes: workflowNodes, connections })
}

// 连接相关方法
const startConnection = (event, node, port, type) => {
  event.preventDefault()
  event.stopPropagation()

  if (type === 'input') return // 只能从输出端口开始连接

  isConnecting.value = true
  connectionStart.value = {
    node: node,
    port: port,
    type: type,
    element: event.target.closest('.port')
  }

  // 更新鼠标位置
  updateMousePosition(event)

  // 创建临时连接线
  updateTempConnection()
}

const endConnection = (event, node, port, type) => {
  if (!isConnecting.value || !connectionStart.value) return

  event.preventDefault()
  event.stopPropagation()

  // 只能连接到输入端口
  if (type !== 'input') {
    cancelConnection()
    return
  }

  // 不能连接到同一个节点
  if (connectionStart.value.node.id === node.id) {
    cancelConnection()
    return
  }

  // 检查是否已存在连接
  const existingConnection = connections.find(conn =>
    conn.fromNode === connectionStart.value.node.id &&
    conn.fromPort === connectionStart.value.port.name &&
    conn.toNode === node.id &&
    conn.toPort === port.name
  )

  if (existingConnection) {
    ElMessage.warning('连接已存在')
    cancelConnection()
    return
  }

  // 创建新连接
  const newConnection = {
    id: `connection_${Date.now()}`,
    fromNode: connectionStart.value.node.id,
    fromPort: connectionStart.value.port.name,
    toNode: node.id,
    toPort: port.name
  }

  connections.push(newConnection)
  ElMessage.success('连接创建成功')

  // 重置连接状态
  cancelConnection()
}

const cancelConnection = () => {
  isConnecting.value = false
  connectionStart.value = null
  tempConnection.value = null
}

const handleCanvasMouseMove = (event) => {
  if (isConnecting.value) {
    updateMousePosition(event)
    updateTempConnection()
  }
}

const handleCanvasMouseUp = (event) => {
  if (isConnecting.value) {
    cancelConnection()
  }
}

const updateMousePosition = (event) => {
  const rect = canvasRef.value.getBoundingClientRect()
  mousePosition.value = {
    x: (event.clientX - rect.left) / zoomLevel.value,
    y: (event.clientY - rect.top) / zoomLevel.value
  }
}

const updateTempConnection = () => {
  if (!connectionStart.value) return

  const fromNode = connectionStart.value.node
  const fromX = fromNode.x + 150
  const fromY = fromNode.y + 40
  const toX = mousePosition.value.x
  const toY = mousePosition.value.y

  const midX = (fromX + toX) / 2

  tempConnection.value = {
    path: `M ${fromX} ${fromY} C ${midX} ${fromY} ${midX} ${toY} ${toX} ${toY}`
  }
}

const highlightPort = (event, highlight) => {
  const portElement = event.target.closest('.port')
  if (portElement) {
    if (highlight) {
      portElement.classList.add('highlighted')
    } else {
      portElement.classList.remove('highlighted')
    }
  }
}

// 删除连接
const deleteConnection = (connection) => {
  const index = connections.findIndex(c => c.id === connection.id)
  if (index > -1) {
    connections.splice(index, 1)
    if (selectedConnection.value?.id === connection.id) {
      selectedConnection.value = null
    }
  }
}

// 获取连接线中点
const getConnectionMidPoint = (connection) => {
  const fromNode = workflowNodes.find(n => n.id === connection.fromNode)
  const toNode = workflowNodes.find(n => n.id === connection.toNode)

  if (!fromNode || !toNode) return { x: 0, y: 0 }

  const fromX = fromNode.x + 150
  const fromY = fromNode.y + 40
  const toX = toNode.x
  const toY = toNode.y + 40

  return {
    x: (fromX + toX) / 2,
    y: (fromY + toY) / 2
  }
}

// 键盘事件处理
const handleKeyDown = (event) => {
  if (event.key === 'Delete' || event.key === 'Backspace') {
    if (selectedNode.value) {
      deleteNode(selectedNode.value)
    } else if (selectedConnection.value) {
      deleteConnection(selectedConnection.value)
    }
  } else if (event.key === 'Escape') {
    if (isConnecting.value) {
      cancelConnection()
    } else {
      selectedNode.value = null
      selectedConnection.value = null
    }
  }
}

// 组件挂载时添加键盘事件监听
onMounted(() => {
  document.addEventListener('keydown', handleKeyDown)
})

// 组件卸载时移除键盘事件监听
onUnmounted(() => {
  document.removeEventListener('keydown', handleKeyDown)
})
</script>

<style lang="scss" scoped>
.coze-workflow-builder {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  .workflow-toolbar {
    height: 60px;
    padding: 0 20px;
    border-bottom: 1px solid #e4e7ed;
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: white;
    
    .toolbar-left {
      display: flex;
      align-items: center;
      gap: 12px;
      
      h3 {
        margin: 0;
        font-size: 16px;
        color: #1f2937;
      }
    }
    
    .toolbar-center {
      display: flex;
      align-items: center;
    }
    
    .toolbar-right {
      display: flex;
      gap: 8px;
    }
  }
  
  .workflow-main {
    flex: 1;
    display: flex;
    overflow: hidden;
    
    .nodes-panel {
      width: 280px;
      border-right: 1px solid #e4e7ed;
      background: #f9fafb;
      display: flex;
      flex-direction: column;

      .panel-search {
        padding: 16px;
        border-bottom: 1px solid #e4e7ed;
        background: white;
      }

      .panel-section {
        padding: 16px;
        border-bottom: 1px solid #e4e7ed;

        .section-header {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 12px;

          .section-icon {
            color: #6b7280;
          }

          h4 {
            margin: 0;
            font-size: 14px;
            font-weight: 600;
            color: #374151;
          }
        }

        .node-list {
          .node-item {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 12px;
            background: white;
            border-radius: 6px;
            margin-bottom: 8px;
            cursor: grab;
            transition: all 0.2s ease;
            border: 1px solid #e5e7eb;

            &:hover {
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
              transform: translateY(-1px);
            }

            &:active {
              cursor: grabbing;
            }

            .node-icon {
              width: 32px;
              height: 32px;
              border-radius: 6px;
              background: #f3f4f6;
              display: flex;
              align-items: center;
              justify-content: center;
              color: #6b7280;

              &.llm-node {
                background: #dbeafe;
                color: #3b82f6;
              }

              &.workflow-node {
                background: #f0f9ff;
                color: #0ea5e9;
              }

              &.logic-node {
                background: #fef3c7;
                color: #f59e0b;
              }

              &.io-node {
                background: #ecfdf5;
                color: #10b981;
              }

              &.database-node {
                background: #fdf2f8;
                color: #ec4899;
              }

              &.knowledge-node {
                background: #f3e8ff;
                color: #8b5cf6;
              }

              &.image-node {
                background: #fef7cd;
                color: #eab308;
              }

              &.notification-node {
                background: #fef2f2;
                color: #ef4444;
              }
            }

            .node-info {
              flex: 1;

              .node-name {
                font-size: 13px;
                font-weight: 500;
                color: #1f2937;
                margin-bottom: 2px;
              }

              .node-desc {
                font-size: 11px;
                color: #6b7280;
                line-height: 1.3;
              }
            }
          }
        }
      }
    }
    
    .canvas-container {
      flex: 1;
      position: relative;
      overflow: auto;
      background: #fafbfc;
      
      .workflow-canvas {
        position: relative;
        width: 2000px;
        height: 2000px;
        transform-origin: 0 0;
        
        .canvas-grid {
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          background-image: 
            radial-gradient(circle, #d1d5db 1px, transparent 1px);
          background-size: 20px 20px;
          opacity: 0.5;
        }
        
        .workflow-node {
          position: absolute;
          width: 150px;
          min-height: 80px;
          background: white;
          border: 2px solid #e5e7eb;
          border-radius: 8px;
          cursor: pointer;
          transition: all 0.2s ease;
          
          &:hover {
            border-color: #3b82f6;
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
          }
          
          &.selected {
            border-color: #3b82f6;
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
          }
          
          .node-header {
            display: flex;
            align-items: center;
            padding: 8px 12px;
            border-bottom: 1px solid #e5e7eb;
            background: #f9fafb;
            border-radius: 6px 6px 0 0;
            
            .node-icon {
              margin-right: 8px;
              color: #6b7280;
            }
            
            .node-title {
              flex: 1;
              font-size: 12px;
              font-weight: 500;
              color: #1f2937;
            }
            
            .node-actions {
              display: flex;
              gap: 4px;
              opacity: 0;
              transition: opacity 0.2s ease;
            }
          }
          
          &:hover .node-actions {
            opacity: 1;
          }
          
          .input-ports,
          .output-ports {
            padding: 8px 0;
            
            .port {
              display: flex;
              align-items: center;
              padding: 4px 12px;
              font-size: 11px;
              color: #6b7280;
              position: relative;
              
              .port-dot {
                width: 8px;
                height: 8px;
                border-radius: 50%;
                background: #d1d5db;
                border: 2px solid white;
                cursor: crosshair;
                
                &:hover {
                  background: #3b82f6;
                }
              }
              
              .port-label {
                margin: 0 8px;
              }
            }
            
            .input-port {
              justify-content: flex-start;
              
              .port-dot {
                position: absolute;
                left: -6px;
              }
            }
            
            .output-port {
              justify-content: flex-end;
              
              .port-dot {
                position: absolute;
                right: -6px;
              }
            }
          }
        }
        
        .connections-layer {
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          pointer-events: none;
          
          .connection-line {
            fill: none;
            stroke: #6b7280;
            stroke-width: 2;
            pointer-events: stroke;
            cursor: pointer;
            
            &:hover {
              stroke: #3b82f6;
              stroke-width: 3;
            }
            
            &.selected {
              stroke: #3b82f6;
              stroke-width: 3;
            }
          }
          
          .temp-connection-line {
            fill: none;
            stroke: #3b82f6;
            stroke-width: 2;
            stroke-dasharray: 5,5;
          }
        }
      }
    }
    
    .properties-panel {
      width: 320px;
      border-left: 1px solid #e4e7ed;
      background: white;
      display: flex;
      flex-direction: column;
      
      .panel-header {
        height: 60px;
        padding: 0 20px;
        border-bottom: 1px solid #e4e7ed;
        display: flex;
        align-items: center;
        
        h4 {
          margin: 0;
          font-size: 14px;
          color: #374151;
        }
      }
      
      .node-properties {
        flex: 1;
        padding: 20px;
        overflow-y: auto;
      }
      
      .no-selection {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }
}
</style>
