package com.transcend.plm.datadriven.api.model;

import com.transsion.framework.enums.BaseEnum;

/**
 * 对象模型配置常量
 *
 * @author jie.luo1
 * @date 2024/07/24
 */
public enum RelationObjectEnum implements BaseEnum<String> {

    /**
     *对象模型编码
     */
    MODEL_CODE("modelCode", "对象模型编码"),
    /**
     *源BID
     */
    SOURCE_BID("sourceBid", "源BID"),
    /**
     *目标BID
     */
    TARGET_BID("targetBid", "目标BID"),
    /**
     *源数据BID
     */
    SOURCE_DATA_BID("sourceDataBid", "源数据BID"),
    /**
     *目标数据BID
     */
    TARGET_DATA_BID("targetDataBid", "目标数据BID"),
    /**
     *是否草稿
     */
    DRAFT("draft", "是否草稿"),
    /**
     *权限BID
     */
    PERMISSION_BID("permissionBid", "权限BID"),
    /**
     *关联行为
     */
    REL_BEHAVIOR("relBehavior", "关联行为")
    ;
    /**
     *code
     */
    String code;
    /**
     *desc
     */
    String desc;

    /**
     * @param code
     * @param desc
     */
    RelationObjectEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * @return {@link String }
     */
    @Override
    public String getCode() {
        return this.code;
    }

    /**
     * @return {@link String }
     */
    @Override
    public String getDesc() {
        return this.desc;
    }
}
