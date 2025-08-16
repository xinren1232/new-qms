<script setup>
import { MessageBox, Message } from 'element-ui'

import TrTable from '@@/feature/TrTableX/index.js'

const props = defineProps({
  // 操作数据的接口
  apis: {
    type: Object,
    default: () => ({})
  },

  // 详情数据
  formData: {
    type: Object,
    default: () => ({})
  },

  fieldKey: {
    type: String,
    default: ''
  },

  // 列表上面的新增按钮点击后弹窗内渲染的新建表单
  createFormRender: {
    type: Object,
    default: null
  },

  // 基本信息下面的列表表头
  listTableColumns: {
    type: Array,
    default: () => []
  },

  // 选取弹窗展示的表头
  selectTableColumns: {
    type: Array,
    default: () => []
  }
})

const listTableRef = shallowRef()
const createFormRef = shallowRef()
const selectTableRef = shallowRef()

const selectDialogVisible = shallowRef(false)
const createDialogVisible = shallowRef(false)

// 弹窗数据
const selectTableData = shallowRef([])

// 新建数据弹窗
const createFormData = ref({})

const selectTableCheckboxConfig = {
  // 判断当前行是否已经包含在外层列表中
  checkMethod: ({ row: { bid } }) => {
    // 如果列表中没有数据则全部可选
    if (!props.formData[props.fieldKey]?.length) {
      return true
    }

    // 否则取差集
    return !props.formData[props.fieldKey].find((item) => item.bid === bid)
  }
}

// 打开选取弹窗
async function onSelectClick() {
  // 因为在修改关系数据时需要设置当前数据的bid，所以在没有bid的时候就表示这条数据是新创建的，需要先保存基本信息，以下同理
  if (!props.formData.bid) {
    Message.warning('请先保存基本信息！')

    return
  }

  selectDialogVisible.value = true
}

// 新增弹窗
function onCreateClick() {
  if (!props.formData.bid) {
    Message.warning('请先保存基本信息！')

    return
  }

  createDialogVisible.value = true
}

// 点击移除
async function onRemoveClick() {
  if (!props.formData.bid) {
    Message.warning('请先保存基本信息！')

    return
  }

  const tableRef = listTableRef.value.$refs.tableRef
  const checkedList = tableRef.getCheckboxRecords()

  if (!checkedList.length) {
    Message.warning('请先选择需要移除的数据！')

    return
  }

  await MessageBox.confirm(`您确定要移除包括【${checkedList[0].name}】在内的${checkedList.length}条数据？`, '提示', { type: 'warning' })

  const { success } = await props.apis.removeConfirm(checkedList)

  if (success) {
    Message.success('移除成功')

    // 移除成功后更新列表数据
    props.formData[props.fieldKey] = props.formData[props.fieldKey].filter(item => !checkedList.some(checkedItem => checkedItem.bid === item.bid))

    tableRef.setCheckboxRow([], false)

    return
  }

  Message.error('移除失败, 请稍后重试')
}

// 选取弹窗确认
async function onSelectDialogConfirm() {
  const checkedList = selectTableRef.value.$refs.tableRef.getCheckboxRecords()

  if (!checkedList.length) {
    selectDialogVisible.value = false

    return
  }

  const { success } = await props.apis.selectConfirm(checkedList)

  if (success) {
    Message.success('选取成功')

    selectDialogVisible.value = false

    // 选取成功后更新列表数据
    props.formData[props.fieldKey].push(...checkedList)

    // 取消数据的选中
    selectTableRef.value.$refs.tableRef.setCheckboxRow([], false)

    return
  }

  Message.error('选取失败, 请稍后重试')
}

// 当新增弹窗确定时创建数据
async function onCreateDialogConfirm() {
  await createFormRef.value.validate()

  const { data, success } = await props.apis.createConfirm(createFormData.value)

  if (!success) {
    Message.error('新增失败, 请稍后重试')

    return
  }

  // 新增数据以后同时调用选取功能，把这条新增的数据选取到列表中
  const { success: selectSuccess } = await props.apis.selectConfirm([data])

  if (selectSuccess) {
    Message.success('新增成功')

    props.formData[props.fieldKey].push(data)

    createDialogVisible.value = false
    createFormData.value = {}

    return
  }

  Message.error('新增失败, 请稍后重试')
}
</script>

<template>
  <div>
    <div class="mb-4">
      <el-button @click="onSelectClick">选取</el-button>
      <el-button type="primary" @click="onCreateClick">新增</el-button>
      <el-button type="danger" @click="onRemoveClick">移除</el-button>
    </div>

    <!-- 数据表格 -->
    <tr-table ref="listTableRef" v-model="formData[fieldKey]" class="w-1000px" height="300px">
      <vxe-table-column type="checkbox" width="50" />
      <vxe-table-column v-for="col of listTableColumns" :key="col.prop" align="center" v-bind="col" />
    </tr-table>

    <!-- 选取弹窗 -->
    <el-dialog :visible.sync="selectDialogVisible" title="选取" width="800px" destroy-on-close>
      <tr-table
        ref="selectTableRef"
        :data="selectTableData"
        :checkbox-config="selectTableCheckboxConfig"
        :http-request="apis.selectRequest"
        height="300px"
        show-pager
        immediate
      >
        <vxe-table-column type="checkbox" width="50" />
        <vxe-table-column v-for="col of selectTableColumns" :key="col.field" align="center" v-bind="col" />
      </tr-table>

      <div class="text-right mt-2">
        <el-button type="primary" @click="onSelectDialogConfirm">确定</el-button>
      </div>
    </el-dialog>

    <!-- 新增弹窗 -->
    <el-dialog :visible.sync="createDialogVisible" width="840px" title="新增">
      <component :is="createFormRender" ref="createFormRef" :form-data="createFormData" />

      <div class="text-right">
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="onCreateDialogConfirm">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>
