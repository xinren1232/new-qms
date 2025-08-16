package com.transcend.plm.datadriven.infrastructure.basedata.repository;

import com.github.pagehelper.PageInfo;
import com.github.pagehelper.page.PageMethod;
import com.google.common.collect.Lists;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.plm.configcenter.api.feign.CfgObjectRelationFeignClient;
import com.transcend.plm.configcenter.api.model.objectrelation.qo.ObjectPathChainQO;
import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationVo;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.config.TableAttributeDefinition;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import com.transcend.plm.datadriven.api.model.mata.RelationCrossUpApplicationQo;
import com.transcend.plm.datadriven.api.model.mata.RelationCrossUpQo;
import com.transcend.plm.datadriven.api.model.qo.ModelFilterQo;
import com.transcend.plm.datadriven.api.model.relation.qo.*;
import com.transcend.plm.datadriven.api.model.relation.vo.RelationAndTargetVo;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.tool.ObjectTools;
import com.transcend.plm.datadriven.common.tool.QueryConveterTool;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.common.util.ObjectCodeUtils;
import com.transcend.plm.datadriven.domain.support.external.table.CfgTableService;
import com.transcend.plm.datadriven.infrastructure.basedata.repository.mapper.RelationDataMapper;
import com.transsion.framework.common.BeanUtil;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.transcend.plm.datadriven.api.model.QueryFilterConditionEnum.EQ;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Repository
public class RelationDataRepository {

    @Resource
    private RelationDataMapper relationDataMapper;

    @Resource
    private CfgObjectRelationFeignClient cfgObjectRelationFeignClient;

    @Value("#{'${transcend.plm.apm.blob.attr.filter:richTextContent,demandDesc,testingRecommendations,text}'.split(',')}")
    private List<String> filterFields;

    /**
     * @param relationQo
     * @return {@link List }<{@link RelationAndTargetVo }>
     */
    public List<RelationAndTargetVo> listRelationDataFloat(RelationQo relationQo) {
        Assert.notNull(relationQo, "relationQo is null");
        Assert.hasText(relationQo.getSourceModelCode(), "modelCode is blank");
        TableDefinition targetTableInfo = ObjectTools.fillTableDefinition(relationQo.getTargetData().getModelCode());
        TableDefinition relationTableInfo = ObjectTools.fillTableDefinition(relationQo.getRelationData().getModelCode());
        relationQo.getRelationData().remove(ObjectEnum.MODEL_CODE.getCode());
        relationQo.getTargetData().remove(ObjectEnum.MODEL_CODE.getCode());
        //构造目标对象的查询条件
        QueryWrapper targetQueryWrapper = buildQueryWrapper(targetTableInfo,relationQo.getTargetData());
        //构造关系对象的查询条件
        QueryWrapper relationQueryWrapper = buildQueryWrapper(relationTableInfo,relationQo.getRelationData());
        return relationDataMapper.listRelationDataFloat(relationTableInfo, targetTableInfo, QueryWrapper.buildSqlQo(relationQueryWrapper), QueryWrapper.buildSqlQo(targetQueryWrapper));
    }

    /**
     * @param relationQo
     * @return {@link List }<{@link RelationAndTargetVo }>
     */
    public List<RelationAndTargetVo> listRelationDataFixed(RelationQo relationQo) {
        Assert.notNull(relationQo, "relationQo is null");
        Assert.hasText(relationQo.getSourceModelCode(), "modelCode is blank");
        TableDefinition targetTableInfo = ObjectTools.fillTableDefinition(relationQo.getTargetData().getModelCode());
        TableDefinition relationTableInfo = ObjectTools.fillTableDefinition(relationQo.getRelationData().getModelCode());
        //构造目标对象的查询条件
        QueryWrapper targetQueryWrapper = buildQueryWrapper(targetTableInfo,relationQo.getTargetData());
        //构造关系对象的查询条件
        QueryWrapper relationQueryWrapper = buildQueryWrapper(relationTableInfo,relationQo.getRelationData());
        return relationDataMapper.listRelationDataFixed(relationTableInfo, targetTableInfo, QueryWrapper.buildSqlQo(relationQueryWrapper), QueryWrapper.buildSqlQo(targetQueryWrapper));
    }

    /**
     * @param tableDefinition
     * @param baseData
     * @return {@link QueryWrapper }
     */
    private QueryWrapper buildQueryWrapper(TableDefinition tableDefinition, MBaseData baseData) {
        Map<String, String> propertyColumnMap = tableDefinition.getTableAttributeDefinitions().stream()
                .collect(Collectors.toMap(TableAttributeDefinition::getProperty, TableAttributeDefinition::getColumnName));
        QueryWrapper queryWrapper = new QueryWrapper();
        if (baseData != null) {
            baseData.entrySet().forEach(entry -> {
                queryWrapper.eq(propertyColumnMap.get(entry.getKey()), entry.getValue());
            });
        }
        return queryWrapper;
    }


    /**
     * 自下向上寻址的跨层级查询源对象的实例集合 强浮动以bid作为关联
     *
     * @param relationCrossUpApplicationQo
     * @param pageQo
     * @return
     */
    public PagedResult<MObject> pageCrossHierarchyUp(RelationCrossUpApplicationQo relationCrossUpApplicationQo,
                                                     BaseRequest<QueryCondition> pageQo) {
        PageMethod.startPage(pageQo.getCurrent(), pageQo.getSize());
        QueryCondition param = pageQo.getParam();
        // 防止空指针
        if (param == null) {
            param = new QueryCondition();
        }
        // 当前关系模型code, 用于从跨层级的关系链中找到当前使用的位置，如 (任务->需求)#(需求->迭代)#(迭代->项目)
        // 当前关系模型code为(需求->迭代)，则当前只需要寻(需求->迭代)#(迭代->项目)即可
        String currentRelationModelCode = relationCrossUpApplicationQo.getCurrentRelationModelCode();
        String currentSourceBid = relationCrossUpApplicationQo.getCurrentSourceBid();
        String crossRelationModelCodes = relationCrossUpApplicationQo.getCrossRelationModelCodes();

        // 目的地的源对象的表元数据
        TableDefinition sourceTable = ObjectTools.fillTableDefinition(relationCrossUpApplicationQo.getDestinationSourceModelCode());
        List<TableDefinition> relationTables = null;
        // 没有传递查询条件时，直接不需要查询关系数据
        if (StringUtil.isNotBlank(currentSourceBid) &&
                StringUtil.isNotBlank(currentRelationModelCode) &&
                StringUtil.isNotBlank(crossRelationModelCodes)) {
            //
            List<String> copyCrossRelationModelCodes = new ArrayList<>();
            // 移除包含匹配到当前关系模型code的关系模型code, 0未匹配，1匹配，2匹配之后
            boolean matchCrossRelationModelCode = false;
            // 跨层级关系对象code
            List<String> crossRelationModelCodeList = Arrays.asList(relationCrossUpApplicationQo.getCrossRelationModelCodes().split("#"));
            for (String crossRelationModelCode : crossRelationModelCodeList) {
                if (crossRelationModelCode.equals(currentRelationModelCode)) {
                    matchCrossRelationModelCode = true;
                    continue;
                }
                // 匹配过后需要收集
                if (matchCrossRelationModelCode) {
                    copyCrossRelationModelCodes.add(crossRelationModelCode);
                }
            }
            // 存在才需要收集到关系查询中
            if (CollectionUtils.isNotEmpty(copyCrossRelationModelCodes)) {
                relationTables = CfgTableService.getInstance().listTableDefinitionByModelCodes(copyCrossRelationModelCodes);
            }
        }
        RelationCrossUpQo relationCrossUpQo = RelationCrossUpQo.of()
                .setTargetBid(relationCrossUpApplicationQo.getCurrentSourceBid())
                .setSourceTable(sourceTable)
                .setRelationTables(relationTables);
        // 解决多此使用是，多线程，或者其他场景更改值之后，会影响其他地方使用
        QueryCondition queryConditionNew = QueryConveterTool.column2Property(relationCrossUpQo.getSourceTable(),
                param.getQueries(), param.getOrders());
        List<MObject> pageList = relationDataMapper.listPageCrossHierarchyUp(relationCrossUpQo, queryConditionNew.getQueries(), queryConditionNew.getOrders());
        PageInfo<MObject> pageInfo = new PageInfo<>(pageList);
        return PageResultTools.create(pageInfo);
    }

    /**
     * @param qo
     * @param pageQo
     * @return {@link PagedResult }<{@link MObject }>
     */
    public PagedResult<MObject> pageCrossRelationInstance(CrossRelationPathChainQO qo, BaseRequest<QueryCondition> pageQo) {
        PageMethod.startPage(pageQo.getCurrent(), pageQo.getSize());
        List<Order> orders = Optional.of(pageQo.getParam()).orElse(null).getOrders();
        List<MObject> pageList = getRelationPathInstanceList(qo.getSpaceBid(), qo.getCurrentSourceModelCode(), qo.getCurrentSourceBid(), qo.getPaths(), orders);
        pageList.forEach(e -> filterFields.forEach(e::remove));
        PageInfo<MObject> pageInfo = new PageInfo<>(pageList);
        return PageResultTools.create(pageInfo);
    }

    /**
     * @param spaceBid
     * @param currentSourceModelCode
     * @param currentSourceBid
     * @param paths
     * @return {@link List }<{@link MObject }>
     */
    public List<MObject> getRelationPathInstanceList(String spaceBid,
                                                    String currentSourceModelCode,
                                                    String currentSourceBid,
                                                    List<CrossRelationPathQO> paths,
                                                     List<Order> orders) {
        // 关系链上有子级对象需找到其父级对象，且在查询条件中增加子级对象
        for (CrossRelationPathQO path : paths) {
            String modelCode = path.getModelCode();
            List<ModelFilterQo> queries = path.getQueries();
            if (modelCode.length() > 3) {
                ModelFilterQo modelFilterQo = new ModelFilterQo();
                modelFilterQo.setProperty(RelationObjectEnum.MODEL_CODE.getCode());
                modelFilterQo.setCondition(EQ.getFilter());
                modelFilterQo.setValue(modelCode);
                queries.add(modelFilterQo);
                path.setModelCode(ObjectCodeUtils.splitModelCode(modelCode).iterator().next());
            }
        }

        List<String> modelCodes = paths.stream().map(CrossRelationPathQO::getModelCode).collect(Collectors.toList());
        // 当前源对象不在路径中无法定位目的地
        if (!modelCodes.contains(currentSourceModelCode)) {
            CrossRelationPathQO qo = new CrossRelationPathQO();
            qo.setModelCode(currentSourceModelCode);
            paths.add(0, qo);
        }

        // 目的地PathQO
        CrossRelationPathQO sourcePathQO = paths.get(paths.size() - 1);
        // 目的地对象表
        TableDefinition sourceTable = ObjectTools.fillTableDefinition(sourcePathQO.getModelCode());
        List<QueryWrapper> sourceQueries = QueryConveterTool.column2Property(sourceTable,
                QueryConveterTool.convert(sourcePathQO.getQueries()), null).getQueries();

        List<CrossRelationPathQueryQO> pathQueries = Lists.newArrayList();

        // 当前源对象如果为路径目的地，不用查询关系数据
        if (StringUtil.isNotBlank(currentSourceBid) && !currentSourceModelCode.equals(modelCodes.get(modelCodes.size() - 1))) {

            List<ObjectPathChainQO> pathChainQos = BeanUtil.copy(paths, ObjectPathChainQO.class);
            // key 两个层级之间 modelCode拼接
            Map<String, CfgObjectRelationVo> objectRelationMap =
                    cfgObjectRelationFeignClient.listCrossRelationByPathChain(pathChainQos).getCheckExceptionData();

            List<String> copyCrossRelationModelCodes = new ArrayList<>();
            Iterator<CrossRelationPathQO> pathIterator = paths.iterator();
            while (pathIterator.hasNext()) {
                CrossRelationPathQO path = pathIterator.next();
                if (currentSourceModelCode.equals(path.getModelCode())) {
                    break;
                }
                pathIterator.remove();
            }
            for (int i = 0; i < paths.size() - 1; i++) {
                String key = paths.get(i).getModelCode() + "->" + paths.get(i + 1).getModelCode();
                CfgObjectRelationVo relationVO = objectRelationMap.get(key);
                if (Objects.isNull(relationVO)) {
                    throw new PlmBizException("关系链中部分关系不存在！");
                }
                copyCrossRelationModelCodes.add(relationVO.getModelCode());
            }
            List<TableDefinition> instanceTables = CfgTableService.getInstance().listTableDefinitionByModelCodes(
                    paths.stream().map(CrossRelationPathQO::getModelCode).collect(Collectors.toList()));
            List<TableDefinition> relationTables = CfgTableService.getInstance().listTableDefinitionByModelCodes(copyCrossRelationModelCodes);

            for (int i = relationTables.size() - 1; i >= 0; i--) {
                CrossRelationPathQueryQO query = new CrossRelationPathQueryQO();
                TableDefinition instanceTable = instanceTables.get(i);
                List<ModelFilterQo> queries = paths.get(i).getQueries();
                query.setDirection("up".equals(paths.get(i + 1).getDirection()) ? "down" : "up");
                query.setRelationTable(relationTables.get(i));
                query.setInstanceTable(instanceTable);
                QueryCondition queryConditionNew = QueryConveterTool.column2Property(instanceTable,
                        QueryConveterTool.convert(queries), null);
                query.setQueries(queryConditionNew.getQueries());
                pathQueries.add(query);
            }
        }

        // 填充前一个关系方位，确认前实例是bid 是 source还是target
        for (int i = 1; i < pathQueries.size(); i++) {
            CrossRelationPathQueryQO queryQO = pathQueries.get(i);
            queryQO.setPreDirection(pathQueries.get(i - 1).getDirection());
        }

        CrossRelationPathChainQueryQO pathChainQueryQO = CrossRelationPathChainQueryQO.of()
                .setSpaceBid(spaceBid)
                .setSourceQueries(sourceQueries)
                .setSourceTable(sourceTable)
                .setDirection(CollectionUtils.isEmpty(pathQueries) ? null : pathQueries.get(pathQueries.size() - 1).getDirection())
                .setCurrentSourceBid(currentSourceBid)
                .setPathQueries(pathQueries);

        return relationDataMapper.relationListPageByPathChain(pathChainQueryQO, orders);
    }
}
