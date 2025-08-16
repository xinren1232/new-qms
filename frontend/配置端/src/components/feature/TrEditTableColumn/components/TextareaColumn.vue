<template>
  <el-input
    type="textarea"
    autosize
    :value="value"
    :disabled="$attrs.disabled"
    :min="1"
    :max="200"
    :maxlength="$attrs.maxlength"
    @input="onInput"
    @blur="onInput(value)"
  />
</template>

<script>
export default {
  // eslint-disable-next-line vue/require-prop-types
  props: ['value'],
  methods: {
    onInput(value) {
      this.$emit('input', value)
    }
  }
}
</script>
