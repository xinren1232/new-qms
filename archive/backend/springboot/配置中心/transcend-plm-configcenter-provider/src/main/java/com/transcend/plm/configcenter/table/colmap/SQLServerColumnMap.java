package com.transcend.plm.configcenter.table.colmap;

import  com.transcend.plm.configcenter.table.model.Column;
import  com.transcend.plm.configcenter.table.model.impl.DefaultColumn;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * SqlServer 列的元数据到columnmodel的映射
 * 
 * @company 广州宏天软件股份有限公司
 * @author heyifan
 * @email heyf@jee-soft.cn
 * @date 2018年4月25日
 */
public class SQLServerColumnMap implements RowMapper<Column> {

	@Override
	public Column mapRow(ResultSet rs, int row) throws SQLException {
		Column column = new DefaultColumn();

		String name = rs.getString("NAME");
		String is_nullable = rs.getString("IS_NULLABLE");
		String data_type = rs.getString("TYPENAME");
		String length = rs.getString("LENGTH");
		String precisions = rs.getString("PRECISION");
		String scale = rs.getString("SCALE");
		String tableName = rs.getString("TABLE_NAME");
		String comments = rs.getString("DESCRIPTION");
		int isPK = rs.getInt("IS_PK");

		int iLength = StringUtils.isEmpty(length) ? 0 : Integer
				.parseInt(length);
		int iPrecisions = StringUtils.isEmpty(precisions) ? 0 : Integer
				.parseInt(precisions);
		int iScale = StringUtils.isEmpty(scale) ? 0 : Integer.parseInt(scale);

		column.setFieldName(name);
		boolean isNull = is_nullable.equals("1");
		column.setIsNull(isNull);
		column.setTableName(tableName);
		column.setComment(comments);
		column.setIsPk(isPK == 1 ? true : false);
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
		if (dbtype.equals("int")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_NUMBER);
			columnModel.setIntLen(precision);
			columnModel.setDecimalLen(scale);
			return;
		}

		if (dbtype.equals("bigint")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_NUMBER);
			columnModel.setIntLen(precision);
			columnModel.setDecimalLen(scale);
			return;
		}
		if (dbtype.equals("smallint")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_NUMBER);
			columnModel.setIntLen(precision);
			columnModel.setDecimalLen(scale);
			return;
		}

		if (dbtype.equals("tinyint")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_NUMBER);
			columnModel.setIntLen(precision);
			columnModel.setDecimalLen(scale);
			return;
		}

		if (dbtype.equals("bit")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_NUMBER);
			return;
		}

		if (dbtype.equals("decimal")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_NUMBER);
			columnModel.setIntLen(precision);
			columnModel.setDecimalLen(scale);
			return;
		}

		if (dbtype.equals("numeric")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_NUMBER);
			columnModel.setIntLen(precision);
			columnModel.setDecimalLen(scale);
			return;
		}

		if (dbtype.equals("real")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_NUMBER);
			columnModel.setIntLen(precision);
			return;
		}

		if (dbtype.equals("float")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_NUMBER);
			columnModel.setIntLen(precision);
			return;
		}

		if (dbtype.equals("varchar")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_VARCHAR);
			columnModel.setCharLen(length);

			return;
		}

		if (dbtype.equals("char")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_VARCHAR);
			columnModel.setCharLen(length);
			return;
		}

		if (dbtype.equals("varchar")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_VARCHAR);
			columnModel.setCharLen(length);

			return;
		}

		if (dbtype.equals("nchar")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_VARCHAR);
			columnModel.setCharLen(length);
			return;
		}

		if (dbtype.equals("nvarchar")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_VARCHAR);
			columnModel.setCharLen(length);

			return;
		}

		if (dbtype.startsWith("datetime")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_DATE);

			return;
		}

		if (dbtype.endsWith("money")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_NUMBER);
			columnModel.setIntLen(precision);
			columnModel.setDecimalLen(scale);
			return;
		}

		if (dbtype.endsWith("smallmoney")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_CLOB);
			columnModel.setIntLen(precision);
			columnModel.setDecimalLen(scale);
			return;
		}

		if (dbtype.endsWith("text")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_CLOB);
			columnModel.setCharLen(length);
			return;
		}

		if (dbtype.endsWith("ntext")) {
			columnModel.setColumnType(Column.COLUMN_TYPE_CLOB);
			columnModel.setCharLen(length);
			return;
		}

	}

}
