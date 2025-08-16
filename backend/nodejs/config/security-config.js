/**
 * 安全配置管理
 * 统一管理API密钥、JWT密钥等敏感信息
 */

const crypto = require('crypto');
const fs = require('fs');
const path = require('path');

class SecurityConfig {
  constructor() {
    this.config = {};
    this.encryptionKey = null;
    this.initialized = false;

    // 不在构造函数中自动初始化，等待手动调用
  }

  /**
   * 初始化安全配置
   */
  initialize() {
    try {
      // 验证必需的环境变量
      this.validateEnvironment();
      
      // 初始化加密密钥
      this.initializeEncryption();
      
      // 加载配置
      this.loadConfiguration();
      
      this.initialized = true;
      console.log('🔐 安全配置初始化完成');
    } catch (error) {
      console.error('❌ 安全配置初始化失败:', error.message);
      throw error;
    }
  }

  /**
   * 验证环境变量
   */
  validateEnvironment() {
    // 在开发环境下设置默认值（但不覆盖已存在的环境变量）
    if (process.env.NODE_ENV !== 'production') {
      process.env.JWT_SECRET = process.env.JWT_SECRET || 'qms-ai-development-jwt-secret-key-32-chars-minimum';
      // 不覆盖已存在的API密钥
      if (!process.env.DEEPSEEK_API_KEY && !process.env.EXTERNAL_AI_API_KEY) {
        process.env.DEEPSEEK_API_KEY = 'sk-development-key';
      }
      if (!process.env.OPENAI_API_KEY && !process.env.INTERNAL_AI_API_KEY) {
        process.env.OPENAI_API_KEY = 'sk-development-key';
      }

      console.log('🔧 开发环境：使用默认安全配置');
      return;
    }

    const requiredEnvVars = [
      'JWT_SECRET',
      'DEEPSEEK_API_KEY',
      'OPENAI_API_KEY'
    ];

    const missingVars = requiredEnvVars.filter(varName => !process.env[varName]);

    if (missingVars.length > 0) {
      throw new Error(`缺少必需的环境变量: ${missingVars.join(', ')}`);
    }

    // 验证JWT密钥强度
    if (process.env.JWT_SECRET.length < 32) {
      throw new Error('JWT_SECRET 长度必须至少32个字符');
    }
    
    console.log('✅ 环境变量验证通过');
  }

  /**
   * 初始化加密
   */
  initializeEncryption() {
    // 使用环境变量或生成加密密钥
    this.encryptionKey = process.env.ENCRYPTION_KEY || this.generateEncryptionKey();
    
    if (!process.env.ENCRYPTION_KEY) {
      console.warn('⚠️ 未设置ENCRYPTION_KEY环境变量，使用临时密钥');
    }
  }

  /**
   * 生成加密密钥
   */
  generateEncryptionKey() {
    return crypto.randomBytes(32).toString('hex');
  }

  /**
   * 加载配置
   */
  loadConfiguration() {
    this.config = {
      // JWT配置
      jwt: {
        secret: process.env.JWT_SECRET,
        expiresIn: process.env.JWT_EXPIRES_IN || '24h',
        issuer: process.env.JWT_ISSUER || 'qms-ai',
        audience: process.env.JWT_AUDIENCE || 'qms-ai-users'
      },
      
      // AI模型配置
      ai: {
        deepseek: {
          apiKey: this.encryptApiKey(process.env.DEEPSEEK_API_KEY),
          baseURL: process.env.DEEPSEEK_BASE_URL || 'https://api.deepseek.com',
          timeout: parseInt(process.env.DEEPSEEK_TIMEOUT) || 30000
        },
        openai: {
          apiKey: this.encryptApiKey(process.env.OPENAI_API_KEY),
          baseURL: process.env.OPENAI_BASE_URL || 'https://api.openai.com/v1',
          timeout: parseInt(process.env.OPENAI_TIMEOUT) || 30000
        },
        anthropic: {
          apiKey: this.encryptApiKey(process.env.ANTHROPIC_API_KEY || ''),
          baseURL: process.env.ANTHROPIC_BASE_URL || 'https://api.anthropic.com',
          timeout: parseInt(process.env.ANTHROPIC_TIMEOUT) || 30000
        }
      },
      
      // 数据库配置
      database: {
        encryption: {
          enabled: process.env.DB_ENCRYPTION_ENABLED === 'true',
          key: this.encryptionKey
        }
      },
      
      // 安全策略
      security: {
        // 密码策略
        password: {
          minLength: parseInt(process.env.PASSWORD_MIN_LENGTH) || 8,
          requireUppercase: process.env.PASSWORD_REQUIRE_UPPERCASE !== 'false',
          requireLowercase: process.env.PASSWORD_REQUIRE_LOWERCASE !== 'false',
          requireNumbers: process.env.PASSWORD_REQUIRE_NUMBERS !== 'false',
          requireSpecialChars: process.env.PASSWORD_REQUIRE_SPECIAL !== 'false'
        },
        
        // 会话策略
        session: {
          timeout: parseInt(process.env.SESSION_TIMEOUT) || 3600000, // 1小时
          maxConcurrent: parseInt(process.env.MAX_CONCURRENT_SESSIONS) || 5
        },
        
        // 速率限制
        rateLimit: {
          windowMs: parseInt(process.env.RATE_LIMIT_WINDOW) || 60000, // 1分钟
          maxRequests: parseInt(process.env.RATE_LIMIT_MAX) || 100,
          skipSuccessfulRequests: process.env.RATE_LIMIT_SKIP_SUCCESS === 'true'
        },
        
        // CORS配置
        cors: {
          origin: this.parseCorsOrigins(),
          credentials: true,
          methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
          allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With']
        }
      }
    };
  }

  /**
   * 解析CORS来源
   */
  parseCorsOrigins() {
    const origins = process.env.CORS_ORIGINS;
    if (!origins) {
      return ['http://localhost:8080', 'http://localhost:8072'];
    }
    
    return origins.split(',').map(origin => origin.trim());
  }

  /**
   * 加密API密钥
   */
  encryptApiKey(apiKey) {
    if (!apiKey) return '';
    
    try {
      const cipher = crypto.createCipher('aes-256-cbc', this.encryptionKey);
      let encrypted = cipher.update(apiKey, 'utf8', 'hex');
      encrypted += cipher.final('hex');
      return encrypted;
    } catch (error) {
      console.error('API密钥加密失败:', error);
      return apiKey; // 降级到明文（不推荐）
    }
  }

  /**
   * 解密API密钥
   */
  decryptApiKey(encryptedKey) {
    if (!encryptedKey) return '';
    
    try {
      const decipher = crypto.createDecipher('aes-256-cbc', this.encryptionKey);
      let decrypted = decipher.update(encryptedKey, 'hex', 'utf8');
      decrypted += decipher.final('utf8');
      return decrypted;
    } catch (error) {
      console.error('API密钥解密失败:', error);
      return encryptedKey; // 假设是明文
    }
  }

  /**
   * 获取JWT配置
   */
  getJWTConfig() {
    return this.config.jwt;
  }

  /**
   * 获取AI配置
   */
  getAIConfig(provider) {
    const aiConfig = this.config.ai[provider];
    if (!aiConfig) {
      throw new Error(`不支持的AI提供商: ${provider}`);
    }
    
    return {
      ...aiConfig,
      apiKey: this.decryptApiKey(aiConfig.apiKey)
    };
  }

  /**
   * 获取所有AI配置
   */
  getAllAIConfigs() {
    const configs = {};
    for (const [provider, config] of Object.entries(this.config.ai)) {
      configs[provider] = {
        ...config,
        apiKey: this.decryptApiKey(config.apiKey)
      };
    }
    return configs;
  }

  /**
   * 获取安全策略
   */
  getSecurityPolicy() {
    return this.config.security;
  }

  /**
   * 获取数据库配置
   */
  getDatabaseConfig() {
    return this.config.database;
  }

  /**
   * 验证密码强度
   */
  validatePassword(password) {
    const policy = this.config.security.password;
    const errors = [];
    
    if (password.length < policy.minLength) {
      errors.push(`密码长度至少${policy.minLength}个字符`);
    }
    
    if (policy.requireUppercase && !/[A-Z]/.test(password)) {
      errors.push('密码必须包含大写字母');
    }
    
    if (policy.requireLowercase && !/[a-z]/.test(password)) {
      errors.push('密码必须包含小写字母');
    }
    
    if (policy.requireNumbers && !/\d/.test(password)) {
      errors.push('密码必须包含数字');
    }
    
    if (policy.requireSpecialChars && !/[!@#$%^&*(),.?":{}|<>]/.test(password)) {
      errors.push('密码必须包含特殊字符');
    }
    
    return {
      isValid: errors.length === 0,
      errors
    };
  }

  /**
   * 生成安全的随机字符串
   */
  generateSecureRandom(length = 32) {
    return crypto.randomBytes(length).toString('hex');
  }

  /**
   * 哈希密码
   */
  hashPassword(password, salt = null) {
    if (!salt) {
      salt = crypto.randomBytes(16).toString('hex');
    }
    
    const hash = crypto.pbkdf2Sync(password, salt, 10000, 64, 'sha512').toString('hex');
    return { hash, salt };
  }

  /**
   * 验证密码
   */
  verifyPassword(password, hash, salt) {
    const { hash: computedHash } = this.hashPassword(password, salt);
    return computedHash === hash;
  }

  /**
   * 获取配置状态
   */
  getStatus() {
    return {
      initialized: this.initialized,
      hasJWTSecret: !!this.config.jwt?.secret,
      hasEncryptionKey: !!this.encryptionKey,
      aiProviders: Object.keys(this.config.ai || {}),
      securityPolicies: Object.keys(this.config.security || {})
    };
  }

  /**
   * 重新加载配置
   */
  reload() {
    console.log('🔄 重新加载安全配置...');
    this.initialized = false;
    this.initialize();
  }
}

// 导出类，让调用者决定何时初始化
module.exports = SecurityConfig;
