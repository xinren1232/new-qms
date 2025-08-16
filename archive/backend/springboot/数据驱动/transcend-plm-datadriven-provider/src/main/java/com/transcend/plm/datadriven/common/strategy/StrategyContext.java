package com.transcend.plm.datadriven.common.strategy;

/**
 * 策略上下文接口
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2024/10/26 11:03
 */
public interface StrategyContext<K, P extends Strategy> {

    /**
     * 获取策略服务
     * 如果找不到服务则会抛出异常
     *
     * @param key 策略服务key
     * @return 策略服务
     */
    default P getStrategyService(K key) {
        return getStrategyService(key, true);
    }

    /**
     * 获取策略服务
     *
     * @param key     策略服务key
     * @param require 是否验证 不验证则可能返回null
     * @return 策略服务
     */
    P getStrategyService(K key, boolean require);
}
