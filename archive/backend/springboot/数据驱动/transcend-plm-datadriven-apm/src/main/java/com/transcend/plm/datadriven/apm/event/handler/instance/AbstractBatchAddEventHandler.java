package com.transcend.plm.datadriven.apm.event.handler.instance;

import com.transcend.plm.datadriven.apm.event.entity.BatchAddEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.entity.base.EventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;

import java.util.List;

/**
 * @Describe
 * @Author yuhao.qiu
 * @Date 2024/6/17
 */

public abstract class AbstractBatchAddEventHandler implements EventHandler<BatchAddEventHandlerParam, Boolean> {
    @Override
    public BatchAddEventHandlerParam initParam(Object[] args) {
        EventHandlerParam param = BatchAddEventHandlerParam.builder()
                .mSpaceAppDataList((List<MSpaceAppData>) args[2])
                .build()
                .initAppAndObj((String) args[1]);
        param.setSpaceBid((String) args[0]);
        return (BatchAddEventHandlerParam) param;
    }
}