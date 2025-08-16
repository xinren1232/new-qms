package com.transcend.plm.datadriven.apm.event.handler.relation;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.RelationMObject;
import com.transcend.plm.datadriven.apm.event.entity.RelationPageEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;
import com.transsion.framework.dto.BaseRequest;

/**
 * @author bin.yin
 * @description: 关系和目标数据删除
 * @version:
 * @date 2024/06/13 13:49
 */
public abstract class AbstractRelationPageEventHandler implements EventHandler<RelationPageEventHandlerParam, PagedResult<MObject>> {

    @Override
    public RelationPageEventHandlerParam initParam(Object[] args) {
        RelationPageEventHandlerParam param = RelationPageEventHandlerParam.builder()
                .relationMObject((BaseRequest<RelationMObject>)args[2])
                .filterRichText((Boolean) args[3])
                .build();
        param.setSpaceBid((String) args[0]);
        param.initAppAndObj((String) args[1]);
        param.initRelationMeta(param.getRelationMObject().getParam().getRelationModelCode());
        return param;
    }
}
