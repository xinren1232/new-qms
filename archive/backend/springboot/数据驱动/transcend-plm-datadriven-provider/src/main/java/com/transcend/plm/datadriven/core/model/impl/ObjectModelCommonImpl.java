package com.transcend.plm.datadriven.core.model.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.core.constantenum.ObjectTypeEnum;
import com.transcend.framework.core.constantenum.RelationBehaviorEnum;
import com.transcend.framework.core.exception.TranscendBizException;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.feign.CfgObjectFeignClient;
import com.transcend.plm.configcenter.api.feign.CfgObjectRelationFeignClient;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectModelLifeCycleVO;
import com.transcend.plm.configcenter.api.model.objectrelation.qo.ObjectRelationRuleQo;
import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationVo;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.dto.LifeCyclePromoteDto;
import com.transcend.plm.datadriven.api.model.dto.MObjectCheckDto;
import com.transcend.plm.datadriven.api.model.dto.ReviseDto;
import com.transcend.plm.datadriven.api.model.qo.ModelFilterQo;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.api.model.qo.RelationCrossLevelQo;
import com.transcend.plm.datadriven.api.model.vo.DraftVO;
import com.transcend.plm.datadriven.common.constant.DataBaseConstant;
import com.transcend.plm.datadriven.common.constant.RelationConst;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.pojo.dto.NotifyPostEventBusDto;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.common.tool.ObjectTreeTools;
import com.transcend.plm.datadriven.common.tool.QueryConveterTool;
import com.transcend.plm.datadriven.common.tool.SecrecyWrapperHandler;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.config.eventbus.NotifyEventBus;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.domain.object.base.*;
import com.transcend.plm.datadriven.domain.object.relation.RelationObjectManageService;
import com.transcend.plm.datadriven.domain.support.external.object.ObjectPojoConverter;
import com.transcend.plm.datadriven.infrastructure.basedata.repository.RelationDataRepository;
import com.transcend.plm.datadriven.infrastructure.draft.po.DraftPO;
import com.transcend.plm.datadriven.infrastructure.draft.repository.DraftRepository;
import com.transcend.plm.datadriven.platform.feign.model.dto.PlatFormUserDTO;
import com.transcend.plm.datadriven.platform.service.IPlatformService;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.exception.BusinessException;
import com.transsion.framework.uac.model.dto.PageDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * TODO 对象模型实现 此处需要加前置后置方法
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/5/18 17:24
 */
@Slf4j
@Component
public class ObjectModelCommonImpl implements ObjectModelStandardI<MObject> {

    @Resource
    private ObjectModelDomainService objectModelDomainService;

    @Resource
    private VersionModelDomainService versionModelDomainService;

    @Resource
    private RelationModelDomainService relationModelDomainService;

    @Resource
    private RelationObjectManageService relationObjectManageService;

    @Resource
    private BaseObjectTreeService baseObjectTreeService;

    @Resource
    private CfgObjectFeignClient cfgObjectClient;

    @Resource
    private DraftRepository draftRepository;

    @Resource
    private CfgObjectRelationFeignClient cfgObjectRelationClient;

    @Resource
    private OperationLogService operationLogService;
    @Resource
    private IPlatformService platformService;

    @Resource
    private RelationDataRepository relationDataRepository;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Value("#{'${transcend.plm.apm.blob.attr.filter:richTextContent,demandDesc,testingRecommendations,text}'.split(',')}")
    private List<String> filterFields;

    /**
     * @param mObject
     * @param objectModelLifeCycleVO
     * @param modelCode
     */
    @Override
    public void setMObjectDefaultValue(MObject mObject, ObjectModelLifeCycleVO objectModelLifeCycleVO, String modelCode) {
        mObject.put(TranscendModelBaseFields.ENABLE_FLAG, 0);
        mObject.put(TranscendModelBaseFields.DELETE_FLAG, 0);
        mObject.put(TranscendModelBaseFields.TENANT_ID, SsoHelper.getTenantId());
        if (StringUtil.isBlank(mObject.getCreatedBy())) {
            mObject.put(TranscendModelBaseFields.CREATED_BY, SsoHelper.getJobNumber());
        }
        if (StringUtil.isBlank(mObject.getUpdatedBy())) {
            mObject.put(TranscendModelBaseFields.UPDATED_BY, SsoHelper.getJobNumber());
        }
        if (ObjectUtils.isEmpty(mObject.get(TranscendModelBaseFields.CREATED_TIME))) {
            mObject.put(TranscendModelBaseFields.CREATED_TIME, new Date());
        }
        mObject.put(TranscendModelBaseFields.UPDATED_TIME, new Date());
        if (mObject.get(TranscendModelBaseFields.BID) == null) {
            mObject.put(TranscendModelBaseFields.BID, SnowflakeIdWorker.nextIdStr());
        }
        if (mObject.get(TranscendModelBaseFields.DATA_BID) == null) {
            mObject.put(TranscendModelBaseFields.DATA_BID, SnowflakeIdWorker.nextIdStr());
        }
        if (objectModelLifeCycleVO != null) {
            if (mObject.get(TranscendModelBaseFields.LC_TEMPL_BID) == null && StringUtil.isNotBlank(objectModelLifeCycleVO.getLcTemplBid())) {
                mObject.put(TranscendModelBaseFields.LC_TEMPL_BID, objectModelLifeCycleVO.getLcTemplBid());
            }
            if (mObject.get(TranscendModelBaseFields.LC_TEMPL_VERSION) == null && StringUtil.isNotBlank(objectModelLifeCycleVO.getLcTemplVersion())) {
                mObject.put(TranscendModelBaseFields.LC_TEMPL_VERSION, objectModelLifeCycleVO.getLcTemplVersion());
            }
            if (StringUtil.isBlank(mObject.getLifeCycleCode())) {
                //查询初始状态
                mObject.setLifeCycleCode(objectModelLifeCycleVO.getInitState());
            }
            mObject.put(TranscendModelBaseFields.LC_MODEL_CODE, objectModelLifeCycleVO.getInitState() + ":" + modelCode);
        }
    }

    @Override
    public MObject add(String modelCode, MObject mObject) {
        //初始化固定值
        mObject.setModelCode(modelCode);
        mObject.put(TranscendModelBaseFields.MODEL_CODE, modelCode);
        String bid = SnowflakeIdWorker.nextIdStr();
        if (mObject.get(TranscendModelBaseFields.BID) == null || StringUtil.isBlank(mObject.getBid())) {
            mObject.put(TranscendModelBaseFields.BID, bid);
        }
        if (mObject.get(TranscendModelBaseFields.DATA_BID) == null || StringUtil.isBlank(mObject.get(TranscendModelBaseFields.DATA_BID).toString())) {
            mObject.put(TranscendModelBaseFields.DATA_BID, bid);
        }
        ObjectModelLifeCycleVO objectModelLifeCycleVO = cfgObjectClient.findObjectLifecycleByModelCode(modelCode)
                .getCheckExceptionData();
        setMObjectDefaultValue(mObject, objectModelLifeCycleVO, modelCode);
        CfgObjectVo cfgObjectVo = cfgObjectClient.getByModelCode(modelCode).getCheckExceptionData();
        if (cfgObjectVo == null) {
            throw new BusinessException("对象不存在");
        }
        AtomicReference<MObject> mObjectAtomicReference = new AtomicReference<>();
        transactionTemplate.execute(status -> {
            //如果是有版本对象 需要写历史表
            if (ObjectTypeEnum.VERSION.getCode().equals(cfgObjectVo.getType())) {
                if (mObject.get(VersionObjectEnum.VERSION.getCode()) == null
                        || StringUtil.isBlank(String.valueOf(mObject.get(VersionObjectEnum.VERSION.getCode())))) {
                    mObject.put(VersionObjectEnum.VERSION.getCode(), "1");
                }
                if (mObject.get(VersionObjectEnum.REVISION.getCode()) == null
                        || StringUtil.isBlank(String.valueOf(mObject.get(VersionObjectEnum.REVISION.getCode())))) {
                    mObject.put(VersionObjectEnum.REVISION.getCode(), "A");
                }
                objectModelDomainService.addHis(modelCode, mObject);
            }
            mObjectAtomicReference.set(objectModelDomainService.add(modelCode, mObject));
            return true;
        });
        // 发布后置处理事件
        try {
            // 1. 获取当前方法名字
            String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
            // 2. 发布事件方法
            NotifyEventBus.publishPostEvent(new NotifyPostEventBusDto(mObject, SsoHelper.getJobNumber(), methodName));
        } catch (Exception e) {
            log.error("执行modelCode为：{}的新增实例的后置方法异常！", modelCode);
        }
        return mObjectAtomicReference.get();

    }

    /**
     * @param modelCode 模型code
     * @param bid       实例bid
     * @return Boolean
     */
    @Override
    public Boolean logicalDelete(String modelCode, String bid) {

        return objectModelDomainService.logicalDeleteByBid(modelCode, bid);
    }

    /**
     * @param modelCode
     * @param mObject
     * @param wrappers
     * @return {@link Boolean }
     */
    @Override
    public Boolean update(String modelCode, MObject mObject, List<QueryWrapper> wrappers) {
        return null;
    }

    @Override
    public Boolean batchUpdatePartialContentByIds(String modelCode, MObject mObject, List<String> bids) {
        QueryWrapper qo = new QueryWrapper();
        qo.in(RelationEnum.BID.getColumn(), bids);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        objectModelDomainService.update(modelCode, mObject, queryWrappers);
        return Boolean.TRUE;
    }


    @Override
    public Boolean batchUpdatePartialContentByCodingList(String modelCode, MObject mObject, List<String> codingList) {
        QueryWrapper qo = new QueryWrapper();
        qo.in(ObjectEnum.CODING.getCode(), codingList);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        objectModelDomainService.update(modelCode, mObject, queryWrappers);
        return Boolean.TRUE;
    }

    @Override
    public Boolean batchUpdateByQueryWrapper(String modelCode, List<BatchUpdateBO<MObject>> batchUpdateBoList, boolean isHistory) {
        return objectModelDomainService.batchUpdateByQueryWrapper(modelCode, batchUpdateBoList, isHistory);
    }

    @Override
    public List<MObject> list(String modelCode, List<QueryWrapper> wrappers) {
        return objectModelDomainService.list(modelCode, wrappers);
    }

    @Override
    public List<MObject> list(String modelCode, QueryCondition queryCondition) {
        return objectModelDomainService.list(modelCode, queryCondition);
    }

    @Override
    public List<MObject> signObjectRecursionTreeList(String modelCode, QueryCondition queryCondition) {
        return objectModelDomainService.signObjectRecursionTreeList(modelCode, queryCondition);
    }

    @Override
    public List<MObjectTree> listTree(String modelCode, RelationMObject relationMObject) {
        // 临时设置为TRUE 以后需要修改
        return listTree(modelCode, relationMObject, null);
    }

    @Override
    public List<MObjectTree> selectTree(String modelCode, RelationMObject relationMObject) {
        return listTree(modelCode, relationMObject, Boolean.TRUE);
    }


    @Override
    public List<MObjectTree> listTree(List<MObject> list, RelationMObject relationMObject, Boolean isSelect) {
        List<MObjectTree> resList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            Boolean isSelectFlag = Boolean.TRUE;
            //查询选中的数据
            if (isSelectFlag.equals(isSelect)) {
                selectCheckTree(relationMObject, list);
            }
            List<MObjectTree> treeList = list.stream().map(mObject -> {
                MObjectTree mObjectTree = new MObjectTree();
                mObjectTree.putAll(mObject);
                return mObjectTree;
            }).collect(Collectors.toList());
            //校验是否是树形结构
            resList = baseObjectTreeService.convert2Tree(treeList);
        }
        return resList;
    }

    private List<MObjectTree> listTree(String modelCode, RelationMObject relationMObject, Boolean isSelect) {
        List<MObjectTree> resList = new ArrayList<>();
        List<QueryWrapper> wrappers = QueryConveterTool.convert(relationMObject.getModelMixQo()).getQueries();
        List<MObject> list = objectModelDomainService.list(modelCode, wrappers);
        if (CollectionUtils.isNotEmpty(list)) {
            Boolean isSelectFlag = Boolean.TRUE;
            //查询选中的数据
            if (isSelectFlag.equals(isSelect)) {
                selectCheckTree(relationMObject, list);
            }
            List<MObjectTree> treeList = list.stream().map(mObject -> {
                MObjectTree mObjectTree = new MObjectTree();
                mObjectTree.putAll(mObject);
                return mObjectTree;
            }).collect(Collectors.toList());
            //校验是否是树形结构
            resList = baseObjectTreeService.convert2Tree(treeList);
        }
        return resList;
    }

    /**
     * @param relationMObject
     * @param list
     */
    private void selectCheckTree(RelationMObject relationMObject, List<MObject> list) {
        RelationMObject relationMObject1 = RelationMObject.builder().sourceBid(relationMObject.getSourceBid()).relationModelCode(relationMObject.getRelationModelCode()).build();
        List<MObject> mRelationObjectList = listOnlyRelationMObjects(relationMObject1);
        if (CollectionUtils.isNotEmpty(mRelationObjectList)) {
            //树形结构选中处理
            Map<String, String> relModelMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
            for (MObject mObject : mRelationObjectList) {
                relModelMap.put(mObject.get("targetBid") + "", mObject.get(TranscendModelBaseFields.BID) + "");
            }
            for (MObject mObject : list) {
                if (relModelMap.containsKey(mObject.getBid())) {
                    mObject.put("checked", true);
                } else {
                    mObject.put("checked", false);
                }
            }
        }
    }


    /**
     * @param modelCode
     * @param mObject
     * @return {@link MObject }
     */
    @Override
    public MObject saveOrUpdate(String modelCode, MObject mObject) {
        String bid = mObject.getBid();
        if (StringUtil.isBlank(mObject.getBid())) {
            return add(modelCode, mObject);
        }
        // 更新
        updateByBid(modelCode, bid, mObject);
        return mObject;
    }

    /**
     * @param modelCode
     * @param bid
     * @return {@link MObject }
     */
    @Override
    public MObject getByBid(String modelCode, String bid) {
        MObject mObject = objectModelDomainService.getByBid(modelCode, bid);
        if (mObject == null) {
            //直接从草稿取
            DraftPO draftPO = draftRepository.getByBid(bid);
            if (draftPO != null && StringUtil.isNotBlank(draftPO.getContent())) {
                MObject mObject2 = JSON.parseObject(draftPO.getContent(), MObject.class);
                if (mObject2.get(VersionObjectEnum.CHECKOUT_BY.getCode()) != null) {
                    String checkOutBy = mObject2.get(VersionObjectEnum.CHECKOUT_BY.getCode()) + "";
                    if (StringUtil.isNotBlank(checkOutBy) && checkOutBy.equals(SsoHelper.getJobNumber())) {
                        mObject2.put("dataSource", "draft");
                        return mObject2;
                    }
                }
            }
        }
        //取历史
        if (mObject == null) {
            mObject = getHisByBid(modelCode, bid);
            if (mObject == null) {
                return null;
            }
            mObject.put("dataSource", "history");
            return mObject;
        }
        mObject.put("dataSource", "instance");
        //判断是否检出
        if (mObject.get(VersionObjectEnum.CHECKOUT_BY.getCode()) != null) {
            String checkOutBy = mObject.get(VersionObjectEnum.CHECKOUT_BY.getCode()) + "";
            if (StringUtil.isNotBlank(checkOutBy) && checkOutBy.equals(SsoHelper.getJobNumber())) {
                //查询草稿表
                DraftPO draftPO = draftRepository.getByDataBid(mObject.get(TranscendModelBaseFields.DATA_BID) + "");
                if (draftPO == null || StringUtil.isBlank(draftPO.getContent())) {
                    return mObject;
                }
                mObject = JSON.parseObject(draftPO.getContent(), MObject.class);
                mObject.put("dataSource", "draft");
            }
        }
        return mObject;
    }

    @Override
    public MObject getByBidNotDelete(String modelCode, String bid) {
        MObject mObject = objectModelDomainService.getByBidNotDelete(modelCode, bid);
        if (mObject == null) {
            //直接从草稿取
            DraftPO draftPO = draftRepository.getByBid(bid);
            if (draftPO != null && StringUtil.isNotBlank(draftPO.getContent())) {
                MObject mObject2 = JSON.parseObject(draftPO.getContent(), MObject.class);
                if (mObject2.get(VersionObjectEnum.CHECKOUT_BY.getCode()) != null) {
                    String checkOutBy = mObject2.get(VersionObjectEnum.CHECKOUT_BY.getCode()) + "";
                    if (StringUtil.isNotBlank(checkOutBy) && checkOutBy.equals(SsoHelper.getJobNumber())) {
                        mObject2.put("dataSource", "draft");
                        return mObject2;
                    }
                }
            }
        }
        //取历史
        if (mObject == null) {
            mObject = getHisByBid(modelCode, bid);
            if (mObject == null) {
                return null;
            }
            mObject.put("dataSource", "history");
            return mObject;
        }
        mObject.put("dataSource", "instance");
        //判断是否检出
        if (mObject.get(VersionObjectEnum.CHECKOUT_BY.getCode()) != null) {
            String checkOutBy = mObject.get(VersionObjectEnum.CHECKOUT_BY.getCode()) + "";
            if (StringUtil.isNotBlank(checkOutBy) && checkOutBy.equals(SsoHelper.getJobNumber())) {
                //查询草稿表
                DraftPO draftPO = draftRepository.getByDataBid(mObject.get(TranscendModelBaseFields.DATA_BID) + "");
                if (draftPO == null || StringUtil.isBlank(draftPO.getContent())) {
                    return mObject;
                }
                mObject = JSON.parseObject(draftPO.getContent(), MObject.class);
                mObject.put("dataSource", "draft");
            }
        }
        return mObject;
    }

    /**
     * @param modelCode
     * @param bid
     * @param isObjVersion
     * @return {@link MObject }
     */
    @Override
    public MObject getByBid(String modelCode, String bid, Boolean isObjVersion) {
        MObject mObject = objectModelDomainService.getByBid(modelCode, bid);
        if ((isObjVersion == null || !isObjVersion) && mObject == null) {
            return mObject;
        }
        if (mObject == null) {
            //直接从草稿取
            DraftPO draftPO = draftRepository.getByBid(bid);
            if (draftPO != null && StringUtil.isNotBlank(draftPO.getContent())) {
                MObject mObject2 = JSON.parseObject(draftPO.getContent(), MObject.class);
                if (mObject2.get(VersionObjectEnum.CHECKOUT_BY.getCode()) != null) {
                    String checkOutBy = mObject2.get(VersionObjectEnum.CHECKOUT_BY.getCode()) + "";
                    if (StringUtil.isNotBlank(checkOutBy) && checkOutBy.equals(SsoHelper.getJobNumber())) {
                        mObject2.put("dataSource", "draft");
                        return mObject2;
                    }
                }
            }
        }
        //取历史
        if (mObject == null) {
            mObject = getHisByBid(modelCode, bid);
            if (mObject == null) {
                return null;
            }
            mObject.put("dataSource", "history");
            return mObject;
        }
        mObject.put("dataSource", "instance");
        //判断是否检出
        if (mObject.get(VersionObjectEnum.CHECKOUT_BY.getCode()) != null) {
            String checkOutBy = mObject.get(VersionObjectEnum.CHECKOUT_BY.getCode()) + "";
            if (StringUtil.isNotBlank(checkOutBy) && checkOutBy.equals(SsoHelper.getJobNumber())) {
                //查询草稿表
                DraftPO draftPO = draftRepository.getByDataBid(mObject.get(TranscendModelBaseFields.DATA_BID) + "");
                if (draftPO == null || StringUtil.isBlank(draftPO.getContent())) {
                    return mObject;
                }
                mObject = JSON.parseObject(draftPO.getContent(), MObject.class);
                mObject.put("dataSource", "draft");
            }
        }
        return mObject;
    }

    /**
     * @param modelCode
     * @param bid
     * @return {@link MObject }
     */
    @Override
    public MObject getHisByBid(String modelCode, String bid) {
        // 判断是否版本对象
        CfgObjectVo cfgObjectVo = cfgObjectClient.getByModelCode(modelCode).getCheckExceptionData();
        if (cfgObjectVo != null && ObjectTypeEnum.VERSION.getCode().equals(cfgObjectVo.getType())) {
            return objectModelDomainService.getHisByBid(modelCode, bid);
        }
        return null;
    }

    @Override
    public PagedResult<MObject> page(String modelCode, BaseRequest<QueryCondition> pageQo, boolean filterRichText) {
        return page(modelCode, pageQo, filterRichText, null);
    }

    /**
     * 分页查询(需要部分结果集)
     *
     * @param modelCode      模型code
     * @param pageQo         分页查询条件
     * @param filterRichText 是否过滤富文本
     * @param fields
     * @return Boolean
     */
    @Override
    public PagedResult<MObject> page(String modelCode, BaseRequest<QueryCondition> pageQo, boolean filterRichText, Set<String> fields) {
        PagedResult<MObject> result = objectModelDomainService.page(modelCode, pageQo, filterRichText, fields);
        result.getData().forEach(mObject -> {
            if (filterRichText) {
                filterFields.forEach(mObject::remove);
            }
        });
        return result;
    }

    @Override
    public List<MObject> relationSelectList(String modelCode, MObjectCheckDto mObjectCheckDto) {
        List<QueryWrapper> wrappers = QueryConveterTool.convert(mObjectCheckDto.getModelMixQo()).getQueries();
        //查询实例数据
        List<MObject> list = listByQueryWrapper(modelCode, wrappers);
        if (CollectionUtils.isNotEmpty(list)) {
            //查询关系数据
            RelationMObject relationMObject = RelationMObject.builder().sourceBid(mObjectCheckDto.getSourceBid()).relationModelCode(mObjectCheckDto.getRelationModelCode()).build();
            List<MObject> mRelationObjectList = listOnlyRelationMObjects(relationMObject);
            Map<String, String> relModelMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
            if (CollectionUtils.isNotEmpty(mRelationObjectList)) {
                //树形结构选中处理
                for (MObject mObject : mRelationObjectList) {
                    relModelMap.put(mObject.get("targetBid") + "", mObject.get(TranscendModelBaseFields.BID) + "");
                }
            }
            for (int i = list.size() - 1; i >= 0; i--) {
                if (relModelMap.containsKey(list.get(i).getBid())) {
                    list.remove(i);
                    continue;
                }
                MObject mObject = list.get(i);
                filterFields.forEach(field -> mObject.remove(field));
            }
        }
        return list;
    }

    @Override
    public List<MObject> relationSelectList(List<MObject> list, MObjectCheckDto mObjectCheckDto) {
        if (CollectionUtils.isNotEmpty(list)) {
            //查询关系数据
            RelationMObject relationMObject = RelationMObject.builder().sourceBid(mObjectCheckDto.getSourceBid()).relationModelCode(mObjectCheckDto.getRelationModelCode()).build();
            List<MObject> mRelationObjectList = listOnlyRelationMObjects(relationMObject);
            Map<String, String> relModelMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
            if (CollectionUtils.isNotEmpty(mRelationObjectList)) {
                //树形结构选中处理
                for (MObject mObject : mRelationObjectList) {
                    relModelMap.put(mObject.get("targetBid") + "", mObject.get(TranscendModelBaseFields.BID) + "");
                }
            }
            for (int i = list.size() - 1; i >= 0; i--) {
                if (relModelMap.containsKey(list.get(i).getBid())) {
                    list.remove(i);
                    continue;
                }
                MObject mObject = list.get(i);
                filterFields.forEach(field -> mObject.remove(field));
            }
        }
        return list;
    }

    @Override
    public Boolean addBatch(String modelCode, List<MObject> mObjects) {
        if (CollUtil.isEmpty(mObjects)) {
            return Boolean.TRUE;
        }
        return objectModelDomainService.addBatch(modelCode, mObjects);
    }

    @Override
    public List<MObject> listByQueryWrapper(String modelCode, List<QueryWrapper> queryWrappers) {
        if (StringUtils.isEmpty(modelCode) || CollectionUtils.isEmpty(queryWrappers)) {
            return null;
        }
        return objectModelDomainService.list(modelCode, queryWrappers);
    }

    @Override
    public List<MObject> listByQueryCondition(String modelCode, QueryCondition queryCondition) {
        if (queryCondition == null) {
            return null;
        }
        return objectModelDomainService.list(modelCode, queryCondition);
    }

    public PagedResult<MObject> listByQueryConditionPage(String modelCode, BaseRequest<QueryCondition> queryCondition, boolean filterRichText) {
        if (queryCondition.getParam() == null) {
            return null;
        }
        return objectModelDomainService.page(modelCode, queryCondition, filterRichText);
    }

    /**
     * @param modelCode
     * @param source
     * @param mObjectCheckDto
     * @return {@link List }<{@link MObject }>
     */
    @Override
    public List<MObject> relationSelectListExpand(String modelCode, String source, MObjectCheckDto mObjectCheckDto) {
        if ("inner".equals(source)) {
            return relationSelectList(modelCode, mObjectCheckDto);
        } else if ("outer".equals(source)) {
            switch (mObjectCheckDto.getRelationModelCode()) {
                case "A3H": {
                    ModelFilterQo queryCondition = mObjectCheckDto.getModelMixQo().getQueries().stream()
                            .filter(query -> "name".equals(query.getProperty()))
                            .findFirst()
                            .orElseThrow(() -> new PlmBizException("查询条件中必须包含name属性"));
                    String conditionValue = (String) queryCondition.getValue();
                    PageDTO<PlatFormUserDTO> userData = platformService.queryPlatformUser(conditionValue);
                    return userData.getDataList().stream().map(user -> {
                        MObject mObject = new MObject();
                        mObject.setModelCode("A3H");
                        mObject.setBid(SnowflakeIdWorker.nextIdStr());
                        mObject.put("name", user.getEmployeeName());
                        mObject.put("perno", user.getEmployeeNo());
                        return mObject;
                    }).collect(Collectors.toList());
                }
                default: {
                }
            }
        }
        return null;
    }

    /**
     * @param mObjectTreeList
     * @return {@link List }<{@link MObjectTree }>
     */
    @Override
    public List<MObjectTree> convertToTree(List<MObjectTree> mObjectTreeList) {
        return baseObjectTreeService.convert2Tree(mObjectTreeList);
    }

    /**
     * @param modelCode
     * @param distinctProperty
     * @param queryCondition
     * @return
     */
    @Override
    public List<Object> listPropertyDistinct(String modelCode, String distinctProperty, QueryCondition queryCondition) {
        return objectModelDomainService.listPropertyDistinct(modelCode, distinctProperty, queryCondition);
    }

    @Override
    public MVersionObject revise(ReviseDto reviseDto) {
        return relationObjectManageService.revise(reviseDto);
    }

    /**
     * @param promoteDto
     * @return {@link MVersionObject }
     */
    @Override
    public MVersionObject promote(LifeCyclePromoteDto promoteDto) {
        return relationObjectManageService.promote(promoteDto);

    }

    @Override
    public int count(String modelCode, List<QueryWrapper> wrappers) {
        return objectModelDomainService.count(modelCode, wrappers);
    }

    /**
     * @param relationMObject
     * @return {@link List }<{@link MObject }>
     */
    @Override
    public List<MObject> listOnlyRelationMObjects(RelationMObject relationMObject) {
        //先查询关系实例表
        QueryWrapper qo = new QueryWrapper();
        if (CollectionUtils.isEmpty(relationMObject.getSourceBids())) {
            if (StringUtils.isEmpty(relationMObject.getSourceBid()) || StringUtils.isEmpty(relationMObject.getSourceBid().trim())) {
                return new ArrayList<>();
            }
            qo.eq(RelationEnum.SOURCE_BID.getColumn(), relationMObject.getSourceBid());
        } else {
            qo.in(RelationEnum.SOURCE_BID.getColumn(), relationMObject.getSourceBids());
        }
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        if (CollectionUtils.isNotEmpty(relationMObject.getTargetBids())) {
            QueryWrapper qo2 = new QueryWrapper();
            qo2.in(RelationEnum.TARGET_BID.getColumn(), relationMObject.getTargetBids());
            queryWrappers = QueryConveterTool.appendMoreQueriesAndCondition(queryWrappers, QueryWrapper.buildSqlQo(qo2));
        }
        List<MObject> mRelationObjectList;
        try {
            mRelationObjectList = objectModelDomainService.list(relationMObject.getRelationModelCode(), queryWrappers);
        } catch (Exception e) {
            log.error("错误信息", e);
            throw new BusinessException(String.format("查询关系实例表异常，可能是数据问题或者表不存在，关系对象模型编码[%s]", relationMObject.getRelationModelCode()));
        }
        return mRelationObjectList;
    }

    /**
     * @param relationMObject
     * @return {@link List }<{@link MObject }>
     */
    @Override
    public List<MObject> listNotVersionRelationMObjects(RelationMObject relationMObject) {
        //先查询关系实例表
        QueryWrapper qo = new QueryWrapper();
        if (CollectionUtils.isEmpty(relationMObject.getSourceBids())) {
            qo.eq(RelationEnum.SOURCE_BID.getColumn(), relationMObject.getSourceBid());
        } else {
            qo.in(RelationEnum.SOURCE_BID.getColumn(), relationMObject.getSourceBids());
        }
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        List<MObject> mRelationObjectList;
        try {
            mRelationObjectList = objectModelDomainService.list(relationMObject.getRelationModelCode(), queryWrappers);
        } catch (Exception e) {
            throw new BusinessException(String.format("查询关系实例表异常，可能是数据问题或者表不存在，关系对象模型编码[%s]", relationMObject.getRelationModelCode()));
        }
        if (CollectionUtils.isEmpty(mRelationObjectList)) {
            return Arrays.asList();
        }
        return listNoVersionRelation(relationMObject, null, mRelationObjectList);
    }

    /**
     * @param relationMObject
     * @return {@link List }<{@link MObject }>
     */
    @Override
    public List<MObject> listRelationMObjects(RelationMObject relationMObject) {
        //查询目标对象
        CfgObjectVo cfgObjectVo = cfgObjectClient.getByModelCode(relationMObject.getTargetModelCode()).getCheckExceptionData();
        if (cfgObjectVo == null) {
            throw new TranscendBizException(String.format("模型[%s]不存在", relationMObject.getSourceModelCode()));
        }
        //源对象是否是版本对象
        Boolean targetVersion = "VERSION".equals(cfgObjectVo.getType());
        return listRelationMObjects(targetVersion, relationMObject);
    }

    /**
     * @param targetVersion
     * @param relationMObject
     * @return {@link List }<{@link MObject }>
     */
    @Override
    public List<MObject> listRelationMObjects(Boolean targetVersion, RelationMObject relationMObject) {
        //查询源实例数据
        MObject mObject = getByBid(relationMObject.getSourceModelCode(), relationMObject.getSourceBid(), targetVersion);
        if (mObject == null) {
            throw new BusinessException("源实例数据为空");
        }
        //如果是历史版本，需要查关系历史表
        boolean isHis = "history".equals(mObject.get("dataSource"));
        List<MObject> mRelationObjectList = listRelationDataBySourceBid(isHis, relationMObject);
        if (CollectionUtils.isEmpty(mRelationObjectList)) {
            return null;
        }
        boolean targetIsEmptyObj = mRelationObjectList.stream().anyMatch(
                m -> Objects.isNull(m.get(RelationEnum.TARGET_BID.getCode()))
                        || StringUtils.isBlank(m.get(RelationEnum.TARGET_BID.getCode()).toString()));
        if (targetIsEmptyObj) {
            return mRelationObjectList;
        }

        ObjectRelationRuleQo objectRelationRuleQo = new ObjectRelationRuleQo();
        objectRelationRuleQo.setLcTemplateBid(mObject.get(TranscendModelBaseFields.LC_TEMPL_BID) + "");
        objectRelationRuleQo.setVersion(mObject.get(TranscendModelBaseFields.LC_TEMPL_VERSION) + "");
        objectRelationRuleQo.setFromLifeCycleCode(mObject.getLifeCycleCode());
        objectRelationRuleQo.setRelationModelCode(relationMObject.getRelationModelCode());
        String relRule = cfgObjectRelationClient.getRelationRuleRes(objectRelationRuleQo).getCheckExceptionData();
        Boolean isInstance = getTargetModelRoute(targetVersion, relRule, isHis);
        if (isInstance) {
            return getMObjectByBids(mRelationObjectList, relationMObject, relationMObject.getModelMixQo());
        } else {
            return getMVersionObjectByBids(mRelationObjectList, relationMObject.getTargetModelCode(), relationMObject.getModelMixQo());
        }
    }

    /**
     * @param targetVersion
     * @param pageParam
     * @param spaceAppBid
     * @param filterRichText
     * @return {@link PagedResult }<{@link MObject }>
     */
    @Override
    public PagedResult<MObject> listRelationMObjectsPage(Boolean targetVersion, BaseRequest<RelationMObject> pageParam, String spaceAppBid, boolean filterRichText) {
        RelationMObject relationMObject = pageParam.getParam();
        //查询源实例数据
        MObject mObject = getByBid(relationMObject.getSourceModelCode(), relationMObject.getSourceBid());
        if (mObject == null) {
            throw new BusinessException("源实例数据为空");
        }
        //如果是历史版本，需要查关系历史表
        boolean isHis = "history".equals(mObject.get("dataSource"));
        PagedResult<MObject> relationPagedResult = listRelationPageDataBySourceBid(isHis, pageParam, relationMObject, filterRichText);
        if (Objects.isNull(relationPagedResult) || CollectionUtils.isEmpty(relationPagedResult.getData())) {
            return null;
        }
        boolean targetIsEmptyObj = relationPagedResult.getData().stream().anyMatch(
                m -> Objects.isNull(m.get(RelationEnum.TARGET_BID.getCode()))
                        || StringUtils.isBlank(m.get(RelationEnum.TARGET_BID.getCode()).toString()));
        if (targetIsEmptyObj) {
            return relationPagedResult;
        }

        ObjectRelationRuleQo objectRelationRuleQo = new ObjectRelationRuleQo();
        objectRelationRuleQo.setLcTemplateBid(mObject.get(TranscendModelBaseFields.LC_TEMPL_BID) + "");
        objectRelationRuleQo.setVersion(mObject.get(TranscendModelBaseFields.LC_TEMPL_VERSION) + "");
        objectRelationRuleQo.setFromLifeCycleCode(mObject.getLifeCycleCode());
        objectRelationRuleQo.setRelationModelCode(relationMObject.getRelationModelCode());
        String relRule = cfgObjectRelationClient.getRelationRuleRes(objectRelationRuleQo).getCheckExceptionData();
        Boolean isInstance = getTargetModelRoute(targetVersion, relRule, isHis);
        if (isInstance) {
            return getMObjectByDataBidsPage(relationPagedResult.getData(), relationMObject, pageParam, filterRichText);
        } else {
            return getMVersionObjectByBidsPage(relationPagedResult.getData(), relationMObject.getTargetModelCode(), pageParam, filterRichText);
        }
    }

    /**
     * @param targetVersion
     * @param pageParam
     * @param relationBehavior
     * @param spaceAppBid
     * @param filterRichText
     * @return {@link PagedResult }<{@link MObject }>
     */
    @Override
    public PagedResult<MObject> listRelationMObjectsPage(Boolean targetVersion, BaseRequest<RelationMObject> pageParam, String relationBehavior, String spaceAppBid, boolean filterRichText) {
        RelationMObject relationMObject = pageParam.getParam();
        //查询源实例数据
        MObject mObject = getByBid(relationMObject.getSourceModelCode(), relationMObject.getSourceBid());
        if (mObject == null) {
            throw new BusinessException("源实例数据为空");
        }
        //如果是历史版本，需要查关系历史表
        boolean isHis = "history".equals(mObject.get("dataSource"));
        PagedResult<MObject> relationPagedResult = listRelationPageDataBySourceBid(isHis, pageParam, relationMObject, filterRichText);
        if (Objects.isNull(relationPagedResult) || CollectionUtils.isEmpty(relationPagedResult.getData())) {
            return null;
        }
        boolean targetIsEmptyObj = relationPagedResult.getData().stream().anyMatch(
                m -> Objects.isNull(m.get(RelationEnum.TARGET_BID.getCode()))
                        || StringUtils.isBlank(m.get(RelationEnum.TARGET_BID.getCode()).toString()));
        if (targetIsEmptyObj) {
            return relationPagedResult;
        }

        Boolean isInstance = getTargetModelRoute(targetVersion, relationBehavior, isHis);
        if (isInstance) {
            return getMObjectByDataBidsPage(relationPagedResult.getData(), relationMObject, pageParam, filterRichText);
        } else {
            return getMVersionObjectByBidsPage(relationPagedResult.getData(), relationMObject.getTargetModelCode(), pageParam, filterRichText);
        }
    }

    /**
     * @param targetVersion
     * @param relationBehavior
     * @param isHis
     * @return 返回true表示是查实例表，返回false表示查历史实例表
     */
    public Boolean getTargetModelRoute(Boolean targetVersion, String relationBehavior, Boolean isHis) {
        //目标对象不是有版本对象，目标查实例表
        if (!targetVersion) {
            return Boolean.TRUE;
        }
        //源对象是历史实例，目标查历史表
        if (isHis) {
            return Boolean.FALSE;
        }
        //关系行为为浮动，目标查实例表
        if (RelationBehaviorEnum.FLOAT.getCode().equals(relationBehavior)) {
            return Boolean.TRUE;
        }
        //关系行为为固定，目标查历史表
        return Boolean.FALSE;
    }

    /**
     * @param isHis
     * @param relationMObject
     * @return {@link List }<{@link MObject }>
     */
    private List<MObject> listRelationDataBySourceBid(Boolean isHis, RelationMObject relationMObject) {
        //先查询关系(历史)实例表
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.SOURCE_BID.getColumn(), relationMObject.getSourceBid());
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        List<MObject> mRelationObjectList;
        try {
            QueryCondition queryCondition = new QueryCondition();
            // 默认更新时间倒序
            queryCondition.setOrders(Lists.newArrayList(Order.of().setProperty(BaseDataEnum.UPDATED_TIME.getCode()).setDesc(true)));
            queryCondition.setQueries(queryWrappers);
            if (isHis) {
                mRelationObjectList = objectModelDomainService.listHis(relationMObject.getRelationModelCode(), queryCondition);
            } else {
                mRelationObjectList = objectModelDomainService.list(relationMObject.getRelationModelCode(), queryCondition);
            }
        } catch (Exception e) {
            throw new BusinessException(String.format("查询关系%s实例表异常，可能是数据问题或者表不存在，关系对象模型编码[%s]", isHis ? "历史" : "", relationMObject.getRelationModelCode()));
        }
        return mRelationObjectList;
    }

    /**
     * @param isHis
     * @param pageParam
     * @param relationMObject
     * @param filterRichText
     * @return {@link PagedResult }<{@link MObject }>
     */
    private PagedResult<MObject> listRelationPageDataBySourceBid(Boolean isHis, BaseRequest<RelationMObject> pageParam,
                                                                 RelationMObject relationMObject, boolean filterRichText) {
        //先查询关系(历史)实例表
        QueryWrapper qo = new QueryWrapper();
        qo.eq(relationMObject.sourceBidFieldName(), relationMObject.getSourceBid());
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        try {
            QueryCondition queryCondition = new QueryCondition();
            // 默认更新时间倒序
            queryCondition.setOrders(Lists.newArrayList(Order.of().setProperty(BaseDataEnum.UPDATED_TIME.getCode()).setDesc(true)));
            queryCondition.setQueries(queryWrappers);
            BaseRequest<QueryCondition> pageQo = new BaseRequest<>();
            pageQo.setParam(queryCondition);
            pageQo.setCurrent(pageParam.getCurrent());
            pageQo.setSize(pageParam.getSize());
            if (isHis) {
                return objectModelDomainService.hisPage(relationMObject.getRelationModelCode(), pageQo, filterRichText);
            } else {
                return objectModelDomainService.page(relationMObject.getRelationModelCode(), pageQo, filterRichText);
            }
        } catch (Exception e) {
            throw new BusinessException(String.format("查询关系%s实例表异常，可能是数据问题或者表不存在，关系对象模型编码[%s]", isHis ? "历史" : "", relationMObject.getRelationModelCode()));
        }
    }

    /**
     * @param relationModelCode
     * @param targetModelCode
     * @param mObject
     * @param sourceBid
     * @return {@link List }<{@link MObject }>
     */
    @Override
    public List<MObject> listNoVersionRelationMObjects(String relationModelCode, String targetModelCode, MObject mObject, String sourceBid) {
        //先查询关系实例表
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.SOURCE_BID.getColumn(), sourceBid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        //查询源对象
        List<MObject> mRelationObjectList;
        try {
            QueryCondition queryCondition = new QueryCondition();
            queryCondition.setQueries(queryWrappers);
            mRelationObjectList = objectModelDomainService.list(relationModelCode, queryCondition);
        } catch (Exception e) {
            throw new BusinessException(String.format("查询关系实例表异常，可能是数据问题或者表不存在，关系对象模型编码[%s]", relationModelCode));
        }
        //查询源实例数据
        RelationMObject relationMObject = RelationMObject.builder().build();
        relationMObject.setTargetModelCode(targetModelCode);
        return listNoVersionRelation(relationMObject, mObject, mRelationObjectList);
    }

    /**
     * 无版本对象直接通过浮动来 TODO ,后续无版本的浮动按照bid的方式来
     *
     * @param relationParam       关系入参
     * @param mObject             源对象实例
     * @param mRelationObjectList 关系实例数据
     * @return
     */
    @NotNull
    private List<MObject> listNoVersionRelation(RelationMObject relationParam, MObject mObject, List<MObject> mRelationObjectList) {
        Map<String, MObject> relModelMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        List<Object> targetBids = new ArrayList<>();
        // 收集关系实例数据的信息，给下边使用
        for (MObject relationData : mRelationObjectList) {
            targetBids.add(relationData.get(RelationEnum.TARGET_BID.getCode()) + "");
            relModelMap.put(relationData.get(RelationEnum.TARGET_BID.getCode()) + "", relationData);
        }
        QueryWrapper qo = new QueryWrapper();
        qo.in(RelationEnum.BID.getColumn(), targetBids);
        List<QueryWrapper> queryWrappers = new ArrayList<>();
        ModelMixQo modelMixQo = relationParam.getModelMixQo();
        QueryCondition queryCondition = new QueryCondition();
        if (modelMixQo != null) {
            queryCondition.setOrders(modelMixQo.getOrders());
            List<ModelFilterQo> queries = modelMixQo.getQueries();
            if (queries == null) {
                queries = new ArrayList<>();
            }
            modelMixQo.setQueries(queries);
            queryWrappers = QueryConveterTool.convert(modelMixQo).getQueries();
        }
        if (CollectionUtils.isNotEmpty(queryWrappers)) {
            queryWrappers = QueryConveterTool.appendMoreQueriesAndCondition(queryWrappers, QueryWrapper.buildSqlQo(qo));
        } else {
            queryWrappers = QueryWrapper.buildSqlQo(qo);
        }
        queryCondition.setQueries(queryWrappers);
        List<MObject> targetDataList = listByQueryCondition(relationParam.getTargetModelCode(), queryCondition);
        List<MObject> results = new ArrayList<>();
        if (modelMixQo != null && CollectionUtils.isNotEmpty(modelMixQo.getOrders())) {
            List<MObject> sortList = ObjectTreeTools.sortObjectList(modelMixQo.getOrders(), targetDataList);
            sortList.forEach(e -> {
                results.add(e);
                e.put(RelationConst.RELATION_LIST_RELATION_TAG,
                        relModelMap.get(e.getBid()));
            });
        } else {
            // 转换为map
            Map<String, MObject> targetDataMap = Optional.of(targetDataList).orElse(new ArrayList<>())
                    .stream().collect(Collectors.toMap(MBaseData::getBid, m -> m, (k1, k2) -> k1));
            // 按照关系顺序排序
            targetBids.forEach(targetBid -> {
                MObject targetData = targetDataMap.get(targetBid.toString());
                if (targetData != null) {
                    results.add(targetData);
                    // 收集关系数据到目标对象中
                    targetData.put(RelationConst.RELATION_LIST_RELATION_TAG,
                            relModelMap.get(targetData.getBid()));
                }
            });
        }
        return results;
    }

    /**
     * 无版本对象直接通过浮动来 TODO ,后续无版本的浮动按照bid的方式来
     *
     * @param pageParam           关系入参
     * @param mRelationObjectList 关系实例数据
     * @param filterRichText
     * @return
     */
    @NotNull
    private PagedResult<MObject> listNoVersionRelationPage(BaseRequest<RelationMObject> pageParam, List<MObject> mRelationObjectList, boolean filterRichText) {
        RelationMObject relationParam = pageParam.getParam();
        Map<String, MObject> relModelMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        List<Object> targetBids = new ArrayList<>();
        // 收集关系实例数据的信息，给下边使用
        for (MObject relationData : mRelationObjectList) {
            targetBids.add(relationData.get(RelationEnum.TARGET_BID.getCode()) + "");
            relModelMap.put(relationData.get(RelationEnum.TARGET_BID.getCode()) + "", relationData);
        }
        QueryWrapper qo = new QueryWrapper();
        qo.in(RelationEnum.BID.getColumn(), targetBids);
        List<QueryWrapper> queryWrappers = new ArrayList<>();
        ModelMixQo modelMixQo = relationParam.getModelMixQo();
        QueryCondition queryCondition = new QueryCondition();
        if (modelMixQo != null) {
            queryCondition.setOrders(modelMixQo.getOrders());
            List<ModelFilterQo> queries = modelMixQo.getQueries();
            if (queries == null) {
                queries = new ArrayList<>();
            }
            modelMixQo.setQueries(queries);
            queryWrappers = QueryConveterTool.convert(modelMixQo).getQueries();
        }
        if (CollectionUtils.isNotEmpty(queryWrappers)) {
            queryWrappers = QueryConveterTool.appendMoreQueriesAndCondition(queryWrappers, QueryWrapper.buildSqlQo(qo));
        } else {
            queryWrappers = QueryWrapper.buildSqlQo(qo);
        }
        queryCondition.setQueries(queryWrappers);
        BaseRequest<QueryCondition> pageQo = new BaseRequest<>();
        pageQo.setParam(queryCondition);
        pageQo.setCurrent(pageParam.getCurrent());
        pageQo.setSize(pageParam.getSize());
        PagedResult<MObject> pageData = listByQueryConditionPage(relationParam.getTargetModelCode(), pageQo, filterRichText);
        List<MObject> targetDataList = pageData.getData();
        List<MObject> results = new ArrayList<>();
        if (modelMixQo != null && CollectionUtils.isNotEmpty(modelMixQo.getOrders())) {
            List<MObject> sortList = ObjectTreeTools.sortObjectList(modelMixQo.getOrders(), targetDataList);
            sortList.forEach(e -> {
                results.add(e);
                e.put(RelationConst.RELATION_LIST_RELATION_TAG,
                        relModelMap.get(e.getBid()));
            });
        } else {
            // 转换为map
            Map<String, MObject> targetDataMap = Optional.of(targetDataList).orElse(new ArrayList<>())
                    .stream().collect(Collectors.toMap(MBaseData::getBid, m -> m, (k1, k2) -> k1));
            // 按照关系顺序排序
            targetBids.forEach(targetBid -> {
                MObject targetData = targetDataMap.get(targetBid.toString());
                if (targetData != null) {
                    results.add(targetData);
                    // 收集关系数据到目标对象中
                    targetData.put(RelationConst.RELATION_LIST_RELATION_TAG,
                            relModelMap.get(targetData.getBid()));
                }
            });
        }
        pageData.setData(results);
        return pageData;
    }

    /**
     * @param mObjectList
     * @param relationObject
     * @param pageParam
     * @param filterRichText
     * @return {@link PagedResult }<{@link MObject }>
     */
    private PagedResult<MObject> getMObjectByDataBidsPage(List<MObject> mObjectList, RelationMObject relationObject,
                                                          BaseRequest<RelationMObject> pageParam, boolean filterRichText) {
        String targetBidFieldName = relationObject.targetBidFieldName();
        String modelCode = relationObject.getTargetModelCode();

        ModelMixQo modelMixQo = pageParam.getParam().getModelMixQo();
        //浮动关系直接根据dataBid查询
        QueryCondition targetDataQueryParam = buildBidQuery(mObjectList, modelMixQo, targetBidFieldName);
        BaseRequest<QueryCondition> pageQo = new BaseRequest<>();
        pageQo.setParam(targetDataQueryParam);
        pageQo.setCurrent(pageParam.getCurrent());
        pageQo.setSize(pageParam.getSize());

        //ALM特殊逻辑，添加保密权限控制
        targetDataQueryParam.setQueries(SecrecyWrapperHandler.autoAddSecrecyWrapper(
                targetDataQueryParam.getQueries(), modelCode, SsoHelper.getJobNumber()));


        PagedResult<MObject> resList = objectModelDomainService.page(modelCode, pageQo, filterRichText);
        Map<String, MObject> relModelMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        for (MObject mObject : mObjectList) {
            relModelMap.put(mObject.get(targetBidFieldName) + "", mObject);
        }
        for (MObject mObject : resList.getData()) {
            mObject.put("dataSource", "instance");
            mObject.put(RelationConst.RELATION_LIST_RELATION_TAG, relModelMap.get(mObject.get(TranscendModelBaseFields.DATA_BID) + ""));
        }
        return resList;
    }

    /**
     * @param mObjectList
     * @param modelMixQo
     * @return {@link List }<{@link MObject }>
     */
    private List<MObject> getMObjectByBids(List<MObject> mObjectList, RelationMObject relationMObject, ModelMixQo modelMixQo) {

        String modelCode = relationMObject.getTargetModelCode();
        String targetBidFieldName = relationMObject.targetBidFieldName();

        //浮动关系直接根据dataBid查询
        QueryCondition targetDataQueryParam = buildBidQuery(mObjectList, modelMixQo, targetBidFieldName);
        List<MObject> resList = objectModelDomainService.list(modelCode, targetDataQueryParam);
        Map<String, MObject> relModelMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        for (MObject mObject : mObjectList) {
            relModelMap.put(mObject.get(targetBidFieldName) + "", mObject);
        }
        for (MObject mObject : resList) {
            mObject.put("dataSource", "instance");
            mObject.put(RelationConst.RELATION_LIST_RELATION_TAG, relModelMap.get(mObject.get(TranscendModelBaseFields.BID) + ""));
        }
        return resList;
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
            qo.in(RelationEnum.BID.getColumn(), targetBids);
            List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
            List<QueryWrapper> queries = QueryConveterTool.convert(modelMixQo.getQueries(), modelMixQo.getAnyMatch());
            List<QueryWrapper> queryWrapperList = QueryConveterTool.appendMoreQueriesAndCondition(queryWrappers, queries);
            targetDataQueryParam.setQueries(queryWrapperList);
            if (CollectionUtils.isNotEmpty(modelMixQo.getOrders())) {
                targetDataQueryParam.setOrders(modelMixQo.getOrders());
            }
        } else {
            QueryWrapper qo = new QueryWrapper();
            qo.in(RelationEnum.BID.getColumn(), targetBids);
            List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
            targetDataQueryParam.setQueries(queryWrappers);
            if (Objects.nonNull(modelMixQo) && CollectionUtils.isNotEmpty(modelMixQo.getOrders())) {
                targetDataQueryParam.setOrders(modelMixQo.getOrders());
            }
        }
        return targetDataQueryParam;
    }

    /**
     * @param mObjectList
     * @param modelCode
     * @param modelMixQo
     * @return {@link List }<{@link MObject }>
     */
    private List<MObject> getMVersionObjectByBids(List<MObject> mObjectList, String modelCode, ModelMixQo modelMixQo) {
        //固定关系 根据bid直接查历史表
        List<String> bids = new ArrayList<>();
        Map<String, MObject> relModelMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        for (MObject mObject : mObjectList) {
            bids.add(mObject.get("targetBid") + "");
            relModelMap.put(mObject.get("targetBid") + "", mObject);
        }
        QueryWrapper qo3 = new QueryWrapper();
        qo3.in(RelationEnum.BID.getColumn(), bids).and().eq(BaseDataEnum.DELETE_FLAG.getColumn(), 0);
        List<QueryWrapper> queryWrappers3 = QueryWrapper.buildSqlQo(qo3);
        List<MObject> resList3 = objectModelDomainService.listHis(modelCode, queryWrappers3);
        List<String> bidList = new ArrayList<>();
        for (MObject mObject : resList3) {
            bidList.add(mObject.get(TranscendModelBaseFields.BID) + "");
            mObject.put(RelationConst.RELATION_LIST_RELATION_TAG, relModelMap.get(mObject.get(TranscendModelBaseFields.BID) + ""));
        }
        //查询实例表 需要看是否有被检出
        QueryWrapper qo = new QueryWrapper();
        qo.in(RelationEnum.BID.getColumn(), bidList).and().eq(BaseDataEnum.DELETE_FLAG.getColumn(), 0);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        List<MObject> mObjectList1 = objectModelDomainService.list(modelCode, queryWrappers);
        Map<String, String> checkOutMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        for (MObject mObject : mObjectList1) {
            if (mObject.get(VersionObjectEnum.CHECKOUT_BY.getCode()) != null && StringUtil.isNotBlank(mObject.get(VersionObjectEnum.CHECKOUT_BY.getCode()) + "")) {
                checkOutMap.put(mObject.get(BaseDataEnum.BID.getCode()) + "", mObject.get(VersionObjectEnum.CHECKOUT_BY.getCode()) + "");
            }
        }
        for (MObject mObject : resList3) {
            if (checkOutMap.containsKey(mObject.get(TranscendModelBaseFields.BID) + "")) {
                mObject.put("dataSource", "instance");
            } else {
                mObject.put("dataSource", "history");
            }
            mObject.put(VersionObjectEnum.CHECKOUT_BY.getCode(), checkOutMap.get(mObject.get(TranscendModelBaseFields.BID) + ""));
        }
        return resList3;
    }

    /**
     * @param mObjectList
     * @param modelCode
     * @param pageParam
     * @param filterRichText
     * @return {@link PagedResult }<{@link MObject }>
     */
    private PagedResult<MObject> getMVersionObjectByBidsPage(List<MObject> mObjectList, String modelCode, BaseRequest<RelationMObject> pageParam, boolean filterRichText) {
        ModelMixQo modelMixQo = pageParam.getParam().getModelMixQo();
        //固定关系 根据bid直接查历史表
        List<String> bids = new ArrayList<>();
        Map<String, MObject> relModelMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        for (MObject mObject : mObjectList) {
            bids.add(mObject.get("targetBid") + "");
            relModelMap.put(mObject.get("targetBid") + "", mObject);
        }
        QueryCondition targetDataQueryParam = new QueryCondition();
        if (CollectionUtils.isNotEmpty(modelMixQo.getQueries())) {
            ModelFilterQo targetDataBidFilter = new ModelFilterQo();
            targetDataBidFilter.setProperty(RelationEnum.BID.getColumn());
            targetDataBidFilter.setCondition(QueryFilterConditionEnum.IN.getSqlCondition());
            targetDataBidFilter.setValue(bids);
            modelMixQo.getQueries().add(targetDataBidFilter);
            targetDataQueryParam = QueryConveterTool.convert(modelMixQo);
        } else {
            QueryWrapper qo = new QueryWrapper();
            qo.in(RelationEnum.BID.getColumn(), bids);
            List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
            targetDataQueryParam.setQueries(queryWrappers);
            if (CollectionUtils.isNotEmpty(modelMixQo.getOrders())) {
                targetDataQueryParam.setOrders(modelMixQo.getOrders());
            }
        }
        BaseRequest<QueryCondition> pageQo = new BaseRequest<>();
        pageQo.setParam(targetDataQueryParam);
        pageQo.setCurrent(pageParam.getCurrent());
        pageQo.setSize(pageParam.getSize());
        PagedResult<MObject> resList = objectModelDomainService.hisPage(modelCode, pageQo, filterRichText);
        List<String> bidList = new ArrayList<>();
        for (MObject mObject : resList.getData()) {
            bidList.add(mObject.get(TranscendModelBaseFields.BID) + "");
            mObject.put(RelationConst.RELATION_LIST_RELATION_TAG, relModelMap.get(mObject.get(TranscendModelBaseFields.BID) + ""));
        }
        //查询实例表 需要看是否有被检出
        QueryWrapper qo = new QueryWrapper();
        qo.in(RelationEnum.BID.getColumn(), bidList).and().eq(BaseDataEnum.DELETE_FLAG.getColumn(), 0);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        List<MObject> mObjectList1 = objectModelDomainService.list(modelCode, queryWrappers);
        Map<String, String> checkOutMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        for (MObject mObject : mObjectList1) {
            if (mObject.get(VersionObjectEnum.CHECKOUT_BY.getCode()) != null && StringUtil.isNotBlank(mObject.get(VersionObjectEnum.CHECKOUT_BY.getCode()) + "")) {
                checkOutMap.put(mObject.get(BaseDataEnum.BID.getCode()) + "", mObject.get(VersionObjectEnum.CHECKOUT_BY.getCode()) + "");
            }
        }
        for (MObject mObject : resList.getData()) {
            if (checkOutMap.containsKey(mObject.get(TranscendModelBaseFields.BID) + "")) {
                mObject.put("dataSource", "instance");
            } else {
                mObject.put("dataSource", "history");
            }
            mObject.put(VersionObjectEnum.CHECKOUT_BY.getCode(), checkOutMap.get(mObject.get(TranscendModelBaseFields.BID) + ""));
        }
        return resList;
    }

    /**
     * @param bidList
     * @param modelCode
     * @return {@link List }<{@link MObject }>
     */
    @Override
    public List<MObject> listByBids(List<String> bidList, String modelCode) {
        QueryWrapper qo = new QueryWrapper();
        qo.in(RelationEnum.BID.getColumn(), bidList);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        return objectModelDomainService.list(modelCode, queryWrappers);
    }

    /**
     * @param bidList
     * @param modelCode
     * @return {@link List }<{@link MObject }>
     */
    @Override
    public List<MObject> listByBidsIncludeDelete(List<String> bidList, String modelCode) {
        QueryWrapper qo = new QueryWrapper();
        qo.in(RelationEnum.BID.getColumn(), bidList);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        return objectModelDomainService.listIncludeDelete(modelCode, queryWrappers);
    }

    /**
     * @param bidList
     * @param modelCode
     * @return {@link List }<{@link MObject }>
     */
    private List<MObject> getMObjectByBids(List<String> bidList, String modelCode) {
        //查询实例表 需要看是否有被检出
        QueryWrapper qo = new QueryWrapper();
        qo.in(RelationEnum.BID.getColumn(), bidList);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        return objectModelDomainService.list(modelCode, queryWrappers);
    }

    /**
     * @param relationMObject
     * @return {@link Boolean }
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addRelatons(RelationMObject relationMObject) {
        boolean isDraft = isSourceObjectCheckoutByCurrentUser(relationMObject);

        List<MObject> relMObjects = new ArrayList<>();
        String jobNumber = SsoHelper.getJobNumber();
        Long tenantId = SsoHelper.getTenantId();
        for (int i = relationMObject.getTargetMObjects().size() - 1; i >= 0; i--) {
            MObject mObject = relationMObject.getTargetMObjects().get(i);
            String bid = mObject.getBid();
            String dataBid = mObject.get(TranscendModelBaseFields.DATA_BID) + "";
            boolean isSelected = false;
            if (StringUtils.isEmpty(mObject.getBid()) || mObject.get(TranscendModelBaseFields.DATA_BID) == null) {
                bid = SnowflakeIdWorker.nextIdStr();
                dataBid = SnowflakeIdWorker.nextIdStr();
            } else {
                isSelected = true;
            }
            mObject.setBid(bid);
            mObject.setUpdatedBy(jobNumber);
            mObject.setCreatedBy(mObject.getUpdatedBy());
            mObject.put(TranscendModelBaseFields.DATA_BID, dataBid);
            mObject.setUpdatedTime(LocalDateTime.now());
            mObject.setCreatedTime(LocalDateTime.now());
            MObject relMObject = new MObject();
            relMObject.setBid(bid);
            relMObject.setCreatedBy(mObject.getCreatedBy());
            relMObject.setUpdatedBy(mObject.getUpdatedBy());
            relMObject.put("sourceBid", relationMObject.getSourceBid());
            relMObject.put("targetBid", bid);
            relMObject.put("sourceDataBid", relationMObject.getSourceDataBid());
            relMObject.put("targetDataBid", dataBid);
            relMObject.put("draft", isDraft);
            relMObject.put(TranscendModelBaseFields.BID, SnowflakeIdWorker.nextIdStr());
            relMObject.put(TranscendModelBaseFields.DATA_BID, SnowflakeIdWorker.nextIdStr());
            relMObject.setModelCode(relationMObject.getRelationModelCode());
            relMObject.put(TranscendModelBaseFields.DATA_BID, SnowflakeIdWorker.nextIdStr());
            relMObject.setCreatedBy(mObject.getCreatedBy());
            relMObject.setUpdatedBy(mObject.getCreatedBy());
            relMObject.setUpdatedTime(LocalDateTime.now());
            relMObject.setCreatedTime(LocalDateTime.now());
            relMObject.setEnableFlag(true);
            relMObject.setDeleteFlag(false);
            relMObject.setTenantId(tenantId);
            if (CollectionUtils.isNotEmpty(relationMObject.getRelationMObject())) {
                relMObject.putAll(relationMObject.getRelationMObject());
            }
            relMObjects.add(relMObject);
            if (isSelected) {
                relationMObject.getTargetMObjects().remove(i);
            }
        }

        if (CollectionUtils.isNotEmpty(relationMObject.getTargetMObjects())) {
            ObjectModelLifeCycleVO objectModelLifeCycleVO = cfgObjectClient.findObjectLifecycleByModelCode(relationMObject.getTargetModelCode())
                    .getCheckExceptionData();
            for (MObject mObject : relationMObject.getTargetMObjects()) {
                setMObjectDefaultValue(mObject, objectModelLifeCycleVO, relationMObject.getTargetModelCode());
                if (StringUtil.isBlank(mObject.getModelCode())) {
                    mObject.setModelCode(relationMObject.getTargetModelCode());
                }
            }
            //判断目标对象是否是版本对象，如果是版本对象需要入历史表
            CfgObjectVo cfgObjectVo = cfgObjectClient.getByModelCode(relationMObject.getTargetModelCode()).getCheckExceptionData();
            if (ObjectTypeEnum.VERSION.getCode().equals(cfgObjectVo.getType())) {
                for (MObject mObject : relationMObject.getTargetMObjects()) {
                    setVersionDefaulstValue(mObject);
                }
                objectModelDomainService.addHisBatch(relationMObject.getTargetModelCode(), relationMObject.getTargetMObjects());
            }
            objectModelDomainService.addBatch(relationMObject.getTargetModelCode(), relationMObject.getTargetMObjects());
        }
        if (CollectionUtils.isNotEmpty(relMObjects)) {
            objectModelDomainService.addBatch(relationMObject.getRelationModelCode(), relMObjects);
            if (!isDraft) {
                objectModelDomainService.addHisBatch(relationMObject.getRelationModelCode(), relMObjects);
            }
        }
        return true;
    }

    /**
     * @param relationMObject
     * @return boolean
     */
    private boolean isSourceObjectCheckoutByCurrentUser(RelationMObject relationMObject) {
        //查看源对象信息
        CfgObjectRelationVo objectRelationVo = cfgObjectRelationClient.getRelation(relationMObject.getRelationModelCode()).getCheckExceptionData();
        if (objectRelationVo == null) {
            throw new PlmBizException("未查询到关系配置");
        }
        relationMObject.setSourceModelCode(objectRelationVo.getSourceModelCode());
        //查看源对象是否被检出
        MObject sourceMObject = getByBid(relationMObject.getSourceModelCode(), relationMObject.getSourceBid());
        if (sourceMObject == null) {
            throw new PlmBizException("未能在数据库中找到源对象");
        }
        boolean isDraft = false;
        if (SsoHelper.getJobNumber().equals(sourceMObject.get(VersionObjectEnum.CHECKOUT_BY.getCode()))) {
            isDraft = true;
        } else if (sourceMObject.get(VersionObjectEnum.CHECKOUT_BY.getCode()) != null && StringUtils.isNoneBlank(String.valueOf(sourceMObject.get(VersionObjectEnum.CHECKOUT_BY.getCode())))) {
            throw new PlmBizException("源对象已被其他人检出");
        }
        return isDraft;
    }

    /**
     * @param mObject
     */
    private void setVersionDefaulstValue(MObject mObject) {
        if (mObject.get(VersionObjectEnum.VERSION.getCode()) == null
                || StringUtil.isBlank(String.valueOf(mObject.get(VersionObjectEnum.VERSION.getCode())))) {
            mObject.put(VersionObjectEnum.VERSION.getCode(), "1");
        }
        if (mObject.get(VersionObjectEnum.REVISION.getCode()) == null
                || StringUtil.isBlank(String.valueOf(mObject.get(VersionObjectEnum.REVISION.getCode())))) {
            mObject.put(VersionObjectEnum.REVISION.getCode(), "A");
        }
    }

    /**
     * @param relationMObject
     * @return {@link Boolean }
     */
    @Override
    public Boolean logicalDeleteRelations(RelationMObject relationMObject) {
        //查看源对象是否被检出
        QueryWrapper qo = new QueryWrapper();
        qo.eq("source_bid", relationMObject.getSourceBid()).and().in("target_data_bid", relationMObject.getTargetDataBids());
        if (isSourceObjectCheckoutByCurrentUser(relationMObject)) {
            qo.eq("draft", true);
        }
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        return objectModelDomainService.logicalDelete(relationMObject.getRelationModelCode(), queryWrappers);
    }

    /**
     * @param relationMObject
     * @return {@link Boolean }
     */
    @Override
    public Boolean logicalDeleteRelationsByTargetBids(RelationMObject relationMObject) {
        //查看源对象是否被检出
        QueryWrapper qo = new QueryWrapper();
        qo.eq("source_bid", relationMObject.getSourceBid()).and().in("target_bid", relationMObject.getTargetBids());
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        return objectModelDomainService.logicalDelete(relationMObject.getRelationModelCode(), queryWrappers);
    }

    /**
     * @param relationMObject
     * @return {@link Boolean }
     */
    @Override
    public Boolean deleteRelations(RelationMObject relationMObject) {
        //查看源对象是否被检出
        QueryWrapper qo = new QueryWrapper();
        qo.eq("source_bid", relationMObject.getSourceBid()).and().in("target_data_bid", relationMObject.getTargetDataBids());
        if (isSourceObjectCheckoutByCurrentUser(relationMObject)) {
            qo.and().eq("draft", true);
        }
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        return objectModelDomainService.delete(relationMObject.getRelationModelCode(), queryWrappers);
    }

    @Override
    public Map<String, List<MObject>> groupList(String modelCode, String groupProperty, QueryCondition queryCondition) {
        // 暂时使用sort排序
//        queryCondition.setOrders(
//                Lists.newArrayList(
//                        Order.of().setProperty(ObjectTreeEnum.SORT.getCode()).setDesc(false)
//                )
//        );

        List<MObject> list = objectModelDomainService.list(modelCode, queryCondition);
        if (CollectionUtils.isEmpty(list)) {
            return Maps.newHashMap();
        }
        if (StringUtil.isBlank(groupProperty)) {
            Map<String, List<MObject>> result = Maps.newHashMap();
            result.put("default", list);
            return result;
        }
        //验证分组属性是否存在
        CfgObjectVo cfgObjectVo = cfgObjectClient.getByModelCode(modelCode).getCheckExceptionData();
        if (cfgObjectVo == null) {
            throw new TranscendBizException(String.format("模型[%s]不存在", modelCode));
        }
        if (CollectionUtils.isEmpty(cfgObjectVo.getAttrList()) || cfgObjectVo.getAttrList().stream().noneMatch(attr -> groupProperty.equals(attr.getCode()))) {
            throw new TranscendBizException(String.format("模型[%s]中不存在[%s]分组属性", modelCode, groupProperty));
        }
        return list.stream().collect(Collectors.groupingBy(mObject -> String.valueOf(mObject.get(groupProperty))));
    }

    /**
     * @param modelCode
     * @param wrappers
     * @return {@link List }<{@link MObjectTree }>
     */
    @Override
    public List<MObjectTree> tree(String modelCode, List<QueryWrapper> wrappers) {
        //查询源对象
        CfgObjectVo cfgObjectVo = cfgObjectClient.getByModelCode(modelCode).getCheckExceptionData();
        if (cfgObjectVo == null) {
            throw new TranscendBizException(String.format("模型[%s]不存在", modelCode));
        }
        return baseObjectTreeService.tree(modelCode, wrappers);
    }

    /**
     * @param modelCode
     * @param treeList
     * @return {@link Boolean }
     */
    @Override
    public Boolean moveTreeNode(String modelCode, List<MObjectTree> treeList) {
        return baseObjectTreeService.updateTreeByBidBatch(modelCode, treeList);
    }

    /**
     * @param relationMObject
     * @return {@link List }<{@link MObjectTree }>
     */
    @Override
    public List<MObjectTree> relationTree(RelationMObject relationMObject) {
        //查询源对象
        List<CfgObjectVo> cfgObjectVo = cfgObjectClient.listByModelCodes(Arrays.asList(relationMObject.getRelationModelCode(), relationMObject.getTargetModelCode())).getCheckExceptionData();
        if (cfgObjectVo.size() != 2) {
            throw new TranscendBizException(String.format("模型[%s],[%s]不存在", relationMObject.getRelationModelCode(), relationMObject.getTargetModelCode()));
        }
        List<MObject> list = listRelationMObjects(relationMObject);
        // 空 直接返回
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        // 内存排序
        ModelMixQo modelMixQo = relationMObject.getModelMixQo();
        // 查询条件为空，需要初始化
        if (modelMixQo == null) {
            modelMixQo = new ModelMixQo();
        }
        // 内存排序
        list = ObjectTreeTools.sortObjectList(modelMixQo.getOrders(), list);
        List<MObjectTree> treeList = list.stream().map(mObject -> {
            MObjectTree mObjectTree = new MObjectTree();
            mObjectTree.putAll(mObject);
            return mObjectTree;
        }).collect(Collectors.toList());
        //校验是否是树形结构
        return baseObjectTreeService.convert2Tree(treeList);
    }

    /**
     * @param isVersion
     * @param relationMObject
     * @param filterRichText
     * @return {@link List }<{@link MObjectTree }>
     */
    @Override
    public List<MObjectTree> relationTree(Boolean isVersion, RelationMObject relationMObject, boolean filterRichText) {
        //查询源对象
        List<CfgObjectVo> cfgObjectVo = cfgObjectClient.listByModelCodes(Arrays.asList(relationMObject.getRelationModelCode(), relationMObject.getTargetModelCode())).getCheckExceptionData();
        if (cfgObjectVo.size() != 2) {
            throw new TranscendBizException(String.format("模型[%s],[%s]不存在", relationMObject.getRelationModelCode(), relationMObject.getTargetModelCode()));
        }
        List<MObject> list = listRelationMObjects(isVersion, relationMObject);
        // 空 直接返回
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        // 内存排序
        ModelMixQo modelMixQo = relationMObject.getModelMixQo();
        // 查询条件为空，需要初始化
        if (modelMixQo == null) {
            modelMixQo = new ModelMixQo();
        }
        // 内存排序
        list = ObjectTreeTools.sortObjectList(modelMixQo.getOrders(), list);
        List<MObjectTree> treeList = list.stream().map(mObject -> {
            MObjectTree mObjectTree = new MObjectTree();
            mObjectTree.putAll(mObject);
            if (filterRichText) {
                filterFields.forEach(mObjectTree::remove);
            }
            return mObjectTree;
        }).collect(Collectors.toList());
        //校验是否是树形结构
        return baseObjectTreeService.convert2Tree(treeList);
    }

    /**
     * @param relationModelCode
     * @param sourceModelCode
     * @param targetBid
     * @return {@link List }<{@link MObject }>
     */
    @Override
    public List<MObject> listSourceMObjects(String relationModelCode, String sourceModelCode, List<String> targetBid) {
        //先查询关系实例表
        QueryWrapper qo = new QueryWrapper();
        qo.in(RelationEnum.TARGET_BID.getColumn(), targetBid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        List<MRelationObject> mRelationObjectList;
        try {
            mRelationObjectList = relationModelDomainService.list(relationModelCode, queryWrappers);
        } catch (Exception e) {
            throw new BusinessException(String.format("查询关系实例表异常，可能是数据问题或者表不存在，关系对象模型编码[%s]", relationModelCode));
        }
        if (CollectionUtils.isEmpty(mRelationObjectList)) {
            return Lists.newArrayList();
        }
        List<String> sourceBids = mRelationObjectList.stream().map(MRelationObject::getSourceBid).collect(Collectors.toList());
        QueryWrapper qo2 = new QueryWrapper();
        qo2.in(BaseDataEnum.BID.getColumn(), sourceBids);
        if (StringUtils.isNoneBlank(sourceModelCode)) {
            qo2.and().eq(BaseDataEnum.MODEL_CODE.getColumn(), sourceModelCode);
        }
        List<QueryWrapper> queryWrappers2 = QueryWrapper.buildSqlQo(qo2);
        return objectModelDomainService.list(sourceModelCode, queryWrappers2);
    }

    /**
     * @param relationModelCode
     * @param sourceModelCode
     * @param targetBid
     * @return {@link List }<{@link MObject }>
     */
    @Override
    public List<MObject> listHisSourceMObjects(String relationModelCode, String sourceModelCode, List<String> targetBid) {
        //先查询关系实例表
        QueryWrapper qo = new QueryWrapper();
        qo.in(RelationEnum.TARGET_BID.getColumn(), targetBid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        List<MRelationObject> mRelationObjectList;
        try {
            mRelationObjectList = relationModelDomainService.listHis(relationModelCode, queryWrappers);
        } catch (Exception e) {
            throw new BusinessException(String.format("查询关系实例表异常，可能是数据问题或者表不存在，关系对象模型编码[%s]", relationModelCode));
        }
        if (CollectionUtils.isEmpty(mRelationObjectList)) {
            return Lists.newArrayList();
        }
        List<String> sourceBids = mRelationObjectList.stream().map(MRelationObject::getSourceBid).collect(Collectors.toList());
        QueryWrapper qo2 = new QueryWrapper();
        qo2.in(BaseDataEnum.BID.getColumn(), sourceBids);
        if (StringUtils.isNoneBlank(sourceModelCode)) {
            qo2.and().eq(BaseDataEnum.MODEL_CODE.getColumn(), sourceModelCode);
        }
        List<QueryWrapper> queryWrappers2 = QueryWrapper.buildSqlQo(qo2);
        return objectModelDomainService.listHis(sourceModelCode, queryWrappers2);
    }

    /**
     * @param modelCode
     * @param bid
     * @return {@link Boolean }
     */
    @Override
    public Boolean logicalDeleteByBid(String modelCode, String bid) {
        return objectModelDomainService.logicalDeleteByBid(modelCode, bid);
    }

    /**
     * @param modelCode
     * @param bids
     * @return {@link Boolean }
     */
    @Override
    public Boolean batchLogicalDeleteByBids(String modelCode, List<String> bids) {
        return objectModelDomainService.batchLogicalDeleteByBid(modelCode, bids);
    }

    /**
     * @param modelCode
     * @param sourceBid
     * @param targetBids
     * @return {@link Boolean }
     */
    @Override
    public Boolean deleteRel(String modelCode, String sourceBid, List<String> targetBids) {
        QueryWrapper qo = new QueryWrapper();
        qo.in(DataBaseConstant.COLUMN_TARGET_BID, targetBids).and().eq(DataBaseConstant.COLUMN_SOURCE_BID, sourceBid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        return objectModelDomainService.deleteByWrappers(modelCode, queryWrappers);
    }

    /**
     * @param modelCode
     * @param bid
     * @return {@link Boolean }
     */
    @Override
    public Boolean deleteByBid(String modelCode, String bid) {
        return objectModelDomainService.deleteByBid(modelCode, bid);
    }

    /**
     * @param modelCode
     * @param bids
     * @return {@link Boolean }
     */
    @Override
    public Boolean batchDeleteByBids(String modelCode, List<String> bids) {
        return objectModelDomainService.batchDeleteByBids(modelCode, bids);
    }

    /**
     * @param modelCode
     * @param bid
     * @param mObject
     * @return {@link Boolean }
     */
    @Override
    public Boolean updateByBid(String modelCode, String bid, MObject mObject) {
        boolean res = objectModelDomainService.updateByBid(modelCode, bid, mObject);
        // 发布后置处理事件
        try {
            // 1. 获取当前方法名字
            String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
            // 2. 发布事件方法
            NotifyEventBus.publishPostEvent(new NotifyPostEventBusDto(mObject, methodName, SsoHelper.getJobNumber()));
        } catch (Exception e) {
            log.error("执行modelCode为：{}的更新实例的后置方法异常！", modelCode);
        }
        return res;
    }

    /**
     * @param modelCode
     * @param bid
     * @return {@link MVersionObject }
     */
    @Override
    public MVersionObject checkOut(String modelCode, String bid) {
        return relationObjectManageService.checkOut(modelCode, bid);
    }

    /**
     * @param modelCode
     * @param bid
     * @return {@link MVersionObject }
     */
    @Override
    public MVersionObject cancelCheckOut(String modelCode, String bid) {
        return relationObjectManageService.cancelCheckOut(modelCode, bid);
    }

    @Override
    public MVersionObject checkIn(String modelCode, MVersionObject mObject) {
        return relationObjectManageService.checkIn(modelCode, mObject);
    }

    @Override
    public Boolean saveDraft(MObject mObject) {
        DraftVO draftVO = new DraftVO();
        draftVO.setBid(mObject.getBid());
        draftVO.setDataBid(mObject.get(TranscendModelBaseFields.DATA_BID) + "");
        draftVO.setBaseModel(mObject.getModelCode());
        draftVO.setContent(JSON.toJSONString(mObject));
        draftVO.setEnableFlag((short) 1);
        draftVO.setDeleteFlag(0);
        draftVO.setTenantId(SsoHelper.getTenantId());
        draftVO.setCreatedBy(SsoHelper.getJobNumber());
        draftVO.setCreatedTime(new Date());
        draftVO.setUpdatedBy(draftVO.getCreatedBy());
        draftVO.setUpdatedTime(new Date());

        return draftRepository.saveDraftData(ObjectPojoConverter.INSTANCE.draftVO2DraftPO(draftVO));
    }


    /**
     * TODO 具体是bid还是dataBid待定
     *
     * @param modelCode
     * @param bid
     * @return
     */
    @Override
    public MVersionObject getOrDraftByBid(String modelCode, String bid) {
        // 判断是否草稿状态
        MVersionObject mVersionObject = versionModelDomainService.getByBid(modelCode, bid);
        if (com.transcend.framework.common.util.StringUtil.isBlank(mVersionObject.getCheckoutBy())) {
            return mVersionObject;
        }
        // 有暂存则取暂存数据，无则还是数据库数据
        DraftPO draftPO = draftRepository.getByDataBid(mVersionObject.getDataBid());
        ObjectMapper objectMapper = new ObjectMapper();
        if (draftPO != null) {
            String content = draftPO.getContent();
            if (null == content) {
                return mVersionObject;
            }
            try {
                objectMapper.readValue(content, MVersionObject.class);
            } catch (JsonProcessingException e) {
                log.error("草稿json转换错误！content: {}", content);
            }
        }
        return mVersionObject;
    }

    /**
     * 关系下拉选项的分页查询（作为目标对象查询以源对象的可选集合） TODO
     *
     * @param qo 查询条件
     * @return 分页结果
     */
    @Override
    public PagedResult<MObject> relationTargetSelectPage(RelationCrossLevelQo qo) {
        // 1. 如果没有源对象modelCode，则直接返回空
        if (StringUtil.isBlank(qo.getSourceModelCode())) {
            return PageResultTools.createEmpty();
        }
        qo.getPageQo();
        BaseRequest<QueryCondition> baseRequest = QueryConveterTool.convert(qo.getPageQo());
        List<String> bids = new ArrayList<>();
        // 2. 如果条件中不携带需要穿透的内容，则不用过滤条件,收集过滤条件
        if (StringUtil.isEmpty(qo.getCrossLevelRelationModelCodes())
                && StringUtil.isEmpty(qo.getCrossLevelSourceBid())) {
            // 2.1 收集过滤条件
            String[] crossLevelRelationModelCodes = qo.getCrossLevelRelationModelCodes().split("#");

        }
        return page(qo.getSourceModelCode(), baseRequest, true);
    }

}
