package com.transcend.plm.datadriven.common.dynamic.fields;

import com.transcend.plm.datadriven.common.strategy.MultipleStrategy;

/**
 * 动态字段
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/26 10:01
 */
public interface DynamicFields extends MultipleStrategy<String> {


    /**
     * 是否支持服务
     *
     * @param value 字段值
     * @return 是否支持
     */
    @Override
    default boolean isSupport(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return value.contains(getPlaceholder());
    }

    /**
     * 获取占位符
     *
     * @return 占位符
     */
    default String getPlaceholder() {
        return String.format("{%s}", getCode());
    }


    /**
     * 获取字段值
     *
     * @return 字段值
     */
    String getCode();

    /**
     * 获取字段名称
     *
     * @return 字段名称
     */
    String getName();

    /**
     * 获取字段值
     *
     * @return 字段值
     */
    Object getValue();

}
