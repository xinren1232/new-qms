package com.transcend.plm.datadriven.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author peng.qin
 * @date 2024/07/24
 */
@Configuration
public class AppKeyFeignInterceptor implements RequestInterceptor {
    @Value("${app.secret:4c2a11ee324644c6a5f279ea12d09d25ba19ed11b31b4ef19c50486f9fdb3245}")
    private String appSecret;

    /**
     * @param requestTemplate
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("appSecret", appSecret);
    }
}
