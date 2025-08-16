package com.transcend.plm.datadriven.apm.permission.pojo.ao;

import com.transcend.plm.datadriven.apm.permission.pojo.bo.ApmUser;
import lombok.Data;

import java.util.List;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 角色用户AO
 * @createTime 2023-10-07 14:02:00
 */
@Data
public class ApmRoleUserAO {

    private String roleBid;

    private String roleName;

    private List<ApmUser> userList;
}
