package com.transcend.plm.datadriven.apm.flow.pojo.ao;

import lombok.Data;

import java.util.Date;

/**
 * @author unknown
 */
@Data
public class ApmFlowNodeDirectionConditionAO {
    /**
     * 业务id
     */
    private String bid;

    /**
     * 所属流程bid
     */
    private String flowTemplateBid;

    /**
     * 所属流程版本号
     */
    private String version;

    /**
     * 流程节点流转表业务bid
     */
    private String flowNodeDirectionBid;

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
     * 租户ID
     */
    private Long tenantId;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 删除标识
     */
    private Boolean deleteFlag;

    /**
     * 启用标志，0未启用，1启用，2禁用
     */
    private Boolean enableFlag;

    /**
     * 对象字段值类型，string.字符串，number.数字，date.日期，role.角色，now.日期当前时间。loginUser.当前登录人
     */
    private String filedValueType;
}
