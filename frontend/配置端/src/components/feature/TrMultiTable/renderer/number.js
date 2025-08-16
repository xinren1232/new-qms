import { VXETable } from 'vxe-table'


VXETable.renderer.add('numberEditor', {
  renderEdit (h, renderOpts, { row, column }) {
    return <el-input-number v-model={row[column.field]} />
  },
  renderCell (h, renderOpts, { row, column }) {
    return [
      <span>{row[column.field]}</span>
    ]
  }
})
