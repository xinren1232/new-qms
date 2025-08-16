package com.transcend.plm.alm.demandmanagement.event.handler;

import com.google.common.collect.Lists;
import com.transcend.plm.alm.tools.TranscendTools;
import com.transcend.plm.datadriven.api.model.BatchUpdateBO;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.RelationMObject;
import com.transcend.plm.datadriven.apm.enums.TypeEnum;
import com.transcend.plm.datadriven.apm.event.entity.UpdateEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractUpdateEventHandler;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceRoleUser;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowInstanceRoleUserService;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleVO;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmSphere;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmSphereService;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmLifeCycleStateVO;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmStateVO;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppConfigManageService;
import com.transcend.plm.datadriven.common.constant.DataBaseConstant;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author unknown
 * IR状态变更需要驱动SR状态变更
 */
@Slf4j
@Component
public class IRDemandUpdateEventHandler extends AbstractUpdateEventHandler {
    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Autowired
    private IApmSpaceAppConfigManageService apmSpaceAppConfigManageService;

    /**AR modelCode*/
    @Value("${transcend.plm.apm.moudle.arModelCode:A5X}")
    private String arModelCode;

    @Value("${transcend.plm.apm.moudle.arSrRelModelCode:A5Y}")
    private String arSrRelModelCode;

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
    public Boolean postHandle(UpdateEventHandlerParam param, Boolean result) {
        String lifeCycleCode = param.getMSpaceAppData().getLifeCycleCode();
        String nullStr = "null";
        if (param.getMSpaceAppData().get(TranscendModelBaseFields.LIFE_CYCLE_CODE) != null && StringUtils.isNotEmpty(lifeCycleCode) && !nullStr.equals(lifeCycleCode)) {
            RelationMObject relationMObject = RelationMObject.builder().sourceBid(param.getMSpaceAppData().getBid()).sourceModelCode("A5G").targetModelCode("A5F").relationModelCode("A5O").build();
            List<MObject> mObjects = objectModelCrudI.listOnlyRelationMObjects(relationMObject);
            List<BatchUpdateBO<MObject>> batchUpdateBoList = Lists.newArrayList();
            if (CollectionUtils.isNotEmpty(mObjects)) {
                ApmLifeCycleStateVO apmLifeCycleStateVO = apmSpaceAppConfigManageService.getLifeCycleState("1253654532919947264");
                String stageState = null;
                if(apmLifeCycleStateVO != null){
                    Map<String, ApmStateVO> phaseStateMap = apmLifeCycleStateVO.getPhaseStateMap();
                    if (CollectionUtils.isNotEmpty(phaseStateMap)) {
                        ApmStateVO apmStateVO = phaseStateMap.get(lifeCycleCode);
                        if(apmStateVO != null){
                            stageState = apmStateVO.getLifeCycleCode();
                        }
                    }
                }
                List<String> srBids = new ArrayList<>();
                for (MObject mObject : mObjects) {
                    QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
                    queryWrapper.setProperty(DataBaseConstant.COLUMN_BID);
                    queryWrapper.setCondition("=");
                    queryWrapper.setValue(mObject.get("targetBid"));
                    MObject updateObject = new MObject();
                    updateObject.setLifeCycleCode(lifeCycleCode);
                    if(StringUtils.isNotEmpty(stageState)){
                        updateObject.put("stage",stageState);
                    }
                    srBids.add(mObject.get("targetBid").toString());
                    BatchUpdateBO<MObject> batchUpdateBO = new BatchUpdateBO<>();
                    batchUpdateBO.setBaseData(updateObject);
                    List<QueryWrapper> queryWrapperList = new ArrayList<>();
                    queryWrapperList.add(queryWrapper);
                    batchUpdateBO.setWrappers(queryWrapperList);
                    batchUpdateBoList.add(batchUpdateBO);
                }
                //查询SR 和 AR 关系
                RelationMObject arSrRelationMObject = RelationMObject.builder().sourceBids(srBids).sourceModelCode("A5F").targetModelCode(arModelCode).relationModelCode(arSrRelModelCode).build();
                List<MObject> arSrMObjects = objectModelCrudI.listOnlyRelationMObjects(arSrRelationMObject);
                if(CollectionUtils.isNotEmpty(arSrMObjects)){
                    List<BatchUpdateBO<MObject>> arBatchUpdateBoList = Lists.newArrayList();
                    for (MObject mObject : arSrMObjects) {
                        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
                        queryWrapper.setProperty(DataBaseConstant.COLUMN_BID);
                        queryWrapper.setCondition("=");
                        queryWrapper.setValue(mObject.get("targetBid"));
                        MObject updateObject = new MObject();
                        updateObject.setLifeCycleCode(lifeCycleCode);
                        if(StringUtils.isNotEmpty(stageState)){
                            updateObject.put("stage",stageState);
                        }
                        BatchUpdateBO<MObject> batchUpdateBO = new BatchUpdateBO<>();
                        batchUpdateBO.setBaseData(updateObject);
                        List<QueryWrapper> queryWrapperList = new ArrayList<>();
                        queryWrapperList.add(queryWrapper);
                        batchUpdateBO.setWrappers(queryWrapperList);
                        arBatchUpdateBoList.add(batchUpdateBO);
                    }
                    //更新AR状态
                    if (CollectionUtils.isNotEmpty(arBatchUpdateBoList)) {
                        objectModelCrudI.batchUpdateByQueryWrapper(arModelCode, arBatchUpdateBoList, false);
                    }
                }

            }
            //更新SR状态
            if (CollectionUtils.isNotEmpty(batchUpdateBoList)) {
                objectModelCrudI.batchUpdateByQueryWrapper("A5F", batchUpdateBoList, false);
            }

        }
        if(param.getMSpaceAppData().get("targetOSversion") != null){
            String targetOSversionBid = param.getMSpaceAppData().get("targetOSversion")+"";
            MObject mObject = objectModelCrudI.getByBid("A4D",targetOSversionBid);
            if(mObject != null){
                //查角色BID
                ApmSphere spaceSphere = apmSphereService.getByBizBidAndType(param.getApmSpaceApp().getBid(), TypeEnum.OBJECT.getCode());
                if (spaceSphere != null) {
                    List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = new ArrayList<>();
                    List<ApmRoleVO> apmRoleVOS = roleService.listByRoleBidsByCodes(osVersionRoleCodes, spaceSphere.getBid());
                    List<String> updatedRoleBids = new ArrayList<>();
                    if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(apmRoleVOS)) {
                        Map<String, String> codeBidMap = apmRoleVOS.stream().collect(Collectors.toMap(ApmRoleVO::getCode, ApmRoleVO::getBid));
                        for(int i = 0 ; i < osVersionRoleCodes.size(); i++){
                            Set<String> userSet = new HashSet<>();
                            String[] attrs = osVersionAttrCodes.get(i).split("#");
                            for (String attr : attrs) {
                                Object obj = mObject.get(attr);
                                List<String> user = TranscendTools.analysisPersions(obj);
                                if (CollectionUtils.isNotEmpty(user)) {
                                    userSet.addAll(user);
                                }
                            }
                            List<ApmFlowInstanceRoleUser> tempUsers = getUsers(new ArrayList<>(userSet), param.getBid(), codeBidMap.get(osVersionRoleCodes.get(i)), null, param.getApmSpaceApp().getBid(),param.getSpaceBid());
                            apmFlowInstanceRoleUsers.addAll(tempUsers);
                            updatedRoleBids.add(codeBidMap.get(osVersionRoleCodes.get(i)));
                        }
                    }
                    apmFlowInstanceRoleUserService.deleteByInstanceBidAndRoleBids(param.getBid(), updatedRoleBids);
                    if(CollectionUtils.isNotEmpty(apmFlowInstanceRoleUsers)){
                        apmFlowInstanceRoleUserService.saveBatch(apmFlowInstanceRoleUsers);
                    }
                }
            }
        }
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

    @Override
    public boolean isMatch(UpdateEventHandlerParam param) {
        ApmSpaceApp apmSpaceApp = param.getApmSpaceApp();
        MSpaceAppData mSpaceAppData = param.getMSpaceAppData();
        if(apmSpaceApp == null || CollectionUtils.isEmpty(mSpaceAppData)){
            return false;
        }
        String spaceAppBid = "1253665116014346240";
        return spaceAppBid.equals(apmSpaceApp.getBid());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
