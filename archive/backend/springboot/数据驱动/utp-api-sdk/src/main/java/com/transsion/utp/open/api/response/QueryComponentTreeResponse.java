package com.transsion.utp.open.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.transsion.utp.open.api.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 测试模块树响应
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/22 15:54
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryComponentTreeResponse extends Response {

    /**
     * 数据
     */
    @JsonProperty("data")
    private List<Data> data;

    @NoArgsConstructor
    @lombok.Data
    public static class Data {
        /**
         * 名称
         */
        @JsonProperty("name")
        private String name;
        /**
         * 编码
         */
        @JsonProperty("code")
        private String code;
        /**
         * 父层级编码
         */
        @JsonProperty("parentCode")
        private String parentCode;

        /**
         * 模块负责人
         */
        @JsonProperty("ownerName")
        private String ownerName;
        /**
         * 模块负责人
         */
        @JsonProperty("ownerCode")
        private String ownerCode;
        /**
         * 类型
         * group 领域
         * subGroup 子领域
         * component 模块
         * subComponent 子模块
         */
        @JsonProperty("type")
        private String type;

        /**
         * 子层数据
         */
        @JsonProperty("children")
        private List<Data> children;

        /**
         * 是否删除
         */
        @JsonProperty("isDelete")
        private Boolean isDelete;
    }

}
