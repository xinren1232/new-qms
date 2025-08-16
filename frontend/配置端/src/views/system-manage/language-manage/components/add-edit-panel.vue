<template>
  <el-dialog
    :title="isAdd?'新增':'编辑'"
    :visible.sync="visible"
    width="600px"
    :close-on-click-modal="false"
    class="add-attr-dialog"
    @closed="closeDialog"
  >
    <tr-form ref="TrFormRef" v-model="form" :items="formItems" :rules="rules" :show-buttons="false" />
    <div class="footer flex justify-end">
      <el-button @click="onCancel">取消</el-button>
      <el-button type="primary" @click="onConfirm">确定</el-button>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, nextTick, computed } from 'vue'
import { Message, Loading } from 'element-ui'
import CONSTRAINT from '@/config/constraint.config.js'
import TrForm from '@@/feature/TrForm'
import { createOrUpdate } from '../api/index.js'
import regularExpressionConfig from '@/config/regularExpression.config'

// 判断是新增还是编辑
const isAdd = ref(true)
const TrFormRef = ref(null)
const visible = ref(false)
const form = ref({})

const emit = defineEmits(['add-success'])

const rules = {
  code: [
    { required: true, message: '请输入编码', trigger: 'blur' },
    { max: CONSTRAINT.TEXT, message: `长度不能超过${CONSTRAINT.TEXT}` },
    {
      // 只能输入大小写字母，数字，下划线，且不能以数字开头
      pattern: regularExpressionConfig.upperCaseAndNumber.pattern,
      message: regularExpressionConfig.upperCaseAndNumber.message,
      trigger: ['blur', 'change']
    }
  ],
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' },
    { max: CONSTRAINT.TEXT, message: `长度不能超过${CONSTRAINT.TEXT}` },
    {
      pattern: /^(\w|[\u4e00-\u9fa5])+$/g,
      message: '只能输入汉字,字母,数字,下划线(_)',
      trigger: ['blur', 'change']
    }
  ],
  zh: [
    { required: true, message: '请输入中文', trigger: 'blur' }
  ],
  en: [
    { required: true, message: '请输入英文', trigger: 'blur' }
  ],
  description: [
    { max: CONSTRAINT.TEXTAREA, message: `长度不能超过${CONSTRAINT.TEXTAREA}` }
  ]
}

const formItems = computed(() => {
  return [
    { label: '名称', prop: 'name', span: 24, attrs: { maxlength: CONSTRAINT.TEXT, placeholder: `最大长度${CONSTRAINT.TEXT}` } },
    { label: '编码', prop: 'code', span: 24, attrs: { maxlength: CONSTRAINT.TEXT, disabled: !isAdd.value, placeholder: regularExpressionConfig.upperCaseAndNumber.message } },
    { label: '中文', prop: 'zh', span: 24, attrs: { maxlength: CONSTRAINT.TEXT, placeholder: `请输入中文值` } },
    { label: '英文', prop: 'en', span: 24, attrs: { maxlength: CONSTRAINT.TEXT, placeholder: `请输入英文值` } },
    {
      label: '描述',
      prop: 'description',
      span: 24,
      attrs: { type: 'textarea', maxlength: CONSTRAINT.TEXTAREA, placeholder: `最大长度${CONSTRAINT.TEXTAREA}` }
    }
  ]
})

const show = (row) => {

  if (row) {
    const zh = row.items?.find(item => item.languageCode === 'zh')?.value
    const en = row.items?.find(item => item.languageCode === 'en')?.value

    isAdd.value = false
    form.value = { zh,en,...row }
  } else {
    isAdd.value = true
    form.value = {}
  }
  visible.value = true
  // 清除表单校验
  nextTick(() => TrFormRef.value.clearValidate())
}

const onConfirm = () => {
  const loadingInstance = Loading.service({ lock: true, target: '.add-attr-dialog .el-dialog', text: '正在保存...' })

  validate().then(async () => {
    const params = {
      ...form.value,
      items: [{ languageCode: 'en',value: form.value.en },{ languageCode: 'zh','value': form.value.zh }]
    }

    delete params.en
    delete params.zh
    const data = await createOrUpdate(params)

    if (data?.success) {
      Message.success(data.message)
      nextTick(() => loadingInstance.close())
      closeDialog()
      emit('add-success')
    } else {
      Message.error(data?.message)
      nextTick(() => loadingInstance.close())
    }
  }).catch(() => {
    nextTick(() => loadingInstance.close())
  })
}
// 表单校验
const validate = () => {
  return new Promise((resolve, reject) => {
    TrFormRef.value.validate((val) => {
      if (val) {
        resolve()
      } else {
        reject()
      }
    })
  })
}
const onCancel = () => {
  closeDialog()
}
const closeDialog = () => {
  TrFormRef.value.resetFields()
  visible.value = false
}

defineExpose({
  show
})
</script>

<style lang="scss" scoped>
.footer {
  margin-top: 25px;
  text-align: right;
}
</style>
