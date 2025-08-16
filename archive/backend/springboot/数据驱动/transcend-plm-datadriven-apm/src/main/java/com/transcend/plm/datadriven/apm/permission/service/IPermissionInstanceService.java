package com.transcend.plm.datadriven.apm.permission.service;

import com.transcend.plm.datadriven.apm.permission.pojo.dto.AppPermissionDto;

/**
 * @author unknown
 */
public interface IPermissionInstanceService {
    /**
     * updateInstanceByBid
     *
     * @param spaceAppBid   spaceAppBid
     * @param instanceBid   instanceBid
     * @param permissionBid permissionBid
     * @return {@link boolean}
     */
    boolean updateInstanceByBid(String spaceAppBid, String instanceBid, String permissionBid);

    /**
     * updateInstanceByCondition
     *
     * @param permissionBid    permissionBid
     * @param appPermissionDto appPermissionDto
     * @return {@link boolean}
     */
    boolean updateInstanceByCondition(String permissionBid, AppPermissionDto appPermissionDto);

    /**
     * updateOldPermissionBidToNewBid
     *
     * @param spaceAppBid      spaceAppBid
     * @param oldPermissionBid oldPermissionBid
     * @param newPermissionBid newPermissionBid
     * @return {@link boolean}
     */
    boolean updateOldPermissionBidToNewBid(String spaceAppBid, String oldPermissionBid, String newPermissionBid);

    /**
     * updateAppPermission
     *
     * @param permissionBid permissionBid
     * @param spaceAppBid   spaceAppBid
     * @return {@link boolean}
     */
    boolean updateAppPermission(String permissionBid, String spaceAppBid);
}
