import { VXETable } from 'vxe-table'
import PictureEditorView from '../widget/picture-upload/view.vue'
import PictureEditorEdit from '../widget/picture-upload/edit.vue'


VXETable.renderer.add('pictureEditor', {

  renderEdit (h, renderOpts, { row, column }) {
    const { fileUploadConfig } = column.params.extendData || {}

    return <PictureEditorEdit image-list={row[column.field]} row={row} column={column}
                              fileUploadConfig={fileUploadConfig}
    ></PictureEditorEdit>
  },
  renderCell (h, renderOpts, { row, column }) {
    return [
      <PictureEditorView image-list={row[column.field]} row={row} column={column}></PictureEditorView>
    ]
  }
})
