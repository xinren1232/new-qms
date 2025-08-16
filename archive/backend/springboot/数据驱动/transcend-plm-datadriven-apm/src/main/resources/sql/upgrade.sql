-- 2024年7月24日14:31:31
ALTER TABLE cfg_life_cycle_template_node ADD COLUMN `key_path_flag` bit(1) DEFAULT b'0' COMMENT '是否关键路径，0:否,1:是';
ALTER TABLE apm_role_identity ADD COLUMN  `input_percentage` int DEFAULT NULL COMMENT '人员投入比率';
-------------------------------------------------------------20240630-------------------------------------------------------------
ALTER TABLE `apm_space_app_tab` 
ADD COLUMN `show_condition_content` json NULL COMMENT '关系tab展示条件配置(tab独立配置)';


ALTER TABLE `apm_flow_template_node`
add column`show_help_tip_flag` bit(1) DEFAULT b'0' COMMENT '是否显示帮助，0否，1是', add column node_help_tip text COMMENT '节点帮助提示内容';
