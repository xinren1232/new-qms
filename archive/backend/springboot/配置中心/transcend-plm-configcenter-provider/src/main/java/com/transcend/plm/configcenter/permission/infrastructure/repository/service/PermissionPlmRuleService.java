package com.transcend.plm.configcenter.permission.infrastructure.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.configcenter.permission.infrastructure.repository.po.PermissionPlmRule;

import java.util.List;

/**
 *
 */
public interface PermissionPlmRuleService extends IService<PermissionPlmRule> {

    int deleteByRules(List<PermissionPlmRule> permissionPlmRules);

    int deleteByBids(List<String> bids);

}
