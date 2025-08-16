<template>
  <span>
    <span v-for="item in depList" :key="item.id" size="medium">{{ item.name }}</span>
  </span>
</template>

<script>
export default {
  name: 'DepartmentSelectorView'
}
</script>
<script setup>
import { ref, computed } from 'vue'
import { batchQueryDeptById } from './api'

const depList = ref([])
const props = defineProps({
  value: {
    type: [String, Array],
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

// 查询部门信息
const queryDeptInfo = async () => {
  let depIds = []

  if (field.value.multiple && Array.isArray(props.value)) {
    depIds = depIds.concat(props.value)
  } else {
    depIds = [props.value]
  }
  depIds = depIds.filter(Boolean).flat(1)
  if (!depIds.length) return
  const { data } = await batchQueryDeptById(depIds)

  depList.value = data
}

queryDeptInfo()
</script>

<style scoped lang="scss">

</style>
