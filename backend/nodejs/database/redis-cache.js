const redis = require('redis');

class RedisCache {
  constructor() {
    this.client = null;
    this.isConnected = false;
  }

  // 初始化Redis连接
  async initialize() {
    try {
      const config = {
        socket: {
          host: process.env.REDIS_HOST || 'localhost',
          port: parseInt(process.env.REDIS_PORT) || 6379,
          connectTimeout: 10000
        },
        password: process.env.REDIS_PASSWORD || undefined,
        database: parseInt(process.env.REDIS_DB) || 0
      };

      this.client = redis.createClient(config);

      // 错误处理
      this.client.on('error', (err) => {
        console.error('❌ Redis连接错误:', err.message);
        this.isConnected = false;
      });

      this.client.on('connect', () => {
        console.log('🔄 Redis正在连接...');
      });

      this.client.on('ready', () => {
        console.log('✅ Redis连接成功');
        this.isConnected = true;
      });

      this.client.on('end', () => {
        console.log('⚠️ Redis连接已断开');
        this.isConnected = false;
      });

      // 连接Redis
      await this.client.connect();

      // 测试连接
      await this.client.ping();
      console.log('📍 Redis服务器响应正常');

      return true;
    } catch (error) {
      console.error('❌ Redis初始化失败:', error.message);
      this.isConnected = false;
      return false;
    }
  }

  // 检查连接状态
  isReady() {
    return this.isConnected && this.client && this.client.isReady;
  }

  // 设置缓存
  async set(key, value, ttl = 3600) {
    if (!this.isReady()) {
      console.warn('⚠️ Redis未连接，跳过缓存设置');
      return false;
    }

    try {
      const serializedValue = JSON.stringify(value);
      await this.client.setEx(key, ttl, serializedValue);
      return true;
    } catch (error) {
      console.error('❌ Redis设置缓存失败:', error.message);
      return false;
    }
  }

  // 获取缓存
  async get(key) {
    if (!this.isReady()) {
      console.warn('⚠️ Redis未连接，跳过缓存获取');
      return null;
    }

    try {
      const value = await this.client.get(key);
      return value ? JSON.parse(value) : null;
    } catch (error) {
      console.error('❌ Redis获取缓存失败:', error.message);
      return null;
    }
  }

  // 删除缓存
  async del(key) {
    if (!this.isReady()) {
      return false;
    }

    try {
      await this.client.del(key);
      return true;
    } catch (error) {
      console.error('❌ Redis删除缓存失败:', error.message);
      return false;
    }
  }

  // 批量删除缓存（通过模式匹配）
  async delPattern(pattern) {
    if (!this.isReady()) {
      return false;
    }

    try {
      const keys = await this.client.keys(pattern);
      if (keys.length > 0) {
        await this.client.del(keys);
      }
      return true;
    } catch (error) {
      console.error('❌ Redis批量删除缓存失败:', error.message);
      return false;
    }
  }

  // 检查键是否存在
  async exists(key) {
    if (!this.isReady()) {
      return false;
    }

    try {
      const result = await this.client.exists(key);
      return result === 1;
    } catch (error) {
      console.error('❌ Redis检查键存在失败:', error.message);
      return false;
    }
  }

  // 设置键的过期时间
  async expire(key, ttl) {
    if (!this.isReady()) {
      return false;
    }

    try {
      await this.client.expire(key, ttl);
      return true;
    } catch (error) {
      console.error('❌ Redis设置过期时间失败:', error.message);
      return false;
    }
  }

  // 获取键的剩余过期时间
  async ttl(key) {
    if (!this.isReady()) {
      return -1;
    }

    try {
      return await this.client.ttl(key);
    } catch (error) {
      console.error('❌ Redis获取TTL失败:', error.message);
      return -1;
    }
  }

  // 缓存用户会话信息
  async cacheUserSession(userId, sessionData, ttl = 86400) { // 24小时
    const key = `user:session:${userId}`;
    return await this.set(key, sessionData, ttl);
  }

  // 获取用户会话信息
  async getUserSession(userId) {
    const key = `user:session:${userId}`;
    return await this.get(key);
  }

  // 缓存对话列表
  async cacheUserConversations(userId, conversations, ttl = 1800) { // 30分钟
    const key = `user:conversations:${userId}`;
    return await this.set(key, conversations, ttl);
  }

  // 获取缓存的对话列表
  async getUserConversations(userId) {
    const key = `user:conversations:${userId}`;
    return await this.get(key);
  }

  // 清除用户相关缓存
  async clearUserCache(userId) {
    const patterns = [
      `user:session:${userId}`,
      `user:conversations:${userId}`,
      `user:stats:${userId}`,
      `conversation:${userId}:*`
    ];

    for (const pattern of patterns) {
      await this.delPattern(pattern);
    }
    return true;
  }

  // 缓存AI模型列表
  async cacheAIModels(models, ttl = 3600) { // 1小时
    const key = 'ai:models';
    return await this.set(key, models, ttl);
  }

  // 获取AI模型列表
  async getAIModels() {
    const key = 'ai:models';
    return await this.get(key);
  }

  // 缓存系统统计信息
  async cacheSystemStats(stats, ttl = 300) { // 5分钟
    const key = 'system:stats';
    return await this.set(key, stats, ttl);
  }

  // 获取系统统计信息
  async getSystemStats() {
    const key = 'system:stats';
    return await this.get(key);
  }

  // 缓存用户统计信息
  async cacheUserStats(userId, stats, ttl = 1800) { // 30分钟
    const key = `user:stats:${userId}`;
    return await this.set(key, stats, ttl);
  }

  // 获取用户统计信息
  async getUserStats(userId) {
    const key = `user:stats:${userId}`;
    return await this.get(key);
  }

  // 实现分布式锁
  async acquireLock(lockKey, ttl = 30, retryTimes = 3) {
    if (!this.isReady()) {
      return false;
    }

    const lockValue = Date.now().toString();
    
    for (let i = 0; i < retryTimes; i++) {
      try {
        const result = await this.client.set(lockKey, lockValue, {
          NX: true, // 只在键不存在时设置
          EX: ttl   // 设置过期时间
        });
        
        if (result === 'OK') {
          return lockValue; // 返回锁值，用于释放锁时验证
        }
        
        // 等待一段时间后重试
        await new Promise(resolve => setTimeout(resolve, 100));
      } catch (error) {
        console.error('❌ 获取分布式锁失败:', error.message);
      }
    }
    
    return false;
  }

  // 释放分布式锁
  async releaseLock(lockKey, lockValue) {
    if (!this.isReady()) {
      return false;
    }

    try {
      // 使用Lua脚本确保原子性操作
      const luaScript = `
        if redis.call("get", KEYS[1]) == ARGV[1] then
          return redis.call("del", KEYS[1])
        else
          return 0
        end
      `;
      
      const result = await this.client.eval(luaScript, {
        keys: [lockKey],
        arguments: [lockValue]
      });
      
      return result === 1;
    } catch (error) {
      console.error('❌ 释放分布式锁失败:', error.message);
      return false;
    }
  }

  // 获取Redis信息
  async getInfo() {
    if (!this.isReady()) {
      return null;
    }

    try {
      const info = await this.client.info();
      return info;
    } catch (error) {
      console.error('❌ 获取Redis信息失败:', error.message);
      return null;
    }
  }

  // 健康检查
  async healthCheck() {
    try {
      if (!this.isReady()) {
        return {
          status: 'unhealthy',
          cache: 'redis',
          connected: false,
          error: 'Redis未连接',
          timestamp: new Date().toISOString()
        };
      }

      await this.client.ping();
      
      return {
        status: 'healthy',
        cache: 'redis',
        connected: true,
        timestamp: new Date().toISOString()
      };
    } catch (error) {
      return {
        status: 'unhealthy',
        cache: 'redis',
        connected: false,
        error: error.message,
        timestamp: new Date().toISOString()
      };
    }
  }

  // 关闭连接
  async close() {
    if (this.client) {
      await this.client.quit();
      this.isConnected = false;
      console.log('✅ Redis连接已关闭');
    }
  }
}

module.exports = RedisCache;
