package com.transcend.plm.datadriven.api.model;

import io.swagger.annotations.Api;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;


/**
 * ModelQuery
 *
 * @author yss
 * @date 2022/06/01 20:16
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@Api(value = "领域对象属性QO", tags = "操作领域对象QO")
public class QueryCondition {


    private List<QueryWrapper> queries;

    private List<Order> orders;

    /**
     * @return {@link QueryCondition }
     */
    public static QueryCondition of() {
        return new QueryCondition();
    }

    /**
     * 需要拼接的查询条件
     */
    private List<String> conditionStr;

    private String jsonConditinStr;

    private String additionalSql;

    /**
     * 分页大小
     */
    private Integer pageSize;

    /**
     * 当前页号
     */
    private Integer pageCurrent;


    public Integer getPageSize() {
        if (pageSize == null || pageSize > 1000) {
            return 1000;
        }
        return pageSize;
    }

    public QueryCondition setPage(Integer pageSize, Integer pageCurrent) {
        this.pageSize = pageSize;
        this.pageCurrent = pageCurrent;
        return this;
    }

    public Integer getLimitStartNum() {
        if (pageSize == null || pageCurrent == null) {
            return 0;
        }
        return (pageCurrent - 1) * pageSize;
    }
}
