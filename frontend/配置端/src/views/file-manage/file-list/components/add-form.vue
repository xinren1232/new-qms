<script setup>
import TrForm from '@@/feature/TrForm/index.js'
import TrDict from '@@/feature/TrDict'
import MinoUploader from '@@/plm/mino-uploader/index.vue'

defineProps({
  formData: {
    type: Object,
    default: () => ({})
  }
})
const formRef = shallowRef(null)

const formRules = {
  name: { required: true, message: '请输入文件名称', trigger: 'blur' },
  type: { required: true, message: '请选择文件类型', trigger: 'change' }
}

const formItems = [
  { label: '文件名称', prop: 'name', type: 'input', span: 12, attrs: { maxlength: 50 } },
  { label: '文件类型', prop: 'type', component: TrDict, span: 12, attrs: { type: 'METHOD_TYPE' } },
  { label: '上传文件', prop: 'files', component: MinoUploader,span: 24 },
  { label: '描述', prop: 'description', span: 24, attrs: { type: 'textarea', rows: 3, maxlength: 1000, showWordLimit: true } }
]

const validate = () => {
  return formRef.value.validate()
}

defineExpose({
  validate
})

</script>

<template>
  <tr-form
    ref="formRef"
    v-model="formData"
    class="w-1000px"
    :rules="formRules"
    :items="formItems"
    :show-buttons="false"
  />
</template>
