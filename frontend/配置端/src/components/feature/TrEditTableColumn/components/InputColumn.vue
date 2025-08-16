<template>
  <component
    :is="renderComponent"
    v-bind="$attrs"
    :value="value"
    :source="source"
    @input="onInput"
    @blur="onInput(value)"
  />
</template>

<script>
import FormatterInput from '@@/special/TrPainter/components/TrInput'

export default {
  components: { FormatterInput },
  // eslint-disable-next-line vue/require-prop-types
  props: ['value', 'source'],
  computed: {
    renderComponent({ $attrs: { conversion, format } }) {
      return format || conversion ? 'FormatterInput' : 'el-input'
    }
  },
  methods: {
    onInput(value) {
      this.$emit('input', value)
    }
  }
}
</script>
