package com.transcend.plm.datadriven.apm.flow.event.customize;

import com.transcend.plm.datadriven.apm.flow.event.FlowEventBO;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 流程事件自定义方法定义，用户自己实现的类需要注入到spring容器中
 * @createTime 2023-10-08 14:20:00
 */
public interface IFlowCustomizeMethod {
    /**
     * 流程事件自定义方法
     *
     * @param eventBO 流程事件上下文
     */
    void execute(FlowEventBO eventBO);
}
