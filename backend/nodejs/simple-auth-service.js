/**
 * QMS-AI 简化认证服务
 * 专门用于配置端的管理员登录
 */

const express = require('express');
const cors = require('cors');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');

const app = express();
const PORT = 8084;

// JWT密钥
const JWT_SECRET = 'qms-ai-secret-key-2024';

// 中间件
app.use(cors({
  origin: ['http://localhost:8072', 'http://localhost:8080', 'http://localhost:3000'],
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With']
}));
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// 简化的用户数据（内存存储）
const users = [
  {
    id: 'admin_001',
    username: 'admin',
    email: 'admin@qms.com',
    password: '$2b$10$rOzJqQqQqQqQqQqQqQqQqOzJqQqQqQqQqQqQqQqQqOzJqQqQqQqQqQ', // admin123
    role: 'ADMIN',
    department: '系统管理部',
    permissions: ['*'],
    status: 'active'
  }
];

// 预先计算密码哈希
async function initializeUsers() {
  const hashedPassword = await bcrypt.hash('admin123', 10);
  users[0].password = hashedPassword;
  console.log('✅ 管理员用户初始化完成');
}

// 健康检查
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    service: 'QMS-AI认证服务',
    timestamp: new Date().toISOString()
  });
});

// 用户登录
app.post('/api/auth/login', async (req, res) => {
  try {
    const { username, password } = req.body;

    if (!username || !password) {
      return res.status(400).json({
        success: false,
        message: '用户名和密码不能为空'
      });
    }

    // 查找用户
    const user = users.find(u => u.username === username);
    if (!user) {
      return res.status(401).json({
        success: false,
        message: '用户名或密码错误'
      });
    }

    // 验证密码
    const isPasswordValid = await bcrypt.compare(password, user.password);
    if (!isPasswordValid) {
      return res.status(401).json({
        success: false,
        message: '用户名或密码错误'
      });
    }

    // 生成JWT Token
    const token = jwt.sign(
      {
        userId: user.id,
        username: user.username,
        email: user.email,
        role: user.role,
        permissions: user.permissions
      },
      JWT_SECRET,
      { expiresIn: '24h' }
    );

    console.log(`✅ 用户登录成功: ${username} (${user.id})`);

    res.json({
      success: true,
      message: '登录成功',
      data: {
        token,
        user: {
          id: user.id,
          username: user.username,
          email: user.email,
          role: user.role,
          department: user.department,
          permissions: user.permissions
        }
      }
    });

  } catch (error) {
    console.error('❌ 登录失败:', error);
    res.status(500).json({
      success: false,
      message: '服务器内部错误'
    });
  }
});

// 获取用户信息
app.get('/api/auth/userinfo', (req, res) => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({
      success: false,
      message: '未提供有效的认证令牌'
    });
  }

  try {
    const token = authHeader.substring(7);
    const decoded = jwt.verify(token, JWT_SECRET);
    
    const user = users.find(u => u.id === decoded.userId);
    if (!user) {
      return res.status(401).json({
        success: false,
        message: '用户不存在'
      });
    }

    res.json({
      success: true,
      data: {
        id: user.id,
        username: user.username,
        email: user.email,
        role: user.role,
        department: user.department,
        permissions: user.permissions
      }
    });

  } catch (error) {
    console.error('❌ 获取用户信息失败:', error);
    res.status(401).json({
      success: false,
      message: '无效的认证令牌'
    });
  }
});

// 用户登出
app.post('/api/auth/logout', (req, res) => {
  res.json({
    success: true,
    message: '登出成功'
  });
});

// 启动服务
async function startService() {
  try {
    await initializeUsers();
    
    app.listen(PORT, () => {
      console.log('🚀 QMS-AI简化认证服务启动成功');
      console.log(`📍 服务地址: http://localhost:${PORT}`);
      console.log(`🏥 健康检查: http://localhost:${PORT}/health`);
      console.log(`🔐 登录接口: http://localhost:${PORT}/api/auth/login`);
      console.log(`👤 管理员账户: admin / admin123`);
    });
    
  } catch (error) {
    console.error('❌ 服务启动失败:', error);
    process.exit(1);
  }
}

startService();
