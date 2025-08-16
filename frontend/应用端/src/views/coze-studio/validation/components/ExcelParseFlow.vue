<template>
  <el-card class="flow-card">
    <template #header>
      <div class="card-header">
        <span>多格式解析验证</span>
        <small>上传 → 自动选择插件（CSV/XLSX/PDF/JSON/XML/DOCX） → 结果确认</small>
      </div>
    </template>

    <el-steps :active="step" finish-status="success" align-center>
      <el-step title="上传文件" />
      <el-step :title="`解析（${percent}%）`" />
      <el-step title="结果确认" />
    </el-steps>

    <div v-show="step===0" class="step-pane">
      <el-upload
        drag
        :auto-upload="false"
        :on-change="onFileChange"
        accept=".xlsx,.xls,.csv,.pdf,.json,.xml,.docx"
      >
        <i class="el-icon-upload" />
        <div class="el-upload__text">拖拽文件到此或点击上传</div>
        <div class="el-upload__tip">支持 .csv/.xlsx/.xls/.pdf/.json/.xml/.docx</div>
      </el-upload>
      <div class="quick-samples">
        <el-button size="small" @click="quickRun('csv')">CSV 示例</el-button>
        <el-button size="small" @click="quickRun('json')">JSON 示例</el-button>
        <el-button size="small" @click="quickRun('xml')">XML 示例</el-button>
        <el-button size="small" @click="quickRun('docx')">DOCX 示例</el-button>
      </div>
      <el-button :disabled="!file" type="primary" @click="goParse">开始解析</el-button>
    </div>

    <div v-show="step===1" class="step-pane">
      <el-alert :title="`正在解析（${stage}）`" type="info" :closable="false"/>
      <el-progress :percentage="percent" :text-inside="true" style="margin-top:8px;"/>
    </div>

    <div v-show="step===2" class="step-pane">
      <el-alert title="解析完成" type="success" :closable="false"/>
      <div class="result">
        <div class="meta">行数：{{ result.row_count || (result.preview?.length || 0) }}</div>
        <div class="meta">列数：{{ (result.columns && Object.keys(result.columns).length) || 0 }}</div>
        <el-table :data="result.preview || []" border size="small" style="margin-top:8px;" max-height="260px">
          <el-table-column v-for="(v,k) in (result.preview?.[0]||{})" :key="k" :prop="k" :label="k" />
        </el-table>
      </div>
      <el-button @click="reset">重新开始</el-button>
    </div>
  </el-card>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { executePlugin as apiExecutePlugin } from '@/api/coze-studio'

const step = ref(0)
const file = ref(null)
const result = ref({})
const percent = ref(0)
const stage = ref('待开始')

function onFileChange(uploadFile){
  const f = uploadFile?.raw || null
  if (f && f.size > 40 * 1024 * 1024) {
    ElMessage.error('文件过大，建议小于40MB。请尝试拆分或使用CSV')
    file.value = null
    return
  }
  file.value = f
}

async function goParse(){
  if(!file.value){ ElMessage.info('请先选择文件'); return }
  step.value = 1
  percent.value = 0
  stage.value = '读取文件'
  try{
    const name = (file.value.name||'').toLowerCase()
    const isCsv = /\.csv$/.test(name)
    const isXlsx = /\.(xlsx|xls)$/.test(name)
    const isPdf = /\.pdf$/.test(name)
    const isJson = /\.json$/.test(name)
    const isXml = /\.xml$/.test(name)
    const isDocx = /\.docx$/.test(name)

    // 读取文件：文本类走 readAsText，二进制类走 Base64
    const payload = await new Promise((resolve, reject) => {
      const reader = new FileReader()
      reader.onprogress = (e) => { if (e.lengthComputable) percent.value = Math.round((e.loaded / e.total) * 40) }
      reader.onload = () => {
        const res = reader.result || ''
        if (isCsv || isJson || isXml) {
          const text = typeof res === 'string' ? res : ''
          if (isCsv) resolve({ csv: text })
          else if (isJson) resolve({ text })
          else resolve({ text })
        } else {
          const str = typeof res === 'string' ? res : ''
          resolve({ base64: (str.split(',')[1] || '') })
        }
      }
      reader.onerror = () => reject(reader.error || new Error('文件读取失败'))
      if (isCsv || isJson || isXml) reader.readAsText(file.value)
      else reader.readAsDataURL(file.value)
    })

    // 上传/后端处理阶段
    stage.value = '上传与解析'
    percent.value = Math.max(percent.value, 45)
    const pluginId = isCsv ? 'csv_parser' : isXlsx ? 'xlsx_parser' : isPdf ? 'pdf_parser' : isJson ? 'json_parser' : isXml ? 'xml_parser' : isDocx ? 'docx_parser' : 'excel_analyzer'
    const resp = await apiExecutePlugin(pluginId, { input: payload })

    // 解析成功（兼容不同返回结构）
    const data = resp?.data?.result || resp?.data || resp || {}
    result.value = data
    percent.value = 100

  async function quickRun(kind){
    step.value = 1; percent.value = 0; stage.value = '准备示例数据'
    try{
      let pluginId = ''; let input = {}
      if (kind==='csv') { pluginId='csv_parser'; input={ csv:'a,b\n1,2\n3,4' } }
      if (kind==='json') { pluginId='json_parser'; input={ text: JSON.stringify([{a:1,b:2},{a:3,b:4}]) } }
      if (kind==='xml') { pluginId='xml_parser'; input={ text:'<rows><row><a>1</a><b>2</b></row><row><a>3</a><b>4</b></row></rows>' } }
      if (kind==='docx') { pluginId='docx_parser'; input={ text:'这是一个DOCX内容示例。' } }
      percent.value = 55; stage.value = '上传与解析'
      const resp = await apiExecutePlugin(pluginId, { input })
      const data = resp?.data?.result || resp?.data || resp || {}
      result.value = data; percent.value = 100; stage.value='完成'; step.value=2
    }catch(e){
      const msg = e?.response?.data?.error || e?.response?.data?.message || e.message || '未知错误'
      ElMessage.error('示例执行失败：'+ msg)
      step.value = 0
    }
  }

    stage.value = '完成'
    step.value = 2
  }catch(e){
    const msg = e?.response?.data?.error || e?.response?.data?.message || e.message || '未知错误'
    ElMessage.error('解析失败：'+ msg)
    step.value=0
    percent.value = 0
    stage.value = '失败'
  }
}

function reset(){ step.value=0; file.value=null; result.value={} }
</script>

<style scoped>
.flow-card{ margin-bottom:16px; }
.step-pane{ margin-top:12px; }
.result .meta{ margin:6px 0; color:#606266; }
</style>

