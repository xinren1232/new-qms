<script setup>
import TrForm from '@@/feature/TrForm/index.js'

defineProps({
  formData: {
    type: Object,
    default: () => ({})
  }
})
const formRef = shallowRef(null)

const formRules = {
  name: { required: true, message: '请输入查看器名称', trigger: 'blur' },
  url: { required: true, message: '请输入查看器URL', trigger: 'blur' }
}

const formItems = [
  { label: '查看器名称', prop: 'name', type: 'input', span: 12, attrs: { maxlength: 50 } },
  { label: 'URL', prop: 'url', type: 'input', span: 12 },
  { label: '描述', prop: 'description', span: 24, attrs: { type: 'textarea', rows: 3, maxlength: 1000, showWordLimit: true } }
]

defineExpose({
  validate() {
    return formRef.value.validate()
  }
})

</script>

<template>
  <tr-form
    ref="formRef"
    v-model="formData"
    class="w-800px"
    :rules="formRules"
    :items="formItems"
    :show-buttons="false"
  />
</template>
