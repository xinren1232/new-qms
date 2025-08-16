<template>
  <el-popover placement="bottom" width="460" trigger="click" popper-class="filter-popover-box">
    <div class="list-box">
      <div class="title">设置筛选条件</div>
      <div class="filter-type-bpx">
        <span>符合以下</span>
        <el-select v-model="filterType">
          <el-option value="and" label="所有" />
          <el-option value="or" label="任一" />
        </el-select>
        <span>条件</span>
      </div>
    </div>
    <div class="fix-box">
      <div v-for="(f, index) in currentFilterList" :key="index" class="item">
        <div class="left">
          <el-select
            v-model="f[fieldMap.label]"
            class="filter-filed"
            filterable
            clearable
            @change="onFilterFieldChange($event, f)"
          >
            <el-option
              v-for="o in hiddenColumns"
              :key="o[fieldMap.value]"
              :label="o[fieldMap.label]"
              :value="o[fieldMap.value]"
            >
              <div style="display: flex; align-items: center">
                <tr-svg-icon class="t-btn" :icon-class="calcIcon(o.type, o.multiple)" style="font-size: 24px" />
                <span>{{ o[fieldMap.label] }}</span>
              </div>
            </el-option>
          </el-select>
        </div>
        <div class="right">
          <el-select v-model="f.rule" class="filter-rule" filterable clearable>
            <el-option v-for="o in filterRuleList" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>

          <!--下拉选择器渲染-->
          <el-select
            v-if="f.type === 'select'"
            v-model="f.value"
            class="filter-value"
            :multiple="f.multiple"
            filterable
            clearable
          >
            <el-option
              v-for="list in optionsMap[f.remoteDictType]"
              :key="list.keyCode"
              :label="list.zh"
              :value="list.keyCode"
            />
          </el-select>
          <!--日期渲染-->
          <el-date-picker
            v-else-if="f.type === 'date'"
            v-model="f.value"
            class="filter-value"
            type="date"
            format="yyyy-MM-dd"
            value-format="yyyy-MM-dd"
          />
          <!--人员渲染-->
          <template v-else-if="f.type === 'user'" class="filter-value">
            <div style="display: flex; align-items: center">
              <user-select v-model="f.value" style="width: 120px" />
            </div>
          </template>
          <el-input v-else v-model="f.value" class="filter-value" clearable />
          <el-button
            type="text"
            icon="el-icon-close"
            class="close-btn t-btn"
            @click="currentFilterList.splice(index, 1)"
          />
        </div>
      </div>
    </div>
    <div class="filter-box-footer">
      <el-button type="text" class="t-btn" icon="el-icon-plus" @click="addFilterRule">添加条件</el-button>
    </div>
    <div slot="reference" class="btn-box">
      <tr-svg-icon icon-class="t-filter" />
      <span class="text"><span v-if="filterNumber > 0" class="filter-rule-number">{{ filterNumber }}</span>筛选</span>
    </div>
  </el-popover>
</template>

<script setup>
import UserSelect from '../components/user-select'
import { ref, watch, computed } from 'vue'
import { calcIcon } from '../utils/index'

const emit = defineEmits(['filter-change'])

const props = defineProps({
  hiddenColumns: {
    type: Array,
    default: () => []
  },
  optionsMap: {
    type: Object,
    default: () => ({})
  },
  fieldMap: {
    type: Object,
    default: () => ({ label: 'label', value: 'name' })
  }
})
// 有效的筛选提交数量
const filterNumber = computed(() => {
  return currentFilterList.value.filter(item => item[props.fieldMap.label] && item.rule && item.value).length
})

// 筛选条件类型
const filterType = ref('and')

// 筛选规则
const filterRuleList = ref([
  { label: '等于', value: 'eq' },
  { label: '不等于', value: 'neq' },
  { label: '包含', value: 'like' },
  { label: '不包含', value: 'notlike' },
  { label: '大于', value: 'gt' },
  { label: '小于', value: 'lt' },
  { label: '大于等于', value: 'gte' },
  { label: '小于等于', value: 'lte' },
  { label: '为空', value: 'isnull' },
  { label: '不为空', value: 'notnull' }
])

const addFilterRule = () => {
  currentFilterList.value.push({
    name: '',
    rule: 'eq',
    value: '',
    type: ''
  })
}

// 当前筛选条件
const currentFilterList = ref([
  {
    innerName: props.hiddenColumns?.[0]?.[props.fieldMap.value],
    rule: filterRuleList.value?.[0]?.value,
    value: '',
    type: props.hiddenColumns?.[0]?.type
  }
])

// 过滤字段改变
const onFilterFieldChange = (innerName, item) => {
  props.hiddenColumns.forEach(col => {
    if (col[props.fieldMap.value] === innerName) {
      item.value = ''
      item.type = col.type
      item.remoteDictType = col?.remoteDictType ?? ''
    }
  })
}

// 监听筛选条件变化
watch(
  () => currentFilterList,
  list => {
    emit('filter-change', { filterType: filterType.value, filterList: list.value })
  },
  { deep: true }
)

// 监听筛选条件类型变化
watch(
  () => filterType,
  type => {
    emit('filter-change', { filterType: type.value, filterList: currentFilterList.value })
  },
  { deep: true }
)
</script>

<style scoped lang="scss">
.btn-box {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  cursor: pointer;
}
.filter-popover-box {
  padding: 0 !important;
  border: 1px solid #dee0e3;
  .list-box {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1px solid #dcdede;
    padding: 12px 15px;

    .title {
      font-size: 14px;
      font-weight: bold;
      color: #1d2129;
    }
    .filter-type-bpx {
      display: flex;
      align-items: center;
      .el-select {
        width: 86px;
        margin: 0 4px;
      }
    }
  }
  .fix-box {
    width: 100%;
    max-height: 300px;
    overflow-y: auto;
    padding: 10px 0;

    .item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 6px 10px;
      .left,
      .right {
        display: flex;
        align-items: center;
        .filter-filed {
          width: 120px;
        }
        .filter-rule {
          width: 100px;
          margin-right: 10px;
        }
        .filter-value {
          width: 140px;
        }
        .tr-svg-icon {
          font-size: 23px;
        }
        .field-title {
          padding: 4px 0;
          width: 100px;
          text-overflow: ellipsis;
          overflow: hidden;
          white-space: nowrap;
        }
      }
      .close-btn {
        font-size: 14px;
        margin-left: 6px;
        color: #727477;
      }
    }
  }
  .filter-box-footer {
    padding: 15px;
    .t-btn {
      color: #535454;
    }
  }
}
.filter-rule-number {
  color: #409eff;
  padding: 0 2px;
  font-weight: bolder;
}
</style>
