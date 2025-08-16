package com.transcend.plm.datadriven.domain.object.base;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import com.transcend.plm.datadriven.domain.support.external.table.CfgTableService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Component
@Slf4j
public class RelationModelDomainService extends AbstractObjectDomainService<MRelationObject> {

    @Resource
    private VersionModelDomainService versionModelDomainService;

    /**
     * @param modelService
     */
    public RelationModelDomainService(ModelService<MRelationObject> modelService) {
        this.modelService = modelService;
    }

    /**
     * 根据源bid批量复制数据，并且更新Map中的数据
     *
     * @param modelCode   关系对象modelCode
     * @param bid         源对象实例bid
     * @param resetValues 需要重置的值
     * @param relBehavior
     * @return
     */
    @Override
    public List<MRelationObject> copyAndReset(String modelCode, String bid, ImmutableMap<String, Object> resetValues, String relBehavior) {
        //查询关系数据
        List<MRelationObject> relationObjects = listBySourceBid(modelCode, bid);
        if (CollectionUtils.isEmpty(relationObjects)) {
            return Lists.newArrayList();
        }

        //更新关系数据
        for (MRelationObject relationObject : relationObjects) {
            relationObject.putAll(resetValues);
            if (StringUtils.isNotBlank(relBehavior)) {
                relationObject.setRelBehavior(relBehavior);
            }
            setRelAttrNull(relationObject);
        }
        this.addBatch(modelCode, relationObjects);
        return relationObjects;
    }

    /**
     * @param targetModelCode
     * @param relationModelCode
     * @param oldSourceBid
     * @param newSourceBid
     * @param relBehavior
     * @return {@link List }<{@link MRelationObject }>
     */
    @Transactional(rollbackFor = Exception.class)
    public List<MRelationObject> copyAndModify(String targetModelCode, String relationModelCode, String oldSourceBid, String newSourceBid, String relBehavior) {
        //查询关系数据
        List<MRelationObject> relationObjects = listBySourceBid(relationModelCode, oldSourceBid);
        if (CollectionUtils.isEmpty(relationObjects)) {
            return Lists.newArrayList();
        }
        //更新关系数据
        List<String> targetDataBids = relationObjects.stream().map(MRelationObject::getTargetDataBid).collect(Collectors.toList());
        List<MVersionObject> mVersionObjects = versionModelDomainService.listMVersionObjects(targetModelCode, targetDataBids);
        Map<String, String> mVersionObjectMap = mVersionObjects.stream().collect(Collectors.toMap(MVersionObject::getDataBid, MVersionObject::getBid));
        for (MRelationObject relationObject : relationObjects) {
            //查询目标实例数据
            if (mVersionObjectMap.containsKey(relationObject.getTargetDataBid())) {
                relationObject.setTargetBid(mVersionObjectMap.get(relationObject.getTargetDataBid()));
            }
            relationObject.setSourceBid(newSourceBid);
            if (!oldSourceBid.equals(newSourceBid)) {
                setRelAttrNull(relationObject);
            }
            relationObject.setRelBehavior(relBehavior);
        }
        if (oldSourceBid.equals(newSourceBid)) {
            for (MRelationObject relationObject : relationObjects) {
                this.updateHisByBid(relationModelCode, relationObject.getBid(), relationObject);
                this.updateByBid(relationModelCode, relationObject.getBid(), relationObject);
            }
        } else {
            deleteBySourceBid(relationModelCode, oldSourceBid);
            this.addBatch(relationModelCode, relationObjects);
            modelService.addHisBatch(relationModelCode, relationObjects);
        }
        return relationObjects;
    }

    /**
     * @param relationModelCode
     * @param oldSourceBid
     * @return {@link List }<{@link MRelationObject }>
     */
    private List<MRelationObject> listBySourceBid(String relationModelCode, String oldSourceBid) {
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.SOURCE_BID.getColumn(), oldSourceBid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        List<MRelationObject> relationObjects = this.list(relationModelCode, queryWrappers);
        return relationObjects;
    }

    /**
     * @param relationModelCode
     * @param oldSourceBid
     * @return {@link List }<{@link MRelationObject }>
     */
    private List<MRelationObject> listHisBySourceBid(String relationModelCode, String oldSourceBid) {
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.SOURCE_BID.getColumn(), oldSourceBid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        List<MRelationObject> relationObjects = this.listHis(relationModelCode, queryWrappers);
        return relationObjects;
    }

    /**
     * 设置关系实例数据 部分属性置空
     *
     * @param mRelationObject mRelationObject
     */
    private void setRelAttrNull(MRelationObject mRelationObject) {
        mRelationObject.remove(BaseDataEnum.ID.getCode());
        mRelationObject.setBid(null);
        mRelationObject.setCreatedBy(null);
        mRelationObject.setCreatedTime(null);
        mRelationObject.setUpdatedBy(null);
        mRelationObject.setUpdatedTime(null);
    }

    /**
     * 根据源对象bid进行删除
     *
     * @param modelCode 对象类型
     * @param sourceBid 源对象bid
     * @return 删除成功
     */
    public boolean deleteBySourceBid(String modelCode, String sourceBid) {
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.SOURCE_BID.getColumn(), sourceBid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        return this.delete(modelCode, queryWrappers);
    }

    /**
     * 根据源对象bid进行逻辑删除
     *
     * @param modelCode modelCode
     * @param sourceBid sourceBid
     * @return boolean
     * @version: 1.0
     * @date: 2023/10/25 10:30
     * @author: bin.yin
     */
    public boolean logicalDeleteBySourceBid(String modelCode, String sourceBid) {
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.SOURCE_BID.getColumn(), sourceBid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        return this.logicalDelete(modelCode, queryWrappers);
    }

    /**
     * 根据目标对象实例bid进行逻辑删除
     *
     * @param modelCode modelCode
     * @param targetBid targetBid
     * @return boolean
     * @version: 1.0
     * @date: 2023/10/25 10:30
     * @author: bin.yin
     */
    public boolean logicalDeleteByTargetBid(String modelCode, String targetBid) {
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.TARGET_BID.getColumn(), targetBid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        return this.logicalDelete(modelCode, queryWrappers);
    }

    /**
     * 根据源对象bid进行逻辑删除
     *
     * @param modelCode modelCode
     * @param sourceBid sourceBid
     * @return boolean
     */
    public boolean deleteHisBySourceBid(String modelCode, String sourceBid) {
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.SOURCE_BID.getColumn(), sourceBid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        return modelService.deleteHis(modelCode, queryWrappers);
    }

    /**
     * 批量根据源对象bid进行逻辑删除
     *
     * @param deleteParams key: modelCode, value: sourceBidSet
     * @return boolean
     * @version: 1.0
     * @date: 2023/11/29 16:04
     * @author: bin.yin
     */
    public Boolean batchLogicalDeleteBySourceBid(Map<String, Set<String>> deleteParams) {
        // 删除bid为空的数据
        Iterator<Map.Entry<String, Set<String>>> iterator = deleteParams.entrySet().iterator();
        if (iterator.hasNext() && CollUtil.isEmpty(iterator.next().getValue())) {
            iterator.remove();
        }
        if (CollUtil.isEmpty(deleteParams)) {
            return true;
        }
        return this.batchLogicalDeleteByModeCodeAndSourceBid(deleteParams);
    }

    /**
     * 生效草稿数据
     *
     * @param modelCode
     * @param sourceDataBid
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean effectDraft(String modelCode, String sourceDataBid) {
        if (StringUtils.isAnyBlank(modelCode, sourceDataBid)) {
            throw new IllegalArgumentException("参数ModelCode或者SourceDataBid为空");
        }
        //删除之前生效的数据
        if (!deleteCurrentEffectiveData(modelCode, sourceDataBid)) {
            log.info("删除关系之前生效的数据失败，modelCode:{},sourceDataBid:{}", modelCode, sourceDataBid);
        }
        //修改草稿状态为生效
        if (!updateDraftStatus(modelCode, sourceDataBid)) {
            log.info("修改关系草稿状态为生效失败，modelCode:{},sourceDataBid:{}", modelCode, sourceDataBid);
        }
        //拷贝到历史表
        copy2HisBySource(modelCode, sourceDataBid);
        return true;
    }

    /**
     * 更新关系实例并且写入历史表
     *
     * @param modelCode
     * @param sourceDataBid
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAndAddHis(String modelCode, String sourceDataBid, ImmutableMap<String, Object> updateValues) {
        if (StringUtils.isAnyBlank(modelCode, sourceDataBid)) {
            throw new IllegalArgumentException("参数ModelCode或者SourceDataBid为空");
        }
        //查询关系数据
        List<MRelationObject> relationObjects = listBySourceBid(modelCode, sourceDataBid);
        if (CollectionUtils.isEmpty(relationObjects)) {
            return Boolean.TRUE;
        }
        //更新关系数据
        for (MRelationObject relationObject : relationObjects) {
            relationObject.putAll(updateValues);
            setRelAttrNull(relationObject);
        }
        //删除之前生效的数据
        deleteBySourceBid(modelCode, sourceDataBid);
        //新增更新后的数据
        this.addBatch(modelCode, relationObjects);
        //拷贝更新后的数据到历史表
        modelService.addHisBatch(modelCode, relationObjects);
        return Boolean.TRUE;
    }

    /**
     * 拷贝到历史表
     *
     * @param modelCode
     * @param sourceDataBid
     */
    public void copy2HisBySource(String modelCode, String sourceDataBid) {
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.SOURCE_DATA_BID.getColumn(), sourceDataBid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        List<MRelationObject> mRelationObjects = list(modelCode, queryWrappers);
        if (CollectionUtils.isEmpty(mRelationObjects)) {
            return;
        }
        //清空mRelationObjects的id
        mRelationObjects.forEach(mRelationObject -> mRelationObject.remove(BaseDataEnum.ID.getCode()));
        modelService.addHisBatch(modelCode, mRelationObjects);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MRelationObject add(String modelCode, MRelationObject mObject) {
        MRelationObject result = modelService.add(modelCode, mObject);
        this.addHis(modelCode, result);
        return result;
    }

    @Override
    public MRelationObject addHis(String modelCode, MRelationObject mObject) {
        return modelService.addHis(modelCode, mObject);
    }

    private boolean updateDraftStatus(String modelCode, String sourceDataBid) {
        MRelationObject updateValue = new MRelationObject();
        updateValue.setDraft(false);
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.SOURCE_DATA_BID.getColumn(), sourceDataBid);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        return this.update(modelCode, updateValue, queryWrappers);
    }

    private boolean deleteCurrentEffectiveData(String modelCode, String sourceDataBid) {
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.SOURCE_DATA_BID.getColumn(), sourceDataBid);
        qo.and();
        qo.eq(RelationEnum.DRAFT.getColumn(), Boolean.FALSE);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        return this.delete(modelCode, queryWrappers);
    }


}
