-- 创建正确的qms_ai_user用户
CREATE USER IF NOT EXISTS 'qms_ai_user'@'localhost' IDENTIFIED BY 'QmsAi2024@User';
CREATE USER IF NOT EXISTS 'qms_ai_user'@'%' IDENTIFIED BY 'QmsAi2024@User';

-- 授权数据库权限
GRANT ALL PRIVILEGES ON db_transcend_plm_configcenter.* TO 'qms_ai_user'@'localhost';
GRANT ALL PRIVILEGES ON db_transcend_plm_configcenter.* TO 'qms_ai_user'@'%';
GRANT ALL PRIVILEGES ON db_transcend_plm_datadriven.* TO 'qms_ai_user'@'localhost';
GRANT ALL PRIVILEGES ON db_transcend_plm_datadriven.* TO 'qms_ai_user'@'%';

-- 刷新权限
FLUSH PRIVILEGES;

-- 验证用户创建
SELECT 'User created successfully' as status;
SELECT User, Host FROM mysql.user WHERE User = 'qms_ai_user';
