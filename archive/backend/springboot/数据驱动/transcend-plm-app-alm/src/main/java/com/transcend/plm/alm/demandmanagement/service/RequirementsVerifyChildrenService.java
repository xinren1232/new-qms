package com.transcend.plm.alm.demandmanagement.service;

import lombok.Data;

/**
 * 需求校验子层服务
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/19 16:12
 */
public interface RequirementsVerifyChildrenService {


    @Data
    class Config {
        /**
         * 需求模型代码
         */
        private String modelCode;
        /**
         * 验证状态
         */
        private String status;
        /**
         * 关联模型代码
         */
        private String relationModelCode;
        /**
         * 子层模型代码
         */
        private String childrenModelCode;
        /**
         * 子层状态
         */
        private String childrenStatus;
        /**
         * 错误信息
         */
        private String errorMessage;

        /**
         * 处理状态排序时使用，非外部传入配置
         */
        private int order;

        /**
         * 是否匹配任意状态
         */
        private boolean anyMatch;

        /**
         * 跳过状态回退
         */
        private boolean skipFallback = true;

    }

    /**
     * 是否支持校验
     *
     * @param modelCode 需求模型代码
     * @return true/false
     */
    boolean isSupport(String modelCode);

    /**
     * 验证需求状态
     *
     * @param modelCode    需求模型代码
     * @param bid          数据Bid
     * @param targetStatus 目标状态
     */
    void verify(String modelCode, String bid, String targetStatus);

}
