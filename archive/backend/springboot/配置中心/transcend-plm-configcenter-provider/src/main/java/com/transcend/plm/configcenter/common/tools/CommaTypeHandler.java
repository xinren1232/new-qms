package com.transcend.plm.configcenter.common.tools;


import com.transcend.plm.configcenter.common.constant.CommonConst;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.assertj.core.util.Sets;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

/**
 * , 号分隔转为set
 * @author jie.luo1
 */
@MappedTypes({Object.class})
@MappedJdbcTypes({JdbcType.VARCHAR})
public class CommaTypeHandler extends BaseTypeHandler<Set<String>> {
    public CommaTypeHandler() {
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Set<String> parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, this.toString(parameter));
    }

    @Override
    public Set<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return StringUtils.isBlank(json) ? null : this.parse(json);
    }

    @Override
    public Set<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return StringUtils.isBlank(json) ? null : this.parse(json);
    }

    @Override
    public Set<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return StringUtils.isBlank(json) ? null : this.parse(json);
    }

    /**
     * value 转为 set
     * a,dd,dfd ->["a","dd","dfd"]
     *
     * @param value
     * @return
     */
    protected Set<String> parse(String value) {
        return Sets.newLinkedHashSet(value.split(CommonConst.COMMA));
    }

    protected String toString(Set<String> obj) {
        return String.join(CommonConst.COMMA, obj);
    }
}