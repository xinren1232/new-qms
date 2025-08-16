package com.transcend.plm.configcenter.a_springframework.config.interceptor;

import com.transsion.framework.common.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义拦截器
 *
 * @author luojie
 * @date 2018/11/5 19:06
 */
@Slf4j
@Component
public class TenantRequestInterceptor implements HandlerInterceptor {


    /**
     * 动态库名的ThreadLocal
     */
    public static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();

    final private String TENANT_ID_HEADER = "TENANT_ID";
    final private Long DEFAULT_TENANT_ID = 1002L;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String requestTenantIdHeader = request.getHeader(TENANT_ID_HEADER);
        Long tenantId = DEFAULT_TENANT_ID;
        if (StringUtil.isNotBlank(requestTenantIdHeader)){
            tenantId = Long.valueOf(requestTenantIdHeader);
        }

        TENANT_ID.set(tenantId);
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 后置通知清除threadLocal
        unload();
        if (handler instanceof HandlerMethod) {

        }

    }


    public void unload() {
        TENANT_ID.remove(); // Compliant
    }
}
