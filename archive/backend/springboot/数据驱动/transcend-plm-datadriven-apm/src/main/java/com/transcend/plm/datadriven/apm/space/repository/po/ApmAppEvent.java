package com.transcend.plm.datadriven.apm.space.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author unknown
 * @TableName apm_app_event
 */
@TableName(value ="apm_app_event")
@Data
public class ApmAppEvent implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    @TableField(value = "bid")
    private String bid;

    /**
     * 空间BID
     */
    @TableField(value = "space_bid")
    private String spaceBid;

    /**
     * 应用BID
     */
    @TableField(value = "app_bid")
    private String appBid;

    /**
     * 事件类型
     */
    @TableField(value = "type")
    private String type;

    /**
     * 字段名称
     */
    @TableField(value = "field_name")
    private String fieldName;

    /**
     * 执行类路径
     */
    @TableField(value = "path")
    private String path;

    /**
     * 启用标识
     */
    @TableField(value = "enable_flag")
    private Integer enableFlag;

    /**
     * 删除标识
     */
    @TableField(value = "delete_flag")
    private Integer deleteFlag;

    /**
     * 租户ID
     */
    @TableField(value = "tenant_id")
    private String tenantId;

    /**
     * 创建人
     */
    @TableField(value = "created_by")
    private String createdBy;

    /**
     * 更新人
     */
    @TableField(value = "updated_by")
    private String updatedBy;

    /**
     * 创建时间
     */
    @TableField(value = "created_time")
    private Date createdTime;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time")
    private Date updatedTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}