package com.transcend.plm.configcenter.table.meta.impl;

import  com.transcend.plm.configcenter.table.meta.IDbType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class BaseDbType implements IDbType {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	protected JdbcTemplate jdbcTemplate;

	@Override
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
}
