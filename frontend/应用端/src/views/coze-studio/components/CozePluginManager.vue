<template>
  <div class="coze-plugin-manager">
    <!-- 工具栏 -->
    <div class="plugin-toolbar">
      <div class="toolbar-left">
        <h3>插件管理</h3>
        <el-tag type="info">{{ installedPlugins.length }} 个已安装</el-tag>
      </div>
      <div class="toolbar-right">
        <el-button :icon="Plus" @click="showCreateDialog = true">创建插件</el-button>
        <el-button :icon="Download" @click="importPlugin">导入插件</el-button>
      </div>
    </div>
    
    <!-- 主要内容 -->
    <div class="plugin-main">
      <!-- 插件分类标签 -->
      <div class="plugin-tabs">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="已安装" name="installed" />
          <el-tab-pane label="插件市场" name="market" />
          <el-tab-pane label="我的插件" name="my" />
        </el-tabs>
      </div>
      
      <!-- 插件列表 -->
      <div class="plugin-content">
        <!-- 已安装插件 -->
        <div v-if="activeTab === 'installed'" class="plugin-grid">
          <div
            v-for="plugin in installedPlugins"
            :key="plugin.id"
            class="plugin-card installed"
          >
            <div class="plugin-header">
              <div class="plugin-icon">
                <img v-if="plugin.icon" :src="plugin.icon" />
                <el-icon v-else><Tools /></el-icon>
              </div>
              <div class="plugin-info">
                <h4>{{ plugin.name }}</h4>
                <p>{{ plugin.description }}</p>
              </div>
              <div class="plugin-status">
                <el-switch
                  v-model="plugin.enabled"
                  @change="togglePlugin(plugin)"
                />
              </div>
            </div>
            
            <div class="plugin-meta">
              <el-tag size="small" :type="getCategoryType(plugin.category)">
                {{ plugin.category }}
              </el-tag>
              <span class="plugin-version">v{{ plugin.version }}</span>
            </div>
            
            <div class="plugin-actions">
              <el-button size="small" @click="configurePlugin(plugin)">配置</el-button>
              <el-button size="small" @click="viewPlugin(plugin)">详情</el-button>
              <el-button size="small" type="danger" @click="uninstallPlugin(plugin)">
                卸载
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- 插件市场 -->
        <div v-if="activeTab === 'market'" class="plugin-grid">
          <div
            v-for="plugin in marketPlugins"
            :key="plugin.id"
            class="plugin-card market"
          >
            <div class="plugin-header">
              <div class="plugin-icon">
                <img v-if="plugin.icon" :src="plugin.icon" />
                <el-icon v-else><Tools /></el-icon>
              </div>
              <div class="plugin-info">
                <h4>{{ plugin.name }}</h4>
                <p>{{ plugin.description }}</p>
              </div>
              <div class="plugin-rating">
                <el-rate v-model="plugin.rating" disabled size="small" />
                <span class="rating-text">({{ plugin.downloads }})</span>
              </div>
            </div>
            
            <div class="plugin-meta">
              <el-tag size="small" :type="getCategoryType(plugin.category)">
                {{ plugin.category }}
              </el-tag>
              <span class="plugin-price">{{ plugin.price === 0 ? '免费' : `¥${plugin.price}` }}</span>
            </div>
            
            <div class="plugin-actions">
              <el-button size="small" @click="viewPlugin(plugin)">详情</el-button>
              <el-button
                size="small"
                type="primary"
                @click="installPlugin(plugin)"
                :loading="plugin.installing"
              >
                {{ plugin.price === 0 ? '安装' : '购买' }}
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- 我的插件 -->
        <div v-if="activeTab === 'my'" class="plugin-grid">
          <div
            v-for="plugin in myPlugins"
            :key="plugin.id"
            class="plugin-card my"
          >
            <div class="plugin-header">
              <div class="plugin-icon">
                <img v-if="plugin.icon" :src="plugin.icon" />
                <el-icon v-else><Tools /></el-icon>
              </div>
              <div class="plugin-info">
                <h4>{{ plugin.name }}</h4>
                <p>{{ plugin.description }}</p>
              </div>
              <div class="plugin-status">
                <el-tag :type="getStatusType(plugin.status)">
                  {{ getStatusText(plugin.status) }}
                </el-tag>
              </div>
            </div>
            
            <div class="plugin-meta">
              <el-tag size="small" :type="getCategoryType(plugin.category)">
                {{ plugin.category }}
              </el-tag>
              <span class="plugin-version">v{{ plugin.version }}</span>
            </div>
            
            <div class="plugin-actions">
              <el-button size="small" @click="editPlugin(plugin)">编辑</el-button>
              <el-button size="small" @click="publishPlugin(plugin)">发布</el-button>
              <el-button size="small" type="danger" @click="deletePlugin(plugin)">
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 创建插件对话框 -->
    <el-dialog v-model="showCreateDialog" title="创建插件" width="600px">
      <el-form :model="newPlugin" label-width="100px">
        <el-form-item label="插件名称" required>
          <el-input v-model="newPlugin.name" placeholder="输入插件名称" />
        </el-form-item>
        <el-form-item label="插件描述" required>
          <el-input
            v-model="newPlugin.description"
            type="textarea"
            :rows="3"
            placeholder="描述插件功能"
          />
        </el-form-item>
        <el-form-item label="插件类型" required>
          <el-select v-model="newPlugin.category" placeholder="选择插件类型">
            <el-option label="API工具" value="api" />
            <el-option label="数据处理" value="data" />
            <el-option label="文件操作" value="file" />
            <el-option label="通知服务" value="notification" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="插件图标">
          <el-upload
            class="icon-uploader"
            :show-file-list="false"
            :on-success="handleIconSuccess"
            action="/api/upload"
          >
            <img v-if="newPlugin.icon" :src="newPlugin.icon" class="icon" />
            <el-icon v-else class="icon-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createPlugin">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Plus, Download, Tools } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// Props
const props = defineProps({
  project: {
    type: Object,
    default: null
  }
})

// Emits
const emit = defineEmits(['install', 'configure'])

// 响应式数据
const activeTab = ref('installed')
const showCreateDialog = ref(false)

// 新插件表单
const newPlugin = reactive({
  name: '',
  description: '',
  category: '',
  icon: ''
})

// 已安装插件
const installedPlugins = reactive([
  {
    id: 1,
    name: 'HTTP请求工具',
    description: '发送HTTP请求并处理响应',
    category: 'api',
    version: '1.2.0',
    enabled: true,
    icon: null
  },
  {
    id: 2,
    name: '文件上传助手',
    description: '上传文件到云存储服务',
    category: 'file',
    version: '2.1.0',
    enabled: true,
    icon: null
  },
  {
    id: 3,
    name: '邮件通知',
    description: '发送邮件通知',
    category: 'notification',
    version: '1.0.5',
    enabled: false,
    icon: null
  },
  {
    id: 4,
    name: '飞书集成',
    description: '集成飞书API，支持消息推送、文档操作、日程管理',
    category: 'collaboration',
    version: '2.0.0',
    enabled: true,
    icon: '/icons/feishu-logo.png',
    features: ['webhook', 'message', 'document', 'calendar', 'bot']
  },
  {
    id: 5,
    name: 'MCP服务连接器',
    description: '连接Model Context Protocol服务，扩展AI能力',
    category: 'ai',
    version: '1.0.0',
    enabled: true,
    icon: '/icons/mcp-logo.png',
    features: ['protocol', 'context', 'tools']
  }
])

// 插件市场
const marketPlugins = reactive([
  {
    id: 101,
    name: '微信机器人',
    description: '集成微信机器人功能',
    category: 'notification',
    version: '1.0.0',
    price: 0,
    rating: 4.5,
    downloads: 1250,
    icon: null,
    installing: false
  },
  {
    id: 102,
    name: '数据库连接器',
    description: '连接各种数据库',
    category: 'data',
    version: '2.0.0',
    price: 99,
    rating: 4.8,
    downloads: 890,
    icon: null,
    installing: false
  },
  {
    id: 103,
    name: 'PDF生成器',
    description: '生成PDF文档',
    category: 'file',
    version: '1.5.0',
    price: 0,
    rating: 4.2,
    downloads: 2100,
    icon: null,
    installing: false
  }
])

// 我的插件
const myPlugins = reactive([
  {
    id: 201,
    name: '自定义API工具',
    description: '我创建的API工具',
    category: 'api',
    version: '1.0.0',
    status: 'draft',
    icon: null
  },
  {
    id: 202,
    name: '质量检测插件',
    description: '质量管理专用插件',
    category: 'data',
    version: '0.9.0',
    status: 'published',
    icon: null
  }
])

// 方法
const handleTabChange = (tab) => {
  console.log('切换到标签:', tab)
}

const getCategoryType = (category) => {
  const typeMap = {
    api: 'primary',
    data: 'success',
    file: 'warning',
    notification: 'info',
    other: ''
  }
  return typeMap[category] || ''
}

const getStatusType = (status) => {
  const typeMap = {
    draft: 'info',
    published: 'success',
    rejected: 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    draft: '草稿',
    published: '已发布',
    rejected: '被拒绝'
  }
  return textMap[status] || '未知'
}

const togglePlugin = (plugin) => {
  ElMessage.success(`${plugin.enabled ? '启用' : '禁用'}插件: ${plugin.name}`)
}

const configurePlugin = (plugin) => {
  emit('configure', plugin)
  ElMessage.success(`配置插件: ${plugin.name}`)
}

const viewPlugin = (plugin) => {
  ElMessage.success(`查看插件详情: ${plugin.name}`)
}

const uninstallPlugin = async (plugin) => {
  try {
    await ElMessageBox.confirm(
      `确定要卸载插件 "${plugin.name}" 吗？`,
      '确认卸载',
      {
        confirmButtonText: '卸载',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const index = installedPlugins.findIndex(p => p.id === plugin.id)
    if (index > -1) {
      installedPlugins.splice(index, 1)
      ElMessage.success('插件卸载成功')
    }
  } catch {
    // 用户取消
  }
}

const installPlugin = async (plugin) => {
  plugin.installing = true
  
  try {
    // 模拟安装过程
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    const newInstalledPlugin = {
      id: plugin.id,
      name: plugin.name,
      description: plugin.description,
      category: plugin.category,
      version: plugin.version,
      enabled: true,
      icon: plugin.icon
    }
    
    installedPlugins.push(newInstalledPlugin)
    emit('install', newInstalledPlugin)
    
    ElMessage.success(`插件 "${plugin.name}" 安装成功`)
  } catch (error) {
    ElMessage.error('插件安装失败')
  } finally {
    plugin.installing = false
  }
}

const editPlugin = (plugin) => {
  ElMessage.success(`编辑插件: ${plugin.name}`)
}

const publishPlugin = (plugin) => {
  plugin.status = 'published'
  ElMessage.success(`插件 "${plugin.name}" 发布成功`)
}

const deletePlugin = async (plugin) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除插件 "${plugin.name}" 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const index = myPlugins.findIndex(p => p.id === plugin.id)
    if (index > -1) {
      myPlugins.splice(index, 1)
      ElMessage.success('插件删除成功')
    }
  } catch {
    // 用户取消
  }
}

const importPlugin = () => {
  ElMessage.success('导入插件功能')
}

const handleIconSuccess = (response) => {
  newPlugin.icon = response.url
  ElMessage.success('图标上传成功')
}

const createPlugin = () => {
  if (!newPlugin.name || !newPlugin.description || !newPlugin.category) {
    ElMessage.error('请填写完整信息')
    return
  }
  
  const plugin = {
    id: Date.now(),
    ...newPlugin,
    version: '1.0.0',
    status: 'draft'
  }
  
  myPlugins.push(plugin)
  
  // 重置表单
  Object.assign(newPlugin, {
    name: '',
    description: '',
    category: '',
    icon: ''
  })
  
  showCreateDialog.value = false
  ElMessage.success('插件创建成功')
}
</script>

<style lang="scss" scoped>
.coze-plugin-manager {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  .plugin-toolbar {
    height: 60px;
    padding: 0 20px;
    border-bottom: 1px solid #e4e7ed;
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: white;
    
    .toolbar-left {
      display: flex;
      align-items: center;
      gap: 12px;
      
      h3 {
        margin: 0;
        font-size: 16px;
        color: #1f2937;
      }
    }
    
    .toolbar-right {
      display: flex;
      gap: 8px;
    }
  }
  
  .plugin-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    
    .plugin-tabs {
      padding: 0 20px;
      background: white;
      border-bottom: 1px solid #e4e7ed;
    }
    
    .plugin-content {
      flex: 1;
      padding: 20px;
      overflow-y: auto;
      background: #f9fafb;
      
      .plugin-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
        gap: 20px;
        
        .plugin-card {
          background: white;
          border-radius: 8px;
          padding: 20px;
          border: 1px solid #e5e7eb;
          transition: all 0.2s ease;
          
          &:hover {
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            transform: translateY(-2px);
          }
          
          .plugin-header {
            display: flex;
            align-items: flex-start;
            gap: 12px;
            margin-bottom: 16px;
            
            .plugin-icon {
              width: 48px;
              height: 48px;
              border-radius: 8px;
              background: #f3f4f6;
              display: flex;
              align-items: center;
              justify-content: center;
              overflow: hidden;
              
              img {
                width: 100%;
                height: 100%;
                object-fit: cover;
              }
              
              .el-icon {
                font-size: 24px;
                color: #6b7280;
              }
            }
            
            .plugin-info {
              flex: 1;
              
              h4 {
                margin: 0 0 8px 0;
                font-size: 16px;
                font-weight: 600;
                color: #1f2937;
              }
              
              p {
                margin: 0;
                font-size: 14px;
                color: #6b7280;
                line-height: 1.4;
              }
            }
            
            .plugin-status,
            .plugin-rating {
              display: flex;
              flex-direction: column;
              align-items: flex-end;
              gap: 4px;
              
              .rating-text {
                font-size: 12px;
                color: #6b7280;
              }
            }
          }
          
          .plugin-meta {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 16px;
            
            .plugin-version,
            .plugin-price {
              font-size: 12px;
              color: #6b7280;
            }
            
            .plugin-price {
              font-weight: 600;
              color: #059669;
            }
          }
          
          .plugin-actions {
            display: flex;
            gap: 8px;
            justify-content: flex-end;
          }
        }
      }
    }
  }
}

.icon-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: all 0.3s;
    
    &:hover {
      border-color: #409eff;
    }
  }
  
  .icon-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 60px;
    height: 60px;
    text-align: center;
    line-height: 60px;
  }
  
  .icon {
    width: 60px;
    height: 60px;
    display: block;
  }
}
</style>
