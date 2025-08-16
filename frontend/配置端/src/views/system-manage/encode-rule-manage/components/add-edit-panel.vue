<template>
  <el-dialog
    :visible.sync="visible"
    :close-on-click-modal="false"
    width="1200px"
    title="编码规则"
  >
    <div class="tools-box">
      <div class="btn-box" @click="onAddEditBtnClick(null)"><tr-svg-icon icon-class="add-list" /><span class="text">新增</span></div>
    </div>
    <vxe-table
      ref="TrTableRef"
      :data="tableData"
      height="450"
      border
      :column-config="{resizable: true}"
      :row-config="{height:tableConfig.rowHeight}"
    >
      <!--      <vxe-table-column title="序号" type="seq" align="center" width="60" />-->
      <vxe-table-column title="名称" field="name" sortable align="center" min-width="150" />
      <vxe-table-column title="类型" field="type" sortable align="center" width="150">
        <template #default="{row}">
          <span>{{ renderFieldByDict(row.type,'ENCODE_PROP_TYPE') }}</span>
        </template>
      </vxe-table-column>
      <vxe-table-column title="前分隔符" field="beforeSeparator" align="center" min-width="150" />
      <vxe-table-column title="后分隔符" field="afterDelimiter" align="center" min-width="150" />
      <vxe-table-column title="顺序" field="sort" sortable align="center" min-width="60" />
      <vxe-table-column title="说明" field="description" align="center" min-width="200" />
      <vxe-table-column title="操作" align="center" width="100">
        <template #default="{row}">
          <div style="display: flex;align-items: center;">
            <el-button type="text" @click="onAddEditBtnClick(row)">配置</el-button>
            <el-divider direction="vertical" />
            <el-button type="text" class="danger-color" @click="onDeleteBtnClick(row)">删除</el-button>
          </div>
        </template>
      </vxe-table-column>
    </vxe-table>
    <add-edit-rule-item-panel ref="addEditItemRef" @add-edit-success="onAddEditSuccess" />
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { queryRuleItem,deleteRuleItem } from '../api/index.js'
import AddEditRuleItemPanel from './add-edit-rule-item.vue'
import { renderFieldByDict } from '@/utils/dict'
import tableConfig from '@/config/table.config'

const visible = ref(false)
const TrTableRef = ref(null)
const addEditItemRef = ref(null)
const tableData = ref([])
const currentRow = ref(null)

const show = (row) => {
  currentRow.value = row
  getRuleList()
  visible.value = true
}

const onAddEditBtnClick = (row) => {
  addEditItemRef.value.show(row, currentRow.value.bid)
}
// 获取编码规则列表
const getRuleList = async () => {
  const params = { count: true, param: {
    codeRuleBid: currentRow.value.bid
  }, current: 1, size: 500 }
  const { data,success } = await queryRuleItem(params)

  if (success) {
    tableData.value = data?.data || []
    // 按照顺序排序
    tableData.value.sort((a, b) => a.sort - b.sort)
  }
}
const onDeleteBtnClick = async (row) => {
  const { success } = await deleteRuleItem(row.bid)

  if (success) {
    await getRuleList()
  }
}
const onAddEditSuccess = () => {
  getRuleList()
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
</style>
