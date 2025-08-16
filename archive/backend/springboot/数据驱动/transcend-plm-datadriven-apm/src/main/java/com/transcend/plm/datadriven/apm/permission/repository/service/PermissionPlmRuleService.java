package com.transcend.plm.datadriven.apm.permission.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.permission.repository.entity.PermissionPlmRule;

import java.util.List;

/**
 * @author unknown
 */
public interface PermissionPlmRuleService extends IService<PermissionPlmRule> {
    /**
     * saveList
     *
     * @param permissionPlmRuleNews permissionPlmRuleNews
     */
    void saveList(List<PermissionPlmRule> permissionPlmRuleNews);
}
