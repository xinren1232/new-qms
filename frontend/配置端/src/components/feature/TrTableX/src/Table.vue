<template>
  <div class="tr-table margin-bottom" :class="{ draggable: canDrag }">
    <vxe-table
      ref="tableRef"
      v-bind="$attrs"
      :data="tableData"
      border
      row-key
      highlight-hover-row
      :edit-config="editConfig"
      :loading="loading"
      :max-height="maxHeight"
      v-on="$listeners"
    >
      <slot name="before" />

      <template v-if="columns.length">
        <!-- 注意：使用 table_format_list 传参的原因是jsx里面取不到用短横线连接转化为驼峰的形式 -->
        <table-column
          v-for="column of columns"
          :key="column.prop"
          :table_format_list="tableFormatList"
          :format="getLabel"
          v-bind="column"
        />
      </template>
      <template v-else>
        <template v-if="sortable">
          <table-column
            key="sortable"
            v-bind="SortColumn"
          />
        </template>
        <template v-if="seqColumn">
          <table-column
            key="seq-column"
            v-bind="SeqIndexColumn"
          />
        </template>

        <slot />
      </template>

      <slot name="after" />

      <template #empty>
        <tr-no-data type="common" />
      </template>
    </vxe-table>

    <el-pagination
      v-if="pagerVisible"
      class="flex justify-center mt-2"
      v-bind="paginationConfig"
      @size-change="onSizeChange"
      @current-change="onPageChange"
    />
  </div>
</template>

<script>
import { onBeforeUnmount, ref } from 'vue'

import { useSortable } from '@/composables/sortable.js'
import formatTable from '@/utils/mixins/format-table.js'

import { useEditMode } from '../composables/edit-mode.js'
import { usePagerVisible } from '../composables/pagination.js'
import { useSeqColumn } from '../composables/seq-column.js'
import { useSetSortColumn } from '../composables/sortable.js'
import { useAsyncData } from '../composables/table-data.js'
import SeqIndexColumn from './SeqColumn'
import SortColumn from './SortColumn'

import EventBus from '@/utils/event.js'

export default {
  name: 'TrTable',
  components: {
    TableColumn: () => import('./Column'),
    TrNoData: () => import('../../TrNoData')
  },
  mixins: [formatTable],
  inject: {
    elForm: {
      default: () => ({})
    }
  },
  inheritAttrs: false,
  model: { prop: 'data' },
  props: {
    // 触发编辑模式的操作类型, 可选值: click, dblclick, contextmenu
    trigger: {
      type: String,
      default: ''
    },
    // 控制是否可以切换成编辑模式
    activeMethod: {
      type: Function,
      default: () => true
    },
    // 表格数据
    data: {
      type: Array,
      default: () => []
    },
    // 表格表列
    columns: {
      type: Array,
      default: () => []
    },
    // 是否显示分页器，如果设置为 auto, 那么当数据量大于 ${pageSize} 的时候会出现, ${pageSize} 可通过 ${pagination} 设置，不设置则默认为 10
    showPager: {
      type: [Boolean, String],
      default: false
    },
    // 请求数据时需要携带的参数
    params: {
      type: Object,
      default: () => ({})
    },
    // 外部可自定义传入对分页器的属性
    pagination: {
      type: Object,
      default: () => ({ pageSizes: [20,50,100] })
    },
    // 在组件初始化时立即请求数据
    immediate: {
      type: Boolean,
      default: false
    },
    // 自定义请求的函数
    httpRequest: {
      type: [Promise, Function],
      default: null
    },
    // 是否开启拖拽排序列, 开启后会在表列前增加一列
    sortable: {
      type: Boolean,
      default: false
    },
    // 在开启拖拽列后，控制拖拽列是否可用
    draggable: {
      type: [String, Boolean, Number],
      default: true
    },
    // 是否开启序号列
    seqColumn: {
      type: Boolean,
      default: true
    },
    maxHeight: {
      type: [String, Number],
      default: null
    }
  },
  emits: ['input', 'cell-click'],
  setup(props, { emit }) {
    const isTableX = true // 是否是tablex组件的表格， 别删，在mixins里面有判断是tablex组件才重新赋值tableData，不然不重新渲染数据
    const tableRef = ref(null)
    const editConfig = useEditMode(props)
    const { pagerVisible, paginationConfig } = usePagerVisible(props)
    const { loading, onPageChange, onSizeChange, query, tableData } = useAsyncData({
      props,
      emit,
      config: paginationConfig
    })

    props.seqColumn && useSeqColumn(props)

    if (props.sortable) {
      useSetSortColumn(props)
      useSortable({
        props,
        root: tableRef,
        selector: '.body--wrapper>.vxe-table--body tbody'
      })
    }

    const resizeTableWidth = () => {
      tableRef.value && tableRef.value.recalculate()
    }

    EventBus.$on('sidebar-resize', resizeTableWidth)

    onBeforeUnmount(() => {
      EventBus.$off('sidebar-resize', resizeTableWidth)
    })

    return {
      SeqIndexColumn,
      SortColumn,
      query,
      isTableX,
      editConfig,
      tableRef,
      pagerVisible,
      paginationConfig,
      onSizeChange,
      onPageChange,
      tableData,
      loading
    }
  },
  computed: {
    // modelValue: {
    //   get({ data }) {
    //     return data
    //   },
    //   set(list) {
    //     this.$emit('input', list)
    //   }
    // },
    canDrag({ draggable, elForm }) {
      return elForm ? !elForm.disabled : draggable
    }
  }
}
</script>

<style>
/* 设置拖拽列是否可用 */
.vxe-cell .allow-sort {
  pointer-events: none;
  color: #ccc;
}

.draggable .vxe-cell .allow-sort {
  pointer-events: all;
  color: unset;
}
</style>
