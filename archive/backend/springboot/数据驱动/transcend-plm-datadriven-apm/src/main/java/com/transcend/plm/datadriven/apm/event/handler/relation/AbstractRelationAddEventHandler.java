package com.transcend.plm.datadriven.apm.event.handler.relation;

import com.transcend.plm.datadriven.apm.event.entity.RelationAddEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.AddExpandAo;

/**
 * @author bin.yin
 * @description: 检入事件
 * @version:
 * @date 2024/06/13 13:49
 */
public abstract class AbstractRelationAddEventHandler implements EventHandler<RelationAddEventHandlerParam, Object> {

    @Override
    public RelationAddEventHandlerParam initParam(Object[] args) {
        RelationAddEventHandlerParam param = RelationAddEventHandlerParam.builder()
                .source((String) args[2])
                .addExpandAo((AddExpandAo) args[3]).build().initAppAndObj((String) args[1]);
        param.setSpaceBid((String) args[0]);
        param.initRelationMeta(param.getAddExpandAo().getRelationModelCode());
        return param;
    }
}
