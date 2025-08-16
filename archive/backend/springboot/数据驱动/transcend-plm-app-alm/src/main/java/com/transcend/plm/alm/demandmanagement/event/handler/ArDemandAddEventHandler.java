package com.transcend.plm.alm.demandmanagement.event.handler;

import cn.hutool.core.collection.CollUtil;
import com.transcend.plm.alm.demandmanagement.entity.ao.SelectAo;
import com.transcend.plm.alm.demandmanagement.entity.vo.SelectVo;
import com.transcend.plm.alm.demandmanagement.enums.DemandManagementEnum;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.alm.tools.TranscendTools;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.RelationEnum;
import com.transcend.plm.datadriven.api.model.RelationMObject;
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
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppConfigManageService;
import com.transcend.plm.datadriven.common.constant.NumberConstant;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import springfox.documentation.service.ApiListing;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.transcend.plm.alm.demandmanagement.constants.DemandManagementConstant.DASH;
import static com.transcend.plm.alm.demandmanagement.constants.DemandManagementConstant.SR_PREFIX;

/**
 * SR需求新增事件处理器
 *
 * @author yuanhu.huang
 * @date 2024/08/02
 */
@Slf4j
@Component
public class ArDemandAddEventHandler extends AbstractRelationAddEventHandler {
    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Autowired
    private IApmSpaceAppConfigManageService apmSpaceAppConfigManageService;

    @Autowired
    private ApmSpaceAppService apmSpaceAppService;

    public static final String REQUIREMENT_CODING = "requirementcoding";

    public static final String AR_PREFIX = "AR";

    @Value("${transcend.plm.apm.object.arModelCode:A5X}")
    private String arModelCode;

    @Value("${transcend.plm.apm.moudle.arSrRelModelCode:A5Y}")
    private String arSrRelModelCode;

    @Autowired
    private CodeService codeService;

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

    @Autowired
    private ObjectModelStandardI objectModelStandardI;

    @Override
    public int getOrder() {
        return 0;
    }


    /**
     * @param currentSerialNumber 当前流水号
     * @return {@link String }
     */
    public String handleSerialNumber(int currentSerialNumber) {
        if (currentSerialNumber < NumberConstant.TEN) {
            return "00" + currentSerialNumber;
        } else if (currentSerialNumber < NumberConstant.HUNDRED) {
            return "0" + currentSerialNumber;
        } else {
            return String.valueOf(currentSerialNumber);
        }
    }

    @Override
    public RelationAddEventHandlerParam preHandle(RelationAddEventHandlerParam param) {
        AddExpandAo addExpandAo = param.getAddExpandAo();
        String sourceBid = addExpandAo.getSourceBid();
        MObject mObject = objectModelCrudI.getByBid("A5F", sourceBid,false);
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
            //取ir已经关联的sr中后三位最大的编码
            RelationMObject relationMObject = RelationMObject.builder()
                    .relationModelCode(param.getAddExpandAo().getRelationModelCode())
                    .sourceBid(param.getAddExpandAo().getSourceBid()).build();
            List<MObject> arAndIrRel = objectModelStandardI.listOnlyRelationMObjects(relationMObject);
            String suffixCode = "000";
            if (CollUtil.isNotEmpty(arAndIrRel)) {
                List<String> targetBids = new ArrayList<>();
                for (MObject relation : arAndIrRel) {
                    targetBids.add(relation.get(RelationEnum.TARGET_BID.getCode()) + "");
                }
                List<MObject> targetList = objectModelStandardI.listByBids(targetBids, arModelCode);
                if (CollUtil.isNotEmpty(targetList)) {
                    suffixCode = targetList.stream().filter(v -> ObjectUtils.isNotEmpty(v.get(REQUIREMENT_CODING)))
                            .map(v -> v.get(REQUIREMENT_CODING).toString().substring(v.get(REQUIREMENT_CODING).toString().length() - 3))
                            .max(Comparator.comparing(Integer::valueOf)).orElse("000");
                }
            }
            for(MSpaceAppData targetMObject : targetMObjects){
                suffixCode = String.format("%03d", Integer.parseInt(suffixCode) + 1);
                targetMObject.setLifeCycleCode(mObject.getLifeCycleCode());
                if(StringUtils.isNotEmpty(stageState)){
                    targetMObject.put("stage",stageState);
                }
                // 处理AR的编码中间段，取SR的编码后缀
                String middleCode = buildArMiddleCode(addExpandAo.getSourceSpaceAppBid(), sourceBid);
                String resultCodeBuilder = AR_PREFIX + DASH +
                        middleCode +
                        DASH +
                        suffixCode;
                targetMObject.put(REQUIREMENT_CODING, resultCodeBuilder);
            }
            addExpandAo.setTargetMObjects(targetMObjects);
            param.setAddExpandAo(addExpandAo);
        }
        return super.preHandle(param);
    }

    /**
     * 构建ar关联需求编码,使用sr得编码后缀来作为ar的中间编码
     * @param sourceSpaceAppBid
     * @param sourceBid
     * @return
     */
    private String buildArMiddleCode(String sourceSpaceAppBid, String sourceBid) {
        String middleCode  = "";
        if (StringUtils.isBlank(sourceBid) || StringUtils.isBlank(sourceSpaceAppBid)) {
            return middleCode;
        }
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(sourceSpaceAppBid);
        MObject srObject = objectModelCrudI.getByBid(apmSpaceApp.getModelCode(), sourceBid,false);
        String srCode = MapUtils.getString(srObject, REQUIREMENT_CODING);
        if(StringUtils.isNotEmpty(srCode) && srCode.length() > 3){
            middleCode = srCode.substring(3);
        }
        return middleCode;
    }

    @Override
    public Object postHandle(RelationAddEventHandlerParam param, Object result) {
        AddExpandAo addExpandAo = param.getAddExpandAo();
            List<MSpaceAppData> targetMObjects = addExpandAo.getTargetMObjects();
            for (MSpaceAppData targetMObject : targetMObjects) {
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
        return super.postHandle(param,result);
    }

    @Override
    public boolean isMatch(RelationAddEventHandlerParam param) {
        //IR-SR关系
        String relationModelCode = arSrRelModelCode;
        AddExpandAo addExpandAo = param.getAddExpandAo();
        if(addExpandAo == null){
            return false;
        }
        return relationModelCode.equals(addExpandAo.getRelationModelCode());
    }
}
