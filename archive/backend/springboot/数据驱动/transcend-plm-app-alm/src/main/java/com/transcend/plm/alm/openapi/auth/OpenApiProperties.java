package com.transcend.plm.alm.openapi.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * OpenApi配置文件
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/26 21:19
 */
@Data
@ConfigurationProperties(prefix = "transsion.alm.open.api")
public class OpenApiProperties {

    /**
     * 时间戳有效期（单位：秒）
     */
    private int timestampValiditySeconds = 300;
    /**
     * openApi前缀路径
     */
    private String openApiPath;
    /**
     * 客户端列表
     */
    private List<Client> clients;


    @Data
    public static class Client {
        /**
         * 访问key
         */
        private String accessKey;
        /**
         * 密钥
         */
        private String secretKey;

        /**
         * 允许的路径
         */
        private String[] permitPaths;

    }

}
