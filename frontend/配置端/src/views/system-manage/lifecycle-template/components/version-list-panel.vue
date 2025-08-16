<template>
  <el-dialog
    title="版本记录"
    :visible.sync="visible"
    :close-on-click-modal="false"
    width="900px"
  >
    <vxe-table
      ref="TrTableRef"
      :data="tableData"
      height="440"
      :column-config="{resizable: true}"
      show-overflow
      :row-config="{height:tableConfig.rowHeight}"
      border
      :show-pager="false"
      :loading="loading"
    >
      <vxe-table-column title="模板名称" field="name" sortable align="left" min-width="150">
        <template #default="{ row }">
          <el-tooltip :content="row.name" placement="top" :open-delay="600">
            <span class="active-hover font-bold">{{ row.name }}</span>
          </el-tooltip>
        </template>
      </vxe-table-column>
      <vxe-table-column title="版本号" field="version" sortable align="center" width="120">
        <template #default="{row}">
          <el-tag effect="dark">{{ row.version }}</el-tag>
        </template>
      </vxe-table-column>
      <vxe-table-column title="更新人" field="updatedBy" sortable align="center" min-width="130">
        <template #default="{ row }">
          <user-cell :job-number="row.updatedBy" :avatar-size="22" />
        </template>
      </vxe-table-column>
      <vxe-table-column title="更新时间" field="updatedTime" sortable align="center" min-width="140" />

      <vxe-table-column title="操作" align="left" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="text" size="small" @click="onSetCurrentVersion(row)">设为当前版本</el-button>
          <el-divider direction="vertical" />
          <el-button type="text" size="small" @click="onViewBtnClick(row)">查看</el-button>
        </template>
      </vxe-table-column>
    </vxe-table>

  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import route from '@/router'
import { Message } from 'element-ui'
import UserCell from '@@/plm/user/user-cell.vue'
import { getAvailableVersionList, setDefaultVersion } from '../api/index'
import { mergeApiNeedUserInfo } from '@/utils'
import tableConfig from '@/config/table.config'

const tableData = ref([])
const visible = ref(false)
const params = ref({})
const loading = ref(false)

const emits = defineEmits(['update-success'])
const onViewBtnClick = (row) => {
  const _router = route.resolve(`/lifecycle-template-manage/action/detail/${row.templateBid}/${row.version}`)

  window.open(_router.href, '_blank')
}
const show = async(row) => {
  visible.value = true

  tableData.value = []
  params.value = row
  loading.value = true
  const { data ,success,message } = await mergeApiNeedUserInfo(getAvailableVersionList(params.value),['updatedBy']).finally(() => {
    loading.value = false
  })

  if (!success) {
    Message.error(message)

    return
  }
  tableData.value = data
}
// 设为当前版本
const onSetCurrentVersion = async(row) => {
  const { success } = await setDefaultVersion(row.templateBid, row.version)

  if (success) {
    emits('update-success')
    visible.value = false
    Message.success('设置成功')
  }
}

defineExpose({
  show
})
</script>

<style scoped lang="scss">

</style>
