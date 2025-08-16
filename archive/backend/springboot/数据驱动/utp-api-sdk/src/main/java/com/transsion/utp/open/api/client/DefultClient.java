package com.transsion.utp.open.api.client;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Validator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transsion.utp.open.api.*;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

/**
 * OkHttp客户端
 *
 * @author Alan Wu
 * @version v1.0
 * @date 2023/5/31
 */
@Log4j2
public class DefultClient implements Client {
    private final okhttp3.OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    /**
     * 配置信息
     */
    private final DefaultClientConfig config;


    public DefultClient(@NonNull DefaultClientConfig config) {
        Assert.isTrue(Validator.isUrl(config.getDomain()), "request for domain name error.");
        Assert.notBlank(config.getAccessKey(), "accessKey cannot be null.");
        Assert.notBlank(config.getSecretKey(), "secretKey cannot be null.");
        this.config = config;

        HttpLoggingInterceptor.Level level = Optional.ofNullable(config.getLogLevel())
                .filter(StringUtils::isNotBlank).map(HttpLoggingInterceptor.Level::valueOf)
                .orElse(HttpLoggingInterceptor.Level.NONE);
        this.httpClient = new okhttp3.OkHttpClient.Builder().addNetworkInterceptor(new HttpLoggingInterceptor(log::info)
                .setLevel(level)).build();

        this.objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * 构造Post请求参数
     *
     * @param request 请求内容
     * @return 请求内容体
     */
    @SneakyThrows
    private RequestBody createPostBody(Request<?> request) {
        MediaType mediaType = MediaType.get("application/json");
        String params = objectMapper.writeValueAsString(request);
        return RequestBody.create(params.getBytes(StandardCharsets.UTF_8), mediaType);
    }

    /**
     * 构造请求对象
     *
     * @param request 请求信息
     * @return 请求对象
     */
    private okhttp3.Request buildRequest(Request<?> request) {
        String url = RequestUtils.joinUrl(config.getDomain(), request.uri());
        okhttp3.Request.Builder builder;
        switch (request.httpMethod()) {
            case GET:
                HttpUrl.Builder urlBuilder = HttpUrl.get(url).newBuilder();
                RequestUtils.paramMap(request).forEach(urlBuilder::addQueryParameter);
                builder = new okhttp3.Request.Builder().url(urlBuilder.build());
                break;
            case POST:
                builder = new okhttp3.Request.Builder().url(url).post(createPostBody(request));
                break;
            default:
                throw new IllegalArgumentException("request method was not recognized.");
        }
        String token = TokenGenerator.getToken(config.getAccessKey(), config.getSecretKey());
        builder.addHeader(Constant.HEADER_ACCESS_TOKEN_KEY, token);
        return builder.build();
    }


    /**
     * 解析响应结果
     *
     * @param tClass   响应结果类型
     * @param response 响应结果
     * @param <T>      响应结果类型
     * @return 响应结果
     * @throws IOException 流异常
     */
    private <T extends Response> T getResponse(Class<T> tClass, okhttp3.Response response) throws IOException {
        ResponseBody body = response.body();
        if (body == null) {
            throw new IllegalStateException("no response body.");
        }

        String json = "json";
        if (json.equalsIgnoreCase(Objects.requireNonNull(body.contentType()).subtype())) {
            return objectMapper.readValue(body.string(), tClass);
        }
        throw new IllegalStateException("response content cannot be recognized");
    }

    @Override
    @SneakyThrows
    public <T extends Response> T syncInvoke(@NonNull Request<T> request) {
        try (okhttp3.Response response = httpClient.newCall(buildRequest(request)).execute()) {
            if (!response.isSuccessful()) {
                throw new IllegalStateException(String.format("status[%s] request failed.", response.code()));
            }
            return getResponse(request.responseClass(), response);
        }
    }

}
