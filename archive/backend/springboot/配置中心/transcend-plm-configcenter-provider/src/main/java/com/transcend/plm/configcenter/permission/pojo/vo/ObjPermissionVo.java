package com.transcend.plm.configcenter.permission.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class ObjPermissionVo {
    private String spaceAppBid;
    private String modelCode;
    private String permissionBid;
    private String ruleName;

    /**
     * 权限操作列表
     */
    private List<PermissionOperationItemVo> permissionOperationList;
    /**
     * 继承权限
     */
    private List<PermissionOperationItemVo> objPermissionOperationList;
}
