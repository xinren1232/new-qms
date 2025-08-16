package com.transcend.plm.datadriven.apm.event.handler.instance;

import com.transcend.plm.datadriven.api.model.MVersionObject;
import com.transcend.plm.datadriven.apm.event.entity.BatchSaveDraftEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;

import java.util.List;

/**
 * @author bin.yin
 * @description: 批量暂存事件处理
 * @version:
 * @date 2024/06/17 11:45
 */
public abstract class AbstractBatchSaveDraftEventHandler implements EventHandler<BatchSaveDraftEventHandlerParam, Boolean> {
    @Override
    public BatchSaveDraftEventHandlerParam initParam(Object[] args) {
        return BatchSaveDraftEventHandlerParam.builder().draftDataList((List<MVersionObject>) args[1]).build().initAppAndObj((String) args[0]);
    }
}
