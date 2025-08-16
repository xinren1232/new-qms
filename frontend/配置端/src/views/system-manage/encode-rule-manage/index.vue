<template>
  <div>
    <div class="tools-box">
      <div class="btn-box" @click="onAddEditBaseInfoBtnClick(null)">
        <tr-svg-icon icon-class="add-list" />
        <span class="text">新增</span></div>
      <el-divider direction="vertical" />
      <!--      <div class="btn-box">-->
      <!--        <tr-svg-icon icon-class="t-import" />-->
      <!--        <span class="text">导入</span></div>-->
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
      border
      immediate
      show-overflow
      :seq-column="false"
      :column-config="{resizable: true}"
      :row-config="{height:tableConfig.rowHeight}"
    >
      <vxe-column title="No." type="seq" width="60" align="center" />
      <vxe-table-column title="编码名称" field="name" sortable align="left" min-width="150">
        <template #default="{row}">
          <el-tooltip content="点击我编辑【基本信息】" :open-delay="600">
            <span class="common-btn active-hover" @click="onAddEditBaseInfoBtnClick(row)">
              {{ row.name }}
            </span>
          </el-tooltip>
        </template>
      </vxe-table-column>
      <vxe-table-column title="状态" field="enableFlag" sortable align="center" min-width="180">
        <template #default="{ row }">
          <tr-switch
            v-model="row.enableFlag"
            :params="row"
            :http-request="createOrUpdateEncode"
            :inactive-value="2"
            :active-value="1"
            :invalid-value="0"
            :params-code="STATUS_FIELD"
          />
        </template>
      </vxe-table-column>

      <vxe-table-column title="更新人" field="updatedBy" sortable align="center" min-width="120">
        <template #default="{ row }">
          <user-cell :job-number="row.updatedBy" :avatar-size="22" />
        </template>
      </vxe-table-column>
      <vxe-table-column title="更新时间" field="updatedTime" sortable align="center" min-width="160" />
      <vxe-table-column title="说明" field="description" sortable align="center" min-width="200" />
      <vxe-table-column title="操作" align="left" min-width="180" fixed="right">
        <template #default="{ row }" style="display: flex">
          <el-button type="text" size="small" @click="onAddEditBaseInfoBtnClick(row)">基本信息</el-button>
          <el-button type="text" size="small" @click="onAddEditBtnClick(row)">合成规则</el-button>
          <el-divider v-if="row.enableFlag === 0" direction="vertical" />
          <el-button
            v-if="row.enableFlag === 0"
            type="text"
            class="danger-color"
            size="small"
            @click="onDeleteBtnClick(row)"
          >删除
          </el-button>
        </template>
      </vxe-table-column>
    </tr-table>
    <add-edit-base-info-panel ref="addEditBaseInfoPanelRef" @add-edit-success="onConfirmSuccess" />
    <add-edit-panel ref="addEditPanelRef" @add-edit-success="onConfirmSuccess" />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { MessageBox, Message } from 'element-ui'

import TrSwitch from '@@/feature/TrSwitch'
import TrTable from '@@/feature/TrTableX'
import FilterPanel from './components/filter-panel.vue'
import UserCell from '@@/plm/user/user-cell.vue'
import AddEditPanel from './components/add-edit-panel.vue'
import AddEditBaseInfoPanel from './components/add-edit-base-info.vue'

import { pageQuery, createOrUpdateEncode, deleteRule } from './api/index.js'
import { mergeApiNeedUserInfo } from '@/utils'
import tableConfig from '@/config/table.config'
import { STATUS_FIELD } from '@/config/state.config'
import useExport from '@/hooks/useExport'

const TrTableRef = ref(null)
const addEditPanelRef = ref(null)
const addEditBaseInfoPanelRef = ref(null)
const filterForm = ref({})

// 表格高度
const height = computed(() => window.innerHeight - 106)
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
const onAddEditBaseInfoBtnClick = (row) => {
  addEditBaseInfoPanelRef.value.show(row)
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
    const data = await deleteRule(row.bid)

    if (data?.success) {
      Message.success(data.message)
      onConfirmSuccess()
    }
  }).catch(() => {
  })
}
// 导出
const { exportFile, exportLoading } = useExport()
const onExportBtnClick = () => {
  exportFile('ENCODE')
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
