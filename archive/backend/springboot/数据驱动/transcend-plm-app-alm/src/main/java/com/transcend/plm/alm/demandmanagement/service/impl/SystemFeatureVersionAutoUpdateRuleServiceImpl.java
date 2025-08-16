package com.transcend.plm.alm.demandmanagement.service.impl;

import com.transcend.framework.core.exception.TranscendBizException;
import com.transcend.plm.alm.demandmanagement.entity.ao.SfTreeDataSyncCopyAo;
import com.transcend.plm.alm.demandmanagement.entity.wrapper.SystemFeatureWrapper;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.alm.demandmanagement.service.SystemFeatureSyncDataChangeCollector;
import com.transcend.plm.alm.demandmanagement.service.SystemFeatureVersionAutoUpdateRuleService;
import com.transcend.plm.alm.tools.VersionNumberTools;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.apm.tools.SpaceTreeTools;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transcend.plm.datadriven.infrastructure.basedata.event.BaseDataUpdateEvent;
import com.transsion.framework.common.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 特性自动版本更新规则服务
 *
 * @author jie.luo1  <jie.luo1@transsion.com>
 * @version 1.0
 * createdAt 2025/5/26 16:05
 */
@Slf4j
@Service
public class SystemFeatureVersionAutoUpdateRuleServiceImpl implements SystemFeatureVersionAutoUpdateRuleService {

    @Resource
    private IBaseApmSpaceAppDataDrivenService iBaseApmSpaceAppDataDrivenService;
    @Resource
    private SpaceTreeTools spaceTreeTools;

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;
    /**
     * 特性-IR:A1L
     */
    private final String sfRirModelCode = TranscendModel.RELATION_RSF_IR.getCode();
    /**
     * tos根特性数据（modelCode:A1B）
     */
    private final String tosBaseSfModelCode = TranscendModel.TOS_VERSION_FEATURE_TREE.getCode();
    /**
     * tos根特性数据的版本号字段
     */
    private final String tosBaseSfVersionNumberField = SystemFeatureWrapper.VERSION_NUMBER;
    /**
     * IR-特性:A1L
     */
    private final String iRrSFModelCode = TranscendModel.RELATION_IR_RSF.getCode();
    /**
     * 实际交付版本字段名
     */
    private final String deliveryVersionField = "deliveryVersion";

    /**
     * 同步数据到TOS 的版本变化的规则：
     * 把全集特性树同步过来，并且把TOS 版本号回写 全集特性树中版本号，（tos根特性数据：modelCode:A1B,versionNumber）
     * （同步多次，是否只会影响新同步的SF？？？） 本程序默认只更新最后一次同步过来的数据
     *
     * @param ao           请求参数
     * @param collector    数据变化收集器
     * @param syncDataList 全集特性树同步数据列表
     * @return boolean
     */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean syncTosSystemFeature(SfTreeDataSyncCopyAo ao,
                                        SystemFeatureSyncDataChangeCollector collector,
                                        List<MObject> syncDataList) {
        try {
            // 1. 参数校验
            if (ao == null || collector == null || syncDataList == null) {
                log.error("同步参数不能为空");
                return false;
            }

            // 2.没有新增的，不处理
            List<MObject> instanceAddList = collector.getInstanceAddList();
            if (CollectionUtils.isEmpty(instanceAddList)) {
                return true;
            }

            // 3. 获取基础特性数据
            MObject baseSf = objectModelCrudI.getByBid(tosBaseSfModelCode, ao.getSourceBid());
            if (baseSf == null) {
                log.warn("同步失败: 未找到基础特性数据, sourceBid={}", ao.getSourceBid());
                return false;
            }

            // 4. 版本号校验
            Object versionObj = baseSf.get(tosBaseSfVersionNumberField);
            if (!(versionObj instanceof String) || StringUtil.isBlank((String) versionObj)) {
                log.warn("同步失败: 无效版本号, sourceBid={}", ao.getSourceBid());
                return false;
            }
            String versionNumber = (String) versionObj;

            // 5. 准备更新数据
            MObject updateData = new MObject();
            updateData.put(tosBaseSfVersionNumberField, versionNumber);

            // 6. 处理TOS特性树更新
            processBatchUpdate(instanceAddList,
                    TranscendModel.RSF.getCode(),
                    updateData);

            // 6. 处理全集特性树更新
            if (CollectionUtils.isNotEmpty(ao.getBidList())) {
//                List<String> needUpdateBids = collectUpdateSfBidList(syncDataList, versionNumber);
                // 暂时不care 版本号的大小，直接更新， 全集版本只更新 第一次同步过来的
                Set<String> tosCodingSet = instanceAddList.stream().map(MObject::getCoding).collect(Collectors.toSet());
                List<String> needUpdateBids = syncDataList.stream().filter(d -> tosCodingSet.contains(d.getCoding())).map(MBaseData::getBid).collect(Collectors.toList());
                processBatchUpdate(needUpdateBids,
                        TranscendModel.SF.getCode(),
                        updateData);
            }

            return true;
        } catch (Exception e) {
            log.error("同步全集特性树异常", e);
            throw new TranscendBizException("特性同步版本失败", e);
        }
    }

    private void processBatchUpdate(List<?> idList, String modelCode, MObject updateData) {
        if (CollectionUtils.isEmpty(idList)) return;

        List<String> bids = idList.stream()
                .map(obj -> obj instanceof MObject ? ((MObject) obj).getBid() : obj.toString())
                .collect(Collectors.toList());

        objectModelCrudI.batchUpdatePartialContentByIds(modelCode, updateData, bids);
    }


    /**
     * 收集小于当前tos版本的SF的bid集合（需要更新）
     *
     * @param syncDataList        全集特性树同步数据列表
     * @param versionNumberString 版本号
     * @return List<String>
     */
    private List<String> collectUpdateSfBidList(List<MObject> syncDataList, String versionNumberString) {
        // 某个方法，VersionNumberTools就是比较的版本号 全集特性没有版本号，或者 版本号小于当前版本号
        return syncDataList.stream().
                filter(sf -> {
                    Object tosVersionNumber = sf.get(tosBaseSfVersionNumberField);
                    return tosVersionNumber == null
                            || (tosVersionNumber instanceof String && StringUtil.isBlank(tosVersionNumber.toString()))
                            || VersionNumberTools.compareVersion(tosVersionNumber.toString(), versionNumberString) < 0;
                })
                .map(MObject::getBid)
                .collect(Collectors.toList());
    }

    /**
     * IR状态更新触发器
     * 根据IR数据更新相关特性树的版本信息
     *
     * @param event 数据更新事件
     * @param lifeCycleCode 生命周期代码
     * @return 是否成功处理
     */
    @Override
    public boolean irStateUpdateTrigger(BaseDataUpdateEvent event, String lifeCycleCode) {
        log.info("开始处理IR状态更新触发，modelCode: {}", event.getModelCode());

        // Step 1: 收集版本号到IR BID的映射关系
        Map<String, List<String>> versionToIrBidsMap = collectVersionToIrBidsMap(event);
        // 如果没有IR数据，直接返回
        if (versionToIrBidsMap == null) return false;

        // Step 2: 针对每个版本号，更新其关联的TOS特性树和全集特性树的版本号
        versionToIrBidsMap.forEach((versionNumber, irBids) -> {
            log.debug("正在处理版本号：{}，关联的IR数量：{}", versionNumber, irBids.size());

            // 2.1收集有效的TOS特性树数据（此处仅为示意，实际逻辑需根据业务需求调整）
            List<MObject> validTosFeatures = collectValidTosFeatures(irBids);

            if (validTosFeatures == null) return;

            // 2.2: 收集 TOS 特性 BID 和 Coding，准备批量更新
            List<String> tosFeatureBids = validTosFeatures.stream()
                    .map(MObject::getBid)
                    .collect(Collectors.toList());

            List<String> tosFeatureCodings = validTosFeatures.stream()
                    .map(MObject::getCoding)
                    .collect(Collectors.toList());

            // Step 2.3: 构造更新内容
            MObject updateData = new MObject();
            updateData.put(tosBaseSfVersionNumberField, versionNumber);

            // Step 2.4: 批量更新 TOS 特性树和全集特性树的版本号字段
            objectModelCrudI.batchUpdatePartialContentByIds(
                    TranscendModel.RSF.getCode(), updateData, tosFeatureBids);

            objectModelCrudI.batchUpdatePartialContentByCodingList(
                    TranscendModel.SF.getCode(), updateData, tosFeatureCodings);

            log.info("版本号 {} 的特性树更新完成，共更新 {} 条记录", versionNumber, validTosFeatures.size());
        });

        log.info("IR状态更新触发处理完成");
        return true;
    }

    /**
     * 收集版本号到IR BID的映射关系
     * @param event 数据更新事件
     * @return  Map<String, List<String>> 版本号到IR BID的映射关系，如果无法构建则返回null
     */
    @Nullable
    private Map<String, List<String>> collectVersionToIrBidsMap(BaseDataUpdateEvent event) {
        // Step 0: 获取当前事件关联的所有 IR 数据
        List<MObject> irList = objectModelCrudI.list(event.getModelCode(), event.getWrappers());
        log.info("获取到IR数据 {} 条", irList.size());

        // 如果没有IR数据，直接返回成功
        if (CollectionUtils.isEmpty(irList)) {
            log.warn("未查询到任何IR数据，流程终止");
            return null;
        }

        // Step 1: 构建 IR BID -> 交付版本 BID 的映射关系
        Map<String, String> irBidToDeliveryVersionBidMap = irList.stream()
                .filter(ir -> ir.get(deliveryVersionField) != null)
                .collect(Collectors.toMap(
                        MObject::getBid,
                        ir -> ir.get(deliveryVersionField).toString()
                ));

        if (irBidToDeliveryVersionBidMap.isEmpty()) {
            log.warn("IR数据中不包含有效的交付版本信息，更新终止");
            return null;
        }
        log.info("收集到 {} 个IR的交付版本BID", irBidToDeliveryVersionBidMap.size());

        // Step 2: 查询所有对应的交付版本对象
        QueryWrapper deliveryVersionQuery = new QueryWrapper().in(BaseDataEnum.BID.getColumn(),
                irBidToDeliveryVersionBidMap.values());
        List<MObject> deliveryVersions = objectModelCrudI.list(TranscendModel.TOS_DELIVER_VERSION.getCode(),
                QueryWrapper.buildSqlQo(deliveryVersionQuery));

        if (CollectionUtils.isEmpty(deliveryVersions)) {
            log.warn("未查询到对应的交付版本数据，更新终止");
            return null;
        }
        log.info("查询到 {} 条交付版本数据", deliveryVersions.size());

        // Step 3: 构建 交付版本BID -> 版本号 的映射
        Map<String, String> deliveryVersionBidToNumberMap = deliveryVersions.stream()
                .collect(Collectors.toMap(
                        MObject::getBid,
                        dv -> dv.get(ObjectEnum.NAME.getCode()).toString(),
                        (oldValue, newValue) -> oldValue
                ));

        // Step 4: 构建最终的 IR BID -> 版本号 映射
        Map<String, String> irBidToVersionNumberMap = new HashMap<>();
        irBidToDeliveryVersionBidMap.forEach((irBid, deliveryVersionBid) -> {
            if (deliveryVersionBidToNumberMap.containsKey(deliveryVersionBid)) {
                irBidToVersionNumberMap.put(irBid, deliveryVersionBidToNumberMap.get(deliveryVersionBid));
            }
        });
        log.info("构建完成IR BID到版本号的映射，共 {} 条", irBidToVersionNumberMap.size());

        // Step 5: 按照版本号对IR进行分组
        Map<String, List<String>> versionToIrBidsMap = irBidToVersionNumberMap.entrySet().stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getValue,
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                ));
        return versionToIrBidsMap;
    }

    /**
     * 收集有效的TOS特性树数据
     * @param irBids IR BID 列表
     * @return 有效的TOS特性树数据列表
     */
    @Nullable
    private List<MObject> collectValidTosFeatures(List<String> irBids) {
        // Step 1: 获取该批IR对应的所有4级特性关系数据
        QueryWrapper relationQuery = new QueryWrapper().in(RelationEnum.SOURCE_BID.getColumn(), irBids);
        List<MObject> irToSf4Relations = objectModelCrudI.list(iRrSFModelCode, QueryWrapper.buildSqlQo(relationQuery));

        log.info("获取到IR到4级特性的映射关系 {} 条", irToSf4Relations.size());

        // Step 2: 提取所有4级特性 BID
        List<String> sf4Bids = irToSf4Relations.stream()
                .map(m -> m.get(RelationEnum.TARGET_BID.getCode()).toString())
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(sf4Bids)) {
            log.warn("未找到对应的4级特性数据，跳过当前版本更新");
            return null;
        }

        // Step 3: 查询4级特性数据
        List<MObject> sf4List = listByBids(TranscendModel.RSF.getCode(), sf4Bids);

        // 四级特性有可能被删除（移除，与 tos-关联特性的关系解绑了），过滤掉已被移除或无效的数据（如无A1D关联）
        QueryWrapper qo = new QueryWrapper();
        qo.in(RelationEnum.TARGET_BID.getColumn(), sf4Bids);
        List<MObject> relationTosRsfList = objectModelCrudI.list(TranscendModel.RELATION_TOS_RSF.getCode(), QueryWrapper.buildSqlQo(qo));
        Set<String> validSf4BidSet = relationTosRsfList.stream()
               .map(m -> m.get(RelationEnum.TARGET_BID.getCode()).toString())
               .collect(Collectors.toSet());

        // 提取父级BID（用于查找上级特性）
        List<String> sf4ParentBids = sf4List.stream()
                .filter(m -> validSf4BidSet.contains(m.getBid()))
                .map(m -> m.get(ObjectTreeEnum.PARENT_BID.getCode()).toString())
                .collect(Collectors.toList());

        // 4级有效数据
        List<MObject> sf4ValidFeatures = sf4List.stream()
                .filter(m -> validSf4BidSet.contains(m.getBid()))
                .collect(Collectors.toList());

        if (sf4ParentBids.isEmpty()) {
            log.warn("未找到4级特性的父级数据，跳过当前版本更新");
            return null;
        }

        // Step 4: 查找所有相关的TOS特性数据（包括2、3、4级）
        List<MObject> validTosFeatures =  spaceTreeTools.findAllAncestorDataList(
                TranscendModel.RSF.getCode(), sf4ParentBids, 3);

        if (validTosFeatures.isEmpty()) {
            log.warn("未找到需要更新的TOS特性数据，跳过当前版本更新");
            return null;
        }

        // 添加4级有效数据
        validTosFeatures.addAll(sf4ValidFeatures);

        log.info("查询到所有需要更新的TOS特性数据，共 {} 条", validTosFeatures.size());
        return validTosFeatures;
    }

    /**
     * 查找所有相关的TOS特性数据（包括2、3、4级）
     * @param sf4ParentBids 4级特性的父级BID列表
     * @return TOS特性数据列表
     */
    @NotNull
    private List<MObject> listVaildTosFeatures(List<String> sf4ParentBids) {


        List<MObject> tosAllSfMaybeDeletedList = spaceTreeTools.findAllAncestorDataList(
                TranscendModel.RSF.getCode(), sf4ParentBids, 3);
        // 收集 TOS 特性数据列表
        List<String> tosAllSfBids = tosAllSfMaybeDeletedList.stream()
                .map(MObject::getBid)
                .collect(Collectors.toList());

        // 过滤掉已被移除或无效的数据（如无A1D关联）
        QueryWrapper qo = new QueryWrapper();
        qo.in(RelationEnum.TARGET_BID.getColumn(), tosAllSfBids);
        List<MObject> relationTosRsfList = objectModelCrudI.list(TranscendModel.RELATION_TOS_RSF.getCode(), QueryWrapper.buildSqlQo(qo));

        List<MObject> validTosFeatures = relationTosRsfList.stream()
               .filter(relation -> relation.get(RelationEnum.TARGET_BID.getCode())!= null)
               .collect(Collectors.toList());
        return validTosFeatures;
    }

    /**
     * 更新版本映射
     * 将特性BID与对应的IR版本关联
     *
     * @param tosSfToVersionMap 版本映射
     * @param sfBid 特性BID
     * @param irVersion IR版本
     */
    private void updateVersionMap(Map<String, String> tosSfToVersionMap, String sfBid, String irVersion) {
        if (sfBid != null && irVersion != null) {
            tosSfToVersionMap.put(sfBid, irVersion);
        }
    }

    /**
     * 根据BID列表查询对象
     */
    private List<MObject> listByBids(String modelCode, Collection<String> bids) {
        if (CollectionUtils.isEmpty(bids)) {
            return Collections.emptyList();
        }

        QueryWrapper qo = new QueryWrapper();
        qo.in(BaseDataEnum.BID.getColumn(), bids);
        return objectModelCrudI.list(modelCode, QueryWrapper.buildSqlQo(qo));
    }


}
