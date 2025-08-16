<template>
  <div>
    <div class="tools-box">
      <div class="btn-box" @click="onAddBtnClick"><tr-svg-icon icon-class="add-list" /><span class="text">新增</span></div>
      <el-divider direction="vertical" />
      <!--      <div class="btn-box"><tr-svg-icon icon-class="t-import" /><span class="text">导入</span></div>-->
      <div class="btn-box" @click="onExportBtnClick">
        <tr-svg-icon v-if="!exportLoading" icon-class="t-export" />
        <i v-else class="el-icon-loading" />
        <span class="text">导出</span>
      </div>
      <el-divider direction="vertical" />
      <filter-panel class="btn-box" @filter-change="onFilterChange" />
      <el-divider direction="vertical" />
      <div class="btn-box" @click="TrTableRef.setAllTreeExpand(true)"><tr-svg-icon icon-class="t-expand-all" /><span class="text">展开</span></div>
      <div class="btn-box" @click="TrTableRef.clearTreeExpand()"><tr-svg-icon icon-class="t-close-all" /><span class="text">收起</span></div>
      <el-divider direction="vertical" />
      <div class="search-box">
        <span class="text">快捷查询</span>
        <el-input v-model="searchText" class="search-input" placeholder="请输入角色名称/编码" clearable @clear="onSearchClear" @input="onSearch">
          <template #prefix>
            <tr-svg-icon icon-class="t-search" />
          </template>
        </el-input>
      </div>
    </div>
    <vxe-table
      ref="TrTableRef"
      :data="tableData"
      border
      :height="height"
      :seq-column="false"
      show-overflow
      :edit-config="{trigger:'click',mode:'cell',showIcon:false}"
      :radio-config="{ highlight: true}"
      :column-config="{resizable: true}"
      :tree-config="treeConfig"
      :row-config="{height:tableConfig.rowHeight,isHover: true}"
      :edit-rules="validRules"
      keep-source
      @radio-change="onSelectChange"
      @cell-click="onCurrentChange"
    >
      <!--      <vxe-column type="radio" width="50">-->
      <!--        <template #header>-->
      <!--          <el-button type="text" :disabled="!selectRow" @click="onClearRadioRow">取消</el-button>-->
      <!--        </template>-->
      <!--      </vxe-column>-->
      <!--      <vxe-table-column type="seq" width="80" title="No." />-->
      <vxe-table-column title="角色名称" field="name" sortable min-width="240" tree-node>
        <template #default="{ row }">
          <span class="role-name-box">
            <span>{{ row.name }}</span>
          </span>
          <div class="handler-box">
            <el-tooltip content="新增子节点" :open-delay="500">
              <el-button class="add !p-1" type="primary" icon="el-icon-plus" @click="onAddBtnClick(row)" />
            </el-tooltip>
            <el-tooltip content="编辑名称" :open-delay="500">
              <el-button class="expand !p-1 name-edit" type="primary" icon="el-icon-edit" @click="onEditBtnClick(row)" />
            </el-tooltip>
            <el-tooltip v-if="row.type ==='SYS_GLOBAL'" content="添加人员" :open-delay="500">
              <el-button class="expand !p-1 name-edit" type="primary" icon="el-icon-user" @click="onUserBtnClick(row)" />
            </el-tooltip>
            <el-tooltip content="删除" :open-delay="500">
              <el-button class="delete !p-1" type="danger" icon="el-icon-delete" @click="onDeleteBtnClick(row)" />
            </el-tooltip>
          </div>

        </template>
      </vxe-table-column>
      <vxe-table-column title="角色编码" field="code" sortable align="left" min-width="100">
        <template #edit="{ row }">
          <el-input v-if="!row.bid" v-model="row.code" clearable class="edit-table-input" />
          <span v-else>{{ row.code }}</span>
        </template>
      </vxe-table-column>

      <vxe-table-column title="状态" :field="STATUS_FIELD" sortable align="center" min-width="180">
        <template #default="{ row }">
          <tr-switch
            v-model.number="row[STATUS_FIELD]"
            :params="row"
            :http-request="updateRoleStatus"
            :invalid-value="0"
            :active-value="1"
            :inactive-value="2"
            :params-code="STATUS_FIELD"
          />
        </template>
      </vxe-table-column>
      <vxe-table-column title="类型" field="type" sortable align="center" min-width="80">
        <template #default="{ row }">
          {{ renderFieldByDict(row.type,'SYS_ROLE_TYPE') || '-' }}
        </template>
      </vxe-table-column>
      <vxe-table-column title="说明" field="description" sortable align="center" min-width="200">
        <template #edit="{ row }">
          <el-input v-model="row.description" clearable class="edit-table-input" />
        </template>
      </vxe-table-column>
      <!--      <vxe-table-column title="操作" align="left" min-width="160" fixed="right">-->
      <!--        <template #default="{ row }">-->
      <!--          <el-button type="text" size="small" @click="onAddChildBtnClick(row)">新增</el-button>-->
      <!--          &lt;!&ndash;          <el-button type="text" size="small" @click="onAddUserBtnClick(row)">成员管理</el-button>&ndash;&gt;-->
      <!--          <template v-if="row[STATUS_FIELD] === 0">-->
      <!--            <el-divider direction="vertical" />-->
      <!--            <el-button type="text" class="danger-color" size="small" @click="onDeleteBtnClick(row)">删除</el-button>-->
      <!--          </template>-->
      <!--        </template>-->
      <!--      </vxe-table-column>-->
      <template #empty><tr-no-data type="common" /></template>
    </vxe-table>
    <addMember ref="addMemberRef" />
    <AddRoleDialog ref="addRoleRef" @refresh="onRefresh" />
  </div>
</template>

<script setup>
import XEUtils from 'xe-utils'
import router from '@/router'
import TrSwitch from '@@/feature/TrSwitch'
import useExport from '@/hooks/useExport'
import tableConfig from '@/config/table.config'
import { Message, MessageBox } from 'element-ui'
import { flatTreeData, parseString, setUserStore } from '@/utils'
import TrNoData from '@@/feature/TrNoData/index.vue'
import { computed, nextTick, ref,onMounted } from 'vue'
import { STATUS_FIELD } from '@/config/state.config.js'
import FilterPanel from './components/filter-panel.vue'
import regularExpressionConfig from '@/config/regularExpression.config'
import { deleteRole, getRoleTree, updateRoleStatus } from './api/index.js'
import addMember from './components/add-member.vue'
import AddRoleDialog from './components/add-role-dialog.vue'
import { renderFieldByDict } from '@/utils/dict'

const routerQuery = ref(null)

routerQuery.value = router.currentRoute.query
// 表格组件
const TrTableRef = ref(null)
const selectRow = ref(null)
const validRules = {
  name: [
    { required: true, message: '角色名不能为空' }
  ],
  // 角色编码只能是大写字母、下划线，最大长度为50，最小长度为1，不能重复
  code: [
    { required: true, message: '角色编码不能为空' },
    { pattern: regularExpressionConfig.upperCase.pattern, message: regularExpressionConfig.upperCase.message },
    { min: 1, max: 50, message: '角色编码最大长度为50，最小长度为1' }
  ]
}

const treeConfig = { transform: true,parentField: 'parentBid',rowField: 'bid', iconOpen: 'vxe-icon-square-minus-fill', iconClose: 'vxe-icon-square-plus-fill' }
const addRoleRef = ref(null)
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
    const searchProps = ['name', 'code', 'enableFlag', 'description']

    tableData.value = XEUtils.searchTree(cashTableData.value, item => searchProps.some(key => XEUtils.toValueString(item[key]).toLowerCase().includes(filterName)), options)
    // 搜索之后默认展开所有子节点
    nextTick(() => {
      TrTableRef.value.setAllTreeExpand(true)
    })
  } else {
    tableData.value = cashTableData.value
  }
}

// 查询表单form
// eslint-disable-next-line no-unused-vars
const form = ref({})

// 表格数据
const tableData = ref([])
const cashTableData = ref([])

// 表格高度
const height = computed(() => window.innerHeight - 100)

// 查询角色树
const queryRoleTreeData = async (isRefresh) => {
  // 保存展开的节点
  let expandList = []

  if (isRefresh) {
    expandList = TrTableRef.value.getTreeExpandRecords()
  }
  const { data } = await getRoleTree()

  tableData.value = flatTreeData(data,'children')
  const jobNumberList = [...new Set(tableData.value.map(item => item.createdBy))].filter(Boolean)

  setUserStore(jobNumberList)
  cashTableData.value = parseString(tableData.value,[])
  // 默认展开已经展开的节点
  if (isRefresh) {
    nextTick(() => {
      expandList.forEach(item => {
        const _row = tableData.value.find(o => o.bid === item.bid)

        TrTableRef.value.setTreeExpand(_row, true)
      })
    })
  }

}

queryRoleTreeData()

const onFilterChange = (values) => {
  const searchProps = []

  Object.keys(values).forEach(key => {if (values[key] || [values[key] === 0]) {searchProps.push(key)}})
  // 对象的每一个属性都有值
  const objectHasValue = searchProps.length === Object.keys(values).length

  if (objectHasValue) {
    const options = { children: 'children' }

    tableData.value = XEUtils.searchTree(cashTableData.value, item => searchProps.every(key => {
      return String(item[key]).toLowerCase() === String(values[key]).toLowerCase()
    }), options)
    // 搜索之后默认展开所有子节点
    nextTick(() => {
      TrTableRef.value.setAllTreeExpand(true)
    })
  } else {
    tableData.value = cashTableData.value
  }
}

// 新增字典按钮
const onAddBtnClick = async (row) => {
  addRoleRef.value?.show(row,'add')
}
const onEditBtnClick = async (row) => {
  addRoleRef.value?.show(row,'edit')
}
// 添加用户
const addMemberRef = ref(null)
const onUserBtnClick = async (row) => {
  addMemberRef.value.showUserDialog(row)
}
const onSelectChange = ({ row }) => {
  selectRow.value = row
}

// 删除按钮
const onDeleteBtnClick = row => {
  if (!row.bid) return
  MessageBox.confirm('是否确认删除？', '警告', { type: 'warning' }).then(() => {
    deleteRole(row.bid).then(data => {
      if (data.success) {
        Message.success(data.message)

        TrTableRef.value.remove(row)
      }
    })
  })
}

const onCurrentChange = ({ row }) => {
  if (selectRow.value === row) {
    selectRow.value = null
    TrTableRef.value.clearCurrentRow()
  }
  else selectRow.value = row
}
// 精确选中某一角色
const selectedRowByCode = modelCode => {
  const options = { children: 'children' }
  const searchProps = ['code']
  const data = XEUtils.searchTree(cashTableData.value, item => searchProps.some(key => XEUtils.toValueString(item[key]).toLowerCase() === modelCode.toLowerCase()), options)

  if (data.length) {
    tableData.value = data
    nextTick(() => {
      TrTableRef.value.setAllTreeExpand(true)
      TrTableRef.value.setCurrentRow(data[data.length - 1])
    })
  } else {
    Message.warning('未找到该角色')
  }
}



const onRefresh = () => {
  queryRoleTreeData(true)
}

onMounted(() => {
  // 从对象详情页跳转过来
  const { code } = routerQuery.value

  if (!code) return
  // 如果query中有modelCode，说明是从对象详情页跳转过来的,选中该对象
  setTimeout(() => {

    selectedRowByCode(code)
    // 替换url，去掉modelCode
    router.replace({ query: { ...routerQuery.value, code: undefined } })
  },200)
})
// 导出
const { exportFile,exportLoading } = useExport()
const onExportBtnClick = () => {
  exportFile('ROLE')
}

</script>

<style scoped lang="scss">
@import './css/reset.scss';
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
      width:fit-content;
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
  .search-box{
    display: flex;
    align-items: center;
    width: 400px;
    .text{
      padding: 0 2px;
      width: 80px;
      color: var(--primary);
    }
    .search-input ::v-deep .el-input__inner {
      border: none;
      border-bottom: 1px solid var(--primary);
      border-radius: 0;
      padding: 0 10px;
      height: 34px;
      font-size: 14px;
      &:focus{
        border-width: 2px;
      }
    }
  }
}
::v-deep .is--active .vxe-tree--node-btn{
  //transform: rotate(135deg) !important;
  //margin-bottom: 0.15em;
}
//::v-deep .col--edit.col--actived .vxe-tree-cell,::v-deep .col--edit.col--actived .vxe-cell--tree-node{
//  padding-left: 0 !important;
//}
::v-deep .vxe-table--render-default .vxe-tree--btn-wrapper .vxe-tree--node-btn, .vxe-table--render-default .vxe-table--expand-btn{
  width: 1em !important;
  height: 1em !important;
  border-style: none !important;
  transform: rotate(0deg) translate(50%,25%) !important;
}
.role-name-box{
  display: flex;
  align-items: center;
  font-weight: 600;
  .tr-svg-icon{
    margin-right: 3px;
    color: var(--primary);
    font-size: 17px;
  }
}
::v-deep.vxe-table--render-default .vxe-tree-cell{
  padding-left: 1.6em !important;
}
.handler-box{
  position: absolute;
  // 不设置为0是因为会挡住右边框线
  right: 1px;
  top: 0px;
  display: none;
  padding-right: 6px;
  align-items: center;
  content-visibility: hidden;
  z-index: 100;

  .el-button + .el-button {
    margin-left: 6px;
  }
}
::v-deep .row--hover/*,.row--checked*/ {
  .handler-box{
    display: flex;
    content-visibility: visible;
  }
}
</style>
