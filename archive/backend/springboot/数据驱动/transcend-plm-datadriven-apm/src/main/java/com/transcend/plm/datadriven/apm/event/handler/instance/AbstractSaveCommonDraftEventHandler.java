package com.transcend.plm.datadriven.apm.event.handler.instance;

import com.transcend.plm.datadriven.apm.event.entity.AddEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;

/**
 * description: 保存草稿事件处理器(用一句话描述该文件做什么)
 *
 * @author sgx
 * date 2024/6/21 17:39
 * @version V1.0
 */
public abstract class AbstractSaveCommonDraftEventHandler implements EventHandler<AddEventHandlerParam, MSpaceAppData> {
    @Override
    public AddEventHandlerParam initParam(Object[] args) {
        return AddEventHandlerParam.builder()
                .mSpaceAppData((MSpaceAppData) args[1]).build().initAppAndObj((String) args[0]);
    }
}
