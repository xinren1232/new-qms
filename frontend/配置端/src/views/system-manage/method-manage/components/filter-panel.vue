<template>
  <el-popover
    v-model="visible"
    placement="bottom"
    width="400"
    trigger="click"
    popper-class="filter-popover-box"
  >
    <div class="list-box">
      <div class="title">设置筛选条件</div>
    </div>
    <tr-form ref="TrFormRef" v-model="form" class="form-box" :items="formItems" :show-buttons="false" />
    <div class="footer-box flex justify-end">
      <el-button @click="onReset">重置</el-button>
      <el-button type="primary" @click="onFilter">确定</el-button>
    </div>
    <div slot="reference" class="btn-box"><tr-svg-icon icon-class="t-filter" /><span class="text"><span v-if="filterNumber>0" class="filter-rule-number">{{ filterNumber }}</span>筛选</span></div>
  </el-popover>
</template>

<script setup>
import { ref } from 'vue'
import TrDict from '@@/feature/TrDict'
import TrForm from '@@/feature/TrForm'
import TrSelect from '@@/feature/TrSelect'
import { STATUS } from '@/config/state.config'
import useFilterNumber from '@/hooks/useFilterNumber'

const emit = defineEmits(['filter-change'])

const form = ref({})
const TrFormRef = ref(null)
const visible = ref(false)

const formItems = ref(
  [
    { label: '方法名称', prop: 'name', labelWidth: '80px', span: 24, attrs: { placeholder: '请输入', clearable: true } },
    { label: '方法编码', prop: 'code', labelWidth: '80px', span: 24, attrs: { placeholder: '请输入', clearable: true } },
    { label: '方法分组', prop: 'methodGroup', labelWidth: '80px', component: TrDict, span: 24, attrs: { type: 'METHOD_GROUP' } },
    { label: '语言类型', prop: 'languageType', labelWidth: '80px', component: TrDict, span: 24, attrs: { type: 'LANGUAGE_TYPE' } },
    { label: '状态', prop: 'enableFlag', labelWidth: '80px', component: TrSelect, span: 24, attrs: { data: STATUS,placeholder: '请选择',clearable: true } }
  ]
)

const { filterNumber,setFilterNumber } = useFilterNumber()
// 重置
const onReset = () => {
  form.value = {}
  onFilter()
}
// 筛选
const onFilter = () => {
  emit('filter-change', form.value)
  setFilterNumber(form.value)

  visible.value = false
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
  }
  .form-box{
    padding: 15px;
    margin-bottom: 0;
  }
  .footer-box{
    padding: 0 15px;
  }
}
.filter-rule-number{
  color: #409EFF;
  padding: 0 2px;
  font-weight: bolder;
}
</style>
