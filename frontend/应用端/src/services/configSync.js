/**
 * 配置同步服务
 * 负责与配置端的数据同步
 */
import request from '@/utils/request'

class ConfigSyncService {
  constructor() {
    this.configEndpoint = import.meta.env.VITE_APP_CONFIG_API || 'http://localhost:3001'
  }

  /**
   * 同步配置数据
   * @param {string} type - 配置类型 (conversation, monitoring, data-source)
   * @param {Object} params - 同步参数
   */
  async syncConfig(type, params = {}) {
    try {
      const response = await request({
        url: `/api/config/sync/${type}`,
        method: 'post',
        data: params
      })
      return response.data
    } catch (error) {
      console.error(`配置同步失败 [${type}]:`, error)
      throw error
    }
  }

  /**
   * 获取配置端数据
   * @param {string} type - 数据类型
   * @param {Object} params - 查询参数
   */
  async getConfigData(type, params = {}) {
    try {
      const response = await request({
        url: `/api/config/${type}`,
        method: 'get',
        params
      })
      return response.data
    } catch (error) {
      console.error(`获取配置数据失败 [${type}]:`, error)
      throw error
    }
  }

  /**
   * 推送数据到配置端
   * @param {string} type - 数据类型
   * @param {Object} data - 推送数据
   */
  async pushToConfig(type, data) {
    try {
      const response = await request({
        url: `/api/config/${type}`,
        method: 'post',
        data
      })
      return response.data
    } catch (error) {
      console.error(`推送配置数据失败 [${type}]:`, error)
      throw error
    }
  }

  /**
   * 监听配置变化
   * @param {string} type - 监听类型
   * @param {Function} callback - 回调函数
   */
  watchConfig(type, callback) {
    // 使用WebSocket或轮询方式监听配置变化
    const eventSource = new EventSource(`${this.configEndpoint}/api/config/watch/${type}`)
    
    eventSource.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        callback(data)
      } catch (error) {
        console.error('配置变化监听解析错误:', error)
      }
    }

    eventSource.onerror = (error) => {
      console.error('配置变化监听错误:', error)
    }

    return eventSource
  }

  /**
   * 批量同步多个配置
   * @param {Array} configs - 配置列表
   */
  async batchSync(configs) {
    try {
      const promises = configs.map(config => 
        this.syncConfig(config.type, config.params)
      )
      const results = await Promise.allSettled(promises)
      
      const success = results.filter(r => r.status === 'fulfilled').map(r => r.value)
      const errors = results.filter(r => r.status === 'rejected').map(r => r.reason)
      
      return { success, errors }
    } catch (error) {
      console.error('批量同步失败:', error)
      throw error
    }
  }

  /**
   * 监听配置更新事件
   * @param {Function} callback - 回调函数
   */
  onConfigUpdate(callback) {
    // 监听自定义配置更新事件
    window.addEventListener('configUpdated', callback)

    // 返回取消监听的函数
    return () => {
      window.removeEventListener('configUpdated', callback)
    }
  }

  /**
   * 获取缓存的配置
   * @param {string} type - 配置类型
   * @returns {Object|null} 缓存的配置数据
   */
  getCachedConfig(type) {
    try {
      const cached = localStorage.getItem(`config_${type}`)
      return cached ? JSON.parse(cached) : null
    } catch (error) {
      console.error(`获取缓存配置失败 [${type}]:`, error)
      return null
    }
  }

  /**
   * 设置缓存配置
   * @param {string} type - 配置类型
   * @param {Object} data - 配置数据
   */
  setCachedConfig(type, data) {
    try {
      localStorage.setItem(`config_${type}`, JSON.stringify(data))
    } catch (error) {
      console.error(`设置缓存配置失败 [${type}]:`, error)
    }
  }
}

export default new ConfigSyncService()
