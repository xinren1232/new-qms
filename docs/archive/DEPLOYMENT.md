# QMS AI 部署指南

## 🚀 快速部署

### 前置要求
- Node.js 16+ 
- Python 3.8+
- Git

### 1. 克隆项目
```bash
git clone https://github.com/xinren1232/qmsai.git
cd qmsai
```

### 2. 启动服务

#### Windows 用户
```bash
# 运行启动脚本
start-qms.bat
```

#### Linux/Mac 用户
```bash
# 安装Python依赖
python -m venv .venv
source .venv/bin/activate

# 启动配置端Mock服务 (端口8081)
cd backend/nodejs
node config-center-mock.js &

# 启动聊天服务 (端口3002)  
node chat-service.js &

# 启动配置端前端 (端口8082)
cd ../../frontend/配置端
npm install
npm run dev &

# 启动应用端前端 (端口8080)
cd ../应用端
npm install
npm run dev &
```

### 3. 访问系统

| 服务 | 地址 | 说明 |
|------|------|------|
| **应用端** | http://localhost:8080 | 主要用户界面 |
| **配置端** | http://localhost:8082 | 管理配置界面 |
| **配置端Mock服务** | http://localhost:8081 | 后端API服务 |
| **聊天服务** | http://localhost:3002 | AI模型服务 |

### 4. 测试账户

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |
| developer | dev123 | 开发者 |
| tester | test123 | 测试员 |

## 🎯 功能验证

### 1. 登录验证
- 访问 http://localhost:8080
- 使用测试账户登录
- 验证用户权限和菜单显示

### 2. AI模型管理
- 进入"AI智能问答"页面
- 查看左侧模型管理面板
- 测试模型切换功能
- 验证特性指标显示

### 3. 智能问答
- 选择不同AI模型
- 发送测试消息
- 验证模型响应和特性

## 🔧 配置说明

### AI模型配置
编辑 `backend/nodejs/chat-service.js` 中的 `AI_CONFIGS` 配置：

```javascript
const AI_CONFIGS = {
  'gpt-4o': {
    apiKey: 'your-api-key',
    baseURL: 'your-api-endpoint',
    model: 'gpt-4o',
    // ... 其他配置
  }
}
```

### 端口配置
- 配置端Mock服务: 8081
- 配置端前端: 8082  
- 应用端前端: 8080
- 聊天服务: 3002

## 🐛 故障排除

### 常见问题

1. **端口占用**
   ```bash
   # 检查端口占用
   netstat -ano | findstr :8080
   # 杀死进程
   taskkill /PID <PID> /F
   ```

2. **依赖安装失败**
   ```bash
   # 清理缓存
   npm cache clean --force
   # 重新安装
   npm install
   ```

3. **模型切换失败**
   - 检查聊天服务是否正常运行
   - 验证代理配置是否正确
   - 查看浏览器控制台错误

4. **登录失败**
   - 确认配置端Mock服务正常运行
   - 检查认证API代理配置
   - 验证用户名密码正确性

## 📝 开发说明

### 项目结构
```
qmsai/
├── frontend/
│   ├── 配置端/          # 配置管理界面
│   └── 应用端/          # 用户操作界面
├── backend/
│   ├── nodejs/          # Node.js服务
│   └── springboot/      # Spring Boot服务
├── docs/               # 文档
└── deploy/            # 部署配置
```

### 开发模式
- 前端支持热重载
- 后端服务支持自动重启
- 代理配置支持跨域访问

## 🔒 安全注意事项

1. **API密钥管理**
   - 不要将真实API密钥提交到代码库
   - 使用环境变量管理敏感信息
   - 定期轮换API密钥

2. **访问控制**
   - 生产环境修改默认密码
   - 配置适当的网络访问策略
   - 启用HTTPS加密传输

3. **数据安全**
   - 定期备份重要数据
   - 监控异常访问行为
   - 实施数据加密存储

## 📞 技术支持

如遇到问题，请：
1. 查看本文档的故障排除部分
2. 检查GitHub Issues
3. 联系技术支持团队

---

**QMS AI Team** | 2025-01-31
