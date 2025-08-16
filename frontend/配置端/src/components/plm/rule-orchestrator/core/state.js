import { ref } from 'vue'

// 重做撤销装填
// 是否可以回退和前进状态管理
const canUndo = ref(false)
const canRedo = ref(false)

// 有选中的节点或者边
const hasSelected = ref(false)

// 单选选中的节点state
const activeCellState = ref({})

export {
  canUndo,
  canRedo,
  hasSelected,
  activeCellState
}
