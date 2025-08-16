<template>
  <el-cascader
    ref="fieldEditor"
    v-model="fieldModel"
    :options="optionItems"
    class="full-width-input"
    :disabled="field.disabled"
    :size="field.size"
    :clearable="field.clearable"
    :filterable="field.filterable"
    :show-all-levels="showFullPath"
    :props="{
      checkStrictly: field.checkStrictly,
      multiple: field.multiple,
      children:'children',
      label: 'label',
      value: 'value',
      expandTrigger: 'click',
      lazy: true,
      lazyLoad:lazyLoad,
      emitPath: false
    }"
    :placeholder="field.placeholder"
    @change="handleChangeEvent"
  />
</template>

<script setup>
import { ref, computed } from 'vue'
import { queryRootDeptList, queryChildDeptAndEmployee } from './api'

const props = defineProps({
  value: {
    type: [String,Array],
    default: ''
  },
  row: {
    type: Object,
    default: () => ({})
  },
  column: {
    type: Object,
    default: () => ({})
  }
})


const field = computed(() => props.column.params)

const fieldModel = ref(props.value)

const optionItems = ref([])
const showFullPath = ref(false)

// eslint-disable-next-line no-unused-vars
const getRootDepList = async () => {
  const { data } = await queryRootDeptList()

  optionItems.value = data.map(item => {
    return {
      label: item.deptName,
      value: item.deptId,
      children: [],
      isLeaf: false
    }
  })
}

// getRootDepList()

// 懒加载部门数据
const lazyLoad = (node, resolve) => {
  const params = { deptId: node?.data?.value }


  queryChildDeptAndEmployee(params).then(res => {
    const data = res.data
    const children = data.map(item => {
      return {
        value: item.deptId,
        label: item.deptName
      }
    })

    resolve(children)
  })
}

// 监听值变化
const handleChangeEvent = (val) => {
  fieldModel.value = val
  props.row[props.column.field] = val
}
</script>

<style lang="scss" scoped>

</style>
