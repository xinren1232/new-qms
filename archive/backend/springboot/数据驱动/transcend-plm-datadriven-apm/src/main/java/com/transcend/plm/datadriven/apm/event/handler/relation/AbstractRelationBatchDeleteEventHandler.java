package com.transcend.plm.datadriven.apm.event.handler.relation;

import com.transcend.plm.datadriven.apm.event.entity.RelationBatchDeleteEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.RelationDelAndRemParamAo;

/**
 * @author bin.yin
 * @description: 关系和目标数据删除
 * @version:
 * @date 2024/06/13 13:49
 */
public abstract class AbstractRelationBatchDeleteEventHandler implements EventHandler<RelationBatchDeleteEventHandlerParam, Boolean> {

    @Override
    public RelationBatchDeleteEventHandlerParam initParam(Object[] args) {
        RelationDelAndRemParamAo relationDelAndRemParamAo = (RelationDelAndRemParamAo) args[1];
        RelationBatchDeleteEventHandlerParam param = RelationBatchDeleteEventHandlerParam.builder()
                .relationDelAndRemParamAo(relationDelAndRemParamAo)
                .build();
        param.setSpaceBid((String) args[0]);
        param.initAppAndObj(relationDelAndRemParamAo.getSourceSpaceAppBid());
        param.initRelationMeta(relationDelAndRemParamAo.getModelCode());
        return param;
    }
}
