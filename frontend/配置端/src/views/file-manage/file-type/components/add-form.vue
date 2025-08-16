<script setup>
import { Message } from 'element-ui'

import TrForm from '@@/feature/TrForm/index.js'
import TrDict from '@@/bussiness/TrDict/index.js'
import TrSelect from '@@/feature/TrSelect/index.js'
import { integerNumber } from '@/utils/regexp/index.js'

const props = defineProps({
  formData: {
    type: Object,
    default: () => ({})
  },
  codeDisabled: {
    type: Boolean,
    default: false
  }
})

const formRef = shallowRef()

const formRules = {
  name: { required: true, message: '请输入文件库名称', trigger: 'blur' },
  suffixName: { required: true, message: '请输入文件库名称', trigger: 'blur' },
  priority: { required: true, message: '请输入优先级(正整数)', pattern: integerNumber, trigger: 'blur' },
  code: { required: true, message: '请输入标识', trigger: 'blur' },
  readRule: { pattern: integerNumber, message: '请输入正整数', trigger: 'blur' }
}

const formItems = computed(() => [
  { label: '文件类型名称', prop: 'name', span: 12, attrs: { maxlength: 50 } },
  { label: '后缀名(支持正则)', prop: 'suffixName', span: 12 },
  { label: '读取规则', prop: 'readRule', span: 12, attrs: { type: 'number', min: 0 } },
  { label: '匹配', prop: 'matching', span: 12 },
  { label: '优先级', prop: 'priority', span: 12, attrs: { type: 'number', min: 0 } },
  { label: 'MIME类型', prop: 'mimeType', component: TrDict, span: 12, attrs: { type: 'MIME_TYPE', labelField: 'zh', valueField: 'keyCode' } },
  { label: '存储规则', prop: 'storageRule', component: TrSelect, span: 12, attrs: {
    data: [
      // { label: 'IP归属地', value: 'IP' },
      { label: 'Base归属地', value: 'Base' }
    ]
  } },
  { label: '标识', prop: 'code', span: 12, attrs: { disabled: props.codeDisabled } },
  { label: '描述', prop: 'description', span: 24, attrs: { type: 'textarea', rows: 3, maxlength: 1000, showWordLimit: true } }
])

defineExpose({
  validate() {
    if (
      (!props.formData.readRule && props.formData.matching) ||
      (props.formData.readRule && !props.formData.matching)
    ) {
      Message.error('【读取规则】和【匹配】输入框需要同时为空或同时不为空')

      return Promise.reject('')
    }

    return formRef.value.validate()
  }
})

</script>

<template>
  <tr-form
    ref="formRef"
    v-model="formData"
    class="w-800px"
    label-width="120px"
    :rules="formRules"
    :items="formItems"
    :show-buttons="false"
  />
</template>
