package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.alm.demandmanagement.service.RelationUnlinkInterceptorService;
import com.transcend.plm.datadriven.apm.event.entity.RelationBatchDeleteEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.relation.AbstractRelationBatchDeleteEventHandler;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.RelationDelAndRemParamAo;
import com.transcend.plm.datadriven.common.tool.Assert;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 删除关联关系拦截器
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/5 18:07
 */
@Service
@AllArgsConstructor
public class RelationBatchDeleteInterceptorHandler extends AbstractRelationBatchDeleteEventHandler {

    private final RelationUnlinkInterceptorService interceptorService;

    @Override
    public RelationBatchDeleteEventHandlerParam preHandle(RelationBatchDeleteEventHandlerParam param) {
        RelationDelAndRemParamAo paramAo = param.getRelationDelAndRemParamAo();
        boolean intercept = interceptorService.isIntercept(paramAo.getModelCode(),
                paramAo.getSourceSpaceAppBid(), paramAo.getSourceBid(), paramAo.getRelationBids());

        Assert.isFalse(intercept, "无法解除关系，需至少保留一个");
        return super.preHandle(param);
    }

    @Override
    public boolean isMatch(RelationBatchDeleteEventHandlerParam param) {
        return Optional.ofNullable(param.getRelationDelAndRemParamAo())
                .map(RelationDelAndRemParamAo::getModelCode)
                .map(interceptorService::isSupport).orElse(false);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
