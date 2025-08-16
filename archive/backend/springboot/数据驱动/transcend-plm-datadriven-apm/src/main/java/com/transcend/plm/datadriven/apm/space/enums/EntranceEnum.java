package com.transcend.plm.datadriven.apm.space.enums;

import com.transsion.framework.enums.BaseEnum;

/**
 *
 * @author jie.luo1
 * @version 1.0
 * @className /**
 * @description desc
 * @date 2025/4/10 10:09
 */
public enum EntranceEnum implements BaseEnum<String> {

    /**
     * 文本框
     */
    RELATION("relation", "关系TAB"),
    APP("app", "app");


    String code;
    String desc;

    EntranceEnum(String code, String desc) {
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
