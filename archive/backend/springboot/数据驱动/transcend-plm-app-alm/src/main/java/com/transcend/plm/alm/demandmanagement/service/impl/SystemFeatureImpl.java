package com.transcend.plm.alm.demandmanagement.service.impl;

import com.google.common.collect.Lists;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.core.exception.TranscendBizException;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.alm.demandmanagement.constants.SystemFeatureConstant;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.alm.demandmanagement.service.SystemFeatureService;
import com.transcend.plm.configcenter.api.feign.CfgObjectFeignClient;
import com.transcend.plm.configcenter.api.feign.CfgObjectRelationFeignClient;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationVo;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.model.SpaceAppDataEnum;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.service.IAppDataService;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.constant.DataBaseConstant;
import com.transcend.plm.datadriven.common.constant.RelationConst;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.common.tool.ObjectTreeTools;
import com.transcend.plm.datadriven.common.tool.QueryConveterTool;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transcend.plm.datadriven.domain.object.base.BaseObjectTreeService;
import com.transcend.plm.datadriven.domain.object.base.ObjectModelDomainService;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.transcend.plm.datadriven.apm.space.model.SpaceAppDataEnum.SPACE_BID;


@Service
@AllArgsConstructor
public class SystemFeatureImpl implements SystemFeatureService {

    @Resource
    private CfgObjectFeignClient cfgObjectClient;

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private ObjectModelDomainService objectModelDomainService;

    @Resource
    private BaseObjectTreeService baseObjectTreeService;

    @Resource
    private IBaseApmSpaceAppDataDrivenService iBaseApmSpaceAppDataDrivenService;

    @Resource
    private ApmSpaceAppService apmSpaceAppService;

    private final CfgObjectRelationFeignClient cfgObjectRelationClient;

    @Resource
    private IAppDataService appDataService;

    @Override
    public List<MObjectTree> tree(String spaceBid, String spaceAppBid, ModelMixQo modelMixQo, boolean b) {
        //1.查询IR
        BaseRequest<ModelMixQo> pageQo = new BaseRequest<>();
        pageQo.setCurrent(1);
        pageQo.setSize(1000);
        pageQo.setParam(modelMixQo);
        // 调用服务接口获取用户有权限的数据
        PagedResult<MSpaceAppData> page = iBaseApmSpaceAppDataDrivenService.page(spaceAppBid, QueryConveterTool.convertFitterNullValue(pageQo), true);
        List<MSpaceAppData> irData = page.getData();

        //2.查询当前空间对应的特性树实例
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(SystemFeatureConstant.ASSOCIATE_TOS_PROJECT, spaceBid);
        List<QueryWrapper> wrappers = QueryWrapper.buildSqlQo(queryWrapper);
        List<MObject> dataList = (List<MObject>) objectModelCrudI.list(TranscendModel.TOS_VERSION_FEATURE_TREE.getCode(), wrappers);
        if (ObjectUtils.isEmpty(dataList)) {
            if (CollectionUtils.isNotEmpty(irData)) {
                return irData.stream().map(mObject -> {
                    MObjectTree mObjectTree = new MObjectTree();
                    mObjectTree.putAll(mObject);
                    return mObjectTree;
                }).collect(Collectors.toList());
            }
            return Lists.newArrayList();
        }
        //特性树BID
        String sourceBid = dataList.get(0).getBid();
        //查询关系对象和目标对象
        List<CfgObjectVo> cfgObjectVo = cfgObjectClient.listByModelCodes(Arrays.asList(TranscendModel.RELATION_TOS_RSF.getCode(), TranscendModel.RSF.getCode())).getCheckExceptionData();
        if (cfgObjectVo.size() != 2) {
            throw new TranscendBizException(String.format("模型[%s],[%s]不存在", TranscendModel.RELATION_TOS_RSF.getCode(), TranscendModel.RSF.getCode()));
        }
        //查询关系实例
        QueryCondition queryCondition = new QueryCondition();
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.SOURCE_BID.getColumn(), sourceBid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        queryCondition.setQueries(queryWrappers);
        queryCondition.setOrders(Lists.newArrayList(Order.of().setProperty(BaseDataEnum.UPDATED_TIME.getCode()).setDesc(true)));
        List<MObject> mRelationObjectList = objectModelDomainService.list(TranscendModel.RELATION_TOS_RSF.getCode(), queryCondition);
        if (ObjectUtils.isEmpty(mRelationObjectList)) {
            if (CollectionUtils.isNotEmpty(irData)) {
                return irData.stream().map(mObject -> {
                    MObjectTree mObjectTree = new MObjectTree();
                    mObjectTree.putAll(mObject);
                    return mObjectTree;
                }).collect(Collectors.toList());
            }
            return Lists.newArrayList();
        }
        //查询关联特性实例
        QueryCondition targetDataQueryParam = buildBidQuery(mRelationObjectList, modelMixQo, RelationEnum.TARGET_BID.getCode());
        List<MObject> systemFeatureList = objectModelDomainService.list(TranscendModel.RSF.getCode(), targetDataQueryParam);
        if (CollectionUtils.isEmpty(systemFeatureList)) {
            if (CollectionUtils.isNotEmpty(irData)) {
                return irData.stream().map(mObject -> {
                    MObjectTree mObjectTree = new MObjectTree();
                    mObjectTree.putAll(mObject);
                    return mObjectTree;
                }).collect(Collectors.toList());
            }
            return Lists.newArrayList();
        }

        Map<String, MObject> relModelMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        for (MObject mObject : mRelationObjectList) {
            relModelMap.put(mObject.get(RelationEnum.TARGET_BID.getCode()) + "", mObject);
        }
        for (MObject mObject : systemFeatureList) {
            mObject.put("dataSource", "instance");
            mObject.put(RelationConst.RELATION_LIST_RELATION_TAG, relModelMap.get(mObject.get(TranscendModelBaseFields.BID) + ""));
        }

        //3.查询与IR关联的4级特性
        if (CollectionUtils.isNotEmpty(irData)) {
            List<String> irBids = irData.stream().map(MSpaceAppData::getBid).collect(Collectors.toList());
            QueryCondition l3QueryCondition = new QueryCondition();
            QueryWrapper l3QueryWrapper = new QueryWrapper();
            l3QueryWrapper.in(RelationEnum.SOURCE_BID.getColumn(), irBids);
            List<QueryWrapper> l3QueryWrappers = QueryWrapper.buildSqlQo(l3QueryWrapper);
            l3QueryCondition.setQueries(l3QueryWrappers);
            List<MObject> l4RelationObjectList = objectModelDomainService.list(TranscendModel.RELATION_IR_RSF.getCode(), l3QueryCondition);
            if (CollectionUtils.isNotEmpty(l4RelationObjectList)) {
                List<String> l4SfBids = l4RelationObjectList.stream().map(v->v.get(RelationEnum.TARGET_BID.getCode()).toString()).collect(Collectors.toList());
                //查询4级特性的父级
                List<MObject> l4Objects = objectModelCrudI.listByBids(new ArrayList<>(l4SfBids), TranscendModel.RSF.getCode());
                List<String> l3SfBids = l4Objects.stream().map(v->v.get(ObjectTreeEnum.PARENT_BID.getCode()).toString()).collect(Collectors.toList());
                //查询三级特性和IR编码的关系
                Map<String, List<String>> l3SfIrCodeMap = getL3SfIrCodeMap(l4RelationObjectList, l4Objects, irData);
                for (MObject mObject : systemFeatureList) {
                    if (l3SfIrCodeMap.containsKey(mObject.getBid())){
                        mObject.put("relationIrCodeList", l3SfIrCodeMap.get(mObject.getBid()));
                    }
                }
            }
        }
        //移除没有关联IR编码的3级特性
        systemFeatureList = systemFeatureList.stream().filter(v -> !"2".equals(v.get(SystemFeatureConstant.SF_LEVEL).toString()) || v.get("relationIrCodeList") != null).collect(Collectors.toList());
        // 内存排序
        systemFeatureList = ObjectTreeTools.sortObjectList(modelMixQo.getOrders(), systemFeatureList);
        List<MObjectTree> systemFeatureTree = systemFeatureList.stream().map(mObject -> {
            MObjectTree mObjectTree = new MObjectTree();
            mObjectTree.putAll(mObject);
            return mObjectTree;
        }).collect(Collectors.toList());
        //转树结构
        systemFeatureTree = baseObjectTreeService.convert2Tree(systemFeatureTree);
        if (CollectionUtils.isNotEmpty(irData)) {
            List<MObjectTree> irDataTree = irData.stream().map(mObject -> {
                MObjectTree mObjectTree = new MObjectTree();
                mObjectTree.putAll(mObject);
                return mObjectTree;
            }).collect(Collectors.toList());
            //查询tos特性与IR关系实例
            QueryCondition irRelationQueryCondition = new QueryCondition();
            QueryWrapper irRelationQo = new QueryWrapper();
            List<String> sourceBids = systemFeatureList.stream().map(MObject::getBid).collect(Collectors.toList());
            irRelationQo.in(RelationEnum.SOURCE_BID.getColumn(), sourceBids);
            List<QueryWrapper> irRelationQueryWrappers = QueryWrapper.buildSqlQo(irRelationQo);
            irRelationQueryCondition.setQueries(irRelationQueryWrappers);
            irRelationQueryCondition.setOrders(Lists.newArrayList(Order.of().setProperty(BaseDataEnum.UPDATED_TIME.getCode()).setDesc(true)));
            List<MObject> irRelationObjectList = objectModelDomainService.list(TranscendModel.RELATION_RSF_IR.getCode(), irRelationQueryCondition);
            if (CollectionUtils.isNotEmpty(irRelationObjectList)) {
                Map<String, List<String>> irRelationMap = irRelationObjectList.stream().collect(Collectors.groupingBy(v -> v.get(RelationEnum.SOURCE_BID.getCode()).toString(), Collectors.mapping(v -> v.get(RelationEnum.TARGET_BID.getCode()).toString(), Collectors.toList())));
                Map<String, MObjectTree> irBidMap = irDataTree.stream().collect(Collectors.toMap(MObjectTree::getBid, Function.identity()));
                setIr(systemFeatureTree, irRelationMap, irBidMap, irDataTree);
            }
            systemFeatureTree.addAll(irDataTree);
        }
        //校验是否是树形结构
        return systemFeatureTree;
    }

    private Map<String, List<String>> getL3SfIrCodeMap(List<MObject> l4RelationObjectList, List<MObject> l4Objects, List<MSpaceAppData> irData) {
        Map<String, String> l4SfParentBidMap = l4Objects.stream().collect(Collectors.toMap(MObject::getBid, v -> v.get(ObjectTreeEnum.PARENT_BID.getCode()).toString(), (v1, v2) -> v1));
        Map<String, String> irBidCodeMap = irData.stream().collect(Collectors.toMap(MSpaceAppData::getBid, v->v.get(TranscendModelBaseFields.CODING).toString()));
        Map<String, List<String>> l3SfIrCodeMap = new HashMap<>();
        for (MObject mObject : l4RelationObjectList) {
            String irBid = mObject.get(RelationEnum.SOURCE_BID.getCode()).toString();
            String l4SfBid = mObject.get(RelationEnum.TARGET_BID.getCode()).toString();
            String l3SfBid = l4SfParentBidMap.get(l4SfBid);
            if (l3SfBid != null) {
                String irCode = irBidCodeMap.get(irBid);
                if (irCode != null){
                    if (!l3SfIrCodeMap.containsKey(l3SfBid)){
                        l3SfIrCodeMap.put(l3SfBid, Lists.newArrayList(irCode));
                    }else {
                        l3SfIrCodeMap.get(l3SfBid).add(irCode);
                    }
                }
            }
        }
        return l3SfIrCodeMap;
    }

    private static void setIr(List<MObjectTree> systemFeatureTree, Map<String, List<String>> irRelationMap, Map<String, MObjectTree> irBidMap, List<MObjectTree> irDataTree) {
        for (MObjectTree mObjectTree : systemFeatureTree) {
            if (irRelationMap.containsKey(mObjectTree.getBid())) {
                List<String> childrenBids = irRelationMap.get(mObjectTree.getBid());
                List<MObjectTree> children = childrenBids.stream().filter(irBidMap::containsKey).map(irBidMap::get).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(children)){
                    children.forEach(irDataTree::remove);
                    for (MObjectTree child : children) {
                        child.setParentBid(mObjectTree.getBid());
                    }
                    List<MObjectTree> sfChildren = mObjectTree.getChildren();
                    if (CollectionUtils.isNotEmpty(sfChildren)){
                        sfChildren.addAll(children);
                    }else {
                        mObjectTree.setChildren(children);
                    }

                }
            }else if (CollectionUtils.isNotEmpty(mObjectTree.getChildren())){
                setIr(mObjectTree.getChildren(), irRelationMap, irBidMap, irDataTree);
            }
        }
    }

    @Override
    public List<MObjectTree> getSourceData(String spaceBid, String targetSpaceAppBid, ModelMixQo modelMixQo) {
        //查询目标空间下所有实例
        ApmSpaceApp spaceAppVo = apmSpaceAppService.getByBid(targetSpaceAppBid);
        QueryCondition queryCondition = new QueryCondition();
        QueryWrapper spaceAppBidWrapper = new QueryWrapper();
        spaceAppBidWrapper.eq(CommonConst.SPACE_APP_BID, targetSpaceAppBid);
        queryCondition.setQueries(QueryWrapper.buildSqlQo(spaceAppBidWrapper));
        List<MObject> list = objectModelCrudI.list(spaceAppVo.getModelCode(), queryCondition);
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        List<String> targetBids = list.stream().map(MObject::getBid).collect(Collectors.toList());
        //查询关系实例表
        QueryCondition relationQueryCondition = new QueryCondition();
        QueryWrapper relationQo = new QueryWrapper();
        relationQo.in(RelationEnum.TARGET_BID.getColumn(), targetBids);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(relationQo);
        relationQueryCondition.setQueries(queryWrappers);
        relationQueryCondition.setOrders(Lists.newArrayList(Order.of().setProperty(BaseDataEnum.UPDATED_TIME.getCode()).setDesc(true)));
        List<MObject> mRelationObjectList = objectModelDomainService.list(modelMixQo.getRelationModelCode(), relationQueryCondition);
        if (CollectionUtils.isEmpty(mRelationObjectList)) {
            return Lists.newArrayList();
        }
        List<String> sourceBids = mRelationObjectList.stream().map(v -> v.get(RelationEnum.SOURCE_BID.getCode()).toString()).collect(Collectors.toList());
        //查询目标实例
        CfgObjectRelationVo relationVo =
                cfgObjectRelationClient.getRelation(modelMixQo.getRelationModelCode()).getCheckExceptionData();
        ;
        if (relationVo == null) {
            return Lists.newArrayList();
        }
        //查询源对象实例列表
        QueryCondition sourceQueryCondition = new QueryCondition();
        QueryWrapper sourceWrapper = new QueryWrapper();
        sourceWrapper.in(CommonConst.BID_STR, sourceBids);
        sourceQueryCondition.setQueries(QueryWrapper.buildSqlQo(sourceWrapper));
        List<MObject> l2Objects = objectModelCrudI.list(relationVo.getSourceModelCode(), sourceQueryCondition);
        if (CollectionUtils.isEmpty(l2Objects)) {
            return Lists.newArrayList();
        }
        //查询当前空间对应的特性树实例
        List<String> rsfBySpaceBid = listRsfBySpaceBid(spaceBid);
        //移除不是当前空间下的特性
        l2Objects.removeIf(v->!rsfBySpaceBid.contains(v.getBid()));
        if (CollectionUtils.isEmpty(l2Objects)) {
            return Lists.newArrayList();
        }
        //查询一级特性
        Set<Object> l1SfBids = l2Objects.stream().map(v -> String.valueOf(v.get(ObjectTreeEnum.PARENT_BID.getCode()))).collect(Collectors.toSet());
        List<MObject> l3Objects = objectModelCrudI.listByBids(new ArrayList<>(l1SfBids), TranscendModel.RSF.getCode());
        l2Objects.addAll(l3Objects);

        List<MObjectTree> systemFeatureTree = l2Objects.stream().map(mObject -> {
            MObjectTree mObjectTree = new MObjectTree();
            mObjectTree.putAll(mObject);
            return mObjectTree;
        }).collect(Collectors.toList());
        //转树结构
        return baseObjectTreeService.convert2Tree(systemFeatureTree);
    }

    @Override
    public List<MObjectTree> selectSF(String spaceBid, String targetSpaceAppBid, String parentBid) {
        //查询当前空间对应的特性树实例
        List<String> targetBids = listRsfBySpaceBid(spaceBid);
        if (ObjectUtils.isEmpty(targetBids)) {
            return Lists.newArrayList();
        }
        //查询特性树实例数据
        QueryCondition targetDataQueryParam = new QueryCondition();
        QueryWrapper sfQo = new QueryWrapper();
        sfQo.in(RelationEnum.BID.getColumn(), targetBids);
        List<QueryWrapper> sfQueryWrappers = QueryWrapper.buildSqlQo(sfQo);
        targetDataQueryParam.setQueries(sfQueryWrappers);
        List<MObject> systemFeatureList = objectModelDomainService.list(TranscendModel.RSF.getCode(), targetDataQueryParam);
        if (CollectionUtils.isEmpty(systemFeatureList)) {
            return Lists.newArrayList();
        }
        //因为只能选择4级特性，其他特性禁用
        for (MObject mObject : systemFeatureList) {
            if (3 != Integer.parseInt(mObject.get(SystemFeatureConstant.SF_LEVEL).toString())) {
                mObject.put("disabled", true);
            }
        }
        List<MObjectTree> systemFeatureTree = systemFeatureList.stream().map(mObject -> {
            MObjectTree mObjectTree = new MObjectTree();
            mObjectTree.putAll(mObject);
            mObjectTree.put("nodeId", mObjectTree.getBid());
            mObjectTree.put("nodeParentId", mObjectTree.getParentBid());
            return mObjectTree;
        }).collect(Collectors.toList());
        //转树结构
        systemFeatureTree = baseObjectTreeService.convert2Tree(systemFeatureTree);
        if (StringUtil.isNotBlank(parentBid)) {
            for (MObjectTree mObjectTree : systemFeatureTree) {
                if (mObjectTree.getBid().equals(parentBid)){
                    return Lists.newArrayList(mObjectTree);
                }
                if (mObjectTree.getChildren() != null && !mObjectTree.getChildren().isEmpty()){
                    for (MObjectTree mObjectTree1 : mObjectTree.getChildren()) {
                        if (mObjectTree1.getBid().equals(parentBid)){
                            mObjectTree.setChildren(Lists.newArrayList(mObjectTree1));
                            return Lists.newArrayList(mObjectTree);
                        }
                    }
                }
            }
            return systemFeatureTree.stream()
                    .filter(mObjectTree -> mObjectTree.getBid().equals(parentBid))
                    .collect(Collectors.toList());
        }
        return systemFeatureTree;
    }

    private List<String> listRsfBySpaceBid(String spaceBid) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(SystemFeatureConstant.ASSOCIATE_TOS_PROJECT, spaceBid);
        List<QueryWrapper> wrappers = QueryWrapper.buildSqlQo(queryWrapper);
        List<MObject> dataList = (List<MObject>) objectModelCrudI.list(TranscendModel.TOS_VERSION_FEATURE_TREE.getCode(), wrappers);
        if (ObjectUtils.isEmpty(dataList)) {
            return Lists.newArrayList();
        }
        //特性树BID
        String sourceBid = dataList.get(0).getBid();
        //查询关系对象和目标对象
        List<CfgObjectVo> cfgObjectVo = cfgObjectClient.listByModelCodes(Arrays.asList(TranscendModel.RELATION_TOS_RSF.getCode(), TranscendModel.RSF.getCode())).getCheckExceptionData();
        if (cfgObjectVo.size() != 2) {
            throw new TranscendBizException(String.format("模型[%s],[%s]不存在", TranscendModel.RELATION_TOS_RSF.getCode(), TranscendModel.RSF.getCode()));
        }
        //查询关系实例
        QueryCondition queryCondition = new QueryCondition();
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.SOURCE_BID.getColumn(), sourceBid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        queryCondition.setQueries(queryWrappers);
        queryCondition.setOrders(Lists.newArrayList(Order.of().setProperty(BaseDataEnum.UPDATED_TIME.getCode()).setDesc(true)));
        List<MObject> mRelationObjectList = objectModelDomainService.list(TranscendModel.RELATION_TOS_RSF.getCode(), queryCondition);
        if (ObjectUtils.isEmpty(mRelationObjectList)) {
            return Lists.newArrayList();
        }
        List<String> targetBids = new ArrayList<>();
        for (MObject mObject : mRelationObjectList) {
            targetBids.add(mObject.get(RelationEnum.TARGET_BID.getCode()) + "");
        }
        return targetBids;
    }

    /**
     * ir关联三级特性
     *
     * @param spaceBid spaceBid
     * @param irBid    irBid
     * @param l4SfBids   l4SfBids
     * @return 查询条件对象
     */
    @Override
    public Boolean addSFRelation(MSpaceAppData mSpaceAppData, String spaceBid, String irBid, Set<String> l4SfBids) {
        if (CollectionUtils.isEmpty(l4SfBids)) {
            //删除ir和4级特性原来的关联关系，新增新的关联关系
            QueryWrapper qo = new QueryWrapper();
            qo.eq("source_bid", irBid);
            List<QueryWrapper> deleteQueryWrappers = QueryWrapper.buildSqlQo(qo);
            objectModelDomainService.logicalDelete(TranscendModel.RELATION_IR_RSF.getCode(), deleteQueryWrappers);
            return true;
        }
        //校验4级特性是否同一2级特性的子节点，如果不是，则不能关联
        List<MObject> mObjects = objectModelCrudI.listByBids(new ArrayList<>(l4SfBids), TranscendModel.RSF.getCode());
        Set<Object> l3SfBids = mObjects.stream().map(v -> String.valueOf(v.get(ObjectTreeEnum.PARENT_BID.getCode()))).collect(Collectors.toSet());
        List<MObject> l3Objects = objectModelCrudI.listByBids(new ArrayList<>(l3SfBids), TranscendModel.RSF.getCode());
        Set<Object> l2SfBids = l3Objects.stream().map(v -> String.valueOf(v.get(ObjectTreeEnum.PARENT_BID.getCode()))).collect(Collectors.toSet());
        if (l2SfBids.size() != 1) {
            throw new PlmBizException("只能关联同一个二级特性下的四级特性");
        }
        //删除ir和4级特性原来的关联关系，新增新的关联关系
        QueryWrapper qo = new QueryWrapper();
        qo.eq("source_bid", irBid);
        List<QueryWrapper> deleteQueryWrappers = QueryWrapper.buildSqlQo(qo);
        objectModelDomainService.logicalDelete(TranscendModel.RELATION_IR_RSF.getCode(), deleteQueryWrappers);
        List<MObject> relationObject = l4SfBids.stream().map(v -> assembleRelationObject(TranscendModel.RELATION_IR_RSF.getCode(), irBid, v, spaceBid)).collect(Collectors.toList());
        appDataService.addBatch(TranscendModel.RELATION_IR_RSF.getCode(), relationObject);
        // 删除2级特性和IR原来的关联关系，新增新的关联关系
        QueryWrapper deleteQo2 = new QueryWrapper();
        deleteQo2.eq("target_bid", irBid);
        List<QueryWrapper> deleteQueryWrappers2 = QueryWrapper.buildSqlQo(deleteQo2);
        objectModelDomainService.logicalDelete(TranscendModel.RELATION_RSF_IR.getCode(), deleteQueryWrappers2);
        mSpaceAppData.put(SystemFeatureConstant.belong_SF, l2SfBids.iterator().next());
        MObject mObject = assembleRelationObject(TranscendModel.RELATION_RSF_IR.getCode(), l2SfBids.iterator().next(), irBid, spaceBid);
        appDataService.add(TranscendModel.RELATION_RSF_IR.getCode(), mObject);
        return true;
    }


    /**
     * ir关联二级特性
     *
     * @param spaceBid spaceBid
     * @param irBid    irBid
     * @param l2SfBid   l2SfBid
     * @return 查询条件对象
     */
    @Override
    public Boolean addL2SFRelation(MSpaceAppData mSpaceAppData, String spaceBid, String irBid, String l2SfBid) {
        if (StringUtil.isNotBlank(l2SfBid)) {
            //建立2级特性与Ir的关系
            MObject mObject = assembleRelationObject(TranscendModel.RELATION_RSF_IR.getCode(), l2SfBid, irBid, spaceBid);
            appDataService.add(TranscendModel.RELATION_RSF_IR.getCode(), mObject);
        }
        return true;
    }

    @Override
    public void deleteIrRsfRelation(List<String> sfBids) {
        //查询rsf-ir关联关系
        QueryCondition irRelationQueryCondition = new QueryCondition();
        QueryWrapper irRelationQo = new QueryWrapper();
        irRelationQo.in(RelationEnum.SOURCE_BID.getColumn(), sfBids);
        List<QueryWrapper> irRelationQueryWrappers = QueryWrapper.buildSqlQo(irRelationQo);
        irRelationQueryCondition.setQueries(irRelationQueryWrappers);
        List<MObject> irRelationObjectList = objectModelDomainService.list(TranscendModel.RELATION_RSF_IR.getCode(), irRelationQueryCondition);
        //rsf-ir源和目标对象bid
        List<String> irBids = new ArrayList<>();
        List<String> rsfBids = new ArrayList<>();
        //ir-rsf源和目标对象bid
        List<String> irBids2 = new ArrayList<>();
        List<String> rsfBids2 = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(irRelationObjectList)) {
            irBids = irRelationObjectList.stream().map(v -> v.get(RelationEnum.TARGET_BID.getCode()).toString()).collect(Collectors.toList());
            rsfBids = irRelationObjectList.stream().map(v -> v.get(RelationEnum.SOURCE_BID.getCode()).toString()).collect(Collectors.toList());
        }
        sfBids.removeAll(rsfBids);
        if (CollectionUtils.isNotEmpty(sfBids)) {
            //查询ir-rsf关联关系
            QueryCondition irRelationQueryCondition2 = new QueryCondition();
            QueryWrapper irRelationQo2 = new QueryWrapper();
            irRelationQo2.in(RelationEnum.TARGET_BID.getColumn(), sfBids);
            List<QueryWrapper> irRelationQueryWrappers2 = QueryWrapper.buildSqlQo(irRelationQo2);
            irRelationQueryCondition2.setQueries(irRelationQueryWrappers2);
            List<MObject> irRelationObjectList2 = objectModelDomainService.list(TranscendModel.RELATION_IR_RSF.getCode(), irRelationQueryCondition2);
            if (CollectionUtils.isNotEmpty(irRelationObjectList2)) {
                irBids2 = irRelationObjectList2.stream().map(v -> v.get(RelationEnum.SOURCE_BID.getCode()).toString()).collect(Collectors.toList());
                rsfBids2 = irRelationObjectList2.stream().map(v -> v.get(RelationEnum.TARGET_BID.getCode()).toString()).collect(Collectors.toList());
            }
        }
        if (CollectionUtils.isEmpty(irBids) && CollectionUtils.isEmpty(irBids2)) {
            return;
        }
        List<String> bids= new ArrayList<>();
        bids.addAll(irBids);
        bids.addAll(irBids2);
        List<MObject> mObjects = objectModelCrudI.listByBids(new ArrayList<>(bids), TranscendModel.IR.getCode());
        if (CollectionUtils.isEmpty(mObjects)) {
            return;
        }
        List<BatchUpdateBO<MSpaceAppData>> batchUpdateBoList = Lists.newArrayList();
        for (MObject mObject : mObjects) {
            MSpaceAppData mSpaceAppData = new MSpaceAppData();
            if (irBids.contains(mObject.getBid())) {
                mSpaceAppData.put(SystemFeatureConstant.belong_SF,null);
                mSpaceAppData.put(SystemFeatureConstant.MOUNT_SF,null);
                QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
                queryWrapper.setProperty(DataBaseConstant.COLUMN_BID);
                queryWrapper.setCondition("=");
                queryWrapper.setValue(mObject.getBid());
                BatchUpdateBO<MSpaceAppData> batchUpdateBO = new BatchUpdateBO<>();
                batchUpdateBO.setBaseData(mSpaceAppData);
                batchUpdateBO.setWrappers(Collections.singletonList(queryWrapper));
                batchUpdateBoList.add(batchUpdateBO);
            } else if (irBids2.contains(mObject.getBid()) && ObjectUtils.isNotEmpty(mObject.get(SystemFeatureConstant.MOUNT_SF))) {
                Object mountSf = mObject.get(SystemFeatureConstant.MOUNT_SF);
                if (mountSf instanceof List) {
                    List<Object> mountSFBidObject = (List<Object>) mountSf;
                    List<String> finalRsfBids = rsfBids2;
                    mountSFBidObject.removeIf(v->{
                        String bid = null;
                        if (v instanceof String){
                            bid =  v.toString();
                        } else if (v instanceof List){
                            bid =  ((List<?>)v).get(((List<?>) v).size()-1).toString();
                        }
                        return bid != null && finalRsfBids.contains(bid);
                    });
                    mSpaceAppData.put(SystemFeatureConstant.MOUNT_SF,mountSFBidObject);
                    QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
                    queryWrapper.setProperty(DataBaseConstant.COLUMN_BID);
                    queryWrapper.setCondition("=");
                    queryWrapper.setValue(mObject.getBid());
                    BatchUpdateBO<MSpaceAppData> batchUpdateBO = new BatchUpdateBO<>();
                    batchUpdateBO.setBaseData(mSpaceAppData);
                    batchUpdateBO.setWrappers(Collections.singletonList(queryWrapper));
                    batchUpdateBoList.add(batchUpdateBO);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(batchUpdateBoList)) {
            objectModelCrudI.batchUpdateByQueryWrapper(TranscendModel.IR.getCode(), batchUpdateBoList, false);
        }
        if (CollectionUtils.isNotEmpty(irBids)) {
            QueryWrapper qo = new QueryWrapper();
            qo.in("target_bid", irBids);
            List<QueryWrapper> deleteQueryWrappers = QueryWrapper.buildSqlQo(qo);
            objectModelDomainService.logicalDelete(TranscendModel.RELATION_RSF_IR.getCode(), deleteQueryWrappers);

            QueryWrapper qo2 = new QueryWrapper();
            qo2.in("source_bid", irBids);
            List<QueryWrapper> deleteQueryWrappers2 = QueryWrapper.buildSqlQo(qo2);
            objectModelDomainService.logicalDelete(TranscendModel.RELATION_IR_RSF.getCode(), deleteQueryWrappers2);
        }
        if (CollectionUtils.isNotEmpty(rsfBids2)) {
            QueryWrapper qo = new QueryWrapper();
            qo.in("target_bid", rsfBids2);
            List<QueryWrapper> deleteQueryWrappers = QueryWrapper.buildSqlQo(qo);
            objectModelDomainService.logicalDelete(TranscendModel.RELATION_IR_RSF.getCode(), deleteQueryWrappers);
        }
    }


    /**
     * @param mObjectList
     * @param modelMixQo
     * @return {@link QueryCondition }
     */
    private QueryCondition buildBidQuery(List<MObject> mObjectList, ModelMixQo modelMixQo, String targetBidFieldName) {

        List<Object> targetBids = new ArrayList<>();
        for (MObject mObject : mObjectList) {
            targetBids.add(mObject.get(targetBidFieldName) + "");
        }
        QueryCondition targetDataQueryParam = new QueryCondition();
        if (modelMixQo != null && CollectionUtils.isNotEmpty(modelMixQo.getQueries())) {
            QueryWrapper qo = new QueryWrapper();
            qo.in(RelationEnum.BID.getColumn(), targetBids).and().in(SystemFeatureConstant.SF_LEVEL, Lists.newArrayList(0, 1, 2));
            List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
            List<QueryWrapper> queries = QueryConveterTool.convert(modelMixQo.getQueries(), modelMixQo.getAnyMatch());
            List<QueryWrapper> queryWrapperList = QueryConveterTool.appendMoreQueriesAndCondition(queryWrappers, queries);
            targetDataQueryParam.setQueries(queryWrapperList);
            if (CollectionUtils.isNotEmpty(modelMixQo.getOrders())) {
                targetDataQueryParam.setOrders(modelMixQo.getOrders());
            }
        } else {
            QueryWrapper qo = new QueryWrapper();
            qo.in(RelationEnum.BID.getColumn(), targetBids).and().in(SystemFeatureConstant.SF_LEVEL, Lists.newArrayList(0, 1, 2));
            List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
            targetDataQueryParam.setQueries(queryWrappers);
            if (Objects.nonNull(modelMixQo) && CollectionUtils.isNotEmpty(modelMixQo.getOrders())) {
                targetDataQueryParam.setOrders(modelMixQo.getOrders());
            }
        }
        return targetDataQueryParam;
    }

    /**
     * 构造查询条件
     *
     * @param bids 待查询的 bid 列表
     * @return 查询条件对象
     */
    private BaseRequest<QueryCondition> buildQueryCondition(List<String> bids) {
        BaseRequest<QueryCondition> pageQo = new BaseRequest<>();
        QueryCondition queryCondition = new QueryCondition();
        QueryWrapper qo = new QueryWrapper();
        qo.in(RelationEnum.BID.getColumn(), bids);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        queryCondition.setQueries(queryWrappers);
        pageQo.setParam(queryCondition);
        pageQo.setCurrent(1);
        pageQo.setSize(1000); // 支持最多 1000 条数据
        return pageQo;
    }


    public MObject assembleRelationObject(String modelCode, Object sourceBid, Object targetBid, String spaceBid) {
        MObject relationObject = new MObject();
        relationObject.setBid(SnowflakeIdWorker.nextIdStr());
        relationObject.put(RelationEnum.DATA_BID.getCode(), SnowflakeIdWorker.nextIdStr());
        relationObject.put(RelationEnum.SOURCE_DATA_BID.getCode(), "-1");
        relationObject.put(RelationEnum.TARGET_DATA_BID.getCode(), targetBid);
        relationObject.put(RelationEnum.SOURCE_BID.getCode(), sourceBid);
        relationObject.put(RelationEnum.TARGET_BID.getCode(), targetBid);
        relationObject.put(SpaceAppDataEnum.SPACE_APP_BID.getCode(), "0");
        relationObject.put(SPACE_BID.getCode(), spaceBid);
        relationObject.put(RelationEnum.DRAFT.getCode(), Boolean.FALSE);
        relationObject.setModelCode(modelCode);
        relationObject.setCreatedBy(SsoHelper.getJobNumber());
        relationObject.setUpdatedBy(SsoHelper.getJobNumber());
        relationObject.setUpdatedTime(LocalDateTime.now());
        relationObject.setCreatedTime(LocalDateTime.now());
        relationObject.setEnableFlag(true);
        relationObject.setDeleteFlag(false);
        relationObject.setTenantId(SsoHelper.getTenantId());
        return relationObject;
    }


}
