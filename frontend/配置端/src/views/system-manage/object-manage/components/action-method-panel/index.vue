<template>
  <el-dialog
    :visible.sync="visible"
    title="事件方法"
    width="900px"
    :close-on-click-modal="false"
  >
    <div class="tools-box">
      <div class="btn-box" @click="onAddBtnClick"><tr-svg-icon icon-class="add-list" /><span class="text">新增</span></div>
    </div>
    <vxe-table
      ref="TrTableRef"
      :data="tableData"
      border
      :height="400"
      :seq-column="false"
      :edit-config="{trigger:'click',mode:'cell',showStatus:false,showIcon:false}"
      :column-config="{resizable: true}"
      :edit-rules="validRules"
      keep-source
      @edit-closed="onEditClosed"
    >
      <vxe-table-column type="seq" width="80" title="No." />
      <vxe-table-column title="事件" field="eventBid" sortable min-width="120" :edit-render="{}">
        <template #edit="{ row }">
          <tr-dict v-model="row.eventBid" type="OBJECT_ACTION" />
        </template>
        <template #default="{ row }">
          <span>{{ renderFieldByDict( row.eventBid,'OBJECT_ACTION') }}</span>
        </template>
      </vxe-table-column>

      <vxe-table-column title="方法" field="methodBid" sortable align="center" min-width="120" :edit-render="{}">
        <template #default="{ row }">
          <span>{{ renderMethodLabel(row.methodBid) }}</span>
        </template>
        <template #edit="{ row }">
          <tr-select v-model="row.methodBid" :data="methodList" label-field="name" value-field="code" />
        </template>
      </vxe-table-column>
      <vxe-table-column title="方法类型" field="executeType" sortable align="center" min-width="120" :edit-render="{}">
        <template #default="{row}">
          <span>{{ renderFieldByDict( row.executeType,'OBJECT_METHOD_TYPE') }}</span>
        </template>
        <template #edit="{ row }">
          <tr-dict v-model="row.executeType" type="OBJECT_METHOD_TYPE" />
        </template>
      </vxe-table-column>
      <vxe-table-column title="说明" field="description" sortable align="center" min-width="200" :edit-render="{}">
        <template #edit="{ row }">
          <el-input v-model="row.description" clearable class="edit-table-input" />
        </template>
      </vxe-table-column>
      <vxe-table-column title="操作" align="center" width="120">
        <template #default="{ row }">
          <el-button type="text" class="danger-color" size="small" @click="onDeleteBtnClick(row)">移除</el-button>
        </template>
      </vxe-table-column>
    </vxe-table>
  </el-dialog>
</template>
<script setup>
import { ref, nextTick } from 'vue'
import { Message } from 'element-ui'
import TrSelect from '@@/feature/TrSelect'
import TrDict from '@@/feature/TrDict'
import { renderFieldByDict } from '@/utils/dict'
import { pageQuery } from '@/views/system-manage/method-manage/api/index.js'
import { createOrUpdateActionMethod,queryActionMethod,deleteActionMethod } from './api.js'
import ConstraintConfig from '@/config/constraint.config'

const visible = ref(false)
const tableData = ref([])
const TrTableRef = ref(null)
const methodList = ref([])
const objectInfo = ref({})

const validRules = ref({
  eventBid: [
    { required: true, message: '请选择事件', trigger: 'change' }
  ],
  methodBid: [
    { required: true, message: '请选择方法', trigger: 'change' }
  ],
  executeType: [
    { required: true, message: '请选择方法类型', trigger: 'change' }
  ],
  description: [
    { required: true, message: `请输入说明,最大长度${ConstraintConfig.TEXTAREA}`, trigger: 'change' },
    { max: ConstraintConfig.TEXTAREA, message: `请输入说明,最大长度${ConstraintConfig.TEXTAREA}`, trigger: 'change' }
  ]
})

const onEditClosed = async ({ row }) => {
  await nextTick()
  const changeData = await TrTableRef.value.getUpdateRecords()

  if (!changeData.length && row.bid) {
    return
  }

  if (!row.eventBid) {
    await TrTableRef.value.remove(row)

    return
  }
  const errMap = await TrTableRef.value.validate(row).catch(errMap => errMap)

  if (errMap) {
    return
  }
  const params = JSON.parse(JSON.stringify(row))

  params.executeType = Number(params.executeType)


  const { data, success,message } = await createOrUpdateActionMethod(params)

  data.executeType = String(data.executeType)
  if (success) {
    Message.success(message)
    // 更新当前行数据
    // TrTableRef.value.remove(row)
    // await TrTableRef.value.insert({ ...row, ...data })
    // await TrTableRef.value.clearAll()
    await getActionMethodList()
  }

}

const onDeleteBtnClick = async (row) => {
  const { message,success } = await deleteActionMethod(row.bid)

  if (success) {
    Message.success(message)
    TrTableRef.value.remove(row)
  }
}

const onAddBtnClick = async () => {
  const { bid, modelCode } = objectInfo.value
  const record = { modelBid: bid, modelCode: modelCode, bid: null, eventBid: null, methodBid: null, executeType: null, description: null }

  const { row } = await TrTableRef.value.insert(record)

  TrTableRef.value.setActiveRow(row)

}
const show = row => {
  objectInfo.value = row
  getActionMethodList()
  visible.value = true
}
// eslint-disable-next-line no-unused-vars
const submit = () => {
  visible.value = false
}

const queryMethodList = async () => {
  const params = { 'count': true, 'param': {}, 'current': 1, 'size': 500 }
  const { data } = await pageQuery(params)

  methodList.value = data.data
}
const renderMethodLabel = (value) => {
  const item = methodList?.value?.find((item) => item.code === value)

  return item ? item.name : ''
}
const getActionMethodList = async () => {
  const { bid,modelCode } = objectInfo.value
  const params = { count: true,
    param: {
      modelBid: bid,
      modelCode
    }, current: 1, size: 500 }
  const { data } = await queryActionMethod(params)

  tableData.value = data.data.map(item => {
    item.executeType = String(item.executeType)

    return item
  })
}

// eslint-disable-next-line no-unused-vars
const createOrUpdate = async () => {
  const params = {
    'actionBid': '1',
    'methodBid': '1',
    'executeType': '1',
    'description': '1'
  }
  const { data } = await createOrUpdateActionMethod(params)

  methodList.value = data.data
}

queryMethodList()
defineExpose({
  show
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
      width:fit-content;
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
  .search-box{
    display: flex;
    align-items: center;
    width: 400px;
    .text{
      color: var(--primary);
      padding: 0 2px;
      width: 80px;
    }
    .search-input ::v-deep .el-input__inner {
      border-radius: 2px;
      padding: 0 10px;
      height: 34px;
      font-size: 14px;
      color: var(--primary);
      &:focus {
        border: 1px solid var(--primary-5);
      }
    }
  }
}
</style>
