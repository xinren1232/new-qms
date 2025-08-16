/**
 * QMS-AI 轻量级配置中心服务
 * 提供HTTP API接口，支持配置的CRUD操作和实时通知
 */

const express = require('express');
const cors = require('cors');
const LightweightConfigCenter = require('./config/lightweight-config-center');
const RedisCacheService = require('./services/redis-cache-service');
const EnhancedMemoryCache = require('./services/enhanced-memory-cache');

const app = express();
const PORT = process.env.CONFIG_SERVICE_PORT || 3003;

// 中间件
app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// 初始化配置中心
const configCenter = new LightweightConfigCenter({
  configDir: process.env.CONFIG_DIR || './config/data'
});

// 智能缓存服务选择
let cacheService;
const cacheType = process.env.CACHE_TYPE || 'auto'; // auto, redis, memory
const cacheEnabled = process.env.CACHE_ENABLED !== 'false';

async function initializeCacheService() {
  if (!cacheEnabled) {
    console.log('📝 缓存已禁用，使用内存存储');
    return null;
  }

  if (cacheType === 'memory') {
    console.log('🧠 使用增强内存缓存');
    cacheService = new EnhancedMemoryCache({
      persistFile: './cache/config-center-cache.json',
      maxMemory: 50 * 1024 * 1024 // 50MB
    });
    return cacheService;
  }

  if (cacheType === 'redis' || cacheType === 'auto') {
    try {
      console.log('🔄 尝试连接Redis...');
      cacheService = new RedisCacheService({
        host: process.env.REDIS_HOST || 'localhost',
        port: process.env.REDIS_PORT || 6379,
        password: process.env.REDIS_PASSWORD || undefined,
        database: process.env.REDIS_DB || 0
      });

      await cacheService.connect();
      console.log('✅ Redis缓存服务已连接');
      return cacheService;
    } catch (error) {
      console.warn('⚠️ Redis连接失败:', error.message);

      if (cacheType === 'redis') {
        throw error; // 强制使用Redis时，连接失败则抛出错误
      }

      // 自动降级到增强内存缓存
      console.log('🔄 自动降级到增强内存缓存');
      cacheService = new EnhancedMemoryCache({
        persistFile: './cache/config-center-cache.json',
        maxMemory: 50 * 1024 * 1024
      });
      return cacheService;
    }
  }

  return null;
}

// 初始化缓存服务（同步方式，避免启动问题）
let cacheInitPromise = initializeCacheService().catch(error => {
  console.error('❌ 缓存服务初始化失败:', error.message);
  cacheService = null;
  return null;
});

// 存储SSE连接
const sseClients = new Set();

// 配置变更监听
configCenter.on('configChanged', async (configName, config) => {
  console.log(`📢 配置变更通知: ${configName}`);

  // 更新缓存
  try {
    await cacheInitPromise; // 等待缓存初始化完成
    if (cacheService && cacheService.isConnected()) {
      await cacheService.set('config_data', configName, config);
      console.log(`✅ 配置已缓存: ${configName}`);
    }
  } catch (error) {
    console.warn(`⚠️ 缓存更新失败: ${error.message}`);
  }

  // 通知所有SSE客户端
  const message = JSON.stringify({
    type: 'config_changed',
    config_name: configName,
    config: config,
    timestamp: new Date().toISOString()
  });

  sseClients.forEach(client => {
    try {
      client.write(`data: ${message}\n\n`);
    } catch (error) {
      console.error('SSE发送失败:', error);
      sseClients.delete(client);
    }
  });
});

// 健康检查
app.get('/health', async (req, res) => {
  try {
    const health = configCenter.getHealthStatus();

    // 等待缓存服务初始化完成
    await cacheInitPromise;

    let cacheHealth = null;
    if (cacheService && cacheService.healthCheck) {
      cacheHealth = await cacheService.healthCheck();
    }

    res.json({
      success: true,
      service: 'QMS轻量级配置中心',
      ...health,
      cache: cacheHealth,
      timestamp: new Date().toISOString()
    });
  } catch (error) {
    console.error('❌ 健康检查失败:', error.message);
    res.status(500).json({
      success: false,
      service: 'QMS轻量级配置中心',
      message: '健康检查失败',
      error: error.message,
      timestamp: new Date().toISOString()
    });
  }
});

// 获取所有配置名称
app.get('/api/configs', (req, res) => {
  try {
    const configNames = configCenter.getConfigNames();
    res.json({
      success: true,
      data: configNames,
      count: configNames.length
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: '获取配置列表失败',
      error: error.message
    });
  }
});

// 获取单个配置
app.get('/api/configs/:configName', async (req, res) => {
  try {
    const { configName } = req.params;
    const { key } = req.query;

    // 先尝试从Redis缓存获取
    let config = null;
    let fromCache = false;

    if (cacheService && cacheService.isConnected()) {
      config = await cacheService.get('config_data', configName);
      if (config) {
        fromCache = true;
        console.log(`📦 从缓存获取配置: ${configName}`);
      }
    }

    // 如果缓存中没有，从配置中心获取
    if (!config) {
      config = configCenter.getConfig(configName, key);

      // 将配置存入缓存
      if (config && cacheService && cacheService.isConnected()) {
        await cacheService.set('config_data', configName, config);
      }
    }

    if (config === null) {
      return res.status(404).json({
        success: false,
        message: `配置不存在: ${configName}${key ? '.' + key : ''}`
      });
    }

    // 如果指定了key，提取嵌套值
    if (key && typeof config === 'object') {
      config = key.split('.').reduce((current, prop) => {
        return current && current[prop] !== undefined ? current[prop] : null;
      }, config);
    }

    res.json({
      success: true,
      data: config,
      config_name: configName,
      key: key || null,
      from_cache: fromCache
    });
  } catch (error) {
    console.error(`❌ 获取配置失败 ${req.params.configName}:`, error.message);
    console.error(`📍 错误堆栈:`, error.stack);
    res.status(500).json({
      success: false,
      message: '获取配置失败',
      error: error.message
    });
  }
});

// 设置配置
app.post('/api/configs/:configName', async (req, res) => {
  try {
    const { configName } = req.params;
    const config = req.body;
    
    await configCenter.setConfig(configName, config);
    
    res.json({
      success: true,
      message: `配置已保存: ${configName}`,
      config_name: configName
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: '保存配置失败',
      error: error.message
    });
  }
});

// 删除配置
app.delete('/api/configs/:configName', async (req, res) => {
  try {
    const { configName } = req.params;
    
    await configCenter.deleteConfig(configName);
    
    res.json({
      success: true,
      message: `配置已删除: ${configName}`,
      config_name: configName
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: '删除配置失败',
      error: error.message
    });
  }
});

// AI模型专用接口 - 获取所有模型配置
app.get('/api/ai/models', (req, res) => {
  try {
    console.log('🔍 收到AI模型配置请求');
    const aiConfig = configCenter.getConfig('ai_models');
    console.log('📊 AI配置数据:', aiConfig ? '存在' : '不存在');

    if (!aiConfig) {
      console.log('❌ AI模型配置不存在');
      return res.status(404).json({
        success: false,
        message: 'AI模型配置不存在'
      });
    }

    console.log('✅ 返回AI模型配置，模型数量:', Object.keys(aiConfig.models || {}).length);
    console.log('📋 模型列表:', Object.keys(aiConfig.models || {}));
    console.log('🔧 响应数据结构:', {
      success: true,
      data: {
        default_model: aiConfig.default_model,
        models: aiConfig.models ? 'object' : 'null',
        rate_limits: aiConfig.rate_limits ? 'object' : 'null'
      }
    });

    const responseData = {
      success: true,
      data: {
        default_model: aiConfig.default_model,
        models: aiConfig.models,
        rate_limits: aiConfig.rate_limits
      }
    };

    res.json(responseData);
  } catch (error) {
    console.error('❌ 获取AI模型配置失败:', error);
    res.status(500).json({
      success: false,
      message: '获取AI模型配置失败',
      error: error.message
    });
  }
});

// AI模型专用接口 - 切换默认模型
app.post('/api/ai/models/switch', async (req, res) => {
  try {
    const { model_key } = req.body;
    
    if (!model_key) {
      return res.status(400).json({
        success: false,
        message: '缺少model_key参数'
      });
    }
    
    const aiConfig = configCenter.getConfig('ai_models');
    
    if (!aiConfig || !aiConfig.models[model_key]) {
      return res.status(404).json({
        success: false,
        message: `模型不存在: ${model_key}`
      });
    }
    
    // 更新默认模型
    aiConfig.default_model = model_key;
    await configCenter.setConfig('ai_models', aiConfig);
    
    res.json({
      success: true,
      message: `默认模型已切换为: ${aiConfig.models[model_key].name}`,
      default_model: model_key,
      model_info: aiConfig.models[model_key]
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: '切换模型失败',
      error: error.message
    });
  }
});

// AI模型专用接口 - 更新模型配置
app.put('/api/ai/models/:modelKey', async (req, res) => {
  try {
    const { modelKey } = req.params;
    const modelConfig = req.body;
    
    const aiConfig = configCenter.getConfig('ai_models');
    
    if (!aiConfig) {
      return res.status(404).json({
        success: false,
        message: 'AI模型配置不存在'
      });
    }
    
    // 更新模型配置
    aiConfig.models[modelKey] = {
      ...aiConfig.models[modelKey],
      ...modelConfig
    };
    
    await configCenter.setConfig('ai_models', aiConfig);
    
    res.json({
      success: true,
      message: `模型配置已更新: ${modelKey}`,
      model_key: modelKey,
      model_config: aiConfig.models[modelKey]
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: '更新模型配置失败',
      error: error.message
    });
  }
});

// 服务配置接口
app.get('/api/services', (req, res) => {
  try {
    const servicesConfig = configCenter.getConfig('services');
    
    if (!servicesConfig) {
      return res.status(404).json({
        success: false,
        message: '服务配置不存在'
      });
    }
    
    res.json({
      success: true,
      data: servicesConfig
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: '获取服务配置失败',
      error: error.message
    });
  }
});

// SSE配置变更通知
app.get('/api/events/config-changes', (req, res) => {
  // 设置SSE头
  res.writeHead(200, {
    'Content-Type': 'text/event-stream',
    'Cache-Control': 'no-cache',
    'Connection': 'keep-alive',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Headers': 'Cache-Control'
  });
  
  // 发送连接确认
  res.write(`data: ${JSON.stringify({
    type: 'connected',
    message: '配置变更通知已连接',
    timestamp: new Date().toISOString()
  })}\n\n`);
  
  // 添加到客户端列表
  sseClients.add(res);
  
  // 客户端断开连接时清理
  req.on('close', () => {
    sseClients.delete(res);
    console.log('📡 SSE客户端断开连接');
  });
  
  console.log('📡 新的SSE客户端连接');
});

// 配置导出接口
app.get('/api/export', (req, res) => {
  try {
    const allConfigs = {};
    
    configCenter.getConfigNames().forEach(name => {
      allConfigs[name] = configCenter.getConfig(name);
    });
    
    res.json({
      success: true,
      data: allConfigs,
      export_time: new Date().toISOString(),
      version: '1.0'
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: '导出配置失败',
      error: error.message
    });
  }
});

// 配置导入接口
app.post('/api/import', async (req, res) => {
  try {
    const { configs, overwrite = false } = req.body;

    if (!configs || typeof configs !== 'object') {
      return res.status(400).json({
        success: false,
        message: '无效的配置数据'
      });
    }

    const results = [];

    for (const [configName, config] of Object.entries(configs)) {
      try {
        if (!overwrite && configCenter.hasConfig(configName)) {
          results.push({
            config_name: configName,
            status: 'skipped',
            message: '配置已存在，跳过'
          });
          continue;
        }

        await configCenter.setConfig(configName, config);
        results.push({
          config_name: configName,
          status: 'success',
          message: '导入成功'
        });
      } catch (error) {
        results.push({
          config_name: configName,
          status: 'error',
          message: error.message
        });
      }
    }

    res.json({
      success: true,
      message: '配置导入完成',
      results: results
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: '导入配置失败',
      error: error.message
    });
  }
});

// Redis缓存管理接口
app.get('/api/cache/stats', async (req, res) => {
  try {
    const stats = await cacheService.getStats();
    res.json({
      success: true,
      data: stats
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: '获取缓存统计失败',
      error: error.message
    });
  }
});

// 清空指定策略的缓存
app.delete('/api/cache/:strategy', async (req, res) => {
  try {
    const { strategy } = req.params;
    const result = await cacheService.clearStrategy(strategy);

    if (result) {
      res.json({
        success: true,
        message: `缓存策略已清空: ${strategy}`
      });
    } else {
      res.status(500).json({
        success: false,
        message: `清空缓存策略失败: ${strategy}`
      });
    }
  } catch (error) {
    res.status(500).json({
      success: false,
      message: '清空缓存失败',
      error: error.message
    });
  }
});

// 错误处理中间件
app.use((error, req, res, next) => {
  console.error('❌ 服务错误:', error);
  res.status(500).json({
    success: false,
    message: '服务内部错误',
    error: error.message
  });
});

// ==================== 配置端前端所需的Mock API ====================

// 对象管理API - 获取对象树
app.get('/api/object/treeAndLockInfo', (req, res) => {
  try {
    // Mock对象树数据
    const mockObjectTree = [
      {
        bid: 'obj_001',
        name: 'QMS系统配置',
        modelCode: 'QMS_CONFIG',
        parentBid: null,
        children: [
          {
            bid: 'obj_002',
            name: 'AI模型配置',
            modelCode: 'AI_MODEL_CONFIG',
            parentBid: 'obj_001',
            children: []
          },
          {
            bid: 'obj_003',
            name: '用户权限配置',
            modelCode: 'USER_PERMISSION_CONFIG',
            parentBid: 'obj_001',
            children: []
          }
        ]
      }
    ];

    res.json({
      success: true,
      data: mockObjectTree,
      message: '获取对象树成功'
    });
  } catch (error) {
    console.error('❌ 获取对象树失败:', error);
    res.status(500).json({
      success: false,
      message: '获取对象树失败'
    });
  }
});

// 属性管理API - 获取标准属性分页数据
app.get('/api/attribute/standard/page', (req, res) => {
  try {
    // Mock属性分页数据
    const mockAttributePage = {
      records: [
        {
          bid: 'attr_001',
          name: 'model_name',
          displayName: '模型名称',
          dataType: 'STRING',
          required: true,
          defaultValue: '',
          description: 'AI模型的名称'
        },
        {
          bid: 'attr_002',
          name: 'model_type',
          displayName: '模型类型',
          dataType: 'ENUM',
          required: true,
          defaultValue: 'chat',
          description: 'AI模型的类型'
        }
      ],
      total: 2,
      size: 10,
      current: 1,
      pages: 1
    };

    res.json({
      success: true,
      data: mockAttributePage,
      message: '获取属性分页数据成功'
    });
  } catch (error) {
    console.error('❌ 获取属性分页数据失败:', error);
    res.status(500).json({
      success: false,
      message: '获取属性分页数据失败'
    });
  }
});

// 字典管理API - 获取字典描述和启用标志
app.get('/api/dictionary/desAndEnableFlags', (req, res) => {
  try {
    // Mock字典数据
    const mockDictionary = [
      {
        code: 'MODEL_TYPE',
        name: '模型类型',
        description: 'AI模型类型字典',
        enabled: true,
        items: [
          { code: 'chat', name: '对话模型', enabled: true },
          { code: 'embedding', name: '嵌入模型', enabled: true },
          { code: 'image', name: '图像模型', enabled: false }
        ]
      },
      {
        code: 'DATA_TYPE',
        name: '数据类型',
        description: '属性数据类型字典',
        enabled: true,
        items: [
          { code: 'STRING', name: '字符串', enabled: true },
          { code: 'NUMBER', name: '数字', enabled: true },
          { code: 'BOOLEAN', name: '布尔值', enabled: true },
          { code: 'ENUM', name: '枚举', enabled: true }
        ]
      }
    ];

    res.json({
      success: true,
      data: mockDictionary,
      message: '获取字典数据成功'
    });
  } catch (error) {
    console.error('❌ 获取字典数据失败:', error);
    res.status(500).json({
      success: false,
      message: '获取字典数据失败'
    });
  }
});

// ==================== 认证Mock API ====================

// 用户登录API
app.post('/api/auth/login', (req, res) => {
  try {
    const { username, password } = req.body;

    // Mock用户验证
    const mockUsers = {
      'admin': 'admin123',
      'developer': 'dev123',
      'quality': 'qa123'
    };

    if (mockUsers[username] && mockUsers[username] === password) {
      // 生成Mock Token
      const token = `mock_token_${username}_${Date.now()}`;

      res.json({
        success: true,
        data: {
          token: token,
          user: {
            id: username,
            username: username,
            name: username === 'admin' ? '管理员' : username === 'developer' ? '开发者' : '质量管理员',
            role: username === 'admin' ? 'admin' : username === 'developer' ? 'developer' : 'quality'
          }
        },
        message: '登录成功'
      });
    } else {
      res.status(401).json({
        success: false,
        message: '用户名或密码错误'
      });
    }
  } catch (error) {
    console.error('❌ 登录失败:', error);
    res.status(500).json({
      success: false,
      message: '登录失败'
    });
  }
});

// 获取用户信息API
app.get('/api/auth/user', (req, res) => {
  try {
    const token = req.headers.authorization?.replace('Bearer ', '');

    if (token && token.startsWith('mock_token_')) {
      const username = token.split('_')[2];

      res.json({
        success: true,
        data: {
          id: username,
          username: username,
          name: username === 'admin' ? '管理员' : username === 'developer' ? '开发者' : '质量管理员',
          role: username === 'admin' ? 'admin' : username === 'developer' ? 'developer' : 'quality'
        },
        message: '获取用户信息成功'
      });
    } else {
      res.status(401).json({
        success: false,
        message: '未授权访问'
      });
    }
  } catch (error) {
    console.error('❌ 获取用户信息失败:', error);
    res.status(500).json({
      success: false,
      message: '获取用户信息失败'
    });
  }
});

// 404处理
app.use('*', (req, res) => {
  res.status(404).json({
    success: false,
    message: 'API接口不存在',
    path: req.originalUrl
  });
});

// 设置服务类型环境变量
process.env.SERVICE_TYPE = 'config';

// 启动服务
const server = app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 QMS轻量级配置中心启动成功！`);
  console.log(`📍 服务地址: http://localhost:${PORT}`);
  console.log(`🔧 健康检查: http://localhost:${PORT}/health`);
  console.log(`📋 配置API: http://localhost:${PORT}/api/configs`);
  console.log(`🤖 AI模型API: http://localhost:${PORT}/api/ai/models`);
  console.log(`📡 配置通知: http://localhost:${PORT}/api/events/config-changes`);
  console.log(`🔧 进程ID: ${process.pid}`);
});

server.on('error', (error) => {
  console.error('❌ 配置服务启动错误:', error);
  if (error.code === 'EADDRINUSE') {
    console.error(`❌ 端口 ${PORT} 已被占用，请检查是否有其他进程使用此端口`);
  }
  process.exit(1);
});

server.on('listening', () => {
  const addr = server.address();
  console.log(`✅ 配置服务正在监听 ${addr.address}:${addr.port}`);
});

// 优雅关闭
process.on('SIGTERM', async () => {
  console.log('🔄 收到SIGTERM信号，开始优雅关闭...');

  server.close(async () => {
    console.log('🔒 HTTP服务已关闭');
    configCenter.close();
    if (cacheService && cacheService.close) {
      await cacheService.close();
    }
    process.exit(0);
  });
});

process.on('SIGINT', async () => {
  console.log('🔄 收到SIGINT信号，开始优雅关闭...');

  server.close(async () => {
    console.log('🔒 HTTP服务已关闭');
    configCenter.close();
    if (cacheService && cacheService.close) {
      await cacheService.close();
    }
    process.exit(0);
  });
});

module.exports = { app, configCenter };
