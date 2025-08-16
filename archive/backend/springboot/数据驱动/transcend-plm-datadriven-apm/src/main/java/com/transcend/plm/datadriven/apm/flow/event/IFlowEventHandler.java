package com.transcend.plm.datadriven.apm.flow.event;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 流程事件处理器
 * @createTime 2023-10-08 10:34:00
 */
public interface IFlowEventHandler {
    /**
     * 处理流程事件
     *
     * @param eventBO 流程实例节点
     */
    void handle(FlowEventBO eventBO);

    /**
     * getEventType
     *
     * @return {@link Integer}
     */
    Integer getEventType();
}
