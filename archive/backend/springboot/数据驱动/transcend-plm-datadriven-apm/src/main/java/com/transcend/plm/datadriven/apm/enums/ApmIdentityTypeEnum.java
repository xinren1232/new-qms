package com.transcend.plm.datadriven.apm.enums;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description Identity类型常量
 * @createTime 2023-09-21 17:49:00
 */
public enum ApmIdentityTypeEnum {
    /**
     * 用户
     */
    USER("employee", "用户"),
    /**
     *用户组
     */
    GROUP("group", "用户组"),
    /**
     *角色
     */
    ROLE("role", "角色"),
    /**
     *部门
     */
    DEPARTMENT("department", "部门");
    /**
     * code
     */
    private String code;
    /**
     * name
     */
    private String name;
    ApmIdentityTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
