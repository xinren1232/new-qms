package com.transcend.plm.configcenter.permission.infrastructure.repository.dto;

import com.transcend.plm.configcenter.permission.infrastructure.repository.po.PermissionPlmAddRuleItem;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.PermissionPlmListRuleItem;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.PermissionPlmOperationRuleItem;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.PermissionPlmRule;
import lombok.Data;

import java.util.List;

/**
 * @author quan.cheng
 * @title PermissionResult
 * @date 2024/5/11 14:49
 * @description 所有权限实体数据
 */
@Data
public class PermissionResult {
    public final List<PermissionPlmRule> targetPlmRules;
    public final List<PermissionPlmAddRuleItem> targetAddRuleItems;
    public final List<PermissionPlmListRuleItem> targetListRuleItems;
    public final List<PermissionPlmOperationRuleItem> targetOperationRuleItems;

    public PermissionResult(List<PermissionPlmRule> targetPlmRules, List<PermissionPlmAddRuleItem> targetAddRuleItems, List<PermissionPlmListRuleItem> targetListRuleItems, List<PermissionPlmOperationRuleItem> targetOperationRuleItems) {
        this.targetPlmRules = targetPlmRules;
        this.targetAddRuleItems = targetAddRuleItems;
        this.targetListRuleItems = targetListRuleItems;
        this.targetOperationRuleItems = targetOperationRuleItems;
    }
}
