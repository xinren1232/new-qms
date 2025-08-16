/**
 * 本地配置中心 - 替换内网配置依赖
 * 提供配置管理、同步、订阅等功能
 */

class LocalConfigCenter {
  constructor() {
    this.configStore = new Map()
    this.subscribers = new Map()
    this.syncTargets = new Map()
    this.syncHistory = []
    this.initDefaultConfigs()
    this.initSyncTargets()
  }

  // 初始化默认配置 - 替换内网配置
  initDefaultConfigs() {
    // 系统配置 - 替换 VUE_APP_BASE_API
    this.setConfig('system.api', {
      base_api: process.env.NODE_ENV === 'production' 
        ? 'https://your-domain.com/api' 
        : 'http://localhost:8080/api',
      config_api: process.env.NODE_ENV === 'production'
        ? 'https://your-domain.com/config-api'
        : 'http://localhost:3003',
      timeout: 10000,
      retry_times: 3
    })

    // 认证配置 - 替换 VUE_APP_SSO
    this.setConfig('auth.config', {
      type: 'local_jwt', // 替换SSO
      login_url: '/api/auth/login',
      logout_url: '/api/auth/logout',
      userinfo_url: '/api/auth/userinfo',
      token_key: 'qms_ai_token',
      refresh_token_key: 'qms_ai_refresh_token',
      token_expire: 24 * 60 * 60 * 1000, // 24小时
      auto_refresh: true
    })

    // 权限配置 - 替换 VUE_APP_PERMISSION_APP_CODE
    this.setConfig('permission.config', {
      app_code: 'QMS_AI',
      app_key: 'qms_ai_local_key_' + Date.now(),
      permission_api: '/api/auth/permissions',
      check_permission_api: '/api/auth/check-permission',
      role_hierarchy: {
        'SUPER_ADMIN': ['*'],
        'ADMIN': ['system:*', 'ai:*', 'user:*'],
        'MANAGER': ['ai:use', 'ai:view', 'user:view'],
        'USER': ['ai:use:basic', 'ai:view:own']
      }
    })

    // AI配置
    this.setConfig('ai.config', {
      models: {
        'gpt-4o': { 
          enabled: true, 
          quota: 1000,
          cost_per_1k_tokens: 0.03,
          capabilities: ['text', 'vision', 'tools']
        },
        'gpt-4o-mini': { 
          enabled: true, 
          quota: 2000,
          cost_per_1k_tokens: 0.015,
          capabilities: ['text']
        },
        'deepseek-r1': { 
          enabled: true, 
          quota: 2000,
          cost_per_1k_tokens: 0.002,
          capabilities: ['text', 'reasoning']
        },
        'claude-3-5-sonnet': { 
          enabled: false, 
          quota: 500,
          cost_per_1k_tokens: 0.03,
          capabilities: ['text', 'tools']
        }
      },
      default_model: 'gpt-4o-mini',
      rate_limits: {
        requests_per_minute: 60,
        tokens_per_day: 10000,
        cost_per_day: 10.0
      },
      features: {
        conversation_export: true,
        conversation_share: true,
        model_switching: true,
        usage_analytics: true
      }
    })

    // 应用端配置
    this.setConfig('app.config', {
      theme: {
        primary_color: '#409EFF',
        dark_mode: false,
        compact_mode: false
      },
      layout: {
        sidebar_collapsed: false,
        show_breadcrumb: true,
        show_tags_view: true
      },
      features: {
        chat_enabled: true,
        analytics_enabled: true,
        export_enabled: true,
        collaboration_enabled: true
      },
      chat: {
        max_conversation_length: 50,
        auto_save_interval: 30000,
        show_model_info: true,
        enable_streaming: true
      }
    })

    // 监控配置
    this.setConfig('monitoring.config', {
      enabled: true,
      metrics_interval: 60000,
      alert_thresholds: {
        error_rate: 0.05,
        response_time: 5000,
        memory_usage: 0.8,
        cpu_usage: 0.8
      },
      retention_days: 30
    })
  }

  // 初始化同步目标
  initSyncTargets() {
    this.syncTargets.set('app-frontend', {
      name: '应用端',
      url: process.env.NODE_ENV === 'production' 
        ? 'https://your-domain.com' 
        : 'http://localhost:8080',
      sync_endpoint: '/api/config/sync',
      health_endpoint: '/health',
      enabled: true
    })

    this.syncTargets.set('config-driven-frontend', {
      name: '配置驱动端',
      url: process.env.NODE_ENV === 'production'
        ? 'https://your-domain.com/config-driven'
        : 'http://localhost:8073',
      sync_endpoint: '/api/config/sync', 
      health_endpoint: '/health',
      enabled: false // 暂时禁用
    })
  }

  // 配置获取 - 替换 Feign 调用
  async getConfig(key) {
    // 先从内存获取
    let config = this.configStore.get(key)
    
    // 如果内存中没有，尝试从本地存储获取
    if (!config) {
      const stored = localStorage.getItem(`config_${key}`)

      if (stored) {
        try {
          config = JSON.parse(stored)
          this.configStore.set(key, config)
        } catch (error) {
          console.warn('解析本地配置失败:', key, error)
        }
      }
    }
    
    return config
  }

  // 配置更新 - 替换内网配置推送
  async setConfig(key, value, syncToApps = false) {
    // 更新内存配置
    this.configStore.set(key, value)
    
    // 持久化到本地存储
    try {
      localStorage.setItem(`config_${key}`, JSON.stringify(value))
    } catch (error) {
      console.error('保存配置到本地存储失败:', key, error)
    }
    
    // 通知订阅者
    const subscribers = this.subscribers.get(key) || []

    subscribers.forEach(callback => {
      try {
        callback(value)
      } catch (error) {
        console.error('配置订阅回调执行失败:', error)
      }
    })
    
    // 如果需要，同步到应用端
    if (syncToApps) {
      await this.syncConfigToApps(key, value)
    }
    
    return true
  }

  // 配置订阅 - 替换内网配置监听
  subscribe(key, callback) {
    if (!this.subscribers.has(key)) {
      this.subscribers.set(key, [])
    }
    this.subscribers.get(key).push(callback)
    
    // 返回取消订阅函数
    return () => {
      const callbacks = this.subscribers.get(key) || []
      const index = callbacks.indexOf(callback)

      if (index > -1) {
        callbacks.splice(index, 1)
      }
    }
  }

  // 同步配置到应用端
  async syncConfigToApps(configKey, configValue, targetApps = []) {
    const results = []
    const targets = targetApps.length > 0 
      ? targetApps.map(id => this.syncTargets.get(id)).filter(Boolean)
      : Array.from(this.syncTargets.values()).filter(t => t.enabled)

    for (const target of targets) {
      try {
        // 检查目标应用健康状态
        const healthResponse = await fetch(`${target.url}${target.health_endpoint}`, {
          method: 'GET',
          timeout: 5000
        })
        
        if (!healthResponse.ok) {
          throw new Error(`目标应用不健康: ${healthResponse.status}`)
        }

        // 推送配置
        const syncResponse = await fetch(`${target.url}${target.sync_endpoint}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'X-Config-Source': 'config-center',
            'X-Config-Version': Date.now().toString(),
            'X-Config-Key': configKey
          },
          body: JSON.stringify({
            key: configKey,
            value: configValue,
            timestamp: new Date().toISOString(),
            source: 'config-center'
          }),
          timeout: 10000
        })

        if (syncResponse.ok) {
          results.push({
            target: target.name,
            status: 'success',
            message: '配置同步成功'
          })
          
          this.recordSyncHistory(target.name, configKey, 'success')
        } else {
          throw new Error(`同步失败: ${syncResponse.status} ${syncResponse.statusText}`)
        }

      } catch (error) {
        results.push({
          target: target.name,
          status: 'failed',
          message: error.message
        })
        
        this.recordSyncHistory(target.name, configKey, 'failed', error.message)
      }
    }

    return results
  }

  // 记录同步历史
  recordSyncHistory(targetName, configKey, status, error = null) {
    this.syncHistory.push({
      id: Date.now() + Math.random(),
      target_name: targetName,
      config_key: configKey,
      status,
      error,
      timestamp: new Date().toISOString()
    })

    // 保持最近100条记录
    if (this.syncHistory.length > 100) {
      this.syncHistory = this.syncHistory.slice(-100)
    }
  }

  // 获取同步状态
  getSyncStatus() {
    const statusMap = new Map()
    
    // 获取每个目标的最新同步状态
    this.syncHistory.forEach(record => {
      const key = `${record.target_name}_${record.config_key}`

      if (!statusMap.has(key) || 
          new Date(record.timestamp) > new Date(statusMap.get(key).timestamp)) {
        statusMap.set(key, record)
      }
    })
    
    return Array.from(statusMap.values())
  }

  // 获取所有配置
  getAllConfigs() {
    const configs = {}

    for (const [key, value] of this.configStore) {
      configs[key] = value
    }

    return configs
  }

  // 批量更新配置
  async batchUpdateConfigs(configs, syncToApps = false) {
    const results = []
    
    for (const [key, value] of Object.entries(configs)) {
      try {
        await this.setConfig(key, value, false) // 先不同步
        results.push({ key, status: 'success' })
      } catch (error) {
        results.push({ key, status: 'failed', error: error.message })
      }
    }
    
    // 如果需要同步，批量同步所有配置
    if (syncToApps) {
      await this.syncAllConfigsToApps()
    }
    
    return results
  }

  // 同步所有配置到应用端
  async syncAllConfigsToApps() {
    const allConfigs = this.getAllConfigs()

    return await this.syncConfigToApps('*', allConfigs)
  }
}

// 创建全局配置中心实例
const localConfigCenter = new LocalConfigCenter()

export default localConfigCenter
export { LocalConfigCenter }
