/**
 * 多级缓存管理器
 * L1: 内存缓存 (最快，容量小)
 * L2: Redis缓存 (快，容量大)
 * L3: 数据库缓存 (慢，持久化)
 */

const LRU = require('lru-cache');
const Redis = require('ioredis');
const EventEmitter = require('events');
const logger = require('../utils/logger');

class MultiLevelCache extends EventEmitter {
  constructor(options = {}) {
    super();
    
    this.options = {
      // L1缓存配置
      l1: {
        max: options.l1?.max || 1000,
        ttl: options.l1?.ttl || 60000, // 1分钟
        updateAgeOnGet: true,
        ...options.l1
      },
      
      // L2缓存配置
      l2: {
        host: process.env.REDIS_MASTER_HOST || 'redis-master',
        port: process.env.REDIS_MASTER_PORT || 6379,
        password: process.env.REDIS_PASSWORD,
        db: options.l2?.db || 0,
        keyPrefix: options.l2?.keyPrefix || 'qms:',
        retryDelayOnFailover: 100,
        maxRetriesPerRequest: 3,
        lazyConnect: true,
        ...options.l2
      },
      
      // L3缓存配置
      l3: {
        enabled: options.l3?.enabled !== false,
        ttl: options.l3?.ttl || 86400, // 24小时
        ...options.l3
      }
    };
    
    // 初始化各级缓存
    this.l1Cache = new LRU(this.options.l1);
    this.l2Cache = null;
    this.l3Cache = null;
    
    // 性能指标
    this.metrics = {
      l1Hits: 0,
      l2Hits: 0,
      l3Hits: 0,
      misses: 0,
      sets: 0,
      errors: 0,
      totalRequests: 0
    };
    
    // 锁管理（防止缓存击穿）
    this.locks = new Map();
    
    this.initialize();
  }

  /**
   * 初始化缓存系统
   */
  async initialize() {
    try {
      // 初始化Redis缓存
      await this.initializeL2Cache();
      
      // 初始化数据库缓存
      if (this.options.l3.enabled) {
        await this.initializeL3Cache();
      }
      
      // 启动指标收集
      this.startMetricsCollection();
      
      logger.info('🔄 多级缓存系统初始化完成', {
        l1: { max: this.options.l1.max, ttl: this.options.l1.ttl },
        l2: { host: this.options.l2.host, port: this.options.l2.port },
        l3: { enabled: this.options.l3.enabled }
      });
      
      this.emit('initialized');
    } catch (error) {
      logger.error('多级缓存系统初始化失败:', error);
      throw error;
    }
  }

  /**
   * 初始化L2缓存 (Redis)
   */
  async initializeL2Cache() {
    this.l2Cache = new Redis(this.options.l2);
    
    this.l2Cache.on('connect', () => {
      logger.info('Redis缓存连接成功');
    });
    
    this.l2Cache.on('error', (error) => {
      logger.error('Redis缓存连接错误:', error);
      this.metrics.errors++;
      this.emit('l2-error', error);
    });
    
    this.l2Cache.on('ready', () => {
      logger.info('Redis缓存就绪');
      this.emit('l2-ready');
    });
    
    // 测试连接
    await this.l2Cache.ping();
  }

  /**
   * 初始化L3缓存 (数据库)
   */
  async initializeL3Cache() {
    // 这里可以集成数据库缓存表
    // 暂时使用简单的Map模拟
    this.l3Cache = new Map();
    logger.info('L3数据库缓存初始化完成');
  }

  /**
   * 获取缓存值
   */
  async get(key) {
    this.metrics.totalRequests++;
    
    try {
      // L1: 内存缓存
      const l1Value = this.l1Cache.get(key);
      if (l1Value !== undefined) {
        this.metrics.l1Hits++;
        logger.debug(`L1缓存命中: ${key}`);
        return l1Value;
      }

      // L2: Redis缓存
      if (this.l2Cache) {
        const l2Value = await this.l2Cache.get(key);
        if (l2Value !== null) {
          this.metrics.l2Hits++;
          logger.debug(`L2缓存命中: ${key}`);
          
          // 回填L1缓存
          const parsedValue = this.parseValue(l2Value);
          this.l1Cache.set(key, parsedValue);
          return parsedValue;
        }
      }

      // L3: 数据库缓存
      if (this.l3Cache && this.l3Cache.has(key)) {
        const l3Value = this.l3Cache.get(key);
        this.metrics.l3Hits++;
        logger.debug(`L3缓存命中: ${key}`);
        
        // 回填L2和L1缓存
        if (this.l2Cache) {
          await this.l2Cache.setex(key, 3600, this.stringifyValue(l3Value));
        }
        this.l1Cache.set(key, l3Value);
        return l3Value;
      }

      this.metrics.misses++;
      return null;
    } catch (error) {
      this.metrics.errors++;
      logger.error(`缓存获取失败: ${key}`, error);
      return null;
    }
  }

  /**
   * 设置缓存值
   */
  async set(key, value, ttl = 3600) {
    this.metrics.sets++;
    
    try {
      // 同时更新所有缓存层
      this.l1Cache.set(key, value);
      
      if (this.l2Cache) {
        await this.l2Cache.setex(key, ttl, this.stringifyValue(value));
      }
      
      if (this.l3Cache) {
        this.l3Cache.set(key, value);
      }
      
      logger.debug(`缓存设置成功: ${key}`);
      return true;
    } catch (error) {
      this.metrics.errors++;
      logger.error(`缓存设置失败: ${key}`, error);
      return false;
    }
  }

  /**
   * 获取或设置缓存（防止缓存击穿）
   */
  async getOrSet(key, fetcher, ttl = 3600) {
    // 尝试获取缓存
    let value = await this.get(key);
    if (value !== null) {
      return value;
    }

    // 防止缓存击穿
    const lockKey = `lock:${key}`;
    if (this.locks.has(lockKey)) {
      // 等待其他请求完成
      await this.waitForLock(lockKey);
      return await this.get(key);
    }

    // 获取锁
    this.locks.set(lockKey, true);

    try {
      // 双重检查
      value = await this.get(key);
      if (value !== null) {
        return value;
      }

      // 获取数据
      value = await fetcher();

      // 防止缓存穿透：即使是null也缓存
      const cacheValue = value !== null ? value : '__NULL__';
      const cacheTtl = value !== null ? ttl : 60; // null值短期缓存

      await this.set(key, cacheValue, cacheTtl);

      return value;
    } finally {
      this.locks.delete(lockKey);
    }
  }

  /**
   * 删除缓存
   */
  async delete(key) {
    try {
      // 从所有缓存层删除
      this.l1Cache.delete(key);
      
      if (this.l2Cache) {
        await this.l2Cache.del(key);
      }
      
      if (this.l3Cache) {
        this.l3Cache.delete(key);
      }
      
      logger.debug(`缓存删除成功: ${key}`);
      return true;
    } catch (error) {
      this.metrics.errors++;
      logger.error(`缓存删除失败: ${key}`, error);
      return false;
    }
  }

  /**
   * 批量删除缓存（支持模式匹配）
   */
  async deletePattern(pattern) {
    try {
      let deletedCount = 0;
      
      // L1缓存：遍历删除匹配的key
      for (const key of this.l1Cache.keys()) {
        if (this.matchPattern(key, pattern)) {
          this.l1Cache.delete(key);
          deletedCount++;
        }
      }
      
      // L2缓存：使用Redis的SCAN命令
      if (this.l2Cache) {
        const stream = this.l2Cache.scanStream({
          match: pattern,
          count: 100
        });
        
        stream.on('data', async (keys) => {
          if (keys.length > 0) {
            await this.l2Cache.del(...keys);
            deletedCount += keys.length;
          }
        });
        
        await new Promise((resolve) => {
          stream.on('end', resolve);
        });
      }
      
      // L3缓存：遍历删除
      if (this.l3Cache) {
        for (const key of this.l3Cache.keys()) {
          if (this.matchPattern(key, pattern)) {
            this.l3Cache.delete(key);
            deletedCount++;
          }
        }
      }
      
      logger.info(`批量删除缓存完成: ${pattern}, 删除了 ${deletedCount} 个key`);
      return deletedCount;
    } catch (error) {
      this.metrics.errors++;
      logger.error(`批量删除缓存失败: ${pattern}`, error);
      return 0;
    }
  }

  /**
   * 智能预热
   */
  async warmup(keys) {
    logger.info(`开始缓存预热: ${keys.length} 个key`);
    
    const warmupPromises = keys.map(async (key) => {
      try {
        if (this.l3Cache && this.l3Cache.has(key)) {
          const value = this.l3Cache.get(key);
          if (this.l2Cache) {
            await this.l2Cache.setex(key, 3600, this.stringifyValue(value));
          }
          this.l1Cache.set(key, value);
        }
      } catch (error) {
        logger.warn(`预热失败: ${key}`, error);
      }
    });
    
    await Promise.all(warmupPromises);
    logger.info('缓存预热完成');
  }

  /**
   * 获取缓存命中率统计
   */
  getHitRate() {
    const total = this.metrics.l1Hits + this.metrics.l2Hits + 
                  this.metrics.l3Hits + this.metrics.misses;
    
    if (total === 0) return { overall: 0, l1: 0, l2: 0, l3: 0 };
    
    return {
      overall: ((this.metrics.l1Hits + this.metrics.l2Hits + this.metrics.l3Hits) / total * 100).toFixed(2),
      l1: (this.metrics.l1Hits / total * 100).toFixed(2),
      l2: (this.metrics.l2Hits / total * 100).toFixed(2),
      l3: (this.metrics.l3Hits / total * 100).toFixed(2),
      miss: (this.metrics.misses / total * 100).toFixed(2)
    };
  }

  /**
   * 获取缓存统计信息
   */
  getStats() {
    return {
      metrics: this.metrics,
      hitRate: this.getHitRate(),
      l1Size: this.l1Cache.size,
      l1Max: this.l1Cache.max,
      l2Connected: this.l2Cache?.status === 'ready',
      l3Enabled: this.options.l3.enabled,
      l3Size: this.l3Cache?.size || 0
    };
  }

  /**
   * 等待锁释放
   */
  async waitForLock(lockKey) {
    while (this.locks.has(lockKey)) {
      await new Promise(resolve => setTimeout(resolve, 10));
    }
  }

  /**
   * 模式匹配
   */
  matchPattern(key, pattern) {
    // 简单的通配符匹配
    const regex = new RegExp(pattern.replace(/\*/g, '.*'));
    return regex.test(key);
  }

  /**
   * 值序列化
   */
  stringifyValue(value) {
    try {
      return JSON.stringify(value);
    } catch (error) {
      return String(value);
    }
  }

  /**
   * 值反序列化
   */
  parseValue(value) {
    try {
      return JSON.parse(value);
    } catch (error) {
      return value;
    }
  }

  /**
   * 启动指标收集
   */
  startMetricsCollection() {
    const metricsInterval = 60000; // 1分钟收集一次
    
    setInterval(() => {
      const stats = this.getStats();
      this.emit('metrics-collected', stats);
      
      logger.debug('缓存指标收集', {
        hitRate: stats.hitRate,
        requests: stats.metrics.totalRequests,
        errors: stats.metrics.errors
      });
    }, metricsInterval);
  }

  /**
   * 关闭缓存系统
   */
  async close() {
    logger.info('正在关闭多级缓存系统...');
    
    try {
      if (this.l2Cache) {
        await this.l2Cache.quit();
      }
      
      this.l1Cache.clear();
      
      if (this.l3Cache) {
        this.l3Cache.clear();
      }
      
      logger.info('多级缓存系统已关闭');
      this.emit('closed');
    } catch (error) {
      logger.error('关闭多级缓存系统失败:', error);
      throw error;
    }
  }
}

module.exports = MultiLevelCache;
