<template>
  <el-drawer :visible.sync="showDrawer" :title="calcTitle">
    <node-info-panel v-show="currentComponent === 'node'" :data="data" :state-list="stateList" />
    <edge-info-panel v-show="currentComponent === 'edge'" :data="data" />
  </el-drawer>
</template>

<script setup>
import { ref, watch,computed } from 'vue'
import { activeCellState } from '../core/state'
import EdgeInfoPanel from './edge-info-panel.vue'
import NodeInfoPanel from './node-info-panel.vue'

defineProps({
  data: {
    type: Object,
    default: () => ({})
  },
  stateList: {
    type: Array,
    default: () => ([])
  }
})

const showDrawer = ref(false)
const currentComponent = ref(null)

watch(() => activeCellState.value, () => {
  if (!activeCellState.value) {
    showDrawer.value = false

    return
  }
  const { type } = activeCellState.value

  if (!type) {
    showDrawer.value = false

    return
  }
  showDrawer.value = true
  currentComponent.value = type
})

const calcTitle = computed(() => {
  if (currentComponent.value === 'node') {
    return '节点设置'
  }

  if (currentComponent.value === 'edge') {
    return '连线设置'
  }

  return ''
})

</script>
