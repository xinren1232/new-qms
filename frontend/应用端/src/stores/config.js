import { defineStore } from 'pinia'
import { ref, reactive, computed } from 'vue'
import { getConfigFromBackend, subscribeConfigUpdates } from '@/api/config'

export const useConfigStore = defineStore('config', () => {
  // 配置数据 - 扩展为更完整的配置结构
  const configs = reactive({
    // AI场景配置
    aiConversation: null,
    dataSource: null,
    aiRule: null,
    userManagement: null,

    // 新增AI业务配置
    qualityInspection: null,    // 质量检测配置
    predictiveAnalysis: null,   // 预测分析配置
    businessRules: null,        // 业务规则配置
    notification: null,         // 通知配置

    // 系统配置
    systemSettings: null,       // 系统设置
    uiLayout: null,            // 界面布局配置
    permissions: null,         // 权限配置

    // 元数据
    metadata: {
      version: '2.0',
      lastSync: null,
      configSource: 'config-center',
      environment: 'development'
    }
  })

  // 加载状态
  const loading = ref(false)
  const syncing = ref(false)

  // 最后更新时间
  const lastUpdateTime = ref(null)

  // 配置有效性状态
  const configValid = ref(true)
  const validationErrors = ref([])
  
  // 计算属性 - 检查权限（移除重复声明）

  // 初始化配置
  const initializeConfig = async () => {
    loading.value = true
    try {
      console.log('🔄 开始初始化配置驱动端配置...')

      // 从配置端获取初始配置
      const configData = await getConfigFromBackend()
      updateConfigs(configData)

      // 验证配置
      validateConfigs()

      // 订阅配置更新
      subscribeConfigUpdates(handleConfigUpdate)

      console.log('✅ 配置驱动端初始化成功', configs)
    } catch (error) {
      console.error('❌ 配置初始化失败:', error)
      configValid.value = false
      validationErrors.value.push('配置初始化失败: ' + error.message)
    } finally {
      loading.value = false
    }
  }

  // 更新配置
  const updateConfigs = (newConfigs) => {
    // 深度合并配置
    Object.keys(newConfigs).forEach(key => {
      if (key === 'metadata') {
        Object.assign(configs.metadata, newConfigs[key])
      } else {
        configs[key] = newConfigs[key]
      }
    })

    lastUpdateTime.value = new Date().toISOString()
    configs.metadata.lastSync = lastUpdateTime.value

    console.log('🔄 配置已更新:', newConfigs)

    // 触发配置变更事件
    window.dispatchEvent(new CustomEvent('configUpdated', {
      detail: { configs: newConfigs, timestamp: lastUpdateTime.value }
    }))
  }

  // 验证配置
  const validateConfigs = () => {
    validationErrors.value = []
    configValid.value = true

    // 验证必要的配置项
    const requiredConfigs = ['aiConversation', 'dataSource', 'aiRule']
    requiredConfigs.forEach(configKey => {
      if (!configs[configKey]) {
        validationErrors.value.push(`缺少必要配置: ${configKey}`)
        configValid.value = false
      }
    })

    console.log('🔍 配置验证结果:', configValid.value ? '通过' : '失败', validationErrors.value)
  }
  
  // 处理配置更新事件
  const handleConfigUpdate = (updateData) => {
    console.log('📡 收到配置更新:', updateData)
    updateConfigs(updateData)
  }
  
  // 获取指定模块的配置
  const getModuleConfig = (moduleKey) => {
    return configs[moduleKey] || null
  }
  
  // 获取字段配置
  const getFieldConfig = (moduleKey) => {
    const moduleConfig = getModuleConfig(moduleKey)
    return moduleConfig?.fields || []
  }
  
  // 获取权限配置
  const getPermissionConfig = (moduleKey) => {
    const moduleConfig = getModuleConfig(moduleKey)
    return moduleConfig?.permissions || {}
  }
  
  // 获取功能配置
  const getFunctionConfig = (moduleKey) => {
    const moduleConfig = getModuleConfig(moduleKey)
    return moduleConfig?.functions || {}
  }
  
  // 获取搜索配置
  const getSearchConfig = (moduleKey) => {
    const moduleConfig = getModuleConfig(moduleKey)
    return moduleConfig?.search || []
  }
  
  // 检查权限
  const hasPermission = (moduleKey, permission) => {
    const permissions = getPermissionConfig(moduleKey)
    const userRoles = ['admin'] // 这里应该从用户状态获取
    
    if (!permission || !permissions[permission]) {
      return true
    }
    
    return permissions[permission].some(role => userRoles.includes(role))
  }
  
  return {
    configs,
    loading,
    syncing,
    lastUpdateTime,
    configValid,
    validationErrors,
    initializeConfig,
    updateConfigs,
    validateConfigs,
    handleConfigUpdate,
    getModuleConfig,
    getFieldConfig,
    getPermissionConfig,
    getFunctionConfig,
    getSearchConfig,
    hasPermission
  }
})
