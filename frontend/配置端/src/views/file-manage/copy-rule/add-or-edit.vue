<script setup>

import { debounce } from 'lodash'
import { MessageBox, Message } from 'element-ui'
import { useLazyLoading } from '@/composables/lazy-loading.js'
import TypeTable from '../components/type-table.vue'
import AddForm from './components/add-form.vue'
import FileTypeAddForm from '../file-type/components/add-form.vue'
import TrTable from '@@/feature/TrTableX/index.js'
import TrSelect from '@@/feature/TrSelect/index.js'
import {
  addOrUpdateCopyRule,
  getSingleCopyRule,
  getFileLibraryList,
  getFileTypeList,
  addOrUpdateFileType,
  operateCopyRuleRelation,
  updateCopyRuleFileLibrary,
  deleteCopyRuleFileLibrary
} from '@/api/file-manage.js'
import { replaceAddToBid } from '../composables/use-route-bid.js'

const route = useRoute()
const router = useRouter()

const formRef = shallowRef()

const {
  loading,
  startLoading,
  cancelLoading
} = useLazyLoading()

const formData = ref({
  name: '',
  copyType: '',
  copyEvent: '',
  timeOut: 0,
  copyMode: '2',
  delayDuration: 600,
  filterMethod: '',
  planTime: {
    type: 'day',
    week: 1,
    day: 1,
    hour: 0,
    minute: 0,
    second: 0
  },
  description: '',
  enableFlag: 0,
  _delayTime: '00:00:00',

  cfgFileTypeVos: [],
  cfgFileLibraryRuleRelVos: [{ sourceLibraryBid: '', targetLibraryBids: [] }]
})

// 文件库列表
const fileLibraryList = shallowRef([])

const relationTabs = [
  {
    title: '文件类型',
    fieldKey: 'cfgFileTypeVos',
    apis: {
      // 选取表格数据请求接口
      selectRequest: (sizeConfig) => getFileTypeList({ count: true, ...sizeConfig, param: { enableFlag: 1 } }),

      // 确认创建 API
      createConfirm: addOrUpdateFileType,

      // 确认移除 API
      removeConfirm: (selectedList) => operateCopyRuleRelation({
        operateType: 'delete',
        ruleBid: formData.value.bid,
        fileTypeBids: selectedList.map((item) => item.bid)
      }),

      // 确认选取 API
      selectConfirm: (selectedList) => operateCopyRuleRelation({
        operateType: 'add',
        ruleBid: formData.value.bid,
        fileTypeBids: selectedList.map((item) => item.bid)
      })
    },

    attrs: {
      createFormRender: FileTypeAddForm,
      listTableColumns: [
        { title: '名称', field: 'name' },
        { title: '描述', field: 'description' },
        { title: '后缀名', field: 'suffixName' }
      ],
      selectTableColumns: [
        { title: '名称', field: 'name' },
        { title: '后缀名', field: 'suffixName' },
        { title: '描述', field: 'description' },
        { title: '优先级', field: 'priority' },
        {
          title: '存储规则',
          field: 'storageRule',
          formatter: ({ row }) => ({
            IP: 'IP归属地',
            Base: 'Base归属地'
          }[row.storageRule])
        }
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

  getSingleCopyRule(route.params.bid)
    .then(({ data }) => {
      const planTime = data.planTime

      data._delayTime = `${planTime.hour}:${planTime.minute}:${planTime.second}`

      data.cfgFileTypeVos ??= []
      data.cfgFileLibraryRuleRelVos ??= []

      // 如果没有数据则默认添加一条空数据
      if (!data.cfgFileLibraryRuleRelVos?.length) {
        data.cfgFileLibraryRuleRelVos = [{ sourceLibraryBid: '', targetLibraryBids: [] }]
      }

      formData.value = data
    })
    .finally(cancelLoading)
}

// 获取文件库列表
function onGetFileLibrary(name) {
  getFileLibraryList({ count: true, current: 1, size: 1000, param: { name } })
    .then(({ data }) => {
      fileLibraryList.value = data.data ?? []
    })
}

// 目标文件库中不可以选择源文件库
function canSelectFileLibraryList({ sourceLibraryBid }) {
  return fileLibraryList.value.filter(item => item.bid !== sourceLibraryBid)
}

// 保存数据
async function onSaveClick() {
  await formRef.value.validate()

  const { data, success } = await addOrUpdateCopyRule(formData.value)

  if (success) {
    Message.success('保存成功')

    // 新建时保存以后返回数据详情, 主要是获取这条数据的bid
    if (!formData.value.bid) {
      data._delayTime = `${data.planTime.hour}:${data.planTime.minute}:${data.planTime.second}`

      // 新增的数据中没有这两个字段，手动补齐一下
      data.cfgFileTypeVos ??= []
      data.cfgFileLibraryRuleRelVos ??= [{ sourceLibraryBid: '', targetLibraryBids: [] }]

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

  router.replace('/file-manage/copy-rule')
}

// 保存并退出页面
async function onCompleteClick() {
  await onSaveClick()

  router.replace('/file-manage/copy-rule')
}

function onAddTargetFileLibrary() {
  if (!formData.value.bid) {
    Message.warning('请先保存基本信息！')

    return
  }

  formData.value.cfgFileLibraryRuleRelVos.push({
    ruleBid: formData.value.bid,
    sourceLibraryBid: '',
    targetLibraryBids: []
  })
}

// 保存文件库数据
const onSaveTargetFileLibrary = debounce(async (row) => {
  if (!row.sourceLibraryBid || !row.targetLibraryBids.length) {
    Message.error('请先选择源文件库或目标文件库')

    return
  }

  const { success, data, message } = await updateCopyRuleFileLibrary({ ...row, ruleBid: formData.value.bid })

  if (success) {
    Message.success('保存成功')

    Object.assign(row, data)
  } else {
    Message.error(message)
  }
}, 300)

// 删除文件库文件
async function onDelTargetFileLibrary(row, index) {
  formData.value.cfgFileLibraryRuleRelVos.splice(index, 1)

  // 如果数据没有保存过则直接删除
  if (!row.bid) {
    return
  }

  const { success } = await deleteCopyRuleFileLibrary(row.bid)

  if (success) {
    return
  }

  // 数据删除失败时，重新插入数据
  formData.value.cfgFileLibraryRuleRelVos.splice(index, 0, row)
  Message.error('删除失败, 请重试')
}

getDetailDataByBid()
onGetFileLibrary()
</script>

<template>
  <div v-loading="loading" class="pl-4 pt-2 !overflow-auto">
    <div class="mb-4">
      <el-button @click="onSaveClick">保存</el-button>
      <el-button type="danger" @click="onCancelClick">取消</el-button>
      <el-button type="primary" @click="onCompleteClick">完成</el-button>
    </div>

    <add-form ref="formRef" :form-data="formData" />

    <el-tabs value="cfgFileTypeVos">
      <el-tab-pane
        v-for="tab of relationTabs"
        :key="tab.title"
        :label="tab.title"
        :name="tab.fieldKey"
      >
        <type-table
          :apis="tab.apis"
          :form-data="formData"
          :field-key="tab.fieldKey"
          v-bind="tab.attrs"
        />
      </el-tab-pane>

      <el-tab-pane
        label="目标文件库"
        name="cfgFileLibraryRuleRelVos"
        lazy
      >
        <tr-table height="300" :data="formData.cfgFileLibraryRuleRelVos" class="w-1000px normal-input">
          <vxe-table-column title="源文件库" field="origin">
            <template #default="{row}">
              <tr-select v-model="row.sourceLibraryBid" :data="fileLibraryList" label-field="name" value-field="bid" />
            </template>
          </vxe-table-column>

          <vxe-table-column title="目标文件库" field="target">
            <template #default="{ row }">
              <tr-select v-model="row.targetLibraryBids" multiple :data="canSelectFileLibraryList(row)" label-field="name" value-field="bid" />
            </template>
          </vxe-table-column>

          <vxe-table-column align="center" width="120">
            <template #header>
              <el-button icon="el-icon-plus" class="!p-1" @click="onAddTargetFileLibrary" />
            </template>

            <template #default="{ row, rowIndex }">
              <el-button type="text" class="active-color" @click="onSaveTargetFileLibrary(row)">保存</el-button>
              <el-button type="text" class="danger-color" @click="onDelTargetFileLibrary(row, rowIndex)">删除</el-button>
            </template>
          </vxe-table-column>
        </tr-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
::v-deep .el-select__tags .el-tag {
  position: relative;
}

::v-deep .el-select__tags .el-select__tags-text {
  display: block;
  padding-right: 14px;
}

::v-deep .el-select__tags .el-tag__close {
  position: absolute;
  top: 2px;
  right: 2px;
}
</style>
