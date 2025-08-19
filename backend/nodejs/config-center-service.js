/**
 * QMS AI配置中心服务
 * 提供系统配置管理功能
 */

const express = require('express');
const cors = require('cors');

const app = express();
const PORT = 3003;

// 中间件
app.use(cors());
app.use(express.json());

// 配置数据存储
const configData = {
  system: {
    name: 'QMS AI智能质量管理系统',
    version: '2.0.0',
    environment: 'production'
  },
  features: {
    analytics: true,
    recommendations: true,
    collaboration: true,
    integration: true,
    export: true
  },
  ai: {
    provider: 'deepseek',
    model: 'deepseek-chat',
    maxTokens: 4000,
    temperature: 0.7
  },
  ai_models: {
    models: {
      // 外部模型 - 直连DeepSeek (2个)
      'deepseek-chat': {
        name: 'DeepSeek Chat',
        model: 'deepseek-chat',
        provider: 'deepseek',
        baseURL: 'https://api.deepseek.com',
        enabled: true,
        capabilities: ['text', 'tools'],
        maxTokens: 4000,
        temperature: 0.7,
        priority: 8
      },
      'deepseek-coder': {
        name: 'DeepSeek Coder',
        model: 'deepseek-coder',
        provider: 'deepseek',
        baseURL: 'https://api.deepseek.com',
        enabled: true,
        capabilities: ['text', 'code', 'tools'],
        maxTokens: 4000,
        temperature: 0.7,
        priority: 7
      },

      // 内部模型 - 通过传音代理 (6个)
      'gpt-4o': {
        name: 'GPT-4o',
        model: 'gpt-4o',
        provider: 'openai',
        baseURL: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
        enabled: true,
        capabilities: ['text', 'image', 'tools'],
        maxTokens: 4000,
        temperature: 0.7,
        priority: 9
      },
      'gpt-4o-mini': {
        name: 'GPT-4o Mini',
        model: 'gpt-4o-mini',
        provider: 'openai',
        baseURL: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
        enabled: true,
        capabilities: ['text', 'tools'],
        maxTokens: 4000,
        temperature: 0.7,
        priority: 6
      },
      'gemini-2.5-pro-thinking': {
        name: 'Gemini 2.5 Pro Thinking',
        model: 'gemini-2.5-pro-thinking',
        provider: 'google',
        baseURL: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
        enabled: true,
        capabilities: ['text', 'reasoning', 'tools'],
        maxTokens: 4000,
        temperature: 0.7,
        priority: 8
      },
      'claude-3-5-sonnet-20241022': {
        name: 'Claude 3.5 Sonnet',
        model: 'claude-3-5-sonnet-20241022',
        provider: 'anthropic',
        baseURL: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
        enabled: true,
        capabilities: ['text', 'analysis', 'tools'],
        maxTokens: 4000,
        temperature: 0.7,
        priority: 7
      },
      'gpt-3.5-turbo': {
        name: 'GPT-3.5 Turbo',
        model: 'gpt-3.5-turbo',
        provider: 'openai',
        baseURL: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
        enabled: true,
        capabilities: ['text', 'tools'],
        maxTokens: 4000,
        temperature: 0.7,
        priority: 5
      },
      'claude-3-haiku': {
        name: 'Claude 3 Haiku',
        model: 'claude-3-haiku-20240307',
        provider: 'anthropic',
        baseURL: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
        enabled: true,
        capabilities: ['text', 'fast'],
        maxTokens: 4000,
        temperature: 0.7,
        priority: 4
      }
    },
    default_model: 'gpt-4o'
  },
  database: {
    type: 'sqlite',
    path: './database/chat_history.db'
  },
  security: {
    enableAuth: true,
    sessionTimeout: 3600,
    maxLoginAttempts: 5
  }
};

// 路由

// 健康检查
app.get('/health', (req, res) => {
  res.json({
    success: true,
    status: 'healthy',
    service: 'config-center',
    port: PORT,
    timestamp: new Date().toISOString(),
    uptime: process.uptime()
  });
});

// 获取所有配置
app.get('/config', (req, res) => {
  res.json({
    success: true,
    data: configData,
    timestamp: new Date().toISOString()
  });
});

// 获取特定配置
app.get('/config/:section', (req, res) => {
  const { section } = req.params;
  
  if (configData[section]) {
    res.json({
      success: true,
      data: configData[section],
      timestamp: new Date().toISOString()
    });
  } else {
    res.status(404).json({
      success: false,
      message: `配置节 '${section}' 不存在`
    });
  }
});

// 更新配置
app.post('/config/:section', (req, res) => {
  const { section } = req.params;
  const updates = req.body;
  
  if (configData[section]) {
    configData[section] = { ...configData[section], ...updates };
    res.json({
      success: true,
      message: `配置节 '${section}' 更新成功`,
      data: configData[section],
      timestamp: new Date().toISOString()
    });
  } else {
    res.status(404).json({
      success: false,
      message: `配置节 '${section}' 不存在`
    });
  }
});

// 获取功能开关状态
app.get('/features', (req, res) => {
  res.json({
    success: true,
    data: configData.features,
    timestamp: new Date().toISOString()
  });
});

// 更新功能开关
app.post('/features/:feature', (req, res) => {
  const { feature } = req.params;
  const { enabled } = req.body;
  
  if (configData.features.hasOwnProperty(feature)) {
    configData.features[feature] = Boolean(enabled);
    res.json({
      success: true,
      message: `功能 '${feature}' ${enabled ? '已启用' : '已禁用'}`,
      data: configData.features,
      timestamp: new Date().toISOString()
    });
  } else {
    res.status(404).json({
      success: false,
      message: `功能 '${feature}' 不存在`
    });
  }
});

// 获取AI模型配置
app.get('/api/ai/models', (req, res) => {
  res.json({
    success: true,
    data: configData.ai_models,
    timestamp: new Date().toISOString()
  });
});

// 兼容配置客户端的API端点
app.get('/api/configs/:configName', (req, res) => {
  const { configName } = req.params;
  const { key } = req.query;

  if (configData[configName]) {
    let data = configData[configName];

    // 如果指定了key，获取嵌套值
    if (key) {
      const keys = key.split('.');
      for (const k of keys) {
        if (data && typeof data === 'object' && data.hasOwnProperty(k)) {
          data = data[k];
        } else {
          return res.status(404).json({
            success: false,
            message: `配置键 '${key}' 在 '${configName}' 中不存在`
          });
        }
      }
    }

    res.json({
      success: true,
      data: data,
      timestamp: new Date().toISOString()
    });
  } else {
    res.status(404).json({
      success: false,
      message: `配置节 '${configName}' 不存在`
    });
  }
});

// 应用端业务API - 对象模型管理
app.post('/api/object-model/:moduleKey/page', (req, res) => {
  const { moduleKey } = req.params;
  const { page = 1, size = 20, ...filters } = req.body;

  console.log(`📋 获取${moduleKey}数据 - 页码:${page}, 大小:${size}`, filters);

  // 模拟数据
  const mockData = generateMockData(moduleKey, page, size, filters);

  res.json({
    success: true,
    data: mockData,
    message: `获取${moduleKey}数据成功`,
    timestamp: new Date().toISOString()
  });
});

// 生成模拟数据的函数
function generateMockData(moduleKey, page, size, filters) {
  const total = 100; // 模拟总数
  const startIndex = (page - 1) * size;
  const records = [];

  for (let i = 0; i < size && startIndex + i < total; i++) {
    const index = startIndex + i + 1;

    switch (moduleKey) {
      case 'aiConversation':
        records.push({
          conversationId: `CONV-${String(index).padStart(6, '0')}`,
          userId: `USER-${String(Math.floor(Math.random() * 1000)).padStart(3, '0')}`,
          userQuestion: `这是第${index}个质量管理相关问题`,
          conversationStatus: ['进行中', '已完成', '已中断'][Math.floor(Math.random() * 3)],
          satisfactionScore: Math.floor(Math.random() * 5) + 1,
          conversationTime: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000).toISOString()
        });
        break;

      case 'dataSource':
        records.push({
          dataSourceName: `数据源-${index}`,
          dataSourceType: ['MySQL', 'PostgreSQL', 'Oracle'][Math.floor(Math.random() * 3)],
          connectionUrl: `jdbc:mysql://localhost:3306/qms_db_${index}`,
          connectionStatus: Math.random() > 0.2 ? '已连接' : '连接失败',
          updateFrequency: ['实时', '每小时', '每日'][Math.floor(Math.random() * 3)]
        });
        break;

      case 'aiRule':
        records.push({
          ruleName: `质量规则-${index}`,
          ruleType: ['检测规则', '预警规则', '优化规则'][Math.floor(Math.random() * 3)],
          priority: Math.floor(Math.random() * 10) + 1,
          ruleStatus: Math.random() > 0.3,
          triggerCondition: `当质量指标低于${Math.floor(Math.random() * 50) + 50}%时`,
          executeAction: `发送预警通知并生成改进建议`
        });
        break;

      case 'userManagement':
        records.push({
          username: `user${index}`,
          realName: `用户${index}`,
          userRole: ['管理员', '质量工程师', '普通用户'][Math.floor(Math.random() * 3)],
          email: `user${index}@qms.com`,
          phone: `138${String(Math.floor(Math.random() * 100000000)).padStart(8, '0')}`,
          accountStatus: Math.random() > 0.1
        });
        break;

      default:
        records.push({
          id: index,
          name: `${moduleKey}-${index}`,
          status: '正常',
          createTime: new Date().toISOString()
        });
    }
  }

  return {
    records,
    total,
    page: parseInt(page),
    size: parseInt(size),
    pages: Math.ceil(total / size)
  };
}

// 获取系统信息
app.get('/system/info', (req, res) => {
  res.json({
    success: true,
    data: {
      ...configData.system,
      nodeVersion: process.version,
      platform: process.platform,
      arch: process.arch,
      uptime: process.uptime(),
      memory: process.memoryUsage(),
      pid: process.pid
    },
    timestamp: new Date().toISOString()
  });
});

// 重置配置
app.post('/config/reset', (req, res) => {
  // 重置为默认配置
  Object.assign(configData, {
    system: {
      name: 'QMS AI智能质量管理系统',
      version: '2.0.0',
      environment: 'production'
    },
    features: {
      analytics: true,
      recommendations: true,
      collaboration: true,
      integration: true,
      export: true
    },
    ai: {
      provider: 'deepseek',
      model: 'deepseek-chat',
      maxTokens: 4000,
      temperature: 0.7
    }
  });
  
  res.json({
    success: true,
    message: '配置已重置为默认值',
    data: configData,
    timestamp: new Date().toISOString()
  });
});

// 错误处理中间件
app.use((err, req, res, next) => {
  console.error('配置中心错误:', err);
  res.status(500).json({
    success: false,
    message: '服务器内部错误',
    error: process.env.NODE_ENV === 'development' ? err.message : undefined
  });
});

// 404处理
app.use((req, res) => {
  res.status(404).json({
    success: false,
    message: '接口不存在',
    path: req.path
  });
});

// 启动服务
app.listen(PORT, () => {
  console.log(`🚀 QMS AI配置中心服务启动成功！`);
  console.log(`📍 服务地址: http://localhost:${PORT}`);
  console.log(`🔧 健康检查: http://localhost:${PORT}/health`);
  console.log(`⚙️ 配置管理: http://localhost:${PORT}/config`);
  console.log(`🎛️ 功能开关: http://localhost:${PORT}/features`);
  console.log(`📊 系统信息: http://localhost:${PORT}/system/info`);
  console.log(`✅ 配置中心服务就绪！`);
});

// 优雅关闭
process.on('SIGTERM', () => {
  console.log('📴 配置中心服务正在关闭...');
  process.exit(0);
});

process.on('SIGINT', () => {
  console.log('📴 配置中心服务正在关闭...');
  process.exit(0);
});
