package com.transcend.plm.configcenter.common.util;

/**
 *  bean相关常量
 */
public class BeanConstant {

    /**
     * insert操作，bean copy时需要忽略的字段
     *
     * @version: 1.0
     * @date: 2020/8/28 11:17
     * @author: huan.zhou
     */
    public static final String[] INSERT_IGNORE_FIELD = {"id", "createdTime", "createdBy", "updatedTime", "updatedBy", "deleteFlag", "bid"};
    /**
     * update操作，bean copy 时需要忽略的字段
     *
     * @version: 1.0
     * @date: 2020/8/28 11:15
     * @author: huan.zhou
     */
    public static final String[] UPDATE_IGNORE_FIELD = {"id", "createdTime", "createdBy", "updatedTime", "updatedBy", "deleteFlag"};

}
