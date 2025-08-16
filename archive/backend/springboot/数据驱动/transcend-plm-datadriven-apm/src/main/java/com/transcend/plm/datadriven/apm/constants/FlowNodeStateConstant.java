package com.transcend.plm.datadriven.apm.constants;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 流程节点状态常量
 * @createTime 2023-10-07 11:33:00
 */
public class FlowNodeStateConstant {
    private FlowNodeStateConstant() {
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
}
