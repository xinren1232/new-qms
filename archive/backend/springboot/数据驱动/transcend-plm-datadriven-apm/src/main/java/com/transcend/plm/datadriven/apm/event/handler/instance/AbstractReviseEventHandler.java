package com.transcend.plm.datadriven.apm.event.handler.instance;

import com.transcend.plm.datadriven.apm.event.entity.ReviseEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;

/**
 * @author bin.yin
 * @description: 修订事件处理
 * @version:
 * @date 2024/06/17 13:46
 */
public abstract class AbstractReviseEventHandler implements EventHandler<ReviseEventHandlerParam, MSpaceAppData> {

    @Override
    public ReviseEventHandlerParam initParam(Object[] args) {
        return ReviseEventHandlerParam.builder().bid((String) args[1]).build().initAppAndObj((String) args[0]);
    }
}
