package com.transcend.plm.datadriven.common.wapper;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 包装基础模型
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2024/11/25 15:09
 */
@Getter
@ToString(callSuper = true)
public class MapWrapper implements Map<String, Object> {

    /**
     * 元数据
     */
    Map<String, Object> metadata;

    public MapWrapper() {
        this.metadata = new HashMap<>();
    }

    public MapWrapper(@NonNull Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public MapWrapper(@NonNull MapWrapper wrapper) {
        this.metadata = wrapper.metadata;
    }

    public Object get(String key) {
        return this.metadata.get(key);
    }

    public <V> V get(Class<V> vClass, String key) {
        return Convert.convert(vClass, this.metadata.get(key));
    }

    public <V extends MapWrapper> V getWrapper(Class<V> vClass, String key) {
        Object object = this.metadata.get(key);
        if (object == null) {
            return null;
        }
        if (object instanceof MapWrapper) {
            return Convert.convert(vClass, object);
        }
        if (object instanceof Map) {
            V instance = ReflectUtil.newInstance(vClass, object);
            this.put(key, instance);
            return instance;
        }
        throw new IllegalArgumentException("无法包装类型" + object.getClass());
    }

    public <V extends MapWrapper> List<V> getWrapperList(Class<V> vClass, String key) {
        List<?> list = Convert.toList(this.metadata.get(key));
        if (list == null) {
            return null;
        }

        return list.stream().map(o -> {
            if (o == null) {
                return null;
            }
            if (vClass.isAssignableFrom(o.getClass())) {
                return Convert.convert(vClass, o);
            }
            if (o instanceof Map) {
                return ReflectUtil.newInstance(vClass, o);
            }
            throw new IllegalArgumentException("无法包装类型" + o.getClass().getSimpleName());
        }).collect(Collectors.toList());
    }

    public String getStr(String key) {
        return Convert.toStr(this.metadata.get(key));
    }

    public Boolean getBool(String key) {
        return Convert.toBool(this.metadata.get(key));
    }

    public Integer getInt(String key) {
        return Convert.toInt(this.metadata.get(key));
    }

    public Long getLong(String key) {
        return Convert.toLong(this.metadata.get(key));
    }

    public Double getDouble(String key) {
        return Convert.toDouble(this.metadata.get(key));
    }

    public LocalDateTime getLocalDateTime(String key) {
        return Convert.toLocalDateTime(this.metadata.get(key));
    }

    public <V> List<V> getList(Class<V> vClass, String key) {
        return Convert.toList(vClass, this.metadata.get(key));
    }

    @Override
    public Object put(String key, Object value) {
        return this.metadata.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return this.metadata.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ?> map) {
        this.metadata.putAll(map);
    }

    @Override
    public void clear() {
        this.metadata.clear();
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return Collections.emptySet();
    }

    @NotNull
    @Override
    public Collection<Object> values() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return Collections.emptySet();
    }

    public void putAll(MapWrapper metadata) {
        if (metadata != null) {
            this.putAll(metadata.metadata);
        }
    }

    public boolean containsKey(String key) {
        return this.metadata.containsKey(key);
    }

    @Override
    public int size() {
        return this.metadata.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public Object get(Object key) {
        return null;
    }

    /**
     * 拆包元数据
     *
     * @return 元数据拆包对象
     */
    @SuppressWarnings({"unchecked"})
    public Map<String, Object> unWrapper() {
        return (Map<String, Object>) unWrapper(this);
    }

    /**
     * 拆包元数据
     *
     * @param value 数据
     * @return 元数据拆包对象
     */
    @SuppressWarnings({"rawtypes"})
    public static Object unWrapper(Object value) {
        if (value instanceof MapWrapper) {
            return unWrapperMap(((MapWrapper) value).metadata);
        } else if (value instanceof Object[]) {
            return unWrapperArray((Object[]) value);
        } else if (value instanceof Iterable) {
            return unWrapperIterable((Iterable) value);
        } else if (value instanceof Map) {
            return unWrapperMap((Map) value);
        }
        return value;
    }

    /**
     * 拆包数组
     *
     * @param data 数据
     * @return 元数据拆包对象
     */
    private static Object[] unWrapperArray(Object[] data) {
        for (int i = 0; i < data.length; i++) {
            data[i] = unWrapper(data[i]);
        }
        return data;
    }

    /**
     * 拆包迭代器
     *
     * @param iterable 迭代器
     * @return 元数据拆包对象
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Iterable unWrapperIterable(Iterable iterable) {
        List newList = new ArrayList();
        boolean isUnpacked = false;

        for (Object data : iterable) {
            Object unpackedData = unWrapper(data);
            newList.add(unpackedData);
            if (unpackedData != data) {
                isUnpacked = true;
            }
        }
        return isUnpacked ? newList : iterable;
    }

    /**
     * 拆包包装
     *
     * @param map 元数据
     * @return 元数据拆包对象
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map unWrapperMap(Map map) {
        map.replaceAll((k, v) -> unWrapper(v));
        return map;
    }
}
