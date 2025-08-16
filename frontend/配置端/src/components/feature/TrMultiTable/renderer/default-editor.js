import { VXETable } from 'vxe-table'
// import cellEvent from '@@/feature/TrMultiTable/core/cellEvent'

VXETable.renderer.add('defaultEditor', {
  renderEdit (h, renderOpts, { row, column }) {
    return <el-input v-model={row[column.field]} />
  },
  renderCell (h, renderOpts, { row, column }) {
    return [
      <span>{row[column.field]}</span>
    ]
  }
})
