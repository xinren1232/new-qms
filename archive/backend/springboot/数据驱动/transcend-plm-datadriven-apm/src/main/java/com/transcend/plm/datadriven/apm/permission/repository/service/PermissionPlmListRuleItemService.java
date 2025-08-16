package com.transcend.plm.datadriven.apm.permission.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.permission.repository.entity.PermissionPlmListRuleItem;

import java.util.List;

/**
 * @author unknown
 */
public interface PermissionPlmListRuleItemService extends IService<PermissionPlmListRuleItem> {

    /**
     * saveList
     *
     * @param permissionPlmListRuleItemNews permissionPlmListRuleItemNews
     */
    void saveList(List<PermissionPlmListRuleItem> permissionPlmListRuleItemNews);

    /**
     * listInstanceRoleCodes
     *
     * @param permissionBid permissionBid
     * @return {@link List<String>}
     */
    List<String> listInstanceRoleCodes(String permissionBid);
}
