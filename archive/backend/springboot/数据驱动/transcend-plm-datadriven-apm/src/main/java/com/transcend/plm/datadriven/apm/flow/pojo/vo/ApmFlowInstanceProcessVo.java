package com.transcend.plm.datadriven.apm.flow.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author unknown
 */
@Data
public class ApmFlowInstanceProcessVo implements Serializable {

    /**
     * 主键id
     */
    private Integer id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 空间应用bid
     */
    private String flowInstanceBid;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 经办人
     */
    private String processedBy;
    /**
     * 经办动作
     */
    private String processedAction;
    /**
     * 经办人名称
     */
    private String processedByName;
    /**
     * 经办动作名称
     */
    private String processedActionName;
    /**
     * 经办流转动作名称  XXX节点 => XXX节点
     */
    private String processedNodeName;
    /**
     * 配置内容
     */
    private Map<String, Object> content;

    /**
     * 到达时间
     */
    private Date arrivalTime;


    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 启用标志
     */
    private Integer enableFlag;

    /**
     * 删除标志
     */
    private Integer deleteFlag;

    private static final long serialVersionUID = 1L;
}