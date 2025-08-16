package com.transcend.plm.datadriven.apm.event.context;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;


/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Slf4j
@WebFilter(filterName = "clearEventThreadLocalFilter",urlPatterns = "/*")
public class ClearEventThreadLocalFilter implements Filter {

    /**
     * @param request
     * @param response
     * @param chain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            chain.doFilter(request, response);
        } finally {
            try {
                TranscendEventContext.clear();
            } catch (Exception e) {
                // 清空失败
                log.error("清空threadLocal失败！", e);
            }
        }
    }

}
