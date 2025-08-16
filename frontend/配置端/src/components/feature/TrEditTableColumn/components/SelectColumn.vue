<template>
  <el-select
    v-model="selectValue"
    :placeholder="$t('common.pleaseSelect')"
    value-key="value"
    clearable
    v-bind="$attrs"
    @input="onInput(selectValue)"
    @clear="onClear"
  >
    <el-option
      v-for="item in data"
      :key="item.value"
      :label="calcLabel(item)"
      :value="item.value"
    />
  </el-select>
</template>

<script>
export default {
  inject: { rootEditTable: { default: '' } },
  // eslint-disable-next-line vue/require-prop-types
  props: ['value', 'data', 'prop', 'row', 'attrs'],
  data() {
    return { selectValue: '' }
  },
  watch: {
    value(now) {
      this.selectValue = now
    }
  },
  created() {
    this.selectValue = this.value
  },
  methods: {
    onInput(value) {
      this.$emit('input', value)
    },
    onClear() {
      this.$emit('input', '')
    },
    calcLabel(item) {
      return item.lang || item.label
    }
  }
}
</script>
