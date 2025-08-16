package com.transcend.plm.datadriven.apm.enums;

import com.transsion.framework.enums.BaseEnum;

/**
 * @author unknown
 */
public enum MessageStateEnum implements BaseEnum<String> {
    /**
     *失败
     */
    FAILURE("failure", "失败"),
    /**
     *成功
     */
    SUCCESS("success", "成功"),
    /**
     *未消费
     */
    UNCONSUMED("unconsumed", "未消费"),
    /**
     *重试
     */
    RETRY("retry", "重试")
    ;

    /**
     *code
     */
    String code;
    /**
     *desc
     */
    String desc;

    MessageStateEnum(String code, String desc) {
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
