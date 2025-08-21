<template>
  <div class="universal-doc-viewer">
    <component :is="viewer" :result="result" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import DocxViewer from './doc/DocxViewer.vue'
import PdfViewer from './pdf/PdfViewer.vue'
import ExcelViewer from './sheet/ExcelViewer.vue'
import PptViewer from './ppt/PptViewer.vue'
import TextViewer from './text/TextViewer.vue'

const props = defineProps({
  result: { type: Object, required: true }
})

const viewer = computed(() => ({
  docx: DocxViewer,
  pdf: PdfViewer,
  xlsx: ExcelViewer,
  csv: ExcelViewer,
  pptx: PptViewer,
  text: TextViewer,
  image: TextViewer
}[props.result?.metadata?.format || props.result?.format || 'text'] || TextViewer))
</script>

<style scoped>
.universal-doc-viewer{display:flex;flex-direction:column;min-height:240px}
</style>

