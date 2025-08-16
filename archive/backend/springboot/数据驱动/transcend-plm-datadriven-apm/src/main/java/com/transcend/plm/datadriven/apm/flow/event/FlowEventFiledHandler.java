package com.transcend.plm.datadriven.apm.flow.event;

import cn.hutool.core.date.DateUtil;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.framework.sso.tool.TranscendUserContextHolder;
import com.transcend.plm.datadriven.apm.constants.FlowEventTypeConstant;
import com.transcend.plm.datadriven.apm.enums.FlowEnum;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowNodeEvent;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppDataDrivenService;
import com.transsion.framework.auth.IUser;
import com.transsion.framework.auth.IUserContext;
import com.transsion.framework.auth.UserContextDto;
import com.transsion.framework.auth.dto.UserLoginDto;
import com.transsion.framework.context.holder.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 流程事件字段处理器
 * @createTime 2023-10-08 11:48:00
 */
@Component
@Slf4j
public class FlowEventFiledHandler implements IFlowEventHandler {
    @Resource
    private IApmSpaceAppDataDrivenService apmSpaceAppDataDrivenService;
    @Override
    public void handle(FlowEventBO eventBO) {
        try {
            UserLoginDto userLoginDto = new UserLoginDto();
            userLoginDto.setEmployeeNo(eventBO.getCompleteEmpNO());
            userLoginDto.setRealName(eventBO.getCompleteEmpName());
            IUserContext<IUser> userContextDto = new UserContextDto<>(null, userLoginDto);
            UserContextHolder.setUser(userContextDto);
            TranscendUserContextHolder.setUser(userContextDto);
            ApmFlowInstanceNode instanceNode = eventBO.getInstanceNode();
            ApmFlowNodeEvent event = eventBO.getEvent();
            if(!getEventType().equals(event.getEventType())){
                log.error("当前节点的事件类型不匹配，节点：{}，事件：{}，期望事件类型：{}，实际事件类型：{}",instanceNode.getBid(),event.getBid(),getEventType(),event.getEventType());
                return;
            }
            MSpaceAppData mSpaceAppData = new MSpaceAppData();
            if(FlowEnum.FLOW_EVENT_NOW.getCode().equals(event.getFiledValueType())){
                mSpaceAppData.put(event.getFiledName(), DateUtil.formatDateTime(new Date()));
            }else if (FlowEnum.FLOW_EVENT_LOGINUSER.getCode().equals(event.getFiledValueType())){
                mSpaceAppData.put(event.getFiledName(), eventBO.getCompleteEmpNO());
            }else{
                mSpaceAppData.put(event.getFiledName(), event.getFiledValue());
            }
            apmSpaceAppDataDrivenService.updatePartialContent(instanceNode.getSpaceAppBid(),instanceNode.getInstanceBid(), mSpaceAppData);
        } finally {
            UserContextHolder.removeUser();
            TranscendUserContextHolder.removeUser();
        }
    }

    @Override
    public Integer getEventType() {
        return FlowEventTypeConstant.MODIFY_FIELD;
    }
}
