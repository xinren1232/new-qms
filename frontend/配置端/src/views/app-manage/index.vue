<template>
  <div>
    <div class="tools-box">
      <div class="btn-box" @click="onAddBtnClick"><tr-svg-icon icon-class="add-list" /><span class="text">新增</span></div>
      <el-divider direction="vertical" />
      <!--      <div class="btn-box">-->
      <!--        <tr-svg-icon icon-class="t-import" />-->
      <!--        <span class="text">导入</span></div>-->
      <div class="btn-box">
        <tr-svg-icon icon-class="t-export" />
        <span class="text">导出</span></div>
      <el-divider direction="vertical" />
      <filter-panel class="btn-box" @filter-change="onFilterChange" />
      <el-divider direction="vertical" />
      <!--      <div class="btn-box" @click="TrTableRef.setAllTreeExpand(true)">-->
      <!--        <tr-svg-icon icon-class="t-expand-all" />-->
      <!--        <span class="text">展开</span></div>-->
      <!--      <div class="btn-box" @click="TrTableRef.clearTreeExpand()">-->
      <!--        <tr-svg-icon icon-class="t-close-all" />-->
      <!--        <span class="text">收起</span></div>-->
      <!--      <el-divider direction="vertical" />-->
      <div class="search-box">
        <span class="text">快捷查询</span>
        <el-input v-model="searchText" class="search-input" placeholder="请输入应用名称" clearable @clear="onSearchClear" @input="onSearch">
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
      highlight-current-row
      :edit-config="{trigger:'click',mode:'cell',showIcon:false,activeMethod:activeMethod}"
      :radio-config="{ highlight: true}"
      :column-config="{resizable: true}"
      :edit-rules="validRules"
      keep-source
      show-overflow
      @edit-closed="onEditClosed"
      @radio-change="onSelectChange"
      @cell-click="onCurrentChange"
    >
      <vxe-table-column type="seq" title="No." width="60" />
      <vxe-table-column title="应用名称" field="name" sortable min-width="200" :edit-render="{}">
        <template #edit="{ row }">
          <el-input v-model="row.name" clearable class="edit-table-input" />
        </template>
        <template #default="{ row }">
          <span class="role-name-box">
            <!--            <template v-if="row.children && row.children.length">-->
            <!--              <tr-svg-icon v-if="TrTableRef.isTreeExpandByRow(row)" icon-class="t-folder-minus" @click="TrTableRef.toggleTreeExpandByRow(row)" />-->
            <!--              <tr-svg-icon v-else icon-class="t-folder-plus" @click="TrTableRef.toggleTreeExpandByRow(row)" />-->
            <!--            </template>-->
            <span>{{ row.name }}</span>
          </span>
        </template>
      </vxe-table-column>
      <vxe-table-column title="应用编码" field="code" sortable align="center" min-width="180" :edit-render="{}">
        <template #edit="{ row }">
          <el-input v-if="!row.bid" v-model="row.code" clearable class="edit-table-input" />
          <span v-else>{{ row.code }}</span>
        </template>
      </vxe-table-column>
      <vxe-table-column title="应用类型" field="typeCode" sortable align="center" min-width="100" :edit-render="{}">
        <template #default="{row}">
          <dict-label :value="row.typeCode" dict-code="APP_TYPE" />
        </template>
        <template #edit="{ row }">
          <tr-dict v-model="row.typeCode" type="APP_TYPE" :label-field="lang" value-field="keyCode" @change="onAppTypeChange(row)" />
        </template>
      </vxe-table-column>
      <vxe-table-column
        title="类型值"
        field="typeValue"
        sortable
        align="center"
        min-width="180"
        :edit-render="{}"
        :params="{multiple:false}"
      >
        <template #edit="{ row }">
          <TrTreeSelect
            v-if="row.typeCode==='object'"
            v-model="row.typeValue"
            :row="row"
            title="对象选择"
            :source-data="objectTreeData"
            effect="edit"
            :column="{}"
            :columns="[{ field: 'modelCode','title': '对象编码' },{ field: 'enableFlag','title': '启用' }]"
            value-field="modelCode"
            key-field="modelCode"
            :multiple="false"
          />
          <el-input v-else-if="row.typeCode==='url'" v-model="row.typeValue" clearable />
          <ViewSelector
            v-else-if="row.typeCode==='view'"
            v-model="row.typeValue"
            :data-map="viewMap"
            effect="edit"
            @change="onViewSelectChange"
          />
        </template>
        <template #default="{ row }">
          <TrTreeSelect
            v-if="row.typeCode==='object'"
            v-model="row.typeValue"
            :row="row"
            title="对象选择"
            :source-data="objectTreeData"
            effect="view"
            :column="{}"
            :columns="[{ field: 'modelCode','title': '对象编码' },{ field: 'enableFlag','title': '启用' }]"
            value-field="modelCode"
            key-field="modelCode"
            :multiple="false"
          />
          <el-link v-else-if="row.typeCode==='url'" type="primary" @click.stop="onLinkClick(row.typeValue)">{{ row.typeValue }}</el-link>
          <ViewSelector v-else-if="row.typeCode==='view'&&viewMapLoaded" v-model="row.typeValue" :data-map="viewMap" effect="view" />
        </template>
      </vxe-table-column>
      <vxe-table-column
        title="应用图标"
        field="icon"
        sortable
        align="center"
        min-width="100"
        :edit-render="{name:'singlePictureEditor'}"
        :params="pictureParams"
      />
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
      <vxe-table-column title="操作" align="left" min-width="60">
        <template #default="{ row }" style="display: flex">
          <el-button type="text" size="small" @click="onAddChildBtnClick(row)">新增</el-button>
          <el-divider direction="vertical" />
          <!--          <el-button type="text" size="small" @click="onAddChildBtnClick(row)">角色</el-button>-->
          <!--          <el-divider v-if="row[STATUS_FIELD] !==0" direction="vertical" />-->
          <el-button type="text" class="danger-color" size="small" @click="onDeleteBtnClick(row)">删除</el-button>
        </template>
      </vxe-table-column>
      <template #empty>
        <tr-no-data type="list" />
      </template>
    </vxe-table>
  </div>
</template>

<script setup>
import { computed, nextTick, ref } from 'vue'
import { Message, MessageBox } from 'element-ui'
import XEUtils from 'xe-utils'
import { STATUS_FIELD } from '@/config/state.config.js'
import FilterPanel from './components/filter-panel.vue'
import TrSwitch from '@@/feature/TrSwitch'
import TrDict from '@@/bussiness/TrDict'
import { flatTreeData, parseString } from '@/utils'
import { addOrEditRole, deleteRole, getRoleTree, updateRoleStatus } from './api/index.js'
import { getViewDetail } from '@/views/system-manage/view-manage/api'
import { getLanguage } from '@/i18n'
import TrTreeSelect from '@@/plm/tree-selector'
import '@@/feature/TrMultiTable/renderer/single-picture-editor'
import ViewSelector from '@@/plm/view-selector'
import { getToken,getRToken,getUserJobNumber } from '@/app/cookie'
import TrNoData from '@@/feature/TrNoData/index.vue'
import { useObjectTreeData } from '@/hooks/useObjectList'

// 表格组件
const TrTableRef = ref(null)
const selectRow = ref(null)
const pictureParams = ref({
  uploadURL: process.env.VUE_APP_FILE_URL,
  disabled: false,
  fileTypes: ['jpg', 'jpeg', 'png', 'gif', 'bmp'],
  fileMaxSize: 2,
  multipleSelect: false,
  token: getToken(),
  rtoken: getRToken(),
  empno: getUserJobNumber()

})
const validRules = {
  name: [
    { required: true, message: '应用名不能为空' }
  ],
  // 角色编码只能是字母和数字、下划线，且不能以数字开头,最大长度为50，最小长度为1，不能重复
  code: [
    { required: true, message: '应用编码不能为空' }
  ],
  typeCode: [
    { required: true, message: '应用类型不能为空' }
  ],
  typeValue: [
    { required: true, message: '类型值不能为空' }
  ],
  icon: [
    { required: true, message: '应用图标不能为空' }
  ]
}
const viewMap = ref({})
const viewMapLoaded = ref(false)

const objectTreeData = ref({})
const getObjectTreeData = async () => {
  objectTreeData.value = await useObjectTreeData()
}

getObjectTreeData()

const lang = computed(() => getLanguage().split('-')[0])

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
    const searchProps = ['name', 'code', 'enableFlag']

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

const activeMethod = ({ row, column }) => {
  return !(row.bid && column.field === 'typeCode')
}

// 查询角色树
const queryRoleTreeData = async (isRefresh) => {
  // 保存展开的节点
  let expandList = []

  if (isRefresh) {
    expandList = TrTableRef.value.getTreeExpandRecords()
  }
  const { data } = await getRoleTree()

  tableData.value = flatTreeData(data,'children')
  // 收集所有的viewBid,查询view基本信息
  const viewBidList = []

  tableData.value.forEach(item => {
    if (item.typeCode === 'view') {viewBidList.push(item.typeValue)}
  })
  getViewInfo(viewBidList)
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
const getViewInfo = list => {
  list.forEach(item => {
    Promise.all([getViewDetail(item)]).then(res => {
      viewMap.value[item] = res[0].data
    }).finally(() => {
      setTimeout(() => {viewMapLoaded.value = true},500)
    })
  })

}

queryRoleTreeData()

const onFilterChange = (values) => {
  const searchProps = []

  Object.keys(values).forEach(key => {if (values[key] || values[key] === 0) {searchProps.push(key)}})
  // 对象的每一个属性都有值
  const objectHasValue = searchProps.length === Object.keys(values).length

  if (objectHasValue) {
    const options = { children: 'children' }

    tableData.value = XEUtils.searchTree(cashTableData.value, item => searchProps.every(key => {
      return String(item[key]).toLowerCase().includes(String(values[key]).toLowerCase())
    }), options)
    // 搜索之后默认展开所有子节点
    // nextTick(() => {
    //   TrTableRef.value.setAllTreeExpand(true)
    // })
  } else {
    tableData.value = cashTableData.value
  }
}

// 新增字典按钮
const onAddBtnClick = async () => {
  const parentBid = selectRow.value ? selectRow.value.bid : '0'
  const { row } = await TrTableRef.value.insert({ parentBid,icon: '', name: '', code: '', description: '', [STATUS_FIELD]: 0 })

  TrTableRef.value.setEditCell(row, 'name')
  TrTableRef.value.setTreeExpand(row, true)
}
// 失去焦点保存
const onEditClosed = async ({ row }) => {
  const changeData = TrTableRef.value.getUpdateRecords()

  if (!changeData.length && row.bid) {
    return
  }

  if (row.code === '' && row.name === '') {
    await TrTableRef.value.remove(row)

    return
  }
  const errMap = await TrTableRef.value.validate(row).catch(errMap => errMap)

  if (errMap) {
    return
  }
  const { success,message } = await addOrEditRole(row)


  if (success) {
    Message.success(message)
    await queryRoleTreeData(true)

  }
}
const onSelectChange = ({ row }) => {
  selectRow.value = row
}

// const onClearRadioRow = () => {
//   selectRow.value = null
//   TrTableRef.value.clearRadioRow()
// }

// 新增子节点
const onAddChildBtnClick = async (row) => {
  const parentBid = row.bid
  const { row: newRow } = await TrTableRef.value.insert({ parentBid, name: '', code: '','typeCode': '',typeValue: '', description: '', [STATUS_FIELD]: 0 })

  await TrTableRef.value.setTreeExpand(row, true)
  await TrTableRef.value.setEditCell(newRow,'name')
}

// 删除按钮
const onDeleteBtnClick = row => {
  if (!row.bid) {
    TrTableRef.value.remove(row)

    return
  }
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
  selectRow.value = row
}
const onAppTypeChange = row => {
  row.typeValue = ''
  TrTableRef.value.setEditCell(row, 'typeValue')
}
const onViewSelectChange = value => {
  viewMap.value[value.bid] = value
}
const onLinkClick = link => {
  window.open(link,'_blank')
}
</script>
<style scoped lang="scss">
 @import './css/reset.scss';
 .tools-box {
   padding: 0 10px 10px;
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
       width: fit-content;
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

   .search-box {
     display: flex;
     align-items: center;
     width: 400px;

     .text {
       color: var(--primary);
       padding: 0 2px;
       width: 80px;
     }

     .search-input ::v-deep .el-input__inner {
       border: none;
       border-bottom: 1px solid var(--primary);
       border-radius: 0;
       padding: 0 10px;
       height: 34px;
       font-size: 14px;

       &:focus {
         border-width: 2px;
       }
     }
   }
 }

 .bussiness {
   margin-right: 3px;
 }

 ::v-deep .is--active .vxe-tree--node-btn {
   transform: rotate(135deg) !important;
   margin-bottom: 0.15em;
 }

 ::v-deep .col--edit.col--actived .vxe-tree-cell,
 ::v-deep .col--edit.col--actived .vxe-cell--tree-node {
   padding-left: 0 !important;
 }

 .role-name-box {
   display: flex;
   align-items: center;
   font-weight: 600;

   .tr-svg-icon {
     margin-right: 3px;
     color: var(--primary);
     font-size: 17px;
   }
 }
 </style>
