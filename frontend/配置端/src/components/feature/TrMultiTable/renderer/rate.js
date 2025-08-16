import { VXETable } from 'vxe-table'


VXETable.renderer.add('rateEditor', {
  renderEdit (h, renderOpts, { row, column }) {
    return <el-rate v-model={row[column.field]} />
  },
  renderCell (h, renderOpts, { row, column }) {
    const { disabled } = column.params

    return [
      <el-rate v-model={row[column.field]} disabled={!!disabled} />
    ]
  }
})
