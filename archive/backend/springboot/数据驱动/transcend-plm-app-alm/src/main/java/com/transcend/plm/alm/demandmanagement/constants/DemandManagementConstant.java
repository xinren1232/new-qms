package com.transcend.plm.alm.demandmanagement.constants;

/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description 需求管理常量
 * @date 2024/06/21 11:21
 **/
public class DemandManagementConstant {

    private DemandManagementConstant() {
    }

    /**
     * duplicateRequirementNumber
     */
    public static final String DEMAND_MANAGEMENT_REPEAT = "duplicateRequirementNumber";

    /**
     * lifeCycleCode
     */
    public static final String DEMAND_MANAGEMENT_LIFE_CYCLE = "lifeCycleCode";
    /**
     * requirementcoding
     */
    public static final String REQUIREMENT_CODING = "requirementcoding";
    /**
     * allFields
     */
    public static final String ALL_FIELDS = "allFields";

    /**
     * 规划中状态
     */
    public static final String STATE_INPLANING = "INPLANING";

    /**
     * 待过滤分发
     */
    public static final String STATE_REQUIREMENTANALYSIS = "REQUIREMENTANALYSIS";

    /**
     * 待价值评估
     */
    public static final String STATE_DOMAINVALUEASSESSMENT = "DOMAINVALUEASSESSMENT";
    /**
     * :
     */
    public static final String REDIS_DELIMITER = ":";
    /**
     * RR
     */
    public static final String RR_PREFIX = "RR";
    /**
     * demandmanagement:RR
     */
    public static final String RR_REDIS_KEY = "demandmanagement:RR";
    /**
     * SR
     */
    public static final String SR_PREFIX = "SR";

    public static final String IR_PREFIX = "IR";

    public static final String IR_REDIS_KEY = "demandmanagement:IR";

    public static final String DASH = "-";

    public static final String IM02_REDIS_KEY = "IM02_:FLOW";

    public static final String PROCESS_CODING = "processCoding";
    public static final String IM02_PREFIX = "IM02";
    public static final String REL_DEMAND_CODE = "relDemandCode";

    public static final String REL_DEMAND = "relDemand";

    public static final String RR_BID = "rrBid";

    public static final String RR_ORIGINAL_REQUIREMENT_NUMBER = "rrOriginalRequirementNumber";

    /**
     * 系统特性层级字段
     */
    public static final String SF_LEVEL = "level";
    /**
     * 系统特性层级字段
     */
    public static final String SF_FEATURE_SE = "featureSE";

    /**
     * 系统特性层级字段
     */
    public static final String SF_VERSION_NUMBER = "versionNumber";
}
