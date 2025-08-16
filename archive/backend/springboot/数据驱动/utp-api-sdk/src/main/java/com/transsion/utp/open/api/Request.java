package com.transsion.utp.open.api;

import java.io.Serializable;

/**
 * 请求对象
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version v1.0
 * @date 2025/5/21
 */
public interface Request<T extends Response> extends Serializable {

    /**
     * 请求方式
     *
     * @return 请求方式
     */
    HttpMethod httpMethod();

    /**
     * 获取接口地址
     *
     * @return 接口地址
     */
    String uri();

    /**
     * 响应参数类型
     *
     * @return 响应参数类型
     */
    Class<T> responseClass();

}
