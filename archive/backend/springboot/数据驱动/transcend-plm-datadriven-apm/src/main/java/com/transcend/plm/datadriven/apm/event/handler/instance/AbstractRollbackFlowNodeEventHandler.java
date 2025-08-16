package com.transcend.plm.datadriven.apm.event.handler.instance;

import com.transcend.plm.datadriven.apm.event.entity.RollbackFlowNodeEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;

/**
 * 回退流程实例事件处理器
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/4/3 09:06
 */
public abstract class AbstractRollbackFlowNodeEventHandler implements EventHandler<RollbackFlowNodeEventHandlerParam, Object> {

    @Override
    public RollbackFlowNodeEventHandlerParam initParam(Object[] args) {
        return new RollbackFlowNodeEventHandlerParam()
                .setBid((String) args[1])
                .setNodeBid((String) args[2])
                .setMSpaceAppData((MSpaceAppData) args[3])
                .initAppAndObj((String) args[0]);
    }
}
