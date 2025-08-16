package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.alm.demandmanagement.service.DemandManagementService;
import com.transcend.plm.alm.tools.TranscendTools;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.apm.enums.TypeEnum;
import com.transcend.plm.datadriven.apm.event.entity.UpdateEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractUpdateEventHandler;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceRoleUser;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowInstanceRoleUserService;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleVO;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmSphere;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmSphereService;
import com.transcend.plm.datadriven.apm.space.enums.PermissionCheckEnum;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.model.SpaceAppDataEnum;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description 需求管理重复需求生命周期变更后置处理事件
 * @date 2024/06/21 11:24
 **/
@Slf4j
@Service
public class DemandManagementHandler extends AbstractUpdateEventHandler {

    private static final String OBJECT_BID = "1253640663282315264";

    @Resource
    private DemandManagementService demandManagementService;

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private IBaseApmSpaceAppDataDrivenService iBaseApmSpaceAppDataDrivenService;

    /**
     * ir需求需要解析的角色
     */
    @Value("#{'${transcend.plm.apm.irDemand.osVersionRoleCodes:osversionproductplanning,TOSVersionSEManager,osSpm}'.split(',')}")
    private List<String> osVersionRoleCodes;

    /**
     * ir需求解析角色对应领域自动
     */
    @Value("#{'${transcend.plm.apm.irDemand.osVersionAttrCodes:osVersionProductPlanningManager,osVersionSeResponsiblePerson,osSpm}'.split(',')}")
    private List<String> osVersionAttrCodes;

    @Resource
    private ApmSphereService apmSphereService;

    @Resource
    private ApmRoleService roleService;

    @Resource
    private ApmFlowInstanceRoleUserService apmFlowInstanceRoleUserService;

    @Override
    public boolean isMatch(UpdateEventHandlerParam param ) {

        return TranscendModel.RR.getCode().equals(param.getCfgObjectVo().getModelCode());
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Boolean postHandle(UpdateEventHandlerParam param, Boolean result) {
        log.info("需求管理后置处理事件开始执行");
        if (StringUtils.isNotBlank(param.getMSpaceAppData().getLifeCycleCode())) {
            demandManagementService.updateBelongDemandLifecycle(param.getCfgObjectVo().getModelCode(),
                    param.getApmSpaceApp().getBid(), param.getBid(), param.getMSpaceAppData().getLifeCycleCode());
        }
       /* if (ObjectUtils.isNotEmpty(param.getMSpaceAppData().get("duplicateRequirementNumber"))) {
            String duplicateRequirementNumber = param.getMSpaceAppData().get("duplicateRequirementNumber").toString();
            MSpaceAppData updateData = new MSpaceAppData();
            updateData.put("isItADuplicateRequirement", "Y");
            updateData.put(PermissionCheckEnum.CHECK_PERMISSION.getCode(),false);
            iBaseApmSpaceAppDataDrivenService.updatePartialContent(param.getApmSpaceApp().getBid(), duplicateRequirementNumber, updateData);
        }*/
        return super.postHandle(param, result);
    }

    private List<ApmFlowInstanceRoleUser> getUsers(List<String> users, String rrBid, String roleBid, String handleFlag, String spaceAppBid,String spaceBid){
        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = new ArrayList<>();
        for (String user : users) {
            ApmFlowInstanceRoleUser apmFlowInstanceRoleUser = new ApmFlowInstanceRoleUser();
            apmFlowInstanceRoleUser.setRoleBid(roleBid);
            apmFlowInstanceRoleUser.setInstanceBid(rrBid);
            apmFlowInstanceRoleUser.setUserNo(user);
            apmFlowInstanceRoleUser.setSpaceBid(spaceBid);
            apmFlowInstanceRoleUser.setSpaceAppBid(spaceAppBid);
            apmFlowInstanceRoleUser.setHandleFlag(handleFlag);
            apmFlowInstanceRoleUsers.add(apmFlowInstanceRoleUser);
        }
        return apmFlowInstanceRoleUsers;
    }

}
