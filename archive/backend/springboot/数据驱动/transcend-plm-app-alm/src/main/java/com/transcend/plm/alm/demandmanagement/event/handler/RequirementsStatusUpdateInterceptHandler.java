package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.alm.demandmanagement.service.RequirementsVerifyChildrenService;
import com.transcend.plm.datadriven.api.model.ObjectEnum;
import com.transcend.plm.datadriven.apm.event.entity.UpdateEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractUpdateEventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 状态更新拦截处理器
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/19 18:32
 */
@Service
@AllArgsConstructor
public class RequirementsStatusUpdateInterceptHandler extends AbstractUpdateEventHandler {

    private RequirementsVerifyChildrenService requirementsVerifyChildrenService;

    @Override
    public UpdateEventHandlerParam preHandle(UpdateEventHandlerParam param) {
        MSpaceAppData mSpaceAppData = param.getMSpaceAppData();
        String modelCode = param.getApmSpaceApp().getModelCode();
        String lifeCycleCode = mSpaceAppData.getLifeCycleCode();
        String bid = Optional.ofNullable(param.getBid()).orElse(mSpaceAppData.getBid());

        //执行校验
        requirementsVerifyChildrenService.verify(modelCode, bid, lifeCycleCode);
        return super.preHandle(param);
    }

    @Override
    public boolean isMatch(UpdateEventHandlerParam param) {
        return Optional.ofNullable(param.getMSpaceAppData())
                //是否修改状态
                .filter(mObject -> mObject.containsKey(ObjectEnum.LIFE_CYCLE_CODE.getCode()))
                //是否为支持的模型
                .map(r -> requirementsVerifyChildrenService.isSupport(param.getApmSpaceApp().getModelCode()))
                .orElse(false);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
