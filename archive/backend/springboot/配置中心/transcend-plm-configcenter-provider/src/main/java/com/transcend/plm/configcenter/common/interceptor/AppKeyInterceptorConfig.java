package com.transcend.plm.configcenter.common.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description AppKey拦截器配置
 * @createTime 2023-11-08 10:25:00
 */
@Configuration()
public class AppKeyInterceptorConfig implements WebMvcConfigurer {

    @Resource
    private AppKeyInterceptor appKeyInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(appKeyInterceptor).addPathPatterns("/api/**");
        WebMvcConfigurer.super.addInterceptors(registry);
    }
}
