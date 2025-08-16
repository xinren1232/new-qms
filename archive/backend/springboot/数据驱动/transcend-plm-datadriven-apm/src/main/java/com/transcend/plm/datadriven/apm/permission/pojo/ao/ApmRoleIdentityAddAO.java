package com.transcend.plm.datadriven.apm.permission.pojo.ao;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @author unknown
 */
@Data
@Accessors(chain = true)
public class ApmRoleIdentityAddAO {
    /**
     * 角色bid
     */
    private String roleBid;

    /**
     * 操作类型 employee人员，department
     */
    private String type;

    private List<ApmRoleIdentityAO> apmRoleIdentityAOS;

    private List<Map<String,Object>> dataList;
}
