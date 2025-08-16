package com.transcend.plm.datadriven.apm.space.event.impl;

import com.google.common.collect.Lists;
import com.transcend.plm.datadriven.apm.flow.pojo.qo.ApmFlowQo;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleUserAO;
import com.transcend.plm.datadriven.apm.permission.pojo.bo.ApmUser;
import com.transcend.plm.datadriven.apm.flow.service.IRuntimeService;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRole;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.apm.space.event.IFieldUpdateEventHandler;
import com.transcend.plm.datadriven.apm.space.pojo.bo.FieldUpdateEvent;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 责任人更新事件处理
 * @createTime 2023-11-02 11:48:00
 */
@Component
public class ResponsibleUpdateHandler implements IFieldUpdateEventHandler {

    @Resource
    private IRuntimeService runtimeService;

    @Resource
    private ApmRoleService apmRoleService;
    @Override
    public void handleFieldUpdateEvent(final FieldUpdateEvent fieldUpdateEvent) {
        ApmRole personResponsibleRole = apmRoleService.getByCodeAndApp("personResponsible", fieldUpdateEvent.getSpaceAppBid());
        ApmRoleUserAO apmRoleUserAO = new ApmRoleUserAO();
        apmRoleUserAO.setRoleBid(personResponsibleRole.getBid());
        //校验fieldUpdateEvent.getFieldValue()是否为空
        if(null == fieldUpdateEvent.getFieldValue()){
            apmRoleUserAO.setUserList(Lists.newArrayList());
            ApmFlowQo apmFlowQo = new ApmFlowQo();
            apmFlowQo.setSpaceBid(fieldUpdateEvent.getSpaceBid());
            apmFlowQo.setSpaceAppBid(fieldUpdateEvent.getSpaceAppBid());
            apmFlowQo.setRoleUserList(Lists.newArrayList(apmRoleUserAO));
            runtimeService.updateRoleUser(runtimeService.getVersionInstanceBid(fieldUpdateEvent.getSpaceAppBid(), fieldUpdateEvent.getInstanceBid()), apmFlowQo);
            return;
        }
        //将fieldUpdateEvent.getFieldValue()转换为List<String>
        if (!(fieldUpdateEvent.getFieldValue() instanceof List)) {
            throw new PlmBizException("责任人更新事件处理失败，fieldUpdateEvent.getFieldValue()不是List<String>类型");
        }
        List<String> empNos = (List<String>)fieldUpdateEvent.getFieldValue();
        if (CollectionUtils.isNotEmpty(empNos)) {
            List<ApmUser> apmUserList = Lists.newArrayList();
            empNos.forEach(empNo -> apmUserList.add(ApmUser.builder().empNo(empNo).build()));
            apmRoleUserAO.setUserList(apmUserList);
        } else {
            apmRoleUserAO.setUserList(Lists.newArrayList());
        }
        ApmFlowQo apmFlowQo = new ApmFlowQo();
        apmFlowQo.setSpaceBid(fieldUpdateEvent.getSpaceBid());
        apmFlowQo.setSpaceAppBid(fieldUpdateEvent.getSpaceAppBid());
        apmFlowQo.setRoleUserList(Lists.newArrayList(apmRoleUserAO));
        runtimeService.updateRoleUser(runtimeService.getVersionInstanceBid(fieldUpdateEvent.getSpaceAppBid(), fieldUpdateEvent.getInstanceBid()), apmFlowQo);
    }
}
