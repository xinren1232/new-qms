package com.transcend.plm.datadriven.apm.permission.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author unknown
 * @TableName apm_role
 */
@TableName(value ="apm_role")
@Data
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApmRole implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    @TableField(value = "bid")
    private String bid;

    /**
     * 父级bid
     */
    @TableField(value = "pbid")
    private String pbid;

    /**
     * 编码
     */
    @TableField(value = "code")
    private String code;

    /**
     * 类型
     */
    private String type;

    /**
     * 名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 父级bid
     */
    @TableField(value = "parent_bid")
    private String parentBid;

    /**
     * 名称
     */
    @TableField(value = "path")
    private String path;

    /**
     * 域id
     */
    @TableField(value = "sphere_bid")
    private String sphereBid;


    /**
     * 角色来源(自定义/系统内置)
     */
    private String roleOrigin;

    /**
     * 描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 创建时间
     */
    @TableField(value = "created_time")
    private Date createdTime;

    /**
     * 创建者
     */
    @TableField(value = "created_by")
    private String createdBy;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time")
    private Date updatedTime;

    /**
     * 更新人
     */
    @TableField(value = "updated_by")
    private String updatedBy;

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

    /**
     * 外部唯一标识
     */
    @TableField(value = "foreign_bid")
    private String foreignBid;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}