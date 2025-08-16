package com.transcend.plm.datadriven.common.strategy;

import java.util.List;

/**
 * 自动注入Map策略
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2024/10/26 11:03
 */
public class AutoMapStrategyContext<K, P extends AutoMapStrategy<K>> extends MapStrategyContext<K, P> {

    public AutoMapStrategyContext() {
    }

    public AutoMapStrategyContext(List<P> strategyList) {
        registers(strategyList);
    }

    /**
     * 批量注册策略
     *
     * @param strategyList 策略列表
     */
    public void registers(List<P> strategyList) {
        if (strategyList != null && !strategyList.isEmpty()) {
            for (P policy : strategyList) {
                this.register(policy.support(), policy);
            }
        }
    }
}
