package com.transcend.plm.datadriven.api.model;

/**
 * 查询过滤条件枚举
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/10/12 11:35
 */
public enum QueryFilterConditionEnum {

    /**
     *AND
     */
    AND("AND", "AND"),
    /**
     *OR
     */
    OR("OR", "OR"),
    /**
     *NOT
     */
    NOT("NOT", "NOT"),
    /**
     *IN
     */
    IN("IN", "IN"),
    /**
     *NOT IN
     */
    NOT_IN("NOT IN", "NOT IN"),
    /**
     *LIKE
     */
    R_LIKE("R_LIKE", "LIKE"),
    /**
     *LIKE
     */
    L_LIKE("L_LIKE", "LIKE"),
    /**
     *LIKE
     */
    LIKE("LIKE", "LIKE"),
    /**
     *NOT LIKE
     */
    NOT_LIKE("NOT LIKE", "NOT LIKE"),
    /**
     *EQ
     */
    EQ("EQ", "="),
    /**
     *NE
     */
    NE("NE", "<>"),
    /**
     *GT
     */
    GT("GT", ">"),
    /**
     *GE
     */
    GE("GE", ">="),
    /**
     *LT
     */
    LT("LT", "<"),
    /**
     *LE
     */
    LE("LE", "<="),
    /**
     *IS_NULL
     */
    IS_NULL("IS_NULL", "IS NULL"),
    /**
     *IS_NOT_NULL
     */
    IS_NOT_NULL("IS_NOT_NULL", "IS NOT NULL"),
    /**
     *SQL_CONDITION
     */
    SQL_CONDITION("SQL_CONDITION", ""),
    /**
     *GROUP BY
     */
    GROUP_BY("GROUP BY", "GROUP BY"),
    /**
     *HAVING
     */
    HAVING("HAVING", "HAVING"),
    /**
     *ORDER BY
     */
    ORDER_BY("ORDER BY", "ORDER BY"),
    /**
     *EXISTS
     */
    EXISTS("EXISTS", "EXISTS"),
    /**
     *NOT EXISTS
     */
    NOT_EXISTS("NOT EXISTS", "NOT EXISTS"),
    /**
     *BETWEEN
     */
    BETWEEN("BETWEEN", "BETWEEN"),
    /**
     *NOT BETWEEN
     */
    NOT_BETWEEN("NOT BETWEEN", "NOT BETWEEN"),
    /**
     *ASC
     */
    ASC("ASC", "ASC"),
    /**
     *DESC
     */
    DESC("DESC", "DESC");
    /**
     *filter
     */
    String filter;
    /**
     *sqlCondition
     */
    String sqlCondition;

    /**
     * <p>Constructor for Operator.</p>
     * @param filter
     * @param sqlCondition
     */
    QueryFilterConditionEnum(String filter, String sqlCondition) {
        this.filter = filter;
        this.sqlCondition = sqlCondition;
    }

    /**
     * @return {@link String }
     */
    public String getFilter() {
        return filter;
    }

    /**
     * @return {@link String }
     */
    public String getSqlCondition() {
        return sqlCondition;
    }
}
