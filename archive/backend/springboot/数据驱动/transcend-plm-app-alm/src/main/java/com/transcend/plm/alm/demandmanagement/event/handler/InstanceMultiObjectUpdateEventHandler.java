package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.datadriven.apm.constants.DemandConstant;
import com.transcend.plm.datadriven.apm.event.entity.MultiObjectUpdateEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractMultiObjectUpdateEventHandler;
import com.transcend.plm.datadriven.apm.permission.enums.OperatorEnum;
import com.transcend.plm.datadriven.apm.permission.service.IPermissionCheckService;
import com.transcend.plm.datadriven.apm.space.pojo.dto.MultiObjectUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;


/**
 * SR,IR批量加锁解锁权限校验
 */
@Slf4j
@Component
public class InstanceMultiObjectUpdateEventHandler extends AbstractMultiObjectUpdateEventHandler {

    @Resource
    private IPermissionCheckService permissionCheckService;

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public MultiObjectUpdateEventHandlerParam preHandle(MultiObjectUpdateEventHandlerParam param) {
        List<MultiObjectUpdateDto> multiObjectUpdateDtoList = param.getMultiObjectUpdateDtoList();
        for (MultiObjectUpdateDto multiObjectUpdateDto : multiObjectUpdateDtoList) {
            if ((TranscendModel.IR.getCode().equals(multiObjectUpdateDto.getModelCode()) || TranscendModel.SR.getCode().equals(multiObjectUpdateDto.getModelCode()))
             && ObjectUtils.isNotEmpty(multiObjectUpdateDto.getData().get(DemandConstant.LOCK_FLAG))) {
                if (DemandConstant.NO_LOCK.equals(multiObjectUpdateDto.getData().get(DemandConstant.LOCK_FLAG))){
                    permissionCheckService.checkSpaceAppPermssion(multiObjectUpdateDto.getModelCode(), multiObjectUpdateDto.getBids(), OperatorEnum.UNLOCK.getCode());
                } else if (DemandConstant.YES_LOCK.equals(multiObjectUpdateDto.getData().get(DemandConstant.LOCK_FLAG))) {
                    permissionCheckService.checkSpaceAppPermssion(multiObjectUpdateDto.getModelCode(), multiObjectUpdateDto.getBids(), OperatorEnum.LOCK.getCode());
                }
            }
        }
        return super.preHandle(param);
    }

    @Override
    public Boolean postHandle(MultiObjectUpdateEventHandlerParam param, Boolean result) {
        return super.postHandle(param, result);
    }

    @Override
    public boolean isMatch(MultiObjectUpdateEventHandlerParam param) {
        return true;
    }
}
