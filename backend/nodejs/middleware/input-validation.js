/**
 * 输入验证中间件
 * 防止注入攻击，确保数据安全
 */

const Joi = require('joi');
// const validator = require('validator'); // 暂时注释，待安装
// const xss = require('xss'); // 暂时注释，待安装
const { APIError, ValidationError } = require('./error-handler');

/**
 * 自定义验证规则
 */
const customValidators = {
  // 安全的字符串（防XSS）
  safeString: Joi.string().custom((value, helpers) => {
    // 简单的XSS防护，移除危险字符
    const cleaned = value
      .replace(/[<>]/g, '') // 移除尖括号
      .replace(/javascript:/gi, '') // 移除javascript协议
      .replace(/on\w+=/gi, ''); // 移除事件处理器

    if (cleaned !== value) {
      return helpers.error('string.unsafe');
    }

    return cleaned;
  }),
  
  // 安全的HTML（允许部分标签）
  safeHtml: Joi.string().custom((value, helpers) => {
    // 简单的HTML清理，只允许基本标签
    const cleaned = value
      .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '') // 移除script标签
      .replace(/<iframe\b[^<]*(?:(?!<\/iframe>)<[^<]*)*<\/iframe>/gi, '') // 移除iframe标签
      .replace(/javascript:/gi, '') // 移除javascript协议
      .replace(/on\w+=/gi, ''); // 移除事件处理器

    return cleaned;
  }),
  
  // SQL注入检测
  sqlSafe: Joi.string().custom((value, helpers) => {
    const sqlPatterns = [
      /(\b(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE|ALTER|EXEC|UNION|SCRIPT)\b)/i,
      /(--|\/\*|\*\/|;|'|"|`)/,
      /(\bOR\b|\bAND\b).*?[=<>]/i
    ];
    
    for (const pattern of sqlPatterns) {
      if (pattern.test(value)) {
        return helpers.error('string.sql_injection');
      }
    }
    
    return value;
  }),
  
  // 文件路径安全检查
  safePath: Joi.string().custom((value, helpers) => {
    const dangerousPatterns = [
      /\.\./,           // 目录遍历
      /[<>:"|?*]/,      // Windows非法字符
      /^\/|\\$/,        // 绝对路径
      /\0/              // 空字节
    ];
    
    for (const pattern of dangerousPatterns) {
      if (pattern.test(value)) {
        return helpers.error('string.unsafe_path');
      }
    }
    
    return value;
  })
};

/**
 * 验证模式定义
 */
const validationSchemas = {
  // 用户认证
  userLogin: Joi.object({
    username: customValidators.safeString
      .alphanum()
      .min(3)
      .max(30)
      .required()
      .messages({
        'string.alphanum': '用户名只能包含字母和数字',
        'string.min': '用户名至少3个字符',
        'string.max': '用户名最多30个字符',
        'any.required': '用户名是必需的'
      }),
    password: Joi.string()
      .min(6)
      .max(128)
      .pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/)
      .required()
      .messages({
        'string.min': '密码至少6个字符',
        'string.max': '密码最多128个字符',
        'string.pattern.base': '密码必须包含大小写字母和数字',
        'any.required': '密码是必需的'
      })
  }),
  
  // 聊天消息
  chatMessage: Joi.object({
    message: customValidators.safeString
      .min(1)
      .max(4000)
      .required()
      .messages({
        'string.min': '消息不能为空',
        'string.max': '消息最多4000个字符',
        'string.unsafe': '消息包含不安全的内容',
        'any.required': '消息是必需的'
      }),
    conversationId: Joi.string()
      .uuid()
      .optional()
      .messages({
        'string.uuid': '对话ID格式不正确'
      }),
    model: Joi.string()
      .valid(
        // OpenAI 模型
        'gpt-4o', 'gpt-4o-mini', 'GPT-4o', 'o3', 'o3-mini',
        // Claude 模型
        'claude-3.5-sonnet', 'claude-3-5-sonnet-20241022', 'claude-3.7-sonnet',
        // DeepSeek 模型
        'deepseek-chat', 'deepseek-coder', 'deepseek-chat-v3-0324', 'deepseek-reasoner-r1-0528',
        'deepseek-r1', 'deepseek-v3', 'deepseek-reasoner',
        // Gemini 模型
        'gemini-1.5-pro', 'gemini-2.5-pro-thinking'
      )
      .optional()
      .messages({
        'any.only': '不支持的AI模型'
      }),
    temperature: Joi.number()
      .min(0)
      .max(2)
      .optional()
      .messages({
        'number.min': '温度参数不能小于0',
        'number.max': '温度参数不能大于2'
      }),
    maxTokens: Joi.number()
      .integer()
      .min(1)
      .max(8000)
      .optional()
      .messages({
        'number.integer': '最大token数必须是整数',
        'number.min': '最大token数不能小于1',
        'number.max': '最大token数不能大于8000'
      })
  }),
  
  // 对话管理
  conversation: Joi.object({
    title: customValidators.safeString
      .min(1)
      .max(200)
      .required()
      .messages({
        'string.min': '对话标题不能为空',
        'string.max': '对话标题最多200个字符',
        'string.unsafe': '对话标题包含不安全的内容'
      }),
    model: Joi.string()
      .valid('gpt-4o', 'gpt-4o-mini', 'claude-3.5-sonnet', 'deepseek-chat', 'deepseek-coder')
      .optional()
  }),
  
  // 配置管理
  configItem: Joi.object({
    key: customValidators.safeString
      .pattern(/^[a-zA-Z0-9._-]+$/)
      .min(1)
      .max(100)
      .required()
      .messages({
        'string.pattern.base': '配置键只能包含字母、数字、点、下划线和连字符',
        'string.min': '配置键不能为空',
        'string.max': '配置键最多100个字符'
      }),
    value: Joi.alternatives()
      .try(
        customValidators.safeString.max(10000),
        Joi.number(),
        Joi.boolean(),
        Joi.object(),
        Joi.array()
      )
      .required()
      .messages({
        'alternatives.match': '配置值格式不正确',
        'string.max': '配置值最多10000个字符'
      }),
    description: customValidators.safeString
      .max(500)
      .optional()
      .messages({
        'string.max': '配置描述最多500个字符'
      }),
    category: customValidators.safeString
      .max(50)
      .optional()
      .messages({
        'string.max': '配置分类最多50个字符'
      })
  }),
  
  // 文件上传
  fileUpload: Joi.object({
    filename: customValidators.safePath
      .pattern(/^[a-zA-Z0-9._-]+\.[a-zA-Z0-9]+$/)
      .max(255)
      .required()
      .messages({
        'string.pattern.base': '文件名格式不正确',
        'string.max': '文件名最多255个字符',
        'string.unsafe_path': '文件名包含不安全的字符'
      }),
    size: Joi.number()
      .integer()
      .min(1)
      .max(10 * 1024 * 1024) // 10MB
      .required()
      .messages({
        'number.min': '文件大小不能为0',
        'number.max': '文件大小不能超过10MB'
      }),
    type: Joi.string()
      .valid('image/jpeg', 'image/png', 'image/gif', 'text/plain', 'application/pdf')
      .required()
      .messages({
        'any.only': '不支持的文件类型'
      })
  }),
  
  // 用户管理
  userProfile: Joi.object({
    username: customValidators.safeString
      .alphanum()
      .min(3)
      .max(30)
      .optional(),
    email: Joi.string()
      .email()
      .max(100)
      .optional()
      .messages({
        'string.email': '邮箱格式不正确',
        'string.max': '邮箱最多100个字符'
      }),
    displayName: customValidators.safeString
      .min(1)
      .max(50)
      .optional()
      .messages({
        'string.min': '显示名称不能为空',
        'string.max': '显示名称最多50个字符'
      }),
    avatar: Joi.string()
      .uri()
      .optional()
      .messages({
        'string.uri': '头像URL格式不正确'
      })
  }),
  
  // 分页参数
  pagination: Joi.object({
    page: Joi.number()
      .integer()
      .min(1)
      .max(1000)
      .default(1)
      .messages({
        'number.min': '页码不能小于1',
        'number.max': '页码不能大于1000'
      }),
    limit: Joi.number()
      .integer()
      .min(1)
      .max(100)
      .default(20)
      .messages({
        'number.min': '每页数量不能小于1',
        'number.max': '每页数量不能大于100'
      }),
    sortBy: customValidators.safeString
      .pattern(/^[a-zA-Z0-9_]+$/)
      .optional()
      .messages({
        'string.pattern.base': '排序字段格式不正确'
      }),
    sortOrder: Joi.string()
      .valid('asc', 'desc')
      .default('desc')
      .optional()
  })
};

/**
 * 创建验证中间件
 */
function createValidator(schema, options = {}) {
  return (req, res, next) => {
    const {
      source = 'body',           // 验证来源: body, query, params
      allowUnknown = false,      // 是否允许未知字段
      stripUnknown = true,       // 是否移除未知字段
      abortEarly = false         // 是否在第一个错误时停止
    } = options;
    
    const dataToValidate = req[source];
    
    const validationOptions = {
      allowUnknown,
      stripUnknown,
      abortEarly,
      errors: {
        label: 'key'
      }
    };
    
    const { error, value } = schema.validate(dataToValidate, validationOptions);
    
    if (error) {
      const errorDetails = error.details.map(detail => ({
        field: detail.path.join('.'),
        message: detail.message,
        value: detail.context?.value
      }));
      
      console.warn('输入验证失败:', {
        source,
        errors: errorDetails,
        originalData: dataToValidate
      });
      
      const validationError = new ValidationError(
        '输入验证失败',
        errorDetails[0].field,
        errorDetails[0].value
      );
      validationError.details = errorDetails;
      
      return next(validationError);
    }
    
    // 将验证后的数据替换原始数据
    req[source] = value;
    next();
  };
}

/**
 * 预定义的验证中间件
 */
const validators = {
  // 用户认证验证
  validateLogin: createValidator(validationSchemas.userLogin),
  
  // 聊天消息验证
  validateChatMessage: createValidator(validationSchemas.chatMessage),
  
  // 对话验证
  validateConversation: createValidator(validationSchemas.conversation),
  
  // 配置验证
  validateConfig: createValidator(validationSchemas.configItem),
  
  // 文件上传验证
  validateFileUpload: createValidator(validationSchemas.fileUpload),
  
  // 用户资料验证
  validateUserProfile: createValidator(validationSchemas.userProfile),
  
  // 查询参数验证
  validateQuery: createValidator(validationSchemas.pagination, { source: 'query' }),
  
  // 路径参数验证
  validateParams: (schema) => createValidator(schema, { source: 'params' })
};

/**
 * 通用输入清理中间件
 */
function sanitizeInput(req, res, next) {
  // 递归清理对象
  function cleanObject(obj) {
    if (typeof obj === 'string') {
      // 移除危险字符
      return obj
        .replace(/[<>]/g, '') // 移除尖括号
        .replace(/javascript:/gi, '') // 移除javascript协议
        .replace(/on\w+=/gi, '') // 移除事件处理器
        .trim();
    } else if (Array.isArray(obj)) {
      return obj.map(cleanObject);
    } else if (obj && typeof obj === 'object') {
      const cleaned = {};
      for (const [key, value] of Object.entries(obj)) {
        // 清理键名
        const cleanKey = key.replace(/[^a-zA-Z0-9_.-]/g, '');
        if (cleanKey) {
          cleaned[cleanKey] = cleanObject(value);
        }
      }
      return cleaned;
    }
    return obj;
  }
  
  // 清理请求数据
  if (req.body) {
    req.body = cleanObject(req.body);
  }
  if (req.query) {
    req.query = cleanObject(req.query);
  }
  if (req.params) {
    req.params = cleanObject(req.params);
  }
  
  next();
}

/**
 * 速率限制验证
 */
function validateRateLimit(maxRequests = 100, windowMs = 60000) {
  const requests = new Map();
  
  return (req, res, next) => {
    const clientId = req.ip || req.connection.remoteAddress;
    const now = Date.now();
    const windowStart = now - windowMs;
    
    // 清理过期记录
    if (requests.has(clientId)) {
      const clientRequests = requests.get(clientId);
      const validRequests = clientRequests.filter(time => time > windowStart);
      requests.set(clientId, validRequests);
    }
    
    // 检查请求数量
    const clientRequests = requests.get(clientId) || [];
    if (clientRequests.length >= maxRequests) {
      const error = new APIError(
        '请求频率过高，请稍后再试',
        'RATE_LIMIT_EXCEEDED',
        429
      );
      return next(error);
    }
    
    // 记录请求
    clientRequests.push(now);
    requests.set(clientId, clientRequests);
    
    next();
  };
}

module.exports = {
  validationSchemas,
  customValidators,
  createValidator,
  validators,
  sanitizeInput,
  validateRateLimit
};
