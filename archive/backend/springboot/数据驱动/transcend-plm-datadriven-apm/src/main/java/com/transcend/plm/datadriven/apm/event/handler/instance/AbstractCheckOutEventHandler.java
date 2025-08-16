package com.transcend.plm.datadriven.apm.event.handler.instance;

import com.transcend.plm.datadriven.apm.event.entity.CheckOutEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;

/**
 * @author bin.yin
 * @description: 检出事件处理
 * @version:
 * @date 2024/06/17 09:47
 */
public abstract class AbstractCheckOutEventHandler implements EventHandler<CheckOutEventHandlerParam, MSpaceAppData> {
    @Override
    public CheckOutEventHandlerParam initParam(Object[] args) {
        return CheckOutEventHandlerParam.builder().bid((String) args[1]).build().initAppAndObj((String) args[0]);
    }
}
