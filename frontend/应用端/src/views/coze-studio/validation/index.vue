<template>
  <div class="validation-page">
    <div class="page-header">
      <div class="header-left">
        <h1 v-if="!isSinglePluginMode">功能验证/巡检</h1>
        <h1 v-else>{{ currentPlugin?.name || targetPluginId }} - 插件验证</h1>
        <p v-if="!isSinglePluginMode">选择要验证的插件与场景，逐步执行并查看实时日志与结果</p>
        <p v-else>专门针对 {{ currentPlugin?.name || targetPluginId }} 插件的功能验证和测试</p>
      </div>
      <div class="header-right">
        <el-button v-if="isSinglePluginMode" @click="$router.push('/coze-studio/plugins')" type="info">
          返回插件列表
        </el-button>
        <el-button type="primary" :icon="VideoPlay" :loading="state.running" @click="runValidation">开始验证</el-button>
        <el-button :icon="Delete" @click="reset">清空</el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="插件验证" name="plugins">
        <!-- 单插件验证模式 -->
        <div v-if="isSinglePluginMode">
          <PluginValidationFlow :plugin-id="targetPluginId" />
        </div>

        <!-- 批量验证模式 -->
        <div v-else>
          <ExcelParseFlow />

          <el-card class="panel" style="margin-top:12px; margin-bottom:16px;">
            <template #header>
              <div class="card-header">
                <span>选择验证项</span>
              </div>
            </template>
            <div class="selectors">
              <el-select v-model="state.selected" multiple placeholder="选择插件/场景" style="min-width:380px">
                <el-option v-for="t in items" :key="t.id" :label="`${t.name} (${t.id})`" :value="t.id" />
              </el-select>
              <el-checkbox v-model="state.autoInstall">自动安装缺失插件</el-checkbox>
            </div>
          </el-card>

          <el-card class="panel" style="margin-bottom:16px;">
            <template #header>
              <div class="card-header">
                <span>实时日志</span>
              </div>
            </template>
            <div class="log-box">
              <pre>{{ logs.join('\n') }}</pre>
            </div>
          </el-card>

          <el-card class="panel">
            <template #header>
              <div class="card-header">
                <span>执行结果</span>
              </div>
            </template>
            <el-table :data="state.results" border size="small">
              <el-table-column prop="id" label="ID" width="180" />
              <el-table-column prop="name" label="名称" width="200" />
              <el-table-column label="状态" width="120">
                <template #default="{ row }">
                  <el-tag :type="row.status==='success' ? 'success' : (row.status==='installed' ? 'info' : 'danger')">{{ row.status }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="durationMs" label="耗时(ms)" width="120" />
              <el-table-column prop="summary" label="摘要" />
            </el-table>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="Agent验证" name="agents">
        <el-card class="panel">
          <template #header><div class="card-header"><span>Agent验证</span></div></template>
          <el-alert title="即将对接：Agent列表、对话测试与依赖插件可视化" type="info" :closable="false" />
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="工作流验证" name="workflows">
        <el-card class="panel">
          <template #header><div class="card-header"><span>工作流验证</span></div></template>
          <el-alert title="即将对接：工作流编排、运行与节点日志" type="info" :closable="false" />
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { VideoPlay, Delete } from '@element-plus/icons-vue'
import { executePlugin as apiExecutePlugin, installPlugin as apiInstallPlugin, executeQualityAnalysisScenario } from '@/api/coze-studio'
import ExcelParseFlow from './components/ExcelParseFlow.vue'
import PluginValidationFlow from './components/PluginValidationFlow.vue'

const route = useRoute()
const activeTab = ref('plugins')

// 获取路由参数中的插件ID
const targetPluginId = ref(route.query.pluginId || null)

// 可选验证项（插件 + 场景）
const items = ref([
  // 文档/数据解析类（按格式）
  { id: 'csv_parser', name: 'CSV解析器', type: 'plugin' },
  { id: 'xlsx_parser', name: 'XLSX解析器', type: 'plugin' },
  { id: 'json_parser', name: 'JSON解析器', type: 'plugin' },
  { id: 'xml_parser', name: 'XML解析器', type: 'plugin' },
  { id: 'pdf_parser', name: 'PDF解析器', type: 'plugin' },
  { id: 'docx_parser', name: 'DOCX解析器', type: 'plugin' },
  { id: 'excel_analyzer', name: 'Excel分析器', type: 'plugin' },
  // 统计/分析类
  { id: 'statistical_analyzer', name: '统计分析器', type: 'plugin' },
  { id: 'spc_controller', name: 'SPC控制图', type: 'plugin' },
  { id: 'data_cleaner', name: '数据清洗器', type: 'plugin' },
  { id: 'anomaly_detector', name: '异常检测器', type: 'plugin' },
  { id: 'text_summarizer', name: '文本摘要器', type: 'plugin' },
  // 质量管理类
  { id: 'fmea_analyzer', name: 'FMEA失效模式分析', type: 'plugin' },
  { id: 'msa_calculator', name: 'MSA测量系统分析', type: 'plugin' },
  { id: 'defect_detector', name: '缺陷检测器', type: 'plugin' },
  // 集成类
  { id: 'api_connector', name: 'API连接器', type: 'plugin' },
  { id: 'database_query', name: '数据库查询器', type: 'plugin' },
  { id: 'ocr_reader', name: 'OCR文字识别', type: 'plugin' },
  // 场景
  { id: 'scenario:quality-analysis', name: '场景：质量问题分析', type: 'scenario' }
])

const state = reactive({
  selected: [],
  autoInstall: true,
  running: false,
  results: []
})

// 当前选中的插件信息
const currentPlugin = computed(() => {
  if (!targetPluginId.value) return null
  return items.value.find(item => item.id === targetPluginId.value)
})

// 是否为单插件验证模式
const isSinglePluginMode = computed(() => !!targetPluginId.value)

const logs = ref([])
const appendLog = (t) => { logs.value.push(`[${new Date().toLocaleTimeString()}] ${t}`) }
const reset = () => { state.results = []; logs.value = []; state.selected = [] }

const samples = {
  // 解析类
  csv_parser: { input: { csv: 'a,b\n1,2\n3,4' } },
  xlsx_parser: { input: { base64: '' } }, // 请在UI中通过上传文件进行
  json_parser: { input: { text: JSON.stringify([{a:1,b:2},{a:3,b:4}]) } },
  xml_parser: { input: { text: '<rows><row><a>1</a><b>2</b></row><row><a>3</a><b>4</b></row></rows>' } },
  pdf_parser: { input: { base64: '' } }, // 请在UI中通过上传文件进行
  docx_parser: { input: { text: '这是一个DOCX内容示例。' } },
  // 分析类
  statistical_analyzer: { dataset: [{value:98},{value:100},{value:97},{value:99}] },
  spc_controller: { series: [98,100,97,99,101,98] },
  data_cleaner: { dataset: [{id:1,value:98},{id:1,value:''},{id:2,value:100},{id:3,value:97}] },
  anomaly_detector: { series: [98,100,97,99,101,200,98], method:'iqr' },
  text_summarizer: { text: '质量问题分析是制造业中的核心工作。通过数据分析和SPC控制，我们可以定位异常。最终形成改进建议。' }
}

async function runValidation(){
  if (!state.selected.length) { ElMessage.info('请选择至少一个验证项'); return }
  state.running = true; state.results = []; logs.value = []

  for (const id of state.selected) {
    const t0 = Date.now()
    try {
      if (id.startsWith('scenario:')) {
        // 场景：目前仅质量问题分析
        appendLog(`执行场景 ${id}`)
        const json = await executeQualityAnalysisScenario({ dataset: [{ value: 98 }, { value: 100 }, { value: 97 }, { value: 99 }], valueField: 'value' })
        if(!json.success) throw new Error(json.message || '场景执行失败')
        const dur = Date.now()-t0
        appendLog(`场景 ${id} 执行成功，用时 ${dur}ms`)
        state.results.push({ id, name: '质量问题分析', status:'success', durationMs: dur, summary: 'ok' })
      } else {
        // 插件
        appendLog(`准备执行插件 ${id}`)
        if (state.autoInstall) {
          try { await apiInstallPlugin(id, {}); appendLog(`插件 ${id} 安装完成/已存在`) } catch(e) { appendLog(`插件 ${id} 安装失败：${e.message}`) }
        }
        const resp = await apiExecutePlugin(id, { input: samples[id] || {} })
        const dur = Date.now()-t0
        const summary = id==='data_cleaner' ? `cleaned=${(resp.data.cleaned||[]).length}/${resp.data.total}`
                      : id==='anomaly_detector' ? `anomalies=${(resp.data.anomalies||[]).length}`
                      : id==='text_summarizer' ? `len=${(resp.data.summary||'').length}`
                      : 'ok'
        appendLog(`插件 ${id} 执行成功，用时 ${dur}ms`)
        state.results.push({ id, name: (items.value.find(x=>x.id===id)||{}).name || id, status:'success', durationMs: dur, summary })
      }
    } catch (e) {
      const dur = Date.now()-t0
      appendLog(`执行 ${id} 失败：${e.message}`)
      state.results.push({ id, name: (items.value.find(x=>x.id===id)||{}).name || id, status:'error', durationMs: dur, summary: e.message })
    }
  }

  state.running = false
}

// 组件挂载时的初始化
onMounted(() => {
  // 如果有指定的插件ID，自动选择该插件
  if (targetPluginId.value && items.value.find(item => item.id === targetPluginId.value)) {
    state.selected = [targetPluginId.value]
    ElMessage.success(`已自动选择插件: ${currentPlugin.value?.name || targetPluginId.value}`)
  }
})
</script>

<style scoped>
.validation-page { padding: 16px; }
.page-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; }
.header-left h1 { margin:0; }
.panel .card-header { display:flex; align-items:center; justify-content:space-between; }
.selectors { display:flex; gap:12px; align-items:center; flex-wrap:wrap; }
.log-box { background:#0b1020; color:#d6e1ff; padding:12px; border-radius:6px; max-height:240px; overflow:auto; }
</style>

