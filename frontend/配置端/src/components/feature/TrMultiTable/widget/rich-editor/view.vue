<template>
  <div class="rich-text">
    <el-input
      v-model="fieldModel"
      type="textarea"
      :disabled="disabled"
      :rows="field.rows"
      :placeholder="field.placeholder"
      :maxlength="field.maxlength"
      :show-word-limit="field.showWordLimit"
      :autosize="field.autosize"
      :resize="field.resize"
      :readonly="field.readonly"
      :autosize="{ minRows: 2, maxRows: 4 }"
    />
  </div>
</template>
<script>
export default {
  name: 'RichTextView'
}
</script>
<script setup>

</script>
<style scoped lang="scss">

</style>
