<template>
  <el-popover
    placement="bottom"
    width="420"
    trigger="click"
    popper-class="filter-popover-box"
  >
    <div class="list-box">
      <div class="title">设置排序条件</div>
    </div>
    <div class="fix-box">
      <div v-for="(f,index) in currentSortList" :key="index" class="item">
        <div class="left">
          <el-select v-model="f.name" class="filter-filed" filterable clearable @change="onSortFieldChange($event,f)">
            <el-option v-for="o in hiddenColumns" :key="o[fieldMap.value]" :label="o[fieldMap.label]" :value="o[fieldMap.value]">
              <div style="display: flex;align-items: center">
                <tr-svg-icon class="t-btn" :icon-class="calcIcon(o.type,o.multiple)" style="font-size: 24px" />
                <span>{{ o[fieldMap.label] }}</span>
              </div>
            </el-option>
          </el-select>
        </div>
        <div class="right">
          <el-radio-group v-if="f.type==='date'|| f.type === 'number'" v-model="f.rule" size="small">
            <el-radio-button label="asc">1 - 9</el-radio-button>
            <el-radio-button label="desc">9 - 1</el-radio-button>
          </el-radio-group>
          <el-radio-group v-else v-model="f.rule" size="small">
            <el-radio-button label="asc">A - Z</el-radio-button>
            <el-radio-button label="desc">Z - A</el-radio-button>
          </el-radio-group>
          <el-button type="text" icon="el-icon-close" class="close-btn t-btn" @click="currentSortList.splice(index,1)" />
        </div>
      </div>
    </div>
    <div class="filter-box-footer">
      <el-button type="text" class="t-btn" icon="el-icon-plus" @click="addSortRule">添加条件</el-button>
    </div>
    <div slot="reference" class="btn-box"><tr-svg-icon icon-class="t-sort" /><span class="text"><span v-if="validSortRuleCount>0" class="sort-rule-number">{{ validSortRuleCount }}</span>排序</span></div>
  </el-popover>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { calcIcon } from '../utils'

const emit = defineEmits(['sort-change'])

const props = defineProps({
  hiddenColumns: {
    type: Array,
    default: () => []
  },
  fieldMap: {
    type: Object,
    default: () => ({ label: 'label',value: 'name' })
  }
})
// 排序规则
const currentSortList = ref([])

watch(() => currentSortList.value, (val) => {
  console.log(val)
  emit('sort-change', val)
},
{ deep: true }
)

// 有效的排序规则个数
const validSortRuleCount = computed(() => {
  return currentSortList.value.filter((o) => o && o.rule).length
})

// 排序字段改变
const onSortFieldChange = (value, item) => {
  const column = props.hiddenColumns.find((o) => o[props.fieldMap.value] === value)

  item.type = column.type
}

// 添加排序规则
const addSortRule = () => {
  currentSortList.value.push({
    [props.fieldMap.value]: '',
    type: '',
    rule: ''
  })
}
</script>

<style scoped lang="scss">
.btn-box{
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  cursor: pointer
}
.filter-popover-box{
  padding: 0 !important;
  border: 1px solid #dee0e3;
  .list-box{
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1px solid #DCDEDE;
    padding: 12px 15px;

    .title{
      font-size: 14px;
      font-weight: bold;
      color: #1d2129;
    }
    .filter-type-bpx{
      display: flex;
      align-items: center;
      .el-select{
        width: 86px;
        margin: 0 4px;
      }
    }
  }
  .fix-box{
    width: 100%;
    max-height: 300px;
    overflow-y: auto;
    padding: 10px 0;

    .item{
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 6px 10px;
      .left,.right{
        display: flex;
        align-items: center;
        .filter-filed{
          width: 200px;
        }
        .filter-value{
          width: 140px;
        }
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
      .close-btn{
        font-size: 14px;
        margin-left: 6px;
        color: #727477;
      }
    }
    ::v-deep .el-radio-button__inner{
      font-size: 14px;
      padding: 7px 15px;
    }
  }
  .filter-box-footer{
    padding: 15px;
    .t-btn{
      color: #535454;
    }
  }

}
.sort-rule-number{
  color: #409EFF;
  padding: 0 2px;
  font-weight: bolder;
}
</style>
