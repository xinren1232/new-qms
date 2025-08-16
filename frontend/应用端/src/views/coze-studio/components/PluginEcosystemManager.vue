<template>
  <div class="plugin-ecosystem-manager">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">
          <i class="el-icon-s-grid"></i>
          插件生态系统
        </h2>
        <p class="page-description">
          管理和使用各种AI插件，包括LangChain Tools、Semantic Kernel、Haystack组件等
        </p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="refreshPlugins">
          <i class="el-icon-refresh"></i>
          刷新插件
        </el-button>
        <el-button @click="$router.push('/coze-studio/validation')">
          <i class="el-icon-finished"></i>
          去功能验证
        </el-button>
      </div>
    </div>

    <!-- 插件统计 -->
    <div class="plugin-stats">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon total">
                <i class="el-icon-s-grid"></i>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ pluginStats.total_plugins || 0 }}</div>
                <div class="stat-label">总插件数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon active">
                <i class="el-icon-check"></i>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ pluginStats.active_plugins || 0 }}</div>
                <div class="stat-label">已安装</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon categories">
                <i class="el-icon-menu"></i>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ Object.keys(pluginStats.by_category || {}).length }}</div>
                <div class="stat-label">插件分类</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon popular">
                <i class="el-icon-star-on"></i>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ pluginStats.most_used?.name || '-' }}</div>
                <div class="stat-label">最受欢迎</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 开源成熟工具推荐 -->
    <!-- 功能自检面板 -->
    <el-card class="plugins-card" style="margin-bottom:16px;">
      <div slot="header" class="card-header">
        <span>功能自检面板</span>
        <small style="margin-left:8px;color:#909399;">选择插件 → 一键检测（自动安装+运行示例）</small>
      </div>

      <div style="display:flex;gap:12px;align-items:center;flex-wrap:wrap;">
        <el-select v-model="selfCheck.selected" multiple style="min-width:320px;" placeholder="选择要检测的插件">
          <el-option v-for="t in plugins.filter(p=>p.status==='active')" :key="t.id" :label="`${t.name}(${t.id})`" :value="t.id" />
        </el-select>
        <el-button type="primary" :loading="selfCheck.running" @click="runSelfCheck">一键检测选中插件</el-button>
        <el-button @click="selfCheck.results=[]" :disabled="!selfCheck.results.length">清空结果</el-button>
      </div>

      <el-table v-if="selfCheck.results.length" :data="selfCheck.results" border size="small" style="margin-top:12px;">
        <el-table-column prop="id" label="插件ID" width="160"/>
        <el-table-column prop="name" label="名称" width="160"/>
        <el-table-column label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="row.status==='success'?'success':(row.status==='installed'?'info':'danger')">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="durationMs" label="耗时(ms)" width="120"/>
        <el-table-column prop="summary" label="摘要"/>
      </el-table>
    </el-card>


    <!-- 场景演示：质量问题分析Demo -->
    <el-card class="plugins-card" style="margin-bottom:16px;">
      <div slot="header" class="card-header">
        <span>质量问题分析 Demo</span>
        <small style="margin-left:8px;color:#909399;">读取根目录示例Excel，自动统计+SPC</small>
      </div>
      <el-button type="success" size="small" @click="runQualityDemo">运行质量分析（示例数据）</el-button>
    </el-card>


    <!-- 插件列表 -->
    <el-card class="plugins-card">
      <div slot="header" class="card-header">
        <span>插件列表</span>
        <div class="header-actions">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索插件..."
            prefix-icon="el-icon-search"
            size="small"
            style="width: 200px; margin-right: 10px;"
            @input="handleSearch"
          />
          <el-select
            v-model="typeFilter"
            placeholder="插件类型"
            size="small"
            style="width: 150px; margin-right: 10px;"
            @change="handleFilter"
          >
            <el-option label="全部类型" value=""></el-option>
            <el-option label="LangChain工具" value="langchain_tool"></el-option>
            <el-option label="Semantic Kernel" value="semantic_kernel"></el-option>
            <el-option label="Haystack组件" value="haystack_component"></el-option>
            <el-option label="自定义插件" value="custom_plugin"></el-option>
          </el-select>
          <el-select
            v-model="categoryFilter"
            placeholder="插件分类"
            size="small"
            style="width: 150px; margin-right: 10px;"
            @change="handleFilter"
          >
            <el-option label="全部分类" value=""></el-option>
            <el-option label="文档处理" value="document_processing"></el-option>
            <el-option label="图像分析" value="image_analysis"></el-option>
            <el-option label="数据分析" value="data_analysis"></el-option>
            <el-option label="质量控制" value="quality_control"></el-option>
            <el-option label="外部集成" value="external_integration"></el-option>
            <el-option label="质量工具" value="quality_tools"></el-option>
          </el-select>
          <el-select
            v-model="statusFilter"
            placeholder="状态筛选"
            size="small"
            style="width: 120px;"
            @change="handleFilter"
          >
            <el-option label="全部状态" value=""></el-option>
            <el-option label="已安装" value="active"></el-option>
            <el-option label="未安装" value="inactive"></el-option>
          </el-select>
        </div>
      </div>

      <div class="plugins-grid">
        <el-row :gutter="20">
          <el-col
            v-for="plugin in filteredPlugins"
            :key="plugin.id"
            :span="8"
          >
            <el-card class="plugin-card" @click.native="viewPlugin(plugin)">
              <div class="plugin-header">
                <div class="plugin-icon">
                  <i :class="getPluginIcon(plugin.type)"></i>
                </div>
                <div class="plugin-info">
                  <div class="plugin-name">{{ plugin.name }}</div>
                  <div class="plugin-author">{{ plugin.author }}</div>
                </div>
                <div class="plugin-status">
                  <el-tag type="success" size="mini">已部署</el-tag>
                </div>
              </div>

              <div class="plugin-description">
                {{ plugin.description }}
              </div>

              <div class="plugin-meta">
                <el-tag :type="getTypeColor(plugin.type)" size="mini">
                  {{ getTypeText(plugin.type) }}
                </el-tag>
                <el-tag type="info" size="mini" style="margin-left: 8px;">
                  {{ getCategoryText(plugin.category) }}
                </el-tag>
              </div>

              <div class="plugin-capabilities">
                <el-tag
                  v-for="capability in (plugin.capabilities || []).slice(0, 3)"
                  :key="capability"
                  size="mini"
                  style="margin-right: 4px; margin-bottom: 4px;"
                >
                  {{ capability }}
                </el-tag>
                <span v-if="(plugin.capabilities || []).length > 3" class="more-capabilities">
                  +{{ (plugin.capabilities || []).length - 3 }}
                </span>
              </div>

              <div class="plugin-stats">
                <span class="usage-count">
                  <i class="el-icon-view"></i>
                  {{ plugin.usage_count || 0 }}次使用
                </span>
                <span class="version">v{{ plugin.version }}</span>
              </div>

              <div class="plugin-actions" @click.stop>
                <el-button
                  type="primary"
                  size="mini"
                  @click.stop="goToValidation(plugin)"
                >
                  去验证
                </el-button>
                <el-button
                  type="info"
                  size="mini"
                  @click.stop="viewPlugin(plugin)"
                >
                  详情
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.page"
          :page-sizes="[12, 24, 48, 96]"
          :page-size="pagination.limit"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
        />
      </div>
    </el-card>

    <!-- 插件详情对话框 -->
    <el-dialog
      title="插件详情"
      v-model="showDetailDialog"
      width="700px"
    >
      <div v-if="selectedPlugin" class="plugin-detail">
        <h3>{{ selectedPlugin.name }}</h3>
        <p><strong>版本:</strong> v{{ selectedPlugin.version }}</p>
        <p><strong>作者:</strong> {{ selectedPlugin.author }}</p>
        <p><strong>描述:</strong> {{ selectedPlugin.description }}</p>

        <div v-if="selectedPlugin.capabilities && selectedPlugin.capabilities.length > 0" style="margin-top: 20px;">
          <h4>插件能力</h4>
          <div>
            <el-tag
              v-for="capability in selectedPlugin.capabilities"
              :key="capability"
              style="margin-right: 8px; margin-bottom: 8px;"
            >
              {{ capability }}
            </el-tag>
          </div>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDetailDialog = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 执行插件对话框 -->
    <el-dialog
      title="执行插件"
      v-model="showExecuteDialog"
      width="600px"
    >
      <div v-if="selectedPlugin">
        <h4>{{ selectedPlugin.name }}</h4>
        <p>{{ selectedPlugin.description }}</p>

        <el-form
          ref="executeForm"
          :model="executeForm"
          label-width="100px"
        >
          <el-form-item label="选择模板" v-if="(selectedPlugin.validation_templates||[]).length">
            <el-select placeholder="选择验证模板" @change="onTemplateChange">
              <el-option v-for="t in selectedPlugin.validation_templates" :key="t.id||t.name" :label="t.name||t.id" :value="t.id||t.name" />
            </el-select>
          </el-form-item>

          <!-- 测试文件选择 -->
          <el-form-item label="测试文件" v-if="availableTestFiles.length > 0">
            <div class="test-file-selector">
              <el-select
                v-model="selectedTestFile"
                placeholder="选择预制测试文件"
                @change="onTestFileChange"
                style="width: 300px; margin-right: 10px;"
              >
                <el-option
                  v-for="file in availableTestFiles"
                  :key="file.file"
                  :label="file.name"
                  :value="file.file"
                >
                  <span style="float: left">{{ file.icon }} {{ file.name }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">{{ file.format }}</span>
                </el-option>
              </el-select>
              <el-button
                type="text"
                @click="loadTestFile"
                :disabled="!selectedTestFile"
                style="color: #409EFF;"
              >
                <i class="el-icon-download"></i> 加载文件
              </el-button>
              <el-button
                type="text"
                @click="showTestFilePreview"
                :disabled="!selectedTestFile"
                style="color: #67C23A;"
              >
                <i class="el-icon-view"></i> 预览
              </el-button>
            </div>
            <div class="test-file-description" v-if="selectedTestFileInfo">
              <el-alert
                :title="selectedTestFileInfo.description"
                type="info"
                :closable="false"
                show-icon
                style="margin-top: 8px;"
              >
              </el-alert>
            </div>
          </el-form-item>

          <el-form-item label="输入数据">
            <div class="input-data-header" style="margin-bottom: 8px;">
              <span style="color: #606266; font-size: 14px;">请输入插件执行所需的数据，格式为JSON</span>
              <el-button
                type="text"
                size="mini"
                @click="formatInputData"
                style="float: right; color: #409EFF;"
              >
                <i class="el-icon-magic-stick"></i> 格式化
              </el-button>
            </div>
            <el-input
              v-model="executeForm.input"
              type="textarea"
              :rows="6"
              placeholder="请输入插件执行所需的数据，格式为JSON，或使用上方的测试文件快速填充"
            />
          </el-form-item>
          <el-form-item label="执行选项">
            <el-input
              v-model="executeForm.options"
              type="textarea"
              :rows="2"
              placeholder="可选的执行选项，格式为JSON"
            />
          </el-form-item>
        </el-form>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="showExecuteDialog = false">取消</el-button>
        <el-button
          type="primary"
          :loading="executing"
          @click="submitExecution"
        >
          执行插件
        </el-button>
      </div>
    </el-dialog>

    <!-- 执行结果对话框 -->
    <el-dialog
      title="执行结果"
      v-model="showResultDialog"
      width="700px"
    >
      <div v-if="executionResult" class="execution-result">
        <el-alert
          :title="executionResult.success ? '执行成功' : '执行失败'"
          :type="executionResult.success ? 'success' : 'error'"
          :closable="false"
          style="margin-bottom: 20px;"
        />

        <div class="result-info">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="插件名称">
              {{ executionResult.plugin_name }}
            </el-descriptions-item>
            <el-descriptions-item label="执行时间">
              {{ formatTime(executionResult.execution_time) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="result-content" style="margin-top: 20px;">
          <h4>执行结果</h4>
          <pre class="result-json">{{ JSON.stringify(executionResult.result || executionResult.error, null, 2) }}</pre>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getPlugins as apiGetPlugins,
  getPlugin as apiGetPlugin,
  executePlugin as apiExecutePlugin,
  getPluginStats as apiGetPluginStats,
  executeQualityAnalysisScenario as apiExecuteQualityAnalysisScenario
} from '@/api/coze-studio'

export default {
  name: 'PluginEcosystemManager',
  data() {
    return {
      loading: false,
      executing: false,
      plugins: [],
      filteredPlugins: [],
      pluginStats: {},
      searchKeyword: '',
      typeFilter: '',
      categoryFilter: '',
      statusFilter: '',
      pagination: {
        page: 1,
        limit: 12,
        total: 0
      },

      // 详情对话框
      showDetailDialog: false,
      selectedPlugin: null,

      // 执行对话框
      showExecuteDialog: false,
      executeForm: {
        input: '',
        options: ''
      },

      // 测试文件相关
      selectedTestFile: '',
      selectedTestFileInfo: null,
      availableTestFiles: [],
      testFileIndex: null,

      // 结果对话框
      showResultDialog: false,
      executionResult: null,

      // 自检面板状态
      selfCheck: {
        selected: [],
        running: false,
        results: []
      }
    }
  },

  mounted() {
    this.loadPlugins()
    this.loadPluginStats()
    this.loadTestFileIndex()
  },

  watch: {
    selectedPlugin: {
      handler() {
        this.updateAvailableTestFiles()
      },
      immediate: true
    }
  },

  methods: {

    async runSelfCheck(){
      if(!this.selfCheck.selected.length){ this.$message.info('请选择至少一个插件'); return }
      this.selfCheck.running = true
      this.selfCheck.results = []

      const samples = {
        excel_analyzer: { dataset: [{value:98},{value:100},{value:97},{value:99}] },
        statistical_analyzer: { dataset: [{value:98},{value:100},{value:97},{value:99}] },
        spc_controller: { series: [98,100,97,99,101,98] },
        data_cleaner: { dataset: [{id:1,value:98},{id:1,value:''},{id:2,value:100},{id:3,value:97}] },
        anomaly_detector: { series: [98,100,97,99,101,200,98], method:'iqr' },
        text_summarizer: { text: '质量问题分析是制造业中的核心工作。通过数据分析和SPC控制，我们可以定位异常。最终形成改进建议。' }
      }

      for (const pid of this.selfCheck.selected) {
        const t0 = Date.now()
        try {
          // 直接执行（统一视为已预部署）
          const resp = await apiExecutePlugin(pid, { input: samples[pid] || {} })
          const dur = Date.now()-t0
          const summary = pid==='data_cleaner' ? `cleaned=${(resp.data.cleaned||[]).length}/${resp.data.total}`
                        : pid==='anomaly_detector' ? `anomalies=${(resp.data.anomalies||[]).length}`
                        : pid==='text_summarizer' ? `len=${(resp.data.summary||'').length}`
                        : 'ok'
          this.selfCheck.results.push({ id: pid, name: (this.curatedTools.find(x=>x.id===pid)||{}).name || pid, status:'success', durationMs: dur, summary })
        } catch(e){
          const dur = Date.now()-t0
          this.selfCheck.results.push({ id: pid, name: (this.curatedTools.find(x=>x.id===pid)||{}).name || pid, status:'error', durationMs: dur, summary: e.message })
        }
      }

      this.selfCheck.running = false
    },


    async runQualityDemo(){
      try {
        // 通过场景接口串联执行（后端将自动安装插件并读取示例文件）
        const json = await apiExecuteQualityAnalysisScenario({ filePath: './制造问题洗后版.xlsx', valueField: 'value' })
        if(!json.success){ throw new Error(json.message || '执行失败') }
        this.executionResult = json.data
        this.showResultDialog = true
      } catch(e){ this.$message.error('质量分析执行失败：'+e.message) }
    },
    async loadPlugins() {
      this.loading = true
      try {
        const params = {
          type: this.typeFilter,
          category: this.categoryFilter,
          status: this.statusFilter
        }

        const response = await apiGetPlugins(params)
        if (response.success) {
          this.plugins = response.data.plugins || []
          this.applyFilters()
        }
      } catch (error) {
        this.$message.error('加载插件列表失败：' + error.message)
      } finally {
        this.loading = false
      }
    },

    async loadPluginStats() {
      try {
        const response = await apiGetPluginStats()
        if (response.success) {
          this.pluginStats = response.data
        }
      } catch (error) {
        console.error('加载插件统计失败:', error)
      }
    },

    applyFilters() {
      let filtered = [...this.plugins]

      // 搜索过滤
      if (this.searchKeyword.trim()) {
        const keyword = this.searchKeyword.toLowerCase()
        filtered = filtered.filter(plugin =>
          plugin.name.toLowerCase().includes(keyword) ||
          plugin.description.toLowerCase().includes(keyword) ||
          (plugin.capabilities || []).some(cap => cap.toLowerCase().includes(keyword))
        )
      }

      // 类型过滤
      if (this.typeFilter) {
        filtered = filtered.filter(plugin => plugin.type === this.typeFilter)
      }

      // 分类过滤
      if (this.categoryFilter) {
        filtered = filtered.filter(plugin => plugin.category === this.categoryFilter)
      }

      // 状态过滤：始终仅展示已部署(active)的插件
      filtered = filtered.filter(plugin => plugin.status === 'active')

      this.filteredPlugins = filtered
      this.pagination.total = filtered.length
    },



    executePlugin(plugin) {
      this.selectedPlugin = plugin
      this.executeForm = {
        input: '',
        options: ''
      }
      this.showExecuteDialog = true
    },


    async submitExecution() {
      if (!this.executeForm.input.trim()) {
        this.$message.warning('请输入执行数据')
        return
      }

      this.executing = true
      try {
        let input, options
        try {
          input = JSON.parse(this.executeForm.input)
        } catch (e) {
          input = this.executeForm.input
        }

        try {
          options = this.executeForm.options ? JSON.parse(this.executeForm.options) : {}
        } catch (e) {
          options = {}
        }

        const response = await apiExecutePlugin(this.selectedPlugin.id, {
          input: input,
          options: options
        })

        this.executionResult = response.data
        this.showExecuteDialog = false
        this.showResultDialog = true

        if (response.success) {
          this.$message.success('插件执行完成')
          this.selectedPlugin.usage_count = (this.selectedPlugin.usage_count || 0) + 1
        }
      } catch (error) {
        this.$message.error('执行插件失败：' + error.message)
      } finally {
        this.executing = false
      }
    },

    onTemplateChange(val){
      if(!this.selectedPlugin) return
      const list = (this.selectedPlugin.validation_templates||[])
      const t = list.find(x=>x.id===val || x.name===val)
      if(t){
        try{ this.executeForm.input = JSON.stringify(t.input ?? {}, null, 2) }catch(e){ this.executeForm.input = String(t.input ?? '') }
        try{ this.executeForm.options = JSON.stringify(t.options ?? {}, null, 2) }catch(e){ this.executeForm.options = '' }
      }
    },

    async viewPlugin(plugin) {
      console.log('viewPlugin called with:', plugin)
      try {
        console.log('Calling apiGetPlugin with ID:', plugin.id)
        const response = await apiGetPlugin(plugin.id)
        console.log('API response:', response)
        if (response.success) {
          this.selectedPlugin = response.data
          this.showDetailDialog = true
          console.log('Dialog should be shown, showDetailDialog:', this.showDetailDialog)
        } else {
          console.error('API response not successful:', response)
          this.$message.error('获取插件详情失败：' + (response.message || '未知错误'))
        }
      } catch (error) {
        console.error('viewPlugin error:', error)
        this.$message.error('加载插件详情失败：' + error.message)
      }
    },

    goToValidation(plugin) {
      this.$router.push({ path: '/coze-studio/validation', query: { pluginId: plugin.id } })
    },

    // 测试文件相关方法
    async loadTestFileIndex() {
      try {
        console.log('开始加载测试文件索引...')
        const response = await fetch('/plugin-test-files/file_index.json')
        console.log('文件索引响应:', response.status, response.ok)
        if (response.ok) {
          this.testFileIndex = await response.json()
          console.log('测试文件索引加载成功:', this.testFileIndex)
        } else {
          console.error('文件索引响应失败:', response.status)
        }
      } catch (error) {
        console.error('加载测试文件索引失败:', error)
      }
    },

    updateAvailableTestFiles() {
      if (!this.selectedPlugin || !this.testFileIndex) {
        this.availableTestFiles = []
        return
      }

      const pluginId = this.selectedPlugin.id
      const files = this.testFileIndex.plugin_test_files.files
      let availableFiles = []

      console.log('查找插件测试文件:', pluginId, files)

      // 根据插件类型查找对应的测试文件
      Object.keys(files).forEach(category => {
        Object.keys(files[category]).forEach(filePluginId => {
          console.log('检查文件匹配:', filePluginId, 'vs', pluginId)
          if (filePluginId === pluginId ||
              pluginId === filePluginId ||
              pluginId.startsWith(filePluginId) ||
              filePluginId.startsWith(pluginId)) {
            console.log('匹配成功:', filePluginId)
            availableFiles.push(files[category][filePluginId])
          }
        })
      })

      console.log('可用测试文件:', availableFiles)
      this.availableTestFiles = availableFiles
      this.selectedTestFile = ''
      this.selectedTestFileInfo = null
    },

    onTestFileChange(fileName) {
      if (!fileName) {
        this.selectedTestFileInfo = null
        return
      }

      // 查找文件信息
      const files = this.testFileIndex.plugin_test_files.files
      let fileInfo = null

      Object.keys(files).forEach(category => {
        Object.keys(files[category]).forEach(pluginId => {
          if (files[category][pluginId].file === fileName) {
            fileInfo = files[category][pluginId]
          }
        })
      })

      this.selectedTestFileInfo = fileInfo
    },

    async loadTestFile() {
      if (!this.selectedTestFile) {
        this.$message.warning('请先选择测试文件')
        return
      }

      try {
        const response = await fetch(`/plugin-test-files/${this.selectedTestFile}`)
        if (!response.ok) {
          throw new Error('文件加载失败')
        }

        const content = await response.text()

        // 根据文件格式处理内容
        if (this.selectedTestFile.endsWith('.json')) {
          // JSON文件直接使用
          this.executeForm.input = content
        } else if (this.selectedTestFile.endsWith('.csv')) {
          // CSV文件包装为JSON
          this.executeForm.input = JSON.stringify({ csv: content }, null, 2)
        } else if (this.selectedTestFile.endsWith('.xml')) {
          // XML文件包装为JSON
          this.executeForm.input = JSON.stringify({ xml: content }, null, 2)
        } else {
          // 文本文件包装为JSON
          this.executeForm.input = JSON.stringify({ text: content }, null, 2)
        }

        this.$message.success('测试文件加载成功')
      } catch (error) {
        console.error('加载测试文件失败:', error)
        this.$message.error('加载测试文件失败: ' + error.message)
      }
    },

    async showTestFilePreview() {
      if (!this.selectedTestFile) {
        this.$message.warning('请先选择测试文件')
        return
      }

      try {
        const response = await fetch(`/plugin-test-files/${this.selectedTestFile}`)
        if (!response.ok) {
          throw new Error('文件加载失败')
        }

        const content = await response.text()
        const preview = content.length > 500 ? content.substring(0, 500) + '...' : content

        this.$alert(preview, `文件预览: ${this.selectedTestFileInfo?.name || this.selectedTestFile}`, {
          confirmButtonText: '确定',
          type: 'info',
          customClass: 'test-file-preview-dialog'
        })
      } catch (error) {
        console.error('预览测试文件失败:', error)
        this.$message.error('预览测试文件失败: ' + error.message)
      }
    },

    formatInputData() {
      try {
        const parsed = JSON.parse(this.executeForm.input)
        this.executeForm.input = JSON.stringify(parsed, null, 2)
        this.$message.success('格式化成功')
      } catch (error) {
        this.$message.warning('输入数据不是有效的JSON格式')
      }
    },

    refreshPlugins() {
      this.loadPlugins()
      this.loadPluginStats()
    },

    handleSearch() {
      this.pagination.page = 1
      this.applyFilters()
    },

    handleFilter() {
      this.pagination.page = 1
      this.loadPlugins()
    },

    handleSizeChange(size) {
      this.pagination.limit = size
      this.applyFilters()
    },

    handleCurrentChange(page) {
      this.pagination.page = page
      this.applyFilters()
    },

    getPluginIcon(type) {
      const icons = {
        langchain_tool: 'el-icon-link',
        semantic_kernel: 'el-icon-cpu',
        haystack_component: 'el-icon-document',
        custom_plugin: 'el-icon-setting'
      }
      return icons[type] || 'el-icon-s-grid'
    },

    getTypeColor(type) {
      const colors = {
        langchain_tool: 'primary',
        semantic_kernel: 'success',
        haystack_component: 'warning',
        custom_plugin: 'danger'
      }
      return colors[type] || 'info'
    },

    getTypeText(type) {
      const texts = {
        langchain_tool: 'LangChain工具',
        semantic_kernel: 'Semantic Kernel',
        haystack_component: 'Haystack组件',
        custom_plugin: '自定义插件'
      }
      return texts[type] || type
    },

    getCategoryText(category) {
      const texts = {
        document_processing: '文档处理',
        image_analysis: '图像分析',
        data_analysis: '数据分析',
        quality_control: '质量控制',
        external_integration: '外部集成',
        quality_tools: '质量工具'
      }
      return texts[category] || category
    },

    getConfigProperties(schema) {
      if (!schema || !schema.properties) return []

      return Object.entries(schema.properties).map(([name, prop]) => ({
        name,
        type: prop.type || 'string',
        default: prop.default !== undefined ? String(prop.default) : '-',
        description: prop.description || '-'
      }))
    },

    formatTime(time) {
      if (!time) return '-'
      return new Date(time).toLocaleString()
    }
  }
}
</script>

<style scoped>
.plugin-ecosystem-manager {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.page-title i {
  margin-right: 8px;
  color: #409EFF;
}

.page-description {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.plugin-stats {
  margin-bottom: 20px;
}

.stat-card {
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  color: white;
}

.stat-icon.total { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-icon.active { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.stat-icon.categories { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.stat-icon.popular { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }

.stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.plugins-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.plugins-grid {
  min-height: 400px;
}

.plugin-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #e4e7ed;
}

.plugin-card:hover {
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.plugin-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.plugin-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #409EFF;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  margin-right: 12px;
}

.plugin-info {
  flex: 1;
}

.plugin-name {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.plugin-author {
  color: #909399;
  font-size: 12px;
}

.plugin-description {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 12px;
  min-height: 42px;
}

.plugin-meta {
  margin-bottom: 12px;
}

.plugin-capabilities {
  margin-bottom: 12px;
  min-height: 24px;
}

.more-capabilities {
  color: #909399;
  font-size: 12px;
}



.usage-count i {
  margin-right: 4px;
}

.plugin-actions {
  display: flex;
  gap: 8px;
}

.pagination-wrapper {
  margin-top: 20px;
  text-align: right;
}

.plugin-detail {
  max-height: 600px;
  overflow-y: auto;
}

.capabilities-section h4,
.config-section h4 {
  margin: 0 0 12px 0;
  color: #303133;
}

.capabilities-list {
  line-height: 2;
}

.execution-result {
  max-height: 500px;
  overflow-y: auto;
}

.result-json {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.5;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-all;
}

/* 测试文件选择器样式 */
.test-file-selector {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.test-file-description {
  margin-top: 8px;
}

.input-data-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 测试文件预览对话框样式 */
.test-file-preview-dialog .el-message-box__content {
  max-height: 400px;
  overflow-y: auto;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  white-space: pre-wrap;
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
}
</style>
