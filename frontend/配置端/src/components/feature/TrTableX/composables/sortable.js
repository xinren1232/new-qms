import { getCurrentInstance, watch } from 'vue'

import SortColumn from '../src/SortColumn'

export const useSetSortColumn = () => {
  const { columns = [] } = getCurrentInstance()?.proxy || {}
  // 添加拖拽排序列到列表中
  const unwatch = watch(
    () => columns,
    (cols) => {
      if (cols.length && !cols.find(({ prop }) => prop === SortColumn.prop)) {
        columns.unshift(SortColumn)
        unwatch?.()
      }
    },
    { immediate: true }
  )
}
