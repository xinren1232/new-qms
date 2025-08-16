import { VXETable } from 'vxe-table'


VXETable.renderer.add('switchEditor', {
  renderEdit (h, renderOpts, { row, column }) {
    return <el-switch v-model={row[column.field]} disabled={column.params.disabled}/>
  },
  renderCell (h, renderOpts, { row, column }) {
    return [
      <el-switch v-model={row[column.field]} disabled={column.params.disabled}/>
    ]
  }
})
