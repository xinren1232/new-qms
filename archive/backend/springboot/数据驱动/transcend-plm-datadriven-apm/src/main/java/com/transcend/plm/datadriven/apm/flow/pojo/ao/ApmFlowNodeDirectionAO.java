package com.transcend.plm.datadriven.apm.flow.pojo.ao;

import lombok.Data;

import java.util.List;
/**
 * @author unknown
 */
@Data
public class ApmFlowNodeDirectionAO {
    /**
     *
     */
    private String bid;

    /**
     * 生命周期模板id
     */
    private String templateBid;

    /**
     * 版本号
     */
    private String version;

    /**
     * 开始节点bid
     */
    private String sourceNodeBid;

    /**
     * 结束节点bid
     */
    private String targetNodeBid;

    /**
     * 开始节点web bid
     */
    private String sourceNodeWebBid;

    /**
     * 结束节点web bid
     */
    private String targetNodeWebBid;

    /**
     * 连线名称
     */
    private String lineName;

    /**
     * 条件匹配方式，and/all.所有条件匹配，or/any.任一条件匹配
     */
    private String directionMatch;

    /**
     * 存储如坐标等信息
     */
    private Object layout;

    List<ApmFlowNodeDirectionConditionAO> apmFlowNodeDirectionConditionAOList;
}
