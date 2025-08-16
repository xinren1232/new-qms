package com.transcend.plm.datadriven.apm.permission.pojo.dto;

import com.transcend.plm.datadriven.apm.permission.pojo.vo.PermissionOperationItemVo;
import com.transcend.plm.datadriven.apm.permission.repository.entity.PermissionPlmRuleCondition;
import lombok.Data;

import java.util.List;

/**
 * @author unknown
 */
@Data
public class  AppPermissionDto {
    /**
     * 空间应用业务ID
     */
    private String spaceAppBid;

    private String ruleName;

    /**
     * 空间应用实例业务ID
     */
    private String instanceBid;

    /**
     * 应用基础权限BID
     */
    private String permissionBid;

    /**
     * 条件权配置
     */
    private List<PermissionPlmRuleCondition> permissionPlmRuleConditionList;
    /**
     * 应用基础操作列表
     */
    List<PermissionOperationItemDto> operationItems;

    private List<PermissionOperationItemVo> appPermissionOperationList;

    private String roleCode;

    /**
     * 角色类型 0，空间角色，1内置角色，2私有角色
     */
    private int roleType;

    private List<String> operatorCodeList;

    /**
     * 删除类型 1 删除应用基础权限，2 删除应用条件权限，3 删除实例权限
     */

    private int deleteType;


}
