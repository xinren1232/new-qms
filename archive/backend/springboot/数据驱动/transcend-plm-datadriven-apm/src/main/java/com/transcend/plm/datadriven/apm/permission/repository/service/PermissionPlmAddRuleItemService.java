package com.transcend.plm.datadriven.apm.permission.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.permission.repository.entity.PermissionPlmAddRuleItem;

import java.util.List;

/**
 * @author unknown
 */
public interface PermissionPlmAddRuleItemService extends IService<PermissionPlmAddRuleItem> {
    /**
     * saveList
     *
     * @param permissionPlmAddRuleItemNews permissionPlmAddRuleItemNews
     */
    void saveList(List<PermissionPlmAddRuleItem> permissionPlmAddRuleItemNews);
}
