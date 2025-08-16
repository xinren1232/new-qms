/**
 * QMS-AI 最小认证服务
 * 快速解决登录问题
 */

const express = require('express');
const cors = require('cors');

const app = express();
const PORT = 8084;

// 中间件
app.use(cors({
  origin: true,
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With', 'Accept', 'Origin']
}));

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// 处理OPTIONS预检请求
app.options('*', (req, res) => {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.header('Access-Control-Allow-Headers', 'Content-Type, Authorization, X-Requested-With, Accept, Origin');
  res.header('Access-Control-Allow-Credentials', 'true');
  res.sendStatus(200);
});

// 模拟用户数据
const users = [
  {
    id: 'admin_001',
    username: 'admin',
    password: 'admin123',
    realName: '系统管理员',
    email: 'admin@qms.com',
    roles: ['ADMIN'],
    permissions: ['*']
  },
  {
    id: 'user_001', 
    username: 'user',
    password: 'user123',
    realName: '普通用户',
    email: 'user@qms.com',
    roles: ['USER'],
    permissions: ['read']
  }
];

// 登录接口
app.post('/api/auth/login', (req, res) => {
  console.log('🔐 收到登录请求:', req.body);
  
  const { username, password } = req.body;
  
  if (!username || !password) {
    return res.status(400).json({
      success: false,
      message: '用户名和密码不能为空'
    });
  }
  
  // 查找用户
  const user = users.find(u => u.username === username && u.password === password);
  
  if (!user) {
    return res.status(401).json({
      success: false,
      message: '用户名或密码错误'
    });
  }
  
  // 生成简单token
  const token = `qms_token_${user.id}_${Date.now()}`;
  
  console.log('✅ 登录成功:', user.username);
  
  res.json({
    success: true,
    message: '登录成功',
    data: {
      token,
      user: {
        id: user.id,
        username: user.username,
        realName: user.realName,
        email: user.email,
        roles: user.roles,
        permissions: user.permissions
      }
    }
  });
});

// 获取用户信息
app.get('/api/auth/userinfo', (req, res) => {
  const token = req.headers.authorization?.replace('Bearer ', '');
  
  if (!token) {
    return res.status(401).json({
      success: false,
      message: '未提供认证token'
    });
  }
  
  // 简单验证token格式
  if (!token.startsWith('qms_token_')) {
    return res.status(401).json({
      success: false,
      message: '无效的token'
    });
  }
  
  // 从token中提取用户ID
  const userId = token.split('_')[2];
  const user = users.find(u => u.id === userId);
  
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
      realName: user.realName,
      email: user.email,
      roles: user.roles,
      permissions: user.permissions
    }
  });
});

// 登出接口
app.post('/api/auth/logout', (req, res) => {
  res.json({
    success: true,
    message: '登出成功'
  });
});

// 健康检查
app.get('/health', (req, res) => {
  res.json({
    status: 'healthy',
    service: 'QMS-AI最小认证服务',
    timestamp: new Date().toISOString()
  });
});

// 启动服务
app.listen(PORT, () => {
  console.log('🚀 QMS-AI最小认证服务启动成功');
  console.log(`📍 服务地址: http://localhost:${PORT}`);
  console.log(`🏥 健康检查: http://localhost:${PORT}/health`);
  console.log(`🔐 登录接口: http://localhost:${PORT}/api/auth/login`);
  console.log(`👤 测试账户: admin/admin123 或 user/user123`);
});
