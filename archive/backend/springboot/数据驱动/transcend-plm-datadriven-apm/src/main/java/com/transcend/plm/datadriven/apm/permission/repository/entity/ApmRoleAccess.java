package com.transcend.plm.datadriven.apm.permission.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author unknown
 * @TableName apm_role_access
 */
@TableName(value ="apm_role_access")
@Data
public class ApmRoleAccess implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 角色bid
     */
    @TableField(value = "role_bid")
    private String roleBid;

    /**
     * 资源bid
     */
    @TableField(value = "access_bid")
    private String accessBid;

    /**
     * 域bid
     */
    @TableField(value = "sphere_bid")
    private String sphereBid;

    /**
     * 对象编码
     */

    @TableField(value = "space_app_bid")
    private String spaceAppBid;

    /**
     * 对象字段名称
     */
    @TableField(value = "filed_name")
    private String filedName;

    /**
     * 表字段名称
     */
    @TableField(value = "column_name")
    private String columnName;

    /**
     * 关系
     */
    @TableField(value = "relationship")
    private String relationship;

    /**
     * 条件比较值
     */
    @TableField(value = "filed_value")
    private String filedValue;

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