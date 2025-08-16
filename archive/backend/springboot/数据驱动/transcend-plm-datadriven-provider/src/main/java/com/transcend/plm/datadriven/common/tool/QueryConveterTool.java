package com.transcend.plm.datadriven.common.tool;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.config.TableAttributeDefinition;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import com.transcend.plm.datadriven.api.model.qo.ModelExpression;
import com.transcend.plm.datadriven.api.model.qo.ModelFilterQo;
import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.common.enums.ViewComponentEnum;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 前端Qo转换为后端Qo工具类
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/10/12 15:38
 * @since 1.0
 */
public class QueryConveterTool {

    /**
     * 默认按照 and拼接条件
     *
     * @param modelFilterQos 模型过滤条件
     * @return QueryWrapper
     */
    public static List<QueryWrapper> convert(List<ModelFilterQo> modelFilterQos) {
        ModelFilterQoTools.analysis(modelFilterQos);
        return convert(modelFilterQos, null);
    }

    /**
     * 包括排序
     *
     * @param modelMixQo
     * @param anyMatch   是否任意匹配（true=任意）
     * @return QueryWrapper
     */
    public static QueryCondition convert(ModelMixQo modelMixQo, Boolean anyMatch) {
        if (modelMixQo != null) {
            ModelFilterQoTools.analysis(modelMixQo.getQueries());
        }
        return QueryCondition.of()
                .setQueries(convert(modelMixQo.getQueries(), anyMatch))
                .setOrders(modelMixQo.getOrders());
    }

    /**
     * 包括分页排序
     *
     * @param pageQo 分页排序qo
     * @return QueryWrapper
     */
    public static BaseRequest<QueryCondition> convert(BaseRequest<ModelMixQo> pageQo) {
        ModelMixQo modelMixQo = pageQo.getParam();
        if (modelMixQo != null) {
            ModelFilterQoTools.analysis(modelMixQo.getQueries());
        }
        BaseRequest<QueryCondition> request = new BaseRequest<>();
        BeanUtil.copyProperties(pageQo, request, "param");
        request.setParam(QueryConveterTool.convert(modelMixQo));
        return request;
    }

    /**
     * 包括分页排序，跳过空值查询条件
     *
     * @param pageQo 分页排序qo
     * @return QueryWrapper
     */
    public static BaseRequest<QueryCondition> convertFitterNullValue(BaseRequest<ModelMixQo> pageQo) {
        ModelMixQo modelMixQo = pageQo.getParam();
        if (modelMixQo != null && modelMixQo.getQueries() != null) {
            List<ModelFilterQo> queries = modelMixQo.getQueries();
            List<ModelFilterQo> modelFilterQoUsed = new ArrayList<>();
            // 先过滤掉值为空的条件
            for (ModelFilterQo modelFilterQo : queries) {
                String property = modelFilterQo.getProperty();
                String condition = modelFilterQo.getCondition();
                Object value = modelFilterQo.getValue();
                if (!QueryFilterConditionEnum.IS_NOT_NULL.getFilter().equals(condition) &&
                        !QueryFilterConditionEnum.IS_NULL.getFilter().equals(condition)) {
                    if (value == null) {
                        continue;
                    }
                    if (StringUtils.isBlank(value.toString())) {
                        continue;
                    }
                    if (value instanceof List || value instanceof JSONArray) {
                        List<Object> values = JSON.parseArray(JSON.toJSONString(value), Object.class);
                        if (values.size() == 0) {
                            continue;
                        }
                    }
                    if (QueryFilterConditionEnum.IN.getFilter().equals(condition) ||
                            QueryFilterConditionEnum.NOT_IN.getFilter().equals(condition) ||
                            QueryFilterConditionEnum.NOT_BETWEEN.getFilter().equals(condition) ||
                            QueryFilterConditionEnum.BETWEEN.getFilter().equals(condition)) {
                        if (!"{LOGIN_USER}".equals(value)) {
                            List<Object> values = JSON.parseArray(JSON.toJSONString(value), Object.class);
                            if (values.size() == 0) {
                                continue;
                            }
                        }
                    }
                }
                modelFilterQoUsed.add(modelFilterQo);
            }
            modelMixQo.setQueries(modelFilterQoUsed);
        }
        return convert(pageQo);
    }

    /**
     * 包括排序
     *
     * @param modelMixQo
     * @return QueryWrapper
     */
    public static QueryCondition convert(ModelMixQo modelMixQo) {
        QueryCondition queryCondition = QueryCondition.of();
        // 不为空才设置值
        if (modelMixQo != null) {
            ModelFilterQoTools.analysis(modelMixQo.getQueries());
            queryCondition
                    .setQueries(convert(modelMixQo.getQueries(), modelMixQo.getAnyMatch()))
                    .setOrders(modelMixQo.getOrders())
                    .setPage(modelMixQo.getPageSize(), modelMixQo.getPageCurrent());
        }
        return queryCondition;
    }


    /**
     * 辅助1=1作为开始查询匹配
     *
     * @param modelFilterQos
     * @param anyMatch       是否任意匹配（true=任意）
     * @return QueryWrapper
     */
    public static List<QueryWrapper> convert(List<ModelFilterQo> modelFilterQos, Boolean anyMatch) {
        ModelFilterQoTools.analysis(modelFilterQos);
        return convert(modelFilterQos, anyMatch, Boolean.TRUE);
    }

    /**
     * ModelFilterQos转换为QueryWrapper
     *
     * @param modelFilterQos    模型过滤条件
     * @param anyMatch          是否任意匹配（true=任意）
     * @param defaultDeleteFlag 是否默认添加删除标识
     * @return QueryWrapper
     */
    public static List<QueryWrapper> convert(List<ModelFilterQo> modelFilterQos, Boolean anyMatch, Boolean defaultDeleteFlag) {
        QueryWrapper queryWrapper = new QueryWrapper();
        if (defaultDeleteFlag) {
            queryWrapper.eq(BaseDataEnum.DELETE_FLAG.getColumn(), 0);
        }
        return getQueryWrappers(queryWrapper, modelFilterQos, anyMatch);
    }

    /**
     * 指定开始条件
     *
     * @param modelFilterQos
     * @param anyMatch       是否任意匹配（true=任意）
     * @return QueryWrapper
     */
    public static List<QueryWrapper> convertOfFixedStart(QueryWrapper startQueryWrapper, List<ModelFilterQo> modelFilterQos, Boolean anyMatch) {
        return getQueryWrappers(startQueryWrapper, modelFilterQos, anyMatch);
    }

    /**
     * 构建查询条件
     *
     * @param startQueryWrapper
     * @param modelFilterQos
     * @param anyMatch
     * @return java.util.List<com.transcend.plm.datadriven.api.model.QueryWrapper>
     */
    @NotNull
    private static List<QueryWrapper> getQueryWrappers(QueryWrapper startQueryWrapper, List<ModelFilterQo> modelFilterQos, Boolean anyMatch) {
        if (CollUtil.isNotEmpty(modelFilterQos)) {
            List<ModelFilterQo> modelFilterQosProcessed = modelFilterQos.stream()
                    .filter(modelFilterQo -> StrUtil.isAllNotBlank(modelFilterQo.getProperty(), modelFilterQo.getCondition())).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(modelFilterQosProcessed)) {
                SpecialQueryBuild(modelFilterQosProcessed);
                if (CollUtil.isEmpty(startQueryWrapper.getModelQueries())) {
                    buildCondition(startQueryWrapper, modelFilterQosProcessed, anyMatch);
                } else {
                    startQueryWrapper.and(children -> buildCondition(children, modelFilterQosProcessed, anyMatch));
                }
            }
        }
        return QueryWrapper.buildSqlQo(startQueryWrapper);
    }

    private static void SpecialQueryBuild(List<ModelFilterQo> query) {
        query.forEach(e -> {
            if (!StringUtil.isBlank(e.getType())) {
                switch (e.getType()) {
                    case ViewComponentEnum.DATE_CONSTANT:
                        if (ViewComponentEnum.DATE.getType().equals(e.getType()) && (QueryFilterConditionEnum.GE.getFilter().equals(e.getCondition())
                                || QueryFilterConditionEnum.LT.getFilter().equals(e.getCondition()))) {
                            Assert.notBlank(String.valueOf(e.getValue()), "时间查询条件不能为空");
                            e.setValue(e.getValue() + " 00:00:00");
                        }
                        if (ViewComponentEnum.DATE.getType().equals(e.getType()) && (QueryFilterConditionEnum.LE.getFilter().equals(e.getCondition())
                                || QueryFilterConditionEnum.GT.getFilter().equals(e.getCondition()))) {
                            Assert.notBlank(String.valueOf(e.getValue()), "时间查询条件不能为空");
                            e.setValue(e.getValue() + " 23:59:59");
                        }
                        if (ViewComponentEnum.DATE.getType().equals(e.getType()) && QueryFilterConditionEnum.EQ.getFilter().equals(e.getCondition())) {
                            Assert.notBlank(String.valueOf(e.getValue()), "时间查询条件不能为空");
                            e.setCondition(QueryFilterConditionEnum.R_LIKE.getFilter());
                        }
                        break;
                    default:
                }
            }
        });
    }

    public static List<QueryWrapper> buildQueryWrapper(List<ModelExpression> modelExpressions) {
        List<QueryWrapper> queryWrappers = Lists.newArrayList();
        boolean first = true;
        //从modelConditionExpressions中获取查询条件
        for (ModelExpression modelExpression : modelExpressions) {
            //如果是嵌套条件
            if (Boolean.TRUE.equals(modelExpression.getNested())) {
                if (first) {
                    queryWrappers.add(new QueryWrapper());
                } else {
                    QueryWrapper relationQueryWrapper = new QueryWrapper(Boolean.TRUE);
                    relationQueryWrapper.setSqlRelation(modelExpression.getRelation());
                    queryWrappers.add(relationQueryWrapper);
                    queryWrappers.add(new QueryWrapper());
                }
                //递归调用
                queryWrappers.addAll(buildQueryWrapper(modelExpression.getExpressions()));
                QueryWrapper bracketWrapper = new QueryWrapper(Boolean.TRUE);
                bracketWrapper.setSqlRelation(")");
                queryWrappers.add(bracketWrapper);
            } else {
                if (!first) {
                    QueryWrapper relationQueryWrapper = new QueryWrapper(Boolean.TRUE);
                    relationQueryWrapper.setSqlRelation(modelExpression.getRelation());
                    queryWrappers.add(relationQueryWrapper);
                }
                QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
                //如果不是嵌套条件
                //获取属性名
                String property = modelExpression.getProperty();
                //获取条件
                String condition = modelExpression.getCondition();
                //获取属性值
                Object value = modelExpression.getValue();
                //如果属性名不为空
                if (StrUtil.isNotBlank(property)) {
                    //如果条件是等于
                    conditionConvert(condition, queryWrapper, property, value, null);
                }
                queryWrappers.addAll(queryWrapper.getModelQueries());
            }
            first = false;
        }
        return queryWrappers;
    }

    /**
     * 条件转换
     *
     * @param condition
     * @param queryWrapper
     * @param property
     * @param value
     * @param type         数据类型
     */
    private static void conditionConvert(String condition, QueryWrapper queryWrapper, String property, Object value, String type) {
        if (QueryFilterConditionEnum.EQ.getFilter().equals(condition)) {
            queryWrapper.eq(property, value, type);
        }
        //如果条件是模糊查询
        if (QueryFilterConditionEnum.LIKE.getFilter().equals(condition)) {
            queryWrapper.like(property, value);
        }
        if (QueryFilterConditionEnum.NOT_LIKE.getFilter().equals(condition)) {
            queryWrapper.notLike(property, value);
        }
        //如果条件是“右”模糊查询
        if (QueryFilterConditionEnum.R_LIKE.getFilter().equals(condition)) {
            queryWrapper.rLike(property, value);
        }
        //如果条件是“左”模糊查询
        if (QueryFilterConditionEnum.L_LIKE.getFilter().equals(condition)) {
            queryWrapper.lLike(property, value);
        }
        //如果条件是不等于
        if (QueryFilterConditionEnum.NE.getFilter().equals(condition)) {
            queryWrapper.ne(property, value);
        }
        //如果条件是大于
        if (QueryFilterConditionEnum.GT.getFilter().equals(condition)) {
            queryWrapper.gt(property, value);
        }
        //如果条件是大于等于
        if (QueryFilterConditionEnum.GE.getFilter().equals(condition)) {
            queryWrapper.ge(property, value);
        }
        //如果条件是小于
        if (QueryFilterConditionEnum.LT.getFilter().equals(condition)) {
            queryWrapper.lt(property, value);
        }
        //如果条件是小于等于
        if (QueryFilterConditionEnum.LE.getFilter().equals(condition)) {
            queryWrapper.le(property, value);
        }
        //如果条件是范围查询
        if (QueryFilterConditionEnum.IN.getFilter().equals(condition)) {
            queryWrapper.in(property, value, type);
        }
        //如果条件是范围查询
        if (QueryFilterConditionEnum.BETWEEN.getFilter().equals(condition)) {
            queryWrapper.between(property, value);
        }
        if (QueryFilterConditionEnum.NOT_BETWEEN.getFilter().equals(condition)) {
            queryWrapper.notBetween(property, value);
        }
        //如果条件是为空
        if (QueryFilterConditionEnum.IS_NULL.getFilter().equals(condition)) {
            queryWrapper.isNull(property, type);
        }
        //如果条件是不为空
        if (QueryFilterConditionEnum.IS_NOT_NULL.getFilter().equals(condition)) {
            queryWrapper.isNotNull(property, type);
        }
        //如果条件是不属于
        if (QueryFilterConditionEnum.NOT_IN.getFilter().equals(condition)) {
            queryWrapper.notIn(property, value, type);
        }
        //如果条件是SQL函数类型
        if (QueryFilterConditionEnum.SQL_CONDITION.getFilter().equals(condition)) {
            queryWrapper.relationAdd(String.valueOf(value));
        }
    }

    private static void buildCondition(QueryWrapper wrapper, List<ModelFilterQo> modelFilterQos, Boolean anyMatch) {
        for (int i = 0; i < modelFilterQos.size(); i++) {
            ModelFilterQo modelFilterQo = modelFilterQos.get(i);
            String property = modelFilterQo.getProperty();
            String condition = modelFilterQo.getCondition();
            Object value = modelFilterQo.getValue();
            String type = modelFilterQo.getType();
            List<Object> values = modelFilterQo.getValues();
            // 等于
            conditionConvert(condition, wrapper, property, value, type);
            if (i == modelFilterQos.size() - 1) {
                continue;
            }
            if (Boolean.TRUE.equals(anyMatch)) {
                wrapper.or();
            } else {
                wrapper.and();
            }
        }

    }

    public static List<QueryWrapper> appendDeleteFlagQueriesAndCondition(List<QueryWrapper> beforeQueries) {
        // 补充默认list
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(BaseDataEnum.DELETE_FLAG.getColumn(), 0);
        return appendMoreQueriesAndCondition(beforeQueries, QueryWrapper.buildSqlQo(queryWrapper));
    }

    public static List<QueryWrapper> appendModelCodeQueriesAndCondition(List<QueryWrapper> beforeQueries, String modelCode) {
        // 补充默认list
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.rLike(RelationEnum.MODEL_CODE.getColumn(), modelCode);
        return appendMoreQueriesAndCondition(beforeQueries, QueryWrapper.buildSqlQo(queryWrapper));
    }

    public static List<QueryWrapper> appendGeneralSearchQueriesAndCondition(List<QueryWrapper> beforeQueries, String gen) {
        // 补充默认list
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.like(ObjectEnum.NAME.getCode(), gen);
        queryWrapper.or().eq(ObjectEnum.CODING.getCode(), gen);
        return appendMoreQueriesAndCondition(beforeQueries, QueryWrapper.buildSqlQo(queryWrapper));
    }

    public static List<QueryWrapper> appendMoreQueriesAndCondition(List<QueryWrapper> beforeQueries, List<QueryWrapper> afterQueries) {
        // 补充默认list
        if (CollectionUtils.isEmpty(beforeQueries)) {
            return afterQueries;
        } else {
            // 追加 未删除条件
            QueryWrapper andWrapper = new QueryWrapper(Boolean.TRUE);
            andWrapper.setSqlRelation(" and ");
            beforeQueries.add(andWrapper);
            beforeQueries.addAll(afterQueries);
        }
        return beforeQueries;
    }


    public static QueryCondition column2Property(TableDefinition table, List<QueryWrapper> modelQueries, List<Order> orders) {
        Set<String> columnSet = new HashSet<>();
        Map<String, String> kvMap = getTableAttributeDefinitions(table).stream().peek(tableAttributeDefinition -> columnSet.add(tableAttributeDefinition.getColumnName()))
                .collect(Collectors.toMap(TableAttributeDefinition::getProperty, TableAttributeDefinition::getColumnName, (a, b) -> a));
        Map<String, String> kvTypeMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        getTableAttributeDefinitions(table).forEach(tableAttributeDefinition -> kvTypeMap.put(tableAttributeDefinition.getProperty(), tableAttributeDefinition.getType()));
        List<QueryWrapper> conventModelQueries = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(modelQueries)) {
            int jsonIndex = 0;
            // 深度复制
            for (int i = 0; i < modelQueries.size(); i++) {
                QueryWrapper data = modelQueries.get(i);
                // 匹配非relation,且column/property不存在的
                QueryWrapper conventData = new QueryWrapper(data.getRelation());
                conventData.setSqlRelation(data.getSqlRelation());
                if ("json".equals(kvTypeMap.get(data.getProperty()))) {
                    String condition = jsonTypeConditionSql(data, kvMap);
                    if (condition != null) {
                        condition = conventData.getSqlRelation() != null ?
                                String.format("%s %s", data.getSqlRelation(), condition) : condition;
                        conventData.setRelation(true);
                        conventData.setSqlRelation(condition);
                    }
                } else if (kvTypeMap.get(data.getProperty()) != null
                        && kvTypeMap.get(data.getProperty()).contains("varchar")
                        && data.getType() != null
                        && "select".equals(data.getType())) {
                    String condition = selectConditionSql(data, kvMap);
                    if (condition != null) {
                        condition = conventData.getSqlRelation() != null ?
                                String.format("%s %s", data.getSqlRelation(), condition) : condition;
                        conventData.setRelation(true);
                        conventData.setSqlRelation(condition);
                    }
                } else {
                    conventData.setProperty(data.getProperty());
                    conventData.setValue(data.getValue());
                    conventData.setValues(data.getValues());
                    conventData.setCondition(data.getCondition());
                    conventData.setIsList(data.getIsList());
                    conventData.setIsBetween(data.getIsBetween());
                }
                conventData.setIndex(data.getIndex());
                // 匹配属性与列
                if (!"json".equals(kvTypeMap.get(data.getProperty())) && !Boolean.TRUE.equals(data.getRelation()) && kvMap.containsKey(data.getProperty())) {
                    // 添加转换
                    conventData.setProperty(kvMap.get(data.getProperty()));
                }
                // 不匹配key，默认给1=0，因为其他条件匹配，如果不添加，会导致sql语句错误 如：and ()
                if (!Boolean.TRUE.equals(data.getRelation()) && (!kvMap.containsKey(data.getProperty()) && !columnSet.contains(data.getProperty()))) {
                    conventData.setProperty("1").setCondition("=").setValue(0).setIsList(false);
                }
                if (kvTypeMap.get(data.getProperty()) != null && kvTypeMap.get(data.getProperty()).contains("varchar")
                        && data.getType() != null && "select".equals(data.getType())) {
                    conventData.setProperty("1");
                }
                conventModelQueries.add(conventData);
            }
        }
        List<Order> conventOrders = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(orders)) {
            for (Order data : orders) {
                if (kvMap.containsKey(data.getProperty())) {
                    Order order = new Order();
                    order.setProperty(kvMap.get(data.getProperty()));
                    order.setDesc(data.getDesc());
                    conventOrders.add(order);
                }
            }
        }
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setQueries(conventModelQueries);
        queryCondition.setOrders(conventOrders);
        return queryCondition;
    }

    private static String selectConditionSql(QueryWrapper data, Map<String, String> propertyTypeMap) {

        List<String> valueList = new ArrayList<>();
        //json查询单独出来
        if (data.getValue() != null) {
            if (data.getValue() instanceof List) {
                valueList = JSON.parseArray(JSON.toJSONString(data.getValue()), String.class);
            } else if (data.getValue() instanceof String) {
                valueList.add(data.getValue().toString());
            }
        } else if (data.getValues() != null) {
            for (Object obj : data.getValues()) {
                valueList.add(obj.toString());
            }
        }
        if (CommonConstant.IN.equals(data.getCondition()) || "in".equals(data.getCondition())) {
            StringBuilder inCondition = new StringBuilder();
            StringBuilder jsonCondition = new StringBuilder();
            inCondition.append(propertyTypeMap.get(data.getProperty())).append(" in (");
            for (int j = 0; j < valueList.size(); j++) {
                String value = valueList.get(j);
                if (j == 0) {
                    jsonCondition.append(String.format("JSON_CONTAINS(%s,JSON_ARRAY('%s'))", propertyTypeMap.get(data.getProperty()), value));
                    inCondition.append("'").append(value).append("'");
                } else {
                    inCondition.append(",'").append(value).append("'");
                    jsonCondition.append(String.format("or JSON_CONTAINS(%s,JSON_ARRAY('%s'))", propertyTypeMap.get(data.getProperty()), value));
                }
            }
            inCondition.append(")");
            return String.format("((%s is not null and %s != '' and JSON_VALID(%s) = 1 and (%s)) or %s)",
                    propertyTypeMap.get(data.getProperty()),
                    propertyTypeMap.get(data.getProperty()),
                    propertyTypeMap.get(data.getProperty()),
                    jsonCondition,
                    inCondition);
        } else if ("=".equals(data.getCondition()) || "EQ".equals(data.getCondition())) {
            StringBuilder jsonCondition = new StringBuilder();
            StringBuilder eqCondition = new StringBuilder();
            /**
             * 	and JSON_CONTAINS(current_handler,'["18653434", "18620779"]','$')
             * 	AND JSON_LENGTH(current_handler) = JSON_LENGTH('["18653434", "18620779"]')
             * 	JSON_CONTAINS 判断是否完全包含，不关心长度和顺序
             * 	JSON_LENGTH 判断长度是否相同
             */
            String jsonString = JSON.toJSONString(valueList);
            if (valueList.size() == 1) {
                jsonCondition.append(String.format(" JSON_CONTAINS(%s,'%s','$') ", propertyTypeMap.get(data.getProperty()), jsonString));
                jsonCondition.append(String.format(" and JSON_LENGTH(%s) = JSON_LENGTH('%s') ", propertyTypeMap.get(data.getProperty()), jsonString));
                eqCondition.append(String.format(" %s ='%s' ", propertyTypeMap.get(data.getProperty()), valueList.get(0)));
            } else {
                jsonCondition.append(String.format(" JSON_CONTAINS(%s,'%s','$') ", propertyTypeMap.get(data.getProperty()), jsonString));
                jsonCondition.append(String.format(" and JSON_LENGTH(%s) = JSON_LENGTH('%s') ", propertyTypeMap.get(data.getProperty()), jsonString));
                eqCondition.append(String.format(" %s ='%s' ", propertyTypeMap.get(data.getProperty()), jsonString));
            }

            return String.format("((%s is not null and %s != '' and JSON_VALID(%s) = 1 and %s) or %s)", propertyTypeMap.get(data.getProperty()),
                    propertyTypeMap.get(data.getProperty()),
                    propertyTypeMap.get(data.getProperty()),
                    jsonCondition,
                    eqCondition);
        } else if (CommonConstant.NOT_IN.equals(data.getCondition()) || "not in".equals(data.getCondition())) {
            StringBuilder inCondition = new StringBuilder();
            StringBuilder jsonCondition = new StringBuilder();
            inCondition.append(propertyTypeMap.get(data.getProperty())).append(" not in (");
            for (int j = 0; j < valueList.size(); j++) {
                if (j == 0) {
                    jsonCondition.append(String.format("JSON_CONTAINS(%s, JSON_ARRAY('%s')) = 0", propertyTypeMap.get(data.getProperty()), valueList.get(j)));
                    inCondition.append("'").append(valueList.get(j)).append("'");
                } else {
                    inCondition.append(",'").append(valueList.get(j)).append("'");
                    jsonCondition.append(String.format(" and JSON_CONTAINS(%s, JSON_ARRAY('%s')) = 0", propertyTypeMap.get(data.getProperty()), valueList.get(j)));
                }
            }
            inCondition.append(")");
            return String.format("((%s is not null and %s != '' and JSON_VALID(%s) = 1 and %s) or (JSON_VALID(%s) = 0 and %s))",
                    propertyTypeMap.get(data.getProperty()),
                    propertyTypeMap.get(data.getProperty()),
                    propertyTypeMap.get(data.getProperty()),
                    jsonCondition,
                    propertyTypeMap.get(data.getProperty()),
                    inCondition);
        }
        return null;
    }

    private static String jsonTypeConditionSql(QueryWrapper data, Map<String, String> propertyTypeMap) {
        List<String> valueList = new ArrayList<>();
        //json查询单独出来
        if (data.getValue() != null) {
            if (data.getValue() instanceof List) {
                valueList = JSON.parseArray(JSON.toJSONString(data.getValue()), String.class);
            } else if (data.getValue() instanceof String) {
                valueList.add(data.getValue().toString());
            }
        } else if (data.getValues() != null) {
            for (Object obj : data.getValues()) {
                valueList.add(obj.toString());
            }
        }

        if (CommonConstant.IS_NULL.equals(data.getCondition())) {
            return "(JSON_LENGTH(" + propertyTypeMap.get(data.getProperty()) + ") = 0 or " + propertyTypeMap.get(data.getProperty()) + " is null or " + propertyTypeMap.get(data.getProperty()) + " = \"\")";
        } else if (CommonConstant.IS_NOT_NULL.equals(data.getCondition())) {
            return "(JSON_LENGTH(" + propertyTypeMap.get(data.getProperty()) + ") > 0 and " + propertyTypeMap.get(data.getProperty()) + " is not null and " + propertyTypeMap.get(data.getProperty()) + " <> \"\")";
        } else if (CollectionUtils.isNotEmpty(valueList)) {
            //拼接查询条件
            if (CommonConstant.IN.equals(data.getCondition()) || "in".equals(data.getCondition()) || "like".equals(data.getCondition()) || "LIKE".equals(data.getCondition())) {
                StringBuilder inCondition = new StringBuilder();
                StringBuilder jsonCondition = new StringBuilder();
                inCondition.append(propertyTypeMap.get(data.getProperty())).append(" in (");
                for (int j = 0; j < valueList.size(); j++) {
                    String value = valueList.get(j);
                    if (CommonConstant.LOWERCASE_LIKE.equals(data.getCondition()) || CommonConstant.LIKE.equals(data.getCondition())) {
                        value = value.replaceAll("%", "");
                    }
                    if (j == 0) {
                        jsonCondition.append("JSON_CONTAINS(").append(propertyTypeMap.get(data.getProperty())).append(", JSON_ARRAY('").append(value).append("'))");
                        inCondition.append("'").append(value).append("'");
                    } else {
                        inCondition.append(",'").append(value).append("'");
                        jsonCondition.append("or JSON_CONTAINS(").append(propertyTypeMap.get(data.getProperty())).append(", JSON_ARRAY('").append(value).append("'))");
                    }
                }
                inCondition.append(")");
                return "(" + jsonCondition + " or " + inCondition + ")";
            } else if ("=".equals(data.getCondition()) || "EQ".equals(data.getCondition())) {
                StringBuilder jsonCondition = new StringBuilder();
                /**
                 * 	and JSON_CONTAINS(current_handler,'["18653434", "18620779"]','$')
                 * 	AND JSON_LENGTH(current_handler) = JSON_LENGTH('["18653434", "18620779"]')
                 * 	JSON_CONTAINS 判断是否完全包含，不关心长度和顺序
                 * 	JSON_LENGTH 判断长度是否相同
                 */
                if (valueList.size() == 1) {
                    jsonCondition.append("(");
                    String jsonString = JSON.toJSONString(valueList);
                    jsonCondition.append(String.format(" JSON_CONTAINS(%s,'%s','$') ", propertyTypeMap.get(data.getProperty()), jsonString));
                    jsonCondition.append(String.format(" and JSON_LENGTH(%s) = JSON_LENGTH('%s') ", propertyTypeMap.get(data.getProperty()), jsonString));
                    jsonCondition.append(") or (");
                    jsonCondition.append(String.format(" %s ='%s' ", propertyTypeMap.get(data.getProperty()), valueList.get(0)));
                    jsonCondition.append(")");
                } else {
                    String jsonString = JSON.toJSONString(valueList);
                    jsonCondition.append(String.format(" JSON_CONTAINS(%s,'%s','$') ", propertyTypeMap.get(data.getProperty()), jsonString));
                    jsonCondition.append(String.format(" and JSON_LENGTH(%s) = JSON_LENGTH('%s') ", propertyTypeMap.get(data.getProperty()), jsonString));
                }
                return "(" + jsonCondition + ")";
            } else if ("<>".equals(data.getCondition()) || CommonConstant.NOT_IN.equals(data.getCondition()) || "not in".equals(data.getCondition()) || "NOT LIKE".equalsIgnoreCase(data.getCondition())) {
                StringBuilder inCondition = new StringBuilder();
                StringBuilder jsonCondition = new StringBuilder();
                inCondition.append(propertyTypeMap.get(data.getProperty())).append(" not in (");
                for (int j = 0; j < valueList.size(); j++) {
                    if (j == 0) {
                        jsonCondition.append(String.format("JSON_CONTAINS(%s, JSON_ARRAY('%s')) = 0", propertyTypeMap.get(data.getProperty()), valueList.get(j)));
                        inCondition.append("'").append(valueList.get(j)).append("'");
                    } else {
                        inCondition.append(",'").append(valueList.get(j)).append("'");
                        jsonCondition.append(String.format(" and JSON_CONTAINS(%s, JSON_ARRAY('%s')) = 0", propertyTypeMap.get(data.getProperty()), valueList.get(j)));
                    }
                }
                inCondition.append(")");
                return "(" + jsonCondition + " and " + inCondition + ")";
            }
        }
        return null;
    }

    public static String getSqlString(TableDefinition table, List<QueryWrapper> modelQueries) {
        Set<String> columnSet = new HashSet<>();

        Map<String, String> kvMap = getTableAttributeDefinitions(table).stream().peek(tableAttributeDefinition -> columnSet.add(tableAttributeDefinition.getColumnName()))
                .collect(Collectors.toMap(TableAttributeDefinition::getProperty, TableAttributeDefinition::getColumnName, (a, b) -> a));
        Map<String, String> kvTypeMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        table.getTableAttributeDefinitions().forEach(tableAttributeDefinition -> kvTypeMap.put(tableAttributeDefinition.getProperty(), tableAttributeDefinition.getType()));
        StringBuffer sqlString = new StringBuffer();
        for (QueryWrapper modelQuerie : modelQueries) {
            if ("json".equals(kvTypeMap.get(modelQuerie.getProperty()))) {
                List<String> valueList = new ArrayList<>();
                //json查询单独出来
                if (modelQuerie.getValue() != null) {
                    if (modelQuerie.getValue() instanceof List) {
                        valueList = JSON.parseArray(JSON.toJSONString(modelQuerie.getValue()), String.class);
                    } else if (modelQuerie.getValue() instanceof String) {
                        valueList.add(modelQuerie.getValue().toString());
                    }
                } else if (modelQuerie.getValues() != null) {
                    for (Object obj : modelQuerie.getValues()) {
                        valueList.add(obj.toString());
                    }
                }
                if (CommonConstant.IS_NULL.equals(modelQuerie.getCondition())) {
                    sqlString.append("(JSON_LENGTH(").append(kvMap.get(modelQuerie.getProperty())).append(") = 0 or " + kvMap.get(modelQuerie.getProperty()) + " is null or " + kvMap.get(modelQuerie.getProperty()) + " = \"\")");
                } else if (CommonConstant.IS_NOT_NULL.equals(modelQuerie.getCondition())) {
                    sqlString.append("(JSON_LENGTH(").append(kvMap.get(modelQuerie.getProperty())).append(") > 0 and " + kvMap.get(modelQuerie.getProperty()) + " is not null and " + kvMap.get(modelQuerie.getProperty()) + " <> \"\")");
                } else if (CollectionUtils.isNotEmpty(valueList)) {
                    //拼接查询条件
                    if (CommonConstant.IN.equals(modelQuerie.getCondition()) || "in".equals(modelQuerie.getCondition()) || "like".equals(modelQuerie.getCondition()) || "LIKE".equals(modelQuerie.getCondition())) {
                        StringBuffer inCondition = new StringBuffer();
                        StringBuffer jsonCondition = new StringBuffer();
                        inCondition.append(kvMap.get(modelQuerie.getProperty())).append(" in (");
                        for (int j = 0; j < valueList.size(); j++) {
                            String value = valueList.get(j);
                            if (CommonConstant.LOWERCASE_LIKE.equals(modelQuerie.getCondition()) || CommonConstant.LIKE.equals(modelQuerie.getCondition())) {
                                value = value.replaceAll("%", "");
                            }
                            if (j == 0) {
                                jsonCondition.append("JSON_CONTAINS(").append(kvMap.get(modelQuerie.getProperty())).append(", JSON_ARRAY('").append(value).append("'))");
                                inCondition.append("'").append(value).append("'");
                            } else {
                                inCondition.append(",'").append(value).append("'");
                                jsonCondition.append("or JSON_CONTAINS(").append(kvMap.get(modelQuerie.getProperty())).append(", JSON_ARRAY('").append(value).append("'))");
                            }
                        }
                        inCondition.append(")");
                        sqlString.append("(").append(jsonCondition).append(" or ").append(inCondition).append(")");
                    } else if ("=".equals(modelQuerie.getCondition()) || "EQ".equals(modelQuerie.getCondition())) {
                        StringBuffer jsonCondition = new StringBuffer();
                        /**
                         * 	and JSON_CONTAINS(current_handler,'["18653434", "18620779"]','$')
                         * 	AND JSON_LENGTH(current_handler) = JSON_LENGTH('["18653434", "18620779"]')
                         * 	JSON_CONTAINS 判断是否完全包含，不关心长度和顺序
                         * 	JSON_LENGTH 判断长度是否相同
                         */
                        if (valueList.size() == 1) {
                            jsonCondition.append("(");
                            String jsonString = JSON.toJSONString(valueList);
                            jsonCondition.append(String.format(" JSON_CONTAINS(%s,'%s','$') ", kvMap.get(modelQuerie.getProperty()), jsonString));
                            jsonCondition.append(String.format(" and JSON_LENGTH(%s) = JSON_LENGTH('%s') ", kvMap.get(modelQuerie.getProperty()), jsonString));
                            jsonCondition.append(") or (");
                            jsonCondition.append(String.format(" %s ='%s' ", kvMap.get(modelQuerie.getProperty()), valueList.get(0)));
                            jsonCondition.append(")");
                        } else {
                            String jsonString = JSON.toJSONString(valueList);
                            jsonCondition.append(String.format(" JSON_CONTAINS(%s,'%s','$') ", kvMap.get(modelQuerie.getProperty()), jsonString));
                            jsonCondition.append(String.format(" and JSON_LENGTH(%s) = JSON_LENGTH('%s') ", kvMap.get(modelQuerie.getProperty()), jsonString));
                        }
                        sqlString.append("(").append(jsonCondition).append(")");
                    } else if (CommonConstant.NOT_IN.equals(modelQuerie.getCondition()) || "not in".equals(modelQuerie.getCondition())) {
                        StringBuffer inCondition = new StringBuffer();
                        StringBuffer jsonCondition = new StringBuffer();
                        inCondition.append(kvMap.get(modelQuerie.getProperty())).append(" not in (");
                        for (int j = 0; j < valueList.size(); j++) {
                            if (j == 0) {
                                jsonCondition.append(String.format("JSON_CONTAINS(%s, JSON_ARRAY('%s')) = 0", kvMap.get(modelQuerie.getProperty()), valueList.get(j)));
                                inCondition.append("'").append(valueList.get(j)).append("'");
                            } else {
                                inCondition.append(",'").append(valueList.get(j)).append("'");
                                jsonCondition.append(String.format(" and JSON_CONTAINS(%s, JSON_ARRAY('%s')) = 0", kvMap.get(modelQuerie.getProperty()), valueList.get(j)));
                            }
                        }
                        inCondition.append(")");
                        sqlString.append("(").append(jsonCondition).append(" and ").append(inCondition).append(")");
                    }
                }
            } else if (kvTypeMap.get(modelQuerie.getProperty()) != null
                    && kvTypeMap.get(modelQuerie.getProperty()).contains("varchar")
                    && modelQuerie.getType() != null
                    && "select".equals(modelQuerie.getType())) {
                List<String> valueList = new ArrayList<>();
                //json查询单独出来
                if (modelQuerie.getValue() != null) {
                    if (modelQuerie.getValue() instanceof List) {
                        valueList = JSON.parseArray(JSON.toJSONString(modelQuerie.getValue()), String.class);
                    } else if (modelQuerie.getValue() instanceof String) {
                        valueList.add(modelQuerie.getValue().toString());
                    }
                } else if (modelQuerie.getValues() != null) {
                    for (Object obj : modelQuerie.getValues()) {
                        valueList.add(obj.toString());
                    }
                }
                if (CommonConstant.IN.equals(modelQuerie.getCondition()) || "in".equals(modelQuerie.getCondition())) {
                    StringBuffer inCondition = new StringBuffer();
                    StringBuffer jsonCondition = new StringBuffer();
                    inCondition.append(kvMap.get(modelQuerie.getProperty())).append(" in (");
                    for (int j = 0; j < valueList.size(); j++) {
                        String value = valueList.get(j);
                        if (j == 0) {
                            jsonCondition.append(String.format("JSON_CONTAINS(%s,JSON_ARRAY('%s'))", kvMap.get(modelQuerie.getProperty()), value));
                            inCondition.append("'").append(value).append("'");
                        } else {
                            inCondition.append(",'").append(value).append("'");
                            jsonCondition.append(String.format("or JSON_CONTAINS(%s,JSON_ARRAY('%s'))", kvMap.get(modelQuerie.getProperty()), value));
                        }
                    }
                    inCondition.append(")");
                    sqlString.append(String.format("((%s is not null and %s != '' and JSON_VALID(%s) = 1 and (%s)) or %s)",
                            kvMap.get(modelQuerie.getProperty()),
                            kvMap.get(modelQuerie.getProperty()),
                            kvMap.get(modelQuerie.getProperty()),
                            jsonCondition,
                            inCondition));
                } else if ("=".equals(modelQuerie.getCondition()) || "EQ".equals(modelQuerie.getCondition())) {
                    StringBuffer jsonCondition = new StringBuffer();
                    StringBuffer eqCondition = new StringBuffer();
                    /**
                     * 	and JSON_CONTAINS(current_handler,'["18653434", "18620779"]','$')
                     * 	AND JSON_LENGTH(current_handler) = JSON_LENGTH('["18653434", "18620779"]')
                     * 	JSON_CONTAINS 判断是否完全包含，不关心长度和顺序
                     * 	JSON_LENGTH 判断长度是否相同
                     */
                    String jsonString = JSON.toJSONString(valueList);
                    if (valueList.size() == 1) {
                        jsonCondition.append(String.format(" JSON_CONTAINS(%s,'%s','$') ", kvMap.get(modelQuerie.getProperty()), jsonString));
                        jsonCondition.append(String.format(" and JSON_LENGTH(%s) = JSON_LENGTH('%s') ", kvMap.get(modelQuerie.getProperty()), jsonString));
                        eqCondition.append(String.format(" %s ='%s' ", kvMap.get(modelQuerie.getProperty()), valueList.get(0)));
                    } else {
                        jsonCondition.append(String.format(" JSON_CONTAINS(%s,'%s','$') ", kvMap.get(modelQuerie.getProperty()), jsonString));
                        jsonCondition.append(String.format(" and JSON_LENGTH(%s) = JSON_LENGTH('%s') ", kvMap.get(modelQuerie.getProperty()), jsonString));
                        eqCondition.append(String.format(" %s ='%s' ", kvMap.get(modelQuerie.getProperty()), jsonString));
                    }
                    sqlString.append(String.format("((%s is not null and %s != '' and JSON_VALID(%s) = 1 and %s) or %s)", kvMap.get(modelQuerie.getProperty()),
                            kvMap.get(modelQuerie.getProperty()),
                            kvMap.get(modelQuerie.getProperty()),
                            jsonCondition,
                            eqCondition));
                } else if (CommonConstant.NOT_IN.equals(modelQuerie.getCondition()) || "not in".equals(modelQuerie.getCondition())) {
                    StringBuffer inCondition = new StringBuffer();
                    StringBuffer jsonCondition = new StringBuffer();
                    inCondition.append(kvMap.get(modelQuerie.getProperty())).append(" not in (");
                    for (int j = 0; j < valueList.size(); j++) {
                        if (j == 0) {
                            jsonCondition.append(String.format("JSON_CONTAINS(%s, JSON_ARRAY('%s')) = 0", kvMap.get(modelQuerie.getProperty()), valueList.get(j)));
                            inCondition.append("'").append(valueList.get(j)).append("'");
                        } else {
                            inCondition.append(",'").append(valueList.get(j)).append("'");
                            jsonCondition.append(String.format(" and JSON_CONTAINS(%s, JSON_ARRAY('%s')) = 0", kvMap.get(modelQuerie.getProperty()), valueList.get(j)));
                        }
                    }
                    inCondition.append(")");
                    sqlString.append(String.format("((%s is not null and %s != '' and JSON_VALID(%s) = 1 and %s) or (JSON_VALID(%s) = 0 and %s))",
                            kvMap.get(modelQuerie.getProperty()),
                            kvMap.get(modelQuerie.getProperty()),
                            kvMap.get(modelQuerie.getProperty()),
                            jsonCondition,
                            kvMap.get(modelQuerie.getProperty()),
                            inCondition));
                }
            } else if (kvMap.containsKey(modelQuerie.getProperty())) {
                sqlString.append(kvMap.get(modelQuerie.getProperty()));
                sqlString.append(" ").append(modelQuerie.getCondition()).append(" ");
                if (!CommonConstant.IS_NOT_NULL.equals(modelQuerie.getCondition()) && !CommonConstant.IS_NULL.equals(modelQuerie.getCondition())) {
                    if (Boolean.TRUE.equals(modelQuerie.getIsList())) {
                        sqlString.append(modelQuerie.getValues().stream().map(String::valueOf).collect(Collectors.joining("','", "('", "')")));
                    } else if (Boolean.TRUE.equals(modelQuerie.getIsBetween())) {
                        sqlString.append(modelQuerie.getValues().stream().map(Object::toString).collect(Collectors.joining(" and ")));
                    } else {
                        sqlString.append("'").append(modelQuerie.getValue()).append("'");
                    }
                }
            } else if (Boolean.TRUE.equals(modelQuerie.getRelation())) {
                sqlString.append(" ").append(modelQuerie.getSqlRelation()).append(" ");
            } else {
                sqlString.append(modelQuerie.getProperty());
                sqlString.append(" ").append(modelQuerie.getCondition()).append(" ");
                if (Boolean.TRUE.equals(modelQuerie.getIsList()) && CollectionUtils.isNotEmpty(modelQuerie.getValues())) {
                    sqlString.append(modelQuerie.getValues().stream().map(String::valueOf).collect(Collectors.joining(",", "(", ")")));
                } else if (Boolean.TRUE.equals(modelQuerie.getIsBetween()) && CollectionUtils.isNotEmpty(modelQuerie.getValues())) {
                    sqlString.append(modelQuerie.getValues().stream().map(Object::toString).collect(Collectors.joining(" and ")));
                } else if (ObjectUtil.isNotEmpty(modelQuerie.getValue())) {
                    sqlString.append("'").append(modelQuerie.getValue()).append("'");
                }
            }
        }
        return sqlString.toString();
    }

    private static List<TableAttributeDefinition> getTableAttributeDefinitions(TableDefinition table) {
        return table.getFullTableAttributeDefinitions();
    }

    /**
     * 属性转换为列
     *
     * @param table    表
     * @param property 属性
     * @return 列
     */
    public static String property2Column(TableDefinition table, String property) {
        Map<String, String> kvMap = getTableAttributeDefinitions(table).stream()
                .collect(Collectors.toMap(TableAttributeDefinition::getProperty, TableAttributeDefinition::getColumnName));
        return kvMap.get(property);
    }

    /**
     * 分组转换为查询条件
     *
     * @param modelMixQo 模型查询对象
     * @param wrappers   查询对象集合
     * @return 查询对象集合
     */
    public static List<QueryWrapper> addGroupCondition(ModelMixQo modelMixQo, List<QueryWrapper> wrappers) {
        if (modelMixQo == null) {
            return wrappers;
        }
        String groupProperty = modelMixQo.getGroupProperty();
        String groupPropertyValue = modelMixQo.getGroupPropertyValue();
        if (StringUtils.isBlank(groupProperty) || StringUtils.isBlank(groupPropertyValue)) {
            return wrappers;
        }

        if (wrappers == null) {
            wrappers = new ArrayList<>();
        }
        if (!wrappers.isEmpty()) {
            wrappers.add(new QueryWrapper(Boolean.TRUE).setSqlRelation(" and "));
        }

        QueryWrapper wrapper = new QueryWrapper(Boolean.FALSE);
        wrapper.setProperty(groupProperty);
        wrapper.setCondition("like");
        wrapper.setValue(groupPropertyValue);
        wrappers.add(wrapper);
        return wrappers;
    }
}
