package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.datadriven.apm.event.entity.RelationAddEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.relation.AbstractRelationAddEventHandler;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.AddExpandAo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.common.wapper.TranscendObjectWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 真实空间应用Bid处理关系事件
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/1 17:11
 */
@Service
public class RealSpaceAppRelAddEventHandler extends AbstractRelationAddEventHandler {

    @Override
    public boolean isMatch(RelationAddEventHandlerParam param) {
        return false;
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    @Override
    public RelationAddEventHandlerParam initParam(Object[] args) {
        RelationAddEventHandlerParam param = super.initParam(args);
        AddExpandAo addExpandAo = param.getAddExpandAo();

        String realSpaceAppBid = addExpandAo.getRealSpaceAppBid();
        if (StringUtils.isNotBlank(realSpaceAppBid)) {
            ApmSpaceApp apmSpaceApp = param.getApmSpaceApp();
            param.initAppAndObj(realSpaceAppBid);
            Optional.ofNullable(addExpandAo.getTargetMObjects())
                    .ifPresent(list -> list.stream().map(TranscendObjectWrapper::new)
                            .forEach(wrapper -> {
                                wrapper.setSpaceBid(apmSpaceApp.getSpaceBid());
                                wrapper.setSpaceAppBid(apmSpaceApp.getBid());
                            }));

            args[0] = realSpaceAppBid;
        }
        return param;
    }


}
