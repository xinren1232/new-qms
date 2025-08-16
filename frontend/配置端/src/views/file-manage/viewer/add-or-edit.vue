<script setup>
import { MessageBox, Message } from 'element-ui'
import { useLazyLoading } from '@/composables/lazy-loading.js'
import AddForm from './components/add-form.vue'
import { addOrUpdateFileViewer, getSingleFileViewer } from '@/api/file-manage.js'
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
  enableFlag: 1
})

// 如果是编辑则获取详情数据
function getDetailDataByBid() {
  if (route.params.bid === 'add') {
    return
  }

  startLoading()

  getSingleFileViewer(route.params.bid)
    .then(({ data, success }) => {
      if (!success) {
        Message.error('获取详情数据失败')

        return
      }

      formData.value = data
    })
    .finally(cancelLoading)
}

// 暂存数据
async function onSaveClick() {
  await formRef.value.validate()

  const { data, success } = await addOrUpdateFileViewer(formData.value)

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
  await MessageBox.confirm('确定要放弃所有操作吗？', '提示', { type: 'warning' })

  router.replace('/file-manage/viewer')
}

// 保存所有修改
async function onCompleteClick() {
  await onSaveClick()

  router.replace('/file-manage/viewer')
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
  </div>
</template>
