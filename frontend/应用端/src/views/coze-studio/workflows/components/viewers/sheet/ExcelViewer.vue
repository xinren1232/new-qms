<template>
  <div class="excel-viewer">
    <el-tabs v-model="active" type="border-card">
      <el-tab-pane v-for="(s,idx) in sheets" :name="String(idx)" :key="idx" :label="s.name || ('Sheet'+(idx+1))">
        <el-table :data="rowsToObjects(s.rows)" height="320" border>
          <el-table-column v-for="(c,i) in inferHeaders(s.rows)" :key="i" :prop="String(i)" :label="c" :min-width="120"/>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<script setup>
import { ref, computed } from 'vue'
const props = defineProps({ result:{ type:Object, required:true } })
const active = ref('0')
const sheets = computed(()=> props.result.sheets || [])
function inferHeaders(rows){ const first = rows?.[0] || []; return first.map((v,i)=> String(v||('列'+(i+1)))) }
function rowsToObjects(rows){ if(!rows) return []; const headers = inferHeaders(rows); return (rows.slice(1)||[]).map(r=>Object.fromEntries(headers.map((h,i)=>[i, r?.[i] ?? '']))) }
</script>
<style scoped>
.excel-viewer{min-height:360px}
</style>

