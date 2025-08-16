package com.transcend.plm.datadriven.api.model;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * ModelQueryWrapper
 *
 * @author leigang.yang
 * @date 2022/10/12 09:40
 */

@Getter
@Setter
@ToString
@Accessors(chain = true)
@Slf4j
public class QueryWrapper extends ModelQuery {

    /**
     * 是否为符号，和sqlRelation配合使用
     */
    private Boolean relation;

    /**
     * 直接放到sql 不进行转化
     */
    private String sqlRelation;

    private List<QueryWrapper> modelQueries;

    /**
     *
     */
    public QueryWrapper() {
        relation = Boolean.TRUE;
        sqlRelation = "(";
        modelQueries = new LinkedList<>();
    }

    /**
     * @param ih
     */
    public QueryWrapper(Boolean ih) {
        relation = ih;
        modelQueries = new LinkedList<>();
    }


    /**
     * 条件 = 的使用
     *
     * @param property
     * @param value
     * @return
     */
    public QueryWrapper eq(String property, Object value) {
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty(property);
        queryWrapper.setCondition("=");
        queryWrapper.setValue(value);
        this.modelQueries.add(queryWrapper);
        return this;
    }

    public QueryWrapper eq(String property, Object value, String type) {
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty(property);
        queryWrapper.setCondition("=");
        queryWrapper.setValue(value);
        queryWrapper.setType(type);
        this.modelQueries.add(queryWrapper);
        return this;
    }

    /**
     * 条件 <></> 的使用
     *
     * @param property
     * @param value
     * @return
     */
    public QueryWrapper ne(String property, Object value) {
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty(property);
        queryWrapper.setCondition("<>");
        queryWrapper.setValue(value);
        this.modelQueries.add(queryWrapper);
        return this;
    }

    /**
     * 条件 in 的使用
     *
     * @param property
     * @param value
     * @return
     */
    public QueryWrapper in(String property, Object value) {
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty(property);
        queryWrapper.setIsList(Boolean.TRUE);
        queryWrapper.setCondition("IN");
        List<Object> values = new ArrayList<>();
        if (value instanceof Collection) {
            values = JSON.parseArray(JSON.toJSONString(value), Object.class);
        } else if (value instanceof String) {
            values.add(value);
        }
        // 没有值，才需要添加空串
        if (CollectionUtils.isEmpty(values)) {
            values = Lists.newArrayList("");
        }
        queryWrapper.setValues(values);
        this.modelQueries.add(queryWrapper);
        return this;
    }

    public QueryWrapper in(String property, Object value, String type) {
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty(property);
        queryWrapper.setIsList(Boolean.TRUE);
        queryWrapper.setCondition("IN");
        List<Object> values = new ArrayList<>();
        if (value instanceof Collection) {
            values = JSON.parseArray(JSON.toJSONString(value), Object.class);
        } else if (value instanceof String) {
            values.add(value);
        }
        // 没有值，才需要添加空串
        if (CollectionUtils.isEmpty(values)) {
            values = Lists.newArrayList("");
        }
        queryWrapper.setType(type);
        queryWrapper.setValues(values);
        this.modelQueries.add(queryWrapper);
        return this;
    }


    /**
     * 条件 between 的使用
     *
     * @param property
     * @param value
     * @return
     */
    public QueryWrapper between(String property, Object value) {
        List<Object> values = JSON.parseArray(JSON.toJSONString(value), Object.class);
        Object o1 = values.get(0);
        Object o2 = values.get(1);
        if (o1 != null && o2 != null) {
            QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
            queryWrapper.setProperty(property);
            queryWrapper.setIsList(Boolean.FALSE);
            queryWrapper.setIsBetween(Boolean.TRUE);
            queryWrapper.setCondition("BETWEEN");
            queryWrapper.setValue(o1 + " AND " + o2);
            queryWrapper.setValues(values);
            this.modelQueries.add(queryWrapper);
        } else if (o1 != null && o2 == null) {
            ge(property, o1);
        } else if (o1 == null && o2 != null) {
            le(property, o2);
        }
        return this;
    }

    /**
     * 条件 not between 的使用
     *
     * @param property
     * @param value
     * @return
     */
    public QueryWrapper notBetween(String property, Object value) {
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty(property);
        queryWrapper.setIsList(Boolean.FALSE);
        queryWrapper.setIsBetween(Boolean.TRUE);
        queryWrapper.setCondition("NOT BETWEEN");
        List<Object> values = JSON.parseArray(JSON.toJSONString(value), Object.class);
        if (values.size() != 2) {
            throw new RuntimeException("NOT BETWEEN 条件的值必须是两个");
        }
        Object o1 = values.get(0);
        Object o2 = values.get(1);
        if (o1 == null || o2 == null) {
            throw new RuntimeException("NOT BETWEEN 条件不能为空");
        }
        queryWrapper.setValue(o1 + " AND " + o2);
        queryWrapper.setValues(values);
        this.modelQueries.add(queryWrapper);
        return this;
    }

    /**
     * 模糊查询 like
     *
     * @param property
     * @param value
     * @return
     */
    public QueryWrapper like(String property, Object value) {
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty(property);
        queryWrapper.setCondition("like");
        if (value instanceof List) {
            queryWrapper.setValue(value);
        } else {
            queryWrapper.setValue("%" + value + "%");
        }
        this.modelQueries.add(queryWrapper);
        return this;
    }

    /**
     * 模糊排除查询 not like
     *
     * @param property 参数
     * @param value    匹配值
     * @return QueryWrapper
     */
    public QueryWrapper notLike(String property, Object value) {
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty(property);
        queryWrapper.setCondition("not like");
        if (value instanceof List) {
            queryWrapper.setValue(value);
        } else {
            queryWrapper.setValue("%" + value + "%");
        }
        this.modelQueries.add(queryWrapper);
        return this;
    }

    /**
     * 右模糊查询
     *
     * @param property
     * @param value
     * @return
     */
    public QueryWrapper rLike(String property, Object value) {
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty(property);
        queryWrapper.setCondition("like");
        queryWrapper.setValue(value + "%");
        this.modelQueries.add(queryWrapper);
        return this;
    }

    /**
     * 左模糊查询
     *
     * @param property
     * @param value
     * @return
     */
    public QueryWrapper lLike(String property, Object value) {
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty(property);
        queryWrapper.setCondition("like");
        queryWrapper.setValue("%" + value);
        this.modelQueries.add(queryWrapper);
        return this;
    }

    /**
     * 条件 > 的使用
     *
     * @param property
     * @param value
     * @return
     */
    public QueryWrapper gt(String property, Object value) {
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty(property);
        queryWrapper.setCondition(">");
        queryWrapper.setValue(value);
        this.modelQueries.add(queryWrapper);
        return this;
    }

    /**
     * 条件 >= 的使用
     *
     * @param property
     * @param value
     * @return
     */
    public QueryWrapper ge(String property, Object value) {
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty(property);
        queryWrapper.setCondition(">=");
        queryWrapper.setValue(value);
        this.modelQueries.add(queryWrapper);
        return this;
    }

    /**
     * 条件 <= 的使用
     *
     * @param property
     * @param value
     * @return
     */
    public QueryWrapper le(String property, Object value) {
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty(property);
        queryWrapper.setCondition("<=");
        queryWrapper.setValue(value);
        this.modelQueries.add(queryWrapper);
        return this;
    }

    /**
     * @param property
     * @param value
     * @return {@link QueryWrapper }
     */
    public QueryWrapper lt(String property, Object value) {
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty(property);
        queryWrapper.setCondition("<");
        queryWrapper.setValue(value);
        this.modelQueries.add(queryWrapper);
        return this;
    }

    /**
     * 条件 != 的使用
     *
     * @param property
     * @return
     */
    public QueryWrapper isNotNull(String property) {
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty(property);
        queryWrapper.setCondition("IS NOT NULL");
        queryWrapper.setValue(null);

        this.modelQueries.add(queryWrapper);
        return this;
    }

    /**
     * 条件 != 的使用
     *
     * @param property
     * @param type     字段类型
     * @return
     */
    public QueryWrapper isNotNull(String property, String type) {
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty(property);
        queryWrapper.setCondition("IS NOT NULL");
        queryWrapper.setValue(null);

        QueryWrapper nullWrapper = new QueryWrapper(Boolean.FALSE);
        nullWrapper.setProperty(property);
        nullWrapper.setCondition("<>");
        nullWrapper.setValue("");

        QueryWrapper emptyWrapper = new QueryWrapper(Boolean.FALSE);
        emptyWrapper.setProperty(property);
        emptyWrapper.setCondition("<>");
        emptyWrapper.setValue("[]");
        if (type != null && type.equals("date")) {
            this.modelQueries.add(queryWrapper);
        } else {
            this.modelQueries.add(queryWrapper);
            and();
            this.modelQueries.add(nullWrapper);
            and();
            this.modelQueries.add(emptyWrapper);
        }
        return this;
    }

    /**
     * @param property
     * @param value
     * @return {@link QueryWrapper }
     */
    public QueryWrapper notIn(String property, Object value) {
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty(property);
        queryWrapper.setIsList(Boolean.TRUE);
        queryWrapper.setCondition("NOT IN");
        List<Object> values = new ArrayList<>();
        if (value instanceof List) {
            values = JSON.parseArray(JSON.toJSONString(value), Object.class);
        } else if (value instanceof String) {
            values.add(value);
        }
        // not in(null)会导致查询不到数据
        // null值替换为空字符串
        for (int i = 0; i < values.size(); i++) {
            Object o = values.get(i);
            if (o == null) {
                values.set(i, "");
            }
        }
        // 没有值，才需要添加空串
        if (CollectionUtils.isEmpty(values)) {
            values = Lists.newArrayList("");
        }
        queryWrapper.setValues(values);
        this.modelQueries.add(queryWrapper);
        return this;
    }

    public QueryWrapper notIn(String property, Object value, String type) {
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty(property);
        queryWrapper.setIsList(Boolean.TRUE);
        queryWrapper.setCondition("NOT IN");
        List<Object> values = new ArrayList<>();
        if (value instanceof List) {
            values = JSON.parseArray(JSON.toJSONString(value), Object.class);
        } else if (value instanceof String) {
            values.add(value);
        }
        // not in(null)会导致查询不到数据
        // null值替换为空字符串
        for (int i = 0; i < values.size(); i++) {
            Object o = values.get(i);
            if (o == null) {
                values.set(i, "");
            }
        }
        // 没有值，才需要添加空串
        if (CollectionUtils.isEmpty(values)) {
            values = Lists.newArrayList("");
        }
        queryWrapper.setValues(values);
        queryWrapper.setType(type);
        this.modelQueries.add(queryWrapper);
        return this;
    }

    public QueryWrapper isNull(String property) {
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty(property);
        queryWrapper.setCondition("IS NULL");
        queryWrapper.setValue(null);
        this.modelQueries.add(queryWrapper);
        return this;
    }

    /**
     * is null
     *
     * @param property
     * @param type     字段类型
     * @return
     */
    public QueryWrapper isNull(String property, String type) {
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
        queryWrapper.setProperty(property);
        queryWrapper.setCondition("IS NULL");
        queryWrapper.setValue(null);

        QueryWrapper nullWrapper = new QueryWrapper(Boolean.FALSE);
        nullWrapper.setProperty(property);
        nullWrapper.setCondition("=");
        nullWrapper.setValue("");

        QueryWrapper emptyWrapper = new QueryWrapper(Boolean.FALSE);
        emptyWrapper.setProperty(property);
        emptyWrapper.setCondition("=");
        emptyWrapper.setValue("[]");
        if (type != null && type.equals("date")) {
            this.modelQueries.add(queryWrapper);
        } else if (type != null && type.equals("instance-select")) {
            this.modelQueries.add(queryWrapper);
        } else {
            QueryWrapper startWrapper = new QueryWrapper(Boolean.TRUE);
            startWrapper.setSqlRelation("(");
            this.modelQueries.add(startWrapper);
            this.modelQueries.add(queryWrapper);
            simpleOr();
            this.modelQueries.add(nullWrapper);
            simpleOr();
            this.modelQueries.add(emptyWrapper);
            QueryWrapper endWrapper = new QueryWrapper(Boolean.TRUE);
            endWrapper.setSqlRelation(")");
            this.modelQueries.add(endWrapper);
        }
        return this;
    }

    /**
     * 声明接下来的条件用and关联
     *
     * @return
     */
    public QueryWrapper and() {
        return relationAdd("and");
    }

    public QueryWrapper and(QueryWrapper queryWrapper, boolean first) {
        if (first) {
            QueryWrapper relationWrapper = new QueryWrapper(Boolean.TRUE);
            relationWrapper.setSqlRelation("and");
            this.addQueryWrapper(relationWrapper);
        }
        QueryWrapper bracketWrapper = new QueryWrapper(Boolean.TRUE);
        bracketWrapper.setSqlRelation("(");
        this.addQueryWrapper(bracketWrapper);
        bracketWrapper.addQueryWrapper(queryWrapper);
        return bracketWrapper;
    }

    public QueryWrapper or(QueryWrapper queryWrapper, boolean first) {
        if (first) {
            QueryWrapper relationWrapper = new QueryWrapper(Boolean.TRUE);
            relationWrapper.setSqlRelation("or");
            this.addQueryWrapper(relationWrapper);
        }
        QueryWrapper bracketWrapper = new QueryWrapper(Boolean.TRUE);
        bracketWrapper.setSqlRelation("(");
        this.addQueryWrapper(bracketWrapper);
        bracketWrapper.addQueryWrapper(queryWrapper);
        return bracketWrapper;
    }

    /**
     * @return {@link QueryWrapper }
     */
    public QueryWrapper simpleAnd() {
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.TRUE);
        queryWrapper.setSqlRelation("and");
        this.addQueryWrapper(queryWrapper);
        return this;
    }

    public QueryWrapper simpleOr() {
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.TRUE);
        queryWrapper.setSqlRelation("or");
        this.addQueryWrapper(queryWrapper);
        return this;
    }

    /**
     * 声明接下来的条件用or关联
     *
     * @return
     */
    public QueryWrapper or() {
        return relationAdd("or");
    }

    public QueryWrapper relationAdd(String relation) {
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.TRUE);
        queryWrapper.setSqlRelation(relation);
        this.modelQueries.add(queryWrapper);
        return this;
    }

    public void addQueryWrapper(QueryWrapper queryWrapper) {
        this.modelQueries.add(queryWrapper);
    }

    /**
     * 可选择的and条件：
     * 如果之前的条件没有添加关联关系(and,or等等)，则会添加and，反之不会，之后会把Consumer中产生的条件使用()进行优先级声明
     *
     * @param consumer
     * @return
     */
    public QueryWrapper optionalAnd(Consumer<QueryWrapper> consumer) {
        return optionalRelationAdd("add", consumer);
    }

    /**
     * 可选择的or条件：
     * 如果之前的条件没有添加关联关系(and,or等等)，则会添加or，反之不会，之后会把Consumer中产生的条件使用()进行优先级声明
     *
     * @param consumer
     * @return
     */
    public QueryWrapper optionalOr(Consumer<QueryWrapper> consumer) {
        return optionalRelationAdd("or", consumer);
    }

    private QueryWrapper optionalRelationAdd(String relation, Consumer<QueryWrapper> consumer) {

        if (Boolean.FALSE.equals(this.modelQueries.get(this.modelQueries.size() - 1).getIsRefCondition())) {
            QueryWrapper queryWrapper = new QueryWrapper(Boolean.TRUE);
            queryWrapper.setSqlRelation(relation);
            this.modelQueries.add(queryWrapper);
        }

        QueryWrapper exQueryWrapper = new QueryWrapper();
        consumer.accept(exQueryWrapper);
        this.modelQueries.add(exQueryWrapper);
        return this;
    }

    /**
     * and关联关系，会把Consumer中产生的条件使用()进行优先级声明
     *
     * @param consumer
     * @return
     */
    public QueryWrapper and(Consumer<QueryWrapper> consumer) {
        return relationAdd("and", consumer);
    }

    /**
     * or关联关系，会把Consumer中产生的条件使用()进行优先级声明
     *
     * @param consumer
     * @return
     */
    public QueryWrapper or(Consumer<QueryWrapper> consumer) {
        return relationAdd("or", consumer);
    }

    /**
     * @param relation
     * @param consumer
     * @return {@link QueryWrapper }
     */
    private QueryWrapper relationAdd(String relation, Consumer<QueryWrapper> consumer) {
        QueryWrapper queryWrapper = new QueryWrapper(Boolean.TRUE);
        queryWrapper.setSqlRelation(relation);
        this.modelQueries.add(queryWrapper);

        QueryWrapper exQueryWrapper = new QueryWrapper();
        consumer.accept(exQueryWrapper);
        this.modelQueries.add(exQueryWrapper);
        return this;
    }

    /**
     * 构建实际可用的sql实体类（主要作用是添加右括号以及平铺条件）
     *
     * @param queryWrapper
     * @return
     */
    public static List<QueryWrapper> buildSqlQo(QueryWrapper queryWrapper) {
        if (Objects.isNull(queryWrapper)) {
            return Collections.emptyList();
        }
        List<QueryWrapper> result = new LinkedList<>();

        //处理自身
        QueryWrapper endMqw = null;
        if (Boolean.TRUE.equals(queryWrapper.getRelation())) {
            result.add(queryWrapper);
            endMqw = new QueryWrapper(Boolean.TRUE);
            endMqw.setSqlRelation(")");
        }

        if (CollectionUtils.isEmpty(queryWrapper.getModelQueries())) {
            return Collections.emptyList();
        }
        //处理其他条件节点
        for (QueryWrapper mw : queryWrapper.getModelQueries()) {
            if (CollectionUtils.isEmpty(mw.getModelQueries())) {
                result.add(mw);
                continue;
            }
            // 递归
            result.addAll(buildSqlQo(mw));
        }

        if (Objects.nonNull(endMqw)) {
            result.add(endMqw);
        }
        return result;
    }

    @Override
    public String toString() {
        if (Boolean.TRUE.equals(this.relation)) {
            return this.sqlRelation;
        }

        String value = "";
        if (Boolean.TRUE.equals(this.getIsList())) {
            value = Optional.ofNullable(this.getValues()).map(list -> list.stream()
                    .map(Object::toString).map(v -> '"' + v + '"').collect(Collectors.joining(","))).orElse("()");
        } else if (Boolean.TRUE.equals(this.getIsBetween())) {
            value = Optional.ofNullable(this.getValues()).map(list -> list.stream()
                    .map(Object::toString).collect(Collectors.joining(" and "))).orElse("");
        } else if (this.getValue() != null) {
            value = String.format("'%s'", this.getValue());
        }

        String subSql = Optional.ofNullable(this.getModelQueries())
                .map(list -> list.stream().map(QueryWrapper::toString).collect(Collectors.joining(" ")))
                .orElse("");
        return String.format("%s %s %s", this.getProperty(), this.getCondition(), value) + subSql;
    }

}
