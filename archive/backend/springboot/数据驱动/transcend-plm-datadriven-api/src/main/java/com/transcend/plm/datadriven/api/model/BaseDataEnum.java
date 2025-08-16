package com.transcend.plm.datadriven.api.model;

import com.transsion.framework.enums.BaseEnum;

/**
 * 基础数据驱动-配置常量
 *
 * @author jie.luo1
 */
public enum BaseDataEnum implements BaseEnum<String> {

    /**
     *业务ID
     */
    BID("bid", "bid","业务ID"),
    /**
     *数据库唯一ID
     */
    ID("id", "id","数据库唯一ID"),
    /**
     *模型编码
     */
    MODEL_CODE("modelCode", "model_code","模型编码"),
    /**
     *创建人
     */
    CREATED_BY("createdBy", "created_by","创建人"),
    /**
     *更新人
     */
    UPDATED_BY("updatedBy", "updated_by","更新人"),
    /**
     *创建时间
     */
    CREATED_TIME("createdTime", "created_time","创建时间"),
    /**
     *更新时间
     */
    UPDATED_TIME("updatedTime", "updated_time","更新时间"),
    /**
     *启用状态
     */
    ENABLE_FLAG("enableFlag", "enable_flag","启用状态"),
    /**
     *删除状态
     */
    DELETE_FLAG("deleteFlag", "delete_flag","删除状态"),
    /**
     *租户ID
     */
    TENANT_ID("tenantId", "tenant_id","租户ID")
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
     *desc
     */
    String desc;

    /**
     * @param code
     * @param column
     * @param desc
     */
    BaseDataEnum(String code,String column, String desc) {
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
