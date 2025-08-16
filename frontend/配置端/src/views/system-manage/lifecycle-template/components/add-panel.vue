<template>
  <el-dialog
    :title="isAdd ? '新增' : '编辑'"
    :visible.sync="visible"
    :width="width"
    :close-on-click-modal="false"
    class="add-attr-dialog"
    destroy-on-close
    @closed="closeDialog"
  >
    <tr-card>
      <tr-form ref="TrFormRef" v-model="form" :items="formItems" :rules="rules" :show-buttons="false" />
    </tr-card>
    <tr-card class="margin-bottom">
      <rule-orchestrator :is-add="isAdd" :state-list="stateList" :data="form" @export-data="onConfirm" />
    </tr-card>
  </el-dialog>
</template>

<script setup>
import { ref, nextTick, computed } from 'vue'
import { Message, Loading } from 'element-ui'
import TrForm from '@@/feature/TrForm'
import TrSelect from '@@/feature/TrSelect'
import RuleOrchestrator from '@@/plm/rule-orchestrator/index.vue'
import { createOrUpdateAttribute, getAllState, getAvailableVersionList, getVersionDetail } from '../api/index.js'
import LengthConfig from '@/config/constraint.config.js'

// 判断是新增还是编辑
const isAdd = ref(true)
const TrFormRef = ref(null)
const visible = ref(false)
const form = ref({})
const stateList = ref([])
// eslint-disable-next-line no-unused-vars
const currentRow = ref({})

const width = computed(() => window.innerWidth - 100 + 'px')
// 版本列表
const versionList = ref([])

const emit = defineEmits(['add-success'])

const rules = {
  name: [
    { required: true, message: '请输入状态名称', trigger: 'blur' },
    { max: LengthConfig.TEXT, message: `长度不能超过${LengthConfig.TEXT}` },
    {
      pattern: /^(\w|[\u4e00-\u9fa5])+$/g,
      message: '只能输入汉字,字母,数字,下划线(_)',
      trigger: ['blur', 'change']
    }
  ]
}

const formItems = computed(() => {
  if (isAdd.value) {
    return [
      {
        label: '模板名称',
        prop: 'name',
        span: 12,
        attrs: { maxlength: LengthConfig.TEXT, placeholder: `最大长度${LengthConfig.TEXT}` }
      },
      {
        label: '说明',
        prop: 'description',
        span: 24,
        attrs: { type: 'textarea', maxlength: LengthConfig.TEXTAREA, placeholder: `最大长度${LengthConfig.TEXTAREA}` }
      }
    ]
  }

  return [
    {
      label: '模板名称',
      prop: 'name',
      span: 12,
      attrs: { maxlength: LengthConfig.TEXT, placeholder: `最大长度${LengthConfig.TEXT}` }
    },
    { label: '当前版本', prop: 'currentVersion', span: 12, component: TrSelect, attrs: { data: versionList.value } },
    {
      label: '说明',
      prop: 'description',
      span: 24,
      attrs: { type: 'textarea', maxlength: LengthConfig.TEXTAREA, placeholder: `最大长度${LengthConfig.TEXTAREA}` }
    }
  ]
})

const show = row => {
  if (row) {
    isAdd.value = false
    form.value = row
    // 获取可用版本列表
    getAvailableVersion(row)
    // 获取版本详情
    getRowDetail(row)
  } else {
    isAdd.value = true
    form.value = {}
  }
  getAllStateList()

  visible.value = true
  // 清除表单校验
  nextTick(() => TrFormRef.value.clearValidate())
}

const onConfirm = exportData => {
  const loadingInstance = Loading.service({ lock: true, target: '.add-attr-dialog .el-dialog', text: '正在保存...' })

  validate()
    .then(async () => {
      const params = {
        ...form.value,
        layouts: exportData.cells
      }

      delete params.cells
      const data = await createOrUpdateAttribute(params)

      if (data?.success) {
        Message.success(data.message)
        nextTick(() => loadingInstance.close())
        closeDialog()
        emit('add-success')
      } else {
        Message.error(data?.message)
        nextTick(() => loadingInstance.close())
      }
    })
    .catch(() => {
      nextTick(() => loadingInstance.close())
    })
}
// 表单校验
const validate = () => {
  return new Promise((resolve, reject) => {
    TrFormRef.value.validate(val => {
      if (val) {
        resolve()
      } else {
        reject()
      }
    })
  })
}
// eslint-disable-next-line no-unused-vars
const onCancel = () => {
  closeDialog()
}
const closeDialog = () => {
  TrFormRef.value.resetFields()
  TrFormRef.value.clearValidate()
  visible.value = false
}

// 获取可用版本列表
const getAvailableVersion = async row => {
  const data = await getAvailableVersionList(row)

  if (data?.success) {
    versionList.value = data.data.map(item => {
      return {
        label: item.name,
        value: item.version
      }
    })
  } else {
    Message.error(data?.message)
  }
}
// 获取所有状态
const getAllStateList = async () => {
  const params = {
    page: 1,
    size: 1000
  }
  const { data, success } = await getAllState(params)

  if (success) {
    data.forEach(item => {
      item.lifeCycleCode = item.code
    })
    stateList.value = data
  }
}
// 获取版本详情
const getRowDetail = async row => {
  const { data, success } = await getVersionDetail(row.bid, row.currentVersion)

  if (success) {
    data.cells = data.layouts
    form.value = data
  }
}

defineExpose({
  show
})
</script>

<style lang="scss" scoped>
.add-attr-dialog {
  ::v-deep .el-dialog__body {
    background-color: #f0f0f3;
    padding-bottom: 0;
  }
  ::v-deep .el-dialog__header {
    padding: 10px 20px;
  }
  .tr-form {
    margin-bottom: 0;
  }
}
.footer {
  margin-top: 25px;
  text-align: right;
}
</style>
