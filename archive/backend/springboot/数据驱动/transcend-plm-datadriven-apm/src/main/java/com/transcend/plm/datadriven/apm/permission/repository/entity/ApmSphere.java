package com.transcend.plm.datadriven.apm.permission.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author unknown
 * @TableName apm_domain
 */
@TableName(value ="apm_sphere")
@Data
public class ApmSphere implements Serializable {
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
     * 父级bid
     */
    @TableField(value = "pbid")
    private String pbid;

    /**
     * 名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 业务id
     */
    @TableField(value = "biz_bid")
    private String bizBid;

    /**
     * 类型 :space空间，object对象，instance对象下实例
     */
    @TableField(value = "type")
    private String type;

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