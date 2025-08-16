package com.transcend.plm.datadriven.apm.flow.util;

import com.transsion.framework.enums.BaseEnum;

/**
 * 对象模型配置常量
 *
 * @author huang.yuanhu
 */
public enum FlowCheckEnum implements BaseEnum<String> {


    /**
     *RAT评审
     */
    RATREVIEW("RATREVIEW", "RATREVIEW", "RAT评审"),
    /**
     *RMT评审
     */
    RMTREVIEW("RMTREVIEW", "RMTREVIEW","RMT评审"),

    /**
     *RMT评审
     */
    PPEOFRAT("PPEOFRAT", "PPEOFRAT","RAT前置初评"),
    ;
    /**
     *code
     */
    String code;
    /**
     *column
     */
    String column;
    /**
     * desc
     */
    String desc;


    /**
     * @param code
     * @param column
     * @param desc
     */
    FlowCheckEnum(String code, String column, String desc) {
        this.code = code;
        this.column = column;
        this.desc = desc;
    }

    /**
     * @return {@link String }
     */
    @Override
    public String getCode() {
        return this.code;
    }

    /**
     * @return {@link String }
     */
    public String getColumn() {
        return this.column;
    }

    /**
     * @return {@link String }
     */
    @Override
    public String getDesc() {
        return this.desc;
    }

}
