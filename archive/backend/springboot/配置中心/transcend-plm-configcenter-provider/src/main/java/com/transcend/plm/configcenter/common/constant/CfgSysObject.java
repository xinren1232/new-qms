package com.transcend.plm.configcenter.common.constant;

public enum CfgSysObject {
    //枚举对象版本记录
    VERSION_RECORD("100", "版本记录"),
    //枚举对象操作记录
    OPERATION_RECORD("101", "操作记录"),
    //枚举对象评论功能
    COMMENT("102", "评论功能");

    private String modelCode;
    private String description;

    //构造方法
    private CfgSysObject(String modelCode, String description) {
        this.modelCode = modelCode;
        this.description = description;
    }

    //获取modelCode
    public String getModelCode() {
        return modelCode;
    }

    //获取description
    public String getDescription() {
        return description;
    }
    }
