package com.transcend.plm.configcenter.permission.enums;

import com.transsion.framework.enums.BaseEnum;

public enum OperatorEnum implements BaseEnum<String> {
    ADD("ADD", "新增"),
    DELETE("DELETE", "删除"),
    EDIT("EDIT", "编辑"),
    DETAIL("DETAIL", "详情"),

    REVISE("REVISE", "修订"),

    PROMOTE("PROMOTE", "提升"),
    LIST("LIST", "列表查询")
    ;
    String code;
    String desc;

    OperatorEnum(String code, String desc) {
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
