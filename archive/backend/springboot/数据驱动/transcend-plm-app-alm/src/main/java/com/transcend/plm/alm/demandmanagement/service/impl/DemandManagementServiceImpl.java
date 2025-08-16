package com.transcend.plm.alm.demandmanagement.service.impl;

import com.alibaba.excel.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.core.exception.TranscendBizException;
import com.transcend.framework.open.entity.EmployeeInfo;
import com.transcend.framework.open.service.OpenUserService;
import com.transcend.plm.alm.demandmanagement.config.DemandManagementProperties;
import com.transcend.plm.alm.demandmanagement.constants.DemandManagementConstant;
import com.transcend.plm.alm.demandmanagement.entity.ao.*;
import com.transcend.plm.alm.demandmanagement.entity.bo.RelDemandBo;
import com.transcend.plm.alm.demandmanagement.entity.vo.SelectVo;
import com.transcend.plm.alm.demandmanagement.enums.DemandManagementEnum;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.alm.demandmanagement.mapstruct.SelectVoConverter;
import com.transcend.plm.alm.demandmanagement.service.DemandManagementService;
import com.transcend.plm.alm.tools.TranscendTools;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.apm.flow.service.impl.RuntimeService;
import com.transcend.plm.datadriven.apm.permission.enums.OperatorEnum;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.MObjectCopyAo;
import com.transcend.plm.datadriven.apm.permission.pojo.dto.PermissionCheckDto;
import com.transcend.plm.datadriven.apm.permission.service.IPermissionCheckService;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmMultiTreeModelMixQo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppRelationDataDrivenService;
import com.transcend.plm.datadriven.common.constant.DataBaseConstant;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transsion.framework.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Sets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.transcend.plm.alm.demandmanagement.constants.DemandManagementConstant.REL_DEMAND;
import static com.transcend.plm.alm.demandmanagement.constants.DemandManagementConstant.REQUIREMENT_CODING;
import static com.transcend.plm.datadriven.api.model.BaseDataEnum.BID;
import static com.transcend.plm.datadriven.api.model.RelationObjectEnum.TARGET_BID;


/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description
 * @date 2024/06/21 11:11
 **/
@Service
@Slf4j
public class DemandManagementServiceImpl implements DemandManagementService {

    @Resource
    private IBaseApmSpaceAppDataDrivenService iBaseApmSpaceAppDataDrivenService;
    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;
    @Resource
    private DemandManagementProperties demandManagementProperties;
    @Resource
    private DemandManagementService demandManagementService;
    @Resource
    @Value("#{'${transcend.plm.apm.blob.attr.filter:richTextContent,demandDesc,testingRecommendations,text}'.split(',')}")
    private List<String> filterBlobAttr;
    @Value("${transcend.plm.apm.special.domainPlanRepresentRoleCode:Domain_Plan_Represent}")
    private String domainPlanRepresentRoleCode;

    @Value("${transcend.plm.apm.special.sreRoleCode:sre}")
    private String sreRoleCode;

    @Value("${transcend.plm.apm.special.pdmRoleCode:pdm}")
    private String pdmRoleCode;
    @Resource
    private ApmSpaceAppService apmSpaceAppService;

    @Value("${transcend.plm.apm.special.domainseRoleCode:domainse}")
    private String domainseRoleCode;

    @Value("${transcend.plm.apm.special.representativeofDomainProductPlanningRoleCode:RepresentativeofDomainProductPlanning}")
    private String representativeofDomainProductPlanningRoleCode;
    @Resource
    private IBaseApmSpaceAppRelationDataDrivenService apmSpaceAppRelationDataDrivenService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private OpenUserService openUserService;

    @Value("#{'${transcend.plm.apm.demand.notProductArea.Bids:1253656300279398400}'.split(',')}")
    private List<String> demandNotIntoProductArea;

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
     * 领域SE(Domain_SE) 角色编码
     */
    @Value("${transcend.plm.apm.special.domainSeRoleCode:Domain_SE}")
    private String domainSeRoleCode;

    /**
     * 领域主任 角色编码
     */
    @Value("${transcend.plm.apm.special.domainDirectorRoleCode:DomainDirector}")
    private String domainDirectorRoleCode;

    /**
     * 领域开发代表 角色编码
     */
    @Value("${transcend.plm.apm.special.domainDevelopmentRepresentativeRoleCode:DomainDevelopmentRepresentative}")
    private String domainDevelopmentRepresentativeRoleCode;

    /**
     * 领域产品规划 角色编码
     */
    @Value("${transcend.plm.apm.special.domainLeaderCode:domainLeader}")
    private String domainLeaderCode;
    /**
     * 领域组件 rr_bid
     **/
    private String rrBidCoumn = "rr_bid";


    @Resource
    private IPermissionCheckService permissionCheckService;


    /**
     * 领域 modelCode字段
     **/
    private String domainModelCodeCoumn = "domainModelCode";

    /**
     * ir应用bid
     */
    private String irSpaceAppBid = "1253665116014346240";

    /**
     * 领域 bid字段
     **/
    private String bidCoumn = "bid";

    @Override
    public Boolean checkRepeat(String spaceBid, String spaceAppBid, String instanceBid) {
        QueryCondition queryCondition = new QueryCondition();
        QueryWrapper repeat = new QueryWrapper();
        repeat.eq(DemandManagementConstant.DEMAND_MANAGEMENT_REPEAT, instanceBid);
        queryCondition.setQueries(QueryWrapper.buildSqlQo(repeat));
        return CollectionUtils.isNotEmpty(iBaseApmSpaceAppDataDrivenService.list(spaceAppBid, queryCondition));
    }

    @Override
    public List<MObject> queryDomainTree(String spaceBid, String rrBid) {
        if (StringUtils.isBlank(rrBid)) {
            return Lists.newArrayList();
        }
        List<MObject> linkedDataList = queryRelData(spaceBid, rrBid);
        Set<String> jobNumberSet = linkedDataList.stream().map(data -> (String) data.get(DemandManagementEnum.DOMAIN_LEADER.getCode())).collect(Collectors.toSet());
        Map<String, String> jobNumberWithName = openUserService.batchFindByEmoNo(Lists.newArrayList(jobNumberSet))
                .stream().collect(Collectors.toMap(EmployeeInfo::getJobNumber, EmployeeInfo::getName));

        List<String> domainBidList = linkedDataList.stream().map(s -> (String) s.get("domainBid")).collect(Collectors.toList());

        // 领域实例的BID
        List<MObject> domainList = objectModelCrudI.listByBids(domainBidList, TranscendModel.DOMAIN.getCode());

        linkedDataList.forEach(e -> {
            filterBlobAttr.forEach(e::remove);
            e.put("userName", jobNumberWithName.getOrDefault((String) e.get(DemandManagementEnum.DOMAIN_LEADER.getCode()), ""));
            Optional<MObject> obj = domainList.stream().filter(s -> e.get("domainBid").equals(s.get(TranscendModelBaseFields.BID))).findFirst();
            if (obj.isPresent()) {
                MObject mObject = obj.get();
                Object domainDirector = mObject.get("domainDirector");
                List<String> alyUserList = Lists.newArrayList();
                if (domainDirector != null) {
                    if (domainDirector instanceof List) {
                        List<String> values = JSON.parseArray(JSON.toJSONString(domainDirector), String.class);
                        alyUserList.addAll(values);
                    } else {
                        alyUserList.add((String) domainDirector);
                    }
                }
                e.put("domainDirector", alyUserList);
            }
            domainList.stream().filter(s -> e.get(TranscendModelBaseFields.DATA_BID).equals(s.get("domainBid"))).findFirst();

        });

        return linkedDataList;
    }

    /**
     * 检查领域是否关联模块
     *
     * @param rrBid
     * @return
     */
    @Override
    public boolean checkDomainHaveModule(String rrBid){
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("rr_bid", rrBid).and().eq("delete_flag", 0);
        List<QueryWrapper> wrappers = QueryWrapper.buildSqlQo(wrapper);
        List<MObject> mObjects = objectModelCrudI.list(TranscendModel.DOMAIN_COMPONENT.getCode(), wrappers);
        if (CollectionUtils.isNotEmpty(mObjects)) {
            //校验领域下是否有模块
            Map<String, String> parentMap = new HashMap<>();
            for (MObject mObject1 : mObjects) {
                if (TranscendModel.MODULE.getCode().equals(mObject1.get(domainModelCodeCoumn))) {
                    parentMap.put(mObject1.get("parentBid") + "", mObject1.get(bidCoumn) + "");
                }
            }
            for (MObject mObject1 : mObjects) {
                if (TranscendModel.DOMAIN.getCode().equals(mObject1.get(domainModelCodeCoumn)) && !parentMap.containsKey(mObject1.get(bidCoumn) + "")) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 获取领域
     *
     * @param rrBid
     * @return
     */
    @Override
    public List<String> getDomainBidListByRrBid(String rrBid, String domainModelCode) {
        if (StringUtils.isEmpty(rrBid)) {
            return Lists.newArrayList();
        }
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("rr_bid", rrBid).and().eq("domain_model_code", domainModelCode).and().eq("delete_flag", 0);
        List<QueryWrapper> wrappers = QueryWrapper.buildSqlQo(wrapper);
        List<MObject> list = objectModelCrudI.list(TranscendModel.DOMAIN_COMPONENT.getCode(), wrappers);
        List<String> domainBidList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            domainBidList = list.stream().map(s -> (String) s.get("domainBid")).collect(Collectors.toList());
        }
        return domainBidList;
    }

    @Override
    public List<MObject> getDomainBidListByRrBids(List<String> rrBids, String domainModelCode){
        if(CollectionUtils.isEmpty(rrBids)){
            return Lists.newArrayList();
        }
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.in("rr_bid", rrBids).and().eq("domain_model_code", domainModelCode).and().eq("delete_flag", 0);
        List<QueryWrapper> wrappers = QueryWrapper.buildSqlQo(wrapper);
        List<MObject> list = objectModelCrudI.list(TranscendModel.DOMAIN_COMPONENT.getCode(), wrappers);
        return list;
    }

    @Override
    public List<SelectVo> queryDomainSelection(String spaceBid, Integer type, String rrBid, String selectedBid) {
        String selectedDomainBid = null;
        boolean notBlank = StringUtils.isNotBlank(selectedBid);
        if (notBlank) {
            MObject selectedData = objectModelCrudI.getByBid(TranscendModel.DOMAIN_COMPONENT.getCode(), selectedBid);
            if (selectedData == null) {
                throw new TranscendBizException("所选数据不存在");
            }
            selectedDomainBid = (String) selectedData.get(DemandManagementEnum.DOMAIN_BID.getCode());
        }
        // 已经关联的数据
        List<MObject> linkedDataList = StringUtils.isBlank(rrBid) ? Lists.newArrayList() : queryRelData(spaceBid, rrBid);
        Map<String, MObject> linkedMap = linkedDataList.stream().collect(Collectors.toMap(e -> (String) e.get(DemandManagementEnum.DOMAIN_BID.getCode()), Function.identity(), (k1, k2) -> k2));
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getBySpaceBidAndModelCode(spaceBid, TranscendModel.DOMAIN.getCode());
        ApmMultiTreeModelMixQo modelMixQo = new ApmMultiTreeModelMixQo();
        List<MObjectTree> tree = iBaseApmSpaceAppDataDrivenService.multiTree(spaceBid, apmSpaceApp.getBid(), modelMixQo, true, false);
        // 过滤出领域数据
        tree = tree.stream().filter(e -> e.getModelCode().equals(TranscendModel.DOMAIN.getCode())).collect(Collectors.toList());
        List<MObjectTree> resultTree = Lists.newArrayList();
        // 已经关联的数据给个标识,不能在选取
        recurseTree(tree, linkedMap, selectedDomainBid, resultTree);
        return SelectVoConverter.INSTANCE.mObjectTree2Vo(notBlank ? resultTree : tree);
    }

    @Override
    public Boolean batchRemoveDomain(String spaceBid, String spaceAppBid, RemoveDomainAo removeDomainAo) {
        String rrBid = removeDomainAo.getRrBid();
        List<String> bids = removeDomainAo.getBids();
        // 查询出需要移除的数据的关联需求bid
        List<MObject> needRemoveList = objectModelCrudI.listByBids(bids, TranscendModel.DOMAIN_COMPONENT.getCode());
        boolean checkModel = false;
        if (CollectionUtils.isNotEmpty(needRemoveList)) {
            for (MObject mObject : needRemoveList) {
                if (TranscendModel.MODULE.getCode().equals(mObject.get(DemandManagementEnum.DOMAIN_MODEL_CODE.getCode()) + "")) {
                    checkModel = true;
                }
            }
        }
        if (irSpaceAppBid.equals(spaceAppBid) && checkModel) {
            //如果是ir的应用bid 需要判断删除后是否还要模块
            List<MObject> mObjects = queryRelData(spaceBid, rrBid);
            for (int i = mObjects.size() - 1; i >= 0; i--) {
                if (bids.contains(mObjects.get(i).getBid())) {
                    mObjects.remove(i);
                }
            }
            if (CollectionUtils.isNotEmpty(mObjects)) {
                //校验领域下是否有模块
                Map<String, String> parentMap = new HashMap<>();
                for (MObject mObject1 : mObjects) {
                    if (TranscendModel.MODULE.getCode().equals(mObject1.get(domainModelCodeCoumn))) {
                        parentMap.put(mObject1.get("parentBid") + "", mObject1.get(bidCoumn) + "");
                    }
                }
                for (MObject mObject1 : mObjects) {
                    if (TranscendModel.DOMAIN.getCode().equals(mObject1.get(domainModelCodeCoumn)) && !parentMap.containsKey(mObject1.get(bidCoumn) + "")) {
                        throw new BusinessException("领域下必须存在至少一个模块！");
                    }
                }
            }
        }
        // 如果删除的数据中有主导领域,则提示先修改主导领域
        Optional<MObject> isLeadDomainOptional = needRemoveList.stream().filter(e -> Integer.valueOf(1).equals(e.get(DemandManagementEnum.IS_LEAD_DOMAIN.getCode()))).findAny();
        if (isLeadDomainOptional.isPresent()) {
            throw new TranscendBizException("移除的数据中包含主导领域，请先变更主导领域!");
        }
        // 移除的ir bids
        Set<String> removeIrBids = Sets.newHashSet();
        needRemoveList.forEach(e -> Optional.ofNullable(e.get(DemandManagementEnum.REL_DEMAND.getCode()))
                .ifPresent(irJson -> {
                    List<RelDemandBo> relDemandBos = JSON.parseObject(JSON.toJSONString(irJson), new TypeReference<List<RelDemandBo>>() {
                    });
                    if (CollectionUtils.isNotEmpty(relDemandBos)) {
                        removeIrBids.addAll(relDemandBos.stream().map(RelDemandBo::getRelDemandBid).collect(Collectors.toSet()));
                    }
                }));
        // 移除领域/模块/应用
        objectModelCrudI.batchLogicalDeleteByBids(TranscendModel.DOMAIN_COMPONENT.getCode(), bids);
        // 查询移除后的数据,对比出IR需求的bid差别,来判断是否移除RR-IR关系数据
        List<MObject> linkedDataList = queryRelData(spaceBid, rrBid);
        Set<String> existIrBids = Sets.newHashSet();
        linkedDataList.forEach(linkedData -> Optional.ofNullable(linkedData.get(DemandManagementEnum.REL_DEMAND.getCode()))
                .ifPresent(irJson -> {
                    List<RelDemandBo> relDemandBos = JSON.parseObject(JSON.toJSONString(irJson), new TypeReference<List<RelDemandBo>>() {
                    });
                    if (CollectionUtils.isNotEmpty(relDemandBos)) {
                        existIrBids.addAll(relDemandBos.stream().map(RelDemandBo::getRelDemandBid).collect(Collectors.toSet()));
                    }
                }));
        // 如果移除后还有其他模块 关联了IR需求bid,那就不能移除RR-IR的关系数据
        removeIrBids.removeIf(existIrBids::contains);
        if (CollectionUtils.isNotEmpty(removeIrBids)) {
            // 移除RR-IR关系数据
            objectModelCrudI.deleteRel(TranscendModel.RELATION_RR_IR.getCode(), rrBid, Lists.newArrayList(removeIrBids));
        }
        // 更新RR或者IR需求上 领域/模块/责任人/主导领域等属性
        updateDomainField(spaceAppBid, rrBid, linkedDataList);
        // 通知流程变更责任人
        notifyFlow(spaceBid, spaceAppBid, linkedDataList, rrBid, null, null);
        return true;
    }

    private void updateDomainField(String spaceAppBid, String rrBid, List<MObject> linkedDataList) {
        MSpaceAppData mSpaceAppData = new MSpaceAppData();
        Set<String> areaSet = Sets.newHashSet();
        Set<String> areaManagerSet = Sets.newHashSet();
        Set<String> moduleSet = Sets.newHashSet();
        Set<String> moduleManagerSet = Sets.newHashSet();
        AtomicReference<String> leaderDomain = new AtomicReference<>();
        linkedDataList.forEach(e -> {
            String domainModelCode = (String) e.get(DemandManagementEnum.DOMAIN_MODEL_CODE.getCode());
            String manager = (String) e.get(DemandManagementEnum.DOMAIN_LEADER.getCode());
            String domainBid = (String) e.get(DemandManagementEnum.DOMAIN_BID.getCode());
            Integer isLeadDomain = (Integer) e.get(DemandManagementEnum.IS_LEAD_DOMAIN.getCode());
            if (TranscendModel.DOMAIN.getCode().equals(domainModelCode)) {
                areaSet.add(domainBid);
                areaManagerSet.add(manager);
                if (Integer.valueOf(1).equals(isLeadDomain)) {
                    leaderDomain.set(domainBid);
                }
            }
            if (TranscendModel.MODULE.getCode().equals(domainModelCode)) {
                moduleSet.add(domainBid);
                moduleManagerSet.add(manager);
            }
        });
        //RR 不需要设置领域
        if (!demandNotIntoProductArea.contains(spaceAppBid)) {
            mSpaceAppData.put(DemandManagementEnum.PRODUCT_AREA.getCode(), areaSet);
        }
        //RR 需要更新模块人 模块责任人moduleResponsiblePerson    取的是personResponsible责任人
        setRRModulePersion(spaceAppBid, moduleSet, mSpaceAppData);
        mSpaceAppData.put(DemandManagementEnum.ALL_FIELDS.getCode(), areaSet);
        mSpaceAppData.put(DemandManagementEnum.PRODUCT_MANAGER.getCode(), areaManagerSet);
        mSpaceAppData.put(DemandManagementEnum.BELONG_MODULE.getCode(), moduleSet);
        mSpaceAppData.put(DemandManagementEnum.PERSON_RESPONSIBLE.getCode(), moduleManagerSet);
        mSpaceAppData.put(DemandManagementEnum.ONWENR.getCode(), leaderDomain.get());
        iBaseApmSpaceAppDataDrivenService.updatePartialContent(spaceAppBid, rrBid, mSpaceAppData);
    }

    @Override
    public Boolean updateBelongDemandLifecycle(String modelCode, String spaceAppBid, String instanceBid, String demandLifecycle) {
        QueryCondition queryCondition = new QueryCondition();
        QueryWrapper repeat = new QueryWrapper();
        repeat.eq(DemandManagementConstant.DEMAND_MANAGEMENT_REPEAT, instanceBid);
        queryCondition.setQueries(QueryWrapper.buildSqlQo(repeat));
        List<MSpaceAppData> list = iBaseApmSpaceAppDataDrivenService.list(spaceAppBid, queryCondition);
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        List<BatchUpdateBO<MSpaceAppData>> batchUpdateBoList = Lists.newArrayList();
        list.forEach(mSpaceAppData -> {
            mSpaceAppData.put(DemandManagementConstant.DEMAND_MANAGEMENT_LIFE_CYCLE, demandLifecycle);
            QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
            queryWrapper.setProperty(DataBaseConstant.COLUMN_BID);
            queryWrapper.setCondition("=");
            queryWrapper.setValue(mSpaceAppData.getBid());
            BatchUpdateBO<MSpaceAppData> batchUpdateBO = new BatchUpdateBO<>();
            batchUpdateBO.setBaseData(mSpaceAppData);
            batchUpdateBO.setWrappers(Collections.singletonList(queryWrapper));
            batchUpdateBoList.add(batchUpdateBO);
        });
        List<String> bids = list.stream().map(MSpaceAppData::getBid).collect(Collectors.toList());
        objectModelCrudI.batchUpdateByQueryWrapper(modelCode, batchUpdateBoList, false);
        //更新流程状态
        runtimeService.copyNodeState(modelCode, instanceBid, bids);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Boolean addIR(String spaceBid, String spaceAppBid, IRAddAndRelateAo param) {
        String irModelCode = TranscendModel.IR.getCode();
        ApmSpaceApp irApp = apmSpaceAppService.getBySpaceBidAndModelCode(param.getRelationAddParam().getSpaceBid(), irModelCode);
        // 新增IR实例 （这里如果直接走关系的新增会拿不到新增之后的实例数据，所以是先新增IR实例再通过选取的形式把关系挂上去）
        MSpaceAppData irData = param.getRelationAddParam().getTargetMObjects().get(0);
        MSpaceAppData irNewData = iBaseApmSpaceAppDataDrivenService.add(irApp.getBid(), irData, null);
        param.getRelationAddParam().getTargetMObjects().remove(0);
        param.getRelationAddParam().getTargetMObjects().add(irNewData);
        // 新增RR与IR的关系实例
        Boolean addResult = apmSpaceAppRelationDataDrivenService.add(param.getRelationAddParam());
        // 如果添加成功，需要把IR的编码更新到领域组件实例中
        if (Boolean.TRUE.equals(addResult)) {
            String domainBid = param.getDomainBid();
            String domainComponentModelCode = TranscendModel.DOMAIN_COMPONENT.getCode();
            MObject domainComponentIns = objectModelCrudI.getByBid(domainComponentModelCode, domainBid);
            List<RelDemandBo> relDemandBos =
                    JSON.parseObject(JSON.toJSONString(domainComponentIns.getOrDefault(REL_DEMAND, new JSONArray())), new TypeReference<List<RelDemandBo>>() {
                    });
            MSpaceAppData mSpaceAppData = new MSpaceAppData();
            RelDemandBo relDemandBo = new RelDemandBo()
                    .setSpaceBid(spaceBid)
                    .setSpaceAppBid(irApp.getBid())
                    .setRelDemandCode(String.valueOf(irNewData.get(REQUIREMENT_CODING)))
                    .setRelDemandBid(String.valueOf(irNewData.get(BID.getCode())));
            relDemandBos.add(relDemandBo);
            mSpaceAppData.put(REL_DEMAND, relDemandBos);
            objectModelCrudI.updateByBid(domainComponentModelCode, domainBid, mSpaceAppData);
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean relateIR(String spaceBid, String spaceAppBid, IRAddAndRelateAo param) {
        // 1. 先给领域组件实例挂上新关联的IR
        String irModelCode = TranscendModel.IR.getCode();
        ApmSpaceApp irApp = apmSpaceAppService.getBySpaceBidAndModelCode(param.getRelationAddParam().getSpaceBid(), irModelCode);
        MObject domainIns = objectModelCrudI.getByBid(TranscendModel.DOMAIN_COMPONENT.getCode(), param.getDomainBid());
        List<RelDemandBo> relDemandBos =
                JSON.parseObject(JSON.toJSONString(domainIns.getOrDefault(REL_DEMAND, new JSONArray())), new TypeReference<List<RelDemandBo>>() {
                });
        List<String> existIrBids = relDemandBos.stream().map(RelDemandBo::getRelDemandBid).collect(Collectors.toList());
        List<MSpaceAppData> targetMObjects = param.getRelationAddParam().getTargetMObjects();
        targetMObjects.stream()
                .filter(data -> !existIrBids.contains(data.getBid()))
                .map(targetMObject -> new RelDemandBo()
                        .setSpaceBid(spaceBid)
                        .setSpaceAppBid(irApp.getBid())
                        .setRelDemandCode(String.valueOf(targetMObject.get(REQUIREMENT_CODING)))
                        .setRelDemandBid(String.valueOf(targetMObject.get(BID.getCode())))
                )
                .forEach(relDemandBos::add);
        domainIns.put(REL_DEMAND, relDemandBos);
        objectModelCrudI.updateByBid(TranscendModel.DOMAIN_COMPONENT.getCode(), param.getDomainBid(), domainIns);
        // 2. 给RR-IR挂上关系
        // 先判断是否当前RR - IR已经存在关系
        RelationMObject relationMObject = RelationMObject.builder()
                .relationModelCode(param.getRelationAddParam().getRelationModelCode())
                .sourceBid(param.getRelationAddParam().getSourceBid())
                .build();
        List<MObject> rrAndIrRel = objectModelCrudI.listOnlyRelationMObjects(relationMObject);
        // 先查询rr下面所有的ir关系
        if (CollectionUtils.isNotEmpty(rrAndIrRel)) {
            // 如果不为空，判断是否已经存在关系
            List<String> existTargetBids = rrAndIrRel.stream()
                    .map(rMObj -> String.valueOf(rMObj.get(TARGET_BID.getCode())))
                    .collect(Collectors.toList());
            List<MSpaceAppData> addRelTargetMObjs = targetMObjects
                    .stream()
                    .filter(targetMObj -> !existTargetBids.contains(targetMObj.getBid()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(addRelTargetMObjs)) {
                // 如果存在未关联的ir，则添加关系
                param.getRelationAddParam().setTargetMObjects(addRelTargetMObjs);
                apmSpaceAppRelationDataDrivenService.add(param.getRelationAddParam());
            }
        } else {
            // 如果为空，则直接添加关系
            apmSpaceAppRelationDataDrivenService.add(param.getRelationAddParam());
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeIR(String spaceBid, String spaceAppBid, IRRemAo param) {
        // 1. 先移除关联的IR
        MObject domainIns = objectModelCrudI.getByBid(TranscendModel.DOMAIN_COMPONENT.getCode(), param.getDomainBid());
        List<RelDemandBo> relDemandBos = JSON.parseObject(
                JSON.toJSONString(domainIns.getOrDefault(REL_DEMAND, new JSONArray())),
                new TypeReference<List<RelDemandBo>>() {
                }
        );
        Set<String> removeIrBids = Sets.newHashSet();
        removeIrBids.add(param.getIrBid());
        relDemandBos.removeIf(relDemandBo -> relDemandBo.getRelDemandBid().equals(param.getIrBid()));
        domainIns.put(REL_DEMAND, relDemandBos);
        objectModelCrudI.updateByBid(TranscendModel.DOMAIN_COMPONENT.getCode(), param.getDomainBid(), domainIns);
        // 2. 判断是否还有其他的领域实例关联了该IR，如果没有，则删除RR-IR的关系
        List<MObject> linkedDataList = queryRelData(spaceBid, param.getRrBid());
        Set<String> existIrBids = Sets.newHashSet();
        linkedDataList.forEach(linkedData -> Optional.ofNullable(linkedData.get(DemandManagementEnum.REL_DEMAND.getCode()))
                .ifPresent(irJson -> {
                    List<RelDemandBo> relDemandBoList = JSON.parseObject(JSON.toJSONString(irJson), new TypeReference<List<RelDemandBo>>() {
                    });
                    if (CollectionUtils.isNotEmpty(relDemandBoList)) {
                        existIrBids.addAll(relDemandBoList.stream().map(RelDemandBo::getRelDemandBid).collect(Collectors.toSet()));
                    }
                }));
        // 如果移除后还有其他模块 关联了IR需求bid,那就不能移除RR-IR的关系数据
        removeIrBids.removeIf(irBid -> existIrBids.contains(param.getIrBid()));
        if (CollectionUtils.isNotEmpty(removeIrBids)) {
            // 移除RR-IR关系数据
            objectModelCrudI.deleteRel(TranscendModel.RELATION_RR_IR.getCode(), param.getRrBid(), Lists.newArrayList(removeIrBids));
        }
        return Boolean.TRUE;
    }

    @Override
    public List<MObject> selectDomain(String spaceBid, String spaceAppBid, String rrBid, Integer type, SelectAo selectAo, String rrDomainRoleCode, String rrProductRoleCode) {
        // 需求的实例数据
        MSpaceAppData appData = new MSpaceAppData();
        Set<String> domainBids = Sets.newHashSet();
        Set<String> domainUsers = Sets.newHashSet();
        Set<String> muduleBids = Sets.newHashSet();
        Set<String> productUsers = Sets.newHashSet();
        List<MObject> result = Lists.newArrayList();
        //领域组件
        String modelcode = TranscendModel.DOMAIN_COMPONENT.getCode();
        List<MSpaceAppData> mSpaceAppDataList = Lists.newArrayList();
        List<MObject> mObjectList = Lists.newArrayList();
        MSpaceAppData mSpaceAppData = null;
        List<List<String>> bidArray = new ArrayList<>();
        //选取的结果
        List<SelectVo> flatList = TranscendTools.flattenTreeIterative(selectAo.getNameList(), SelectVo::getChildren);
        // 查询实例已经存在的领域组件
        List<MObject> exitList = queryRelData(spaceBid, rrBid);
        //将选取的结果扁平转换为map
        Map<String, SelectVo> nameFlatMap = flatList.stream().collect(Collectors.toMap(SelectVo::getBid, Function.identity(), (k1, k2) -> k2));
        String leadDomainBid = exitList.stream().filter(e -> (e.get(DemandManagementEnum.IS_LEAD_DOMAIN.getCode()) != null && Integer.valueOf(1).equals(e.get(DemandManagementEnum.IS_LEAD_DOMAIN.getCode()))))
                .map(e -> (String) e.get(DemandManagementEnum.DOMAIN_BID.getCode())).findFirst().orElse("");
        Map<String, String> exitMap = exitList.stream().collect(Collectors.toMap(e -> (String) e.get(DemandManagementEnum.DOMAIN_BID.getCode()), e -> e.getBid(), (k1, k2) -> k2));
        boolean hasleadDomain = exitList.stream().filter(e -> (e.get(DemandManagementEnum.IS_LEAD_DOMAIN.getCode()) != null && Integer.valueOf(1).equals(e.get(DemandManagementEnum.IS_LEAD_DOMAIN.getCode())))).collect(Collectors.toList()).size() > 0;
        for (int i = 0; i < selectAo.getSelectedList().size(); i++) {
            List<String> selectedList = selectAo.getSelectedList().get(i);
            bidArray.add(new ArrayList<>());
            for (int j = 0; j < selectedList.size(); j++) {
                String selectBid = selectedList.get(j);
                String bid = exitMap.get(selectBid);
                if (StringUtils.isBlank(bid)) {
                    bid = SnowflakeIdWorker.nextIdStr();
                }
                bidArray.get(i).add(bid);
                mSpaceAppData = new MSpaceAppData();
                mSpaceAppData.setBid(bid);
                if (i == 0 && j == 0 && !hasleadDomain) {
                    mSpaceAppData.put(DemandManagementEnum.IS_LEAD_DOMAIN.getCode(), 1);
                    leadDomainBid = selectBid;
                }
                if (nameFlatMap.get(selectBid) != null) {
                    if (TranscendModel.DOMAIN.getCode().equals(Optional.ofNullable(nameFlatMap.get(selectBid)).map(value -> value.getModelCode()).get())) {
                        // 领域
                        domainBids.add(selectBid);
                        domainUsers.add(Optional.ofNullable(nameFlatMap.get(selectBid)).map(value -> value.getDomainLeader()).orElse(""));
                    } else if (TranscendModel.MODULE.getCode().equals(Optional.ofNullable(nameFlatMap.get(selectBid)).map(value -> value.getModelCode()).get())) {
                        // 模块
                        muduleBids.add(selectBid);
                        productUsers.add(Optional.ofNullable(nameFlatMap.get(selectBid)).map(value -> value.getDomainLeader()).orElse(""));
                    }
                }
                mSpaceAppData.put(TranscendModelBaseFields.DATA_BID, bid);
                mSpaceAppData.put(DemandManagementEnum.RR_BID.getCode(), rrBid);
                mSpaceAppData.setModelCode(modelcode);
                mSpaceAppData.setSpaceBid(spaceBid);
                //领域ID
                mSpaceAppData.put(DemandManagementEnum.DOMAIN_BID.getCode(), selectBid);
                //领域SE
                mSpaceAppData.put(DemandManagementEnum.DOMAIN_SE.getCode(), Optional.ofNullable(nameFlatMap.get(selectBid)).map(value -> value.getDomainSe()));
                // 责任人
                mSpaceAppData.put(DemandManagementEnum.DOMAIN_LEADER.getCode(), Optional.ofNullable(nameFlatMap.get(selectBid)).map(value -> value.getDomainLeader()).orElse(""));
                mSpaceAppData.put(DemandManagementEnum.DOMAIN_APP_BID.getCode(), Optional.ofNullable(nameFlatMap.get(selectBid)).map(value -> value.getSpaceAppBid()).orElse(""));
                mSpaceAppData.setName(Optional.ofNullable(nameFlatMap.get(selectBid)).map(value -> value.getName()).orElse(""));
                mSpaceAppData.put(DemandManagementEnum.DOMAIN_MODEL_CODE.getCode(), Optional.ofNullable(nameFlatMap.get(selectBid)).map(value -> value.getModelCode()).orElse(""));
                int finalJ = j;
                String parentBid = Optional.ofNullable(bidArray.get(i)).filter(list -> finalJ - 1 >= 0 && finalJ - 1 < list.size())
                        .map(list -> list.get(finalJ - 1)).orElse("0");
                mSpaceAppData.put(DemandManagementEnum.PARENT_BID.getCode(), parentBid);
                // 已存在领域BID 不添加数据
                if (!exitMap.containsKey(selectBid)) {
                    mSpaceAppDataList.add(mSpaceAppData);
                    exitMap.put(selectBid, bid);
                }
                appData.setBid(rrBid);
                // 主导领域
                appData.put(DemandManagementEnum.ONWENR.getCode(), leadDomainBid);
                // 领域负责人
                appData.put(DemandManagementEnum.PRODUCT_MANAGER.getCode(), domainUsers);
                // 模块IDS
                appData.put(DemandManagementEnum.BELONG_MODULE.getCode(), muduleBids);
                // 产品负责人
                appData.put(DemandManagementEnum.PERSON_RESPONSIBLE.getCode(), productUsers);
            }
        }
        mSpaceAppDataList.stream().forEach(e -> {
            result.add(objectModelCrudI.add(modelcode, e));
            mObjectList.add(e);
        });
        for (int m = 0; m < exitList.size(); m++) {
            MObject mObject = exitList.get(m);
            if (TranscendModel.DOMAIN.getCode().equals(mObject.get(DemandManagementEnum.DOMAIN_MODEL_CODE.getCode()))) {
                // 领域
                domainBids.add((String) mObject.get(DemandManagementEnum.DOMAIN_BID.getCode()));
                domainUsers.add((String) mObject.get(DemandManagementEnum.DOMAIN_LEADER.getCode()));
            } else if (TranscendModel.MODULE.getCode().equals(mObject.get(DemandManagementEnum.DOMAIN_MODEL_CODE.getCode()))) {
                // 模块
                muduleBids.add((String) mObject.get(DemandManagementEnum.DOMAIN_BID.getCode()));
                productUsers.add((String) mObject.get(DemandManagementEnum.DOMAIN_LEADER.getCode()));
            }
        }
        try {
            //RR 需要更新模块人 模块责任人moduleResponsiblePerson    取的是personResponsible责任人
            setRRModulePersion(spaceAppBid, muduleBids, appData);
            appData.put(DemandManagementEnum.ALL_FIELDS.getCode(), domainBids);
            appData.put(DemandManagementEnum.PRODUCT_AREA.getCode(), domainBids);
            appData.put(DemandManagementEnum.LEADING_OVERALL_FIELD.getCode(), domainBids);
            appData.put("isRecordLog", false);
            iBaseApmSpaceAppDataDrivenService.updatePartialContent(spaceAppBid, rrBid, appData);
        } catch (Exception e) {
            // 不处理异常
        }
        mObjectList.addAll(exitList);
        // 通知流程责任人变动
        notifyFlow(spaceBid, spaceAppBid, mObjectList, rrBid, rrDomainRoleCode, rrProductRoleCode);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MObject> selectAllDomain(String spaceBid, String spaceAppBid, String rrBid, SelectAo selectAo, String rrDomainRoleCode, String rrProductRoleCode) {
        //校验编辑权限
        checkEditPermission(spaceBid, spaceAppBid, rrBid);
        // 需求的实例数据
        MSpaceAppData appData = new MSpaceAppData();
        Set<String> domainBids = Sets.newHashSet();
        Set<String> domainUsers = Sets.newHashSet();
        Set<String> muduleBids = Sets.newHashSet();
        Set<String> productUsers = Sets.newHashSet();
        List<MObject> result = Lists.newArrayList();
        //领域组件
        String modelcode = TranscendModel.DOMAIN_COMPONENT.getCode();
        List<MSpaceAppData> mSpaceAppDataList = Lists.newArrayList();
        List<MObject> mObjectList = Lists.newArrayList();
        MSpaceAppData mSpaceAppData = null;
        List<List<String>> bidArray = new ArrayList<>();
        //选取的结果
        List<SelectVo> flatList = TranscendTools.flattenTreeIterative(selectAo.getNameList(), SelectVo::getChildren);
        List<String> selectDomainBids = selectAo.getSelectedList().stream().flatMap(List::stream).collect(Collectors.toList());
        // 查询实例已经存在的领域组件
        List<MObject> exitList = queryRelData(spaceBid, rrBid);
        /*if (CollectionUtils.isNotEmpty(exitList)) {
            // 需要删除的领域组件
            deleteDemandManagement(rrBid, exitList, selectDomainBids);
        }*/
        //将选取的结果扁平转换为map
        Map<String, SelectVo> nameFlatMap = flatList.stream().collect(Collectors.toMap(SelectVo::getBid, Function.identity(), (k1, k2) -> k2));
        String leadDomainBid = exitList.stream().filter(e -> (e.get(DemandManagementEnum.IS_LEAD_DOMAIN.getCode()) != null && Integer.valueOf(1).equals(e.get(DemandManagementEnum.IS_LEAD_DOMAIN.getCode()))))
                .map(e -> (String) e.get(DemandManagementEnum.DOMAIN_BID.getCode())).findFirst().orElse("");
        List<String> exitDomainComponent = new ArrayList<>();
        boolean hasLeadDomain = selectDomainBids.contains(leadDomainBid);
        Map<String, String> bidMap = new HashMap<>();
        for (int i = 0; i < selectAo.getSelectedList().size(); i++) {
            List<String> selectedList = selectAo.getSelectedList().get(i);
            for (int j = 0; j < selectedList.size(); j++) {
                String selectBid = selectedList.get(j);
                String bid = SnowflakeIdWorker.nextIdStr();
                mSpaceAppData = new MSpaceAppData();
                mSpaceAppData.setBid(bid);
                if (hasLeadDomain && selectBid.equals(leadDomainBid)) {
                    mSpaceAppData.put(DemandManagementEnum.IS_LEAD_DOMAIN.getCode(), 1);
                } else if (i == 0 && j == 0 && !hasLeadDomain) {
                    mSpaceAppData.put(DemandManagementEnum.IS_LEAD_DOMAIN.getCode(), 1);
                    leadDomainBid = selectBid;
                }
                if (nameFlatMap.get(selectBid) != null) {
                    if (TranscendModel.DOMAIN.getCode().equals(Optional.ofNullable(nameFlatMap.get(selectBid)).map(value -> value.getModelCode()).get())) {
                        // 领域
                        domainBids.add(selectBid);
                        domainUsers.add(Optional.ofNullable(nameFlatMap.get(selectBid)).map(value -> value.getDomainLeader()).orElse(""));
                    } else if (TranscendModel.MODULE.getCode().equals(Optional.ofNullable(nameFlatMap.get(selectBid)).map(value -> value.getModelCode()).get())) {
                        // 模块
                        muduleBids.add(selectBid);
                        productUsers.add(Optional.ofNullable(nameFlatMap.get(selectBid)).map(value -> value.getDomainLeader()).orElse(""));
                    }
                }
                mSpaceAppData.put(TranscendModelBaseFields.DATA_BID, bid);
                mSpaceAppData.put(DemandManagementEnum.RR_BID.getCode(), rrBid);
                mSpaceAppData.setModelCode(modelcode);
                mSpaceAppData.setSpaceBid(spaceBid);
                //领域ID
                mSpaceAppData.put(DemandManagementEnum.DOMAIN_BID.getCode(), selectBid);
                //领域SE
                mSpaceAppData.put(DemandManagementEnum.DOMAIN_SE.getCode(), Optional.ofNullable(nameFlatMap.get(selectBid)).map(value -> value.getDomainSe()));
                // 责任人
                mSpaceAppData.put(DemandManagementEnum.DOMAIN_LEADER.getCode(), Optional.ofNullable(nameFlatMap.get(selectBid)).map(value -> value.getDomainLeader()).orElse(""));
                mSpaceAppData.put(DemandManagementEnum.DOMAIN_APP_BID.getCode(), Optional.ofNullable(nameFlatMap.get(selectBid)).map(value -> value.getSpaceAppBid()).orElse(""));
                mSpaceAppData.setName(Optional.ofNullable(nameFlatMap.get(selectBid)).map(value -> value.getName()).orElse(""));
                mSpaceAppData.put(DemandManagementEnum.DOMAIN_MODEL_CODE.getCode(), Optional.ofNullable(nameFlatMap.get(selectBid)).map(value -> value.getModelCode()).orElse(""));
                String parentBid = j >= 1 ? bidMap.getOrDefault(selectedList.get(j - 1), "0") : "0";
                mSpaceAppData.put(DemandManagementEnum.PARENT_BID.getCode(), parentBid);
                // 已存在领域BID 不添加数据
                if (!exitDomainComponent.contains(parentBid + "_" + selectBid)) {
                    mSpaceAppDataList.add(mSpaceAppData);
                    exitDomainComponent.add(parentBid + "_" + selectBid);
                    bidMap.put(selectBid, bid);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(exitList)) {
            objectModelCrudI.batchLogicalDeleteByBids(TranscendModel.DOMAIN_COMPONENT.getCode(), exitList.stream().map(MObject::getBid).collect(Collectors.toList()));
        }
        mSpaceAppDataList.stream().forEach(e -> {
            result.add(objectModelCrudI.add(modelcode, e));
            mObjectList.add(e);
        });
        for (int m = 0; m < exitList.size(); m++) {
            MObject mObject = exitList.get(m);
            if (TranscendModel.DOMAIN.getCode().equals(mObject.get(DemandManagementEnum.DOMAIN_MODEL_CODE.getCode()))) {
                // 领域
                domainBids.add((String) mObject.get(DemandManagementEnum.DOMAIN_BID.getCode()));
                domainUsers.add((String) mObject.get(DemandManagementEnum.DOMAIN_LEADER.getCode()));
            } else if (TranscendModel.MODULE.getCode().equals(mObject.get(DemandManagementEnum.DOMAIN_MODEL_CODE.getCode()))) {
                // 模块
                muduleBids.add((String) mObject.get(DemandManagementEnum.DOMAIN_BID.getCode()));
                productUsers.add((String) mObject.get(DemandManagementEnum.DOMAIN_LEADER.getCode()));
            }
        }
        try {
            appData.setBid(rrBid);
            // 主导领域
            appData.put(DemandManagementEnum.DOMINANT_DOMAIN.getCode(), leadDomainBid);
            ;
            appData.put("isRecordLog", false);
            iBaseApmSpaceAppDataDrivenService.updatePartialContent(spaceAppBid, rrBid, appData);
        } catch (Exception e) {
            // 不处理异常
            log.info("更新主导领域失败：【{}】", rrBid);
        }
       /* try {
            //RR 需要更新模块人 模块责任人moduleResponsiblePerson    取的是personResponsible责任人
            setRRModulePersion(spaceAppBid,muduleBids,appData);
            appData.put(DemandManagementEnum.ALL_FIELDS.getCode(),domainBids);
            appData.put(DemandManagementEnum.PRODUCT_AREA.getCode(),domainBids);
            appData.put(DemandManagementEnum.ALL_OVERALL_FIELDS.getCode(),domainBids);
            appData.put("isRecordLog",false);
            iBaseApmSpaceAppDataDrivenService.updatePartialContent(spaceAppBid,rrBid,appData);
        } catch (Exception e) {
            // 不处理异常
        }
        mObjectList.addAll(exitList);
        // 通知流程责任人变动
        notifyFlow(spaceBid, spaceAppBid, mObjectList, rrBid, rrDomainRoleCode, rrProductRoleCode);*/
        return result;
    }

    @Override
    public Boolean copyDomain(String spaceBid, String spaceAppBid, String rrBid, MObjectCopyAo sourceParam) {
        //如果已经有领域数据，则不进行复制
        if (CollectionUtils.isNotEmpty(queryRelData(spaceBid, rrBid))) {
            return true;
        }
        List<MObject> linkedDataList = queryRelData(sourceParam.getSpaceBid(), sourceParam.getSourceBid());
        if (CollectionUtils.isEmpty(linkedDataList)) {
            return true;
        }
        String modelcode = TranscendModel.DOMAIN_COMPONENT.getCode();
        List<MSpaceAppData> mSpaceAppDataList = Lists.newArrayList();
        for (MObject mObject : linkedDataList) {
            MSpaceAppData mSpaceAppData = new MSpaceAppData();
            String bid = SnowflakeIdWorker.nextIdStr();
            mSpaceAppData.setBid(bid);
            mSpaceAppData.put(DemandManagementEnum.IS_LEAD_DOMAIN.getCode(), mObject.get(DemandManagementEnum.IS_LEAD_DOMAIN.getCode()));
            mSpaceAppData.put(TranscendModelBaseFields.DATA_BID, bid);
            mSpaceAppData.put(DemandManagementEnum.RR_BID.getCode(), rrBid);
            mSpaceAppData.setModelCode(modelcode);
            mSpaceAppData.setSpaceBid(sourceParam.getSpaceBid());
            //领域ID
            mSpaceAppData.put(DemandManagementEnum.DOMAIN_BID.getCode(), mObject.get(DemandManagementEnum.DOMAIN_BID.getCode()));
            //领域SE
            mSpaceAppData.put(DemandManagementEnum.DOMAIN_SE.getCode(), mObject.get(DemandManagementEnum.DOMAIN_SE.getCode()));
            // 责任人
            mSpaceAppData.put(DemandManagementEnum.DOMAIN_LEADER.getCode(), mObject.get(DemandManagementEnum.DOMAIN_LEADER.getCode()));
            mSpaceAppData.put(DemandManagementEnum.DOMAIN_APP_BID.getCode(), mObject.get(DemandManagementEnum.DOMAIN_APP_BID.getCode()));
            mSpaceAppData.setName(mObject.getName());
            mSpaceAppData.put(DemandManagementEnum.DOMAIN_MODEL_CODE.getCode(), mObject.get(DemandManagementEnum.DOMAIN_MODEL_CODE.getCode()));
            mSpaceAppData.put(DemandManagementEnum.PARENT_BID.getCode(), mObject.get(DemandManagementEnum.PARENT_BID.getCode()));
            mSpaceAppDataList.add(mSpaceAppData);
        }
        mSpaceAppDataList.stream().forEach(e -> {
            objectModelCrudI.add(modelcode, e);
        });
        return true;
    }

    /**
     * 查询需求 IR/SR 树结构数据，返回包含 IR 节点及其子节点 SR 的树形结构。
     *
     * @param projectName 项目名称，不能为空
     * @return List<MObjectTree> IR/SR 树结构列表
     */
    @Override
    public List<MObjectTree> searchDemandIrsrTree(String projectName) {

        // 1. 参数校验：项目名不能为空
        if (StringUtils.isBlank(projectName)) {
            log.warn("项目名称为空，无法查询 IR/SR 树");
            return Collections.emptyList();
        }

        // 2. 查询项目对象
        String projectModelCode = TranscendModel.PROJECT.getCode();
        QueryWrapper projectQuery = new QueryWrapper().eq(ObjectEnum.NAME.getCode(), projectName);
        List<MObject> projectDataList = objectModelCrudI.list(projectModelCode, QueryWrapper.buildSqlQo(projectQuery));

        if (CollectionUtils.isEmpty(projectDataList)) {
            log.info("未找到项目 [{}] 对应的数据", projectName);
            return Collections.emptyList();
        }

        String projectBid = projectDataList.get(0).getBid();

        // 3. 查询 IR 列表（A00）
        List<MObject> irList = queryObjectListBySpaceBid(TranscendModel.IR.getCode(), projectBid);
        if (CollectionUtils.isEmpty(irList)) {
            log.info("项目 [{}] 下无 IR 数据", projectName);
            return Collections.emptyList();
        }

        // 4. 构建 IR 树节点列表
        List<MObjectTree> irTreeList = convertToMObjectTree(irList);

        // 5. 获取 IR -> SR 的关系映射
        List<String> irBids = irList.stream()
                .map(MObject::getBid)
                .collect(Collectors.toList());

        List<MObject> ir2SrRelationList = queryObjectListBySourceBidList(TranscendModel.RELATION_IR_SR.getCode(), irBids);
        if (CollectionUtils.isEmpty(ir2SrRelationList)) {
            log.info("IR 和 SR 之间无关联关系");
            return irTreeList;
        }

        // 6. 构建 IR -> SR 映射关系
        Map<String, List<String>> irBidToSrBidsMap = ir2SrRelationList.stream()
                .collect(Collectors.groupingBy(
                        e -> e.get(RelationEnum.SOURCE_BID.getCode()).toString(),
                        Collectors.mapping(e -> e.get(RelationEnum.TARGET_BID.getCode()).toString(), Collectors.toList())
                ));

        // 7. 查询所有 SR 数据
        List<String> srBidList = ir2SrRelationList.stream()
                .map(e -> e.get(RelationEnum.TARGET_BID.getCode()).toString())
                .distinct()
                .collect(Collectors.toList());

        List<MObject> srList = queryObjectListByBidList(TranscendModel.SR.getCode(), srBidList);
        if (CollectionUtils.isEmpty(srList)) {
            log.info("未找到对应的 SR 数据");
            return irTreeList;
        }

        // 8. 构建 SR bid -> MObject 映射
        Map<String, MObject> srBidToMObjectMap = srList.stream()
                .collect(Collectors.toMap(MObject::getBid, Function.identity()));

        // 9. 组装树结构：为每个 IR 添加其下挂的 SR 子节点
        irTreeList.forEach(irNode -> {
            List<String> relatedSrBids = irBidToSrBidsMap.get(irNode.getBid());
            if (CollectionUtils.isNotEmpty(relatedSrBids)) {
                List<MObjectTree> srChildren = relatedSrBids.stream()
                        .map(srBid -> convertSingleToMObjectTree(srBidToMObjectMap.get(srBid)))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                irNode.setChildren(srChildren);
            }
        });

        return irTreeList;
    }

// ===================== 辅助方法 =====================

    /**
     * 将 MObject 列表转换为 MObjectTree 列表
     */
    private List<MObjectTree> convertToMObjectTree(List<MObject> objectList) {
        return objectList.stream()
                .map(this::convertSingleToMObjectTree)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 单个 MObject 转换为 MObjectTree，只复制部分字段
     */
    private MObjectTree convertSingleToMObjectTree(MObject obj) {
        if (obj == null) {
            return null;
        }

        MObjectTree tree = new MObjectTree();
        tree.put(BaseDataEnum.BID.getCode(), obj.get(BaseDataEnum.BID.getCode()));
        tree.put(ObjectEnum.CODING.getCode(), obj.get(ObjectEnum.CODING.getCode()));
        tree.put(ObjectEnum.NAME.getCode(), obj.get(ObjectEnum.NAME.getCode()));
        tree.put("description", getDescription(obj)); // 自定义描述字段
        return tree;
    }

    /**
     * 安全获取 description 字段，兼容不同对象类型
     */
    private Object getDescription(MObject obj) {
        Object desc = obj.get("irDescription");
        if (desc == null) {
            desc = obj.get("srDescription");
        }
        return desc;
    }

    private List<MObject> queryObjectListBySpaceBid(String modelCode, String projectBid) {
        QueryWrapper queryWrapper = new QueryWrapper().eq(ObjectEnum.SPACE_BID.getCode(),
                projectBid
        );
        return objectModelCrudI.list(modelCode,
                QueryWrapper.buildSqlQo(queryWrapper));
    }

    private List<MObject> queryObjectListByBidList(String modelCode, List<String> bidList) {
        QueryWrapper queryWrapper = new QueryWrapper().in(BaseDataEnum.BID.getCode(),
                bidList
        );
        return objectModelCrudI.list(modelCode,
                QueryWrapper.buildSqlQo(queryWrapper));
    }
    private List<MObject> queryObjectListBySourceBidList(String modelCode, List<String> bidList) {
        QueryWrapper queryWrapper = new QueryWrapper().in(RelationEnum.SOURCE_BID.getCode(),
                bidList
        );
        return objectModelCrudI.list(modelCode,
                QueryWrapper.buildSqlQo(queryWrapper));
    }

    private void checkEditPermission(String spaceBid, String spaceAppBid, String rrBid) {
        //判断是否有编辑权限
        PermissionCheckDto permissionCheckDto = new PermissionCheckDto();
        permissionCheckDto.setSpaceBid(spaceBid);
        permissionCheckDto.setInstanceBid(rrBid);
        permissionCheckDto.setSpaceAppBid(spaceAppBid);
        permissionCheckDto.setOperatorCode(OperatorEnum.EDIT.getCode());
        if (!permissionCheckService.checkSpaceAppPermssion(permissionCheckDto)) {
            throw new BusinessException("没有编辑权限");
        }
    }

    private void deleteDemandManagement(String rrBid, List<MObject> exitList, List<String> selectDomainBids) {
        //需要删除的实例
        List<MObject> needRemoveList = exitList.stream().filter(m -> !selectDomainBids.contains(m.get(DemandManagementEnum.DOMAIN_BID.getCode()))).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(needRemoveList)) {
            exitList.removeIf(m -> !selectDomainBids.contains(m.get(DemandManagementEnum.DOMAIN_BID.getCode())));
            // 移除的ir bids
            Set<String> removeIrBids = Sets.newHashSet();
            needRemoveList.forEach(e -> Optional.ofNullable(e.get(DemandManagementEnum.REL_DEMAND.getCode()))
                    .ifPresent(irJson -> {
                        List<RelDemandBo> relDemandBos = JSON.parseObject(JSON.toJSONString(irJson), new TypeReference<List<RelDemandBo>>() {
                        });
                        if (CollectionUtils.isNotEmpty(relDemandBos)) {
                            removeIrBids.addAll(relDemandBos.stream().map(RelDemandBo::getRelDemandBid).collect(Collectors.toSet()));
                        }
                    }));
            // 移除领域/模块/应用
            objectModelCrudI.batchLogicalDeleteByBids(TranscendModel.DOMAIN_COMPONENT.getCode(), needRemoveList.stream().map(MObject::getBid).collect(Collectors.toList()));
            // 查询移除后的数据,对比出IR需求的bid差别,来判断是否移除RR-IR关系数据
            Set<String> existIrBids = Sets.newHashSet();
            exitList.forEach(linkedData -> Optional.ofNullable(linkedData.get(DemandManagementEnum.REL_DEMAND.getCode()))
                    .ifPresent(irJson -> {
                        List<RelDemandBo> relDemandBos = JSON.parseObject(JSON.toJSONString(irJson), new TypeReference<List<RelDemandBo>>() {
                        });
                        if (CollectionUtils.isNotEmpty(relDemandBos)) {
                            existIrBids.addAll(relDemandBos.stream().map(RelDemandBo::getRelDemandBid).collect(Collectors.toSet()));
                        }
                    }));
            // 如果移除后还有其他模块 关联了IR需求bid,那就不能移除RR-IR的关系数据
            removeIrBids.removeIf(existIrBids::contains);
            if (CollectionUtils.isNotEmpty(removeIrBids)) {
                // 移除RR-IR关系数据
                objectModelCrudI.deleteRel(TranscendModel.RELATION_RR_IR.getCode(), rrBid, Lists.newArrayList(removeIrBids));
            }
        }
    }

    /**
     * 设置RR模块责任人
     *
     * @param spaceAppBid 空间应用编号
     * @param muduleBids  模块编号
     * @param appData     更新数据
     */
    private void setRRModulePersion(String spaceAppBid, Set<String> muduleBids, MSpaceAppData appData) {
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
        if (apmSpaceApp.getModelCode().equals(TranscendModel.RR.getCode())) {
            List<String> moduleSetBids = new ArrayList<>();
            moduleSetBids.addAll(muduleBids);
            List<MObject> moudleList = objectModelCrudI.listByBids(moduleSetBids, TranscendModel.MODULE.getCode());
            Set<String> modelUsers = new HashSet<>();
            if (CollectionUtils.isNotEmpty(moudleList)) {
                for (MObject mObject : moudleList) {
                    List<String> thisModelUsers = TranscendTools.analysisPersions(mObject.get(DemandManagementEnum.MODULE_RESPONSIBLE.getCode()));
                    if (CollectionUtils.isNotEmpty(thisModelUsers)) {
                        modelUsers.add(thisModelUsers.get(0));
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(modelUsers)) {
                appData.put(DemandManagementEnum.PERSON_RESPONSIBLE.getCode(), modelUsers);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateLeadDomain(String spaceBid, String spaceAppBid, UpdateLeadDomainAo updateLeadDomainAo) {
        String rrBid = updateLeadDomainAo.getRrBid();
        String updateBid = updateLeadDomainAo.getUpdateBid();
        String domainComponentModelCode = TranscendModel.DOMAIN_COMPONENT.getCode();
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.SPACE_BID.getColumn(), spaceBid).and().eq(DemandManagementEnum.RR_BID.getColumn(), rrBid)
                .and().eq(DemandManagementEnum.IS_LEAD_DOMAIN.getColumn(), 1);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        List<MObject> list = objectModelCrudI.list(domainComponentModelCode, queryWrappers);
        if (CollectionUtils.isNotEmpty(list)) {
            MObject cancelObject = list.get(0);
            cancelObject.put(DemandManagementEnum.IS_LEAD_DOMAIN.getCode(), 0);
            objectModelCrudI.updateByBid(domainComponentModelCode, cancelObject.getBid(), cancelObject);
        }
        MObject updateObject = new MObject();
        updateObject.put(DemandManagementEnum.IS_LEAD_DOMAIN.getCode(), 1);
        objectModelCrudI.updateByBid(domainComponentModelCode, updateBid, updateObject);
        MSpaceAppData data = iBaseApmSpaceAppDataDrivenService.get(spaceAppBid, rrBid, false);
        if (data == null) {
            // IR可能还未有数据,不需要处理主导领域属性
            return true;
        }
        MObject queryObject = objectModelCrudI.getByBid(domainComponentModelCode, updateBid);
        if (queryObject != null) {
            // 更新RR或者IR需求的主导领域属性
            String domainBid = (String) queryObject.get(DemandManagementEnum.DOMAIN_BID.getCode());
            MSpaceAppData mSpaceAppData = new MSpaceAppData();
            mSpaceAppData.put(DemandManagementEnum.DOMINANT_DOMAIN.getCode(), domainBid);
            iBaseApmSpaceAppDataDrivenService.updatePartialContent(spaceAppBid, rrBid, mSpaceAppData);
        }
        return true;
    }

    @Override
    public Boolean updateResponsiblePerson(String spaceBid, String spaceAppBid, UpdateResponsibleAo ao) {
        MObject updateObject = new MObject();
        updateObject.put(DemandManagementEnum.DOMAIN_LEADER.getCode(), ao.getResponsiblePerson());
        objectModelCrudI.updateByBid(TranscendModel.DOMAIN_COMPONENT.getCode(), ao.getUpdateBid(), updateObject);
        MSpaceAppData data = iBaseApmSpaceAppDataDrivenService.get(spaceAppBid, ao.getRrBid(), false);
        if (data == null) {
            // IR可能还未有数据,不需要处理属性
            return true;
        }
        List<MObject> linkedDataList = queryRelData(spaceBid, ao.getRrBid());
        // 更新RR或IR属性
        updateDomainField(spaceAppBid, ao.getRrBid(), linkedDataList);
        // 通知到流程变动责任人
        notifyFlow(spaceBid, spaceAppBid, linkedDataList, ao.getRrBid(), null, null);
        return true;
    }

    private List<MObject> queryRelData(String spaceBid, String rrBid) {
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.SPACE_BID.getColumn(), spaceBid).and().eq(DemandManagementEnum.RR_BID.getColumn(), rrBid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        return objectModelCrudI.list(TranscendModel.DOMAIN_COMPONENT.getCode(), queryWrappers);
    }

    private void recurseTree(List<MObjectTree> tree, Map<String, MObject> linkedBidMap, String selectedBid, List<MObjectTree> returnTree) {
        if (CollectionUtils.isNotEmpty(tree)) {
            tree.forEach(e -> {
                if (e.getBid().equals(selectedBid)) {
                    returnTree.add(e);
                }
                String modelCode = e.getModelCode();
                Object domainResponsibleObj = e.get(DemandManagementEnum.DOMAIN_RESPONSIBLE.getCode());
                if (TranscendModel.DOMAIN.getCode().equals(modelCode)) {
                    // 领域责任人
                    domainResponsibleObj = e.get(DemandManagementEnum.DOMAIN_RESPONSIBLE.getCode());
                }
                if (TranscendModel.MODULE.getCode().equals(modelCode)) {
                    // 模块责任人
                    domainResponsibleObj = e.get(DemandManagementEnum.MODULE_RESPONSIBLE.getCode());
                }
                if (TranscendModel.MODULE.getCode().equals(modelCode) &&
                        linkedBidMap.containsKey(e.getBid())) {
                    e.put("disabled", true);
                } else {
                    e.put("disabled", false);
                }
                // 责任人多个取第一个
                if (domainResponsibleObj != null) {
                    if (domainResponsibleObj instanceof List) {
                        List<String> values = JSON.parseArray(JSON.toJSONString(domainResponsibleObj), String.class);
                        if (CollectionUtils.isNotEmpty(values)) {
                            e.put(DemandManagementEnum.DOMAIN_LEADER.getCode(), values.get(0));
                        }
                    } else if (domainResponsibleObj instanceof String) {
                        String person = domainResponsibleObj.toString();
                        e.put(DemandManagementEnum.DOMAIN_LEADER.getCode(), person);
                    }
                }
                if (e.getChildren() != null && e.getChildren().isEmpty()) {
                    e.setChildren(null);
                }
                recurseTree(e.getChildren(), linkedBidMap, selectedBid, returnTree);
            });
        }
    }

    /**
     * 通知流程责任人有变动
     *
     * @param spaceBid       空间bid
     * @param spaceAppBid    应用bid
     * @param linkedDataList RR需求关联的领域数据
     * @param rrBid          RR需求bid
     * @version: 1.0
     * @date: 2024/6/24 17:24
     * @author: bin.yin
     **/
    private void notifyFlow(String spaceBid, String spaceAppBid, List<MObject> linkedDataList, String rrBid, String rrDomainRoleCode, String rrProductRoleCode) {
        Set<String> domainLeader = Sets.newHashSet();
        Set<String> appLeader = Sets.newHashSet();
        Set<String> moduleLeader = Sets.newHashSet();
        List<String> domainBidList = new ArrayList<>();
        linkedDataList.forEach(e -> {
            String modelCode = (String) e.get(DemandManagementEnum.DOMAIN_MODEL_CODE.getCode());
            if (TranscendModel.DOMAIN.getCode().equals(modelCode)) {
                domainLeader.add((String) e.get(DemandManagementEnum.DOMAIN_LEADER.getCode()));
            }
            if (demandManagementProperties.getAppModelCode().equals(modelCode)) {
                appLeader.add((String) e.get(DemandManagementEnum.DOMAIN_LEADER.getCode()));
            }
            if (TranscendModel.MODULE.getCode().equals(modelCode)) {
                moduleLeader.add((String) e.get(DemandManagementEnum.DOMAIN_LEADER.getCode()));
            }
            domainBidList.add(e.get("domainBid") + "");
        });
        //产品经理解析
        List<MObject> moudleList = objectModelCrudI.listByBids(domainBidList, TranscendModel.MODULE.getCode());
        if (CollectionUtils.isNotEmpty(moudleList)) {
            List<String> moudlePersions = new ArrayList<>();
            for (MObject mObject : moudleList) {
                Object obj = mObject.get("personResponsible");
                List<String> listTmp = TranscendTools.analysisPersions(obj);
                moudlePersions.addAll(listTmp);
            }
            runtimeService.saveOrupdateFlowRoleUsers(rrBid, moudlePersions, spaceBid, spaceAppBid, pdmRoleCode);
        }
        //IR 需要变更解析特定角色 产品经理解析人员规则：为选择的所有模块的负责人，领域产品规划代表解析人员规则为：所有领域的领域产品规划代表(productManager)
        ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
        List<MObject> domainList = objectModelCrudI.listByBids(domainBidList, TranscendModel.DOMAIN.getCode());
        if (TranscendModel.IR.getCode().equals(apmSpaceApp.getModelCode())) {
            //ir通用解析
            flowDomainRoleCodeSet(domainList, rrBid, spaceBid, spaceAppBid, irDemandFlowRoleCodes, irDemandDomainAttrCodes);
        } else {
            //RR通用解析
            flowDomainRoleCodeSet(domainList, rrBid, spaceBid, spaceAppBid, rrDemandFlowRoleCodes, rrDemandDomainAttrCodes);
            // 通知流程责任人变动
            runtimeService.updateSpecialFlowRoleUsers(rrBid, Lists.newArrayList(domainLeader),
                    Lists.newArrayList(appLeader), Lists.newArrayList(moduleLeader),
                    spaceBid, spaceAppBid, rrDomainRoleCode, rrProductRoleCode);
        }
    }

    /**
     * 通用的解析流程角色 领域数据
     */
    private void flowDomainRoleCodeSet(List<MObject> domainList, String instanceBid, String spaceBid, String spaceAppBid, List<String> rrDemandFlowRoleCodesList, List<String> rrDemandDomainAttrCodesList) {
        //动态解析领域角色数据
        if (CollectionUtils.isNotEmpty(domainList) && CollectionUtils.isNotEmpty(rrDemandFlowRoleCodesList) && CollectionUtils.isNotEmpty(rrDemandDomainAttrCodesList) && rrDemandFlowRoleCodesList.size() == rrDemandDomainAttrCodesList.size()) {
            Map<String, List<String>> domainRoleMap = new HashMap<>(16);
            for (MObject domain : domainList) {
                for (int i = 0; i < rrDemandFlowRoleCodesList.size(); i++) {
                    Object obj = domain.get(rrDemandDomainAttrCodesList.get(i));
                    List<String> objList = TranscendTools.analysisPersions(obj);
                    List<String> domainRoleList = domainRoleMap.get(rrDemandFlowRoleCodesList.get(i));
                    if (domainRoleList == null) {
                        domainRoleList = new ArrayList<>();
                    }
                    domainRoleList.addAll(objList);
                    domainRoleMap.put(rrDemandFlowRoleCodesList.get(i), domainRoleList);
                }
            }
            if (CollectionUtils.isNotEmpty(domainRoleMap)) {
                for (Map.Entry<String, List<String>> entry : domainRoleMap.entrySet()) {
                    runtimeService.updateFlowRoleUsers(instanceBid, entry.getValue(), spaceBid, spaceAppBid, entry.getKey());
                }
            }
        }
    }
}
