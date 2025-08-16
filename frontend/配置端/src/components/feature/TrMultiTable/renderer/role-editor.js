import { VXETable } from 'vxe-table'
import TreeSelector from '../components/tree-selector'

const columns = [{ field: 'code','title': '角色' },{ field: 'enableFlag','title': '启用' }]

VXETable.renderer.add('ruleEditor', {
  renderEdit (h, renderOpts, { row, column }) {
    const { keyField,valueField,childrenField,data,parentField } = column.params.extendData.role

    return <TreeSelector
      title="角色选择"
      v-model={row[column.field]}
      source-data={data}
      effect="edit"
      row={row}
      column={column}
      columns={columns}
      value-field={valueField}
      key-field={keyField}
      multiple={column.params.multiple}
      childrenField={childrenField}
      parentField={parentField}
    ></TreeSelector>
  },
  // 可编辑显示模板
  renderCell (h, renderOpts, { row, column }) {
    const { keyField,valueField,childrenField,data,parentField } = column.params.extendData.role

    return [
     <TreeSelector
      title="角色选择"
      v-model={row[column.field]}
      source-data={data}
      effect="view"
      row={row}
      column={column}
      columns={columns}
      value-field={valueField}
      key-field={keyField}
      multiple={column.params.multiple}
      childrenField={childrenField}
      parentField={parentField}
    ></TreeSelector>
    ]
  }
})
