const express = require('express');
const cors = require('cors');
const { v4: uuidv4 } = require('uuid');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const DatabaseAdapter = require('./database/database-adapter');
const MonitoringService = require('./services/monitoring-service');
const PrometheusMetrics = require('./services/prometheus-metrics');
const AlertManager = require('./services/alert-manager');
const ConfigClient = require('./config/config-client');
const RedisCacheService = require('./services/redis-cache-service');
const PluginManager = require('./services/plugin-manager');
const WorkflowExecutionEngine = require('./services/workflow-execution-engine');
const IntelligentModelManager = require('./services/intelligent-model-manager');
const { JWTManager, InputValidator, SecurityMiddleware } = require('./utils/security');
const { httpClient } = require('./utils/http-client');
const {
  QMSError,
  ErrorTypes,
  asyncErrorHandler,
  notFoundHandler,
  globalErrorHandler,
  setupGlobalHandlers,
  errorStatsHandler,
  logger
} = require('./middleware/error-handler');
const {
  validators,
  sanitizeInput,
  validateRateLimit
} = require('./middleware/input-validation');
const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '.env') });
const SecurityConfig = require('./config/security-config');
const securityConfig = new SecurityConfig();
securityConfig.initialize();
const app = express();

// 初始化监控服务
const monitoring = new MonitoringService();

// 初始化Prometheus指标
const prometheusMetrics = new PrometheusMetrics();

// 初始化告警管理器
const alertManager = new AlertManager();

// 初始化配置客户端
const configClient = new ConfigClient({
  configServiceUrl: process.env.CONFIG_SERVICE_URL || 'http://localhost:3003',
  configNodes: [process.env.CONFIG_SERVICE_URL || 'http://localhost:3003'],
  maxRetries: 3,
  retryDelay: 2000
});

// 初始化Redis缓存服务
const redisCache = new RedisCacheService({
  host: process.env.REDIS_HOST || 'localhost',
  port: process.env.REDIS_PORT || 6379,
  password: process.env.REDIS_PASSWORD || undefined,
  database: process.env.REDIS_DB || 0
});

// 尝试连接Redis（可选，不影响主要功能）
redisCache.connect().catch(error => {
  console.warn('⚠️ Redis连接失败，将使用内存缓存:', error.message);
});

// 初始化插件管理器
const pluginManager = new PluginManager({
  autoLoad: true,
  maxPlugins: 20
});

// 初始化插件管理器
pluginManager.initialize().catch(error => {
  console.warn('⚠️ 插件管理器初始化失败:', error.message);
});

// 初始化工作流执行引擎
const workflowEngine = new WorkflowExecutionEngine({
  maxConcurrentNodes: 10,
  executionTimeout: 300000, // 5分钟
  retryAttempts: 3
});

// 监听工作流执行事件
workflowEngine.on('workflow-completed', (event) => {
  console.log(`✅ 工作流执行完成: ${event.executionId} (${event.metrics.executionTime}ms)`);
});

workflowEngine.on('workflow-failed', (event) => {
  console.error(`❌ 工作流执行失败: ${event.executionId} - ${event.error}`);
});

workflowEngine.on('node-completed', (event) => {
  console.log(`🔄 节点执行完成: ${event.nodeId}`);
});

// 初始化智能模型管理器
const modelManager = new IntelligentModelManager({
  warmupEnabled: true,
  performanceTracking: true,
  autoOptimization: true
});

// 监听模型切换事件
modelManager.on('model-switched', (event) => {
  console.log(`🔄 模型切换: ${event.from} → ${event.to}`);
});

// 手动初始化模型管理器
(async () => {
  try {
    await modelManager.initializeModels();
    console.log('✅ 智能模型管理器初始化完成');
  } catch (error) {
    console.error('❌ 智能模型管理器初始化失败:', error.message);
    // 继续运行，使用默认配置
  }
})();

// 设置全局错误处理
setupGlobalHandlers();

// 安全中间件配置
app.use(helmet({
  contentSecurityPolicy: {
    directives: {
      defaultSrc: ["'self'"],
      styleSrc: ["'self'", "'unsafe-inline'"],
      scriptSrc: ["'self'"],
      imgSrc: ["'self'", "data:", "https:"],
      connectSrc: ["'self'", "https://api.openai.com", "https://api.deepseek.com", "https://api.anthropic.com"]
    }
  }
}));

// AI API限流 - 调整为更宽松的限制以支持复杂任务
const aiApiLimiter = rateLimit({
  windowMs: 1 * 60 * 1000, // 1分钟
  max: 100, // 每分钟最多100个AI请求，支持复杂任务的多步骤处理
  message: {
    success: false,
    message: 'AI请求过于频繁，请稍后再试'
  },
  standardHeaders: true,
  legacyHeaders: false
});

// 一般API限流
const generalApiLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15分钟
  max: 200, // 每15分钟最多200个请求
  message: {
    success: false,
    message: '请求过于频繁，请稍后再试'
  }
});

app.use('/api/chat/', aiApiLimiter);
app.use('/api/', generalApiLimiter);

// 基础中间件配置
app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// Prometheus指标中间件
app.use(prometheusMetrics.createMiddleware());

// 监控中间件
app.use((req, res, next) => {
  const startTime = Date.now();

  // 记录请求
  res.on('finish', () => {
    const responseTime = Date.now() - startTime;
    const success = res.statusCode < 400;

    monitoring.recordRequest(success, responseTime);

    // 记录请求日志
    const status = success ? '✅' : '❌';
    console.log(`${status} ${new Date().toLocaleTimeString()} ${req.method} ${req.path} - ${res.statusCode} (${responseTime}ms)`);
  });

  next();
});

console.log('Starting QMS Multi-Model Chat Service with Transsion AI & DeepSeek Integration...');

// 初始化数据库适配器
const dbAdapter = new DatabaseAdapter();

// 初始化聊天历史数据库（用于对话记录管理API）
const ChatHistoryDB = require('./database/chat-history-db');
const chatHistoryDB = new ChatHistoryDB();

// 初始化导出服务
const ExportService = require('./services/export-service');
const exportService = new ExportService();

// 用户认证中间件 - 强制JWT认证，不支持匿名用户
const authenticateUser = (req, res, next) => {
  const authHeader = req.headers.authorization;

  if (!authHeader) {
    return res.status(401).json({
      success: false,
      message: '访问被拒绝：请先登录才能使用聊天功能',
      code: 'MISSING_TOKEN'
    });
  }

  // 验证JWT Token
  const decoded = JWTManager.verifyToken(authHeader);

  if (!decoded) {
    return res.status(401).json({
      success: false,
      message: '认证令牌无效或已过期，请重新登录',
      code: 'INVALID_TOKEN'
    });
  }

  // 设置用户信息
  req.user = decoded;
  console.log(`✅ 用户认证成功: ${decoded.username} (${decoded.userId})`);
  next();
};

// 双API密钥配置管理
const getInternalApiConfig = () => ({
  apiKey: process.env.INTERNAL_AI_API_KEY || process.env.AI_API_KEY || 'your_internal_api_key_here',
  baseURL: process.env.INTERNAL_AI_BASE_URL || process.env.AI_BASE_URL || 'https://hk-intra-paas.transsion.com/tranai-proxy/v1'
});

const getExternalApiConfig = () => ({
  apiKey: process.env.EXTERNAL_AI_API_KEY || process.env.DEEPSEEK_API_KEY || 'your_external_api_key_here',
  baseURL: process.env.EXTERNAL_AI_BASE_URL || process.env.DEEPSEEK_BASE_URL || 'https://api.deepseek.com'
});

// 检查必要的环境变量
function checkRequiredEnvVars() {
  const internalRequired = ['INTERNAL_AI_API_KEY', 'INTERNAL_AI_BASE_URL'];
  const externalRequired = ['EXTERNAL_AI_API_KEY', 'EXTERNAL_AI_BASE_URL'];

  const missingInternal = internalRequired.filter(key => !process.env[key] && !process.env[key.replace('INTERNAL_', '')]);
  const missingExternal = externalRequired.filter(key => !process.env[key] && !process.env[key.replace('EXTERNAL_', 'DEEPSEEK_')]);

  if (missingInternal.length > 0) {
    console.warn('⚠️ 缺少内部模型环境变量:', missingInternal.join(', '));
    console.warn('⚠️ 内部6个模型可能无法正常工作');
  }

  if (missingExternal.length > 0) {
    console.warn('⚠️ 缺少外部模型环境变量:', missingExternal.join(', '));
    console.warn('⚠️ 外部模型可能无法正常工作');
  }

  console.log('🔑 API配置状态:');
  console.log('   内部模型API:', getInternalApiConfig().baseURL);
  console.log('   外部模型API:', getExternalApiConfig().baseURL);
}

checkRequiredEnvVars();

// AI模型配置 - 使用安全配置管理
let AI_CONFIGS = {};

// 默认配置 - 将在配置加载后设置
let currentConfig = null;

// 初始化AI配置
function initializeAIConfigs() {
  try {
    const aiConfigs = securityConfig.getAllAIConfigs();

    AI_CONFIGS = {
      'deepseek-chat': {
        name: 'DeepSeek Chat',
        model: 'deepseek-chat',
        provider: 'deepseek',
        baseURL: aiConfigs.deepseek.baseURL,
        apiKey: aiConfigs.deepseek.apiKey,
        timeout: aiConfigs.deepseek.timeout,
        features: {
          streaming: true,
          vision: false,
          tools: true
        }
      },
      'deepseek-coder': {
        name: 'DeepSeek Coder',
        model: 'deepseek-coder',
        provider: 'deepseek',
        baseURL: aiConfigs.deepseek.baseURL,
        apiKey: aiConfigs.deepseek.apiKey,
        timeout: aiConfigs.deepseek.timeout,
        features: {
          streaming: true,
          vision: false,
          tools: true
        }
      },
      'gpt-4o': {
        name: 'GPT-4o',
        model: 'gpt-4o',
        provider: 'openai',
        baseURL: aiConfigs.openai.baseURL,
        apiKey: aiConfigs.openai.apiKey,
        timeout: aiConfigs.openai.timeout,
        features: {
          streaming: true,
          vision: true,
          tools: true
        }
      },
      'gpt-4o-mini': {
        name: 'GPT-4o Mini',
        model: 'gpt-4o-mini',
        provider: 'openai',
        baseURL: aiConfigs.openai.baseURL,
        apiKey: aiConfigs.openai.apiKey,
        timeout: aiConfigs.openai.timeout,
        features: {
          streaming: true,
          vision: true,
          tools: true
        }
      }
    };

    // 设置默认配置
    currentConfig = AI_CONFIGS['deepseek-chat'];

    console.log('🔐 AI配置初始化完成，使用安全配置管理');
    return true;
  } catch (error) {
    console.error('❌ AI配置初始化失败:', error.message);
    return false;
  }
}

// 数据库适配器将在服务启动时初始化

// 聊天历史存储（使用数据库）
let chatHistory = [];
let conversationStats = {
  totalConversations: 0,
  totalMessages: 0,
  averageResponseTime: 0
};

// 质量管理知识库
const QMS_KNOWLEDGE_BASE = {
  systemPrompt: `你是一个专业的质量管理系统(QMS)智能助手。你具备以下专业知识：

1. 质量管理体系标准（ISO 9001、ISO 14001、ISO 45001等）
2. 质量控制方法（SPC、6 Sigma、精益生产等）
3. 质量检测技术和标准
4. 缺陷分析和根因分析方法
5. 质量改进工具和技术
6. 质量数据分析和统计方法
7. 供应商质量管理
8. 产品生命周期质量管理

请用专业、准确、实用的方式回答用户的质量管理相关问题。如果问题超出质量管理范围，请礼貌地引导用户回到质量管理话题。

回答要求：
- 专业准确，基于标准和最佳实践
- 结构清晰，便于理解和执行
- 提供具体的方法和工具建议
- 适当举例说明
- 中文回答`,

  commonQuestions: [
    {
      category: "质量体系",
      questions: [
        "如何建立ISO 9001质量管理体系？",
        "质量手册应该包含哪些内容？",
        "内审和管理评审的区别是什么？"
      ]
    },
    {
      category: "质量控制",
      questions: [
        "SPC统计过程控制如何实施？",
        "控制图的类型和应用场景？",
        "如何设定质量控制限？"
      ]
    },
    {
      category: "缺陷分析",
      questions: [
        "8D问题解决方法的步骤？",
        "鱼骨图分析法如何使用？",
        "5Why分析的注意事项？"
      ]
    }
  ]
};

// 健康检查
app.get('/health', (req, res) => {
  const healthStatus = monitoring.getHealthStatus();

  res.json({
    status: healthStatus.status === 'HEALTHY' ? 'UP' : 'DOWN',
    service: 'QMS Chat Service',
    deepseek_status: 'connected',
    timestamp: new Date().toISOString(),
    health: healthStatus,
    issues: healthStatus.issues,
    uptime: process.uptime()
  });
});

// Prometheus指标端点
app.get('/metrics', async (req, res) => {
  try {
    res.set('Content-Type', prometheusMetrics.getContentType());
    const metrics = await prometheusMetrics.getMetrics();
    res.end(metrics);
  } catch (error) {
    console.error('获取Prometheus指标失败:', error);
    res.status(500).end('Error collecting metrics');
  }
});

// 发送聊天消息 - 使用安全验证和错误处理
app.post('/api/chat/send',
  sanitizeInput,                    // 输入清理
  validators.validateChatMessage,   // 输入验证
  validateRateLimit(200, 60000),    // 速率限制：每分钟200次，支持复杂任务处理
  SecurityMiddleware.optionalAuth,  // 身份验证
  asyncErrorHandler(async (req, res) => {
    const startTime = Date.now();

    try {
      // 输入已经通过验证中间件验证，直接使用
      const {
        message,
        conversationId,
        model = 'deepseek-chat',
        temperature = 0.7,
        maxTokens = 2000
      } = req.body;

      const { history = [], user_info } = req.body;

      console.log(`💬 收到聊天请求: ${message.substring(0, 50)}...`);
      console.log(`🤖 使用模型: ${model}, 对话ID: ${conversationId || '新对话'}`);

      // 输入验证已在中间件中完成，这里只做业务逻辑检查

    // 获取认证用户信息（优先使用认证中间件提供的用户信息）
    const authenticatedUser = req.user;
    const requestUserInfo = user_info || {};

    // 调试：打印认证用户信息
    console.log('🔍 调试认证用户信息:', JSON.stringify(authenticatedUser, null, 2));

    const userData = {
      id: authenticatedUser.id || authenticatedUser.userId, // 兼容不同的用户ID字段
      username: requestUserInfo.username || authenticatedUser.username,
      email: requestUserInfo.email || authenticatedUser.email,
      department: requestUserInfo.department || authenticatedUser.department,
      role: requestUserInfo.role || authenticatedUser.role
    };

    console.log(`👤 用户信息: ${userData.username} (${userData.id})`);

    // 确保用户存在于数据库中
    // 注意：数据库适配器会自动处理用户创建

    // 处理文件信息
    let fileContext = '';
    if (req.body.files && req.body.files.length > 0) {
      const fileList = req.body.files.map(file =>
        `- ${file.name} (${file.type}, ${(file.size / 1024).toFixed(1)}KB)`
      ).join('\n');

      fileContext = `\n\n用户上传了以下文件：\n${fileList}\n\n请根据文件类型和用户的问题提供相应的帮助。如果是图片文件，说明您可以分析图片内容；如果是文档文件，说明您可以帮助分析文档内容；如果是表格文件，说明您可以帮助分析数据。`;
    }

    // 构建对话历史
    const messages = [
      {
        role: 'system',
        content: QMS_KNOWLEDGE_BASE.systemPrompt
      },
      ...history.slice(-10), // 保留最近10轮对话
      {
        role: 'user',
        content: message + fileContext
      }
    ];

    // 获取模型配置 - 优先使用请求中的模型ID
    let modelConfig = currentConfig;

    // 如果请求中指定了模型，查找对应的配置
    if (model && model !== 'deepseek-chat') {
      // 首先尝试直接匹配配置key
      if (AI_CONFIGS[model]) {
        modelConfig = AI_CONFIGS[model];
        console.log(`🔄 使用指定模型配置 (直接匹配): ${modelConfig.name} - ${modelConfig.model}`);
      } else {
        // 然后尝试匹配模型名称
        const configKey = Object.keys(AI_CONFIGS).find(key => {
          const config = AI_CONFIGS[key];
          return config.model === model;
        });

        if (configKey && AI_CONFIGS[configKey]) {
          modelConfig = AI_CONFIGS[configKey];
          console.log(`🔄 使用指定模型配置 (模型名匹配): ${modelConfig.name} - ${modelConfig.model}`);
        }
      }
    }

    // 如果请求中有model_config，优先使用
    if (req.body.model_config) {
      modelConfig = req.body.model_config;
      console.log(`🔄 使用请求中的模型配置: ${modelConfig.name || modelConfig.model}`);
    }

    // 调用AI API
    const aiResponse = await callAIAPI(messages, modelConfig);

    const responseTime = Date.now() - startTime;

    // 生成或使用现有的对话ID
    const currentConversationId = conversationId || generateConversationId();

    // 如果是新对话，创建对话会话记录
    if (!conversationId) {
      await dbAdapter.saveConversation({
        id: currentConversationId,
        user_id: userData.id,
        title: message.substring(0, 50) + (message.length > 50 ? '...' : ''),
        model_provider: modelConfig.name || 'Unknown',
        model_name: modelConfig.model,
        model_config: modelConfig
      });
    }

    // 保存用户消息
    const userMessageId = generateMessageId();
    await dbAdapter.saveMessage({
      id: userMessageId,
      conversation_id: currentConversationId,
      user_id: userData.id,
      message_type: 'user',
      content: message
    });

    // 保存AI回复
    const aiMessageId = generateMessageId();
    await dbAdapter.saveMessage({
      id: aiMessageId,
      conversation_id: currentConversationId,
      user_id: userData.id,
      message_type: 'assistant',
      content: aiResponse.content,
      model_info: {
        provider: modelConfig.name || 'Unknown',
        model: modelConfig.model,
        temperature: modelConfig.temperature
      },
      response_time: responseTime,
      token_usage: aiResponse.usage || {}
    });

    // 记录监控指标
    monitoring.recordAIConversation(
      modelConfig.model || 'unknown',
      responseTime,
      true // 成功
    );
    monitoring.recordMessage();

    // 记录Prometheus指标
    prometheusMetrics.recordAiRequest(
      modelConfig.name || 'Unknown',
      modelConfig.model,
      'success',
      responseTime / 1000 // 转换为秒
    );
    prometheusMetrics.recordMessage('user', modelConfig.name);
    prometheusMetrics.recordMessage('assistant', modelConfig.name);
    prometheusMetrics.recordConversation(userData.id === 'anonymous' ? 'anonymous' : 'registered');

    // 保存对话记录到内存（向后兼容）
    const chatRecord = {
      id: aiMessageId,
      conversation_id: currentConversationId,
      user_message: message,
      ai_response: aiResponse.content,
      timestamp: new Date().toISOString(),
      response_time: responseTime,
      model_info: {
        provider: modelConfig.name || 'Unknown',
        model: modelConfig.model,
        tokens_used: aiResponse.usage || {},
        temperature: modelConfig.temperature
      }
    };

    chatHistory.push(chatRecord);

    // 注意：数据库保存已在上面的新架构中处理，这里移除重复保存逻辑
    console.log('✅ 对话记录已通过新架构保存到数据库');

    // 发送监控数据到后端（暂时禁用，避免404错误）
    // await sendMonitoringData(chatRecord, req);

    // 更新统计信息
    conversationStats.totalMessages++;
    conversationStats.averageResponseTime =
      (conversationStats.averageResponseTime + responseTime) / 2;
    
    console.log(`✅ 聊天响应成功，耗时: ${responseTime}ms`);
    
    res.json({
      success: true,
      data: {
        response: aiResponse.content,
        conversation_id: currentConversationId,
        message_id: aiMessageId,
        user_message_id: userMessageId,
        response_time: responseTime,
        model_info: chatRecord.model_info
      }
    });

  } catch (error) {
    // 抛出错误让全局错误处理中间件处理
    if (error.response && error.response.status) {
      throw new QMSError(
        'AI服务调用失败',
        ErrorTypes.EXTERNAL_API_ERROR,
        error.response.status,
        { aiModel: currentConfig.name, originalError: error.message }
      );
    } else if (error.code === 'ECONNABORTED') {
      throw new QMSError(
        'AI服务响应超时',
        ErrorTypes.TIMEOUT_ERROR,
        408,
        { aiModel: currentConfig.name, timeout: error.config?.timeout }
      );
    } else {
      throw new QMSError(
        '聊天服务内部错误',
        ErrorTypes.INTERNAL_ERROR,
        500,
        { aiModel: currentConfig.name, originalError: error.message }
      );
    }
  }
}));

// 调用AI API（支持多种模型和新的API格式）
async function callAIAPI(messages, config = currentConfig, options = {}) {
  try {
    console.log(`🤖 调用${config.name}模型: ${config.model}`);

    const requestBody = {
      model: config.model,
      messages: messages,
      max_tokens: options.maxTokens || config.maxTokens,
      temperature: options.temperature || config.temperature
    };

    // 添加流式响应支持
    if (options.stream) {
      requestBody.stream = true;
    }

    // 添加工具调用支持
    if (options.tools && config.features.toolCalls) {
      requestBody.tools = options.tools;
      requestBody.tool_choice = options.toolChoice || 'auto';
    }

    // 解除超时限制，支持复杂任务的深度思考
    // 通过前端执行流程展示来让用户了解进展
    let timeout = 0; // 解除超时限制

    // 使用优化的HTTP客户端
    const response = await httpClient.aiRequest(
      `${config.baseURL}/chat/completions`,
      requestBody,
      {
        headers: {
          'Authorization': `Bearer ${config.apiKey}`
        },
        timeout: timeout,
        responseType: options.stream ? 'stream' : 'json'
      }
    );

    if (options.stream) {
      return response; // 返回流式响应
    }

    if (response.data && response.data.choices && response.data.choices[0]) {
      console.log(`✅ ${config.name}响应成功`);

      const choice = response.data.choices[0];
      const result = {
        content: choice.message.content,
        usage: response.data.usage,
        finishReason: choice.finish_reason
      };

      // 处理工具调用响应
      if (choice.message.tool_calls) {
        result.toolCalls = choice.message.tool_calls;
      }

      return result;
    } else {
      throw new Error(`${config.name} API返回格式异常`);
    }

  } catch (error) {
    console.error(`❌ ${config.name} API调用失败:`, {
      message: error.message,
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data,
      url: `${config.baseURL}/chat/completions`,
      model: config.model
    });

    // 如果API调用失败，返回备用响应
    return {
      content: getFallbackResponse(messages[messages.length - 1].content, config.name),
      usage: { total_tokens: 0 },
      error: true,
      error_details: {
        provider: config.name,
        model: config.model,
        error_type: error.response?.status === 405 ? 'API_BLOCKED' : 'API_ERROR',
        message: error.message
      }
    };
  }
}

// 备用响应（当API不可用时）
function getFallbackResponse(userMessage, providerName = 'AI') {
  const fallbackResponses = {
    quality: `关于质量管理的问题，我建议您参考ISO 9001标准或咨询质量管理专家。（${providerName}服务暂时不可用）`,
    defect: `对于缺陷分析，建议使用8D方法或鱼骨图进行系统性分析。（${providerName}服务暂时不可用）`,
    improvement: `质量改进可以采用PDCA循环、6 Sigma或精益生产方法。（${providerName}服务暂时不可用）`,
    standard: `质量标准相关问题建议查阅最新的国际标准文件或行业规范。（${providerName}服务暂时不可用）`,
    default: `抱歉，${providerName}服务暂时无法处理您的问题。请稍后再试，或联系技术支持。`
  };

  const message = userMessage.toLowerCase();

  if (message.includes('质量') || message.includes('quality')) {
    return fallbackResponses.quality;
  } else if (message.includes('缺陷') || message.includes('问题')) {
    return fallbackResponses.defect;
  } else if (message.includes('改进') || message.includes('优化')) {
    return fallbackResponses.improvement;
  } else if (message.includes('标准') || message.includes('规范')) {
    return fallbackResponses.standard;
  } else {
    return fallbackResponses.default;
  }
}

// 图片解析聊天端点
app.post('/api/chat/vision', async (req, res) => {
  const startTime = Date.now();

  try {
    const { message, image_url, image_data_url, image_base64, image_mime = 'image/png', conversation_id, model } = req.body;

    if (!message) {
      return res.status(400).json({ success: false, message: '消息内容不能为空' });
    }

    // 兼容三种图片输入：image_url(http/https或data:)、image_data_url(data:开头)、image_base64(裸的base64)
    let finalImageUrl = null;
    if (typeof image_data_url === 'string' && image_data_url.startsWith('data:')) {
      finalImageUrl = image_data_url;
    } else if (typeof image_base64 === 'string' && image_base64.length > 0) {
      finalImageUrl = `data:${image_mime};base64,${image_base64}`;
    } else if (typeof image_url === 'string' && image_url.length > 0) {
      finalImageUrl = image_url;
    }

    if (!finalImageUrl) {
      return res.status(400).json({ success: false, message: '缺少有效的图片数据，请提供 image_url 或 image_base64/image_data_url' });
    }

    // 选择支持视觉的模型
    const modelConfig = model ? AI_CONFIGS[model] : AI_CONFIGS['gpt-4o'];

    if (!modelConfig.features.vision) {
      return res.status(400).json({
        success: false,
        message: `模型 ${modelConfig.name} 不支持图片解析功能`
      });
    }

    // 构建包含图片的消息
    const messages = [
      {
        role: 'system',
        content: QMS_KNOWLEDGE_BASE.systemPrompt
      },
      {
        role: 'user',
        content: [
          { type: 'text', text: message },
          { type: 'image_url', image_url: { url: finalImageUrl } }
        ]
      }
    ];

    // 调用AI API
    const aiResponse = await callAIAPI(messages, modelConfig, { maxTokens: 4096 });

    const responseTime = Date.now() - startTime;

    // 保存对话记录
    const chatRecord = {
      id: generateMessageId(),
      conversation_id: conversation_id || generateConversationId(),
      user_message: message,
      image_url: image_url,
      ai_response: aiResponse.content,
      timestamp: new Date().toISOString(),
      response_time: responseTime,
      model_info: {
        provider: modelConfig.name,
        model: modelConfig.model,
        tokens_used: aiResponse.usage || {},
        temperature: modelConfig.temperature
      }
    };

    chatHistory.push(chatRecord);

    console.log(`✅ 图片解析响应成功，耗时: ${responseTime}ms`);

    res.json({
      success: true,
      data: {
        response: aiResponse.content,
        conversation_id: chatRecord.conversation_id,
        message_id: chatRecord.id,
        response_time: responseTime,
        model_info: chatRecord.model_info
      }
    });

  } catch (error) {
    console.error('❌ 图片解析服务错误:', error);

    res.status(500).json({
      success: false,
      message: '图片解析服务暂时不可用，请稍后再试',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined
    });
  }
});

// 流式聊天端点
app.post('/api/chat/stream', async (req, res) => {
  try {
    const { message, conversation_id, model } = req.body;

    if (!message) {
      return res.status(400).json({
        success: false,
        message: '消息内容不能为空'
      });
    }

    // 获取对话历史
    const history = conversation_id ?
      chatHistory.filter(chat => chat.conversation_id === conversation_id)
        .map(chat => [
          { role: 'user', content: chat.user_message },
          { role: 'assistant', content: chat.ai_response }
        ]).flat() : [];

    // 构建对话历史
    const messages = [
      {
        role: 'system',
        content: QMS_KNOWLEDGE_BASE.systemPrompt
      },
      ...history.slice(-10), // 保留最近10轮对话
      {
        role: 'user',
        content: message
      }
    ];

    // 获取模型配置
    const modelConfig = model ? AI_CONFIGS[model] : currentConfig;

    // 设置流式响应头
    res.setHeader('Content-Type', 'text/event-stream');
    res.setHeader('Cache-Control', 'no-cache');
    res.setHeader('Connection', 'keep-alive');
    res.setHeader('Access-Control-Allow-Origin', '*');

    // 调用AI API（流式）
    const streamResponse = await callAIAPI(messages, modelConfig, { stream: true });

    let fullResponse = '';
    const conversationId = conversation_id || generateConversationId();

    streamResponse.data.on('data', (chunk) => {
      const lines = chunk.toString().split('\n').filter(line => line.trim() !== '');

      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const data = line.slice(6);

          if (data === '[DONE]') {
            // 保存完整对话记录
            const chatRecord = {
              id: generateMessageId(),
              conversation_id: conversationId,
              user_message: message,
              ai_response: fullResponse,
              timestamp: new Date().toISOString(),
              model_info: {
                provider: modelConfig.name,
                model: modelConfig.model,
                temperature: modelConfig.temperature
              }
            };
            chatHistory.push(chatRecord);

            res.write(`data: [DONE]\n\n`);
            res.end();
            return;
          }

          try {
            const parsed = JSON.parse(data);
            if (parsed.choices && parsed.choices[0] && parsed.choices[0].delta) {
              const content = parsed.choices[0].delta.content;
              if (content) {
                fullResponse += content;
                res.write(`data: ${JSON.stringify({ content })}\n\n`);
              }
            }
          } catch (e) {
            // 忽略解析错误
          }
        }
      }
    });

    streamResponse.data.on('end', () => {
      if (!res.headersSent) {
        res.write(`data: [DONE]\n\n`);
        res.end();
      }
    });

    streamResponse.data.on('error', (error) => {
      console.error('流式响应错误:', error);
      if (!res.headersSent) {
        res.write(`data: ${JSON.stringify({ error: '流式响应中断' })}\n\n`);
        res.end();
      }
    });

  } catch (error) {
    console.error('❌ 流式聊天服务错误:', error);

    if (!res.headersSent) {
      res.status(500).json({
        success: false,
        message: '流式聊天服务暂时不可用，请稍后再试',
        error: process.env.NODE_ENV === 'development' ? error.message : undefined
      });
    }
  }
});

// 获取可用模型列表
app.get('/api/models', (req, res) => {
  try {
    const models = Object.keys(AI_CONFIGS).map(key => {
      const config = AI_CONFIGS[key];
      return {
        id: key,
        name: config.name,
        provider: config.provider,
        model: config.model,
        contextLength: config.contextLength,
        features: config.features,
        tags: config.tags || [],
        maxTokens: config.maxTokens,
        temperature: config.temperature,
        enabled: config.enabled !== false, // 默认为true，除非明确设置为false
        status: config.enabled !== false ? 'available' : 'unavailable'
      };
    });

    res.json({
      success: true,
      data: {
        models: models,
        current_model: Object.keys(AI_CONFIGS).find(key => AI_CONFIGS[key] === currentConfig),
        total_count: models.length
      }
    });
  } catch (error) {
    console.error('❌ 获取模型列表错误:', error);
    res.status(500).json({
      success: false,
      message: '获取模型列表失败',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined
    });
  }
});

// 切换默认模型
app.post('/api/models/switch', (req, res) => {
  try {
    const { model_id } = req.body;

    if (!model_id || !AI_CONFIGS[model_id]) {
      return res.status(400).json({
        success: false,
        message: '无效的模型ID'
      });
    }

    const oldModel = currentConfig.name;
    currentConfig = AI_CONFIGS[model_id];

    console.log(`🔄 模型切换: ${oldModel} -> ${currentConfig.name}`);

    res.json({
      success: true,
      message: `已切换到模型: ${currentConfig.name}`,
      data: {
        previous_model: oldModel,
        current_model: currentConfig.name,
        model_info: {
          provider: currentConfig.provider,
          model: currentConfig.model,
          features: currentConfig.features
        }
      }
    });
  } catch (error) {
    console.error('❌ 切换模型错误:', error);
    res.status(500).json({
      success: false,
      message: '切换模型失败',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined
    });
  }
});

// 工具调用聊天端点
app.post('/api/chat/tools', async (req, res) => {
  const startTime = Date.now();

  try {
    const { message, tools, tool_choice, conversation_id, model } = req.body;

    if (!message) {
      return res.status(400).json({
        success: false,
        message: '消息内容不能为空'
      });
    }

    if (!tools || !Array.isArray(tools)) {
      return res.status(400).json({
        success: false,
        message: '工具定义不能为空'
      });
    }

    // 获取模型配置
    const modelConfig = model ? AI_CONFIGS[model] : currentConfig;

    if (!modelConfig.features.toolCalls) {
      return res.status(400).json({
        success: false,
        message: `模型 ${modelConfig.name} 不支持工具调用功能`
      });
    }

    // 获取对话历史
    const history = conversation_id ?
      chatHistory.filter(chat => chat.conversation_id === conversation_id)
        .map(chat => [
          { role: 'user', content: chat.user_message },
          { role: 'assistant', content: chat.ai_response }
        ]).flat() : [];

    // 构建对话历史
    const messages = [
      {
        role: 'system',
        content: QMS_KNOWLEDGE_BASE.systemPrompt
      },
      ...history.slice(-10), // 保留最近10轮对话
      {
        role: 'user',
        content: message
      }
    ];

    // 调用AI API（带工具）
    const aiResponse = await callAIAPI(messages, modelConfig, {
      tools: tools,
      toolChoice: tool_choice
    });

    const responseTime = Date.now() - startTime;

    // 保存对话记录
    const chatRecord = {
      id: generateMessageId(),
      conversation_id: conversation_id || generateConversationId(),
      user_message: message,
      ai_response: aiResponse.content,
      tool_calls: aiResponse.toolCalls,
      timestamp: new Date().toISOString(),
      response_time: responseTime,
      model_info: {
        provider: modelConfig.name,
        model: modelConfig.model,
        tokens_used: aiResponse.usage || {},
        temperature: modelConfig.temperature
      }
    };

    chatHistory.push(chatRecord);

    console.log(`✅ 工具调用响应成功，耗时: ${responseTime}ms`);

    res.json({
      success: true,
      data: {
        response: aiResponse.content,
        tool_calls: aiResponse.toolCalls,
        conversation_id: chatRecord.conversation_id,
        message_id: chatRecord.id,
        response_time: responseTime,
        model_info: chatRecord.model_info
      }
    });

  } catch (error) {
    console.error('❌ 工具调用服务错误:', error);

    res.status(500).json({
      success: false,
      message: '工具调用服务暂时不可用，请稍后再试',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined
    });
  }
});

// 获取聊天历史
app.get('/api/chat/history', async (req, res) => {
  try {
    const { conversation_id, user_id = 'anonymous', limit = 50 } = req.query;

    let messages = [];

    if (conversation_id) {
      // 从数据库获取特定对话的消息
      const conversation = await chatHistoryDB.getConversationWithMessages(conversation_id, user_id);
      if (conversation && conversation.messages) {
        messages = conversation.messages;
      }
    } else {
      // 获取用户的所有对话历史
      const conversations = await chatHistoryDB.getUserConversations(user_id, { limit: parseInt(limit) });

      // 转换为消息格式
      for (const conv of conversations) {
        const convWithMessages = await chatHistoryDB.getConversationWithMessages(conv.id, user_id);
        if (convWithMessages && convWithMessages.messages) {
          messages = messages.concat(convWithMessages.messages.slice(0, 10));
        }
      }
    }

    // 转换为前端需要的格式
    const formattedMessages = messages.map(msg => ({
      id: msg.id,
      role: msg.message_type,
      content: msg.content,
      timestamp: msg.created_at,
      model_info: msg.model_info ? JSON.parse(msg.model_info) : null,
      response_time: msg.response_time,
      rating: msg.rating,
      feedback: msg.feedback
    }));

    res.json({
      success: true,
      data: formattedMessages,
      total: formattedMessages.length
    });
  } catch (error) {
    console.error('获取聊天历史失败:', error);

    // 降级到内存数据
    let history = chatHistory;

    if (req.query.conversation_id) {
      history = history.filter(record => record.conversation_id === req.query.conversation_id);
    }

    // 按时间倒序排列，取最新的记录
    history = history
      .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
      .slice(0, parseInt(req.query.limit || 50));

    // 转换为前端需要的格式
    const messages = [];
    history.reverse().forEach(record => {
      messages.push({
        role: 'user',
        content: record.user_message,
        timestamp: record.timestamp
      });
      messages.push({
        role: 'assistant',
        content: record.ai_response,
        timestamp: record.timestamp,
        model_info: record.model_info
      });
    });

    res.json({
      success: true,
      data: messages,
      source: 'memory'
    });
  }
});

// 获取推荐问题
app.get('/api/chat/recommended-questions', (req, res) => {
  const { category = 'all' } = req.query;
  
  let questions = [];
  
  if (category === 'all') {
    QMS_KNOWLEDGE_BASE.commonQuestions.forEach(cat => {
      questions.push(...cat.questions);
    });
  } else {
    const categoryData = QMS_KNOWLEDGE_BASE.commonQuestions.find(
      cat => cat.category === category
    );
    if (categoryData) {
      questions = categoryData.questions;
    }
  }
  
  res.json({
    success: true,
    data: questions.slice(0, 10) // 返回最多10个问题
  });
});

// 获取聊天统计
app.get('/api/chat/statistics', async (req, res) => {
  try {
    const { user_id = 'anonymous', days = 7 } = req.query;

    // 从数据库获取统计数据
    const dbStats = await chatHistoryDB.getStatistics(user_id, parseInt(days));

    // 合并内存统计和数据库统计
    const stats = {
      // 数据库统计
      totalConversations: dbStats.totalConversations || 0,
      totalMessages: dbStats.totalMessages || 0,
      averageResponseTime: dbStats.averageResponseTime || 0,

      // 模型使用统计
      modelUsage: dbStats.modelUsage || {},

      // 用户活跃度
      dailyActivity: dbStats.dailyActivity || [],

      // 质量评分
      averageRating: dbStats.averageRating || 0,
      ratingDistribution: dbStats.ratingDistribution || {},

      // 最近活动
      recentActivity: dbStats.recentActivity || [],

      // 内存统计（实时数据）
      memoryStats: {
        ...conversationStats,
        totalConversations: new Set(chatHistory.map(r => r.conversation_id)).size,
        recentActivity: chatHistory.slice(-10).map(record => ({
          timestamp: record.timestamp,
          response_time: record.response_time
        }))
      }
    };

    res.json({
      success: true,
      data: stats,
      generated_at: new Date().toISOString()
    });
  } catch (error) {
    console.error('获取统计数据失败:', error);

    // 降级到内存统计
    const stats = {
      ...conversationStats,
      totalConversations: new Set(chatHistory.map(r => r.conversation_id)).size,
      recentActivity: chatHistory.slice(-10).map(record => ({
        timestamp: record.timestamp,
        response_time: record.response_time
      })),
      source: 'memory'
    };

    res.json({
      success: true,
      data: stats
    });
  }
});

// 评价消息
app.post('/api/chat/rate', async (req, res) => {
  const { message_id, feedback, comment, feedback_score, feedback_reason } = req.body;

  const record = chatHistory.find(r => r.id === message_id);
  if (record) {
    record.feedback = feedback;
    record.feedback_comment = comment;
    record.feedback_time = new Date().toISOString();

    // 发送反馈监控数据（暂时禁用，避免404错误）
    // await sendFeedbackData(record, req, {
    //   feedbackType: feedback,
    //   feedbackScore: feedback_score,
    //   feedbackReason: feedback_reason,
    //   feedbackComment: comment
    // });
  }

  console.log(`📝 收到用户反馈: ${feedback} for message ${message_id}`);

  res.json({
    success: true,
    message: '感谢您的反馈'
  });
});

// 获取模型状态
app.get('/api/chat/model-status', async (req, res) => {
  try {
    // 测试当前AI API连接
    const testResponse = await httpClient.aiRequest(
      `${currentConfig.baseURL}/chat/completions`,
      {
        model: currentConfig.model,
        messages: [{ role: 'user', content: 'test' }],
        max_tokens: 10
      },
      {
        headers: {
          'Authorization': `Bearer ${currentConfig.apiKey}`
        },
        timeout: 5000
      }
    );

    res.json({
      success: true,
      data: {
        status: 'online',
        provider: currentConfig.name,
        model: currentConfig.model,
        api_status: 'connected',
        last_check: new Date().toISOString()
      }
    });

  } catch (error) {
    res.json({
      success: true,
      data: {
        status: 'offline',
        provider: currentConfig.name,
        model: currentConfig.model,
        api_status: 'disconnected',
        error: error.message,
        last_check: new Date().toISOString()
      }
    });
  }
});

// ==================== AI监控API ====================

// 获取总体统计
app.get('/api/ai-monitoring/statistics/overall', (req, res) => {
  try {
    const { startTime, endTime } = req.query;

    // 过滤时间范围内的数据
    let filteredHistory = chatHistory;
    if (startTime && endTime) {
      const start = new Date(startTime);
      const end = new Date(endTime);
      filteredHistory = chatHistory.filter(record => {
        const recordTime = new Date(record.timestamp);
        return recordTime >= start && recordTime <= end;
      });
    }

    // 计算统计数据
    const totalChats = filteredHistory.length;
    const uniqueUsers = new Set(filteredHistory.map(r => r.user_id || 'anonymous')).size;
    const successfulChats = filteredHistory.filter(r => !r.error).length;
    const totalLikes = filteredHistory.filter(r => r.feedback === 'like').length;
    const totalDislikes = filteredHistory.filter(r => r.feedback === 'dislike').length;
    const avgResponseTime = filteredHistory.length > 0
      ? Math.round(filteredHistory.reduce((sum, r) => sum + (r.response_time || 0), 0) / filteredHistory.length)
      : 0;

    res.json({
      success: true,
      data: {
        total_chats: totalChats,
        unique_users: uniqueUsers,
        successful_chats: successfulChats,
        total_likes: totalLikes,
        total_dislikes: totalDislikes,
        success_rate: totalChats > 0 ? Math.round((successfulChats / totalChats) * 100) : 0,
        avg_response_time: avgResponseTime,
        satisfaction_rate: (totalLikes + totalDislikes) > 0
          ? Math.round((totalLikes / (totalLikes + totalDislikes)) * 100)
          : 0
      }
    });
  } catch (error) {
    console.error('❌ 获取总体统计失败:', error);
    res.status(500).json({
      success: false,
      message: '获取统计数据失败',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined
    });
  }
});

// 获取用户统计
app.get('/api/ai-monitoring/statistics/users', (req, res) => {
  try {
    const { startTime, endTime } = req.query;

    // 过滤时间范围内的数据
    let filteredHistory = chatHistory;
    if (startTime && endTime) {
      const start = new Date(startTime);
      const end = new Date(endTime);
      filteredHistory = chatHistory.filter(record => {
        const recordTime = new Date(record.timestamp);
        return recordTime >= start && recordTime <= end;
      });
    }

    // 按用户统计
    const userStats = {};
    filteredHistory.forEach(record => {
      const userId = record.user_id || 'anonymous';
      if (!userStats[userId]) {
        userStats[userId] = {
          user_id: userId,
          total_chats: 0,
          successful_chats: 0,
          total_response_time: 0,
          likes: 0,
          dislikes: 0,
          last_activity: record.timestamp
        };
      }

      const stats = userStats[userId];
      stats.total_chats++;
      if (!record.error) stats.successful_chats++;
      stats.total_response_time += record.response_time || 0;
      if (record.feedback === 'like') stats.likes++;
      if (record.feedback === 'dislike') stats.dislikes++;

      // 更新最后活动时间
      if (new Date(record.timestamp) > new Date(stats.last_activity)) {
        stats.last_activity = record.timestamp;
      }
    });

    // 转换为数组并计算平均响应时间
    const userList = Object.values(userStats).map(stats => ({
      ...stats,
      avg_response_time: stats.total_chats > 0
        ? Math.round(stats.total_response_time / stats.total_chats)
        : 0,
      success_rate: stats.total_chats > 0
        ? Math.round((stats.successful_chats / stats.total_chats) * 100)
        : 0,
      satisfaction_rate: (stats.likes + stats.dislikes) > 0
        ? Math.round((stats.likes / (stats.likes + stats.dislikes)) * 100)
        : 0
    }));

    // 按总对话数排序
    userList.sort((a, b) => b.total_chats - a.total_chats);

    res.json({
      success: true,
      data: userList
    });
  } catch (error) {
    console.error('❌ 获取用户统计失败:', error);
    res.status(500).json({
      success: false,
      message: '获取用户统计失败',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined
    });
  }
});

// 获取模型统计
app.get('/api/ai-monitoring/statistics/models', (req, res) => {
  try {
    const { startTime, endTime } = req.query;

    // 过滤时间范围内的数据
    let filteredHistory = chatHistory;
    if (startTime && endTime) {
      const start = new Date(startTime);
      const end = new Date(endTime);
      filteredHistory = chatHistory.filter(record => {
        const recordTime = new Date(record.timestamp);
        return recordTime >= start && recordTime <= end;
      });
    }

    // 按模型统计
    const modelStats = {};
    filteredHistory.forEach(record => {
      const modelInfo = record.model_info || {};
      const modelKey = `${modelInfo.provider || 'Unknown'}_${modelInfo.model || 'Unknown'}`;

      if (!modelStats[modelKey]) {
        modelStats[modelKey] = {
          provider: modelInfo.provider || 'Unknown',
          model: modelInfo.model || 'Unknown',
          total_chats: 0,
          successful_chats: 0,
          total_response_time: 0,
          total_tokens: 0,
          likes: 0,
          dislikes: 0,
          last_used: record.timestamp
        };
      }

      const stats = modelStats[modelKey];
      stats.total_chats++;
      if (!record.error) stats.successful_chats++;
      stats.total_response_time += record.response_time || 0;
      stats.total_tokens += (modelInfo.tokens_used?.total_tokens || 0);
      if (record.feedback === 'like') stats.likes++;
      if (record.feedback === 'dislike') stats.dislikes++;

      // 更新最后使用时间
      if (new Date(record.timestamp) > new Date(stats.last_used)) {
        stats.last_used = record.timestamp;
      }
    });

    // 转换为数组并计算平均值
    const modelList = Object.values(modelStats).map(stats => ({
      ...stats,
      avg_response_time: stats.total_chats > 0
        ? Math.round(stats.total_response_time / stats.total_chats)
        : 0,
      avg_tokens_per_chat: stats.total_chats > 0
        ? Math.round(stats.total_tokens / stats.total_chats)
        : 0,
      success_rate: stats.total_chats > 0
        ? Math.round((stats.successful_chats / stats.total_chats) * 100)
        : 0,
      satisfaction_rate: (stats.likes + stats.dislikes) > 0
        ? Math.round((stats.likes / (stats.likes + stats.dislikes)) * 100)
        : 0
    }));

    // 按使用次数排序
    modelList.sort((a, b) => b.total_chats - a.total_chats);

    res.json({
      success: true,
      data: modelList
    });
  } catch (error) {
    console.error('❌ 获取模型统计失败:', error);
    res.status(500).json({
      success: false,
      message: '获取模型统计失败',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined
    });
  }
});

// 获取问答记录
app.get('/api/ai-monitoring/chat-records', (req, res) => {
  try {
    const {
      current = 1,
      size = 20,
      userId,
      modelProvider,
      chatStatus,
      startTime,
      endTime
    } = req.query;

    // 过滤数据
    let filteredHistory = chatHistory;

    // 时间范围过滤
    if (startTime && endTime) {
      const start = new Date(startTime);
      const end = new Date(endTime);
      filteredHistory = filteredHistory.filter(record => {
        const recordTime = new Date(record.timestamp);
        return recordTime >= start && recordTime <= end;
      });
    }

    // 用户过滤
    if (userId) {
      filteredHistory = filteredHistory.filter(record =>
        (record.user_id || 'anonymous').includes(userId)
      );
    }

    // 模型提供商过滤
    if (modelProvider) {
      filteredHistory = filteredHistory.filter(record =>
        record.model_info?.provider === modelProvider
      );
    }

    // 状态过滤
    if (chatStatus) {
      if (chatStatus === 'success') {
        filteredHistory = filteredHistory.filter(record => !record.error);
      } else if (chatStatus === 'error') {
        filteredHistory = filteredHistory.filter(record => record.error);
      }
    }

    // 排序（最新的在前）
    filteredHistory.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));

    // 分页
    const total = filteredHistory.length;
    const startIndex = (parseInt(current) - 1) * parseInt(size);
    const endIndex = startIndex + parseInt(size);
    const records = filteredHistory.slice(startIndex, endIndex);

    // 格式化数据
    const formattedRecords = records.map(record => ({
      id: record.id,
      conversation_id: record.conversation_id,
      user_id: record.user_id || 'anonymous',
      user_message: record.user_message,
      ai_response: record.ai_response,
      timestamp: record.timestamp,
      response_time: record.response_time,
      model_provider: record.model_info?.provider || 'Unknown',
      model_name: record.model_info?.model || 'Unknown',
      tokens_used: record.model_info?.tokens_used?.total_tokens || 0,
      feedback: record.feedback || null,
      feedback_comment: record.feedback_comment || null,
      status: record.error ? 'error' : 'success',
      error_message: record.error || null
    }));

    res.json({
      success: true,
      data: {
        records: formattedRecords,
        total,
        current: parseInt(current),
        size: parseInt(size),
        pages: Math.ceil(total / parseInt(size))
      }
    });
  } catch (error) {
    console.error('❌ 获取问答记录失败:', error);
    res.status(500).json({
      success: false,
      message: '获取问答记录失败',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined
    });
  }
});

// 获取反馈统计
app.get('/api/ai-monitoring/statistics/feedback', (req, res) => {
  try {
    const { startTime, endTime } = req.query;

    // 过滤时间范围内的数据
    let filteredHistory = chatHistory;
    if (startTime && endTime) {
      const start = new Date(startTime);
      const end = new Date(endTime);
      filteredHistory = chatHistory.filter(record => {
        const recordTime = new Date(record.timestamp);
        return recordTime >= start && recordTime <= end;
      });
    }

    // 统计反馈数据
    const feedbackStats = {
      total_feedback: 0,
      likes: 0,
      dislikes: 0,
      satisfaction_rate: 0,
      feedback_rate: 0
    };

    const totalChats = filteredHistory.length;
    const feedbackRecords = filteredHistory.filter(record => record.feedback);

    feedbackStats.total_feedback = feedbackRecords.length;
    feedbackStats.likes = filteredHistory.filter(r => r.feedback === 'like').length;
    feedbackStats.dislikes = filteredHistory.filter(r => r.feedback === 'dislike').length;
    feedbackStats.feedback_rate = totalChats > 0
      ? Math.round((feedbackStats.total_feedback / totalChats) * 100)
      : 0;
    feedbackStats.satisfaction_rate = feedbackStats.total_feedback > 0
      ? Math.round((feedbackStats.likes / feedbackStats.total_feedback) * 100)
      : 0;

    res.json({
      success: true,
      data: feedbackStats
    });
  } catch (error) {
    console.error('❌ 获取反馈统计失败:', error);
    res.status(500).json({
      success: false,
      message: '获取反馈统计失败',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined
    });
  }
});

// 获取反馈记录
app.get('/api/ai-monitoring/feedback-records', (req, res) => {
  try {
    const {
      current = 1,
      size = 20,
      feedbackType,
      userId,
      startTime,
      endTime
    } = req.query;

    // 过滤有反馈的记录
    let filteredHistory = chatHistory.filter(record => record.feedback);

    // 时间范围过滤
    if (startTime && endTime) {
      const start = new Date(startTime);
      const end = new Date(endTime);
      filteredHistory = filteredHistory.filter(record => {
        const recordTime = new Date(record.timestamp);
        return recordTime >= start && recordTime <= end;
      });
    }

    // 反馈类型过滤
    if (feedbackType) {
      filteredHistory = filteredHistory.filter(record =>
        record.feedback === feedbackType
      );
    }

    // 用户过滤
    if (userId) {
      filteredHistory = filteredHistory.filter(record =>
        (record.user_id || 'anonymous').includes(userId)
      );
    }

    // 排序（最新的在前）
    filteredHistory.sort((a, b) => new Date(b.feedback_time || b.timestamp) - new Date(a.feedback_time || a.timestamp));

    // 分页
    const total = filteredHistory.length;
    const startIndex = (parseInt(current) - 1) * parseInt(size);
    const endIndex = startIndex + parseInt(size);
    const records = filteredHistory.slice(startIndex, endIndex);

    // 格式化数据
    const formattedRecords = records.map(record => ({
      id: record.id,
      conversation_id: record.conversation_id,
      user_id: record.user_id || 'anonymous',
      user_message: record.user_message.substring(0, 100) + (record.user_message.length > 100 ? '...' : ''),
      ai_response: record.ai_response.substring(0, 200) + (record.ai_response.length > 200 ? '...' : ''),
      feedback_type: record.feedback,
      feedback_comment: record.feedback_comment || '',
      feedback_time: record.feedback_time || record.timestamp,
      response_time: record.response_time,
      model_provider: record.model_info?.provider || 'Unknown',
      model_name: record.model_info?.model || 'Unknown'
    }));

    res.json({
      success: true,
      data: {
        records: formattedRecords,
        total,
        current: parseInt(current),
        size: parseInt(size),
        pages: Math.ceil(total / parseInt(size))
      }
    });
  } catch (error) {
    console.error('❌ 获取反馈记录失败:', error);
    res.status(500).json({
      success: false,
      message: '获取反馈记录失败',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined
    });
  }
});

// 获取仪表板数据
app.get('/api/ai-monitoring/dashboard', (req, res) => {
  try {
    const { startTime, endTime } = req.query;

    // 过滤时间范围内的数据
    let filteredHistory = chatHistory;
    if (startTime && endTime) {
      const start = new Date(startTime);
      const end = new Date(endTime);
      filteredHistory = chatHistory.filter(record => {
        const recordTime = new Date(record.timestamp);
        return recordTime >= start && recordTime <= end;
      });
    }

    // 基础统计
    const totalChats = filteredHistory.length;
    const uniqueUsers = new Set(filteredHistory.map(r => r.user_id || 'anonymous')).size;
    const successfulChats = filteredHistory.filter(r => !r.error).length;
    const totalLikes = filteredHistory.filter(r => r.feedback === 'like').length;
    const totalDislikes = filteredHistory.filter(r => r.feedback === 'dislike').length;

    // 使用趋势（按天统计）
    const usageTrend = {};
    filteredHistory.forEach(record => {
      const date = new Date(record.timestamp).toISOString().split('T')[0];
      usageTrend[date] = (usageTrend[date] || 0) + 1;
    });

    // 模型分布
    const modelDistribution = {};
    filteredHistory.forEach(record => {
      const model = record.model_info?.model || 'Unknown';
      modelDistribution[model] = (modelDistribution[model] || 0) + 1;
    });

    // 响应时间分布
    const responseTimeRanges = {
      '0-1s': 0,
      '1-3s': 0,
      '3-5s': 0,
      '5-10s': 0,
      '10s+': 0
    };

    filteredHistory.forEach(record => {
      const time = record.response_time || 0;
      if (time < 1000) responseTimeRanges['0-1s']++;
      else if (time < 3000) responseTimeRanges['1-3s']++;
      else if (time < 5000) responseTimeRanges['3-5s']++;
      else if (time < 10000) responseTimeRanges['5-10s']++;
      else responseTimeRanges['10s+']++;
    });

    res.json({
      success: true,
      data: {
        overview: {
          total_chats: totalChats,
          unique_users: uniqueUsers,
          success_rate: totalChats > 0 ? Math.round((successfulChats / totalChats) * 100) : 0,
          satisfaction_rate: (totalLikes + totalDislikes) > 0
            ? Math.round((totalLikes / (totalLikes + totalDislikes)) * 100)
            : 0
        },
        usage_trend: Object.entries(usageTrend).map(([date, count]) => ({
          date,
          count
        })).sort((a, b) => a.date.localeCompare(b.date)),
        model_distribution: Object.entries(modelDistribution).map(([model, count]) => ({
          model,
          count,
          percentage: Math.round((count / totalChats) * 100)
        })),
        response_time_distribution: Object.entries(responseTimeRanges).map(([range, count]) => ({
          range,
          count,
          percentage: totalChats > 0 ? Math.round((count / totalChats) * 100) : 0
        }))
      }
    });
  } catch (error) {
    console.error('❌ 获取仪表板数据失败:', error);
    res.status(500).json({
      success: false,
      message: '获取仪表板数据失败',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined
    });
  }
});

// 获取实时监控指标
app.get('/api/ai-monitoring/realtime-metrics', (req, res) => {
  try {
    // 最近1小时的数据
    const oneHourAgo = new Date(Date.now() - 60 * 60 * 1000);
    const recentHistory = chatHistory.filter(record =>
      new Date(record.timestamp) >= oneHourAgo
    );

    // 最近5分钟的数据
    const fiveMinutesAgo = new Date(Date.now() - 5 * 60 * 1000);
    const veryRecentHistory = chatHistory.filter(record =>
      new Date(record.timestamp) >= fiveMinutesAgo
    );

    const metrics = {
      current_qps: Math.round(veryRecentHistory.length / 5), // 每秒查询数
      active_users: new Set(recentHistory.map(r => r.user_id || 'anonymous')).size,
      avg_response_time: recentHistory.length > 0
        ? Math.round(recentHistory.reduce((sum, r) => sum + (r.response_time || 0), 0) / recentHistory.length)
        : 0,
      error_rate: recentHistory.length > 0
        ? Math.round((recentHistory.filter(r => r.error).length / recentHistory.length) * 100)
        : 0,
      current_model: currentConfig.model,
      model_status: 'online', // 简化处理
      timestamp: new Date().toISOString()
    };

    res.json({
      success: true,
      data: metrics
    });
  } catch (error) {
    console.error('❌ 获取实时指标失败:', error);
    res.status(500).json({
      success: false,
      message: '获取实时指标失败',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined
    });
  }
});

// 切换AI模型
app.post('/api/chat/switch-model', (req, res) => {
  const { model, config, provider } = req.body;

  try {
    // 更新当前配置
    if (provider && AI_CONFIGS[provider]) {
      currentConfig = AI_CONFIGS[provider];
      console.log(`🔄 模型切换成功: ${currentConfig.name} - ${currentConfig.model}`);
    } else if (config) {
      if (config.provider && AI_CONFIGS[config.provider]) {
        currentConfig = { ...AI_CONFIGS[config.provider], ...config };
      } else {
        currentConfig = { ...currentConfig, ...config };
      }
      console.log(`🔄 模型切换成功: ${currentConfig.name} - ${currentConfig.model}`);
    } else if (model) {
      // 根据模型名称查找对应的配置
      const configKey = Object.keys(AI_CONFIGS).find(key =>
        AI_CONFIGS[key].model === model
      );
      if (configKey) {
        currentConfig = AI_CONFIGS[configKey];
        console.log(`🔄 模型切换成功: ${currentConfig.name} - ${currentConfig.model}`);
      }
    }

    res.json({
      success: true,
      message: '模型切换成功',
      data: {
        current_model: currentConfig.model,
        provider: currentConfig.name
      }
    });
  } catch (error) {
    console.error('模型切换失败:', error);
    res.status(500).json({
      success: false,
      message: '模型切换失败'
    });
  }
});

// 获取可用模型列表
app.get('/api/chat/models', (req, res) => {
  const models = Object.keys(AI_CONFIGS).map(key => ({
    provider: key,
    name: AI_CONFIGS[key].name,
    model: AI_CONFIGS[key].model,
    baseURL: AI_CONFIGS[key].baseURL,
    features: AI_CONFIGS[key].features || {},
    tags: AI_CONFIGS[key].tags || [],
    contextLength: AI_CONFIGS[key].contextLength,
    maxTokens: AI_CONFIGS[key].maxTokens,
    temperature: AI_CONFIGS[key].temperature,
    enabled: AI_CONFIGS[key].enabled !== false, // 默认为true，除非明确设置为false
    status: AI_CONFIGS[key].enabled !== false ? 'available' : 'unavailable'
  }));

  res.json({
    success: true,
    data: {
      models: models,
      current: {
        provider: currentConfig.name,
        model: currentConfig.model
      }
    }
  });
});

// 获取Python SDK调用示例
app.get('/api/chat/python-examples', (req, res) => {
  const examples = {
    "普通请求": {
      "description": "基础文本生成请求",
      "code": `from openai import OpenAI
client = OpenAI(
    api_key="sk_a264854a38dc034077c23f5951285907e6c40d1b4843f46f95e5f31",
    base_url="https://hk-intra-paas.transsion.com/tranai-proxy/v1"
)

completion = client.chat.completions.create(
  model="gemini-2.5-pro-thinking",
  messages=[
    {"role": "system", "content": "你是QMS质量管理专家。"},
    {"role": "user", "content": "请解释ISO 9001质量管理体系的核心要素。"}
  ]
)

print(completion.choices[0].message.content)`,
      "use_case": "质量管理咨询、标准解读、问题解答"
    },

    "图片分析": {
      "description": "产品质量图像分析",
      "code": `from openai import OpenAI
client = OpenAI(
    api_key="sk_a264854a38dc034077c23f5951285907e6c40d1b4843f46f95e5f31",
    base_url="https://hk-intra-paas.transsion.com/tranai-proxy/v1"
)

response = client.chat.completions.create(
    model="gpt-4o",
    messages=[
        {
            "role": "user",
            "content": [
                {
                    "type": "text",
                    "text": "请分析这张产品图片的质量问题，重点检查外观缺陷、尺寸偏差和装配问题。"
                },
                {
                    "type": "image_url",
                    "image_url": {
                        "url": "https://your-product-image-url.jpg",
                    }
                },
            ],
        }
    ],
    max_tokens=500,
)

print(response.choices[0].message.content)`,
      "use_case": "缺陷检测、外观检查、质量评估"
    },

    "流式回复": {
      "description": "实时质量分析报告生成",
      "code": `from openai import OpenAI
client = OpenAI(
    api_key="sk_a264854a38dc034077c23f5951285907e6c40d1b4843f46f95e5f31",
    base_url="https://hk-intra-paas.transsion.com/tranai-proxy/v1"
)

completion = client.chat.completions.create(
  model="gemini-2.5-pro-thinking",
  messages=[
    {"role": "system", "content": "你是质量工程师，请详细分析质量数据。"},
    {"role": "user", "content": "我们的生产线Cpk值为0.8，请分析原因并提供改进方案。"}
  ],
  stream=True
)

for chunk in completion:
    if chunk.choices[0].delta.content:
        print(chunk.choices[0].delta.content, end="")`,
      "use_case": "实时分析、长篇报告、交互式咨询"
    },

    "工具调用": {
      "description": "质量数据分析工具调用",
      "code": `from openai import OpenAI
client = OpenAI(
    api_key="sk_a264854a38dc034077c23f5951285907e6c40d1b4843f46f95e5f31",
    base_url="https://hk-intra-paas.transsion.com/tranai-proxy/v1"
)

tools = [
  {
    "type": "function",
    "function": {
      "name": "analyze_quality_data",
      "description": "分析质量数据，计算SPC控制限和过程能力",
      "parameters": {
        "type": "object",
        "properties": {
          "data": {
            "type": "array",
            "description": "质量数据数组",
            "items": {"type": "number"}
          },
          "analysis_type": {
            "type": "string",
            "enum": ["spc", "capability", "trend"],
            "description": "分析类型"
          },
          "usl": {"type": "number", "description": "上规格限"},
          "lsl": {"type": "number", "description": "下规格限"}
        },
        "required": ["data", "analysis_type"],
      },
    }
  }
]

messages = [{"role": "user", "content": "请分析这组质量数据的过程能力"}]
completion = client.chat.completions.create(
  model="gpt-4o",
  messages=messages,
  tools=tools,
  tool_choice="auto"
)

print(completion)`,
      "use_case": "SPC分析、过程能力评估、统计分析"
    }
  };

  res.json({
    success: true,
    data: {
      examples: examples,
      supported_models: [
        {
          name: "gpt-4o",
          capabilities: ["文本生成", "图像分析", "工具调用"],
          best_for: "复杂分析、图像识别、多模态任务"
        },
        {
          name: "gpt-4o-mini",
          capabilities: ["文本生成", "工具调用"],
          best_for: "快速问答、简单分析"
        },
        {
          name: "gemini-2.5-pro-thinking",
          capabilities: ["文本生成", "推理分析", "工具调用"],
          best_for: "复杂推理、质量诊断、根因分析"
        },
        {
          name: "claude-3-5-sonnet-20241022",
          capabilities: ["文本生成", "详细分析"],
          best_for: "标准解读、详细报告、流程分析"
        }
      ],
      api_info: {
        base_url: "https://hk-intra-paas.transsion.com/tranai-proxy/v1",
        api_key: "sk_a264854a38dc034077c23f5951285907e6c40d1b4843f46f95e5f31",
        documentation: "兼容OpenAI API格式",
        rate_limits: "请参考平台文档"
      }
    }
  });
});

// 获取模型详细信息
app.get('/api/models/:modelId', (req, res) => {
  try {
    const { modelId } = req.params;
    const model = AI_CONFIGS[modelId];

    if (!model) {
      return res.status(404).json({
        success: false,
        message: '模型不存在'
      });
    }

    res.json({
      success: true,
      data: {
        id: modelId,
        name: model.name,
        provider: model.provider,
        model: model.model,
        contextLength: model.contextLength,
        features: model.features,
        maxTokens: model.maxTokens,
        temperature: model.temperature,
        baseURL: model.baseURL
      }
    });
  } catch (error) {
    console.error('❌ 获取模型详情错误:', error);
    res.status(500).json({
      success: false,
      message: '获取模型详情失败',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined
    });
  }
});

// 检查模型健康状态
app.get('/api/models/:modelId/health', async (req, res) => {
  try {
    const { modelId } = req.params;
    const model = AI_CONFIGS[modelId];

    if (!model) {
      return res.status(404).json({
        success: false,
        message: '模型不存在'
      });
    }

    // 发送简单的健康检查消息
    const healthCheckMessage = [
      {
        role: 'user',
        content: 'Hello'
      }
    ];

    const startTime = Date.now();
    const response = await callAIAPI(healthCheckMessage, model);
    const responseTime = Date.now() - startTime;

    res.json({
      success: true,
      data: {
        model_id: modelId,
        status: response.error ? 'unhealthy' : 'healthy',
        response_time: responseTime,
        error: response.error_details || null,
        timestamp: new Date().toISOString()
      }
    });
  } catch (error) {
    console.error('❌ 模型健康检查错误:', error);
    res.status(500).json({
      success: false,
      message: '模型健康检查失败',
      data: {
        model_id: req.params.modelId,
        status: 'unhealthy',
        error: error.message,
        timestamp: new Date().toISOString()
      }
    });
  }
});

// 工具函数 - 优化ID生成机制
let messageIdCounter = 0;
let conversationIdCounter = 0;

function generateMessageId() {
  messageIdCounter++;
  const timestamp = Date.now();
  const random = Math.random().toString(36).substr(2, 9);
  const counter = messageIdCounter.toString(36).padStart(3, '0');
  return `msg_${timestamp}_${counter}_${random}`;
}

function generateConversationId() {
  conversationIdCounter++;
  const timestamp = Date.now();
  const random = Math.random().toString(36).substr(2, 9);
  const counter = conversationIdCounter.toString(36).padStart(3, '0');
  return `conv_${timestamp}_${counter}_${random}`;
}

// 通用错误处理
app.use((error, req, res, next) => {
  console.error('服务器错误:', error);
  res.status(500).json({
    success: false,
    message: '服务器内部错误',
    error: process.env.NODE_ENV === 'development' ? error.message : undefined
  });
});



// 发送监控数据到后端
async function sendMonitoringData(chatRecord, req) {
  try {
    const monitoringData = {
      bid: generateMessageId(),
      conversationId: chatRecord.conversation_id,
      messageId: chatRecord.id,
      userId: req.headers['user-id'] || 'anonymous',
      userName: req.headers['user-name'] || '匿名用户',
      userMessage: chatRecord.user_message,
      aiResponse: chatRecord.ai_response,
      modelProvider: chatRecord.model_info.provider,
      modelName: chatRecord.model_info.model,
      responseTime: chatRecord.response_time,
      tokenUsage: JSON.stringify(chatRecord.model_info.tokens_used),
      temperature: chatRecord.model_info.temperature,
      chatStatus: 'SUCCESS',
      sessionId: req.sessionID || generateConversationId(),
      ipAddress: req.ip || req.connection.remoteAddress,
      userAgent: req.headers['user-agent'],
      createdTime: new Date().toISOString(),
      createdBy: req.headers['user-id'] || 'system'
    };

    // 发送到Spring Boot后端
    const response = await fetch('http://localhost:8080/api/ai-monitoring/chat-record', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': req.headers['authorization'] || ''
      },
      body: JSON.stringify(monitoringData)
    });

    if (!response.ok) {
      console.warn('发送监控数据失败:', response.status, response.statusText);
    } else {
      console.log('✅ 监控数据发送成功:', chatRecord.id);
    }
  } catch (error) {
    console.error('发送监控数据异常:', error.message);
  }
}

// 发送反馈数据到后端
async function sendFeedbackData(chatRecord, req, feedbackData) {
  try {
    const monitoringData = {
      bid: generateMessageId(),
      messageId: chatRecord.id,
      userId: req.headers['user-id'] || 'anonymous',
      feedbackType: feedbackData.feedbackType?.toUpperCase() === 'LIKE' ? 'LIKE' : 'DISLIKE',
      feedbackScore: feedbackData.feedbackScore || null,
      feedbackReason: feedbackData.feedbackReason || null,
      feedbackComment: feedbackData.feedbackComment || null,
      isHelpful: feedbackData.feedbackType?.toUpperCase() === 'LIKE' ? 1 : 0,
      createdTime: new Date().toISOString(),
      createdBy: req.headers['user-id'] || 'system'
    };

    // 发送到Spring Boot后端
    const response = await fetch('http://localhost:8080/api/ai-monitoring/feedback', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': req.headers['authorization'] || ''
      },
      body: JSON.stringify(monitoringData)
    });

    if (!response.ok) {
      console.warn('发送反馈数据失败:', response.status, response.statusText);
    } else {
      console.log('✅ 反馈数据发送成功:', chatRecord.id);
    }
  } catch (error) {
    console.error('发送反馈数据异常:', error.message);
  }
}

// ==================== 对话记录管理API ====================

// 获取用户对话列表（支持匿名用户）
app.get('/api/chat/conversations', async (req, res) => {
  try {
    // 尝试从JWT获取用户ID，如果没有则使用匿名用户
    let userId = 'anonymous';
    let userRole = 'ANONYMOUS';
    const authHeader = req.headers.authorization;

    if (authHeader) {
      const decoded = JWTManager.verifyToken(authHeader);
      if (decoded) {
        userId = decoded.userId || decoded.id;
        userRole = decoded.role || 'USER';
      }
    }

    // 如果没有JWT，尝试从header获取用户ID
    if (userId === 'anonymous' && req.headers['user-id']) {
      userId = req.headers['user-id'];
    }

    const { limit = 20, offset = 0, model_provider } = req.query;

    // 注释掉匿名用户限制，允许匿名用户查看对话历史
    // 匿名用户也可以有对话记录，应该正常返回
    // if (userRole === 'ANONYMOUS' || userId === 'anonymous') {
    //   return res.json({
    //     success: true,
    //     data: {
    //       conversations: [],
    //       total: 0,
    //       limit: parseInt(limit),
    //       offset: parseInt(offset)
    //     },
    //     message: '匿名用户无对话历史'
    //   });
    // }

    const conversations = await chatHistoryDB.getUserConversations(userId, {
      limit: parseInt(limit),
      offset: parseInt(offset),
      model_provider
    });

    res.json({
      success: true,
      data: {
        conversations,
        total: conversations.length,
        limit: parseInt(limit),
        offset: parseInt(offset),
        userId: userId // 返回用户ID用于前端验证
      }
    });

    console.log(`✅ 获取用户 ${userId} 的对话列表: ${conversations.length} 条`);
  } catch (error) {
    console.error('❌ 获取对话列表失败:', error);
    res.status(500).json({
      success: false,
      message: '获取对话列表失败',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined
    });
  }
});

// 获取对话详情和消息（支持匿名用户）
app.get('/api/chat/conversations/:conversationId', async (req, res) => {
  try {
    const { conversationId } = req.params;

    // 尝试从JWT获取用户ID，如果没有则使用匿名用户
    let userId = 'anonymous';
    const authHeader = req.headers.authorization;

    if (authHeader) {
      const decoded = JWTManager.verifyToken(authHeader);
      if (decoded) {
        userId = decoded.userId || decoded.id;
      }
    }

    // 如果没有JWT，尝试从header获取用户ID
    if (userId === 'anonymous' && req.headers['user-id']) {
      userId = req.headers['user-id'];
    }

    const conversation = await chatHistoryDB.getConversationWithMessages(conversationId, userId);

    if (!conversation) {
      return res.status(404).json({
        success: false,
        message: '对话不存在或无权访问'
      });
    }

    // 验证数据所有权
    if (conversation.user_id !== userId) {
      return res.status(403).json({
        success: false,
        message: '无权访问其他用户的对话'
      });
    }

    res.json({
      success: true,
      data: conversation
    });

    console.log(`✅ 获取用户 ${userId} 的对话详情: ${conversationId}`);
  } catch (error) {
    console.error('❌ 获取对话详情失败:', error);
    res.status(500).json({
      success: false,
      message: '获取对话详情失败',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined
    });
  }
});

// 删除对话
app.delete('/api/chat/conversations/:conversationId', authenticateUser, async (req, res) => {
  try {
    const { conversationId } = req.params;
    const userId = req.user.id;

    const deleted = await chatHistoryDB.deleteConversation(conversationId, userId);

    if (!deleted) {
      return res.status(404).json({
        success: false,
        message: '对话不存在或无权删除'
      });
    }

    res.json({
      success: true,
      message: '对话删除成功'
    });
  } catch (error) {
    console.error('❌ 删除对话失败:', error);
    res.status(500).json({
      success: false,
      message: '删除对话失败',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined
    });
  }
});

// 获取用户统计信息（支持匿名用户）
app.get('/api/chat/stats', async (req, res) => {
  try {
    // 尝试从JWT获取用户ID，如果没有则使用匿名用户
    let userId = 'anonymous';
    const authHeader = req.headers.authorization;

    if (authHeader) {
      const decoded = JWTManager.verifyToken(authHeader);
      if (decoded) {
        userId = decoded.userId || decoded.id;
      }
    }

    // 如果没有JWT，尝试从header获取用户ID
    if (userId === 'anonymous' && req.headers['user-id']) {
      userId = req.headers['user-id'];
    }

    const stats = await chatHistoryDB.getUserStats(userId);

    res.json({
      success: true,
      data: stats
    });
  } catch (error) {
    console.error('❌ 获取用户统计失败:', error);
    res.status(500).json({
      success: false,
      message: '获取用户统计失败',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined
    });
  }
});

// 对消息进行评分
app.post('/api/chat/messages/:messageId/rate', authenticateUser, async (req, res) => {
  try {
    const { messageId } = req.params;
    const { rating, feedback } = req.body;
    const userId = req.user.id;

    // 验证评分范围
    if (!rating || rating < 1 || rating > 5) {
      return res.status(400).json({
        success: false,
        message: '评分必须在1-5之间'
      });
    }

    console.log(`⭐ 用户 ${req.user.username} 对消息 ${messageId} 评分: ${rating}星`);

    const success = await chatHistoryDB.rateMessage(messageId, userId, rating, feedback);

    if (success) {
      res.json({
        success: true,
        message: '评分成功',
        data: {
          message_id: messageId,
          rating: rating,
          feedback: feedback
        }
      });
    } else {
      res.status(404).json({
        success: false,
        message: '消息不存在或无权评分'
      });
    }
  } catch (error) {
    console.error('❌ 消息评分失败:', error);
    res.status(500).json({
      success: false,
      message: '评分失败',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined
    });
  }
});

// 获取评分统计
app.get('/api/chat/ratings/stats', authenticateUser, async (req, res) => {
  try {
    const userId = req.user.id;
    const { model_provider, date_range } = req.query;

    const stats = await chatHistoryDB.getRatingStats(userId, {
      model_provider,
      date_range
    });

    res.json({
      success: true,
      data: stats
    });
  } catch (error) {
    console.error('❌ 获取评分统计失败:', error);
    res.status(500).json({
      success: false,
      message: '获取评分统计失败',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined
    });
  }
});

// 导出对话记录
app.post('/api/chat/export', authenticateUser, async (req, res) => {
  try {
    const userId = req.user.id;
    const { format, conversation_ids, options = {} } = req.body;

    // 验证导出格式
    const supportedFormats = ['pdf', 'word', 'excel', 'markdown', 'json'];
    if (!supportedFormats.includes(format)) {
      return res.status(400).json({
        success: false,
        message: `不支持的导出格式: ${format}。支持的格式: ${supportedFormats.join(', ')}`
      });
    }

    console.log(`📤 用户 ${req.user.username} 请求导出 ${format} 格式，对话数: ${conversation_ids?.length || '全部'}`);

    // 获取要导出的对话数据
    let conversations = [];
    if (conversation_ids && conversation_ids.length > 0) {
      // 导出指定对话
      for (const convId of conversation_ids) {
        const conv = await chatHistoryDB.getConversationWithMessages(convId, userId);
        if (conv) {
          conversations.push(conv);
        }
      }
    } else {
      // 导出所有对话
      const userConversations = await chatHistoryDB.getUserConversations(userId);
      for (const conv of userConversations) {
        const fullConv = await chatHistoryDB.getConversationWithMessages(conv.id, userId);
        if (fullConv) {
          conversations.push(fullConv);
        }
      }
    }

    if (conversations.length === 0) {
      return res.status(404).json({
        success: false,
        message: '没有找到要导出的对话'
      });
    }

    // 执行导出
    let result;
    const exportOptions = {
      ...options,
      user_info: req.user
    };

    switch (format) {
      case 'pdf':
        result = await exportService.exportToPDF(conversations, exportOptions);
        break;
      case 'word':
        result = await exportService.exportToWord(conversations, exportOptions);
        break;
      case 'excel':
        result = await exportService.exportToExcel(conversations, exportOptions);
        break;
      case 'markdown':
        result = await exportService.exportToMarkdown(conversations, exportOptions);
        break;
      case 'json':
        result = await exportService.exportToJSON(conversations, exportOptions);
        break;
    }

    console.log(`✅ 导出成功: ${result.filename} (${(result.size / 1024).toFixed(1)}KB)`);

    res.json({
      success: true,
      message: '导出成功',
      data: {
        filename: result.filename,
        format: format,
        size: result.size,
        conversation_count: conversations.length,
        download_url: `/api/chat/download/${result.filename}`
      }
    });
  } catch (error) {
    console.error('❌ 导出失败:', error);
    res.status(500).json({
      success: false,
      message: '导出失败',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined
    });
  }
});

// 下载导出文件
app.get('/api/chat/download/:filename', authenticateUser, (req, res) => {
  try {
    const { filename } = req.params;
    const filepath = require('path').join(__dirname, 'exports', filename);

    // 验证文件存在
    if (!require('fs').existsSync(filepath)) {
      return res.status(404).json({
        success: false,
        message: '文件不存在'
      });
    }

    console.log(`📥 用户 ${req.user.username} 下载文件: ${filename}`);

    // 设置响应头
    const ext = require('path').extname(filename).toLowerCase();
    const mimeTypes = {
      '.pdf': 'application/pdf',
      '.docx': 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      '.xlsx': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      '.md': 'text/markdown',
      '.json': 'application/json'
    };

    res.setHeader('Content-Type', mimeTypes[ext] || 'application/octet-stream');
    res.setHeader('Content-Disposition', `attachment; filename="${encodeURIComponent(filename)}"`);

    // 发送文件
    res.sendFile(filepath);
  } catch (error) {
    console.error('❌ 文件下载失败:', error);
    res.status(500).json({
      success: false,
      message: '文件下载失败',
      error: process.env.NODE_ENV === 'development' ? error.message : undefined
    });
  }
});

// 监控API端点
app.get('/api/monitoring/metrics', (req, res) => {
  res.json({
    success: true,
    data: monitoring.getMetrics()
  });
});

app.get('/api/monitoring/alerts', (req, res) => {
  const limit = parseInt(req.query.limit) || 50;
  res.json({
    success: true,
    data: monitoring.getAlerts(limit)
  });
});

app.post('/api/monitoring/alerts/clear', (req, res) => {
  monitoring.clearAlerts();
  res.json({
    success: true,
    message: '告警已清除'
  });
});

app.post('/api/monitoring/thresholds', (req, res) => {
  const thresholds = req.body;
  monitoring.updateThresholds(thresholds);
  res.json({
    success: true,
    message: '告警阈值已更新',
    data: monitoring.thresholds
  });
});

app.get('/api/monitoring/health', (req, res) => {
  res.json({
    success: true,
    data: monitoring.getHealthStatus()
  });
});

const PORT = process.env.PORT || 3004;

// 加载AI模型配置
async function loadAIConfigs() {
  try {
    // 确保配置客户端已连接
    if (!configClient.connected) {
      console.log('⏳ 等待配置中心连接...');
      await configClient.connect();
    }

    const aiModelsArray = await configClient.getAIModels();

    if (aiModelsArray && Array.isArray(aiModelsArray) && aiModelsArray.length > 0) {
      // 转换配置格式以兼容现有代码
      const newConfigs = {};

      aiModelsArray.forEach(model => {
        const modelKey = model.id; // 使用模型的id作为key

        // 根据模型特性判断是否为外部模型
        const isExternal = model.features && model.features.external;
        let apiConfig = {};

        if (isExternal) {
          // 外部模型使用外部API配置
          apiConfig = {
            apiKey: model.apiKey || process.env.EXTERNAL_AI_API_KEY || process.env.DEEPSEEK_API_KEY || '',
            baseURL: model.baseURL || process.env.EXTERNAL_AI_BASE_URL || process.env.DEEPSEEK_BASE_URL || ''
          };
        } else {
          // 内部模型使用内部API配置
          apiConfig = {
            apiKey: model.apiKey || process.env.INTERNAL_AI_API_KEY || process.env.AI_API_KEY || '',
            baseURL: model.baseURL || process.env.INTERNAL_AI_BASE_URL || process.env.AI_BASE_URL || ''
          };
        }

        newConfigs[modelKey] = {
          ...apiConfig,
          model: model.model || modelKey,
          maxTokens: model.maxTokens || 4096,
          temperature: model.temperature || 0.7,
          name: model.name || modelKey,
          provider: model.provider || 'Unknown',
          contextLength: model.contextLength || '4k',
          features: {
            multimodal: model.features?.vision || false,
            vision: model.features?.vision || false,
            toolCalls: model.features?.functionCalling || false,
            streaming: model.features?.streaming || true,
            reasoning: model.features?.reasoning || false,
            jsonMode: model.features?.jsonMode || false,
            external: isExternal
          },
          tags: model.tags || [],
          enabled: model.status === 'active'
        };
      });

      // 更新全局配置
      AI_CONFIGS = newConfigs;

      // 更新当前配置 - 使用第一个可用模型作为默认模型
      const defaultModel = 'gpt-4o'; // 优先使用gpt-4o
      if (AI_CONFIGS[defaultModel]) {
        currentConfig = AI_CONFIGS[defaultModel];
        console.log(`✅ 默认模型设置为: ${currentConfig.name}`);
      } else {
        // 如果gpt-4o不可用，使用第一个可用模型
        const firstModelKey = Object.keys(AI_CONFIGS)[0];
        if (firstModelKey) {
          currentConfig = AI_CONFIGS[firstModelKey];
          console.log(`✅ 默认模型设置为: ${currentConfig.name}`);
        }
      }

      console.log(`✅ AI模型配置已加载: ${Object.keys(AI_CONFIGS).length} 个模型`);
      return true;
    } else {
      console.error('❌ 未能从配置中心获取AI模型配置');
      return false;
    }
  } catch (error) {
    console.error('❌ 加载AI模型配置失败:', error.message);
    return false;
  }
}

// 配置变更监听
configClient.on('configChanged', async (configName, config) => {
  if (configName === 'ai_models') {
    console.log('📢 检测到AI模型配置变更，重新加载...');
    await loadAIConfigs();
  }
});

configClient.on('modelSwitched', (modelKey, modelInfo) => {
  console.log(`📢 模型已切换: ${modelInfo.name}`);
  if (AI_CONFIGS[modelKey]) {
    currentConfig = AI_CONFIGS[modelKey];
  }
});

// 错误统计接口
app.get('/api/stats/errors', errorStatsHandler);

// 数据库性能统计接口
app.get('/api/stats/database', asyncErrorHandler(async (req, res) => {
  if (!dbAdapter.performanceOptimizer) {
    throw new QMSError('性能优化器未初始化', ErrorTypes.INTERNAL_ERROR, 500);
  }

  const stats = await dbAdapter.performanceOptimizer.getDatabaseStats();
  res.json({
    success: true,
    data: stats,
    timestamp: new Date().toISOString()
  });
}));

// HTTP客户端统计接口
app.get('/api/stats/http', asyncErrorHandler(async (req, res) => {
  const metrics = httpClient.getMetrics();
  res.json({
    success: true,
    data: metrics,
    timestamp: new Date().toISOString()
  });
}));

// 数据库维护接口
app.post('/api/admin/database/maintenance', SecurityMiddleware.optionalAuth, asyncErrorHandler(async (req, res) => {
  if (!dbAdapter.performanceOptimizer) {
    throw new QMSError('性能优化器未初始化', ErrorTypes.INTERNAL_ERROR, 500);
  }

  const results = await dbAdapter.performanceOptimizer.performMaintenance();
  res.json({
    success: true,
    data: results,
    timestamp: new Date().toISOString()
  });
}));

// 告警管理接口
app.get('/api/alerts', asyncErrorHandler(async (req, res) => {
  const stats = alertManager.getAlertStats();
  const activeAlerts = alertManager.getActiveAlerts();
  const rules = alertManager.getRules();

  res.json({
    success: true,
    data: {
      stats,
      activeAlerts,
      rules: rules.slice(0, 10) // 只返回前10个规则
    },
    timestamp: new Date().toISOString()
  });
}));

// 告警历史接口
app.get('/api/alerts/history', asyncErrorHandler(async (req, res) => {
  const limit = parseInt(req.query.limit) || 50;
  const history = alertManager.getAlertHistory(limit);

  res.json({
    success: true,
    data: history,
    timestamp: new Date().toISOString()
  });
}));

// 添加告警规则接口
app.post('/api/alerts/rules', SecurityMiddleware.optionalAuth, asyncErrorHandler(async (req, res) => {
  const rule = alertManager.addRule(req.body);
  res.json({
    success: true,
    data: rule,
    message: '告警规则添加成功'
  });
}));

// 删除告警规则接口
app.delete('/api/alerts/rules/:ruleId', SecurityMiddleware.optionalAuth, asyncErrorHandler(async (req, res) => {
  const success = alertManager.removeRule(req.params.ruleId);
  if (!success) {
    throw new QMSError('告警规则不存在', ErrorTypes.NOT_FOUND_ERROR, 404);
  }

  res.json({
    success: true,
    message: '告警规则删除成功'
  });
}));

// 404处理中间件
app.use(notFoundHandler);

// 全局错误处理中间件
app.use(globalErrorHandler);

// 收集系统指标
async function collectSystemMetrics() {
  const os = require('os');
  const fs = require('fs');

  // CPU使用率
  const cpus = os.cpus();
  let totalIdle = 0;
  let totalTick = 0;

  cpus.forEach(cpu => {
    for (const type in cpu.times) {
      totalTick += cpu.times[type];
    }
    totalIdle += cpu.times.idle;
  });

  const cpuUsage = 100 - Math.round(100 * totalIdle / totalTick);

  // 内存使用率
  const totalMem = os.totalmem();
  const freeMem = os.freemem();
  const memoryUsage = Math.round((totalMem - freeMem) / totalMem * 100);

  // 磁盘使用率（简化版）
  let diskUsage = 0;
  try {
    const stats = fs.statSync(process.cwd());
    diskUsage = 50; // 简化处理，实际应该查询磁盘使用情况
  } catch (error) {
    diskUsage = 0;
  }

  // HTTP客户端指标
  const httpMetrics = httpClient.getMetrics();

  // 错误率
  const errorRate = httpMetrics.totalRequests > 0 ?
    (httpMetrics.failedRequests / httpMetrics.totalRequests * 100) : 0;

  return {
    cpu: cpuUsage,
    memory: memoryUsage,
    disk: diskUsage,
    responseTime: httpMetrics.averageResponseTime || 0,
    errorRate: Math.round(errorRate * 100) / 100,
    activeConnections: 0 // 简化处理
  };
}

// 启动服务
async function startServer() {
  try {
    // 初始化数据库适配器
    console.log('🔄 正在初始化数据库...');
    const dbInitialized = await dbAdapter.initialize();

    if (!dbInitialized) {
      console.error('❌ 数据库初始化失败，服务启动中止');
      process.exit(1);
    }

    // 初始化AI模型配置 - 优先从配置中心加载
    console.log('🔄 正在初始化AI模型配置...');
    console.log('🔄 尝试从配置中心加载AI模型配置...');
    const configCenterLoaded = await loadAIConfigs();

    if (!configCenterLoaded || Object.keys(AI_CONFIGS).length === 0) {
      console.log('⚠️ 配置中心加载失败，使用默认配置...');
      const fallbackLoaded = initializeAIConfigs();
      if (!fallbackLoaded) {
        console.error('❌ AI模型配置加载失败，服务启动中止');
        process.exit(1);
      }
    }

    if (!currentConfig) {
      console.error('❌ 未设置默认模型，服务启动中止');
      process.exit(1);
    }

    // 启动Prometheus指标定期更新
    prometheusMetrics.startPeriodicUpdates(dbAdapter, dbAdapter.cache);

    // 启动告警管理器
    alertManager.start();

    // 定期更新系统指标
    setInterval(async () => {
      try {
        const metrics = await collectSystemMetrics();
        alertManager.updateMetrics(metrics);
      } catch (error) {
        console.error('❌ 更新系统指标失败:', error.message);
      }
    }, 30000); // 30秒更新一次

    // 设置服务类型环境变量
    process.env.SERVICE_TYPE = 'chat';

    // 启动HTTP服务器
    const server = app.listen(PORT, '0.0.0.0', () => {
      console.log(`🚀 QMS多模型聊天服务启动成功！`);
      console.log(`📍 服务地址: http://localhost:${PORT}`);
      console.log(`🤖 当前模型: ${currentConfig.name} - ${currentConfig.model}`);
      console.log(`🔧 健康检查: http://localhost:${PORT}/health`);
      console.log(`💬 聊天API: http://localhost:${PORT}/api/chat/send`);
      console.log(`🔄 模型切换: http://localhost:${PORT}/api/chat/switch-model`);
      console.log(`📋 模型列表: http://localhost:${PORT}/api/chat/models`);
      console.log(`📚 对话记录: http://localhost:${PORT}/api/chat/conversations`);
      console.log(`📊 用户统计: http://localhost:${PORT}/api/chat/stats`);
      console.log(`🔍 监控指标: http://localhost:${PORT}/api/monitoring/metrics`);
      console.log(`⚠️ 告警管理: http://localhost:${PORT}/api/monitoring/alerts`);
      console.log(`🔧 进程ID: ${process.pid}`);
      console.log(`\n支持的AI服务商:`);
      Object.keys(AI_CONFIGS).forEach(key => {
        console.log(`  - ${AI_CONFIGS[key].name}: ${AI_CONFIGS[key].model}`);
      });
    });

    server.on('error', (error) => {
      console.error('❌ 聊天服务启动错误:', error);
      if (error.code === 'EADDRINUSE') {
        console.error(`❌ 端口 ${PORT} 已被占用，请检查是否有其他进程使用此端口`);
      }
      process.exit(1);
    });

    server.on('listening', () => {
      const addr = server.address();
      console.log(`✅ 聊天服务正在监听 ${addr.address}:${addr.port}`);
    });

    // 优雅关闭
    process.on('SIGTERM', () => {
      console.log('🔄 收到SIGTERM信号，正在关闭聊天服务...');
      server.close(() => {
        console.log('✅ 聊天服务已关闭');
        process.exit(0);
      });
    });

    process.on('SIGINT', () => {
      console.log('🔄 收到SIGINT信号，正在关闭聊天服务...');
      server.close(() => {
        console.log('✅ 聊天服务已关闭');
        process.exit(0);
      });
    });
  } catch (error) {
    console.error('❌ 服务启动失败:', error.message);
    process.exit(1);
  }
}

// ==================== 智能模型管理API ====================

// 智能模型选择
app.post('/api/models/select', asyncErrorHandler(async (req, res) => {
  const { task, context = {} } = req.body;

  if (!task || !task.type) {
    return res.status(400).json({
      success: false,
      message: '缺少任务类型'
    });
  }

  try {
    const selectedModel = await modelManager.selectOptimalModel(task, context);

    res.json({
      success: true,
      data: {
        modelId: selectedModel.id,
        modelName: selectedModel.name,
        provider: selectedModel.provider,
        capabilities: selectedModel.capabilities
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: '模型选择失败',
      error: error.message
    });
  }
}));

// 模型切换
app.post('/api/models/switch', asyncErrorHandler(async (req, res) => {
  const { fromModel, toModel, sessionId } = req.body;

  if (!fromModel || !toModel) {
    return res.status(400).json({
      success: false,
      message: '缺少源模型或目标模型'
    });
  }

  try {
    const result = await modelManager.switchModel(fromModel, toModel, sessionId);

    res.json({
      success: true,
      message: '模型切换成功',
      data: result
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: '模型切换失败',
      error: error.message
    });
  }
}));

// 预热模型
app.post('/api/models/:modelId/warmup', asyncErrorHandler(async (req, res) => {
  const { modelId } = req.params;

  try {
    await modelManager.warmupModel(modelId);

    res.json({
      success: true,
      message: '模型预热成功'
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: '模型预热失败',
      error: error.message
    });
  }
}));

// 获取模型性能统计
app.get('/api/models/performance', asyncErrorHandler(async (req, res) => {
  const stats = modelManager.getPerformanceStats();

  res.json({
    success: true,
    data: stats
  });
}));

// ==================== 案例异常指导 API（最小可用）====================
const CaseGuidanceService = require('./services/case-guidance-service');
const caseGuidance = new CaseGuidanceService(dbAdapter);

// Excel导入案例（multipart/form-data）
// 暂时禁用文件上传功能，等待multer依赖解决
// const multer = require('multer');
// const upload = multer({ limits: { fileSize: 10 * 1024 * 1024 } });
app.post('/api/cases/import', /* upload.single('file'), */ asyncErrorHandler(async (req, res) => {
  const category = req.body?.category || 'general';
  if (!req.file) {
    return res.status(400).json({ success: false, message: '缺少Excel文件' });
  }
  await caseGuidance.initialize();
  const result = await caseGuidance.importFromExcel(req.file.buffer, req.file.originalname, category, 'upload');
  res.json({ success: true, message: '导入成功', data: result });
}));

// 检索案例
app.get('/api/cases/search', asyncErrorHandler(async (req, res) => {
  const { q = '', category, limit } = req.query;
  await caseGuidance.initialize();
  const rows = await caseGuidance.searchCases(q, { category, limit: parseInt(limit) || 8 });
  res.json({ success: true, data: rows });
}));

// 问答：结合历史案例给出经验指导（不调用模型版本）
app.post('/api/chat/case-guidance', asyncErrorHandler(async (req, res) => {
  const { query = '', category, limit = 8 } = req.body || {};
  await caseGuidance.initialize();
  const rows = await caseGuidance.searchCases(query, { category, limit });
  const guidance = caseGuidance.generateGuidance(query, rows);
  res.json({ success: true, data: { query, guidance } });
}));

// 获取可用模型列表（增强版）
app.get('/api/models/available', asyncErrorHandler(async (req, res) => {
  const { taskType } = req.query;
  const models = modelManager.getAvailableModels(taskType);

  res.json({
    success: true,
    data: models.map(model => ({
      id: model.id,
      name: model.name,
      provider: model.provider,
      capabilities: model.capabilities,
      performance: model.performance,
      warmupStatus: model.warmupStatus
    }))
  });
}));

// ==================== 工作流执行API ====================

// 执行工作流
app.post('/api/workflows/execute', asyncErrorHandler(async (req, res) => {
  const { workflow, input = {}, options = {} } = req.body;

  if (!workflow) {
    return res.status(400).json({
      success: false,
      message: '缺少工作流定义'
    });
  }

  try {
    const result = await workflowEngine.executeWorkflow(workflow, input, options);

    res.json({
      success: true,
      message: '工作流执行成功',
      data: result
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: '工作流执行失败',
      error: error.message
    });
  }
}));

// 获取工作流执行状态
app.get('/api/workflows/execution/:executionId', asyncErrorHandler(async (req, res) => {
  const { executionId } = req.params;

  const execution = workflowEngine.getExecutionStatus(executionId);

  if (!execution) {
    return res.status(404).json({
      success: false,
      message: '执行记录不存在'
    });
  }

  res.json({
    success: true,
    data: execution
  });
}));

// 获取工作流性能指标
app.get('/api/workflows/metrics', asyncErrorHandler(async (req, res) => {
  const metrics = workflowEngine.getPerformanceMetrics();

  res.json({
    success: true,
    data: metrics
  });
}));

// 验证工作流定义
app.post('/api/workflows/validate', asyncErrorHandler(async (req, res) => {
  const { workflow } = req.body;

  if (!workflow) {
    return res.status(400).json({
      success: false,
      message: '缺少工作流定义'
    });
  }

  try {
    // 基本验证
    const validation = {
      valid: true,
      errors: [],
      warnings: []
    };

    // 检查必要字段
    if (!workflow.nodes || !Array.isArray(workflow.nodes)) {
      validation.valid = false;
      validation.errors.push('工作流必须包含节点数组');
    }

    if (!workflow.edges || !Array.isArray(workflow.edges)) {
      validation.valid = false;
      validation.errors.push('工作流必须包含边数组');
    }

    // 检查节点类型
    if (workflow.nodes) {
      workflow.nodes.forEach((node, index) => {
        if (!node.type) {
          validation.errors.push(`节点 ${index} 缺少类型定义`);
          validation.valid = false;
        }

        if (!workflowEngine.nodeExecutors.has(node.type)) {
          validation.warnings.push(`节点 ${index} 使用了未知类型: ${node.type}`);
        }
      });
    }

    res.json({
      success: true,
      data: validation
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: '工作流验证失败',
      error: error.message
    });
  }
}));

// ==================== 插件管理API ====================

// 获取所有插件
app.get('/api/plugins', asyncErrorHandler(async (req, res) => {
  const plugins = pluginManager.getAllPlugins();
  const available = pluginManager.getAvailablePlugins();

  res.json({
    success: true,
    data: {
      loaded: plugins,
      available: available
    }
  });
}));

// 加载插件
app.post('/api/plugins/:pluginId/load', asyncErrorHandler(async (req, res) => {
  const { pluginId } = req.params;
  const config = req.body.config || {};

  const result = await pluginManager.loadPlugin(pluginId, config);

  res.json({
    success: true,
    message: '插件加载成功',
    data: result
  });
}));

// 卸载插件
app.post('/api/plugins/:pluginId/unload', asyncErrorHandler(async (req, res) => {
  const { pluginId } = req.params;

  const result = await pluginManager.unloadPlugin(pluginId);

  res.json({
    success: true,
    message: '插件卸载成功',
    data: result
  });
}));

// 启用插件
app.post('/api/plugins/:pluginId/enable', asyncErrorHandler(async (req, res) => {
  const { pluginId } = req.params;

  const result = await pluginManager.enablePlugin(pluginId);

  res.json({
    success: true,
    message: '插件启用成功',
    data: result
  });
}));

// 禁用插件
app.post('/api/plugins/:pluginId/disable', asyncErrorHandler(async (req, res) => {
  const { pluginId } = req.params;

  const result = await pluginManager.disablePlugin(pluginId);

  res.json({
    success: true,
    message: '插件禁用成功',
    data: result
  });
}));

// 调用插件方法
app.post('/api/plugins/:pluginId/call', asyncErrorHandler(async (req, res) => {
  const { pluginId } = req.params;
  const { method, args = [] } = req.body;

  if (!method) {
    return res.status(400).json({
      success: false,
      message: '缺少方法名'
    });
  }

  const result = await pluginManager.callPlugin(pluginId, method, ...args);

  res.json({
    success: true,
    data: result
  });
}));

// 插件健康检查
app.get('/api/plugins/health', asyncErrorHandler(async (req, res) => {
  const health = await pluginManager.healthCheck();

  res.json({
    success: true,
    data: health
  });
}));

// 飞书插件专用API
app.post('/api/plugins/feishu/send-message', asyncErrorHandler(async (req, res) => {
  const feishuPlugin = pluginManager.getPlugin('feishu-integration');
  if (!feishuPlugin || !feishuPlugin.enabled) {
    return res.status(400).json({
      success: false,
      message: '飞书插件未启用'
    });
  }

  const result = await feishuPlugin.sendMessage(req.body);

  res.json({
    success: true,
    data: result
  });
}));

// MCP连接器专用API
app.post('/api/plugins/mcp/connect', asyncErrorHandler(async (req, res) => {
  const mcpConnector = pluginManager.getPlugin('mcp-connector');
  if (!mcpConnector || !mcpConnector.enabled) {
    return res.status(400).json({
      success: false,
      message: 'MCP连接器未启用'
    });
  }

  const { serviceId, config } = req.body;
  const result = await mcpConnector.connectService(serviceId, config);

  res.json({
    success: true,
    data: result
  });
}));

app.post('/api/plugins/mcp/call-tool', asyncErrorHandler(async (req, res) => {
  const mcpConnector = pluginManager.getPlugin('mcp-connector');
  if (!mcpConnector || !mcpConnector.enabled) {
    return res.status(400).json({
      success: false,
      message: 'MCP连接器未启用'
    });
  }

  const { serviceId, toolName, arguments: toolArgs } = req.body;
  const result = await mcpConnector.callTool(serviceId, toolName, toolArgs);

  res.json({
    success: true,
    data: result
  });
}));

// 优雅关闭处理
process.on('SIGINT', async () => {
  console.log('\n🔄 正在关闭服务...');
  alertManager.stop();
  await pluginManager.cleanup();
  await dbAdapter.close();
  configClient.close();
  process.exit(0);
});

process.on('SIGTERM', async () => {
  console.log('\n🔄 正在关闭服务...');
  alertManager.stop();
  await pluginManager.cleanup();
  await dbAdapter.close();
  process.exit(0);
});

// 404处理 - 必须在所有路由之后
app.use('*', (req, res) => {
  res.status(404).json({
    success: false,
    message: 'API接口不存在'
  });
});

// 启动服务
startServer();
