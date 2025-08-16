package com.transcend.plm.alm.demandmanagement.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONValidator;
import com.transcend.plm.alm.demandmanagement.config.DemandManagementProperties;
import com.transcend.plm.alm.demandmanagement.service.RequirementsStatusAutoUpdateService;
import com.transcend.plm.configcenter.api.feign.CfgObjectRelationFeignClient;
import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationVo;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.apm.common.ApmImplicitParameter;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmFlowInstanceVO;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.flow.service.IRuntimeService;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.common.wapper.MapWrapper;
import com.transcend.plm.datadriven.common.wapper.TranscendObjectWrapper;
import com.transcend.plm.datadriven.common.wapper.TranscendRelationWrapper;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 需求自动更新服务实现
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/2/27 09:19
 */
@Log4j2
@Service
public class RequirementsStatusAutoUpdateServiceImpl implements RequirementsStatusAutoUpdateService {
    private static final String AUTO_UPDATE_PRE_MODIFY_STATUS_KEY = "autoUpdatePreModifyStatus";

    private final ObjectModelStandardI<MObject> objectModelCrudI;
    private final IBaseApmSpaceAppDataDrivenService iBaseApmSpaceAppDataDrivenService;
    private final CfgObjectRelationFeignClient cfgObjectRelationClient;
    private final IRuntimeService runtimeService;
    private final Map<String, String[]> lifeCycleCodeMap;
    private List<Config> configs;

    public RequirementsStatusAutoUpdateServiceImpl(ObjectModelStandardI<MObject> objectModelCrudI,
                                                   IBaseApmSpaceAppDataDrivenService iBaseApmSpaceAppDataDrivenService,
                                                   CfgObjectRelationFeignClient cfgObjectRelationClient,
                                                   IRuntimeService runtimeService, DemandManagementProperties properties) {
        this.objectModelCrudI = objectModelCrudI;
        this.iBaseApmSpaceAppDataDrivenService = iBaseApmSpaceAppDataDrivenService;
        this.cfgObjectRelationClient = cfgObjectRelationClient;
        this.runtimeService = runtimeService;
        this.lifeCycleCodeMap = properties.getLiefCycleCode();
    }

    @Value("${transcend.plm.apm.statusAutoUpdate:{}}")
    public void setConfigs(String configsJson) {
        try (JSONValidator validator = JSONValidator.from(configsJson)) {
            if (validator.validate()) {
                this.configs = JSON.parseArray(configsJson, Config.class);
                return;
            }
        } catch (Exception e) {
            log.error("setConfigs configsJson is not valid json", e);
        }
        log.error("setConfigs configsJson is not valid json");
    }

    @Override
    public boolean notFocusModelCode(String modelCode) {
        return configs.stream()
                .flatMap(config -> Stream.of(config.getChildModelCode(), config.getParentModelCode()))
                .filter(Objects::nonNull).distinct()
                .noneMatch(relationCode -> Objects.equals(relationCode, modelCode));
    }

    @Override
    public boolean notFocusRelModelCode(String modelCode) {
        return configs.stream().map(Config::getRelationCode)
                .noneMatch(relationCode -> Objects.equals(relationCode, modelCode));
    }

    @Override
    public List<Config> getSelfConfig(TranscendObjectWrapper obj) {
        if (obj == null) {
            return null;
        }
        //回退状态操作忽略自调用
        if (itFallbackOperation(obj)) {
            return null;
        }
        return configs.stream().filter(config -> config.getParentModelCode().equals(obj.getModelCode()))
                .filter(config -> config.getParentStatusCode().stream().anyMatch(code -> code.equals(obj.getLifeCycleCode())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoUpdateSelf(List<Config> configList, List<QueryWrapper> wrappers) {
        if (CollUtil.isEmpty(configList)) {
            return;
        }
        configList.forEach(config -> {
            log.info("autoUpdateSelfStatus config:{}", config);
            List<MObject> list = objectModelCrudI.list(config.getParentModelCode(), wrappers);
            batchAutoUpdate(config, list);
        });
    }

    @Override
    public List<Config> getParentUpdateConfig(TranscendObjectWrapper obj) {
        if (obj == null) {
            return null;
        }
        return configs.stream().filter(config -> config.getChildModelCode().equals(obj.getModelCode()))
                .filter(config -> config.getChildStatusCode().equals(obj.getLifeCycleCode()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoUpdateParent(List<Config> configList, List<QueryWrapper> wrappers) {
        if (CollUtil.isEmpty(configList)) {
            return;
        }
        configList.forEach(config -> {
            log.info("autoUpdateParentStatus config:{}", config);
            List<MObject> list = objectModelCrudI.list(config.getChildModelCode(), wrappers);
            list.stream().map(MBaseData::getBid)
                    .map(bid -> getParentBidList(config.getRelationCode(), config.isReverseRelation(), bid))
                    .map(bidList -> getMatchStatusParentList(config, bidList))
                    .forEach(parentList -> batchAutoUpdate(config, parentList));
        });
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoUpdateRelationSource(TranscendRelationWrapper relationData) {
        CfgObjectRelationVo relation = cfgObjectRelationClient.getRelation(relationData.getModelCode())
                .getCheckExceptionData();
        if (relation == null || relation.getSourceModelCode() == null) {
            return;
        }
        //查询原始数据
        MObject source = objectModelCrudI.getByBid(relation.getSourceModelCode(), relationData.getSourceBid());
        if (source == null) {
            return;
        }

        //获取配置
        List<Config> configList = getSelfConfig(new TranscendObjectWrapper(source));
        if (CollUtil.isEmpty(configList)) {
            return;
        }
        configList.forEach(config -> this.autoUpdate(config, source));
    }

    @Override
    public void autoUpdateRelationSource(String modelCode, List<QueryWrapper> wrappers) {
        CfgObjectRelationVo relation = cfgObjectRelationClient.getRelation(modelCode)
                .getCheckExceptionData();
        if (relation == null || relation.getSourceModelCode() == null) {
            return;
        }

        List<MObject> relationList = objectModelCrudI.list(modelCode, wrappers);
        if (relationList == null || relationList.isEmpty()) {
            return;
        }
        List<String> sourceBidList = relationList.stream().map(TranscendRelationWrapper::new)
                .map(TranscendRelationWrapper::getSourceBid)
                .distinct().collect(Collectors.toList());
        QueryWrapper wrapper = new QueryWrapper().in(BaseDataEnum.BID.getColumn(), sourceBidList);
        List<MObject> sourceList = objectModelCrudI.list(relation.getSourceModelCode(), QueryWrapper.buildSqlQo(wrapper));
        sourceList.forEach(source -> {
            try {
                List<Config> configList = getSelfConfig(new TranscendObjectWrapper(source));
                if (CollUtil.isEmpty(configList)) {
                    return;
                }
                configList.forEach(config -> this.autoUpdate(config, source));
            } catch (Exception e) {
                log.error("autoUpdateRelationSource error,source:{}", source, e);
            }
        });

    }

    @Override
    public void writePreModifyStatus(String modelCode, TranscendObjectWrapper obj, List<QueryWrapper> wrappers) {
        if (obj == null) {
            return;
        }
        if (obj.getLifeCycleCode() == null) {
            return;
        }
        Map<String, String> statusMap = objectModelCrudI.list(modelCode, wrappers)
                .stream().collect(Collectors.toMap(MBaseData::getBid, MObject::getLifeCycleCode));
        if (statusMap.isEmpty()) {
            return;
        }

        obj.put(AUTO_UPDATE_PRE_MODIFY_STATUS_KEY, statusMap);
    }

    //region 私有方法

    /**
     * 自动更新状态
     *
     * @param config 配置信息
     * @param self   对象信息
     */
    private void autoUpdate(Config config, MObject self) {
        if (config == null || self == null) {
            return;
        }

        boolean fallback = isFallback(config.getParentModelCode(), self.getLifeCycleCode(), config.getParentNewStatusCode());
        if (!isAutoUpdate(config, self, fallback)) {
            return;
        }

        TranscendObjectWrapper wrapper = new TranscendObjectWrapper(self);

        log.info("autoUpdate modelCode:{},bid:{},status:{},newStatus:{}",
                config.getParentModelCode(), wrapper.getBid(), wrapper.getLifeCycleCode(), config.getParentNewStatusCode());

        try (ApmImplicitParameter apmImplicitParameter = new ApmImplicitParameter()) {
            //后续操跳过鉴权
            apmImplicitParameter.setSkipCheckPermission(true);

            //通过流程更新
            if (config.isFlow()) {

                //通过配置的webBid 获取实例的节点bid
                String flowNodeBid = getFlowNodeBid(config, fallback, wrapper.getBid());
                if (flowNodeBid == null) {
                    log.warn("autoUpdate flowNodeBid is null,bid={}", wrapper.getBid());
                    return;
                }

                //流程回退
                if (fallback) {
                    iBaseApmSpaceAppDataDrivenService.updatePartialContentAndRollbackFlowNode(
                            wrapper.getSpaceAppBid(), wrapper.getBid(), flowNodeBid, new MSpaceAppData(), true);
                    return;
                }

                //流程完成
                iBaseApmSpaceAppDataDrivenService.updatePartialContentAndCompleteFlowNode(
                        wrapper.getSpaceAppBid(), wrapper.getBid(), flowNodeBid, new MSpaceAppData());

                return;
            }

            //常规数据更新
            MSpaceAppData updateContent = new MSpaceAppData();
            updateContent.setLifeCycleCode(config.getParentNewStatusCode());
            iBaseApmSpaceAppDataDrivenService.updatePartialContent(
                    wrapper.getSpaceAppBid(), wrapper.getBid(), updateContent);
        } catch (Exception e) {
            log.error("autoUpdate error , bid: {}", wrapper.getBid(), e);
        }
    }

    /**
     * 执行更新状态操作
     *
     * @param config   配置
     * @param selfList 被跟新的对象列表
     */
    private void batchAutoUpdate(Config config, List<MObject> selfList) {
        if (selfList == null || selfList.isEmpty()) {
            return;
        }
        selfList.forEach(self -> autoUpdate(config, self));
    }

    /**
     * 查询父对象Bid列表
     *
     * @param modelCode       关系模型编码
     * @param reverseRelation 是否翻转关系
     * @param bid             子对象bid
     * @return 父对象Bid列表
     */
    @NotNull
    private List<String> getParentBidList(String modelCode, boolean reverseRelation, String bid) {
        String column = reverseRelation ? RelationEnum.SOURCE_BID.getColumn() : RelationEnum.TARGET_BID.getColumn();
        QueryWrapper wrapper = new QueryWrapper().eq(column, bid);
        List<MObject> relationList = objectModelCrudI.list(modelCode, QueryWrapper.buildSqlQo(wrapper));
        return relationList.stream().map(TranscendRelationWrapper::new)
                .map(TranscendRelationWrapper::getSourceBid).distinct().collect(Collectors.toList());
    }

    /**
     * 查询子对象Bid列表
     *
     * @param modelCode       关系模型编码
     * @param reverseRelation 是否翻转关系
     * @param bid             父对象Bid
     * @return 子对象Bid列表
     */
    @NotNull
    private List<String> getChildBidList(String modelCode, boolean reverseRelation, String bid) {
        String column = reverseRelation ? RelationEnum.TARGET_BID.getColumn() : RelationEnum.SOURCE_BID.getColumn();
        QueryWrapper wrapper = new QueryWrapper().eq(column, bid);
        List<MObject> relationList = objectModelCrudI.list(modelCode, QueryWrapper.buildSqlQo(wrapper));
        return relationList.stream().map(TranscendRelationWrapper::new)
                .map(TranscendRelationWrapper::getTargetBid).distinct().collect(Collectors.toList());
    }

    /**
     * 获取满足配置状态要求的父对象列表
     *
     * @param config  配置
     * @param bidList 父对象Bid列表
     * @return 父对象列表
     */
    private List<MObject> getMatchStatusParentList(Config config, List<String> bidList) {
        if (CollUtil.isEmpty(bidList)) {
            return ListUtil.empty();
        }

        QueryWrapper wrapper = new QueryWrapper().in(BaseDataEnum.BID.getColumn(), bidList);

        // 父状态不限制，不需要带条件查询
        if (CollUtil.isNotEmpty(config.getParentStatusCode())) {
            wrapper.and().in(ObjectEnum.LIFE_CYCLE_CODE.getCode(), config.getParentStatusCode());
        }

        return objectModelCrudI.list(config.getParentModelCode(), QueryWrapper.buildSqlQo(wrapper));
    }


    /**
     * 判断是否满足自动更新
     *
     * @param config   配置
     * @param parent   父对象
     * @param fallback 是否回退
     * @return 是否满足自动更新
     */
    private boolean isAutoUpdate(Config config, MObject parent, boolean fallback) {
        if (parent == null) {
            return false;
        }

        //判断父对象状态是否满足配置(不配置父状态则不关心父状态)
        if (CollUtil.isNotEmpty(config.getParentStatusCode()) && !config.getParentStatusCode().contains(parent.getLifeCycleCode())) {
            return false;
        }

        //查询所有的关联子层
        List<String> childBidList = getChildBidList(config.getRelationCode(), config.isReverseRelation(), parent.getBid());

        //一个子层都没有则不能自动更新
        if (CollUtil.isEmpty(childBidList)) {
            return false;
        }

        String[] childMismatchedStatusCodeList = getChildMismatchedStatusCodeArray(config, fallback);
        if (childMismatchedStatusCodeList == null) {
            return true;
        }

        //查询关联子层是否有不满足的状态；注意：不能通过count查询，已有关系目标bid数据中可能有被删除的数据，导致结果不准
        QueryWrapper wrapper = new QueryWrapper().in(BaseDataEnum.BID.getColumn(), childBidList);
        List<MObject> childList = objectModelCrudI.list(config.getChildModelCode(), QueryWrapper.buildSqlQo(wrapper));
        if (CollUtil.isEmpty(childList)) {
            return false;
        }
        //不匹配的状态
        long mismatchedCount = childList.stream().map(MObject::getLifeCycleCode)
                .filter(lifeCycleCode -> ArrayUtils.contains(childMismatchedStatusCodeList, lifeCycleCode))
                .count();

        //任意匹配匹配 || 全部匹配
        return (config.isAnyMatch() && mismatchedCount < childList.size()) || mismatchedCount <= 0;
    }


    /**
     * 获取子对象不满足的状态
     *
     * @param config     配置
     * @param isFallback 是否回退状态
     * @return 子对象不满足的状态
     */
    @Nullable
    private String[] getChildMismatchedStatusCodeArray(Config config, boolean isFallback) {
        String[] lifeCycleCodeArray = this.lifeCycleCodeMap.get(config.getChildModelCode());
        if (lifeCycleCodeArray == null) {
            log.warn("getChildMismatchedStatusCodeArray lifeCycleCodeArray is null, childModelCode:{}",
                    config.getChildModelCode());
            return null;
        }

        int indexOf = ArrayUtils.indexOf(lifeCycleCodeArray, config.getChildStatusCode());
        if (indexOf == -1) {
            log.warn("getChildMismatchedStatusCodeArray  does not exist, childModelCode:{},lifeCycleCode:{}",
                    config.getChildModelCode(), config.getChildStatusCode());
            return null;
        }
        return isFallback ?
                ArrayUtils.subarray(lifeCycleCodeArray, indexOf + 1, lifeCycleCodeArray.length)
                : ArrayUtils.subarray(lifeCycleCodeArray, 0, indexOf);
    }

    /**
     * 获取数据与流程对象
     *
     * @param config      配置
     * @param isFallback  是否回退
     * @param instanceBid 实例Bid
     * @return 数据与流程对象
     */
    @Nullable
    private String getFlowNodeBid(Config config, boolean isFallback, String instanceBid) {
        return Optional.ofNullable(instanceBid)
                .map(runtimeService::listInstanceNodes).map(ApmFlowInstanceVO::getNodes)
                .flatMap(list -> list.stream()
                        .filter(node -> config.getFlowNodeWebBid().equals(node.getWebBid()))
                        //非回退状态需要在进行中
                        .filter(node -> isFallback || node.getNodeState() == 1)
                        .map(ApmFlowInstanceNode::getBid).findFirst()).orElse(null);
    }


    /**
     * 判断是否回退
     *
     * @param currentStatus 当前数据状态
     * @param targetStatus  目标数据状态
     * @return 是否回退
     */
    private boolean isFallback(String modelCode, String currentStatus, String targetStatus) {
        String[] lifeCycleCodeArray = this.lifeCycleCodeMap.get(modelCode);
        if (lifeCycleCodeArray == null) {
            log.warn("isFallback lifeCycleCodeArray is null, modelCode:{}", modelCode);
            return false;
        }

        int currentStatusIndexOf = ArrayUtils.indexOf(lifeCycleCodeArray, currentStatus);
        int targetStatusIndexOf = ArrayUtils.indexOf(lifeCycleCodeArray, targetStatus);

        return currentStatusIndexOf > targetStatusIndexOf;
    }

    /**
     * 判断是否回退修改
     *
     * @param obj 对象
     * @return 是否回退操作
     */
    private boolean itFallbackOperation(TranscendObjectWrapper obj) {
        if (obj == null) {
            return false;
        }

        String modelCode = obj.getModelCode();
        if (modelCode == null) {
            return false;
        }
        String lifeCycleCode = obj.getLifeCycleCode();
        if (lifeCycleCode == null) {
            return false;
        }
        String bid = obj.getBid();
        if (bid == null) {
            return false;
        }
        MapWrapper preModifyStatusMap = obj.getWrapper(MapWrapper.class, AUTO_UPDATE_PRE_MODIFY_STATUS_KEY);
        if (preModifyStatusMap == null) {
            return false;
        }
        String preModifyStatus = preModifyStatusMap.getStr(bid);
        if (preModifyStatus == null) {
            return false;
        }
        return isFallback(modelCode, preModifyStatus, lifeCycleCode);
    }

    //endregion

}
