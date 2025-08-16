/**
 * 全局快捷键管理器
 * 提供统一的快捷键注册、管理和执行
 */

import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

class GlobalShortcuts {
  constructor() {
    this.shortcuts = new Map()
    this.isEnabled = true
    this.router = null
    this.currentContext = 'global'
    this.contextShortcuts = new Map()
    
    this.init()
  }

  /**
   * 初始化快捷键系统
   */
  init() {
    // 监听键盘事件
    document.addEventListener('keydown', this.handleKeyDown.bind(this))
    
    // 注册默认快捷键
    this.registerDefaultShortcuts()
    
    console.log('⌨️ 全局快捷键系统初始化完成')
  }

  /**
   * 设置路由实例
   */
  setRouter(router) {
    this.router = router
  }

  /**
   * 注册默认快捷键
   */
  registerDefaultShortcuts() {
    // ==================== 全局快捷键 ====================
    
    // 全局搜索
    this.register('ctrl+k', {
      name: '全局搜索',
      description: '打开全局搜索对话框',
      action: () => this.openGlobalSearch(),
      context: 'global'
    })
    
    // 创建快捷键
    this.register('ctrl+n', {
      name: '新建',
      description: '打开创建对话框',
      action: () => this.openCreateDialog(),
      context: 'global'
    })
    
    this.register('ctrl+shift+a', {
      name: '创建Agent',
      description: '快速创建AI Agent',
      action: () => this.createAgent(),
      context: 'global'
    })
    
    this.register('ctrl+shift+w', {
      name: '创建工作流',
      description: '快速创建工作流',
      action: () => this.createWorkflow(),
      context: 'global'
    })
    
    this.register('ctrl+shift+k', {
      name: '创建知识库',
      description: '快速创建知识库',
      action: () => this.createKnowledge(),
      context: 'global'
    })
    
    // 导航快捷键
    this.register('ctrl+1', {
      name: '控制台',
      description: '跳转到控制台',
      action: () => this.navigateTo('/coze-plugins/dashboard'),
      context: 'global'
    })
    
    this.register('ctrl+2', {
      name: 'Agent管理',
      description: '跳转到Agent管理',
      action: () => this.navigateTo('/coze-plugins/agents'),
      context: 'global'
    })
    
    this.register('ctrl+3', {
      name: '工作流设计',
      description: '跳转到工作流设计',
      action: () => this.navigateTo('/coze-plugins/workflows'),
      context: 'global'
    })
    
    this.register('ctrl+4', {
      name: '知识库管理',
      description: '跳转到知识库管理',
      action: () => this.navigateTo('/coze-plugins/knowledge'),
      context: 'global'
    })
    
    this.register('ctrl+5', {
      name: '插件管理',
      description: '跳转到插件管理',
      action: () => this.navigateTo('/coze-plugins/plugins'),
      context: 'global'
    })
    
    this.register('ctrl+6', {
      name: '模型管理',
      description: '跳转到模型管理',
      action: () => this.navigateTo('/coze-plugins/models'),
      context: 'global'
    })
    
    // 系统快捷键
    this.register('ctrl+,', {
      name: '设置',
      description: '打开系统设置',
      action: () => this.openSettings(),
      context: 'global'
    })
    
    this.register('ctrl+/', {
      name: '快捷键帮助',
      description: '显示快捷键帮助',
      action: () => this.showShortcutHelp(),
      context: 'global'
    })
    
    // ==================== 工作流编辑器快捷键 ====================
    
    this.register('ctrl+s', {
      name: '保存',
      description: '保存当前工作流',
      action: () => this.saveCurrentWorkflow(),
      context: 'workflow-editor'
    })
    
    this.register('ctrl+z', {
      name: '撤销',
      description: '撤销上一步操作',
      action: () => this.undoLastAction(),
      context: 'workflow-editor'
    })
    
    this.register('ctrl+y', {
      name: '重做',
      description: '重做上一步操作',
      action: () => this.redoLastAction(),
      context: 'workflow-editor'
    })
    
    this.register('ctrl+shift+r', {
      name: '运行工作流',
      description: '执行当前工作流',
      action: () => this.runWorkflow(),
      context: 'workflow-editor'
    })
    
    this.register('delete', {
      name: '删除节点',
      description: '删除选中的节点',
      action: () => this.deleteSelectedNodes(),
      context: 'workflow-editor'
    })
    
    this.register('ctrl+d', {
      name: '复制节点',
      description: '复制选中的节点',
      action: () => this.duplicateSelectedNodes(),
      context: 'workflow-editor'
    })
    
    this.register('ctrl+a', {
      name: '全选',
      description: '选择所有节点',
      action: () => this.selectAllNodes(),
      context: 'workflow-editor'
    })
    
    // ==================== Agent编辑器快捷键 ====================
    
    this.register('ctrl+enter', {
      name: '测试Agent',
      description: '测试当前Agent',
      action: () => this.testAgent(),
      context: 'agent-editor'
    })
    
    this.register('ctrl+shift+p', {
      name: '发布Agent',
      description: '发布当前Agent',
      action: () => this.publishAgent(),
      context: 'agent-editor'
    })
    
    // ==================== 通用编辑快捷键 ====================
    
    this.register('escape', {
      name: '取消',
      description: '取消当前操作或关闭对话框',
      action: () => this.cancelCurrentAction(),
      context: 'global'
    })
    
    this.register('f11', {
      name: '全屏',
      description: '切换全屏模式',
      action: () => this.toggleFullscreen(),
      context: 'global'
    })
  }

  /**
   * 注册快捷键
   */
  register(combination, config) {
    const key = this.normalizeKey(combination)
    
    this.shortcuts.set(key, {
      combination,
      ...config,
      registered: Date.now()
    })
    
    // 按上下文分组
    const context = config.context || 'global'
    if (!this.contextShortcuts.has(context)) {
      this.contextShortcuts.set(context, new Map())
    }
    this.contextShortcuts.get(context).set(key, this.shortcuts.get(key))
  }

  /**
   * 注销快捷键
   */
  unregister(combination) {
    const key = this.normalizeKey(combination)
    const shortcut = this.shortcuts.get(key)
    
    if (shortcut) {
      this.shortcuts.delete(key)
      
      // 从上下文中移除
      const context = shortcut.context || 'global'
      if (this.contextShortcuts.has(context)) {
        this.contextShortcuts.get(context).delete(key)
      }
    }
  }

  /**
   * 设置当前上下文
   */
  setContext(context) {
    this.currentContext = context
  }

  /**
   * 处理键盘事件
   */
  handleKeyDown(event) {
    if (!this.isEnabled) return
    
    // 忽略在输入框中的按键
    if (this.isInputElement(event.target)) {
      // 只处理特定的快捷键
      const key = this.getKeyFromEvent(event)
      const shortcut = this.shortcuts.get(key)
      
      if (shortcut && this.isGlobalShortcut(shortcut)) {
        event.preventDefault()
        this.executeShortcut(shortcut)
      }
      return
    }
    
    const key = this.getKeyFromEvent(event)
    const shortcut = this.getShortcutForContext(key)
    
    if (shortcut) {
      event.preventDefault()
      this.executeShortcut(shortcut)
    }
  }

  /**
   * 获取当前上下文的快捷键
   */
  getShortcutForContext(key) {
    // 优先查找当前上下文的快捷键
    const contextShortcuts = this.contextShortcuts.get(this.currentContext)
    if (contextShortcuts && contextShortcuts.has(key)) {
      return contextShortcuts.get(key)
    }
    
    // 查找全局快捷键
    const globalShortcuts = this.contextShortcuts.get('global')
    if (globalShortcuts && globalShortcuts.has(key)) {
      return globalShortcuts.get(key)
    }
    
    return null
  }

  /**
   * 执行快捷键
   */
  executeShortcut(shortcut) {
    try {
      shortcut.action()
      console.log(`⌨️ 执行快捷键: ${shortcut.name} (${shortcut.combination})`)
    } catch (error) {
      console.error('快捷键执行失败:', error)
      ElMessage.error('快捷键执行失败')
    }
  }

  /**
   * 从事件获取按键组合
   */
  getKeyFromEvent(event) {
    const parts = []
    
    if (event.ctrlKey) parts.push('ctrl')
    if (event.altKey) parts.push('alt')
    if (event.shiftKey) parts.push('shift')
    if (event.metaKey) parts.push('meta')
    
    const key = event.key.toLowerCase()
    if (key !== 'control' && key !== 'alt' && key !== 'shift' && key !== 'meta') {
      parts.push(key)
    }
    
    return parts.join('+')
  }

  /**
   * 标准化按键组合
   */
  normalizeKey(combination) {
    return combination.toLowerCase().split('+').sort().join('+')
  }

  /**
   * 检查是否为输入元素
   */
  isInputElement(element) {
    const inputTypes = ['input', 'textarea', 'select']
    return inputTypes.includes(element.tagName.toLowerCase()) ||
           element.contentEditable === 'true'
  }

  /**
   * 检查是否为全局快捷键
   */
  isGlobalShortcut(shortcut) {
    const globalKeys = ['ctrl+k', 'ctrl+/', 'escape', 'f11']
    return globalKeys.includes(shortcut.combination)
  }

  /**
   * 启用/禁用快捷键
   */
  setEnabled(enabled) {
    this.isEnabled = enabled
  }

  /**
   * 获取所有快捷键
   */
  getAllShortcuts() {
    return Array.from(this.shortcuts.values())
  }

  /**
   * 获取指定上下文的快捷键
   */
  getShortcutsByContext(context) {
    const contextShortcuts = this.contextShortcuts.get(context)
    return contextShortcuts ? Array.from(contextShortcuts.values()) : []
  }

  // ==================== 快捷键动作实现 ====================

  openGlobalSearch() {
    // 触发全局搜索事件
    window.dispatchEvent(new CustomEvent('open-global-search'))
  }

  openCreateDialog() {
    window.dispatchEvent(new CustomEvent('open-create-dialog'))
  }

  createAgent() {
    if (this.router) {
      this.router.push('/coze-plugins/agents?action=create')
    }
  }

  createWorkflow() {
    if (this.router) {
      this.router.push('/coze-plugins/workflows?action=create')
    }
  }

  createKnowledge() {
    if (this.router) {
      this.router.push('/coze-plugins/knowledge?action=create')
    }
  }

  navigateTo(path) {
    if (this.router) {
      this.router.push(path)
    }
  }

  openSettings() {
    window.dispatchEvent(new CustomEvent('open-settings'))
  }

  showShortcutHelp() {
    window.dispatchEvent(new CustomEvent('show-shortcut-help'))
  }

  saveCurrentWorkflow() {
    window.dispatchEvent(new CustomEvent('save-workflow'))
  }

  undoLastAction() {
    window.dispatchEvent(new CustomEvent('undo-action'))
  }

  redoLastAction() {
    window.dispatchEvent(new CustomEvent('redo-action'))
  }

  runWorkflow() {
    window.dispatchEvent(new CustomEvent('run-workflow'))
  }

  deleteSelectedNodes() {
    window.dispatchEvent(new CustomEvent('delete-selected-nodes'))
  }

  duplicateSelectedNodes() {
    window.dispatchEvent(new CustomEvent('duplicate-selected-nodes'))
  }

  selectAllNodes() {
    window.dispatchEvent(new CustomEvent('select-all-nodes'))
  }

  testAgent() {
    window.dispatchEvent(new CustomEvent('test-agent'))
  }

  publishAgent() {
    window.dispatchEvent(new CustomEvent('publish-agent'))
  }

  cancelCurrentAction() {
    window.dispatchEvent(new CustomEvent('cancel-action'))
  }

  toggleFullscreen() {
    if (document.fullscreenElement) {
      document.exitFullscreen()
    } else {
      document.documentElement.requestFullscreen()
    }
  }
}

// 创建全局实例
const globalShortcuts = new GlobalShortcuts()

export default globalShortcuts
