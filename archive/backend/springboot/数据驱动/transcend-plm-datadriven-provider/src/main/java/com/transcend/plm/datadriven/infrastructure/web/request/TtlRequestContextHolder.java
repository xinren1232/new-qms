package com.transcend.plm.datadriven.infrastructure.web.request;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.NoArgsConstructor;
import org.springframework.web.context.request.RequestAttributes;

/**
 * Ttl版请求上下文持有者
 * 便于父子线程请求上下文同步
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/27 11:15
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class TtlRequestContextHolder {

    /**
     * 使用 TTL 实现 RequestAttributes 的存储
     */
    private static final ThreadLocal<RequestAttributes> REQUEST_ATTRIBUTES_HOLDER = new TransmittableThreadLocal<>();

    /**
     * 设置当前线程的 RequestAttributes
     *
     * @param attributes RequestAttributes 实例
     */
    public static void setRequestAttributes(RequestAttributes attributes) {
        REQUEST_ATTRIBUTES_HOLDER.set(attributes);
    }

    /**
     * 获取当前线程的 RequestAttributes
     *
     * @return 当前线程的 RequestAttributes
     */
    public static RequestAttributes getRequestAttributes() {
        return REQUEST_ATTRIBUTES_HOLDER.get();
    }

    /**
     * 清理当前线程的 RequestAttributes
     */
    public static void resetRequestAttributes() {
        REQUEST_ATTRIBUTES_HOLDER.remove();
    }
}
