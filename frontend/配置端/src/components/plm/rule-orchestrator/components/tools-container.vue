<template>
  <div class="tool-box">
    <span v-for="item in toolList" :key="item.id">
      <el-tooltip :content="item.name">
        <el-button
          :disabled="renderDisabled(item)"
          size="small"
          :type="item.type"
          :icon="item.icon"
          circle
          @click="onHandler(item)"
        />
      </el-tooltip>
    </span>
    <el-divider direction="vertical" />
    <el-button class="danger-color" type="text" @click="goHomePage">取消</el-button>
    <el-button type="text" @click="exportData">保存</el-button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import route from '@/router'

import FlowGraph from '../core'
import { canUndo, canRedo, hasSelected,activeCellState } from '../core/state'
import screenfull from 'screenfull'

const emits = defineEmits(['export-data'])
const toolList = ref([])

toolList.value = [
  // {
  //   id: 1,
  //   name: '后退',
  //   type: '',
  //   color: '#6A5DFB',
  //   icon: 'el-icon-back',
  //   action: 'back'
  // },
  // {
  //   id: 2,
  //   name: '前进',
  //   type: '',
  //   color: '#27C777',
  //   icon: 'el-icon-right',
  //   action: 'forwards'
  // },
  {
    id: 3,
    name: '放大',
    type: '',
    color: '#FFAF21',
    icon: 'el-icon-zoom-in',
    action: 'zoomIn'
  },
  {
    id: 4,
    name: '缩小',
    type: '',
    color: '#FFAF21',
    icon: 'el-icon-zoom-out',
    action: 'zoomOut'
  },
  {
    id: 5,
    name: '居中',
    type: '',
    color: '#FFAF21',
    icon: 'el-icon-c-scale-to-original',
    action: 'autoFit'
  },
  {
    id: 6,
    name: '全选',
    type: '',
    color: '#FFAF21',
    icon: 'el-icon-finished',
    action: 'selectAll'
  },
  // {
  //   id: 7,
  //   name: '复制',
  //   type: '',
  //   color: '#FFAF21',
  //   icon: 'el-icon-document-copy',
  //   action: 'copy'
  // },
  {
    id: 8,
    name: '删除',
    type: 'danger',
    color: '#FFAF21',
    icon: 'el-icon-delete',
    action: 'delete'
  },
  {
    id: 9,
    name: '清空画板',
    type: 'danger',
    color: '#FFAF21',
    icon: 'el-icon-refresh',
    action: 'clear'
  },
  {
    id: 10,
    name: '导出图片',
    type: '',
    color: '#FFAF21',
    icon: 'el-icon-download',
    action: 'export'
  }
  // {
  //   id: 11,
  //   name: '全屏',
  //   type: '',
  //   color: '#FFAF21',
  //   icon: 'el-icon-full-screen',
  //   action: 'fullScreen'
  // }
]
const onHandler = (item) => {
  switch (item.action) {
    case 'back':
      onUndo()
      break
    case 'forwards':
      onRedo()
      break
    case 'delete':
      removeNode()
      break
    case 'autoFit':
      return FlowGraph.graph.centerContent()
    case 'zoomIn':
      return FlowGraph.graph.zoom(0.2)
    case 'zoomOut':
      return FlowGraph.graph.zoom(-0.2)
    case 'selectAll':
      return selectAll()
    case 'copy':
      return onCopy()
    case 'clear':
      return onClear()
    case 'export':
      return FlowGraph.graph.exportPNG('chart', {
        width: 1024,
        height: 1024,
        padding: 50,
        backgroundColor: '#32CEA6',
        quality: 1
      })
    case 'fullScreen':
      return screenfull.toggle(document.getElementsByClassName('graph-box')[0])
    default:
      break
  }
}

const copyState = {
  offset: 30,
  useLocalStorage: true
}
const exportData = () => {
  emits('export-data', FlowGraph.graph.toJSON())
}

// 移除NODE
const removeNode = () => {
  const cells = FlowGraph.graph.getSelectedCells()

  if (cells.length) {
    cells.forEach((cell) => {
      cell.removeTools()
    })
    FlowGraph.graph.removeCells(cells)
    activeCellState.value = null
  }
}
const onClear = () => {
  FlowGraph.graph.clearCells()
  activeCellState.value = null
}
// Undo
const onUndo = () => {
  FlowGraph.graph.undo()
}
// Redo
const onRedo = () => {
  FlowGraph.graph.redo()
}
const goHomePage = () => {
  route.push({ path: '/system-manage/lifecycle-template-manage' })
}

// 全选
const selectAll = () => {
  const cells = FlowGraph.graph.getCells()

  if (cells.length) {
    FlowGraph.graph.select(cells)
  }
}
// 复制
const onCopy = () => {
  const cells = FlowGraph.graph.getSelectedCells()

  if (cells && cells.length) {
    FlowGraph.graph.copy(cells, copyState)
  }

  if (!FlowGraph.graph.isClipboardEmpty()) {
    const cells = FlowGraph.graph.paste(copyState)

    FlowGraph.graph.cleanSelection()
    FlowGraph.graph.select(cells)
  }
}

//
const renderDisabled = (item) => {
  switch (item.action) {
    case 'back':
      return !canUndo.value
    case 'forwards':
      return !canRedo.value
    case 'delete':
      return !hasSelected.value
    case 'copy':
      return !hasSelected.value
    default:
      return false
  }
}
</script>

<style scoped lang="scss">
.tool-box{
  position: absolute;
  bottom: 4px;
  left: 0;
  right: 0;
  margin: auto;
  width :500px;
  height :44px;
  border-radius: 12px;
  box-shadow: 0 0 10px 0 rgba(0,0,0,.4) ;
  z-index :90;
  display :flex;
  align-items: center;
  padding: 4px 20px;
  box-sizing: border-box;
  justify-content: space-around;
  overflow: auto;
  background-color: #fff;
  .el-button{
    margin: 0 4px;
  }
}
</style>
