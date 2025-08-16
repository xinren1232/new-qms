package com.transcend.plm.configcenter.permission.infrastructure.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.PermissionPlmListRuleItem;

import java.util.List;

/**
 *
 */
public interface PermissionPlmListRuleItemService extends IService<PermissionPlmListRuleItem> {
    int deleteByRules(List<PermissionPlmListRuleItem> listRuleItems);

    int deleteByBids(List<String> bids);
}
