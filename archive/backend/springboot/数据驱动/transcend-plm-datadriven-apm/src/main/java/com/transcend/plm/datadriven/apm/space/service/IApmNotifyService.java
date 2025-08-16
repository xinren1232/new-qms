package com.transcend.plm.datadriven.apm.space.service;

import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;

/**
 * @author unknown
 */
public interface IApmNotifyService {
    /**
     * operateApmNotify
     *
     * @param spaceAppBid   spaceAppBid
     * @param operateType   operateType
     * @param mSpaceAppData mSpaceAppData
     */
    void operateApmNotify(String spaceAppBid, String operateType, MSpaceAppData mSpaceAppData);
}
