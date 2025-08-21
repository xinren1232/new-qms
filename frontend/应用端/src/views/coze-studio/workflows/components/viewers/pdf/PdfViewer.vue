<template>
  <div class="pdf-viewer">
    <div v-if="pages && pages.length" class="pdf-pages">
      <div class="pdf-page" v-for="p in pages" :key="p.index">
        <div class="page-header">第 {{ p.index + 1 }} 页</div>
        <pre class="page-text">{{ p.text }}</pre>
      </div>
    </div>
    <el-empty v-else description="无可预览的页文本，已回退为纯文本" />
  </div>
</template>
<script setup>
import { computed } from 'vue'
const props = defineProps({ result:{ type:Object, required:true } })
const pages = computed(()=> props.result.pages || [])
</script>
<style scoped>
.pdf-pages{display:flex;flex-direction:column;gap:8px}
.page-header{background:#f7f9fc;padding:6px 8px;border:1px solid var(--el-border-color-lighter);border-radius:4px}
.page-text{white-space:pre-wrap;margin:0;padding:8px;border:1px solid var(--el-border-color-lighter);border-radius:4px}
</style>

