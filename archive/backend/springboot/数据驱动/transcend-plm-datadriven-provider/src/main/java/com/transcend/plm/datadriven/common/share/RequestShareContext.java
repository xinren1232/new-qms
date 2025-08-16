package com.transcend.plm.datadriven.common.share;

import com.transcend.plm.datadriven.infrastructure.web.CustomWebConfiguration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 共享上下文
 * 为防止一个请求中多次查询单个对象的情况，故把每次查询到的结果放在共享上下文中，便于后续使用
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/26 10:09
 */
@Component
@Scope(value = CustomWebConfiguration.SCOPE_TTL_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestShareContext {
    @SuppressWarnings("rawtypes")
    private Map<Class<? extends ContentProvider>, ContentProvider<?>> providers;
    private final Map<Class<? extends ContentProvider<?>>, Object> cache = new HashMap<>(16);


    @Resource
    public void registers(List<ContentProvider<?>> providers) {
        this.providers = providers.stream().collect(Collectors.toMap(ContentProvider::getClass, Function.identity()));
    }

    /**
     * 获取内容
     *
     * @param providerType 提供者类型
     * @param <T>          数据类型
     * @return 内容信息
     */
    @SuppressWarnings("unchecked")
    public <T> T getContent(Class<? extends ContentProvider<T>> providerType) {
        Object objContent = cache.computeIfAbsent(providerType, key -> {
            ContentProvider<?> contentProvider = providers.get(key);
            if (contentProvider == null) {
                return null;
            }
            return contentProvider.getContent();
        });
        if (objContent == null) {
            return null;
        }
        return (T) objContent;
    }


}
