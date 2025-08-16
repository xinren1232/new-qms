package com.transcend.plm.datadriven.common.mybatis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 配置mybatis插件
 * @createTime 2023-08-30 14:05:00
 */

@Configuration
public class MybatisPluginConfig {
    @Bean
    public JsonWritePlugin jsonWriteInterceptor() {
        return new JsonWritePlugin();
    }

    @Bean
    public JsonReadPlugin jsonReadInterceptor() {
        return new JsonReadPlugin();
    }
}
