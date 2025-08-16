package com.transcend.plm.alm.demandmanagement.enums;

import com.transsion.framework.enums.BaseEnum;
import lombok.Getter;

/**
 * @author bin.yin
 * @description: 需求管理字段枚举
 * @version:
 * @date 2024/06/21 13:42
 */
public enum DemandManagementEnum implements BaseEnum<String> {
    /** RR需求bid **/
    /**
     *RR需求bid
     */
    RR_BID("rrBid", "rr_bid", "RR需求bid"),
    /**
     *领域责任人
     */
    DOMAIN_LEADER("domainLeader", "domain_leader", "领域责任人"),
    /**
     *领域bid
     */
    DOMAIN_BID("domainBid", "domain_bid", "领域bid"),
    /**
     *领域SE
     */
    DOMAIN_SE("domainSe", "domain_se", "领域SE"),
    /**
     *领域应用bid
     */
    DOMAIN_APP_BID("domainAppBid", "domain_app_bid", "领域应用bid"),
    RR_DOMAIN_FLAG("1", "RR_DOMAIN_FLAG", "RR领域处理历史数据"),
    IR_DOMAIN_FLAG("2", "IR_DOMAIN_FLAG", "IR领域处理历史数据"),

    RR_MODULE_FLAG("3", "RR_MODULE_FLAG", "RR模块处理历史数据"),
    IR_MODULE_FLAG("4", "IR_MODULE_FLAG", "IR模块处理历史数据"),
    SR_MODULE_FLAG("5", "SR_MODULE_FLAG", "SR模块处理历史数据"),
    AR_MODULE_FLAG("6", "AR_MODULE_FLAG", "AR模块处理历史数据"),

    IR_OSVERSION_FLAG("7", "IR_OSVERSION_FLAG", "IR os版本处理历史数据"),
    AR_DOMAIN_FLAG("8", "AR_OSVERSION_FLAG", "AR 领域处理历史数据"),
    SR_DOMAIN_FLAG("9", "SR_OSVERSION_FLAG", "SR 领域处理历史数据"),
    /**
     *领域模型编码
     */
    DOMAIN_MODEL_CODE("domainModelCode", "domain_model_code", "领域模型编码"),
    /**
     *是否主导领域
     */
    IS_LEAD_DOMAIN("isleadDomain", "islead_domain", "是否主导领域"),
    /**
     *关联需求JSON
     */
    REL_DEMAND("relDemand", "rel_demand", "关联需求JSON"),
    /**
     *子领域bid
     */
    SUB_DOMAIN("subdomain", "subdomain", "子领域bid"),
    /**
     *父需求bid
     */
    PARENT_BID("parentBid", "parent_bid", "父需求bid"),
    /**
     *领域
     */

    PRODUCT_AREA("productArea", "product_area", "领域"),

    /**
     *主导总领域
     */

    LEADING_OVERALL_FIELD("leadingOverallField", "leading_overall_field", "主导总领域"),
    /**
     *所有总领域
     */

    ALL_OVERALL_FIELDS("allOverallFields", "all_overall_fields", "所有总领域"),
    /**
     *所有领域
     */

    ALL_FIELDS("allFields", "all_fields", "所有领域"),
    /**
     *产品负责人
     */

    PRODUCT_MANAGER("productManager", "product_manager", "产品负责人"),
    /**
     *模块
     */
    BELONG_MODULE("belongModule", "belong_module", "模块"),
    // 2024.7.24 修改替换模块负责人字段
//    PERSON_RESPONSIBLE("personResponsible", "personResponsible", "模块负责人"),
    /**
     *模块负责人
     */
    PERSON_RESPONSIBLE("moduleResponsiblePerson", "module_responsible_person", "模块负责人"),
    /**
     *主导领域
     */
    ONWENR("onwenr", "onwenr", "主导领域"),
    /**
     *主导领域
     */
    DOMINANT_DOMAIN("dominantDomain", "dominant_domain", "主导领域"),

    /**
     *领域对象
     */
    DOMAIN_RESPONSIBLE("productManager", "product_manager", "领域对象 责任人字段"),
    /**
     *责任人字段
     */
    APP_RESPONSIBLE("appManager", "app_manager", "应用对象 责任人字段"),
    /**
     *模块对象
     */
    MODULE_RESPONSIBLE("personResponsible", "person_responsible", "模块对象 责任人字段"),
    ;

    /**
     *code
     */
    final String code;
    /**
     *column
     */
    @Getter
    final String column;
    /**
     *desc
     */
    final String desc;

    DemandManagementEnum(String code, String column, String desc) {
        this.code = code;
        this.column = column;
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
