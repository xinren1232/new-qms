<script setup>
import { MessageBox, Message } from 'element-ui'
import { useLazyLoading } from '@/composables/lazy-loading.js'
import TypeTable from '../components/type-table.vue'
import AddForm from './components/add-form.vue'
import ViewerAddForm from '../viewer/components/add-form.vue'
import {
  getSingleFileType,
  getFileViewerList,
  addOrUpdateFileType,
  addOrUpdateFileViewer,
  operateFileTypeRelation
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
  enableFlag: 0,
  name: '',
  suffixName: '',
  // readRule: '',
  matching: '',
  priority: '',
  mimeType: '',
  storageRule: '',
  description: '',

  cfgFileViewerVos: []
})

const relationTabs = [
  {
    title: '查看器',
    fieldKey: 'cfgFileViewerVos',
    apis: {
      // 选取表格数据请求接口
      selectRequest: (sizeConfig) => getFileViewerList({ count: true, ...sizeConfig, param: { enableFlag: 1 } }),

      // 确认创建 API
      createConfirm: (params) => addOrUpdateFileViewer({ ...params, enableFlag: 1 }),

      // 确认移除 API
      removeConfirm: (selectedList) => operateFileTypeRelation({
        operateType: 'delete',
        fileTypeBid: formData.value.bid,
        fileViewerBidList: selectedList.map(item => item.bid)
      }),

      // 确认选取 API
      selectConfirm: (selectedList) => operateFileTypeRelation({
        operateType: 'add',
        fileTypeBid: formData.value.bid,
        fileViewerBidList: selectedList.map(item => item.bid)
      })
    },
    attrs: {
      createFormRender: ViewerAddForm,
      listTableColumns: [
        { title: '名称', field: 'name' },
        { title: '描述', field: 'description' },
        { title: 'URL', field: 'url' }
      ],
      selectTableColumns: [
        { title: '查看器名称', field: 'name' },
        { title: '描述', field: 'description' },
        { title: 'URL', field: 'url' },
        { title: '是否启用', field: 'enableFlag', align: 'cener', formatter: ({ row }) => row.enableFlag === 1 ? '是' : '否' }
      ]
    }
  }
]

// 如果是编辑则获取详情数据
function getDetailDataByBid() {
  if (route.params.bid === 'add') {
    return
  }

  startLoading()

  getSingleFileType(route.params.bid)
    .then(({ data, success }) => {
      if (success) {
        data.cfgFileViewerVos ??= []

        formData.value = data ?? []
      }
    })
    .finally(cancelLoading)
}

// 保存数据
async function onSaveClick() {
  await formRef.value.validate()

  const _formData = { ...formData.value }

  // 如果是空字符串则设置为 null
  if (_formData.readRule === '') {
    _formData.readRule = null
  } else {
    _formData.readRule = Number(_formData.readRule)
  }

  const { data, success } = await addOrUpdateFileType(_formData)

  if (success) {
    Message.success('保存成功')

    if (!formData.value.bid) {
      data.cfgFileViewerVos = []
      formData.value = data

      replaceAddToBid(data.bid)
    }

    return
  }

  Message.error('保存失败, 请重试')

  return Promise.reject()
}

// 取消保存
async function onCancelClick() {
  await MessageBox.confirm('确定要取消所有操作吗？', '提示', { type: 'warning' })

  router.replace('/file-manage/file-type')
}

// 保存所有修改
async function onCompleteClick() {
  await onSaveClick()

  router.replace('/file-manage/file-type')
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

    <add-form ref="formRef" :form-data="formData" :code-disabled="route.params.bid !== 'add'" />

    <el-tabs value="cfgFileViewerVos">
      <el-tab-pane
        v-for="tab of relationTabs"
        :key="tab.title"
        :label="tab.title"
        :name="tab.fieldKey"
        lazy
      >
        <type-table
          :apis="tab.apis"
          :form-data="formData"
          :field-key="tab.fieldKey"
          v-bind="tab.attrs"
        />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
