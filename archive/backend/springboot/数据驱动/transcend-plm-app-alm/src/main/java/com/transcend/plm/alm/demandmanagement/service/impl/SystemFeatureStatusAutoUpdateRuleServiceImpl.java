package com.transcend.plm.alm.demandmanagement.service.impl;

import com.transcend.plm.alm.demandmanagement.constants.SystemFeatureConstant;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.alm.demandmanagement.service.SystemFeatureStatusAutoUpdateRuleService;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.apm.constants.IrLifeCycleConstant;
import com.transcend.plm.datadriven.apm.constants.SfLifeCycleConstant;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.apm.tools.SpaceTreeTools;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transcend.plm.datadriven.infrastructure.basedata.event.BaseDataUpdateEvent;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 特性状态自动更新规则服务
 *
 * @author jie.luo1  <jie.luo1@transsion.com>
 * @version 1.0
 * createdAt 2025/5/26 16:05
 */
@Slf4j
@Service
public class SystemFeatureStatusAutoUpdateRuleServiceImpl implements SystemFeatureStatusAutoUpdateRuleService {

    @Resource
    private IBaseApmSpaceAppDataDrivenService iBaseApmSpaceAppDataDrivenService;
    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;
    /**
     * 特性-IR:A1L
     */
    private final String SFrIRModelCode = "A1L";
    /**
     * 特性:A1AA01
     */
    private final String sfModelCode = "A1AA01";

    /**
     * IR-特性:A1L
     */
    private final String iRrSFModelCode = TranscendModel.RELATION_IR_RSF.getCode();

    @Resource
    private SpaceTreeTools spaceTreeTools;

    /**
     * IR（A01）状态变化触发特性状态变化：
     * 任意IR（A01）到开发，且SF（A1AA01）无条件，则自动到实现中
     * 全部IR（A01）到完成，且SF（A1AA01）无条件，则自动到已实现
     *
     * @param event         IR的BID列表
     * @param lifeCycleCode 生命周期编码
     * @return boolean
     */
    @Override
    public boolean irStateUpdateTrigger(BaseDataUpdateEvent event, String lifeCycleCode) {

        String sfLifeCycleCode = lifeCycleCode.equals(IrLifeCycleConstant.DEVELOP) ?
                SfLifeCycleConstant.BEING_IMPLEMENTED : SfLifeCycleConstant.REALIZED;
        // 收集待更新的SF
        //查询IR原始数据
        List<MObject> list = objectModelCrudI.list(event.getModelCode(), event.getWrappers());
        List<String> irBidList = list.stream().map(MObject::getBid).collect(Collectors.toList());


        // 查询IR关联的4,3,2,1 特性关系有效数据
        List<MObject> validAllTosFeatures = collectValidTosFeatures(irBidList);

        if (CollectionUtils.isEmpty(validAllTosFeatures)) {
            log.info("IR状态更新触发特性状态变化： 未找到关联的特性");
            return false;
        }

        // 如果是 完成状态，需要判断2级下的IR是否全部完成，才可以更新状态
        if (IrLifeCycleConstant.COMPLETE.equals(lifeCycleCode) && checkNeedHandleCompleteState(validAllTosFeatures)) {
            return false;
        }


        // 2.2: 收集 TOS 特性 BID 和 Coding，准备批量更新
        List<String> tosFeatureBids = validAllTosFeatures.stream()
                .map(MObject::getBid)
                .collect(Collectors.toList());

        List<String> tosFeatureCodings = validAllTosFeatures.stream()
                .map(MObject::getCoding)
                .collect(Collectors.toList());

        // Step 2.3: 构造更新内容
        MObject updateData = new MObject();
        updateData.setLifeCycleCode(sfLifeCycleCode);

        // Step 2.4: 批量更新 TOS 特性树和全集特性树的版本号字段
        objectModelCrudI.batchUpdatePartialContentByIds(
                TranscendModel.RSF.getCode(), updateData, tosFeatureBids);

        objectModelCrudI.batchUpdatePartialContentByCodingList(
                TranscendModel.SF.getCode(), updateData, tosFeatureCodings);

        return false;
    }

    private boolean checkNeedHandleCompleteState(List<MObject> validAllTosFeatures) {
        // 收集2级特性数据
        List<String> sf2Bids = validAllTosFeatures.stream()
                .filter(m -> 1 == Integer.parseInt(m.get(SystemFeatureConstant.SF_LEVEL).toString()))
                .map(MObject::getBid)
                .collect(Collectors.toList());

        // 查询2级特性下的IR 数据
        // 查询IR被2SF挂的数据 (SF关联IR的modelCode:特性-IR:A1L)
        QueryWrapper wrapper = new QueryWrapper().in(RelationEnum.SOURCE_BID.getColumn(), sf2Bids);
        List<MObject> relationList = objectModelCrudI.list(SFrIRModelCode, QueryWrapper.buildSqlQo(wrapper));

        // 收集2SF下的IR BID()
        List<String> irBids = relationList.stream()
                // 这个数据的TARGET_BID 与 irBidList 匹配
//                    .filter(m -> !irBidList.contains(m.get(RelationEnum.TARGET_BID.getCode()).toString()))
                .map(m -> m.get(RelationEnum.TARGET_BID.getCode()).toString())
                .collect(Collectors.toList());

        // 查询所有的IR数据
        List<MObject> irList = listByBids(TranscendModel.IR.getCode(), irBids);
        // 查看是否满足所有ir的状态都为完成


        // 收集所有的IR数据 完成状态的ir数据
        List<MObject> completeIrList = irList.stream()
                .filter(m -> IrLifeCycleConstant.COMPLETE.equals(m.get(TranscendModelBaseFields.LIFE_CYCLE_CODE)))
                .collect(Collectors.toList());

        // 比较
        if (completeIrList.size() != irList.size()) {
            log.info("IR状态更新触发特性状态变化： 2级下的IR未全部完成，无法更新状态为已实现");
            return true;
        }
        return false;
    }

    private void processBatchUpdate(List<?> idList, String modelCode, MObject updateData) {
        if (CollectionUtils.isEmpty(idList)) return;

        List<String> bids = idList.stream()
                .map(obj -> obj instanceof MObject ? ((MObject) obj).getBid() : obj.toString())
                .collect(Collectors.toList());

        objectModelCrudI.batchUpdatePartialContentByIds(modelCode, updateData, bids);
    }

    /**
     * 收集有效的TOS特性树数据（1,2,3,4）
     *
     * @param irBids IR BID 列表
     * @return 有效的TOS特性树数据列表
     */
    @Nullable
    private List<MObject> collectValid4LevelTosFeatures(List<String> irBids) {
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

        if (CollectionUtils.isEmpty(validSf4BidSet)) {
            log.warn("未找到有效的4级特性数据，跳过当前版本更新");
            return null;
        }

        // 4级有效数据
        return sf4List.stream()
                .filter(m -> validSf4BidSet.contains(m.getBid()))
                .collect(Collectors.toList());
    }

    /**
     * 收集有效的TOS特性树数据（1,2,3,4）
     *
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
        List<MObject> validTosFeatures = spaceTreeTools.findAllAncestorDataList(
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

//    /**
//     * 查询父对象Bid列表
//     *
//     * @param modelCode 关系模型编码
//     * @param bidList       子对象bid
//     * @return 父对象Bid列表
//     */
//    @NotNull
//    private List<String> listNextLevelByBidList(String modelCode, List<String> bidList) {
//        QueryWrapper wrapper = new QueryWrapper().in(DemandManagementEnum.PARENT_BID.getColumn(), bidList);
//        List<MObject> nextLevelList = objectModelCrudI.list(modelCode, QueryWrapper.buildSqlQo(wrapper));
//        return nextLevelList.stream().map(TranscendRelationWrapper::new)
//                .map(TranscendRelationWrapper::getBid).distinct().collect(Collectors.toList());
//    }
}
