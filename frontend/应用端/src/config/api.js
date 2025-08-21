/**
 * QMS-AI 统一API配置
 * 支持网关模式和直连模式的自动切换
 */

// 环境配置
const isDevelopment = process.env.NODE_ENV === 'development';
const isProduction = process.env.NODE_ENV === 'production';

// 网关配置
const USE_GATEWAY = process.env.VUE_APP_USE_GATEWAY !== 'false';
const GATEWAY_URL = process.env.VUE_APP_GATEWAY_URL || 'http://localhost:8085';

// 服务端点配置
const SERVICE_PORTS = {
  redis: 6379,
  config: 3003,
  chat: 3004,
  coze: 3005,
  evaluation: 3006,
  export: 3008,
  advanced: 3009,
  monitor: 3010,
  auth: 8084,
  gateway: 8085,
  frontend: 8081
};

// 统一网关模式端点
const GATEWAY_ENDPOINTS = {
  // 核心服务
  chat: `${GATEWAY_URL}/api/chat`,
  config: `${GATEWAY_URL}/api/config`,
  auth: `${GATEWAY_URL}/api/auth`,
  coze: `${GATEWAY_URL}/api/coze`,
  
  // 扩展服务
  export: `${GATEWAY_URL}/api/export`,
  evaluation: `${GATEWAY_URL}/api/evaluation`,
  advanced: `${GATEWAY_URL}/api/advanced`,
  monitor: `${GATEWAY_URL}/api/monitor`,
  
  // WebSocket
  configSync: `ws://localhost:${SERVICE_PORTS.gateway}/config-sync`,
  
  // 静态资源
  static: `${GATEWAY_URL}/static`,
  uploads: `${GATEWAY_URL}/uploads`
};

// 直连模式端点（向后兼容）
const DIRECT_ENDPOINTS = {
  // 核心服务
  chat: `http://localhost:${SERVICE_PORTS.chat}`,
  config: `http://localhost:${SERVICE_PORTS.config}`,
  auth: `http://localhost:${SERVICE_PORTS.auth}`,
  coze: `http://localhost:${SERVICE_PORTS.coze}`,
  
  // 扩展服务
  export: `http://localhost:${SERVICE_PORTS.export}`,
  evaluation: `http://localhost:${SERVICE_PORTS.evaluation}`,
  advanced: `http://localhost:${SERVICE_PORTS.advanced}`,
  monitor: `http://localhost:${SERVICE_PORTS.monitor}`,
  
  // WebSocket
  configSync: `ws://localhost:${SERVICE_PORTS.config}/config-sync`,
  
  // 静态资源
  static: `http://localhost:${SERVICE_PORTS.frontend}/static`,
  uploads: `http://localhost:${SERVICE_PORTS.chat}/uploads`
};

// 选择使用的端点配置
const API_ENDPOINTS = USE_GATEWAY ? GATEWAY_ENDPOINTS : DIRECT_ENDPOINTS;

// API路径配置
const API_PATHS = {
  // 认证相关
  auth: {
    login: '/api/auth/login',
    logout: '/api/auth/logout',
    refresh: '/api/auth/refresh',
    profile: '/api/auth/profile'
  },
  
  // 聊天相关
  chat: {
    send: '/api/chat/send',
    conversations: '/api/chat/conversations',
    messages: '/api/chat/messages',
    models: '/api/chat/models',
    statistics: '/api/chat/statistics',
    modelStatus: '/api/chat/model-status'
  },
  
  // 配置相关
  config: {
    models: '/api/configs/ai_models',
    services: '/api/configs/services',
    system: '/api/configs/system',
    sync: '/api/events/config-changes'
  },
  
  // Coze Studio相关
  coze: {
    workflows: '/api/workflows',
    plugins: '/api/plugins',
    agents: '/api/agents',
    execute: '/api/workflows/execute'
  },
  
  // 导出相关
  export: {
    conversations: '/api/export',
    download: '/api/download',
    cleanup: '/api/cleanup'
  },
  
  // 评估相关
  evaluation: {
    evaluate: '/api/evaluation',
    results: '/api/evaluation/results',
    metrics: '/api/evaluation/metrics'
  },
  
  // 高级功能
  advanced: {
    analytics: '/api/analytics',
    recommendations: '/api/recommendations',
    collaboration: '/api/collaboration',
    integration: '/api/integration',
    dashboard: '/api/dashboard'
  },
  
  // 监控相关
  monitor: {
    services: '/api/services',
    metrics: '/api/metrics',
    overview: '/api/overview',
    health: '/health'
  }
};

// 构建完整URL的辅助函数
function buildUrl(service, path) {
  const baseUrl = API_ENDPOINTS[service];
  if (!baseUrl) {
    console.warn(`未知的服务: ${service}`);
    return path;
  }
  
  // 如果path已经包含完整URL，直接返回
  if (path.startsWith('http')) {
    return path;
  }
  
  // 如果使用网关模式且path已经包含/api前缀，去掉重复
  if (USE_GATEWAY && path.startsWith('/api/')) {
    return `${baseUrl}${path.substring(4)}`;
  }
  
  return `${baseUrl}${path}`;
}

// 服务健康检查配置
const HEALTH_CHECK = {
  interval: 30000, // 30秒检查一次
  timeout: 5000,   // 5秒超时
  retries: 3,      // 重试3次
  services: Object.keys(API_ENDPOINTS).filter(key => !['static', 'uploads', 'configSync'].includes(key))
};

// 错误重试配置
const RETRY_CONFIG = {
  maxRetries: 3,
  retryDelay: 1000,
  retryCondition: (error) => {
    // 网络错误或5xx服务器错误时重试
    return !error.response || (error.response.status >= 500 && error.response.status < 600);
  }
};

// 超时配置
const TIMEOUT_CONFIG = {
  default: 0,        // 解除默认超时限制
  upload: 60000,     // 上传60秒
  download: 120000,  // 下载120秒
  longRunning: 0,    // 解除长时间运行任务超时限制
  chat: 0            // 聊天请求解除超时限制
};

// 导出配置
export {
  // 基础配置
  USE_GATEWAY,
  GATEWAY_URL,
  SERVICE_PORTS,
  
  // 端点配置
  API_ENDPOINTS,
  GATEWAY_ENDPOINTS,
  DIRECT_ENDPOINTS,
  
  // 路径配置
  API_PATHS,
  
  // 辅助函数
  buildUrl,
  
  // 其他配置
  HEALTH_CHECK,
  RETRY_CONFIG,
  TIMEOUT_CONFIG,
  
  // 环境信息
  isDevelopment,
  isProduction
};

// 向后兼容的默认导出
export default {
  baseURL: USE_GATEWAY ? GATEWAY_URL : API_ENDPOINTS.chat,
  endpoints: API_ENDPOINTS,
  paths: API_PATHS,
  buildUrl,
  healthCheck: HEALTH_CHECK,
  retry: RETRY_CONFIG,
  timeout: TIMEOUT_CONFIG
};

// 控制台输出当前配置（仅开发环境）
if (isDevelopment) {
  console.log('🔧 QMS-AI API配置:', {
    mode: USE_GATEWAY ? '网关模式' : '直连模式',
    gateway: GATEWAY_URL,
    endpoints: API_ENDPOINTS,
    healthCheck: HEALTH_CHECK.interval / 1000 + '秒'
  });
}
