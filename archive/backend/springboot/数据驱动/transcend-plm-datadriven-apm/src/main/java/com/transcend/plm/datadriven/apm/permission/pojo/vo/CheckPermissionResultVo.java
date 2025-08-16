package com.transcend.plm.datadriven.apm.permission.pojo.vo;

import lombok.Data;

import java.util.List;

/**
 * @author unknown
 */
@Data
public class CheckPermissionResultVo {
    /**
     *  校验结果
     */
    private Boolean checkResult;

    private String splString;

    public static CheckPermissionResultVo success() {
        CheckPermissionResultVo checkPermissionResultVo = new CheckPermissionResultVo();
        checkPermissionResultVo.setCheckResult(true);
        return checkPermissionResultVo;
    }

    public static CheckPermissionResultVo fail() {
        CheckPermissionResultVo checkPermissionResultVo = new CheckPermissionResultVo();
        checkPermissionResultVo.setCheckResult(false);
        return checkPermissionResultVo;
    }

    public static CheckPermissionResultVo sqlString(String splString) {
        CheckPermissionResultVo checkPermissionResultVo = new CheckPermissionResultVo();
        checkPermissionResultVo.setSplString(splString);
        return checkPermissionResultVo;
    }


}
