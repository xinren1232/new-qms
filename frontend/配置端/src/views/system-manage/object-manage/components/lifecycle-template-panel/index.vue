<template>
  <el-dialog :visible.sync="visible" title="生命周期配置" width="440" :close-on-click-modal="false" custom-class="lc-dialog">
    <el-radio-group v-model="isSelectedStage" class="mb-3 ml-4">
      <el-radio-button :label="false">标准</el-radio-button>
      <el-radio-button :label="true">阶段</el-radio-button>
    </el-radio-group>

    <div class="h-200px">
      <tr-form ref="TrFormRef" v-model="form" :items="formItems" :rules="rules" :show-buttons="false" />
    </div>

    <div class="text-right">
      <el-button @click="onCancel">取消</el-button>
      <el-button type="primary" @click="onSubmit">保存</el-button>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Message, Loading } from 'element-ui'
import TrForm from '@@/feature/TrForm'
import TrSelect from '@@/feature/TrSelect'

import { pageQuery, getVersionDetail } from '@/views/system-manage/lifecycle-template/api/index.js'
import { saveLifecycleTemplateForObject, queryLifecycleTemplateForObject } from './api.js'

const form = ref({
  phaseInitState: ''
})
const TrFormRef = ref(null)
const visible = ref(false)

const isSelectedStage = shallowRef(false)

const lifecycleTemplateList = ref([])
const stateList = ref([])

const formItems = computed(() => {
  const _items = isSelectedStage.value
    ? [ // [阶段]
        {
          label: '阶段生命周期', prop: 'lcPhaseTemplBid', span: 24,
          component: TrSelect,
          placeholder: '请输入生命周期名称',
          attrs: { data: lifecycleTemplateList.value, labelField: 'name', valueField: 'bid' },
          listeners: {
            change: (bid) => {
              const current = lifecycleTemplateList.value.find(o => o.bid === bid)

              if (!current) return

              form.value.lcPhaseTemplVersion = current.currentVersion
            }
          }
        },
        { label: '说明', prop: 'phaseDescription', span: 24, placeholder: '请输入阶段说明说明', attrs: { type: 'textarea' } }
      ]
    : [ // [标准]
        {
          label: '生命周期模板', prop: 'lcTemplBid', span: 24,
          component: TrSelect,
          attrs: { data: lifecycleTemplateList.value, labelField: 'name', valueField: 'bid' },
          listeners: {
            change: (bid) => {
              const current = lifecycleTemplateList.value.find(o => o.bid === bid)

              if (!current) return
              getLifecycleTemplateDetail(bid, current.currentVersion)
              // form.value.initState= null
            }
          },
          placeholder: '请输入生命周期名称'
        },
        {
          label: '初始状态', prop: 'initState', span: 24, placeholder: '请输入生命周期名称',
          component: TrSelect,
          attrs: { data: stateList.value, labelField: 'name', valueField: 'code' }
        },
        { label: '说明', prop: 'description', span: 24, placeholder: '请输入说明', attrs: { type: 'textarea' } }
      ]

  return _items
})

const rules = {
  lcTemplBid: [
    { required: true, message: '请输入生命周期名称', trigger: 'change' }
  ],
  initState: [
    { required: true, message: '请输入初始状态', trigger: 'change' }
  ]
}
const modelCode = ref(null)
const show = row => {
  form.value.modelCode = row.modelCode
  modelCode.value = JSON.parse(JSON.stringify(row.modelCode))
  getLifecycleTemplateForObject()
  queryAllLifecycleTemplate()
  visible.value = true
}
// 查询所有生命周期模板
const queryAllLifecycleTemplate = async () => {
  const params = {
    'count': true,
    'param': {
      enableFlag: 1
    },
    'current': 1,
    'size': 10000
  }
  const { success, data } = await pageQuery(params)

  if (success) {
    lifecycleTemplateList.value = data?.data || []
  }
}
// 获取生命周期状态列表
const getLifecycleTemplateDetail = async (bid, version) => {

  const { success, data } = await getVersionDetail(bid, version)

  if (success) {
    stateList.value = data?.layouts.map(item => {
      if (item && item.shape === 'rect') return item?.data
    }).filter(Boolean)
  }
}

// 获取对象当前绑定的生命周期模板
const getLifecycleTemplateForObject = async () => {
  const { success, data } = await queryLifecycleTemplateForObject(form.value.modelCode)

  if (success) {
    if (data) {
      form.value = data
      const { lcTemplBid, lcTemplVersion } = data

      // 获取对象当前绑定的生命周期模板之后，再获取生命周期状态列表
      await getLifecycleTemplateDetail(lcTemplBid, lcTemplVersion)
    }
  }
}

const onSubmit = async () => {
  const valid = await TrFormRef.value.validate()

  if (!valid) return
  const loadingInstance = Loading.service({ lock: true, target: '.el-dialog.lc-dialog', text: '正在保存...' })

  form.value.modelCode = modelCode.value
  const { success, message } = await saveLifecycleTemplateForObject(form.value).finally(() => {
    loadingInstance.close()
  })

  if (success) {
    visible.value = false
  } else {
    Message.error(message)
  }
}

const onCancel = () => {
  form.value = {}
  TrFormRef.value.resetFields()
  visible.value = false
}

defineExpose({
  show
})
</script>

<style scoped>
.box {
  padding: 0 20px;
  width: 100px;
}
</style>
