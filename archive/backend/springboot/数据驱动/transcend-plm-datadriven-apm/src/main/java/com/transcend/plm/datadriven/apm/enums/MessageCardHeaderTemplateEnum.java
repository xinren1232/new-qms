package com.transcend.plm.datadriven.apm.enums;


/**
 * @Author Qiu Yuhao
 * @Date 2024/3/12 15:00
 * @Describe 飞书卡片header颜色枚举
 */
public enum MessageCardHeaderTemplateEnum {
    /**
     * blue
     */
    BLUE("blue"),
    /**
     *wathet
     */
    WATHET("wathet"),
    /**
     *turquoise
     */
    TURQUOISE("turquoise"),
    /**
     *green
     */
    GREEN("green"),
    /**
     *yellow
     */
    YELLOW("yellow"),
    /**
     *orange
     */
    ORANGE("orange"),
    /**
     *red
     */
    RED("red"),
    /**
     *carmine
     */
    CARMINE("carmine"),
    /**
     *violet
     */
    VIOLET("violet"),
    /**
     *purple
     */
    PURPLE("purple"),
    /**
     *indigo
     */
    INDIGO("indigo"),
    /**
     *grey
     */
    GREY("grey");
    /**
     *value
     */
    private String value;

    MessageCardHeaderTemplateEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}