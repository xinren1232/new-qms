package com.transcend.plm.datadriven.apm.permission.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author unknown
 * @TableName apm_role_identity
 */
@TableName(value ="apm_role_identity")
@Data
@Accessors(chain = true)
public class ApmRoleIdentity implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 角色bid
     */
    @TableField(value = "role_bid")
    private String roleBid;

    /**
     * 
     */
    @TableField(value = "identity")
    private String identity;

    /**
     * employee,用户;department,部门
     */
    @TableField(value = "type")
    private String type;

    /**
     * 创建时间
     */
    @TableField(value = "created_time")
    private Date createdTime;

    /**
     * 创建人
     */
    @TableField(value = "created_by")
    private String createdBy;

    /**
     * 租户ID
     */
    @TableField(value = "tenant_id")
    private Integer tenantId;

    @TableField(value = "sort")
    private Integer sort;

    /**
     * 启用标志
     */
    @TableField(value = "enable_flag")
    private Integer enableFlag;

    @TableField(value = "name")
    private String name;

    @TableField(exist = false)
    private String parentBid;

    @TableField(value = "dept_name")
    private String deptName;



    /**
     * 删除标志
     */
    @TableField(value = "delete_flag")
    @TableLogic
    private Integer deleteFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 角色编码
     */
    @TableField(exist = false)
    private String roleCode;

    /**
     * 角色名称
     */
    @TableField(exist = false)
    private String roleName;

    /**
     * 角色类型
     */
    @TableField(exist = false)
    private String roleType;

    /**
     * 角色描述
     */
    @TableField(exist = false)
    private String description;

    /**
     *
     */
    @TableField(exist = false)
    private String sphereBid;

    /**
     * 外部唯一标识
     */
    @TableField(value = "foreign_bid")
    private String foreignBid;

    /**
     * 人员投入百分比
     */
    @TableField(value = "input_percentage")
    private Integer inputPercentage;

}