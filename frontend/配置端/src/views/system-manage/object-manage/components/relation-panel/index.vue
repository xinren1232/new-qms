<template>
  <el-dialog
    :visible.sync="visible"
    :close-on-click-modal="false"
    width="1000px"
    destroy-on-close
    custom-class="relation-dialog"
    @close="onClose"
  >
    <span slot="title" class="el-dialog__title">
      <span class="margin-right">{{ relationType === 'single' ? '对象关系' : '多对象关系' }}</span><el-tag> {{ form.name }}</el-tag>
    </span>
    <div class="tools-box">
      <div v-if="showCheckInBtn" class="btn-box" @click="onAddEditBtnClick(null)">
        <tr-svg-icon icon-class="add-list" />
        <span class="text">添加</span>
      </div>
      <div v-if="showCheckOutBtn" class="btn-box" @click="onCheckoutBtnClick">
        <tr-svg-icon icon-class="t-checkout" /><span class="text">检出</span>
      </div>
      <div v-if="showCheckInBtn" class="btn-box" @click="onCheckinBtnClick">
        <tr-svg-icon icon-class="t-checkin" /><span class="text">检入</span>
      </div>
      <div v-if="showCheckInBtn" class="btn-box" @click="onCancelCheckinBtnClick">
        <tr-svg-icon icon-class="t-checkin" /><span class="text">撤销检出</span>
      </div>
    </div>
    <vxe-table
      v-if="visible"
      ref="TrTableRef"
      :data="tableData"
      height="460"
      :column-config="{ resizable: true }"
      border
      :edit-rules="validRules"
      :edit-config="{ trigger: 'dblclick', mode: 'row', showIcon: false, beforeEditMethod: activeRowMethod }"
      show-overflow
      :loading="loading"
      :row-config="{ height: tableConfig.rowHeight }"
    >
      <vxe-column type="seq" width="50" title="No." />

      <!-- 对象关系的 -->
      <template v-if="relationType === 'single'">
        <vxe-column title="关系名称" field="name" sortable align="left" min-width="200" :edit-render="{}">
          <template #default="{ row }">
            <span>{{ row.name }}</span>
          </template>
        </vxe-column>

        <vxe-column title="Tab名称" field="tabName" sortable align="center" min-width="120" :edit-render="{}">
          <template #default="{ row }">
            <span>{{ row.tabName }}</span>
          </template>
        </vxe-column>
        <vxe-column title="目标对象" field="targetModelCode" sortable align="center" min-width="120" :edit-render="{}">
          <template #default="{ row }">
            <el-tag v-if="row.targetModelCode" slot="reference" class="active-hover">{{
              renderObjectName(row.targetModelCode)
            }}</el-tag>
          </template>
        </vxe-column>
      </template>
      <template v-else>
        <vxe-column title="关系名称" field="relatedModelCode" sortable align="left" min-width="200" :edit-render="{}">
          <template #default="{ row }">
            <el-tag v-if="row.relatedModelCode" slot="reference" class="active-hover">{{
              renderObjectName(row.relatedModelCode)
            }}</el-tag>
          </template>
        </vxe-column>
      </template>
      <vxe-column title="描述" field="description" sortable align="center" min-width="80" :edit-render="{}">
        <template #default="{ row }">
          <span>{{ row.description }}</span>
        </template>
      </vxe-column>
      <vxe-column v-if="showCheckInBtn" title="操作" field="editable" align="center" width="140">
        <template #default="{ row, rowIndex }">
          <el-button type="text" class="danger-color" @click="onDeleteBtnClick(row, rowIndex)">移除</el-button>
          <el-button type="text" @click="onAddEditBtnClick(row)">编辑</el-button>
        </template>
      </vxe-column>
    </vxe-table>
    <add-edit-panel
      ref="addEditPanelRef"
      :source-model-code="form.modelCode"
      :relation-type="relationType"
      :object-data-map-info="objectDataMapInfo"
      :object-data-map="objectDataMap"
      :object-tree-data="objectTreeData"
      @add-success="onConfirmSuccess"
    />
  </el-dialog>
</template>

<script setup>
import { ref, shallowRef, computed } from 'vue'
import { Message, Loading, MessageBox } from 'element-ui'
import {
  objectCheckout,
  objectCheckin,
  objectCancelCheckout,
  queryObjectTree,
  getObjectAttrByBid
} from '@/views/system-manage/object-manage/api/index.js'
import { listRelation, getMultiRelatedByModelCode } from '@/views/system-manage/relation-manage/api/index'
import store from '@/store'
import AddEditPanel from './components/add-panel.vue'
import { treeTolist, deepClone } from '@/utils'
import { cloneDeep } from 'lodash'
import tableConfig from '@/config/table.config'

const emits = defineEmits(['check-status-change'])
const addEditPanelRef = shallowRef(null)
const visible = ref(false)
const relationType = ref('single') //  single  关系  multi  多对象关系
const TrTableRef = shallowRef(null)
const form = ref({})
const tableData = ref([])
const loading = ref(false)
const objectTreeData = ref([])
const objectDataMap = ref({})
const objectDataMapInfo = ref({})
const validRules = {
  name: [{ required: true, message: '关系名称必须填写' }],
  tabName: [{ required: true, message: 'Tab名称必须填写' }],
  targetModelCode: [{ required: true, message: '目标对象必须选择' }]
}
// 当前用户工号
const currentUserNo = computed(() => store.state.user?.user.employeeNo)
// 显示检入按钮
const showCheckInBtn = computed(() => {
  // 有锁的信息，且锁的人是自己，才显示检入按钮
  return form.value.lockInfo && form.value.checkoutBy === currentUserNo.value
})
const showCheckOutBtn = computed(() => {
  return !form.value.lockInfo
})

const show = (row, type = 'single') => {
  const _row = cloneDeep(row)

  delete _row.children
  delete _row._X_ROW_CHILD
  relationType.value = type
  form.value = _row
  visible.value = true
  getObjectAttrList()
}

const hide = () => {
  visible.value = false
}
const getObjectAttrList = async () => {
  loading.value = true
  let path = listRelation

  if (relationType.value === 'multi') {
    path = getMultiRelatedByModelCode
  }
  const { data } = await path(form.value.modelCode).finally(() => (loading.value = false))

  tableData.value = data || []

  getObjectBaseInfo()
}

const getObjectBaseInfo = async () => {
  loading.value = true
  const { data } = await getObjectAttrByBid(form.value.bid).finally(() => (loading.value = false))

  const attrList = data.attrList.map(item => {
    return {
      inherit: false,
      isShow: false,
      isRelationShow: false,
      ...item
    }
  })

  form.value = { ...form.value, ...data ,attrList }
}

const onDeleteBtnClick = (row, rowIndex) => {
  MessageBox.confirm('是否确认删除？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      // 删除tableData中的数据
      TrTableRef.value.remove(row)
      tableData.value.splice(rowIndex, 1)
    })
    .catch(() => {})
}

// 检出操作
const onCheckoutBtnClick = async () => {
  const loading = Loading.service({ target: '.relation-dialog', text: '正在检出...' })
  const { success, message } = await objectCheckout(form.value.bid).finally(() => loading.close())

  if (success) {
    Message.success(message)
    // form.value = { ...form.value, ...data }
    emits('check-status-change', true)
  } else {
    Message.warning(message)
  }
}
// 检入操作
const onCheckinBtnClick = async () => {
  // form.value.attrList = []
  // 格式化数据（后端需要传值）
  const data = tableData.value.map(item => {
    return {
      inherit: false,
      isShow: false,
      isRelationShow: false,
      ...item
    }
  })

  if (relationType.value === 'single') {
    form.value.relationList = data
  } else {
    form.value.multiRelatedList = data
  }

  // 表格校验
  const errMap = await TrTableRef.value.fullValidate().catch(errMap => errMap)

  if (errMap) return
  const loading = Loading.service({ target: '.relation-dialog', text: '正在提交...' })

  const { success, message } = await objectCheckin(form.value).finally(() => loading.close())

  if (success) {
    Message.success(message)
    emits('check-status-change', true)
    hide()
  } else {
    Message.warning(message)
  }
}
// 取消检出
const onCancelCheckinBtnClick = async () => {
  const loading = Loading.service({ target: '.relation-dialog', text: '正在取消检出...' })
  const { success, message } = await objectCancelCheckout(form.value.bid).finally(() => loading.close())

  if (success) {
    Message.success(message)
    emits('check-status-change', true)
    hide()
  } else {
    Message.warning(message)
  }
}
// 获取对象树数据
const getObjectTreeData = async () => {
  const { data } = await queryObjectTree()
  const list = treeTolist(data, 'children')

  // 递归过滤掉type==='RELATION'的数据
  objectTreeData.value = list.filter(item => item.type !== 'RELATION')
  list.forEach(item => {
    objectDataMap.value[item.modelCode] = item.name
    objectDataMapInfo.value[item.modelCode] = item
  })
}

getObjectTreeData()
const renderObjectName = modelCode => {
  return objectDataMap.value[modelCode] || modelCode
}

// 新增关系
const onAddEditBtnClick = row => {
  addEditPanelRef.value.show(row)
}

// 新增关系成功
function onConfirmSuccess(row = {}, isAdd = true) {
  console.log(deepClone(row))
  if (isAdd) {
    tableData.value.unshift(deepClone(row))
  } else {
    // 编辑的更新逻辑还需在处理一下
    const index = tableData.value.findIndex(
      item => item.targetModelCode === row.targetModelCode || item.relatedModelCode === row.relatedModelCode
    )

    tableData.value.splice(index, 1, deepClone(row))
  }
}

function activeRowMethod() {
  return showCheckInBtn.value
}

const onClose = () => {
  form.value = {}
  tableData.value = []
}

defineExpose({
  show,
  hide
})
</script>

<style scoped lang="scss">
.tools-box {
  padding: 10px 10px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  .btn-box {
    display: flex;
    align-items: center;
    cursor: pointer;
    color: var(--primary);
    padding: 4px;
    width: fit-content;
    .text {
      font-size: 14px;
      margin-right: 4px;
      vertical-align: -0.2em;
    }
    .tr-svg-icon {
      font-size: 16px;
      margin-right: 4px;
      vertical-align: -0.15em;
    }

    &:hover {
      background-color: #eaedec;
      border-radius: 4px;
    }
    &:active {
      background-color: #dee1e0;
    }
  }
}
::v-deep .inherit-row {
  .vxe-body--column {
    background-color: #ebeeef;
  }
}
</style>
