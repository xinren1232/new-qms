-- QMS AI聊天记录数据库结构设计 - PostgreSQL版本
-- 用于存储用户个人对话历史记录

-- 创建数据库扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(200),
    department VARCHAR(100),
    role VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 创建更新时间触发器函数
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 为用户表创建更新触发器
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- 对话会话表
CREATE TABLE IF NOT EXISTS chat_conversations (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL DEFAULT '新对话',
    model_provider VARCHAR(50) NOT NULL,
    model_name VARCHAR(100) NOT NULL,
    model_config JSONB,
    message_count INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    
    CONSTRAINT fk_conversations_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 为对话表创建更新触发器
CREATE TRIGGER update_conversations_updated_at 
    BEFORE UPDATE ON chat_conversations 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- 对话消息表
CREATE TABLE IF NOT EXISTS chat_messages (
    id VARCHAR(50) PRIMARY KEY,
    conversation_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    message_type VARCHAR(20) NOT NULL CHECK (message_type IN ('user', 'assistant')),
    content TEXT NOT NULL,
    model_info JSONB,
    response_time INTEGER,
    token_usage JSONB,
    rating INTEGER DEFAULT NULL CHECK (rating >= 1 AND rating <= 5),
    feedback TEXT DEFAULT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    
    CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id) REFERENCES chat_conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 对话标签表
CREATE TABLE IF NOT EXISTS chat_tags (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    color VARCHAR(20) DEFAULT '#409EFF',
    description VARCHAR(200),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 对话-标签关联表
CREATE TABLE IF NOT EXISTS conversation_tags (
    conversation_id VARCHAR(50) NOT NULL,
    tag_id INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (conversation_id, tag_id),
    CONSTRAINT fk_conv_tags_conversation FOREIGN KEY (conversation_id) REFERENCES chat_conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_conv_tags_tag FOREIGN KEY (tag_id) REFERENCES chat_tags(id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_conversations_user_created ON chat_conversations(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_conversations_model_provider ON chat_conversations(model_provider);
CREATE INDEX IF NOT EXISTS idx_conversations_updated_at ON chat_conversations(updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_conversations_is_deleted ON chat_conversations(is_deleted) WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_messages_conversation_created ON chat_messages(conversation_id, created_at ASC);
CREATE INDEX IF NOT EXISTS idx_messages_user_created ON chat_messages(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_messages_type ON chat_messages(message_type);
CREATE INDEX IF NOT EXISTS idx_messages_is_deleted ON chat_messages(is_deleted) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_messages_rating ON chat_messages(rating) WHERE rating IS NOT NULL;

-- 全文搜索索引
CREATE INDEX IF NOT EXISTS idx_messages_content_gin ON chat_messages USING gin(to_tsvector('english', content));
CREATE INDEX IF NOT EXISTS idx_conversations_title_gin ON chat_conversations USING gin(to_tsvector('english', title));

-- 插入默认标签
INSERT INTO chat_tags (name, color, description) VALUES
('质量管理', '#67C23A', '质量管理相关问题'),
('缺陷分析', '#E6A23C', '产品缺陷分析讨论'),
('流程优化', '#409EFF', '业务流程优化建议'),
('标准规范', '#909399', '标准规范咨询'),
('数据分析', '#F56C6C', '数据统计分析'),
('其他', '#C0C4CC', '其他类型对话')
ON CONFLICT (name) DO NOTHING;

-- 创建视图：用户对话统计
CREATE OR REPLACE VIEW user_chat_stats AS
SELECT 
    u.id as user_id,
    u.username,
    COUNT(DISTINCT c.id) as total_conversations,
    COUNT(m.id) as total_messages,
    COUNT(CASE WHEN m.message_type = 'user' THEN 1 END) as user_messages,
    COUNT(CASE WHEN m.message_type = 'assistant' THEN 1 END) as ai_responses,
    AVG(m.rating) as avg_rating,
    MAX(c.updated_at) as last_chat_time,
    MIN(c.created_at) as first_chat_time
FROM users u
LEFT JOIN chat_conversations c ON u.id = c.user_id AND c.is_deleted = FALSE
LEFT JOIN chat_messages m ON c.id = m.conversation_id AND m.is_deleted = FALSE
GROUP BY u.id, u.username;

-- 创建视图：模型使用统计
CREATE OR REPLACE VIEW model_usage_stats AS
SELECT 
    model_provider,
    model_name,
    COUNT(DISTINCT conversation_id) as conversation_count,
    COUNT(*) as message_count,
    AVG(response_time) as avg_response_time,
    AVG(rating) as avg_rating,
    DATE(created_at) as usage_date
FROM chat_messages m
JOIN chat_conversations c ON m.conversation_id = c.id
WHERE m.message_type = 'assistant' AND m.is_deleted = FALSE AND c.is_deleted = FALSE
GROUP BY model_provider, model_name, DATE(created_at)
ORDER BY usage_date DESC, conversation_count DESC;

-- 创建分区表（按月分区消息表，提升查询性能）
-- 注意：这需要PostgreSQL 10+
-- CREATE TABLE chat_messages_partitioned (LIKE chat_messages INCLUDING ALL) PARTITION BY RANGE (created_at);

-- 创建性能优化函数
CREATE OR REPLACE FUNCTION get_user_recent_conversations(p_user_id VARCHAR(50), p_limit INTEGER DEFAULT 20)
RETURNS TABLE(
    id VARCHAR(50),
    title VARCHAR(200),
    model_provider VARCHAR(50),
    model_name VARCHAR(100),
    message_count INTEGER,
    last_message_time TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        c.id,
        c.title,
        c.model_provider,
        c.model_name,
        c.message_count,
        c.updated_at as last_message_time,
        c.created_at
    FROM chat_conversations c
    WHERE c.user_id = p_user_id AND c.is_deleted = FALSE
    ORDER BY c.updated_at DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql;

-- 创建全文搜索函数
CREATE OR REPLACE FUNCTION search_messages(
    p_user_id VARCHAR(50),
    p_search_text TEXT,
    p_limit INTEGER DEFAULT 50
)
RETURNS TABLE(
    message_id VARCHAR(50),
    conversation_id VARCHAR(50),
    conversation_title VARCHAR(200),
    content TEXT,
    message_type VARCHAR(20),
    created_at TIMESTAMP WITH TIME ZONE,
    rank REAL
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        m.id as message_id,
        m.conversation_id,
        c.title as conversation_title,
        m.content,
        m.message_type,
        m.created_at,
        ts_rank(to_tsvector('english', m.content), plainto_tsquery('english', p_search_text)) as rank
    FROM chat_messages m
    JOIN chat_conversations c ON m.conversation_id = c.id
    WHERE m.user_id = p_user_id 
        AND m.is_deleted = FALSE 
        AND c.is_deleted = FALSE
        AND to_tsvector('english', m.content) @@ plainto_tsquery('english', p_search_text)
    ORDER BY rank DESC, m.created_at DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql;

-- 创建数据库用户和权限
-- CREATE USER qms_app WITH PASSWORD 'your_secure_password';
-- GRANT CONNECT ON DATABASE qms TO qms_app;
-- GRANT USAGE ON SCHEMA public TO qms_app;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO qms_app;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO qms_app;

-- 创建备份和维护脚本的注释
-- 定期清理软删除的数据
-- DELETE FROM chat_messages WHERE is_deleted = TRUE AND created_at < NOW() - INTERVAL '90 days';
-- DELETE FROM chat_conversations WHERE is_deleted = TRUE AND created_at < NOW() - INTERVAL '90 days';

-- 更新统计信息
-- ANALYZE chat_conversations;
-- ANALYZE chat_messages;
-- ANALYZE users;
