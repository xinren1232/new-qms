package com.transcend.plm.datadriven.apm.flow.event;

import com.transcend.plm.datadriven.apm.constants.FlowEventTypeConstant;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowNodeEvent;
import com.transcend.plm.datadriven.apm.flow.event.customize.IFlowCustomizeMethod;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.spring.PlmContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 流程事件自定义方法处理器
 * @createTime 2023-10-08 13:35:00
 */
@Component
@Slf4j
public class FlowEventCustomizeMethodHandler implements IFlowEventHandler {
    @Override
    public void handle(FlowEventBO eventBO) {
        ApmFlowInstanceNode instanceNode = eventBO.getInstanceNode();
        ApmFlowNodeEvent event = eventBO.getEvent();
        String nodeMethodPath = event.getNodeMethodPath();
        if(!getEventType().equals(event.getEventType())){
            log.error("当前节点的事件类型不匹配，节点：{}，事件：{}，期望事件类型：{}，实际事件类型：{}",instanceNode.getBid(),event.getBid(),getEventType(),event.getEventType());
            return;
        }
        IFlowCustomizeMethod customizeMethod = PlmContextHolder.getBean(nodeMethodPath, IFlowCustomizeMethod.class);
        if(customizeMethod == null){
            throw new PlmBizException("流程事件自定义方法不存在");
        }
        customizeMethod.execute(eventBO);
    }

    @Override
    public Integer getEventType() {
        return FlowEventTypeConstant.CUSTOMIZE_METHOD;
    }
}
