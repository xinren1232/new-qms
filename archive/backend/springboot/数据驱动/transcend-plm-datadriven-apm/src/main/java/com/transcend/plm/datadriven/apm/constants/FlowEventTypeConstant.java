package com.transcend.plm.datadriven.apm.constants;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 流程事件类型常量
 * @createTime 2023-10-08 15:46:00
 */
public class FlowEventTypeConstant {
    public static final Integer RELATE_COMPLETE_TYPE_ONE = 1;
    public static final Integer RELATE_COMPLETE_TYPE_ALL = 2;

    private FlowEventTypeConstant() {
    }

    /**
     * 事件类型，1.状态扭转，2.修改字段值，3.指定节点负责人，4.指定角色负责人,5.自定义方法,6.关联驱动
     */
    public static final Integer NODE_STATUS = 1;
    public static final Integer MODIFY_FIELD = 2;
    public static final Integer NODE_RESPONSIBLE = 3;
    public static final Integer ROLE_RESPONSIBLE = 4;
    public static final Integer CUSTOMIZE_METHOD = 5;
    public static final Integer DRIVE_RELATE = 6;

    /**
     * 触发时间，1.激活前，2.完成后
     */
    public static final Integer BEFORE_ACTIVE = 1;
    public static final Integer AFTER_COMPLETE = 2;
}
