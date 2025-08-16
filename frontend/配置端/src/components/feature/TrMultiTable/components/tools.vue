<template>
  <div class="tools-box">
    <div class="btn-box" @click="addRow"><tr-svg-icon icon-class="add-list" /><span class="text">添加一条</span></div>
    <el-divider direction="vertical" />
    <el-popover
      placement="bottom"
      width="240"
      trigger="click"
      popper-class="popover-box"
    >
      <div class="list-box">
        <div class="title">字段配置</div>
        <div class="fix-box">
          <draggable ref="draggableRef" v-model="columns" chosen-class="chosen" handle=".cursor-move" animation="300" @end="onDragEnd">
            <transition-group>
              <div v-for="col in columns" :key="col[fieldMap.value]" class="item">
                <div class="left">
                  <tr-svg-icon class="t-btn cursor-move" icon-class="t-drag" />
                  <tr-svg-icon class="t-btn" :icon-class="calcIcon(col.type,col.multiple)" :class="col.isHeader?'':'is-hidden'" />
                  <span class="field-title" :class="col.isHeader?'':'is-hidden'">{{ col.label }}</span>
                </div>
                <div class="right">
                  <tr-svg-icon v-if="col.isHeader" class="t-btn" icon-class="eye-open" @click="onVisible(col,false)" />
                  <tr-svg-icon v-else class="t-btn" icon-class="eye-close" @click="onVisible(col,true)" />
                  <!--                  <tr-svg-icon class="t-btn" icon-class="handler-more" />-->
                </div>
              </div>
            </transition-group>
          </draggable>

        </div>
        <!--        <div class="fix-box">-->
        <!--          <div v-for="col in hiddenColumns" :key="col.name" class="item">-->
        <!--            <div class="left">-->
        <!--              <tr-svg-icon class="t-btn cursor-move" icon-class="t-drag" />-->
        <!--              <tr-svg-icon class="t-btn" :icon-class="calcIcon(col.type,col.field.options.multiple)" :class="col.isHeader?'':'is-hidden'" />-->
        <!--              <span class="field-title" :class="col.isHeader?'':'is-hidden'">{{ col.field.options.label }}</span>-->
        <!--            </div>-->
        <!--            <div class="right">-->
        <!--              <tr-svg-icon v-if="col.isHeader" class="t-btn" icon-class="eye-open" @click="onVisible(col,false)" />-->
        <!--              <tr-svg-icon v-else class="t-btn" icon-class="eye-close" @click="onVisible(col,true)" />-->
        <!--              <tr-svg-icon class="t-btn" icon-class="handler-more" />-->
        <!--            </div>-->
        <!--          </div>-->
        <!--        </div>-->
      </div>
      <div slot="reference" class="btn-box"><tr-svg-icon icon-class="t-setting" /><span class="text">字段配置</span></div>
    </el-popover>
    <!-- 动态过滤组件 -->
    <filter-panel
      class="btn-box"
      :hidden-columns="hiddenColumns"
      :options-map="optionsMap"
      :field-map="fieldMap"
      @filter-change="onFilterChange"
    />
    <!--    <div class="btn-box" @click="onGroup"><tr-svg-icon icon-class="t-group" /><span class="text">分组</span></div>-->
    <sort-panel
      class="btn-box"
      :hidden-columns="hiddenColumns"
      :field-map="fieldMap"
      @sort-change="onSortChange"
    />
    <el-divider direction="vertical" />
    <div class="btn-box danger-color" @click="onDelete"><tr-svg-icon icon-class="delete" /><span class="text">删除</span></div>
    <!--    <div class="btn-box"><tr-svg-icon icon-class="t-form" /><span class="text">生成表单</span></div>-->
    <slot name="tools" />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import FilterPanel from './filter-panel.vue'
import SortPanel from './sort-panel.vue'
import { calcIcon } from '../utils'
import draggable from 'vuedraggable'

const emit = defineEmits(['add-row', 'delete-rows','visible-column', 'column-group', 'filter-change','drag-end','sort-change'])

const props = defineProps({
  hiddenColumns: {
    type: Array,
    default: () => ([])
  },
  optionsMap: {
    type: Object,
    default: () => ({})
  },
  fieldMap: {
    type: Object,
    default: () => ({ label: 'label',value: 'name' })
  }
}
)
const columns = ref([])
const draggableRef = ref(null)

watch(() => props.hiddenColumns, (val) => {
  columns.value = val
})

const addRow = () => {
  emit('add-row')
}

// 控制列的显示隐藏
const onVisible = (col, visible) => {
  emit('visible-column', { col, visible })
}

// 过滤条件改变
const onFilterChange = (filter) => {
  emit('filter-change', filter)
}

// 分组
// eslint-disable-next-line no-unused-vars
const onGroup = (col) => {
  emit('column-group', { col, field: 'state_code' })
}

// 表头拖拽结束
const onDragEnd = () => {
  emit('drag-end', draggableRef.value.value)
}
const onDelete = () => {
  emit('delete-rows')
}
// 排序
const onSortChange = (sort) => {
  emit('sort-change', sort)
}
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
    margin-right: 4px;
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

</style>

<style lang="scss">

.popover-box{
  padding: 0 !important;
  border: 1px solid #dee0e3;
  .list-box{
    width: 100%;
    .title{
      padding: 12px 15px;
      font-size: 14px;
      font-weight: bold;
      color: #1d2129;
      border-bottom: 1px solid #DCDEDE;
    }
    .fix-box{
      width: 100%;
      max-height: 300px;
      overflow-y: auto;
      padding: 4px 10px;
      border-bottom: 1px solid #DCDEDE;

      .item{
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 5px 0;
        .left,.right{
          display: flex;
          align-items: center;
          .tr-svg-icon{
            font-size: 23px;
          }
          .field-title{
            padding: 4px 0;
            width: 100px;
            text-overflow: ellipsis;
            overflow: hidden;
            white-space: nowrap;
          }
        }
        .is-hidden{
          color: #BFC5C8 !important;
        }
        &:hover{
          background-color: #ECEEEE;
          border-radius: 4px;
        }
      }
    }
    .chosen {
      border: solid 2px var(--primary-3) !important;
    }
  }
}

</style>
