package com.transcend.plm.datadriven.apm.enums;

import com.transsion.framework.enums.BaseEnum;

/**
 * @author unknown
 */
public enum CommonEnum implements BaseEnum<String> {
    /**
     * 责任人
     */
    PER_ROLE_CODE("personResponsible", "责任人"),
    REPETITIVE_DEMAND("repetitiveDemand", "重复需求"),
    Y("Y", "是"),
    /**
     * 草稿状态
     */
    DRAFT("DRAFT", "草稿状态"),
    /*TASK_MODEL_CODE("A28", "任务"),
    PLAN_TASK_MODEL_CODE("A39", "计划任务"),
    SSRMP_CODE("A2S", "战略供应商关系流程")*/
    ;
    /**
     * code
     */
    String code;
    /**
     *desc
     */
    String desc;

    CommonEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDesc() {
        return this.desc;
    }
}
