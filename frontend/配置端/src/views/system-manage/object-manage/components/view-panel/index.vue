<template>
  <el-dialog
    :visible.sync="visible"
    width="1000px"
    :close-on-click-modal="false"
    custom-class="views-dialog"
  >
    <div slot="title">
      <span class="el-dialog__title">视图配置</span>
      <el-tooltip content="点我去设计视图">
        <i class="el-icon-top-right view-page common-btn" @click="onJumpToView" />
      </el-tooltip>
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
      keep-source
      @edit-closed="onEditClosed"
    >
      <vxe-table-column type="seq" title="No." width="60" />
      <vxe-table-column title="视图名称" field="viewName" sortable align="left" min-width="150">
        <template #default="{row}">
          <el-tooltip content="点击我跳转到【视图设计】" :open-delay="600">
            <span class="common-btn active-hover" @click="onJumpToViewDesigner(row.viewBid)">{{ row.viewName }}</span>
          </el-tooltip>
        </template>
      </vxe-table-column>
      <vxe-table-column title="生命周期" field="lcStateCode" sortable align="center" min-width="140" :edit-render="{}">
        <template #default="{row}">
          <span>{{ renderStateLabel(row.lcStateCode) }}</span>
        </template>
        <template #edit="{row}">
          <el-select v-model="row.lcStateCode" placeholder="请选择生命周期状态" clearable>
            <el-option v-for="item in stateList" :key="item.code" :label="item.name" :value="item.code">{{ item.name }}</el-option>
          </el-select>
        </template>
      </vxe-table-column>
      <vxe-table-column title="角色" field="roleCode" sortable align="center" min-width="150" :edit-render="{}">
        <template #default="{row}">
          <span>{{ roleMapData[row.roleCode] }}</span>
        </template>
        <template #edit="{row}">
          <role-select v-model="row.roleCode" :multiple="false" :data="roleTreeData" :data-map="roleMapData" />
        </template>
      </vxe-table-column>
      <vxe-table-column title="优先级" field="priority" sortable align="center" min-width="100" :edit-render="{}">
        <template #default="{row}">
          <span>{{ row.priority }}</span>
        </template>
        <template #edit="{row}">
          <el-input-number v-model.trim="row.priority" :min="0" :max="999" />
        </template>
      </vxe-table-column>
      <vxe-table-column title="标签" field="tags" sortable align="center" min-width="200" :edit-render="{}">
        <template #default="{row}">
          <el-tag v-for="(item,index) in row.tags" :key="index" disable-transitions>{{ item }}</el-tag>
        </template>
        <template #edit="{row}">
          <el-select
            v-model="row.tags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="输入新增标签"
            no-data-text="输入新增标签"
          >
            <el-option
              v-for="item in tagOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </template>
      </vxe-table-column>
      <!--      <vxe-table-column title="操作" align="center" width="100" fixed="right">-->
      <!--        <template #default="{ row }" style="display: flex">-->
      <!--          <el-button v-if="!row.inherit" type="text" size="small" @click="onSubmit(row)">保存</el-button>-->
      <!--        </template>-->
      <!--      </vxe-table-column>-->
    </vxe-table>
  </el-dialog>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { flatTreeData } from '@/utils'
import { Message } from 'element-ui'
import RoleSelect from '@@/plm/role-select/role-select.vue'
import { getAllRoleTree } from '@/views/system-manage/role-manage/api'
import { getVersionDetail } from '@/views/system-manage/lifecycle-template/api'
import { objectBindView,queryObjectBindViews } from './api.js'
import { queryLifecycleTemplateForObject } from '@/views/system-manage/object-manage/components/lifecycle-template-panel/api.js'
import router from '@/router'

const visible = ref(false)
const tableData = ref([])
const TrTableRef = ref(null)
const currentObjectInfo = ref({})
const stateList = ref([])

// 角色树数据
const roleTreeData = ref([])
// 角色map数据
const roleMapData = ref({})
const roleMapInfo = ref({})


const validRules = computed(() => {
  return {
    // roleCode: [
    //   { required: true, message: '请选择角色', trigger: 'change' }
    // ],
    // lcStateCode: [
    //   { required: true, message: '请选择生命周期状态', trigger: 'change' }
    // ],
    operations: [
      { required: true, message: '请选择操作权限', trigger: 'change' }
    ]
  }
})

// 标签数据
const tagOptions = []
const activeCellMethod = ({ row }) => {
  // 继承的权限不可编辑
  return !row.inherit
}
const onEditClosed = async ({ row }) => {
  console.log(row,'-----')
  await nextTick()
  const changeData = await TrTableRef.value.getUpdateRecords()

  if (!changeData.length) {
    return
  }

  const errMap = await TrTableRef.value.validate(row).catch(errMap => errMap)

  if (errMap) {
    return
  }
  const { success,message } = await objectBindView(row)

  if (success) {
    Message.success(message)
  } else {
    Message.warning(message)
  }

}
const show = row => {
  currentObjectInfo.value = row
  visible.value = true
  queryObjectBindViewsList()
  getLifecycleTemplateForObject()
  getRoleTreeData()
}

const getRoleTreeData = async () => {
  const { data } = await getAllRoleTree()

  roleTreeData.value = data
  // bid 作为key,name 作为value
  roleMapData.value = flatTreeData(data,'children').reduce((acc, cur) => {
    acc[cur.bid] = cur.name

    return acc
  }, {})
  roleMapInfo.value = flatTreeData(data,'children').reduce((acc, cur) => {
    acc[cur.bid] = cur

    return acc
  }, {})
}

// 获取生命周期状态列表
const getLifecycleTemplateDetail = async (bid,version) => {

  const { success,data } = await getVersionDetail(bid,version)

  if (success) {
    stateList.value = data?.layouts.map(item => {
      if (item && item.shape === 'rect') return item?.data
    }).filter(Boolean)
  }
}

// 获取对象当前绑定的生命周期模板
const getLifecycleTemplateForObject = async () => {
  const { success,data } = await queryLifecycleTemplateForObject(currentObjectInfo.value.modelCode)

  if (success) {
    if (data) {
      const { lcTemplBid,lcTemplVersion } = data

      // 获取对象当前绑定的生命周期模板之后，再获取生命周期状态列表
      await getLifecycleTemplateDetail(lcTemplBid, lcTemplVersion)
    }
  }
}

const renderStateLabel = code => {
  const item = stateList.value.find(item => item.code === code)

  return item ? item.name : ''
}

const queryObjectBindViewsList = async () => {
  const { success,data } = await queryObjectBindViews(currentObjectInfo.value.modelCode)

  if (success) {
    tableData.value = data.sort((a, b) => a.inherit - b.inherit)
  }
}

const rowClassName = ({ row }) => {
  return row.inherit ? 'readonly-row' : ''
}
const onJumpToView = () => {
  const _router = router.resolve('/system-manage/view-manage')

  window.open(_router.href, '_blank')
}
const onJumpToViewDesigner = bid => {
  const _router = router.resolve(`/view-manage/action/designer/${bid}`)

  window.open(_router.href, '_blank')
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
.view-page{
color: var(--primary);
}
</style>
