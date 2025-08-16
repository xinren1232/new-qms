package com.transcend.plm.datadriven.apm.event.handler.instance;

import com.transcend.plm.datadriven.apm.event.entity.AddEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;
import com.transcend.plm.datadriven.apm.space.model.ApmSpaceAppDataDrivenOperationFilterBo;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;

/**
 * @author bin.yin
 * @description:
 * @version:
 * @date 2024/06/13 13:49
 */
public abstract class AbstractAddEventHandler implements EventHandler<AddEventHandlerParam, MSpaceAppData> {

    @Override
    public AddEventHandlerParam initParam(Object[] args) {
        return AddEventHandlerParam.builder()
                .mSpaceAppData((MSpaceAppData) args[1])
                .filterBo(args.length > 2 ? (ApmSpaceAppDataDrivenOperationFilterBo) args[2] : null).build().initAppAndObj((String) args[0]);
    }
}
