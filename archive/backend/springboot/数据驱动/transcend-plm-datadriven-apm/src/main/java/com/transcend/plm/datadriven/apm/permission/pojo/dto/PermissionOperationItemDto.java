package com.transcend.plm.datadriven.apm.permission.pojo.dto;

import lombok.Data;

/**
 * @author unknown
 */
@Data
public class PermissionOperationItemDto {
    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色类型 0，空间角色，1内置角色，2私有角色
     */
    private int roleType;

    /**
     * 操作编码
     */
    private String operatorCode;


}
