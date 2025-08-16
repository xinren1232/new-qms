-- QMS-AI 数据库初始化脚本
-- 创建日期: 2025-01-23

-- 创建配置中心数据库
CREATE DATABASE IF NOT EXISTS db_transcend_plm_configcenter 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 创建数据驱动数据库
CREATE DATABASE IF NOT EXISTS db_transcend_plm_datadriven 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 创建QMS-AI用户
CREATE USER IF NOT EXISTS 'qms_ai_user'@'localhost' IDENTIFIED BY 'QmsAi2024@User';

-- 授权配置中心数据库权限
GRANT ALL PRIVILEGES ON db_transcend_plm_configcenter.* TO 'qms_ai_user'@'localhost';

-- 授权数据驱动数据库权限
GRANT ALL PRIVILEGES ON db_transcend_plm_datadriven.* TO 'qms_ai_user'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;

-- 验证数据库创建
SHOW DATABASES;

-- 验证用户创建
SELECT User, Host FROM mysql.user WHERE User = 'qms_ai_user';
