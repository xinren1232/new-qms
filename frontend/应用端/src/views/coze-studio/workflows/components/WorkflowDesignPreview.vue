<template>
  <div class="workflow-design-preview">
    <div class="preview-header">
      <div class="title">
        <h3>{{ workflow?.name || '工作流预览' }}</h3>
        <el-tag size="small" type="info" v-if="workflow?.version">v{{ workflow.version }}</el-tag>
        <el-tag size="small" class="scene" v-if="workflow?.category">场景：{{ sceneLabel }}</el-tag>
      </div>
      <div class="meta">
        <span>节点：{{ (workflow?.nodes || []).length }}</span>
        <span v-if="workflow?.updatedAt">更新：{{ formatDate(workflow.updatedAt) }}</span>
      </div>
    </div>

    <div class="content">
      <!-- 左侧：只读可视化画布 -->
      <div class="canvas-wrapper" ref="canvasRef" :class="{ dragging }"
           @wheel.prevent="onWheel"
           @mousedown="onMouseDown">
        <div class="canvas" :style="{ width: canvasWidth + 'px', height: canvasHeight + 'px' }">
          <!-- 视口：负责平移/缩放 -->
          <div class="viewport" :style="viewportStyle">
            <!-- 节点 -->
            <div
              v-for="node in positionedNodes"
              :key="node.id"
              class="node"
              :class="[`node-${node.type}`, { 'node-hover': hoveredNodeId === node.id }]"
              :style="{ left: node.x + 'px', top: node.y + 'px' }"
              @mouseenter="hoveredNodeId = node.id"
              @mouseleave="hoveredNodeId = null"
            >
              <div class="node-header">
                <div class="node-icon"><el-icon><component :is="getNodeIcon(node.type)" /></el-icon></div>
                <div class="node-title">{{ node.name }}</div>
                <el-tag size="small" type="info">{{ getNodeTypeName(node.type) }}</el-tag>
              </div>
            </div>

            <!-- 连接线 -->
            <svg class="edges">
              <defs>
                <marker id="arrow" viewBox="0 0 10 10" refX="10" refY="5" markerWidth="8" markerHeight="8" orient="auto-start-reverse">
                  <path d="M 0 0 L 10 5 L 0 10 z" fill="#409EFF" />
                </marker>
              </defs>
              <g v-for="(edge, i) in (workflow?.connections || [])" :key="i">
                <path
                  :id="'edge-path-' + i"
                  :d="getConnectionPath(edge)"
                  class="edge"
                  :class="{ 'edge-highlight': isEdgeRelated(edge, hoveredNodeId) }"
                  marker-end="url(#arrow)"
                />
                <text v-if="showEdgeLabels && edge.label" class="edge-label">
                  <textPath :href="'#edge-path-' + i" startOffset="50%" text-anchor="middle">{{ edge.label }}</textPath>
                </text>
              </g>
            </svg>
          </div>

          <!-- 画布工具条 -->
          <div class="canvas-toolbar">
            <el-button size="small" @click.stop="zoomOut">-</el-button>
            <el-button size="small" @click.stop="resetView">100%</el-button>
            <el-button size="small" @click.stop="fitToView">适配</el-button>
            <el-button size="small" @click.stop="zoomIn">+</el-button>
          </div>
        </div>
      </div>

      <!-- 右侧：设计类型/统计 -->
      <div class="summary">
        <h4>设计类型</h4>
        <div class="chips">
          <el-tag v-for="t in nodeTypeSummary" :key="t.type" size="small" :type="t.tagType" class="chip">
            {{ t.label }} × {{ t.count }}
          </el-tag>
        </div>
        <el-divider />
        <h4>基础信息</h4>
        <ul class="meta-list">
          <li><span>状态</span><span>{{ statusText }}</span></li>
          <li><span>节点数</span><span>{{ (workflow?.nodes || []).length }}</span></li>
          <li v-if="workflow?.executionCount != null"><span>执行次数</span><span>{{ workflow.executionCount }}</span></li>
          <li v-if="workflow?.successRate"><span>成功率</span><span>{{ workflow.successRate }}</span></li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'
import { Cpu, Connection, Document, VideoPlay } from '@element-plus/icons-vue'

const props = defineProps({
  workflow: { type: Object, default: () => ({}) },
  autoFit: { type: Boolean, default: true }
})

const canvasRef = ref(null)
const canvasWidth = ref(1200)
const canvasHeight = ref(520)

// 悬停高亮
const hoveredNodeId = ref(null)
const showEdgeLabels = ref(true)
function isEdgeRelated(edge, nodeId){
  if(!nodeId) return false
  return edge.from === nodeId || edge.to === nodeId
}

// 视口平移/缩放
const scale = ref(1)
const translate = ref({ x: 0, y: 0 })
const viewportStyle = computed(() => ({
  transform: `translate(${translate.value.x}px, ${translate.value.y}px) scale(${scale.value})`,
  transformOrigin: '0 0'
}))

function fitToView(){
  // 计算节点边界，按容器大小自适应
  const nodes = positionedNodes.value
  const wrapper = canvasRef.value
  if(!nodes.length || !wrapper) return resetView()
  const minX = Math.min(...nodes.map(n=>n.x))
  const minY = Math.min(...nodes.map(n=>n.y))
  const maxX = Math.max(...nodes.map(n=>n.x+170))
  const maxY = Math.max(...nodes.map(n=>n.y+80))
  const bboxW = maxX - minX
  const bboxH = maxY - minY
  const rect = wrapper.getBoundingClientRect()
  const pad = 40
  const s = Math.min((rect.width - pad) / bboxW, (rect.height - pad) / bboxH)
  scale.value = clamp(s, 0.4, 2.5)
  // 居中显示
  const viewW = rect.width
  const viewH = rect.height
  translate.value = {
    x: (viewW - bboxW * scale.value)/2 - minX*scale.value,
    y: (viewH - bboxH * scale.value)/2 - minY*scale.value
  }
}

function clamp(v, min, max){ return Math.max(min, Math.min(max, v)) }
function zoom(delta, center){
  const prev = scale.value
  const next = clamp(prev * (1 + delta), 0.4, 2.5)
  if (!center) {
    const rect = canvasRef.value?.getBoundingClientRect?.()
    center = rect ? { x: rect.width/2, y: rect.height/2 } : { x: 0, y: 0 }
  }
  // 以中心点为基准缩放，保持锚点不偏移
  translate.value = {
    x: center.x - (next/prev) * (center.x - translate.value.x),
    y: center.y - (next/prev) * (center.y - translate.value.y)
  }
  scale.value = next
}
function zoomIn(){ zoom(0.1) }
function zoomOut(){ zoom(-0.1) }
function resetView(){ scale.value = 1; translate.value = { x: 0, y: 0 } }

let dragging = false, start = {x:0,y:0}, startT = {x:0,y:0}
function onMouseDown(e){
  if(e.button!==0) return
  dragging = true
  start = {x:e.clientX,y:e.clientY}
  startT = {...translate.value}
  window.addEventListener('mousemove', onMouseMove)
  window.addEventListener('mouseup', onMouseUp)
}
function onMouseMove(e){
  if(!dragging) return
  translate.value = { x: startT.x + (e.clientX - start.x), y: startT.y + (e.clientY - start.y) }
}
function onMouseUp(){
  dragging = false
  window.removeEventListener('mousemove', onMouseMove)
  window.removeEventListener('mouseup', onMouseUp)
}
function onWheel(e){
  const rect = canvasRef.value?.getBoundingClientRect?.()
  const center = rect ? { x: e.clientX - rect.left, y: e.clientY - rect.top } : { x: 0, y: 0 }
  zoom(e.deltaY < 0 ? 0.12 : -0.12, center)
}

onBeforeUnmount(()=>{ window.removeEventListener('mousemove', onMouseMove); window.removeEventListener('mouseup', onMouseUp) })

const sceneLabel = computed(() => ({
  'data-processing': '数据处理',
  'content-generation': '内容生成',
  automation: '自动化',
  other: '其他'
})[props.workflow?.category] || '未分类')

const statusText = computed(() => ({ running: '运行中', draft: '草稿', stopped: '已停止', active: '启用' })[props.workflow?.status] || '未知')

const positionedNodes = computed(() => {
  const nodes = props.workflow?.nodes || []
  if (!nodes.length) return []
  // 如果已有坐标，直接用；否则做简单水平布局
  const hasPos = nodes.every(n => typeof n.x === 'number' && typeof n.y === 'number')
  if (hasPos) return nodes
  return nodes.map((n, i) => ({ ...n, x: 60 + i * 180, y: 120 + (i % 2) * 140 }))
})

const nodeMap = computed(() => Object.fromEntries(positionedNodes.value.map(n => [n.id, n])))

function getConnectionPath(conn) {
  const from = nodeMap.value[conn.from]
  const to = nodeMap.value[conn.to]
  if (!from || !to) return ''
  const x1 = from.x + 140, y1 = from.y + 24
  const x2 = to.x, y2 = to.y + 24
  const dx = Math.max(60, Math.abs(x2 - x1) / 2)
  return `M ${x1} ${y1} C ${x1 + dx} ${y1}, ${x2 - dx} ${y2}, ${x2} ${y2}`
}

function getNodeIcon(type) {
  switch (type) {
    case 'ai': return Cpu
    case 'condition': return Connection
    case 'http':
    case 'database':
    case 'document': return Document
    case 'start':
    case 'end':
    default: return VideoPlay
  }
}

function getNodeTypeName(type) {
  return ({ start: '开始', end: '结束', ai: '大模型/AI', condition: '条件判断', http: 'HTTP请求', database: '数据库', document: '文档处理' }[type]) || type
}

const typeOrder = ['start', 'ai', 'condition', 'http', 'database', 'document', 'end']
const tagByType = { start: 'success', ai: 'warning', condition: 'info', http: '', database: 'primary', document: 'success', end: 'danger' }

const nodeTypeSummary = computed(() => {
  const groups = {}
  for (const n of positionedNodes.value) {
    groups[n.type] = (groups[n.type] || 0) + 1
  }
  return typeOrder
    .filter(t => groups[t])
    .map(t => ({ type: t, count: groups[t], label: getNodeTypeName(t), tagType: tagByType[t] }))
})

function formatDate(d) {
  try { return new Date(d).toLocaleString() } catch { return '' }
}

onMounted(() => {
  // 简单自适应：根据节点范围调整画布大小
  const xs = positionedNodes.value.map(n => n.x)
  const ys = positionedNodes.value.map(n => n.y)
  if (xs.length) {
    canvasWidth.value = Math.max(800, Math.max(...xs) + 240)
    canvasHeight.value = Math.max(320, Math.max(...ys) + 160)
  }
  // 自动适配
  if (props.autoFit) setTimeout(() => fitToView(), 0)
})
</script>

<style scoped>
.workflow-design-preview{display:flex;flex-direction:column;height:100%}
.preview-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:12px}
.preview-header .title{display:flex;align-items:center;gap:8px}
.preview-header .scene{margin-left:8px}
.content{display:flex;gap:16px;min-height:420px}
.canvas-wrapper{flex:1;overflow:hidden;background:var(--el-fill-color-lighter);border:1px solid var(--el-border-color-lighter);border-radius:8px;position:relative;cursor:grab}
.canvas-wrapper.dragging{cursor:grabbing}
.canvas{position:relative}
.viewport{position:relative;transform-origin:0 0}
.canvas-toolbar{position:absolute;right:12px;bottom:12px;display:flex;gap:6px}
.node{position:absolute;width:170px;background:white;border:1px solid var(--el-border-color-lighter);border-radius:10px;box-shadow:0 2px 6px rgba(0,0,0,0.06);transition:box-shadow .2s ease, transform .2s ease}
.node:hover,.node-hover{box-shadow:0 6px 16px rgba(0,0,0,0.12);transform:translateY(-1px)}
/* 节点类型主题色 */
.node-start .node-header{border-left:4px solid #67C23A}
.node-ai .node-header{border-left:4px solid #E6A23C}
.node-condition .node-header{border-left:4px solid #909399}
.node-http .node-header{border-left:4px solid #409EFF}
.node-database .node-header{border-left:4px solid #409EFF}
.node-document .node-header{border-left:4px solid #67C23A}
.node-end .node-header{border-left:4px solid #F56C6C}
.node-header{display:flex;align-items:center;gap:8px;padding:10px 12px}
.node-title{font-weight:600;flex:1}
.edges{position:absolute;inset:0;pointer-events:none}
.edge{fill:none;stroke:#409EFF;stroke-width:2;opacity:.6}
.edge.edge-highlight{stroke:#FF7E33;opacity:1}
.edge-label{font-size:12px;fill:#606266}
.summary{width:300px;border:1px solid var(--el-border-color-lighter);border-radius:8px;padding:12px;background:white}
.chips{display:flex;flex-wrap:wrap;gap:8px}
.meta-list{list-style:none;padding:0;margin:0;display:flex;flex-direction:column;gap:6px}
.meta-list li{display:flex;justify-content:space-between;color:var(--el-text-color-regular)}
</style>

