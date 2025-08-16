package com.transcend.plm.datadriven.apm.event.handler.instance;

import com.transcend.plm.datadriven.apm.event.entity.CancelCheckOutEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;

/**
 * @author bin.yin
 * @description: 取消检出事件处理
 * @version:
 * @date 2024/06/17 10:54
 */
public abstract class AbstractCancelCheckOutEventHandler implements EventHandler<CancelCheckOutEventHandlerParam, MSpaceAppData> {

    @Override
    public CancelCheckOutEventHandlerParam initParam(Object[] args) {
        return CancelCheckOutEventHandlerParam.builder().bid((String) args[1]).build().initAppAndObj((String) args[0]);
    }
}
