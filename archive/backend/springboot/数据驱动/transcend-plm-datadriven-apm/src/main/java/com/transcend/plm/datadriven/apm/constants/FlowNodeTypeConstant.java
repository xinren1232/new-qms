package com.transcend.plm.datadriven.apm.constants;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 节点类型常量
 * @createTime 2023-10-07 14:24:00
 */
public class FlowNodeTypeConstant {
    private FlowNodeTypeConstant() {
    }

    public static final Integer START_NODE = 0;
    /**
     * 进行中节点
     */
    public static final Integer PROCESSING_NODE = 1;
    /**
     * 结束节点
     */
    public static final Integer END_NODE = 2;
}
