package com.transcend.plm.datadriven.common.mapping;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 源对象直接映射子对象映射上下文工具
 *
 * @author xin.wu2
 * @version v1.0
 * createdAt 2024/11/30
 */
public class MappingSourceContext<S, K, V> extends MappingContext<S, K, V> {

    /**
     * 查询通过源对象查询实例的方法
     */
    protected Function<List<S>, Map<K, V>> findFunctionBySource;

    /**
     * 构建对象
     */
    public MappingSourceContext() {
        this.forceMapped = false;
    }

    /**
     * 构建对象
     *
     * @param findFunction         查询实例方法 通过key列表查询实例信息
     * @param findFunctionBySource 查询通过源对象查询实例的方法
     * @param keyTransformFunction 从目标对象中提取出key对象的方法
     * @param forceMapped          是否强制映射，默认强制加载 如果findFunction为null 则forceMapped始终为false
     * @param sourceList           源对象列表
     */
    public MappingSourceContext(Function<List<K>, Map<K, V>> findFunction,
                                @NonNull Function<List<S>, Map<K, V>> findFunctionBySource,
                                @NonNull Function<S, K> keyTransformFunction,
                                Boolean forceMapped,
                                Collection<S> sourceList) {
        if (findFunction != null) {
            this.findFunction = findFunction;
            if (forceMapped != null) {
                this.forceMapped = forceMapped;
            }
        } else {
            this.forceMapped = false;
        }

        this.findFunctionBySource = findFunctionBySource;
        this.keyTransformFunction = keyTransformFunction;
        loadInstance(sourceList);
    }

    /**
     * 构建对象
     *
     * @param findFunctionBySource 查询通过源对象查询实例的方法
     * @param keyTransformFunction 从目标对象中提取出key对象的方法
     */
    public MappingSourceContext(Function<List<S>, Map<K, V>> findFunctionBySource,
                                Function<S, K> keyTransformFunction) {
        this(null, findFunctionBySource, keyTransformFunction, null, null);
    }

    /**
     * 根据源对象列表加载实例
     *
     * @param sourceList 源对象列表
     */
    @Override
    public void loadInstance(Collection<S> sourceList) {
        if (sourceList == null || sourceList.isEmpty()) {
            return;
        }
        Map<K, V> result = findFunctionBySource.apply(new ArrayList<>(sourceList));
        instances.putAll(result);
    }

}
