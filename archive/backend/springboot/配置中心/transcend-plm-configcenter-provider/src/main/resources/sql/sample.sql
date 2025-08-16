DROP TABLE IF EXISTS t_sample;

CREATE TABLE `t_sample` (
  `id` varchar(32),
  -- `name` varchar(32) DEFAULT NULL,
  -- `qty` int DEFAULT '0',
  -- `email` varchar(32) DEFAULT NULL,
	-- `status` int DEFAULT NULL,
  `tenant_id` BIGINT DEFAULT '100' COMMENT '租户ID',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `delete_flag` tinyint DEFAULT '0' COMMENT '是否删除(0-未删除；1已删除)',
  `enable_flag` tinyint DEFAULT '1' COMMENT '是否有效(0-失效；1-有效)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB;