package com.transcend.plm.datadriven.apm.event.handler.relation;

import com.transcend.plm.datadriven.apm.event.entity.RelationBatchSelectEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;
import com.transcend.plm.datadriven.apm.space.model.ApmRelationMultiTreeAddParam;

/**
 * @author haijun.ren
 * @description: 目标对象批量选取
 * @version:
 * @date 2024/08/28 20:01
 */
public abstract class AbstractRelationBatchSelectEventHandler implements EventHandler<RelationBatchSelectEventHandlerParam, Object> {

    @Override
    public RelationBatchSelectEventHandlerParam initParam(Object[] args) {
        ApmRelationMultiTreeAddParam relationMultiTreeAddParam = (ApmRelationMultiTreeAddParam) args[2];
        RelationBatchSelectEventHandlerParam param = RelationBatchSelectEventHandlerParam.builder()
                .relationMultiTreeAddParam(relationMultiTreeAddParam)
                .build();
        param.setSpaceBid((String) args[0]);
        param.initAppAndObj((String) args[1]);
        param.initRelationMeta(relationMultiTreeAddParam.getRelationModelCode());
        return param;
    }
}
