package com.transcend.plm.datadriven.apm.permission.pojo.dto;

import lombok.Data;

/**
 * @author unknown
 */
@Data
public class PermissionInstanceDto {
    private String tableName;
    private String whereCondition;
    private String permissionBid;
}
