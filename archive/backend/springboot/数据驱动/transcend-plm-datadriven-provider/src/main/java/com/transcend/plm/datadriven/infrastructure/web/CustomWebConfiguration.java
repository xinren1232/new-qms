package com.transcend.plm.datadriven.infrastructure.web;

import com.transcend.plm.datadriven.infrastructure.web.request.TtlRequestContextFilter;
import com.transcend.plm.datadriven.infrastructure.web.scope.TtlRequestScope;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 请求自定义配置
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/27 11:20
 */
@Configuration
public class CustomWebConfiguration {

    public static final String SCOPE_TTL_REQUEST = "ttl-request";

    @Bean
    public static CustomScopeConfigurer customScopeConfigurer() {
        CustomScopeConfigurer configurer = new CustomScopeConfigurer();
        // 注册 TtlRequestScope
        configurer.addScope(SCOPE_TTL_REQUEST, new TtlRequestScope());
        return configurer;
    }

    @Bean
    public FilterRegistrationBean<TtlRequestContextFilter> ttlRequestContextFilter() {
        FilterRegistrationBean<TtlRequestContextFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TtlRequestContextFilter());
        // 拦截所有请求
        registrationBean.addUrlPatterns("/*");
        registrationBean.setName("ttlRequestContextFilter");
        // 设置过滤器执行顺序
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
