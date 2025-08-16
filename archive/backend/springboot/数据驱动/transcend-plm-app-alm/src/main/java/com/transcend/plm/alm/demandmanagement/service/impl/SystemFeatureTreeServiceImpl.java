package com.transcend.plm.alm.demandmanagement.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.core.constant.TenantConst;
import com.transcend.plm.alm.demandmanagement.entity.ao.SfTreeDataSyncCopyAo;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.alm.demandmanagement.mapstruct.AlmSystemFeatureDtoConverter;
import com.transcend.plm.alm.demandmanagement.service.SystemFeatureSyncDataChangeCollector;
import com.transcend.plm.alm.demandmanagement.service.SystemFeatureTreeService;
import com.transcend.plm.alm.demandmanagement.service.SystemFeatureVersionAutoUpdateRuleService;
import com.transcend.plm.alm.openapi.dto.AlmSystemFeatureDTO;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.apm.log.OperationLogEsService;
import com.transcend.plm.datadriven.apm.log.model.dto.GenericLogAddParam;
import com.transcend.plm.datadriven.apm.log.model.dto.OperationLogAddParam;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppConfigDrivenService;
import com.transcend.plm.datadriven.apm.tools.ViewMetaUtils;
import com.transcend.plm.datadriven.common.tool.Assert;
import com.transcend.plm.datadriven.common.util.EsUtil;
import com.transcend.plm.datadriven.common.wapper.TranscendObjectWrapper;
import com.transcend.plm.datadriven.common.wapper.TranscendRelationWrapper;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.Diff;
import org.apache.commons.lang3.builder.DiffResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 特性树服务
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/14 11:01
 */
@Log4j2
@Service
@AllArgsConstructor
public class SystemFeatureTreeServiceImpl implements SystemFeatureTreeService {

    private final ObjectModelStandardI<MObject> objectModelCrudI;
    private final OperationLogEsService operationLogEsService;
    private final IApmSpaceAppConfigDrivenService apmSpaceAppConfigDrivenService;


    private final SystemFeatureVersionAutoUpdateRuleService systemFeatureVersionAutoUpdateRuleService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncData(SfTreeDataSyncCopyAo ao) {

        Assert.notNull(ao, "同步参数不能为空");

        //1、查询要同步的全局的数据
        List<MObject> syncDataList = getFeatureData(TranscendModel.SF, ao.getBidList());
        Assert.notEmpty(syncDataList, "同步数据不能为空");

        //2、查询原始关联数据
        List<MObject> existDataList = getExistData(ao.getRelationModelCode(), ao.getSourceBid());

        //3、收集更新数据
        SystemFeatureSyncDataChangeCollector collector = new SystemFeatureSyncDataChangeCollector(
                ao.getSpaceBid(), ao.getSpaceAppBid());
        collector.collect(syncDataList, existDataList);

        //4、保存数据
        //新增保存
        batchAdd(ao.getRelationModelCode(), ao.getSourceBid(), collector.getInstanceAddList());
        //更新数据
        batchUpdate(collector.getInstanceUpdateList());
        //删除数据
        batchDelete(ao.getRelationModelCode(), ao.getSourceBid(), collector.getInstanceDeleteList());

        //同步反写版本
        systemFeatureVersionAutoUpdateRuleService.syncTosSystemFeature(ao, collector, syncDataList);

        //5、记录日志
        CompletableFuture.runAsync(() -> {
            try {
                saveOperationLog(ao.getSpaceAppBid(), ao.getSourceBid(), collector);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
        });
    }

    @Override
    public List<AlmSystemFeatureDTO> getSystemFeatureTree(String searchKey) {
        QueryWrapper wrapper = new QueryWrapper().eq(BaseDataEnum.DELETE_FLAG.getColumn(), 0);
        //全部数据查询，搜索也需要补偿，只能在程序中进行处理
        List<MObject> list = objectModelCrudI.list(
                TranscendModel.SF.getCode(), QueryWrapper.buildSqlQo(wrapper)
        );

        //转换为传输对象
        List<AlmSystemFeatureDTO> featureList = AlmSystemFeatureDtoConverter.INSTANCE.toDtoList(list);

        //组装成树
        List<AlmSystemFeatureDTO> treeList = buildTree(featureList, Comparator.comparing(AlmSystemFeatureDTO::getName));

        //过滤条件
        treeList = searchTree(false, treeList, searchKey);

        //仅保留有四级特性树数据
        return onlyLevel4FeatureTree(treeList);
    }


    @Override
    public AlmSystemFeatureDTO getSystemFeatureByBid(String bid) {
        MObject object = objectModelCrudI.getByBid(TranscendModel.SF.getCode(), bid);
        return AlmSystemFeatureDtoConverter.INSTANCE.toDto(object);
    }


    //region 私有方法

    /**
     * 查询特性数列表
     *
     * @param model   模型
     * @param bidList 特性Bid列表
     * @return 全局特性树列表
     */
    @Nonnull
    private List<MObject> getFeatureData(TranscendModel model, List<String> bidList) {
        if (CollUtil.isEmpty(bidList)) {
            return Collections.emptyList();
        }
        return Optional.ofNullable(objectModelCrudI.listByBids(bidList, model.getCode()))
                .orElse(Collections.emptyList());
    }

    /**
     * 查询已存在的
     *
     * @param relationModelCode 关系模型编码
     * @param sourceBid         数据源bid
     * @return 关联数据列表
     */
    @Nonnull
    private List<MObject> getExistData(String relationModelCode, String sourceBid) {
        Assert.notBlank(relationModelCode, "同步关联数据模型编码不能为空");
        Assert.notBlank(sourceBid, "同步关联数据Bid不能为空");

        //查询关系数据
        QueryWrapper relationWrapper = new QueryWrapper().eq(RelationEnum.SOURCE_BID.getColumn(), sourceBid)
                .and().eq(BaseDataEnum.DELETE_FLAG.getColumn(), 0);
        List<MObject> list = objectModelCrudI.list(relationModelCode, QueryWrapper.buildSqlQo(relationWrapper));

        //查询实例数据
        List<String> bidList = list.stream().map(TranscendRelationWrapper::new)
                .map(TranscendRelationWrapper::getTargetBid).collect(Collectors.toList());
        return getFeatureData(TranscendModel.RSF, bidList);
    }

    /**
     * 批量新增
     *
     * @param relationModelCode 关系模型编码
     * @param sourceBid         数据源bid
     * @param dataList          实例新增列表
     */
    private void batchAdd(String relationModelCode, String sourceBid, List<MObject> dataList) {
        if (CollUtil.isEmpty(dataList)) {
            return;
        }

        objectModelCrudI.addBatch(TranscendModel.RSF.getCode(), dataList);
        objectModelCrudI.addBatch(relationModelCode, convertRealtionList(relationModelCode, sourceBid, dataList));
    }

    /**
     * 转换为关系数据
     *
     * @param relationModelCode 关系模型编码
     * @param sourceBid         数据源bid
     * @param dataList          数据列表
     * @return 关系数据列表
     */
    @NotNull
    private static List<MObject> convertRealtionList(String relationModelCode, String
            sourceBid, List<MObject> dataList) {
        return dataList.stream().map(TranscendObjectWrapper::new)
                .map(instance -> {
                    MObject relationObj = new MObject();

                    TranscendRelationWrapper relation = new TranscendRelationWrapper(relationObj);
                    relation.setModelCode(relationModelCode);
                    relation.setSourceBid(sourceBid);
                    relation.setSourceDataBid(sourceBid);

                    relation.setSpaceBid(instance.getSpaceBid());
                    relation.setSpaceAppBid(instance.getSpaceAppBid());

                    relation.setTargetBid(instance.getBid());
                    relation.setTargetDataBid(instance.getBid());
                    relation.setDraft(false);
                    relation.setPermissionBid("DEFAULT:6666666666666");
                    relation.setBid(SnowflakeIdWorker.nextIdStr());
                    relation.setTenantId(String.valueOf(TenantConst.TENANT_ID_DEFAULT));
                    relation.setDeleteFlag(false);
                    relation.setEnableFlag(true);
                    relation.put(VersionObjectEnum.DATA_BID.getCode(), instance.getBid());

                    return relationObj;
                }).collect(Collectors.toList());
    }

    /**
     * 批量更新操作
     *
     * @param dataList 数据列表
     */
    private void batchUpdate(List<MObject> dataList) {
        if (CollUtil.isEmpty(dataList)) {
            return;
        }

        List<BatchUpdateBO<MObject>> updateList = dataList.stream().map(obj -> {
            BatchUpdateBO<MObject> updateBo = new BatchUpdateBO<>();
            String id = obj.getId();
            Assert.notBlank(id, "更新实例id不能为空");
            QueryWrapper wrapper = new QueryWrapper().eq(BaseDataEnum.ID.getColumn(), id)
                    .and().eq(BaseDataEnum.DELETE_FLAG.getColumn(), 0);
            updateBo.setWrappers(QueryWrapper.buildSqlQo(wrapper));
            updateBo.setBaseData(obj);
            return updateBo;
        }).collect(Collectors.toList());
        objectModelCrudI.batchUpdateByQueryWrapper(TranscendModel.RSF.getCode(), updateList, false);
    }


    /**
     * 批量删除
     *
     * @param relationModelCode 关系模型编码
     * @param sourceBid         数据源bid
     * @param dataList          删除数据
     */
    private void batchDelete(String relationModelCode, String sourceBid, List<MObject> dataList) {
        if (CollUtil.isEmpty(dataList)) {
            return;
        }

        List<String> bidList = dataList.stream().map(MBaseData::getBid).collect(Collectors.toList());
        objectModelCrudI.deleteRel(relationModelCode, sourceBid, bidList);
        objectModelCrudI.batchLogicalDeleteByBids(TranscendModel.RSF.getCode(), bidList);
    }


    /**
     * 报错操作日志
     *
     * @param spaceAppBid 数据空间应用bid
     * @param sourceBid   数据源bid
     * @param collector   变更收集器对象
     */
    private void saveOperationLog(String spaceAppBid, String sourceBid, SystemFeatureSyncDataChangeCollector
            collector) {

        //新增日志
        List<GenericLogAddParam> addLogParamList = collector.getInstanceAddList().stream()
                .map(TranscendObjectWrapper::new).map(instance ->
                        GenericLogAddParam.builder()
                                .spaceBid(instance.getSpaceBid())
                                .modelCode(instance.getModelCode())
                                .instanceBid(instance.getBid())
                                .logMsg(String.format("同步新增 [%s]%s 特性", instance.getBid(), instance.getName()))
                                .type(EsUtil.EsType.LOG.getType())
                                .build()
                ).collect(Collectors.toList());

        //删除日志
        List<GenericLogAddParam> deleteLogList = collector.getInstanceDeleteList()
                .stream().map(TranscendObjectWrapper::new).map(instance ->
                        GenericLogAddParam.builder()
                                .spaceBid(instance.getSpaceBid())
                                .modelCode(instance.getModelCode())
                                .instanceBid(sourceBid)
                                .logMsg(String.format("同步删除 [%s]%s 特性", instance.getBid(), instance.getName()))
                                .type(EsUtil.EsType.LOG.getType())
                                .build()
                ).collect(Collectors.toList());

        List<GenericLogAddParam> logParamList = new ArrayList<>();
        logParamList.addAll(addLogParamList);
        logParamList.addAll(deleteLogList);
        if (CollUtil.isNotEmpty(logParamList)) {
            operationLogEsService.genericBulkSave(logParamList);
        }

        //更新日志
        List<DiffResult<MObject>> instanceUpdateDiffList = collector.getInstanceUpdateDiffList();
        if (CollUtil.isNotEmpty(instanceUpdateDiffList)) {
            List<CfgViewMetaDto> metaList = apmSpaceAppConfigDrivenService.baseViewGetMeteModels(spaceAppBid);
            metaList = ViewMetaUtils.compensationMetadata(TranscendModel.RSF.getCode(), metaList);

            Map<String, CfgViewMetaDto> metaMap = metaList.stream().collect(Collectors.toMap(CfgViewMetaDto::getName, Function.identity(), (o1, o2) -> o1));

            instanceUpdateDiffList.forEach(diffResult -> {
                List<Diff<?>> diffs = diffResult.getDiffs();
                TranscendObjectWrapper oldObj = new TranscendObjectWrapper(diffResult.getRight());
                diffs.forEach(diff -> {
                    OperationLogAddParam operationLogAddParam = OperationLogAddParam.builder()
                            .cfgViewMetaDto(metaMap.get(diff.getFieldName())).isAppView(true)
                            .modelCode(oldObj.getModelCode())
                            .spaceBid(oldObj.getSpaceBid())
                            .spaceAppBid(oldObj.getSpaceAppBid())
                            .instanceBid(oldObj.getBid())
                            .fieldName(diff.getFieldName())
                            .fieldValue(diff.getLeft())
                            .build();
                    operationLogEsService.save(operationLogAddParam, diff.getRight());
                });
            });
        }

    }


    /**
     * 构建树形结构
     *
     * @param flatList   平铺列表
     * @param comparator 排序比较器
     * @return 树形结构列表
     */
    @Nonnull
    private static List<AlmSystemFeatureDTO> buildTree(List<AlmSystemFeatureDTO> flatList,
                                                       Comparator<AlmSystemFeatureDTO> comparator) {
        if (flatList == null || flatList.isEmpty()) {
            return Collections.emptyList();
        }

        //通过父级节点进行分组
        String rootKey = "0";
        Map<String, List<AlmSystemFeatureDTO>> parentGroupMap = flatList.stream()
                .collect(Collectors.groupingBy(node -> Optional.ofNullable(node.getParentBid())
                        .filter(StringUtils::isNotBlank).orElse(rootKey)));


        //获取根节点
        List<AlmSystemFeatureDTO> rootList = parentGroupMap.get(rootKey);
        if (CollUtil.isEmpty(rootList)) {
            return Collections.emptyList();
        }

        //排序操作
        if (comparator != null) {
            parentGroupMap.values().forEach(list -> list.sort(comparator));
        }

        //填充子节点
        rootList.forEach(rootNode -> putChildren(1, rootNode, parentGroupMap));

        return rootList;
    }

    /**
     * 填充子节点
     *
     * @param depth          当前层级
     * @param parentNode     父节点
     * @param parentGroupMap 所需节点的元数据信息
     */
    private static void putChildren(int depth, AlmSystemFeatureDTO parentNode,
                                    Map<String, List<AlmSystemFeatureDTO>> parentGroupMap) {
        //放在树的层级太深，或者多层循环依赖
        int maxDepth = 10;
        if (depth >= maxDepth) {
            return;
        }

        String parentBid = parentNode.getBid();
        if (StringUtils.isBlank(parentBid)) {
            return;
        }

        List<AlmSystemFeatureDTO> children = parentGroupMap.get(parentBid);
        if (children == null) {
            return;
        }

        parentNode.setChildren(children);

        //递归填充子节点
        final int nextDepth = depth + 1;
        children.forEach(node -> putChildren(nextDepth, node, parentGroupMap));
    }


    /**
     * 判断是否匹配
     *
     * @param dto     特性树节点对象
     * @param keyword 关键字
     * @return 是否匹配
     */
    private boolean isMatched(AlmSystemFeatureDTO dto, String keyword) {
        if (dto == null) {
            return false;
        }
        return dto.getName() != null && dto.getName().contains(keyword);
    }

    /**
     * 递归搜索树
     *
     * @param parentMatched 是否父节点匹配结果
     * @param list          特性树节点列表
     * @param keyword       关键字
     * @return 匹配的节点列表
     */
    public List<AlmSystemFeatureDTO> searchTree(boolean parentMatched, List<AlmSystemFeatureDTO> list, String keyword) {
        if (CollUtil.isEmpty(list) || StringUtils.isBlank(keyword)) {
            return list;
        }

        //进行过滤操作
        List<AlmSystemFeatureDTO> filterList = list.stream().filter(dto -> {
            boolean matched = isMatched(dto, keyword);
            List<AlmSystemFeatureDTO> children = searchTree(matched, dto.getChildren(), keyword);
            dto.setChildren(children);

            //匹配上或者子节点有数据，则当前节点必须返回
            return matched || CollUtil.isNotEmpty(children);
        }).collect(Collectors.toList());

        //如果父节点匹配，但是子节点都不匹配，则返回全部节点数据
        if (parentMatched && filterList.isEmpty()) {
            return list;
        }

        return filterList;
    }


    /**
     * 仅保留有4级特性的数据
     *
     * @param treeList 树形结构列表
     * @return 过滤仅4级特性树形结构列表
     */
    private static List<AlmSystemFeatureDTO> onlyLevel4FeatureTree(List<AlmSystemFeatureDTO> treeList) {
        if (treeList == null || treeList.isEmpty()) {
            return treeList;
        }
        return treeList.stream().filter(node -> {
            List<AlmSystemFeatureDTO> children = onlyLevel4FeatureTree(node.getChildren());
            node.setChildren(children);
            return node.getLevel() >= 3 || CollUtil.isNotEmpty(children);
        }).collect(Collectors.toList());
    }
    //endregion
}
