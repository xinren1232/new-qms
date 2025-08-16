<script setup>
import { MessageBox, Message } from 'element-ui'
import { useLazyLoading } from '@/composables/lazy-loading.js'
import AddForm from './components/add-form.vue'
import { copyTypeMap, copyEventMap, copyModeMap } from '../copy-rule/config.js'
import TrTable from '@@/feature/TrTableX'
import {
  getSingleFileLibrary,
  addOrUpdateFileLibrary
} from '@/api/file-manage.js'
import { replaceAddToBid } from '../composables/use-route-bid.js'

const route = useRoute()
const router = useRouter()

const {
  loading,
  startLoading,
  cancelLoading
} = useLazyLoading()

const formRef = shallowRef()
const formData = ref({
  name: '',
  urlRule: '',
  url: '',
  address: '',
  description: '',
  defaultFlag: false
})

const listTableColumns = [
  { title: '复制规则名称', field: 'name' },
  { title: '复制事件', field: 'copyEvent', width: 100, formatter: ({ row }) => copyEventMap[row.copyEvent] },
  { title: '复制模式', field: 'copyMode', width: 100, formatter: ({ row }) => copyModeMap[row.copyMode] },
  { title: '复制类型', field: 'copyType', width: 100, formatter: ({ row }) => copyTypeMap[row.copyType] },
  { title: '过滤方法', field: 'filterMethod' },
  {
    title: '延迟时长(秒)', field: 'delayDuration', formatter({ row }) {
      // 只有【复制模式】等于【延迟】时才展示延迟时长
      if (row.copyMode === '2') {
        return row.delayDuration
      }
    }
  },
  { title: '复制超时(秒)', field: 'timeOut' }
]

// 如果是编辑则获取详情数据
function getDetailDataByBid() {
  if (route.params.bid === 'add') {
    return
  }

  startLoading()

  getSingleFileLibrary(route.params.bid)
    .then(({ data, success }) => {
      if (success) {
        formData.value = data
      }
    })
    .finally(cancelLoading)
}

// 保存数据
async function onSaveClick() {
  await formRef.value.validate()

  const { data, success } = await addOrUpdateFileLibrary(formData.value)

  if (success) {
    Message.success('保存成功')
    // 保存成功以后增加 bid 属性，再次调用的时候这条数据就是保存数据了
    formData.value = data

    replaceAddToBid(data.bid)

    return
  }

  Message.error('保存失败, 请重试')

  return Promise.reject()
}

// 取消保存
async function onCancelClick() {
  await MessageBox.confirm('确定要取消所有操作吗？', '提示', { type: 'warning' })

  router.replace('/file-manage/file-library')
}

// 保存所有修改
async function onCompleteClick() {
  await onSaveClick()
  router.replace('/file-manage/file-library')
}

getDetailDataByBid()
</script>

<template>
  <div v-loading="loading" class="pl-4 pt-2 !overflow-auto">
    <div class="mb-4">
      <el-button @click="onSaveClick">保存</el-button>
      <el-button type="danger" @click="onCancelClick">取消</el-button>
      <el-button type="primary" @click="onCompleteClick">完成</el-button>
    </div>

    <add-form ref="formRef" :form-data="formData" />

    <tr-table :data="formData.cfgFileCopyRuleVos" class="w-1000px" height="300px">
      <vxe-table-column v-for="col of listTableColumns" :key="col.prop" align="center" v-bind="col" />
    </tr-table>
  </div>
</template>
