<template>
  <el-date-picker
    v-model="defaultValue"
    :disabled="$attrs.disabled"
    type="date"
    value-format="yyyy-MM-dd"
  />
</template>

<script>
export default {
  // eslint-disable-next-line vue/require-prop-types
  props: ['value'],
  computed: {
    defaultValue: {
      get({ value }) {
        return ['null', ''].includes(value) ? null : value
      },
      set(value) {
        this.$emit('input', value)
      }
    }
  }
}
</script>
