package com.transcend.plm.datadriven.apm.constants;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 流程状态常量
 * @createTime 2023-10-10 11:46:00
 */
public class FlowStateConstant {
    private FlowStateConstant() {
    }
    /**
     * 未开始
     */
    public static final Integer NOT_START = 0;
    /**
     * 进行中
     */
    public static final Integer RUNNING = 1;
    /**
     * 完成
     */
    public static final Integer COMPLETED = 2;

    public static final String COLUMN_NAME_FLOW_STATE = "flow_state";
    public static final String COLUMN_NAME_WORK_ITEM_STATE = "work_item_state";
}
