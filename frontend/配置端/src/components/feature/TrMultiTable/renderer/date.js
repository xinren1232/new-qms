import { VXETable } from 'vxe-table'


VXETable.renderer.add('dateEditor', {
  renderEdit (h, renderOpts, { row, column }) {
    return <el-date-picker
      v-model={row[column.field]}
      type="date"
      format="yyyy-MM-dd"
      value-format="yyyy-MM-dd"
    />
  },
  renderCell (h, renderOpts, { row, column }) {
    return [
      <span>{row[column.field]}</span>
    ]
  }
})
