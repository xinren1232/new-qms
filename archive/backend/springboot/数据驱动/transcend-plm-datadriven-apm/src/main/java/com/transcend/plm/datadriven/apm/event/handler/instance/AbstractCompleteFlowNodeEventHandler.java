package com.transcend.plm.datadriven.apm.event.handler.instance;

import com.transcend.plm.datadriven.apm.event.entity.CompleteFlowNodeEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;

/**
 * @author haijun.ren
 * @description: 目标对象批量选取
 * @version:
 * @date 2024/08/28 20:01
 */
public abstract class AbstractCompleteFlowNodeEventHandler implements EventHandler<CompleteFlowNodeEventHandlerParam, Object> {

    @Override
    public CompleteFlowNodeEventHandlerParam initParam(Object[] args) {
        CompleteFlowNodeEventHandlerParam param = CompleteFlowNodeEventHandlerParam.builder()
                .bid((String) args[1])
                .nodeBid((String) args[2])
                .mSpaceAppData((MSpaceAppData)args[3])
                .build();
        param.initAppAndObj((String) args[0]);
        return param;
    }
}
