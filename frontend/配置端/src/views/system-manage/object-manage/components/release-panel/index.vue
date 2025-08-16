<script setup>
import { ref, nextTick } from 'vue'
import { renderFieldByDict } from '@/utils/dict'
import { releaseProperties } from './api.js'
import { getObjectAttrByModelCode } from '@/views/system-manage/object-manage/api'
import { Message } from 'element-ui'

const visible = ref(false)
const currentRow = ref(null)
const tableData = ref([])
const TrTableRef = ref(null)
const releaseLoading = ref(false)
const loading = ref(false)

const show = (row) => {
  currentRow.value = row
  visible.value = true
  getPropertyList()
}
const hide = () => {
  visible.value = false
}

// 获取属性列表
const getPropertyList = async () => {
  loading.value = true
  const { modelCode } = currentRow.value
  const { data,success,message } = await getObjectAttrByModelCode(modelCode).finally(() => {
    loading.value = false
  })

  if (!success) {
    Message.error(message)

    return
  }
  // published=true的放到前面
  tableData.value = data.attrList.sort((a,b) => {
    return !!a.published - !!b.published
  })

  // 设置默认checkbox 的值
  nextTick(() => {
    TrTableRef.value?.setCheckboxRow(tableData.value.filter(item => item.published),true)
  })

}

const checkMethod = ({ row }) => {
  return row?.published === false || !row?.published
}
// 发布功能
const onRelease = async () => {
  if (releaseLoading.value) return
  const selectedRows = TrTableRef.value?.getCheckboxRecords().filter(item => !item.published).map(item => item.bid) || []

  // if (!selectedRows.length) {
  //   Message.warning('请选择需要发布的属性')
  //
  //   return
  // }
  releaseLoading.value = true
  const { success,message } = await releaseProperties(currentRow.value.modelCode,selectedRows).finally(() => {
    releaseLoading.value = false
  })

  if (!success) {
    Message.error(message)

    return
  }
  Message.success(message)
  hide()
}

defineExpose({
  show,
  hide
})
</script>

<template>
  <el-dialog
    :visible.sync="visible"
    :close-on-click-modal="false"
    custom-class="release-panel"
    width="68%"
  >
    <div slot="title" class="title-box">
      <div class="left">
        <el-tooltip>
          <span slot="content">发布功能的作用是在数据库中建立一个新的表，并定义该表的基本字段。<br>在使用实例之前，对应的基类对象必须先进行发布操作。</span>
          <i class="el-icon-warning font-bold" />
        </el-tooltip>
        <span slot="title" class="el-dialog__title">发布</span></div>
      <div class="right t-btn">
        <div class="btn-box" @click="onRelease">
          <tr-svg-icon v-if="!releaseLoading" icon-class="release" />
          <i v-else class="el-icon-loading" />
          <span class="text">发布</span></div>
      </div>
    </div>
    <vxe-table
      ref="TrTableRef"
      :data="tableData"
      height="500"
      :column-config="{resizable: true}"
      border
      show-overflow
      highlight-current-row
      row-key
      :row-config="{stripe: true, highlight: true, size: 'mini',rowKey: 'bid'}"
      :checkbox-config="{checkMethod:checkMethod}"
    >
      <vxe-table-column type="checkbox" width="50" title=" " />
      <vxe-table-column type="seq" width="50" title=" " />
      <vxe-table-column title="属性名称" field="name" sortable align="left" min-width="150" />
      <vxe-table-column title="属性编码" field="code" sortable align="center" min-width="120" />
      <vxe-table-column title="数据类型" field="dataType" sortable align="center" min-width="120">
        <template #default="{ row }">{{ renderFieldByDict(row.dataType,'PROPERTY_DATA_TYPE') }}</template>
      </vxe-table-column>
      <vxe-table-column title="是否必填" field="required" sortable align="center" min-width="120">
        <template #default="{ row }">
          <el-tag v-if="row.required" type="danger">是</el-tag>
          <el-tag v-else type="info">否</el-tag>
        </template>
      </vxe-table-column>
      <vxe-table-column title="发布状态" field="published" sortable align="center" min-width="120">
        <template #default="{ row }">
          <el-tag v-if="row.published" type="danger">已发布</el-tag>
          <el-tag v-else type="info">未发布</el-tag>
        </template>
      </vxe-table-column>
    </vxe-table>
  </el-dialog>
</template>

<style scoped lang="scss">
.release-panel{
  .title-box{
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-right: 20px;
    .el-icon-warning{
      font-size: 16px;
      color: var(--warning-color);
      margin-right: 4px;
      cursor: pointer;
    }
    .right{
      display: flex;
      align-items: center;
      .btn-box{
        display: flex;
        align-items: center;
      }
    }
  }
}
::v-deep .el-dialog__headerbtn{
  top: 25px;
}
</style>
