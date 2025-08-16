package com.transcend.plm.alm.demandmanagement.event.handler;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.util.StringUtil;
import com.transcend.plm.alm.demandmanagement.config.DemandManagementProperties;
import com.transcend.plm.alm.demandmanagement.constants.DemandManagementConstant;
import com.transcend.plm.alm.demandmanagement.entity.ao.SelectAo;
import com.transcend.plm.alm.demandmanagement.entity.vo.SelectVo;
import com.transcend.plm.alm.demandmanagement.enums.DemandManagementEnum;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.alm.demandmanagement.service.DemandManagementService;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.RelationEnum;
import com.transcend.plm.datadriven.apm.enums.TypeEnum;
import com.transcend.plm.datadriven.apm.event.entity.AddEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractAddEventHandler;
import com.transcend.plm.datadriven.apm.flow.service.IRuntimeService;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleVO;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRoleIdentity;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmSphere;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleIdentityService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmSphereService;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.pool.SimpleThreadPool;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @Describe 需求管理生成RR 领域后置事件
 * @Author yuanhu.huang
 * @Date 2024/7/10
 */
@Slf4j
@Component
public class RrDemandDomainHandler extends AbstractAddEventHandler {
    private static final String RR_OBJ_BID = "1253640663282315264";
    @Resource
    private DemandManagementService demandManagementService;

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

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

    @Value("${transcend.plm.apm.rr.roleCode.rRdemandoperations:RRdemandoperations}")
    private String rRdemandoperationsRoleCode;



    @Resource
    private IRuntimeService runtimeService;

    @Resource
    private DemandManagementProperties demandManagementProperties;

    private String analysisRRdemandoperations = "";

    @Resource
    private ApmSphereService apmSphereService;

    @Resource
    private ApmRoleService roleService;

    @Resource
    ApmRoleIdentityService apmRoleIdentityService;


    /**
     * RR处理前置事件
     * @param param 入参
     * @return
     */
    @Override
    public AddEventHandlerParam preHandle(AddEventHandlerParam param) {
        MSpaceAppData mSpaceAppData = param.getMSpaceAppData();
        /*领域为空：状态直接赋值为   REQUIREMENTANALYSIS	待过滤分发
        领域不为空：状态直接赋值为  DOMAINVALUEASSESSMENT 待价值评估*/
        //直接改状态
        if(mSpaceAppData.get("field") == null){
            mSpaceAppData.setLifeCycleCode(DemandManagementConstant.STATE_REQUIREMENTANALYSIS);
        }else{
            mSpaceAppData.setLifeCycleCode(DemandManagementConstant.STATE_DOMAINVALUEASSESSMENT);
        }
        //处理当前处理人
        List<String> currentUsers = new ArrayList<>();
        String productArea = mSpaceAppData.get("productArea")+"";
        if(mSpaceAppData.get("productArea") == null || StringUtil.isEmpty(productArea)){
           //直接解析当前人员为角色管理中【RR需求运营】RRdemandoperations 的人员
            List<ApmRoleIdentity> apmRoleIdentities = apmRoleIdentityService.listEmpByBizBidAndCodes(TypeEnum.OBJECT.getCode(),param.getApmSpaceApp().getBid(),Arrays.asList(rRdemandoperationsRoleCode));
            if(CollectionUtils.isNotEmpty(apmRoleIdentities)){
                currentUsers = apmRoleIdentities.stream().map(ApmRoleIdentity::getIdentity).collect(Collectors.toList());
            }
        }else{
            //领域不为空：解析选择的领域的  领域产品规划代表 productManager
            MObject mObject = objectModelCrudI.getByBid(TranscendModel.DOMAIN.getCode(),mSpaceAppData.get("productArea")+"");
            if(mObject != null){
                Object objPm = mObject.get("productManager");
                currentUsers = getObjectList(objPm);
            }
        }
        if(CollectionUtils.isNotEmpty(currentUsers)){
            mSpaceAppData.put(demandManagementProperties.getCurrentHandler(), currentUsers);
        }
        return super.preHandle(param);
    }

    /**
     * 异步处理后置事件
     * @param param param
     * @param result result
     */
    public void handlePostHandle(AddEventHandlerParam param, MSpaceAppData result){
        String spaceBid = param.getSpaceBid();
        String spaceAppBid = param.getApmSpaceApp().getBid();
        String newBid = result.getBid();
        String domainAreaBid = result.get(CommonConst.PRODUCT_AREA_STR)+"";
        //解析创建者
        List<String> creatorList = new ArrayList<>();
        creatorList.add(param.getMSpaceAppData().getCreatedBy());
        runtimeService.updateFlowInnerRoleUsers(newBid,creatorList,spaceBid,spaceAppBid,CommonConst.INNER_CREATER_STR);
        //解析关注人
        if (ObjectUtils.isNotEmpty(param.getMSpaceAppData().get(CommonConst.FOLLOWERS))){
            List<String> followerList = getObjectList(param.getMSpaceAppData().get(CommonConst.FOLLOWERS));
            runtimeService.saveFlowRoleUsers(newBid,followerList,spaceBid,spaceAppBid,CommonConst.FOLLOWERS);
        }
        if(!Objects.isNull(result.get(CommonConst.PRODUCT_AREA_STR)) && StringUtils.isNotEmpty(domainAreaBid)){
            List<SelectVo> selectVoList = demandManagementService.queryDomainSelection(param.getSpaceBid(), 2, result.getBid(), null);
            if(CollectionUtils.isNotEmpty(selectVoList)){
                SelectAo selectAo = new SelectAo();
                List<List<String>> selectedList = new ArrayList<>();
                List<String> domainList = new ArrayList<>();
                domainList.add(domainAreaBid);
                selectedList.add(domainList);
                selectAo.setSelectedList(selectedList);
                selectAo.setNameList(selectVoList);
                demandManagementService.selectDomain(param.getSpaceBid(), param.getApmSpaceApp().getBid(),result.getBid(), 2, selectAo,analysisRRdemandoperations,null);
            }
        }
        //领域组件的数据
        // 查询领域
        List<String> domainBidList = new ArrayList<>();
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty("rr_bid");
        queryWrapper.setCondition("=");
        queryWrapper.setValue(newBid);
        List<QueryWrapper> queryWrapperList = new ArrayList<>();
        queryWrapperList.add(queryWrapper);
        List<MObject> domainCompList = objectModelCrudI.list(TranscendModel.DOMAIN_COMPONENT.getCode(),queryWrapperList);
        if(CollectionUtils.isNotEmpty(domainCompList)){
            for(MObject domainComp : domainCompList){
                domainBidList.add(domainComp.get(CommonConst.DOMAIN_BID_STR)+"");
            }
        }
        if(CollectionUtils.isNotEmpty(domainBidList)){
            List<MObject> domainList = objectModelCrudI.listByBids(domainBidList,TranscendModel.DOMAIN.getCode());
            if(CollectionUtils.isNotEmpty(domainList)){
                List<String> uers = new ArrayList<>();
                Map<String,List<String>> domainRoleMap = new HashMap<>(16);
                for(MObject domain : domainList){
                    Object obj3 = domain.get(CommonConst.PRODUCT_MANAGER_STR);
                    List<String> list3 = getObjectList(obj3);
                    uers.addAll(list3);
                     //动态解析领域角色数据
                    if(CollectionUtils.isNotEmpty(rrDemandFlowRoleCodes) && CollectionUtils.isNotEmpty(rrDemandDomainAttrCodes) && rrDemandFlowRoleCodes.size() == rrDemandDomainAttrCodes.size()){
                       for(int i = 0 ; i < rrDemandFlowRoleCodes.size(); i++){
                           Object obj = domain.get(rrDemandDomainAttrCodes.get(i));
                           List<String> objList = getObjectList(obj);
                           List<String> domainRoleList = domainRoleMap.get(rrDemandFlowRoleCodes.get(i));
                           if(domainRoleList == null){
                               domainRoleList = new ArrayList<>();
                           }
                           domainRoleList.addAll(objList);
                           domainRoleMap.put(rrDemandFlowRoleCodes.get(i),domainRoleList);
                       }
                    }
                }
                if(CollectionUtils.isNotEmpty(domainRoleMap)){
                    for(Map.Entry<String,List<String>> entry : domainRoleMap.entrySet()){
                        runtimeService.updateFlowRoleUsers(newBid,entry.getValue(),spaceBid,spaceAppBid,entry.getKey());
                    }
                }
                if(CollectionUtils.isNotEmpty(uers)){
                    runtimeService.updateFlowRoleUsers(newBid,uers,spaceBid,spaceAppBid,"domainLeader");
                }
            }
        }
    }

    @Override
    public MSpaceAppData postHandle(AddEventHandlerParam param, MSpaceAppData result) {
        CompletableFuture.runAsync(() -> handlePostHandle(param,result), SimpleThreadPool.getInstance());
        return super.postHandle(param, result);
    }
    private List<String> getObjectList(Object object){
        List<String> list = new ArrayList<>();
        if(object == null){
            return list;
        }
        if(object instanceof List){
            list = JSON.parseArray(object.toString(), String.class);
        }else if(object instanceof String){
            String objectStr = (String) object;
            //将objectStr中"替换成空格
            objectStr = objectStr.replaceAll("\"", "");
            if(StringUtils.isNotEmpty(objectStr)){
                list.add(objectStr);
            }
        }else{
            if(object.toString().startsWith(CommonConstant.OPEN_BRACKET)){
                list = JSON.parseArray(object.toString(), String.class);
            }else{
                list.add(object.toString());
            }
        }
        return list;
    }

    private List<MObject> queryRelData(String spaceBid, String rrBid) {
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.SPACE_BID.getColumn(), spaceBid).and().eq(DemandManagementEnum.RR_BID.getColumn(), rrBid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        return objectModelCrudI.list(TranscendModel.DOMAIN_COMPONENT.getCode(), queryWrappers);
    }
    @Override
    public boolean isMatch(AddEventHandlerParam param) {
        String objBid = param.getObjBid();
        return RR_OBJ_BID.equals(objBid);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
