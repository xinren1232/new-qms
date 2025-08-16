<template>
  <el-dialog title="快速开始" :model-value="modelValue" @close="$emit('update:modelValue', false)">
    <el-form @submit.prevent>
      <el-form-item label="问题">
        <el-input v-model="question" placeholder="输入要咨询的问题" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="start">开始</el-button>
        <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
const props = defineProps({ modelValue: Boolean })
const emit = defineEmits(['update:modelValue', 'start-chat'])
const question = ref('')

watch(() => props.modelValue, v => { if (!v) question.value = '' })

const start = () => {
  emit('start-chat', { type: 'agent', agent: { id: 'quick', name: '临时助手' }, question: question.value })
  emit('update:modelValue', false)
}
</script>

