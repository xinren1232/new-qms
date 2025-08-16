//package com.transcend.plm.configcenter.table.meta.impl;
//
//import com.hotent.base.util.BeanUtils;
//import com.hotent.base.util.StringUtil;
//import  com.transcend.plm.configcenter.table.colmap.KingBaseColumnMap;
//import  com.transcend.plm.configcenter.table.model.Column;
//import  com.transcend.plm.configcenter.table.model.Table;
//import  com.transcend.plm.configcenter.table.model.impl.DefaultTable;
//import org.apache.commons.lang.StringUtils;
//import org.springframework.jdbc.core.RowMapper;
//import org.springframework.util.Assert;
//
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.*;
//
///**
// * @company: 广州宏天软件股份有限公司
// * @description: KingBaseTable表元数据的实现类
// * @author: czm
// * @create: 2021-10-21
// **/
//public class KingBaseTableMeta extends BaseTableMeta {
//    /**
//     * 取得注释
//     */
//    private final String sqlTableComment = "select relname as table_name,cast(obj_description(relfilenode,'pg_class') as varchar) as table_comment from sys_class c ";
//    /**
//     * 取得列表
//     */
//    private final String SQL_GET_COLUMNS = "SELECT "
//            + " 	A.TABLE_NAME TABLE_NAME, "
//            + " 	lower(A.COLUMN_NAME) NAME, "
//            + " 	A.DATA_TYPE TYPENAME, "
//            + " 	A.DATA_LENGTH LENGTH,  "
//            + " 	A.DATA_PRECISION \"PRECISION\", "
//            + " 	A.DATA_SCALE SCALE, "
//            + " 	A.DATA_DEFAULT, "
//            + " 	A.NULLABLE,  "
//            + " 	DECODE(B.COMMENTS,NULL,A.COLUMN_NAME,B.COMMENTS) DESCRIPTION, "
//            + " 	( " + "   	  SELECT " + "   	    COUNT(*) " + "   	  FROM  "
//            + "   	    USER_CONSTRAINTS CONS, "
//            + "    	   USER_CONS_COLUMNS CONS_C  " + "    	 WHERE  "
//            + "    	   CONS.CONSTRAINT_NAME=CONS_C.CONSTRAINT_NAME "
//            + "    	   AND CONS.CONSTRAINT_TYPE='P' "
//            + "    	   AND CONS.TABLE_NAME=B.TABLE_NAME "
//            + "     	  AND CONS_C.COLUMN_NAME=A.COLUMN_NAME "
//            + "  	 ) AS IS_PK " + " FROM  "
//            + " 	USER_TAB_COLUMNS A, "
//            + " 	USER_COL_COMMENTS B  "
//            + "     WHERE  "
//            + " 	A.COLUMN_NAME=B.COLUMN_NAME "
//            + " 	AND A.TABLE_NAME = B.TABLE_NAME "
//            + " 	AND lower(A.TABLE_NAME)='%s' "
//            + "     ORDER BY "
//            + "  	A.COLUMN_ID";
//    /**
//     * 取得列表
//     */
//    private final String SQL_GET_COLUMNS_BATCH = "SELECT "
//            + " 	A.TABLE_NAME TABLE_NAME, "
//            + " 	lower(A.COLUMN_NAME) NAME, "
//            + " 	A.DATA_TYPE TYPENAME, "
//            + " 	A.DATA_LENGTH LENGTH,  "
//            + " 	A.DATA_PRECISION \"PRECISION\", "
//            + " 	A.DATA_SCALE SCALE, "
//            + " 	A.DATA_DEFAULT, "
//            + " 	A.NULLABLE,  "
//            + " 	DECODE(B.COMMENTS,NULL,A.COLUMN_NAME,B.COMMENTS) DESCRIPTION, "
//            + " 	( " + "   	  SELECT " + "   	    COUNT(*) " + "   	  FROM  "
//            + "   	    USER_CONSTRAINTS CONS, "
//            + "    	   USER_CONS_COLUMNS CONS_C  " + "    	 WHERE  "
//            + "    	   CONS.CONSTRAINT_NAME=CONS_C.CONSTRAINT_NAME "
//            + "    	   AND CONS.CONSTRAINT_TYPE='P' "
//            + "    	   AND CONS.TABLE_NAME=B.TABLE_NAME "
//            + "     	  AND CONS_C.COLUMN_NAME=A.COLUMN_NAME "
//            + "  	 ) AS IS_PK " + " FROM  " + " 	USER_TAB_COLUMNS A, "
//            + " 	USER_COL_COMMENTS B  " + " WHERE  "
//            + " 	A.COLUMN_NAME=B.COLUMN_NAME "
//            + " 	AND A.TABLE_NAME = B.TABLE_NAME ";
//
//    /**
//     * 取得数据库所有表
//     */
//    private String sqlAllTables = "select TABLE_NAME,DECODE(COMMENTS,null,TABLE_NAME,comments) comments from user_tab_comments where table_type='TABLE'  ";
//
//    /**
//     * 取得主键
//     * **/
//    private String SQL_GET_PRIMARY_KEY="select column_name from USER_CONS_COLUMNS where table_name = upper('%s') and constraint_name =(select lower( constraint_name ) from USER_CONSTRAINTS where CONSTRAINT_TYPE = 'P' and table_name = upper('%s'))";
//
//    /**
//     * 根据表名查询列表，如果表名为空则去系统所有的数据库表。
//     */
//    @Override
//    public Map<String, String> getTablesByName(String tableName) {
//        String sql = sqlTableComment;
//        if (StringUtils.isNotEmpty(tableName)) {
//        	sql = sql + " where lower(table_name) like '%"+ tableName.toLowerCase() +"%'";
//        }
//
//        List<Map<String, String>> list = jdbcTemplate.query(sql,
//                new RowMapper<Map<String, String>>() {
//                    @Override
//                    public Map<String, String> mapRow(ResultSet rs, int row)
//                            throws SQLException {
//                        String tableName = rs.getString("table_name");
//                        String comments = rs.getString("table_comment");
//                        Map<String, String> map = new HashMap<String, String>();
//                        map.put("name", tableName);
//                        map.put("comments", StringUtil.isNotEmpty(comments) ? comments : tableName);
//                        return map;
//                    }
//                });
//        Map<String, String> map = new LinkedHashMap<String, String>();
//        for (int i = 0; i < list.size(); i++) {
//            Map<String, String> tmp = (Map<String, String>) list.get(i);
//            String name = tmp.get("name");
//            String comments = tmp.get("comments");
//            map.put(name, comments);
//        }
//
//        return map;
//    }
//
//    @SuppressWarnings({ "rawtypes", "unchecked" })
//    public Map<String, String> getTablesByName(List<String> names) {
//        StringBuffer sb = new StringBuffer();
//        for (String name : names) {
//            sb.append("'");
//            sb.append(name);
//            sb.append("',");
//        }
//        sb.deleteCharAt(sb.length() - 1);
//        String sql = sqlAllTables + " and  lower(table_name) in ("
//                + sb.toString().toLowerCase() + ")";
//
//
//        List list = jdbcTemplate.query(sql,
//                new RowMapper<Map<String, String>>() {
//                    @Override
//                    public Map<String, String> mapRow(ResultSet rs, int row)
//                            throws SQLException {
//                        String tableName = rs.getString("TABLE_NAME");
//                        String comments = rs.getString("COMMENTS");
//                        Map<String, String> map = new HashMap<String, String>();
//                        map.put("NAME", tableName);
//                        map.put("COMMENTS", comments);
//                        return map;
//                    }
//                });
//        Map<String, String> map = new LinkedHashMap<String, String>();
//        for (int i = 0; i < list.size(); i++) {
//            Map<String, String> tmp = (Map<String, String>) list.get(i);
//            String name = tmp.get("NAME");
//            String comments = tmp.get("COMMENTS");
//            map.put(name, comments);
//        }
//
//        return map;
//    }
//
//    /**
//     * 获取表对象
//     */
//    @Override
//    public Table getTableByName(String tableName) {
//    	Assert.notNull(tableName, "表名不能为空");
//        Table model = getTable(tableName.toLowerCase());
//        // 获取列对象
//        List<Column> columnList = getColumnsByTableName(tableName);
//        model.setColumnList(columnList);
//        return model;
//    }
//
//    /**
//     * 根据表名获取tableModel。
//     *
//     * @param tableName
//     * @return
//     */
//    private Table getTable(final String tableName) {
//        String sql = String.format(sqlTableComment + "where lower(relname) ='%s' ", tableName.toLowerCase());
//        Table tableModel = (Table) jdbcTemplate.queryForObject(sql,
//                new RowMapper<Table>() {
//
//                    @Override
//                    public Table mapRow(ResultSet rs, int row)
//                            throws SQLException {
//                        Table tableModel = new DefaultTable();
//                        tableModel.setTableName(tableName);
//                        String comments = rs.getString("table_comment");
//                        tableModel.setComment(StringUtil.isNotEmpty(comments) ? comments : tableName);
//                        return tableModel;
//                    }
//                });
//        if (BeanUtils.isEmpty(tableModel))
//            tableModel = new DefaultTable();
//
//        return tableModel;
//    }
//
//    /**
//     * 根据表名获取列
//     *
//     * @param tableName
//     * @return
//     */
//    private List<Column> getColumnsByTableName(String tableName) {
//        String sql = String.format(SQL_GET_COLUMNS, tableName.toLowerCase());
//        List<Column> columnList = jdbcTemplate.query(sql,
//                new KingBaseColumnMap());
//        String getPrimaryKeySql=SQL_GET_PRIMARY_KEY.replace("%s",tableName);
//        List<String> column_name = jdbcTemplate.query(getPrimaryKeySql, new RowMapper<String>() {
//            @Override
//            public String mapRow(ResultSet rs, int rowNum) throws SQLException {
//                if (rs != null) {
//                    return rs.getString("column_name");
//                }
//                return null;
//            }
//        });
//        if (column_name!=null && column_name.size()>0){
//            String pkColumn= column_name.get(0);
//            for (Column column : columnList) {
//                if (column.getFieldName().equalsIgnoreCase(pkColumn)){
//                    column.setIsPk(true);
//                }
//            }
//        }
//        return columnList;
//    }
//
//    /**
//     * 根据表名获取列。此方法使用批量查询方式。
//     *
//     * @param tableName
//     * @return
//     */
//    private Map<String, List<Column>> getColumnsByTableName(
//            List<String> tableNames) {
//        String sql = SQL_GET_COLUMNS_BATCH;
//        Map<String, List<Column>> map = new HashMap<String, List<Column>>();
//        if (tableNames != null && tableNames.size() == 0) {
//            return map;
//        } else {
//            StringBuffer buf = new StringBuffer();
//            for (String str : tableNames) {
//                buf.append("'" + str + "',");
//            }
//            buf.deleteCharAt(buf.length() - 1);
//            sql += " AND A.TABLE_NAME IN (" + buf.toString() + ") ";
//        }
//
//
//        List<Column> columnModels = jdbcTemplate.query(sql, new KingBaseColumnMap());
//
//        for (Column columnModel : columnModels) {
//            String tableName = columnModel.getTableName();
//            if (map.containsKey(tableName)) {
//                map.get(tableName).add(columnModel);
//            } else {
//                List<Column> cols = new ArrayList<Column>();
//                cols.add(columnModel);
//                map.put(tableName, cols);
//            }
//        }
//        return map;
//    }
//
//    @Override
//    public List<Table> getTableModelByName(String tableName) throws Exception {
//        String sql = sqlAllTables;
//
//        if (StringUtils.isNotEmpty(tableName)) {
//            sql += " AND  LOWER(table_name) LIKE '%" + tableName.toLowerCase()
//                    + "%'";
//        }
//        RowMapper<Table> rowMapper = new RowMapper<Table>() {
//            @Override
//            public Table mapRow(ResultSet rs, int row) throws SQLException {
//                Table tableModel = new DefaultTable();
//                tableModel.setTableName(rs.getString("TABLE_NAME"));
//                tableModel.setComment(rs.getString("COMMENTS"));
//                return tableModel;
//            }
//        };
//
//        List<Table> tableModels = jdbcTemplate.query(sql,  rowMapper);
//        List<String> tableNames = new ArrayList<String>();
//        // get all table names
//        for (Table model : tableModels) {
//            tableNames.add(model.getTableName());
//        }
//        // batch get table columns
//        Map<String, List<Column>> tableColumnsMap = getColumnsByTableName(tableNames);
//        // extract table columns from paraTypeMap by table name;
//        for (Map.Entry<String, List<Column>> entry : tableColumnsMap.entrySet()) {
//            // set Table's columns
//            for (Table model : tableModels) {
//                if (model.getTableName().equalsIgnoreCase(entry.getKey())) {
//                    model.setColumnList(entry.getValue());
//                }
//            }
//        }
//
//        return tableModels;
//    }
//
//    @Override
//    public String getAllTableSql() {
//        return this.sqlAllTables;
//    }
//
//    @Override
//    public List<Map<String, Object>> getTablesByNameIndex(String tableName) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//}
//
