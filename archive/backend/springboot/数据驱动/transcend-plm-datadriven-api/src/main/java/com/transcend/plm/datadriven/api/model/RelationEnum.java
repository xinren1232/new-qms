package com.transcend.plm.datadriven.api.model;

import com.transsion.framework.enums.BaseEnum;

/**
 * 对象模型配置常量
 *
 * @author jie.luo1
 */
public enum RelationEnum implements BaseEnum<String> {


    /**
     *对象模型编码
     */
    MODEL_CODE("modelCode", "model_code", "对象模型编码"),
    /**
     *源对象BID
     */
    SOURCE_BID("sourceBid", "source_bid","源对象BID"),
    /**
     *对象数据DATABID
     */
    DATA_BID("dataBid", "data_bid","对象数据DATABID"),
    /**
     *空间bid
     */
    SPACE_BID("spaceBid", "space_bid","空间bid"),
    /**
     *空间bid
     */
    SPACE_APP_BID("spaceAppBid", "space_app_bid","空间bid"),
    /**
     *空间bid
     */
    MOUNT_SPACE_BID("mountSpaceBid", "mount_space_bid","空间bid"),
    /**
     *对象bid
     */
    BID("bid", "bid","对象bid"),
    /**
     *源对象数据BID
     */
    SOURCE_DATA_BID("sourceDataBid", "source_data_bid","源对象数据BID"),
    /**
     *是否草稿
     */
    DRAFT("draft", "draft","是否草稿"),
    /**
     *目标对象BID
     */
    TARGET_BID("targetBid", "target_bid","目标对象BID"),
    /**
     *目标对象数据BID
     */
    TARGET_DATA_BID("targetDataBid", "target_data_bid","目标对象数据BID"),
    /**
     *父对象bid
     */
    PARENT_BID("parentBid", "parent_bid","父对象bid"),
    /**
     *关联行为
     */
    REL_BEHAVIOR("relBehavior", "rel_behavior", "关联行为")
    ;
    /**
     *code
     */
    String code;
    /**
     *column
     */
    String column;
    /**
     * desc
     */
    String desc;


    /**
     * @param code
     * @param column
     * @param desc
     */
    RelationEnum(String code, String column, String desc) {
        this.code = code;
        this.column = column;
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
    public String getColumn() {
        return this.column;
    }

    /**
     * @return {@link String }
     */
    @Override
    public String getDesc() {
        return this.desc;
    }

}
