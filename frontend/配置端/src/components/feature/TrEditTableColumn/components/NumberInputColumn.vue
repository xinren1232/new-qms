<template>
  <el-input-number
    :value="value"
    controls-position="right"
    :disabled="$attrs.disabled"
    :min="1"
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

<style lang="scss" scoped>
.el-input-number {
  width: 82px;
}
</style>
