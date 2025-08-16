package com.transcend.plm.alm.demandmanagement.service;

import lombok.Data;

import java.util.List;

/**
 * 关系解除拦截服务
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/5 17:51
 */
public interface RelationUnlinkInterceptorService {

    /**
     * 配置信息
     */
    @Data
    class Config {

        /**
         * 关系code
         */
        private String modelCode;

        /**
         * 达到某个状态后
         */
        private String status;

    }

    /**
     * 是否支持
     *
     * @param modelCode 模型编码
     * @return 是否支持
     */
    boolean isSupport(String modelCode);

    /**
     * 判断是否执行拦截
     *
     * @param modelCode         关系模型编码
     * @param sourceSpaceAppBid 源数据所属SpaceApp的Bid
     * @param sourceBid         源数据Bid
     * @param relationBidList   关联数据Bid集合
     * @return true 表示需要拦截，false表示不需要拦截
     */
    boolean isIntercept(String modelCode, String sourceSpaceAppBid,
                        String sourceBid, List<String> relationBidList);
}
