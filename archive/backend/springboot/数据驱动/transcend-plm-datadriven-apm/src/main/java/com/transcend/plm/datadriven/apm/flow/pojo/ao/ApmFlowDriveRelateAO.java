package com.transcend.plm.datadriven.apm.flow.pojo.ao;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 流程驱动关联AO
 * @createTime 2023-10-26 09:50:00
 */
@Data
public class ApmFlowDriveRelateAO implements Serializable {
    /**
     * ID
     */
    private Integer id;


    /**
     * 业务BID
     */
    
    private String bid;

    /**
     * 事件BID
     */
    
    private String eventBid;

    /**
     * 关系编码
     */
    
    private String relationModelCode;

    /**
     * 源对象CODE
     */
    
    private String sourceModelCode;

    /**
     * 源对象名称
     */
    
    private String sourceModelName;

    /**
     * 源流程模板BID
     */
    
    private String sourceFlowTemplateBid;

    /**
     * 源流程模板名称
     */
    
    private String sourceFlowTemplateName;

    /**
     * 源流程节点BID
     */
    
    private String sourceNodeBid;

    /**
     * 源流程节点名称
     */
    
    private String sourceNodeName;

    /**
     * 源状态
     */
    private String sourceLifeCycleCode;

    /**
     * 完成类型，1：一个完成就驱动，2：全部完成才驱动
     */
    
    private Integer completeType;

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
    
    private Integer enableFlag;

    private static final long serialVersionUID = 1L;
}
