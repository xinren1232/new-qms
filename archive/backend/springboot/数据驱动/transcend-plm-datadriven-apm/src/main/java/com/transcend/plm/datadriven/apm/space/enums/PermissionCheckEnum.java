package com.transcend.plm.datadriven.apm.space.enums;

import com.transsion.framework.enums.BaseEnum;

/**
 * @author unknown
 */
public enum PermissionCheckEnum implements BaseEnum<String> {
    /**
     * 校验权限
     */
    CHECK_PERMISSION("CHECK_PERMISSION", "校验权限")
    ;
    String code;
    String desc;

    PermissionCheckEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDesc() {
        return this.desc;
    }
}
