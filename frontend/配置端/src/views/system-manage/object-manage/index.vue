<template>
  <div>
    <div class="tools-box">
      <div class="btn-box" @click="onAddBtnClick">
        <tr-svg-icon icon-class="add-list" />
        <span class="text">新增</span>
      </div>
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
      <el-divider direction="vertical" />
      <div class="btn-box" @click="TrTableRef.setAllTreeExpand(true)">
        <tr-svg-icon icon-class="t-expand-all" />
        <span class="text">展开</span>
      </div>
      <div class="btn-box" @click="TrTableRef.clearTreeExpand()">
        <tr-svg-icon icon-class="t-close-all" />
        <span class="text">收起</span>
      </div>
      <el-divider direction="vertical" />
      <div class="search-box">
        <div class="search-box">
          <span class="text">快捷查询</span>
          <el-input
            v-model="searchText"
            class="search-input"
            placeholder="请输入对象名称或编码"
            clearable
            @clear="onSearchClear"
            @input="onSearch"
          />
        </div>
      </div>
    </div>
    <vxe-table
      ref="TrTableRef"
      :loading="loading"
      :data="tableData"
      border
      :height="height"
      highlight-current-row
      show-overflow
      :row-config="{ height: tableConfig.rowHeight }"
      :seq-column="false"
      :edit-config="{ trigger: 'manual', mode: 'cell', showIcon: false }"
      :radio-config="{ highlight: true }"
      :column-config="{ resizable: true }"
      row-id="modelCode"
      :tree-config="{ transform: true, rowField: 'modelCode', parentField: 'parentBid' }"
      :edit-rules="validRules"
      keep-source
      :row-class-name="rowClassName"
      :cell-class-name="cellClassName"
      @edit-closed="onEditClosed"
      @radio-change="onSelectChange"
      @cell-click="onCurrentChange"
    >
      <vxe-table-column type="seq" width="80" title="No." />
      <vxe-table-column title="对象名称" field="name" sortable min-width="260" :edit-render="{}" tree-node>
        <template #edit="{ row }">
          <el-input v-model="row.name" clearable class="edit-table-input" />
        </template>
        <template #default="{ row }">
          <span class="object-name">
            <el-tooltip v-if="row.lockInfo && row.checkoutBy" placement="top" :content="row.lockInfo">
              <!--              <el-avatar v-if="row.lockInfo" style="margin-right:2px;cursor: pointer" :src="checkoutAvatar(row.checkoutBy)" :size="18" />-->
              <user-cell model="AVATAR" :job-number="row.checkoutBy" :avatar-size="20" />
            </el-tooltip>
            <el-tooltip v-if="row.lockInfo && !row.checkoutBy" placement="top" :content="row.lockInfo">
              <i class="el-icon-lock lock-icon-class" />
            </el-tooltip>
            <!--            <template v-if="row.children && row.children.length">-->
            <!--              <tr-svg-icon v-if="TrTableRef.isTreeExpandByRow(row)" icon-class="t-folder-minus" @click="TrTableRef.toggleTreeExpandByRow(row)" />-->
            <!--              <tr-svg-icon v-else icon-class="t-folder-plus" @click="TrTableRef.toggleTreeExpandByRow(row)" />-->
            <!--            </template>-->
            <el-tooltip content="点我可以编辑【基本信息】" :open-delay="600">
              <span class="common-btn" @click.stop="onObjectNameBtnClick(row)">{{ row.name }}</span>
            </el-tooltip>
          </span>
        </template>
      </vxe-table-column>

      <vxe-table-column title="对象编码" field="modelCode" sortable align="left" min-width="120">
        <!--        <template #edit="{ row }">-->
        <!--          <el-input v-if="!row.bid" v-model="row.code" clearable class="edit-table-input" />-->
        <!--          <span v-else>{{ row.code }}</span>-->
        <!--        </template>-->
      </vxe-table-column>
      <vxe-table-column title="基类编码" field="baseModel" sortable align="left" min-width="120" :edit-render="{}">
        <template #edit="{ row }">
          <el-input v-model="row.baseModel" clearable />
        </template>
        <template #default="{ row }">
          <span>{{ row.baseModel }}</span>
        </template>
      </vxe-table-column>
      <vxe-table-column title="分类" field="type" sortable align="center" min-width="120" :edit-render="{}">
        <template #edit="{ row }">
          <tr-dict v-model="row.type" type="PROJECT_TYPE" />
        </template>
        <template #default="{ row }">
          <dict-label :value="row.type" dict-code="PROJECT_TYPE" />
        </template>
      </vxe-table-column>

      <vxe-table-column title="状态" :field="STATUS_FIELD" sortable align="center" min-width="100">
        <template #default="{ row }">
          <el-tag v-if="row[STATUS_FIELD] === 0" type="info">未启用</el-tag>
          <el-tag v-else-if="row[STATUS_FIELD] === 1" type="success">启用</el-tag>
          <el-tag v-else-if="row[STATUS_FIELD] === 2" type="danger">禁用</el-tag>
        </template>
      </vxe-table-column>
      <vxe-table-column title="操作" align="left" min-width="450" fixed="right">
        <template #default="{ row }">
          <el-tooltip content="点我可以【新增子节点】" :open-delay="400">
            <el-button type="text" size="small" @click="onAddChildBtnClick(row)">新增</el-button>
          </el-tooltip>
          <el-tooltip content="点我可以【设置对象属性】" :open-delay="400">
            <el-button type="text" size="small" @click="onPropertyBtnClick(row)">属性</el-button>
          </el-tooltip>
          <el-tooltip content="点我可以【配置视图匹配规则】" :open-delay="400">
            <el-button type="text" size="small" @click="onViewBtnClick(row)">视图</el-button>
          </el-tooltip>
          <el-tooltip content="点我可以【绑定对象生命周期】" :open-delay="400">
            <el-button type="text" size="small" @click="onLifecycleTemplateBtnClick(row)">生命周期</el-button>
          </el-tooltip>
          <el-tooltip content="点我可以【绑定角色】和【操作权限】" :open-delay="400">
            <el-button type="text" size="small" @click="onPermissionBtnClick(row)">权限</el-button>
          </el-tooltip>
          <el-tooltip content="点我可以绑定【事件】【方法】" :open-delay="400">
            <el-button type="text" size="small" @click="onActionMethodBtnClick(row)">事件</el-button>
          </el-tooltip>
          <el-tooltip content="让属性在【实例】中生效" :open-delay="400">
            <el-button type="text" size="small" @click="onReleaseBtnClick(row)">发布</el-button>
          </el-tooltip>
          <el-tooltip content="点我可以关联【关系】" :open-delay="400">
            <el-button
              v-if="row.multiObjType === 'single'"
              type="text"
              size="small"
              @click="onRelationBtnClick(row, 'single')"
            >
              关系
            </el-button>
          </el-tooltip>
          <el-tooltip content="点我可以关联【poly对象】" :open-delay="400">
            <el-button
              v-if="row.multiObjType === 'poly'"
              type="text"
              size="small"
              @click="onRelationBtnClick(row, 'multi')"
            >
              多对象关系
            </el-button>
          </el-tooltip>
          <template v-if="row[STATUS_FIELD] === 0">
            <el-divider direction="vertical" />
            <el-button type="text" class="danger-color" size="small" @click="onDeleteBtnClick(row)">删除</el-button>
          </template>
          <!--          <el-popover-->
          <!--            placement="left"-->
          <!--            width="100"-->
          <!--            trigger="click"-->
          <!--            popper-class="object-table-handler-popover"-->
          <!--          >-->
          <!--            <div class="popover-item" @click="onLifecycleTemplateBtnClick(row)"><el-button type="text" size="small">生命周期</el-button></div>-->
          <!--            <div class="popover-item" @click="onPermissionBtnClick(row)"><el-button type="text" size="small">权限</el-button></div>-->
          <!--            <div class="popover-item" @click="onAddChildBtnClick(row)"> <el-button type="text" size="small">视图</el-button></div>-->
          <!--            <div class="popover-item" @click="onActionMethodBtnClick(row)"><el-button type="text" size="small">事件方法</el-button></div>-->
          <!--            <div class="popover-item" @click="onDeleteBtnClick(row)"><el-button type="text" class="danger-color" size="small">删除</el-button></div>-->
          <!--            <el-button slot="reference" class="margin-right margin-left" type="text" size="small" icon="el-icon-more" />-->
          <!--          </el-popover>-->
        </template>
      </vxe-table-column>
      <template #empty>
        <tr-no-data type="common" />
      </template>
    </vxe-table>
    <property-panel ref="propertyPanelRef" @check-status-change="queryObjectTreeData" />
    <lifecycle-template-panel ref="lifecycleTemplatePanelRef" />
    <permission-panel ref="permissionPanelRef" />
    <action-method-panel ref="actionMethodPanelRef" />
    <view-panel ref="viewPanelRef" />
    <ReleasePanel ref="releasePanelRef" />
    <RelationPanel ref="relationPanelRef" @check-status-change="queryObjectTreeData" />
  </div>
</template>

<script setup>
import { computed, nextTick, ref, shallowRef, onMounted } from 'vue'
import XEUtils from 'xe-utils'
import { Message, MessageBox } from 'element-ui'
import { STATUS_FIELD } from '@/config/state.config.js'
import { flatTreeData, parseString, setUserStore } from '@/utils'
import UserCell from '@@/plm/user/user-cell.vue'
import { queryObjectTree, createObjectNode, removeObjectNode } from './api/index.js'
import tableConfig from '@/config/table.config.js'
import router from '@/router'

import FilterPanel from './components/filter-panel.vue'
import PropertyPanel from './components/property-panel.vue'
import PermissionPanel from './components/permission-panel/index.vue'
import LifecycleTemplatePanel from './components/lifecycle-template-panel/index.vue'
import ActionMethodPanel from './components/action-method-panel/index.vue'
import ViewPanel from './components/view-panel/index.vue'
import ReleasePanel from './components/release-panel/index.vue'
import RelationPanel from './components/relation-panel/index.vue'
import TrNoData from '@@/feature/TrNoData/index.vue'
import TrDict from '@@/feature/TrDict'
import useExport from '@/hooks/useExport'

const propertyPanelRef = ref(null)
const lifecycleTemplatePanelRef = ref(null)
const permissionPanelRef = ref(null)
const actionMethodPanelRef = ref(null)
const viewPanelRef = ref(null)
const TrTableRef = ref(null)
const selectRow = ref(null)
const loading = ref(false)
const routerQuery = ref(null)

routerQuery.value = router.currentRoute.query

const validRules = {
  name: [{ required: true, message: '对象名不能为空' }],
  baseModel: [{ required: true, message: '基类不能为空' }],
  type: [{ required: true, message: '版本标识不能为空' }]
}

// 搜索条件
const searchText = ref('')
const onSearchClear = () => {
  searchText.value = ''
  TrTableRef.value.clearFilter()
}
const onSearch = () => {
  const filterName = XEUtils.toValueString(searchText.value).trim().toLowerCase()

  if (filterName) {
    const options = { children: 'children' }
    const searchProps = ['name', 'modelCode']

    tableData.value = XEUtils.searchTree(
      cashTableData.value,
      item => searchProps.some(key => XEUtils.toValueString(item[key]).toLowerCase().indexOf(filterName) > -1),
      options
    )
    // 搜索之后默认展开所有子节点
    nextTick(() => {
      TrTableRef.value.setAllTreeExpand(true)
    })
  } else {
    tableData.value = cashTableData.value
  }
}

// 精确选中某一对象
const selectedRowByModeCode = modelCode => {
  const options = { children: 'children' }
  const searchProps = ['modelCode']
  const data = XEUtils.searchTree(
    cashTableData.value,
    item => searchProps.some(key => XEUtils.toValueString(item[key]).toLowerCase() === modelCode.toLowerCase()),
    options
  )

  if (data.length) {
    tableData.value = data
    nextTick(() => {
      TrTableRef.value.setAllTreeExpand(true)
      TrTableRef.value.setCurrentRow(data[data.length - 1])
    })
  } else {
    Message.warning('未找到该对象')
  }
}

// 表格数据
const tableData = ref([])
const cashTableData = ref([])

// 表格高度
const height = computed(() => window.innerHeight - 96)

// 查询角色树
const queryObjectTreeData = async isRefresh => {
  // 保存展开的节点
  let expandList = []

  if (isRefresh) {
    expandList = TrTableRef.value.getTreeExpandRecords()
  }
  loading.value = true
  const { data } = await queryObjectTree().finally(() => {
    loading.value = false
  })

  tableData.value = flatTreeData(data, 'children').map(item => {
    if (item.modelCode.length > 3) {
      item.parentBid = item.modelCode.slice(0, -3)
    } else {
      item.parentBid = '0'
    }
    delete item.children

    return item
  })
  cashTableData.value = parseString(tableData.value, [])
  // 获取检出人工号，去重
  const checkoutBy = tableData.value.map(item => item.checkoutBy).filter(Boolean)
  const checkoutBySet = [...new Set(checkoutBy)]

  setUserStore(checkoutBySet)
  // 默认展开已经展开的节点
  if (isRefresh) {
    nextTick(() => {
      expandList.forEach(item => {
        const _row = tableData.value.find(o => o.modelCode === item.modelCode)

        TrTableRef.value.setTreeExpand(_row, true)
      })
    })
  }
}

queryObjectTreeData()

const onFilterChange = values => {
  const searchProps = []

  Object.keys(values).forEach(key => {
    if (values[key] || values[key] === 0) {
      searchProps.push(key)
    }
  })
  // 对象的每一个属性都有值
  const objectHasValue = searchProps.length === Object.keys(values).length

  if (objectHasValue) {
    const options = { children: 'children' }

    tableData.value = XEUtils.searchTree(
      cashTableData.value,
      item =>
        searchProps.every(key => {
          return String(item[key]).toLowerCase() === String(values[key]).toLowerCase()
        }),
      options
    )
    // 搜索之后默认展开所有子节点
    nextTick(() => {
      TrTableRef.value.setAllTreeExpand(true)
    })
  } else {
    tableData.value = cashTableData.value
  }
}

// 新增字典按钮
const onAddBtnClick = async () => {
  const parentBid = selectRow.value ? selectRow.value.modelCode : '0'
  const baseModel = selectRow.value ? selectRow.value.baseModel : null
  const { row } = await TrTableRef.value.insert({
    baseModel,
    parentBid,
    name: '',
    code: '',
    description: '',
    [STATUS_FIELD]: 0
  })

  await TrTableRef.value.setTreeExpand(row, true)
  await TrTableRef.value.setActiveRow(row)
}
// 失去焦点保存
const onEditClosed = async ({ row }) => {
  if (!row.name) {
    await TrTableRef.value.remove(row)

    return
  }
  const errMap = await TrTableRef.value.validate(row).catch(errMap => errMap)

  if (errMap) {
    return
  }
  // row.modelCode = row.parentBid
  delete row.modelCode
  row.parentCode = row.parentBid
  // row.baseModel = row.parentBid.slice(0,3)

  const { success, message } = await createObjectNode(row)

  if (success) {
    Message.success(message)
    await queryObjectTreeData(true)
  } else {
    Message.error(message)
    TrTableRef.value.remove(row)
  }
}
const onSelectChange = ({ row }) => {
  selectRow.value = row
}
// 新增子节点
const onAddChildBtnClick = async row => {
  // 截取父节点的modelCode，从倒数第三位开始
  const parentBid = row.modelCode
  const baseModel = row ? row.baseModel : null
  const { row: newRow } = await TrTableRef.value.insert({
    baseModel,
    parentBid,
    name: '',
    code: '',
    description: '',
    [STATUS_FIELD]: 0
  })

  // delete newRow.modelCode
  await TrTableRef.value.setTreeExpand(row, true)
  await TrTableRef.value.setActiveRow(newRow)
}

// 删除按钮
const onDeleteBtnClick = row => {
  if (!row.modelCode) {
    TrTableRef.value.remove(row)

    return
  }
  MessageBox.confirm('是否确认删除？', '警告', { type: 'error' }).then(() => {
    removeObjectNode(row.bid).then(data => {
      if (data.success) {
        Message.success(data.message)

        TrTableRef.value.remove(row)
      }
    })
  })
}

// 行样式
const rowClassName = ({ row }) => {
  if (row.checkoutBy) {
    return 'checkout-row'
  }

  if (row.lockInfo) {
    return 'lock-row'
  }
}
const cellClassName = ({ column }) => {
  if (column.property === 'name') {
    return 'link-text'
  }
}

const onPropertyBtnClick = row => {
  propertyPanelRef.value.show(row)
  propertyPanelRef.value.setActiveTab('property')
}
// 对象名称点击
const onObjectNameBtnClick = row => {
  propertyPanelRef.value.show(row)
  propertyPanelRef.value.setActiveTab('baseInfo')
}
const onLifecycleTemplateBtnClick = row => {
  lifecycleTemplatePanelRef.value.show(row)
}

const onPermissionBtnClick = row => {
  permissionPanelRef.value.show(row)
}
const onCurrentChange = ({ row }) => {
  if (selectRow.value === row) {
    selectRow.value = null
    TrTableRef.value.clearCurrentRow()
  } else {
    selectRow.value = row
  }
}
const onActionMethodBtnClick = row => {
  actionMethodPanelRef.value.show(row)
}
const onViewBtnClick = row => {
  viewPanelRef.value.show(row)
}

// 对象发布功能
const releasePanelRef = ref(null)
const onReleaseBtnClick = row => {
  releasePanelRef.value.show(row)
}
// 关系功能
const relationPanelRef = shallowRef(null)
const onRelationBtnClick = (row, type = 'single') => {
  console.log(row, type)
  relationPanelRef.value.show(row, type)
}

onMounted(() => {
  // 从对象详情页跳转过来
  const { modelCode } = routerQuery.value

  if (!modelCode) return
  // 如果query中有modelCode，说明是从对象详情页跳转过来的,选中该对象
  setTimeout(() => {
    selectedRowByModeCode(modelCode)
    // 替换url，去掉modelCode
    router.replace({ query: { ...routerQuery.value, modelCode: undefined } })
  }, 200)
})
// 导出
const { exportFile, exportLoading } = useExport()
const onExportBtnClick = () => {
  exportFile('OBJECT')
}
</script>

<style scoped lang="scss">
@import './css/reset.scss';
@import './css/index.scss';
</style>
<style lang="scss">
.object-table-handler-popover {
  display: flex;
  flex-direction: column;

  .popover-item {
    display: flex;
    align-items: center;
    cursor: pointer;
    padding: 0 4px;
    border-radius: 4px;

    span {
      color: #333;
    }

    .danger-color {
      span {
        color: #f56c6c;
      }
    }

    &:hover {
      background-color: #f3f3f3;
    }

    &.active {
      background-color: #f3f3f3;
    }
  }
}
.object-name{
  cursor: pointer;
  padding: 4px;
  border-bottom: 1px dashed var(--primary-5);
  color: var(--primary-3);
  font-weight: bold;
  &:hover {
    background-color: #eaedec;
    border-radius: 4px;
  }
  &:active {
    background-color: #dee1e0;
  }
}
</style>
