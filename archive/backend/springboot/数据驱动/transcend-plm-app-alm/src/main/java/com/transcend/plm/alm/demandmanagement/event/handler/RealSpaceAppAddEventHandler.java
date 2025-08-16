package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.datadriven.apm.event.entity.AddEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractAddEventHandler;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.common.wapper.TranscendObjectWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 真实空间应用Bid处理
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/1 17:11
 */
@Service
public class RealSpaceAppAddEventHandler extends AbstractAddEventHandler {

    @Override
    public boolean isMatch(AddEventHandlerParam param) {
        return false;
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    @Override
    public AddEventHandlerParam initParam(Object[] args) {
        AddEventHandlerParam param = super.initParam(args);

        TranscendObjectWrapper objectWrapper = new TranscendObjectWrapper(param.getMSpaceAppData());
        String realSpaceAppBidName = "realSpaceAppBid";
        String realSpaceAppBid = objectWrapper.getStr(realSpaceAppBidName);
        if (StringUtils.isNotBlank(realSpaceAppBid)) {
            param.initAppAndObj(realSpaceAppBid);

            ApmSpaceApp apmSpaceApp = param.getApmSpaceApp();
            objectWrapper.setSpaceBid(apmSpaceApp.getSpaceBid());
            objectWrapper.setSpaceAppBid(apmSpaceApp.getBid());

            args[0] = realSpaceAppBid;
        }
        return param;
    }
}
