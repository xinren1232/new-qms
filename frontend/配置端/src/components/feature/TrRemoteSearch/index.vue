<template>
  <el-select
    ref="select"
    v-model="modelValue"
    :loading="loading"
    class="tr-dict-select"
    :placeholder="placeholder"
    :remote-method="debounceQuery"
    :value-key="valueField"
    reserve-keyword
    filterable
    clearable
    remote
    v-bind="$attrs"
    :disabled="disabled"
    @input="onInput"
    @clear="onClear"
  >
    <el-option
      v-for="item in options"
      :key="item[valueField]"
      :label="getLabel(item)"
      :value="getValue(item)"
    />
  </el-select>
</template>

<script>
import { debounce } from 'lodash'

export default {
  inheritAttrs: false,
  props: {
    value: {
      type: [Number, String, Array, Object],
      default: '',
      comment: '数据双向绑定的值'
    },
    labelField: {
      type: String,
      default: 'label',
      comment: '以哪个字段值作为显示'
    },
    valueField: {
      type: String,
      default: 'value',
      comment: '以哪个值作为保存'
    },
    valueType: {
      type: String,
      default: 'string',
      comment: '返回值的对象类型：string/object'
    },
    formatter: {
      type: Function,
      default: null,
      comment: '自定义渲染显示label的文本'
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      options: [],
      loading: false
    }
  },

  computed: {
    debounceQuery() {
      return debounce(this.query, 300, { leading: true, trailing: false })
    },
    placeholder() {
      return this.$attrs.disabled ? '' : this.$t('common.pleaseInput')
    },
    optionMap() {
      const map = {}

      this.options.forEach((option) => {
        map[option[this.valueField]] = option
      })

      return map
    },
    modelValue: {
      get() {
        return this.value
      },
      set(value) {
        this.$emit('input', value)
      }
    }
  },

  methods: {
    onInput(value) {
      const selectData = Array.isArray(value) ? value.map((val) => this.optionMap[val]) : this.optionMap[value]

      this.$emit('select', selectData || { [this.valueField]: '' })
      this.$emit('input', value)
    },
    onClear() {
      const selectData = this.$attrs.multiple ? [] : { [this.valueField]: '' }

      this.$emit('select', selectData)
      this.$emit('input', this.$attrs.multiple ? [] : '')
    },

    query(value) {
      value = value.toString()
      const httpRequest = this.$attrs['remote-method']

      if (!httpRequest) return

      this.loading = true
      httpRequest(value)
        .then(({ data }) => {
          this.options = data
        })
        .finally(() => {
          this.loading = false
        })
    },
    getValue(item) {
      if (this.valueType === 'object') {
        return item
      } else {
        return item[this.valueField]
      }
    },
    getLabel(item) {
      if (this.formatter) {
        return this.formatter(item)
      }

      return this.labelField === this.valueField
        ? item[this.valueField]
        : `(${item[this.valueField]})${item[this.labelField]}`
    }
  }
}
</script>
