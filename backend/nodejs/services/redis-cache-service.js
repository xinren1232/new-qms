/**
 * QMS-AI Redis缓存服务
 * 提供高性能缓存和会话管理功能
 */

const redis = require('redis');
const EventEmitter = require('events');

class RedisCacheService extends EventEmitter {
  constructor(options = {}) {
    super();

    this.options = {
      host: options.host || process.env.REDIS_HOST || 'localhost',
      port: options.port || process.env.REDIS_PORT || 6379,
      password: options.password || process.env.REDIS_PASSWORD || undefined,
      database: options.database || process.env.REDIS_DB || 0,
      retryDelayOnFailover: options.retryDelayOnFailover || 100,
      maxRetriesPerRequest: options.maxRetriesPerRequest || 3,
      connectTimeout: options.connectTimeout || 10000,
      lazyConnect: true
    };

    this.client = null;
    this.connected = false;
    this.connecting = false;
    this.errorLogged = false;

    // 缓存策略配置
    this.cacheStrategies = {
      // AI模型配置缓存
      ai_models: {
        ttl: 1800, // 30分钟
        prefix: 'qms:ai:models:'
      },
      // 配置数据缓存
      config_data: {
        ttl: 3600, // 1小时
        prefix: 'qms:config:'
      },
      // 用户会话缓存
      user_session: {
        ttl: 86400, // 24小时
        prefix: 'qms:session:'
      },
      // 聊天历史缓存
      chat_history: {
        ttl: 7200, // 2小时
        prefix: 'qms:chat:'
      },
      // 知识库（轻量RAG M1）
      kb: {
        ttl: 604800, // 7天
        prefix: 'qms:kb:'
      },
      // 执行过程 Traces
      traces: {
        ttl: 86400, // 24小时
        prefix: 'qms:traces:'
      },
      // 系统状态缓存
      system_status: {
        ttl: 300, // 5分钟
        prefix: 'qms:status:'
      }
    };
  }

  /**
   * 连接Redis
   */
  async connect() {
    if (this.connected || this.connecting) {
      return this.client;
    }

    this.connecting = true;

    try {
      console.log('🔄 正在连接Redis服务器...');

      this.client = redis.createClient({
        socket: {
          host: this.options.host,
          port: this.options.port,
          connectTimeout: this.options.connectTimeout,
          reconnectStrategy: false // 禁用自动重连
        },
        password: this.options.password || undefined,
        database: this.options.database
      });

      // 错误处理 - 不再重复输出错误
      this.client.on('error', (error) => {
        if (!this.errorLogged) {
          console.error('❌ Redis连接错误:', error.message || error);
          this.errorLogged = true;
        }
        this.connected = false;
        this.connecting = false;
        this.emit('error', error);
      });

      // 连接成功
      this.client.on('connect', () => {
        console.log('🔗 Redis连接已建立');
      });

      // 准备就绪
      this.client.on('ready', () => {
        console.log('✅ Redis服务已就绪');
        this.connected = true;
        this.connecting = false;
        this.emit('ready');
      });

      // 断开连接
      this.client.on('end', () => {
        console.log('🔌 Redis连接已断开');
        this.connected = false;
        this.emit('disconnected');
      });

      await this.client.connect();

      // 测试连接
      await this.client.ping();
      console.log('✅ Redis连接测试成功');

      return this.client;
    } catch (error) {
      if (!this.errorLogged) {
        console.error('❌ Redis连接失败:', error.message || error);
        console.warn('⚠️ 将使用内存存储替代Redis缓存');
        this.errorLogged = true;
      }

      this.connected = false;
      this.connecting = false;

      // 不抛出异常，允许服务继续运行
      return null;
    }
  }

  /**
   * 检查连接状态
   */
  isConnected() {
    return this.connected && this.client && this.client.isReady;
  }

  /**
   * 获取缓存键名
   */
  getCacheKey(strategy, key) {
    const config = this.cacheStrategies[strategy];
    if (!config) {
      throw new Error(`未知的缓存策略: ${strategy}`);
    }
    return `${config.prefix}${key}`;
  }

  /**
   * 设置缓存
   */
  async set(strategy, key, value, customTTL = null) {
    if (!this.isConnected()) {
      console.warn('⚠️ Redis未连接，跳过缓存设置');
      return false;
    }

    try {
      const config = this.cacheStrategies[strategy];
      const cacheKey = this.getCacheKey(strategy, key);
      const ttl = customTTL || config.ttl;

      const serializedValue = JSON.stringify(value);

      if (ttl > 0) {
        await this.client.setEx(cacheKey, ttl, serializedValue);
      } else {
        await this.client.set(cacheKey, serializedValue);
      }

      console.log(`✅ 缓存已设置: ${strategy}:${key} (TTL: ${ttl}s)`);
      return true;
    } catch (error) {
      console.error(`❌ 设置缓存失败 ${strategy}:${key}:`, error.message);
      return false;
    }
  }

  /**
   * 获取缓存
   */
  async get(strategy, key) {
    if (!this.isConnected()) {
      console.warn('⚠️ Redis未连接，返回null');
      return null;
    }

    try {
      const cacheKey = this.getCacheKey(strategy, key);
      const value = await this.client.get(cacheKey);

      if (value === null) {
        return null;
      }

      const parsedValue = JSON.parse(value);
      console.log(`✅ 缓存命中: ${strategy}:${key}`);
      return parsedValue;
    } catch (error) {
      console.error(`❌ 获取缓存失败 ${strategy}:${key}:`, error.message);
      return null;
    }
  }

  /**
   * 删除缓存
   */
  async del(strategy, key) {
    if (!this.isConnected()) {
      console.warn('⚠️ Redis未连接，跳过缓存删除');
      return false;
    }

    try {
      const cacheKey = this.getCacheKey(strategy, key);
      const result = await this.client.del(cacheKey);

      console.log(`✅ 缓存已删除: ${strategy}:${key}`);
      return result > 0;
    } catch (error) {
      console.error(`❌ 删除缓存失败 ${strategy}:${key}:`, error.message);
      return false;
    }
  }

  /**
   * 检查缓存是否存在
   */
  async exists(strategy, key) {
    if (!this.isConnected()) {
      return false;
    }

    try {
      const cacheKey = this.getCacheKey(strategy, key);
      const result = await this.client.exists(cacheKey);
      return result === 1;
    } catch (error) {
      console.error(`❌ 检查缓存存在性失败 ${strategy}:${key}:`, error.message);
      return false;
    }
  }

  /**
   * 设置缓存过期时间
   */
  async expire(strategy, key, ttl) {
    if (!this.isConnected()) {
      return false;
    }

    try {
      const cacheKey = this.getCacheKey(strategy, key);
      const result = await this.client.expire(cacheKey, ttl);

      console.log(`✅ 缓存过期时间已设置: ${strategy}:${key} (TTL: ${ttl}s)`);
      return result === 1;
    } catch (error) {
      console.error(`❌ 设置缓存过期时间失败 ${strategy}:${key}:`, error.message);
      return false;
    }
  }

  /**
   * 获取缓存剩余过期时间
   */
  async ttl(strategy, key) {
    if (!this.isConnected()) {
      return -1;
    }

    try {
      const cacheKey = this.getCacheKey(strategy, key);
      return await this.client.ttl(cacheKey);
    } catch (error) {
      console.error(`❌ 获取缓存TTL失败 ${strategy}:${key}:`, error.message);
      return -1;
    }
  }

  /**
   * 清空指定策略的所有缓存
   */
  async clearStrategy(strategy) {
    if (!this.isConnected()) {
      return false;
    }

    try {
      const config = this.cacheStrategies[strategy];
      const pattern = `${config.prefix}*`;

      const keys = await this.client.keys(pattern);
      if (keys.length > 0) {
        await this.client.del(keys);
        console.log(`✅ 已清空缓存策略: ${strategy} (${keys.length}个键)`);
      }

      return true;
    } catch (error) {
      console.error(`❌ 清空缓存策略失败 ${strategy}:`, error.message);
      return false;
    }
  }

  /**
   * 获取缓存统计信息
   */
  async getStats() {
    if (!this.isConnected()) {
      return null;
    }

    try {
      const info = await this.client.info('memory');
      const stats = {
        connected: this.connected,
        memory_usage: this.parseRedisInfo(info, 'used_memory_human'),
        total_keys: 0,
        strategies: {}
      };

      // 统计各策略的键数量
      for (const [strategy, config] of Object.entries(this.cacheStrategies)) {
        const pattern = `${config.prefix}*`;
        const keys = await this.client.keys(pattern);
        stats.strategies[strategy] = {
          count: keys.length,
          ttl: config.ttl
        };
        stats.total_keys += keys.length;
      }

      return stats;
    } catch (error) {
      console.error('❌ 获取缓存统计失败:', error.message);
      return null;
    }
  }

  /**
   * 解析Redis INFO命令输出
   */
  parseRedisInfo(info, key) {
    const lines = info.split('\r\n');
    for (const line of lines) {
      if (line.startsWith(`${key}:`)) {
        return line.split(':')[1];
      }
    }
    return 'unknown';
  }

  /**
   * 关闭连接
   */
  async close() {
    if (this.client) {
      try {
        await this.client.quit();
        console.log('🔒 Redis连接已关闭');
      } catch (error) {
        console.error('❌ 关闭Redis连接失败:', error.message);
      }
    }

    this.connected = false;
    this.client = null;
  }

  /**
   * 健康检查
   */
  async healthCheck() {
    try {
      if (!this.isConnected()) {
        return {
          status: 'unhealthy',
          message: 'Redis未连接'
        };
      }

      const start = Date.now();
      await this.client.ping();
      const responseTime = Date.now() - start;

      const stats = await this.getStats();

      return {
        status: 'healthy',
        response_time: `${responseTime}ms`,
        memory_usage: stats?.memory_usage || 'unknown',
        total_keys: stats?.total_keys || 0,
        strategies: stats?.strategies || {}
      };
    } catch (error) {
      return {
        status: 'unhealthy',
        message: error.message
      };
    }
  }
}

module.exports = RedisCacheService;
