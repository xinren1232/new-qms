package com.transcend.plm.datadriven.apm.constants;

import com.transcend.plm.datadriven.apm.space.service.impl.DefaultAppExcelTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author sgx
 * @version V1.0
 * @Title:
 * @Package
 * @Description: 对象模型编码，可以从字典和配置文件中加载；
 * * 加载顺序为1.默认值 2.配置文件 3.字典，
 * * 最终值和加载顺序有关，加载顺序越靠后优先级越高
 * @date 说明：
 * 修改配置后需要重启服务，才能生效
 * 新增 示例：
 * 1.只在读取代码定义，不从配置文件中读取
 * public static String PROJECT_MODEL_CODE = "A00";
 * 2.可以从Apollo配置中心读取,不从字典中读取,添加@Value注解方法
 * @Value("${transcend.plm.apm.modelCode.project:}") public void setProjectModelCode(String modelCode) {
 * if (StringUtils.isNotBlank(modelCode)){
 * PROJECT_MODEL_CODE = modelCode;
 * }
 * <p>
 * }
 * 注意添加冒号，否则会报错,构造方法不为static
 * 3.可以从字典中读取，在init方法中添加 取值代码
 * if (StringUtils.isNotBlank(modelItemMap.get("PROJECT_MODEL_CODE"))){
 * PROJECT_MODEL_CODE = modelItemMap.get("PROJECT_MODEL_CODE");
 * }
 * <p>
 * 注意判断是否为空，否则会覆盖默认值
 * <p>
 * 建议常规对象为XXX_MODEL_CODE，关系对象为XXX_XXX_REL_MODEL_CODE
 * 配置文件中的key为transcend.plm.apm.modelCode加上驼峰命名的XXX
 */

@Configuration
@Slf4j
public class ModelCodeProperties {

    public static final String MODEL_CODE_DICT = "MODEL_CODE_DICT";

    @Resource
    private DefaultAppExcelTemplateService defaultAppExcelTemplateService;

    /**
     * 项目模型编码
     */
    public static String PROJECT_MODEL_CODE = "A00";

    /**
     * 产品需求模型编码
     */
    public static String DEMAND_MODEL_CODE = "A27";

    /**
     * 项目需求关联模型编码
     */
    public static String PROJECT_DEMAND_REL_MODEL_CODE = "A2E";

    /**
     * 迭代模型编码
     */
    public static String REVISION_MODEL_CODE = "A1K";

    /**
     * 迭代-产品需求关系模型编码
     */
    public static String REVISION_DEMAND_REL_MODEL_CODE = "A2G";

    /**
     * 任务模型编码
     */
    public static String TASK_MODEL_CODE = "A28";

    /**
     * 迭代-任务关系模型编码
     */
    public static String REVISION_TASK_REL_MODEL_CODE = "A2H";

    /**
     * 计划任务模型编码
     */
    public static String PLAN_TASK_MODEL_CODE = "A39";

    /**
     * 战略供应商关系流程模型编码
     */
    public static String SSRMP_MODEL_CODE = "A2S";

    /**
     * 空间code模型编码
     */
    public static String SPACE_MODEL_CODE = "100";

    /**
     * 空间项目的 模板bid
     */
    public static String SPACE_TEMPLATE_BID = "templateBid";

    /**
     * 项目和任务关系模型编码
     */
    public static String PROJECT_TASK_REL_MODEL_CODE = "A2D";


    /**
     * 故事和任务关系模型编码
     */
    public static String STORY_TASK_REL_MODEL_CODE = "A2M";

    /**
     * 业务模块模型编码
     */
    public static String BUS_MODLUE_MODEL_CODE = "A07";

    /**
     * 项目和任务关系模型编码
     */
    public static String PROJECT_REVISION_REL_MODEL_CODE = "A1M";

    /**
     * KPI考核模型编码
     */
    public static String KPI_INFO_MODEL_CODE = "A4Y";

    /**
     * 产品需求下的史诗模型编码
     */
    public static String DEMAND_EPIC_MODEL_CODE = "A27A00";

    /**
     * 产品需求下的特性模型编码
     */
    public static String DEMAND_SPECIFIC_MODEL_CODE = "A27A01";

    /**
     * 产品需求下的故事模型编码
     */
    public static String DEMAND_STORY_MODEL_CODE = "A27A02";


    /**
     * 成员模型编码
     */
    public static String MEMBERSOFT_MODEL_CODE = "A3E";

    /**
     * KPI_进度明细模型编码
     */
    public static String KPI_SCHEDULE_MODEL_CODE = "A4Z";

    /**
     * KPI_质量明细模型编码
     */
    public static String KPI_QUALITY_MODEL_CODE = "A50";

    /**
     * KPI_行为明细模型编码
     */
    public static String KPI_BEHAVIOR_MODEL_CODE = "A51";

    /**
     * KPI_价值明细模型编码
     */
    public static String KPI_VALUE_P_MODEL_CODE = "A52";

    /**
     * KPI考核_进度关系
     */
    public static String KPI_INFO_SCHEDULE_REL_MODEL_CODE = "A53";


    /**
     * KPI考核_质量关系
     */
    public static String KPI_INFO_QUALITY_REL_MODEL_CODE = "A54";

    /**
     * KPI考核_行为关系
     */
    public static String KPI_INFO_BEHAVIOR_REL_MODEL_CODE = "A55";

    /**
     * KPI考核_价值关系
     */
    public static String KPI_INFO_VALUE_REL_MODEL_CODE = "A56";

    /**
     * 成员模型编码
     */
    @Value("${transcend.plm.apm.modelCode.membersoft:}")
    public void setMembersoftModelCode(String modelCode) {
        if (StringUtils.isNotBlank(modelCode)) {
            MEMBERSOFT_MODEL_CODE = modelCode;
        }
    }

    /**
     * KPI_进度明细模型编码
     */
    @Value("${transcend.plm.apm.modelCode.kpiSchedule:}")
    public void setKpiScheduleModelCode(String modelCode) {
        if (StringUtils.isNotBlank(modelCode)) {
            KPI_SCHEDULE_MODEL_CODE = modelCode;
        }
    }

    /**
     * KPI_质量明细模型编码
     */
    @Value("${transcend.plm.apm.modelCode.kpiQuality:}")
    public void setKpiQualityModelCode(String modelCode) {
        if (StringUtils.isNotBlank(modelCode)) {
            KPI_QUALITY_MODEL_CODE = modelCode;
        }
    }

    /**
     * KPI_行为明细模型编码
     */
    @Value("${transcend.plm.apm.modelCode.kpiBehavior:}")
    public void setKpiBehaviorModelCode(String modelCode) {
        if (StringUtils.isNotBlank(modelCode)) {
            KPI_BEHAVIOR_MODEL_CODE = modelCode;
        }
    }

    /**
     * KPI_价值明细模型编码
     */
    @Value("${transcend.plm.apm.modelCode.kpiValueP:}")
    public void setKpiValuePModelCode(String modelCode) {
        if (StringUtils.isNotBlank(modelCode)) {
            KPI_VALUE_P_MODEL_CODE = modelCode;
        }
    }

    /**
     * KPI考核_进度关系
     */
    @Value("${transcend.plm.apm.modelCode.kpiInfoScheduleRel:}")
    public void setKpiInfoScheduleRelModelCode(String modelCode) {
        if (StringUtils.isNotBlank(modelCode)) {
            KPI_INFO_SCHEDULE_REL_MODEL_CODE = modelCode;
        }
    }

    /**
     * KPI考核_质量关系
     */
    @Value("${transcend.plm.apm.modelCode.kpiInfQualityRel:}")
    public void setKpiInfoQualityRelModelCode(String modelCode) {
        if (StringUtils.isNotBlank(modelCode)) {
            KPI_INFO_QUALITY_REL_MODEL_CODE = modelCode;
        }
    }

    /**
     * KPI考核_行为关系
     */
    @Value("${transcend.plm.apm.modelCode.kpiInfoBehaviorRel:}")
    public void setKpiInfoBehaviorRelModelCode(String modelCode) {
        if (StringUtils.isNotBlank(modelCode)) {
            KPI_INFO_BEHAVIOR_REL_MODEL_CODE = modelCode;
        }
    }

    /**
     * KPI考核_价值关系
     */
    @Value("${transcend.plm.apm.modelCode.kpiInfoValueRel:}")
    public void setKpiInfoValueRelModelCode(String modelCode) {
        if (StringUtils.isNotBlank(modelCode)) {
            KPI_INFO_VALUE_REL_MODEL_CODE = modelCode;
        }
    }

    /**
     * 产品需求下的史诗模型编码
     */
    @Value("${transcend.plm.apm.modelCode.demandEpic:}")
    public void setDemandEpicModelCode(String modelCode) {
        if (StringUtils.isNotBlank(modelCode)) {
            DEMAND_EPIC_MODEL_CODE = modelCode;
        }
    }

    /**
     * 产品需求下的特性模型编码
     */
    @Value("${transcend.plm.apm.modelCode.demandSpecific:}")
    public void setDemandSpecificModelCode(String modelCode) {
        if (StringUtils.isNotBlank(modelCode)) {
            DEMAND_SPECIFIC_MODEL_CODE = modelCode;
        }
    }

    /**
     * 产品需求下的故事模型编码
     */
    @Value("${transcend.plm.apm.modelCode.demandStory:}")
    public void setDemandStoryModelCode(String modelCode) {
        if (StringUtils.isNotBlank(modelCode)) {
            DEMAND_STORY_MODEL_CODE = modelCode;
        }
    }

    /**
     * KPI考核模型编码
     */
    @Value("${transcend.plm.apm.modelCode.kpiInfo:}")
    public void setKpiInfoModelCode(String modelCode) {
        if (StringUtils.isNotBlank(modelCode)) {
            KPI_INFO_MODEL_CODE = modelCode;
        }
    }

    /**
     * 业务模块模型编码
     */
    @Value("${transcend.plm.apm.modelCode.busModlue:}")
    public void setBusModlueModelCode(String modelCode) {
        if (StringUtils.isNotBlank(modelCode)) {
            BUS_MODLUE_MODEL_CODE = modelCode;
        }
    }

    /**
     * 项目和任务关系模型编码
     */
    @Value("${transcend.plm.apm.modelCode.projectRevisionRel:}")
    public void setProjectRevisionRelModelCode(String modelCode) {
        if (StringUtils.isNotBlank(modelCode)) {
            PROJECT_REVISION_REL_MODEL_CODE = modelCode;
        }
    }

    /**
     * 故事和任务关系模型编码
     */
    @Value("${transcend.plm.apm.modelCode.storyTaskRel:}")
    public void setStoryTaskRelModelCode(String modelCode) {
        if (StringUtils.isNotBlank(modelCode)) {
            STORY_TASK_REL_MODEL_CODE = modelCode;
        }
    }

    /**
     * 项目模型编码
     */
    @Value("${transcend.plm.apm.modelCode.project:}")
    public void setProjectModelCode(String modelCode) {
        if (StringUtils.isNotBlank(modelCode)) {
            PROJECT_MODEL_CODE = modelCode;
        }

    }

    /**
     * 产品需求模型编码
     */
    @Value("${transcend.plm.apm.modelCode.demand:}")
    public void setDemandModelCode(String modelCode) {
        if (StringUtils.isNotBlank(modelCode)) {
            DEMAND_MODEL_CODE = modelCode;
        }
    }

    /**
     * 项目需求关联模型编码
     */
    @Value("${transcend.plm.apm.modelCode.projectDemandRel:}")
    public void setProjectDemandRelModelCode(String modelCode) {
        if (StringUtils.isNotBlank(modelCode)) {
            PROJECT_DEMAND_REL_MODEL_CODE = modelCode;
        }
    }

    /**
     * 迭代模型编码
     */
    @Value("${transcend.plm.apm.modelCode.revision:}")
    public void setRevisionModelCode(String modelCode) {
        if (StringUtils.isNotBlank(modelCode)) {
            REVISION_MODEL_CODE = modelCode;
        }
    }

    /**
     * 迭代-产品需求关系模型编码
     */
    @Value("${transcend.plm.apm.modelCode.revisionDemandRel:}")
    public void setRevisionDemandRelModelCode(String modelCode) {
        if (StringUtils.isNotBlank(modelCode)) {
            REVISION_DEMAND_REL_MODEL_CODE = modelCode;
        }

    }

    /**
     * 任务模型编码
     */
    @Value("${transcend.plm.apm.modelCode.task:}")
    public void setTaskModelCode(String modelCode) {
        if (StringUtils.isNotBlank(modelCode)) {
            TASK_MODEL_CODE = modelCode;
        }

    }

    /**
     * 迭代-任务关系模型编码
     */
    @Value("${transcend.plm.apm.modelCode.revisionTaskRel:}")
    public void setRevisionTaskRelModelCode(String modelCode) {
        if (StringUtils.isNotBlank(modelCode)) {
            REVISION_TASK_REL_MODEL_CODE = modelCode;
        }

    }

    /**
     * 计划任务模型编码
     */
    @Value("${transcend.plm.apm.modelCode.planTask:}")
    public void setPlanTaskModelCode(String modelCode) {
        if (StringUtils.isNotBlank(modelCode)) {
            PLAN_TASK_MODEL_CODE = modelCode;
        }

    }

    /**
     * 战略供应商关系流程模型编码
     */
    @Value("${transcend.plm.apm.modelCode.ssrmp:}")
    public void setSsrmpModelCode(String modelCode) {
        if (StringUtils.isNotBlank(modelCode)) {
            SSRMP_MODEL_CODE = modelCode;
        }

    }

    /**
     * 项目和任务关系模型编码
     */
    @Value("${transcend.plm.apm.modelCode.projectTaskRel:}")
    public void setProjectTaskRelModelCode(String modelCode) {
        if (StringUtils.isNotBlank(modelCode)) {
            PROJECT_TASK_REL_MODEL_CODE = modelCode;
        }

    }


    /**
     * 移除从字典中获取的配置，便于使用动态配置
     */
    /*@PostConstruct
    public void init() throws NoSuchFieldException, IllegalAccessException {
        // 查询常用对象编码的字典
        Map<String, String> modelItemMap = defaultAppExcelTemplateService.getDictCodeAndEnNameMap(MODEL_CODE_DICT);
        if (modelItemMap != null) {
            for (Map.Entry<String, String> entry : modelItemMap.entrySet()) {
                if (StringUtils.isNotBlank(entry.getValue())) {
                    // 使用反射设置属性值
                    Field field = ModelCodeProperties.class.getDeclaredField(entry.getKey());
                    if (field != null) {
                        field.setAccessible(true);
                        // 访问静态字段，使用null作为实例
                        field.set(null, entry.getValue());
                        log.info("设置模型编码属性值：{}={}", entry.getKey(), entry.getValue());
                    }
                }
            }
        }
    }*/
}
