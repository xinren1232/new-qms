<script setup>
// import { MessageBox } from 'element-ui'
import { useLazyLoading } from '@/composables/lazy-loading.js'
// import TypeTable from '../components/type-table.vue'
import AddForm from './components/add-form.vue'
// import LibraryAddForm from '../file-library/components/add-form.vue'

const route = useRoute()
// const router = useRouter()

const {
  loading,
  startLoading,
  cancelLoading
} = useLazyLoading()

const formRef = shallowRef()

const formData = ref({})

// const relationTabs = [
//   {
//     title: '文件类型',
//     fieldKey: 'cfgFileTypeVos',
//
//     apis: {
//       // 选取表格数据请求接口
//       selectRequest: (param) => Promise.resolve(),
//
//       // 确认创建 API
//       createConfirm(formData) { },
//
//       // 确认移除 API
//       removeConfirm(selectedList) { },
//
//       // 确认选取 API
//       selectConfirm(selectedList) {
//         return
//       }
//     },
//
//     attrs: {
//       createFormRender: LibraryAddForm,
//       listTableColumns: [
//         { title: '名称', field: 'name' },
//         { title: '描述', field: 'description' }
//       ],
//       selectTableColumns: [
//         { title: '文件库名称', field: 'name' },
//         { title: '描述', field: 'name' },
//         { title: 'URL规则', field: 'name' },
//         { title: '启用', field: 'enable', align: 'cener', formatter: () => '启用' }
//       ]
//     }
//   }
// ]

// 如果是编辑则获取详情数据
function getDetailDataByBid() {
  if (route.params.bid === 'add') {
    return
  }

  startLoading()

  Promise.resolve()
    .then(() => {
      formData.value = {}
    })
    .finally(cancelLoading)
}

// // 暂存数据
// function onSaveClick() {
// }
//
// // 取消保存
// async function onCancelClick() {
//   await MessageBox.confirm('确定要放弃所有操作吗？', '提示', { type: 'warning' })
//
//   router.replace('/file-manage/file-list')
// }
//
// // 保存所有修改
// async function onCompleteClick() {
//   await formRef.value.validate()
//
//   console.log('formData.value', formData.value)
// }

getDetailDataByBid()
</script>

<template>
  <div v-loading="loading" class="pl-4 pt-2 !!overflow-auto">
    <!--    <div class="mb-4">-->
    <!--      <el-button @click="onSaveClick">暂存</el-button>-->
    <!--      <el-button type="danger" @click="onCancelClick">放弃</el-button>-->
    <!--      <el-button type="primary" @click="onCompleteClick">完成</el-button>-->
    <!--    </div>-->

    <add-form ref="formRef" :form-data="formData" />

    <!--    <el-tabs value="cfgFileTypeVos">-->
    <!--      <el-tab-pane-->
    <!--        v-for="tab of relationTabs"-->
    <!--        :key="tab.title"-->
    <!--        :label="tab.title"-->
    <!--        :name="tab.fieldKey"-->
    <!--        lazy-->
    <!--      >-->
    <!--        <type-table-->
    <!--          :apis="tab.apis"-->
    <!--          :form-data="formData"-->
    <!--          :field-key="tab.fieldKey"-->
    <!--          v-bind="tab.attrs"-->
    <!--        />-->
    <!--      </el-tab-pane>-->
    <!--    </el-tabs>-->
  </div>
</template>
