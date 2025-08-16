<template>
  <div>
    <div class="tools-box">
      <div class="btn-box" @click="onAddEditBtnClick(null)">
        <tr-svg-icon icon-class="add-list" />
        <span class="text">{{ $t('common.add') }}</span></div>
      <el-divider direction="vertical" />
      <div class="btn-box" @click="()=>{}">
        <tr-svg-icon icon-class="t-import" />
        <span class="text">{{ $t('common.import') }}</span></div>
      <div class="btn-box" @click="onExportBtnClick">
        <tr-svg-icon v-if="!exportLoading" icon-class="t-export" />
        <i v-else class="el-icon-loading" />
        <span class="text">{{ $t('common.export') }}</span></div>
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
      show-overflow
      :column-config="{resizable: true}"
      border
      immediate
    >
      <vxe-table-column :title="$t('property.name')" field="name" sortable align="left" min-width="200">
        <template #default="{row}">
          <el-tooltip content="点击我编辑基本信息" :open-delay="600">
            <span class="common-btn active-hover" @click="onAddEditBtnClick(row)">{{ row.name }}</span>
          </el-tooltip>
        </template>
      </vxe-table-column>
      <vxe-table-column :title="$t('property.code')" field="code" sortable align="center" min-width="120" />
      <vxe-table-column :title="$t('property.group')" field="groupName" sortable align="center" min-width="120">
        <template #default="{ row }">
          <dict-label :value="row.groupName" dict-code="PROPERTY_GROUP" />
        </template>
      </vxe-table-column>
      <vxe-table-column :title="$t('property.dataType')" field="dataType" sortable align="center" min-width="120">
        <template #default="{ row }">
          <dict-label :value="row.dataType" dict-code="PROPERTY_DATA_TYPE" />
        </template>
      </vxe-table-column>
      <vxe-table-column :title="$t('common.status')" field="enableFlag" sortable align="center" min-width="180">
        <template #default="{ row }">
          <tr-switch
            v-model="row.enableFlag"
            params-code="enableFlag"
            :params="row"
            :http-request="createOrUpdateAttribute"
            :active-value="1"
            :inactive-value="2"
            :invalid-value="0"
          />
        </template>
      </vxe-table-column>
      <vxe-table-column :title="$t('common.updatedBy')" field="updatedBy" sortable align="center" min-width="140">
        <template #default="{ row }">
          <user-cell :job-number="row.updatedBy" :avatar-size="22" />
        </template>
      </vxe-table-column>
      <vxe-table-column :title="$t('common.updatedTime')" field="updatedTime" sortable align="center" min-width="160" />
      <vxe-table-column :title="$t('common.action')" align="left" min-width="110" fixed="right">
        <template #default="{ row }" class="flex">
          <el-button type="text" size="small" @click="onAddEditBtnClick(row)">{{ $t('common.edit') }}</el-button>
          <el-divider v-if="row[STATUS_FIELD] === 0" direction="vertical" />
          <el-button
            v-if="row[STATUS_FIELD] === 0"
            type="text"
            class="danger-color"
            size="small"
            @click="onDeleteBtnClick(row)"
          >{{ $t('common.delete') }}
          </el-button>
        </template>
      </vxe-table-column>
    </tr-table>
    <add-edit-panel ref="addEditPanelRef" @add-success="onConfirmSuccess" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { MessageBox, Message } from 'element-ui'
import TrSwitch from '@@/feature/TrSwitch'
import TrTable from '@@/feature/TrTableX'
import AddEditPanel from './components/add-edit-panel.vue'
import FilterPanel from './components/filter-panel.vue'
import UserCell from '@@/plm/user/user-cell.vue'
import i18n from '@/i18n'
import { pageQuery, createOrUpdateAttribute, deleteAttribute } from './api/index.js'
import { mergeApiNeedUserInfo } from '@/utils'
import tableConfig from '@/config/table.config'
import route from '@/router'
import useExport from '@/hooks/useExport'
import { STATUS_FIELD } from '@/config/state.config'
// import component, { TrSelect } from 'trWebCommon/component'
// import utils from 'trWebCommon/utils'

// console.log(TrSelect)
// console.log(utils)
// console.log(component.TrSelect)
// console.log(component)

const TrTableRef = ref(null)
const addEditPanelRef = ref(null)
const filterForm = ref({})

// 获取路由上的参数
const routerQuery = route.currentRoute.query

// 表格高度
const height = computed(() => window.innerHeight - 106)
// 人员信息集合
// 表格数据
const tableData = ref([])
// 查询数据方法
const httpRequest = (params) => {
  if (routerQuery?.code) {
    params.param.code = routerQuery.code
  }

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
  MessageBox.confirm(i18n.t('delete.sub_title'), i18n.t('delete.title'), {
    confirmButtonText: i18n.t('common.confirm'),
    cancelButtonText: i18n.t('common.cancel'),
    type: 'warning'
  }).then(async () => {
    const data = await deleteAttribute(row.bid)

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
  exportFile('ATTRS')
}

onMounted(() => {
  // 重置路由参数
  if (routerQuery?.code) {
    route.replace({ query: {} })
  }

})
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
