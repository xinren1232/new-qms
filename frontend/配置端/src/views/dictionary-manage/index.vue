<template>
  <div>
    <div class="tools-box">
      <div class="btn-box" @click="onAddBtnClick(null)">
        <tr-svg-icon icon-class="add-list" /><span class="text">新增</span>
      </div>
      <el-divider direction="vertical" />
      <!--      <div class="btn-box" @click="() => {}"><tr-svg-icon icon-class="t-import" /><span class="text">导入</span></div>-->
      <div class="btn-box" @click="onExportBtnClick">
        <tr-svg-icon v-if="!exportLoading" icon-class="t-export" />
        <i v-else class="el-icon-loading" />
        <span class="text">导出</span>
      </div>
      <el-divider direction="vertical" />
      <filter-panel class="btn-box" @filter-change="onFilterChange" />
    </div>
    <tr-table
      ref="TrTableRef"
      v-model="tableData"
      :show-pager="true"
      :http-request="httpRequest"
      :height="height"
      :params="{ count: true, param: form }"
      border
      immediate
      :column-config="{ resizable: true }"
      :row-config="{ height: tableConfig.rowHeight }"
      :cell-class-name="cellClassName"
      @cell-click="onCellClick"
    >
      <vxe-table-column title="字典名称" field="name" sortable align="left" min-width="200">
        <template #default="{ row }">
          <el-tooltip content="点击我编辑字典信息" :open-delay="600">
            <span class="common-btn active-hover" @click="onAddBtnClick(row)">{{ row.name }}</span>
          </el-tooltip>
        </template>
      </vxe-table-column>
      <vxe-table-column title="字典编码" field="code" sortable align="left" min-width="200" />
      <vxe-table-column title="状态" :field="STATUS_FIELD" sortable align="center" min-width="160">
        <template #default="{ row }">
          <tr-switch
            v-model.number="row[STATUS_FIELD]"
            :params="row"
            params-code="enableFlag"
            :http-request="updateDictStatus"
            :invalid-value="0"
            :active-value="1"
            :inactive-value="2"
          />
        </template>
      </vxe-table-column>
      <!--        <vxe-table-column title="字典描述" field="describe" sortable align="center" min-width="120" />-->
      <vxe-table-column title="更新人" field="updatedBy" sortable align="center" min-width="140">
        <template #default="{ row }"> <user-cell :job-number="row.updatedBy" :avatar-size="22" /> </template>
      </vxe-table-column>
      <vxe-table-column title="更新时间" field="updatedTime" sortable align="center" min-width="160" />
      <vxe-table-column title="操作" align="left" width="140" fixed="right">
        <template #default="{ row }">
          <el-button type="text" size="small" @click="onAddBtnClick(row)">编辑</el-button>
          <template v-if="row[STATUS_FIELD] === 0">
            <el-divider direction="vertical" />
            <el-button type="text" class="danger-color" size="small" @click="onDeleteBtnClick(row)">删除</el-button>
          </template>
        </template>
      </vxe-table-column>
    </tr-table>
    <add-edit-panel ref="addPanel" @add-success="onAddSuccess" />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Message, MessageBox } from 'element-ui'
import { STATUS_FIELD } from '@/config/state.config.js'
import AddEditPanel from './components/add-edit-panel.vue'
import FilterPanel from './components/filter-panel.vue'
import TrSwitch from '@@/feature/TrSwitch'
import TrTable from '@@/feature/TrTableX'
import UserCell from '@@/plm/user/user-cell.vue'
import { pageQuery, deleteDict, updateDictStatus } from './api/index.js'
import { mergeApiNeedUserInfo } from '@/utils'
import tableConfig from '@/config/table.config'
import useExport from '@/hooks/useExport'

// 表格组件
const TrTableRef = ref(null)
// 新增面板组件
const addPanel = ref(null)

// 查询表单form
const form = ref({})

// 表格数据
const tableData = ref([])

// 表格高度
const height = computed(() => window.innerHeight - 106)

// 查询数据方法
const httpRequest = params => {
  return mergeApiNeedUserInfo(pageQuery(params), ['updatedBy'])
}

// form查询按钮
const handleQuery = form => {
  const params = { param: form }

  TrTableRef.value.query(params)
}

const onFilterChange = values => {
  form.value = values
  handleQuery(values)
}

// 新增字典按钮
const onAddBtnClick = row => {
  addPanel.value.show(row)
}
// 新增成功回调
const onAddSuccess = () => {
  handleQuery(form.value)
}

// 删除按钮
const onDeleteBtnClick = row => {
  MessageBox.confirm('是否确认删除？', '警告', { type: 'warning' }).then(() => {
    deleteDict(row.bid).then(data => {
      if (data.success) {
        Message.success(data.message)
        const params = { param: {} }

        TrTableRef.value.query(params)
      }
    })
  })
}

const cellClassName = ({ column }) => {
  if (column.property === 'code') {
    return 'copy-class'
  }
}
const onCellClick = ({ row, column }) => {
  if (column.property === 'code') {
    const input = document.createElement('input')

    input.value = row[column.property]
    document.body.appendChild(input)
    input.select()
    document.execCommand('Copy')
    document.body.removeChild(input)
    Message.success('复制成功')
  }
}
// 导出
const { exportFile, exportLoading } = useExport()
const onExportBtnClick = () => {
  exportFile('DICT')
}
</script>

<style scoped lang="scss">
.tools-box {
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
    .text {
      font-size: 14px;
      margin-right: 4px;
      vertical-align: -0.2em;
    }
    .tr-svg-icon {
      font-size: 16px;
      margin-right: 4px;
      vertical-align: -0.15em;
    }

    &:hover {
      background-color: #eaedec;
      border-radius: 4px;
    }
    &:active {
      background-color: #dee1e0;
    }
  }
}
::v-deep .copy-class .vxe-cell--label {
  cursor: pointer;
  padding: 4px;
  border-bottom: 1px dashed var(--primary-5);
  &:hover {
    background-color: #eaedec;
    border-radius: 4px;
  }
  &:active {
    background-color: #dee1e0;
  }
}
.bussiness {
  margin-right: 3px;
}
</style>
