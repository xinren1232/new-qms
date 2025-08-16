<template>
  <el-dialog
    :title="isAdd ? '新增' : '编辑'"
    :visible.sync="visible"
    width="800px"
    append-to-body
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
import { Loading } from 'element-ui'
import TrForm from '@@/feature/TrForm'
import TrDict from '@@/feature/TrDict'
import LengthConfig from '@/config/constraint.config.js'
import ObjectSelect from '@@/plm/object-select/object-select.vue'
import { deepClone } from '@/utils'

const props = defineProps({
  objectTreeData: {
    type: Array,
    default: () => []
  },
  objectDataMap: {
    type: Object,
    default: () => ({})
  },
  objectDataMapInfo: {
    type: Object,
    default: () => ({})
  },
  // 目标对象
  sourceModelCode: {
    type: String,
    default: () => ''
  },
  // 新增类型
  relationType: {
    type: String,
    default: () => 'single'
  }

})

// 判断是新增还是编辑
const isAdd = ref(true)
const TrFormRef = ref(null)
const visible = ref(false)
const form = ref({
  behavior: ''
})

const emit = defineEmits(['add-success'])

const rules = {
  name: [
    { required: true, message: '请输入关系名称', trigger: 'blur' },
    { max: LengthConfig.TEXT, message: `长度不能超过${LengthConfig.TEXT}` }
  ],
  tabName: [
    { required: true, message: '请输入TAB名称', trigger: 'blur' },
    { max: LengthConfig.TEXT, message: `长度不能超过${LengthConfig.TEXT}` }
  ],
  sourceModelCode: [{ required: true, message: '请选择源对象', trigger: 'change' }],
  targetModelCode: [{ required: true, message: '请选择关联对象', trigger: 'change' }],
  behavior: [{ required: true, message: '请选择关联行为', trigger: 'change' }],
  isRequired: [{ required: true, message: '请选择是否必填', trigger: 'change' }],
  type: [{ required: true, message: '请选择关联类型', trigger: 'change' }],
  showType: [{ required: true, message: '请选择展示模式', trigger: 'change' }],
  hideTab: [{ required: true, message: '请选择', trigger: 'change' }]
}
const disabled = computed(() => {
  return !isAdd.value
})
// 关联行为的只读性，如果源对象是版本对象，关联行为只能是强浮动&&不可修改
const behaviorIsDisabled = ref(false)
const formItems = computed(() => {
  if (props.relationType === 'single') {
    return [
      {
        label: '关系名称',
        prop: 'name',
        span: 12,
        attrs: { maxlength: LengthConfig.TEXT, placeholder: `关系名称,最大长度${LengthConfig.TEXT}` }
      },
      {
        label: 'TAB名称',
        prop: 'tabName',
        span: 12,
        attrs: { maxlength: LengthConfig.TEXT, placeholder: `TAB名称,最大长度${LengthConfig.TEXT}` }
      },
      {
        label: '源对象',
        prop: 'sourceModelCode',
        component: ObjectSelect,
        span: 12,
        attrs: {
          disabled: true,
          data: props.objectTreeData,
          dataMap: props.objectDataMap,
          multiple: false,
          placeholder: '请选择源对象'
        },
        listeners: {
          input: val => {
            form.value.sourceModelCode = val
            TrFormRef.value.clearValidate('sourceModelCode')
          }
        }
      },
      {
        label: '目标对象',
        prop: 'targetModelCode',
        span: 12,
        component: ObjectSelect,
        attrs: {
          disabled: disabled.value,
          data: props.objectTreeData,
          dataMap: props.objectDataMap,
          multiple: false,
          placeholder: '请选择目标对象'
        },
        listeners: {
          input: val => {
            form.value.targetModelCode = val
            TrFormRef.value.clearValidate('targetModelCode')
          }
        }
      },
      {
        label: '关联行为',
        prop: 'behavior',
        component: TrDict,
        span: 12,
        attrs: { type: 'RELATION_BEHAVIOR', disabled: behaviorIsDisabled.value }
      },
      { label: '关联方式', prop: 'type', component: TrDict, span: 12, attrs: { type: 'RELATION_LINK_TYPE' } },
      { label: '关联项必填', prop: 'isRequired', component: TrDict, span: 12, attrs: { type: 'STANDARD_BOOLE' } },
      { label: '应用所有对象', prop: 'hideTab', span: 12, component: TrDict, attrs: { type: 'STANDARD_BOOLE' } },
      // { label: '最小数量', prop: 'minNumber', span: 12 },
      // { label: '最大数量', prop: 'maxNumber', span: 12 },
      { label: '展示类型', prop: 'showType', span: 12, component: TrDict, attrs: { type: 'RELATION_SHOW_TYPE' } },
      { label: '说明', prop: 'description', span: 24, attrs: { type: 'textarea' } }
    ]
  }

  return [
    {
      label: '关联对象',
      prop: 'relatedModelCode',
      span: 12,
      component: ObjectSelect,
      attrs: {
        disabled: disabled.value,
        data: props.objectTreeData,
        dataMap: props.objectDataMap,
        multiple: false,
        placeholder: '请选择目标对象'
      },
      listeners: {
        input: val => {
          // set(form.value, 'relatedModelCode', val)
          form.value.relatedModelCode = val
          console.log(val, form.value)
          TrFormRef.value.clearValidate('relatedModelCode')
        }
      }
    },
    { label: '说明', prop: 'description', span: 24, attrs: { type: 'textarea' } }
  ]
})

const show = (_row) => {
  if (props.relationType === 'single') {
    if (_row) {
      const row = deepClone(_row)

      // 字典的KEY不能为数字，但服务端保存的是Number,所以需要转换String
      row.isRequired = String(row.isRequired) || '0'
      row.hideTab = String(row.hideTab) || '0'

      isAdd.value = false
      form.value = row
    } else {
      isAdd.value = true
      form.value = {
        isRequired: '0',
        behavior: 'STRONG_FLOAT',
        showType: 'table',
        type: 'both',
        hideTab: '0'
      }
    }
    form.value.sourceModelCode ??= props.sourceModelCode
  }
  else {
    if (_row) {
      isAdd.value = false
      form.value = deepClone(_row)
    } else {
      isAdd.value = true
    }
  }
  visible.value = true
  // 清除表单校验
  nextTick(() => TrFormRef.value.clearValidate())
}


const onConfirm = () => {
  const loadingInstance = Loading.service({ lock: true, target: '.add-attr-dialog .el-dialog', text: '正在保存...' })

  TrFormRef.value.validate(val => {
    if (val) {
      emit('add-success', form.value, isAdd.value)
      nextTick(() => loadingInstance.close())
      closeDialog()
    } 
  })

}
// 表单校验
// const validate = () => {
//   return new Promise((resolve, reject) => {
//     TrFormRef.value.validate(val => {
//       if (val) {
//         resolve()
//       } else {
//         reject()
//       }
//     })
//   })
// }
const onCancel = () => {
  closeDialog()
}
const closeDialog = () => {
  TrFormRef.value.resetFields()
  TrFormRef.value.clearValidate()
  behaviorIsDisabled.value = false
  visible.value = false
}

// const columns = computed((toggleVisible) => {
//   return [
//     { label: '关系属性', prop: 'innerName', align: 'center', showOverflowTooltip: true },
//     { label: '显示名', prop: 'explain', align: 'center', showOverflowTooltip: true }
//   ]
// })

// const toggleVisible = (row) => {
//   row.realUseInView = !row.realUseInView
//   // forceUpdate() // 加这一句是因为后端没有返回这个字段时候切换图标不及时更改
// }

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
