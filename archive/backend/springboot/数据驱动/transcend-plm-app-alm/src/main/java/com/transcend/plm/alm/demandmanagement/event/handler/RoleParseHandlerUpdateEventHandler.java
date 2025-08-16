package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.alm.demandmanagement.config.DemandManagementProperties;
import com.transcend.plm.alm.demandmanagement.config.RoleParseProperties;
import com.transcend.plm.alm.demandmanagement.service.DemandManagementService;
import com.transcend.plm.alm.tools.TranscendTools;
import com.transcend.plm.datadriven.apm.enums.TypeEnum;
import com.transcend.plm.datadriven.apm.event.entity.UpdateEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractUpdateEventHandler;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceRoleUser;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowInstanceRoleUserService;
import com.transcend.plm.datadriven.apm.flow.service.impl.RuntimeService;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleVO;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmSphere;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmSphereService;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Describe 角色解析
 * @Author haijun.ren
 * @Date 2024/8/27
 */
@Slf4j
@Component
public class RoleParseHandlerUpdateEventHandler extends AbstractUpdateEventHandler {

    @Resource
    private RoleParseProperties roleParseProperties;

    @Resource
    private ApmFlowInstanceRoleUserService apmFlowInstanceRoleUserService;

    @Resource
    private ApmSphereService apmSphereService;

    @Resource
    private ApmRoleService roleService;


    @Override
    public UpdateEventHandlerParam preHandle(UpdateEventHandlerParam param) {
        MSpaceAppData mSpaceAppData = param.getMSpaceAppData();
        Map<String, Map<String, String>> modelCodeRoleMap = roleParseProperties.getModelCodeRoleMap();
        Map<String, String> roleMap = modelCodeRoleMap.get(param.getApmSpaceApp().getModelCode());
        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(roleMap)){
            ApmSphere spaceSphere = apmSphereService.getByBizBidAndType(param.getApmSpaceApp().getBid(), TypeEnum.OBJECT.getCode());
            List<ApmRoleVO> apmRoleVOS = roleService.listByRoleBidsByCodes(new ArrayList<>(roleMap.values()), spaceSphere.getBid());
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(apmRoleVOS)) {
                Map<String, String> codeBidMap = apmRoleVOS.stream().collect(Collectors.toMap(ApmRoleVO::getCode, ApmRoleVO::getBid));
                List<String> roleBids = new ArrayList<>();
                roleMap.forEach((attr,roleCode)->{
                    if (ObjectUtils.isNotEmpty(mSpaceAppData.get(attr)) && codeBidMap.containsKey(roleCode)){
                        List<String> userNo = TranscendTools.analysisPersions(mSpaceAppData.get(attr));
                        List<ApmFlowInstanceRoleUser> users = TranscendTools.getUsers(userNo,param.getBid(),codeBidMap.get(roleCode),null,param.getApmSpaceApp().getBid(),param.getSpaceBid());
                        roleBids.add(codeBidMap.get(roleCode));
                        apmFlowInstanceRoleUsers.addAll(users);
                    }
                });
                if(CollectionUtils.isNotEmpty(apmFlowInstanceRoleUsers)){
                    //根据instanceBid和roleBids删除
                    apmFlowInstanceRoleUserService.deleteByInstanceBidAndRoleBids(param.getBid(), roleBids);
                    apmFlowInstanceRoleUserService.saveBatch(apmFlowInstanceRoleUsers);
                }
            }
        }
        return super.preHandle(param);
    }


    @Override
    public Boolean postHandle(UpdateEventHandlerParam param, Boolean result) {
        return super.postHandle(param, result);
    }

    @Override
    public boolean isMatch(UpdateEventHandlerParam param) {
        return roleParseProperties.getModelCodeRoleMap().containsKey(param.getApmSpaceApp().getModelCode());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
