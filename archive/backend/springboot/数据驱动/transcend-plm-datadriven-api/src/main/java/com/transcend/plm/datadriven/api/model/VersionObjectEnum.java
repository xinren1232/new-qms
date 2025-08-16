package com.transcend.plm.datadriven.api.model;

import com.transsion.framework.enums.BaseEnum;

/**
 * 对象版本模型配置常量
 *
 * @author jie.luo1
 * @date 2024/07/24
 */
public enum VersionObjectEnum implements BaseEnum<String> {

    /**
     * 数据BID
     */
    DATA_BID("dataBid", "数据BID"),
    /**
     *版本
     */
    VERSION("version", "版本"),
    /**
     *检出人
     */
    CHECKOUT_BY("checkoutBy", "检出人"),
    /**
     *检入描述
     */
    CHECKIN_DESCRIPTION("checkinDescription", "检入描述"),
    /**
     *检出时间
     */
    CHECKOUT_TIME("checkoutTime", "检出时间"),
    /**
     *修订版本
     */
    REVISION("revision", "修订版本");


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
    VersionObjectEnum(String code, String desc) {
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
