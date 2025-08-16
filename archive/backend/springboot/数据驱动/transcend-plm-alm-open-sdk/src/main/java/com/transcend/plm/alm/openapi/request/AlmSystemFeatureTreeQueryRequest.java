package com.transcend.plm.alm.openapi.request;

import com.transcend.plm.alm.openapi.HttpMethod;
import com.transcend.plm.alm.openapi.Request;
import com.transcend.plm.alm.openapi.response.AlmSystemFeatureTreeQueryResponse;
import lombok.Data;

/**
 * Alm特性树查询请求
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/27 17:17
 */
@Data
public class AlmSystemFeatureTreeQueryRequest implements Request<AlmSystemFeatureTreeQueryResponse> {

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.GET;
    }

    @Override
    public String uri() {
        return "/open/api/alm/system/feature/tree";
    }

    @Override
    public Class<AlmSystemFeatureTreeQueryResponse> responseClass() {
        return AlmSystemFeatureTreeQueryResponse.class;
    }

    /**
     * 关键词搜索
     * 特性名称与特性编码的模糊匹配
     */
    private String searchKey;
}
