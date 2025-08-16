import { VXETable } from 'vxe-table'
import { set } from 'vue'

VXETable.renderer.add('checkboxEditor', {
  renderEdit (h, renderOpts, { row, column }) {
    const { optionItems,disabled } = column.params

    return <el-checkbox-group class="flex justify-center" v-model={row[column.field]} disabled={!!disabled} style="padding:0 10px;overflow:hidden" >
      { optionItems.map(item => <el-checkbox label={item.value} key={item.value}>{item.label}</el-checkbox>)}
    </el-checkbox-group>
  },
  renderCell (h, renderOpts, { row, column }) {
    const { optionItems,disabled } = column.params

    if (!row?.[column.field] || !Array.isArray(row?.[column.field])) set(row,column.field,[])

    return [
      <el-checkbox-group class="flex justify-center" v-model={row[column.field]} disabled={!!disabled} >
        { optionItems.map(item => <el-checkbox label={item.value} key={item.value}>{item.label}</el-checkbox>)}
      </el-checkbox-group>
    ]
  }
})
