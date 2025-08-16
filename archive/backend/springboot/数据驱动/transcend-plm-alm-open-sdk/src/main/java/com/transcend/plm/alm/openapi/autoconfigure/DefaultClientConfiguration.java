package com.transcend.plm.alm.openapi.autoconfigure;

import com.transcend.plm.alm.openapi.Client;
import com.transcend.plm.alm.openapi.client.DefaultClientConfig;
import com.transcend.plm.alm.openapi.client.DefultClient;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ALMApi自动配置
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/22 15:22
 */
@AllArgsConstructor
@Configuration
@EnableConfigurationProperties(DefaultClientProperties.class)
@ConditionalOnProperty(prefix = "transsion.alm.open.api.client", name = {"domain", "accessKey", "secretKey"})
public class DefaultClientConfiguration {

    private final DefaultClientProperties properties;

    @Bean
    public Client getClient() {
        DefaultClientConfig config = new DefaultClientConfig();
        config.setDomain(properties.getDomain());
        config.setAccessKey(properties.getAccessKey());
        config.setSecretKey(properties.getSecretKey());
        config.setLogLevel(properties.getLogLevel());
        return new DefultClient(config);
    }
}
