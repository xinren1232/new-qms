package com.transcend.plm.datadriven.apm.integration.message;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 实例操作类型枚举
 * @createTime 2023-12-20 11:45:00
 */
public enum InstanceOperateTypeEnum {
    /**
     *新增
     */
    ADD("add", "新增"),
    /**
     *修改
     */
    UPDATE("update", "修改"),
    /**
     *删除
     */
    DELETE("delete", "删除"),
    /**
     *批量新增
     */
    BATCH_ADD("batchAdd", "批量新增"),
    /**
     *批量修改
     */
    BATCH_UPDATE("batchUpdate", "批量修改"),
    /**
     *批量删除
     */
    BATCH_DELETE("batchDelete", "批量删除"),
    /**
     *批量新增关系
     */
    BATCH_ADD_RELATION("batchAddRelation", "批量新增关系"),
    /**
     *批量删除关系
     */
    BATCH_DELETE_RELATION("batchDeleteRelation", "批量删除关系"),
    /**
     *批量修改关系
     */
    BATCH_UPDATE_RELATION("batchUpdateRelation", "批量修改关系"),
    /**
     *批量新增关系
     */
    ADD_RELATION("addRelation", "批量新增关系"),
    /**
     *批量删除关系
     */
    DELETE_RELATION("deleteRelation", "批量删除关系"),
    /**
     *批量修改关系
     */
    UPDATE_RELATION("updateRelation", "批量修改关系"),
    /**
     *批量新增关系
     */
    BATCH_ADD_RELATION_DATA("batchAddRelationData", "批量新增关系"),
    /**
     *批量删除关系
     */
    BATCH_DELETE_RELATION_DATA("batchDeleteRelationData", "批量删除关系"),
    /**
     *批量修改关系
     */
    BATCH_UPDATE_RELATION_DATA("batchUpdateRelationData", "批量修改关系"),
    /**
     *批量新增关系
     */
    ADD_RELATION_DATA("addRelationData", "批量新增关系"),
    /**
     *批量删除关系
     */
    DELETE_RELATION_DATA("deleteRelationData", "批量删除关系"),
    /**
     *批量修改关系
     */
    UPDATE_RELATION_DATA("updateRelationData", "批量修改关系"),
    /**
     *批量新增关系
     */
    BATCH_ADD_RELATION_DATA_BY_DATA_BID("batchAddRelationDataByDataBid", "批量新增关系");

    /**
     * code
     */
    private final String code;
    /**
     * description
     */
    private final String description;

    InstanceOperateTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
    /**
     * 查看code是否存在
     */
    public static boolean contains(String code) {
        for (InstanceOperateTypeEnum e : InstanceOperateTypeEnum.values()) {
            if (e.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
}

