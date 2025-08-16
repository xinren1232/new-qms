<template>
  <el-dialog
    :title="isAdd ? $t('property.add') : $t('property.edit')"
    :visible.sync="visible"
    width="600px"
    :close-on-click-modal="false"
    class="add-attr-dialog"
    @closed="closeDialog"
  >
    <tr-form ref="TrFormRef" v-model="form" :items="formItems" :rules="rules" :show-buttons="false" />
    <div class="footer flex justify-end">
      <el-button @click="onCancel">{{ $t('common.cancel') }}</el-button>
      <el-button type="primary" @click="onConfirm">{{ $t('common.submit') }}</el-button>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, nextTick, computed } from 'vue'
import { Message, Loading } from 'element-ui'
import CONSTRAINT from '@/config/constraint.config.js'
import TrForm from '@@/feature/TrForm'
import TrDict from '@@/feature/TrDict'
import { createOrUpdateAttribute } from '../api/index.js'
import i18n from '@/i18n'
import regularExpressionConfig from '@/config/regularExpression.config'
import ObjectSelector from '@@/plm/object-select/object-select.vue'
import { queryObjectTree } from '@/views/system-manage/object-manage/api/index.js'
import { flatTreeData } from '@/utils/index.js'

// 判断是新增还是编辑
const isAdd = ref(true)
const TrFormRef = shallowRef(null)
const visible = ref(false)
const form = ref({})

const objectFlatMap = shallowRef({})
const objectTreeData = shallowRef([])

const emit = defineEmits(['add-success'])

const rules = {
  code: [
    { required: true, message: i18n.t('rule.required'), trigger: 'blur' },
    { max: CONSTRAINT.TEXT, message: `${i18n.t('rule.maxLength')}${CONSTRAINT.TEXT}` },
    {
      // 只能输入驼峰的字母、数字,不能以数字开头
      pattern: regularExpressionConfig.lowerCaseAndNumber.pattern,
      message: regularExpressionConfig.lowerCaseAndNumber.message,
      trigger: ['blur', 'change']
    }
  ],
  name: [
    { required: true, message: i18n.t('rule.required'), trigger: 'blur' },
    { max: CONSTRAINT.TEXT, message: `${i18n.t('rule.maxLength')}${CONSTRAINT.TEXT}` },
    {
      pattern: /^(\w|[\u4e00-\u9fa5])+$/g,
      message: i18n.t('rule.name'),
      trigger: ['blur', 'change']
    }
  ],
  dataType: [{ required: true, message: i18n.t('rule.required'), trigger: ['blur', 'change'] }],
  groupName: [{ required: true, message: i18n.t('rule.required'), trigger: ['blur', 'change'] }],
  modelCode: [{ required: true, message: i18n.t('rule.required'), trigger: ['blur', 'change'] }]
}

const formItems = computed(() => {
  const fileds = [
    { label: i18n.t('property.name'), prop: 'name', span: 24, attrs: { maxlength: CONSTRAINT.TEXT, placeholder: `${i18n.t('rule.maxLength')}${CONSTRAINT.TEXT}` } },
    { label: i18n.t('property.code'), prop: 'code', span: 24, attrs: { maxlength: CONSTRAINT.TEXT, disabled: !isAdd.value, placeholder: `${regularExpressionConfig.lowerCaseAndNumber.message},${i18n.t('rule.maxLength')}${CONSTRAINT.TEXT}` } },
    {
      label: i18n.t('property.dataType'),
      prop: 'dataType',
      component: TrDict,
      span: 24,
      attrs: { type: 'PROPERTY_DATA_TYPE' }
    },
    {
      label: i18n.t('property.group'),
      prop: 'groupName',
      component: TrDict,
      span: 24,
      attrs: { type: 'PROPERTY_GROUP' }
    },
    // 绑定国际化编码
    { label: i18n.t('common.lang'), prop: 'lang', span: 24, attrs: { maxlength: CONSTRAINT.TEXT, placeholder: `${i18n.t('rule.maxLength')}${CONSTRAINT.TEXT}` } }

  ]

  // 如果选择的是对象类型，需要添加关联对象
  if (form.value.dataType === 'objectType') {
    fileds.splice(3, 0, {
      label: i18n.t('property.relationObject'),
      prop: 'modelCode',
      component: ObjectSelector,
      span: 24,
      attrs: {
        multiple: false,
        data: objectTreeData.value,
        dataMap: objectFlatMap.value
      }
    })
  }

  return fileds
})

// 获取对象树数据
async function getObjectTreeDataAndFlatMap() {
  const { data } = await queryObjectTree()

  objectTreeData.value = data
  // 递归过滤掉type==='RELATION'的数据
  objectTreeData.value = flatTreeData(objectTreeData.value, 'children').filter(item => item.type !== 'RELATION')
  flatTreeData(objectTreeData.value,'children').forEach(item => {
    objectFlatMap.value[item.modelCode] = item.name
  })
}

getObjectTreeDataAndFlatMap()

const show = (row) => {
  if (row) {
    isAdd.value = false
    form.value = JSON.parse(JSON.stringify(row))
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
