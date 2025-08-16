package com.transcend.plm.datadriven.apm.permission.pojo.ao;

import lombok.Data;

import java.util.List;

/**
 * @author unknown
 */
@Data
public class ApmRoleAccessAddAO {

    private String roleBid;
    private List<ApmRoleAccessAO> apmRoleAccessAOS;

}
