package com.transcend.plm.datadriven.apm.permission.pojo.bo;

import lombok.Data;

import java.util.List;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 部门BO
 * @createTime 2023-09-21 15:36:00
 */
@Data
public class ApmDepartmentBO implements ApmIdentity {
    private String id;
    private String bid;
    private String name;
    private List<ApmUser> userList;
    @Override
    public List<ApmUser> getApmUserList() {
        return this.userList;
    }
}
