package com.transcend.plm.datadriven.apm.common;


/**
 * 隐式参数调用类型
 *
 * @author xin.wu
 * @version 1.0
 * createdAt 2024/8/26 14:45
 */
public class ApmImplicitParameter extends ThreadLocalImplicitParameter {
    private static final String SKIP_CHECK_PERMISSION_KEY = "skipCheckPermission";

    /**
     * 设置是否校验权限
     *
     * @param skip true跳过校验权限 false不跳过校验权限
     */
    public void setSkipCheckPermission(boolean skip) {
        super.set(SKIP_CHECK_PERMISSION_KEY, skip);
    }

    /**
     * 是否跳过鉴权
     *
     * @return true跳过校验权限 false不跳过校验权限
     */
    public static boolean isSkipCheckPermission() {
        return Boolean.TRUE.equals(ThreadLocalImplicitParameter.get(SKIP_CHECK_PERMISSION_KEY, Boolean.class));
    }
}
