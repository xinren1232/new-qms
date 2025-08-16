<template>
  <el-dialog
    title="方法选择"
    :visible.sync="visible"
    :close-on-click-modal="false"
    width="900px"
    append-to-body
  >
    <tr-table
      ref="TrTableRef"
      v-model="tableData"
      class="margin-top"
      :show-pager="true"
      :http-request="httpRequest"
      height="410"
      :params="{ count: true, param: filterForm }"
      :column-config="{resizable: true}"
      border
      immediate
      row-id="code"
      @checkbox-change="onCheckboxChange"
      @current-change="onCurrentChange"
      @checkbox-all="onCheckboxChange"
    >
      <vxe-table-column type="checkbox" width="50" />
      <vxe-table-column title="方法名称" field="name" sortable align="center" min-width="150" />
      <vxe-table-column title="方法分组" field="groupName" sortable align="center" min-width="120">
        <template #default="{ row }">{{ renderFieldByDict(row.groupName,'PROPERTY_GROUP') }}</template>
      </vxe-table-column>
      <vxe-table-column title="语言类型" field="dataType" sortable align="center" min-width="120">
        <template #default="{ row }">{{ renderFieldByDict(row.dataType,'PROPERTY_DATA_TYPE') }}</template>
      </vxe-table-column>
    </tr-table>
    <el-col :span="24" class="text-right">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submit">确定</el-button>
    </el-col>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'

import TrTable from '@@/feature/TrTableX'

import { pageQuery } from '@/views/system-manage/properties-manage/api/index.js'
import { mergeApiNeedUserInfo } from '@/utils'
import { renderFieldByDict } from '@/utils/dict'

const TrTableRef = ref(null)
const filterForm = ref({})
const visible = ref(false)
const selectedRows = ref([])

const emits = defineEmits(['add-method-submit'])

const props = defineProps({
  selectedData: {
    type: Array,
    default: () => []
  }
})

watch(() => props.selectedData, (val) => {
  selectedRows.value = val
  setTimeout(() => {
    TrTableRef.value?.$refs?.tableRef.setCheckboxRow(selectedRows.value,true)
  },200)
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

  selectedRows.value.push(...records)
  // 根据bid去重
  selectedRows.value = selectedRows.value.reduce((prev, next) => {
    prev.some(item => item.bid === next.bid) ? '' : prev.push(next)

    return prev
  }, [])
}

const onCurrentChange = () => {
  const records = TrTableRef.value?.$refs?.tableRef.getCheckboxRecords()

  selectedRows.value.push(...records)
  // 根据bid去重
  selectedRows.value = selectedRows.value.reduce((prev, next) => {
    prev.some(item => item.bid === next.bid) ? '' : prev.push(next)

    return prev
  }, [])

  setTimeout(() => {
    TrTableRef.value?.$refs?.tableRef.setCheckboxRow(selectedRows.value,true)
  },200)
}

const submit = () => {
  emits('add-method-submit', selectedRows.value)
  visible.value = false
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
</style>
