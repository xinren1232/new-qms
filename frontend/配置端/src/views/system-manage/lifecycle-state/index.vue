<template>
  <div>
    <div class="tools-box">
      <div class="btn-box" @click="onAddEditBtnClick(null)"><tr-svg-icon icon-class="add-list" /><span class="text">新增</span></div>
      <el-divider direction="vertical" />
      <!--      <div class="btn-box" @click="()=>{}"><tr-svg-icon icon-class="t-import" /><span class="text">导入</span></div>-->
      <div class="btn-box" @click="onExportBtnClick">
        <tr-svg-icon v-if="!exportLoading" icon-class="t-export" />
        <i v-else class="el-icon-loading" />
        <span class="text">导出</span>
      </div>
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
      :row-config="{height:tableConfig.rowHeight}"
      :column-config="{resizable: true}"
      border
      immediate
      show-overflow
    >
      <vxe-table-column title="状态名称" field="name" sortable align="left" min-width="200">
        <template #default="{row}">
          <el-tooltip content="点击我编辑【基本信息】" :open-delay="600">
            <span
              class="common-btn cursor-default inline-block rounded text-sm py-2px px-6px empty:hidden"
              :style="{color: replaceOpacity(row.color), backgroundColor: row.color }"
              @click="onAddEditBtnClick(row)"
            >{{ row.name }}</span>
          </el-tooltip>
        </template>
      </vxe-table-column>
      <vxe-table-column title="状态编码" field="code" sortable align="center" min-width="120" />
      <vxe-table-column title="分组" field="groupCode" sortable align="center" min-width="120">
        <template #default="{ row }">
          <dict-label :value="row.groupCode" dict-code="LIFECYCLE_STATE_GROUPS" />
        </template>
      </vxe-table-column>

      <vxe-table-column title="状态" field="enableFlag" sortable align="center" min-width="180">
        <template #default="{ row }">
          <tr-switch
            v-model.number="row.enableFlag"
            :params="row"
            :http-request="createOrUpdateAttribute"
            :inactive-value="2"
            :active-value="1"
            :invalid-value="0"
            params-code="enableFlag"
          />
        </template>
      </vxe-table-column>
      <!--      <vxe-table-column title="标签" field="tag" sortable align="center" min-width="120">-->
      <!--        <template #default="{ row }">-->
      <!--          <dict-label :value="row.tag" dict-code="LIFECYCLE_STATE_TAG" />-->
      <!--        </template>-->
      <!--      </vxe-table-column>-->
      <!--      <vxe-table-column title="说明" field="description" sortable align="center" min-width="200" />-->
      <vxe-table-column title="更新人" field="updatedBy" sortable align="center" min-width="140">
        <template #default="{ row }">
          <user-cell :job-number="row.updatedBy" :avatar-size="22" />
        </template>
      </vxe-table-column>
      <vxe-table-column title="更新时间" field="updatedTime" sortable align="center" min-width="160" />
      <vxe-table-column title="操作" align="left" min-width="60" fixed="right">
        <template #default="{ row }" style="display: flex">
          <el-button type="text" size="small" @click="onAddEditBtnClick(row)">编辑</el-button>
          <el-divider v-if="row.enableFlag === 0" direction="vertical" />
          <el-button v-if="row.enableFlag === 0" type="text" class="danger-color" size="small" @click="onDeleteBtnClick(row)">删除</el-button>
        </template>
      </vxe-table-column>
    </tr-table>
    <add-edit-panel ref="addEditPanelRef" @add-success="onConfirmSuccess" />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { MessageBox, Message } from 'element-ui'

import TrSwitch from '@@/feature/TrSwitch'
import TrTable from '@@/feature/TrTableX'
import AddEditPanel from './components/add-panel.vue'
import FilterPanel from './components/filter-panel.vue'
import UserCell from '@@/plm/user/user-cell.vue'
import tableConfig from '@/config/table.config.js'
import { pageQuery, createOrUpdateAttribute, deleteAttribute } from './api/index.js'
import { mergeApiNeedUserInfo } from '@/utils'
import useExport from '@/hooks/useExport'
import { replaceOpacity } from '@/config/universal-colors.js'

const TrTableRef = ref(null)
const addEditPanelRef = ref(null)
const filterForm = ref({})

// 表格高度
const height = computed(() => window.innerHeight - 120)
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
    const data = await deleteAttribute(row.bid)

    if (data?.success) {
      Message.success(data.message)
      onConfirmSuccess()
    }
  }).catch(() => {})
}
// 导出
const { exportFile,exportLoading } = useExport()
const onExportBtnClick = () => {
  exportFile('LC_STATE')
}
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
