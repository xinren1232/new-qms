package com.transcend.plm.datadriven.apm.permission.enums;

import com.transsion.framework.enums.BaseEnum;

/**
 * @author unknown
 * 扩展按钮
 */
public enum OperatorExtendEnum implements BaseEnum<String> {

    /**
     * 扩展按钮1
     */
    OPERATOR_1("operation1", "扩展按钮1"),
    OPERATOR_2("operation2", "扩展按钮2"),
    OPERATOR_3("operation3", "扩展按钮3"),
    OPERATOR_4("operation4", "扩展按钮4"),
    OPERATOR_5("operation5", "扩展按钮5"),
    OPERATOR_6("operation6", "扩展按钮6"),
    OPERATOR_7("operation7", "扩展按钮7"),
    OPERATOR_8("operation8", "扩展按钮8"),
    OPERATOR_9("operation9", "扩展按钮9"),
    OPERATOR_10("operation10", "扩展按钮10");
    String code;
    String desc;

    OperatorExtendEnum(String code, String desc) {
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
