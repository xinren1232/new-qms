<script setup>
import { Message } from 'element-ui'
import TrForm from '@@/feature/TrForm/index.vue'
import TrSelect from '@@/feature/TrSelect/index.js'
import { copyTypeList, copyEventList, copyModeList } from '../config.js'
import { integerNumber } from '@/utils/regexp/index.js'

const props = defineProps({
  formData: {
    type: Object,
    default: () => ({})
  }
})

const formRef = shallowRef()

const formRules = {
  name: { required: true, message: '请输入文件库名称', trigger: 'blur' },
  copyMode: { required: true, message: '请选择复制模式', trigger: 'blur' },
  copyEvent: { required: true, message: '请选择复制事件', trigger: 'blur' },
  copyType: { required: true, message: '请选择复制类型', trigger: 'blur' },
  timeOut: { pattern: integerNumber, message: '请输入复制超时（正整数）', trigger: 'blur' },
  delayDuration: { pattern: integerNumber, message: '请输入延迟时间（正整数）', trigger: 'blur' }
}

const formItems = computed(() => {
  const fields = [
    { label: '复制规则名称', prop: 'name', span: 12, attrs: { maxlength: 50 } },
    {
      label: '复制事件', prop: 'copyEvent', component: TrSelect, span: 12, attrs: {
        data: copyEventList
      }
    },
    {
      label: '复制模式', prop: 'copyMode', component: TrSelect, span: 12, attrs: {
        data: copyModeList
      },
      listeners: {
        change() {
          // 每次切换时，重置计划时间
          props.formData._delayTime = '00:00:00'
          props.formData.planTime = {
            type: 'day',
            day: 1,
            week: 1,
            hour: 0,
            minute: 0,
            second: 0
          }
        }
      }
    },
    {
      label: '复制类型', prop: 'copyType', component: TrSelect, span: 12, attrs: {
        data: copyTypeList
      }
    },
    { label: '复制超时(秒)', prop: 'timeOut', span: 12, attrs: { type: 'number', min: 0 } },
    { label: '过滤方法', prop: 'filterMethod', span: 12 },
    { label: '描述', prop: 'description', type: 'input', span: 24, attrs: { type: 'textarea', rows: 3, maxlength: 1000, showWordLimit: true } }
  ]

  // 【立即】和【手动】时不需要设置延迟时间
  if (!['1', '4'].includes(props.formData.copyMode)) {
    fields.splice(6, 0, {
      label: props.formData.copyMode === '2' ? '延迟时长(秒)' : '计划时间',
      prop: 'delayDuration',
      span: 12
    })
  }

  return fields
})

// 计划时间第二个下拉框列表
const secondaryTime = computed(() => {
  if (props.formData.planTime.type === 'month') { // 每月
    return {
      fieldKey: 'day',
      list: [
        { label: '第一天', value: 1 },
        { label: '第二天', value: 2 },
        { label: '最后一天', value: -1 }
      ]
    }
  } else if (props.formData.planTime.type === 'week') { // 每周
    return {
      fieldKey: 'week',
      list: [
        { label: '周一', value: 1 },
        { label: '周二', value: 2 },
        { label: '周三', value: 3 },
        { label: '周四', value: 4 },
        { label: '周五', value: 5 },
        { label: '周六', value: 6 },
        { label: '周日', value: 7 }
      ]
    }
  }

  return {
    fieldKey: '',
    list: []
  }
})

defineExpose({
  validate() {
    // 如果复制模式是 【计划】 时就把时间格式化
    if (props.formData.copyMode === '3') {
      if (!props.formData._delayTime) {
        Message.warning('请设置延迟时间')

        return Promise.reject('请设置延迟时间')
      }

      // 分别给 时分秒 设置值
      [
        props.formData.planTime.hour,
        props.formData.planTime.minute,
        props.formData.planTime.second
      ] = props.formData._delayTime.split(':').map(Number)
    }

    return formRef.value.validate()
  }
})
</script>

<template>
  <tr-form ref="formRef" v-model="formData" class="w-800px" :rules="formRules" :items="formItems" :show-buttons="false">
    <template #delayDuration>
      <!-- 延迟 -->
      <template v-if="formData.copyMode === '2'">
        <el-input v-model="formData.delayDuration" type="number" min="0" />
      </template>
      <!-- 计划 -->
      <div v-else-if="formData.copyMode === '3'" class="flex items-center gap-1">
        <el-select v-model="formData.planTime.type" class="!w-80px min-w-80px" placeholder="">
          <el-option label="每月" value="month" />
          <el-option label="每周" value="week" />
          <el-option label="每日" value="day" />
        </el-select>

        <el-select v-if="formData.planTime.type !== 'day'" v-model="formData.planTime[secondaryTime.fieldKey]" class="w-80px" placeholder="">
          <el-option v-for="item of secondaryTime.list" :key="item.value" v-bind="item" />
        </el-select>

        <el-time-picker v-model="formData._delayTime" value-format="HH:mm:ss" />
      </div>
    </template>
  </tr-form>
</template>

<style scoped>
::v-deep .el-date-editor .el-input__inner {
  padding-right: 20px;
}
</style>
