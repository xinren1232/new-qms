package com.transcend.plm.datadriven.apm.common;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.HashMap;
import java.util.Map;

/**
 * 线程隐式参数传递工具
 * 注意：
 * 1、该工具只能用于线程内传递参数，多线程间无法传递 如需使用
 * 2、每次使用完毕后需立即调用clear方法清除数据，且将该工具的clear方法放在finally中
 *
 * @author xin.wu
 * @version 1.0
 * createdAt 2024/8/26 14:13
 */
public class ThreadLocalImplicitParameter implements AutoCloseable {
    private static final TransmittableThreadLocal<Map<String, Object>> LOCAL = new TransmittableThreadLocal<>();

    /**
     * 设置参数
     * ps：为什么不是静态方法？
     * 防止使用了不清理污染后续线程的数据，所以使用实例方法，继承了AutoCloseable接口，可使用try-with-resource语法
     *
     * @param key   key
     * @param value value
     */
    protected void set(String key, Object value) {
        Map<String, Object> params = LOCAL.get();
        if (params == null) {
            params = new HashMap<>(16);
            LOCAL.set(params);
        }
        params.put(key, value);
    }

    /**
     * 获取参数
     *
     * @param key  key
     * @param type 类型
     * @param <T>  T
     * @return value
     */
    protected static <T> T get(String key, Class<T> type) {
        Map<String, Object> params = LOCAL.get();
        if (params == null) {
            return null;
        }
        Object value = params.get(key);
        //类型不匹配，返回null，避免强转异常
        if (!type.isInstance(value)) {
            return null;
        }
        return type.cast(value);
    }


    /**
     * 清除数据
     */
    public static void clear() {
        LOCAL.remove();
    }

    @Override
    public void close() {
        clear();
    }
}
