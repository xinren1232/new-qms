package com.transsion.utp.open.api.client;

import lombok.Data;

/**
 * 配置信息
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/21 18:37
 */
@Data
public class DefaultClientConfig {
    /**
     * 访问域名
     */
    private String domain;
    /**
     * 公钥
     */
    private String accessKey;
    /**
     * 私钥
     */
    private String secretKey;

    /**
     * 日志级别
     * NONE=不打印
     * BASIC=打印基础信息
     * HEADERS=打印头信息
     * BODY=打印详细内容信息
     */
    private String logLevel;

}
