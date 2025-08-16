package com.transcend.plm.datadriven.apm.interceptor;

import com.transcend.framework.core.exception.TranscendBizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description AppKey拦截器
 * @createTime 2023-11-08 10:14:00
 */
@Configuration
public class AppKeyInterceptor implements HandlerInterceptor {

    @Value("${app.secret:4c2a11ee324644c6a5f279ea12d09d25ba19ed11b31b4ef19c50486f9fdb3245}")
    private String appSecret;

    /**
     * 不需要校验的url
     */
    @Value("#{'${transcend.plm.not.check.urls:/api/app/pi/syncProduct/saveOrUpdate}'.split(',')}")
    private List<String> notCheckUrls;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String appSecret = request.getHeader("appSecret");
        String url = request.getRequestURI();
        if(notCheckUrls.contains(url)) {
            return HandlerInterceptor.super.preHandle(request, response, handler);
        }
        if (!this.appSecret.equals(appSecret)) {
            throw new TranscendBizException("appSecret is invalid");
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }
}
