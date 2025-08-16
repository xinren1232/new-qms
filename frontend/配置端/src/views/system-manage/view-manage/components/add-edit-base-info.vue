<template>
  <el-dialog
    title="基本信息"
    :visible.sync="visible"
    :close-on-click-modal="false"
    width="600px"
  >
    <tr-form ref="TrFormRef" v-model="form" :items="formItems" :show-buttons="false" :rules="formRules" />
    <el-row>
      <el-col :span="24" style="text-align: right;">
        <el-button @click="onCancel">取消</el-button>
        <el-button type="primary" @click="onSubmit">确定</el-button>
      </el-col>
    </el-row>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { Message } from 'element-ui'
import TrForm from '@@/feature/TrForm'
import TrDict from '@@/feature/TrDict'
import { cloneDeep } from 'lodash'
import { saveOrUpdate } from '../api/index.js'
import LengthConfig from '@/config/constraint.config.js'

const emits = defineEmits(['on-submit'])
const props = defineProps({
  objectTreeData: {
    type: Array,
    default: () => ([])
  }
})

const TrFormRef = ref(null)
const visible = ref(false)
const form = ref({})

const baseFormItems = computed(() => {
  return [
    { label: '视图名称', prop: 'name', span: 24, attrs: { maxlength: LengthConfig.TEXT, placeholder: `最大长度${LengthConfig.TEXT}` },type: 'base' },
    { label: '视图类型', prop: 'type', span: 24, component: TrDict, attrs: { type: 'VIEW_TYPE' ,placeholder: '请选择视图类型' },type: 'base' },
    { label: '对象类型', prop: 'modelCode', span: 24,component: 'ElCascader',type: 'object',
      attrs: { placeholder: '请输入对象名', filterable: true,showAllLevels: false, options: props.objectTreeData,clearable: true,props: { emitPath: false, checkStrictly: true,label: 'name',value: 'modelCode' } } },
    { label: '客户端类型', prop: 'clientType', span: 24, component: TrDict, attrs: { type: 'CLIENT_TYPE' ,placeholder: '客户端类型' },type: 'base' },
    { label: '视图描述', prop: 'description', span: 24, attrs: { type: 'textarea', maxlength: LengthConfig.TEXTAREA, placeholder: `最大长度${LengthConfig.TEXTAREA}` },type: 'base' }
  ]
})
const formItems = ref([])
const formRules = {
  name: [
    { required: true, message: '请输入视图名称', trigger: 'blur' },
    { max: LengthConfig.TEXT, message: `长度不能超过${LengthConfig.TEXT}` }
  ],
  type: [
    { required: true, message: '请选择视图类型', trigger: 'change' }
  ]
}

watch(() => form.value, () => {
  if (form.value.type === 'OBJECT') {
    formItems.value = baseFormItems.value.filter(item => item.type === 'base' || item.type === 'object')
    formRules.modelCode = [
      { required: true, message: '请选择对象类型', trigger: 'change' }
    ]
  } else {
    formItems.value = baseFormItems.value.filter(item => item.type === 'base')
  }
}, { deep: true, immediate: true })
const show = (row) => {
  const _row = cloneDeep(row)

  if (_row) {
    form.value = _row
  }
  visible.value = true
}

const onCancel = () => {
  form.value = {}
  TrFormRef.value.resetFields()
  visible.value = false
}

const onSubmit = () => {
  TrFormRef.value.validate().then(() => {
    saveOrUpdate(form.value).then(({ success,message }) => {
      if (success) {
        Message.success(message)
        emits('on-submit')
        onCancel()
      } else {
        Message.error(message)
      }
    })
  })
}

defineExpose({
  show
})
</script>

<style scoped>

</style>
