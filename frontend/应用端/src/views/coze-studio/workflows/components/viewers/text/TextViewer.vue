<template>
  <el-input v-model="text" type="textarea" :rows="12" readonly />
</template>
<script setup>
import { computed } from 'vue'
const props = defineProps({ result:{ type:Object, required:true } })
const text = computed(()=> props.result.text || props.result.content || '')
</script>

