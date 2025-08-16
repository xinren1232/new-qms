<template>
  <div class="coze-plugins">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <el-icon size="48"><Cpu /></el-icon>
        </div>
        <div class="header-text">
          <h1>Coze Studio 插件系统</h1>
          <p>基于官方Coze Studio平台的插件管理和开发环境</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button @click="refreshPlugins" :icon="Refresh" :loading="loading">刷新</el-button>
        <el-button @click="openCozeStudio" type="primary" :icon="Link">打开Coze Studio</el-button>
      </div>
    </div>

    <!-- 插件概览 -->
    <div class="plugins-overview">
      <div class="overview-card">
        <div class="overview-icon">
          <el-icon><Grid /></el-icon>
        </div>
        <div class="overview-content">
          <div class="overview-value">{{ installedPlugins.length }}</div>
          <div class="overview-label">已安装插件</div>
        </div>
      </div>
      <div class="overview-card">
        <div class="overview-icon">
          <el-icon><Check /></el-icon>
        </div>
        <div class="overview-content">
          <div class="overview-value">{{ authorizedPlugins.length }}</div>
          <div class="overview-label">已授权插件</div>
        </div>
      </div>
      <div class="overview-card">
        <div class="overview-icon">
          <el-icon><Star /></el-icon>
        </div>
        <div class="overview-content">
          <div class="overview-value">{{ officialPlugins.length }}</div>
          <div class="overview-label">官方插件</div>
        </div>
      </div>
      <div class="overview-card">
        <div class="overview-icon">
          <el-icon><User /></el-icon>
        </div>
        <div class="overview-content">
          <div class="overview-value">{{ customPlugins.length }}</div>
          <div class="overview-label">自定义插件</div>
        </div>
      </div>
    </div>

    <!-- 主要内容 -->
    <div class="main-content">
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 插件市场 -->
        <el-tab-pane label="插件市场" name="market">
          <div class="market-section">
            <div class="section-header">
              <h3>官方插件市场</h3>
              <div class="header-filters">
                <el-select v-model="categoryFilter" placeholder="选择分类" style="width: 150px">
                  <el-option label="全部分类" value="" />
                  <el-option label="办公协作" value="office" />
                  <el-option label="数据分析" value="data" />
                  <el-option label="文档处理" value="document" />
                  <el-option label="AI工具" value="ai" />
                  <el-option label="开发工具" value="dev" />
                </el-select>
                <el-input
                  v-model="searchQuery"
                  placeholder="搜索插件..."
                  :prefix-icon="Search"
                  style="width: 300px"
                />
              </div>
            </div>

            <div class="plugins-grid">
              <div
                v-for="plugin in filteredMarketPlugins"
                :key="plugin.id"
                class="plugin-card market-plugin"
                :class="{ 'installed': plugin.installed }"
              >
                <div class="plugin-header">
                  <div class="plugin-icon">
                    <el-icon><component :is="plugin.icon || 'Grid'" /></el-icon>
                  </div>
                  <div class="plugin-info">
                    <h4>{{ plugin.name }}</h4>
                    <p>{{ plugin.description }}</p>
                    <div class="plugin-meta">
                      <el-tag :type="plugin.official ? 'primary' : 'info'" size="small">
                        {{ plugin.official ? '官方' : '第三方' }}
                      </el-tag>
                      <el-tag v-if="plugin.category" size="small">{{ getCategoryName(plugin.category) }}</el-tag>
                    </div>
                  </div>
                </div>

                <div class="plugin-stats">
                  <div class="stat-item">
                    <el-icon><Download /></el-icon>
                    <span>{{ plugin.downloads }}</span>
                  </div>
                  <div class="stat-item">
                    <el-icon><Star /></el-icon>
                    <span>{{ plugin.rating }}</span>
                  </div>
                  <div class="stat-item">
                    <el-icon><Calendar /></el-icon>
                    <span>{{ plugin.lastUpdate }}</span>
                  </div>
                </div>

                <div class="plugin-actions">
                  <el-button
                    v-if="!plugin.installed"
                    @click="installPlugin(plugin)"
                    type="primary"
                    size="small"
                    :loading="plugin.installing"
                  >
                    安装
                  </el-button>
                  <el-button
                    v-else
                    @click="uninstallPlugin(plugin)"
                    size="small"
                    :loading="plugin.uninstalling"
                  >
                    卸载
                  </el-button>
                  <el-button @click="viewPluginDetails(plugin)" size="small" text>
                    详情
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 已安装插件 -->
        <el-tab-pane label="已安装插件" name="installed">
          <div class="installed-section">
            <div class="section-header">
              <h3>已安装插件</h3>
              <el-button @click="checkUpdates" :loading="checkingUpdates">检查更新</el-button>
            </div>

            <div class="plugins-grid">
              <div
                v-for="plugin in installedPlugins"
                :key="plugin.id"
                class="plugin-card installed-plugin"
                :class="{ 
                  'authorized': plugin.authorized,
                  'unauthorized': !plugin.authorized && plugin.requiresAuth
                }"
              >
                <div class="plugin-header">
                  <div class="plugin-icon">
                    <el-icon><component :is="plugin.icon || 'Grid'" /></el-icon>
                  </div>
                  <div class="plugin-info">
                    <h4>{{ plugin.name }}</h4>
                    <p>{{ plugin.description }}</p>
                    <div class="plugin-status">
                      <el-tag
                        :type="getStatusType(plugin)"
                        size="small"
                      >
                        {{ getStatusText(plugin) }}
                      </el-tag>
                      <el-tag v-if="plugin.hasUpdate" type="warning" size="small">
                        有更新
                      </el-tag>
                    </div>
                  </div>
                  <div class="plugin-toggle">
                    <el-switch
                      v-model="plugin.enabled"
                      @change="togglePlugin(plugin)"
                      :disabled="!plugin.authorized && plugin.requiresAuth"
                    />
                  </div>
                </div>

                <div class="plugin-config" v-if="plugin.requiresAuth">
                  <div class="config-item">
                    <span class="config-label">认证状态:</span>
                    <span class="config-value" :class="{ 'error': !plugin.authorized }">
                      {{ plugin.authorized ? '已授权' : '未授权' }}
                    </span>
                  </div>
                  <div class="config-item" v-if="plugin.apiKeyRequired">
                    <span class="config-label">API密钥:</span>
                    <span class="config-value">
                      {{ plugin.apiKey ? '已配置' : '未配置' }}
                    </span>
                  </div>
                </div>

                <div class="plugin-actions">
                  <el-button
                    v-if="!plugin.authorized && plugin.requiresAuth"
                    @click="authorizePlugin(plugin)"
                    type="primary"
                    size="small"
                    :loading="plugin.authorizing"
                  >
                    授权
                  </el-button>
                  <el-button
                    @click="configurePlugin(plugin)"
                    size="small"
                    :disabled="!plugin.authorized && plugin.requiresAuth"
                  >
                    配置
                  </el-button>
                  <el-button
                    v-if="plugin.hasUpdate"
                    @click="updatePlugin(plugin)"
                    size="small"
                    type="warning"
                    :loading="plugin.updating"
                  >
                    更新
                  </el-button>
                  <el-button @click="uninstallPlugin(plugin)" size="small" text>
                    卸载
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 开发工具 -->
        <el-tab-pane label="开发工具" name="development">
          <div class="development-section">
            <div class="section-header">
              <h3>插件开发</h3>
              <el-button @click="createNewPlugin" type="primary" :icon="Plus">创建插件</el-button>
            </div>

            <div class="dev-tools">
              <el-card class="tool-card">
                <div class="tool-icon">
                  <el-icon><Edit /></el-icon>
                </div>
                <h4>插件编辑器</h4>
                <p>在线编辑和调试插件代码</p>
                <el-button @click="openPluginEditor" size="small">打开编辑器</el-button>
              </el-card>

              <el-card class="tool-card">
                <div class="tool-icon">
                  <el-icon><DocumentCopy /></el-icon>
                </div>
                <h4>API文档</h4>
                <p>查看Coze Studio插件开发文档</p>
                <el-button @click="openApiDocs" size="small">查看文档</el-button>
              </el-card>

              <el-card class="tool-card">
                <div class="tool-icon">
                  <el-icon><Monitor /></el-icon>
                </div>
                <h4>调试工具</h4>
                <p>测试和调试插件功能</p>
                <el-button @click="openDebugger" size="small">打开调试器</el-button>
              </el-card>

              <el-card class="tool-card">
                <div class="tool-icon">
                  <el-icon><Upload /></el-icon>
                </div>
                <h4>插件发布</h4>
                <p>将插件发布到官方市场</p>
                <el-button @click="publishPlugin" size="small">发布插件</el-button>
              </el-card>
            </div>

            <!-- 我的插件项目 -->
            <div class="my-plugins">
              <h4>我的插件项目</h4>
              <el-table :data="myPluginProjects" style="width: 100%">
                <el-table-column prop="name" label="插件名称" />
                <el-table-column prop="version" label="版本" width="100" />
                <el-table-column prop="status" label="状态" width="120">
                  <template #default="scope">
                    <el-tag :type="getProjectStatusType(scope.row.status)" size="small">
                      {{ scope.row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="lastModified" label="最后修改" width="180" />
                <el-table-column label="操作" width="200">
                  <template #default="scope">
                    <el-button @click="editProject(scope.row)" size="small" text>编辑</el-button>
                    <el-button @click="testProject(scope.row)" size="small" text>测试</el-button>
                    <el-button @click="deployProject(scope.row)" size="small" text>部署</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-tab-pane>

        <!-- 插件日志 -->
        <el-tab-pane label="插件日志" name="logs">
          <div class="logs-section">
            <div class="section-header">
              <h3>插件运行日志</h3>
              <div class="header-filters">
                <el-select v-model="logLevelFilter" placeholder="日志级别" style="width: 120px">
                  <el-option label="全部" value="" />
                  <el-option label="错误" value="error" />
                  <el-option label="警告" value="warning" />
                  <el-option label="信息" value="info" />
                  <el-option label="调试" value="debug" />
                </el-select>
                <el-select v-model="logPluginFilter" placeholder="选择插件" style="width: 200px">
                  <el-option label="全部插件" value="" />
                  <el-option
                    v-for="plugin in installedPlugins"
                    :key="plugin.id"
                    :label="plugin.name"
                    :value="plugin.id"
                  />
                </el-select>
                <el-button @click="clearLogs" size="small">清空日志</el-button>
              </div>
            </div>

            <div class="logs-container">
              <div
                v-for="log in filteredLogs"
                :key="log.id"
                class="log-entry"
                :class="log.level"
              >
                <div class="log-time">{{ log.timestamp }}</div>
                <div class="log-plugin">{{ log.pluginName }}</div>
                <div class="log-level">{{ log.level.toUpperCase() }}</div>
                <div class="log-message">{{ log.message }}</div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 插件配置对话框 -->
    <el-dialog
      v-model="showConfigDialog"
      :title="`配置 ${selectedPlugin?.name}`"
      width="600px"
    >
      <div v-if="selectedPlugin" class="plugin-config-form">
        <el-form :model="pluginConfig" label-width="120px">
          <el-form-item v-if="selectedPlugin.apiKeyRequired" label="API密钥" required>
            <el-input
              v-model="pluginConfig.apiKey"
              type="password"
              placeholder="输入API密钥"
              show-password
            />
            <div class="config-help">
              <el-text type="info" size="small">
                请在 {{ selectedPlugin.authProvider }} 获取API密钥
              </el-text>
            </div>
          </el-form-item>
          
          <el-form-item v-if="selectedPlugin.webhookRequired" label="Webhook URL">
            <el-input
              v-model="pluginConfig.webhookUrl"
              placeholder="输入Webhook地址"
            />
          </el-form-item>

          <el-form-item label="启用状态">
            <el-switch v-model="pluginConfig.enabled" />
          </el-form-item>

          <el-form-item label="配置选项" v-if="selectedPlugin.configOptions">
            <div class="config-options">
              <div
                v-for="option in selectedPlugin.configOptions"
                :key="option.key"
                class="config-option"
              >
                <label>{{ option.label }}:</label>
                <el-input
                  v-if="option.type === 'text'"
                  v-model="pluginConfig.options[option.key]"
                  :placeholder="option.placeholder"
                />
                <el-switch
                  v-else-if="option.type === 'boolean'"
                  v-model="pluginConfig.options[option.key]"
                />
                <el-input-number
                  v-else-if="option.type === 'number'"
                  v-model="pluginConfig.options[option.key]"
                  :min="option.min"
                  :max="option.max"
                />
              </div>
            </div>
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <el-button @click="showConfigDialog = false">取消</el-button>
        <el-button @click="testPluginConfig" :loading="testingConfig">测试配置</el-button>
        <el-button @click="savePluginConfig" type="primary" :loading="savingConfig">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Refresh, Link, Grid, Check, Star, User, Search,
  Download, Calendar, Plus, Edit, DocumentCopy,
  Monitor, Upload, Cpu, Document
} from '@element-plus/icons-vue'

// 响应式数据
const activeTab = ref('market')
const loading = ref(false)
const checkingUpdates = ref(false)
const testingConfig = ref(false)
const savingConfig = ref(false)

const showConfigDialog = ref(false)
const selectedPlugin = ref(null)

const categoryFilter = ref('')
const searchQuery = ref('')
const logLevelFilter = ref('')
const logPluginFilter = ref('')

// 插件市场数据
const marketPlugins = ref([
  {
    id: 'feishu-docs',
    name: '飞书云文档',
    description: '集成飞书云文档，支持文档创建、编辑和分享',
    icon: 'Document',
    category: 'office',
    official: true,
    downloads: '10.2k',
    rating: '4.8',
    lastUpdate: '2025-01-05',
    installed: true,
    requiresAuth: true,
    apiKeyRequired: true,
    authProvider: '飞书开放平台'
  },
  {
    id: 'github-integration',
    name: 'GitHub集成',
    description: '连接GitHub仓库，管理代码和问题',
    icon: 'Monitor',
    category: 'dev',
    official: true,
    downloads: '8.5k',
    rating: '4.6',
    lastUpdate: '2025-01-03',
    installed: false,
    requiresAuth: true,
    apiKeyRequired: true,
    authProvider: 'GitHub'
  },
  {
    id: 'data-analyzer',
    name: '数据分析器',
    description: '强大的数据分析和可视化工具',
    icon: 'Grid',
    category: 'data',
    official: true,
    downloads: '6.8k',
    rating: '4.7',
    lastUpdate: '2025-01-01',
    installed: true,
    requiresAuth: false
  },
  {
    id: 'pdf-processor',
    name: 'PDF处理器',
    description: '处理PDF文档，支持提取、转换和编辑',
    icon: 'DocumentCopy',
    category: 'document',
    official: false,
    downloads: '3.2k',
    rating: '4.3',
    lastUpdate: '2024-12-28',
    installed: false,
    requiresAuth: false
  }
])

// 已安装插件数据
const installedPlugins = ref([
  {
    id: 'feishu-docs',
    name: '飞书云文档',
    description: '集成飞书云文档，支持文档创建、编辑和分享',
    icon: 'Document',
    enabled: true,
    authorized: false,
    requiresAuth: true,
    apiKeyRequired: true,
    authProvider: '飞书开放平台',
    hasUpdate: false,
    version: '1.2.0'
  },
  {
    id: 'data-analyzer',
    name: '数据分析器',
    description: '强大的数据分析和可视化工具',
    icon: 'Grid',
    enabled: true,
    authorized: true,
    requiresAuth: false,
    hasUpdate: true,
    version: '2.1.0'
  }
])

// 我的插件项目
const myPluginProjects = ref([
  {
    id: 1,
    name: 'QMS质量管理插件',
    version: '1.0.0',
    status: '开发中',
    lastModified: '2025-01-08 15:30'
  },
  {
    id: 2,
    name: '自定义报表生成器',
    version: '0.8.0',
    status: '测试中',
    lastModified: '2025-01-07 10:20'
  }
])

// 插件日志
const pluginLogs = ref([
  {
    id: 1,
    timestamp: '2025-01-08 15:30:25',
    pluginName: '飞书云文档',
    level: 'error',
    message: 'API密钥验证失败，请检查配置'
  },
  {
    id: 2,
    timestamp: '2025-01-08 15:25:10',
    pluginName: '数据分析器',
    level: 'info',
    message: '数据分析任务完成，生成报告'
  },
  {
    id: 3,
    timestamp: '2025-01-08 15:20:05',
    pluginName: '飞书云文档',
    level: 'warning',
    message: '文档同步延迟，正在重试'
  }
])

// 插件配置
const pluginConfig = reactive({
  apiKey: '',
  webhookUrl: '',
  enabled: true,
  options: {}
})

// 计算属性
const authorizedPlugins = computed(() => installedPlugins.value.filter(p => p.authorized))
const officialPlugins = computed(() => marketPlugins.value.filter(p => p.official))
const customPlugins = computed(() => marketPlugins.value.filter(p => !p.official))

const filteredMarketPlugins = computed(() => {
  let filtered = marketPlugins.value
  
  if (categoryFilter.value) {
    filtered = filtered.filter(p => p.category === categoryFilter.value)
  }
  
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(p => 
      p.name.toLowerCase().includes(query) ||
      p.description.toLowerCase().includes(query)
    )
  }
  
  return filtered
})

const filteredLogs = computed(() => {
  let filtered = pluginLogs.value
  
  if (logLevelFilter.value) {
    filtered = filtered.filter(log => log.level === logLevelFilter.value)
  }
  
  if (logPluginFilter.value) {
    const plugin = installedPlugins.value.find(p => p.id === logPluginFilter.value)
    if (plugin) {
      filtered = filtered.filter(log => log.pluginName === plugin.name)
    }
  }
  
  return filtered
})

// 方法定义
const refreshPlugins = async () => {
  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('插件列表已刷新')
  } catch (error) {
    ElMessage.error('刷新失败')
  } finally {
    loading.value = false
  }
}

const openCozeStudio = () => {
  window.open('https://www.coze.cn/studio', '_blank')
  ElMessage.info('正在打开Coze Studio官方平台')
}

const installPlugin = async (plugin) => {
  plugin.installing = true
  try {
    await new Promise(resolve => setTimeout(resolve, 2000))
    plugin.installed = true
    
    // 添加到已安装列表
    installedPlugins.value.push({
      ...plugin,
      enabled: false,
      authorized: !plugin.requiresAuth,
      hasUpdate: false,
      version: '1.0.0'
    })
    
    ElMessage.success(`${plugin.name} 安装成功`)
  } catch (error) {
    ElMessage.error(`${plugin.name} 安装失败`)
  } finally {
    plugin.installing = false
  }
}

const uninstallPlugin = async (plugin) => {
  try {
    await ElMessageBox.confirm(`确定要卸载 ${plugin.name} 吗？`, '确认卸载', {
      type: 'warning'
    })
    
    plugin.uninstalling = true
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // 从已安装列表移除
    const index = installedPlugins.value.findIndex(p => p.id === plugin.id)
    if (index > -1) {
      installedPlugins.value.splice(index, 1)
    }
    
    // 更新市场插件状态
    const marketPlugin = marketPlugins.value.find(p => p.id === plugin.id)
    if (marketPlugin) {
      marketPlugin.installed = false
    }
    
    ElMessage.success(`${plugin.name} 卸载成功`)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${plugin.name} 卸载失败`)
    }
  } finally {
    plugin.uninstalling = false
  }
}

const togglePlugin = async (plugin) => {
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success(`${plugin.name} 已${plugin.enabled ? '启用' : '禁用'}`)
  } catch (error) {
    plugin.enabled = !plugin.enabled
    ElMessage.error('操作失败')
  }
}

const authorizePlugin = async (plugin) => {
  plugin.authorizing = true
  try {
    // 模拟OAuth授权流程
    await new Promise(resolve => setTimeout(resolve, 2000))
    plugin.authorized = true
    ElMessage.success(`${plugin.name} 授权成功`)
  } catch (error) {
    ElMessage.error(`${plugin.name} 授权失败`)
  } finally {
    plugin.authorizing = false
  }
}

const configurePlugin = (plugin) => {
  selectedPlugin.value = plugin
  Object.assign(pluginConfig, {
    apiKey: plugin.apiKey || '',
    webhookUrl: plugin.webhookUrl || '',
    enabled: plugin.enabled,
    options: { ...plugin.options } || {}
  })
  showConfigDialog.value = true
}

const testPluginConfig = async () => {
  testingConfig.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 2000))
    ElMessage.success('配置测试成功')
  } catch (error) {
    ElMessage.error('配置测试失败')
  } finally {
    testingConfig.value = false
  }
}

const savePluginConfig = async () => {
  savingConfig.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 更新插件配置
    if (selectedPlugin.value) {
      Object.assign(selectedPlugin.value, {
        apiKey: pluginConfig.apiKey,
        webhookUrl: pluginConfig.webhookUrl,
        enabled: pluginConfig.enabled,
        options: { ...pluginConfig.options },
        authorized: pluginConfig.apiKey ? true : selectedPlugin.value.authorized
      })
    }
    
    ElMessage.success('配置保存成功')
    showConfigDialog.value = false
  } catch (error) {
    ElMessage.error('配置保存失败')
  } finally {
    savingConfig.value = false
  }
}

const checkUpdates = async () => {
  checkingUpdates.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 2000))
    ElMessage.success('检查更新完成')
  } catch (error) {
    ElMessage.error('检查更新失败')
  } finally {
    checkingUpdates.value = false
  }
}

const updatePlugin = async (plugin) => {
  plugin.updating = true
  try {
    await new Promise(resolve => setTimeout(resolve, 2000))
    plugin.hasUpdate = false
    plugin.version = '2.2.0'
    ElMessage.success(`${plugin.name} 更新成功`)
  } catch (error) {
    ElMessage.error(`${plugin.name} 更新失败`)
  } finally {
    plugin.updating = false
  }
}

const viewPluginDetails = (plugin) => {
  ElMessage.info(`查看插件详情: ${plugin.name}`)
}

const createNewPlugin = () => {
  ElMessage.info('创建新插件功能')
}

const openPluginEditor = () => {
  ElMessage.info('打开插件编辑器')
}

const openApiDocs = () => {
  window.open('https://www.coze.cn/docs', '_blank')
}

const openDebugger = () => {
  ElMessage.info('打开调试工具')
}

const publishPlugin = () => {
  ElMessage.info('发布插件到市场')
}

const editProject = (project) => {
  ElMessage.info(`编辑项目: ${project.name}`)
}

const testProject = (project) => {
  ElMessage.info(`测试项目: ${project.name}`)
}

const deployProject = (project) => {
  ElMessage.info(`部署项目: ${project.name}`)
}

const clearLogs = () => {
  pluginLogs.value = []
  ElMessage.success('日志已清空')
}

const getCategoryName = (category) => {
  const categoryMap = {
    office: '办公协作',
    data: '数据分析',
    document: '文档处理',
    ai: 'AI工具',
    dev: '开发工具'
  }
  return categoryMap[category] || category
}

const getStatusType = (plugin) => {
  if (!plugin.requiresAuth) return 'success'
  return plugin.authorized ? 'success' : 'danger'
}

const getStatusText = (plugin) => {
  if (!plugin.requiresAuth) return '正常'
  return plugin.authorized ? '已授权' : '未授权'
}

const getProjectStatusType = (status) => {
  const statusMap = {
    '开发中': 'primary',
    '测试中': 'warning',
    '已发布': 'success',
    '已暂停': 'info'
  }
  return statusMap[status] || 'info'
}

// 生命周期
onMounted(() => {
  // 初始化
})
</script>

<style scoped>
.coze-plugins {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon img {
  width: 48px;
  height: 48px;
  border-radius: 8px;
}

.header-text h1 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 28px;
  font-weight: 600;
}

.header-text p {
  margin: 0;
  color: #606266;
  font-size: 16px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.plugins-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.overview-card {
  display: flex;
  align-items: center;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.overview-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #409eff;
  border-radius: 12px;
  margin-right: 16px;
}

.overview-icon .el-icon {
  font-size: 24px;
  color: white;
}

.overview-value {
  font-size: 32px;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 4px;
}

.overview-label {
  font-size: 14px;
  color: #606266;
}

.main-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.market-section,
.installed-section,
.development-section,
.logs-section {
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 20px;
  font-weight: 600;
}

.header-filters {
  display: flex;
  gap: 12px;
  align-items: center;
}

.plugins-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 24px;
}

.plugin-card {
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s ease;
}

.plugin-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

.plugin-card.installed {
  border-color: #67c23a;
  background: #f0f9ff;
}

.plugin-card.authorized {
  border-color: #67c23a;
  background: #f0f9ff;
}

.plugin-card.unauthorized {
  border-color: #f56c6c;
  background: #fef0f0;
}

.plugin-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.plugin-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.plugin-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.plugin-icon img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.plugin-icon .el-icon {
  font-size: 24px;
  color: #909399;
}

.plugin-info h4 {
  margin: 0 0 4px 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}

.plugin-info p {
  margin: 0 0 8px 0;
  color: #606266;
  font-size: 14px;
}

.plugin-meta,
.plugin-status {
  display: flex;
  gap: 8px;
}

.plugin-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #606266;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.plugin-config {
  margin-bottom: 16px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.config-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
}

.config-item:last-child {
  margin-bottom: 0;
}

.config-label {
  color: #606266;
  font-weight: 500;
}

.config-value {
  color: #2c3e50;
}

.config-value.error {
  color: #f56c6c;
}

.plugin-actions {
  display: flex;
  gap: 8px;
}

.dev-tools {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.tool-card {
  text-align: center;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tool-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

.tool-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #409eff;
  border-radius: 12px;
  margin: 0 auto 16px;
}

.tool-icon .el-icon {
  font-size: 24px;
  color: white;
}

.tool-card h4 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}

.tool-card p {
  margin: 0 0 16px 0;
  color: #606266;
  font-size: 14px;
}

.my-plugins {
  margin-top: 32px;
}

.my-plugins h4 {
  margin: 0 0 16px 0;
  color: #2c3e50;
  font-size: 18px;
  font-weight: 600;
}

.logs-container {
  background: #1e1e1e;
  border-radius: 8px;
  padding: 16px;
  max-height: 400px;
  overflow-y: auto;
  font-family: 'Courier New', monospace;
}

.log-entry {
  display: grid;
  grid-template-columns: 180px 150px 80px 1fr;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #333;
  font-size: 12px;
}

.log-entry:last-child {
  border-bottom: none;
}

.log-time {
  color: #888;
}

.log-plugin {
  color: #61dafb;
}

.log-level {
  font-weight: bold;
}

.log-entry.error .log-level {
  color: #ff6b6b;
}

.log-entry.warning .log-level {
  color: #ffa726;
}

.log-entry.info .log-level {
  color: #4fc3f7;
}

.log-entry.debug .log-level {
  color: #81c784;
}

.log-message {
  color: #fff;
}

.plugin-config-form {
  max-height: 400px;
  overflow-y: auto;
}

.config-help {
  margin-top: 8px;
}

.config-options {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.config-option {
  display: flex;
  align-items: center;
  gap: 12px;
}

.config-option label {
  min-width: 120px;
  color: #606266;
  font-weight: 500;
}
</style>
