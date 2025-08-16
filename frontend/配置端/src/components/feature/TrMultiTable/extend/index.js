import { VXETable } from 'vxe-table'
import { Message } from 'element-ui'

export function injectExtendComponent(extendsComponentList = []) {
  if (!extendsComponentList || extendsComponentList.length === 0) {
    return
  }
  extendsComponentList.forEach(item => {
    injectComponent(item)
  })
}

function injectComponent(component) {
  const { name,component: { view: viewRender,edit } } = component

  if (!name || !component) {
    Message.error('TrMultiTable::inject extend component error,  name or component is null')

    return
  }

  if (!viewRender || !edit || !component) {
    Message.error('TrMultiTable::inject extend component error,  view or edit is null')

    return
  }
  VXETable.renderer.add(name, {
    renderEdit (h, renderOpts, { row, column }) {
      return <edit v-model={row[column.field]} row={row} column={column}></edit>
    },
    renderCell (h, renderOpts, { row, column }) {
      return [
        <viewRender v-model={row[column.field]} row={row} column={column}></viewRender>
      ]
    }
  })
}
