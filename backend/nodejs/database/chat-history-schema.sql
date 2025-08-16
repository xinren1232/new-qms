-- QMS AI聊天记录数据库结构设计
-- 用于存储用户个人对话历史记录

-- 用户表 (如果不存在)
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(200),
    department VARCHAR(100),
    role VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 对话会话表
CREATE TABLE IF NOT EXISTS chat_conversations (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL DEFAULT '新对话',
    model_provider VARCHAR(50) NOT NULL,
    model_name VARCHAR(100) NOT NULL,
    model_config JSON,
    message_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_created (user_id, created_at DESC),
    INDEX idx_model_provider (model_provider),
    INDEX idx_updated_at (updated_at DESC)
);

-- 对话消息表
CREATE TABLE IF NOT EXISTS chat_messages (
    id VARCHAR(50) PRIMARY KEY,
    conversation_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    message_type ENUM('user', 'assistant') NOT NULL,
    content TEXT NOT NULL,
    model_info JSON,
    response_time INT,
    token_usage JSON,
    rating INT DEFAULT NULL, -- 用户评分 1-5
    feedback TEXT DEFAULT NULL, -- 用户反馈
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    
    FOREIGN KEY (conversation_id) REFERENCES chat_conversations(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_conversation_created (conversation_id, created_at ASC),
    INDEX idx_user_created (user_id, created_at DESC),
    INDEX idx_message_type (message_type)
);

-- 对话标签表 (用于分类管理)
CREATE TABLE IF NOT EXISTS chat_tags (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    color VARCHAR(20) DEFAULT '#409EFF',
    description VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 对话-标签关联表
CREATE TABLE IF NOT EXISTS conversation_tags (
    conversation_id VARCHAR(50) NOT NULL,
    tag_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (conversation_id, tag_id),
    FOREIGN KEY (conversation_id) REFERENCES chat_conversations(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES chat_tags(id) ON DELETE CASCADE
);

-- 插入默认标签
INSERT IGNORE INTO chat_tags (name, color, description) VALUES
('质量管理', '#67C23A', '质量管理相关问题'),
('缺陷分析', '#E6A23C', '产品缺陷分析讨论'),
('流程优化', '#409EFF', '业务流程优化建议'),
('标准规范', '#909399', '标准规范咨询'),
('数据分析', '#F56C6C', '数据统计分析'),
('其他', '#C0C4CC', '其他类型对话');

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
