<template>
  <tr-card>
    <div class="box">
      <tr-form-render
        ref="preFormRef"
        :form-json="formJson"
        :preview-state="true"
        :global-dsv="globalDsv"
        @appendButtonClick="testOnAppendButtonClick"
        @buttonClick="testOnButtonClick"
        @formChange="handleFormChange"
      />
    </div>
  </tr-card>
</template>

<script setup>
import { ref } from 'vue'
import { getViewDetail } from '@/views/system-manage/view-manage/api'
import route from '@/router'
// 表单设计器
import '@root/libs/form-designer/TrFormDesigner.css'
import { getToken, getRToken } from '@/app/cookie'

const id = route.currentRoute?.params?.id
const preFormRef = ref(null)
const formJson = ref({})
const globalDsv = {
  requestHeader: {
    'token': getToken(),
    'rtoken': getRToken(),
    'appId': process.env.VUE_APP_CENTER_BASE_APP_ID
  },
  fileServiceUrl: process.env.VUE_APP_FILE_URL,
  gateway: process.env.VUE_APP_GATEWAY_URL,
  microServices: process.env.VUE_APP_PUBLIC_PATH === '/transcend-pi-configcenter/' ? '/transcend-plm-configcenter-pi/' : '/transcend-plm-configcenter/'
}
const testOnAppendButtonClick = (field) => {
  console.log('appendButtonClick', field)
}

const testOnButtonClick = (field) => {
  console.log('buttonClick', field)
}

const handleFormChange = (field, value) => {
  console.log('formChange', field, value)
}

const getDetail = async () => {
  const { success, data } = await getViewDetail(id)

  if (success) {
    formJson.value = data.content
    preFormRef.value.setFormJson(data.content)
  }
}

getDetail()
</script>

<style scoped lang="scss">
.box{
  height: calc(100vh - 20px);
  overflow: overlay;
}
</style>
