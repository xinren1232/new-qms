<template>
  <tr-card>
    <div class="tools-box">
      <div class="btn-box" @click="onAddEditBtnClick(null)">
        <tr-svg-icon icon-class="add-list" />
        <span class="text">新增</span></div>
      <el-divider direction="vertical" />
      <filter-panel class="btn-box" @filter-change="onFilterChange" />
    </div>
    <tr-table
      ref="TrTableRef"
      v-model="tableData"
      :show-pager="true"
      :http-request="httpRequest"
      :height="height"
      :params="{ count: true, param: filterForm }"
      :column-config="{resizable: true}"
      :row-config="{height:tableConfig.rowHeight}"
      border
      immediate
      show-overflow
    >
      <vxe-table-column title="空间图标" field="icon" sortable align="center" min-width="50">
        <template #default="{ row }">
          <el-avatar :src="row.icon" :size="26" />
        </template>
      </vxe-table-column>
      <vxe-table-column title="空间名称" field="name" sortable align="left" min-width="150" />
      <vxe-table-column title="空间描述" field="description" sortable align="left" min-width="200" />
      <vxe-table-column title="创建人" field="createdBy" sortable align="center" min-width="100">
        <template #default="{ row }">
          <user-cell :job-number="row.updatedBy" :avatar-size="22" />
        </template>
      </vxe-table-column>
      <vxe-table-column title="创建时间" field="createdTime" sortable align="center" min-width="160" />
      <vxe-table-column title="操作" align="left" width="100" fixed="right">
        <template #default="{ row }" style="display: flex">
          <el-button type="text" size="small" @click="onAddEditBtnClick(row)">编辑</el-button>
          <template v-if="row[STATUS_FIELD] === 0">
            <el-divider direction="vertical" />
            <el-button type="text" class="danger-color" size="small" @click="onDeleteBtnClick(row)">删除</el-button>
          </template>
        </template>
      </vxe-table-column>
    </tr-table>
    <add-edit-panel ref="addEditPanelRef" @add-success="onConfirmSuccess" />
  </tr-card>
</template>

<script setup>
import { ref, computed } from 'vue'
import { MessageBox, Message } from 'element-ui'

import TrTable from '@@/feature/TrTableX'
import TrCard from '@@/feature/TrCard'
import AddEditPanel from './components/add-edit-panel.vue'
import FilterPanel from './components/filter-panel.vue'
import UserCell from '@@/plm/user/user-cell.vue'

import { pageQuery, deleteItem } from './api/index.js'
import { mergeApiNeedUserInfo } from '@/utils'
import { STATUS_FIELD } from '@/config/state.config'
import tableConfig from '@/config/table.config'

const TrTableRef = ref(null)
const addEditPanelRef = ref(null)
const filterForm = ref({})

// 表格高度
const height = computed(() => window.innerHeight - 146)
// 人员信息集合
// 表格数据
const tableData = ref([])
// 查询数据方法
const httpRequest = (params) => {
  return mergeApiNeedUserInfo(pageQuery(params), ['updatedBy'])
}
// 查询条件变更
const onFilterChange = form => {
  filterForm.value = form
  const params = { param: form }

  TrTableRef.value.query(params)
}

// 新增
const onAddEditBtnClick = (row) => {
  addEditPanelRef.value.show(row)
}
// 新增成功回调
const onConfirmSuccess = () => {
  onFilterChange(filterForm.value)
}
// 删除
const onDeleteBtnClick = row => {
  MessageBox.confirm('是否确认删除？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const data = await deleteItem(row.bid)

    if (data?.success) {
      Message.success(data.message)
      onConfirmSuccess()
    }
  }).catch(() => {
  })
}
</script>

<style scoped lang="scss">
.bussiness {
  margin-right: 3px;
}

.tools-box {
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

    .text {
      font-size: 14px;
      margin-right: 4px;
      vertical-align: -0.2em;
    }

    .tr-svg-icon {
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
