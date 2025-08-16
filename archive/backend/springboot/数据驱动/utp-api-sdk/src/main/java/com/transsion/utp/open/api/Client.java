package com.transsion.utp.open.api;

/**
 * 调用客户端
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version v1.0
 * @date 2025/5/21
 */
public interface Client {

    /**
     * 同步调用方法
     *
     * @param request 请求信息
     * @param <T>     响应类型
     * @return 响应结果
     */
    <T extends Response> T syncInvoke(Request<T> request);


}
