<template>
  <el-table-column
    :prop="prop"
    :label="label"
    v-bind="column"
    v-on="$listeners"
  >
    <slot
      slot-scope="{ row, $index }"
      v-bind="{ row, $index }"
    />
    <template
      v-if="!column.children"
      slot-scope="{ row, $index }"
    >
      <template v-if="alwaysEditing || (column.editable && row.editable)">
        <component
          :is="component"
          v-model="row[prop]"
          :disabled="_getDisabledState(column)"
          v-bind="column.attrs"
          :prop="prop"
          :source="source"
          :row="row"
          v-on="column.listeners"
          @input="onChangeCellField($event, row, $index, prop)"
          @select="onChangeCellField($event, row, $index, prop)"
        />
        <span
          v-if="errorMessages[$index]"
          class="f-error"
        >
          {{ errorMessages[$index] || '--' }}
        </span>
      </template>
      <span
        v-else
        :style="getLabelStyle(row, prop)"
        v-html="column.formatter ? column.formatter(row[prop]) : row[prop]"
      />
    </template>
  </el-table-column>
</template>

<script>
import { getComponentByType } from './factory'
import { validate } from './validate'

export default {
  name: 'TrEditTableColumn',

  props: {
    column: {
      type: Object,
      default: () => ({
        prop: '',
        label: '',
        editable: true,
        formatter: null, // 格式化数据方法
        validate: [{ required: true, message: '不能为空' }],
        type: 'input', // 展示的组件, 默认input, 其他预设值: [input, date, time, checkbox, dateYMD, datetime], 除此之外, 还可以传入ElRate, ElSwitch,ElSider等任何挂在到全局的组件
        component: null, // 自定义传入的组件 当都满足不了需求 那么可以放入自定义组件来渲染自定义组件
        listeners: {}, // 监听的事件名, 比如 input focus blur, 或者是组件的自定义事件
        attrs: {} // 对el-table-column需要设置的属性, 比如 align fixed sortable 属性, 支持el-table-column属性
      })
    },
    disabled: {
      type: Boolean,
      default: false
    },
    alwaysEditing: {
      type: Boolean,
      default: false,
      comment: '是否始终处于编辑状态'
    },
    source: {
      type: Object,
      default: () => ({})
    }
  },

  data() {
    return { errorMessages: [] }
  },

  computed: {
    component() {
      return this.column.component || getComponentByType(this.column.type)
    },
    prop() {
      return this.column.prop
    },
    label() {
      return this.column.label
    }
  },

  methods: {
    _getDisabledState(column) {
      return this.disabled || column.attrs?.disabled
    },
    onValidate(value, row, index) {
      const message = validate(value, row, this.column.rules)

      this.$set(this.errorMessages, index, message)
      message && this.$set(row, 'VALIDATE_MESSAGE', message)
    },
    getLabelStyle(row, prop) {
      const style = this.column.style

      if (typeof style === 'function') {
        return style(row, prop)
      } else if (style) {
        return style
      }
    },
    onChangeCellField(value, row, index, prop) {
      this.onValidate(value, row, index)
      this.$emit('change-row', { prop, value, row, index })
    }
  }
}
</script>

<style scoped>
.f-error {
  display: block;
  padding: 5px;
  font-size: 12px;
  color: red;
}
</style>
