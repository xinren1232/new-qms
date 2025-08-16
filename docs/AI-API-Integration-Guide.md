# QMS AI集成开发指南

## 概述

本文档详细介绍了QMS系统中AI功能的集成方案，基于统一的ChatCompletion接口，支持多种AI模型和功能。

## API基础信息

### 基础配置
- **API端点**: `https://hk-intra-paas.transsion.com/tranai-proxy/v1`
- **API密钥**: `sk_a264854a38dc034077c23f5951285907e6c40d1b4843f46f95e5f31`
- **请求格式**: JSON
- **认证方式**: Bearer Token

### 支持的模型

| 模型ID | 名称 | 提供商 | 上下文长度 | 多模态 | 视觉 | 工具调用 | 流式 | 推理 |
|--------|------|--------|------------|--------|------|----------|------|------|
| `gpt-4o` | GPT-4o | Azure (OpenAI) | 128k/16k | ✅ | ✅ | ✅ | ✅ | ❌ |
| `o3` | O3 | Azure (OpenAI) | 200k/100k | ✅ | ❌ | ✅ | ✅ | ✅ |
| `gemini-2.5-pro-thinking` | Gemini 2.5 Pro Thinking | Google | 2000k | ✅ | ❌ | ✅ | ✅ | ✅ |
| `claude-3-7-sonnet@20250219` | Claude 3.7 Sonnet | Anthropic | 200k/8k | ❌ | ❌ | ✅ | ✅ | ❌ |
| `tran-ai/deepseek-r1` | DeepSeek R1 | DeepSeek | 128k/8k | ✅ | ❌ | ❌ | ✅ | ✅ |
| `deepseek-v3` | DeepSeek V3 | DeepSeek | 128k/8k | ❌ | ❌ | ❌ | ✅ | ❌ |

## API接口详解

### 1. 普通聊天接口

**端点**: `POST /api/chat`

**请求示例**:
```json
{
  "model": "gpt-4o",
  "messages": [
    {
      "role": "system",
      "content": "你是一个专业的质量管理系统助手。"
    },
    {
      "role": "user",
      "content": "请介绍ISO 9001的核心要求"
    }
  ],
  "max_tokens": 4096,
  "temperature": 0.7
}
```

**响应示例**:
```json
{
  "id": "chatcmpl-123456",
  "object": "chat.completion",
  "created": 1728933352,
  "model": "gpt-4o-2024-08-06",
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": "ISO 9001是国际标准化组织制定的质量管理体系标准...",
        "refusal": null
      },
      "logprobs": null,
      "finish_reason": "stop"
    }
  ],
  "usage": {
    "prompt_tokens": 19,
    "completion_tokens": 150,
    "total_tokens": 169
  }
}
```

### 2. 图片解析接口

**端点**: `POST /api/chat/vision`

**请求示例**:
```json
{
  "model": "gpt-4o",
  "messages": [
    {
      "role": "user",
      "content": [
        {
          "type": "text",
          "text": "请分析这张质量控制图表"
        },
        {
          "type": "image_url",
          "image_url": {
            "url": "https://example.com/control-chart.jpg"
          }
        }
      ]
    }
  ],
  "max_tokens": 4096
}
```

### 3. 流式响应接口

**端点**: `POST /api/chat/stream`

**请求示例**:
```json
{
  "model": "gpt-4o",
  "messages": [
    {
      "role": "user",
      "content": "详细解释PDCA循环"
    }
  ],
  "stream": true
}
```

**响应格式**:
```
data: {"id":"chatcmpl-123","object":"chat.completion.chunk","created":1694268190,"model":"gpt-4o","choices":[{"index":0,"delta":{"role":"assistant","content":""},"finish_reason":null}]}

data: {"id":"chatcmpl-123","object":"chat.completion.chunk","created":1694268190,"model":"gpt-4o","choices":[{"index":0,"delta":{"content":"PDCA"},"finish_reason":null}]}

data: [DONE]
```

### 4. 工具调用接口

**端点**: `POST /api/chat/tools`

**请求示例**:
```json
{
  "model": "gpt-4o",
  "messages": [
    {
      "role": "user",
      "content": "分析我们的缺陷率数据"
    }
  ],
  "tools": [
    {
      "type": "function",
      "function": {
        "name": "quality_analysis",
        "description": "分析质量数据并生成报告",
        "parameters": {
          "type": "object",
          "properties": {
            "data_type": {
              "type": "string",
              "enum": ["defect_rate", "customer_complaints", "process_capability"]
            },
            "time_period": {
              "type": "string",
              "description": "时间周期"
            }
          },
          "required": ["data_type", "time_period"]
        }
      }
    }
  ],
  "tool_choice": "auto"
}
```

**工具调用响应**:
```json
{
  "id": "chatcmpl-abc123",
  "object": "chat.completion",
  "created": 1699896916,
  "model": "gpt-4o",
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": null,
        "tool_calls": [
          {
            "id": "call_abc123",
            "type": "function",
            "function": {
              "name": "quality_analysis",
              "arguments": "{\"data_type\": \"defect_rate\", \"time_period\": \"2024-01\"}"
            }
          }
        ]
      },
      "finish_reason": "tool_calls"
    }
  ],
  "usage": {
    "prompt_tokens": 82,
    "completion_tokens": 17,
    "total_tokens": 99
  }
}
```

## 后端实现

### 模型配置
```javascript
const AI_CONFIGS = {
  'gpt-4o': {
    apiKey: 'sk_a264854a38dc034077c23f5951285907e6c40d1b4843f46f95e5f31',
    baseURL: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
    model: 'gpt-4o',
    maxTokens: 4096,
    temperature: 0.7,
    name: 'GPT-4o',
    provider: 'Azure (OpenAI)',
    features: {
      multimodal: true,
      vision: true,
      toolCalls: true,
      streaming: true
    }
  }
  // ... 其他模型配置
}
```

### API调用函数
```javascript
async function callAIAPI(messages, config, options = {}) {
  const requestBody = {
    model: config.model,
    messages: messages,
    max_tokens: options.maxTokens || config.maxTokens,
    temperature: options.temperature || config.temperature
  }

  if (options.stream) {
    requestBody.stream = true
  }

  if (options.tools && config.features.toolCalls) {
    requestBody.tools = options.tools
    requestBody.tool_choice = options.toolChoice || 'auto'
  }

  const response = await fetch(`${config.baseURL}/chat/completions`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${config.apiKey}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(requestBody)
  })

  return response.json()
}
```

## 前端集成

### API服务封装
```javascript
// api/ai-models.js
export function chat(data) {
  return request({
    url: '/api/chat',
    method: 'post',
    data
  })
}

export function chatWithVision(data) {
  return request({
    url: '/api/chat/vision',
    method: 'post',
    data
  })
}
```

### Vue组件使用
```vue
<script>
import { chat } from '@/api/ai-models'

export default {
  setup() {
    const sendMessage = async () => {
      const response = await chat({
        message: '你好',
        model: 'gpt-4o'
      })
      
      if (response.success) {
        console.log(response.data.response)
      }
    }
    
    return { sendMessage }
  }
}
</script>
```

## 质量管理专用工具

### 预设工具定义
1. **质量分析工具** (`quality_analysis`)
2. **缺陷分析工具** (`defect_analysis`)
3. **流程改进工具** (`process_improvement`)
4. **标准查询工具** (`standard_lookup`)

### 使用示例
```javascript
const tools = [
  {
    type: 'function',
    function: {
      name: 'quality_analysis',
      description: '分析质量数据并生成报告',
      parameters: {
        type: 'object',
        properties: {
          data_type: {
            type: 'string',
            enum: ['defect_rate', 'customer_complaints', 'process_capability']
          }
        }
      }
    }
  }
]
```

## 最佳实践

### 1. 模型选择建议
- **普通对话**: `gpt-4o` 或 `claude-3-7-sonnet`
- **图片分析**: `gpt-4o` (唯一支持视觉的模型)
- **复杂推理**: `o3` 或 `gemini-2.5-pro-thinking`
- **工具调用**: `gpt-4o`, `o3`, `gemini-2.5-pro-thinking`, `claude-3-7-sonnet`

### 2. 错误处理
```javascript
try {
  const response = await callAIAPI(messages, config)
  // 处理成功响应
} catch (error) {
  if (error.response?.status === 429) {
    // 处理速率限制
  } else if (error.response?.status === 401) {
    // 处理认证错误
  } else {
    // 处理其他错误
  }
}
```

### 3. 性能优化
- 使用流式响应提升用户体验
- 合理设置max_tokens避免过度消耗
- 实现请求缓存机制
- 监控API使用情况和成本

## 部署说明

### 环境变量配置
```bash
AI_API_KEY=sk_a264854a38dc034077c23f5951285907e6c40d1b4843f46f95e5f31
AI_BASE_URL=https://hk-intra-paas.transsion.com/tranai-proxy/v1
DEFAULT_MODEL=gpt-4o
```

### Docker部署
```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
EXPOSE 3000
CMD ["npm", "start"]
```

## 监控和日志

### 关键指标
- API响应时间
- Token使用量
- 错误率
- 模型切换频率

### 日志格式
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "level": "info",
  "message": "AI API调用成功",
  "model": "gpt-4o",
  "response_time": 1250,
  "tokens_used": 156,
  "user_id": "user123"
}
```

## 故障排除

### 常见问题
1. **401 Unauthorized**: 检查API密钥配置
2. **429 Too Many Requests**: 实现重试机制和速率限制
3. **模型不支持功能**: 检查模型功能支持表
4. **响应超时**: 调整超时设置和网络配置

### 调试工具
- API测试页面: `/config/ai-management/chat-test`
- 模型管理页面: `/config/ai-management/model-config`
- 日志查看: `/admin/logs`
