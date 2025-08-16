package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.alm.demandmanagement.config.DemandManagementProperties;
import com.transcend.plm.alm.demandmanagement.entity.ao.SelectAo;
import com.transcend.plm.alm.demandmanagement.entity.vo.SelectVo;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.alm.demandmanagement.service.DemandManagementService;
import com.transcend.plm.alm.tools.TranscendTools;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.RelationEnum;
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
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transsion.framework.common.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Describe Ar修改事件
 * @Author haijun.ren
 * @Date 2024/9/27
 */
@Slf4j
@Component
public class ArDemandUpdateEventHandler extends AbstractUpdateEventHandler {

    /**
     * TARGET_MODELCODE
     */
    @Resource
    private RuntimeService runtimeService;

    @Value("${transcend.plm.apm.special.pdmRoleCode:pdm}")
    private String pdmRoleCode;

    /***生产 A61*/
    @Value("${transcend.plm.apm.object.arModelCode:A5X}")
    private String arModelCode;
    /***生产 1268235106967724032 */
    @Value("${transcend.plm.apm.app.arAppBid:1264957596881903616}")
    private String arSpaceAppBid;

    @Resource
    private ApmFlowInstanceRoleUserService apmFlowInstanceRoleUserService;

    @Resource
    private ApmSphereService apmSphereService;

    @Resource
    private ApmRoleService roleService;

    /**
     * rr需求需要解析的角色
     */
    @Value("#{'${transcend.plm.apm.rrDemand.flowRoleCodes:DomainDevelopmentRepresentative,DomainDirector,domainse,UserExperienceDesignRepresentative,Softwareprojectrepresentative}'.split(',')}")
    private List<String> rrDemandFlowRoleCodes;

    /**
     * rr需求解析角色对应领域自动
     */
    @Value("#{'${transcend.plm.apm.rrDemand.domainAttrCode:domainDevelopmenRepresentative,domainDirector,domainSe,userExperienceDesignRepresentative,softwareProjectRepresentative}'.split(',')}")
    private List<String> rrDemandDomainAttrCodes;

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;
    @Override
    public Boolean postHandle(UpdateEventHandlerParam param, Boolean result) {
        MSpaceAppData mSpaceAppData = param.getMSpaceAppData();
        if(mSpaceAppData.get("module") != null){
            String modudelBid = mSpaceAppData.get("module")+"";
            MObject mObjectModule = objectModelCrudI.getByBid(TranscendModel.MODULE.getCode(), modudelBid,false);
            if(mObjectModule != null){
                Object obj = mObjectModule.get("personResponsible");
                List<String> moudlePersions = TranscendTools.analysisPersions(obj);
                if(CollectionUtils.isNotEmpty(moudlePersions)){
                    runtimeService.saveOrupdateFlowRoleUsers(mSpaceAppData.getBid(),moudlePersions,param.getSpaceBid(),param.getApmSpaceApp().getBid(),pdmRoleCode);
                }
            }
        }
        //解析领域
        if(mSpaceAppData.get("productArea") != null){
            String domianBid = mSpaceAppData.get("productArea")+"";
            MObject domain = objectModelCrudI.getByBid(TranscendModel.DOMAIN.getCode(),domianBid);
            //查角色BID
            ApmSphere spaceSphere = apmSphereService.getByBizBidAndType(param.getApmSpaceApp().getBid(), TypeEnum.OBJECT.getCode());
            if (spaceSphere != null) {
                List<ApmRoleVO> apmRoleVOS = roleService.listByRoleBidsByCodes(rrDemandFlowRoleCodes, spaceSphere.getBid());
                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(apmRoleVOS)) {
                    Map<String, String> codeBidMap = apmRoleVOS.stream().collect(Collectors.toMap(ApmRoleVO::getCode, ApmRoleVO::getBid));
                    List<String> roleBids = apmRoleVOS.stream().map(ApmRoleVO::getBid).collect(Collectors.toList());
                    List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = new ArrayList<>();
                    if(domain != null){
                        for(int i = 0 ; i < rrDemandFlowRoleCodes.size(); i++){
                            Object obj = domain.get(rrDemandDomainAttrCodes.get(i));
                            List<String> objList = TranscendTools.analysisPersions(obj);
                            List<ApmFlowInstanceRoleUser> users = TranscendTools.getUsers(objList,param.getBid(),codeBidMap.get(rrDemandFlowRoleCodes.get(i)),null,param.getApmSpaceApp().getBid(),param.getSpaceBid());
                            apmFlowInstanceRoleUsers.addAll(users);
                        }
                        if(CollectionUtils.isNotEmpty(apmFlowInstanceRoleUsers)){
                            //根据instanceBid和roleBids删除
                            apmFlowInstanceRoleUserService.deleteByInstanceBidAndRoleBids(param.getBid(), roleBids);
                            apmFlowInstanceRoleUserService.saveBatch(apmFlowInstanceRoleUsers);
                        }
                    }
                }
            }
        }
        return super.postHandle(param, result);
    }

    @Override
    public boolean isMatch(UpdateEventHandlerParam param) {
        CfgObjectVo cfgObjectVo = param.getCfgObjectVo();
        if(cfgObjectVo != null){
            return arModelCode.equals(cfgObjectVo.getModelCode());
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
