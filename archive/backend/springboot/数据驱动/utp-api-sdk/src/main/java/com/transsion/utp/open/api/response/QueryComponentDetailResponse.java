package com.transsion.utp.open.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.transsion.utp.open.api.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 查询组件详情接口
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/26 10:05
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class QueryComponentDetailResponse extends Response {

    /**
     * 响应数据
     */
    @JsonProperty("data")
    private Data data;

    @NoArgsConstructor
    @lombok.Data
    public static class Data {
        /**
         * 组件名称
         */
        @JsonProperty("name")
        private String name;
        /**
         * 组件编码
         */
        @JsonProperty("code")
        private String code;
        /**
         * 父组件编码
         */
        @JsonProperty("parentCode")
        private String parentCode;
        /**
         * 组件类型
         */
        @JsonProperty("type")
        private String type;
    }
}
