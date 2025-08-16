package com.transcend.plm.datadriven.apm.event.handler.relation;

import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.RelationMObject;
import com.transcend.plm.datadriven.apm.event.entity.RelationListEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;

import java.util.List;

/**
 * @author bin.yin
 * @description: 关系和目标数据删除
 * @version:
 * @date 2024/06/13 13:49
 */
public abstract class AbstractRelationListEventHandler implements EventHandler<RelationListEventHandlerParam, List<MObject>> {

    @Override
    public RelationListEventHandlerParam initParam(Object[] args) {
        RelationListEventHandlerParam param = RelationListEventHandlerParam.builder()
                .relationMObject((RelationMObject)args[2])
                .build();
        param.setSpaceBid((String) args[0]);
        param.initAppAndObj((String) args[1]);
        param.initRelationMeta(param.getRelationMObject().getRelationModelCode());
        return param;
    }
}
