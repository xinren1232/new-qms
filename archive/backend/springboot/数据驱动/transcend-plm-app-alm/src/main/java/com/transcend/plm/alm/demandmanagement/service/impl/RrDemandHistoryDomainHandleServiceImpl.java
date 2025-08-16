package com.transcend.plm.alm.demandmanagement.service.impl;

import com.google.common.collect.Lists;
import com.transcend.plm.alm.demandmanagement.config.DemandManagementProperties;
import com.transcend.plm.alm.demandmanagement.enums.DemandManagementEnum;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.alm.demandmanagement.service.RrDemandHistoryDomainHandleService;
import com.transcend.plm.alm.tools.TranscendTools;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.apm.enums.TypeEnum;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceRoleUser;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowInstanceRoleUserService;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleVO;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmSphere;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmSphereService;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RR需求历史数据解析领域
 */
@Service
public class RrDemandHistoryDomainHandleServiceImpl implements RrDemandHistoryDomainHandleService {
    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private ApmFlowInstanceRoleUserService apmFlowInstanceRoleUserService;


    /*SELECT
	*
    FROM
            apm_role
    WHERE
	`sphere_bid` = '1195029861299785729'
    AND CODE IN ( 'DomainDirector', 'domainLeader', 'domainse', 'DomainDevelopmentRepresentative', 'DomainTestRepresentative','UserExperienceDesignRepresentative','sre','Softwareprojectrepresentative');*/

    @Resource
    private ApmSphereService apmSphereService;

    @Resource
    private ApmRoleService roleService;

    @Value("${transcend.plm.rrDomain.historyHandle.spaceAppBid:1253656300279398400}")
    private String spaceAppBid;

    /**
     * rr需求需要解析的角色 UAT:Domain_SE prod:domainse
     */
    @Value("#{'${transcend.plm.apm.rrDemand.rrDemandFlowDomainRoleCodes:DomainDirector,domainLeader,domainse,DomainDevelopmentRepresentative,DomainTestRepresentative,UserExperienceDesignRepresentative,sre,Softwareprojectrepresentative}'.split(',')}")
    private List<String> rrDemandFlowDomainRoleCodes;

    /**
     * rr需求解析角色对应领域自动
     */
    @Value("#{'${transcend.plm.apm.rrDemand.rrDemandFlowDomainAttrCodes:domainDirector,productManager,domainSe,domainDevelopmenRepresentative,softwareTestingRepresentative,userExperienceDesignRepresentative,requirementManagementRepresentative,softwareProjectRepresentative}'.split(',')}")
    private List<String> rrDemandFlowDomainAttrCodes;

    /**
     * ir需求需要解析的角色
     */
    @Value("#{'${transcend.plm.apm.irDemand.flowRoleCodes:DomainDevelopmentRepresentative,DomainDirector,domainse,UserExperienceDesignRepresentative,Softwareprojectrepresentative,domainLeader,DomainTestRepresentative,sre}'.split(',')}")
    private List<String> irDemandFlowRoleCodes;

    /**
     * ir需求解析角色对应领域自动
     */
    @Value("#{'${transcend.plm.apm.irDemand.domainAttrCode:domainDevelopmenRepresentative,domainDirector,domainSe,userExperienceDesignRepresentative,softwareProjectRepresentative,productManager,softwareTestingRepresentative,requirementManagementRepresentative}'.split(',')}")
    private List<String> irDemandDomainAttrCodes;

    /**
     * ir需求需要解析的角色
     */
    @Value("#{'${transcend.plm.apm.demand.module.flowRoleCodes:pdm}'.split(',')}")
    private List<String> demandModuleFlowRoleCodes;

    /**
     * ir需求解析角色对应领域自动
     */
    @Value("#{'${transcend.plm.apm.demand.module.attrCode:personResponsible}'.split(',')}")
    private List<String> demandModuleAttrCodes;

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
    private DemandManagementProperties demandManagementProperties;

    /***生产 A61*/
    @Value("${transcend.plm.apm.object.arModelCode:A5X}")
    private String arModelCode;
    /***生产 1268235106967724032 */
    @Value("${transcend.plm.apm.app.arAppBid:1264957596881903616}")
    private String arSpaceAppBid;

    public boolean handleIrDomain() {
        if(CollectionUtils.isEmpty(irDemandFlowRoleCodes)||CollectionUtils.isEmpty(irDemandDomainAttrCodes) || irDemandFlowRoleCodes.size() != irDemandDomainAttrCodes.size()){
            return false;
        }
        //查询所有RR数据
        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = new ArrayList<>();
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty("delete_flag");
        queryWrapper.setCondition("=");
        queryWrapper.setValue(0);
        List<QueryWrapper> queryWrapperList = new ArrayList<>();
        queryWrapperList.add(queryWrapper);
        List<MObject> irList = objectModelCrudI.list("A5G",queryWrapperList);
        if(CollectionUtils.isNotEmpty(irList)){
            List<String> rrBidList = new ArrayList<>();
            for (MObject ir : irList) {
                rrBidList.add(ir.getBid());
            }
            //查领域组件
            QueryWrapper qo = new QueryWrapper();
            qo.eq("domain_model_code", TranscendModel.DOMAIN.getCode())
                    .and()
                    .in("rr_bid", rrBidList);
            List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
            List<MObject> queryList = objectModelCrudI.list(TranscendModel.DOMAIN_COMPONENT.getCode(), queryWrappers);
            //查询领域
            if(CollectionUtils.isNotEmpty(queryList)) {
                Set<String> set = new HashSet<>();
                //rrBid和领域Bid关系
                Map<String,List<String>> rrDomainRelMap = new HashMap<>(16);
                for (MObject domain : queryList) {
                    set.add(domain.get("domainBid")+"");
                    List<String> rrBidTempList = rrDomainRelMap.get(domain.get("rrBid")+"");
                    if(rrBidTempList == null){
                        rrBidTempList = new ArrayList<>();
                    }
                    rrBidTempList.add(domain.get("domainBid")+"");
                    rrDomainRelMap.put(domain.get("rrBid")+"",rrBidTempList);
                }
                //查询领域数据
                QueryWrapper domainQo = new QueryWrapper();
                domainQo.in(TranscendModelBaseFields.BID, set);
                List<QueryWrapper> domainWrappers = QueryWrapper.buildSqlQo(domainQo);
                List<MObject> domainList = objectModelCrudI.list(TranscendModel.DOMAIN.getCode(), domainWrappers);
                Map<String,MObject> domainMap = new HashMap<>(16);
                for (MObject domain : domainList) {
                    domainMap.put(domain.get(TranscendModelBaseFields.BID)+"", domain);
                }
                //查角色BID
                ApmSphere spaceSphere = apmSphereService.getByBizBidAndType("1253665116014346240", TypeEnum.OBJECT.getCode());
                if (spaceSphere == null) {
                    return false;
                }
                List<ApmRoleVO> apmRoleVOS = roleService.listByRoleBidsByCodes(irDemandFlowRoleCodes, spaceSphere.getBid());
                if (org.apache.commons.collections4.CollectionUtils.isEmpty(apmRoleVOS)) {
                    return false;
                }
                Map<String, String> codeBidMap = apmRoleVOS.stream().collect(Collectors.toMap(ApmRoleVO::getCode, ApmRoleVO::getBid));

                for(Map.Entry<String,List<String>> entry:rrDomainRelMap.entrySet()){
                    List<String> domainForList = entry.getValue();
                    Map<String,Set<String>> domainRoleMap = new HashMap<>(16);
                    for (String domainFor : domainForList) {
                        MObject domain = domainMap.get(domainFor);
                        if (null != domain) {
                            for(int i = 0 ; i < irDemandFlowRoleCodes.size(); i++){
                                Object obj = domain.get(irDemandDomainAttrCodes.get(i));
                                List<String> objList = TranscendTools.analysisPersions(obj);
                                Set<String> domainRoleList = domainRoleMap.get(irDemandFlowRoleCodes.get(i));
                                if(domainRoleList == null){
                                    domainRoleList = new HashSet<>();
                                }
                                domainRoleList.addAll(objList);
                                domainRoleMap.put(irDemandFlowRoleCodes.get(i),domainRoleList);
                            }

                            //解析角色
                        }
                    }
                    for(Map.Entry<String,Set<String>> domanEntry:domainRoleMap.entrySet()){
                        //组装ApmFlowInstanceRoleUser 数据
                        List<ApmFlowInstanceRoleUser> users = getUsers(domanEntry.getValue(),entry.getKey(),codeBidMap.get(domanEntry.getKey()),"2","1253665116014346240");
                        apmFlowInstanceRoleUsers.addAll(users);
                    }
                }
            }
        }
        if(CollectionUtils.isNotEmpty(apmFlowInstanceRoleUsers)){
            int maxInstanceUserSize = 1100;
            if(apmFlowInstanceRoleUsers.size() > maxInstanceUserSize){
                List<List<ApmFlowInstanceRoleUser>>  parList= Lists.partition(apmFlowInstanceRoleUsers, 1000);
                for(List<ApmFlowInstanceRoleUser> list:parList){
                    apmFlowInstanceRoleUserService.saveBatch(list);
                }
                return true;
            }else{
                return apmFlowInstanceRoleUserService.saveBatch(apmFlowInstanceRoleUsers);
            }
        }
        return false;
    }

    public boolean handleIrModule() {
        if(CollectionUtils.isEmpty(demandModuleFlowRoleCodes)||CollectionUtils.isEmpty(demandModuleAttrCodes) || demandModuleFlowRoleCodes.size() != demandModuleAttrCodes.size()){
            return false;
        }
        //查询所有RR数据
        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = new ArrayList<>();
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty("delete_flag");
        queryWrapper.setCondition("=");
        queryWrapper.setValue(0);
        List<QueryWrapper> queryWrapperList = new ArrayList<>();
        queryWrapperList.add(queryWrapper);
        List<MObject> irList = objectModelCrudI.list("A5G",queryWrapperList);
        if(CollectionUtils.isNotEmpty(irList)){
            List<String> rrBidList = new ArrayList<>();
            for (MObject ir : irList) {
                rrBidList.add(ir.getBid());
            }
            //查领域组件
            QueryWrapper qo = new QueryWrapper();
            qo.eq("domain_model_code", TranscendModel.MODULE.getCode())
                    .and()
                    .in("rr_bid", rrBidList);
            List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
            List<MObject> queryList = objectModelCrudI.list(TranscendModel.DOMAIN_COMPONENT.getCode(), queryWrappers);
            //查询领域
            if(CollectionUtils.isNotEmpty(queryList)) {
                Set<String> set = new HashSet<>();
                //rrBid和领域Bid关系
                Map<String,List<String>> rrDomainRelMap = new HashMap<>(16);
                for (MObject domain : queryList) {
                        set.add(domain.get("domainBid")+"");
                        List<String> rrBidTempList = rrDomainRelMap.get(domain.get("rrBid")+"");
                        if(rrBidTempList == null){
                            rrBidTempList = new ArrayList<>();
                        }
                        rrBidTempList.add(domain.get("domainBid")+"");
                        rrDomainRelMap.put(domain.get("rrBid")+"",rrBidTempList);
                }
                //查询模块数据
                QueryWrapper domainQo = new QueryWrapper();
                domainQo.in(TranscendModelBaseFields.BID, set);
                List<QueryWrapper> domainWrappers = QueryWrapper.buildSqlQo(domainQo);
                List<MObject> domainList = objectModelCrudI.list(TranscendModel.MODULE.getCode(), domainWrappers);
                Map<String,MObject> domainMap = new HashMap<>(16);
                for (MObject domain : domainList) {
                    domainMap.put(domain.get(TranscendModelBaseFields.BID)+"", domain);
                }
                //查角色BID
                ApmSphere spaceSphere = apmSphereService.getByBizBidAndType("1253665116014346240", TypeEnum.OBJECT.getCode());
                if (spaceSphere == null) {
                    return false;
                }
                List<ApmRoleVO> apmRoleVOS = roleService.listByRoleBidsByCodes(demandModuleFlowRoleCodes, spaceSphere.getBid());
                if (org.apache.commons.collections4.CollectionUtils.isEmpty(apmRoleVOS)) {
                    return false;
                }
                Map<String, String> codeBidMap = apmRoleVOS.stream().collect(Collectors.toMap(ApmRoleVO::getCode, ApmRoleVO::getBid));

                for(Map.Entry<String,List<String>> entry:rrDomainRelMap.entrySet()){
                    List<String> domainForList = entry.getValue();
                    Map<String,Set<String>> domainRoleMap = new HashMap<>(16);
                    for (String domainFor : domainForList) {
                        MObject domain = domainMap.get(domainFor);
                        if (null != domain) {
                            for(int i = 0 ; i < demandModuleFlowRoleCodes.size(); i++){
                                Object obj = domain.get(demandModuleAttrCodes.get(i));
                                List<String> objList = TranscendTools.analysisPersions(obj);
                                Set<String> domainRoleList = domainRoleMap.get(demandModuleFlowRoleCodes.get(i));
                                if(domainRoleList == null){
                                    domainRoleList = new HashSet<>();
                                }
                                domainRoleList.addAll(objList);
                                domainRoleMap.put(demandModuleFlowRoleCodes.get(i),domainRoleList);
                            }

                            //解析角色
                        }
                    }
                    for(Map.Entry<String,Set<String>> domanEntry:domainRoleMap.entrySet()){
                        //组装ApmFlowInstanceRoleUser 数据
                        List<ApmFlowInstanceRoleUser> users = getUsers(domanEntry.getValue(),entry.getKey(),codeBidMap.get(domanEntry.getKey()),DemandManagementEnum.IR_MODULE_FLAG.getCode(),"1253665116014346240");
                        apmFlowInstanceRoleUsers.addAll(users);
                    }
                }
            }
        }
        if(CollectionUtils.isNotEmpty(apmFlowInstanceRoleUsers)){
            int maxInstanceUserSize = 1100;
            if(apmFlowInstanceRoleUsers.size() > maxInstanceUserSize){
                List<List<ApmFlowInstanceRoleUser>>  parList= Lists.partition(apmFlowInstanceRoleUsers, 1000);
                for(List<ApmFlowInstanceRoleUser> list:parList){
                    apmFlowInstanceRoleUserService.saveBatch(list);
                }
                return true;
            }else{
                return apmFlowInstanceRoleUserService.saveBatch(apmFlowInstanceRoleUsers);
            }
        }
        return false;
    }

    public boolean handleIrOsVersion() {
        if(CollectionUtils.isEmpty(osVersionRoleCodes)||CollectionUtils.isEmpty(osVersionAttrCodes) || osVersionRoleCodes.size() != osVersionAttrCodes.size()){
            return false;
        }
        //查询所有RR数据
        //查询所有RR数据
        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = new ArrayList<>();
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty("delete_flag");
        queryWrapper.setCondition("=");
        queryWrapper.setValue(0);
        List<QueryWrapper> queryWrapperList = new ArrayList<>();
        queryWrapperList.add(queryWrapper);
        List<MObject> srList = objectModelCrudI.list("A5G",queryWrapperList);
        Set<String> set = new HashSet<>();
        if(CollectionUtils.isNotEmpty(srList)){
            for(MObject mObject:srList){
                if(mObject.get("targetOSversion") != null){
                    set.add(mObject.get("targetOSversion")+"");
                }
            }
        }
        if(CollectionUtils.isNotEmpty(set)) {
            //查询版本
            QueryWrapper domainQo = new QueryWrapper();
            domainQo.in(TranscendModelBaseFields.BID, set);
            List<QueryWrapper> domainWrappers = QueryWrapper.buildSqlQo(domainQo);
            List<MObject> domainList = objectModelCrudI.list("A4D", domainWrappers);
            Map<String,MObject> domainMap = new HashMap<>(16);
            for (MObject domain : domainList) {
                domainMap.put(domain.get(TranscendModelBaseFields.BID)+"", domain);
            }
            //查角色BID
            ApmSphere spaceSphere = apmSphereService.getByBizBidAndType("1253665116014346240", TypeEnum.OBJECT.getCode());
            if (spaceSphere == null) {
                return false;
            }
            List<ApmRoleVO> apmRoleVOS = roleService.listByRoleBidsByCodes(osVersionRoleCodes, spaceSphere.getBid());
            if (org.apache.commons.collections4.CollectionUtils.isEmpty(apmRoleVOS)) {
                return false;
            }
            Map<String, String> codeBidMap = apmRoleVOS.stream().collect(Collectors.toMap(ApmRoleVO::getCode, ApmRoleVO::getBid));
            for(MObject mObject:srList){
                if(mObject.get("targetOSversion") != null){
                    MObject domain = domainMap.get(mObject.get("targetOSversion")+"");
                    if (null != domain) {
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
                            if (CollectionUtils.isNotEmpty(userSet)) {
                                List<ApmFlowInstanceRoleUser> users = getUsers(userSet,mObject.getBid(),codeBidMap.get(osVersionRoleCodes.get(i)),DemandManagementEnum.IR_OSVERSION_FLAG.getCode(),"1253665116014346240");
                                apmFlowInstanceRoleUsers.addAll(users);
                            }
                        }
                    }
                }
            }
        }
        if(CollectionUtils.isNotEmpty(apmFlowInstanceRoleUsers)){
            int maxInstanceUserSize = 1100;
            if(apmFlowInstanceRoleUsers.size() > maxInstanceUserSize){
                List<List<ApmFlowInstanceRoleUser>>  parList= Lists.partition(apmFlowInstanceRoleUsers, 1000);
                for(List<ApmFlowInstanceRoleUser> list:parList){
                    apmFlowInstanceRoleUserService.saveBatch(list);
                }
                return true;
            }else{
                return apmFlowInstanceRoleUserService.saveBatch(apmFlowInstanceRoleUsers);
            }
        }
        return false;
    }

    public boolean handleSrModule() {
        if(CollectionUtils.isEmpty(demandModuleFlowRoleCodes)||CollectionUtils.isEmpty(demandModuleAttrCodes) || demandModuleFlowRoleCodes.size() != demandModuleAttrCodes.size()){
            return false;
        }
        //查询所有RR数据
        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = new ArrayList<>();
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty("delete_flag");
        queryWrapper.setCondition("=");
        queryWrapper.setValue(0);
        List<QueryWrapper> queryWrapperList = new ArrayList<>();
        queryWrapperList.add(queryWrapper);
        List<MObject> srList = objectModelCrudI.list("A5F",queryWrapperList);
        Set<String> set = new HashSet<>();
        if(CollectionUtils.isNotEmpty(srList)){
            for(MObject mObject:srList){
                if(mObject.get("module") != null){
                    set.add(mObject.get("module")+"");
                }
            }
        }
        if(CollectionUtils.isNotEmpty(set)) {
            //查询领域数据
            QueryWrapper domainQo = new QueryWrapper();
            domainQo.in(TranscendModelBaseFields.BID, set);
            List<QueryWrapper> domainWrappers = QueryWrapper.buildSqlQo(domainQo);
            List<MObject> domainList = objectModelCrudI.list(TranscendModel.MODULE.getCode(), domainWrappers);
            Map<String,MObject> domainMap = new HashMap<>(16);
            for (MObject domain : domainList) {
                domainMap.put(domain.get(TranscendModelBaseFields.BID)+"", domain);
            }
            //查角色BID
            ApmSphere spaceSphere = apmSphereService.getByBizBidAndType("1253654532919947264", TypeEnum.OBJECT.getCode());
            if (spaceSphere == null) {
                return false;
            }
            List<ApmRoleVO> apmRoleVOS = roleService.listByRoleBidsByCodes(demandModuleFlowRoleCodes, spaceSphere.getBid());
            if (org.apache.commons.collections4.CollectionUtils.isEmpty(apmRoleVOS)) {
                return false;
            }
            Map<String, String> codeBidMap = apmRoleVOS.stream().collect(Collectors.toMap(ApmRoleVO::getCode, ApmRoleVO::getBid));
            for(MObject mObject:srList){
                if(mObject.get("module") != null){
                    MObject domain = domainMap.get(mObject.get("module")+"");
                    if (null != domain) {
                        for(int i = 0 ; i < demandModuleFlowRoleCodes.size(); i++){
                            Object obj = domain.get(demandModuleAttrCodes.get(i));
                            List<String> objList = TranscendTools.analysisPersions(obj);
                            if(CollectionUtils.isNotEmpty(objList)){
                                //组装ApmFlowInstanceRoleUser 数据
                                Set<String> userSet = new HashSet<>();
                                userSet.addAll(objList);
                                List<ApmFlowInstanceRoleUser> users = getUsers(userSet,mObject.getBid(),codeBidMap.get(demandModuleFlowRoleCodes.get(i)),DemandManagementEnum.SR_MODULE_FLAG.getCode(),"1253654532919947264");
                                apmFlowInstanceRoleUsers.addAll(users);
                            }
                        }
                    }
                }
            }
        }
        if(CollectionUtils.isNotEmpty(apmFlowInstanceRoleUsers)){
            int maxInstanceUserSize = 1100;
            if(apmFlowInstanceRoleUsers.size() > maxInstanceUserSize){
                List<List<ApmFlowInstanceRoleUser>>  parList= Lists.partition(apmFlowInstanceRoleUsers, 1000);
                for(List<ApmFlowInstanceRoleUser> list:parList){
                    apmFlowInstanceRoleUserService.saveBatch(list);
                }
                return true;
            }else{
                return apmFlowInstanceRoleUserService.saveBatch(apmFlowInstanceRoleUsers);
            }
        }
        return false;
    }

    public boolean handleArModule() {
        if(CollectionUtils.isEmpty(demandModuleFlowRoleCodes)||CollectionUtils.isEmpty(demandModuleAttrCodes) || demandModuleFlowRoleCodes.size() != demandModuleAttrCodes.size()){
            return false;
        }
        //查询所有RR数据
        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = new ArrayList<>();
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty("delete_flag");
        queryWrapper.setCondition("=");
        queryWrapper.setValue(0);
        List<QueryWrapper> queryWrapperList = new ArrayList<>();
        queryWrapperList.add(queryWrapper);
        List<MObject> srList = objectModelCrudI.list(arModelCode,queryWrapperList);
        Set<String> set = new HashSet<>();
        if(CollectionUtils.isNotEmpty(srList)){
            for(MObject mObject:srList){
                if(mObject.get("module") != null){
                    set.add(mObject.get("module")+"");
                }
            }
        }
        if(CollectionUtils.isNotEmpty(set)) {
            //查询领域数据
            QueryWrapper domainQo = new QueryWrapper();
            domainQo.in(TranscendModelBaseFields.BID, set);
            List<QueryWrapper> domainWrappers = QueryWrapper.buildSqlQo(domainQo);
            List<MObject> domainList = objectModelCrudI.list(TranscendModel.MODULE.getCode(), domainWrappers);
            Map<String,MObject> domainMap = new HashMap<>(16);
            for (MObject domain : domainList) {
                domainMap.put(domain.get(TranscendModelBaseFields.BID)+"", domain);
            }
            //查角色BID
            ApmSphere spaceSphere = apmSphereService.getByBizBidAndType(arSpaceAppBid, TypeEnum.OBJECT.getCode());
            if (spaceSphere == null) {
                return false;
            }
            List<ApmRoleVO> apmRoleVOS = roleService.listByRoleBidsByCodes(demandModuleFlowRoleCodes, spaceSphere.getBid());
            if (org.apache.commons.collections4.CollectionUtils.isEmpty(apmRoleVOS)) {
                return false;
            }
            Map<String, String> codeBidMap = apmRoleVOS.stream().collect(Collectors.toMap(ApmRoleVO::getCode, ApmRoleVO::getBid));
            for(MObject mObject:srList){
                if(mObject.get("module") != null){
                    MObject domain = domainMap.get(mObject.get("module")+"");
                    if (null != domain) {
                        for(int i = 0 ; i < demandModuleFlowRoleCodes.size(); i++){
                            Object obj = domain.get(demandModuleAttrCodes.get(i));
                            List<String> objList = TranscendTools.analysisPersions(obj);
                            if(CollectionUtils.isNotEmpty(objList)){
                                //组装ApmFlowInstanceRoleUser 数据
                                Set<String> userSet = new HashSet<>();
                                userSet.addAll(objList);
                                List<ApmFlowInstanceRoleUser> users = getUsers(userSet,mObject.getBid(),codeBidMap.get(demandModuleFlowRoleCodes.get(i)),DemandManagementEnum.AR_MODULE_FLAG.getCode(),arSpaceAppBid);
                                apmFlowInstanceRoleUsers.addAll(users);
                            }
                        }
                    }
                }
            }
        }
        if(CollectionUtils.isNotEmpty(apmFlowInstanceRoleUsers)){
            int maxInstanceUserSize = 1100;
            if(apmFlowInstanceRoleUsers.size() > maxInstanceUserSize){
                List<List<ApmFlowInstanceRoleUser>>  parList= Lists.partition(apmFlowInstanceRoleUsers, 1000);
                for(List<ApmFlowInstanceRoleUser> list:parList){
                    apmFlowInstanceRoleUserService.saveBatch(list);
                }
                return true;
            }else{
                return apmFlowInstanceRoleUserService.saveBatch(apmFlowInstanceRoleUsers);
            }
        }
        return false;
    }


    public boolean handleArDomain() {
        if(CollectionUtils.isEmpty(rrDemandFlowDomainRoleCodes)||CollectionUtils.isEmpty(rrDemandFlowDomainAttrCodes) || rrDemandFlowDomainRoleCodes.size() != rrDemandFlowDomainAttrCodes.size()){
            return false;
        }
        //查询所有RR数据
        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = new ArrayList<>();
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty("delete_flag");
        queryWrapper.setCondition("=");
        queryWrapper.setValue(0);
        List<QueryWrapper> queryWrapperList = new ArrayList<>();
        queryWrapperList.add(queryWrapper);
        List<MObject> srList = objectModelCrudI.list(arModelCode,queryWrapperList);
        Set<String> set = new HashSet<>();
        if(CollectionUtils.isNotEmpty(srList)){
            for(MObject mObject:srList){
                if(mObject.get("productArea") != null){
                    set.add(mObject.get("productArea")+"");
                }
            }
        }
        if(CollectionUtils.isNotEmpty(set)) {
            //查询领域数据
            QueryWrapper domainQo = new QueryWrapper();
            domainQo.in(TranscendModelBaseFields.BID, set);
            List<QueryWrapper> domainWrappers = QueryWrapper.buildSqlQo(domainQo);
            List<MObject> domainList = objectModelCrudI.list(TranscendModel.DOMAIN.getCode(), domainWrappers);
            Map<String,MObject> domainMap = new HashMap<>(16);
            for (MObject domain : domainList) {
                domainMap.put(domain.get(TranscendModelBaseFields.BID)+"", domain);
            }
            //查角色BID
            ApmSphere spaceSphere = apmSphereService.getByBizBidAndType(arSpaceAppBid, TypeEnum.OBJECT.getCode());
            if (spaceSphere == null) {
                return false;
            }
            List<ApmRoleVO> apmRoleVOS = roleService.listByRoleBidsByCodes(rrDemandFlowDomainRoleCodes, spaceSphere.getBid());
            if (org.apache.commons.collections4.CollectionUtils.isEmpty(apmRoleVOS)) {
                return false;
            }
            Map<String, String> codeBidMap = apmRoleVOS.stream().collect(Collectors.toMap(ApmRoleVO::getCode, ApmRoleVO::getBid));
            for(MObject mObject:srList){
                if(mObject.get("productArea") != null){
                    MObject domain = domainMap.get(mObject.get("productArea")+"");
                    if (null != domain) {
                        for(int i = 0 ; i < rrDemandFlowDomainRoleCodes.size(); i++){
                            Object obj = domain.get(rrDemandFlowDomainAttrCodes.get(i));
                            List<String> objList = TranscendTools.analysisPersions(obj);
                            if(CollectionUtils.isNotEmpty(objList)){
                                //组装ApmFlowInstanceRoleUser 数据
                                Set<String> userSet = new HashSet<>();
                                userSet.addAll(objList);
                                String roleBid = codeBidMap.get(rrDemandFlowDomainRoleCodes.get(i));
                                if(StringUtils.isEmpty(roleBid)){
                                    roleBid = rrDemandFlowDomainRoleCodes.get(i);
                                }
                                List<ApmFlowInstanceRoleUser> users = getUsers(userSet,mObject.getBid(),roleBid,DemandManagementEnum.AR_DOMAIN_FLAG.getCode(),arSpaceAppBid);
                                apmFlowInstanceRoleUsers.addAll(users);
                            }
                        }
                    }
                }
            }
        }
        if(CollectionUtils.isNotEmpty(apmFlowInstanceRoleUsers)){
            int maxInstanceUserSize = 1100;
            if(apmFlowInstanceRoleUsers.size() > maxInstanceUserSize){
                List<List<ApmFlowInstanceRoleUser>>  parList= Lists.partition(apmFlowInstanceRoleUsers, 1000);
                for(List<ApmFlowInstanceRoleUser> list:parList){
                    apmFlowInstanceRoleUserService.saveBatch(list);
                }
                return true;
            }else{
                return apmFlowInstanceRoleUserService.saveBatch(apmFlowInstanceRoleUsers);
            }
        }
        return false;
    }

    public boolean handleSrDomain() {
        if(CollectionUtils.isEmpty(rrDemandFlowDomainRoleCodes)||CollectionUtils.isEmpty(rrDemandFlowDomainAttrCodes) || rrDemandFlowDomainRoleCodes.size() != rrDemandFlowDomainAttrCodes.size()){
            return false;
        }
        //查询所有RR数据
        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = new ArrayList<>();
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty("delete_flag");
        queryWrapper.setCondition("=");
        queryWrapper.setValue(0);
        List<QueryWrapper> queryWrapperList = new ArrayList<>();
        queryWrapperList.add(queryWrapper);
        List<MObject> srList = objectModelCrudI.list("A5F",queryWrapperList);
        Set<String> set = new HashSet<>();
        if(CollectionUtils.isNotEmpty(srList)){
            for(MObject mObject:srList){
                if(mObject.get("productArea") != null){
                    set.add(mObject.get("productArea")+"");
                }
            }
        }
        if(CollectionUtils.isNotEmpty(set)) {
            //查询领域数据
            QueryWrapper domainQo = new QueryWrapper();
            domainQo.in(TranscendModelBaseFields.BID, set);
            List<QueryWrapper> domainWrappers = QueryWrapper.buildSqlQo(domainQo);
            List<MObject> domainList = objectModelCrudI.list(TranscendModel.DOMAIN.getCode(), domainWrappers);
            Map<String,MObject> domainMap = new HashMap<>(16);
            for (MObject domain : domainList) {
                domainMap.put(domain.get(TranscendModelBaseFields.BID)+"", domain);
            }
            //查角色BID
            ApmSphere spaceSphere = apmSphereService.getByBizBidAndType("1253654532919947264", TypeEnum.OBJECT.getCode());
            if (spaceSphere == null) {
                return false;
            }
            List<ApmRoleVO> apmRoleVOS = roleService.listByRoleBidsByCodes(rrDemandFlowDomainRoleCodes, spaceSphere.getBid());
            if (org.apache.commons.collections4.CollectionUtils.isEmpty(apmRoleVOS)) {
                return false;
            }
            Map<String, String> codeBidMap = apmRoleVOS.stream().collect(Collectors.toMap(ApmRoleVO::getCode, ApmRoleVO::getBid));
            for(MObject mObject:srList){
                if(mObject.get("productArea") != null){
                    MObject domain = domainMap.get(mObject.get("productArea")+"");
                    if (null != domain) {
                        for(int i = 0 ; i < rrDemandFlowDomainRoleCodes.size(); i++){
                            Object obj = domain.get(rrDemandFlowDomainAttrCodes.get(i));
                            List<String> objList = TranscendTools.analysisPersions(obj);
                            if(CollectionUtils.isNotEmpty(objList)){
                                //组装ApmFlowInstanceRoleUser 数据
                                Set<String> userSet = new HashSet<>();
                                userSet.addAll(objList);
                                String roleBid = codeBidMap.get(rrDemandFlowDomainRoleCodes.get(i));
                                if(StringUtils.isEmpty(roleBid)){
                                    roleBid = rrDemandFlowDomainRoleCodes.get(i);
                                }
                                List<ApmFlowInstanceRoleUser> users = getUsers(userSet,mObject.getBid(),roleBid,DemandManagementEnum.SR_DOMAIN_FLAG.getCode(),"1253654532919947264");
                                apmFlowInstanceRoleUsers.addAll(users);
                            }
                        }
                    }
                }
            }
        }
        if(CollectionUtils.isNotEmpty(apmFlowInstanceRoleUsers)){
            int maxInstanceUserSize = 1100;
            if(apmFlowInstanceRoleUsers.size() > maxInstanceUserSize){
                List<List<ApmFlowInstanceRoleUser>>  parList= Lists.partition(apmFlowInstanceRoleUsers, 1000);
                for(List<ApmFlowInstanceRoleUser> list:parList){
                    apmFlowInstanceRoleUserService.saveBatch(list);
                }
                return true;
            }else{
                return apmFlowInstanceRoleUserService.saveBatch(apmFlowInstanceRoleUsers);
            }
        }
        return false;
    }

    public boolean handleRrModule() {
        if(CollectionUtils.isEmpty(demandModuleFlowRoleCodes)||CollectionUtils.isEmpty(demandModuleAttrCodes) || demandModuleFlowRoleCodes.size() != demandModuleAttrCodes.size()){
            return false;
        }
        //查询所有RR数据
        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = new ArrayList<>();
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty("delete_flag");
        queryWrapper.setCondition("=");
        queryWrapper.setValue(0);
        List<QueryWrapper> queryWrapperList = new ArrayList<>();
        queryWrapperList.add(queryWrapper);
        List<MObject> rrList = objectModelCrudI.list("A5E",queryWrapperList);
        if(CollectionUtils.isNotEmpty(rrList)){
            List<String> rrBidList = new ArrayList<>();
            for (MObject rr : rrList) {
                rrBidList.add(rr.getBid());
            }
            //查领域组件
            QueryWrapper qo = new QueryWrapper();
            qo.eq("domain_model_code", TranscendModel.MODULE.getCode())
                    .and()
                    .in("rr_bid", rrBidList);
            List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
            List<MObject> queryList = objectModelCrudI.list(TranscendModel.DOMAIN_COMPONENT.getCode(), queryWrappers);
            //查询领域
            if(CollectionUtils.isNotEmpty(queryList)) {
                Set<String> set = new HashSet<>();
                //rrBid和领域Bid关系
                Map<String,List<String>> rrDomainRelMap = new HashMap<>(16);
                for (MObject domain : queryList) {
                        set.add(domain.get("domainBid")+"");
                        List<String> rrBidTempList = rrDomainRelMap.get(domain.get("rrBid")+"");
                        if(rrBidTempList == null){
                            rrBidTempList = new ArrayList<>();
                        }
                        rrBidTempList.add(domain.get("domainBid")+"");
                        rrDomainRelMap.put(domain.get("rrBid")+"",rrBidTempList);
                }
                //查询领域数据
                QueryWrapper domainQo = new QueryWrapper();
                domainQo.in(TranscendModelBaseFields.BID, set);
                List<QueryWrapper> domainWrappers = QueryWrapper.buildSqlQo(domainQo);
                List<MObject> domainList = objectModelCrudI.list(TranscendModel.MODULE.getCode(), domainWrappers);
                Map<String,MObject> domainMap = new HashMap<>(16);
                for (MObject domain : domainList) {
                    domainMap.put(domain.get(TranscendModelBaseFields.BID)+"", domain);
                }
                //查角色BID
                ApmSphere spaceSphere = apmSphereService.getByBizBidAndType(spaceAppBid, TypeEnum.OBJECT.getCode());
                if (spaceSphere == null) {
                    return false;
                }
                List<ApmRoleVO> apmRoleVOS = roleService.listByRoleBidsByCodes(demandModuleFlowRoleCodes, spaceSphere.getBid());
                if (org.apache.commons.collections4.CollectionUtils.isEmpty(apmRoleVOS)) {
                    return false;
                }
                Map<String, String> codeBidMap = apmRoleVOS.stream().collect(Collectors.toMap(ApmRoleVO::getCode, ApmRoleVO::getBid));

                /*该领域下所有的属性角色：
                前面字段 后面角色
                领域主任domainDirector  ---- 》领域主任DomainDirector
                领域产品规划代表 productManager   ---->领域产品规划domainLeader
                领域se  domainSe------>    领域SE  domainse
                领域开始代表 domainDevelopmenRepresentative--->  领域开发代表 DomainDevelopmentRepresentative
                领域测试代表  softwareTestingRepresentative   -----> 软件测试代表 DomainTestRepresentative

                用户体验设计代表  userExperienceDesignRepresentative   ----->用户体验设计代表 UserExperienceDesignRepresentative

                SRE需求管理代表  requirementManagementRepresentative---->  SRE软件需求管理    sre
                软件项目代表  softwareProjectRepresentative--->      软件项目代表  Softwareprojectrepresentative*/
                for(Map.Entry<String,List<String>> entry:rrDomainRelMap.entrySet()){
                    List<String> domainForList = entry.getValue();
                    Map<String,Set<String>> domainRoleMap = new HashMap<>(16);
                    for (String domainFor : domainForList) {
                        MObject domain = domainMap.get(domainFor);
                        if (null != domain) {
                            for(int i = 0 ; i < demandModuleFlowRoleCodes.size(); i++){
                                Object obj = domain.get(demandModuleAttrCodes.get(i));
                                List<String> objList = TranscendTools.analysisPersions(obj);
                                Set<String> domainRoleList = domainRoleMap.get(demandModuleFlowRoleCodes.get(i));
                                if(domainRoleList == null){
                                    domainRoleList = new HashSet<>();
                                }
                                domainRoleList.addAll(objList);
                                domainRoleMap.put(demandModuleFlowRoleCodes.get(i),domainRoleList);
                            }

                            //解析角色
                        }
                    }
                    for(Map.Entry<String,Set<String>> domanEntry:domainRoleMap.entrySet()){
                        //组装ApmFlowInstanceRoleUser 数据
                        List<ApmFlowInstanceRoleUser> users = getUsers(domanEntry.getValue(),entry.getKey(),codeBidMap.get(domanEntry.getKey()),DemandManagementEnum.RR_MODULE_FLAG.getCode(),"1253656300279398400");
                        apmFlowInstanceRoleUsers.addAll(users);
                    }
                }
            }
        }
        if(CollectionUtils.isNotEmpty(apmFlowInstanceRoleUsers)){
            int maxInstanceUserSize = 1100;
            if(apmFlowInstanceRoleUsers.size() > maxInstanceUserSize){
                List<List<ApmFlowInstanceRoleUser>>  parList= Lists.partition(apmFlowInstanceRoleUsers, 1000);
                for(List<ApmFlowInstanceRoleUser> list:parList){
                    apmFlowInstanceRoleUserService.saveBatch(list);
                }
                return true;
            }else{
                return apmFlowInstanceRoleUserService.saveBatch(apmFlowInstanceRoleUsers);
            }
        }
        return false;
    }

    public boolean handleDomain() {
        if(CollectionUtils.isEmpty(rrDemandFlowDomainRoleCodes)||CollectionUtils.isEmpty(rrDemandFlowDomainAttrCodes) || rrDemandFlowDomainRoleCodes.size() != rrDemandFlowDomainAttrCodes.size()){
            return false;
        }
        //查询所有RR数据
        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = new ArrayList<>();
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty("delete_flag");
        queryWrapper.setCondition("=");
        queryWrapper.setValue(0);
        List<QueryWrapper> queryWrapperList = new ArrayList<>();
        queryWrapperList.add(queryWrapper);
        List<MObject> rrList = objectModelCrudI.list("A5E",queryWrapperList);
        if(CollectionUtils.isNotEmpty(rrList)){
            List<String> rrBidList = new ArrayList<>();
            for (MObject rr : rrList) {
                rrBidList.add(rr.getBid());
            }
            //查领域组件
            QueryWrapper qo = new QueryWrapper();
            qo.eq("domain_model_code", TranscendModel.DOMAIN.getCode())
                    .and()
                    .in("rr_bid", rrBidList);
            List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
            List<MObject> queryList = objectModelCrudI.list(TranscendModel.DOMAIN_COMPONENT.getCode(), queryWrappers);
            //查询领域
            if(CollectionUtils.isNotEmpty(queryList)) {
                Set<String> set = new HashSet<>();
                //rrBid和领域Bid关系
                Map<String,List<String>> rrDomainRelMap = new HashMap<>(16);
                for (MObject domain : queryList) {
                    set.add(domain.get("domainBid")+"");
                    List<String> rrBidTempList = rrDomainRelMap.get(domain.get("rrBid")+"");
                    if(rrBidTempList == null){
                        rrBidTempList = new ArrayList<>();
                    }
                    rrBidTempList.add(domain.get("domainBid")+"");
                    rrDomainRelMap.put(domain.get("rrBid")+"",rrBidTempList);
                }
                //查询领域数据
                QueryWrapper domainQo = new QueryWrapper();
                domainQo.in(TranscendModelBaseFields.BID, set);
                List<QueryWrapper> domainWrappers = QueryWrapper.buildSqlQo(domainQo);
                List<MObject> domainList = objectModelCrudI.list(TranscendModel.DOMAIN.getCode(), domainWrappers);
                Map<String,MObject> domainMap = new HashMap<>(16);
                for (MObject domain : domainList) {
                    domainMap.put(domain.get(TranscendModelBaseFields.BID)+"", domain);
                }
                //查角色BID
                ApmSphere spaceSphere = apmSphereService.getByBizBidAndType(spaceAppBid, TypeEnum.OBJECT.getCode());
                if (spaceSphere == null) {
                    return false;
                }
                List<ApmRoleVO> apmRoleVOS = roleService.listByRoleBidsByCodes(rrDemandFlowDomainRoleCodes, spaceSphere.getBid());
                if (org.apache.commons.collections4.CollectionUtils.isEmpty(apmRoleVOS)) {
                    return false;
                }
                Map<String, String> codeBidMap = apmRoleVOS.stream().collect(Collectors.toMap(ApmRoleVO::getCode, ApmRoleVO::getBid));

                /*该领域下所有的属性角色：
                前面字段 后面角色
                领域主任domainDirector  ---- 》领域主任DomainDirector
                领域产品规划代表 productManager   ---->领域产品规划domainLeader
                领域se  domainSe------>    领域SE  domainse
                领域开始代表 domainDevelopmenRepresentative--->  领域开发代表 DomainDevelopmentRepresentative
                领域测试代表  softwareTestingRepresentative   -----> 软件测试代表 DomainTestRepresentative

                用户体验设计代表  userExperienceDesignRepresentative   ----->用户体验设计代表 UserExperienceDesignRepresentative

                SRE需求管理代表  requirementManagementRepresentative---->  SRE软件需求管理    sre
                软件项目代表  softwareProjectRepresentative--->      软件项目代表  Softwareprojectrepresentative*/
                for(Map.Entry<String,List<String>> entry:rrDomainRelMap.entrySet()){
                    List<String> domainForList = entry.getValue();
                    Map<String,Set<String>> domainRoleMap = new HashMap<>(16);
                    for (String domainFor : domainForList) {
                        MObject domain = domainMap.get(domainFor);
                        if (null != domain) {
                            for(int i = 0 ; i < rrDemandFlowDomainRoleCodes.size(); i++){
                                Object obj = domain.get(rrDemandFlowDomainAttrCodes.get(i));
                                List<String> objList = TranscendTools.analysisPersions(obj);
                                Set<String> domainRoleList = domainRoleMap.get(rrDemandFlowDomainRoleCodes.get(i));
                                if(domainRoleList == null){
                                    domainRoleList = new HashSet<>();
                                }
                                domainRoleList.addAll(objList);
                                domainRoleMap.put(rrDemandFlowDomainRoleCodes.get(i),domainRoleList);
                            }

                            //解析角色
                        }
                    }
                    for(Map.Entry<String,Set<String>> domanEntry:domainRoleMap.entrySet()){
                        //组装ApmFlowInstanceRoleUser 数据
                        List<ApmFlowInstanceRoleUser> users = getUsers(domanEntry.getValue(),entry.getKey(),codeBidMap.get(domanEntry.getKey()),"1","1253656300279398400");
                        apmFlowInstanceRoleUsers.addAll(users);
                    }
                }
            }
        }
        if(CollectionUtils.isNotEmpty(apmFlowInstanceRoleUsers)){
            int maxInstanceUserSize = 1100;
            if(apmFlowInstanceRoleUsers.size() > maxInstanceUserSize){
                List<List<ApmFlowInstanceRoleUser>>  parList= Lists.partition(apmFlowInstanceRoleUsers, 1000);
                for(List<ApmFlowInstanceRoleUser> list:parList){
                    apmFlowInstanceRoleUserService.saveBatch(list);
                }
                return true;
            }else{
                return apmFlowInstanceRoleUserService.saveBatch(apmFlowInstanceRoleUsers);
            }
        }
        return false;
    }

    private List<ApmFlowInstanceRoleUser> getUsers(Set<String> users,String rrBid,String roleBid,String handleFlag,String spaceAppBid){
        List<ApmFlowInstanceRoleUser> apmFlowInstanceRoleUsers = new ArrayList<>();
        for (String user : users) {
            ApmFlowInstanceRoleUser apmFlowInstanceRoleUser = new ApmFlowInstanceRoleUser();
            apmFlowInstanceRoleUser.setRoleBid(roleBid);
            apmFlowInstanceRoleUser.setInstanceBid(rrBid);
            apmFlowInstanceRoleUser.setUserNo(user);
            apmFlowInstanceRoleUser.setSpaceBid("1195029861299785728");
            apmFlowInstanceRoleUser.setSpaceAppBid(spaceAppBid);
            apmFlowInstanceRoleUser.setHandleFlag(handleFlag);
            apmFlowInstanceRoleUsers.add(apmFlowInstanceRoleUser);
        }
        return apmFlowInstanceRoleUsers;
    }

}
