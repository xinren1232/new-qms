<template>
  <div class="container" :style="{ width: width + 'px' }">
    <tools
      :hidden-columns="columns"
      :options-map="dictMap"
      :field-map="fieldMap"
      @filter-change="onFilterChange"
      @add-row="onAddRow"
      @visible-column="onVisibleColumn"
      @column-group="onColumnGroup"
      @sort-change="onSortChange"
      @drag-end="onDragEnd"
      @delete-rows="onDeleteRows"
    >
      <template #tools><slot name="tools" /></template>
    </tools>

    <vxe-table
      ref="xTable"
      auto-resize
      border
      show-overflow
      :loading="loading"
      :data="tableData"
      :height="height"
      :column-config="{ resizable: true }"
      :row-config="{ isHover: true, height: 40,useKey:true,keyField:'bid' }"
      :edit-config="{ trigger: 'click', mode: 'cell', showIcon: false, beforeEditMethod: activeCellMethod }"
      :checkbox-config="{ highlight: true }"
      :row-class-name="rowClassName"
      :cell-class-name="cellClassName"
      :sort-config="{ multiple: true, chronological: false }"
      :tree-config="{}"
      :edit-rules="validRules"
      sync-resize
      @valid-error="validError"
      @cell-mouseenter="cellMouseEnter"
      @cell-mouseleave="cellMouseLeave"
      @edit-closed="onEditClosed"
    >
      <vxe-table-column type="checkbox" width="50" fixed="left" />
      <vxe-table-column width="55" fixed="left" title=" " align="center">
        <template #default="{ row }">
          <tr-svg-icon
            v-if="row._isHover"
            style="font-size: 28px; color: var(--primary); vertical-align: top"
            class="t-btn"
            icon-class="expand"
            @click="onRowDetailClick(row)"
          />
          <span v-else>{{ xTable.getRowSeq(row) }}</span>
        </template>
      </vxe-table-column>
      <vxe-table-column
        v-for="item in columns.filter(o => !!o[fieldMap.isHeader])"
        :key="item[fieldMap.value]"
        :field="item[fieldMap.value]"
        :title="item[fieldMap.label]"
        :min-width="calcColumnWidth(item)"
        :params="{ ...item, dictMap, superSelectMap, lifecycleData, objectInfo, extendData, fileUploadConfig }"
        align="left"
        :tree-node="item[fieldMap.value] === 'state_code'"
        :edit-render="{ name: calcRenderType(item, extendData.extendComponent) }"
      >
        <template #header="{ column }">
          <el-tooltip content="不可编辑">
            <tr-svg-icon
              v-if="item.disabled"
              style="vertical-align: -0.15em"
              icon-class="t-lock"
              class="mr-1 active-color"
            />
          </el-tooltip>
          <tr-svg-icon
            style="vertical-align: -0.2em"
            class="mr-1"
            :icon-class="calcIcon(item.type, item.multiple)"
          />
          <span class="truncate">{{ column.title }}</span>
        </template>
      </vxe-table-column>
    </vxe-table>
  </div>
</template>

<script setup>
import { nextTick, ref, shallowRef, computed, watch } from 'vue'
import XEUtils from 'xe-utils'
import { Message } from 'element-ui'
import TrSvgIcon from './components/tr-svg-icon'
import Tools from './components/tools.vue'
import { calcIcon, calcRenderType } from './utils/index.js'
import './renderer/index.js' // 注册渲染器
import { injectExtendComponent } from './extend/index.js' // 注册扩展组件

const emits = defineEmits(['row-detail-click', 'edit-closed'])

const xTable = shallowRef(null)
const props = defineProps({
  fieldList: {
    type: Array,
    default: () => []
  },
  data: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  height: {
    type: Number,
    default: 600
  },
  width: {
    type: Number,
    default: 600
  },
  extendData: {
    type: Object,
    default: () => ({}),
    command: '扩展数据,比如角色树data,对象树data'
  },
  columnRules: {
    type: Object,
    default: () => ({}),
    command: '表头列对应的校验规则'
  }
})

// 动态注入扩展组件
injectExtendComponent(props.extendData.extendComponent)
const dictMap = computed(() => props.extendData.dictMap)
const superSelectMap = computed(() => props.extendData.superSelectMap)
const objectInfo = computed(() => props.extendData.objectInfo)
const lifecycleData = computed(() => props.extendData.lifecycleData)
const fieldMap = computed(() => props.extendData.fieldMap)
const fileUploadConfig = computed(() => props.extendData.fileUploadConfig)
const validRules = computed(() => props.columnRules)

const columns = ref([])
// const validRules = ref({})
const isPassValidate = ref(false) // 当前是否存在 校验

watch(
  () => props.fieldList,
  () => {
    columns.value = props.fieldList
      .map((item, index) => ({ type: item.type, ...item.field.options, [fieldMap.value.isHeader]: true, sort: index }))
      .filter(item => !item.hidden)
    columns.value.forEach((item, index) => {
      if (index > 8) {
        item[fieldMap.value.isHeader] = false
      }
    })
    // columns.value = columns.value.filter(item => !!item[fieldMap.value.isHeader])
    // const rules = {}

    // columns.value.forEach(item => {
    //   if (item.required) {
    //     rules[item.name] = [{ required: true, message: `${item.label}不能为空`, trigger: 'blur' }]
    //   }
    // })
    // validRules.value = rules
  }
)
const cashData = ref([])

watch(
  () => props.data,
  () => {
    tableData.value = props.data
    cashData.value = JSON.parse(JSON.stringify(props.data))
  }
)

const calcColumnWidth = item => {
  if (item[fieldMap.value.value] === 'name') return 200
  switch (item.type) {
    case 'select':
      return 180
    case 'date':
      return 140
    case 'user':
      return 150
    case 'iframe':
      return 150
    case 'file':
      return 180
    case 'number':
      return 120
    case 'slider':
      return 160
    case 'rate':
      return 160
    case 'switch':
      return 120
    case 'text':
      return 200
    case 'picture-upload':
      return 200
    case 'file-upload':
      return 200
    case 'rich-editor':
      return 500
    case 'cascader':
      return 200
    case 'tree-select':
      return 200
    case 'checkbox':
      return 240
    case 'radio':
      return 240
    case 'object':
      return 150
    case 'role':
      return 150
    default:
      return 120
  }
}

const activeCellMethod = ({ column }) => {
  if (isPassValidate.value) {
    return false
  }

  return !column.params.disabled
}
const validError = ({ column, row }) => {
  isPassValidate.value = true

  xTable.value.setEditCell(row, column.field)
}
const tableData = ref([])

const rowClassName = ({ row }) => {
  if (row?.checked) {
    return 'selected-row'
  }
}
const cellClassName = ({ column }) => {
  if (column?.property === 'name') {
    return 'bolder'
  }
}

// const dropDown = () => {
//   nextTick(() => {
//     Sortable.create(xTable.value.$el.querySelector('.body--wrapper>.vxe-table--body tbody'), {
//       handle: '.drag-btn',
//       animation: 150,
//       easing: 'cubic-bezier(1, 0, 0, 1)',
//       ghostClass: 'sortable-ghost', // Class name for the drop placeholder
//       chosenClass: 'sortable-chosen', // Class name for the chosen item
//       dragClass: 'sortable-drag', // Class name for the dragging item
//       onEnd: ({ newIndex, oldIndex }) => {
//         console.log(newIndex, oldIndex)
//
//         const currRow = tableData.value.splice(oldIndex, 1)[0]
//
//         tableData.value.splice(newIndex, 0, currRow)
//       }
//     })
//   })
// }
// 工具栏事件
const onAddRow = async () => {
  const { row: newRow } = await xTable.value.insert({})

  await xTable.value.setActiveRow(newRow)
}

// 显示隐藏列
const onVisibleColumn = ({ col, visible }) => {
  col.isHeader = visible

  nextTick(() => {
    xTable.value.refreshColumn()
  })
}

// 按照属性分组

const groupFieldList = ref([])

const onColumnGroup = ({ field }) => {
  groupFieldList.value = [field]
  tableData.value = cashData.value

  const result = []

  XEUtils.each(XEUtils.groupBy(tableData.value, field), (childs, key) => {
    result.push({
      [field]: key,
      children: childs,
      ...field
    })
  })
  tableData.value = result
  const columns = xTable.value.getColumns()

  nextTick(() => {
    xTable.value.reloadColumn(columns)
  })
}

// 过滤 根据过滤条件过滤数据
const onFilterChange = ({ filterType, filterList }) => {
  if (!filterList.length) {
    tableData.value = cashData.value

    return
  }

  switch (filterType) {
    case 'and':
      tableData.value = cashData.value.filter(item => {
        return filterList.every(rule => {
          return calcFilterRules(rule, item)
        })
      })
      break
    case 'or':
      tableData.value = cashData.value.filter(item => {
        return filterList.some(rule => {
          return calcFilterRules(rule, item)
        })
      })
  }
}
// 解析匹配规则，eq、ne、gt、gte、lt、lte、like、between
const calcFilterRules = (rule, item) => {
  // 如果值为空，直接返回true
  if (!rule.value || rule.rule.indexOf('null') < -1) return true

  if (rule.rule === 'eq') return item[rule.label] == rule.value // 等于
  if (rule.rule === 'ne') return item[rule.label] != rule.value // 不等于
  if (rule.rule === 'gt') return item[rule.label] > rule.value // 大于
  if (rule.rule === 'gte') return item[rule.label] >= rule.value // 大于等于
  if (rule.rule === 'lt') return item[rule.label] < rule.value // 小于
  if (rule.rule === 'lte') return item[rule.label] <= rule.value // 小于等于
  if (rule.rule === 'like') return item[rule.label].indexOf(rule.value) > -1 // 包含
  if (rule.rule === 'between') return item[rule.label] >= rule.value[0] && item[rule.label] <= rule.value[1] // 区间
  if (rule.rule === 'isnull')
    return (
      !item[rule.name] ||
      (Array.isArray(item[rule.label]) && !item[rule.label].length) ||
      (typeof item[rule.label] === 'object' && !Object.keys(item[rule.label]).length)
    )
  if (rule.rule === 'notnull') {
    return (
      item[rule.label] &&
      Array.isArray(item[rule.label]) &&
      item[rule.label].length &&
      typeof item[rule.label] === 'object' &&
      Object.keys(item[rule.label]).length
    )
  }

  return true
}

// 排序
const onSortChange = sortList => {
  if (!sortList.length) {
    tableData.value = cashData.value

    return
  }
  // 去除无效的排序规则
  sortList = sortList.filter(item => item.rule)

  tableData.value = XEUtils.orderBy(
    cashData.value,
    sortList.map(item => [item.name, item.rule])
  )
  setTimeout(() => {
    xTable.value.reloadData(tableData.value)
    xTable.value.refreshColumn()
    xTable.value.recalculate()
  }, 200)
}

const cellMouseEnter = ({ row }) => {
  tableData.value.forEach(item => {
    item._isHover = false
  })
  row._isHover = true
  xTable.value.refreshColumn()
  if (isPassValidate.value) {
    isPassValidate.value = false
  }
}

const cellMouseLeave = ({ row }) => {
  row._isHover = false
  xTable.value.refreshColumn()
}

const onRowDetailClick = row => {
  emits('row-detail-click', row)
}

const setTableData = data => {
  tableData.value = data
  cashData.value = data
  xTable.value.reloadData(data)
  // const columns = xTable.value.getColumns()

  // columns.forEach(item => {
  //   setTimeout(() => {xTable.value.setEditCell(row, item.field)})
  // })
}
// const onFieldChange = (row,column) => {
//   if (!column?.field) return
//   setTimeout(() => xTable.value.setEditCell(row, column.field))
// }
const onEditClosed = async ({ row }) => {
  if (isPassValidate.value) {
    Message.warning('请先修正表格中的错误信息')

    return
  }
  const errMap = await xTable.value.validate(row).catch(errMap => errMap)

  if (errMap) {
    Message.warning('请先修正表格中的错误信息')

    return
  }
  emits('edit-closed', row)
}

const onDragEnd = data => {
  const columns = xTable.value.getColumns()
  // 根据拖拽后的顺序重新排序
  const newColumns = data
    .map(item => {
      return columns.find(col => col.field === item.name)
    })
    .filter(Boolean)

  newColumns.unshift(columns[0], columns[1])
  xTable.value.reloadColumn(newColumns)
}

const onDeleteRows = async () => {
  const selectRecords = xTable.value?.getCheckboxRecords() || []

  if (!selectRecords.length) {
    Message.warning('请先选择需要删除的数据')

    return
  }

  emits('delete-rows', selectRecords)
}

defineExpose({
  setTableData,
  xTable
})

</script>

<style scoped lang="scss">
@import './style/reset.scss';
</style>
