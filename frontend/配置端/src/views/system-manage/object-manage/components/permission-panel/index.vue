<template>
  <el-dialog
    :visible.sync="visible"
    title="权限配置"
    width="1000px"
    :close-on-click-modal="false"
    custom-class="permission-dialog"
    @close="onCancel"
  >
    <div class="tools-box">
      <div class="btn-box" @click="onAddBtnClick"><tr-svg-icon icon-class="add-list" /><span class="text">新增</span></div>
      <div class="btn-box" @click="onSaveBtnClick"><tr-svg-icon icon-class="t-save" /><span class="text">保存</span></div>
      <el-divider direction="vertical" />
      <div class="btn-box" @click="onDistributeBtnClick('app')"><span class="text">下发到应用</span></div>
      <div class="btn-box" @click="onDistributeBtnClick('object')"><span class="text">下发到子对象</span></div>
      <!--      <el-dropdown class="m-l-15px" split-button @command="onDistributeBtnClick">-->
      <!--        权限下发-->
      <!--        <el-dropdown-menu slot="dropdown">-->
      <!--          <el-dropdown-item class="line-height-44px" command="app">到空间应用</el-dropdown-item>-->
      <!--          <el-dropdown-item class="line-height-44px" command="object">到对象子类</el-dropdown-item>-->
      <!--        </el-dropdown-menu>-->
      <!--      </el-dropdown>-->
    </div>
    <vxe-table
      ref="TrTableRef"
      class="permission-table"
      :auto-size="false"
      :data="tableData"
      height="400px"
      :edit-config="{trigger: 'click', mode: 'cell',showIcon: false,activeMethod: activeCellMethod}"
      :edit-rules="validRules"
      show-overflow
      border
      :row-class-name="rowClassName"
      :row-config="{height: tableConfig.rowHeight}"
      keep-source
    >

      <vxe-table-column title="角色" field="roleCode" sortable align="center" min-width="150" :edit-render="{}">
        <template #default="{row}">
          <el-tag disable-transitions>{{ renderRoleLabel(row.roleCode) }}</el-tag>
        </template>
        <template #edit="{row}">
          <TrTreeSelect
            v-model="row.roleCode"
            :row="row"
            title="角色选择"
            :source-data="roleTreeData"
            effect="edit"
            :columns="[{ field: 'code','title': '角色编码' },{ field: 'enableFlag','title': '状态' }]"
            value-field="code"
            key-field="bid"
            :multiple="false"
          />
          <!--          <role-select v-model="row.roleCode" :multiple="false" :data="roleTreeData" :data-map="roleMapData" />-->
        </template>
      </vxe-table-column>
      <vxe-table-column title="类型" field="originType" sortable align="center" width="100">
        <template #default="{row}">
          <el-tag :type="row.originType==='object'?'danger':''">{{ row.originType ==='object'?'继承':'自定义' }}</el-tag>
        </template>
      </vxe-table-column>
      <vxe-table-column title="操作权限" field="operatorList" sortable align="center" min-width="260" :edit-render="{}">
        <template #default="{row}">
          <el-tag v-for="o in row.operatorList" :key="o" disable-transitions>{{ renderOperationLabel(o) }}</el-tag>
        </template>
        <template #edit="{row}">
          <el-select v-model="row.operatorList" placeholder="请选择操作权限" multiple>
            <el-option v-for="item in operationList" :key="item.code" :label="item.name" :value="item.code">{{ item.name }}</el-option>
          </el-select>
        </template>
      </vxe-table-column>
      <vxe-table-column title="操作" align="center" width="100" fixed="right">
        <template #default="{ row }" style="display: flex">
          <el-button v-if="row.originType ==='custom'" type="text" size="small" class="danger-color" @click="onDeleteRow(row)">删除</el-button>
        </template>
      </vxe-table-column>
    </vxe-table>
  </el-dialog>
</template>

<script setup>
import { Message } from 'element-ui'
import tableConfig from '@/config/table.config'
import TrTreeSelect from '@@/plm/tree-selector/index.vue'
import { roleTreeData,queryRoleTreeData } from '@@/feature/TrMultiTable/core/dataTier'
import { addUpdateObjectPermission,queryObjectBindOperationPermission,deletePermission,distributeObjectPermission,distributeAppPermission } from './api.js'
import { PERMISSION_LIST } from '@/views/system-manage/object-manage/components/permission-panel/config'

const visible = ref(false)
const tableData = ref([])
const TrTableRef = ref(null)
const currentObjectInfo = ref({})
const permissionInfo = ref({})
// 操作权限列表
const operationList = ref(PERMISSION_LIST)

queryRoleTreeData()

const validRules = computed(() => {
  return {
    roleCode: [
      { required: true, message: '请选择角色', trigger: 'change' }
    ],
    operatorList: [
      { required: true, message: '请选择操作权限', trigger: 'change' }
    ]
  }
})

const activeCellMethod = ({ row }) => {
  // 继承的权限不可编辑
  if (row.originType === 'object') {
    Message.warning('继承的权限不可编辑')

    return false
  }

  return true
}

const onAddBtnClick = async () => {
  const record = { roleCode: '',operatorList: [] }
  const { row } = await TrTableRef.value.insert(record)

  await TrTableRef.value.setActiveRow(row)
}

const onSaveBtnClick = async () => {
  let tableData = TrTableRef.value.getTableData().tableData
  // 校验
  const valid = await TrTableRef.value.validate(true)

  if (valid) {
    return Message.error('请填写完整信息')
  }

  if (tableData.length === 0) {
    const { success,message } = await deletePermission(currentObjectInfo.value.modelCode,permissionInfo.value.permissionBid)

    if (!success) return Message.error(message)
    Message.success('保存成功')
    await queryObjectBindOperationPermissionList()

    return
  }
  tableData = tableData.filter(item => {
    return item.originType === 'custom' || item.originType === null
  })
  const params = {
    modelCode: currentObjectInfo.value.modelCode,
    permissionBid: permissionInfo.value?.permissionBid,
    appPermissionOperationList: tableData?.map(item => {
      return {
        roleCode: item.roleCode,
        operatorList: item.operatorList
      }
    })
  }

  const { success,message } = await addUpdateObjectPermission(params)

  if (!success) return Message.error(message)
  Message.success('保存成功')
  await queryObjectBindOperationPermissionList()
}

const onCancel = () => {
  visible.value = false
  tableData.value = []
  permissionInfo.value = {}
}
const show = row => {
  currentObjectInfo.value = row
  queryObjectBindOperationPermissionList()
  visible.value = true
}

// 移除行
const onDeleteRow = row => {
  TrTableRef.value.remove(row)
}


const renderRoleLabel = roleCode => {
  return roleTreeData.value.map[roleCode]?.name || roleCode
}


const renderOperationLabel = code => {
  const item = operationList.value.find(item => item.code === code)

  return item ? item.name : ''
}

const queryObjectBindOperationPermissionList = async () => {
  const { success,data } = await queryObjectBindOperationPermission(currentObjectInfo.value.modelCode)

  if (!data) return
  if (!success) return Message.error(data)
  permissionInfo.value = data
  data?.permissionOperationList.forEach(item => item.originType = 'custom')
  data?.objPermissionOperationList.forEach(item => item.originType = 'object')
  tableData.value = [...data.permissionOperationList,...data.objPermissionOperationList] ?? []
  // 对operatorList去重
  tableData.value.forEach(item => {
    item.operatorList = Array.from(new Set(item.operatorList))
  })
}

const rowClassName = ({ row }) => {
  return row.inherit ? 'readonly-row' : ''
}

const onDistributeBtnClick = async (type) => {
  if (type === 'app') {
    const { success,message } = await distributeAppPermission(currentObjectInfo.value.modelCode)

    if (!success) return Message.error(message)
    Message.success('下发成功')

    return
  }
  const { success,message } = await distributeObjectPermission(currentObjectInfo.value.modelCode)

  if (!success) return Message.error(message)
  Message.success('下发成功')
}

defineExpose({
  show
})
</script>

<style scoped lang="scss">
.tools-box{
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
    .text{
      font-size: 14px;
      margin-right: 4px;
      vertical-align: -0.2em;
    }
    .tr-svg-icon{
      font-size: 16px;
      margin-right: 4px;
      vertical-align: -0.15em;
    }

    &:hover {
      background-color: #EAEDEC;
      border-radius: 4px;
    }
    &:active {
      background-color: #dee1e0;
    }
  }
}
::v-deep .permission-table .vxe-body--row.readonly-row{
  background-color: #f5f5f5;
  cursor: no-drop;
}
::v-deep .el-select{
  z-index: 100;
}
</style>
