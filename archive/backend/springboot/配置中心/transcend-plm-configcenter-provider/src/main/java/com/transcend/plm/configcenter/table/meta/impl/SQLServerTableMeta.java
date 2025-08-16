//package com.transcend.plm.configcenter.table.meta.impl;
//
//import com.hotent.base.util.BeanUtils;
//import  com.transcend.plm.configcenter.table.colmap.SQLServerColumnMap;
//import  com.transcend.plm.configcenter.table.model.Column;
//import  com.transcend.plm.configcenter.table.model.Table;
//import  com.transcend.plm.configcenter.table.model.impl.DefaultTable;
//import org.apache.commons.lang.StringUtils;
//import org.springframework.jdbc.core.RowMapper;
//
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.*;
//import java.util.Map.Entry;
//
///**
// * SQLServer 表元数据的实现类
// *
// * @company 广州宏天软件股份有限公司
// * @author heyifan
// * @email heyf@jee-soft.cn
// * @date 2018年4月25日
// */
//public class SQLServerTableMeta extends BaseTableMeta {
//
//	/**
//	 * 取得主键
//	 */
//	private String sqlPk = "SELECT c.COLUMN_NAME COLUMN_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS pk ,INFORMATION_SCHEMA.KEY_COLUMN_USAGE c "
//			+ "WHERE 	pk.TABLE_NAME LIKE '%s' "
//			+ "and	CONSTRAINT_TYPE = 'PRIMARY KEY' "
//			+ "and	c.TABLE_NAME = pk.TABLE_NAME "
//			+ "and	c.CONSTRAINT_NAME = pk.CONSTRAINT_NAME ";
//
//	/**
//	 * 取得注释
//	 */
//	private String sqlTableComment = "select top 1 cast((case b.minor_id when 0 then b.value else a.name end) as varchar) comment from sys.tables a left join sys.extended_properties b on a.object_id=b.major_id where a.type='U' and a.name='%s' ORDER BY b.minor_id asc";
//
//	/**
//	 * 取得列表
//	 */
//	private String SQL_GET_COLUMNS = "SELECT B.NAME TABLE_NAME,A.NAME NAME, C.NAME TYPENAME, A.MAX_LENGTH LENGTH, A.IS_NULLABLE IS_NULLABLE,A.PRECISION PRECISION,A.SCALE SCALE, "
//			+ "  ( "
//			+ " 		SELECT COUNT(*) "
//			+ " 		FROM  "
//			+ " 		SYS.IDENTITY_COLUMNS  "
//			+ " 		WHERE SYS.IDENTITY_COLUMNS.OBJECT_ID = A.OBJECT_ID AND A.COLUMN_ID = SYS.IDENTITY_COLUMNS.COLUMN_ID"
//			+ "	) AS AUTOGEN, "
//			+ " 	( "
//			+ " 		SELECT CAST(VALUE AS VARCHAR) "
//			+ " 		FROM SYS.EXTENDED_PROPERTIES  "
//			+ " 		WHERE SYS.EXTENDED_PROPERTIES.MAJOR_ID = A.OBJECT_ID AND SYS.EXTENDED_PROPERTIES.MINOR_ID = A.COLUMN_ID "
//			+ " 	) AS DESCRIPTION, "
//			+ " 	( "
//			+ " 		SELECT COUNT(*) "
//			+ " 		FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS pk ,INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu "
//			+ " 		WHERE 	pk.TABLE_NAME = B.NAME "
//			+ " 			 AND	CONSTRAINT_TYPE = 'PRIMARY KEY'  "
//			+ " 			 AND	KCU.TABLE_NAME = PK.TABLE_NAME  "
//			+ " 			 AND	KCU.CONSTRAINT_NAME = PK.CONSTRAINT_NAME "
//			+ " 			 AND 	KCU.COLUMN_NAME =A.NAME "
//			+ " 	) AS IS_PK "
//			+ " FROM SYS.COLUMNS A, SYS.TABLES B, SYS.TYPES C  "
//			+ " WHERE A.OBJECT_ID = B.OBJECT_ID AND A.SYSTEM_TYPE_ID=C.SYSTEM_TYPE_ID AND B.NAME='%s' "
//			+ " 		AND C.NAME<>'SYSNAME' " + "		ORDER BY A.COLUMN_ID ";
//
//	private String SQL_GET_COLUMNS_BATCH = "SELECT B.NAME TABLE_NAME,A.NAME NAME, C.NAME TYPENAME, A.MAX_LENGTH LENGTH, A.IS_NULLABLE IS_NULLABLE,A.PRECISION PRECISION,A.SCALE SCALE, "
//			+ " ( "
//			+ " 	SELECT COUNT(*) "
//			+ " 	FROM  "
//			+ " 	SYS.IDENTITY_COLUMNS  "
//			+ " WHERE SYS.IDENTITY_COLUMNS.OBJECT_ID = A.OBJECT_ID AND A.COLUMN_ID = SYS.IDENTITY_COLUMNS.COLUMN_ID) AS AUTOGEN, "
//			+ " 	( "
//			+ " 			SELECT CAST(VALUE AS VARCHAR) "
//			+ " 			FROM SYS.EXTENDED_PROPERTIES  "
//			+ " 		WHERE SYS.EXTENDED_PROPERTIES.MAJOR_ID = A.OBJECT_ID AND SYS.EXTENDED_PROPERTIES.MINOR_ID = A.COLUMN_ID "
//			+ " 	) AS DESCRIPTION, "
//			+ " 	( "
//			+ " 		SELECT COUNT(*) "
//			+ " 		FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS pk ,INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu "
//			+ " 		WHERE 	pk.TABLE_NAME = B.NAME "
//			+ " 			 AND	CONSTRAINT_TYPE = 'PRIMARY KEY'  "
//			+ " 			 AND	KCU.TABLE_NAME = PK.TABLE_NAME  "
//			+ " 			 AND	KCU.CONSTRAINT_NAME = PK.CONSTRAINT_NAME "
//			+ " 			 AND 	KCU.COLUMN_NAME =A.NAME "
//			+ " 	) AS IS_PK "
//			+ " FROM SYS.COLUMNS A, SYS.TABLES B, SYS.TYPES C  "
//			+ " WHERE A.OBJECT_ID = B.OBJECT_ID AND A.SYSTEM_TYPE_ID=C.SYSTEM_TYPE_ID "
//			+ " 		AND C.NAME<>'SYSNAME' ";
//
//	/**
//	 * 取得数据库所有表
//	 */
//	private String sqlAllTables = "select a.name name, cast(b.value as varchar) comment from sys.tables a left join sys.extended_properties b on a.object_id=b.major_id where a.type='U' ";
//
//	/**
//	 * 获取表对象
//	 */
//	@Override
//	public Table getTableByName(String tableName) {
//		Table model = getTable(tableName);
//		// 获取列对象
//		List<Column> columnList = getColumnsByTableName(tableName);
//		model.setColumnList(columnList);
//		return model;
//	}
//
//	/**
//	 * 根据表名获取主键列名
//	 *
//	 * @param tableName
//	 * @return
//	 */
//	@SuppressWarnings({ "unused" })
//	private String getPkColumn(String tableName) {
//		String sql = String.format(sqlPk, tableName);
//		Object rtn = jdbcTemplate.queryForObject(sql,
//				new RowMapper<String>() {
//					@Override
//					public String mapRow(ResultSet rs, int row)
//							throws SQLException {
//						return rs.getString("COLUMN_NAME");
//					}
//				});
//		if (rtn == null)
//			return "";
//
//		return rtn.toString();
//	}
//
//	/**
//	 * 根据表名获取tableModel。
//	 *
//	 * @param tableName
//	 * @return
//	 */
//	private Table getTable(final String tableName) {
//		String sql = String.format(sqlTableComment, tableName);
//		Table tableModel = (Table) jdbcTemplate.queryForObject(sql,
//				new RowMapper<Table>() {
//
//					@Override
//					public Table mapRow(ResultSet rs, int row)
//							throws SQLException {
//						Table tableModel = new DefaultTable();
//						tableModel.setTableName(tableName);
//						tableModel.setComment(rs.getString("comment"));
//						return tableModel;
//					}
//				});
//		if (BeanUtils.isEmpty(tableModel))
//			tableModel = new DefaultTable();
//
//		tableModel.setTableName(tableName);
//
//		return tableModel;
//	}
//
//	/**
//	 * 根据表名查询列表，如果表名为空则去系统所有的数据库表。
//	 */
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	@Override
//	public Map<String, String> getTablesByName(String tableName) {
//		String sql = sqlAllTables;
//		if (StringUtils.isNotEmpty(tableName))
//			sql += " and  lower(a.name) like '%" + tableName.toLowerCase()+ "%'";
//
//		List list = jdbcTemplate.queryForList(sql);
//
//		Map<String, String> map = new LinkedHashMap<String, String>();
//		for (int i = 0; i < list.size(); i++) {
//			Map<String, String> tmp = (Map<String, String>) list.get(i);
//			String name = tmp.get("name");
//			String comments = tmp.get("comment");
//			map.put(name, comments);
//		}
//
//		return map;
//	}
//
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	@Override
//	public Map<String, String> getTablesByName(List<String> names) {
//		StringBuffer sb = new StringBuffer();
//		for (String name : names) {
//			sb.append("'");
//			sb.append(name);
//			sb.append("',");
//		}
//		sb.deleteCharAt(sb.length() - 1);
//		String sql = sqlAllTables + " and  a.name in ("
//				+ sb.toString().toLowerCase() + ")";
//
//		Map parameter = new HashMap();
//		List list = jdbcTemplate.queryForList(sql, parameter,
//				new RowMapper<Map<String, String>>() {
//					@Override
//					public Map<String, String> mapRow(ResultSet rs, int row)
//							throws SQLException {
//						String tableName = rs.getString("name");
//						String comments = rs.getString("comment");
//						Map<String, String> map = new HashMap<String, String>();
//						map.put("name", tableName);
//						map.put("comments", comments);
//						return map;
//					}
//				});
//		Map<String, String> map = new LinkedHashMap<String, String>();
//		for (int i = 0; i < list.size(); i++) {
//			Map<String, String> tmp = (Map<String, String>) list.get(i);
//			String name = tmp.get("name");
//			String comments = tmp.get("comments");
//			map.put(name, comments);
//		}
//
//		return map;
//	}
//
//
//
//	/**
//	 * 根据表名获取列
//	 *
//	 * @param tableName
//	 * @return
//	 */
//	private List<Column> getColumnsByTableName(String tableName) {
//		String sql = String.format(SQL_GET_COLUMNS, tableName);
//		List<Column> list = jdbcTemplate.query(sql,
//				new SQLServerColumnMap());
//		for (Column model : list) {
//			model.setTableName(tableName);
//		}
//		return list;
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
//			sql += " AND B.NAME IN (" + buf.toString() + ") ";
//		}
//		List<Column> columnModels = jdbcTemplate.query(sql, new SQLServerColumnMap());
//
//		for (Column columnModel : columnModels) {
//			String tableName = columnModel.getTableName();
//			if (map.containsKey(tableName)) {
//				map.get(tableName).add(columnModel);
//			} else {
//				List<Column> cols = new ArrayList<Column>();
//				cols.add(columnModel);
//				map.put(tableName, cols);
//			}
//		}
//		return map;
//	}
//
//	@Override
//	public List<Table> getTableModelByName(String tableName) throws Exception {
//		String sql = sqlAllTables;
//		if (StringUtils.isNotEmpty(tableName))
//			sql += " AND  LOWER(name) LIKE '%" + tableName.toLowerCase() + "%'";
//		RowMapper<Table> rowMapper = new RowMapper<Table>() {
//			@Override
//			public Table mapRow(ResultSet rs, int row) throws SQLException {
//				Table tableModel = new DefaultTable();
//				tableModel.setTableName(rs.getString("NAME"));
//				tableModel.setComment(rs.getString("COMMENT"));
//				return tableModel;
//			}
//		};
//
//		List<Table> tableModels = jdbcTemplate.query(sql,rowMapper);
//		// 获取列对象
//		List<String> tableNames = new ArrayList<String>();
//		// get all table names
//		for (Table model : tableModels) {
//			tableNames.add(model.getTableName());
//		}
//		// batch get table columns
//		Map<String, List<Column>> tableColumnsMap = getColumnsByTableName(tableNames);
//		// extract table columns from paraTypeMap by table name;
//		for (Entry<String, List<Column>> entry : tableColumnsMap.entrySet()) {
//			// set Table's columns
//			for (Table model : tableModels) {
//				if (model.getTableName().equalsIgnoreCase(entry.getKey())) {
//					model.setColumnList(entry.getValue());
//				}
//			}
//		}
//
//		return tableModels;
//	}
//
//	@Override
//	public String getAllTableSql() {
//		return this.sqlAllTables;
//	}
//
//	@Override
//	public List<Map<String, Object>> getTablesByNameIndex(String tableName) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//}
