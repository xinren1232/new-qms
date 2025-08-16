package com.transcend.plm.datadriven.common.mapping;

import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 子对象映射上下文工具
 *
 * @author xin.wu2
 * @version v1.0
 * createdAt 2024/11/30
 */
@NoArgsConstructor
public class MappingContext<S, K, V> {

    /**
     * 实例信息
     */
    protected final Map<K, V> instances = new HashMap<>();

    /**
     * 强制映射
     * true = 查询实例时，如果未缓存则直接使用查询方法进行查询
     * false = 查询实例时，如果未缓存则直接范围null
     * 默认 true
     */
    protected boolean forceMapped = true;
    /**
     * 查询实例方法
     */
    protected Function<List<K>, Map<K, V>> findFunction;

    /**
     * 从另外一个对象中获取映射实例的key
     */
    protected Function<S, K> keyTransformFunction;

    /**
     * 构建对象
     *
     * @param findFunction         查询实例方法 通过key列表查询实例信息
     * @param keyTransformFunction 从目标对象中提取出key对象的方法
     * @param sourceList           源对象列表
     * @param forceMapped          是否强制映射，默认强制加载
     */
    public MappingContext(@NonNull Function<List<K>, Map<K, V>> findFunction,
                          @NonNull Function<S, K> keyTransformFunction,
                          Boolean forceMapped,
                          Collection<S> sourceList) {
        if (forceMapped != null) {
            this.forceMapped = forceMapped;
        }
        this.findFunction = findFunction;
        this.keyTransformFunction = keyTransformFunction;
        loadInstance(sourceList);
    }

    /**
     * 构建对象
     * 注意此构建不会进行强制加载
     *
     * @param findFunction         查询实例方法 通过key列表查询实例信息
     * @param keyTransformFunction 从目标对象中提取出key对象的方法
     * @param sourceList           源对象列表
     */
    public MappingContext(Function<List<K>, Map<K, V>> findFunction,
                          Function<S, K> keyTransformFunction,
                          Collection<S> sourceList) {
        this(findFunction, keyTransformFunction, false, sourceList);
    }

    /**
     * 构建对象
     * 注意此构建会进行强制加载
     *
     * @param findFunction         查询实例方法 通过key列表查询实例信息
     * @param keyTransformFunction 从目标对象中提取出key对象的方法
     * @param forceMapped          是否强制映射
     */
    public MappingContext(Function<List<K>, Map<K, V>> findFunction,
                          Function<S, K> keyTransformFunction, Boolean forceMapped) {
        this(findFunction, keyTransformFunction, forceMapped, null);
    }

    /**
     * 构建对象
     * 注意此构建会进行强制加载
     *
     * @param findFunction         查询实例方法 通过key列表查询实例信息
     * @param keyTransformFunction 从目标对象中提取出key对象的方法
     */
    public MappingContext(Function<List<K>, Map<K, V>> findFunction,
                          Function<S, K> keyTransformFunction) {
        this(findFunction, keyTransformFunction, true);
    }


    /**
     * 根据源对象列表加载实例
     *
     * @param sourceList 源对象列表
     */
    public void loadInstance(Collection<S> sourceList) {
        if (sourceList == null || sourceList.isEmpty()) {
            return;
        }
        List<K> keys = sourceList.stream().map(keyTransformFunction)
                .filter(Objects::nonNull)
                .filter(key -> !this.instances.containsKey(key))
                .collect(Collectors.toList());

        loadInstanceKeys(keys);
    }

    /**
     * 根据key列表加载实例
     *
     * @param keys key列表
     */
    public void loadInstanceKeys(List<K> keys) {
        if (keys == null || keys.isEmpty() || findFunction == null) {
            return;
        }
        keys = keys.stream()
                //去重
                .distinct()
                //去除已有实例的数据
                .filter(key -> !instances.containsKey(key))
                .collect(Collectors.toList());

        //查询实例
        Map<K, V> result = findFunction.apply(keys);
        instances.putAll(result);

        //有数据未返回的情况,增加空值映射，防止二次查询
        if (keys.size() > result.size()) {
            keys.stream().filter(key -> !instances.containsKey(key))
                    .forEach(key -> instances.put(key, null));
        }
    }


    /**
     * 根据源对象获取映射实例
     *
     * @param sourceObject 源对象
     * @return 实例
     */
    public V get(S sourceObject) {
        return getOrDefault(sourceObject, null);
    }

    /**
     * 根据源对象获取映射实例，如果未找到或者值为null则返回默认值
     *
     * @param sourceObject 源对象
     * @param defaultValue 默认值
     * @return 实例
     */
    public V getOrDefault(S sourceObject, V defaultValue) {
        if (sourceObject == null) {
            return null;
        }
        return getByKeyOrDefault(keyTransformFunction.apply(sourceObject), defaultValue);
    }

    /**
     * 根据key获取映射实例
     *
     * @param key 实例key
     * @return 实例
     */
    public V getByKey(K key) {
        return getByKeyOrDefault(key, null);
    }

    /**
     * 根据key获取映射实例，如果未找到或者值为null则返回默认值
     *
     * @param key          key
     * @param defaultValue 默认值
     * @return value
     */
    public V getByKeyOrDefault(K key, V defaultValue) {
        if (this.forceMapped && !instances.containsKey(key)) {
            loadInstanceKeys(Collections.singletonList(key));
        }
        V v = instances.get(key);
        return v == null ? defaultValue : v;
    }
}
