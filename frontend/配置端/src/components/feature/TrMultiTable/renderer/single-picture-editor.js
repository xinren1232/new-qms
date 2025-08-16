import { VXETable } from 'vxe-table'
import PictureEditorView from '../widget/picture-upload-single/view.vue'
import PictureEditorEdit from '../widget/picture-upload-single/edit.vue'


VXETable.renderer.add('singlePictureEditor', {
  renderEdit (h, renderOpts, { row, column }) {
    const { fileUploadConfig } = column.params.extendData || {}

    return <PictureEditorEdit v-model={row[column.field]} row={row} column={column} fileUploadConfig={fileUploadConfig}
    ></PictureEditorEdit>
  },
  renderCell (h, renderOpts, { row, column }) {
    return [
      <PictureEditorView v-model={row[column.field]} row={row} column={column}></PictureEditorView>
    ]
  }
})
