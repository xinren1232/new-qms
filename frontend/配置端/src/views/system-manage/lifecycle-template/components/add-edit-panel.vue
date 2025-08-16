<template>
  <div class="container">
    <tr-card header="基本信息" :expand="defaultExpand" @toggle="onTrCardToggleExpand">
      <tr-form ref="TrFormRef" v-model="form" collapsible :items="formItems" :rules="rules" :show-buttons="false" />
    </tr-card>
    <div v-loading="loading" class="graph-box">
      <rule-orchestrator
        v-if="showGraph"
        ref="ruleOrchestratorRef"
        :state-list="stateList"
        :data="form"
        @export-data="onConfirm"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, computed, onMounted } from 'vue'
import { Message } from 'element-ui'
import route from '@/router'
import TrForm from '@@/feature/TrForm'
import TrSelect from '@@/feature/TrSelect'
import RuleOrchestrator from '@@/plm/rule-orchestrator/index.vue'
import { createOrUpdateAttribute, getAllState, getAvailableVersionList, getVersionDetail } from '../api/index.js'
import LengthConfig from '@/config/constraint.config.js'

const id = route.currentRoute?.params?.id
const version = route.currentRoute?.params?.version
const TrFormRef = ref(null)
const form = ref({})
const stateList = ref([])
const loading = ref(false)
const showGraph = ref(false)
const defaultExpand = ref(!id) // 默认展开状态
const ruleOrchestratorRef = ref(null)

// 如果是新增就显示图形
if (!id && !version) {
  showGraph.value = true
}
// 版本列表
const versionList = ref([])

const rules = {
  name: [
    { required: true, message: '请输入状态名称', trigger: 'blur' },
    { max: LengthConfig.TEXT, message: `长度不能超过${LengthConfig.TEXT}` }
  ]
}

const formItems = computed(() => {
  if (!id) {
    return [
      {
        label: '模板名称',
        prop: 'name',
        span: 12,
        attrs: {
          clearable: true,
          maxlength: LengthConfig.TEXT,
          placeholder: `请输入名称，最大长度${LengthConfig.TEXT}`
        }
      },
      {
        label: '描述',
        prop: 'description',
        span: 24,
        attrs: {
          clearable: true,
          type: 'textarea',
          maxlength: LengthConfig.TEXTAREA,
          placeholder: `请输入描述，最大长度${LengthConfig.TEXTAREA}`
        }
      },
      {
        label: '是否阶段状态',
        prop: 'phaseState',
        span: 24,
        component: 'ElSwitch',
        attrs: {
          activeText: '是',
          inactiveText: '否',
          activeValue: 1,
          inactiveValue: 0
        }
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
    {
      label: '切换版本',
      prop: 'version',
      span: 12,
      component: TrSelect,
      attrs: { data: versionList.value },
      listeners: {
        change: value => {
          showGraph.value = false
          route.push(`/lifecycle-template-manage/action/detail/${id}/${value}`)
          getRowDetail(id, value)
        }
      }
    },
    {
      label: '说明',
      prop: 'description',
      span: 24,
      attrs: { type: 'textarea', maxlength: LengthConfig.TEXTAREA, placeholder: `最大长度${LengthConfig.TEXTAREA}` }
    },
    {
      label: '是否阶段状态',
      prop: 'phaseState',
      span: 24,
      component: 'ElSwitch',
      attrs: {
        activeText: '是',
        inactiveText: '否',
        activeValue: 1,
        inactiveValue: 0
      }
    }
  ]
})
// 获取可用版本列表
const getAvailableVersion = async (bid, version) => {
  const data = await getAvailableVersionList({ bid, currentVersion: version })

  if (data?.success) {
    versionList.value = data.data.map(item => {
      return {
        label: item.version,
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
    stateList.value = data.filter(item => item.enableFlag === 1)
  }
}
// 获取版本详情
const getRowDetail = async (bid, version) => {
  loading.value = true
  const { data, success } = await getVersionDetail(bid, version).finally(() => {
    showGraph.value = true
    setTimeout(() => {
      loading.value = false
    }, 600)
  })

  if (success) {
    data.layouts.forEach(item => {
      delete item.tools
    })
    data.cells = data.layouts
    form.value = data
  }
}

if (id && version) {
  // 获取可用版本列表
  getAvailableVersion(id, version)
  // 获取版本详情
  getRowDetail(id, version)
}
getAllStateList()

// 清除表单校验
nextTick(() => TrFormRef.value.clearValidate())

const onConfirm = exportData => {
  loading.value = true
  exportData.cells.forEach(item => {
    delete item?.tools
  })
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
        loading.value = false
        await route.push({ path: '/system-manage/lifecycle-template-manage' })
      } else {
        Message.error(data?.message)
        loading.value = false
      }
    })
    .catch(() => {
      loading.value = false
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

const onTrCardToggleExpand = isExpand => {
  if (isExpand) {
    ruleOrchestratorRef.value?.setContainerSize('100%', 'calc(100vh - 285px)')
  } else {
    ruleOrchestratorRef.value?.setContainerSize('100%', 'calc(100vh - 83px)')
  }
}

onMounted(() => {
  setTimeout(() => {
    onTrCardToggleExpand(defaultExpand.value)
  }, 400)
})
</script>

<style lang="scss" scoped>
.graph-box {
  // 阴影
  box-shadow: 0 0 15px 0 rgba(0, 0, 0, 0.1);
  border-radius: 4px;
  overflow: hidden;
  background-color: #ffffff;
}
</style>
