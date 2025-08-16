package com.transcend.plm.datadriven.apm.event.handler.instance;

import com.transcend.plm.datadriven.apm.event.entity.BatchDeleteEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;

import java.util.List;

/**
 * @Describe
 * @Author yuhao.qiu
 * @Date 2024/6/17
 */
public abstract class AbstractBatchDeleteEventHandler implements EventHandler<BatchDeleteEventHandlerParam, Boolean> {
    @Override
    public BatchDeleteEventHandlerParam initParam(Object[] args) {
        return BatchDeleteEventHandlerParam.builder()
                .bids((List<String>) args[1])
                .build()
                .initAppAndObj((String) args[0]);
    }
}
