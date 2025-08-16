package com.transcend.plm.configcenter.role.pojo.vo;

import com.transcend.plm.configcenter.api.model.role.vo.CfgRoleVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-22 10:30
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CfgRoleAndTypeVo extends CfgRoleVo {

    /**
     * 角色类型（0-系统角色，1-业务角色） default = 0
     */
    private Byte roleType = 0;

    /**
     * 前端需要的隐藏类型 default = false
     */
    private Boolean disabled = false;




    private static final long serialVersionUID = 1L;

    public static CfgRoleAndTypeVo of() {
        return new CfgRoleAndTypeVo();
    }
}
