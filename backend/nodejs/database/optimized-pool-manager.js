/**
 * 优化的数据库连接池管理器
 * 支持读写分离、连接池监控、智能路由
 */

const { Pool } = require('pg');
const EventEmitter = require('events');
const logger = require('../utils/logger');

class OptimizedPoolManager extends EventEmitter {
  constructor(config = {}) {
    super();
    
    this.config = {
      // 读库配置
      read: {
        host: process.env.DB_READ_HOST || process.env.DB_HOST || 'localhost',
        port: process.env.DB_PORT || 5432,
        database: process.env.DB_NAME || 'qms_config',
        user: process.env.DB_USER || 'qms_app',
        password: process.env.DB_PASSWORD || 'qms123',
        max: 15,              // 读库连接数更多
        min: 5,               // 最小保持连接
        idleTimeoutMillis: 30000,
        connectionTimeoutMillis: 2000,
        acquireTimeoutMillis: 60000,
        ...config.read
      },
      
      // 写库配置
      write: {
        host: process.env.DB_WRITE_HOST || process.env.DB_HOST || 'localhost',
        port: process.env.DB_PORT || 5432,
        database: process.env.DB_NAME || 'qms_config',
        user: process.env.DB_USER || 'qms_app',
        password: process.env.DB_PASSWORD || 'qms123',
        max: 10,              // 写库连接数适中
        min: 3,
        idleTimeoutMillis: 30000,
        connectionTimeoutMillis: 2000,
        acquireTimeoutMillis: 60000,
        ...config.write
      }
    };
    
    this.pools = {};
    this.metrics = {
      totalQueries: 0,
      readQueries: 0,
      writeQueries: 0,
      avgResponseTime: 0,
      errorCount: 0,
      connectionErrors: 0,
      lastError: null
    };
    
    this.healthStatus = {
      read: true,
      write: true,
      lastCheck: Date.now()
    };
    
    this.initialize();
  }

  /**
   * 初始化连接池
   */
  async initialize() {
    try {
      // 创建读库连接池
      this.pools.read = new Pool(this.config.read);
      
      // 创建写库连接池
      this.pools.write = new Pool(this.config.write);
      
      // 设置连接池事件监听
      this.setupPoolEvents();
      
      // 启动健康检查
      this.startHealthCheck();
      
      // 启动指标收集
      this.startMetricsCollection();
      
      logger.info('🗄️ 数据库连接池初始化完成', {
        readPool: this.config.read,
        writePool: this.config.write
      });
      
      this.emit('initialized');
    } catch (error) {
      logger.error('数据库连接池初始化失败:', error);
      throw error;
    }
  }

  /**
   * 设置连接池事件监听
   */
  setupPoolEvents() {
    // 读库事件
    this.pools.read.on('connect', (client) => {
      logger.debug('读库连接建立');
    });
    
    this.pools.read.on('error', (err, client) => {
      logger.error('读库连接错误:', err);
      this.metrics.connectionErrors++;
      this.healthStatus.read = false;
      this.emit('pool-error', { type: 'read', error: err });
    });
    
    // 写库事件
    this.pools.write.on('connect', (client) => {
      logger.debug('写库连接建立');
    });
    
    this.pools.write.on('error', (err, client) => {
      logger.error('写库连接错误:', err);
      this.metrics.connectionErrors++;
      this.healthStatus.write = false;
      this.emit('pool-error', { type: 'write', error: err });
    });
  }

  /**
   * 智能查询路由：读写分离
   */
  async query(sql, params = [], options = {}) {
    const startTime = Date.now();
    const isWrite = this.isWriteOperation(sql) || options.forceWrite;
    const poolType = isWrite ? 'write' : 'read';
    const pool = this.pools[poolType];
    
    // 检查连接池健康状态
    if (!this.healthStatus[poolType]) {
      // 如果写库不健康，抛出错误
      if (isWrite) {
        throw new Error(`写库连接池不健康`);
      }
      // 如果读库不健康，尝试使用写库
      if (!isWrite && this.healthStatus.write) {
        logger.warn('读库不健康，使用写库执行查询');
        return this.query(sql, params, { ...options, forceWrite: true });
      }
      throw new Error('所有数据库连接池都不健康');
    }
    
    try {
      const result = await pool.query(sql, params);
      
      // 记录成功指标
      this.recordMetrics(isWrite, Date.now() - startTime, true);
      
      logger.debug(`${poolType}库查询成功`, {
        sql: sql.substring(0, 100),
        params: params.length,
        rows: result.rows?.length || 0,
        duration: Date.now() - startTime
      });
      
      return result;
    } catch (error) {
      // 记录失败指标
      this.recordMetrics(isWrite, Date.now() - startTime, false);
      this.metrics.lastError = {
        message: error.message,
        sql: sql.substring(0, 100),
        timestamp: new Date().toISOString()
      };
      
      logger.error(`${poolType}库查询失败:`, {
        error: error.message,
        sql: sql.substring(0, 100),
        params: params.length
      });
      
      throw error;
    }
  }

  /**
   * 判断是否为写操作
   */
  isWriteOperation(sql) {
    const writeKeywords = [
      'INSERT', 'UPDATE', 'DELETE', 'CREATE', 'DROP', 'ALTER',
      'TRUNCATE', 'REPLACE', 'MERGE', 'UPSERT'
    ];
    const upperSql = sql.trim().toUpperCase();
    return writeKeywords.some(keyword => upperSql.startsWith(keyword));
  }

  /**
   * 记录性能指标
   */
  recordMetrics(isWrite, responseTime, success) {
    this.metrics.totalQueries++;
    
    if (isWrite) {
      this.metrics.writeQueries++;
    } else {
      this.metrics.readQueries++;
    }
    
    // 计算平均响应时间
    this.metrics.avgResponseTime = 
      (this.metrics.avgResponseTime * (this.metrics.totalQueries - 1) + responseTime) / 
      this.metrics.totalQueries;
    
    if (!success) {
      this.metrics.errorCount++;
    }
    
    // 发送指标事件
    this.emit('metrics-updated', {
      type: isWrite ? 'write' : 'read',
      responseTime,
      success,
      totalQueries: this.metrics.totalQueries
    });
  }

  /**
   * 启动健康检查
   */
  startHealthCheck() {
    const checkInterval = 30000; // 30秒检查一次
    
    setInterval(async () => {
      await this.performHealthCheck();
    }, checkInterval);
    
    // 立即执行一次健康检查
    this.performHealthCheck();
  }

  /**
   * 执行健康检查
   */
  async performHealthCheck() {
    const checkQuery = 'SELECT 1 as health_check';
    
    // 检查读库
    try {
      await this.pools.read.query(checkQuery);
      if (!this.healthStatus.read) {
        logger.info('读库连接恢复正常');
        this.healthStatus.read = true;
        this.emit('pool-recovered', { type: 'read' });
      }
    } catch (error) {
      if (this.healthStatus.read) {
        logger.error('读库健康检查失败:', error.message);
        this.healthStatus.read = false;
        this.emit('pool-unhealthy', { type: 'read', error });
      }
    }
    
    // 检查写库
    try {
      await this.pools.write.query(checkQuery);
      if (!this.healthStatus.write) {
        logger.info('写库连接恢复正常');
        this.healthStatus.write = true;
        this.emit('pool-recovered', { type: 'write' });
      }
    } catch (error) {
      if (this.healthStatus.write) {
        logger.error('写库健康检查失败:', error.message);
        this.healthStatus.write = false;
        this.emit('pool-unhealthy', { type: 'write', error });
      }
    }
    
    this.healthStatus.lastCheck = Date.now();
  }

  /**
   * 启动指标收集
   */
  startMetricsCollection() {
    const metricsInterval = 60000; // 1分钟收集一次
    
    setInterval(() => {
      this.collectPoolMetrics();
    }, metricsInterval);
  }

  /**
   * 收集连接池指标
   */
  collectPoolMetrics() {
    const readPoolStats = {
      total: this.pools.read.totalCount,
      idle: this.pools.read.idleCount,
      waiting: this.pools.read.waitingCount
    };
    
    const writePoolStats = {
      total: this.pools.write.totalCount,
      idle: this.pools.write.idleCount,
      waiting: this.pools.write.waitingCount
    };
    
    this.emit('pool-metrics', {
      read: readPoolStats,
      write: writePoolStats,
      queries: this.metrics,
      health: this.healthStatus,
      timestamp: new Date().toISOString()
    });
    
    logger.debug('连接池指标收集', {
      read: readPoolStats,
      write: writePoolStats,
      queries: {
        total: this.metrics.totalQueries,
        read: this.metrics.readQueries,
        write: this.metrics.writeQueries,
        avgTime: Math.round(this.metrics.avgResponseTime),
        errors: this.metrics.errorCount
      }
    });
  }

  /**
   * 获取连接池状态
   */
  getPoolStatus() {
    return {
      read: {
        total: this.pools.read.totalCount,
        idle: this.pools.read.idleCount,
        waiting: this.pools.read.waitingCount,
        healthy: this.healthStatus.read
      },
      write: {
        total: this.pools.write.totalCount,
        idle: this.pools.write.idleCount,
        waiting: this.pools.write.waitingCount,
        healthy: this.healthStatus.write
      },
      metrics: this.metrics,
      config: {
        read: { max: this.config.read.max, min: this.config.read.min },
        write: { max: this.config.write.max, min: this.config.write.min }
      }
    };
  }

  /**
   * 获取性能统计
   */
  getPerformanceStats() {
    const readRatio = this.metrics.totalQueries > 0 ? 
      (this.metrics.readQueries / this.metrics.totalQueries * 100).toFixed(1) : 0;
    const writeRatio = this.metrics.totalQueries > 0 ? 
      (this.metrics.writeQueries / this.metrics.totalQueries * 100).toFixed(1) : 0;
    const errorRate = this.metrics.totalQueries > 0 ? 
      (this.metrics.errorCount / this.metrics.totalQueries * 100).toFixed(2) : 0;
    
    return {
      totalQueries: this.metrics.totalQueries,
      readWriteRatio: `${readRatio}% / ${writeRatio}%`,
      avgResponseTime: Math.round(this.metrics.avgResponseTime),
      errorRate: `${errorRate}%`,
      connectionErrors: this.metrics.connectionErrors,
      lastError: this.metrics.lastError,
      uptime: Date.now() - this.healthStatus.lastCheck
    };
  }

  /**
   * 事务支持
   */
  async transaction(callback, useWritePool = true) {
    const pool = useWritePool ? this.pools.write : this.pools.read;
    const client = await pool.connect();
    
    try {
      await client.query('BEGIN');
      const result = await callback(client);
      await client.query('COMMIT');
      return result;
    } catch (error) {
      await client.query('ROLLBACK');
      throw error;
    } finally {
      client.release();
    }
  }

  /**
   * 批量操作
   */
  async batchQuery(queries, useWritePool = true) {
    const pool = useWritePool ? this.pools.write : this.pools.read;
    const client = await pool.connect();
    
    try {
      const results = [];
      for (const { sql, params } of queries) {
        const result = await client.query(sql, params);
        results.push(result);
      }
      return results;
    } finally {
      client.release();
    }
  }

  /**
   * 关闭连接池
   */
  async close() {
    logger.info('正在关闭数据库连接池...');
    
    try {
      await Promise.all([
        this.pools.read.end(),
        this.pools.write.end()
      ]);
      
      logger.info('数据库连接池已关闭');
      this.emit('closed');
    } catch (error) {
      logger.error('关闭数据库连接池失败:', error);
      throw error;
    }
  }
}

module.exports = OptimizedPoolManager;
