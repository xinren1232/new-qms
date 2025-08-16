<template>
  <div>
    <div class="tools-box">
      <div class="btn-box" @click="onAddEditBtnClick(null)"><tr-svg-icon icon-class="add-list" /><span class="text">新增</span></div>
      <el-divider direction="vertical" />
      <!--      <div class="btn-box" @click="importBtnHandler"><tr-svg-icon icon-class="t-import" /><span class="text">导入</span></div>-->
      <!--      <div class="btn-box" @click="exportBtnHandler"><tr-svg-icon icon-class="t-export" /><span class="text">导出</span></div>-->
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
      :column-config="{resizable: true}"
      :row-config="{height:tableConfig.rowHeight}"
      border
      immediate
      show-overflow
    >
      <vxe-table-column title="关系名称" field="name" sortable align="left" min-width="200">
        <template #default="{row}">
          <el-tooltip content="点击我编辑【基本信息】" :open-delay="600">
            <span class="common-btn active-hover" @click="onAddEditBtnClick(row)">{{ row.name }}</span>
          </el-tooltip>
        </template>
      </vxe-table-column>
      <vxe-table-column title="TAB名称" field="tabName" sortable align="left" min-width="120" />
      <vxe-table-column title="源对象" field="sourceModelCode" sortable align="left" min-width="120">
        <template #default="{ row }">
          <el-popconfirm
            v-if="renderObjectName(row.sourceModelCode)"
            :title="`确定要跳转到对象【${renderObjectName(row.sourceModelCode) }】吗？`"
            @confirm="onJumpToObjectBtnClick(row.sourceModelCode)"
          >
            <el-tag slot="reference" class="active-hover">{{ renderObjectName(row.sourceModelCode) }}</el-tag>
          </el-popconfirm>
        </template>
      </vxe-table-column>
      <vxe-table-column title="目标对象" field="targetModelCode" sortable align="left" min-width="120">
        <template #default="{ row }">
          <el-popconfirm
            v-if="renderObjectName(row.targetModelCode)"
            :title="`确定要跳转到对象【${renderObjectName(row.targetModelCode) }】吗？`"
            @confirm="onJumpToObjectBtnClick(row.targetModelCode)"
          >
            <el-tag slot="reference" class="active-hover">{{ renderObjectName(row.targetModelCode) }}</el-tag>
          </el-popconfirm>
        </template>
      </vxe-table-column>
      <!--      <vxe-table-column title="关系对象" field="relationModelCode" sortable align="center" min-width="120">-->
      <!--        <template #default="{ row }">-->
      <!--          <el-popconfirm-->
      <!--            v-if="renderObjectName(row.modelCode)"-->
      <!--            :title="`确定要跳转到对象【${renderObjectName(row.modelCode) }】吗？`"-->
      <!--            @confirm="onJumpToObjectBtnClick(row.modelCode)"-->
      <!--          >-->
      <!--            <el-tag slot="reference" class="active-hover">{{ renderObjectName(row.modelCode) }}</el-tag>-->
      <!--          </el-popconfirm>-->
      <!--        </template>-->
      <!--      </vxe-table-column>-->
      <vxe-table-column title="状态" field="enableFlag" sortable align="center" min-width="180">
        <template #default="{ row }">
          <tr-switch
            v-model.number="row.enableFlag"
            :params="row"
            :http-request="setStatus"
            :inactive-value="2"
            :active-value="1"
            :invalid-value="0"
            params-code="enableFlag"
          />
        </template>
      </vxe-table-column>
      <!--      <vxe-table-column title="说明" field="description" sortable align="center" min-width="180" />-->
      <vxe-table-column title="更新人" field="updatedBy" sortable align="center" min-width="140">
        <template #default="{ row }">
          <user-cell :job-number="row.updatedBy" :avatar-size="22" />
        </template>
      </vxe-table-column>
      <vxe-table-column title="更新时间" field="updatedTime" sortable align="center" min-width="160" />
      <vxe-table-column title="操作" align="left" min-width="100" fixed="right">
        <template #default="{ row }" style="display: flex">
          <el-button type="text" size="small" @click="onAddEditBtnClick(row)">编辑</el-button>
          <template v-if="row[STATUS_FIELD] === 0">
            <el-divider direction="vertical" />
            <el-button type="text" class="danger-color" size="small" @click="onDeleteBtnClick(row)">删除</el-button>
          </template>
        </template>
      </vxe-table-column>
    </tr-table>
    <add-edit-panel
      ref="addEditPanelRef"
      :object-data-map-info="objectDataMapInfo"
      :object-data-map="objectDataMap"
      :object-tree-data="objectTreeData"
      @add-success="onConfirmSuccess"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { MessageBox, Message } from 'element-ui'
import route from '@/router'
import TrSwitch from '@@/feature/TrSwitch'
import TrTable from '@@/feature/TrTableX'
import AddEditPanel from './components/add-panel.vue'
import FilterPanel from './components/filter-panel.vue'
import UserCell from '@@/plm/user/user-cell.vue'
import tableConfig from '@/config/table.config'

import { pageQuery, deleteItem, setStatus } from './api/index.js'
import { flatTreeData, mergeApiNeedUserInfo } from '@/utils'
import { queryObjectTree } from '@/views/system-manage/object-manage/api/index.js'
import { STATUS_FIELD } from '@/config/state.config'

const TrTableRef = ref(null)
const addEditPanelRef = ref(null)
// const importPanelRef = ref(null)
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
  }).catch(() => {})
}

const objectTreeData = ref([])
const objectDataMap = ref({})
const objectDataMapInfo = ref({})
const getObjectTreeData = async() => {
  const { data } = await queryObjectTree()

  objectTreeData.value = data
  // 递归过滤掉type==='RELATION'的数据
  objectTreeData.value = flatTreeData(objectTreeData.value, 'children').filter(item => item.type !== 'RELATION')
  flatTreeData(objectTreeData.value,'children').forEach(item => {
    objectDataMap.value[item.modelCode] = item.name
    objectDataMapInfo.value[item.modelCode] = item
  })
}

getObjectTreeData()

const renderObjectName = (modelCode) => {

  return objectDataMap.value[modelCode]
}
// 跳转到对象管理
const onJumpToObjectBtnClick = (modelCode) => {
  route.push(`/system-manage/object-manage?modelCode=${modelCode}`)
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
