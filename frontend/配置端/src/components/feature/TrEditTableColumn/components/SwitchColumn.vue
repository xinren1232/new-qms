<template>
  <el-switch
    :value="value"
    active-color="#13ce66"
    inactive-color="#ff4949"
    :active-value="column.activeValue || 'enable'"
    :inactive-value="column.inactiveValue || 'disable'"
    :disabled="$attrs.disabled"
    @change="onInput"
  />
</template>

<script>
export default {
  // eslint-disable-next-line vue/require-prop-types
  props: ['value', 'column'],
  methods: {
    onInput(value) {
      this.$emit('input', value)
    }
  }
}
</script>
