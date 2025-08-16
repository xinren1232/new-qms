package com.transcend.plm.datadriven.apm.event.handler.instance;

import com.transcend.plm.datadriven.api.model.MVersionObject;
import com.transcend.plm.datadriven.apm.event.entity.CheckInEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;

/**
 * @author bin.yin
 * @description: 检入事件
 * @version:
 * @date 2024/06/13 13:49
 */
public abstract class AbstractCheckInEventHandler implements EventHandler<CheckInEventHandlerParam, MSpaceAppData> {

    @Override
    public CheckInEventHandlerParam initParam(Object[] args) {
        return CheckInEventHandlerParam.builder()
                .mObject((MVersionObject) args[1]).build().initAppAndObj((String) args[0]);
    }
}
