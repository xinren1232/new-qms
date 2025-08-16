package com.transcend.plm.alm.demandmanagement.service;

import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import lombok.Data;

import java.util.Map;

/**
 * 流程角色处理人增加处理器
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/4/12 11:18
 */
public interface FlowRolePersonnelAdditionService {


    @Data
    class Config {
        /**
         * 空间应用Bid
         */
        private String spaceAppBid;
        /**
         * 流程节点Bid
         */
        private String flowNodeWebBid;
        /**
         * 实例字段名称
         */
        private String fieldName;
        /**
         * 流程角色
         */
        private String roleCode;
    }

    /**
     * 是否支持
     *
     * @param spaceAppBid 空间应用Bid
     * @return true 支持 false 不支持
     */
    boolean isSupport(String spaceAppBid);


    /**
     * 执行增加操作
     *
     * @param spaceApp     空间应用
     * @param flowNodeBid  流程节点Bid
     * @param updateData 更新的数据
     */
    void execute(ApmSpaceApp spaceApp, String flowNodeBid, Map<String, Object> updateData);
}
