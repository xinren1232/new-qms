package com.transcend.plm.datadriven.common.strategy;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.OrderComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 多策略上下文
 * <p>
 * 主要用于复杂件实施判定策略
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2024/10/26 11:03
 */
public class MultipleStrategyContext<E, P extends MultipleStrategy<E>> implements StrategyContext<E, P> {

    private final List<P> strategyList;

    protected MultipleStrategyContext() {
        this.strategyList = new ArrayList<>(16);
    }

    protected MultipleStrategyContext(@NonNull List<P> strategyList) {
        this.strategyList = strategyList.stream().filter(Objects::nonNull).collect(Collectors.toList());
        OrderComparator.sort(this.strategyList);
    }

    /**
     * 注册策略服务
     *
     * @param strategy 策略服务
     */
    public synchronized void register(P strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("注册的策略服务不能为空");
        }

        strategyList.add(strategy);
        OrderComparator.sort(this.strategyList);
    }

    @Override
    public P getStrategyService(E entity, boolean require) {
        for (P policyService : strategyList) {
            if (policyService.isSupport(entity)) {
                return policyService;
            }
        }
        if (require) {
            throw new UnsupportedOperationException("未找到支持的策略服务");
        }
        return null;
    }

    /**
     * 获取支持的服务列表
     *
     * @param entity 判定对象
     * @return 支持的服务列表
     */
    @NotNull
    public List<P> supportServices(E entity) {
        return this.strategyList.stream().filter(service -> service.isSupport(entity))
                .collect(Collectors.toList());
    }


}
