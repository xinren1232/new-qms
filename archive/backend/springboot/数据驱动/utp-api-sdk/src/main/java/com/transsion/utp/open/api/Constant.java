package com.transsion.utp.open.api;

/**
 * 常量
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version v1.0
 * @date 2025/5/21
 */
public interface Constant {
    /**
     * 访问凭证请求头key
     */
    String HEADER_ACCESS_TOKEN_KEY = "UTP-TOKEN";


    /**
     * 模块类型标识
     */
    interface ComponentType {
        /**
         * 领域
         */
        String GROUP = "group";
        /**
         * 子领域
         */
        String SUB_GROUP = "subGroup";
        /**
         * 模块
         */
        String COMPONENT = "component";
        /**
         * 子模块
         */
        String SUB_COMPONENT = "subComponent";
    }
}
