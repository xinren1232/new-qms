package com.transcend.plm.datadriven.notify.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Data
public class NotifyTriggerRuleVo {
    private Integer id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 通知触发表业务id
     */
    private String notifyConfigTriggerBid;

    /**
     * 对象属性编码
     */
    private String attrCode;

    /**
     * 规则类型：1.触发规则，2.通知规则
     */
    private Integer ruleType;

    /**
     * 对象属性数据库字段
     */
    private String attrDbKey;

    /**
     * 比较条件：contain.包含，notContain.不包含，equal.等于，notEqual.不等于，null.为空，notNull.不为空，gt.大于，lt.小于，notGt.小于等于，notLt.大于等于
     */
    private String relationship;

    /**
     * 比较条件值
     */
    private String relationValue;

    /**
     * 比较条件值类型，STRING.字符串，NUMBER.数字，DATE.日期，NOW.当天时间
     */
    private String relationValueType;

    /**
     * 字典类型
     */
    private String remoteDictType;

    /**
     * 规则（未来统一在此维护规则）
     */
    private Object triggerRuleInfo;
    /**
     * 比较条件值，now_time:与当前时间比较时间值 -2（天 单位不一定）：当前时间减去两天 +2当前时间加两天
     */
    private String comparisonValue;

    /**
     * 启用标志，0未启用，1启用，2禁用
     */
    private Boolean enableFlag;

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
     * 说明
     */
    private String description;
}
