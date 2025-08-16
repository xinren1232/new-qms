package com.transcend.plm.datadriven.apm.permission.service;

import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.apm.permission.pojo.dto.PermissionCheckDto;

import java.util.List;

/**
 * @author unknown
 */
public interface IPermissionCheckService {
    /**
     * checkSpaceAppPermssion
     *
     * @param permissionCheckDto permissionCheckDto
     * @return {@link boolean}
     */
    boolean checkSpaceAppPermssion(PermissionCheckDto permissionCheckDto);

    boolean checkSpaceAppPermssion(String modelCode, List<String> bids, String operatorCode);

    /**
     * checkInstancePermission
     *
     * @param permissionCheckDto permissionCheckDto
     * @return {@link boolean}
     */
    boolean checkInstancePermission(PermissionCheckDto permissionCheckDto);

    void getInnerRole(MBaseData appData, String userNO, List<String> roleCodes);

    /**
     * checkPermission
     *
     * @param permissionCheckDto permissionCheckDto
     * @return {@link boolean}
     */
    boolean checkPermission(PermissionCheckDto permissionCheckDto);

    /**
     * checkInstancesPermission
     *
     * @param list         list
     * @param spaceBid     spaceBid
     * @param operatorCode operatorCode
     * @return {@link boolean}
     */
    boolean checkInstancesPermission(List<MObject> list, String spaceBid, String operatorCode);
}
