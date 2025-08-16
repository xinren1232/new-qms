package com.transcend.plm.datadriven.apm.space.enums;

import com.transsion.framework.enums.BaseEnum;

/**
 * @author shu.zhang
 * @version 1.0
 * @className ViewFieldDataTypeEnum
 * @description desc
 * @date 2024/6/3 10:09
 */
public enum ViewFieldDataTypeEnum implements BaseEnum<String> {

    /**
     * 文本框
     */
    TEXTAREA("textarea", "文本框"),
    INPUT("input", "输入框"),
    SELECT("select", "下拉框"),
    DATE("date", "日期选择器"),
    RELATION("relation", "关系选择器"),
    USER("user", "用户"),
    RICH_EDITOR("rich-editor", "富文本"),
    LABEL_SELECT("label-select", "标签组件"),
    FILE_UPLOAD("file-upload", "文件上传"),
    INSTANCE_SELECT("instance-select", "实例选择器"),
    CASCADE("cascader", "级联选择"),
    SPACE_SELECT("space-select", "空间选择器"),

    CHARACTER_ROLE("character-role", "角色选择器"),;


    String code;
    String desc;

    ViewFieldDataTypeEnum(String code, String desc) {
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
