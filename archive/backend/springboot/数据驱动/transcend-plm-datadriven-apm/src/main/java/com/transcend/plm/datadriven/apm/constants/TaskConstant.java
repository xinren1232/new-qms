package com.transcend.plm.datadriven.apm.constants;

/**
 * @author unknown
 */
public class TaskConstant {
    private TaskConstant() {
    }
    /**
     * 未开始
     */
    public static final Integer NOT_START = 0;

    /**
     * 进行中
     */
    public static final Integer ACTIVE = 1;
    /**
     * 完成
     */
    public static final Integer COMPLETED = 2;

    /**
     * 流程类型
     */
    public static final String FLOW_TYPE = "flow";

    /**
     * 状态流程
     */
    public static final String FLOW_TYPE_STATE = "state";
    /**
     * 未处理
     */
    public static final String UNDO = "undo";

    /**
     * 已完成
     */
    public static final String DONE = "done";
}
