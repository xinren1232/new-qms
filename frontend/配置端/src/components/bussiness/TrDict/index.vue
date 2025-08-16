<template>
  <tr-select
    :value="value"
    :data="options"
    v-bind="$attrs"
    :clearable="clearable"
    :label-field="labelField"
    :value-field="valueField"
    :placeholder="placeholder"
    :title="($attrs.showTitle || $attrs.title)"
    @input="onInput"
    v-on="$listeners"
  />
</template>

<script>
import TrSelect from '@@/feature/TrSelect'

export default {
  name: 'TrDict',
  components: { TrSelect },
  inject: { elForm: { default: '' } },
  inheritAttrs: false,
  props: {
    value: {
      type: [String, Number, Array, Object],
      default: null,
      comment: '表单数据值'
    },
    data: {
      type: [Array, String],
      default: null,
      comment: '特殊情况可能需要自己传递data数组'
    },
    type: {
      type: String,
      default: '',
      comment: '字典类型值，每个字典项对应一个类型'
    },
    labelField: {
      type: String,
      default: 'value',
      comment: '字段名'
    },
    valueField: {
      type: String,
      default: 'key',
      comment: '字段值'
    },
    clearable: {
      type: Boolean,
      default: true,
      comment: '清空选项值'
    },
    noShowLabel: {
      type: Array,
      default: () => [],
      comment: '不展示的值'
    }
  },

  computed: {
    options() {
      if (typeof this.data === 'string') {
        return JSON.parse(this.data)
      }

      const options = (this.data?.length ? this.data : this.$store.getters.dict[this.type]) || []

      return options.filter(opt => !this.noShowLabel.includes(opt[this.labelField]))
    },
    placeholder({ $attrs, elForm }) {
      return $attrs.disabled || elForm?.disabled ? '' : this.$t('common.pleaseInput')
    }
  },
  created() {
    !this.data && this.$store.dispatch('dict/queryDict', this.type)
  },

  methods: {
    onInput(value) {
      this.$emit('input', value)
    }
  }
}
</script>
