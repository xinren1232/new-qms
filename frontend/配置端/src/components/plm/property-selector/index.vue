<template>
  <el-dialog
    :visible.sync="visible"
    :close-on-click-modal="false"
    width="1000px"
    append-to-body
    @close="onClose"
  >
    <div slot="title" class="flex justify-between padding-right margin-right">
      <span class="el-dialog__title">属性选取</span>
      <div class="search-box">
        <el-input v-model="searchValue" clearable placeholder="请输入属性名称" @input="onChange" />
      </div>
    </div>
    <tr-table
      ref="TrTableRef"
      v-model="tableData"
      class="margin-top"
      :show-pager="true"
      :http-request="httpRequest"
      height="410"
      :params="{ count: true, param: filterForm}"
      :column-config="{resizable: true}"
      border
      immediate
      row-id="code"
      :pagination="{pageSize}"
      @checkbox-change="onCheckboxChange"
      @current-change="onCurrentChange"
      @checkbox-all="onCheckboxChange"
    >
      <vxe-table-column type="checkbox" width="40" />
      <vxe-table-column title="属性名称" field="name" sortable align="center" min-width="150" />
      <vxe-table-column title="属性编码" field="code" sortable align="center" min-width="120" />
      <vxe-table-column title="属性组" field="groupName" sortable align="center" min-width="120">
        <template #default="{ row }">{{ renderFieldByDict(row.groupName,'PROPERTY_GROUP') }}</template>
      </vxe-table-column>
      <vxe-table-column title="数据类型" field="dataType" sortable align="center" min-width="120">
        <template #default="{ row }">{{ renderFieldByDict(row.dataType,'PROPERTY_DATA_TYPE') }}</template>
      </vxe-table-column>
    </tr-table>
    <el-col :span="24" class="text-right padding-bottom">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submit">确定</el-button>
    </el-col>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'

import TrTable from '@@/feature/TrTableX'

import { pageQuery } from '@/views/system-manage/properties-manage/api/index.js'
import { mergeApiNeedUserInfo } from '@/utils'
import { renderFieldByDict } from '@/utils/dict'

const TrTableRef = ref(null)
const filterForm = ref({})
const pageSize = 1000
const visible = ref(false)
const selectedRows = ref([])
const searchValue = ref('')

const emits = defineEmits(['add-property-submit'])

defineProps({
  selectedData: {
    type: Array,
    default: () => []
  }
})


const show = () => {
  visible.value = true
}
// 人员信息集合
// 表格数据
const tableData = ref([])
// 查询数据方法
const httpRequest = (params) => {
  return mergeApiNeedUserInfo(pageQuery(params), ['updatedBy'])
}
const onCheckboxChange = () => {
  const records = TrTableRef.value?.$refs?.tableRef.getCheckboxRecords()

  selectedRows.value = records
  console.log(records,selectedRows.value)
}

const onCurrentChange = () => {
  selectedRows.value = TrTableRef.value?.$refs?.tableRef.getCheckboxRecords()
}

const submit = () => {
  emits('add-property-submit', selectedRows.value)
  visible.value = false
}
const onClose = () => {
  TrTableRef.value?.$refs?.tableRef.clearCheckboxRow()
}

const onChange = (val) => {
  filterForm.value = {
    param: {
      ...filterForm.value,
      name: val
    }
  }
  TrTableRef.value?.query(filterForm.value)
}

defineExpose({
  show
})
</script>

<style scoped lang="scss">
.bussiness {
  margin-right: 3px;
}
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
::v-deep .el-dialog__body{
  padding-bottom: 2px;
}
::v-deep .el-dialog__headerbtn{
  top: 25px;
}

.search-box {
  width: 300px;
  display: flex;
  align-items: center;
  margin: 0 10px;

  .el-button {
    margin-left: 10px;
  }
}
</style>
