package com.transcend.plm.configcenter.common.validator;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;


/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-03-22 11:44
 **/
@Slf4j
@WebFilter(filterName = "clearThreadLocalFilter",urlPatterns = "/*")
public class ClearThreadLocalFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            chain.doFilter(request, response);
        } finally {

            try {
                ValidatorGroupContext.clear();
                RequestParamContext.clear();
            } catch (Exception e) {
                // 清空失败
                log.error("清空threadLocal失败！", e);
            }
        }
    }

}
