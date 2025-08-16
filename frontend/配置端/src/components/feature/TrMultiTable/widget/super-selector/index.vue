<script setup>
import { ref, watch, computed } from 'vue'
import UserSelect from '../../components/user-select'
import { setUserStore } from '../../utils'
import request from '../../request'
import store from '@/store'

const emits = defineEmits(['input'])

const props = defineProps({
  row: {
    type: Object,
    default: () => ({})
  },
  column: {
    type: Object,
    default: () => ({})
  },
  value: {
    type: [String,Array],
    default: null
  }
})


const pagination = ref({
  total: 0,
  pageSize: 10,
  currentPage: 1
})
const configParams = ref(props.column.params)

const { dictMap,superSelectMap,multiple,disabled,apiDataPath,apiHeaders,apiTableHeader,apiLabel,apiMethod,apiParams,apiTableSearch,apiTableTitle,apiUrl,apiValue } = configParams.value || {}

const multipleSelection = ref([])
const dialogVisible = ref(false)
const searchForm = ref({})

const fieldModel = computed(
  {
    get({ value }) {
      if (!value || !value.length) return []
      if (multiple && Array.isArray(value)) {
        return value.map(item => superSelectMap[item])
      } else {
        return [{ ...superSelectMap[value] }]
      }
    },
    set(value) {
      emits('input', value)
    }
  }
)

watch(fieldModel,(value) => {
  multipleSelection.value = value
}, {
  immediate: true
})
const tableData = ref([])
const loading = ref(false)
const tableHeight = computed(() => {
  return document.body.offsetHeight - 380
})
const timeout = ref(null)
const xTable = ref(null)

watch(
  () => searchForm.value,
  () => {
    clearTimeout(timeout.value)
    timeout.value = setTimeout(() => {
      initData()
    }, 500)
  },
  {
    deep: true
  }
)

const calcSearchItemOptions = item => {

  const dictItem = dictMap?.[item.dict]

  return dictItem ? dictItem.map((dict) => ({ label: dict.zh, value: dict.keyCode })) : []
}
const renderDictItem = (item,row) => {
  let result = row[item.value]

  if (item.dict && item.dict !== '__user__') {
    const dictItem = dictMap?.[item.dict]

    if (dictItem) {
      const dict = dictItem.find((dict) => dict.keyCode === row[item.value])

      if (dict) {result = dict.zh}
    }
  }

  return result
}
const renderUserItem = (item,row) => {
  const userMap = store.getters.userInfoMap
  let result = row[item.value]

  if (item.dict && item.dict === '__user__') {
    const user = userMap[row[item.value]]

    if (user) {result = user.name}
  }

  return result
}
const initData = () => {
  const headers = {}
  const params = {}

  if (!apiUrl) return
  if (apiParams && apiParams.length) {
    apiParams.forEach((item) => {
      params[item.key] = item.value
    })
  }


  if (apiHeaders) {
    apiHeaders?.forEach((item) => {
      headers[item.key] = item.value
    })
  }

  loading.value = true
  request({
    method: apiMethod,
    url: apiUrl,
    data: {
      param: { ...params,...searchForm.value },
      current: pagination.value.currentPage,
      size: pagination.value.pageSize
    },
    headers: headers
  }).then((res) => {
    let _data = []

    // 数据路径解析
    if (apiDataPath) {
      const dataPathArr = apiDataPath.split('.')
      let data = res.data

      if (!data) return

      pagination.value.total = data?.total || 0

      dataPathArr.forEach(path => {data = data[path]})
      _data = data || []
    } else {
      _data = res?.data?.data || []
    }
    _data = res?.data?.data || []
    calcJobNumberToUserInfo(_data)
  }).finally(() => {
    loading.value = false
  })
}

const calcJobNumberToUserInfo = (data) => {
  // 手机工号数据
  const jobNumberList = []
  const userFieldList = []

  apiTableHeader?.forEach((item) => {
    if (item.dict === '__user__') {
      userFieldList.push(item.value)
    }
  })
  data.forEach((item) => {
    userFieldList.forEach((field) => {
      jobNumberList.push(item[field])
    })
  })
  setUserStore([...new Set(jobNumberList)])
  tableData.value = data
}
const handleClick = () => {
  if (disabled.value) return
  dialogVisible.value = true
}
// 分页
const handleCurrentPageChange = (val) => {
  pagination.value.currentPage = val
  initData()
}
const onSizeChange = (val) => {
  pagination.value.pageSize = val
  initData()
}
const handleConfirm = () => {
  if (multiple) {
    const res = []

    multipleSelection.value.forEach((item) => {res.push(item[apiValue])})
    fieldModel.value = res
  } else {
    fieldModel.value = xTable.value.getCurrentRecord()[apiValue]
  }
  dialogVisible.value = false
}
const handleSelectionChange = () => {
  multipleSelection.value = xTable.value.getCheckboxRecords().concat(xTable.value.getCheckboxReserveRecords(true))
}
const handleCurrentChange = () => {
  if (configParams.value.multiple) return
  multipleSelection.value = [xTable.value.getCurrentRecord()]
}

const onDialogOpen = () => {
  initData()
}
</script>

<template>
  <div>
    <div class="super-view-box tag-box border" @click="handleClick">
      <el-tag v-for="(item,index) in multipleSelection" :key="index" type="info" effect="light">{{ item[apiLabel] }}</el-tag>
    </div>
    <el-dialog
      :title="apiTableTitle"
      :visible.sync="dialogVisible"
      width="76%"
      :close-on-click-modal="false"
      append-to-body
      custom-class="el-picker-panel"
      @open="onDialogOpen"
    >
      <el-form
        v-if="apiTableSearch.length"
        label-position="right"
        :model="searchForm"
        label-width="100px"
        size="mini"
        :inline="true"
      >
        <el-form-item
          v-for="(item,index) in apiTableSearch"
          :key="index"
          :label="item.label"
        >
          <el-select v-if="item.dict && item.dict !=='__user__'" v-model="searchForm[item.value]" style="width: 100%" clearable filterable>
            <el-option v-for="(option, i) in calcSearchItemOptions(item)" :key="i" :label="option.label" :value="option.value" />
          </el-select>
          <user-select v-else-if="item.dict && item.dict ==='__user__'" v-model="searchForm[item.value]" />
          <el-input v-else v-model="searchForm[item.value]" style="width: 100%" clearable />
        </el-form-item>
      </el-form>
      <div v-if="configParams.multiple" class="selected-box">
        <div class="left">已选项：</div>
        <div class="right">
          <el-tag v-for="(item, index) in multipleSelection" :key="index" effect="dark" :disable-transitions="false">{{ item[apiLabel] }}</el-tag>
        </div>

      </div>
      <div :style="{height:tableHeight+'px'}">
        <vxe-table
          ref="xTable"
          v-loading="loading"
          :data="tableData"
          style="width: 100%"
          border
          :height="tableHeight"
          highlight-current-row
          :row-config="{isCurrent:true,useKey:true,keyField : 'bid'}"
          :checkbox-config="{reserve:true}"
          resizable
          @current-change="handleCurrentChange"
          @checkbox-change="handleSelectionChange"
          @checkbox-all="handleSelectionChange"
        >
          <vxe-table-column v-if="configParams.multiple" type="checkbox" width="45" />
          <vxe-table-column type="seq" width="50" align="center" title=" " />
          <vxe-table-column
            v-for="(item, index) in apiTableHeader"
            :key="index"
            :field="item.value"
            :title="item.label"
          >
            <template slot-scope="scope">
              <span v-if="item.dict && item.dict !== '__user__'">{{ renderDictItem(item,scope.row) }}</span>
              <span v-else-if="item.dict && item.dict === '__user__'">{{ renderUserItem(item,scope.row) }}</span>
              <span v-else>{{ scope.row[item.value] }}</span>
            </template>
          </vxe-table-column>
        </vxe-table>
      </div>
      <div style="display: flex;justify-content: center">
        <el-pagination
          style="margin-top: 20px"
          layout="prev, pager, next, sizes"
          :total="pagination.total"
          :page-size="pagination.pageSize"
          :current-page="pagination.currentPage"
          @current-change="handleCurrentPageChange"
          @size-change="onSizeChange"
        />
      </div>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleConfirm">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.tag-box,.selected-box{
  width: 100%;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  overflow: hidden;
  .el-tag{
    height: 26px;
    line-height: 26px;
    min-width: inherit !important;
    margin-right: 10px;
  }
}
.border{
  border: 2px solid var(--primary);
  cursor: pointer;
}
::v-deep .current-row{
  background-color: #d1e8ff;
}
::v-deep .el-table__body tr.current-row>td.el-table__cell{
  background-color: #d1e8ff;
}
::v-deep .el-dialog__body{
  padding: 0 30px;
}
::v-deep .el-dialog__body{
  padding-bottom:  0;
  padding-top: 0;
}
::v-deep .el-dialog__header {
  padding: 20px 20px 20px;
}
::v-deep .el-dialog__footer{
  padding-top: 0;
}
::v-deep .vxe-body--column{
  height: 42px !important;
}
::v-deep  .vxe-table--render-default .vxe-body--column:not(.col--ellipsis), .vxe-table--render-default .vxe-footer--column:not(.col--ellipsis), .vxe-table--render-default {
  padding: 6px 0 !important;
}
::v-deep .vxe-header--column{
  padding: 7px 0 !important;
  background-color: #FFFFFF;
}
.super-view-box{
  width: 100%;
  height: 38px;
  border-radius: 2px;
  overflow: hidden;
  padding: 0 10px;
  .el-tag{
    margin-right: 3px;
  }
  &.disabled{
    cursor: not-allowed;
    background-color:#F5F7FA ;
  }
}
.selected-box{
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  min-height: 30px;
  .left{
    width: 60px;
    font-size: 12px;
  }
  .right{
    display: flex;
    flex-wrap: wrap;
    align-items: center;
  }
  .el-tag{
    margin-right: 6px;
    margin-bottom: 4px;
  }
}
</style>
