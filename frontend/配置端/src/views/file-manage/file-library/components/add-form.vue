<script setup>
import TrForm from '@@/feature/TrForm/index.js'
import TrDict from '@@/bussiness/TrDict/index.js'

const props = defineProps({
  formData: {
    type: Object,
    default: () => ({})
  }
})

const formRef = shallowRef()

const formRules = {
  name: { required: true, message: '请输入文件库名称', trigger: 'blur' },
  url: { required: true, message: '请输入文件库URL', trigger: 'blur' },
  address: { required: true, message: '请选择归属地', trigger: 'change' }
}
// 默认文件库一旦被设置 就不能再更改
let firstDefaultLibFlag = false
const unwatch = watch(() => props.formData.defaultFlag, (val) => {
  if (val) {
    firstDefaultLibFlag = val
    unwatch()
  }
})

// 设置成了默认库 就没法编辑了
const defaultLibDisabled = computed(() => {
  return props.formData.bid && firstDefaultLibFlag
})
const formItems = computed(() => {
  return [
    { label: '文件库名称', prop: 'name', span: 12, margin: 12,attrs: { maxlength: 50 } },
    { label: 'URL规则', prop: 'urlRule', span: 12, margin: 12 },
    { label: '文件库URL', prop: 'url', span: 12, margin: 12 },
    { label: '归属地', prop: 'address', component: TrDict, span: 12, margin: 12, attrs: { type: 'FILE_STORAGE_ADDRESS', labelField: 'zh', valueField: 'keyCode' } },
    { label: '文件库描述', prop: 'description', span: 12, margin: 12, attrs: { type: 'textarea', rows: 3, maxlength: 1000, showWordLimit: true } },
    {
      label: '是否默认库', prop: 'defaultFlag', component: 'ElSwitch', attrs: {
        activeValue: true,
        inactiveValue: false,
        disabled: defaultLibDisabled.value
      }
    }
  ]
})


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
    :rules="formRules"
    :items="formItems"
    :show-buttons="false"
  />
</template>
