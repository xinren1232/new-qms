package com.transcend.plm.datadriven.apm.event.handler.instance;

import com.transcend.plm.datadriven.apm.event.entity.BatchUpdateEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.entity.UpdateEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;

import java.util.List;

/**
 * @Describe
 * @Author yuhao.qiu
 * @Date 2024/6/14
 */
public abstract class AbstractBatchUpdateEventHandler implements EventHandler<BatchUpdateEventHandlerParam, Boolean> {

    @Override
    public BatchUpdateEventHandlerParam initParam(Object[] args) {
        return BatchUpdateEventHandlerParam.builder()
                .bids((List<String>) args[2])
                .mSpaceAppData((MSpaceAppData) args[3])
                .build()
                .initAppAndObj((String) args[1]);
    }
}
