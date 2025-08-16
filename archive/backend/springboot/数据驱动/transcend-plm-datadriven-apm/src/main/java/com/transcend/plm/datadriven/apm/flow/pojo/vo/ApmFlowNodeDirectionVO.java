package com.transcend.plm.datadriven.apm.flow.pojo.vo;

import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowNodeDirectionCondition;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author unknown
 */
@Data
public class ApmFlowNodeDirectionVO {
    private Integer id;

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
     * 条件匹配方式，all.所有条件匹配，any.任一条件匹配
     */
    private String directionMatch;

    /**
     * 存储如坐标等信息
     */
    private Object layout;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 删除标识
     */
    private Boolean deleteFlag;

    /**
     * 启用标志，0未启用，1启用，2禁用
     */
    private Boolean enableFlag;


    private List<ApmFlowNodeDirectionCondition> apmFlowNodeDirectionConditionList;
}
