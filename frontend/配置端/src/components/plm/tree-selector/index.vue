<script setup>
import { ref, computed, nextTick, watch } from 'vue'
import { STATUS_FIELD } from '@/config/state.config'
import { deepClone } from '@/utils'
import XEUtils from 'xe-utils'
import tableConfig from '@/config/table.config'
import TrSwitch from '@@/feature/TrSwitch'
// import UserCell from '@@/plm/user/user-cell.vue'
import route from '@/router'

const emits = defineEmits(['input','confirm'])
const props = defineProps({
  type: {
    type: String,
    default: '' // 'tableCell','formItem'
  },
  title: {
    type: String,
    default: 'Tree选择器'
  },
  value: {
    type: [Array,String],
    default: ''
  },
  multiple: {
    type: Boolean,
    default: false
  },
  effect: {
    type: String,
    default: 'view'
  },
  sourceData: {
    type: Object,
    default: () => ({})
  },
  keyField: {
    type: String,
    default: 'bid'
  },
  valueField: {
    type: String,
    default: 'bid'
  },
  childrenField: {
    type: String,
    default: 'children'
  },
  fullSearchField: {
    type: [Array, String],
    default: () => ['name']
  },
  columns: {
    type: Array,
    default: () => [{ field: 'name',title: '名称',width: 200 }]
  },
  parentField: {
    type: String,
    default: 'parentBid'
  }
})
const height = computed(() => {
  return document.documentElement.clientHeight - 250
})
const visible = ref(false)
const tableData = ref([])

watch(
  () => props.sourceData,
  () => {
    tableData.value = props.sourceData?.list || []
  },
  { immediate: true }
)
const dataMap = computed(() => {
  return props.sourceData?.map || {}
})
const cashTableData = deepClone(tableData.value)
const TrTableRef = ref(null)
const searchText = ref('')
const selectedRows = ref([])

const modelValue = computed(
  {
    get({ value }) {
      if (!value || !value.length) return []
      if (props.multiple && Array.isArray(value)) {
        return value.map(item => ({ label: dataMap.value[item]?.name,value: item }))
      } else {
        return [{ label: dataMap.value[value]?.name,value: value }]
      }

    },
    set(value) {
      emits('input', value)
    }
  }
)


const onInputClick = () => {
  visible.value = true
}
const onSearchClear = () => {
  searchText.value = ''
  TrTableRef.value.clearFilter()
}
const onSearch = () => {
  const filterName = XEUtils.toValueString(searchText.value).trim().toLowerCase()

  if (filterName) {
    const options = { children: props.childrenField }
    const searchProps = props.fullSearchField

    tableData.value = XEUtils.searchTree(cashTableData, item => searchProps.some(key => XEUtils.toValueString(item[key]).toLowerCase().includes(filterName)), options)
    // 搜索之后默认展开所有子节点
    nextTick(() => {
      TrTableRef.value.setAllTreeExpand(true)
    })
  } else {
    tableData.value = cashTableData
  }
  selectDefaultNode()
}

const onSubmit = () => {
  if (selectedRows.value.length)modelValue.value = selectedRows.value
  visible.value = false
  const _selectedRows = TrTableRef.value.getCheckboxRecords()

  emits('confirm', _selectedRows)
}
const onCancel = () => {
  visible.value = false
}
const onCheckboxChange = ({ records }) => {
  if (!records.length) return
  if (props.multiple) {
    selectedRows.value = records.map(item => item?.[props.valueField])
  } else {
    selectedRows.value = records?.[0]?.[props.valueField]
  }
}

const onOpen = () => {
  selectDefaultNode()
  nextTick(() => {
    TrTableRef.value.recalculate()
  })
}
// 选中默认的节点
const selectDefaultNode = () => {
  if (!tableData.value.length) return

  modelValue.value.forEach(item => {
    const node = tableData.value.find(node => node[props.valueField] === item.value)
    let parentNodeList = []

    // 递归找到父节点
    const findParentNode = (node) => {
      if (node?.[props.parentField]) {
        const parentNode = tableData.value.find(item => item[props.keyField] === node[props.parentField])

        if (parentNode) {
          parentNodeList.push(parentNode)
          findParentNode(parentNode)
        }
      }
    }

    findParentNode(node)
    nextTick(() => {
      parentNodeList.forEach(item => {
        TrTableRef.value.setTreeExpand(item, true)
      })
    })

    if (node) {
      nextTick(() => {
        if (props.multiple) {
          TrTableRef.value.setCheckboxRow(node, true)
        } else {
          TrTableRef.value.setCurrentRow(node)
        }
      })
    }
  })
}

const onCurrentChange = ({ row }) => {
  if (!props.multiple) {
    selectedRows.value = row?.[props.valueField]
  }
}
const onJumpToManageBtnClick = (code) => {
  const _route = route.resolve(`/system-manage/role-manage?code=${code}`)

  window.open(_route.href, '_blank')
}
const onClose = () => {
  searchText.value = ''
  selectedRows.value = []
  TrTableRef.value.clearCheckboxRow()
}
</script>

<template>
  <div>
    <div v-if="effect==='view'" class="tag-box">
      <el-tag v-for="(item,index) in modelValue" :key="index" type="info" effect="light">{{ item.label }}</el-tag>
    </div>
    <div v-else :class="['tag-box','border',type==='formItem'?'formItem':'']" @click="onInputClick">
      <el-tag v-for="(item,index) in modelValue" :key="index" type="info" effect="light">{{ item.label }}</el-tag>
    </div>
    <el-dialog
      :visible.sync="visible"
      :title="title"
      width="64%"
      custom-class="el-picker-panel"
      append-to-body
      @open="onOpen"
      @close="onClose"
    >
      <div class="tools-box">
        <div class="btn-box" @click="TrTableRef.setAllTreeExpand(true)"><tr-svg-icon icon-class="t-expand-all" /><span class="text">展开</span></div>
        <div class="btn-box" @click="TrTableRef.clearTreeExpand()"><tr-svg-icon icon-class="t-close-all" /><span class="text">收起</span></div>
        <el-divider direction="vertical" />
        <div class="search-box">
          <span class="text">快捷查询</span>
          <el-input v-model="searchText" class="search-input" placeholder="请输入对象名称或编码" clearable @clear="onSearchClear" @input="onSearch">
            <template #prefix><tr-svg-icon icon-class="t-search" /></template>
          </el-input>
        </div>
      </div>
      <vxe-table
        ref="TrTableRef"
        :data="tableData"
        border
        :height="height"
        :seq-column="false"
        highlight-current-row
        :row-config="{isCurrent:true,height:tableConfig.rowHeight}"
        :edit-config="{trigger:'click',mode:'cell',showIcon:false}"
        :column-config="{resizable: true}"
        :tree-config="{transform: true, rowField:keyField ,parentField:parentField}"
        keep-source
        show-overflow
        @checkbox-change="onCheckboxChange"
        @current-change="onCurrentChange"
      >
        <vxe-column v-if="multiple" type="checkbox" width="50" />
        <vxe-table-column title="名称" field="name" sortable width="260" tree-node>
          <template #default="{ row }">
            <el-popconfirm
              :title="`确定要跳转到管理页面吗`"
              popper-class="el-picker-panel"
              @confirm="onJumpToManageBtnClick(row[valueField])"
            >
              <span slot="reference" class="common-btn active-hover font-bold">{{ row.name }}</span>
            </el-popconfirm>
          </template>
        </vxe-table-column>
        <vxe-table-column
          v-for="col in columns"
          :key="col.field"
          :title="col.title"
          :field="col.field"
          sortable
          align="center"
          :min-width="col.width || 100"
        >
          <template v-if="col.field===STATUS_FIELD" #default="{ row }">
            <tr-switch
              v-model.number="row[STATUS_FIELD]"
              :params="row"
              disabled
              :invalid-value="0"
              :active-value="1"
              :inactive-value="2"
              :params-code="STATUS_FIELD"
            />
          </template>
        </vxe-table-column>
        <!--        <vxe-table-column title="创建人" field="createdBy" sortable align="center" min-width="100">-->
        <!--          <template #default="{ row }">-->
        <!--            <user-cell :job-number="row.createdBy" :avatar-size="22" />-->
        <!--          </template>-->
        <!--        </vxe-table-column>-->
      </vxe-table>
      <el-row class="flex justify-end m-y-10px">
        <el-button type="info" @click="onCancel">取消</el-button>
        <el-button type="primary" @click="onSubmit">确定</el-button>
      </el-row>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
@import '../../../views/system-manage/role-manage/css/reset.scss';
.tag-box{
  width: 100%;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  overflow: hidden;
  .el-tag{
    height: 28px;
    line-height: 28px;
    min-width: inherit !important;
    margin-right: 10px;
  }
  &.border{
    border: 2px solid var(--primary);
    cursor: pointer;
  }
}
.bussiness {
  margin-right: 3px;
}
::v-deep .is--active .vxe-tree--node-btn{
  transform: rotate(135deg) !important;
  margin-bottom: 0.15em;
}
::v-deep .col--edit.col--actived .vxe-tree-cell{
  padding: 0 !important;
}
::v-deep.vxe-table--render-default .vxe-tree-cell{
  padding-left: 1.5em !important;
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
.tools-box{
  padding: 0 0 10px;
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
      color: var(--primary);
      padding: 0 2px;
      width: 80px;
    }
    .search-input ::v-deep .el-input__inner {
      padding: 0 10px;
      height: 34px;
      font-size: 14px;
      color: var(--primary);
      border: none;
      border-bottom: 1px solid var(--primary);
      border-radius: 0;
      &:focus {
        border-bottom: 2px solid var(--primary);
      }
    }
  }
}
::v-deep .row--level-0 .vxe-cell--tree-node{
  padding-left: 10px !important;
}
::v-deep .el-dialog__header{
  padding: 10px 20px 0 !important;
}
::v-deep .el-dialog__body{
  padding-bottom:  var(--margin) !important;
}
::v-deep .vxe-body--column{
  height: 42px !important;
}
.formItem{
  border-radius: var(--radius) !important;
  border: 1px solid #DCDFE6 !important;
  line-height: 34px !important;
  height: 34px !important;
  padding: 0 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: inherit;
  &:hover{
    border-color: var(--primary) !important;
  }
}
</style>
