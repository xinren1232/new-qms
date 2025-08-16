package com.transcend.plm.datadriven.apm.enums;

import com.transsion.framework.enums.BaseEnum;

/**
 * @author unknown
 */
public enum FlowEnum implements BaseEnum<String> {
    /**
     *匹配所有
     */
    FLOW_MATCH_ALL("all", "匹配所有"),
    /**
     *匹配任一
     */
    FLOW_MATCH_ANY("any", "匹配任一"),
    /**
     *流程事件匹配当前系统时间
     */
    FLOW_EVENT_NOW("now", "流程事件匹配当前系统时间"),
    /**
     *流程事件匹配当前登录人
     */
    FLOW_EVENT_LOGINUSER("loginUser", "流程事件匹配当前登录人")
    ;
    /**
     *code
     */
    String code;
    /**
     *desc
     */
    String desc;

    FlowEnum(String code, String desc) {
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
