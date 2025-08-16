package com.transcend.plm.datadriven.common.share;

/**
 * 内容提供者
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/26 14:12
 */
public interface ContentProvider<T> {

    /**
     * 获取内容
     *
     * @return 内容
     */
    T getContent();

}