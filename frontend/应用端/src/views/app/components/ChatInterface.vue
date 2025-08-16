<template>
  <div class="chat-interface">
    <div class="messages">
      <div v-for="m in messages" :key="m.id" :class="['msg', m.role]">{{ m.text }}</div>
    </div>
    <div class="input">
      <el-input v-model="input" placeholder="请输入..." @keyup.enter="send" />
      <el-button type="primary" @click="send">发送</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const props = defineProps({ agent: Object, conversation: Object })
const emit = defineEmits(['close'])

const messages = ref([{ id: 1, role: 'system', text: '欢迎使用QMS AI' }])
const input = ref('')

const send = () => {
  if (!input.value.trim()) return
  messages.value.push({ id: Date.now(), role: 'user', text: input.value })
  input.value = ''
}
</script>

<style scoped>
.chat-interface { display: flex; flex-direction: column; height: 100%; }
.messages { flex: 1; overflow: auto; padding: 8px; }
.msg { margin: 6px 0; }
.msg.user { text-align: right; }
.input { display: flex; gap: 8px; }
</style>

