package com.transcend.plm.datadriven.apm.event.handler.instance;

import com.transcend.plm.datadriven.apm.event.entity.BatchCancelCheckOutHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;

import java.util.List;
import java.util.Map;

/**
 * @author bin.yin
 * @description: 批量取消检出事件处理
 * @version:
 * @date 2024/06/17 10:49
 */
public abstract class AbstractBatchCancelCheckOutEventHandler implements EventHandler<BatchCancelCheckOutHandlerParam, Map<String, MSpaceAppData>> {
    @Override
    public BatchCancelCheckOutHandlerParam initParam(Object[] args) {
        return BatchCancelCheckOutHandlerParam.builder().bids((List<String>) args[1]).build().initAppAndObj((String) args[0]);
    }
}
