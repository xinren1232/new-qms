/**
 * 快速认证服务 - 解决登录问题
 */

const express = require('express');
const cors = require('cors');

const app = express();
const PORT = 8084;

console.log('🚀 启动快速认证服务...');

// 中间件
app.use(cors({
  origin: true,
  credentials: true
}));
app.use(express.json());

// 用户数据
const users = {
  admin: { id: '1', username: 'admin', password: 'admin123', role: 'admin' }
};

// 健康检查
app.get('/health', (req, res) => {
  console.log('📊 健康检查请求');
  res.json({ status: 'ok', service: '快速认证服务' });
});

// 登录
app.post('/api/auth/login', (req, res) => {
  console.log('🔐 登录请求:', req.body);
  
  const { username, password } = req.body;
  const user = users[username];
  
  if (user && user.password === password) {
    const token = Buffer.from(JSON.stringify({ userId: user.id, username })).toString('base64');
    console.log('✅ 登录成功:', username);
    
    res.json({
      success: true,
      message: '登录成功',
      data: {
        token,
        user: { id: user.id, username: user.username, role: user.role }
      }
    });
  } else {
    console.log('❌ 登录失败:', username);
    res.status(401).json({
      success: false,
      message: '用户名或密码错误'
    });
  }
});

// 用户信息
app.get('/api/auth/userinfo', (req, res) => {
  console.log('👤 用户信息请求');
  
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ success: false, message: '未提供token' });
  }
  
  try {
    const token = authHeader.substring(7);
    const decoded = JSON.parse(Buffer.from(token, 'base64').toString());
    const user = users[decoded.username];
    
    if (user) {
      res.json({
        success: true,
        data: { id: user.id, username: user.username, role: user.role }
      });
    } else {
      res.status(401).json({ success: false, message: '用户不存在' });
    }
  } catch (error) {
    res.status(401).json({ success: false, message: 'token无效' });
  }
});

// 登出
app.post('/api/auth/logout', (req, res) => {
  console.log('🚪 登出请求');
  res.json({ success: true, message: '登出成功' });
});

// 启动服务
app.listen(PORT, () => {
  console.log('✅ 快速认证服务启动成功！');
  console.log(`📍 地址: http://localhost:${PORT}`);
  console.log(`🔐 登录: POST http://localhost:${PORT}/api/auth/login`);
  console.log(`👤 账号: admin / admin123`);
}).on('error', (err) => {
  console.error('❌ 启动失败:', err.message);
  if (err.code === 'EADDRINUSE') {
    console.log(`端口 ${PORT} 已被占用，请检查其他服务`);
  }
});
