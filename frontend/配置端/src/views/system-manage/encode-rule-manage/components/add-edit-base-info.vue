<template>
  <el-dialog
    :visible.sync="visible"
    :close-on-click-modal="false"
    :show-close="false"
    width="600px"
    title="编码规则"
  >
    <tr-form
      ref="TrFormRef"
      v-model="form"
      :items="formItems"
      :rules="rules"
      :show-buttons="false"
    />
    <el-col :span="24" class="flex justify-end margin-top">
      <el-button @click="onCancelBtnClick">取消</el-button>
      <el-button type="primary" @click="onSaveBtnClick">保存</el-button>
    </el-col>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { createOrUpdateEncode } from '../api/index.js'
import TrForm from '@@/feature/TrForm'
import { deepClone } from '@/utils'

const emits = defineEmits(['add-edit-success'])
const visible = ref(false)
const TrFormRef = ref(null)
const form = ref({})
const formItems = [
  { label: '名称', prop: 'name',span: 24, attrs: { placeholder: '请输入编码规则名称' }, required: true },
  { label: '描述', prop: 'description',span: 24, attrs: { type: 'textarea',placeholder: '请输入编码规则描述' } }
]
const rules = {
  name: [
    { required: true, message: '请输入编码规则名称', trigger: 'blur' }
  ]
}
const show = (_row) => {
  if (_row) {
    form.value = deepClone(_row)
  } else {
    form.value = {}
  }
  visible.value = true
}
const onCancelBtnClick = () => {
  form.value = {}
  TrFormRef.value.resetFields()
  visible.value = false
}
const onSaveBtnClick = async () => {
  // console.log(form.value)
  // 效验表单
  const valid = await TrFormRef.value.validate()

  if (!valid) {
    return
  }
  const { success } = await createOrUpdateEncode(form.value)

  if (success) {
    emits('add-edit-success')

    visible.value = false
  }

}

defineExpose({ show })
</script>

<style scoped>

</style>
