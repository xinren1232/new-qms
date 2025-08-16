# QMS系统API文档 v2.0

**更新日期**: 2025-01-31  
**API版本**: v2.0  
**支持模型**: 8个AI模型  
**API端点**: 33个

## 🌐 基础信息

### 服务地址
- **统一入口**: http://localhost:8080/api/
- **AI聊天服务**: http://localhost:3004/
- **配置中心服务**: http://localhost:3003/

### 认证方式
- **Token认证**: Bearer Token
- **会话管理**: Session + JWT
- **权限控制**: 基于角色的访问控制 (RBAC)

## 🤖 AI模型管理 API

### 1. 获取模型列表
```http
GET /api/models
```

**响应示例**:
```json
{
  "success": true,
  "data": [
    {
      "id": "gpt-4o",
      "name": "GPT-4o",
      "provider": "Azure (OpenAI)",
      "capabilities": {
        "reasoning": false,
        "tools": true,
        "vision": true,
        "streaming": true
      },
      "status": "active"
    },
    {
      "id": "o3",
      "name": "O3",
      "provider": "Azure (OpenAI)",
      "capabilities": {
        "reasoning": true,
        "tools": true,
        "vision": false,
        "streaming": true
      },
      "status": "active"
    }
  ]
}
```

### 2. 切换默认模型
```http
POST /api/models/switch
Content-Type: application/json

{
  "modelId": "gpt-4o"
}
```

### 3. 模型健康检查
```http
GET /api/models/{modelId}/health
```

### 4. 模型测试
```http
POST /api/models/test
Content-Type: application/json

{
  "modelId": "gpt-4o",
  "message": "测试消息"
}
```

### 5. 批量模型测试
```http
POST /api/models/batch-test
Content-Type: application/json

{
  "models": ["gpt-4o", "claude-3.7-sonnet"],
  "message": "批量测试消息"
}
```

### 6. 模型使用统计
```http
GET /api/models/usage-stats
```

## 💬 AI聊天 API

### 1. 普通聊天
```http
POST /api/chat/send
Content-Type: application/json

{
  "message": "你好，请介绍一下质量管理的基本概念",
  "modelId": "gpt-4o",
  "conversationId": "conv_123456789",
  "context": {
    "temperature": 0.7,
    "maxTokens": 2000
  }
}
```

### 2. 图像识别聊天
```http
POST /api/chat/vision
Content-Type: application/json

{
  "message": "请分析这张质量控制图表",
  "imageUrl": "https://example.com/chart.jpg",
  "modelId": "gpt-4o",
  "conversationId": "conv_123456789"
}
```

### 3. 流式响应聊天
```http
POST /api/chat/stream
Content-Type: application/json

{
  "message": "请详细解释ISO 9001标准",
  "modelId": "gpt-4o",
  "conversationId": "conv_123456789"
}
```

**响应格式**: Server-Sent Events (SSE)
```
data: {"type": "start", "conversationId": "conv_123456789"}
data: {"type": "content", "content": "ISO 9001是..."}
data: {"type": "end", "messageId": "msg_123456789"}
```

### 4. 工具调用聊天
```http
POST /api/chat/tools
Content-Type: application/json

{
  "message": "帮我计算质量成本",
  "modelId": "gpt-4o",
  "conversationId": "conv_123456789",
  "tools": [
    {
      "name": "calculator",
      "description": "数学计算工具"
    }
  ]
}
```

### 5. 获取对话历史
```http
GET /api/chat/conversations?page=1&limit=20
```

### 6. 删除对话
```http
DELETE /api/chat/conversations/{conversationId}
```

### 7. 导出对话记录
```http
POST /api/chat/export
Content-Type: application/json

{
  "conversationIds": ["conv_123", "conv_456"],
  "format": "pdf",
  "includeMetadata": true
}
```

### 8. 下载导出文件
```http
GET /api/chat/download/{filename}
```

## 📊 监控统计 API

### 1. 总体统计
```http
GET /api/ai-monitoring/statistics/overall
```

**响应示例**:
```json
{
  "success": true,
  "data": {
    "totalRequests": 15420,
    "totalUsers": 156,
    "totalConversations": 2340,
    "averageResponseTime": 1.2,
    "successRate": 98.5,
    "topModels": [
      {"name": "GPT-4o", "usage": 45.2},
      {"name": "Claude 3.7", "usage": 28.7}
    ]
  }
}
```

### 2. 用户统计
```http
GET /api/ai-monitoring/statistics/users?period=7d
```

### 3. 模型统计
```http
GET /api/ai-monitoring/statistics/models?period=30d
```

### 4. 仪表板数据
```http
GET /api/ai-monitoring/dashboard
```

### 5. 实时监控指标
```http
GET /api/ai-monitoring/realtime-metrics
```

**响应示例**:
```json
{
  "success": true,
  "data": {
    "activeUsers": 23,
    "currentRequests": 5,
    "systemLoad": 0.65,
    "memoryUsage": 0.78,
    "responseTime": 1.1,
    "errorRate": 0.02
  }
}
```

## 🔐 用户认证 API

### 1. 用户登录
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password123"
}
```

### 2. 用户注册
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "newuser",
  "password": "password123",
  "email": "user@example.com",
  "role": "user"
}
```

### 3. 刷新Token
```http
POST /api/auth/refresh
Authorization: Bearer {refresh_token}
```

### 4. 用户登出
```http
POST /api/auth/logout
Authorization: Bearer {access_token}
```

### 5. 获取用户信息
```http
GET /api/auth/profile
Authorization: Bearer {access_token}
```

## ⚙️ 系统配置 API

### 1. 获取系统配置
```http
GET /api/config/system
```

### 2. 更新系统配置
```http
PUT /api/config/system
Content-Type: application/json

{
  "aiSettings": {
    "defaultModel": "gpt-4o",
    "maxTokens": 2000,
    "temperature": 0.7
  },
  "systemSettings": {
    "enableLogging": true,
    "logLevel": "info"
  }
}
```

### 3. 获取AI模型配置
```http
GET /api/config/ai-models
```

### 4. 更新AI模型配置
```http
PUT /api/config/ai-models/{modelId}
Content-Type: application/json

{
  "enabled": true,
  "apiKey": "sk-...",
  "baseUrl": "https://api.openai.com/v1",
  "maxTokens": 4000,
  "temperature": 0.7
}
```

## 🏥 健康检查 API

### 1. 系统健康检查
```http
GET /api/health
```

**响应示例**:
```json
{
  "status": "healthy",
  "timestamp": "2025-01-31T10:30:00Z",
  "services": {
    "chatService": "healthy",
    "configService": "healthy",
    "database": "healthy",
    "aiModels": {
      "gpt-4o": "healthy",
      "claude-3.7": "healthy",
      "deepseek-r1": "degraded"
    }
  },
  "metrics": {
    "uptime": 86400,
    "memoryUsage": 0.75,
    "cpuUsage": 0.45
  }
}
```

### 2. 服务状态检查
```http
GET /api/health/services
```

### 3. 数据库连接检查
```http
GET /api/health/database
```

## 📝 错误码说明

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 200 | 请求成功 | - |
| 400 | 请求参数错误 | 检查请求参数格式 |
| 401 | 未授权访问 | 检查Token是否有效 |
| 403 | 权限不足 | 联系管理员分配权限 |
| 404 | 资源不存在 | 检查请求路径 |
| 429 | 请求频率过高 | 降低请求频率 |
| 500 | 服务器内部错误 | 联系技术支持 |
| 503 | 服务不可用 | 稍后重试或联系技术支持 |

## 🔧 使用示例

### JavaScript/Axios示例
```javascript
// 发送聊天请求
const response = await axios.post('/api/chat/send', {
  message: '请介绍质量管理体系',
  modelId: 'gpt-4o',
  conversationId: 'conv_' + Date.now()
}, {
  headers: {
    'Authorization': 'Bearer ' + token,
    'Content-Type': 'application/json'
  }
})

console.log(response.data)
```

### Python/Requests示例
```python
import requests

# 获取模型列表
response = requests.get('http://localhost:8080/api/models', 
                       headers={'Authorization': f'Bearer {token}'})
models = response.json()
print(models)
```

## 📊 性能指标

- **平均响应时间**: < 2秒
- **并发支持**: 100+ 用户
- **可用性**: 99.9%
- **错误率**: < 1%
- **吞吐量**: 1000+ 请求/分钟

## 🔄 版本更新记录

### v2.0 (2025-01-31)
- ✅ 新增2个AI模型 (DeepSeek Chat, DeepSeek Reasoner)
- ✅ 增加监控统计API (5个端点)
- ✅ 优化流式响应性能
- ✅ 增强错误处理和日志记录

### v1.0 (2025-01-15)
- ✅ 基础AI聊天功能
- ✅ 6个AI模型集成
- ✅ 用户认证和权限管理
- ✅ 基础监控功能

通过这份API文档，开发者可以快速了解和使用QMS系统的所有功能接口。
