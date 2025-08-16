package com.transcend.plm.configcenter.permission.infrastructure.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.PermissionPlmAddRuleItem;

import java.util.List;

/**
 *
 */
public interface PermissionPlmAddRuleItemService extends IService<PermissionPlmAddRuleItem> {

    int deleteByRules(List<PermissionPlmAddRuleItem> addRuleItems);

    int deleteByBids(List<String> bids);
}
