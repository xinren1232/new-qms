<template>
  <el-dialog
    :visible.sync="visible"
    title="代码编辑"
    width="1200px"
    :close-on-click-modal="false"
  >
    <tr-form v-model="form" :items="formItems" :show-buttons="false" />
    <code-editor ref="codeEditorRef" class="editor-container" :code="form.content" height="500px" :options="editorOptions" />
    <el-row class="flex justify-end margin-top">
      <el-button @click="onCancel">取消</el-button>
      <el-button type="primary" @click="onConfirm">确定</el-button>
    </el-row>
  </el-dialog>
</template>

<script setup>
import { ref, nextTick, computed } from 'vue'
import TrForm from '@@/feature/TrForm'
import TrSelect from '@@/feature/TrSelect'
import CodeEditor from '@@/plm/code-editor/index.vue'
import { Message, Loading } from 'element-ui'
import { createOrUpdateAttribute } from '@/views/system-manage/method-manage/api'
import { parseString } from '@/utils'
import LengthConfig from '@/config/constraint.config.js'

const visible = ref(false)
const form = ref({})
const codeEditorRef = ref(null)

const emit = defineEmits(['edit-content-success'])

const formItems = computed(() => {
  return [
    { label: '关联类名', prop: 'referenceClassName', span: 8, attrs: { maxlength: LengthConfig.TEXT, placeholder: '最大长度50',clearable: true } },
    {
      label: '异步执行',
      prop: 'sync',
      component: TrSelect,
      span: 8,
      attrs: { data: [{ label: '是',value: true },{ label: '否',value: false }],clearable: true }
    },
    { label: '标签', prop: 'tag' ,span: 8,attrs: { placeholder: `请输入标签，以逗号隔开，最大长度${LengthConfig.TEXT}` } }
  ]
})

const editorOptions = computed(() => {
  console.log(form.value.languageType)

  return {
    language: form.value.languageType?.toLowerCase()
  }
})
const onCancel = () => {
  form.value = {}
  visible.value = false
}
const onConfirm = async () => {
  form.value.content = codeEditorRef.value.getCode()
  form.value.tag = JSON.stringify(form.value.tag)

  const loading = Loading.service({ target: '.el-dialog__body', text: '保存中...' })
  const { success } = await createOrUpdateAttribute(form.value).finally(() => {
    nextTick(() => {
      loading.close()
    })
  })

  if (success) {
    loading.close()
    Message.success('保存成功')
    visible.value = false
    emit('edit-content-success')
  }
}

const show = (row) => {
  row.tag = parseString(row.tag,{})
  form.value = row
  visible.value = true
  nextTick(() => {
    codeEditorRef.value.setCode(row?.content || '')
  })
}

defineExpose({
  show
})
</script>

<style scoped lang="scss">
.editor-container{
  border-radius: 4px;
  overflow: hidden;
}

</style>
