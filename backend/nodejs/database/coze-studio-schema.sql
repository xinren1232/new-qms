-- Coze Studio 数据库表结构
-- 创建时间: 2025-08-06

-- ================================
-- Agent 相关表
-- ================================

-- Agent 主表
CREATE TABLE IF NOT EXISTS agents (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    avatar TEXT,
    category TEXT DEFAULT 'general',
    config TEXT, -- JSON格式的Agent配置
    metadata TEXT, -- JSON格式的元数据
    status TEXT DEFAULT 'draft', -- draft, published, archived
    version INTEGER DEFAULT 1,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT
);

-- Agent 版本历史表
CREATE TABLE IF NOT EXISTS agent_versions (
    id TEXT PRIMARY KEY,
    agent_id TEXT NOT NULL,
    version INTEGER NOT NULL,
    config TEXT, -- JSON格式的配置快照
    metadata TEXT,
    created_by TEXT NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY (agent_id) REFERENCES agents(id)
);

-- Agent 对话会话表
CREATE TABLE IF NOT EXISTS agent_conversations (
    id TEXT PRIMARY KEY,
    agent_id TEXT NOT NULL,
    user_id TEXT NOT NULL,
    title TEXT,
    context TEXT, -- JSON格式的对话上下文
    status TEXT DEFAULT 'active', -- active, archived, deleted
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (agent_id) REFERENCES agents(id)
);

-- Agent 对话消息表
CREATE TABLE IF NOT EXISTS agent_messages (
    id TEXT PRIMARY KEY,
    conversation_id TEXT NOT NULL,
    role TEXT NOT NULL, -- user, assistant, system
    content TEXT NOT NULL,
    metadata TEXT, -- JSON格式，包含模型信息、token使用等
    created_at TEXT NOT NULL,
    FOREIGN KEY (conversation_id) REFERENCES agent_conversations(id)
);

-- ================================
-- Workflow 相关表
-- ================================

-- 工作流主表
CREATE TABLE IF NOT EXISTS workflows (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    category TEXT DEFAULT 'general',
    definition TEXT, -- JSON格式的工作流定义
    config TEXT, -- JSON格式的配置
    status TEXT DEFAULT 'draft', -- draft, published, archived
    version INTEGER DEFAULT 1,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT
);

-- 工作流执行记录表
CREATE TABLE IF NOT EXISTS workflow_executions (
    id TEXT PRIMARY KEY,
    workflow_id TEXT NOT NULL,
    user_id TEXT NOT NULL,
    input_data TEXT, -- JSON格式的输入数据
    output_data TEXT, -- JSON格式的输出数据
    status TEXT DEFAULT 'running', -- running, completed, failed, cancelled
    error_message TEXT,
    started_at TEXT NOT NULL,
    completed_at TEXT,
    FOREIGN KEY (workflow_id) REFERENCES workflows(id)
);

-- 工作流节点执行日志表
CREATE TABLE IF NOT EXISTS workflow_node_logs (
    id TEXT PRIMARY KEY,
    execution_id TEXT NOT NULL,
    node_id TEXT NOT NULL,
    node_type TEXT NOT NULL,
    input_data TEXT,
    output_data TEXT,
    status TEXT DEFAULT 'running',
    error_message TEXT,
    started_at TEXT NOT NULL,
    completed_at TEXT,
    FOREIGN KEY (execution_id) REFERENCES workflow_executions(id)
);

-- ================================
-- Knowledge 相关表
-- ================================

-- 知识库主表
CREATE TABLE IF NOT EXISTS knowledge_bases (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    type TEXT DEFAULT 'document', -- document, qa, structured
    config TEXT, -- JSON格式的配置
    status TEXT DEFAULT 'active',
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT
);

-- 知识库文档表
CREATE TABLE IF NOT EXISTS knowledge_documents (
    id TEXT PRIMARY KEY,
    knowledge_base_id TEXT NOT NULL,
    name TEXT NOT NULL,
    content TEXT,
    file_path TEXT,
    file_type TEXT,
    file_size INTEGER,
    metadata TEXT, -- JSON格式的元数据
    status TEXT DEFAULT 'processing', -- processing, indexed, failed
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (knowledge_base_id) REFERENCES knowledge_bases(id)
);

-- 知识库向量索引表
CREATE TABLE IF NOT EXISTS knowledge_vectors (
    id TEXT PRIMARY KEY,
    document_id TEXT NOT NULL,
    chunk_index INTEGER NOT NULL,
    content TEXT NOT NULL,
    vector TEXT, -- JSON格式的向量数据
    metadata TEXT,
    created_at TEXT NOT NULL,
    FOREIGN KEY (document_id) REFERENCES knowledge_documents(id)
);

-- ================================
-- Plugin 相关表
-- ================================

-- 插件主表
CREATE TABLE IF NOT EXISTS plugins (
    id TEXT PRIMARY KEY,
    user_id TEXT,
    name TEXT NOT NULL,
    description TEXT,
    category TEXT DEFAULT 'tool',
    type TEXT DEFAULT 'api', -- api, code, builtin, langchain_tool, semantic_kernel, haystack_component, custom_plugin
    author TEXT DEFAULT 'Unknown',
    capabilities TEXT, -- JSON格式的能力列表
    config_schema TEXT, -- JSON格式的配置模式
    config TEXT, -- JSON格式的插件配置
    code TEXT, -- 插件代码（如果是代码类型）
    status TEXT DEFAULT 'active', -- active, inactive, error
    version TEXT DEFAULT '1.0.0',
    usage_count INTEGER DEFAULT 0,
    last_used TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT
);

-- 插件执行日志表
CREATE TABLE IF NOT EXISTS plugin_executions (
    id TEXT PRIMARY KEY,
    plugin_id TEXT NOT NULL,
    user_id TEXT NOT NULL,
    input_data TEXT,
    output_data TEXT,
    status TEXT DEFAULT 'running',
    error_message TEXT,
    execution_time INTEGER, -- 执行时间（毫秒）
    created_at TEXT NOT NULL,
    FOREIGN KEY (plugin_id) REFERENCES plugins(id)
);

-- ================================
-- 项目管理表
-- ================================

-- 项目主表
CREATE TABLE IF NOT EXISTS projects (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    type TEXT NOT NULL, -- agent, workflow, knowledge, plugin
    resource_id TEXT, -- 关联的资源ID
    config TEXT, -- JSON格式的项目配置
    status TEXT DEFAULT 'active',
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    deleted_at TEXT
);

-- ================================
-- 索引创建
-- ================================

-- Agent 相关索引
CREATE INDEX IF NOT EXISTS idx_agents_user_id ON agents(user_id);
CREATE INDEX IF NOT EXISTS idx_agents_status ON agents(status);
CREATE INDEX IF NOT EXISTS idx_agents_category ON agents(category);
CREATE INDEX IF NOT EXISTS idx_agents_created_at ON agents(created_at);

-- Agent 对话相关索引
CREATE INDEX IF NOT EXISTS idx_agent_conversations_agent_id ON agent_conversations(agent_id);
CREATE INDEX IF NOT EXISTS idx_agent_conversations_user_id ON agent_conversations(user_id);
CREATE INDEX IF NOT EXISTS idx_agent_messages_conversation_id ON agent_messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_agent_messages_created_at ON agent_messages(created_at);

-- Workflow 相关索引
CREATE INDEX IF NOT EXISTS idx_workflows_user_id ON workflows(user_id);
CREATE INDEX IF NOT EXISTS idx_workflows_status ON workflows(status);
CREATE INDEX IF NOT EXISTS idx_workflow_executions_workflow_id ON workflow_executions(workflow_id);
CREATE INDEX IF NOT EXISTS idx_workflow_executions_user_id ON workflow_executions(user_id);

-- Knowledge 相关索引
CREATE INDEX IF NOT EXISTS idx_knowledge_bases_user_id ON knowledge_bases(user_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_documents_kb_id ON knowledge_documents(knowledge_base_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_vectors_document_id ON knowledge_vectors(document_id);

-- Plugin 相关索引
CREATE INDEX IF NOT EXISTS idx_plugins_user_id ON plugins(user_id);
CREATE INDEX IF NOT EXISTS idx_plugins_category ON plugins(category);
CREATE INDEX IF NOT EXISTS idx_plugin_executions_plugin_id ON plugin_executions(plugin_id);

-- Project 相关索引
CREATE INDEX IF NOT EXISTS idx_projects_user_id ON projects(user_id);
CREATE INDEX IF NOT EXISTS idx_projects_type ON projects(type);
CREATE INDEX IF NOT EXISTS idx_projects_resource_id ON projects(resource_id);

-- ================================
-- AutoGPT 相关表
-- ================================

-- AutoGPT 规划表
CREATE TABLE IF NOT EXISTS autogpt_plans (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    goal TEXT NOT NULL,
    analysis TEXT, -- JSON格式的目标分析
    tasks TEXT, -- JSON格式的任务列表
    execution_plan TEXT, -- JSON格式的执行计划
    resource_assessment TEXT, -- JSON格式的资源评估
    status TEXT DEFAULT 'planned', -- planned, executing, completed, failed
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

-- AutoGPT 执行记录表
CREATE TABLE IF NOT EXISTS autogpt_executions (
    id TEXT PRIMARY KEY,
    plan_id TEXT NOT NULL,
    status TEXT DEFAULT 'running', -- running, completed, failed, cancelled
    context TEXT, -- JSON格式的执行上下文
    results TEXT, -- JSON格式的执行结果
    completed_tasks TEXT, -- JSON格式的已完成任务列表
    failed_tasks TEXT, -- JSON格式的失败任务列表
    started_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    completed_at TEXT,
    FOREIGN KEY (plan_id) REFERENCES autogpt_plans(id)
);

-- AutoGPT 任务执行日志表
CREATE TABLE IF NOT EXISTS autogpt_task_logs (
    id TEXT PRIMARY KEY,
    execution_id TEXT NOT NULL,
    task_id TEXT NOT NULL,
    task_name TEXT NOT NULL,
    task_type TEXT NOT NULL,
    result TEXT, -- JSON格式的任务执行结果
    created_at TEXT NOT NULL,
    FOREIGN KEY (execution_id) REFERENCES autogpt_executions(id)
);

-- ================================
-- CrewAI 相关表
-- ================================

-- Agent 团队表
CREATE TABLE IF NOT EXISTS agent_crews (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    agents TEXT, -- JSON格式的Agent列表
    workflow TEXT, -- JSON格式的协作工作流
    status TEXT DEFAULT 'active', -- active, inactive, archived
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

-- 团队任务执行记录表
CREATE TABLE IF NOT EXISTS crew_executions (
    id TEXT PRIMARY KEY,
    crew_id TEXT NOT NULL,
    task_description TEXT NOT NULL,
    agent_assignments TEXT, -- JSON格式的Agent任务分配
    execution_log TEXT, -- JSON格式的执行日志
    results TEXT, -- JSON格式的执行结果
    status TEXT DEFAULT 'running', -- running, completed, failed
    started_at TEXT NOT NULL,
    completed_at TEXT,
    FOREIGN KEY (crew_id) REFERENCES agent_crews(id)
);

-- ================================
-- LangChain Memory 相关表
-- ================================

-- 对话记忆表
CREATE TABLE IF NOT EXISTS langchain_memories (
    id TEXT PRIMARY KEY,
    agent_id TEXT NOT NULL,
    conversation_id TEXT,
    memory_type TEXT NOT NULL, -- buffer, summary, vector, entity
    content TEXT NOT NULL,
    metadata TEXT, -- JSON格式的元数据
    embedding TEXT, -- 向量嵌入（如果是向量记忆）
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (agent_id) REFERENCES agents(id)
);

-- 实体记忆表（用于实体记忆）
CREATE TABLE IF NOT EXISTS langchain_entities (
    id TEXT PRIMARY KEY,
    agent_id TEXT NOT NULL,
    entity_name TEXT NOT NULL,
    entity_type TEXT, -- person, organization, location, concept等
    description TEXT,
    attributes TEXT, -- JSON格式的实体属性
    last_mentioned TEXT, -- 最后提及时间
    mention_count INTEGER DEFAULT 1,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (agent_id) REFERENCES agents(id)
);

-- ================================
-- 新增索引
-- ================================

-- AutoGPT 相关索引
CREATE INDEX IF NOT EXISTS idx_autogpt_plans_user_id ON autogpt_plans(user_id);
CREATE INDEX IF NOT EXISTS idx_autogpt_plans_status ON autogpt_plans(status);
CREATE INDEX IF NOT EXISTS idx_autogpt_executions_plan_id ON autogpt_executions(plan_id);
CREATE INDEX IF NOT EXISTS idx_autogpt_executions_status ON autogpt_executions(status);
CREATE INDEX IF NOT EXISTS idx_autogpt_task_logs_execution_id ON autogpt_task_logs(execution_id);

-- CrewAI 相关索引
CREATE INDEX IF NOT EXISTS idx_agent_crews_user_id ON agent_crews(user_id);
CREATE INDEX IF NOT EXISTS idx_crew_executions_crew_id ON crew_executions(crew_id);

-- ================================
-- LangChain Memory 相关表
-- ================================

-- LangChain 记忆表
CREATE TABLE IF NOT EXISTS langchain_memories (
    id TEXT PRIMARY KEY,
    agent_id TEXT NOT NULL,
    conversation_id TEXT,
    memory_type TEXT NOT NULL, -- buffer, summary, vector, entity
    content TEXT NOT NULL,
    metadata TEXT, -- JSON格式的元数据
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

-- LangChain 实体表
CREATE TABLE IF NOT EXISTS langchain_entities (
    id TEXT PRIMARY KEY,
    agent_id TEXT NOT NULL,
    entity_name TEXT NOT NULL,
    entity_type TEXT NOT NULL, -- persons, organizations, products, concepts, dates, locations, numbers
    description TEXT,
    attributes TEXT, -- JSON格式的属性
    last_mentioned TEXT,
    mention_count INTEGER DEFAULT 1,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

-- LangChain Memory 相关索引
CREATE INDEX IF NOT EXISTS idx_langchain_memories_agent_id ON langchain_memories(agent_id);
CREATE INDEX IF NOT EXISTS idx_langchain_memories_type ON langchain_memories(memory_type);
CREATE INDEX IF NOT EXISTS idx_langchain_memories_conversation_id ON langchain_memories(conversation_id);
CREATE INDEX IF NOT EXISTS idx_langchain_entities_agent_id ON langchain_entities(agent_id);
CREATE INDEX IF NOT EXISTS idx_langchain_entities_type ON langchain_entities(entity_type);
CREATE INDEX IF NOT EXISTS idx_langchain_entities_name ON langchain_entities(entity_name);

-- LangChain Memory 相关索引
CREATE INDEX IF NOT EXISTS idx_langchain_memories_agent_id ON langchain_memories(agent_id);
CREATE INDEX IF NOT EXISTS idx_langchain_memories_conversation_id ON langchain_memories(conversation_id);
CREATE INDEX IF NOT EXISTS idx_langchain_memories_type ON langchain_memories(memory_type);
CREATE INDEX IF NOT EXISTS idx_langchain_entities_agent_id ON langchain_entities(agent_id);
CREATE INDEX IF NOT EXISTS idx_langchain_entities_name ON langchain_entities(entity_name);
