package com.transcend.plm.datadriven.apm.permission.pojo.vo;

import com.transcend.plm.datadriven.apm.permission.repository.entity.PermissionPlmRuleCondition;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author unknown
 */
@Data
public class AppConditionPermissionVo {

    private String spaceAppBid;
    private String permissionBid;
    private String ruleName;
    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 条件权配置
     */
    private List<PermissionPlmRuleCondition> permissionPlmRuleConditionList;


    /**
     * 权限操作列表
     */
    private List<PermissionOperationItemVo> appPermissionOperationList;
}
