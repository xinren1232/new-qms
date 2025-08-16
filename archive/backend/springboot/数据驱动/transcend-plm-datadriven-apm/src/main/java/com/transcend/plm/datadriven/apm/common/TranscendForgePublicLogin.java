package com.transcend.plm.datadriven.apm.common;

import com.transsion.framework.constant.SysGlobalConst;
import com.transsion.framework.core.context.ServletRequestContextHolder;
import com.transsion.framework.sdk.core.config.CredentialsProperties;
import com.transsion.framework.uac.model.dto.LoginUserDTO;
import com.transsion.framework.uac.model.dto.RSAKeyPairDTO;
import com.transsion.framework.uac.model.request.LoginRequest;
import com.transsion.framework.uac.service.IUacLoginService;
import com.transsion.sdk.core.utils.RSAUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;
import java.util.function.Supplier;

/**
 * 伪造公共登录
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/6/3 09:56
 */
@Component
public class TranscendForgePublicLogin {
    private static final String USERNAME = "IPMPublicAccount";
    private static final String PASSWORD = "IPMPublicAccount";

    private static TranscendForgePublicLogin instance;

    @Resource
    private IUacLoginService iUacLoginService;
    @Resource
    private CredentialsProperties credentialsProperties;
    @Value("${spring.application.sys-code}")
    private String sysCode;

    private TranscendForgePublicLogin() {
        TranscendForgePublicLogin.instance = this;
    }

    /**
     * 通过静态方法获取实例
     *
     * @return 实例
     */
    public static TranscendForgePublicLogin getInstance() {
        return TranscendForgePublicLogin.instance;
    }

    /**
     * 执行登录操作
     *
     * @return 登录用户信息
     * @throws Exception 登录异常
     */
    public LoginUserDTO login() throws Exception {
        RSAKeyPairDTO rsaKeyPair = iUacLoginService.getRsaKeyPair().getData();
        String pwd = RSAUtils.encryptByPublicKey(PASSWORD, rsaKeyPair.getPublicKey(), StandardCharsets.UTF_8.name());
        LoginRequest loginRequest = LoginRequest.builder().username(USERNAME).pwd(
                pwd).appId(credentialsProperties.getAppKey()).verifyKey(rsaKeyPair.getVerifyKey()).build();
        return iUacLoginService.loginByAccount(loginRequest).getData();
    }

    /**
     * 伪造传递Header请求
     *
     * @param loginUser 登录用户信息
     * @return 请求头传递对象
     */
    @NotNull
    public HeaderPassRequest forgeRequest(@Nonnull LoginUserDTO loginUser) {
        HeaderPassRequest request = new HeaderPassRequest();
        request.addHeader(SysGlobalConst.HTTP_HEADER_X_APP_ID, credentialsProperties.getAppKey());
        request.addHeader(SysGlobalConst.HTTP_HEADER_X_AUTH, loginUser.getRtoken());
        request.addHeader(SysGlobalConst.HTTP_HEADER_X_RTOKEN, loginUser.getUtoken());
        request.addHeader(SysGlobalConst.HTTP_HEADER_X_EMP_NO, loginUser.getEmployeeNo());
        request.addHeader(SysGlobalConst.HTTP_HEADER_X_SYS_CODE, sysCode);
        return request;
    }

    /**
     * 伪造请求头到到ServletRequestContextHolder中
     * 并执行业务方法
     * 执行完成或者报错则清空ServletRequestContextHolder
     * <p>
     * 注意：ServletRequestContextHolder中已有请求时，则不会进行伪造或者清空
     *
     * @param supplier 伪造请求后需要执行的逻辑方法
     */
    public static <T> T forgeRequestExecute(Supplier<T> supplier) throws Exception {
        boolean clear = false;
        try {
            HttpServletRequest context = ServletRequestContextHolder.getContext();
            if (context == null) {
                LoginUserDTO login = TranscendForgePublicLogin.getInstance().login();
                HeaderPassRequest request = TranscendForgePublicLogin.getInstance().forgeRequest(login);
                ServletRequestContextHolder.setContext(request);
                clear = true;
            }
            return supplier.get();
        } finally {
            if (clear) {
                ServletRequestContextHolder.removeContext();
            }
        }
    }

    /**
     * 获取请求头参数值的方法
     * ps：请求头可能被转换为小写
     *
     * @param headers    请求头信息
     * @param headerName 请求头名称
     * @return 请求头参数值
     */
    public static String getHeaderValue(Map<String, String> headers, String headerName) {
        return Optional.ofNullable(headers.get(headerName)).orElseGet(() -> headers.get(headerName.toLowerCase()));
    }


    /**
     * 请求头传递对象
     */
    @SuppressWarnings("all")
    public static class HeaderPassRequest implements HttpServletRequest {

        private final Map<String, List<String>> headers = new HashMap<>();

        public void addHeader(String name, String value) {
            headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
        }

        // 获取单个请求头信息
        @Override
        public String getHeader(String name) {
            List<String> values = headers.get(name);
            if (values != null && !values.isEmpty()) {
                return values.get(0);
            }
            return null;
        }

        // 获取所有请求头名称
        @Override
        public Enumeration<String> getHeaderNames() {
            return Collections.enumeration(headers.keySet());
        }

        // 获取指定请求头的所有值
        @Override
        public Enumeration<String> getHeaders(String name) {
            List<String> values = headers.get(name);
            if (values != null) {
                return Collections.enumeration(values);
            }
            return Collections.emptyEnumeration();
        }

        // 未实现的方法抛出未实现异常
        @Override
        public String getParameter(String name) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public Enumeration<String> getParameterNames() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String[] getParameterValues(String name) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getAuthType() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public Cookie[] getCookies() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public long getDateHeader(String name) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public int getIntHeader(String name) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getMethod() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getPathInfo() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getPathTranslated() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getContextPath() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getQueryString() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getRemoteUser() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public boolean isUserInRole(String role) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public Principal getUserPrincipal() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getRequestedSessionId() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getRequestURI() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public StringBuffer getRequestURL() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getServletPath() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public HttpSession getSession(boolean create) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public HttpSession getSession() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String changeSessionId() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public boolean isRequestedSessionIdValid() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public boolean isRequestedSessionIdFromCookie() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public boolean isRequestedSessionIdFromURL() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public boolean isRequestedSessionIdFromUrl() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
            return false;
        }

        @Override
        public void login(String username, String password) throws ServletException {

        }

        @Override
        public void logout() throws ServletException {

        }

        @Override
        public Collection<Part> getParts() throws IOException, ServletException {
            return Collections.emptyList();
        }

        @Override
        public Part getPart(String name) throws IOException, ServletException {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public Object getAttribute(String name) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getCharacterEncoding() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public int getContentLength() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public long getContentLengthLong() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getContentType() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getProtocol() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getScheme() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getServerName() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public int getServerPort() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public BufferedReader getReader() throws IOException {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getRemoteAddr() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getRemoteHost() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void setAttribute(String name, Object o) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void removeAttribute(String name) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public Locale getLocale() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public Enumeration<Locale> getLocales() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public boolean isSecure() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String path) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getRealPath(String path) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public int getRemotePort() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getLocalName() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getLocalAddr() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public int getLocalPort() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public ServletContext getServletContext() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public AsyncContext startAsync() throws IllegalStateException {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public boolean isAsyncStarted() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public boolean isAsyncSupported() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public AsyncContext getAsyncContext() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public DispatcherType getDispatcherType() {
            throw new UnsupportedOperationException("Not implemented.");
        }
    }

}
