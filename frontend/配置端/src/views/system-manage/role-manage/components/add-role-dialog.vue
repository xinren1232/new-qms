<template>
  <el-dialog
    title="新增角色"
    :visible.sync="visible"
    :close-on-click-modal="false"
    width="500px"
    @close="onClose"
  >
    <el-form ref="TrFormRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="角色名称" prop="name"><el-input v-model="form.name" clearable /></el-form-item>
      <el-form-item label="角色编码" prop="code"><el-input v-model="form.code" :disabled="!!form.bid" clearable /></el-form-item>
      <el-form-item label="角色类型" prop="type">
        <tr-dict-select v-model="form.type" type="SYS_ROLE_TYPE" :disabled="disableRoleType" />
      </el-form-item>
      <el-form-item label="说明" prop="description">
        <el-input v-model="form.description" type="textarea" />
      </el-form-item>
      <el-col class="m-operate-item text-right">
        <el-button @click="onClose">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </el-col>
    </el-form>
  </el-dialog>
</template>

<script setup>
import TrDictSelect from '@@/feature/TrDict'
import { addOrEditRole } from '../api/index'


const emits = defineEmits(['refresh'])

const parentRow = ref(null)
const form = ref({})
const TrFormRef = ref(null)
const rules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  type: [{ required: true, message: '请选择角色类型', trigger: 'blur' }]
}

const disableRoleType = computed(() => form.value.bid && form.value.enableFlag !== 0)

const visible = ref(false)

const show = (row,type) => {
  visible.value = true
  if (row) {
    if (type === 'add') {
      parentRow.value = row
    } else if (type === 'edit') {
      form.value = { ...row }
    }
  }
}
const onClose = () => {
  visible.value = false
  form.value = {}
  parentRow.value = null
}
const submitForm = async() => {
  // 表单校验
  await TrFormRef.value.validate()
  const params = {
    ...form.value,
    parentBid: parentRow.value?.bid
  }

  if (!form.value?.bid) {
    params.code = '_' + params.code
  }
  const { success } = await addOrEditRole(params)

  if (!success) return
  onClose()
  emits('refresh')
}

defineExpose({
  show
})

</script>

<style></style>
