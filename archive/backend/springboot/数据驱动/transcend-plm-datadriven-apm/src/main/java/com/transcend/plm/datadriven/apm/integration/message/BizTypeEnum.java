package com.transcend.plm.datadriven.apm.integration.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务类型枚举
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2024/9/10 10:54
 */
@AllArgsConstructor
@Getter
public enum BizTypeEnum {
    /**
     * 角色
     */
    ROLE("role", "角色"),
    /**
     * 空间
     */
    SPACE("space", "空间"),
    /**
     * 身份
     */
    IDENTITY("identity", "身份"),


    ;
    /**
     * code
     */
    private final String code;
    /**
     * description
     */
    private final String description;


}
