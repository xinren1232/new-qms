
-- 数据库优化SQL
-- 为conversations表添加索引
CREATE INDEX IF NOT EXISTS idx_conversations_user_id ON conversations(user_id);
CREATE INDEX IF NOT EXISTS idx_conversations_created_at ON conversations(created_at);

-- 为messages表添加索引
CREATE INDEX IF NOT EXISTS idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_messages_created_at ON messages(created_at);

-- 为ratings表添加索引
CREATE INDEX IF NOT EXISTS idx_ratings_message_id ON ratings(message_id);
CREATE INDEX IF NOT EXISTS idx_ratings_created_at ON ratings(created_at);

-- 分析表统计信息
ANALYZE conversations;
ANALYZE messages;
ANALYZE ratings;
