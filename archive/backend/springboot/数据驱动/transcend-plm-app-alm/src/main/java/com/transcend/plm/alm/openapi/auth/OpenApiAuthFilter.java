package com.transcend.plm.alm.openapi.auth;

import cn.hutool.core.text.AntPathMatcher;
import com.transcend.plm.alm.openapi.Constants;
import com.transcend.plm.alm.openapi.Signature;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * OpenAPI鉴权过滤器
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/26 16:56
 */
@AllArgsConstructor
public class OpenApiAuthFilter extends OncePerRequestFilter {

    private final Map<String, OpenApiProperties.Client> clients;
    private final int timestampValiditySeconds;

    /**
     * 路径匹配器
     */
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @SuppressWarnings("NullableProblems")
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 从请求头中提取访问key、signature 和时间戳
        String accessKey = request.getHeader(Constants.ACCESS_KEY_HEADER);
        String signature = request.getHeader(Constants.SIGNATURE_HEADER);
        String timestamp = request.getHeader(Constants.TIMESTAMP_HEADER);

        // 验证请求头是否缺失
        if (accessKey == null || signature == null || timestamp == null) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing required headers");
            return;
        }

        //获取客户端配置信息
        OpenApiProperties.Client client = clients.get(accessKey);
        if (client == null) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid access key");
            return;
        }

        // 检查时间戳是否在有效范围内
        if (!isTimestampValid(timestamp)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid or expired timestamp");
            return;
        }

        // 提取请求参数（Query 参数）
        String requestParamsStr = extractRequestParams(request);
        String requestRequestBodyStr = extractRequestBody(request);

        //验签
        String requestUri = request.getRequestURI();
        String sign = Signature.sign(client.getAccessKey(), client.getSecretKey(), timestamp, request.getMethod(),
                requestUri, requestParamsStr, requestRequestBodyStr);
        if (!signature.equals(sign)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid or expired signature");
            return;
        }

        //验证是否放行接口
        String[] permitPaths = client.getPermitPaths();
        if (!isPermitUri(permitPaths, requestUri)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "unauthorized access path");
            return;
        }

        // 验证通过，放行请求
        filterChain.doFilter(request, response);
    }

    /**
     * 验证时间戳是否在有效范围内
     *
     * @param timestampStr 时间戳字符串
     * @return 是否在有效范围内
     */
    private boolean isTimestampValid(String timestampStr) {
        try {
            long requestTimestamp = Long.parseLong(timestampStr);
            // 转换为秒
            long currentTimestamp = System.currentTimeMillis() / 1000;
            return Math.abs(currentTimestamp - requestTimestamp) <= timestampValiditySeconds;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 提取请求参数并拼接为字符串
     *
     * @param request 请求对象
     * @return 请求参数字符串
     */
    private String extractRequestParams(HttpServletRequest request) {
        Enumeration<String> parameterNames = request.getParameterNames();
        TreeMap<String, String> sortedParams = new TreeMap<>();

        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            sortedParams.put(paramName, paramValue);
        }

        // 拼接参数为字符串
        return sortedParams.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
    }

    /**
     * 提取请求体数据
     *
     * @param request 请求对象
     * @return 请求体数据
     */
    @SneakyThrows
    private String extractRequestBody(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (MediaType.APPLICATION_JSON_VALUE.equals(contentType)) {
            StringBuilder builder = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            }
            return builder.toString();
        }
        return "";
    }

    /**
     * 判断当前 URI 是否匹配放行规则
     *
     * @param permitPaths 放行的地址列表
     * @param requestUri  请求地址
     * @return 是否放行
     */
    private boolean isPermitUri(String[] permitPaths, String requestUri) {
        for (String permitPath : permitPaths) {
            if (pathMatcher.match(permitPath, requestUri)) {
                return true;
            }
        }
        return false;
    }


}
