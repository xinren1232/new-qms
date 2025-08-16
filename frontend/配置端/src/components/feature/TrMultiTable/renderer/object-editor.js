import { VXETable } from 'vxe-table'
import TreeSelector from '../components/tree-selector'

const columns = [{ field: 'modelCode','title': '对象编码' },{ field: 'enableFlag','title': '启用' }]

VXETable.renderer.add('objectEditor', {
  renderEdit (h, renderOpts, { row, column }) {
    const { keyField,valueField,childrenField,data,parentField } = column.params.extendData.object

    return <TreeSelector
      title="对象选择"
      v-model={row[column.field]}
      source-data={data}
      effect="edit"
      row={row}
      column={column}
      parentField={parentField}
      columns={columns}
      value-field={valueField}
      key-field={keyField}
      multiple={column.params.multiple}
      childrenField={childrenField}
    ></TreeSelector>
  },
  // 可编辑显示模板
  renderCell (h, renderOpts, { row, column }) {
    const { keyField,valueField,childrenField,data,parentField } = column.params.extendData.object

    return [
     <TreeSelector
      title="对象选择"
      v-model={row[column.field]}
      source-data={data}
      effect="view"
      row={row}
      column={column}
      columns={columns}
      value-field={valueField}
      key-field={keyField}
      parentField={parentField}
      multiple={column.params.multiple}
      childrenField={childrenField}
    ></TreeSelector>
    ]
  }
})
