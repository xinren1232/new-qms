/**
 * 聊天服务缓存集成模块
 * 为聊天服务提供智能缓存功能
 */

const { createCacheManager } = require('../cache-service/cache-manager')

class ChatCacheService {
  constructor() {
    // 初始化缓存管理器
    this.cache = createCacheManager({
      strategy: 'hybrid', // 使用混合缓存策略
      memoryTTL: 300,     // 内存缓存5分钟
      maxKeys: 500        // 最大缓存500个键
    })
    
    // 缓存键前缀
    this.prefixes = {
      conversation: 'conv:',
      userProfile: 'user:',
      modelResponse: 'model:',
      systemConfig: 'config:',
      statistics: 'stats:'
    }
    
    // 缓存TTL配置 (秒)
    this.ttl = {
      conversation: 1800,    // 对话缓存30分钟
      userProfile: 3600,     // 用户配置1小时
      modelResponse: 600,    // 模型响应10分钟
      systemConfig: 7200,    // 系统配置2小时
      statistics: 300        // 统计数据5分钟
    }
  }
  
  // 生成缓存键
  generateKey(prefix, ...parts) {
    return prefix + parts.join(':')
  }
  
  // 对话相关缓存
  async getCachedConversation(userId, conversationId) {
    const key = this.generateKey(this.prefixes.conversation, userId, conversationId)
    return await this.cache.get(key)
  }
  
  async setCachedConversation(userId, conversationId, conversation) {
    const key = this.generateKey(this.prefixes.conversation, userId, conversationId)
    return await this.cache.set(key, conversation, this.ttl.conversation)
  }
  
  async deleteCachedConversation(userId, conversationId) {
    const key = this.generateKey(this.prefixes.conversation, userId, conversationId)
    return await this.cache.delete(key)
  }
  
  // 用户配置缓存
  async getCachedUserProfile(userId) {
    const key = this.generateKey(this.prefixes.userProfile, userId)
    return await this.cache.get(key)
  }
  
  async setCachedUserProfile(userId, profile) {
    const key = this.generateKey(this.prefixes.userProfile, userId)
    return await this.cache.set(key, profile, this.ttl.userProfile)
  }
  
  async invalidateUserProfile(userId) {
    const key = this.generateKey(this.prefixes.userProfile, userId)
    return await this.cache.delete(key)
  }
  
  // 模型响应缓存 (相同问题的缓存)
  async getCachedModelResponse(modelName, questionHash) {
    const key = this.generateKey(this.prefixes.modelResponse, modelName, questionHash)
    return await this.cache.get(key)
  }
  
  async setCachedModelResponse(modelName, questionHash, response) {
    const key = this.generateKey(this.prefixes.modelResponse, modelName, questionHash)
    return await this.cache.set(key, response, this.ttl.modelResponse)
  }
  
  // 系统配置缓存
  async getCachedSystemConfig(configKey) {
    const key = this.generateKey(this.prefixes.systemConfig, configKey)
    return await this.cache.get(key)
  }
  
  async setCachedSystemConfig(configKey, config) {
    const key = this.generateKey(this.prefixes.systemConfig, configKey)
    return await this.cache.set(key, config, this.ttl.systemConfig)
  }
  
  async invalidateSystemConfig(configKey = null) {
    if (configKey) {
      const key = this.generateKey(this.prefixes.systemConfig, configKey)
      return await this.cache.delete(key)
    } else {
      // 清空所有系统配置缓存
      return await this.cache.deletePattern(this.prefixes.systemConfig)
    }
  }
  
  // 统计数据缓存
  async getCachedStatistics(statsType, timeRange) {
    const key = this.generateKey(this.prefixes.statistics, statsType, timeRange)
    return await this.cache.get(key)
  }
  
  async setCachedStatistics(statsType, timeRange, stats) {
    const key = this.generateKey(this.prefixes.statistics, statsType, timeRange)
    return await this.cache.set(key, stats, this.ttl.statistics)
  }
  
  // 智能缓存策略
  async shouldCacheResponse(question, response) {
    // 缓存策略判断
    const conditions = [
      question.length > 10,           // 问题长度大于10字符
      response.length > 50,           // 回答长度大于50字符
      !question.includes('时间'),      // 不包含时间相关词汇
      !question.includes('现在'),      // 不包含实时性词汇
      !question.includes('今天'),      // 不包含日期相关词汇
      !response.includes('当前时间')   // 回答不包含时间信息
    ]
    
    return conditions.filter(Boolean).length >= 4
  }
  
  // 生成问题哈希
  generateQuestionHash(question, context = '') {
    const crypto = require('crypto')
    const content = question + (context || '')
    return crypto.createHash('md5').update(content).digest('hex')
  }
  
  // 缓存中间件
  createCacheMiddleware() {
    return async (req, res, next) => {
      // 为请求添加缓存服务
      req.cacheService = this
      
      // 添加缓存辅助方法
      req.getCached = async (key) => {
        return await this.cache.get(key)
      }
      
      req.setCached = async (key, value, ttl) => {
        return await this.cache.set(key, value, ttl)
      }
      
      next()
    }
  }
  
  // 预热缓存
  async warmupCache() {
    try {
      console.log('🔥 开始预热缓存...')
      
      // 预热系统配置
      const systemConfigs = [
        'ai_models',
        'rate_limits',
        'feature_flags'
      ]
      
      for (const config of systemConfigs) {
        // 这里应该从数据库加载配置
        const configData = await this.loadSystemConfig(config)
        if (configData) {
          await this.setCachedSystemConfig(config, configData)
          console.log(`✅ 预热配置: ${config}`)
        }
      }
      
      console.log('🎉 缓存预热完成')
    } catch (error) {
      console.error('❌ 缓存预热失败:', error)
    }
  }
  
  // 模拟加载系统配置 (实际应该从数据库加载)
  async loadSystemConfig(configKey) {
    const configs = {
      ai_models: {
        'gpt-4o': { enabled: true, priority: 1 },
        'claude-3': { enabled: true, priority: 2 },
        'deepseek': { enabled: true, priority: 3 }
      },
      rate_limits: {
        chat: { requests: 30, window: 60000 },
        api: { requests: 100, window: 60000 }
      },
      feature_flags: {
        smart_recommendations: true,
        theme_switching: true,
        advanced_analytics: false
      }
    }
    
    return configs[configKey] || null
  }
  
  // 获取缓存统计
  async getStats() {
    return await this.cache.getStats()
  }
  
  // 健康检查
  async healthCheck() {
    return await this.cache.healthCheck()
  }
  
  // 清理过期缓存
  async cleanup() {
    try {
      console.log('🧹 开始清理过期缓存...')
      
      // 清理超过1天的对话缓存
      const oneDayAgo = Date.now() - (24 * 60 * 60 * 1000)
      await this.cache.deletePattern(`${this.prefixes.conversation}*:${oneDayAgo}`)
      
      // 清理超过1小时的统计缓存
      await this.cache.deletePattern(this.prefixes.statistics)
      
      console.log('✅ 缓存清理完成')
    } catch (error) {
      console.error('❌ 缓存清理失败:', error)
    }
  }
}

// 创建单例实例
let chatCacheInstance = null

function createChatCacheService() {
  if (!chatCacheInstance) {
    chatCacheInstance = new ChatCacheService()
  }
  return chatCacheInstance
}

function getChatCacheService() {
  if (!chatCacheInstance) {
    throw new Error('聊天缓存服务未初始化')
  }
  return chatCacheInstance
}

module.exports = {
  ChatCacheService,
  createChatCacheService,
  getChatCacheService
}
