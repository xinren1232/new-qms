<template>
  <div class="ppt-viewer">
    <div v-if="slides && slides.length" class="slides">
      <div class="slide" v-for="s in slides" :key="s.index">
        <h4 class="title">{{ s.title || ('幻灯片 '+(s.index+1)) }}</h4>
        <ul class="bullets"><li v-for="(b,i) in (s.bullets||[])" :key="i">{{ b }}</li></ul>
      </div>
    </div>
    <el-empty v-else description="没有可展示的幻灯片文本" />
  </div>
</template>
<script setup>
import { computed } from 'vue'
const props = defineProps({ result:{ type:Object, required:true } })
const slides = computed(()=> props.result.slides || [])
</script>
<style scoped>
.slides{display:flex;flex-direction:column;gap:12px}
.slide{border:1px solid var(--el-border-color-lighter);border-radius:8px;padding:10px;background:white}
.title{margin:0 0 6px 0}
</style>

