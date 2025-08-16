package com.transcend.plm.configcenter.common.validator;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-03-22 11:44
 **/
public class ValidatorGroupContext {

    private static final ThreadLocal<Class<?>> GROUP_CLASS_HOLDER = new ThreadLocal<>();

    /**
     * 设置临时的校验分组
     *
     */
    public static void set(Class<?> groupValue) {
        GROUP_CLASS_HOLDER.set(groupValue);
    }

    /**
     * 获取临时校验分组
     *
     */
    public static Class<?> get() {
        return GROUP_CLASS_HOLDER.get();
    }

    /**
     * 清除临时缓存的校验分组
     *
     */
    public static void clear() {
        GROUP_CLASS_HOLDER.remove();
    }
}
