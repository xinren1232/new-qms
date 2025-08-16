<template>
  <el-dialog
    :visible.sync="visible"
    :close-on-click-modal="false"
    width="1000px"
    custom-class="property-dialog"
    @close="onClose"
  >
    <span slot="title" class="el-dialog__title">
      <span class="margin-right">属性配置</span><el-tag> {{ form.name }}</el-tag>
    </span>
    <div class="tools-box">
      <div v-if="activeName==='property'&&showCheckInBtn" class="btn-box" @click="onAddBtnClick">
        <tr-svg-icon icon-class="add-list" />
        <span class="text">添加</span></div>
      <div v-if="showCheckOutBtn" class="btn-box" @click="onCheckoutBtnClick"><tr-svg-icon icon-class="t-checkout" /><span class="text">检出</span></div>
      <div v-if="showCheckInBtn" class="btn-box" @click="onCheckinBtnClick"><tr-svg-icon icon-class="t-checkin" /><span class="text">检入</span></div>
      <div v-if="showCheckInBtn" class="btn-box" @click="onCancelCheckinBtnClick"><tr-svg-icon icon-class="t-checkin" /><span class="text">撤销检出</span></div>
      <div v-if="activeName==='property'" class="btn-box">
        <el-checkbox v-model="isShowInnerProperties" @change="onShowInnerPropertiesChange">显示继承属性</el-checkbox>
      </div>

    </div>
    <el-tabs v-model="activeName" type="card" style="min-height:520px" @tab-click="onTabClick">
      <el-tab-pane label="基本信息" name="baseInfo">
        <tr-form ref="TrFormRef" v-model="form" :items="formItems" :rules="formRules" :show-buttons="false" />
      </el-tab-pane>
      <el-tab-pane label="属性配置" name="property">
        <vxe-table
          ref="TrTableRef"
          :data="tableData"
          height="460"
          :column-config="{resizable: true}"
          border
          show-overflow
          :loading="loading"
          :row-config="{height:tableConfig.rowHeight}"
        >
          <vxe-table-column type="seq" width="50" title="No." />
          <vxe-table-column title="属性名称" field="name" sortable align="left" min-width="200">
            <template #default="{ row }">
              <el-tag v-if="row.inherit" :disable-transitions="false" type="danger">继承</el-tag>
              <el-tag v-else :disable-transitions="false">私有</el-tag>
              <el-tooltip v-if="!row.inherit" content="点击我跳转到【编辑属性】" :open-delay="600">
                <span class="common-btn active-hover" @click="onJumpToProperties(row)">{{ row.name }}</span>
              </el-tooltip>
              <el-tooltip v-else content="继承的属性不可删除" :open-delay="600">
                <span class="font-bold ">{{ row.name }}</span>
              </el-tooltip>
            </template>
          </vxe-table-column>
          <vxe-table-column title="属性编码" field="code" sortable align="center" min-width="120" />
          <vxe-table-column title="数据类型" field="dataType" sortable align="center" min-width="100">
            <template #default="{ row }">{{ renderFieldByDict(row.dataType,'PROPERTY_DATA_TYPE') }}</template>
          </vxe-table-column>
          <vxe-table-column title="是否必填" field="required" sortable align="center" min-width="100">
            <template #default="{ row }">
              <el-tag v-if="row.required" type="danger">是</el-tag>
              <el-tag v-else type="info">否</el-tag>
            </template>
          </vxe-table-column>
          <vxe-table-column title="是否展示" field="isShow" sortable align="center" min-width="100">
            <template #default="{ row }">
              <el-switch
                v-if="showCheckInBtn&&editBtn(row)" 
                v-model="row.isShow"
              />
              <div v-else>
                <el-tag v-if="row.isShow" type="danger">是</el-tag>
                <el-tag v-else type="info">否</el-tag>
              </div>
            </template>
          </vxe-table-column>
          <vxe-table-column title="关系是否展示" field="isShow" sortable align="center" min-width="100">
            <template #default="{ row }">
              <el-switch
                v-if="showCheckInBtn&&editBtn(row)"
                v-model="row.isRelationShow"
              />
              <div v-else>
                <el-tag v-if="row.isRelationShow" type="danger">是</el-tag>
                <el-tag v-else type="info">否</el-tag>
              </div>
            </template>
          </vxe-table-column>
          <vxe-table-column title="排序" field="sort" sortable align="center" min-width="120">
            <template #default="{row}">
              <el-input-number v-if="showCheckInBtn&&editBtn(row)" v-model.trim="row.sort" size="mini" :min="0" />
              <div v-else>{{ row.sort }}</div>
            </template>
          </vxe-table-column>
          <vxe-table-column v-if="showCheckInBtn" title="操作" field="editable" sortable align="center" width="100">
            <template #default="{ row }">
              <el-button v-if="!(row.inherit||row.published)" type="text" class="danger-color" @click="onDeleteBtnClick(row)">移除</el-button>
            </template>
          </vxe-table-column>
        </vxe-table>
      </el-tab-pane>
    </el-tabs>
    <property-select ref="propertySelectorRef" :selected-data="selectedData" @add-property-submit="onAddPropertySubmit" />
  </el-dialog>
</template>

<script setup>

import { ref, computed, nextTick } from 'vue'
import { Message,Loading } from 'element-ui'
import TrForm from '@@/feature/TrForm'
import TrSwitch from '@@/feature/TrSwitch'
import PropertySelect from '@@/plm/property-selector/index.vue'
import { objectCheckout,objectCheckin, objectCancelCheckout, getObjectAttrByBid } from '../api/index.js'
import { renderFieldByDict } from '@/utils/dict'
import ConstraintConfig from '@/config/constraint.config'
import store from '@/store'
import { cloneDeep } from 'lodash'
import TrSelect from '@@/feature/TrSelect/index.vue'
import innerPropertyConfig from '@/config/innerProperty.config'
import tableConfig from '@/config/table.config'
import route from '@/router'

const objectTypeOptions = [{ label: '常规对象', value: 'NORMAL' }, { label: '版本对象', value: 'VERSION' }, { label: '关系对象', value: 'RELATION' }]
const versionOptions = [{ label: '是', value: 1 }, { label: '否', value: 0 }]
const multiObjectOptions = [{ label: '常规', value: 'single' }, { label: 'poly', value: 'poly' }]
const emits = defineEmits(['check-status-change'])
const visible = ref(false)
const activeName = ref('baseInfo')
const TrTableRef = ref(null)
const propertySelectorRef = ref(null)
const TrFormRef = ref(null)
const formItems = computed(() => [
  { label: '对象名称', prop: 'name', placeholder: '请输入对象名称',span: 24,attrs: { disabled: !showCheckInBtn.value,clearable: true } },
  { label: '对象编码', prop: 'modelCode', placeholder: '请输入对象编码', span: 24,attrs: { disabled: true } },
  { label: '基类对象', prop: 'baseModel',span: 24,attrs: { disabled: true } },
  { label: '对象类别', prop: 'type',span: 24,
    component: TrSelect,
    attrs: {
      data: objectTypeOptions,
      disabled: true
    } },
  { label: '版本对象', prop: 'versionFlag',span: 24,
    component: TrSelect,
    attrs: {
      data: versionOptions,
      disabled: !showCheckInBtn.value
    } },
  { label: '多对象类型', prop: 'multiObjType',span: 24,
    component: TrSelect,
    attrs: {
      data: multiObjectOptions,
      disabled: !showCheckInBtn.value
    } },
  { label: '启用', prop: 'enableFlag', component: TrSwitch,span: 24,
    attrs: { disabled: !showCheckInBtn.value,invalidValue: 0,activeValue: 1,inactiveValue: 2,paramsCode: 'enableFlag' } },
  {
    label: '多对象模式',
    prop: 'isMultiObjectModel',
    component: TrSwitch,
    span: 24,
    attrs: {
      disabled: !showCheckInBtn.value,
      invalidValue: 0,
      activeValue: 1,
      inactiveValue: 2,
      paramsCode: 'enableFlag'
    }
  },
  {
    label: '说明',
    prop: 'description',
    span: 24,
    attrs: {
      disabled: !showCheckInBtn.value,
      type: 'textarea',
      maxlength: ConstraintConfig.TEXTAREA,
      showWordLimit: true
    }
  }
])
//
const tableData = ref([])
const loading = ref(false)
const cashTableData = ref([])
const isShowInnerProperties = ref(true)

const selectedData = computed(() => {
  return cashTableData.value.filter(item => !innerPropertyConfig.includes(item.modelCode))
})
// 当前用户工号
const currentUserNo = computed(() => store.state.user?.user.employeeNo)
// 显示检入按钮
const showCheckInBtn = computed(() => {
  // 有锁的信息，且锁的人是自己，才显示检入按钮
  return form.value.lockInfo && form.value.checkoutBy === currentUserNo.value
})

const showCheckOutBtn = computed(() => {
  return !form.value.lockInfo
})


const formRules = computed(() => ({
  name: [
    { required: true, message: '请输入对象名称', trigger: 'blur' },
    { min: 1, max: ConstraintConfig.TEXT, message: `最大长度为${ConstraintConfig.TEXT}`, trigger: 'blur' }
  ]
}))
const show = row => {
  const _row = cloneDeep(row)

  delete _row.children
  delete _row._X_ROW_CHILD
  form.value = _row
  visible.value = true
  getObjectAttrList()
}
const form = ref({})
const hide = () => {
  visible.value = false
}
// 是否能编辑
const editBtn = (row) => {
  const { baseAttr,inherit } = row

  return (baseAttr && inherit) || !inherit
}
const getObjectAttrList = async () => {
  loading.value = true
  const { data } = await getObjectAttrByBid(form.value.bid).finally(() => loading.value = false)

  tableData.value = data.attrList.sort((a, b) => !!a.inherit - !!b.inherit)
  cashTableData.value = cloneDeep(data.attrList).sort((a, b) => !!a.inherit - !!b.inherit)
  form.value = { ...form.value,...data }
}
const onAddBtnClick = () => {
  propertySelectorRef.value.show()
}
const onAddPropertySubmit = (dataList) => {
  // 判断dataList是否已经存在，存在则给出提示，不存在则添加
  dataList.forEach(item => {
    const isExist = cashTableData.value.some(_item => _item.code === item.code)

    if (isExist) {
      Message.warning(`属性${item.name}已存在`)
    } else {
      // 对象类型的数据会自带 modelCode, 但检入时不需要
      if (item.dataType === 'objectType') {
        delete item.modelCode
      }

      tableData.value.unshift(item)
      if (!isShowInnerProperties.value)cashTableData.value.push(item)
    }
  })

  // TrTableRef.value.updateData()
}
const onDeleteBtnClick = (row) => {
  TrTableRef.value.remove(row)
  // 删除tableData中的数据
  tableData.value = tableData.value.filter(item => item.code !== row.code)
  cashTableData.value = cashTableData.value.filter(item => item.code !== row.code)
}

// 检出操作
const onCheckoutBtnClick = async () => {
  const loading = Loading.service({ target: '.property-dialog', text: '正在检出...' })
  const { data, success, message } = await objectCheckout(form.value.bid).finally(() => loading.close())

  if (success) {
    Message.success(message)
    form.value = { ...form.value,...data }
    emits('check-status-change',true)
  } else {
    Message.warning(message)
  }
}
// 检入操作
const onCheckinBtnClick = async () => {
  form.value.attrList = TrTableRef.value.getData().filter(item => editBtn(item))
  // 格式化数据
  form.value.attrList.forEach(item => {
    item.inherit = !!item.inherit
  })
  // 表单校验
  const valid = await TrFormRef.value.validate()

  if (!valid) return
  const loading = Loading.service({ target: '.property-dialog', text: '正在提交...' })
  const { success, message } = await objectCheckin(form.value).finally(() => loading.close())

  if (success) {
    Message.success(message)
    emits('check-status-change',true)
    hide()
  } else {
    Message.warning(message)
  }
}
// 取消检出
const onCancelCheckinBtnClick = async () => {
  const loading = Loading.service({ target: '.property-dialog', text: '正在取消检出...' })
  const { success, message } = await objectCancelCheckout(form.value.bid).finally(() => loading.close())

  if (success) {
    Message.success(message)
    emits('check-status-change',true)
    hide()
  } else {
    Message.warning(message)
  }
}
const onClose = () => {
  TrFormRef?.value?.resetFields()
  form.value = {}
  tableData.value = []
  cashTableData.value = []
  isShowInnerProperties.value = true
}
const onTabClick = () => {
  nextTick(() => {
    TrTableRef.value.recalculate()
  })
}
const setActiveTab = (name) => {
  activeName.value = name
}

// 过滤掉内置属性
const hideInnerProperties = () => {
  tableData.value = cashTableData.value.filter(item => !item.inherit)
}
const onShowInnerPropertiesChange = (value) => {
  isShowInnerProperties.value = value
  if (value) {
    tableData.value = cashTableData.value
  } else {
    hideInnerProperties()
  }
}

// 跳转去编辑属性
const onJumpToProperties = row => {
  const _router = route.resolve(`/system-manage/properties-manage?code=${row.code}`)

  window.open(_router.href, '_blank')
}

defineExpose({
  show,
  hide,
  setActiveTab
})
</script>

<style scoped lang="scss">
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
::v-deep .inherit-row{
  .vxe-body--column{
    background-color: #ebeeef;

  }
}
::v-deep .el-input{
    .el-input__inner{
      height: 26px !important;
      line-height: 26px !important;
      border: none;
    }
}
::v-deep .el-input-number__decrease,::v-deep .el-input-number__increase{
      top: 0 !important;
      line-height: 26px !important;
      background-color: var(--primary) !important;
}

</style>