package com.transsion.utp.open.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.transsion.utp.open.api.HttpMethod;
import com.transsion.utp.open.api.Request;
import com.transsion.utp.open.api.response.QueryComponentDetailResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询测试模块详情接口
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/26 10:04
 */
@NoArgsConstructor
@Data
public class QueryComponentDetailRequest implements Request<QueryComponentDetailResponse> {

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.POST;
    }

    @Override
    public String uri() {
        return "/openapi/cases/queryDetail";
    }

    @Override
    public Class<QueryComponentDetailResponse> responseClass() {
        return QueryComponentDetailResponse.class;
    }

    /**
     * 模块编码 必须
     */
    @JsonProperty("code")
    private String code;
    /**
     * 查询类型 必须
     */
    @JsonProperty("queryType")
    private String queryType;

}
