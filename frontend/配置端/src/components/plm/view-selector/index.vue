<script setup>
import { ref, computed, watch } from 'vue'
import { pageQuery } from '@/views/system-manage/view-manage/api'
import FilterPanel from '@/views/system-manage/view-manage/components/filter-panel.vue'
import TrSwitch from '@@/feature/TrSwitch'
import UserCell from '@@/plm/user/user-cell.vue'
import { mergeApiNeedUserInfo } from '@/utils'
import router from '@/router'
import { STATUS_FIELD } from '@/config/state.config'
import tableConfig from '@/config/table.config'

const emits = defineEmits(['input'])
const modelValue = ref([])
const searchText = ref('')
const loading = ref(false)
const props = defineProps({
  value: {
    type: String,
    default: ''
  },
  multiple: {
    type: Boolean,
    default: false
  },
  dataMap: {
    type: Object,
    default: () => ({})
  },
  effect: {
    type: String,
    default: ''
  }
})
const selectedRows = ref([])

watch(
  () => props.value,
  (value) => {
    if (value) {
      if (!value || !value.length) return []
      if (props.multiple && Array.isArray(value)) {
        modelValue.value = value.map(item => ({ label: props.dataMap[item]?.name,value: item }))
      } else {
        modelValue.value = [{ label: props.dataMap[value]?.name,value: value }]
      }
    } else {
      modelValue.value = []
    }
  },
  { immediate: true,deep: true }
)

const TrTableRef = ref(null)
const tableData = ref([])
const filterForm = ref({})
const showDialog = ref(false)
const totalSize = ref(0)
const params = ref({
  count: true,
  param: { ...filterForm.value },
  current: 1,
  size: 20
})
// 表格高度
const height = computed(() => window.innerHeight - 350)
// 查询条件变更
const onFilterChange = form => {
  filterForm.value = form
  initData()
}
const onOpen = () => {
  initData()
}
const initData = async () => {
  params.value.param = { ...filterForm.value }
  loading.value = true
  const { data } = await mergeApiNeedUserInfo(pageQuery(params.value), ['updatedBy']).finally(() => {
    loading.value = false
  })

  tableData.value = data.data
  totalSize.value = data.total
}
const onInputClick = () => {
  showDialog.value = true
}
const onCurrentChange = (val) => {
  params.value.current = val
  initData()
}
const onsubmit = () => {
  modelValue.value = [{ label: selectedRows.value.name,value: selectedRows.value.bid }]
  emits('input',selectedRows.value.bid)
  emits('change',selectedRows.value)
  showDialog.value = false
}
const onCurrentRowChange = ({ row }) => {
  if (!props.multiple) {
    selectedRows.value = row
  }
}
const onSearchClear = () => {
  searchText.value = ''
  TrTableRef.value.clearFilter()
}
const onSearch = () => {
  onFilterChange({ name: searchText.value })
}
const onJumpToViewBtnClick = bid => {
  const _router = router.resolve(`/view-manage/action/designer/${bid}`)

  window.open(_router.href,'_blank')
}
</script>

<template>
  <div>
    <div v-if="effect==='view'" class="tag-box">
      <el-tag v-for="item in modelValue" :key="item.value" type="info" effect="light">{{ item.label }}</el-tag>
    </div>
    <div v-else class="tag-box border" @click="onInputClick">
      <el-tag v-for="item in modelValue" :key="item.value" type="info" effect="light">{{ item.label }}</el-tag>
    </div>
    <el-dialog
      :visible.sync="showDialog"
      title="视图列表"
      append-to-body
      width="70%"
      custom-class="el-picker-panel"
      @open="onOpen"
    >
      <div class="tools-box">
        <filter-panel class="btn-box" @filter-change="onFilterChange" />
        <el-divider direction="vertical" />
        <div class="search-box">
          <span class="text">快捷查询</span>
          <el-input v-model="searchText" class="search-input" placeholder="请输入视图名称" clearable @clear="onSearchClear" @input="onSearch">
            <template #prefix><tr-svg-icon icon-class="t-search" /></template>
          </el-input>
        </div>
      </div>
      <vxe-table
        ref="TrTableRef"
        :data="tableData"
        :height="height"
        :column-config="{resizable: true}"
        border
        highlight-current-row
        :row-config="{isCurrent:true,height:tableConfig.rowHeight}"
        show-overflow
        :loading="loading"
        @current-change="onCurrentRowChange"
      >
        <vxe-table-column type="seq" width="50" title=" " />
        <vxe-table-column title="视图名称" field="name" sortable align="left" min-width="150">
          <template #default="{row}">
            <el-popconfirm
              :title="`确定要设计视图【${row.name}】吗？`"
              @confirm="onJumpToViewBtnClick(row.bid)"
            >
              <span slot="reference" class="common-btn active-hover font-bold">{{ row.name }}</span>
            </el-popconfirm>
          </template>
        </vxe-table-column>
        <vxe-table-column title="视图类型" field="type" sortable align="left" min-width="120">
          <template #default="{row}">
            <dict-label dict-code="VIEW_TYPE" :value="row.type" />
          </template>
        </vxe-table-column>
        <vxe-table-column title="状态" field="enableFlag" sortable align="center" min-width="180">
          <template #default="{ row }">
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
        <vxe-table-column title="更新人" field="updatedBy" sortable align="center" min-width="130">
          <template #default="{ row }"><user-cell v-if="row.updatedBy" :job-number="row.updatedBy" :avatar-size="22" /></template>
        </vxe-table-column>
        <vxe-table-column title="更新时间" field="updatedTime" sortable align="center" min-width="120" />
      </vxe-table>
      <el-row class="flex justify-center margin-top">
        <el-pagination :current-page="params.current" :page-size="params.size" :total="totalSize" layout="total, prev, pager, next, jumper" @current-change="onCurrentChange" />
      </el-row>
      <el-row class="flex justify-end">
        <el-button type="info" @click="showDialog=false">取消</el-button>
        <el-button type="primary" @click="onsubmit">确定</el-button>
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
</style>
