/**
 * QMS AI缓存管理服务
 * 提供多层缓存策略，支持内存缓存和Redis缓存
 */

const Redis = require('redis')
const NodeCache = require('node-cache')
const winston = require('winston')

// 日志配置
const logger = winston.createLogger({
  level: 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.json()
  ),
  transports: [
    new winston.transports.File({ filename: 'logs/cache.log' }),
    new winston.transports.Console()
  ]
})

class CacheManager {
  constructor(options = {}) {
    this.options = {
      // Redis配置
      redis: {
        host: options.redisHost || 'localhost',
        port: options.redisPort || 6379,
        password: options.redisPassword || null,
        db: options.redisDb || 0
      },
      // 内存缓存配置
      memory: {
        stdTTL: options.memoryTTL || 600, // 10分钟
        checkperiod: options.checkPeriod || 120, // 2分钟检查一次
        maxKeys: options.maxKeys || 1000
      },
      // 缓存策略
      strategy: options.strategy || 'hybrid' // 'memory', 'redis', 'hybrid'
    }
    
    this.memoryCache = null
    this.redisClient = null
    this.isRedisConnected = false
    
    this.init()
  }
  
  async init() {
    try {
      // 初始化内存缓存
      this.memoryCache = new NodeCache(this.options.memory)
      logger.info('内存缓存初始化成功')
      
      // 初始化Redis缓存
      if (this.options.strategy === 'redis' || this.options.strategy === 'hybrid') {
        await this.initRedis()
      }
      
      // 设置缓存统计
      this.stats = {
        hits: 0,
        misses: 0,
        sets: 0,
        deletes: 0,
        errors: 0
      }
      
      logger.info(`缓存管理器初始化完成，策略: ${this.options.strategy}`)
    } catch (error) {
      logger.error('缓存管理器初始化失败:', error)
      throw error
    }
  }
  
  async initRedis() {
    try {
      this.redisClient = Redis.createClient(this.options.redis)
      
      this.redisClient.on('connect', () => {
        logger.info('Redis连接建立')
      })
      
      this.redisClient.on('ready', () => {
        this.isRedisConnected = true
        logger.info('Redis连接就绪')
      })
      
      this.redisClient.on('error', (error) => {
        this.isRedisConnected = false
        logger.error('Redis连接错误:', error)
      })
      
      this.redisClient.on('end', () => {
        this.isRedisConnected = false
        logger.warn('Redis连接断开')
      })
      
      await this.redisClient.connect()
    } catch (error) {
      logger.error('Redis初始化失败:', error)
      // 如果是混合策略，Redis失败时降级到内存缓存
      if (this.options.strategy === 'hybrid') {
        logger.warn('Redis不可用，降级到内存缓存')
        this.options.strategy = 'memory'
      } else {
        throw error
      }
    }
  }
  
  // 获取缓存
  async get(key) {
    try {
      let value = null
      
      // 优先从内存缓存获取
      if (this.options.strategy === 'memory' || this.options.strategy === 'hybrid') {
        value = this.memoryCache.get(key)
        if (value !== undefined) {
          this.stats.hits++
          logger.debug(`内存缓存命中: ${key}`)
          return value
        }
      }
      
      // 从Redis获取
      if ((this.options.strategy === 'redis' || this.options.strategy === 'hybrid') && this.isRedisConnected) {
        const redisValue = await this.redisClient.get(key)
        if (redisValue !== null) {
          value = JSON.parse(redisValue)
          
          // 如果是混合策略，同时写入内存缓存
          if (this.options.strategy === 'hybrid') {
            this.memoryCache.set(key, value, this.options.memory.stdTTL)
          }
          
          this.stats.hits++
          logger.debug(`Redis缓存命中: ${key}`)
          return value
        }
      }
      
      this.stats.misses++
      logger.debug(`缓存未命中: ${key}`)
      return null
    } catch (error) {
      this.stats.errors++
      logger.error(`获取缓存失败 ${key}:`, error)
      return null
    }
  }
  
  // 设置缓存
  async set(key, value, ttl = null) {
    try {
      const actualTTL = ttl || this.options.memory.stdTTL
      
      // 写入内存缓存
      if (this.options.strategy === 'memory' || this.options.strategy === 'hybrid') {
        this.memoryCache.set(key, value, actualTTL)
      }
      
      // 写入Redis缓存
      if ((this.options.strategy === 'redis' || this.options.strategy === 'hybrid') && this.isRedisConnected) {
        await this.redisClient.setEx(key, actualTTL, JSON.stringify(value))
      }
      
      this.stats.sets++
      logger.debug(`缓存设置成功: ${key}, TTL: ${actualTTL}s`)
      return true
    } catch (error) {
      this.stats.errors++
      logger.error(`设置缓存失败 ${key}:`, error)
      return false
    }
  }
  
  // 删除缓存
  async delete(key) {
    try {
      // 从内存缓存删除
      if (this.options.strategy === 'memory' || this.options.strategy === 'hybrid') {
        this.memoryCache.del(key)
      }
      
      // 从Redis删除
      if ((this.options.strategy === 'redis' || this.options.strategy === 'hybrid') && this.isRedisConnected) {
        await this.redisClient.del(key)
      }
      
      this.stats.deletes++
      logger.debug(`缓存删除成功: ${key}`)
      return true
    } catch (error) {
      this.stats.errors++
      logger.error(`删除缓存失败 ${key}:`, error)
      return false
    }
  }
  
  // 批量删除缓存
  async deletePattern(pattern) {
    try {
      let deletedCount = 0
      
      // 内存缓存批量删除
      if (this.options.strategy === 'memory' || this.options.strategy === 'hybrid') {
        const keys = this.memoryCache.keys()
        const matchedKeys = keys.filter(key => key.includes(pattern))
        matchedKeys.forEach(key => this.memoryCache.del(key))
        deletedCount += matchedKeys.length
      }
      
      // Redis批量删除
      if ((this.options.strategy === 'redis' || this.options.strategy === 'hybrid') && this.isRedisConnected) {
        const keys = await this.redisClient.keys(`*${pattern}*`)
        if (keys.length > 0) {
          await this.redisClient.del(keys)
          deletedCount += keys.length
        }
      }
      
      logger.info(`批量删除缓存: ${pattern}, 删除数量: ${deletedCount}`)
      return deletedCount
    } catch (error) {
      this.stats.errors++
      logger.error(`批量删除缓存失败 ${pattern}:`, error)
      return 0
    }
  }
  
  // 清空所有缓存
  async clear() {
    try {
      // 清空内存缓存
      if (this.options.strategy === 'memory' || this.options.strategy === 'hybrid') {
        this.memoryCache.flushAll()
      }
      
      // 清空Redis缓存
      if ((this.options.strategy === 'redis' || this.options.strategy === 'hybrid') && this.isRedisConnected) {
        await this.redisClient.flushDb()
      }
      
      logger.info('所有缓存已清空')
      return true
    } catch (error) {
      this.stats.errors++
      logger.error('清空缓存失败:', error)
      return false
    }
  }
  
  // 获取缓存统计
  getStats() {
    const memoryStats = this.memoryCache ? this.memoryCache.getStats() : {}
    
    return {
      ...this.stats,
      memory: {
        keys: memoryStats.keys || 0,
        hits: memoryStats.hits || 0,
        misses: memoryStats.misses || 0,
        ksize: memoryStats.ksize || 0,
        vsize: memoryStats.vsize || 0
      },
      redis: {
        connected: this.isRedisConnected,
        strategy: this.options.strategy
      },
      hitRate: this.stats.hits / (this.stats.hits + this.stats.misses) || 0
    }
  }
  
  // 健康检查
  async healthCheck() {
    const health = {
      status: 'healthy',
      memory: true,
      redis: this.isRedisConnected,
      strategy: this.options.strategy,
      stats: this.getStats()
    }
    
    // 测试内存缓存
    try {
      const testKey = '__health_check_memory__'
      this.memoryCache.set(testKey, 'test', 1)
      const testValue = this.memoryCache.get(testKey)
      health.memory = testValue === 'test'
      this.memoryCache.del(testKey)
    } catch (error) {
      health.memory = false
      health.status = 'degraded'
    }
    
    // 测试Redis缓存
    if (this.isRedisConnected) {
      try {
        const testKey = '__health_check_redis__'
        await this.redisClient.setEx(testKey, 1, 'test')
        const testValue = await this.redisClient.get(testKey)
        health.redis = testValue === 'test'
        await this.redisClient.del(testKey)
      } catch (error) {
        health.redis = false
        health.status = 'degraded'
      }
    }
    
    if (!health.memory && !health.redis) {
      health.status = 'unhealthy'
    }
    
    return health
  }
  
  // 关闭连接
  async close() {
    try {
      if (this.memoryCache) {
        this.memoryCache.close()
      }
      
      if (this.redisClient && this.isRedisConnected) {
        await this.redisClient.quit()
      }
      
      logger.info('缓存管理器已关闭')
    } catch (error) {
      logger.error('关闭缓存管理器失败:', error)
    }
  }
}

// 创建单例实例
let cacheManagerInstance = null

function createCacheManager(options = {}) {
  if (!cacheManagerInstance) {
    cacheManagerInstance = new CacheManager(options)
  }
  return cacheManagerInstance
}

function getCacheManager() {
  if (!cacheManagerInstance) {
    throw new Error('缓存管理器未初始化，请先调用 createCacheManager()')
  }
  return cacheManagerInstance
}

module.exports = {
  CacheManager,
  createCacheManager,
  getCacheManager
}
