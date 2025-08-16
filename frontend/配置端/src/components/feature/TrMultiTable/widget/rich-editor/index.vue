<script setup>
import { ref } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
// import { DomEditor } from '@wangeditor/editor'
import '@wangeditor/editor/dist/css/style.css'


defineProps({
  value: {
    type: String,
    default: ''
  },
  row: {
    type: Object,
    default: () => ({})
  },
  column: {
    type: Object,
    default: () => ({})
  }
})
const showEditor = ref(true)
const toolBarRef = ref(null)


const editor = ref(null)

const toolbarConfig = ref({
  insertKeys: {
    index: 1,
    keys: ['fontSize']
  },
  excludeKeys: ['insertVideo','header1', 'header2', 'header3','headerSelect', 'blockquote', '|', '|', 'lineHeight', '|', 'emotion', 'bulletedList', 'numberedList', 'todo', 'group-indent','group-image','group-video', 'group-more-style', '|', 'insertLink', 'insertTable', 'codeBlock', 'divider', '|', 'undo', 'redo', '|', 'fullScreen']
})

const onCreated = editors => {
  editor.value = Object.seal(editors)
  // setTimeout(() => {
  //   const toolbar = DomEditor.getToolbar(editor.value)
  //
  //   const curToolbarConfig = toolbar.getConfig()
  //
  //   console.log(curToolbarConfig.toolbarKeys)
  // },1000)
}

</script>

<template>
  <el-tooltip
    v-model="showEditor"
    popper-class="el-picker-panel"
    class="item"
    effect="light"
    placement="top"
    manual
    :visible-arrow="false"
    :offset="20"
  >
    <template #content>
      <Toolbar
        ref="toolBarRef"
        style="border-bottom: 1px solid #ccc"
        :editor="editor"
        :default-config="toolbarConfig"
        mode="simple"
      />
      <Editor
        v-model="row[column.field]"
        style="height: 220px;width: 540px"
        @onCreated="onCreated"
      />
    </template>
    <el-input v-model="row[column.field]" readonly />
  </el-tooltip>
</template>

<style scoped lang="scss">


</style>
