package com.transsion.utp.open.api.autoconfigure;

import com.transsion.utp.open.api.Client;
import com.transsion.utp.open.api.client.DefaultClientConfig;
import com.transsion.utp.open.api.client.DefultClient;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * UtpApi自动配置
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/22 15:22
 */
@AllArgsConstructor
@Configuration
@EnableConfigurationProperties(UtpApiProperties.class)
@ConditionalOnProperty(prefix = "transsion.utp.open.api", name = {"domain", "accessKey", "secretKey"})
public class UtpApiConfiguration {

    private final UtpApiProperties properties;

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
