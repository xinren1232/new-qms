package com.transcend.plm.alm.openapi.request;

import com.transcend.plm.alm.openapi.HttpMethod;
import com.transcend.plm.alm.openapi.Request;
import com.transcend.plm.alm.openapi.response.AlmSystemFeatureDetailQueryResponse;
import lombok.Data;

/**
 * Alm特性树查询请求
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/27 17:17
 */
@Data
public class AlmSystemFeatureDetailQueryRequest implements Request<AlmSystemFeatureDetailQueryResponse> {

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.GET;
    }

    @Override
    public String uri() {
        return "/open/api/alm/system/feature/detail";
    }

    @Override
    public Class<AlmSystemFeatureDetailQueryResponse> responseClass() {
        return AlmSystemFeatureDetailQueryResponse.class;
    }

    /**
     * 特性bid
     */
    private String bid;

}
