<script setup>

defineProps({
  value: {
    type: String,
    default: 'Hello World'
  }
})
</script>

<template>
  <div class="custom-span">
    <span style="color:red">{{ value }}</span>
  </div>
</template>

<style scoped lang="scss">

</style>
