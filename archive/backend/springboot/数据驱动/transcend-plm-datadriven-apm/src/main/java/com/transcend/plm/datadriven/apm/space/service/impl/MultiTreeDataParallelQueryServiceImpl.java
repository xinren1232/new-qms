package com.transcend.plm.datadriven.apm.space.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.ttl.TtlWrappers;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.RelationObjectEnum;
import com.transcend.plm.datadriven.apm.space.pojo.vo.MultiTreeConfigVo;
import com.transcend.plm.datadriven.apm.space.service.MultiTreeDataQueryExecutor;
import com.transcend.plm.datadriven.apm.space.service.MultiTreeDataQueryService;
import com.transcend.plm.datadriven.apm.space.utils.MultiTreeUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 多对象树并行查询实现
 * 仅适用于无查询条件的情况下，并行加载所有数据
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/4/7 14:33
 */
@Service
@AllArgsConstructor
public class MultiTreeDataParallelQueryServiceImpl implements MultiTreeDataQueryService {
    /**
     * 不在筛选字段中的字段
     */
    private final String[] nonFilterFields = {"deleteFlag", "delete_flag"};

    private MultiTreeDataQueryExecutor executor;

    @Override
    public Data query(Params params) {
        MultiTreeConfigVo multiTreeConfig = params.getMultiAppTreeConfig();
        List<MultiTreeConfigVo> configList = MultiTreeUtils.getConfigFlatList(multiTreeConfig);

        //查询实例数据
        List<MultiTreeDataQueryExecutor.Params> instanceQueryParams = configList.stream()
                .filter(config -> StringUtils.isNotBlank(config.getSourceModelCode()))
                .map(config -> MultiTreeDataQueryExecutor.Params.ofInstance(config, params.getEmpNo(),
                        params.getSpaceBid(), params.getCheckPermission(), params.queryWrappers(config.getSourceModelCode())))
                .filter(Objects::nonNull).collect(Collectors.toList());

        Map<String, List<MObject>> instanceDataMap = multiModelParallelQuery(instanceQueryParams);


        //查询关系数据
        List<MultiTreeDataQueryExecutor.Params> relationQueryParams = configList.stream()
                .filter(config -> StringUtils.isNotBlank(config.getRelationModelCode()))
                .map(config -> {
                    //通过级联获取关系数据，防止拉取全部的关系数据
                    List<MObject> list = instanceDataMap.get(config.getSourceModelCode());
                    if (CollUtil.isEmpty(list)) {
                        return null;
                    }
                    List<String> sourceBidList = list.stream().map(MBaseData::getBid).collect(Collectors.toList());
                    QueryWrapper wrapper = new QueryWrapper().in(RelationObjectEnum.SOURCE_BID.getCode(), sourceBidList);

                    return MultiTreeDataQueryExecutor.Params.ofRelation(config, QueryWrapper.buildSqlQo(wrapper));
                }).filter(Objects::nonNull).collect(Collectors.toList());
        Map<String, List<MObject>> relationDataMap = multiModelParallelQuery(relationQueryParams);

        //组装数据结果
        Data data = new Data(multiTreeConfig);
        data.putDataAll(instanceDataMap);
        data.putDataAll(relationDataMap);
        return data;
    }

    @Override
    public boolean isSupport(Params params) {
        if (CollUtil.isEmpty(params.getQueryWrapperMap())) {
            return true;
        }
        return params.getQueryWrapperMap().values().stream().noneMatch(this::includeFilterFields);
    }


    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }


    /**
     * 多对象并行查询方法
     *
     * @param dataQueryParams 数据查询参数
     * @return 查询结果
     */
    @NotNull
    private Map<String, List<MObject>> multiModelParallelQuery(List<MultiTreeDataQueryExecutor.Params> dataQueryParams) {
        Function<MultiTreeDataQueryExecutor.Params, Map.Entry<String, List<MObject>>> queryFunction =
                TtlWrappers.wrapFunction(params -> new AbstractMap.SimpleEntry<>(params.getModelCode(), executor.execute(params)));
        return dataQueryParams.parallelStream().map(queryFunction)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> {
                    v1.addAll(v2);
                    return v1;
                }));
    }


    /**
     * 是否包含筛选字段
     *
     * @param wrappers 查询条件
     * @return 是否包含筛选字段
     */
    private boolean includeFilterFields(List<QueryWrapper> wrappers) {
        for (QueryWrapper wrapper : wrappers) {
            if (StringUtils.isNotBlank(wrapper.getProperty()) && !ArrayUtils.contains(nonFilterFields, wrapper.getProperty())) {
                return true;
            }
            if (includeFilterFields(wrapper.getModelQueries())) {
                return true;
            }
        }
        return false;
    }

}
