package com.transcend.plm.datadriven.common.strategy;

/**
 * 自动注入策略接口标记
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2024/10/26 11:03
 */
public interface AutoMapStrategy<T> extends Strategy {

    /**
     * 支持类型
     *
     * @return 支持类型
     */
    T support();

}
