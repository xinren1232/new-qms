package com.transcend.plm.datadriven.apm.permission.pojo.ao;

import lombok.Data;

/**
 * @author unknown
 */
@Data
public class ApmRoleIdentityAO {
    private Integer id;

    /**
     * 角色bid
     */
    private String roleBid;

    /**
     *
     */
    private String identity;

    /**
     * employee,用户;department,部门
     */
    private String type;

    private String name;

    private String deptName;

    private String foreignBid;
    /**
     * 人员投入百分比
     */
    private Integer inputPercentage;
}
