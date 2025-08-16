// eslint-disable-next-line no-unused-vars
import { defineComponent, h } from 'vue'
import { Column } from 'vxe-table'

const render = function () {
  const {
    prop = '',
    label = '',
    formatType = '', // 需要格式化的列数据
    formatter,
    showOverflowTooltip,
    slots = {},
    format = () => '',
    table_format_list = [],
    ...attrs
  } = this.$attrs

  return (
    <Column
      {...{
        props: {
          field: prop,
          title: label,
          showOverflow: showOverflowTooltip,
          // 只有设置了 edit 插槽点击的时候才允许编辑
          editRender: slots.edit ? {} : undefined,
          // 如果存在格式化器则包装一下，简化获取参数的方式，防止声明无用参数
          ...(formatter && {
            formatter: ({ callValue, column, row }) => formatter([callValue || row[column.property], row, column])
          }),
          ...(table_format_list.includes(formatType) && {
            formatter: ({ row }) => format(row, this.$attrs)
          }),
          ...attrs
        },

        scopedSlots: {
          ...slots,
          ...(slots.default && {
            default: ({ column, row, rowIndex }) => {
              return slots.default([row[column.property], row, column, rowIndex])
            }
          }),
          // 方便自定义渲染值
          ...(slots.edit && {
            edit: ({ column, row, rowIndex }) => {
              return slots.edit([row[column.property], row, column, rowIndex])
            }
          })
        }
      }}
    />
  )
}

export default defineComponent({
  inheritAttrs: false,
  render
})
