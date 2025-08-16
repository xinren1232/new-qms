package com.transcend.plm.datadriven.apm.event.handler.instance;

import com.transcend.plm.datadriven.api.model.MVersionObject;
import com.transcend.plm.datadriven.apm.event.entity.SaveDraftEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;

/**
 * @author bin.yin
 * @description: 暂存事件处理
 * @version:
 * @date 2024/06/17 11:42
 */
public abstract class AbstractSaveDraftEventHandler implements EventHandler<SaveDraftEventHandlerParam, Boolean> {
    @Override
    public SaveDraftEventHandlerParam initParam(Object[] args) {
        return SaveDraftEventHandlerParam.builder().draftData((MVersionObject) args[1]).build().initAppAndObj((String) args[0]);
    }
}
