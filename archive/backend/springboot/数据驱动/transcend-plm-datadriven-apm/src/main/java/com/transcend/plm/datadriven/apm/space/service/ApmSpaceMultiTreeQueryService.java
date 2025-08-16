package com.transcend.plm.datadriven.apm.space.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.qo.ModelFilterQo;
import com.transcend.plm.datadriven.apm.space.model.AppViewModelEnum;
import com.transcend.plm.datadriven.apm.space.pojo.dto.MultiAppConfig;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmMultiTreeModelMixQo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceAppVo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.MultiTreeConfigVo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmAppViewConfig;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceAppViewModelPo;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmAppViewConfigService;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppViewModelService;
import com.transcend.plm.datadriven.apm.space.utils.MultiTreeUtils;
import com.transcend.plm.datadriven.apm.tools.StayDurationHandler;
import com.transcend.plm.datadriven.common.tool.Assert;
import com.transcend.plm.datadriven.common.tool.ObjectTreeTools;
import com.transcend.plm.datadriven.common.tool.QueryConveterTool;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.common.wapper.TranscendRelationWrapper;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 空间应用多对象树查询服务
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/15 10:06
 */
@Slf4j
@Service
public class ApmSpaceMultiTreeQueryService {
    private final String TREE_ROOT_FLAG = "0";


    @Resource
    private ApmSpaceAppService apmSpaceAppService;
    @Resource
    private ApmAppViewConfigService apmAppViewConfigService;
    @Resource
    private ApmSpaceAppViewModelService apmSpaceAppViewModelService;
    @Resource
    private MultiTreeDataQueryHolder multiTreeDataQueryHolder;
    @Resource
    private DictionaryApplicationService dictionaryApplicationService;
    @Resource
    private ObjectModelStandardI<MObject> objectModelCrudI;

    /**
     * 多对象树查询
     *
     * @param spaceBid        空间Bid
     * @param spaceAppBid     空间应用Bid
     * @param modelMixQo      多对象树查询条件
     * @param checkPermission 是否检查权限
     * @return List<MObjectTree> 多对象树数据
     */
    public List<MObjectTree> multiTree(String spaceBid, String spaceAppBid, ApmMultiTreeModelMixQo modelMixQo, Boolean checkPermission) {
        if (modelMixQo == null) {
            return Collections.emptyList();
        }


        Boolean strictMode = modelMixQo.getStrictMode();
        List<String> outputModelCodeList = modelMixQo.getOutputModelCodeList();
        Boolean needCompensationTreeBid = modelMixQo.getNeedCompensationTreeBid();

        //参数准备
        MultiAppConfig multiAppConfig = getMultiAppConfig(spaceAppBid, modelMixQo.getMultiAppConfig());
        Assert.notNull(multiAppConfig, "多对象树配置信息不存在");
        MultiTreeConfigVo multiAppTreeConfig = multiAppConfig.getMultiAppTreeConfig();
        Assert.notNull(multiAppTreeConfig, "多对象树配置信息不存在");

        //防止有传入空条件的情况
        List<ModelFilterQo> queries = modelMixQo.getQueries();
        if (CollUtil.isNotEmpty(queries)) {
            queries = queries.stream().filter(filter -> StringUtils.isNotBlank(filter.getProperty()))
                    .filter(filter -> filter.getValue() != null || filter.getValues() == null).collect(Collectors.toList());
            modelMixQo.setQueries(queries);
        }

        /*//从筛选条件中获取需要展示的数据
        List<String> exclusiveQueriesModelCodes = getExclusiveQueriesModelCodes(queries);
        if (CollUtil.isNotEmpty(exclusiveQueriesModelCodes)) {

            //没有全部条件才进行自动剔除输出层级
            List<ModelFilterQo> globalQueries = getGlobalQueries(queries);
            if (CollUtil.isEmpty(globalQueries)) {
                outputModelCodeList = exclusiveQueriesModelCodes;
            }

            //有独立条件时，自动开启严谨模型
            if (strictMode == null) {
                strictMode = true;
            }
        }*/
        //有独立条件时，自动开启严谨模型
        if (strictMode == null) {
            strictMode = true;
        }

        //获取需要的配置列表
        MultiTreeConfigVo needConfigList = multiAppTreeConfig;
        if (CollUtil.isNotEmpty(outputModelCodeList)) {
            needConfigList = MultiTreeUtils.getNeedConfigList(multiAppTreeConfig, new HashSet<>(outputModelCodeList));
        }

        if (needConfigList == null) {
            return Collections.emptyList();
        }

        //加载所有所需的数据
        MultiTreeDataQueryService.Data resultData = multiTreeDataQueryHolder.query(
                new MultiTreeDataQueryService.Params()
                        .setEmpNo(SsoHelper.getJobNumber())
                        .setSpaceBid(spaceBid)
                        .setCheckPermission(checkPermission)
                        .setMultiAppTreeConfig(needConfigList)
                        .setQueryWrapperMap(getModelWrapperMap(modelMixQo, needConfigList))
                        .setStrictMode(strictMode)
        );

        List<MObject> instanceList = resultData.getInstanceList();
        List<MObject> relationList = resultData.getRelationList();

        //ALM特殊逻辑 计算停留时长
        StayDurationHandler.handle(instanceList);

        //组装成树
        List<MObjectTree> treeList = convertTree(relationList, instanceList);

        //过滤掉不满足根展示的数据
        if (!Boolean.TRUE.equals(strictMode)) {
            Set<String> rootModelCodeList = getRootModelCodeList(multiAppConfig);
            treeList = treeList.stream().
                    filter(item -> rootModelCodeList.contains(item.getModelCode())).
                    collect(Collectors.toList());
        }

        //补偿树bid 要求补偿，并且在严谨模式下
        if (CollUtil.isNotEmpty(treeList) && Boolean.TRUE.equals(needCompensationTreeBid) && Boolean.TRUE.equals(strictMode)) {
            compensationTreeBid(multiAppTreeConfig, treeList.stream().collect(Collectors.groupingBy(MObject::getModelCode)));
        }

        //对数据排序
        List<Order> orders = modelMixQo.getOrders();
        if (CollectionUtils.isNotEmpty(orders)) {
            orders.add(0, new Order().setProperty(ObjectEnum.MODEL_CODE.getCode()));
            treeList = ObjectTreeTools.sortObjectList(orders, treeList, dictionaryApplicationService.getDictSortMap(orders));
        }

        return treeList;
    }


    //region 私有方法

    /**
     * 获取应用配置
     *
     * @param spaceAppBid 空间应用配置
     * @param enterConfig 输入配置
     * @return 应用配置 可能为空
     */
    @Nullable
    private MultiAppConfig getMultiAppConfig(String spaceAppBid, MultiAppConfig enterConfig) {

        //配置齐全时，直接使用传入配置
        if (enterConfig != null && enterConfig.getMultiAppTreeConfig() != null) {
            return enterConfig;
        }

        //参数要求使用导航配置
        if (Boolean.TRUE.equals(Optional.ofNullable(enterConfig).map(MultiAppConfig::isNavQuery).orElse(null))) {
            ApmAppViewConfig config = apmAppViewConfigService.getOne(apmAppViewConfigService.lambdaQuery().getWrapper()
                    .eq(ApmAppViewConfig::getBid, enterConfig.getAppViewConfigBid()));
            if (config != null && CollUtil.isNotEmpty(config.getNavConfigContent())) {
                return JSON.parseObject(JSON.toJSONString(config.getNavConfigContent()), MultiAppConfig.class);
            }
        }

        //获取视图模型配置中的应用配置
        LambdaQueryWrapper<ApmSpaceAppViewModelPo> wrapper = Wrappers.<ApmSpaceAppViewModelPo>lambdaQuery()
                .eq(ApmSpaceAppViewModelPo::getSpaceAppBid, spaceAppBid)
                .eq(ApmSpaceAppViewModelPo::getCode, AppViewModelEnum.MULTI_TREE_VIEW.getCode());

        ApmSpaceAppViewModelPo viewModel = apmSpaceAppViewModelService.getOne(wrapper);
        if (viewModel != null && CollUtil.isNotEmpty(viewModel.getConfigContent())) {
            return JSON.parseObject(JSON.toJSONString(viewModel.getConfigContent()), MultiAppConfig.class);
        }

        return null;
    }

    /**
     * 获取根节点的模型code
     *
     * @param multiAppConfig 应用配置
     * @return 根节点的模型code
     */
    private Set<String> getRootModelCodeList(MultiAppConfig multiAppConfig) {
        List<String> rootSpaceAppBidList = multiAppConfig.getMultiAppTreeFirstLevel();
        if (CollUtil.isEmpty(rootSpaceAppBidList)) {
            return Collections.emptySet();
        }

        Map<String, String> spaceAppBidWithModelCodeMap = apmSpaceAppService.listSpaceInfo(rootSpaceAppBidList)
                .stream().collect(Collectors.toMap(ApmSpaceAppVo::getBid, ApmSpaceAppVo::getModelCode));
        return rootSpaceAppBidList.stream().map(spaceAppBidWithModelCodeMap::get)
                .filter(Objects::nonNull).collect(Collectors.toSet());
    }


    /**
     * 获取各模型所需的查询条件
     *
     * @param modelMixQo         查询条件
     * @param multiAppTreeConfig 多对象树配置
     * @return 查询条件
     */
    @NotNull
    private static Map<String, List<QueryWrapper>> getModelWrapperMap(ApmMultiTreeModelMixQo modelMixQo,
                                                                      MultiTreeConfigVo multiAppTreeConfig) {
        List<String> modelCodes = MultiTreeUtils.getInstanceFlatModelCodes(multiAppTreeConfig);
        List<ModelFilterQo> queries = modelMixQo.getQueries();
        List<ModelFilterQo> globalQueries = getGlobalQueries(queries);
        Map<String, List<ModelFilterQo>> exclusiveQueries = getModelExclusiveQueries(queries);

        return modelCodes.stream().distinct().map(modelCode -> {
            ArrayList<ModelFilterQo> modelFilterQos = new ArrayList<>(globalQueries);
            List<ModelFilterQo> filterList = exclusiveQueries.get(modelCode);
            if (CollUtil.isNotEmpty(filterList)) {
                modelFilterQos.addAll(filterList);
            }
            List<QueryWrapper> wrappers = QueryConveterTool.addGroupCondition(modelMixQo,
                    QueryConveterTool.convert(modelFilterQos, modelMixQo.getAnyMatch()));
            return new AbstractMap.SimpleEntry<>(modelCode, wrappers);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * 转换为树对象
     *
     * @param relationList 关系数据列表
     * @param instanceList 实例数据列表
     * @return 树对象
     */
    @NotNull
    private List<MObjectTree> convertTree(List<MObject> relationList, List<MObject> instanceList) {

        //关系集合转换为Map，需考虑一个目标被多个源关联的情况
        Map<String, List<TranscendRelationWrapper>> relationMap = Optional.of(relationList)
                .map(list -> list.stream().map(TranscendRelationWrapper::new)
                        .collect(Collectors.groupingBy(TranscendRelationWrapper::getTargetBid)))
                .orElseGet(Collections::emptyMap);

        //转换成树 且 在子对象中写入父bid
        List<MObjectTree> treeList = instanceList.parallelStream().flatMap(instance -> {
            List<TranscendRelationWrapper> parentBidList = relationMap.get(instance.getBid());
            if (CollectionUtils.isEmpty(parentBidList)) {
                parentBidList = new ArrayList<>();
                parentBidList.add(new TranscendRelationWrapper().setSourceBid(TREE_ROOT_FLAG));
            }
            //可能会有多对多的情况，所以需要数据扩散
            return parentBidList.stream().map(relation -> {
                MObjectTree objectTree = new MObjectTree();
                objectTree.putAll(instance);
                objectTree.setParentBid(relation.getSourceBid());
                objectTree.setRelInstanceBid(relation.getBid());
                return objectTree;
            });
        }).collect(Collectors.toList());

        return buildTree(treeList);
    }


    /**
     * 构建树结构
     *
     * @param flatList 平铺列表
     * @return 根节点列表
     */
    private List<MObjectTree> buildTree(List<MObjectTree> flatList) {
        if (flatList == null || flatList.isEmpty()) {
            return Collections.emptyList();
        }

        //通过父级节点进行分组
        Map<String, List<MObjectTree>> dataGroupMap = flatList.stream()
                .collect(Collectors.groupingBy(node -> Optional.ofNullable(node.getParentBid())
                        .filter(StringUtils::isNotBlank).orElse(TREE_ROOT_FLAG)));

        //获取根节点
        List<MObjectTree> rootList = dataGroupMap.get(TREE_ROOT_FLAG);
        if (CollUtil.isEmpty(rootList)) {
            return Collections.emptyList();
        }

        //填充子节点
        rootList.forEach(rootNode -> {
            rootNode.put("nodeParentId", TREE_ROOT_FLAG);
            putChildren(1, rootNode, dataGroupMap);
        });

        return rootList;
    }

    /**
     * 填充子节点
     *
     * @param parentNode   父节点
     * @param dataGroupMap 所需节点的元数据信息
     */
    private void putChildren(int depth, MObjectTree parentNode, Map<String, List<MObjectTree>> dataGroupMap) {
        String nodeParentId = IdUtil.objectId();
        parentNode.put("nodeId", nodeParentId);

        //防止树的层级太深，或者多层循环依赖
        int maxDepth = 100;
        if (depth >= maxDepth) {
            return;
        }

        String parentBid = parentNode.getBid();
        if (StringUtils.isBlank(parentBid)) {
            return;
        }

        List<MObjectTree> childrenList = dataGroupMap.get(parentBid);
        if (childrenList == null) {
            return;
        }
        List<MObjectTree> copyChildList = new ArrayList<>();
        //如果子节点已经被其他节点引用，就复制一个子节点
        for (MObjectTree mObjectTree : childrenList) {
            if (ObjectUtil.isNotEmpty(mObjectTree.get("nodeId"))) {
                MObjectTree copyData = new MObjectTree();
                copyData.putAll(mObjectTree);
                copyData.remove("nodeId");
                copyData.remove("nodeParentId");
                copyChildList.add(copyData);
            } else {
                copyChildList.add(mObjectTree);
            }
        }

        parentNode.setChildren(copyChildList);

        //递归填充子节点
        final int nextDepth = depth + 1;
        copyChildList.forEach(node -> {
            node.put("nodeParentId", nodeParentId);
            putChildren(nextDepth, node, dataGroupMap);
        });
    }


    /**
     * 获取模型专属查询条件
     *
     * @param queries 查询条件
     * @return 模型专属查询条件
     */
    public static Map<String, List<ModelFilterQo>> getModelExclusiveQueries(List<ModelFilterQo> queries) {
        if (CollectionUtils.isEmpty(queries)) {
            return Collections.emptyMap();
        }
        return queries.stream().filter(Objects::nonNull).filter(query -> StringUtils.isNotBlank(query.getModelCode()))
                .collect(Collectors.groupingBy(ModelFilterQo::getModelCode));
    }

    /**
     * 获取全局查询条件
     *
     * @param queries 查询条件
     * @return 全局查询条件
     */
    public static List<ModelFilterQo> getGlobalQueries(List<ModelFilterQo> queries) {
        if (CollectionUtils.isEmpty(queries)) {
            return Collections.emptyList();
        }
        return queries.stream().filter(Objects::nonNull).filter(query -> StringUtils.isBlank(query.getModelCode()))
                .collect(Collectors.toList());
    }

    /**
     * 获取模型专属查询条件
     *
     * @param queries 查询条件
     * @return 模型专属查询条件
     */
    public static List<String> getExclusiveQueriesModelCodes(List<ModelFilterQo> queries) {
        if (CollectionUtils.isEmpty(queries)) {
            return Collections.emptyList();
        }
        return queries.stream().map(ModelFilterQo::getModelCode).filter(StringUtils::isNotBlank)
                .distinct().collect(Collectors.toList());
    }


    /**
     * 补偿树bid
     *
     * @param treeConfig 树配置
     * @param dataMap    数据
     * @return List<MObjectTree> 需要上层补偿的数据
     */
    private List<MObjectTree> compensationTreeBid(MultiTreeConfigVo treeConfig, Map<String, List<MObjectTree>> dataMap) {

        //自下而上，先从最底层开始处理
        List<MObjectTree> processData = new ArrayList<>();
        if (treeConfig.getTargetModelCode() != null) {
            processData.addAll(compensationTreeBid(treeConfig.getTargetModelCode(), dataMap));
        }

        //准备获取上层的关系数据
        List<String> targetBidList = processData.stream().map(MObjectTree::getTreeBid)
                .filter(Objects::nonNull).flatMap(Collection::stream).filter(Objects::nonNull)
                .distinct().collect(Collectors.toList());
        if (!targetBidList.isEmpty()) {
            //查询关系数据
            QueryWrapper wrapper = new QueryWrapper().in(RelationEnum.TARGET_BID.getCode(), targetBidList)
                    .and().eq(BaseDataEnum.DELETE_FLAG.getColumn(), 0);
            Map<String, List<String>> relBidMap = objectModelCrudI.list(treeConfig.getRelationModelCode(), QueryWrapper.buildSqlQo(wrapper))
                    .stream().map(TranscendRelationWrapper::new)
                    .collect(Collectors.groupingBy(TranscendRelationWrapper::getTargetBid,
                            Collectors.mapping(TranscendRelationWrapper::getSourceBid, Collectors.toList())));
            //写入数据
            processData.forEach(item ->{
                Optional.ofNullable(item.getTreeBid()).map(treeBid ->
                        treeBid.stream().map(relBidMap::get).filter(Objects::nonNull)
                        .flatMap(Collection::stream).filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                                .ifPresent(item::setTreeBid);
            });
        }


        //将当前层的数据取出，赋值上当前层的bid给treeBid，并放入处理数据
        Optional.ofNullable(dataMap.get(treeConfig.getSourceModelCode())).ifPresent(list -> {
            list.forEach(item -> item.setTreeBid(ListUtil.toList(item.getBid())));
            processData.addAll(list);
        });

        return processData;
    }


    //endregion

}
