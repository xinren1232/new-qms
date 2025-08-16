import { VXETable } from 'vxe-table'
import FileEditorEdit from '../widget/file-upload/edit.vue'
import FileEditorView from '../widget/file-upload/view.vue'


VXETable.renderer.add('fileEditor', {
  renderEdit (h, renderOpts, { row, column }) {
    const { fileUploadConfig } = column.params.extendData || {}

    return <FileEditorEdit file-list={row[column.field]} row={row} column={column} fileUploadConfig={fileUploadConfig}></FileEditorEdit>
  },
  renderCell (h, renderOpts, { row, column }) {
    return [
      <FileEditorView file-list={row[column.field]} row={row} column={column}></FileEditorView>
    ]
  }
})
