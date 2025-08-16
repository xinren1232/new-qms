package com.transsion.utp.open.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.transsion.utp.open.api.HttpMethod;
import com.transsion.utp.open.api.Request;
import com.transsion.utp.open.api.response.QueryComponentTreeResponse;
import lombok.Data;

import java.util.List;

/**
 * 查询测试模块树
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/22 15:52
 */
@Data
public class QueryComponentTreeRequest implements Request<QueryComponentTreeResponse> {


    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.POST;
    }

    @Override
    public String uri() {
        return "/openapi/cases/queryTree";
    }

    @Override
    public Class<QueryComponentTreeResponse> responseClass() {
        return QueryComponentTreeResponse.class;
    }

    /**
     * 模块或者子模块的负责人工号
     */
    @JsonProperty("ownerCodeList")
    private List<String> ownerCodeList;
    /**
     * 模糊搜索字段
     */
    @JsonProperty("searchKey")
    private String searchKey;

    /**
     * 是否删除
     */
    @JsonProperty("isDelete")
    private Boolean isDelete;

}
