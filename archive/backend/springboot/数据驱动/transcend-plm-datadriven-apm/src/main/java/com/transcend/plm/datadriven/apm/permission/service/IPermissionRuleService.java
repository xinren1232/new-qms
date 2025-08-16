package com.transcend.plm.datadriven.apm.permission.service;

import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;

/**
 * @author unknown
 */
public interface IPermissionRuleService {
    /**
     * getPermissionBidByAppData
     *
     * @param appData appData
     * @return {@link String}
     */
    String getPermissionBidByAppData(MSpaceAppData appData);
}
