/**
 * 增强版内存缓存服务
 * 无需外部依赖，支持持久化，兼容Redis接口
 */

const fs = require('fs');
const path = require('path');
const EventEmitter = require('events');

class EnhancedMemoryCache extends EventEmitter {
  constructor(options = {}) {
    super();
    
    this.options = {
      persistFile: options.persistFile || './cache/memory-cache.json',
      autoSave: options.autoSave !== false,
      saveInterval: options.saveInterval || 60000, // 1分钟自动保存
      maxMemory: options.maxMemory || 100 * 1024 * 1024, // 100MB
      evictionPolicy: options.evictionPolicy || 'lru' // lru, lfu, random
    };
    
    this.cache = new Map();
    this.ttlMap = new Map();
    this.accessCount = new Map(); // LFU计数
    this.accessTime = new Map();  // LRU时间
    this.memoryUsage = 0;
    
    this.connected = true; // 兼容Redis接口
    
    // 缓存策略配置（兼容Redis服务）
    this.cacheStrategies = {
      ai_models: { ttl: 1800, prefix: 'qms:ai:models:' },
      config_data: { ttl: 3600, prefix: 'qms:config:' },
      user_session: { ttl: 86400, prefix: 'qms:session:' },
      chat_history: { ttl: 7200, prefix: 'qms:chat:' },
      system_status: { ttl: 300, prefix: 'qms:status:' }
    };
    
    this.init();
  }

  async init() {
    try {
      // 确保缓存目录存在
      const cacheDir = path.dirname(this.options.persistFile);
      if (!fs.existsSync(cacheDir)) {
        fs.mkdirSync(cacheDir, { recursive: true });
      }
      
      // 加载持久化数据
      await this.loadFromFile();
      
      // 启动自动保存
      if (this.options.autoSave) {
        this.startAutoSave();
      }
      
      // 启动过期清理
      this.startTTLCleanup();
      
      console.log('✅ 增强内存缓存服务已启动');
      console.log(`📁 持久化文件: ${this.options.persistFile}`);
      console.log(`💾 最大内存: ${Math.round(this.options.maxMemory / 1024 / 1024)}MB`);
      
      this.emit('ready');
    } catch (error) {
      console.error('❌ 增强内存缓存初始化失败:', error.message);
      this.emit('error', error);
    }
  }

  // Redis兼容接口
  isConnected() {
    return this.connected;
  }

  async connect() {
    return this;
  }

  async close() {
    if (this.autoSaveTimer) {
      clearInterval(this.autoSaveTimer);
    }
    if (this.ttlCleanupTimer) {
      clearInterval(this.ttlCleanupTimer);
    }
    await this.saveToFile();
    this.connected = false;
    console.log('🔒 增强内存缓存已关闭');
  }

  // 缓存策略兼容方法
  getCacheKey(strategy, key) {
    const config = this.cacheStrategies[strategy];
    if (!config) {
      throw new Error(`未知的缓存策略: ${strategy}`);
    }
    return `${config.prefix}${key}`;
  }

  async set(strategyOrKey, keyOrValue, valueOrTTL = null, customTTL = null) {
    try {
      let finalKey, finalValue, finalTTL;
      
      // 兼容两种调用方式
      if (typeof strategyOrKey === 'string' && this.cacheStrategies[strategyOrKey]) {
        // 策略模式: set(strategy, key, value, customTTL)
        const strategy = strategyOrKey;
        const key = keyOrValue;
        const value = valueOrTTL;
        const config = this.cacheStrategies[strategy];
        
        finalKey = this.getCacheKey(strategy, key);
        finalValue = value;
        finalTTL = customTTL || config.ttl;
      } else {
        // 直接模式: set(key, value, ttl)
        finalKey = strategyOrKey;
        finalValue = keyOrValue;
        finalTTL = valueOrTTL || 0;
      }
      
      // 检查内存限制
      const valueSize = this.calculateSize(finalValue);
      if (this.memoryUsage + valueSize > this.options.maxMemory) {
        await this.evictMemory(valueSize);
      }
      
      // 设置缓存
      this.cache.set(finalKey, finalValue);
      this.memoryUsage += valueSize;
      
      // 设置TTL
      if (finalTTL > 0) {
        this.ttlMap.set(finalKey, Date.now() + finalTTL * 1000);
      }
      
      // 更新访问统计
      this.updateAccessStats(finalKey);
      
      return true;
    } catch (error) {
      console.error('❌ 缓存设置失败:', error.message);
      return false;
    }
  }

  async get(strategyOrKey, key = null) {
    try {
      let finalKey;
      
      if (typeof strategyOrKey === 'string' && this.cacheStrategies[strategyOrKey] && key !== null) {
        // 策略模式: get(strategy, key)
        finalKey = this.getCacheKey(strategyOrKey, key);
      } else {
        // 直接模式: get(key)
        finalKey = strategyOrKey;
      }
      
      // 检查TTL
      if (this.ttlMap.has(finalKey)) {
        if (Date.now() > this.ttlMap.get(finalKey)) {
          await this.del(finalKey);
          return null;
        }
      }
      
      const value = this.cache.get(finalKey);
      if (value !== undefined) {
        this.updateAccessStats(finalKey);
        return value;
      }
      
      return null;
    } catch (error) {
      console.error('❌ 缓存获取失败:', error.message);
      return null;
    }
  }

  async del(strategyOrKey, key = null) {
    try {
      let finalKey;
      
      if (typeof strategyOrKey === 'string' && this.cacheStrategies[strategyOrKey] && key !== null) {
        finalKey = this.getCacheKey(strategyOrKey, key);
      } else {
        finalKey = strategyOrKey;
      }
      
      if (this.cache.has(finalKey)) {
        const value = this.cache.get(finalKey);
        this.memoryUsage -= this.calculateSize(value);
        
        this.cache.delete(finalKey);
        this.ttlMap.delete(finalKey);
        this.accessCount.delete(finalKey);
        this.accessTime.delete(finalKey);
        
        return true;
      }
      
      return false;
    } catch (error) {
      console.error('❌ 缓存删除失败:', error.message);
      return false;
    }
  }

  async exists(strategyOrKey, key = null) {
    const value = await this.get(strategyOrKey, key);
    return value !== null;
  }

  async expire(strategyOrKey, keyOrTTL, ttl = null) {
    try {
      let finalKey, finalTTL;
      
      if (typeof strategyOrKey === 'string' && this.cacheStrategies[strategyOrKey] && ttl !== null) {
        finalKey = this.getCacheKey(strategyOrKey, keyOrTTL);
        finalTTL = ttl;
      } else {
        finalKey = strategyOrKey;
        finalTTL = keyOrTTL;
      }
      
      if (this.cache.has(finalKey)) {
        this.ttlMap.set(finalKey, Date.now() + finalTTL * 1000);
        return true;
      }
      
      return false;
    } catch (error) {
      console.error('❌ 设置过期时间失败:', error.message);
      return false;
    }
  }

  async ttl(strategyOrKey, key = null) {
    try {
      let finalKey;
      
      if (typeof strategyOrKey === 'string' && this.cacheStrategies[strategyOrKey] && key !== null) {
        finalKey = this.getCacheKey(strategyOrKey, key);
      } else {
        finalKey = strategyOrKey;
      }
      
      if (this.ttlMap.has(finalKey)) {
        const expireTime = this.ttlMap.get(finalKey);
        const remaining = Math.max(0, Math.floor((expireTime - Date.now()) / 1000));
        return remaining;
      }
      
      return -1;
    } catch (error) {
      console.error('❌ 获取TTL失败:', error.message);
      return -1;
    }
  }

  async clearStrategy(strategy) {
    try {
      const config = this.cacheStrategies[strategy];
      const pattern = config.prefix;
      let count = 0;
      
      for (const key of this.cache.keys()) {
        if (key.startsWith(pattern)) {
          await this.del(key);
          count++;
        }
      }
      
      console.log(`✅ 已清空缓存策略: ${strategy} (${count}个键)`);
      return true;
    } catch (error) {
      console.error(`❌ 清空缓存策略失败 ${strategy}:`, error.message);
      return false;
    }
  }

  // 内存管理
  calculateSize(value) {
    return JSON.stringify(value).length * 2; // 粗略估算
  }

  async evictMemory(neededSize) {
    const targetSize = this.options.maxMemory * 0.8; // 清理到80%
    
    while (this.memoryUsage > targetSize) {
      let keyToEvict;
      
      switch (this.options.evictionPolicy) {
        case 'lru':
          keyToEvict = this.findLRUKey();
          break;
        case 'lfu':
          keyToEvict = this.findLFUKey();
          break;
        default:
          keyToEvict = this.findRandomKey();
      }
      
      if (keyToEvict) {
        await this.del(keyToEvict);
      } else {
        break;
      }
    }
  }

  findLRUKey() {
    let oldestKey = null;
    let oldestTime = Date.now();
    
    for (const [key, time] of this.accessTime.entries()) {
      if (time < oldestTime) {
        oldestTime = time;
        oldestKey = key;
      }
    }
    
    return oldestKey;
  }

  findLFUKey() {
    let leastKey = null;
    let leastCount = Infinity;
    
    for (const [key, count] of this.accessCount.entries()) {
      if (count < leastCount) {
        leastCount = count;
        leastKey = key;
      }
    }
    
    return leastKey;
  }

  findRandomKey() {
    const keys = Array.from(this.cache.keys());
    return keys[Math.floor(Math.random() * keys.length)];
  }

  updateAccessStats(key) {
    this.accessTime.set(key, Date.now());
    this.accessCount.set(key, (this.accessCount.get(key) || 0) + 1);
  }

  // 持久化
  async saveToFile() {
    try {
      const data = {
        cache: Array.from(this.cache.entries()),
        ttl: Array.from(this.ttlMap.entries()),
        accessCount: Array.from(this.accessCount.entries()),
        accessTime: Array.from(this.accessTime.entries()),
        timestamp: Date.now(),
        version: '1.0'
      };
      
      fs.writeFileSync(this.options.persistFile, JSON.stringify(data, null, 2));
      console.log('💾 缓存已保存到文件');
    } catch (error) {
      console.error('❌ 缓存保存失败:', error.message);
    }
  }

  async loadFromFile() {
    try {
      if (fs.existsSync(this.options.persistFile)) {
        const data = JSON.parse(fs.readFileSync(this.options.persistFile, 'utf8'));
        
        this.cache = new Map(data.cache || []);
        this.ttlMap = new Map(data.ttl || []);
        this.accessCount = new Map(data.accessCount || []);
        this.accessTime = new Map(data.accessTime || []);
        
        // 清理过期数据
        await this.cleanupExpired();
        
        // 重新计算内存使用
        this.recalculateMemoryUsage();
        
        console.log(`✅ 缓存已从文件恢复 (${this.cache.size}个键)`);
      }
    } catch (error) {
      console.warn('⚠️ 缓存文件加载失败:', error.message);
    }
  }

  recalculateMemoryUsage() {
    this.memoryUsage = 0;
    for (const value of this.cache.values()) {
      this.memoryUsage += this.calculateSize(value);
    }
  }

  startAutoSave() {
    this.autoSaveTimer = setInterval(() => {
      this.saveToFile();
    }, this.options.saveInterval);
  }

  startTTLCleanup() {
    this.ttlCleanupTimer = setInterval(() => {
      this.cleanupExpired();
    }, 30000); // 30秒清理一次
  }

  async cleanupExpired() {
    const now = Date.now();
    const expiredKeys = [];
    
    for (const [key, expireTime] of this.ttlMap.entries()) {
      if (now > expireTime) {
        expiredKeys.push(key);
      }
    }
    
    for (const key of expiredKeys) {
      await this.del(key);
    }
    
    if (expiredKeys.length > 0) {
      console.log(`🧹 已清理过期缓存: ${expiredKeys.length}个键`);
    }
  }

  // 统计信息
  async getStats() {
    return {
      connected: this.connected,
      total_keys: this.cache.size,
      memory_usage: `${Math.round(this.memoryUsage / 1024 / 1024 * 100) / 100}MB`,
      memory_limit: `${Math.round(this.options.maxMemory / 1024 / 1024)}MB`,
      memory_usage_percent: `${Math.round(this.memoryUsage / this.options.maxMemory * 100)}%`,
      strategies: Object.fromEntries(
        Object.keys(this.cacheStrategies).map(strategy => {
          const prefix = this.cacheStrategies[strategy].prefix;
          const count = Array.from(this.cache.keys()).filter(key => key.startsWith(prefix)).length;
          return [strategy, { count, ttl: this.cacheStrategies[strategy].ttl }];
        })
      )
    };
  }

  async healthCheck() {
    try {
      const stats = await this.getStats();
      return {
        status: 'healthy',
        type: 'enhanced_memory_cache',
        ...stats
      };
    } catch (error) {
      return {
        status: 'unhealthy',
        message: error.message
      };
    }
  }
}

module.exports = EnhancedMemoryCache;
