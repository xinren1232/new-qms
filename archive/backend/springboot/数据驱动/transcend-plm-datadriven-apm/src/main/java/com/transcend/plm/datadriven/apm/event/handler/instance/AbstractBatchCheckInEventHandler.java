package com.transcend.plm.datadriven.apm.event.handler.instance;

import com.transcend.plm.datadriven.api.model.MVersionObject;
import com.transcend.plm.datadriven.apm.event.entity.BatchCheckInHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;

import java.util.List;

/**
 * @author bin.yin
 * @description: 批量检入事件处理
 * @version:
 * @date 2024/06/17 11:20
 */
public abstract class AbstractBatchCheckInEventHandler implements EventHandler<BatchCheckInHandlerParam, List<MSpaceAppData>> {

    @Override
    public BatchCheckInHandlerParam initParam(Object[] args) {
        return BatchCheckInHandlerParam.builder().instanceList((List<MVersionObject>) args[1]).build().initAppAndObj((String) args[0]);
    }
}
