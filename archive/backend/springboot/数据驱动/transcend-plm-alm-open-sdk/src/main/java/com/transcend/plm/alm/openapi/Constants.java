package com.transcend.plm.alm.openapi;

/**
 * 常量信息
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/27 14:57
 */
public interface Constants {
    /**
     * 访问key请求头
     */
    String ACCESS_KEY_HEADER = "x-alm-access-key";
    /**
     * 时间戳请求头
     */
    String TIMESTAMP_HEADER = "x-alm-timestamp";
    /**
     * 验签请求头
     */
    String SIGNATURE_HEADER = "x-alm-signature";
}
