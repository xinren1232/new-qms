/**
 * 本地API适配器 - 替换内网API依赖
 * 提供与原有API相同的接口，但使用本地配置中心实现
 */

import localConfigCenter from '@/config/local-config-center'

class LocalAPIAdapter {
  constructor() {
    this.baseURL = process.env.NODE_ENV === 'production' 
      ? 'https://your-domain.com/api' 
      : 'http://localhost:8080/api'
  }

  // 模拟HTTP请求响应格式
  createResponse(data, success = true, message = 'success') {
    return {
      success,
      message,
      data,
      code: success ? 200 : 500,
      timestamp: new Date().toISOString()
    }
  }

  // 模拟异步请求
  async mockRequest(handler, delay = 100) {
    return new Promise((resolve, reject) => {
      setTimeout(async () => {
        try {
          const result = await handler()

          resolve(result)
        } catch (error) {
          reject(error)
        }
      }, delay)
    })
  }

  // ==================== 认证相关API ====================
  
  // 用户登录 - 替换 VUE_APP_SSO
  async login(credentials) {
    return this.mockRequest(async () => {
      const { username, password } = credentials
      
      // 模拟用户验证
      const users = [
        { 
          id: 'admin_001', 
          username: 'admin', 
          password: 'admin123',
          realName: '系统管理员',
          email: 'admin@qms.com',
          roles: ['ADMIN'],
          permissions: ['*']
        },
        { 
          id: 'config_001', 
          username: 'config', 
          password: 'config123',
          realName: '配置管理员',
          email: 'config@qms.com',
          roles: ['CONFIG_ADMIN'],
          permissions: ['config:*', 'ai:*']
        }
      ]
      
      const user = users.find(u => u.username === username && u.password === password)
      
      if (user) {
        // 生成模拟token
        const token = 'local_token_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
        
        // 保存到本地存储
        localStorage.setItem('qms_config_token', token)
        localStorage.setItem('qms_config_user', JSON.stringify(user))
        
        return this.createResponse({
          token,
          user: {
            id: user.id,
            username: user.username,
            realName: user.realName,
            email: user.email,
            roles: user.roles,
            permissions: user.permissions
          }
        })
      } else {
        return this.createResponse(null, false, '用户名或密码错误')
      }
    })
  }

  // 获取用户信息
  async getUserInfo() {
    return this.mockRequest(async () => {
      const token = localStorage.getItem('qms_config_token')
      const userStr = localStorage.getItem('qms_config_user')
      
      if (token && userStr) {
        const user = JSON.parse(userStr)

        return this.createResponse(user)
      } else {
        return this.createResponse(null, false, '未登录或登录已过期')
      }
    })
  }

  // 用户登出
  async logout() {
    return this.mockRequest(async () => {
      localStorage.removeItem('qms_config_token')
      localStorage.removeItem('qms_config_user')

      return this.createResponse(null, true, '登出成功')
    })
  }

  // ==================== 配置管理API ====================
  
  // 获取配置 - 替换 Feign 调用
  async getConfig(configKey) {
    return this.mockRequest(async () => {
      const config = await localConfigCenter.getConfig(configKey)

      return this.createResponse(config)
    })
  }

  // 更新配置
  async updateConfig(configKey, configValue, syncToApps = false) {
    return this.mockRequest(async () => {
      await localConfigCenter.setConfig(configKey, configValue, syncToApps)

      return this.createResponse(null, true, '配置更新成功')
    })
  }

  // 批量更新配置
  async batchUpdateConfigs(configs, syncToApps = false) {
    return this.mockRequest(async () => {
      const results = await localConfigCenter.batchUpdateConfigs(configs, syncToApps)

      return this.createResponse(results)
    })
  }

  // 获取所有配置
  async getAllConfigs() {
    return this.mockRequest(async () => {
      const configs = localConfigCenter.getAllConfigs()

      return this.createResponse(configs)
    })
  }

  // ==================== 应用管控API ====================
  
  // 获取应用状态
  async getAppStatus() {
    return this.mockRequest(async () => {
      // 模拟检查应用状态
      const apps = [
        {
          id: 'app-frontend',
          name: '应用端',
          url: 'http://localhost:8080',
          status: 'running',
          online_users: Math.floor(Math.random() * 50) + 10,
          today_conversations: Math.floor(Math.random() * 500) + 100,
          response_time: Math.floor(Math.random() * 1000) + 500,
          error_rate: Math.random() * 5,
          start_time: new Date(Date.now() - Math.random() * 86400000).toISOString()
        },
        {
          id: 'config-driven-frontend',
          name: '配置驱动端',
          url: 'http://localhost:8073',
          status: Math.random() > 0.5 ? 'running' : 'stopped',
          online_users: Math.floor(Math.random() * 10),
          today_conversations: Math.floor(Math.random() * 100),
          response_time: Math.floor(Math.random() * 1000) + 500,
          error_rate: Math.random() * 3,
          start_time: new Date(Date.now() - Math.random() * 86400000).toISOString()
        }
      ]
      
      return this.createResponse(apps)
    })
  }

  // 推送配置到应用端
  async pushConfigToApps(configType, configData, targetApps) {
    return this.mockRequest(async () => {
      const results = await localConfigCenter.syncConfigToApps(configType, configData, targetApps)

      return this.createResponse(results)
    }, 2000) // 模拟较长的网络请求
  }

  // 获取配置同步状态
  async getSyncStatus() {
    return this.mockRequest(async () => {
      const status = localConfigCenter.getSyncStatus()

      return this.createResponse(status)
    })
  }

  // ==================== 用户管理API ====================
  
  // 获取用户列表
  async getUserList(params = {}) {
    return this.mockRequest(async () => {
      // 模拟用户数据
      const users = [
        {
          id: 'user_001',
          username: 'admin',
          real_name: '系统管理员',
          email: 'admin@qms.com',
          phone: '13800138000',
          department: '系统管理部',
          department_id: 'dept_001',
          position: '系统管理员',
          role: 'ADMIN',
          status: 'active',
          ai_quota_daily: 2000,
          ai_quota_used: Math.floor(Math.random() * 500),
          ai_quota_monthly: 50000,
          cost_limit_monthly: 100,
          ai_models_allowed: ['gpt-4o', 'gpt-4o-mini', 'deepseek-r1'],
          feature_permissions: ['chat:create', 'chat:export', 'analytics:view'],
          conversation_retention_days: 365,
          export_permission: 'all',
          share_permissions: ['share_conversation', 'share_team'],
          last_login_at: new Date(Date.now() - Math.random() * 86400000).toISOString(),
          created_at: '2024-01-01T00:00:00.000Z'
        },
        {
          id: 'user_002',
          username: 'developer',
          real_name: '开发工程师',
          email: 'developer@qms.com',
          phone: '13800138001',
          department: '研发部',
          department_id: 'dept_002',
          position: '高级开发工程师',
          role: 'DEVELOPER',
          status: 'active',
          ai_quota_daily: 1000,
          ai_quota_used: Math.floor(Math.random() * 300),
          ai_quota_monthly: 25000,
          cost_limit_monthly: 50,
          ai_models_allowed: ['gpt-4o-mini', 'deepseek-r1'],
          feature_permissions: ['chat:create', 'chat:export'],
          conversation_retention_days: 90,
          export_permission: 'own',
          share_permissions: ['share_conversation'],
          last_login_at: new Date(Date.now() - Math.random() * 86400000).toISOString(),
          created_at: '2024-01-02T00:00:00.000Z'
        }
      ]
      
      // 模拟分页和搜索
      let filteredUsers = users

      if (params.keyword) {
        const keyword = params.keyword.toLowerCase()

        filteredUsers = users.filter(user => 
          user.username.toLowerCase().includes(keyword) ||
          user.real_name.toLowerCase().includes(keyword) ||
          user.email.toLowerCase().includes(keyword)
        )
      }
      
      const total = filteredUsers.length
      const page = params.page || 1
      const pageSize = params.pageSize || 20
      const start = (page - 1) * pageSize
      const end = start + pageSize
      
      return this.createResponse({
        list: filteredUsers.slice(start, end),
        total,
        page,
        pageSize
      })
    })
  }

  // 更新用户信息
  async updateUser(userId, userData) {
    return this.mockRequest(async () => {
      // 模拟更新用户
      console.log('更新用户:', userId, userData)

      return this.createResponse(null, true, '用户信息更新成功')
    })
  }

  // 更新用户权限
  async updateUserPermissions(userId, permissions) {
    return this.mockRequest(async () => {
      // 同步权限配置到应用端
      await localConfigCenter.setConfig(`user_permission.${userId}`, permissions, true)

      return this.createResponse(null, true, '用户权限更新成功')
    })
  }

  // ==================== 监控统计API ====================
  
  // 获取实时指标
  async getRealTimeMetrics() {
    return this.mockRequest(async () => {
      const metrics = {
        total_conversations: Math.floor(Math.random() * 2000) + 1000,
        avg_response_time: Math.floor(Math.random() * 2000) + 1000,
        today_cost: Math.random() * 50 + 10,
        error_rate: Math.random() * 5,
        active_users: Math.floor(Math.random() * 100) + 50,
        model_usage: {
          'gpt-4o': Math.floor(Math.random() * 500) + 200,
          'gpt-4o-mini': Math.floor(Math.random() * 800) + 400,
          'deepseek-r1': Math.floor(Math.random() * 300) + 100
        }
      }
      
      return this.createResponse(metrics)
    })
  }

  // 获取模型性能指标
  async getModelMetrics() {
    return this.mockRequest(async () => {
      const models = [
        {
          model_name: 'GPT-4o',
          status: 'healthy',
          requests_count: Math.floor(Math.random() * 1000) + 200,
          avg_response_time: Math.floor(Math.random() * 1000) + 1000,
          success_rate: 95 + Math.random() * 5,
          cost: Math.random() * 20 + 5
        },
        {
          model_name: 'GPT-4o Mini',
          status: 'healthy',
          requests_count: Math.floor(Math.random() * 1500) + 500,
          avg_response_time: Math.floor(Math.random() * 800) + 800,
          success_rate: 97 + Math.random() * 3,
          cost: Math.random() * 10 + 2
        },
        {
          model_name: 'DeepSeek R1',
          status: Math.random() > 0.7 ? 'healthy' : 'degraded',
          requests_count: Math.floor(Math.random() * 500) + 100,
          avg_response_time: Math.floor(Math.random() * 2000) + 1500,
          success_rate: 90 + Math.random() * 8,
          cost: Math.random() * 5 + 1
        }
      ]
      
      return this.createResponse(models)
    })
  }

  // ==================== 通用HTTP方法 ====================
  
  async get(url, params = {}) {
    // 根据URL路由到对应的方法
    if (url.includes('/auth/login')) return this.login(params)
    if (url.includes('/auth/userinfo')) return this.getUserInfo()
    if (url.includes('/config/')) return this.getConfig(params.key)
    if (url.includes('/app/status')) return this.getAppStatus()
    if (url.includes('/users')) return this.getUserList(params)
    if (url.includes('/metrics/realtime')) return this.getRealTimeMetrics()
    if (url.includes('/metrics/models')) return this.getModelMetrics()
    
    // 默认返回
    return this.createResponse(null, false, '接口未实现')
  }

  async post(url, data = {}) {
    if (url.includes('/auth/login')) return this.login(data)
    if (url.includes('/auth/logout')) return this.logout()
    if (url.includes('/config/update')) return this.updateConfig(data.key, data.value, data.sync)
    if (url.includes('/config/push')) return this.pushConfigToApps(data.config_type, data.config_data, data.target_apps)
    if (url.includes('/users/update')) return this.updateUser(data.id, data)
    if (url.includes('/users/permissions')) return this.updateUserPermissions(data.userId, data.permissions)
    
    return this.createResponse(null, false, '接口未实现')
  }

  async put(url, data = {}) {
    return this.post(url, data)
  }

  async delete(url, _params = {}) {
    return this.createResponse(null, true, '删除成功')
  }
}

// 创建全局API适配器实例
const localAPIAdapter = new LocalAPIAdapter()

export default localAPIAdapter
export { LocalAPIAdapter }
