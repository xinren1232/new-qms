package com.transcend.plm.datadriven.apm.permission.pojo.vo;

import lombok.Data;

import java.util.List;

/**
 * @author unknown
 */
@Data
public class AppBasePermissionVo {
    private String spaceAppBid;
    private String instanceBid;
    private String permissionBid;
    private String ruleName;

    /**
     * 应用或者实例权限操作列表
     */
    private List<PermissionOperationItemVo> permissionOperationList;

    /**
     * 对象穿透权限操作列表
     */
    private List<PermissionOperationItemVo> objPermissionOperationList;



}
