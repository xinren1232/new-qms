/**
 * 配置中心集群管理器
 * 解决单点故障问题，提供高可用配置服务
 */

const EventEmitter = require('events');
const axios = require('axios');
const fs = require('fs').promises;
const path = require('path');

class ConfigCenterCluster extends EventEmitter {
  constructor(options = {}) {
    super();
    
    this.options = {
      nodes: options.nodes || [
        { id: 'primary', url: 'http://localhost:3003', priority: 1, role: 'master' },
        { id: 'secondary', url: 'http://localhost:3004', priority: 2, role: 'slave' },
        { id: 'tertiary', url: 'http://localhost:3005', priority: 3, role: 'slave' }
      ],
      healthCheckInterval: options.healthCheckInterval || 30000, // 30秒
      retryAttempts: options.retryAttempts || 3,
      retryDelay: options.retryDelay || 1000,
      localCacheFile: options.localCacheFile || path.join(__dirname, 'config-cache.json'),
      syncInterval: options.syncInterval || 60000, // 1分钟同步一次
      ...options
    };
    
    // 节点健康状态
    this.nodeHealth = new Map();
    this.options.nodes.forEach(node => {
      this.nodeHealth.set(node.id, { healthy: true, lastCheck: Date.now(), errors: 0 });
    });
    
    // 本地配置缓存
    this.localCache = new Map();
    this.configVersion = 0;
    
    // 当前主节点
    this.currentMaster = null;
    
    // 初始化
    this.initialize();
  }

  /**
   * 初始化集群
   */
  async initialize() {
    try {
      // 加载本地缓存
      await this.loadLocalCache();
      
      // 启动健康检查
      this.startHealthCheck();
      
      // 启动配置同步
      this.startConfigSync();
      
      // 选举主节点
      await this.electMaster();
      
      console.log('🔧 配置中心集群初始化完成');
      this.emit('cluster-ready');
    } catch (error) {
      console.error('配置中心集群初始化失败:', error);
      throw error;
    }
  }

  /**
   * 获取配置
   */
  async getConfig(key, options = {}) {
    const startTime = Date.now();
    
    try {
      // 1. 尝试从本地缓存获取
      if (this.localCache.has(key) && !options.forceRefresh) {
        const cached = this.localCache.get(key);
        if (cached.expiry > Date.now()) {
          console.log(`📋 配置缓存命中: ${key}`);
          return cached.value;
        }
      }

      // 2. 从健康的节点获取
      const healthyNodes = this.getHealthyNodes();
      if (healthyNodes.length === 0) {
        console.warn('⚠️ 所有配置节点不可用，使用本地缓存');
        return this.getFromLocalCache(key);
      }

      // 3. 按优先级尝试获取配置
      for (const node of healthyNodes) {
        try {
          const config = await this.fetchConfigFromNode(node, key);
          
          // 更新本地缓存
          this.updateLocalCache(key, config);
          
          console.log(`✅ 从节点 ${node.id} 获取配置: ${key} (${Date.now() - startTime}ms)`);
          return config;
        } catch (error) {
          console.warn(`⚠️ 节点 ${node.id} 获取配置失败: ${error.message}`);
          this.markNodeUnhealthy(node.id, error);
        }
      }

      // 4. 所有节点都失败，使用本地缓存
      console.error('❌ 所有节点获取配置失败，使用本地缓存');
      return this.getFromLocalCache(key);
      
    } catch (error) {
      console.error(`配置获取失败: ${key}`, error);
      return this.getFromLocalCache(key);
    }
  }

  /**
   * 设置配置
   */
  async setConfig(key, value, operator = 'system') {
    try {
      const configData = {
        key,
        value,
        operator,
        timestamp: new Date().toISOString(),
        version: ++this.configVersion
      };

      // 1. 更新主节点
      if (this.currentMaster) {
        await this.updateConfigOnNode(this.currentMaster, configData);
      }

      // 2. 同步到从节点
      const slaveNodes = this.getSlaveNodes();
      const syncPromises = slaveNodes.map(node => 
        this.syncConfigToNode(node, configData).catch(error => {
          console.warn(`同步配置到节点 ${node.id} 失败:`, error.message);
        })
      );
      
      await Promise.allSettled(syncPromises);

      // 3. 更新本地缓存
      this.updateLocalCache(key, value);

      // 4. 保存到本地文件
      await this.saveLocalCache();

      console.log(`✅ 配置更新成功: ${key}`);
      this.emit('config-updated', { key, value, operator });
      
      return { success: true, version: this.configVersion };
    } catch (error) {
      console.error(`配置更新失败: ${key}`, error);
      throw error;
    }
  }

  /**
   * 从节点获取配置
   */
  async fetchConfigFromNode(node, key) {
    const response = await axios.get(`${node.url}/api/config/${key}`, {
      timeout: 5000,
      headers: { 'X-Cluster-Node': node.id }
    });
    
    if (response.data.success) {
      return response.data.data;
    } else {
      throw new Error(response.data.error || '配置获取失败');
    }
  }

  /**
   * 更新节点配置
   */
  async updateConfigOnNode(node, configData) {
    const response = await axios.post(`${node.url}/api/config`, configData, {
      timeout: 10000,
      headers: { 'X-Cluster-Node': node.id }
    });
    
    if (!response.data.success) {
      throw new Error(response.data.error || '配置更新失败');
    }
    
    return response.data;
  }

  /**
   * 同步配置到节点
   */
  async syncConfigToNode(node, configData) {
    return this.updateConfigOnNode(node, configData);
  }

  /**
   * 健康检查
   */
  startHealthCheck() {
    setInterval(async () => {
      await this.performHealthCheck();
    }, this.options.healthCheckInterval);
    
    // 立即执行一次
    this.performHealthCheck();
  }

  /**
   * 执行健康检查
   */
  async performHealthCheck() {
    const checkPromises = this.options.nodes.map(async (node) => {
      try {
        const response = await axios.get(`${node.url}/health`, {
          timeout: 5000
        });
        
        if (response.status === 200) {
          this.markNodeHealthy(node.id);
        } else {
          this.markNodeUnhealthy(node.id, new Error(`健康检查失败: ${response.status}`));
        }
      } catch (error) {
        this.markNodeUnhealthy(node.id, error);
      }
    });
    
    await Promise.allSettled(checkPromises);
    
    // 检查是否需要重新选举主节点
    if (this.currentMaster && !this.isNodeHealthy(this.currentMaster.id)) {
      console.warn('🔄 主节点不健康，重新选举');
      await this.electMaster();
    }
  }

  /**
   * 标记节点健康
   */
  markNodeHealthy(nodeId) {
    const health = this.nodeHealth.get(nodeId);
    if (health && !health.healthy) {
      console.log(`✅ 节点 ${nodeId} 恢复健康`);
      this.emit('node-recovered', nodeId);
    }
    
    this.nodeHealth.set(nodeId, {
      healthy: true,
      lastCheck: Date.now(),
      errors: 0
    });
  }

  /**
   * 标记节点不健康
   */
  markNodeUnhealthy(nodeId, error) {
    const health = this.nodeHealth.get(nodeId) || { errors: 0 };
    health.healthy = false;
    health.lastCheck = Date.now();
    health.errors++;
    health.lastError = error.message;
    
    this.nodeHealth.set(nodeId, health);
    
    console.warn(`⚠️ 节点 ${nodeId} 不健康: ${error.message}`);
    this.emit('node-unhealthy', { nodeId, error: error.message });
  }

  /**
   * 获取健康的节点
   */
  getHealthyNodes() {
    return this.options.nodes
      .filter(node => this.isNodeHealthy(node.id))
      .sort((a, b) => a.priority - b.priority);
  }

  /**
   * 获取从节点
   */
  getSlaveNodes() {
    return this.options.nodes.filter(node => node.role === 'slave');
  }

  /**
   * 检查节点是否健康
   */
  isNodeHealthy(nodeId) {
    const health = this.nodeHealth.get(nodeId);
    return health && health.healthy;
  }

  /**
   * 选举主节点
   */
  async electMaster() {
    const healthyNodes = this.getHealthyNodes();
    
    if (healthyNodes.length === 0) {
      console.error('❌ 没有健康的节点可用');
      this.currentMaster = null;
      return;
    }
    
    // 选择优先级最高的健康节点作为主节点
    const newMaster = healthyNodes[0];
    
    if (!this.currentMaster || this.currentMaster.id !== newMaster.id) {
      this.currentMaster = newMaster;
      console.log(`🎯 选举新的主节点: ${newMaster.id}`);
      this.emit('master-elected', newMaster);
    }
  }

  /**
   * 启动配置同步
   */
  startConfigSync() {
    setInterval(async () => {
      await this.syncConfigurations();
    }, this.options.syncInterval);
  }

  /**
   * 同步配置
   */
  async syncConfigurations() {
    if (!this.currentMaster) return;
    
    try {
      // 从主节点获取所有配置
      const response = await axios.get(`${this.currentMaster.url}/api/config/all`, {
        timeout: 10000
      });
      
      if (response.data.success) {
        const configs = response.data.data;
        
        // 更新本地缓存
        for (const [key, value] of Object.entries(configs)) {
          this.updateLocalCache(key, value);
        }
        
        // 保存到本地文件
        await this.saveLocalCache();
        
        console.log('🔄 配置同步完成');
      }
    } catch (error) {
      console.warn('配置同步失败:', error.message);
    }
  }

  /**
   * 更新本地缓存
   */
  updateLocalCache(key, value) {
    this.localCache.set(key, {
      value,
      expiry: Date.now() + 3600000, // 1小时过期
      timestamp: Date.now()
    });
  }

  /**
   * 从本地缓存获取
   */
  getFromLocalCache(key) {
    const cached = this.localCache.get(key);
    if (cached) {
      console.log(`📋 使用本地缓存: ${key}`);
      return cached.value;
    }
    
    console.warn(`⚠️ 本地缓存中没有找到配置: ${key}`);
    return null;
  }

  /**
   * 加载本地缓存
   */
  async loadLocalCache() {
    try {
      const data = await fs.readFile(this.options.localCacheFile, 'utf8');
      const cache = JSON.parse(data);
      
      for (const [key, item] of Object.entries(cache)) {
        this.localCache.set(key, item);
      }
      
      console.log('📋 本地配置缓存加载完成');
    } catch (error) {
      console.log('📋 本地配置缓存文件不存在，将创建新的');
    }
  }

  /**
   * 保存本地缓存
   */
  async saveLocalCache() {
    try {
      const cache = {};
      for (const [key, value] of this.localCache.entries()) {
        cache[key] = value;
      }
      
      await fs.writeFile(this.options.localCacheFile, JSON.stringify(cache, null, 2));
    } catch (error) {
      console.error('保存本地缓存失败:', error);
    }
  }

  /**
   * 获取集群状态
   */
  getClusterStatus() {
    const nodes = this.options.nodes.map(node => ({
      id: node.id,
      url: node.url,
      role: node.role,
      priority: node.priority,
      healthy: this.isNodeHealthy(node.id),
      health: this.nodeHealth.get(node.id)
    }));
    
    return {
      currentMaster: this.currentMaster,
      nodes,
      healthyCount: nodes.filter(n => n.healthy).length,
      totalCount: nodes.length,
      localCacheSize: this.localCache.size,
      configVersion: this.configVersion
    };
  }
}

module.exports = ConfigCenterCluster;
