// import XEUtils from 'xe-utils'
import { Colgroup,
  Column,
  Edit,
  // 功能模块
  // Icon,
  Filter,
  Footer,
  Header,
  Menu,
  Table,
  // Export,
  // Keyboard,
  Tooltip,
  Validator,
  // 全局对象
  VXETable } from 'vxe-table'
import VXETablePluginElement from 'vxe-table-plugin-element'
import 'vxe-table-plugin-element/dist/style.css'
// ...

import i18n from '@/i18n/index.js'

import TableComponent from './src/Table'

VXETable.setup({
  // 对组件内置的提示语进行国际化翻译
  i18n: (key, args) => i18n.t(key, args),
  // vxeTable 在el-tab中使用时，autoResize====true，会导致内存溢出，el-tab设置异步async
  table: {
    autoResize: false,
    resizeConfig: {
      refreshDelay: 1000
    }
  }
})
VXETable.use(VXETablePluginElement)

export function installTable(app) {
  app
    .use(Header)
    .use(Footer)
    .use(Filter)
    .use(Tooltip)
    .use(Column)
    .use(Validator)
    .use(Edit)
    .use(Menu)
    .use(Table)
    .use(Colgroup)
}

export { default as TableColumn } from './src/Column'
export default TableComponent
