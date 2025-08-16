package com.transcend.plm.configcenter.api.model.object.enums;

public enum CfgSysAppTabEnum {
    BASE_INFO("001", "基本信息"),

    OVERVIEW("002", "概览"),
    //枚举对象版本记录
    VERSION_RECORD("100", "版本记录"),
    //枚举对象操作记录
    OPERATION_RECORD("101", "操作记录"),
    //枚举对象评论功能
    COMMENT("102", "评论功能"),
    //团队管理
    TEAM_MANAGEMENT("103", "团队管理"),

    //资源管理
    RESOURCE_MANAGEMENT("104", "资源管理");

    private String modelCode;
    private String description;

    //构造方法
    private CfgSysAppTabEnum(String modelCode, String description) {
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
