import { VXETable } from 'vxe-table'
import SuperSelector from '../widget/super-selector/index.vue'
import SuperSelectorView from '../widget/super-selector/view.vue'


VXETable.renderer.add('superSelectEditor', {
  renderEdit (h, renderOpts, { row, column }) {

    return <SuperSelector
      v-model={row[column.field]}
      row={row}
      column={column}
    ></SuperSelector>
  },
  // 可编辑显示模板
  renderCell (h, renderOpts, { row, column }) {
    return [
     <SuperSelectorView
      v-model={row[column.field]}
      row={row}
      column={column}
    ></SuperSelectorView>
    ]
  }
})
