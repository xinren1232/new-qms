package com.transcend.plm.datadriven.apm.permission.pojo.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

/**
 * @author unknown
 */
@Data
public class ApmRoleIdentityVO {

    private Integer id;

    /**
     * 角色bid
     */
    private String roleBid;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     *
     */
    private String identity;

    /**
     * employee,用户;department,部门
     */
    private String type;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 创建人
     */
    @TableField(value = "created_by")
    private String createdBy;

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

    private String name;

    private String deptName;

    private String deptNo;

    private String employeeNo;

    private String employeeName;

    /**
     * 外部唯一标识
     */
    private String foreignBid;
    /**
     * 人员投入百分比
     */
    private Integer inputPercentage;



}
