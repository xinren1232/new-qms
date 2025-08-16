package com.transcend.plm.configcenter.api.model.tabmanage.enums;

import lombok.Getter;

/**
 * 视图组件枚举
 * @author yinbin
 * @version:
 * @date 2023/10/08 11:16
 */
@Getter
public enum ViewComponentEnum {


    /**
     * 基础组件
     */
    INPUT(ViewComponentEnum.INPUT_CONSTANT, "","单行输入组件"),
    /**
     * 下拉选项
     */
    SELECT(ViewComponentEnum.SELECT_CONSTANT, ViewComponentEnum.SELECT_CONSTANT,"下拉选项"),

    /**
     * 多行输入
     */
    TEXTAREA("textarea", "","多行输入"),
    /**
     * 计数器
     */
    NUMBER("number", "","计数器"),
    /**
     * 单选项
     */
    RADIO("radio", ViewComponentEnum.SELECT_CONSTANT,"单选项"),
    /**
     * 多选项
     */
    CHECKBOX("checkbox", ViewComponentEnum.SELECT_CONSTANT,"多选项"),
    TIME("time", "","时间"),
    /**
     * 时间范围
     */
    TIME_RANGE("time-range", "","时间范围"),
    /**
     * 日期
     */
    DATE("date", ViewComponentEnum.DATE_CONSTANT,"日期"),
    /**
     * 日期范围
     */
    DATE_RANGE("date-range", "","日期范围"),
    /**
     * 开关
     */
    SWITCH("switch", "","开关"),
    /**
     * 评分
     */
    RATE("rate", "","评分"),
    /**
     * 颜色
     */
    COLOR("color", "","颜色"),
    /**
     * 滑块（进度条）
     */
    SLIDER("slider", "","滑块（进度条）"),
    /**
     * 静态文字
     */
    STATIC_TEXT("static-text", "","静态文字"),
    /**
     * html
     */
    HTML_TEXT("html-text", "","html"),
    /**
     * 按钮
     */
    BUTTON("button", "","按钮"),
    /**
     * 分割线
     */
    DIVIDER("divider", "","分割线"),
    /**
     * 图片
     */
    IMAGE("image", "","图片"),
    /**
     * 内嵌网页
     */
    IFRAME("iframe", "","内嵌网页"),

    /**
     * 实例选择
     */
    INSTANCE_SELECT(ViewComponentEnum.INSTANCE_SELECT_CONSTANT, ViewComponentEnum.INSTANCE_SELECT_CONSTANT,"实例选择"),

    /**     高级组件      **/
    /**
     * 图片上传
     */
    PICTURE_UPLOAD("picture-upload", "","图片上传"),
    /**
     * 文件上传
     */
    FILE_UPLOAD("file-upload", "","文件上传"),
    /**
     * 富文本
     */
    RICH_EDITOR("rich-editor ", "","富文本"),
    /**
     * 级联选择
     */
    CASCADE("cascader", "","级联选择"),
    /**
     * 超级选择器（对象选择器）
     */
    SUPER_SELECT("super-select", "","超级选择器（对象选择器）"),
    /**
     * 数据表格
     */
    DATA_TABLE("data-table", "","数据表格"),
    /**
     *
     */

    /**     业务组件      **/
    /**
     * 人员选择器
     */
    USER(ViewComponentEnum.USER_CONSTANT, ViewComponentEnum.USER_CONSTANT,"人员选择器"),
    /**
     * 部门选择器
     */
    DEPARTMENT(ViewComponentEnum.DEPT_CONSTANT, ViewComponentEnum.DEPT_CONSTANT,"部门选择器"),
    /**
     * 对象选择器
     */
    OBJECT(ViewComponentEnum.OBJECT_CONSTANT, ViewComponentEnum.OBJECT_CONSTANT,"对象选择器"),
    /**
     * 角色选择器
     */
    ROLE(ViewComponentEnum.ROLE_CONSTANT, ViewComponentEnum.ROLE_CONSTANT,"角色选择器"),
    /**
     * 关系选择器
     */
    RELATION(ViewComponentEnum.RELATION_CONSTANT, ViewComponentEnum.RELATION_CONSTANT,"关系选择器"),
    /**
     * 角色用户组件
     */
    ROLE_USER(ViewComponentEnum.ROLE_USER_CONSTANT,ViewComponentEnum.ROLE_USER_CONSTANT,"角色用户组件");

    ViewComponentEnum(String type, String strategy, String desc) {
        this.type = type;
        this.strategy = strategy;
        this.desc = desc;
    }

    private final String type;

    private final String strategy;

    private final String desc;

    public static String getStrategy(String type) {
        for (ViewComponentEnum value : ViewComponentEnum.values()) {
            if (value.getType().equals(type)) {
                return value.getStrategy();
            }
        }
        return "";
    }

    public static final String INPUT_CONSTANT = "input";
    public static final String SELECT_CONSTANT = "select";
    public static final String USER_CONSTANT = "user";
    public static final String DEPT_CONSTANT = "department";
    public static final String OBJECT_CONSTANT = "object";
    public static final String ROLE_CONSTANT = "role";
    public static final String DEFAULT_CONSTANT = "default";
    public static final String RELATION_CONSTANT = "relation";
    public static final String ROLE_USER_CONSTANT = "role-user";
    public static final String DATE_CONSTANT = "date";
    public static final String INSTANCE_SELECT_CONSTANT = "instance-select";
}
