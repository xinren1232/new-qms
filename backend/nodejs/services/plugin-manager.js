/**
 * 插件管理器 - 统一管理Coze Studio插件
 * 支持插件的加载、启用、禁用、配置等功能
 */

const path = require('path');
const fs = require('fs').promises;
const { EventEmitter } = require('events');
const { logger } = require('../middleware/error-handler');

// 导入插件
const FeishuPlugin = require('../plugins/feishu-plugin');
const MCPConnector = require('../plugins/mcp-connector');

class PluginManager extends EventEmitter {
  constructor(config = {}) {
    super();
    
    this.config = {
      pluginsDir: config.pluginsDir || path.join(__dirname, '../plugins'),
      autoLoad: config.autoLoad !== false,
      maxPlugins: config.maxPlugins || 50,
      ...config
    };
    
    this.plugins = new Map();
    this.pluginConfigs = new Map();
    this.initialized = false;
    
    // 预定义插件注册表
    this.pluginRegistry = {
      'feishu-integration': {
        class: FeishuPlugin,
        name: '飞书集成插件',
        category: 'collaboration',
        autoStart: true
      },
      'mcp-connector': {
        class: MCPConnector,
        name: 'MCP服务连接器',
        category: 'ai',
        autoStart: false
      }
    };
  }

  /**
   * 初始化插件管理器
   */
  async initialize() {
    try {
      logger.info('🔌 初始化插件管理器...');
      
      // 加载插件配置
      await this.loadPluginConfigs();
      
      // 自动加载插件
      if (this.config.autoLoad) {
        await this.autoLoadPlugins();
      }
      
      this.initialized = true;
      logger.info('✅ 插件管理器初始化成功');
      
      return {
        success: true,
        message: '插件管理器初始化成功',
        loadedPlugins: Array.from(this.plugins.keys())
      };
    } catch (error) {
      logger.error('❌ 插件管理器初始化失败:', error);
      throw error;
    }
  }

  /**
   * 加载插件配置
   */
  async loadPluginConfigs() {
    try {
      const configPath = path.join(this.config.pluginsDir, 'config.json');
      
      try {
        const configData = await fs.readFile(configPath, 'utf8');
        const configs = JSON.parse(configData);
        
        for (const [pluginId, config] of Object.entries(configs)) {
          this.pluginConfigs.set(pluginId, config);
        }
        
        logger.info(`📋 加载了 ${this.pluginConfigs.size} 个插件配置`);
      } catch (error) {
        if (error.code !== 'ENOENT') {
          logger.warn('⚠️ 加载插件配置失败，使用默认配置:', error.message);
        }
      }
    } catch (error) {
      logger.error('❌ 加载插件配置失败:', error);
    }
  }

  /**
   * 保存插件配置
   */
  async savePluginConfigs() {
    try {
      const configPath = path.join(this.config.pluginsDir, 'config.json');
      const configs = Object.fromEntries(this.pluginConfigs);
      
      await fs.writeFile(configPath, JSON.stringify(configs, null, 2));
      logger.info('💾 插件配置已保存');
    } catch (error) {
      logger.error('❌ 保存插件配置失败:', error);
    }
  }

  /**
   * 自动加载插件
   */
  async autoLoadPlugins() {
    const loadPromises = [];
    
    for (const [pluginId, pluginInfo] of Object.entries(this.pluginRegistry)) {
      if (pluginInfo.autoStart) {
        loadPromises.push(
          this.loadPlugin(pluginId).catch(error => {
            logger.error(`自动加载插件失败 ${pluginId}:`, error);
          })
        );
      }
    }
    
    await Promise.all(loadPromises);
  }

  /**
   * 加载插件
   * @param {string} pluginId - 插件ID
   * @param {Object} config - 插件配置
   */
  async loadPlugin(pluginId, config = {}) {
    try {
      if (this.plugins.has(pluginId)) {
        throw new Error(`插件 ${pluginId} 已经加载`);
      }

      if (this.plugins.size >= this.config.maxPlugins) {
        throw new Error(`已达到最大插件数限制: ${this.config.maxPlugins}`);
      }

      const pluginInfo = this.pluginRegistry[pluginId];
      if (!pluginInfo) {
        throw new Error(`未找到插件: ${pluginId}`);
      }

      logger.info(`🔌 加载插件: ${pluginInfo.name}`);

      // 合并配置
      const savedConfig = this.pluginConfigs.get(pluginId) || {};
      const finalConfig = { ...savedConfig, ...config };

      // 创建插件实例
      const PluginClass = pluginInfo.class;
      const plugin = new PluginClass(finalConfig);

      // 初始化插件
      const initResult = await plugin.initialize();
      if (!initResult.success) {
        throw new Error(`插件初始化失败: ${initResult.message}`);
      }

      // 注册插件
      this.plugins.set(pluginId, {
        id: pluginId,
        instance: plugin,
        info: pluginInfo,
        config: finalConfig,
        loadedAt: new Date(),
        status: 'loaded'
      });

      // 设置插件事件监听
      this.setupPluginEvents(pluginId, plugin);

      this.emit('plugin-loaded', { pluginId, plugin });
      logger.info(`✅ 插件加载成功: ${pluginInfo.name}`);

      return {
        success: true,
        pluginId,
        metadata: initResult.metadata
      };
    } catch (error) {
      logger.error(`❌ 加载插件失败 ${pluginId}:`, error);
      throw error;
    }
  }

  /**
   * 卸载插件
   * @param {string} pluginId - 插件ID
   */
  async unloadPlugin(pluginId) {
    try {
      const pluginData = this.plugins.get(pluginId);
      if (!pluginData) {
        throw new Error(`插件 ${pluginId} 未加载`);
      }

      logger.info(`🔌 卸载插件: ${pluginData.info.name}`);

      // 清理插件资源
      if (typeof pluginData.instance.cleanup === 'function') {
        await pluginData.instance.cleanup();
      }

      // 移除事件监听
      pluginData.instance.removeAllListeners();

      // 从管理器中移除
      this.plugins.delete(pluginId);

      this.emit('plugin-unloaded', { pluginId });
      logger.info(`✅ 插件卸载成功: ${pluginData.info.name}`);

      return {
        success: true,
        pluginId
      };
    } catch (error) {
      logger.error(`❌ 卸载插件失败 ${pluginId}:`, error);
      throw error;
    }
  }

  /**
   * 启用插件
   * @param {string} pluginId - 插件ID
   */
  async enablePlugin(pluginId) {
    try {
      const pluginData = this.plugins.get(pluginId);
      if (!pluginData) {
        throw new Error(`插件 ${pluginId} 未加载`);
      }

      if (pluginData.instance.enabled) {
        return { success: true, message: '插件已启用' };
      }

      pluginData.instance.enabled = true;
      pluginData.status = 'enabled';

      this.emit('plugin-enabled', { pluginId });
      logger.info(`✅ 插件已启用: ${pluginData.info.name}`);

      return {
        success: true,
        pluginId
      };
    } catch (error) {
      logger.error(`❌ 启用插件失败 ${pluginId}:`, error);
      throw error;
    }
  }

  /**
   * 禁用插件
   * @param {string} pluginId - 插件ID
   */
  async disablePlugin(pluginId) {
    try {
      const pluginData = this.plugins.get(pluginId);
      if (!pluginData) {
        throw new Error(`插件 ${pluginId} 未加载`);
      }

      pluginData.instance.enabled = false;
      pluginData.status = 'disabled';

      this.emit('plugin-disabled', { pluginId });
      logger.info(`⏸️ 插件已禁用: ${pluginData.info.name}`);

      return {
        success: true,
        pluginId
      };
    } catch (error) {
      logger.error(`❌ 禁用插件失败 ${pluginId}:`, error);
      throw error;
    }
  }

  /**
   * 获取插件实例
   * @param {string} pluginId - 插件ID
   */
  getPlugin(pluginId) {
    const pluginData = this.plugins.get(pluginId);
    return pluginData ? pluginData.instance : null;
  }

  /**
   * 获取所有插件信息
   */
  getAllPlugins() {
    const plugins = [];
    
    for (const [pluginId, pluginData] of this.plugins) {
      plugins.push({
        id: pluginId,
        name: pluginData.info.name,
        category: pluginData.info.category,
        status: pluginData.status,
        enabled: pluginData.instance.enabled,
        loadedAt: pluginData.loadedAt,
        metadata: pluginData.instance.metadata || {}
      });
    }
    
    return plugins;
  }

  /**
   * 获取可用插件列表
   */
  getAvailablePlugins() {
    const available = [];
    
    for (const [pluginId, pluginInfo] of Object.entries(this.pluginRegistry)) {
      available.push({
        id: pluginId,
        name: pluginInfo.name,
        category: pluginInfo.category,
        loaded: this.plugins.has(pluginId),
        autoStart: pluginInfo.autoStart
      });
    }
    
    return available;
  }

  /**
   * 调用插件方法
   * @param {string} pluginId - 插件ID
   * @param {string} method - 方法名
   * @param {...any} args - 参数
   */
  async callPlugin(pluginId, method, ...args) {
    try {
      const pluginData = this.plugins.get(pluginId);
      if (!pluginData) {
        throw new Error(`插件 ${pluginId} 未加载`);
      }

      if (!pluginData.instance.enabled) {
        throw new Error(`插件 ${pluginId} 未启用`);
      }

      const plugin = pluginData.instance;
      if (typeof plugin[method] !== 'function') {
        throw new Error(`插件 ${pluginId} 不支持方法: ${method}`);
      }

      return await plugin[method](...args);
    } catch (error) {
      logger.error(`❌ 调用插件方法失败 ${pluginId}.${method}:`, error);
      throw error;
    }
  }

  /**
   * 设置插件事件监听
   * @param {string} pluginId - 插件ID
   * @param {Object} plugin - 插件实例
   */
  setupPluginEvents(pluginId, plugin) {
    // 转发插件事件
    plugin.on('error', (error) => {
      this.emit('plugin-error', { pluginId, error });
    });

    plugin.on('warning', (warning) => {
      this.emit('plugin-warning', { pluginId, warning });
    });

    plugin.on('info', (info) => {
      this.emit('plugin-info', { pluginId, info });
    });
  }

  /**
   * 健康检查
   */
  async healthCheck() {
    try {
      if (!this.initialized) {
        return {
          status: 'not_initialized',
          message: '插件管理器未初始化'
        };
      }

      const pluginHealths = [];
      for (const [pluginId, pluginData] of this.plugins) {
        try {
          const health = await pluginData.instance.healthCheck();
          pluginHealths.push({
            pluginId,
            name: pluginData.info.name,
            ...health
          });
        } catch (error) {
          pluginHealths.push({
            pluginId,
            name: pluginData.info.name,
            status: 'error',
            message: error.message
          });
        }
      }

      const healthyPlugins = pluginHealths.filter(p => p.status === 'healthy').length;
      const totalPlugins = pluginHealths.length;

      return {
        status: totalPlugins === 0 ? 'no_plugins' : (healthyPlugins === totalPlugins ? 'healthy' : 'partial'),
        message: `${healthyPlugins}/${totalPlugins} 插件健康`,
        totalPlugins,
        healthyPlugins,
        plugins: pluginHealths
      };
    } catch (error) {
      return {
        status: 'error',
        message: error.message
      };
    }
  }

  /**
   * 清理资源
   */
  async cleanup() {
    logger.info('🧹 清理插件管理器资源...');
    
    const unloadPromises = [];
    for (const pluginId of this.plugins.keys()) {
      unloadPromises.push(
        this.unloadPlugin(pluginId).catch(error => {
          logger.error(`清理插件失败 ${pluginId}:`, error);
        })
      );
    }
    
    await Promise.all(unloadPromises);
    await this.savePluginConfigs();
    
    this.initialized = false;
    logger.info('✅ 插件管理器资源清理完成');
  }
}

module.exports = PluginManager;
