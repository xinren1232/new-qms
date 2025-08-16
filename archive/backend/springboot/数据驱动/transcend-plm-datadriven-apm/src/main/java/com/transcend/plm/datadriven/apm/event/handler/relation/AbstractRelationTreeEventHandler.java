package com.transcend.plm.datadriven.apm.event.handler.relation;

import com.transcend.plm.datadriven.api.model.MObjectTree;
import com.transcend.plm.datadriven.api.model.RelationMObject;
import com.transcend.plm.datadriven.apm.event.entity.RelationTreeEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;

import java.util.List;

/**
 * @author bin.yin
 * @description: 关系和目标数据删除
 * @version:
 * @date 2024/06/13 13:49
 */
public abstract class AbstractRelationTreeEventHandler implements EventHandler<RelationTreeEventHandlerParam, List<MObjectTree>> {

    @Override
    public RelationTreeEventHandlerParam initParam(Object[] args) {
        RelationTreeEventHandlerParam param = RelationTreeEventHandlerParam.builder()
                .relationMObject((RelationMObject)args[2])
                .filterRichText((Boolean) args[3])
                .build();
        param.setSpaceBid((String) args[0]);
        param.initAppAndObj((String) args[1]);
        param.initRelationMeta(param.getRelationMObject().getRelationModelCode());
        return param;
    }
}
