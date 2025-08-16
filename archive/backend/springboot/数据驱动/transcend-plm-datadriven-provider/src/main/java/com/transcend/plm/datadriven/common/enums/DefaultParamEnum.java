package com.transcend.plm.datadriven.common.enums;

import com.transsion.framework.enums.BaseEnum;

/**
 * @author unknown
 * 默认参数枚举
 */
public enum DefaultParamEnum implements BaseEnum<String> {
    /**
     *当前时间
     */
    NOW_TIME("{NOW_TIME} ", "当前时间"),
    /**
     *当前登录用户
     */
    LOGIN_USER("{LOGIN_USER}", "当前登录用户");
    /**
     *code
     */
    String code;
    /**
     *desc
     */
    String desc;

    DefaultParamEnum(String code, String desc) {
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
