<script setup>

import { Message, MessageBox } from 'element-ui'
import TrTable from '@@/feature/TrTableX'
import { tableColumnsByCode } from './columns.js'
import { getCopyExecList, batchExecCopy } from '@/api/file-manage.js'

const currentActivedTab = shallowRef(3)

const TABLE_HEIGHT = window.innerHeight - 140

const tableRef = shallowRef()
// 筛选数据弹窗
const filterForm = ref({ _date: [] })

const tableData = shallowRef([])

// 计算条件数量
const setConditionsNumbers = computed(() => Object.values(filterForm.value).filter((v) => v && v.length).length)

// 当前状态渲染的表头
const tableColumns = computed(() => tableColumnsByCode[currentActivedTab.value])

const status = computed(() => ({
  waiting: currentActivedTab.value === 0,
  executing: currentActivedTab.value === 1,
  success: currentActivedTab.value === 2,
  failed: [3, 4].includes(currentActivedTab.value)
}))

// 不同的标签请求不同的接口数据
const httpRequest = (sizeConfig) => {
  // 执行失败的传 3,4, 其他的状态传当前的tab值
  const executeState = currentActivedTab.value < 3 ? [currentActivedTab.value] : [3, 4]

  const { _date, ..._filterForm } = filterForm.value

  return getCopyExecList({ ...sizeConfig, param: { ..._filterForm, executeState } })
}

// 点击查询或重置时
function onResetFilterForm(reassign = false) {
  if (reassign) {
    filterForm.value = {
      _date: []
    }
  } else {
    const [start, end] = filterForm.value._date

    filterForm.value.executeStartTime = start
    filterForm.value.executeEndTime = end
  }

  // 关闭弹窗
  document.body.click()
}

function onRefreshTableData() {
  tableRef.value.query()
}

// 查看错误原因
function onViewLog(row) {
  MessageBox.confirm(row.executionResult, '执行日志', {
    showCancelButton: false,
    showConfirmButton: false
  })
}

// 重新执行(包括批量执行)，如果没有传入 row，则表示批量执行
function onReExecute(row) {
  const bids = []

  if (row) {
    bids.push(row.bid)
  } else {
    const checkedList = tableRef.value.$refs.tableRef.getCheckboxRecords()

    bids.push(...checkedList.map((v) => v.bid))
  }

  if (!bids.length) {
    Message.warning('请选择要操作的数据')

    return
  }

  batchExecCopy(bids)
    .then(({ success }) => {
      if (success) {
        Message.success('操作成功')
        onRefreshTableData()
      } else {
        Message.error('操作失败')
      }
    })
}
</script>

<template>
  <div>
    <el-radio-group v-model="currentActivedTab">
      <el-radio-button :label="3">执行失败</el-radio-button>
      <el-radio-button :label="2">执行成功</el-radio-button>
      <el-radio-button :label="1">执行中</el-radio-button>
      <el-radio-button :label="0">未开始</el-radio-button>
    </el-radio-group>

    <div class="tools-box !pl-0">
      <div v-if="status.failed" class="btn-box" @click="onReExecute()">批量执行</div>

      <el-popover placement="bottom-start" @hide="onRefreshTableData()">
        <template #reference>
          <div slot="reference" class="btn-box">
            <tr-svg-icon icon-class="t-filter" />
            <span><span v-if="setConditionsNumbers>0" class="filter-rule-number">{{ setConditionsNumbers }}</span>{{ $t('common.filter') }}</span>
          </div>
        </template>

        <div class="p-2">
          <el-form label-position="right" label-width="100px">
            <el-form-item label="文件名">
              <el-input v-model="filterForm.fileName" clearable />
            </el-form-item>
            <el-form-item label="复制规则名称">
              <el-input v-model="filterForm.copyRuleName" clearable />
            </el-form-item>
            <el-form-item v-if="!status.waiting" label="执行开始时间">
              <el-date-picker v-model="filterForm._date" type="datetimerange" value-format="yyyy-MM-dd HH:mm:ss" class="!w-205px" clearable />
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
      :key="currentActivedTab"
      :data="tableData"
      :height="TABLE_HEIGHT"
      :http-request="httpRequest"
      show-pager
      immediate
    >
      <vxe-table-column v-if="status.failed" type="checkbox" width="50" fixed="left" />

      <vxe-table-column v-for="column of tableColumns" :key="column.name" align="center" v-bind="column" />

      <vxe-table-column v-if="status.failed" title="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button type="text" class="active-color !py-1" @click="onViewLog(row)">查看日志</el-button>
          <el-button type="text" class="active-color !py-1" @click="onReExecute(row)">重新执行</el-button>
        </template>
      </vxe-table-column>
    </tr-table>
  </div>
</template>
