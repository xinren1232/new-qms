package com.transcend.plm.alm.openapi;

import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Alm加签方法
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/27 15:08
 */
public class Signature {

    /**
     * Alm加签方法
     *
     * @param accessKey 公钥
     * @param secretKey 私钥
     * @param timestamp 时间戳
     * @param method    请求方法
     * @param path      请求路径
     * @param params    请求参数
     * @param body      请求体
     * @return 加签结果
     */
    @SneakyThrows
    public static String sign(String accessKey, String secretKey, String timestamp, String method, String path,
                              String params, String body) {
        String rawData = accessKey + timestamp + method + path + params + body + secretKey;
        MessageDigest sha1Digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = sha1Digest.digest(rawData.getBytes(StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        for (byte b : hashBytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

}


