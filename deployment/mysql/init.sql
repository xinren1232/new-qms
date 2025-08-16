-- QMS-AI数据库初始化脚本
-- 创建数据库和基础表结构

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建用户表
CREATE TABLE IF NOT EXISTS `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL UNIQUE,
  `password` varchar(255) NOT NULL,
  `email` varchar(100),
  `role` varchar(20) DEFAULT 'user',
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_username` (`username`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建对话表
CREATE TABLE IF NOT EXISTS `conversations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `title` varchar(255) DEFAULT '新对话',
  `model` varchar(50) DEFAULT 'gpt-4o',
  `status` varchar(20) DEFAULT 'active',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`),
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建消息表
CREATE TABLE IF NOT EXISTS `messages` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `conversation_id` int(11) NOT NULL,
  `role` varchar(20) NOT NULL,
  `content` text NOT NULL,
  `model` varchar(50),
  `tokens` int(11) DEFAULT 0,
  `response_time` int(11) DEFAULT 0,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_created_at` (`created_at`),
  FOREIGN KEY (`conversation_id`) REFERENCES `conversations`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建用户评分表
CREATE TABLE IF NOT EXISTS `user_ratings` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `message_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `rating` int(1) NOT NULL CHECK (rating >= 1 AND rating <= 5),
  `feedback` text,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_message_id` (`message_id`),
  KEY `idx_user_id` (`user_id`),
  FOREIGN KEY (`message_id`) REFERENCES `messages`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `config_key` varchar(100) NOT NULL UNIQUE,
  `config_value` text,
  `description` varchar(255),
  `category` varchar(50) DEFAULT 'general',
  `data_type` varchar(20) DEFAULT 'string',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_config_key` (`config_key`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建AI模型配置表
CREATE TABLE IF NOT EXISTS `ai_models` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `model_id` varchar(50) NOT NULL UNIQUE,
  `model_name` varchar(100) NOT NULL,
  `provider` varchar(50) NOT NULL,
  `api_endpoint` varchar(255),
  `max_tokens` int(11) DEFAULT 4000,
  `temperature` decimal(3,2) DEFAULT 0.70,
  `status` varchar(20) DEFAULT 'active',
  `config` json,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_model_id` (`model_id`),
  KEY `idx_provider` (`provider`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入默认用户
INSERT INTO `users` (`username`, `password`, `email`, `role`) VALUES
('admin', '$2b$10$rOzJqQZQZQZQZQZQZQZQZOzJqQZQZQZQZQZQZQZQZOzJqQZQZQZQZQ', 'admin@qms-ai.com', 'admin'),
('developer', '$2b$10$rOzJqQZQZQZQZQZQZQZQZOzJqQZQZQZQZQZQZQZQZOzJqQZQZQZQZQ', 'dev@qms-ai.com', 'developer'),
('quality', '$2b$10$rOzJqQZQZQZQZQZQZQZQZOzJqQZQZQZQZQZQZQZQZOzJqQZQZQZQZQ', 'quality@qms-ai.com', 'quality')
ON DUPLICATE KEY UPDATE username=username;

-- 插入系统配置
INSERT INTO `system_config` (`config_key`, `config_value`, `description`, `category`) VALUES
('system.name', 'QMS-AI智能管理系统', '系统名称', 'system'),
('system.version', '2.0.0', '系统版本', 'system'),
('system.maintenance_mode', 'false', '维护模式', 'system'),
('ai.default_model', 'gpt-4o', '默认AI模型', 'ai'),
('ai.max_tokens_default', '2000', '默认最大token数', 'ai'),
('ai.temperature_default', '0.7', '默认温度参数', 'ai'),
('cache.ttl_default', '3600', '默认缓存TTL（秒）', 'cache'),
('api.rate_limit_rpm', '100', 'API每分钟请求限制', 'api')
ON DUPLICATE KEY UPDATE config_value=VALUES(config_value);

-- 插入AI模型配置
INSERT INTO `ai_models` (`model_id`, `model_name`, `provider`, `max_tokens`, `temperature`, `config`) VALUES
('gpt-4o', 'GPT-4 Omni', 'transsion', 4000, 0.7, '{"context_length": 128000, "supports_vision": true}'),
('gpt-4o-mini', 'GPT-4 Omni Mini', 'transsion', 4000, 0.7, '{"context_length": 128000, "supports_vision": true}'),
('gpt-4-turbo', 'GPT-4 Turbo', 'transsion', 4000, 0.7, '{"context_length": 128000, "supports_vision": true}'),
('gpt-3.5-turbo', 'GPT-3.5 Turbo', 'transsion', 4000, 0.7, '{"context_length": 16385}'),
('claude-3-5-sonnet', 'Claude 3.5 Sonnet', 'transsion', 4000, 0.7, '{"context_length": 200000}'),
('claude-3-haiku', 'Claude 3 Haiku', 'transsion', 4000, 0.7, '{"context_length": 200000}'),
('deepseek-chat', 'DeepSeek Chat', 'deepseek', 4000, 0.7, '{"context_length": 32768}'),
('deepseek-coder', 'DeepSeek Coder', 'deepseek', 4000, 0.7, '{"context_length": 16384, "specialized": "coding"}')
ON DUPLICATE KEY UPDATE model_name=VALUES(model_name);

-- 设置外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 创建索引优化查询性能
CREATE INDEX IF NOT EXISTS idx_messages_conversation_created ON messages(conversation_id, created_at);
CREATE INDEX IF NOT EXISTS idx_conversations_user_updated ON conversations(user_id, updated_at);
CREATE INDEX IF NOT EXISTS idx_user_ratings_rating ON user_ratings(rating);

-- 显示创建结果
SELECT 'QMS-AI数据库初始化完成' as status;
