package com.transcend.plm.datadriven.infrastructure.web.request;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Ttl版请求上下文过滤器
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/27 11:26
 */
public class TtlRequestContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 将当前请求的 RequestAttributes 写入 TtlRequestContextHolder
        RequestAttributes attributes = new ServletRequestAttributes(request);
        TtlRequestContextHolder.setRequestAttributes(attributes);

        try {
            // 继续处理请求
            filterChain.doFilter(request, response);
        } finally {
            // 确保在请求结束后清理上下文
            TtlRequestContextHolder.resetRequestAttributes();
        }
    }
}
