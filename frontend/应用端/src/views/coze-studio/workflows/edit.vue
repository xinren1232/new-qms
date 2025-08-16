<template>
  <div class="workflow-editor">
    <!-- 顶部工具栏 -->
    <div class="editor-toolbar">
      <div class="toolbar-left">
        <el-button @click="goBack" :icon="ArrowLeft" circle />
        <div class="workflow-info">
          <el-input 
            v-model="workflow.name" 
            class="workflow-name-input"
            placeholder="工作流名称"
          />
          <el-button @click="saveWorkflow" type="primary" :icon="Check">
            保存
          </el-button>
        </div>
      </div>
      
      <div class="toolbar-center">
        <el-button-group>
          <el-button @click="zoomIn" :icon="ZoomIn">放大</el-button>
          <el-button @click="zoomOut" :icon="ZoomOut">缩小</el-button>
          <el-button @click="resetZoom" :icon="Refresh">重置</el-button>
        </el-button-group>
      </div>
      
      <div class="toolbar-right">
        <el-button @click="runTest" :icon="VideoPlay">测试运行</el-button>
        <el-button @click="showSettings = true" :icon="Setting">设置</el-button>
      </div>
    </div>

    <!-- 主编辑区域 -->
    <div class="editor-main">
      <!-- 左侧节点面板 -->
      <div class="node-panel">
        <div class="panel-header">
          <h3>节点库</h3>
        </div>
        <div class="node-categories">
          <div v-for="category in nodeCategories" :key="category.name" class="category">
            <div class="category-header" @click="toggleCategory(category.name)">
              <span>{{ category.label }}</span>
              <el-icon>
                <ArrowDown v-if="expandedCategories.includes(category.name)" />
                <ArrowRight v-else />
              </el-icon>
            </div>
            <div v-show="expandedCategories.includes(category.name)" class="category-nodes">
              <div 
                v-for="node in category.nodes" 
                :key="node.type"
                class="node-item"
                draggable="true"
                @dragstart="handleDragStart($event, node)"
              >
                <div class="node-icon">
                  <el-icon><component :is="node.icon" /></el-icon>
                </div>
                <div class="node-info">
                  <div class="node-name">{{ node.name }}</div>
                  <div class="node-desc">{{ node.description }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 中间画布区域 -->
      <div class="canvas-container">
        <div 
          ref="canvas"
          class="workflow-canvas"
          @drop="handleDrop"
          @dragover="handleDragOver"
          @click="clearSelection"
        >
          <!-- 网格背景 -->
          <div class="canvas-grid"></div>
          
          <!-- 工作流节点 -->
          <div 
            v-for="node in workflow.nodes" 
            :key="node.id"
            class="workflow-node"
            :class="{ 
              'selected': selectedNode?.id === node.id,
              [`node-${node.type}`]: true 
            }"
            :style="{ 
              left: node.x + 'px', 
              top: node.y + 'px' 
            }"
            @click.stop="selectNode(node)"
            @mousedown="startDrag($event, node)"
          >
            <div class="node-header">
              <div class="node-icon">
                <el-icon><component :is="getNodeIcon(node.type)" /></el-icon>
              </div>
              <div class="node-title">{{ node.name }}</div>
              <div class="node-actions">
                <el-button 
                  size="small" 
                  :icon="Delete" 
                  circle 
                  @click.stop="deleteNode(node)"
                />
              </div>
            </div>
            
            <!-- 连接点 -->
            <div class="connection-points">
              <div class="input-point" v-if="node.type !== 'start'"></div>
              <div class="output-point" v-if="node.type !== 'end'"></div>
            </div>
          </div>
          
          <!-- 连接线 -->
          <svg class="connections-layer">
            <path 
              v-for="connection in workflow.connections" 
              :key="`${connection.from}-${connection.to}`"
              :d="getConnectionPath(connection)"
              class="connection-line"
            />
          </svg>
        </div>
      </div>

      <!-- 右侧属性面板 -->
      <div class="property-panel">
        <div class="panel-header">
          <h3>{{ selectedNode ? '节点属性' : '工作流属性' }}</h3>
        </div>
        
        <div v-if="selectedNode" class="node-properties">
          <el-form :model="selectedNode" label-width="80px">
            <el-form-item label="节点名称">
              <el-input v-model="selectedNode.name" />
            </el-form-item>
            
            <el-form-item label="节点类型">
              <el-select v-model="selectedNode.type" disabled>
                <el-option 
                  v-for="type in nodeTypes" 
                  :key="type.value"
                  :label="type.label" 
                  :value="type.value" 
                />
              </el-select>
            </el-form-item>
            
            <!-- AI节点特有配置 -->
            <template v-if="selectedNode.type === 'ai'">
              <el-form-item label="AI模型">
                <el-select v-model="selectedNode.config.model">
                  <el-option label="GPT-3.5 Turbo" value="gpt-3.5-turbo" />
                  <el-option label="GPT-4" value="gpt-4" />
                  <el-option label="Claude-3" value="claude-3" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="提示词">
                <el-input 
                  v-model="selectedNode.config.prompt" 
                  type="textarea" 
                  :rows="4"
                  placeholder="输入AI提示词..."
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
            
            <!-- 条件节点特有配置 -->
            <template v-if="selectedNode.type === 'condition'">
              <el-form-item label="条件表达式">
                <el-input 
                  v-model="selectedNode.config.expression" 
                  placeholder="例如: input.type === 'urgent'"
                />
              </el-form-item>
            </template>
            
            <!-- HTTP节点特有配置 -->
            <template v-if="selectedNode.type === 'http'">
              <el-form-item label="请求方法">
                <el-select v-model="selectedNode.config.method">
                  <el-option label="GET" value="GET" />
                  <el-option label="POST" value="POST" />
                  <el-option label="PUT" value="PUT" />
                  <el-option label="DELETE" value="DELETE" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="请求URL">
                <el-input 
                  v-model="selectedNode.config.url" 
                  placeholder="https://api.example.com/endpoint"
                />
              </el-form-item>
            </template>
          </el-form>
        </div>
        
        <div v-else class="workflow-properties">
          <el-form :model="workflow" label-width="80px">
            <el-form-item label="工作流名称">
              <el-input v-model="workflow.name" />
            </el-form-item>
            
            <el-form-item label="描述">
              <el-input 
                v-model="workflow.description" 
                type="textarea" 
                :rows="3"
              />
            </el-form-item>
            
            <el-form-item label="触发方式">
              <el-select v-model="workflow.trigger">
                <el-option label="手动触发" value="manual" />
                <el-option label="定时触发" value="schedule" />
                <el-option label="事件触发" value="event" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>

    <!-- 设置对话框 -->
    <el-dialog v-model="showSettings" title="工作流设置" width="600px">
      <el-form :model="workflow" label-width="100px">
        <el-form-item label="超时时间">
          <el-input-number v-model="workflow.timeout" :min="1" :max="3600" />
          <span style="margin-left: 8px;">秒</span>
        </el-form-item>
        
        <el-form-item label="重试次数">
          <el-input-number v-model="workflow.retryCount" :min="0" :max="10" />
        </el-form-item>
        
        <el-form-item label="并发限制">
          <el-input-number v-model="workflow.concurrency" :min="1" :max="100" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showSettings = false">取消</el-button>
        <el-button type="primary" @click="saveSettings">保存设置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  ArrowLeft, 
  Check, 
  ZoomIn, 
  ZoomOut, 
  Refresh,
  VideoPlay, 
  Setting,
  ArrowDown,
  ArrowRight,
  Delete,
  User,
  Cpu,
  Connection,
  Document,
  Link,
  DataAnalysis
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

// 响应式数据
const canvas = ref(null)
const selectedNode = ref(null)
const showSettings = ref(false)
const expandedCategories = ref(['basic', 'ai'])

// 工作流数据
const workflow = reactive({
  id: route.params.id,
  name: '客户服务工作流',
  description: '处理客户咨询和问题解决的自动化工作流',
  trigger: 'manual',
  timeout: 300,
  retryCount: 3,
  concurrency: 10,
  nodes: [
    {
      id: 'start_1',
      name: '开始',
      type: 'start',
      x: 100,
      y: 100,
      config: {}
    },
    {
      id: 'ai_1',
      name: '问题分析',
      type: 'ai',
      x: 300,
      y: 100,
      config: {
        model: 'gpt-3.5-turbo',
        prompt: '分析客户问题的类型和紧急程度',
        temperature: 0.7
      }
    }
  ],
  connections: [
    { from: 'start_1', to: 'ai_1' }
  ]
})

// 节点分类
const nodeCategories = ref([
  {
    name: 'basic',
    label: '基础节点',
    nodes: [
      { type: 'start', name: '开始', description: '工作流开始节点', icon: 'VideoPlay' },
      { type: 'end', name: '结束', description: '工作流结束节点', icon: 'Document' },
      { type: 'condition', name: '条件判断', description: '根据条件分支', icon: 'Connection' }
    ]
  },
  {
    name: 'ai',
    label: 'AI节点',
    nodes: [
      { type: 'ai', name: 'AI对话', description: 'AI模型处理', icon: 'Cpu' },
      { type: 'analysis', name: '智能分析', description: '数据分析处理', icon: 'DataAnalysis' }
    ]
  },
  {
    name: 'integration',
    label: '集成节点',
    nodes: [
      { type: 'http', name: 'HTTP请求', description: 'HTTP API调用', icon: 'Link' },
      { type: 'database', name: '数据库', description: '数据库操作', icon: 'Document' }
    ]
  }
])

const nodeTypes = ref([
  { label: '开始节点', value: 'start' },
  { label: 'AI节点', value: 'ai' },
  { label: '条件节点', value: 'condition' },
  { label: 'HTTP节点', value: 'http' },
  { label: '结束节点', value: 'end' }
])

// 方法
const goBack = () => {
  router.back()
}

const saveWorkflow = () => {
  ElMessage.success('工作流已保存')
}

const runTest = () => {
  ElMessage.success('开始测试运行...')
}

const toggleCategory = (categoryName) => {
  const index = expandedCategories.value.indexOf(categoryName)
  if (index > -1) {
    expandedCategories.value.splice(index, 1)
  } else {
    expandedCategories.value.push(categoryName)
  }
}

const handleDragStart = (event, node) => {
  event.dataTransfer.setData('application/json', JSON.stringify(node))
}

const handleDragOver = (event) => {
  event.preventDefault()
}

const handleDrop = (event) => {
  event.preventDefault()
  const nodeData = JSON.parse(event.dataTransfer.getData('application/json'))
  const rect = canvas.value.getBoundingClientRect()
  
  const newNode = {
    id: `${nodeData.type}_${Date.now()}`,
    name: nodeData.name,
    type: nodeData.type,
    x: event.clientX - rect.left - 75,
    y: event.clientY - rect.top - 40,
    config: getDefaultConfig(nodeData.type)
  }
  
  workflow.nodes.push(newNode)
  ElMessage.success(`已添加${nodeData.name}节点`)
}

const getDefaultConfig = (type) => {
  const configs = {
    ai: { model: 'gpt-3.5-turbo', prompt: '', temperature: 0.7 },
    condition: { expression: '' },
    http: { method: 'GET', url: '' },
    start: {},
    end: {}
  }
  return configs[type] || {}
}

const selectNode = (node) => {
  selectedNode.value = node
}

const clearSelection = () => {
  selectedNode.value = null
}

const deleteNode = (node) => {
  const index = workflow.nodes.findIndex(n => n.id === node.id)
  if (index > -1) {
    workflow.nodes.splice(index, 1)
    // 删除相关连接
    workflow.connections = workflow.connections.filter(
      conn => conn.from !== node.id && conn.to !== node.id
    )
    if (selectedNode.value?.id === node.id) {
      selectedNode.value = null
    }
    ElMessage.success('节点已删除')
  }
}

const getNodeIcon = (type) => {
  const icons = {
    start: 'VideoPlay',
    ai: 'Cpu',
    condition: 'Connection',
    http: 'Link',
    end: 'Document'
  }
  return icons[type] || 'Document'
}

const getConnectionPath = (connection) => {
  const fromNode = workflow.nodes.find(n => n.id === connection.from)
  const toNode = workflow.nodes.find(n => n.id === connection.to)
  
  if (!fromNode || !toNode) return ''
  
  const x1 = fromNode.x + 150
  const y1 = fromNode.y + 40
  const x2 = toNode.x
  const y2 = toNode.y + 40
  
  return `M ${x1} ${y1} Q ${(x1 + x2) / 2} ${y1} ${x2} ${y2}`
}

const startDrag = (event, node) => {
  // 节点拖拽逻辑
  let isDragging = false
  const startX = event.clientX - node.x
  const startY = event.clientY - node.y
  
  const handleMouseMove = (e) => {
    if (!isDragging) {
      isDragging = true
    }
    node.x = e.clientX - startX
    node.y = e.clientY - startY
  }
  
  const handleMouseUp = () => {
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', handleMouseUp)
  }
  
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}

const zoomIn = () => {
  ElMessage.info('放大功能待实现')
}

const zoomOut = () => {
  ElMessage.info('缩小功能待实现')
}

const resetZoom = () => {
  ElMessage.info('重置缩放功能待实现')
}

const saveSettings = () => {
  showSettings.value = false
  ElMessage.success('设置已保存')
}

onMounted(() => {
  // 初始化编辑器
})
</script>

<style lang="scss" scoped>
.workflow-editor {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;

  .editor-toolbar {
    height: 60px;
    background: white;
    border-bottom: 1px solid #e4e7ed;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;

    .toolbar-left {
      display: flex;
      align-items: center;
      gap: 16px;

      .workflow-info {
        display: flex;
        align-items: center;
        gap: 12px;

        .workflow-name-input {
          width: 200px;
        }
      }
    }

    .toolbar-center {
      flex: 1;
      display: flex;
      justify-content: center;
    }

    .toolbar-right {
      display: flex;
      gap: 12px;
    }
  }

  .editor-main {
    flex: 1;
    display: flex;
    overflow: hidden;

    .node-panel {
      width: 280px;
      background: white;
      border-right: 1px solid #e4e7ed;
      overflow-y: auto;

      .panel-header {
        padding: 16px;
        border-bottom: 1px solid #e4e7ed;

        h3 {
          margin: 0;
          font-size: 16px;
          font-weight: 600;
          color: #1f2937;
        }
      }

      .node-categories {
        .category {
          .category-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 12px 16px;
            cursor: pointer;
            background: #f9fafb;
            border-bottom: 1px solid #e5e7eb;
            font-weight: 500;
            color: #374151;

            &:hover {
              background: #f3f4f6;
            }
          }

          .category-nodes {
            .node-item {
              display: flex;
              align-items: center;
              gap: 12px;
              padding: 12px 16px;
              cursor: grab;
              border-bottom: 1px solid #f3f4f6;

              &:hover {
                background: #f9fafb;
              }

              &:active {
                cursor: grabbing;
              }

              .node-icon {
                width: 32px;
                height: 32px;
                border-radius: 6px;
                background: #3b82f6;
                display: flex;
                align-items: center;
                justify-content: center;
                color: white;
                font-size: 14px;
              }

              .node-info {
                .node-name {
                  font-size: 14px;
                  font-weight: 500;
                  color: #1f2937;
                  margin-bottom: 2px;
                }

                .node-desc {
                  font-size: 12px;
                  color: #6b7280;
                }
              }
            }
          }
        }
      }
    }

    .canvas-container {
      flex: 1;
      position: relative;
      overflow: hidden;

      .workflow-canvas {
        width: 100%;
        height: 100%;
        position: relative;
        background: #fafbfc;
        overflow: auto;

        .canvas-grid {
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          background-image: 
            radial-gradient(circle, #d1d5db 1px, transparent 1px);
          background-size: 20px 20px;
          pointer-events: none;
        }

        .workflow-node {
          position: absolute;
          width: 150px;
          background: white;
          border: 2px solid #e5e7eb;
          border-radius: 8px;
          cursor: pointer;
          user-select: none;

          &.selected {
            border-color: #3b82f6;
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
          }

          &.node-start {
            border-color: #10b981;
          }

          &.node-end {
            border-color: #ef4444;
          }

          &.node-ai {
            border-color: #8b5cf6;
          }

          .node-header {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 12px;

            .node-icon {
              width: 24px;
              height: 24px;
              border-radius: 4px;
              background: #3b82f6;
              display: flex;
              align-items: center;
              justify-content: center;
              color: white;
              font-size: 12px;
            }

            .node-title {
              flex: 1;
              font-size: 14px;
              font-weight: 500;
              color: #1f2937;
            }

            .node-actions {
              opacity: 0;
              transition: opacity 0.2s;
            }
          }

          &:hover .node-actions {
            opacity: 1;
          }

          .connection-points {
            .input-point,
            .output-point {
              position: absolute;
              width: 8px;
              height: 8px;
              border-radius: 50%;
              background: #3b82f6;
              border: 2px solid white;
            }

            .input-point {
              left: -6px;
              top: 50%;
              transform: translateY(-50%);
            }

            .output-point {
              right: -6px;
              top: 50%;
              transform: translateY(-50%);
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
            stroke-dasharray: 5,5;
          }
        }
      }
    }

    .property-panel {
      width: 320px;
      background: white;
      border-left: 1px solid #e4e7ed;
      overflow-y: auto;

      .panel-header {
        padding: 16px;
        border-bottom: 1px solid #e4e7ed;

        h3 {
          margin: 0;
          font-size: 16px;
          font-weight: 600;
          color: #1f2937;
        }
      }

      .node-properties,
      .workflow-properties {
        padding: 16px;
      }
    }
  }
}
</style>
