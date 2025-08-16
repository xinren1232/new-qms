package com.transcend.plm.datadriven.apm.permission.driven.standard.product;

/**
 * @author unknown
 * 标准权限action接口
 */
public interface IStandardPermissionProduct<T, R> {
    /**
     * execute
     *
     * @param param param
     * @return {@link R}
     */
    R execute(T param);
}
