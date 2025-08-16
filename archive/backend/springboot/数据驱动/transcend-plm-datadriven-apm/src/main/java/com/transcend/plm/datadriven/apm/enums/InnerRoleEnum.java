package com.transcend.plm.datadriven.apm.enums;

import com.transsion.framework.enums.BaseEnum;

import java.util.HashSet;
import java.util.Set;

/**
 * @author unknown
 * 内置角色枚举
 */
public enum InnerRoleEnum implements BaseEnum<String> {
    /**
     * 所有人
     */
    ALL("all", "所有人"),
    /**
     *创建者
     */
    CREATER("INNER_CREATER", "创建者"),
    /**
     *所有者
     */
    PERSON_RESPONSIBLE("INNER_PERSON_RESPONSIBLE", "所有者"),
    /**
     * 加密人
     */
    CONFIDENTIAL_MEMBER("INNER_CONFIDENTIAL_MEMBER", "加密人"),
    /**
     *关注人
     */
    FOLLOW_MEMBER("INNER_FOLLOW_MEMBER", "关注人"),

    /**
     * 技术负责人
     */
    TECHNICAL_DIRECTOR("TECHNICAL_DIRECTOR", "技术负责人"),

    /**
     * UX代表
     */
    UX_AGENT("UX_AGENT", "UX代表");

    /**
     *code
     */

    String code;
    /**
     *desc
     */
    String desc;

    InnerRoleEnum(String code, String desc) {
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

    // 获取code
    public static Set<String> getCodes() {
        InnerRoleEnum[] enums = InnerRoleEnum.values();
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < enums.length; i++) {
            codes.add(enums[i].getCode());
        }
        return codes;
    }
}
