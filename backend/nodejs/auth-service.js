/**
 * QMS-AI 统一用户认证服务
 * 提供用户登录、注册、权限验证等功能
 * 确保用户数据独立保存和长期保存
 */

const express = require('express');
const cors = require('cors');
const bcrypt = require('bcrypt');
const { v4: uuidv4 } = require('uuid');
const { JWTManager, SecurityMiddleware } = require('./utils/security');
const DatabaseAdapter = require('./database/database-adapter');
const RedisCacheService = require('./services/redis-cache-service');

const app = express();
const PORT = process.env.AUTH_SERVICE_PORT || 8084;

// 中间件
app.use(cors({
  origin: true, // 允许所有来源，用于开发环境
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With', 'Accept', 'Origin'],
  optionsSuccessStatus: 200 // 支持旧版浏览器
}));
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// 明确处理OPTIONS预检请求
app.options('*', (req, res) => {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.header('Access-Control-Allow-Headers', 'Content-Type, Authorization, X-Requested-With, Accept, Origin');
  res.header('Access-Control-Allow-Credentials', 'true');
  res.sendStatus(200);
});

// 初始化数据库和缓存
const dbAdapter = new DatabaseAdapter();
const redisCache = new RedisCacheService();

// 初始化服务
async function initializeService() {
  try {
    console.log('🚀 启动QMS-AI认证服务...');

    // 连接数据库
    try {
      await dbAdapter.initialize();
      console.log('✅ 数据库连接成功');
    } catch (dbError) {
      console.warn('⚠️ 数据库连接失败，使用内存模式:', dbError.message);
    }
    
    // 连接Redis缓存（带超时）
    try {
      const connectPromise = redisCache.connect();
      const timeoutPromise = new Promise((_, reject) =>
        setTimeout(() => reject(new Error('Redis连接超时')), 5000)
      );

      await Promise.race([connectPromise, timeoutPromise]);
      console.log('✅ Redis缓存连接成功');
    } catch (error) {
      console.warn('⚠️ Redis连接失败，使用内存缓存:', error.message);
    }
    
    // 创建默认用户
    await createDefaultUsers();
    
  } catch (error) {
    console.error('❌ 服务初始化失败:', error);
    throw error; // 抛出错误让外层处理
  }
}

// 修复数据库表结构
async function fixDatabaseSchema() {
  try {
    console.log('🔧 检查并修复数据库表结构...');

    // 检查users表是否有password列
    const hasPasswordColumn = await new Promise((resolve) => {
      dbAdapter.primaryDB.db.all("PRAGMA table_info(users)", (err, rows) => {
        if (err) {
          console.error('检查表结构失败:', err.message);
          resolve(false);
          return;
        }

        const hasPassword = rows.some(row => row.name === 'password');
        resolve(hasPassword);
      });
    });

    if (!hasPasswordColumn) {
      console.log('⚠️ users表缺少password列，正在添加...');

      // 添加password列
      await new Promise((resolve, reject) => {
        dbAdapter.primaryDB.db.run('ALTER TABLE users ADD COLUMN password TEXT', (err) => {
          if (err && !err.message.includes('duplicate column')) {
            reject(err);
          } else {
            resolve();
          }
        });
      });

      // 添加其他可能缺失的列
      const columnsToAdd = [
        'permissions TEXT',
        'status TEXT DEFAULT "active"'
      ];

      for (const column of columnsToAdd) {
        await new Promise((resolve) => {
          dbAdapter.primaryDB.db.run(`ALTER TABLE users ADD COLUMN ${column}`, (err) => {
            if (err && !err.message.includes('duplicate column')) {
              console.warn(`添加列失败 ${column}:`, err.message);
            }
            resolve();
          });
        });
      }

      console.log('✅ 数据库表结构修复完成');
    } else {
      console.log('✅ 数据库表结构正常');
    }
  } catch (error) {
    console.error('❌ 修复数据库表结构失败:', error.message);
  }
}

// 创建默认用户
async function createDefaultUsers() {
  console.log('🔧 开始创建默认用户...');
  // 先修复数据库表结构
  await fixDatabaseSchema();
  console.log('✅ 数据库表结构修复完成');

  const defaultUsers = [
    {
      id: 'admin_001',
      username: 'admin',
      email: 'admin@qms.com',
      password: 'admin123',
      role: 'ADMIN',
      department: '系统管理部',
      permissions: ['*'], // 所有权限
      status: 'active'
    },
    {
      id: 'user_001',
      username: 'developer',
      email: 'developer@qms.com',
      password: 'dev123',
      role: 'DEVELOPER',
      department: '开发部',
      permissions: ['chat:send', 'chat:view', 'chat:history', 'config:read'],
      status: 'active'
    },
    {
      id: 'user_002',
      username: 'quality',
      email: 'quality@qms.com',
      password: 'quality123',
      role: 'QUALITY_ENGINEER',
      department: '质量管理部',
      permissions: ['chat:send', 'chat:view', 'chat:history', 'quality:manage'],
      status: 'active'
    }
  ];

  for (const userData of defaultUsers) {
    try {
      console.log(`🔍 检查用户是否存在: ${userData.username}`);
      // 检查用户是否已存在
      const existingUser = await dbAdapter.getUserById(userData.id);
      if (!existingUser) {
        console.log(`🔐 加密密码: ${userData.username}`);
        // 加密密码
        const hashedPassword = await bcrypt.hash(userData.password, 10);

        console.log(`💾 创建用户: ${userData.username}`);
        // 创建用户
        await dbAdapter.createUser({
          ...userData,
          password: hashedPassword,
          created_at: new Date().toISOString()
        });

        console.log(`✅ 创建默认用户: ${userData.username}`);
      } else {
        console.log(`⏭️ 用户已存在: ${userData.username}`);
      }
    } catch (error) {
      console.warn(`⚠️ 创建用户失败 ${userData.username}:`, error.message);
    }
  }
  console.log('✅ 默认用户创建完成');
}

// ==================== 认证API ====================

// 用户登录
app.post('/api/auth/login', async (req, res) => {
  try {
    const { username, password, rememberMe = false } = req.body;

    if (!username || !password) {
      return res.status(400).json({
        success: false,
        message: '用户名和密码不能为空'
      });
    }

    // 查找用户
    const user = await dbAdapter.getUserByUsername(username);
    if (!user) {
      return res.status(401).json({
        success: false,
        message: '用户名或密码错误'
      });
    }

    // 检查用户密码是否存在
    if (!user.password) {
      console.log(`⚠️ 用户 ${username} 的密码字段为空，重新创建用户`);

      // 根据用户名确定默认密码和角色
      let defaultPassword, role, department, permissions;
      switch (username) {
        case 'admin':
          defaultPassword = 'admin123';
          role = 'ADMIN';
          department = '系统管理部';
          permissions = ['*'];
          break;
        case 'developer':
          defaultPassword = 'dev123';
          role = 'DEVELOPER';
          department = '开发部';
          permissions = ['chat:send', 'chat:view', 'chat:history', 'config:read'];
          break;
        case 'quality':
          defaultPassword = 'quality123';
          role = 'QUALITY_ENGINEER';
          department = '质量管理部';
          permissions = ['chat:send', 'chat:view', 'chat:history', 'quality:manage'];
          break;
        default:
          return res.status(401).json({
            success: false,
            message: '用户名或密码错误'
          });
      }

      // 删除旧用户并重新创建
      await dbAdapter.deleteUser(user.id);

      // 加密新密码
      const hashedPassword = await bcrypt.hash(defaultPassword, 10);

      // 重新创建用户
      await dbAdapter.createUser({
        id: user.id,
        username: username,
        email: user.email || `${username}@qms.com`,
        password: hashedPassword,
        role: role,
        department: department,
        permissions: permissions,
        status: 'active',
        created_at: new Date().toISOString()
      });

      console.log(`✅ 重新创建用户: ${username}`);
      user.password = hashedPassword;
    }

    // 验证密码
    const isValidPassword = await bcrypt.compare(password, user.password);
    if (!isValidPassword) {
      return res.status(401).json({
        success: false,
        message: '用户名或密码错误'
      });
    }

    // 检查用户状态
    if (user.status !== 'active') {
      return res.status(401).json({
        success: false,
        message: '账户已被禁用，请联系管理员'
      });
    }

    // 生成JWT Token
    const token = JWTManager.generateToken(user);

    // 更新最后登录时间
    await dbAdapter.updateUserLastLogin(user.id);

    // 缓存用户会话
    if (redisCache.isConnected()) {
      await redisCache.set('user_session', user.id, {
        userId: user.id,
        username: user.username,
        loginTime: new Date().toISOString(),
        rememberMe
      }, rememberMe ? 86400 * 7 : 86400); // 7天或1天
    }

    // 返回用户信息（不包含密码）
    const { password: _, ...userInfo } = user;

    res.json({
      success: true,
      message: '登录成功',
      data: {
        token,
        user: userInfo,
        permissions: user.permissions || [],
        roles: [user.role]
      }
    });

    console.log(`✅ 用户登录成功: ${username} (${user.id})`);

  } catch (error) {
    console.error('❌ 登录失败:', error);
    res.status(500).json({
      success: false,
      message: '登录处理失败，请稍后重试'
    });
  }
});

// 用户注册
app.post('/api/auth/register', async (req, res) => {
  try {
    const { username, email, password, department = '质量管理部' } = req.body;

    if (!username || !email || !password) {
      return res.status(400).json({
        success: false,
        message: '用户名、邮箱和密码不能为空'
      });
    }

    // 检查用户名是否已存在
    const existingUser = await dbAdapter.getUserByUsername(username);
    if (existingUser) {
      return res.status(409).json({
        success: false,
        message: '用户名已存在'
      });
    }

    // 检查邮箱是否已存在
    const existingEmail = await dbAdapter.getUserByEmail(email);
    if (existingEmail) {
      return res.status(409).json({
        success: false,
        message: '邮箱已被注册'
      });
    }

    // 加密密码
    const hashedPassword = await bcrypt.hash(password, 10);

    // 创建用户
    const newUser = {
      id: uuidv4(),
      username,
      email,
      password: hashedPassword,
      role: 'USER',
      department,
      permissions: ['chat:send', 'chat:view', 'chat:history'],
      status: 'active',
      created_at: new Date().toISOString()
    };

    await dbAdapter.createUser(newUser);

    // 返回成功信息（不包含密码）
    const { password: _, ...userInfo } = newUser;

    res.status(201).json({
      success: true,
      message: '注册成功',
      data: {
        user: userInfo
      }
    });

    console.log(`✅ 用户注册成功: ${username} (${newUser.id})`);

  } catch (error) {
    console.error('❌ 注册失败:', error);
    res.status(500).json({
      success: false,
      message: '注册处理失败，请稍后重试'
    });
  }
});

// 获取用户信息
app.get('/api/auth/userinfo', SecurityMiddleware.authenticateToken, async (req, res) => {
  try {
    const user = await dbAdapter.getUserById(req.user.userId);
    if (!user) {
      return res.status(404).json({
        success: false,
        message: '用户不存在'
      });
    }

    // 返回用户信息（不包含密码）
    const { password: _, ...userInfo } = user;

    res.json({
      success: true,
      message: '获取用户信息成功',
      data: {
        ...userInfo,
        permissions: user.permissions ? JSON.parse(user.permissions) : [],
        roles: [user.role]
      }
    });
  } catch (error) {
    console.error('❌ 获取用户信息失败:', error);
    res.status(500).json({
      success: false,
      message: '获取用户信息失败'
    });
  }
});

// 验证Token
app.post('/api/auth/verify', SecurityMiddleware.authenticateToken, (req, res) => {
  res.json({
    success: true,
    message: 'Token有效',
    data: {
      user: req.user
    }
  });
});

// 刷新Token
app.post('/api/auth/refresh', (req, res) => {
  try {
    const { token } = req.body;
    
    if (!token) {
      return res.status(400).json({
        success: false,
        message: '缺少刷新令牌'
      });
    }

    const newToken = JWTManager.refreshToken(token);
    
    if (!newToken) {
      return res.status(401).json({
        success: false,
        message: '刷新令牌无效或已过期'
      });
    }

    res.json({
      success: true,
      message: 'Token刷新成功',
      data: {
        token: newToken
      }
    });

  } catch (error) {
    console.error('❌ Token刷新失败:', error);
    res.status(500).json({
      success: false,
      message: 'Token刷新失败'
    });
  }
});

// 用户登出
app.post('/api/auth/logout', SecurityMiddleware.authenticateToken, async (req, res) => {
  try {
    const token = req.headers.authorization.replace('Bearer ', '');

    // 将Token加入黑名单（如果Redis可用）
    if (redisCache.isConnected()) {
      const decoded = JWTManager.verifyToken(req.headers.authorization);
      if (decoded) {
        const expiresIn = decoded.exp - Math.floor(Date.now() / 1000);
        await redisCache.set('blacklist_token', token, true, expiresIn);
      }
    }

    // 清除用户会话缓存
    if (redisCache.isConnected()) {
      await redisCache.delete('user_session', req.user.userId);
    }

    res.json({
      success: true,
      message: '退出登录成功'
    });

    console.log(`✅ 用户退出登录: ${req.user.username} (${req.user.userId})`);

  } catch (error) {
    console.error('❌ 退出登录失败:', error);
    res.status(500).json({
      success: false,
      message: '退出登录失败'
    });
  }
});

// 检查权限
app.post('/api/auth/check-permission', SecurityMiddleware.authenticateToken, async (req, res) => {
  try {
    const { permission } = req.body;

    if (!permission) {
      return res.status(400).json({
        success: false,
        message: '权限参数不能为空'
      });
    }

    const user = await dbAdapter.getUserById(req.user.userId);
    if (!user) {
      return res.status(404).json({
        success: false,
        message: '用户不存在'
      });
    }

    // 管理员拥有所有权限
    if (user.role === 'ADMIN') {
      return res.json({
        success: true,
        data: { hasPermission: true }
      });
    }

    // 检查用户权限
    const userPermissions = user.permissions ? JSON.parse(user.permissions) : [];
    const hasPermission = userPermissions.includes(permission) || userPermissions.includes('*');

    res.json({
      success: true,
      data: { hasPermission }
    });

  } catch (error) {
    console.error('❌ 权限检查失败:', error);
    res.status(500).json({
      success: false,
      message: '权限检查失败'
    });
  }
});

// 获取用户权限列表
app.get('/api/auth/permissions', SecurityMiddleware.authenticateToken, async (req, res) => {
  try {
    const user = await dbAdapter.getUserById(req.user.userId);
    if (!user) {
      return res.status(404).json({
        success: false,
        message: '用户不存在'
      });
    }

    const permissions = user.permissions ? JSON.parse(user.permissions) : [];

    res.json({
      success: true,
      data: {
        permissions,
        role: user.role
      }
    });

  } catch (error) {
    console.error('❌ 获取权限列表失败:', error);
    res.status(500).json({
      success: false,
      message: '获取权限列表失败'
    });
  }
});

// 用户登出
app.post('/api/auth/logout', SecurityMiddleware.authenticateToken, async (req, res) => {
  try {
    const userId = req.user.userId;

    // 清除Redis缓存
    if (redisCache.isConnected()) {
      await redisCache.del('user_session', userId);
    }

    res.json({
      success: true,
      message: '登出成功'
    });

    console.log(`✅ 用户登出: ${userId}`);

  } catch (error) {
    console.error('❌ 登出失败:', error);
    res.status(500).json({
      success: false,
      message: '登出处理失败'
    });
  }
});

// 获取用户信息
app.get('/api/auth/user', SecurityMiddleware.authenticateToken, async (req, res) => {
  try {
    const userId = req.user.userId;
    const user = await dbAdapter.getUserById(userId);

    if (!user) {
      return res.status(404).json({
        success: false,
        message: '用户不存在'
      });
    }

    // 返回用户信息（不包含密码）
    const { password: _, ...userInfo } = user;

    res.json({
      success: true,
      data: {
        user: userInfo
      }
    });

  } catch (error) {
    console.error('❌ 获取用户信息失败:', error);
    res.status(500).json({
      success: false,
      message: '获取用户信息失败'
    });
  }
});

// 健康检查
app.get('/health', (req, res) => {
  res.json({
    status: 'healthy',
    service: 'QMS-AI认证服务',
    timestamp: new Date().toISOString(),
    database: dbAdapter.isConnected() ? 'connected' : 'disconnected',
    redis: redisCache.isConnected() ? 'connected' : 'disconnected'
  });
});

// 启动服务
console.log('🔄 开始启动认证服务...');

// 设置服务类型环境变量
process.env.SERVICE_TYPE = 'auth';

initializeService().then(() => {
  console.log('✅ 服务初始化完成，开始监听端口...');

  const server = app.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 QMS-AI认证服务启动成功`);
    console.log(`📍 服务地址: http://localhost:${PORT}`);
    console.log(`🏥 健康检查: http://localhost:${PORT}/health`);
    console.log(`🔐 登录接口: http://localhost:${PORT}/api/auth/login`);
    console.log(`🔧 进程ID: ${process.pid}`);
  });

  server.on('error', (error) => {
    console.error('❌ 服务器启动错误:', error);
    if (error.code === 'EADDRINUSE') {
      console.error(`❌ 端口 ${PORT} 已被占用，请检查是否有其他进程使用此端口`);
    }
    process.exit(1);
  });

  server.on('listening', () => {
    const addr = server.address();
    console.log(`✅ 服务器正在监听 ${addr.address}:${addr.port}`);
  });

  // 优雅关闭
  process.on('SIGTERM', () => {
    console.log('🔄 收到SIGTERM信号，正在关闭服务器...');
    server.close(() => {
      console.log('✅ 认证服务已关闭');
      process.exit(0);
    });
  });

  process.on('SIGINT', () => {
    console.log('🔄 收到SIGINT信号，正在关闭服务器...');
    server.close(() => {
      console.log('✅ 认证服务已关闭');
      process.exit(0);
    });
  });

}).catch((error) => {
  console.error('❌ 认证服务启动失败:', error);
  console.error('❌ 错误堆栈:', error.stack);
  process.exit(1);
});

module.exports = app;
