package com.transcend.plm.datadriven.apm.constants;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 节点完成类型常量
 * @createTime 2023-10-07 14:36:00
 */
public class FlowNodeCompleteType {
    private FlowNodeCompleteType() {
    }

    /**
     * 自动完成
     */
    public static final Integer AUTO_COMPLETE = 0;
    /**
     * 单人确认完成
     */
    public static final Integer SINGLE_CONFIRM_COMPLETE = 1;
    /**
     * 多人确认完成
     */
    public static final Integer MULTI_CONFIRM_COMPLETE = 2;
}
