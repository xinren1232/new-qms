package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.alm.demandmanagement.service.FlowRolePersonnelAdditionService;
import com.transcend.plm.datadriven.apm.event.entity.CompleteFlowNodeEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractCompleteFlowNodeEventHandler;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 流程角色追加处理器
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/4 16:57
 */
@Component
@AllArgsConstructor
public class FlowRolePersonnelAdditionHandler extends AbstractCompleteFlowNodeEventHandler {
    private FlowRolePersonnelAdditionService flowRolePersonnelAdditionService;


    @Override
    public CompleteFlowNodeEventHandlerParam preHandle(CompleteFlowNodeEventHandlerParam param) {
        flowRolePersonnelAdditionService.execute(param.getApmSpaceApp(), param.getNodeBid(),param.getMSpaceAppData());
        return super.preHandle(param);
    }

    @Override
    public boolean isMatch(CompleteFlowNodeEventHandlerParam param) {
        return Optional.ofNullable(param.getApmSpaceApp()).map(ApmSpaceApp::getBid)
                .map(flowRolePersonnelAdditionService::isSupport).orElse(false);
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
