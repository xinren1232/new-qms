package com.transcend.plm.configcenter.table.colmap;

import  com.transcend.plm.configcenter.table.meta.impl.MySQLTableMeta;
import  com.transcend.plm.configcenter.table.model.Column;
import  com.transcend.plm.configcenter.table.model.impl.DefaultColumn;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * mysql 列的元数据到columnmodel的映射
 * 
 * @company 广州宏天软件股份有限公司
 * @author heyifan
 * @email heyf@jee-soft.cn
 * @date 2018年4月25日
 */
public class MySQLColumnMap implements RowMapper<Column> {

	@Override
	public Column mapRow(ResultSet rs, int row) throws SQLException {
		Column column = new DefaultColumn();

		String name = rs.getString("column_name");
		String is_nullable = rs.getString("is_nullable");
		String data_type = rs.getString("data_type");
		String length = rs.getString("length");
		String precisions = rs.getString("precisions");
		String scale = rs.getString("scale");
		String column_key = rs.getString("column_key");
		String column_comment = rs.getString("column_comment");
		String table_name = rs.getString("table_name");
		String column_type = rs.getString("column_type");
		column_comment = MySQLTableMeta.getComments(column_comment, name);
		int iLength = 0;
		try {
			iLength = StringUtils.isEmpty(length) ? 0 : Integer
					.parseInt(length);
		} catch (NumberFormatException e) {
		}
		int iPrecisions = StringUtils.isEmpty(precisions) ? 0 : Integer
				.parseInt(precisions);
		int iScale = StringUtils.isEmpty(scale) ? 0 : Integer.parseInt(scale);

		column.setFieldName(name);
		column.setTableName(table_name);
		column.setComment(column_comment);
		column.setFcolumnType(column_type);
		if (StringUtils.isNotEmpty(column_key) && "PRI".equals(column_key))
			column.setIsPk(true);
		boolean isNull = is_nullable.equals("YES");
		column.setIsNull(isNull);

		setType(data_type, iLength, iPrecisions, iScale, column);

		return column;
	}

	/**
	 * 设置列类型
	 * 
	 * @param dbtype
	 * @param length
	 * @param precision
	 * @param scale
	 * @param columnModel
	 */
	private void setType(String dbtype, int length, int precision, int scale,
			Column columnModel) {
		if (dbtype.equals("bigint")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_NUMBER);
			columnModel.setIntLen(19);
			columnModel.setDecimalLen(0);
			return;
		}

		if (dbtype.equals("int")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_NUMBER);
			columnModel.setIntLen(10);
			columnModel.setDecimalLen(0);
			return;
		}

		if (dbtype.equals("mediumint")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_NUMBER);
			columnModel.setIntLen(7);
			columnModel.setDecimalLen(0);
			return;
		}

		if (dbtype.equals("smallint")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_NUMBER);
			columnModel.setIntLen(5);
			columnModel.setDecimalLen(0);
			return;
		}

		if (dbtype.equals("tinyint")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_NUMBER);
			columnModel.setIntLen(3);
			columnModel.setDecimalLen(0);
			return;
		}

		if (dbtype.equals("decimal")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_NUMBER);
			columnModel.setIntLen(precision - scale);
			columnModel.setDecimalLen(scale);
			return;
		}

		if (dbtype.equals("double")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_NUMBER);
			columnModel.setIntLen(18);
			columnModel.setDecimalLen(4);
			return;
		}

		if (dbtype.equals("float")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_NUMBER);
			columnModel.setIntLen(8);
			columnModel.setDecimalLen(4);
			return;
		}

		if (dbtype.equals("varchar")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_VARCHAR);
			columnModel.setCharLen(length);

			return;
		}

		if (dbtype.equals("char") || dbtype.equals("bit")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_VARCHAR);
			columnModel.setCharLen(length);
			return;
		}

		if (dbtype.startsWith("date")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_DATE);

			return;
		}

		if (dbtype.startsWith("time")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_DATE);

			return;
		}

		if (dbtype.endsWith("text")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_CLOB);
			columnModel.setCharLen(65535);
			return;
		}
		if (dbtype.endsWith("blob")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_CLOB);
			columnModel.setCharLen(65535);
			return;
		}
		if (dbtype.endsWith("clob")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_CLOB);
			columnModel.setCharLen(65535);
			return;
		}

	}

}
