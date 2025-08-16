package com.transcend.plm.datadriven.apm.permission.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.permission.repository.entity.PermissionPlmOperationRuleItem;

import java.util.List;

/**
 * @author unknown
 */
public interface PermissionPlmOperationRuleItemService extends IService<PermissionPlmOperationRuleItem> {

    /**
     * saveList
     *
     * @param permissionPlmOperationRuleItemNews permissionPlmOperationRuleItemNews
     */
    void saveList(List<PermissionPlmOperationRuleItem> permissionPlmOperationRuleItemNews);
}
