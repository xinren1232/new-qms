import { VXETable } from 'vxe-table'

VXETable.renderer.add('radioEditor', {
  renderEdit (h, renderOpts, { row, column }) {
    const { optionItems,disabled } = column.params

    return <el-radio-group class="flex justify-center" v-model={row[column.field]} disabled={!!disabled} style="padding:0 10px;overflow:hidden" >
      { optionItems.map(item => <el-radio label={item.value} key={item.value}>{item.label}</el-radio>)}
    </el-radio-group>
  },
  renderCell (h, renderOpts, { row, column }) {
    const { optionItems,disabled } = column.params

    return [
      <el-radio-group class="flex justify-center" v-model={row[column.field]} disabled={!!disabled} >
        { optionItems.map(item => <el-radio label={item.value} key={item.value}>{item.label}</el-radio>)}
      </el-radio-group>
    ]
  }
})
