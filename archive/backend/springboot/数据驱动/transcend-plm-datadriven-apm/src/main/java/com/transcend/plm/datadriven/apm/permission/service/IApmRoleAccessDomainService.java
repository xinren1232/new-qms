package com.transcend.plm.datadriven.apm.permission.service;

import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmRoleAccessAddAO;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleAccessVO;

import java.util.List;

/**
 * @author unknown
 */
public interface IApmRoleAccessDomainService {
    /**
     * saveOrUpdate
     *
     * @param apmRoleAccessAddAO apmRoleAccessAddAO
     * @return {@link boolean}
     */
    boolean saveOrUpdate(ApmRoleAccessAddAO apmRoleAccessAddAO);

    /**
     * listByRoleBid
     *
     * @param roleBid roleBid
     * @return {@link List<ApmRoleAccessVO>}
     */
    List<ApmRoleAccessVO> listByRoleBid(String roleBid);
}
