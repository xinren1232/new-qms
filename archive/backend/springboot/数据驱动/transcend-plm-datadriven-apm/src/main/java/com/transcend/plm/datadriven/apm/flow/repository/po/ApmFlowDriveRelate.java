package com.transcend.plm.datadriven.apm.flow.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @author unknown
 * @TableName apm_flow_drive_relate
 */
@TableName(value ="apm_flow_drive_relate")
@Data
public class ApmFlowDriveRelate implements Serializable {
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 业务BID
     */
    @TableField(value = "bid")
    private String bid;

    /**
     * 事件BID
     */
    @TableField(value = "event_bid")
    private String eventBid;

    /**
     * 关系编码
     */
    @TableField(value = "relation_model_code")
    private String relationModelCode;

    /**
     * 源对象CODE
     */
    @TableField(value = "source_model_code")
    private String sourceModelCode;

    /**
     * 源对象名称
     */
    @TableField(value = "source_model_name")
    private String sourceModelName;

    /**
     * 源对象生命周期编码
     */
    @TableField(value = "source_life_cycle_code")
    private String sourceLifeCycleCode;

    /**
     * 源流程模板BID
     */
    @TableField(value = "source_flow_template_bid")
    private String sourceFlowTemplateBid;

    /**
     * 源流程模板名称
     */
    @TableField(value = "source_flow_template_name")
    private String sourceFlowTemplateName;

    /**
     * 源流程节点BID
     */
    @TableField(value = "source_node_bid")
    private String sourceNodeBid;

    /**
     * 源流程节点名称
     */
    @TableField(value = "source_node_name")
    private String sourceNodeName;

    /**
     * 完成类型，1：一个完成就驱动，2：全部完成才驱动
     */
    @TableField(value = "complete_type")
    private Integer completeType;

    /**
     * 租户ID
     */
    @TableField(value = "tenant_id")
    private Long tenantId;

    /**
     * 创建人
     */
    @TableField(value = "created_by")
    private String createdBy;

    /**
     * 创建时间
     */
    @TableField(value = "created_time")
    private Date createdTime;

    /**
     * 更新人
     */
    @TableField(value = "updated_by")
    private String updatedBy;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time")
    private Date updatedTime;

    /**
     * 删除标识
     */
    @TableField(value = "delete_flag")
    private Boolean deleteFlag;

    /**
     * 启用标志，0未启用，1启用，2禁用
     */
    @TableField(value = "enable_flag")
    private Integer enableFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}