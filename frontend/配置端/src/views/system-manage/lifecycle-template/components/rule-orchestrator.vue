<template>
  <el-dialog
    title="规则编排"
    :visible.sync="visible"
    :close-on-click-modal="false"
    :width="width"
    custom-class="rule-orchestrator-dialog"
  >
    <div class="container">
      <rule-orchestrator :state-list="stateList" :data="currentRow" />
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import RuleOrchestrator from '@@/plm/rule-orchestrator/index.vue'
import { getVersionDetail,getAllState } from '../api/index.js'

const currentRow = ref({})

const stateList = ref([])

const width = computed(() => window.innerWidth - 20 + 'px')

const visible = ref(false)
// eslint-disable-next-line no-unused-vars
const emit = defineEmits(['add-success'])

const show = (row) => {
  currentRow.value = row
  getDetail(row.bid,row.currentVersion)
  getAllStateList()
  visible.value = true
}

// eslint-disable-next-line no-unused-vars
const closeDialog = () => {
  currentRow.value = {}
  visible.value = false
}

const getDetail = async (templateBid,version) => {
  const { data,success } = await getVersionDetail(templateBid,version)

  if (success) currentRow.value = data
}

// 获取所有状态
const getAllStateList = async () => {
  const params = {
    page: 1,
    size: 1000
  }
  const { data,success } = await getAllState(params)

  if (success) stateList.value = data
}

defineExpose({
  show
})
</script>

<style scoped lang="scss">
.container{
  width: 100%;
  background-color: #f4f5f6;
}
::v-deep .rule-orchestrator-dialog{
  .el-dialog__body{
    padding: 0;
    overflow: hidden;
    height: 70vh;
  }
  .el-dialog__header{
    padding: 15px 20px;
    font-weight: bolder;
    font-size: 13px;
  }
}
</style>
