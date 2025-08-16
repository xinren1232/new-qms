package com.transcend.plm.datadriven.common.strategy;

/**
 * 自动排序支持
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2024/10/26 12:25
 */
public interface AutoMapOrderedStrategy<T> extends AutoMapStrategy<T> {

    /**
     * 最高优先级
     *
     * @see Integer#MIN_VALUE
     */
    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

    /**
     * 最低优先级
     *
     * @see Integer#MAX_VALUE
     */
    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;


    /**
     * 获取优先级
     * 值越小越优先 值相同时后注册的会覆盖先前注册的服务
     *
     * @return 优先级
     */
    default int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
