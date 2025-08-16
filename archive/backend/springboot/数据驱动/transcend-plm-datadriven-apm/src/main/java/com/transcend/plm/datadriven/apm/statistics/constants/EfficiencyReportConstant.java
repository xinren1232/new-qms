package com.transcend.plm.datadriven.apm.statistics.constants;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description 研发能效报表枚举
 * @date 2023/10/25 16:43
 **/
public class EfficiencyReportConstant {
    /**
     * 按照工时统计图
     */
    public static final String BURN_DOWN_STATISTIC_TYPE_WORK_HOURS = "workHours";
    /**
     * 按照用户故事完成数量统计图
     */
    public static final String BURN_DOWN_STATISTIC_TYPE_COUNT = "count";

    /**
     * 用户故事modelCode
     */
//    public static final String MODEL_CODE_USER_DEMAND = "A27A02";

    public static final String RETURN_TITLE = "title";

    /**
     * 图表生命周期转换
     */
    public static ImmutableMap<String,String> REPORT_LIFE_CODE_NAME = ImmutableMap.of("CLOSED","已完成","PUBLISHED","已完成",
            "DELIVERY_IN_PROGRESS","进行中","TO_BE_ACCEPTED","进行中","UNDER_DEVELOP","进行中","TO_BE_TESTED","进行中",
            "UNDER_TESTING", "进行中","underway","进行中","completed","已完成");

    /**
     * 图表modelCode转换
     */
    public static Map<String,String> MODEL_TYPE_MAP = ImmutableMap.of("A27A00","史诗","A27A01","特性","A27A02","用户故事","A28","任务");


    /**
     * 迭代燃尽图报表常量
     * @description 研发能效报表常量
     */
    public class RevisionBurnDownConstant{
        public static final String BIZ_TYPE = "revisionBurnDown";
        public static final String START_TIME = "startTime";
        public static final String END_TIME = "endTime";
        public static final String PLAN_COMPLETE_TIME = "plannedCompletionTime";
        public static final String ACTUAL_COMPLETE_TIME = "releaseDate";
        public static final String CALCULATE_START_TIME = "calculateStartTime";
        public static final String SCHEDULE_START_TIME = "scheduleStartTime";
        public static final String PLAN_WORKING_HOURS = "workingHours";
        public static final String DEVELOPMENT_START_TIME = "developmentStartTime";
        public static final String TEST_START_TIME = "testStartTime";
    }

    /**
     * 工作状态分布图标常量类
     * @description 研发能效报表常量
     */
    public class DemandDistributionChartConstant{
        public static final String STATISTIC_RANGE_PROJECT = "project";
        public static final String STATISTIC_RANGE_DEMAND_MANAGER = "demandManager";
        public static final String STATISTIC_RANGE_REVISION = "revision";
        public static final String LIFE_CODE_NAME = "reportStatus";

        public static final String LIFE_UN_START = "未开始";
        public static final String LIFE_PROCESSING = "进行中";
        public static final String LIFE_COMPLETED = "已完成";
    }

    /**
     * 团队工作状态分布图标常量类
     * @description 研发能效报表常量
     */
    public class MemberDemandDistributionChartConstant{
        public static final String PERSON_RESPONSIBLE = "personResponsible";
        public static final String PERSON_IN_CHARGE_OF_TESTING = "personInChargeOfTesting";
        public static final String DEVELOPMENT_RESPONSIBLE = "developmentResponsible";
        public static final String PRODUCT_OWNER = "productOwner";
    }

    /**
     * PLM的燃尽图常量类
     * @description 研发能效报表常量
     */
    public class PlmBurnoutChartConstant {
        /**
         * 关闭状态的字典编码
         */
        public static final String CLOSE_LIFE_CYCLE_CODE = "CLOSE_LIFE_CYCLE_CODE";

        /**
         * 进行中状态的编码
         */
        public static final String UNDERWAY_LIFE_CYCLE_CODE ="UNDERWAY";

        /**
         * 迭代需求燃尽图字段
         */
        public static final String VISION_DEMAND_BURNOUT_ATTR = "VISION_DEMAND_BURNOUT_ATTR";

        /**
         * 迭代任务燃尽图字段
         */
        public static final String VISION_TASK_BURNOUT_ATTR = "VISION_TASK_BURNOUT_ATTR";

        /**
         * 计划结束日期
         */
        public static final String PLAN_END_DATE = "PLAN_END_DATE";

        /**
         * 计划开始日期
         */
        public static final String PLAN_START_DATE = "PLAN_START_DATE";

        /**
         * 实际开始日期
         */
        public static final String ACTUAL_START_DATE = "ACTUAL_START_DATE";

        /**
         * 实际结束日期
         */
        public static final String ACTUAL_END_DATE = "ACTUAL_END_DATE";

        /**
         * 任务预估工时
         */
        public static final String ESTIMATED_WORK_HOUR = "ESTIMATED_WORK_HOUR";

    }

}
