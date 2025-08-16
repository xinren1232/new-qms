<template>
  <div>
    <div class="tools-box">
      <div class="btn-box" @click="onAddEditBaseInfoBtnClick(null)"><tr-svg-icon icon-class="add-list" /><span class="text">新增</span></div>
      <el-divider direction="vertical" />
      <!--      <div class="btn-box" @click="()=>{}"><tr-svg-icon icon-class="t-import" /><span class="text">导入</span></div>-->
      <div class="btn-box" @click="onExportBtnClick">
        <tr-svg-icon v-if="!exportLoading" icon-class="t-export" />
        <i v-else class="el-icon-loading" />
        <span class="text">导出</span></div>
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
      <vxe-table-column title="视图名称" field="name" sortable align="left" min-width="150">
        <template #default="{row}">
          <el-tooltip content="点击我编辑基本信息" :open-delay="600">
            <span class="common-btn active-hover" @click="onAddEditBaseInfoBtnClick(row)">{{ row.name }}</span>
          </el-tooltip>
        </template>
      </vxe-table-column>
      <vxe-table-column title="视图类型" field="type" sortable align="left" min-width="140">
        <template #default="{row}">
          <dict-label :value="row.type" dict-code="VIEW_TYPE" />
          <el-popconfirm
            :title="`确定要跳转到对象【${renderRelationObjectLabel(row.modelCode)}】吗？`"
            @confirm="onJumpToObjectBtnClick(row.modelCode)"
          >
            <span v-if="row.modelCode &&renderRelationObjectLabel(row.modelCode)" slot="reference" class="cursor-pointer">
              [ <el-tag class="active-hover">{{ renderRelationObjectLabel(row.modelCode) }}</el-tag> ]
            </span>
          </el-popconfirm>


        </template>
      </vxe-table-column>
      <!--      <vxe-table-column title="对象" field="modelCode" sortable align="center" min-width="100">-->
      <!--        <template #default="{row}">-->
      <!--        -->
      <!--        </template>-->
      <!--      </vxe-table-column>-->
      <vxe-table-column title="状态" field="enableFlag" sortable align="center" min-width="180">
        <template #default="{ row }">
          <tr-switch
            v-model.number="row.enableFlag"
            :params="row"
            :http-request="setViewStatus"
            :inactive-value="2"
            :active-value="1"
            :invalid-value="0"
            params-code="enableFlag"
          />
        </template>
      </vxe-table-column>
      <!--      <vxe-table-column title="说明" show-overflow field="description" sortable align="center" min-width="100" />-->
      <vxe-table-column title="终端类型" field="clientType" sortable align="center" min-width="90">
        <template #default="{row}">
          <dict-label :value="row.clientType" dict-code="CLIENT_TYPE" />
        </template>
      </vxe-table-column>
      <vxe-table-column title="更新人" field="updatedBy" sortable align="center" min-width="130">
        <template #default="{ row }">
          <user-cell v-if="row.updatedBy" :job-number="row.updatedBy" :avatar-size="22" />
        </template>
      </vxe-table-column>
      <vxe-table-column title="更新时间" field="updatedTime" sortable align="center" min-width="120" />
      <vxe-table-column title="操作" align="left" min-width="200" fixed="right">
        <template #default="{ row }" style="display: flex">
          <el-button type="text" size="small" @click="onAddEditBaseInfoBtnClick(row)">基本信息</el-button>
          <el-button type="text" size="small" @click="onViewDesignerBtnClick(row)">设计</el-button>
          <el-button type="text" size="small" @click="onViewPreviewBtnClick(row)">预览</el-button>
          <!--          <el-button type="text" size="small" @click="onViewInstanceListBtnClick(row)">实例查询</el-button>-->
          <el-divider v-if="row.enableFlag === 0" direction="vertical" />
          <el-button v-if="row.enableFlag === 0" type="text" class="danger-color" size="small" @click="onDeleteBtnClick(row)">删除</el-button>
        </template>
      </vxe-table-column>
    </tr-table>
    <add-edit-base-info ref="addEditBaseInfoRef" :object-tree-data="objectTreeData" @on-submit="onConfirmSuccess" />
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
import AddEditBaseInfo from './components/add-edit-base-info.vue'

import { pageQuery, deleteView,setViewStatus } from './api/index.js'
import { queryObjectTree } from '@/views/system-manage/object-manage/api/index.js'
import { mergeApiNeedUserInfo,flatTreeData } from '@/utils'
import tableConfig from '@/config/table.config'
import useExport from '@/hooks/useExport'

const TrTableRef = ref(null)
const filterForm = ref({})
const objectTreeData = ref([])
const objectTreeDataFlat = ref([])
const addEditBaseInfoRef = ref(null)

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

// 视图设计
const onViewDesignerBtnClick = (row) => {
  const _router = route.resolve(`/view-manage/action/designer/${row.bid}`)

  // 新开页面
  window.open(_router.href, '_blank')

}

// 新增基本信息
const onAddEditBaseInfoBtnClick = (row) => {
  addEditBaseInfoRef.value.show(row)
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
    const data = await deleteView(row.bid)

    console.log(data)

    if (data?.success) {
      Message.success(data.message)
      onConfirmSuccess()
    }
  }).catch(() => {})
}


// 查询对象树
const queryObjectTreeData = async () => {
  const data = await queryObjectTree()

  if (data?.success) {
    objectTreeData.value = data.data
    objectTreeDataFlat.value = flatTreeData(data.data,'children')
  }
}

queryObjectTreeData()
const renderRelationObjectLabel = (modelCode) => {
  const object = objectTreeDataFlat.value.find(item => item.modelCode === modelCode)

  return object?.name
}

const onViewPreviewBtnClick = (row) => {
  let routeData = route.resolve('/view-manage/action/preview/' + row.bid)

  window.open(routeData.href, '_blank')
}

// 视图实例数据查询
// eslint-disable-next-line no-unused-vars
const onViewInstanceListBtnClick = (row) => {
  let routeData = route.resolve('/view-instance-list/' + row.bid)

  window.open(routeData.href, '_blank')
}
// 跳转到对象管理
const onJumpToObjectBtnClick = (modelCode) => {
  route.push(`/system-manage/object-manage?modelCode=${modelCode}`)
}
// 导出
const { exportFile,exportLoading } = useExport()
const onExportBtnClick = () => {
  exportFile('VIEW')
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
