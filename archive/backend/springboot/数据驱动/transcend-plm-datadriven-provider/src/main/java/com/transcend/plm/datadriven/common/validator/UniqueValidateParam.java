package com.transcend.plm.datadriven.common.validator;

import lombok.Builder;
import lombok.Data;

/**
 * @Program transcend-plm-datadriven
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-03-22 11:44
 **/
@Data
@Builder
public class UniqueValidateParam {

    /**
     * 表名称
     */
    String tableName;

    /**
     * 列名称
     */
    String columnName;

    /**
     * 被参数校验时候的字段的值
     */
    Object value;

    /**
     * 校验时，是否排除当前的记录
     */
    Boolean excludeCurrentRecord;

    /**
     * 主键id的字段名
     */
    String idFieldName;

    /**
     * 当前记录的主键id
     */
    String id;

    /**
     * 排除所有被逻辑删除的记录的控制
     */
    Boolean excludeLogicDeleteItems;

    /**
     * 逻辑删除的字段名
     */
    String logicDeleteFieldName;

    /**
     * 逻辑删除的字段的值
     */
    boolean logicDeleteValue;

}
