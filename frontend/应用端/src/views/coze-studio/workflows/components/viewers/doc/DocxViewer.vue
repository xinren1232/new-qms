<template>
  <div class="docx-viewer">
    <div v-if="result.html" class="docx-html" v-html="sanitizedHtml"></div>
    <el-input v-else v-model="text" type="textarea" :rows="10" readonly />
  </div>
</template>
<script setup>
import { computed } from 'vue'
const props = defineProps({ result:{ type:Object, required:true } })
const text = computed(()=> props.result.text || props.result.content || '')
// 简单白名单（可替换为更严谨的DOMPurify）
const sanitizedHtml = computed(()=> (props.result.html||'').replace(/<script[^>]*>[\s\S]*?<\/script>/gi,'').replace(/on[a-z]+="[^"]*"/gi,''))
</script>
<style scoped>
.docx-html{font-size:14px;line-height:1.7}
.docx-html h1,.docx-html h2,.docx-html h3{margin:8px 0}
.docx-html p{margin:6px 0}
</style>

