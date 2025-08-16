package com.transcend.plm.datadriven.apm.flow.pojo.ao;

import lombok.Data;

/**
 * @author unknown
 */
@Data
public class ApmFlowNodeDisplayConditionAO {

    /**
     * 业务id
     */
    private String bid;


    /**
     * 对象字段名称
     */
    private String filedName;

    /**
     * 对象字段类型，string.字符串，number.数字，date.日期，role.角色
     */
    private String filedType;

    /**
     * 表字段名称
     */
    private String columnName;

    /**
     * 比较条件：contain.包含，notContain.不包含，equal.等于，notEqual.不等于，null.为空，notNull.不为空，gt.大于，lt.小于，notGt.小于等于，notLt.大于等于，between.在区间
     */
    private String relationship;

    /**
     * 条件比较值
     */
    private String filedValue;

    /**
     * 条件比较值类型，string.字符串，number.数字，date.日期，role.角色，now.日期当前时间。loginUser.当前登录人工号
     */
    private String filedValueType;
}
