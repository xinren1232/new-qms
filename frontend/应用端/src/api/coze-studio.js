/**
 * Coze Studio API 客户端
 * 提供Agent、Workflow、Knowledge、Plugin等功能的API调用
 */
import request from '@/utils/request'

const COZE_STUDIO_BASE_URL = '/api/coze-studio'

// ================================
// Agent 管理 API
// ================================

/**
 * 获取Agent列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.limit - 每页数量
 * @param {string} params.search - 搜索关键词
 * @param {string} params.category - 分类
 */
export function getAgents(params = {}) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/agents`,
    method: 'get',
    params
  })
}

/**
 * 创建新Agent
 * @param {Object} data - Agent数据
 * @param {string} data.name - Agent名称
 * @param {string} data.description - Agent描述
 * @param {string} data.avatar - Agent头像
 * @param {string} data.category - Agent分类
 * @param {Object} data.config - Agent配置
 * @param {Object} data.metadata - Agent元数据
 */
export function createAgent(data) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/agents`,
    method: 'post',
    data
  })
}

/**
 * 获取Agent详情
 * @param {string} id - Agent ID
 */
export function getAgent(id) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/agents/${id}`,
    method: 'get'
  })
}

/**
 * 更新Agent
 * @param {string} id - Agent ID
 * @param {Object} data - 更新数据
 */
export function updateAgent(id, data) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/agents/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除Agent
 * @param {string} id - Agent ID
 */
export function deleteAgent(id) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/agents/${id}`,
    method: 'delete'
  })
}

/**
 * 复制Agent
 * @param {string} id - Agent ID
 * @param {Object} data - 复制数据
 * @param {string} data.name - 新Agent名称
 */
export function cloneAgent(id, data) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/agents/${id}/clone`,
    method: 'post',
    data
  })
}

/**
 * 发布Agent
 * @param {string} id - Agent ID
 */
export function publishAgent(id) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/agents/${id}/publish`,
    method: 'post'
  })
}

/**
 * 测试Agent
 * @param {string} id - Agent ID
 * @param {Object} data - 测试数据
 * @param {string} data.message - 测试消息
 */
export function testAgent(id, data) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/agents/${id}/test`,
    method: 'post',
    data
  })
}

// ================================
// Agent 对话 API
// ================================

/**
 * 创建Agent对话会话
 * @param {string} agentId - Agent ID
 * @param {Object} data - 会话数据
 * @param {string} data.title - 会话标题
 */
export function createAgentConversation(agentId, data) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/agents/${agentId}/conversations`,
    method: 'post',
    data
  })
}

/**
 * Agent对话
 * @param {string} agentId - Agent ID
 * @param {Object} data - 对话数据
 * @param {string} data.message - 用户消息
 * @param {string} data.conversation_id - 对话会话ID
 */
export function chatWithAgent(agentId, data) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/agents/${agentId}/chat`,
    method: 'post',
    data
  })
}

/**
 * 获取Agent对话历史
 * @param {string} agentId - Agent ID
 * @param {string} conversationId - 对话会话ID
 * @param {Object} params - 查询参数
 */
export function getAgentMessages(agentId, conversationId, params = {}) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/agents/${agentId}/conversations/${conversationId}/messages`,
    method: 'get',
    params
  })
}

/**
 * 获取Agent的所有对话会话
 * @param {string} agentId - Agent ID
 * @param {Object} params - 查询参数
 */
export function getAgentConversations(agentId, params = {}) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/agents/${agentId}/conversations`,
    method: 'get',
    params
  })
}

/**
 * 删除对话会话
 * @param {string} agentId - Agent ID
 * @param {string} conversationId - 对话会话ID
 */
export function deleteAgentConversation(agentId, conversationId) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/agents/${agentId}/conversations/${conversationId}`,
    method: 'delete'
  })
}

/**
 * 清空对话历史
 * @param {string} agentId - Agent ID
 * @param {string} conversationId - 对话会话ID
 */
export function clearAgentMessages(agentId, conversationId) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/agents/${agentId}/conversations/${conversationId}/messages`,
    method: 'delete'
  })
}

// ================================
// Workflow 管理 API (待实现)
// ================================

/**
 * 获取Workflow列表
 */
export function getWorkflows(params = {}) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/workflows`,
    method: 'get',
    params
  })
}

/**
 * 创建新Workflow
 */
export function createWorkflow(data) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/workflows`,
    method: 'post',
    data
  })
}

/**
 * 获取工作流详情
 * @param {string} id - 工作流ID
 */
export function getWorkflow(id) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/workflows/${id}`,
    method: 'get'
  })
}

/**
 * 执行Workflow
 */
export function executeWorkflow(id, data) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/workflows/${id}/execute`,
    method: 'post',
    data
  })
}

/** 文档解析场景工作流执行（异步：返回 execution_id 需轮询）*/
export function executeDocumentParsingWorkflow(payload){
  return request({
    url: `/api/workflows/document-parsing/execute`,
    method: 'post',
    data: payload
  })
}

/** 文档解析执行（同步直返结果，便于聊天页即时展示）*/
export function executeDocumentParsingDirect(payload){
  return request({
    url: `/api/workflows/execute/document-parsing`,
    method: 'post',
    data: payload
  })
}

/** 查询文档解析执行结果（用于轮询） */
export function getDocumentParsingExecutionStatus(executionId){
  return request({
    url: `/api/workflows/document-parsing/executions/${executionId}`,
    method: 'get'
  })
}

/** 通用工作流运行（按JSON定义） */
export function runWorkflowDefinition(payload){
  return request({
    url: `${COZE_STUDIO_BASE_URL}/workflows/run`,
    method: 'post',
    data: payload
  })
}


/**
 * 获取工作流执行状态
 * @param {string} executionId - 执行ID
 */
export function getWorkflowExecution(executionId) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/workflows/executions/${executionId}`,
    method: 'get'
  })
}

// ================================
// Knowledge 管理 API (待实现)
// ================================

/**
 * 获取知识库列表
 */
export function getKnowledgeBases(params = {}) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/knowledge`,
    method: 'get',
    params
  })
}

/**
 * 创建知识库
 */
export function createKnowledgeBase(data) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/knowledge`,
    method: 'post',
    data
  })
}

/**
 * 上传文档到知识库
 */
export function uploadDocument(knowledgeBaseId, data) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/knowledge/${knowledgeBaseId}/documents`,
    method: 'post',
    data
  })
}

/**
 * 知识库检索
 * @param {string} id - 知识库ID
 * @param {Object} data - 检索数据
 * @param {string} data.query - 检索查询
 * @param {number} data.top_k - 返回结果数量
 * @param {number} data.threshold - 相似度阈值
 */
export function searchKnowledge(id, data) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/knowledge/${id}/search`,
    method: 'post',
    data
  })
}


/**
 * 轻量知识入库 (M1)
 * @param {Object} data
 * @param {string} data.fileName
 * @param {string} data.fileType - MIME 或简名，如 application/pdf, text, csv
 * @param {string} [data.base64] - 文件Base64
 * @param {string} [data.text] - 纯文本
 * @param {string} [data.saveScope='user'] - 'user' | 'session'
 * @param {string} [data.conversation_id]
 */
export function ingestKnowledge(data) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/knowledge/ingest`,
    method: 'post',
    data
  })
}

/**
 * 轻量知识检索 (M1)
 * @param {Object} data
 * @param {string} data.query
 * @param {number} [data.top_k=3]
 * @param {string} [data.kb_scope='user'] - 'user' | 'session'
 * @param {string} [data.conversation_id]
 */
export function searchKnowledgeLite(data) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/knowledge/search`,
    method: 'post',
    data
  })
}

/**
 * 从对话消息入库到知识库
 * @param {Object} data
 * @param {string} data.conversation_id
 * @param {Array<{role:string,content:string}>} data.messages
 * @param {string} [data.saveScope='user']
 * @param {string} [data.conversation_scope_id]
 */
export function ingestFromMessages(data) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/knowledge/ingest-from-messages`,
    method: 'post',
    data
  })
}

/** Traces: 追加执行过程记录 */
export function appendTrace(data) {
  return request({
    url: `/api/traces/append`,
    method: 'post',
    data
  })
}

/** Traces: 获取执行过程记录 */
export function getTraces(params) {
  return request({
    url: `/api/traces`,
    method: 'get',
    params
  })
}


/** 工作流：获取案例应用指导模板 */
export function getCaseGuidanceWorkflowTemplate() {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/workflows/templates/case-guidance`,
    method: 'get'
  })
}

/** 工作流：执行案例应用指导 */
export function executeCaseGuidanceWorkflow(data) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/workflows/case-guidance/execute`,
    method: 'post',
    data
  })
}


// ================================
// AutoGPT 自主规划 API
// ================================

/**
 * 创建AutoGPT规划任务
 * @param {Object} data - 规划数据
 * @param {string} data.goal - 目标描述
 * @param {Array} data.constraints - 约束条件
 * @param {Object} data.config - 配置参数
 */
export function createAutoGPTPlan(data) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/autogpt/plans`,
    method: 'post',
    data
  })
}

/**
 * 获取AutoGPT规划列表
 * @param {Object} params - 查询参数
 */
export function getAutoGPTPlans(params = {}) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/autogpt/plans`,
    method: 'get',
    params
  })
}

/**
 * 获取AutoGPT规划详情
 * @param {string} planId - 规划ID
 */
export function getAutoGPTPlan(planId) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/autogpt/plans/${planId}`,
    method: 'get'
  })
}

/**
 * 执行AutoGPT规划
 * @param {string} planId - 规划ID
 * @param {Object} data - 执行参数
 */
export function executeAutoGPTPlan(planId, data = {}) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/autogpt/plans/${planId}/execute`,
    method: 'post',
    data
  })
}

/**
 * 获取AutoGPT执行状态
 * @param {string} executionId - 执行ID
 */
export function getAutoGPTExecution(executionId) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/autogpt/executions/${executionId}`,
    method: 'get'
  })
}

// ================================
// CrewAI 多智能体协作 API
// ================================

/**
 * 创建Agent团队
 * @param {Object} data - 团队数据
 * @param {string} data.name - 团队名称
 * @param {string} data.description - 团队描述
 * @param {Array} data.agents - Agent列表
 * @param {string} data.collaboration_mode - 协作模式
 */
export function createCrew(data) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/crewai/crews`,
    method: 'post',
    data
  })
}

/**
 * 获取Agent团队列表
 * @param {Object} params - 查询参数
 */
export function getCrews(params = {}) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/crewai/crews`,
    method: 'get',
    params
  })
}

/**
 * 执行团队任务
 * @param {string} crewId - 团队ID
 * @param {Object} data - 任务数据
 * @param {Object} data.task - 任务描述
 * @param {Object} data.context - 上下文信息
 */
export function executeCrewTask(crewId, data) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/crewai/crews/${crewId}/execute`,
    method: 'post',
    data
  })
}

/**
 * 获取Agent角色模板
 */
export function getAgentRoles() {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/crewai/agent-roles`,
    method: 'get'
  })
}

// ================================
// LangChain Memory 记忆系统 API
// ================================

/**
 * 为Agent创建记忆系统
 * @param {string} agentId - Agent ID
 * @param {Object} data - 记忆配置
 */
export function createAgentMemory(agentId, data = {}) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/memory/agents/${agentId}/create`,
    method: 'post',
    data
  })
}

/**
 * 添加对话记忆
 * @param {string} agentId - Agent ID
 * @param {Object} data - 对话数据
 * @param {string} data.conversationId - 对话ID
 * @param {string} data.message - 用户消息
 * @param {string} data.response - Agent回复
 */
export function addConversationMemory(agentId, data) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/memory/agents/${agentId}/conversations`,
    method: 'post',
    data
  })
}

/**
 * 检索相关记忆
 * @param {string} agentId - Agent ID
 * @param {Object} data - 检索参数
 * @param {string} data.query - 查询内容
 * @param {Object} data.options - 检索选项
 */
export function retrieveMemory(agentId, data) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/memory/agents/${agentId}/retrieve`,
    method: 'post',
    data
  })
}

/**
 * 获取Agent记忆统计
 * @param {string} agentId - Agent ID
 */
export function getMemoryStats(agentId) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/memory/agents/${agentId}/stats`,
    method: 'get'
  })
}

/**
 * 清理Agent过期记忆
 * @param {string} agentId - Agent ID
 * @param {number} retentionDays - 保留天数
 */
export function cleanupMemory(agentId, retentionDays = 30) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/memory/agents/${agentId}/cleanup`,
    method: 'delete',
    params: { retentionDays }
  })
}

// ================================
// 插件生态系统 API
// ================================

/**
 * 获取插件列表
 * @param {Object} params - 查询参数
 * @param {string} params.type - 插件类型
 * @param {string} params.category - 插件分类
 * @param {string} params.status - 插件状态
 */
export function getPlugins(params = {}) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/plugins`,
    method: 'get',
    params
  })
}

/**
 * 获取插件详情
 * @param {string} pluginId - 插件ID
 */
export function getPlugin(pluginId) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/plugins/${pluginId}`,
    method: 'get'
  })
}

/**
 * 安装插件
 * @param {string} pluginId - 插件ID
 * @param {Object} config - 插件配置
 */
export function installPlugin(pluginId, config = {}) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/plugins/${pluginId}/install`,
    method: 'post',
    data: { config }
  })
}

/**
 * 执行插件
 * @param {string} pluginId - 插件ID
 * @param {Object} data - 执行数据
 * @param {Object} data.input - 输入数据
 * @param {Object} data.options - 执行选项
 */
export function executePlugin(pluginId, data) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/plugins/${pluginId}/execute`,
    method: 'post',
    data
  })
}

/**
 * 卸载插件
 * @param {string} pluginId - 插件ID
 */
export function uninstallPlugin(pluginId) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/plugins/${pluginId}/uninstall`,
    method: 'delete'
  })
}

/**
 * 获取插件统计信息
 */
export function getPluginStats() {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/plugins/stats`,
    method: 'get'
  })
}

// ================================
// 项目管理 API (待实现)
// ================================

/**
 * 获取项目列表
 */
export function getProjects(params = {}) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/projects`,
    method: 'get',
    params
  })
}

/**
 * 创建项目
 */
export function createProject(data) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/projects`,
    method: 'post',
    data
  })
}

// ================================
// 系统配置 API
// ================================

/**
 * 获取可用模型列表
 */
export function getAvailableModels() {
  return request({
    url: '/api/ai/models',
    method: 'get'
  })
}

/**
 * 健康检查
 */
export function healthCheck() {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/health`,
    method: 'get'
  })
}

// ================================
// Model Integration API
// ================================

/**
 * 获取可用的AI模型列表
 */
export function getModels() {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/models`,
    method: 'get'
  })
}

/**
 * 测试模型连接
 * @param {string} modelId - 模型ID
 * @param {Object} data - 测试数据
 * @param {string} data.message - 测试消息
 */
export function testModel(modelId, data) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/models/${modelId}/test`,
    method: 'post',
    data
  })
}


// ================================
// 场景验证 API
// ================================
export function executeQualityAnalysisScenario(data) {
  return request({
    url: `${COZE_STUDIO_BASE_URL}/scenarios/quality-analysis`,
    method: 'post',
    data
  })
}

// 导出所有API
export default {
  // Agent相关
  getAgents,
  createAgent,
  getAgent,
  updateAgent,
  deleteAgent,
  cloneAgent,
  publishAgent,
  testAgent,
  createAgentConversation,
  chatWithAgent,
  getAgentMessages,
  getAgentConversations,
  deleteAgentConversation,
  clearAgentMessages,

  // Workflow相关
  getWorkflows,
  createWorkflow,
  getWorkflow,
  executeWorkflow,
  getWorkflowExecution,
  executeDocumentParsingWorkflow,
  executeDocumentParsingDirect,
  getDocumentParsingExecutionStatus,


  // Knowledge相关
  getKnowledgeBases,
  createKnowledgeBase,
  uploadDocument,
  searchKnowledge,

  // AutoGPT相关
  createAutoGPTPlan,
  getAutoGPTPlans,
  getAutoGPTPlan,
  executeAutoGPTPlan,
  getAutoGPTExecution,

  // CrewAI相关
  createCrew,
  getCrews,
  executeCrewTask,
  getAgentRoles,

  // Knowledge M1 轻量接口
  ingestKnowledge,
  searchKnowledgeLite,
  ingestFromMessages,

  // Traces 接口
  appendTrace,
  getTraces,

  // Workflows
  getCaseGuidanceWorkflowTemplate,
  executeCaseGuidanceWorkflow,

  // LangChain Memory相关
  createAgentMemory,
  addConversationMemory,
  retrieveMemory,
  getMemoryStats,
  cleanupMemory,

  // 插件生态系统相关
  getPlugins,
  getPlugin,
  installPlugin,
  executePlugin,
  uninstallPlugin,
  getPluginStats,

  // 项目管理
  getProjects,
  createProject,

  // 模型集成
  getModels,
  testModel,

  // 系统配置
  // 系统配置
  getAvailableModels,
  healthCheck
}
