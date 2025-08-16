package com.transcend.plm.datadriven.apm.event.handler.instance;

import com.transcend.plm.datadriven.apm.event.entity.LogicalDeleteEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;

/**
 * @Describe
 * @Author yuhao.qiu
 * @Date 2024/6/14
 */
public abstract class AbstractLogicalDeleteEventHandler implements EventHandler<LogicalDeleteEventHandlerParam, Boolean> {
    @Override
    public LogicalDeleteEventHandlerParam initParam(Object[] args) {
        return LogicalDeleteEventHandlerParam.builder()
                .bid((String) args[1]).build().initAppAndObj((String) args[0]);
    }
}
