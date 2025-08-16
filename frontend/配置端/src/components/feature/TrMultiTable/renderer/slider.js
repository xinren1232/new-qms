import { VXETable } from 'vxe-table'


VXETable.renderer.add('sliderEditor', {
  renderEdit (h, renderOpts, { row, column }) {
    return <el-input
      v-model={row[column.field]}
      type="number"
      oninput={(value) => {
        if (!/^[0-9]+$/.test(value)) value = value.replace(/\D/g,'')
        if (value > 100) value = 100
        if (value < 0)value = 0
      }}
    />
  },
  renderCell (h, renderOpts, { row, column }) {
    row[column.field] = Number(row[column.field])

    return [
      <el-progress percentage={row[column.field]} />
    ]
  }
})
