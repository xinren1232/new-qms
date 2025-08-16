package com.transcend.plm.datadriven.configcenter.model.enums;

import com.transsion.framework.enums.BaseEnum;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
public enum ConfigKeyEnum implements BaseEnum<java.lang.String> {

    /**
     *生命周期
     */
    LIFT_CYCLE("LIFT_CYCLE", "生命周期"),
    /**
     *对象视图
     */
    OBJECT_VIEW("OBJECT_VIEW", "对象视图");


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
    ConfigKeyEnum(String code, String desc) {
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
