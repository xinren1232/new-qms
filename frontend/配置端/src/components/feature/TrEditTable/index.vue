<template>
  <el-table
    ref="table"
    v-bind="$attrs"
    :data="data"
    border
    size="mini"
    class="tr-edit-table"
    style="width: 100%"
    v-on="$listeners"
    @row-dblclick="onDblClickRow"
  >
    <slot name="before" />
    <tr-edit-table-column
      v-for="(column, index) in columns"
      :key="index"
      :column="column"
      :source="column"
      :disabled="disabled"
      :always-editing="alwaysEditing"
      @change-row="onChangeRow"
    />

    <tr-default-page
      v-if="showNoData"
      slot="empty"
      type="nodata"
      :title="$t('common.noData')"
      :icon-size="flg ? 200 : 100"
    />
    <slot name="after" />
  </el-table>
</template>

<script>
// import TrNoData from '@@/feature/TrNoData'
import TrDefaultPage from '@@/feature/TrDefaultPage'
import TrEditTableColumn from '@@/feature/TrEditTableColumn'
import { validate } from '@@/feature/TrEditTableColumn/validate'

export default {
  components: {
    TrEditTableColumn,
    // TrNoData
    TrDefaultPage
  },
  model: { prop: 'data' },
  props: {
    data: {
      type: Array,
      default: () => [],
      comment: '最终需要展示的数据'
    },
    disabled: {
      type: Boolean,
      default: false,
      comment: '是否禁用表格编辑 禁用后 编辑事件不再响应'
    },
    source: {
      type: Object,
      default: () => ({})
    },
    columns: {
      type: Array,
      default: () => [],
      comment: `列数据, 对el-table-column需要设置的属性, 比如 align fixed sortable 属性,
      // 详情见TrEditTableColumn。数据项如下:
      {
        prop: '', // 绑定的字段
        label: '', // 标签文本
        type: 'input', // 展示的组件, 默认input, 其他预设值: [input, date, time, checkbox, dateYMD, datetime], 除此之外, 还可以传入ElRate, ElSwitch,ElSider等任何挂在到全局的组件
        component: null, // 自定义传入的组件 当都满足不了需求 那么可以放入自定义组件来渲染自定义组件
        listeners: {}, // 监听的事件名, 比如 input focus blur, 或者是组件的自定义事件
        attrs: {}, // 对显示的组件需要设置的属性, 比如 placeholder type disabled 属性, 支持element表单属性
      }
      `
    },
    alwaysEditing: {
      type: Boolean,
      default: false,
      comment: '是否始终处于编辑状态'
    },
    showNoData: {
      type: Boolean,
      default: true,
      comment: '是否启用nodata'
    }
  },

  data() {
    return {
      currentRow: null,
      flg: true,
      columnsLength: ''
    }
  },

  computed: {
    columnMap() {
      const map = {}

      this.columns.forEach((column) => {
        map[column.prop] = column
      })

      return map
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.columnsLength = this.$refs?.table?.columns?.length
    })
    this.getColumnslen()
  },
  methods: {
    /**
     * 当行对象发生变化时
     * @param {object} rowConf 编辑行
     * @param {object} rowConf.row 行对象
     * @param {string} rowConf.prop 字段
     * @param {any} rowConf.value 值
     * @param {object} rowConf.index 行索引
     */
    onChangeRow(rowConf) {
      this.$emit('change-row', rowConf)
    },
    getRowByColumns() {
      return this.columns.reduce((prev, next) => {
        prev[next.prop] = ''

        return prev
      }, {})
    },
    validate() {
      for (let i = 0; i < this.data.length; i++) {
        const node = this.data[i]

        for (const key in node) {
          const columnOption = this.columnMap[key]

          if (columnOption) {
            const errorMessage = validate(node[key], node, columnOption.rules)

            if (errorMessage) {
              return `${columnOption.label}${errorMessage}`
            }
          }
        }
      }

      return ''
    },
    exportExcel(options) {
      this.$refs.table.exportExcel(options)
    },

    // 双击行编辑数据
    async onDblClickRow(row, column, event) {
      const precondition = this.$attrs['on-dblclick']

      if (this.disabled || (precondition !== undefined && !(await precondition?.(row, column, event)))) return
      if (!this.alwaysEditing && !row.editable && this.data.some((it) => it.editable)) {
        this.$message.error('请先保存数据后重试！')

        return
      }

      this.$set(row, 'editable', true)
      this.currentRow = row
    },
    $onSaveRow(row) {
      if (row?.VALIDATE_MESSAGE) {
        this.$message.error(row?.VALIDATE_MESSAGE)

        return
      }

      this.$set(row, 'editable', false)
    },
    $createRow(rows, pos = 'after') {
      if (!this.alwaysEditing && this.data.some((it) => it.editable)) {
        this.$message.error('请先保存数据,再重试！')

        return
      }

      if (!rows) {
        rows = [this.getRowByColumns()]
      } else if (!Array.isArray(rows)) {
        rows = [rows]
      }

      const METHOD = pos === 'after' ? 'push' : 'unshift'

      rows.forEach((row) => {
        row.editable = true
        this.data[METHOD](row)
      })

      return rows
    },
    $removeRow(row) {
      const index = this.data.indexOf(row)

      this.data.splice(index, 1)
    },
    getColumnslen() {
      this.$nextTick(() => {
        const columnsLength = this.$refs?.table?.columns?.length

        this.flg = !!(columnsLength === 0 || columnsLength > 2)
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.tr-edit-table {
  vertical-align: middle;
  border: none;
  .el-input__inner {
    line-height: 26px;
    height: 26px;
  }
  td {
    vertical-align: top;
    line-height: 26px;
    padding: 3px 0;
  }
  ::v-deep th,
  ::v-deep td {
    border-right: none;
  }
}

.tr-edit-table th.required-header > div::before {
  content: '*';
  color: #ff4949;
  margin-right: 4px;
}
</style>
