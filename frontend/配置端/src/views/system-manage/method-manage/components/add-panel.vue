<template>
  <el-dialog
    title="方法详情"
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
import TrForm from '@@/feature/TrForm'
import TrDict from '@@/feature/TrDict'
import { createOrUpdateAttribute } from '../api/index.js'
import CONSTRAINT from '@/config/constraint.config.js'
import { deepClone } from '@/utils'
import regularExpressionConfig from '@/config/regularExpression.config'

// 判断是新增还是编辑
const isAdd = ref(true)
const TrFormRef = ref(null)
const visible = ref(false)
const form = ref({})

const emit = defineEmits(['add-success'])

const rules = {
  code: [
    { required: true, message: '请输入属性编码', trigger: 'blur' },
    { max: CONSTRAINT.TEXT, message: `长度不能超过${CONSTRAINT.TEXT}` },
    {
      // 只能输入大写字母,数字,下划线(_),且不能以数字开头,不能以_结尾

      pattern: regularExpressionConfig.upperCase.pattern,
      message: regularExpressionConfig.upperCase.message,
      trigger: ['blur', 'change']
    }
  ],
  name: [
    { required: true, message: '请输入属性名称', trigger: 'blur' },
    { max: CONSTRAINT.TEXT, message: `长度不能超过${CONSTRAINT.TEXT}` },
    {
      pattern: /^(\w|[\u4e00-\u9fa5])+$/g,
      message: '只能输入汉字,字母,数字,下划线(_)',
      trigger: ['blur', 'change']
    }
  ],
  languageType: [{ required: true, message: '请选择语言类型', trigger: ['blur', 'change'] }],
  methodGroup: [{ required: true, message: '请选择方法分组', trigger: ['blur', 'change'] }]
}

const formItems = computed(() => {
  return [
    { label: '方法名称', prop: 'name', span: 24, attrs: { maxlength: CONSTRAINT.TEXT, placeholder: `最大长度${CONSTRAINT.TEXT}` } },
    { label: '方法编码', prop: 'code', span: 24, attrs: { maxlength: CONSTRAINT.TEXT, disabled: !isAdd.value, placeholder: `${regularExpressionConfig.upperCase.message},最大长度${CONSTRAINT.TEXT}` } },
    { label: '方法分组', prop: 'methodGroup', component: TrDict, span: 24, attrs: { type: 'METHOD_TYPE' } },
    { label: '语言类型', prop: 'languageType', component: TrDict, span: 24, attrs: { type: 'LANGUAGE_TYPE' } },
    { label: '方法说明', prop: 'description', span: 24, attrs: { type: 'textarea', maxlength: CONSTRAINT.TEXTAREA, placeholder: `最大长度${CONSTRAINT.TEXTAREA}` } }
  ]
})

const show = (_row) => {
  if (_row) {
    const row = deepClone(_row)

    isAdd.value = false
    form.value = row
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
    const data = await createOrUpdateAttribute(form.value)

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
  TrFormRef.value.clearValidate()
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
