<template>
  <div class="vue-editor__mantle">
    <span style="color: #E36049">Failed to compile.</span>
    <br>
    <br>
    <slot name="strError" />
  </div>
</template>

<script>
export default {
  name: 'ErrorMantle'
}
</script>

<style lang="scss" scoped>
.vue-editor__mantle {
  background-color: rgba(0, 0, 0, 0.85);
  position: absolute;
  z-index: 990;
  box-sizing: border-box;
  inset: 0px;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.85);
  color: rgb(232, 232, 232);
  font-family: Menlo, Consolas, monospace;
  font-size: large;
  padding: 10px;
  line-height: 1.2;
  white-space: pre-wrap;
  overflow: auto;
}
</style>
