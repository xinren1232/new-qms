package com.transcend.plm.datadriven.apm.permission.pojo.vo;

import lombok.Data;

import java.util.List;

/**
 * @author unknown
 */
@Data
public class PermissionOperationItemVo {
    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色类型 0，空间角色，1内置角色，2私有角色
     */
    private int roleType;

    List<String> operatorList;
}
