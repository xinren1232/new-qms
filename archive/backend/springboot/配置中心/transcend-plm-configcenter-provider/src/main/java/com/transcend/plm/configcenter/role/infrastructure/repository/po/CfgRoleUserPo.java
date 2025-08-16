package com.transcend.plm.configcenter.role.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import lombok.Data;

/**
 * @author peng.qin
 * @version 1.0.0
 * @createTime 2023-08-18 15:52:00
 */

@Data
@TableName("cfg_role_user")
public class CfgRoleUserPo extends BasePoEntity {
    private String roleBid;
    private String type;
    private String roleCode;
    private String jobNumber;
    private String userName;
}
