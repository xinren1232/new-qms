//package com.transcend.plm.configcenter.table.operator.impl.postgresql;
//
//import com.hotent.base.query.PageBean;
//import com.hotent.base.query.PageList;
//import  com.transcend.plm.configcenter.table.colmap.MySQLColumnMap;
//import  com.transcend.plm.configcenter.table.model.Column;
//import  com.transcend.plm.configcenter.table.model.Table;
//import  com.transcend.plm.configcenter.table.model.impl.DefaultTable;
//import  com.transcend.plm.configcenter.table.operator.IViewOperator;
//import  com.transcend.plm.configcenter.table.operator.impl.BaseViewOperator;
//import org.apache.commons.lang.StringUtils;
//import org.springframework.jdbc.core.RowMapper;
//
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//
///**
// * postgreSQL 视图操作的实现类
// *
// * @company 广州宏天软件股份有限公司
// * @author heyifan
// * @email heyf@jee-soft.cn
// * @date 2018年4月25日
// */
//public class PostgreSQLViewOperator extends BaseViewOperator implements
//		IViewOperator {
//
//	private static final String sqlAllView = "SELECT TABLE_NAME FROM information_schema.`TABLES` WHERE TABLE_TYPE LIKE 'VIEW'";
//	private static final String SQL_GET_COLUMNS_BATCH = "SELECT"
//			+ " TABLE_NAME,COLUMN_NAME,IS_NULLABLE,DATA_TYPE,CHARACTER_OCTET_LENGTH LENGTH,"
//			+ " NUMERIC_PRECISION PRECISIONS,NUMERIC_SCALE SCALE,COLUMN_KEY,COLUMN_COMMENT,COLUMN_TYPE "
//			+ " FROM" + " INFORMATION_SCHEMA.COLUMNS "
//			+ " WHERE TABLE_SCHEMA=DATABASE() ";
//
//	@Override
//	public void createOrRep(String viewName, String sql) throws Exception {
//		String getSql = "CREATE OR REPLACE VIEW " + viewName + " as (" + sql
//				+ ")";
//		jdbcTemplate.execute(getSql);
//	}
//
//	@Override
//	public PageList<String> getViews(String viewName) throws SQLException {
//		String sql = "select viewname from pg_views where schemaname ='public'";
//		if (StringUtils.isNotEmpty(viewName))
//			sql += " AND viewname LIKE '" + viewName + "%'";
//		List<String> list = new ArrayList<String>();
//		List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
//		for (Map<String, Object> line : results) {
//			list.add(line.get("viewname").toString());
//		}
//		return new PageList<String>(list);
//	}
//
//	@Override
//	public PageList<String> getViews(String viewName, PageBean pageBean)
//			throws SQLException, Exception {
//		String sql = sqlAllView;
//		if (StringUtils.isNotEmpty(viewName))
//			sql += " AND TABLE_NAME LIKE '" + viewName + "%'";
//		return super.getForList(sql, pageBean, String.class);
//	}
//
//	/*
//	 * (non-Javadoc)
//	 *
//	 * @see
//	 * com.hotent.base.api.db.IViewOperator#getViewsByName(java.lang.String,
//	 * com.hotent.base.api.Page)
//	 */
//	@Override
//	public PageList<Table> getViewsByName(String viewName, PageBean pageBean)
//			throws Exception {
//		String sql = sqlAllView;
//		if (StringUtils.isNotEmpty(viewName)) {
//			sql += " AND TABLE_NAME LIKE '" + viewName + "%'";
//		}
//
//		RowMapper<Table> rowMapper = new RowMapper<Table>() {
//			@Override
//			public Table mapRow(ResultSet rs, int row) throws SQLException {
//				Table table = new DefaultTable();
//				table.setTableName(rs.getString("table_name"));
//				table.setComment(table.getTableName());
//				return table;
//			}
//		};
//		PageList<Table> tableModels = getForList(sql, pageBean, rowMapper);
//
//		List<String> tableNames = new ArrayList<String>();
//		// get all table names
//		for (Table table : tableModels.getRows()) {
//			tableNames.add(table.getTableName());
//		}
//		// batch get table columns
//		Map<String, List<Column>> tableColumnsMap = getColumnsByTableName(tableNames);
//		// extract table columns from paraTypeMap by table name;
//		for (Entry<String, List<Column>> entry : tableColumnsMap.entrySet()) {
//			// set TableModel's columns
//			for (Table table : tableModels.getRows()) {
//				if (table.getTableName().equalsIgnoreCase(entry.getKey())) {
//					table.setColumnList(entry.getValue());
//				}
//			}
//		}
//		return tableModels;
//	}
//
//	/**
//	 * 根据表名获取列。此方法使用批量查询方式。
//	 *
//	 * @param tableName
//	 * @return
//	 */
//	private Map<String, List<Column>> getColumnsByTableName(
//			List<String> tableNames) {
//		String sql = SQL_GET_COLUMNS_BATCH;
//		Map<String, List<Column>> map = new HashMap<String, List<Column>>();
//		if (tableNames != null && tableNames.size() == 0) {
//			return map;
//		} else {
//			StringBuffer buf = new StringBuffer();
//			for (String str : tableNames) {
//				buf.append("'" + str + "',");
//			}
//			buf.deleteCharAt(buf.length() - 1);
//			sql += " AND TABLE_NAME IN (" + buf.toString() + ") ";
//		}
//		// jdbcHelper.setCurrentDb(currentDb);
//
//		List<Column> columns = jdbcTemplate.query(sql, new MySQLColumnMap());
//		for (Column column : columns) {
//			String tableName = column.getTableName();
//			if (map.containsKey(tableName)) {
//				map.get(tableName).add(column);
//			} else {
//				List<Column> cols = new ArrayList<Column>();
//				cols.add(column);
//				map.put(tableName, cols);
//			}
//		}
//		return map;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 *
//	 * @see com.hotent.base.db.table.BaseViewOperator#getType(java.lang.String)
//	 */
//	@Override
//	public String getType(String type) {
//		type = type.toLowerCase();
//		if (type.indexOf("number") > -1)
//			return Column.COLUMN_TYPE_NUMBER;
//		else if (type.indexOf("date") > -1) {
//			return Column.COLUMN_TYPE_DATE;
//		} else if (type.indexOf("char") > -1) {
//			return Column.COLUMN_TYPE_VARCHAR;
//		}
//		return Column.COLUMN_TYPE_VARCHAR;
//	}
//
//}
