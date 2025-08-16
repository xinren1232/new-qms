<template>
  <div class="drawer-box">
    <el-tabs v-model="activeName" type="card">
      <el-tab-pane label="节点信息" name="info">
        <tr-form v-model="form" :items="formItems" :show-buttons="false" />
      </el-tab-pane>
      <el-tab-pane label="关系行为" name="relation">
        <tr-form v-model="form" :items="relationForm" :show-buttons="false" />
        <div v-if="form.behaviorScope === 0" class="relation-box">
          <div class="tools-box">
            <div class="btn-box" @click="onAddBtnClick">
              <tr-svg-icon icon-class="add-list" /><span class="text">新增</span>
            </div>
          </div>
          <vxe-table
            ref="TrTableRef"
            :data="form.lifeCycTemObjRelList"
            border
            :height="330"
            show-overflow
            :row-config="{ height: 36 }"
            :seq-column="false"
            :edit-config="{ trigger: 'click', mode: 'cell', showIcon: false }"
            :column-config="{ resizable: true }"
            :edit-rules="validRules"
            keep-source
            @edit-closed="onEditClosed"
          >
            <vxe-table-column title="目标对象" field="targetObjBid" sortable min-width="100" :edit-render="{}">
              <template #edit="{ row }">
                <object-select
                  ref="objectSelectRef"
                  v-model="row.targetObjBid"
                  :data="objectTreeData"
                  :data-map="objectDataMap"
                  :multiple="false"
                  node-key="bid"
                  @confirm="onObjectSelectConfirm($event, row)"
                />
              </template>
              <template #default="{ row }">
                <span>{{ renderTargetObj(row.targetObjBid) }}</span>
              </template>
            </vxe-table-column>
            <vxe-table-column
              title="关系行为"
              field="targetObjRel"
              sortable
              align="center"
              min-width="100"
              :edit-render="{}"
            >
              <template #default="{ row }">
                <span>{{ renderRelationType(row.targetObjRel) }}</span>
              </template>
              <template #edit="{ row }">
                <el-select v-model="row.targetObjRel" clearable>
                  <el-option v-for="item in relationType" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </template>
            </vxe-table-column>

            <vxe-table-column title="操作" align="center" width="80">
              <template #default="{ row }">
                <el-button type="text" class="danger-color" size="small" @click="onDeleteBtnClick(row)">
                  删除
                </el-button>
              </template>
            </vxe-table-column>
          </vxe-table>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { activeCellState } from '../core/state'
import TrForm from '@@/feature/TrForm'
import TrDict from '@@/feature/TrDict'
import TrSelect from '@@/feature/TrSelect'
import ObjectSelect from '@@/plm/object-select/object-select.vue'
import { queryObjectTree } from '@/views/system-manage/object-manage/api'
import { flatTreeData } from '@/utils'

const props = defineProps({
  data: {
    type: Object,
    default: () => ({})
  },
  stateList: {
    type: Array,
    default: () => ([])
  }
})

const form = ref({})
const TrTableRef = ref(null)
// const tableData = ref([])

const relationType = [
  { label: '固定', value: 'fixed' },
  { label: '浮动', value: 'float' }
]

watch(
  () => form.value,
  () => {
    const { cell } = activeCellState.value

    cell.data = form.value
  },
  { deep: true }
)

watch(
  () => activeCellState.value,
  () => {
    if (!activeCellState.value?.type) {
      return
    }
    const { cell } = activeCellState.value

    form.value = cell?.data || {}
  },
  { immediate: true }
)

const activeName = ref('info')
const formItems = computed(() => {
  const _items = [
    { label: '节点名称', prop: 'name', span: 24, attrs: { disabled: true } },
    { label: '节点编号', prop: 'code', span: 24, attrs: { disabled: true } },
    {
      label: '节点分组',
      prop: 'groupCode',
      component: TrDict,
      span: 24,
      attrs: { disabled: true, type: 'LIFECYCLE_STATE_GROUPS' }
    },
    { label: '节点说明', prop: 'description', span: 24, attrs: { type: 'textarea', disabled: true } },
    // { label: '节点流程', prop: 'bindProcess', span: 24, attrs: { disabled: true } },
    {
      label: '是否发行',
      prop: 'published',
      span: 24,
      component: TrSelect,
      attrs: {
        data: [
          { label: '是', value: 1 },
          { label: '否', value: 0 }
        ]
      }
    },
    { label: '节点标签', prop: 'flag', span: 24, component: TrDict, attrs: { type: 'LIFECYCLE_STATE_CODE_TAG' } },
    {
      label: '排序',
      prop: 'sort',
      type: 'inputNumber',
      span: 24,
      attrs: {}
    }
  ]

  if (props.data.phaseState) {
    _items.push({
      label: '是否关键路径',
      prop: 'keyPathFlag',
      component: 'ElSwitch',
      span: 24
    })
  } else {
    // 只有非 [阶段] 的生命周期才可以设置 [阶段状态]
    _items.push({
      label: '阶段状态',
      prop: 'phaseStateCode',
      component: TrSelect,
      span: 24,
      attrs: {
        isKeyValue: true,
        filterable: true,
        labelField: 'name',
        valueField: 'code',
        data: props.stateList.map(item => ({ key: item.code, name: item.name, code: item.code }))
      },
      listeners: {
        change: (val) => {
          const targetNode = props.stateList.find(item => item.code === val)

          form.value.phaseState = targetNode
        }
      }
    })
  }

  return _items
})
const relationForm = computed(() => {
  return [
    { label: '关系行为', prop: 'behavior', span: 24, component: TrDict, attrs: { type: 'LC_RELATION_BEHAVIOR' } },
    {
      label: '作用于全局',
      prop: 'behaviorScope',
      span: 24,
      component: TrSelect,
      attrs: {
        data: [
          { label: '是', value: 1 },
          { label: '否', value: 0 }
        ]
      },
      listeners: {
        change: val => {
          form.value.behaviorScope = Number(val)
        }
      }
    }
  ]
})

// 对象信息数据
const objectTreeData = ref([])
const objectDataMap = ref({})

// 新增字典按钮
const onAddBtnClick = async () => {
  const record = {
    targetObjBid: '',
    targetObjName: '',
    targetObjRel: ''
  }
  const { row } = await TrTableRef.value.insert(record)

  await TrTableRef.value.setActiveRow(row)
}
// 删除按钮
const onDeleteBtnClick = row => {
  TrTableRef.value.remove(row)
}

// 失去焦点保存
const onEditClosed = async ({ row }) => {
  if (!row.targetObjBid) {
    TrTableRef.value.remove(row)
  }
  const errMap = await TrTableRef.value.validate(row).catch(errMap => errMap)

  if (errMap) {
    return
  }

  form.value.lifeCycTemObjRelList = TrTableRef.value.getTableData().tableData
  const { cell } = activeCellState.value

  cell.data.lifeCycTemObjRelList = form.value.lifeCycTemObjRelList
}

const validRules = {
  targetObjBid: [{ required: true, message: '目标对象不能为空' }],
  targetObjRel: [{ required: true, message: '关联行为不能为空' }]
}

const renderRelationType = val => {
  const item = relationType.find(item => item.value === val)

  return item?.label || ''
}
const renderTargetObj = val => {
  return objectDataMap.value?.[val] || ''
}
const onObjectSelectConfirm = (val, row) => {
  row.targetObjName = val[0]?.name
  row.targetModelCode = val[0]?.modelCode
}

const getObjectTreeData = async () => {
  const { data } = await queryObjectTree()

  objectTreeData.value = data
  objectDataMap.value = flatTreeData(objectTreeData.value, 'children').reduce((acc, cur) => {
    acc[cur.bid] = cur.name

    return acc
  }, {})
}

getObjectTreeData()
</script>

<style scoped lang="scss">
@import '../css/reset.scss';
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
</style>
