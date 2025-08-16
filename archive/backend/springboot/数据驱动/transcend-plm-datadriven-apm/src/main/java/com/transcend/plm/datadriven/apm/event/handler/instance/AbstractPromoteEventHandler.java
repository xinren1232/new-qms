package com.transcend.plm.datadriven.apm.event.handler.instance;

import com.transcend.plm.datadriven.api.model.dto.LifeCyclePromoteDto;
import com.transcend.plm.datadriven.apm.event.entity.PromoteEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;

/**
 * @author bin.yin
 * @description: 生命周期提升事件处理
 * @version:
 * @date 2024/06/17 13:43
 */
public abstract class AbstractPromoteEventHandler implements EventHandler<PromoteEventHandlerParam, MSpaceAppData> {

    @Override
    public PromoteEventHandlerParam initParam(Object[] args) {
        PromoteEventHandlerParam promoteEventHandlerParam = PromoteEventHandlerParam.builder()
                .lifeCyclePromoteDto((LifeCyclePromoteDto) args[2]).build()
                .initAppAndObj((String) args[1]);
        promoteEventHandlerParam.setSpaceBid((String) args[0]);
        return promoteEventHandlerParam;
    }
}
