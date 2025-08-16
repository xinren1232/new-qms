package com.transcend.plm.datadriven.apm.flow.repository.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author unknown
 * @TableName apm_flow_instance_process
 */
@TableName(value ="apm_flow_instance_process", autoResultMap = true)
@Data
public class ApmFlowInstanceProcessPo implements Serializable {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 业务id
     */
    @TableField(value = "bid")
    private String bid;

    /**
     * 空间应用bid
     */
    @TableField(value = "flow_instance_bid")
    private String flowInstanceBid;

    /**
     * 节点名称
     */
    @TableField(value = "node_name")
    private String nodeName;

    /**
     * 经办人
     */
    @TableField(value = "processed_by")
    private String processedBy;
    /**
     * 经办动作
     */
    @TableField(value = "processed_action")
    private String processedAction;
    /**
     * 经办人名称
     */
    @TableField(value = "processed_by_name")
    private String processedByName;
    /**
     * 经办动作名称
     */
    @TableField(value = "processed_action_name")
    private String processedActionName;

    /**
     * 经办流转动作名称  XXX节点 => XXX节点
     */
    @TableField(value = "processed_node_name")
    private String processedNodeName;
    /**
     * 配置内容
     */
    @TableField(value = "content", typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Map<String, Object> content;

    /**
     * 到达时间
     */
    @TableField(value = "arrival_time")
    private Date arrivalTime;


    /**
     * 结束时间
     */
    @TableField(value = "end_time")
    private Date endTime;

    /**
     * 创建时间
     */
    @TableField(value = "created_time")
    private Date createdTime;

    /**
     * 租户ID
     */
    @TableField(value = "tenant_id")
    private Integer tenantId;

    /**
     * 启用标志
     */
    @TableField(value = "enable_flag")
    private Integer enableFlag;

    /**
     * 删除标志
     */
    @TableField(value = "delete_flag")
    @TableLogic
    private Integer deleteFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}