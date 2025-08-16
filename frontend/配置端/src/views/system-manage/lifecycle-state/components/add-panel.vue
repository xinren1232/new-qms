<template>
  <el-dialog
    :title="isAdd?'新增':'编辑'"
    :visible.sync="visible"
    width="600px"
    :close-on-click-modal="false"
    class="add-attr-dialog"
    @closed="closeDialog"
  >
    <tr-form ref="TrFormRef" v-model="form" :items="formItems" :rules="rules" :show-buttons="false">
      <template #preview>
        <span
          class="inline-block rounded text-sm py-2px px-6px empty:hidden"
          :style="{color: replaceOpacity(form.color), backgroundColor: form.color }"
        >
          {{ form.name }}
        </span>
      </template>
    </tr-form>

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
import LengthConfig from '@/config/constraint.config.js'
import { deepClone } from '@/utils'
import regularExpressionConfig from '@/config/regularExpression.config'
import { universalColors, replaceOpacity } from '@/config/universal-colors.js'

// 判断是新增还是编辑
const isAdd = ref(true)
const TrFormRef = shallowRef(null)
const visible = ref(false)
const form = ref({})

const emit = defineEmits(['add-success'])

const rules = {
  code: [
    { required: true, message: '请输入状态编码', trigger: 'blur' },
    { max: LengthConfig.TEXT, message: `长度不能超过${LengthConfig.TEXT}` },
    {
      // 只能输入大小写字母，数字，下划线，且不能以数字开头
      pattern: regularExpressionConfig.upperCase.pattern,
      message: regularExpressionConfig.upperCase.message,
      trigger: ['blur', 'change']
    }
  ],
  name: [
    { required: true, message: '请输入状态名称', trigger: 'blur' },
    { max: LengthConfig.TEXT, message: `长度不能超过${LengthConfig.TEXT}` },
    {
      pattern: regularExpressionConfig.chineseAndEnglishAndNumber.pattern,
      message: regularExpressionConfig.chineseAndEnglishAndNumber.message,
      trigger: ['blur', 'change']
    }
  ],
  groupCode: [{ required: true, message: '请选择分组', trigger: ['blur', 'change'] }]
}

// const dataTypeChange = () => {
// if (!this.showOptions) {
//   this.$nextTick(() => {
//     this.$refs.search.onClear()
//   })
// }
// }

const formItems = computed(() => {
  return [
    { label: '状态名称', prop: 'name', span: 24, attrs: { maxlength: LengthConfig.TEXT, placeholder: `最大长度${LengthConfig.TEXT}` } },
    { label: '状态编码', prop: 'code', span: 24, attrs: { maxlength: LengthConfig.TEXT, disabled: !isAdd.value, placeholder: regularExpressionConfig.upperCase.message } },
    {
      label: '分组',
      prop: 'groupCode',
      component: TrDict,
      span: 24,
      attrs: { type: 'LIFECYCLE_STATE_GROUPS' }
    },
    {
      label: '颜色', prop: 'color', span: 24,
      component: 'ElColorPicker',
      attrs: { predefine: universalColors, showAlpha: true }
    },
    {
      label: '预览', prop: 'preview', span: 24
    },

    // {
    //   label: '标签',
    //   prop: 'tag',
    //   component: TrDict,
    //   span: 24,
    //   attrs: { type: 'LIFECYCLE_STATE_CODE_TAG' }
    // },
    {
      label: '说明',
      prop: 'description',
      span: 24,
      attrs: { type: 'textarea', maxlength: LengthConfig.TEXTAREA, placeholder: '最大长度200' }
    }
  ]
})

const show = (_row) => {
  isAdd.value = !_row
  if (_row) {
    const row = deepClone(_row)

    form.value = row
  } else {
    form.value = { color: universalColors[0] }
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
  form.value = {}
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
