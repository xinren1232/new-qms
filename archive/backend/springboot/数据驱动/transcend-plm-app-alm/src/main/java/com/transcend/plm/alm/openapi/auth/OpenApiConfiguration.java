package com.transcend.plm.alm.openapi.auth;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * OpenApi配置
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/27 16:17
 */
@AllArgsConstructor
@Configuration
@EnableConfigurationProperties(OpenApiProperties.class)
@ConditionalOnProperty(prefix = "transsion.alm.open.api", name = {"openApiPath"})
public class OpenApiConfiguration {

    private final OpenApiProperties properties;


    @Bean
    public FilterRegistrationBean<OpenApiAuthFilter> openApiAuthFilter() {
        Map<String, OpenApiProperties.Client> clientMap = Optional.ofNullable(properties.getClients()).map(clients ->
                clients.stream().collect(Collectors.toMap(OpenApiProperties.Client::getAccessKey, Function.identity()))
        ).orElseGet(Collections::emptyMap);

        OpenApiAuthFilter openApiAuthFilter = new OpenApiAuthFilter(clientMap, properties.getTimestampValiditySeconds());

        FilterRegistrationBean<OpenApiAuthFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(openApiAuthFilter);
        // 拦截请求
        registrationBean.addUrlPatterns("/open/api/*");
        registrationBean.setName("openApiAuthFilter");
        // 设置过滤器执行顺序
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
}
