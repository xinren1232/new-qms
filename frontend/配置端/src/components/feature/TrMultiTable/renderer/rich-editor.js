import { VXETable } from 'vxe-table'
import RichEditor from '../widget/rich-editor'


VXETable.renderer.add('richEditor', {
  renderEdit (h, renderOpts, { row, column }) {
    return <RichEditor v-model={row[column.field]} row={row} column={column} ></RichEditor>
  },
  renderCell (h, renderOpts, { row, column }) {
    return [
      <span domProps={{ innerHTML: row[column.field] }} />
    ]
  }
})
