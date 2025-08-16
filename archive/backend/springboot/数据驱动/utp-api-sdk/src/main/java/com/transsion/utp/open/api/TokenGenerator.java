package com.transsion.utp.open.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * utp Token生成器
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/21 14:34
 */
public class TokenGenerator {

    /**
     * 获取token
     *
     * @param accessKey 公钥
     * @param secretKey 私钥
     * @return token
     */
    public static String getToken(String accessKey, String secretKey) {
        long timestamp = System.currentTimeMillis() / 1000;
        String nonce = UUID.randomUUID().toString().replace("-", "").toLowerCase();

        return getTokenV1(accessKey, secretKey, timestamp, nonce);
    }


    /**
     * 获取token
     *
     * @param accessKey 公钥
     * @param secretKey 私钥
     * @param timestamp 时间戳
     * @param nonce     随机字符串
     * @return token
     */
    static String getTokenV1(String accessKey, String secretKey, long timestamp, String nonce) {
        String version = "v1";

        //加签内容 v1|timestamp|accessKey|nonce|secretKey
        String contentPrefix = String.format("%s|%s|%s|%s|", version, timestamp, accessKey, nonce);
        String content = contentPrefix + secretKey;

        String sign = md5(content);

        //返回 v1|timestamp|accessKey|nonce|sign
        return contentPrefix + sign;
    }

    /**
     * md5加密
     *
     * @param content 内容
     * @return 加密结果
     */
    static String md5(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] hashInBytes = md.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashInBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
