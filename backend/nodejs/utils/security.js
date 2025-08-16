/**
 * QMS-AI 安全工具模块
 * 提供JWT认证、密码加密、输入验证等安全功能
 */

const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');
const escapeHtml = require('escape-html');
const Joi = require('joi');

// 安全配置
const SECURITY_CONFIG = {
  jwt: {
    secret: process.env.JWT_SECRET || 'qms-ai-super-secret-key-change-in-production-2024',
    expiresIn: process.env.JWT_EXPIRES_IN || '24h',
    issuer: 'qms-ai-system',
    audience: 'qms-ai-users'
  },
  bcrypt: {
    saltRounds: 12
  },
  rateLimit: {
    windowMs: 15 * 60 * 1000, // 15分钟
    max: 100 // 每个IP最多100个请求
  }
};

/**
 * JWT Token 管理
 */
class JWTManager {
  /**
   * 生成JWT Token
   * @param {Object} user - 用户信息
   * @returns {string} JWT Token
   */
  static generateToken(user) {
    try {
      const payload = {
        userId: user.id,
        username: user.username,
        email: user.email,
        roles: user.roles || ['USER'],
        permissions: user.permissions || [],
        iat: Math.floor(Date.now() / 1000)
      };

      return jwt.sign(payload, SECURITY_CONFIG.jwt.secret, {
        expiresIn: SECURITY_CONFIG.jwt.expiresIn,
        issuer: SECURITY_CONFIG.jwt.issuer,
        audience: SECURITY_CONFIG.jwt.audience,
        algorithm: 'HS256'
      });
    } catch (error) {
      console.error('❌ JWT Token生成失败:', error.message);
      throw new Error('Token生成失败');
    }
  }

  /**
   * 验证JWT Token
   * @param {string} token - JWT Token
   * @returns {Object|null} 解码后的用户信息
   */
  static verifyToken(token) {
    try {
      if (!token) {
        return null;
      }

      // 移除Bearer前缀
      const cleanToken = token.replace(/^Bearer\s+/, '');

      const decoded = jwt.verify(cleanToken, SECURITY_CONFIG.jwt.secret, {
        issuer: SECURITY_CONFIG.jwt.issuer,
        audience: SECURITY_CONFIG.jwt.audience,
        algorithms: ['HS256']
      });

      // 检查Token是否即将过期（30分钟内）
      const now = Math.floor(Date.now() / 1000);
      if (decoded.exp - now < 1800) {
        console.warn('⚠️ JWT Token即将过期，建议刷新');
      }

      return decoded;
    } catch (error) {
      if (error.name === 'TokenExpiredError') {
        console.warn('⚠️ JWT Token已过期');
      } else if (error.name === 'JsonWebTokenError') {
        console.warn('⚠️ JWT Token无效:', error.message);
      } else {
        console.error('❌ JWT Token验证失败:', error.message);
      }
      return null;
    }
  }

  /**
   * 刷新JWT Token
   * @param {string} token - 旧的JWT Token
   * @returns {string|null} 新的JWT Token
   */
  static refreshToken(token) {
    try {
      const decoded = this.verifyToken(token);
      if (!decoded) {
        return null;
      }

      // 创建新的用户对象
      const user = {
        id: decoded.userId,
        username: decoded.username,
        email: decoded.email,
        roles: decoded.roles,
        permissions: decoded.permissions
      };

      return this.generateToken(user);
    } catch (error) {
      console.error('❌ JWT Token刷新失败:', error.message);
      return null;
    }
  }
}

/**
 * 密码管理
 */
class PasswordManager {
  /**
   * 加密密码
   * @param {string} password - 明文密码
   * @returns {Promise<string>} 加密后的密码
   */
  static async hashPassword(password) {
    try {
      if (!password || typeof password !== 'string') {
        throw new Error('密码不能为空');
      }

      if (password.length < 6) {
        throw new Error('密码长度不能少于6位');
      }

      return await bcrypt.hash(password, SECURITY_CONFIG.bcrypt.saltRounds);
    } catch (error) {
      console.error('❌ 密码加密失败:', error.message);
      throw error;
    }
  }

  /**
   * 验证密码
   * @param {string} password - 明文密码
   * @param {string} hashedPassword - 加密后的密码
   * @returns {Promise<boolean>} 验证结果
   */
  static async verifyPassword(password, hashedPassword) {
    try {
      if (!password || !hashedPassword) {
        return false;
      }

      return await bcrypt.compare(password, hashedPassword);
    } catch (error) {
      console.error('❌ 密码验证失败:', error.message);
      return false;
    }
  }

  /**
   * 生成随机密码
   * @param {number} length - 密码长度
   * @returns {string} 随机密码
   */
  static generateRandomPassword(length = 12) {
    const charset = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*';
    let password = '';
    
    for (let i = 0; i < length; i++) {
      password += charset.charAt(Math.floor(Math.random() * charset.length));
    }
    
    return password;
  }
}

/**
 * 输入验证和清理
 */
class InputValidator {
  /**
   * HTML转义
   * @param {string} input - 输入字符串
   * @returns {string} 转义后的字符串
   */
  static escapeHtml(input) {
    if (typeof input !== 'string') {
      return input;
    }
    return escapeHtml(input);
  }

  /**
   * 清理和验证聊天消息
   * @param {string} message - 聊天消息
   * @returns {Object} 验证结果
   */
  static validateChatMessage(message) {
    const schema = Joi.object({
      message: Joi.string()
        .min(1)
        .max(10000)
        .required()
        .messages({
          'string.empty': '消息不能为空',
          'string.min': '消息长度不能少于1个字符',
          'string.max': '消息长度不能超过10000个字符',
          'any.required': '消息是必填项'
        })
    });

    const { error, value } = schema.validate({ message });
    
    if (error) {
      return {
        isValid: false,
        error: error.details[0].message,
        sanitized: null
      };
    }

    return {
      isValid: true,
      error: null,
      sanitized: this.escapeHtml(value.message)
    };
  }

  /**
   * 验证用户登录信息
   * @param {Object} loginData - 登录数据
   * @returns {Object} 验证结果
   */
  static validateLoginData(loginData) {
    const schema = Joi.object({
      username: Joi.string()
        .alphanum()
        .min(3)
        .max(30)
        .required()
        .messages({
          'string.alphanum': '用户名只能包含字母和数字',
          'string.min': '用户名长度不能少于3个字符',
          'string.max': '用户名长度不能超过30个字符',
          'any.required': '用户名是必填项'
        }),
      password: Joi.string()
        .min(6)
        .max(128)
        .required()
        .messages({
          'string.min': '密码长度不能少于6个字符',
          'string.max': '密码长度不能超过128个字符',
          'any.required': '密码是必填项'
        }),
      rememberMe: Joi.boolean().optional()
    });

    const { error, value } = schema.validate(loginData);
    
    if (error) {
      return {
        isValid: false,
        error: error.details[0].message,
        sanitized: null
      };
    }

    return {
      isValid: true,
      error: null,
      sanitized: {
        username: this.escapeHtml(value.username),
        password: value.password, // 密码不转义，但会在后续处理中加密
        rememberMe: value.rememberMe || false
      }
    };
  }

  /**
   * 验证AI模型配置
   * @param {Object} modelConfig - 模型配置
   * @returns {Object} 验证结果
   */
  static validateModelConfig(modelConfig) {
    const schema = Joi.object({
      model: Joi.string()
        .valid('gpt-4o', 'claude-3-5-sonnet', 'deepseek-chat', 'deepseek-coder', 'gemini-1-5-pro', 'gpt-4-turbo', 'claude-3-haiku', 'gpt-3-5-turbo')
        .required(),
      temperature: Joi.number().min(0).max(2).optional(),
      max_tokens: Joi.number().min(1).max(8192).optional(),
      conversation_id: Joi.string().uuid().optional()
    });

    const { error, value } = schema.validate(modelConfig);
    
    if (error) {
      return {
        isValid: false,
        error: error.details[0].message,
        sanitized: null
      };
    }

    return {
      isValid: true,
      error: null,
      sanitized: value
    };
  }
}

/**
 * 安全中间件
 */
class SecurityMiddleware {
  /**
   * JWT认证中间件（强制认证，不支持匿名用户）
   */
  static authenticateToken(req, res, next) {
    const authHeader = req.headers.authorization;

    if (!authHeader) {
      return res.status(401).json({
        success: false,
        message: '访问被拒绝：缺少认证令牌',
        code: 'MISSING_TOKEN'
      });
    }

    const decoded = JWTManager.verifyToken(authHeader);

    if (!decoded) {
      return res.status(401).json({
        success: false,
        message: '认证令牌无效或已过期，请重新登录',
        code: 'INVALID_TOKEN'
      });
    }

    // 将用户信息添加到请求对象
    req.user = decoded;
    next();
  }

  /**
   * 增强的JWT认证中间件（包含黑名单检查）
   */
  static async authenticateTokenWithBlacklist(req, res, next) {
    const authHeader = req.headers.authorization;

    if (!authHeader) {
      return res.status(401).json({
        success: false,
        message: '访问被拒绝：缺少认证令牌',
        code: 'MISSING_TOKEN'
      });
    }

    const decoded = JWTManager.verifyToken(authHeader);

    if (!decoded) {
      return res.status(401).json({
        success: false,
        message: '认证令牌无效或已过期，请重新登录',
        code: 'INVALID_TOKEN'
      });
    }

    // 检查Token黑名单（需要Redis缓存服务）
    try {
      const token = authHeader.replace('Bearer ', '');
      const redisCache = req.app.locals.redisCache;

      if (redisCache && redisCache.isConnected()) {
        const isBlacklisted = await redisCache.get('blacklist_token', token);
        if (isBlacklisted) {
          return res.status(401).json({
            success: false,
            message: '认证令牌已失效，请重新登录',
            code: 'TOKEN_BLACKLISTED'
          });
        }
      }
    } catch (error) {
      console.warn('⚠️ 黑名单检查失败:', error.message);
      // 黑名单检查失败不影响正常认证流程
    }

    // 将用户信息添加到请求对象
    req.user = decoded;
    next();
  }

  /**
   * 可选认证中间件（支持匿名访问）
   */
  static optionalAuth(req, res, next) {
    const authHeader = req.headers.authorization;
    
    if (authHeader) {
      const decoded = JWTManager.verifyToken(authHeader);
      if (decoded) {
        req.user = decoded;
      }
    }
    
    // 如果没有Token或Token无效，创建匿名用户
    if (!req.user) {
      req.user = {
        userId: 'anonymous',
        username: 'anonymous',
        roles: ['ANONYMOUS'],
        permissions: ['chat:send', 'chat:view']
      };
    }
    
    next();
  }

  /**
   * 权限检查中间件
   */
  static requirePermission(permission) {
    return (req, res, next) => {
      if (!req.user) {
        return res.status(401).json({
          success: false,
          message: '未认证用户',
          code: 'UNAUTHENTICATED'
        });
      }

      const userPermissions = req.user.permissions || [];
      const userRoles = req.user.roles || [];

      // 管理员拥有所有权限
      if (userRoles.includes('ADMIN')) {
        return next();
      }

      // 检查具体权限
      if (!userPermissions.includes(permission)) {
        return res.status(403).json({
          success: false,
          message: '权限不足',
          code: 'INSUFFICIENT_PERMISSION',
          required_permission: permission
        });
      }

      next();
    };
  }
}

module.exports = {
  JWTManager,
  PasswordManager,
  InputValidator,
  SecurityMiddleware,
  SECURITY_CONFIG
};
