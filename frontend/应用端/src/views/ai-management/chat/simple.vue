<template>
  <div class="simple-chat">
    <h2>AI智能问答测试页面</h2>
    
    <div class="chat-area">
      <div v-for="(msg, index) in messages" :key="index" class="message">
        <div class="message-role">{{ msg.role === 'user' ? '用户' : 'AI助手' }}:</div>
        <div class="message-content">{{ msg.content }}</div>
      </div>
    </div>
    
    <div class="input-area">
      <el-input 
        v-model="inputMessage" 
        placeholder="输入您的问题..."
        @keyup.enter="sendMessage"
      />
      <el-button @click="sendMessage" type="primary" :loading="loading">
        发送
      </el-button>
    </div>
    
    <div class="quick-buttons">
      <el-button @click="sendQuick('你好，请介绍一下质量管理体系')" size="small">
        质量管理体系
      </el-button>
      <el-button @click="sendQuick('如何进行缺陷分析？')" size="small">
        缺陷分析
      </el-button>
      <el-button @click="sendQuick('ISO 9001标准的主要内容是什么？')" size="small">
        ISO 9001标准
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const inputMessage = ref('')
const loading = ref(false)
const messages = ref([])

const sendMessage = async () => {
  if (!inputMessage.value.trim()) return
  
  const userMsg = {
    role: 'user',
    content: inputMessage.value
  }
  
  messages.value.push(userMsg)
  const currentInput = inputMessage.value
  inputMessage.value = ''
  loading.value = true
  
  try {
    const response = await fetch('/api/chat/send', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        message: currentInput,
        conversation_id: 'test_' + Date.now()
      })
    })
    
    const result = await response.json()
    
    if (result.success) {
      messages.value.push({
        role: 'assistant',
        content: result.data.response
      })
    } else {
      throw new Error(result.message || '发送失败')
    }
  } catch (error) {
    console.error('发送失败:', error)
    ElMessage.error('发送失败: ' + error.message)
    messages.value.push({
      role: 'assistant',
      content: '抱歉，发送失败了，请稍后再试。'
    })
  } finally {
    loading.value = false
  }
}

const sendQuick = (question) => {
  inputMessage.value = question
  sendMessage()
}
</script>

<style scoped>
.simple-chat {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.chat-area {
  height: 400px;
  border: 1px solid #ddd;
  padding: 15px;
  overflow-y: auto;
  margin: 20px 0;
  background: #f9f9f9;
}

.message {
  margin-bottom: 15px;
  padding: 10px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.message-role {
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.message-content {
  line-height: 1.6;
}

.input-area {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.quick-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
</style>
