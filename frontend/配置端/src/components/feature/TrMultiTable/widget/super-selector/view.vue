<script>
export default { name: 'SuperSelectorView' }
</script>
<script setup>
import { ref, watch, inject } from 'vue'

const superSelectMap = inject('superSelectMap')
const props = defineProps({
  value: {
    type: [String,Array],
    default: ''
  },
  column: {
    type: Object,
    default: () => ({})
  }
})
const multipleSelection = ref(props.value || [])
const configParams = ref(props.column.params || {})

const { multiple,disabled,apiLabel,apiValue } = configParams.value || {}

watch(() => props.value, (val) => {
  if (multiple && val && val.length) {
    multipleSelection.value = val.map(item => superSelectMap.value[item]).filter(Boolean)
  } else if (!multiple && val) {
    multipleSelection.value = [superSelectMap.value[val]].filter(Boolean)
  }
},{ deep: true,immediate: true })

</script>

<template>
  <el-tooltip
    v-if="multipleSelection.length"
    :disabled="disabled"
    :content="multipleSelection.map(o => o[apiLabel]).join(',')"
    placement="top"
    effect="dark"
  >
    <div class="super-view-box" :class="{ disabled: disabled }">
      <el-tag
        v-for="item in multipleSelection"
        :key="item[apiValue]"
        type="info"
        effect="light"
        :disable-transitions="false"
      >
        {{ item[apiLabel] }}
      </el-tag>
    </div>
  </el-tooltip></template>

<style scoped lang="scss">
.super-view-box{
  width: 100%;
  height: 38px;
  line-height: 38px;
  overflow: hidden;
  padding: 0 10px;
  .el-tag{
    margin-right: 3px;
    height: 26px;
    line-height: 26px;
  }
  &.disabled{
    cursor: not-allowed;
    background-color:#F5F7FA ;
  }
}
</style>
