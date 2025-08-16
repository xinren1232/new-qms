package com.transcend.plm.datadriven.common.strategy;

import org.springframework.core.Ordered;

/**
 * 支持型策略服务
 * <p>
 * 该服务需要自行判断是否支持类型操作,且需要实现排序要求
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2024/10/26 11:03
 */
public interface MultipleStrategy<E> extends Strategy, Ordered {

    /**
     * 是否支持服务
     *
     * @param entity 实体对象
     * @return 是否支持
     */
    boolean isSupport(E entity);
}
