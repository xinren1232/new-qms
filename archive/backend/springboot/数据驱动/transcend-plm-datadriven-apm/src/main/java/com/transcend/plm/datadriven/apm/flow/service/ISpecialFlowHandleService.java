package com.transcend.plm.datadriven.apm.flow.service;

import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;

/**
 * @author unknown
 */
public interface ISpecialFlowHandleService {
    /**
     * setSpecialApmRoleUsers
     *
     * @param spaceAppBid   spaceAppBid
     * @param instanceBid   instanceBid
     * @param mSpaceAppData mSpaceAppData
     * @return {@link boolean}
     */
    boolean setSpecialApmRoleUsers(String spaceAppBid, String instanceBid, MSpaceAppData mSpaceAppData);
}
