<template>
  <div>
    <div class="tools-box">
      <div class="btn-box" @click="onAddEditBtnClick(null)"><tr-svg-icon icon-class="add-list" /><span class="text">新增</span></div>
      <el-divider direction="vertical" />
      <!--      <div class="btn-box" @click="()=>{}"><tr-svg-icon icon-class="t-import" /><span class="text">导入</span></div>-->
      <!--      <div class="btn-box" @click="()=>{}"><tr-svg-icon icon-class="t-export" /><span class="text">导出</span></div>-->
      <!--      <el-divider direction="vertical" />-->
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
      <vxe-table-column title="模板名称" field="name" sortable align="left" min-width="150">
        <template #default="{row}">
          <el-tooltip content="点击我编辑【基本信息】" :open-delay="600">
            <span class="common-btn active-hover" @click="onAddEditBtnClick(row)">{{ row.name }}</span>
          </el-tooltip>
        </template>
      </vxe-table-column>
      <vxe-table-column title="当前版本" field="currentVersion" sortable align="center" width="110">
        <template #default="{row}">
          <el-tag type="danger">{{ row.currentVersion }}</el-tag>
        </template>
      </vxe-table-column>
      <vxe-table-column title="状态" field="enableFlag" sortable align="center" min-width="180">
        <template #default="{ row }">
          <tr-switch
            v-model.number="row.enableFlag"
            :params="row"
            :http-request="setState"
            :inactive-value="2"
            :active-value="1"
            :invalid-value="0"
            params-code="enableFlag"
          />
        </template>
      </vxe-table-column>
      <vxe-table-column title="说明" show-overflow field="description" sortable align="center" min-width="200" />
      <vxe-table-column title="阶段/状态" field="phaseState" align="center" width="100">
        <template #default="{row}">
          <el-tag>{{ row.phaseState ? '阶段' : '状态' }}</el-tag>
        </template>
      </vxe-table-column>
      <vxe-table-column title="更新人" field="updatedBy" sortable align="center" min-width="130">
        <template #default="{ row }">
          <user-cell :job-number="row.updatedBy" :avatar-size="22" />
        </template>
      </vxe-table-column>
      <vxe-table-column title="更新时间" field="updatedTime" sortable align="center" min-width="140" />
      <vxe-table-column title="操作" align="left" min-width="160" fixed="right">
        <template #default="{ row }" style="display: flex">
          <el-tooltip content="点我可以编辑【状态扭转规则】和【基本信息】" :open-delay="400">
            <el-button type="text" size="small" @click="onAddEditBtnClick(row)">编辑</el-button>
          </el-tooltip>
          <el-tooltip content="点我可以设置【当前启用的版本】" :open-delay="400">
            <el-button type="text" size="small" @click="onVersionListBtnClick(row)">版本设置</el-button>
          </el-tooltip>
          <template v-if="row[STATUS_FIELD] === 0">
            <el-divider direction="vertical" />
            <el-button type="text" class="danger-color" size="small" @click="onDeleteBtnClick(row)">删除</el-button>
          </template>
        </template>
      </vxe-table-column>
    </tr-table>
    <version-list-panel ref="versionListPanelRef" @update-success="onConfirmSuccess" />

  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import route from '@/router'
import { MessageBox, Message } from 'element-ui'

import TrSwitch from '@@/feature/TrSwitch'
import TrTable from '@@/feature/TrTableX'
import FilterPanel from './components/filter-panel.vue'
import UserCell from '@@/plm/user/user-cell.vue'
import VersionListPanel from './components/version-list-panel.vue'

import { pageQuery, deleteTemplate,setState } from './api/index.js'
import { mergeApiNeedUserInfo } from '@/utils'
import tableConfig from '@/config/table.config'
import { STATUS_FIELD } from '@/config/state.config'

const TrTableRef = ref(null)
const filterForm = ref({})
const versionListPanelRef = ref(null)

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
  let router = ''

  if (row) {
    router = route.resolve(`/lifecycle-template-manage/action/detail/${row.bid}/${row.currentVersion}`)
  } else {
    router = route.resolve('/lifecycle-template-manage/action/add')
  }
  window.open(router.href, '_blank')
}
// 新增成功回调
const onConfirmSuccess = () => {
  onFilterChange(filterForm.value)
}
// 版本列表
const onVersionListBtnClick = row => {
  versionListPanelRef.value.show(row)
}
// 删除
const onDeleteBtnClick = row => {
  MessageBox.confirm('是否确认删除？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const data = await deleteTemplate(row.bid)

    if (data?.success) {
      Message.success(data.message)
      onConfirmSuccess()
    }
  }).catch(() => {})
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
