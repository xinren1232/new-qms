package com.transcend.plm.datadriven.api.model;

import com.transsion.framework.enums.BaseEnum;

/**
 * 对象模型配置常量
 *
 * @author jie.luo1
 * @date 2024/07/24
 */
public enum RelationConfigEnum implements BaseEnum<String> {

    /**
     * 浮动到固定
     */
    FLOAT2FIXED("FLOAT2FIXED", "浮动到固定"),
    /**
     *浮动到浮动
     */
    FLOAT2FLOAT("FLOAT2FLOAT", "浮动到浮动"),
    /**
     *固定到浮动
     */
    FIXED2FLOAT("FIXED2FLOAT", "固定到浮动"),
    /**
     *固定到固定
     */
    FIXED2FIXED("FIXED2FIXED", "固定到固定");
    /**
     *code
     */

    String code;
    /**
     *desc
     */
    String desc;


    /**
     * @param code
     * @param desc
     */
    RelationConfigEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * @return {@link String }
     */
    @Override
    public String getCode() {
        return this.code;
    }

    /**
     * @return {@link String }
     */
    @Override
    public String getDesc() {
        return this.desc;
    }
}
