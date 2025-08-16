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
    <el-form ref="TrFormRef" v-model="form" class="form-box" label-width="100px">
      <el-form-item label="对象名称"><el-input v-model="form.name" clearable /></el-form-item>
      <el-form-item label="对象编码"><el-input v-model="form.modelCode" clearable /></el-form-item>
      <el-form-item label="状态">
        <el-select v-model="form[STATUS_FIELD]" clearable>
          <el-option v-for="s in STATUS" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
      </el-form-item>
    </el-form>
    <div class="footer-box flex justify-end">
      <el-button @click="onReset">重置</el-button>
      <el-button type="primary" @click="onFilter">确定</el-button>
    </div>
    <div slot="reference" class="btn-box"><tr-svg-icon icon-class="t-filter" /><span class="text"><span v-if="filterNumber>0" class="filter-rule-number">{{ filterNumber }}</span>筛选</span></div>
  </el-popover>
</template>

<script setup>
import { ref } from 'vue'
import { STATUS, STATUS_FIELD } from '@/config/state.config.js'

const emit = defineEmits(['filter-change'])

const form = ref({})
const TrFormRef = ref(null)
const visible = ref(false)

// 有效的筛选提交数量
const filterNumber = ref(0)
// 计算筛选条件
const calcFilterNumber = () => {
  let number = 0

  for (const key in form.value) {
    if (form.value[key] || form.value[key] === 0) {
      number++
    }
  }

  filterNumber.value = number
}
// 重置
const onReset = () => {
  form.value = {}
  onFilter()
}
// 筛选
const onFilter = () => {
  calcFilterNumber()
  emit('filter-change', form.value)
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
