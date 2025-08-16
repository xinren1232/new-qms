<template>
  <el-dialog
    v-model="visible"
    title="快捷键帮助"
    width="800px"
    :before-close="handleClose"
    class="shortcut-help-dialog"
  >
    <div class="shortcut-help-content">
      <!-- 搜索框 -->
      <div class="search-section">
        <el-input
          v-model="searchQuery"
          placeholder="搜索快捷键..."
          prefix-icon="Search"
          clearable
          class="search-input"
        />
      </div>

      <!-- 上下文切换 -->
      <div class="context-tabs">
        <el-tabs v-model="activeContext" @tab-click="handleContextChange">
          <el-tab-pane
            v-for="context in contexts"
            :key="context.key"
            :label="context.label"
            :name="context.key"
          >
            <div class="shortcuts-list">
              <div
                v-for="shortcut in filteredShortcuts[context.key]"
                :key="shortcut.combination"
                class="shortcut-item"
              >
                <div class="shortcut-keys">
                  <span
                    v-for="key in parseKeys(shortcut.combination)"
                    :key="key"
                    class="key-badge"
                    :class="getKeyClass(key)"
                  >
                    {{ formatKey(key) }}
                  </span>
                </div>
                <div class="shortcut-info">
                  <div class="shortcut-name">{{ shortcut.name }}</div>
                  <div class="shortcut-description">{{ shortcut.description }}</div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 提示信息 -->
      <div class="help-tips">
        <el-alert
          title="提示"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <ul class="tips-list">
              <li>按 <kbd>Ctrl</kbd> + <kbd>/</kbd> 可随时打开此帮助</li>
              <li>在输入框中，只有部分全局快捷键生效</li>
              <li>不同页面可能有特定的快捷键</li>
              <li>按 <kbd>Esc</kbd> 可取消大多数操作</li>
            </ul>
          </template>
        </el-alert>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button type="primary" @click="printShortcuts">打印快捷键</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import globalShortcuts from '@/utils/global-shortcuts'

// 响应式数据
const visible = ref(false)
const searchQuery = ref('')
const activeContext = ref('global')

// 上下文定义
const contexts = ref([
  { key: 'global', label: '全局快捷键' },
  { key: 'workflow-editor', label: '工作流编辑器' },
  { key: 'agent-editor', label: 'Agent编辑器' }
])

// 所有快捷键
const allShortcuts = ref({})

// 过滤后的快捷键
const filteredShortcuts = computed(() => {
  const result = {}
  
  contexts.value.forEach(context => {
    const shortcuts = allShortcuts.value[context.key] || []
    
    if (!searchQuery.value) {
      result[context.key] = shortcuts
    } else {
      const query = searchQuery.value.toLowerCase()
      result[context.key] = shortcuts.filter(shortcut =>
        shortcut.name.toLowerCase().includes(query) ||
        shortcut.description.toLowerCase().includes(query) ||
        shortcut.combination.toLowerCase().includes(query)
      )
    }
  })
  
  return result
})

// 方法
const show = () => {
  visible.value = true
  loadShortcuts()
}

const hide = () => {
  visible.value = false
}

const handleClose = () => {
  hide()
}

const handleContextChange = (tab) => {
  activeContext.value = tab.name
}

const loadShortcuts = () => {
  contexts.value.forEach(context => {
    allShortcuts.value[context.key] = globalShortcuts.getShortcutsByContext(context.key)
  })
}

const parseKeys = (combination) => {
  return combination.split('+')
}

const formatKey = (key) => {
  const keyMap = {
    'ctrl': 'Ctrl',
    'alt': 'Alt',
    'shift': 'Shift',
    'meta': 'Cmd',
    'enter': 'Enter',
    'escape': 'Esc',
    'delete': 'Del',
    'backspace': 'Backspace',
    'tab': 'Tab',
    'space': 'Space',
    'arrowup': '↑',
    'arrowdown': '↓',
    'arrowleft': '←',
    'arrowright': '→'
  }
  
  return keyMap[key.toLowerCase()] || key.toUpperCase()
}

const getKeyClass = (key) => {
  const modifierKeys = ['ctrl', 'alt', 'shift', 'meta']
  return {
    'modifier-key': modifierKeys.includes(key.toLowerCase()),
    'function-key': key.toLowerCase().startsWith('f') && key.length <= 3,
    'special-key': ['enter', 'escape', 'delete', 'backspace', 'tab', 'space'].includes(key.toLowerCase())
  }
}

const printShortcuts = () => {
  const printContent = generatePrintContent()
  const printWindow = window.open('', '_blank')
  printWindow.document.write(printContent)
  printWindow.document.close()
  printWindow.print()
}

const generatePrintContent = () => {
  let content = `
    <html>
      <head>
        <title>Coze Studio 快捷键参考</title>
        <style>
          body { font-family: Arial, sans-serif; margin: 20px; }
          h1 { color: #409eff; border-bottom: 2px solid #409eff; padding-bottom: 10px; }
          h2 { color: #606266; margin-top: 30px; }
          .shortcut-item { margin: 10px 0; display: flex; align-items: center; }
          .shortcut-keys { margin-right: 20px; min-width: 150px; }
          .key-badge { 
            background: #f5f7fa; 
            border: 1px solid #dcdfe6; 
            padding: 2px 6px; 
            margin: 0 2px; 
            border-radius: 3px; 
            font-family: monospace;
          }
          .shortcut-name { font-weight: bold; }
          .shortcut-description { color: #909399; font-size: 14px; }
        </style>
      </head>
      <body>
        <h1>Coze Studio 快捷键参考</h1>
  `
  
  contexts.value.forEach(context => {
    const shortcuts = allShortcuts.value[context.key] || []
    if (shortcuts.length > 0) {
      content += `<h2>${context.label}</h2>`
      shortcuts.forEach(shortcut => {
        const keys = parseKeys(shortcut.combination)
          .map(key => `<span class="key-badge">${formatKey(key)}</span>`)
          .join('')
        
        content += `
          <div class="shortcut-item">
            <div class="shortcut-keys">${keys}</div>
            <div>
              <div class="shortcut-name">${shortcut.name}</div>
              <div class="shortcut-description">${shortcut.description}</div>
            </div>
          </div>
        `
      })
    }
  })
  
  content += `
      </body>
    </html>
  `
  
  return content
}

// 事件监听
const handleShowShortcutHelp = () => {
  show()
}

onMounted(() => {
  window.addEventListener('show-shortcut-help', handleShowShortcutHelp)
  loadShortcuts()
})

onUnmounted(() => {
  window.removeEventListener('show-shortcut-help', handleShowShortcutHelp)
})

// 暴露方法
defineExpose({
  show,
  hide
})
</script>

<style lang="scss" scoped>
.shortcut-help-dialog {
  .shortcut-help-content {
    .search-section {
      margin-bottom: 20px;
      
      .search-input {
        width: 100%;
      }
    }
    
    .context-tabs {
      .shortcuts-list {
        max-height: 400px;
        overflow-y: auto;
        
        .shortcut-item {
          display: flex;
          align-items: flex-start;
          padding: 12px 0;
          border-bottom: 1px solid var(--el-border-color-lighter);
          
          &:last-child {
            border-bottom: none;
          }
          
          .shortcut-keys {
            min-width: 180px;
            margin-right: 20px;
            display: flex;
            flex-wrap: wrap;
            gap: 4px;
            
            .key-badge {
              display: inline-block;
              padding: 4px 8px;
              background: var(--el-fill-color-light);
              border: 1px solid var(--el-border-color);
              border-radius: 4px;
              font-family: 'Courier New', monospace;
              font-size: 12px;
              font-weight: 500;
              color: var(--el-text-color-primary);
              
              &.modifier-key {
                background: var(--el-color-primary-light-9);
                border-color: var(--el-color-primary-light-7);
                color: var(--el-color-primary);
              }
              
              &.function-key {
                background: var(--el-color-warning-light-9);
                border-color: var(--el-color-warning-light-7);
                color: var(--el-color-warning);
              }
              
              &.special-key {
                background: var(--el-color-success-light-9);
                border-color: var(--el-color-success-light-7);
                color: var(--el-color-success);
              }
            }
          }
          
          .shortcut-info {
            flex: 1;
            
            .shortcut-name {
              font-weight: 600;
              color: var(--el-text-color-primary);
              margin-bottom: 4px;
            }
            
            .shortcut-description {
              font-size: 14px;
              color: var(--el-text-color-regular);
              line-height: 1.4;
            }
          }
        }
      }
    }
    
    .help-tips {
      margin-top: 20px;
      
      .tips-list {
        margin: 0;
        padding-left: 20px;
        
        li {
          margin: 8px 0;
          
          kbd {
            display: inline-block;
            padding: 2px 6px;
            background: var(--el-fill-color-light);
            border: 1px solid var(--el-border-color);
            border-radius: 3px;
            font-family: 'Courier New', monospace;
            font-size: 12px;
            margin: 0 2px;
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .shortcut-help-dialog {
    :deep(.el-dialog) {
      width: 95% !important;
      margin: 5vh auto !important;
    }
    
    .shortcut-help-content {
      .shortcuts-list {
        .shortcut-item {
          flex-direction: column;
          align-items: flex-start;
          
          .shortcut-keys {
            margin-right: 0;
            margin-bottom: 8px;
            min-width: auto;
          }
        }
      }
    }
  }
}
</style>
