-- QMS-AI数据库初始化脚本
-- 创建优化的数据库结构和索引

-- 创建扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- 创建配置表
CREATE TABLE IF NOT EXISTS config_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    key VARCHAR(255) NOT NULL UNIQUE,
    value TEXT,
    description TEXT,
    category VARCHAR(100),
    data_type VARCHAR(50) DEFAULT 'string',
    is_encrypted BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    version INTEGER DEFAULT 1,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 创建配置历史表
CREATE TABLE IF NOT EXISTS config_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    config_id UUID NOT NULL,
    old_value TEXT,
    new_value TEXT,
    operation VARCHAR(20) NOT NULL, -- INSERT, UPDATE, DELETE
    operator VARCHAR(100),
    reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (config_id) REFERENCES config_items(id) ON DELETE CASCADE
);

-- 创建对话表
CREATE TABLE IF NOT EXISTS chat_conversations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id VARCHAR(100) NOT NULL,
    title VARCHAR(500),
    model_provider VARCHAR(100),
    model_name VARCHAR(100),
    system_prompt TEXT,
    temperature DECIMAL(3,2) DEFAULT 0.7,
    max_tokens INTEGER DEFAULT 2000,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 创建消息表
CREATE TABLE IF NOT EXISTS chat_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    conversation_id UUID NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    message_type VARCHAR(20) NOT NULL, -- user, assistant, system
    content TEXT NOT NULL,
    model_info JSONB,
    response_time INTEGER, -- 响应时间（毫秒）
    token_usage JSONB, -- token使用情况
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES chat_conversations(id) ON DELETE CASCADE
);

-- 创建AI模型配置表
CREATE TABLE IF NOT EXISTS ai_models (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL UNIQUE,
    provider VARCHAR(50) NOT NULL,
    model_id VARCHAR(100) NOT NULL,
    base_url VARCHAR(500),
    api_key_encrypted TEXT,
    max_tokens INTEGER DEFAULT 4000,
    temperature_default DECIMAL(3,2) DEFAULT 0.7,
    supports_vision BOOLEAN DEFAULT FALSE,
    supports_tools BOOLEAN DEFAULT FALSE,
    supports_streaming BOOLEAN DEFAULT TRUE,
    cost_per_1k_tokens DECIMAL(10,6),
    is_enabled BOOLEAN DEFAULT TRUE,
    priority INTEGER DEFAULT 5,
    rate_limit_rpm INTEGER DEFAULT 60,
    rate_limit_tpm INTEGER DEFAULT 60000,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 创建模型使用统计表
CREATE TABLE IF NOT EXISTS model_usage_stats (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    model_id UUID NOT NULL,
    user_id VARCHAR(100),
    request_count INTEGER DEFAULT 0,
    token_count INTEGER DEFAULT 0,
    error_count INTEGER DEFAULT 0,
    avg_response_time INTEGER DEFAULT 0,
    total_cost DECIMAL(10,4) DEFAULT 0,
    date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (model_id) REFERENCES ai_models(id) ON DELETE CASCADE,
    UNIQUE(model_id, user_id, date)
);

-- 创建系统日志表
CREATE TABLE IF NOT EXISTS system_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    level VARCHAR(20) NOT NULL, -- ERROR, WARN, INFO, DEBUG
    category VARCHAR(50) NOT NULL, -- API, DB, CACHE, AI, CONFIG
    message TEXT NOT NULL,
    details JSONB,
    user_id VARCHAR(100),
    ip_address INET,
    user_agent TEXT,
    request_id VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 创建性能指标表
CREATE TABLE IF NOT EXISTS performance_metrics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(15,6) NOT NULL,
    metric_unit VARCHAR(20),
    tags JSONB,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
-- 配置表索引
CREATE INDEX IF NOT EXISTS idx_config_items_key ON config_items(key);
CREATE INDEX IF NOT EXISTS idx_config_items_category ON config_items(category);
CREATE INDEX IF NOT EXISTS idx_config_items_active ON config_items(is_active);
CREATE INDEX IF NOT EXISTS idx_config_history_config_id ON config_history(config_id);
CREATE INDEX IF NOT EXISTS idx_config_history_created_at ON config_history(created_at DESC);

-- 对话表索引
CREATE INDEX IF NOT EXISTS idx_conversations_user_id ON chat_conversations(user_id);
CREATE INDEX IF NOT EXISTS idx_conversations_updated_at ON chat_conversations(updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_conversations_user_updated ON chat_conversations(user_id, updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_conversations_deleted ON chat_conversations(is_deleted);

-- 消息表索引
CREATE INDEX IF NOT EXISTS idx_messages_conversation_id ON chat_messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_messages_user_id ON chat_messages(user_id);
CREATE INDEX IF NOT EXISTS idx_messages_created_at ON chat_messages(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_messages_conv_created ON chat_messages(conversation_id, created_at ASC);
CREATE INDEX IF NOT EXISTS idx_messages_type ON chat_messages(message_type);
CREATE INDEX IF NOT EXISTS idx_messages_deleted ON chat_messages(is_deleted);

-- AI模型表索引
CREATE INDEX IF NOT EXISTS idx_ai_models_provider ON ai_models(provider);
CREATE INDEX IF NOT EXISTS idx_ai_models_enabled ON ai_models(is_enabled);
CREATE INDEX IF NOT EXISTS idx_ai_models_priority ON ai_models(priority DESC);

-- 模型统计表索引
CREATE INDEX IF NOT EXISTS idx_model_stats_model_date ON model_usage_stats(model_id, date DESC);
CREATE INDEX IF NOT EXISTS idx_model_stats_user_date ON model_usage_stats(user_id, date DESC);

-- 系统日志表索引
CREATE INDEX IF NOT EXISTS idx_system_logs_level ON system_logs(level);
CREATE INDEX IF NOT EXISTS idx_system_logs_category ON system_logs(category);
CREATE INDEX IF NOT EXISTS idx_system_logs_created_at ON system_logs(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_system_logs_user_id ON system_logs(user_id);

-- 性能指标表索引
CREATE INDEX IF NOT EXISTS idx_performance_metrics_name ON performance_metrics(metric_name);
CREATE INDEX IF NOT EXISTS idx_performance_metrics_timestamp ON performance_metrics(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_performance_metrics_name_time ON performance_metrics(metric_name, timestamp DESC);

-- 创建触发器函数：自动更新updated_at字段
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 为需要的表创建触发器
CREATE TRIGGER update_config_items_updated_at 
    BEFORE UPDATE ON config_items 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_conversations_updated_at 
    BEFORE UPDATE ON chat_conversations 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ai_models_updated_at 
    BEFORE UPDATE ON ai_models 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 插入默认配置数据
INSERT INTO config_items (key, value, description, category, data_type) VALUES
('system.name', 'QMS-AI智能管理系统', '系统名称', 'system', 'string'),
('system.version', '2.0.0', '系统版本', 'system', 'string'),
('system.maintenance_mode', 'false', '维护模式', 'system', 'boolean'),
('ai.default_model', 'gpt-4o', '默认AI模型', 'ai', 'string'),
('ai.max_tokens_default', '2000', '默认最大token数', 'ai', 'integer'),
('ai.temperature_default', '0.7', '默认温度参数', 'ai', 'decimal'),
('cache.ttl_default', '3600', '默认缓存TTL（秒）', 'cache', 'integer'),
('cache.max_size', '1000', '缓存最大条目数', 'cache', 'integer'),
('api.rate_limit_rpm', '100', 'API每分钟请求限制', 'api', 'integer'),
('api.timeout_seconds', '30', 'API超时时间（秒）', 'api', 'integer')
ON CONFLICT (key) DO NOTHING;

-- 插入默认AI模型配置
INSERT INTO ai_models (name, provider, model_id, base_url, max_tokens, supports_vision, supports_tools, priority) VALUES
('GPT-4o', 'openai', 'gpt-4o', 'https://api.openai.com/v1', 4000, true, true, 10),
('GPT-4o-mini', 'openai', 'gpt-4o-mini', 'https://api.openai.com/v1', 4000, true, true, 9),
('Claude-3.5-Sonnet', 'anthropic', 'claude-3-5-sonnet-20241022', 'https://api.anthropic.com', 4000, true, true, 9),
('DeepSeek-Chat', 'deepseek', 'deepseek-chat', 'https://api.deepseek.com/v1', 4000, false, true, 8),
('DeepSeek-Coder', 'deepseek', 'deepseek-coder', 'https://api.deepseek.com/v1', 4000, false, true, 7)
ON CONFLICT (name) DO NOTHING;

-- 创建视图：对话统计
CREATE OR REPLACE VIEW conversation_stats AS
SELECT 
    c.id,
    c.user_id,
    c.title,
    c.model_provider,
    c.model_name,
    COUNT(m.id) as message_count,
    MAX(m.created_at) as last_message_time,
    c.created_at,
    c.updated_at
FROM chat_conversations c
LEFT JOIN chat_messages m ON c.id = m.conversation_id AND m.is_deleted = false
WHERE c.is_deleted = false
GROUP BY c.id, c.user_id, c.title, c.model_provider, c.model_name, c.created_at, c.updated_at;

-- 创建视图：模型使用统计
CREATE OR REPLACE VIEW model_daily_stats AS
SELECT 
    m.name as model_name,
    m.provider,
    s.date,
    SUM(s.request_count) as total_requests,
    SUM(s.token_count) as total_tokens,
    SUM(s.error_count) as total_errors,
    AVG(s.avg_response_time) as avg_response_time,
    SUM(s.total_cost) as total_cost
FROM ai_models m
JOIN model_usage_stats s ON m.id = s.model_id
GROUP BY m.name, m.provider, s.date
ORDER BY s.date DESC, total_requests DESC;

-- 设置表的所有者和权限
ALTER TABLE config_items OWNER TO qms_app;
ALTER TABLE config_history OWNER TO qms_app;
ALTER TABLE chat_conversations OWNER TO qms_app;
ALTER TABLE chat_messages OWNER TO qms_app;
ALTER TABLE ai_models OWNER TO qms_app;
ALTER TABLE model_usage_stats OWNER TO qms_app;
ALTER TABLE system_logs OWNER TO qms_app;
ALTER TABLE performance_metrics OWNER TO qms_app;

-- 完成初始化
SELECT 'QMS-AI数据库初始化完成' as status;
