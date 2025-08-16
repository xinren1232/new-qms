<template>
  <div class="flow-box flex relative">
    <!--  节点区域   -->
    <node-container :state-list="stateList" />
    <!-- 工具操作区域    -->
    <tools-container ref="toolsRef" @export-data="onExportData" />
    <div class="content">
      <div id="container" />
    </div>
    <cell-drawer :data="data" :state-list="stateList" />
  </div>
</template>

<script setup>
import { watch, nextTick } from 'vue'
import NodeContainer from './components/node-container.vue'
import ToolsContainer from './components/tools-container.vue'
import CellDrawer from './components/cell-drawer.vue'
import FlowGraph from './core'
import route from '@/router'

// 获取container的宽度和高度
let width,height

const id = route.currentRoute?.params?.id
const version = route.currentRoute?.params?.version
const props = defineProps({
  stateList: {
    type: Array,
    default: () => ([])
  },
  data: {
    type: Object,
    default: () => ({})
  }
})
const emits = defineEmits(['export-data'])

const unwatch = watch(() => props.data, () => {
  if (!id || !version) return
  // 销毁实例
  document.getElementById('container').innerHTML = ''
  FlowGraph.init(document.getElementById('container'), width, height)
  FlowGraph.initGraphShape(props.data)
  setTimeout(() => {
    FlowGraph.graph.centerContent()
    if (unwatch) unwatch()
  }, 500)
}, { deep: true })

nextTick(() => {
  const { offsetWidth, offsetHeight } = document.getElementById('container')

  width = offsetWidth
  height = offsetHeight
  FlowGraph.init(document.getElementById('container'), width, height)
})
const onExportData = (data) => {
  emits('export-data', data)
}

// 设置图标外部容器的大小content
const setContainerSize = (w,h) => {
  const content = document.querySelector('.content')

  content.style.width = w
  content.style.height = h
}

onBeforeUnmount(() => {
  FlowGraph.destroy()
})

defineExpose({
  setContainerSize
})
</script>

<style lang="scss" scoped>
.content {
  height: calc(100vh - 230px);
}
</style>
