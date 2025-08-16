package com.transcend.plm.datadriven.apm.event.handler.instance;

import com.transcend.plm.datadriven.apm.event.entity.BatchDeleteEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.entity.BatchLogicDeleteEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;
import com.transcend.plm.datadriven.apm.space.pojo.qo.BatchLogicDelAndRemQo;

import java.util.List;

/**
 * @Describe
 * @Author yuhao.qiu
 * @Date 2024/6/17
 */
public abstract class AbstractBatchLogicDeleteEventHandler implements EventHandler<BatchLogicDeleteEventHandlerParam, Boolean> {
    @Override
    public BatchLogicDeleteEventHandlerParam initParam(Object[] args) {
        return BatchLogicDeleteEventHandlerParam.builder()
                .qo((BatchLogicDelAndRemQo) args[1])
                .build()
                .initAppAndObj((String) args[0]);
    }
}
