package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.alm.demandmanagement.entity.ao.SelectAo;
import com.transcend.plm.alm.demandmanagement.entity.vo.SelectVo;
import com.transcend.plm.alm.demandmanagement.enums.DemandManagementEnum;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.alm.demandmanagement.service.DemandManagementService;
import com.transcend.plm.alm.tools.TranscendTools;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.apm.enums.TypeEnum;
import com.transcend.plm.datadriven.apm.event.entity.RelationAddEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.relation.AbstractRelationAddEventHandler;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceRoleUser;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowInstanceRoleUserService;
import com.transcend.plm.datadriven.apm.flow.service.impl.RuntimeService;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.AddExpandAo;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleVO;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmSphere;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmSphereService;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmLifeCycleStateVO;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmStateVO;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppConfigManageService;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author unknown
 * SR需求新增事件处理器
 */
@Slf4j
@Component
public class SrDemandAddEventHandler extends AbstractRelationAddEventHandler {
    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Autowired
    private IApmSpaceAppConfigManageService apmSpaceAppConfigManageService;

    @Resource
    private DemandManagementService demandManagementService;

    @Value("${transcend.plm.apm.special.pdmRoleCode:pdm}")
    private String pdmRoleCode;
    @Resource
    private RuntimeService runtimeService;

    @Resource
    private ApmSphereService apmSphereService;

    @Resource
    private ApmRoleService roleService;

    public static final String DASH = "-";

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

    @Resource
    private ApmFlowInstanceRoleUserService apmFlowInstanceRoleUserService;

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public RelationAddEventHandlerParam preHandle(RelationAddEventHandlerParam param) {
        AddExpandAo addExpandAo = param.getAddExpandAo();
        String sourceBid = addExpandAo.getSourceBid();
        MObject mObject = objectModelCrudI.getByBid("A5G", sourceBid,false);
        if(mObject != null){
            //查询阶段生命周期
            ApmLifeCycleStateVO apmLifeCycleStateVO = apmSpaceAppConfigManageService.getLifeCycleState(addExpandAo.getSpaceAppBid());
            String stageState = null;
            if(apmLifeCycleStateVO != null){
                Map<String, ApmStateVO> phaseStateMap = apmLifeCycleStateVO.getPhaseStateMap();
                if (CollectionUtils.isNotEmpty(phaseStateMap)) {
                    ApmStateVO apmStateVO = phaseStateMap.get(mObject.getLifeCycleCode());
                    if(apmStateVO != null){
                        stageState = apmStateVO.getLifeCycleCode();
                    }
                }
            }
            List<MSpaceAppData> targetMObjects = addExpandAo.getTargetMObjects();
            for(MSpaceAppData targetMObject : targetMObjects){
                targetMObject.setLifeCycleCode(mObject.getLifeCycleCode());
                if(StringUtils.isNotEmpty(stageState)){
                    targetMObject.put("stage",stageState);
                }
            }
            addExpandAo.setTargetMObjects(targetMObjects);
            param.setAddExpandAo(addExpandAo);
        }
        return super.preHandle(param);
    }

    @Override
    public Object postHandle(RelationAddEventHandlerParam param, Object result) {
        AddExpandAo addExpandAo = param.getAddExpandAo();
        String sourceBid = addExpandAo.getSourceBid();
        MObject mObject = objectModelCrudI.getByBid("A5G", sourceBid,false);
        if(mObject != null){
            List<MSpaceAppData> targetMObjects = addExpandAo.getTargetMObjects();
            List<List<String>> selectedList = new ArrayList<>();
            for (MSpaceAppData targetMObject : targetMObjects) {
                List<String> selected = new ArrayList<>();
                if (ObjectUtils.isNotEmpty(targetMObject.get(DemandManagementEnum.PRODUCT_AREA.getCode()))){
                    selected.add(targetMObject.get(DemandManagementEnum.PRODUCT_AREA.getCode()).toString());
                    if (ObjectUtils.isNotEmpty(targetMObject.get("module"))){
                        selected.add(targetMObject.get("module").toString());
                    }
                    selectedList.add(selected);
                }
                if(targetMObject.get("module") != null){
                    String modudelBid = targetMObject.get("module")+"";
                    MObject mObjectModule = objectModelCrudI.getByBid(TranscendModel.MODULE.getCode(), modudelBid,false);
                    if(mObjectModule != null && StringUtils.isNotEmpty(targetMObject.getBid())){
                        Object obj = mObjectModule.get("personResponsible");
                        List<String> moudlePersions = TranscendTools.analysisPersions(obj);
                        if(CollectionUtils.isNotEmpty(moudlePersions)){
                            runtimeService.saveOrupdateFlowRoleUsers(targetMObject.getBid(),moudlePersions,addExpandAo.getSpaceBid(),addExpandAo.getSpaceAppBid(),pdmRoleCode);
                        }
                    }
                }
                //解析领域
                if(targetMObject.get("productArea") != null){
                    String domianBid = targetMObject.get("productArea")+"";
                    MObject domain = objectModelCrudI.getByBid(TranscendModel.DOMAIN.getCode(),domianBid);
                    //查角色BID
                    ApmSphere spaceSphere = apmSphereService.getByBizBidAndType(addExpandAo.getSpaceAppBid(), TypeEnum.OBJECT.getCode());
                    if (spaceSphere == null) {
                        continue;
                    }
                    List<ApmRoleVO> apmRoleVOS = roleService.listByRoleBidsByCodes(rrDemandFlowRoleCodes, spaceSphere.getBid());
                    if (org.apache.commons.collections4.CollectionUtils.isEmpty(apmRoleVOS)) {
                        continue;
                    }
                    Map<String, String> codeBidMap = apmRoleVOS.stream().collect(Collectors.toMap(ApmRoleVO::getCode, ApmRoleVO::getBid));
                    List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = new ArrayList<>();
                    if(domain != null){
                        for(int i = 0 ; i < rrDemandFlowRoleCodes.size(); i++){
                            Object obj = domain.get(rrDemandDomainAttrCodes.get(i));
                            List<String> objList = TranscendTools.analysisPersions(obj);
                            List<ApmFlowInstanceRoleUser> users = TranscendTools.getUsers(objList,targetMObject.getBid(),codeBidMap.get(rrDemandFlowRoleCodes.get(i)),null,addExpandAo.getSpaceAppBid(),addExpandAo.getSpaceBid());
                            apmFlowInstanceRoleUsers.addAll(users);
                        }
                        if(CollectionUtils.isNotEmpty(apmFlowInstanceRoleUsers)){
                            apmFlowInstanceRoleUserService.saveBatch(apmFlowInstanceRoleUsers);
                        }
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(selectedList)){
                List<SelectVo> nameList = new ArrayList<>();
                List<SelectVo> domainList = demandManagementService.queryDomainSelection(param.getSpaceBid(), 2, null, null);
                if (CollectionUtils.isNotEmpty(domainList)){
                    Map<String, SelectVo> domainBidMap = domainList.stream().collect(Collectors.toMap(SelectVo::getBid, Function.identity(), (k1, k2) -> k1));
                    for (List<String> list : selectedList) {
                        SelectVo selectVo = domainBidMap.get(list.get(0));
                        if (selectVo != null){
                            nameList.add(selectVo);
                        }
                    }
                }
                SelectAo selectAo = new SelectAo();
                selectAo.setSelectedList(selectedList);
                selectAo.setNameList(nameList);
                demandManagementService.selectDomain(param.getSpaceBid(), param.getApmSpaceApp().getBid(),sourceBid, 2, selectAo,null,null);
            }
        }
        return super.postHandle(param,result);
    }

    @Override
    public boolean isMatch(RelationAddEventHandlerParam param) {
        //IR-SR关系
        String relationModelCode = "A5O";
        AddExpandAo addExpandAo = param.getAddExpandAo();
        if(addExpandAo == null){
            return false;
        }
        return relationModelCode.equals(addExpandAo.getRelationModelCode());
    }
}
