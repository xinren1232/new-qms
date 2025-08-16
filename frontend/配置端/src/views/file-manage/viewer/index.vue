<script setup>

import { Message, MessageBox } from 'element-ui'
import TrTable from '@@/feature/TrTableX/src/Table.vue'
import UserCell from '../components/user.vue'
import { mergeApiNeedUserInfo } from '@/utils/index.js'
import { getFileViewerList, updateFileViewerStatus, deleteFileViewer } from '@/api/file-manage.js'
import TrSwitch from '@@/feature/TrSwitch'

const TABLE_HEIGHT = window.innerHeight - 110

const router = useRouter()

const tableRef = shallowRef()
const tableData = shallowRef([])
const filterForm = ref({})

// 计算条件数量
const setConditionsNumbers = computed(() => {
  return Object.values(filterForm.value).filter(Boolean).length
})

function onResetFilterForm(reassign = false) {
  if (reassign) {
    filterForm.value = {}
  }
  // 关闭弹窗
  document.body.click()
}

function onRefreshTableData() {
  tableRef.value.query()
}

// 获取表格数据
async function getTableData(sizeConfig = { current: 1, size: 20 }) {

  return mergeApiNeedUserInfo(
    getFileViewerList({
      count: true,
      ...sizeConfig,
      param: filterForm.value
    }),

    [ 'updatedBy' ]
  )
}

// 切换启用状态
const updateState = (params) => {
  const _params = {
    bid: params.bid,
    enableFlag: params.enableFlag
  }

  return updateFileViewerStatus(_params)
}

// 点击新增或编辑按钮
function onAddOrEditClick(row) {
  router.push(`/file-manage/viewer/${row.bid || 'add'}`)
}

// 删除数据以后重新请求数据
async function onDeleteData(row) {
  await MessageBox.confirm(`确定删除【${row.name}】吗?`, '提示', { type: 'warning' })

  deleteFileViewer(row.bid)
    .then(({ success }) => {
      if (success) {
        onRefreshTableData()
        Message.success('删除成功')

        return
      }

      Message.error('删除失败, 请稍后重试')
    })
}
</script>

<template>
  <div class="">
    <div class="tools-box">
      <div class="btn-box" @click="onAddOrEditClick">
        <tr-svg-icon icon-class="add-list" />
        <span class="text">{{ $t('common.add') }}</span>
      </div>

      <el-divider direction="vertical" />

      <el-popover @hide="onRefreshTableData()">
        <template #reference>
          <div slot="reference" class="btn-box">
            <tr-svg-icon icon-class="t-filter" />
            <span class="text"><span v-if="setConditionsNumbers>0" class="filter-rule-number">{{ setConditionsNumbers }}</span>{{ $t('common.filter') }}</span>
          </div>
        </template>

        <div class="p-2">
          <el-form label-position="right" label-width="100px">
            <el-form-item label="查看器名称">
              <el-input v-model="filterForm.name" clearable />
            </el-form-item>
          </el-form>

          <div class="text-right mt-4">
            <el-button @click="onResetFilterForm(true)">重置</el-button>
            <el-button type="primary" @click="onResetFilterForm()">查询</el-button>
          </div>
        </div>
      </el-popover>
    </div>

    <tr-table
      ref="tableRef"
      v-model="tableData"
      :http-request="getTableData"
      :height="TABLE_HEIGHT"
      :column-config="{resizable: true}"
      show-overflow
      show-pager
      immediate
    >
      <vxe-table-column title="查看器名称" field="name" align="center" />
      <vxe-table-column title="描述" field="description" align="center" show-overflow />
      <vxe-table-column title="URL" field="url" align="center" />
      <vxe-table-column title="启用" field="enableFlag" align="center" width="200">
        <template #default="{ row }">
          <tr-switch
            v-model.number="row.enableFlag"
            :params="row"
            :http-request="updateState"
            :inactive-value="2"
            :active-value="1"
            :invalid-value="0"
            params-code="enableFlag"
          />
        </template>
      </vxe-table-column>
      <vxe-table-column title="更新人" field="updatedBy" align="center">
        <template #default="{ row }">
          <user-cell :job-number="row.updatedBy" :avatar-size="22" />
        </template>
      </vxe-table-column>
      <vxe-table-column title="更新时间" field="updatedTime" align="center" width="160" />
      <vxe-table-column title="操作" align="center">
        <template #default="{ row }">
          <el-button type="text" @click="onAddOrEditClick(row)">编辑</el-button>
          <el-button v-if="row.enableFlag === 0" type="text" class="danger-color" @click="onDeleteData(row)">删除</el-button>
        </template>
      </vxe-table-column>
    </tr-table>
  </div>
</template>
