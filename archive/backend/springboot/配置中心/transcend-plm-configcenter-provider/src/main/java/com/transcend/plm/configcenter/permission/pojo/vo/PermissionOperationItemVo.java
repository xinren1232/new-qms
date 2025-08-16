package com.transcend.plm.configcenter.permission.pojo.vo;

import lombok.Data;

import java.util.List;
@Data
public class PermissionOperationItemVo {
    /**
     * 角色编码
     */
    private String roleCode;

    List<String> operatorList;
}
