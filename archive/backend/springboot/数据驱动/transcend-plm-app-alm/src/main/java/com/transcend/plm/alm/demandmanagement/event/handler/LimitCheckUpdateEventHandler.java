package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.datadriven.apm.event.entity.UpdateEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractUpdateEventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.strategy.UpdatePropertyLimitStrategy;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Describe Ar修改事件
 * @Author haijun.ren
 * @Date 2024/9/27
 */
@Slf4j
@Component
public class LimitCheckUpdateEventHandler extends AbstractUpdateEventHandler {

    @Resource
    UpdatePropertyLimitStrategy updatePropertyLimitStrategy;

    @Override
    public UpdateEventHandlerParam preHandle(UpdateEventHandlerParam param) {
        MSpaceAppData copyMspaceAppData = new MSpaceAppData();
        copyMspaceAppData.putAll(param.getMSpaceAppData());
        copyMspaceAppData.put(CommonConst.BID_STR, param.getBid());
        updatePropertyLimitStrategy.preHandler(param.getApmSpaceApp(), copyMspaceAppData);
        return super.preHandle(param);
    }

    @Override
    public Boolean postHandle(UpdateEventHandlerParam param, Boolean result) {
        MSpaceAppData mSpaceAppData = param.getMSpaceAppData();
        return super.postHandle(param, result);
    }

    @Override
    public boolean isMatch(UpdateEventHandlerParam param) {
        return true;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
