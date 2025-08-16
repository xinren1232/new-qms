package com.transcend.plm.datadriven.apm.event.handler.instance;

import com.transcend.plm.datadriven.apm.event.entity.UpdateEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;

/**
 * @Describe
 * @Author yuhao.qiu
 * @Date 2024/6/14
 */
public abstract class AbstractUpdateEventHandler implements EventHandler<UpdateEventHandlerParam, Boolean> {

    @Override
    public UpdateEventHandlerParam initParam(Object[] args) {
        return UpdateEventHandlerParam.builder()
                .bid((String) args[1])
                .mSpaceAppData((MSpaceAppData) args[2])
                .build()
                .initAppAndObj((String) args[0]);
    }
}
