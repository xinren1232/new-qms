package com.transcend.plm.configcenter.a_springframework.config;

/**
 * TODO 描述
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/4/25 17:10
 * @since 1.0
 */

import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.transcend.plm.configcenter.a_springframework.config.interceptor.TenantRequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Mybatis-plus多租户配置
 */
@Configuration
@Slf4j
public class MybatisPlusConfig implements WebMvcConfigurer {

    @Bean
    public TenantRequestInterceptor getTenantRequestInterceptor() {
        log.info("====== 自定义租户请求拦截器 ======");
        return new TenantRequestInterceptor();
    }



    /**
     * 多租户插件
     */
    /*@Bean
    public TenantLineInnerInterceptor tenantLineInnerInterceptor() {
        TenantLineInnerInterceptor tenantLineInnerInterceptor = new TenantLineInnerInterceptor();
        // 设置租户字段名
        tenantLineInnerInterceptor.setTenantLineHandler(
                () -> new LongValue(getCurrentTenantId())
        );
        return tenantLineInnerInterceptor;
    }*/

    /**
     * 获取当前租户ID
     */
    private Long getCurrentTenantId() {
        // 从上下文中获取当前租户ID
        return TenantRequestInterceptor.TENANT_ID.get();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(getTenantRequestInterceptor())
                .addPathPatterns("/**");
    }
}