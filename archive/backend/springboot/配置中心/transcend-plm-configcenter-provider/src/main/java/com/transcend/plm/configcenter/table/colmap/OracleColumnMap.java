package com.transcend.plm.configcenter.table.colmap;

import  com.transcend.plm.configcenter.table.model.Column;
import  com.transcend.plm.configcenter.table.model.impl.DefaultColumn;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * oracle 列的元数据到columnmodel的映射
 * 
 * @company 广州宏天软件股份有限公司
 * @author heyifan
 * @email heyf@jee-soft.cn
 * @date 2018年4月25日
 */
public class OracleColumnMap implements RowMapper<Column> {

	@Override
	public Column mapRow(ResultSet rs, int row) throws SQLException {
		Column column = new DefaultColumn();
		String name = rs.getString("NAME");
		String typeName = rs.getString("TYPENAME");
		int length = rs.getInt("LENGTH");
		int precision = rs.getInt("PRECISION");
		int scale = rs.getInt("SCALE");
		boolean isNull = rs.getString("NULLABLE").equals("Y");
		String comments = rs.getString("DESCRIPTION");
		String tableName = rs.getString("TABLE_NAME");
		int isPK = rs.getInt("IS_PK");

		column.setFieldName(name);
		column.setComment(comments);
		column.setIsNull(isNull);
		column.setTableName(tableName);
		column.setIsPk(isPK == 1 ? true : false);
		int columnLength = length;
		if("NUMBER".equals(typeName)) {//数值类型
			columnLength = precision;
		}
		column.setFcolumnType(length == 0 ? typeName : typeName + "("+columnLength +")");
		setType(typeName, length, precision, scale, column);
		return column;
	}

	/**
	 * 设置列类型。
	 * 
	 * @param dbtype
	 *            数据库类型
	 * @param length
	 *            字符串长度
	 * @param precision
	 *            整数位
	 * @param scale
	 *            小数位
	 * @param column
	 *            列对象
	 */
	private void setType(String dbtype, int length, int precision, int scale,
			Column column) {
		if (dbtype.indexOf("CHAR") > -1) {
			column.setColumnType(Column.COLUMN_TYPE_VARCHAR);
			column.setCharLen(length);
			return;
		}
		if (dbtype.equals("NUMBER")) {
			column.setColumnType(Column.COLUMN_TYPE_NUMBER);
			column.setIntLen(precision - scale);
			column.setDecimalLen(scale);
			int i = precision - scale;
			if (i == 0 && scale == 0) {
				column.setIntLen(38);
			}
			return;
		}
		if (dbtype.equals("INTEGER")) {
			column.setColumnType(Column.COLUMN_TYPE_NUMBER);
			column.setIntLen(10);
			column.setDecimalLen(0);

			return;
		}
		if (dbtype.equals("DATE") || dbtype.indexOf("TIMESTAMP") != -1) {
			column.setColumnType(Column.COLUMN_TYPE_DATE);
			return;
		}

		if (dbtype.equals("CLOB")) {
			column.setColumnType(Column.COLUMN_TYPE_CLOB);
			return;
		}

		if(dbtype.equals("BIGINT")){
			column.setColumnType(Column.COLUMN_TYPE_NUMBER);
			column.setIntLen(length);
			column.setDecimalLen(precision);
		}
	}

}
