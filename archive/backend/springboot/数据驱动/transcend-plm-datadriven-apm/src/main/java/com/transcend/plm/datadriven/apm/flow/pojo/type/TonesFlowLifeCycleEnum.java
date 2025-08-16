package com.transcend.plm.datadriven.apm.flow.pojo.type;

/**
 * Tones生命周期
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2024/9/10 10:19
 */
public enum TonesFlowLifeCycleEnum {

    /**
     * 需求分析
     */
    REQUIREMENTANALYSIS,
    /**
     * 设计
     */
    DOMAINVALUEASSESSMENT,
    /**
     * 完成
     */
    COMPLETED,
    /**
     * 计划中
     */
    INPLANING,
    /**
     * 验证中
     */
    VERIFIED,
    /**
     * 已失效
     */
    INVALID,
    /**
     * 已拒绝
     */
    BUG_REJECTED,
    /**
     * 验证不通过
     */
    VERIFY_REJECTED,
}
