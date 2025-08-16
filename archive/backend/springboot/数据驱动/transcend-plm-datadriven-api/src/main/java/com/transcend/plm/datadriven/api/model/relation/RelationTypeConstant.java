package com.transcend.plm.datadriven.api.model.relation;

/**
 * @version 1.0.0
 * @Description 类型对象类型
 * @createTime 2023-10-07 11:33:00
 */
public class RelationTypeConstant {
    private RelationTypeConstant() {
    }
    /**
     * 两者皆可
     */
    public static final String BOTH = "both";
    /**
     * 仅选取
     */
    public static final String SELECT_ONLY = "selectOnly";
    /**
     * 仅新增
     */
    public static final String CREATE_ONLY = "createOnly";
}
