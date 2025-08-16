package com.transcend.plm.datadriven.common.strategy;

import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * MaP型策略上下文
 * <p>
 * 主要用于类型策略服务上下文
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2024/10/26 11:03
 */
public class MapStrategyContext<K, P extends Strategy> implements StrategyContext<K, P> {

    private final Map<K, P> strategyMap;

    protected MapStrategyContext() {
        this.strategyMap = new HashMap<>(16);
    }

    protected MapStrategyContext(@NonNull Map<K, P> strategyMap) {
        this.strategyMap = new HashMap<>(strategyMap);
    }

    /**
     * 注册策略服务
     * 遇到服务重复注册时，如果该服务策略类型为{@link AutoMapOrderedStrategy} ，则选择优先级高的策略服务
     * 反之则
     * 否则，抛出{@link UnsupportedOperationException}异常
     *
     * @param key      策略服务标识
     * @param strategy 策略服务
     */
    public synchronized void register(K key, P strategy) {
        if (key == null || strategy == null) {
            throw new IllegalArgumentException("注册的策略服务标识或策略服务不能为空");
        }

        P currentStrategy = this.strategyMap.get(key);
        if (currentStrategy != null) {
            if (currentStrategy instanceof AutoMapOrderedStrategy && strategy instanceof AutoMapOrderedStrategy) {
                // Ordered 值越小越优先 值相同时后注册的会覆盖先前注册的服务
                if (((AutoMapOrderedStrategy<?>) currentStrategy).getOrder() < ((AutoMapOrderedStrategy<?>) strategy).getOrder()) {
                    return;
                }
            } else {
                throw new UnsupportedOperationException("重复注册的策略服务");
            }
        }
        this.strategyMap.put(key, strategy);
    }

    @Override
    public P getStrategyService(K key, boolean require) {
        P policy = this.strategyMap.get(key);
        if (require && policy == null) {
            throw new UnsupportedOperationException("未找到支持的策略服务");
        }
        return policy;
    }
}
