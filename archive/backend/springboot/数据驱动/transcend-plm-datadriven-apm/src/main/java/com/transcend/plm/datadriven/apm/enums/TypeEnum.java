package com.transcend.plm.datadriven.apm.enums;

import com.transsion.framework.enums.BaseEnum;

/**
 * @author unknown
 */
public enum TypeEnum implements BaseEnum<String> {
    /**
     * 实例
     */
    INSTANCE("instance", "实例"),
    /**
     *对象
     */
    OBJECT("object", "对象"),
    /**
     *人员
     */
    EMPLOYEE("employee", "人员"),
    /**
     *部门
     */
    DEPARTMENT("department", "部门"),
    /**
     *空间
     */
    SPACE("space", "空间");
    /**
     *code
     */
    String code;
    /**
     *desc
     */
    String desc;

    TypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDesc() {
        return this.desc;
    }
}
