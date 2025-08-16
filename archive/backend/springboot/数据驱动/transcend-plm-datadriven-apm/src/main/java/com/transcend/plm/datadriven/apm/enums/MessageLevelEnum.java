package com.transcend.plm.datadriven.apm.enums;

import com.transsion.framework.enums.BaseEnum;

/**
 * @author unknown
 */
public enum MessageLevelEnum implements BaseEnum<String> {
    /**
     *高
     */
    HIGH("high", "高"),
    /**
     *中
     */
    MIDDLE("middle", "中"),
    /**
     *低
     */
    LOW("low", "低");
    /**
     *code
     */
    String code;
    /**
     *desc
     */
    String desc;

    MessageLevelEnum(String code, String desc) {
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
