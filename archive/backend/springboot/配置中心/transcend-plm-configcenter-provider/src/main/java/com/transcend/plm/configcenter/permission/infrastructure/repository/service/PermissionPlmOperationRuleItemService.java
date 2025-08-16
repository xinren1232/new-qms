package com.transcend.plm.configcenter.permission.infrastructure.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.PermissionPlmOperationRuleItem;

import java.util.List;

/**
 *
 */
public interface PermissionPlmOperationRuleItemService extends IService<PermissionPlmOperationRuleItem> {

    int deleteByRules(List<PermissionPlmOperationRuleItem> operationRuleItems);

    int deleteByBids(List<String> bids);
}
