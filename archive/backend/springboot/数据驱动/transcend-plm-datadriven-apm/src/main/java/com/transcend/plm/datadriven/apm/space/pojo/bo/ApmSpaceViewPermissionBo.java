package com.transcend.plm.datadriven.apm.space.pojo.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;

/**
 * 空间查看权限控制
 * @author unknown
 */
@Data
@Accessors(chain = true)
public class ApmSpaceViewPermissionBo {
    /**
     * 是否忽略权限
     */
    private Boolean ignorePermission;
    /**
     * 有权限的空间bid集合
     */
    private Set<String> permissionSpaceBidSet;

    public static ApmSpaceViewPermissionBo of() {
        return new ApmSpaceViewPermissionBo();
    }
}
