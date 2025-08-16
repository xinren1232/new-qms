/**
 * QMS-AI 轻量级配置中心
 * 基于文件+内存的配置管理方案，适合本地开发和小规模部署
 */

const fs = require('fs');
const path = require('path');
const EventEmitter = require('events');

class LightweightConfigCenter extends EventEmitter {
  constructor(options = {}) {
    super();
    
    this.configDir = options.configDir || path.join(__dirname, 'data');
    this.configs = new Map();
    this.watchers = new Map();
    this.initialized = false;
    
    // 确保配置目录存在
    this.ensureConfigDir();
    
    // 初始化配置
    this.init();
  }

  /**
   * 确保配置目录存在
   */
  ensureConfigDir() {
    if (!fs.existsSync(this.configDir)) {
      fs.mkdirSync(this.configDir, { recursive: true });
      console.log(`📁 配置目录已创建: ${this.configDir}`);
    }
  }

  /**
   * 初始化配置中心
   */
  async init() {
    try {
      // 加载所有配置文件
      await this.loadAllConfigs();
      
      // 设置文件监听
      this.setupFileWatchers();
      
      this.initialized = true;
      this.emit('initialized');
      
      console.log('🚀 轻量级配置中心初始化完成');
      console.log(`📍 配置目录: ${this.configDir}`);
      console.log(`📊 已加载配置: ${this.configs.size} 个`);
      
    } catch (error) {
      console.error('❌ 配置中心初始化失败:', error);
      throw error;
    }
  }

  /**
   * 加载所有配置文件
   */
  async loadAllConfigs() {
    const configFiles = fs.readdirSync(this.configDir)
      .filter(file => file.endsWith('.json'));
    
    for (const file of configFiles) {
      const configName = path.basename(file, '.json');
      await this.loadConfig(configName);
    }
    
    // 如果没有配置文件，创建默认配置
    if (configFiles.length === 0) {
      await this.createDefaultConfigs();
    }
  }

  /**
   * 创建默认配置
   */
  async createDefaultConfigs() {
    // AI模型配置
    const aiModelsConfig = {
      default_model: 'gpt-4o',
      models: {
        'gpt-4o': {
          name: 'GPT-4o',
          model: 'gpt-4o',
          apiKey: process.env.OPENAI_API_KEY || '',
          baseURL: 'https://api.openai.com/v1',
          maxTokens: 4096,
          temperature: 0.7,
          features: {
            reasoning: false,
            tools: true,
            vision: true
          },
          enabled: true
        },
        'o3': {
          name: 'O3',
          model: 'o3-mini',
          apiKey: process.env.OPENAI_API_KEY || '',
          baseURL: 'https://api.openai.com/v1',
          maxTokens: 4096,
          temperature: 0.7,
          features: {
            reasoning: true,
            tools: true,
            vision: false
          },
          enabled: true
        },
        'gemini-2.5-pro-thinking': {
          name: 'Gemini 2.5 Pro Thinking',
          model: 'gemini-2.5-pro-thinking',
          apiKey: process.env.GEMINI_API_KEY || '',
          baseURL: 'https://generativelanguage.googleapis.com/v1beta',
          maxTokens: 4096,
          temperature: 0.7,
          features: {
            reasoning: true,
            tools: true,
            vision: false
          },
          enabled: true
        },
        'claude-3.7-sonnet': {
          name: 'Claude 3.7 Sonnet',
          model: 'claude-3-5-sonnet-20241022',
          apiKey: process.env.ANTHROPIC_API_KEY || '',
          baseURL: 'https://api.anthropic.com',
          maxTokens: 4096,
          temperature: 0.7,
          features: {
            reasoning: false,
            tools: true,
            vision: false
          },
          enabled: true
        },
        'deepseek-r1': {
          name: 'DeepSeek R1',
          model: 'deepseek-reasoner',
          apiKey: process.env.DEEPSEEK_API_KEY || '',
          baseURL: 'https://api.deepseek.com/v1',
          maxTokens: 4096,
          temperature: 0.7,
          features: {
            reasoning: true,
            tools: false,
            vision: false
          },
          enabled: true
        },
        'deepseek-v3': {
          name: 'DeepSeek V3',
          model: 'deepseek-chat',
          apiKey: process.env.DEEPSEEK_API_KEY || '',
          baseURL: 'https://api.deepseek.com/v1',
          maxTokens: 4096,
          temperature: 0.7,
          features: {
            reasoning: false,
            tools: false,
            vision: false
          },
          enabled: true
        },
        'deepseek-chat-v3-0324': {
          name: 'DeepSeek Chat (V3-0324)',
          model: 'deepseek-chat',
          apiKey: process.env.DEEPSEEK_API_KEY || '',
          baseURL: 'https://api.deepseek.com/v1',
          maxTokens: 4096,
          temperature: 0.7,
          features: {
            reasoning: false,
            tools: true,
            vision: false,
            external: true
          },
          enabled: true
        },
        'deepseek-reasoner-r1-0528': {
          name: 'DeepSeek Reasoner (R1-0528)',
          model: 'deepseek-reasoner',
          apiKey: process.env.DEEPSEEK_API_KEY || '',
          baseURL: 'https://api.deepseek.com/v1',
          maxTokens: 4096,
          temperature: 0.7,
          features: {
            reasoning: true,
            tools: true,
            vision: false,
            external: true
          },
          enabled: true
        }
      },
      rate_limits: {
        requests_per_minute: 60,
        requests_per_hour: 1000
      },
      timeout: 30000,
      retry_attempts: 3
    };

    // 服务配置
    const servicesConfig = {
      chat_service: {
        port: 3004,
        host: 'localhost',
        health_check_interval: 30000,
        max_connections: 100
      },
      config_center: {
        port: 8081,
        host: 'localhost',
        health_check_interval: 30000
      },
      advanced_features: {
        port: 3009,
        host: 'localhost',
        health_check_interval: 30000
      }
    };

    // 系统配置
    const systemConfig = {
      environment: process.env.NODE_ENV || 'development',
      log_level: process.env.LOG_LEVEL || 'info',
      database: {
        type: 'sqlite',
        path: './database/chat_history.db'
      },
      cache: {
        enabled: false,
        ttl: 3600
      },
      security: {
        cors_enabled: true,
        rate_limit_enabled: true
      }
    };

    // 保存默认配置
    await this.setConfig('ai_models', aiModelsConfig);
    await this.setConfig('services', servicesConfig);
    await this.setConfig('system', systemConfig);

    console.log('📝 默认配置已创建');
  }

  /**
   * 设置文件监听
   */
  setupFileWatchers() {
    if (this.watchers.size > 0) {
      // 清理现有监听器
      this.watchers.forEach(watcher => watcher.close());
      this.watchers.clear();
    }

    const configFiles = fs.readdirSync(this.configDir)
      .filter(file => file.endsWith('.json'));

    configFiles.forEach(file => {
      const filePath = path.join(this.configDir, file);
      const configName = path.basename(file, '.json');
      
      const watcher = fs.watchFile(filePath, { interval: 1000 }, async () => {
        try {
          console.log(`📝 配置文件变更: ${file}`);
          await this.loadConfig(configName);
          this.emit('configChanged', configName, this.configs.get(configName));
        } catch (error) {
          console.error(`❌ 重新加载配置失败 ${file}:`, error);
        }
      });
      
      this.watchers.set(configName, watcher);
    });
  }

  /**
   * 加载单个配置
   */
  async loadConfig(configName) {
    const filePath = path.join(this.configDir, `${configName}.json`);

    if (!fs.existsSync(filePath)) {
      console.warn(`⚠️ 配置文件不存在: ${filePath}`);
      return null;
    }

    try {
      const content = fs.readFileSync(filePath, 'utf8');
      const config = JSON.parse(content);

      this.configs.set(configName, config);
      console.log(`✅ 配置已加载: ${configName}`);

      return config;
    } catch (error) {
      console.error(`❌ 加载配置失败 ${configName}:`, error);
      throw error;
    }
  }

  /**
   * 获取配置
   */
  getConfig(configName, key = null) {
    const config = this.configs.get(configName);
    
    if (!config) {
      return null;
    }
    
    if (key) {
      return this.getNestedValue(config, key);
    }
    
    return config;
  }

  /**
   * 设置配置
   */
  async setConfig(configName, config) {
    const filePath = path.join(this.configDir, `${configName}.json`);
    
    try {
      // 更新内存中的配置
      this.configs.set(configName, config);
      
      // 写入文件
      fs.writeFileSync(filePath, JSON.stringify(config, null, 2), 'utf8');
      
      console.log(`✅ 配置已保存: ${configName}`);
      
      // 触发配置变更事件
      this.emit('configChanged', configName, config);
      
      return true;
    } catch (error) {
      console.error(`❌ 保存配置失败 ${configName}:`, error);
      throw error;
    }
  }

  /**
   * 获取嵌套值
   */
  getNestedValue(obj, key) {
    return key.split('.').reduce((current, prop) => {
      return current && current[prop] !== undefined ? current[prop] : null;
    }, obj);
  }

  /**
   * 获取所有配置名称
   */
  getConfigNames() {
    return Array.from(this.configs.keys());
  }

  /**
   * 检查配置是否存在
   */
  hasConfig(configName) {
    return this.configs.has(configName);
  }

  /**
   * 删除配置
   */
  async deleteConfig(configName) {
    const filePath = path.join(this.configDir, `${configName}.json`);
    
    try {
      // 从内存中删除
      this.configs.delete(configName);
      
      // 删除文件
      if (fs.existsSync(filePath)) {
        fs.unlinkSync(filePath);
      }
      
      // 清理监听器
      if (this.watchers.has(configName)) {
        this.watchers.get(configName).close();
        this.watchers.delete(configName);
      }
      
      console.log(`🗑️ 配置已删除: ${configName}`);
      
      this.emit('configDeleted', configName);
      
      return true;
    } catch (error) {
      console.error(`❌ 删除配置失败 ${configName}:`, error);
      throw error;
    }
  }

  /**
   * 获取健康状态
   */
  getHealthStatus() {
    return {
      status: this.initialized ? 'healthy' : 'initializing',
      configs_loaded: this.configs.size,
      config_dir: this.configDir,
      watchers_active: this.watchers.size,
      uptime: process.uptime()
    };
  }

  /**
   * 关闭配置中心
   */
  close() {
    // 清理所有文件监听器
    this.watchers.forEach(watcher => watcher.close());
    this.watchers.clear();
    
    // 清理配置
    this.configs.clear();
    
    this.initialized = false;
    
    console.log('🔒 配置中心已关闭');
  }
}

module.exports = LightweightConfigCenter;
