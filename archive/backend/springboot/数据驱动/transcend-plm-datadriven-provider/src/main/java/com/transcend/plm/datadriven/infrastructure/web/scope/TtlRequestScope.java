package com.transcend.plm.datadriven.infrastructure.web.scope;

import com.transcend.plm.datadriven.infrastructure.web.request.TtlRequestContextHolder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.web.context.request.RequestAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Ttl版请求范围
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/27 11:18
 */
public class TtlRequestScope implements Scope {

    private static final String SCOPE_NAME = "ttl-request-scope";

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        // 获取当前线程的 RequestAttributes
        RequestAttributes attributes = TtlRequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("No RequestAttributes found for current thread." +
                    " Ensure TtlRequestContextHolder is properly initialized.");
        }

        // 从 RequestAttributes 中获取缓存的 Bean
        @SuppressWarnings("unchecked")
        Map<String, Object> scopedObjects = (Map<String, Object>) attributes.getAttribute(SCOPE_NAME,
                RequestAttributes.SCOPE_REQUEST);
        if (scopedObjects == null) {
            scopedObjects = new HashMap<>(16);
            attributes.setAttribute(SCOPE_NAME, scopedObjects, RequestAttributes.SCOPE_REQUEST);
        }

        // 如果 Bean 不存在，则通过 ObjectFactory 创建
        return scopedObjects.computeIfAbsent(name, key -> objectFactory.getObject());
    }

    @Override
    public Object remove(String name) {
        RequestAttributes attributes = TtlRequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("No RequestAttributes found for current thread." +
                    " Ensure TtlRequestContextHolder is properly initialized.");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> scopedObjects = (Map<String, Object>) attributes.getAttribute(SCOPE_NAME,
                RequestAttributes.SCOPE_REQUEST);
        if (scopedObjects != null) {
            return scopedObjects.remove(name);
        }
        return null;
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        // 此处可以实现销毁回调逻辑，但通常在请求范围内不需要手动销毁
        // Spring 会在请求结束时自动清理 RequestAttributes
    }

    @Override
    public Object resolveContextualObject(String key) {
        // 可以提供上下文对象的动态解析，例如 "request" 或 "session"
        return null;
    }

    @Override
    public String getConversationId() {
        // 使用线程 ID 或其他标志作为作用域标识符
        return Thread.currentThread().getName();
    }
}
