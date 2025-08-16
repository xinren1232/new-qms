package com.transcend.plm.datadriven.apm.event.handler.instance;

import com.transcend.plm.datadriven.apm.event.entity.MultiObjectUpdateEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;
import com.transcend.plm.datadriven.apm.space.pojo.dto.MultiObjectUpdateDto;

import java.util.List;

/**
 * @author bin.yin
 * @description:
 * @version:
 * @date 2024/06/13 13:49
 */
public abstract class AbstractMultiObjectUpdateEventHandler implements EventHandler<MultiObjectUpdateEventHandlerParam, Boolean> {

    @Override
    public MultiObjectUpdateEventHandlerParam initParam(Object[] args) {
        return MultiObjectUpdateEventHandlerParam.builder()
                .multiObjectUpdateDtoList((List<MultiObjectUpdateDto>) args[1]).build();
    }
}
