/**
 * AI模型管理相关API - Vue3版本
 */
import request from '@/utils/request'

// AI模型相关类型定义
export interface ModelInfo {
  id: string
  name: string
  description: string
  provider: string
  status: 'active' | 'inactive' | 'maintenance'
  capabilities: string[]
  maxTokens: number
  costPerToken: number
}

export interface ChatMessage {
  role: 'user' | 'assistant' | 'system'
  content: string
  timestamp?: string
}

export interface ChatRequest {
  message: string
  conversation_id?: string | null
  model: string
  temperature?: number
  max_tokens?: number
  tools?: any[]
  tool_choice?: string
}

export interface VisionChatRequest extends ChatRequest {
  image_url: string
}

export interface ModelConfig {
  temperature: number
  max_tokens: number
  top_p: number
  frequency_penalty: number
  presence_penalty: number
}

export interface UsageStats {
  total_requests: number
  total_tokens: number
  average_response_time: number
  success_rate: number
  cost: number
}

// 获取可用模型列表
export function getModels(): Promise<ModelInfo[]> {
  return request({
    url: '/api/models',
    method: 'get'
  })
}

// 切换默认模型
export function switchModel(modelId: string) {
  return request({
    url: '/api/models/switch',
    method: 'post',
    data: {
      model_id: modelId
    }
  })
}

// 普通聊天
export function chat(data: ChatRequest) {
  return request({
    url: '/api/chat',
    method: 'post',
    data
  })
}

// 图片解析聊天
export function chatWithVision(data: VisionChatRequest) {
  return request({
    url: '/api/chat/vision',
    method: 'post',
    data
  })
}

// 工具调用聊天
export function chatWithTools(data: ChatRequest) {
  return request({
    url: '/api/chat/tools',
    method: 'post',
    data
  })
}

// 流式聊天（返回EventSource）
export function chatStream(data: ChatRequest): EventSource {
  return new EventSource('/api/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)
  } as any)
}

// 获取聊天历史
export function getChatHistory(params?: any) {
  return request({
    url: '/api/chat/history',
    method: 'get',
    params
  })
}

// 获取聊天统计
export function getChatStats(): Promise<UsageStats> {
  return request({
    url: '/api/chat/stats',
    method: 'get'
  })
}

// 清空聊天历史
export function clearChatHistory() {
  return request({
    url: '/api/chat/history',
    method: 'delete'
  })
}

// 删除特定对话
export function deleteConversation(conversationId: string) {
  return request({
    url: `/api/chat/conversation/${conversationId}`,
    method: 'delete'
  })
}

// 导出聊天历史
export function exportChatHistory(params?: any) {
  return request({
    url: '/api/chat/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 模型性能测试
export function testModel(data: any) {
  return request({
    url: '/api/models/test',
    method: 'post',
    data
  })
}

// 批量测试模型
export function batchTestModels(data: any) {
  return request({
    url: '/api/models/batch-test',
    method: 'post',
    data
  })
}

// 获取模型使用统计
export function getModelUsageStats(params?: any): Promise<UsageStats> {
  return request({
    url: '/api/models/usage-stats',
    method: 'get',
    params
  })
}

// 模型配置更新
export function updateModelConfig(modelId: string, config: ModelConfig) {
  return request({
    url: `/api/models/${modelId}/config`,
    method: 'put',
    data: config
  })
}

// 获取模型详细信息
export function getModelDetails(modelId: string): Promise<ModelInfo> {
  return request({
    url: `/api/models/${modelId}`,
    method: 'get'
  })
}

// 检查模型健康状态
export function checkModelHealth(modelId: string) {
  return request({
    url: `/api/models/${modelId}/health`,
    method: 'get'
  })
}

// 获取模型支持的功能
export function getModelFeatures(modelId: string) {
  return request({
    url: `/api/models/${modelId}/features`,
    method: 'get'
  })
}

// 工具定义类型
export interface ToolDefinition {
  type: 'function'
  function: {
    name: string
    description: string
    parameters: {
      type: 'object'
      properties: Record<string, any>
      required: string[]
    }
  }
}

// 预设工具定义
export const PRESET_TOOLS: Record<string, ToolDefinition> = {
  // 质量管理工具
  quality_analysis: {
    type: 'function',
    function: {
      name: 'quality_analysis',
      description: '分析质量数据并生成报告',
      parameters: {
        type: 'object',
        properties: {
          data_type: {
            type: 'string',
            enum: ['defect_rate', 'customer_complaints', 'process_capability'],
            description: '数据类型'
          },
          time_period: {
            type: 'string',
            description: '时间周期，如"2024-01"'
          },
          analysis_method: {
            type: 'string',
            enum: ['spc', 'pareto', 'fishbone', '5why'],
            description: '分析方法'
          }
        },
        required: ['data_type', 'time_period']
      }
    }
  },

  // 缺陷分析工具
  defect_analysis: {
    type: 'function',
    function: {
      name: 'defect_analysis',
      description: '执行缺陷根因分析',
      parameters: {
        type: 'object',
        properties: {
          defect_description: {
            type: 'string',
            description: '缺陷描述'
          },
          analysis_method: {
            type: 'string',
            enum: ['8d', 'fishbone', '5why', 'fmea'],
            description: '分析方法'
          },
          severity: {
            type: 'string',
            enum: ['low', 'medium', 'high', 'critical'],
            description: '严重程度'
          }
        },
        required: ['defect_description', 'analysis_method']
      }
    }
  },

  // 流程改进工具
  process_improvement: {
    type: 'function',
    function: {
      name: 'process_improvement',
      description: '生成流程改进建议',
      parameters: {
        type: 'object',
        properties: {
          process_name: {
            type: 'string',
            description: '流程名称'
          },
          current_issues: {
            type: 'array',
            items: { type: 'string' },
            description: '当前问题列表'
          },
          improvement_method: {
            type: 'string',
            enum: ['lean', 'six_sigma', 'kaizen', 'pdca'],
            description: '改进方法'
          }
        },
        required: ['process_name', 'current_issues']
      }
    }
  },

  // 标准查询工具
  standard_lookup: {
    type: 'function',
    function: {
      name: 'standard_lookup',
      description: '查询质量标准和规范',
      parameters: {
        type: 'object',
        properties: {
          standard_type: {
            type: 'string',
            enum: ['iso_9001', 'iso_14001', 'iso_45001', 'iatf_16949', 'as9100'],
            description: '标准类型'
          },
          query_topic: {
            type: 'string',
            description: '查询主题'
          },
          industry: {
            type: 'string',
            description: '行业类型'
          }
        },
        required: ['standard_type', 'query_topic']
      }
    }
  }
}

// 获取预设工具列表
export function getPresetTools() {
  return Object.keys(PRESET_TOOLS).map(key => ({
    id: key,
    name: PRESET_TOOLS[key].function.name,
    description: PRESET_TOOLS[key].function.description,
    definition: PRESET_TOOLS[key]
  }))
}

// 示例请求格式
export const EXAMPLE_REQUESTS = {
  // 普通聊天示例
  basic_chat: {
    message: "请介绍一下ISO 9001质量管理体系的核心要求",
    conversation_id: null,
    model: "gpt-4o"
  },

  // 图片解析示例
  vision_chat: {
    message: "请分析这张质量控制图表，识别其中的趋势和异常点",
    image_url: "https://example.com/control-chart.jpg",
    conversation_id: null,
    model: "gpt-4o"
  },

  // 工具调用示例
  tools_chat: {
    message: "请分析我们产品的缺陷率数据，时间周期是2024年1月",
    tools: [PRESET_TOOLS.quality_analysis],
    tool_choice: "auto",
    conversation_id: null,
    model: "gpt-4o"
  },

  // 流式响应示例
  stream_chat: {
    message: "请详细解释PDCA循环在质量改进中的应用",
    conversation_id: null,
    model: "gpt-4o"
  }
}
