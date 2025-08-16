<template>
  <el-dialog
    :visible.sync="visible"
    :close-on-click-modal="false"
    width="800px"
    title="编码规则属性设置"
    append-to-body
  >
    <tr-form
      ref="TrFormRef"
      v-model="form"
      :items="formItem"
      :rules="rules"
      :show-buttons="false"
    >
      <el-input
        slot="beforeSeparator"
        v-model.trim="form.beforeSeparator"
        clearable
        class="input-with-select"
        placeholder="空格请输入&nbsp;"
      >
        <el-button slot="append" @click="form.beforeSeparator = '&nbsp;'">快捷录入空格</el-button>
      </el-input>
      <el-input
        slot="afterDelimiter"
        v-model.trim="form.afterDelimiter"
        clearable
        class="input-with-select"
        placeholder="空格请输入&nbsp;"
      >
        <el-button slot="append" @click.native="form.afterDelimiter = '&nbsp;'">快捷录入空格</el-button>
      </el-input>
    </tr-form>
    <tr-form
      ref="TrVariableFormRef"
      v-model="variableForm"
      :items="variableFormItem"
      :show-buttons="false"
      :rules="variableRules"
    />
    <el-row>
      <el-col :span="24" class="flex justify-end margin-top">
        <el-button @click="onCancelBtnClick">取消</el-button>
        <el-button type="primary" @click="onSaveBtnClick">保存</el-button>
      </el-col>
    </el-row>
  </el-dialog>
</template>

<script setup>
import { ref, watch, set } from 'vue'
import { createUpdateRuleItem,queryRuleItemDetail } from '../api/index.js'
import TrForm from '@@/feature/TrForm'
import TrDict from '@@/feature/TrDict'
import TrSelect from '@@/feature/TrSelect'
import { queryObjectTree,getObjectAttrByBid } from '@/views/system-manage/object-manage/api/index.js'
import { flatTreeData } from '@/utils/index.js'
import ConstraintConfig from '@/config/constraint.config'

const visible = ref(false)
const TrFormRef = ref(null)
const TrVariableFormRef = ref(null)
const form = ref({})
const objectTreeData = ref([])
const flatTreeDataList = ref([])
const propertiesList = ref([])
const emits = defineEmits(['add-edit-success'])
const formItem = ref([])
const variableFormItem = ref([])
const variableForm = ref({})
const currentRuleBid = ref(null) // 当前规则bid
const getObjectTreeData = async () => {
  const { success, data } = await queryObjectTree()

  if (success) {
    // TODO 深度遍历Tree，如果children为空数组，则删除children属性
    objectTreeData.value = data
    flatTreeDataList.value = flatTreeData(objectTreeData.value,'children')
    set(variableFormItem.value.find(i => i.prop === 'modelCode').attrs, 'options', objectTreeData.value)
    if (variableForm.value.modelCode && propertiesList.value.length === 0) {
      await getObjectProperties(variableForm.value.modelCode)
    }
  }
}

const baseFormItem = ref([
  { label: '名称', prop: 'name',span: 24, placeholder: '请输入编码规则名称',group: 'base' },
  { label: '描述', prop: 'description',span: 24, placeholder: '请输入编码规则描述',group: 'base' },
  { label: '顺序', prop: 'sort',span: 24,group: 'base' },
  { label: '前分隔符', prop: 'beforeSeparator',span: 24,group: 'base' },
  { label: '后分隔符', prop: 'afterDelimiter',span: 24,group: 'base' },
  { label: '类型', prop: 'type',span: 24,group: 'base', component: TrDict,attrs: { type: 'ENCODE_PROP_TYPE' } },
  { label: '常量值', prop: 'value',span: 24, placeholder: '请输入常量值', group: 'CONSTANT' },
  { label: '日期格式', prop: 'dateFormat',span: 24, placeholder: '请输入日期格式', group: 'DATE' },
  { label: '对象', prop: 'modelCode',component: 'ElCascader',
    attrs: { filterable: true,showAllLevels: false, options: objectTreeData.value,clearable: true,props: { emitPath: false, checkStrictly: true,label: 'name',value: 'modelCode' } },span: 24,
    placeholder: '请输入对象名', required: true,group: 'OBJECT' },
  { label: '对象属性', prop: 'selectProperties',span: 24,component: TrSelect,attrs: { data: propertiesList.value,labelField: 'name',valueField: 'code' }, placeholder: '请选择属性',group: 'OBJECT' },
  { label: '长度', prop: 'theLengthOfValue',span: 24,group: 'WATER_MARK' },
  { label: '初始值', prop: 'initialValue',span: 24,group: 'WATER_MARK' },
  { label: '填充值', prop: 'fillingValue',span: 24,group: 'WATER_MARK' },
  { label: '步长值', prop: 'stepLengthValue',span: 24,group: 'WATER_MARK' },
  { label: '字典编码', prop: 'selectDictionary',span: 24,group: 'ENUMERATE',placeholder: '请输入字典编码' },
  { label: '取值字段', prop: 'eActualValue',component: TrDict,attrs: { type: 'ENCODE_ENUMERATE_VALUE_FIELD' },span: 24,group: 'ENUMERATE' }
])

const getObjectProperties = async (val) => {
  if (!val) return
  const objBid = flatTreeDataList.value.find(i => i.modelCode === val)?.bid

  if (!objBid) return

  const { success, data } = await getObjectAttrByBid(objBid)

  if (success) {
    propertiesList.value = data?.attrList || []
    set(variableFormItem.value.find(i => i.prop === 'selectProperties').attrs, 'data', propertiesList.value)
  }
}

formItem.value = baseFormItem.value.filter(item => item.group === 'base')

watch(() => form.value.type, (val) => {
  if (val === 'OBJECT') {
    getObjectTreeData()
  }
  variableForm.value = {}
  variableFormItem.value = baseFormItem.value.filter(item => (item.group === val))

}, { immediate: true })

watch(() => variableForm.value.modelCode, (val) => {
  getObjectProperties(val)
},{ immediate: true }) // 获取对象属性

const rules = {
  name: [
    { required: true, message: '请输入编码规则名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择编码规则类型', trigger: 'change' }
  ],
  sort: [
    { required: true, message: '请输入编码规则顺序', trigger: 'blur' }
  ]
}
const variableRules = {
  modelCode: [
    { required: true, message: '请选择对象名', trigger: 'change' }
  ],
  selectProperties: [
    { required: true, message: '请选择对象属性', trigger: 'change' }
  ],
  selectDictionary: [
    { required: true, message: '请输入字典编码', trigger: 'blur' }
  ],
  eActualValue: [
    { required: true, message: '请选择取值字段', trigger: 'change' }
  ],
  theLengthOfValue: [
    { required: true, message: '请输入长度', trigger: 'blur' }
  ],
  initialValue: [
    { required: true, message: '请输入初始值', trigger: 'blur' }
  ],
  fillingValue: [
    { required: true, message: '请输入填充值', trigger: 'blur' }
  ],
  stepLengthValue: [
    { required: true, message: '请输入整数步长值', trigger: 'blur' },
    { pattern: /^[0-9]*$/, message: '步长值只能输入数字', trigger: 'blur' }
  ],
  dateFormat: [
    { required: true, message: '请输入日期格式', trigger: 'blur' }
  ],
  value: [
    { required: true, message: '请输入常量值', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9]+$/, message: '常量值只能输入字母和数字', trigger: 'blur' },
    { max: ConstraintConfig.TEXT, message: `常量值长度不能超过${ConstraintConfig.TEXT}个字符`, trigger: 'blur' }
  ]
}
const show = (row,bid) => {
  currentRuleBid.value = bid
  if (row) {
    form.value = row
    variableForm.value = row?.value || {}
    getRuleItemInfo()
  } else {
    form.value = {}
    variableForm.value = {}
  }
  form.value.codeRuleBid = currentRuleBid.value
  visible.value = true
}

const onCancelBtnClick = () => {
  form.value = {}
  variableForm.value = {}
  TrFormRef.value.resetFields()
  if (TrVariableFormRef.value) TrVariableFormRef.value.resetFields()

  visible.value = false
}
const onSaveBtnClick = async () => {
  // 效验表单
  const valid = await TrFormRef.value.validate()

  if (!valid) {
    return
  }
  const variableValid = await TrVariableFormRef.value.validate()

  if (!variableValid) {
    return
  }
  form.value.value = variableForm.value
  const { success } = await createUpdateRuleItem(form.value)

  if (success) {
    emits('add-edit-success')

    visible.value = false
  }

}

// 获取规则条目信息
const getRuleItemInfo = async () => {
  const { success, data } = await queryRuleItemDetail(form.value.bid)

  if (success) {
    form.value = { ...data,...form.value }
    variableForm.value = data?.value || { }
  }
}


defineExpose({ show })
</script>

<style scoped lang="scss">
@import '../css/reset.scss';
.tools-box{
  padding: 10px 10px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  .btn-box {
    display: flex;
    align-items: center;
    cursor: pointer;
    color: var(--primary);
    padding: 4px;
    width: fit-content;
    .text{
      font-size: 14px;
      margin-right: 4px;
      vertical-align: -0.2em;
    }
    .tr-svg-icon{
      font-size: 16px;
      margin-right: 4px;
      vertical-align: -0.15em;
    }

    &:hover {
      background-color: #EAEDEC;
      border-radius: 4px;
    }
    &:active {
      background-color: #dee1e0;
    }
  }
}
.add-edit-rule-item-dialog{
  z-index: 900;
}
</style>
