<script setup>
import { renderFieldByDict,getDictColor } from '@/utils/dict'

defineProps({
  dictCode: {
    type: String,
    default: '',
    command: '字典编码'
  },
  value: {
    type: String,
    default: '',
    command: '字段值'
  },
  type: {
    type: String,
    default: 'text',
    command: 'Label渲染模式，text||tag'
  }
})

</script>

<template>
  <span v-if="type==='text'" class="inline-block rounded text-sm py-2px px-6px empty:hidden" :style="getDictColor(value,dictCode)">{{ renderFieldByDict(value,dictCode) }}</span>
  <el-tag v-else :style="getDictColor(value,dictCode)">{{ renderFieldByDict(value,dictCode) }}</el-tag>
</template>
<script>
export default {
  name: 'DictLabel'
}
</script>
